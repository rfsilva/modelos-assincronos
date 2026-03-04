-- Inicialização do banco de Projections para Arquitetura Híbrida
-- Banco otimizado para leitura (Query Side)

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- Configurações otimizadas para leitura
ALTER SYSTEM SET synchronous_commit = 'off';
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;

-- Configurações de performance para leituras
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET work_mem = '4MB';

-- Configurações de estatísticas para otimização de queries
ALTER SYSTEM SET default_statistics_target = 100;
ALTER SYSTEM SET track_activities = 'on';
ALTER SYSTEM SET track_counts = 'on';
ALTER SYSTEM SET track_io_timing = 'on';

-- Schema para projeções
CREATE SCHEMA IF NOT EXISTS projections;

-- Tabela de projeção de sinistros (view otimizada)
CREATE TABLE IF NOT EXISTS projections.sinistro_view (
    id UUID PRIMARY KEY,
    protocolo VARCHAR(50) NOT NULL UNIQUE,
    cpf_segurado VARCHAR(11) NOT NULL,
    nome_segurado VARCHAR(200),
    placa VARCHAR(8) NOT NULL,
    renavam VARCHAR(11) NOT NULL,
    descricao TEXT,
    status VARCHAR(50) NOT NULL,
    valor_estimado DECIMAL(15,2),
    data_abertura TIMESTAMP WITH TIME ZONE NOT NULL,
    data_ultima_atualizacao TIMESTAMP WITH TIME ZONE NOT NULL,
    consulta_detran_status VARCHAR(50),
    dados_detran JSONB,
    historico_status JSONB,
    tags TEXT[],
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Tabela de projeção de consultas Detran
CREATE TABLE IF NOT EXISTS projections.detran_consulta_view (
    id UUID PRIMARY KEY,
    sinistro_id UUID NOT NULL,
    placa VARCHAR(8) NOT NULL,
    renavam VARCHAR(11) NOT NULL,
    status VARCHAR(50) NOT NULL,
    tentativa INTEGER NOT NULL DEFAULT 1,
    dados_retornados JSONB,
    erro_message TEXT,
    tempo_resposta_ms INTEGER,
    data_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    data_conclusao TIMESTAMP WITH TIME ZONE,
    origem VARCHAR(50), -- CACHE, DETRAN, FALLBACK
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Tabela de projeção de timeline de eventos
CREATE TABLE IF NOT EXISTS projections.evento_timeline (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_version INTEGER NOT NULL,
    event_summary TEXT NOT NULL,
    event_details JSONB,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Tabela de métricas agregadas
CREATE TABLE IF NOT EXISTS projections.metricas_agregadas (
    id BIGSERIAL PRIMARY KEY,
    metrica_tipo VARCHAR(100) NOT NULL,
    periodo VARCHAR(20) NOT NULL, -- HOUR, DAY, WEEK, MONTH
    periodo_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    periodo_fim TIMESTAMP WITH TIME ZONE NOT NULL,
    valores JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT unique_metrica_periodo UNIQUE (metrica_tipo, periodo, periodo_inicio)
);

-- Índices otimizados para consultas
CREATE INDEX IF NOT EXISTS idx_sinistro_view_cpf ON projections.sinistro_view(cpf_segurado);
CREATE INDEX IF NOT EXISTS idx_sinistro_view_placa ON projections.sinistro_view(placa);
CREATE INDEX IF NOT EXISTS idx_sinistro_view_status ON projections.sinistro_view(status);
CREATE INDEX IF NOT EXISTS idx_sinistro_view_data_abertura ON projections.sinistro_view(data_abertura);
CREATE INDEX IF NOT EXISTS idx_sinistro_view_protocolo ON projections.sinistro_view(protocolo);
CREATE INDEX IF NOT EXISTS idx_sinistro_view_tags ON projections.sinistro_view USING GIN(tags);
CREATE INDEX IF NOT EXISTS idx_sinistro_view_dados_detran ON projections.sinistro_view USING GIN(dados_detran);

CREATE INDEX IF NOT EXISTS idx_detran_consulta_sinistro_id ON projections.detran_consulta_view(sinistro_id);
CREATE INDEX IF NOT EXISTS idx_detran_consulta_placa ON projections.detran_consulta_view(placa);
CREATE INDEX IF NOT EXISTS idx_detran_consulta_status ON projections.detran_consulta_view(status);
CREATE INDEX IF NOT EXISTS idx_detran_consulta_data_inicio ON projections.detran_consulta_view(data_inicio);

CREATE INDEX IF NOT EXISTS idx_evento_timeline_aggregate_id ON projections.evento_timeline(aggregate_id);
CREATE INDEX IF NOT EXISTS idx_evento_timeline_aggregate_type ON projections.evento_timeline(aggregate_type);
CREATE INDEX IF NOT EXISTS idx_evento_timeline_event_type ON projections.evento_timeline(event_type);
CREATE INDEX IF NOT EXISTS idx_evento_timeline_occurred_at ON projections.evento_timeline(occurred_at);

CREATE INDEX IF NOT EXISTS idx_metricas_tipo_periodo ON projections.metricas_agregadas(metrica_tipo, periodo);
CREATE INDEX IF NOT EXISTS idx_metricas_periodo_inicio ON projections.metricas_agregadas(periodo_inicio);

-- Views para relatórios comuns
CREATE OR REPLACE VIEW projections.sinistros_dashboard AS
SELECT 
    status,
    COUNT(*) as total,
    AVG(valor_estimado) as valor_medio,
    MIN(data_abertura) as mais_antigo,
    MAX(data_abertura) as mais_recente
FROM projections.sinistro_view
WHERE data_abertura >= NOW() - INTERVAL '30 days'
GROUP BY status
ORDER BY total DESC;

CREATE OR REPLACE VIEW projections.detran_performance AS
SELECT 
    DATE_TRUNC('hour', data_inicio) as hora,
    status,
    COUNT(*) as total_consultas,
    AVG(tempo_resposta_ms) as tempo_medio_ms,
    MIN(tempo_resposta_ms) as tempo_min_ms,
    MAX(tempo_resposta_ms) as tempo_max_ms,
    COUNT(*) FILTER (WHERE origem = 'CACHE') as cache_hits,
    COUNT(*) FILTER (WHERE origem = 'DETRAN') as detran_calls
FROM projections.detran_consulta_view
WHERE data_inicio >= NOW() - INTERVAL '24 hours'
GROUP BY DATE_TRUNC('hour', data_inicio), status
ORDER BY hora DESC, status;

-- Função para busca full-text em sinistros
CREATE OR REPLACE FUNCTION projections.buscar_sinistros(
    p_termo TEXT,
    p_limite INTEGER DEFAULT 50
)
RETURNS TABLE (
    id UUID,
    protocolo VARCHAR(50),
    cpf_segurado VARCHAR(11),
    nome_segurado VARCHAR(200),
    placa VARCHAR(8),
    status VARCHAR(50),
    relevancia REAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        sv.id,
        sv.protocolo,
        sv.cpf_segurado,
        sv.nome_segurado,
        sv.placa,
        sv.status,
        ts_rank(
            to_tsvector('portuguese', 
                COALESCE(sv.protocolo, '') || ' ' ||
                COALESCE(sv.nome_segurado, '') || ' ' ||
                COALESCE(sv.placa, '') || ' ' ||
                COALESCE(sv.descricao, '')
            ),
            plainto_tsquery('portuguese', p_termo)
        ) as relevancia
    FROM projections.sinistro_view sv
    WHERE to_tsvector('portuguese', 
        COALESCE(sv.protocolo, '') || ' ' ||
        COALESCE(sv.nome_segurado, '') || ' ' ||
        COALESCE(sv.placa, '') || ' ' ||
        COALESCE(sv.descricao, '')
    ) @@ plainto_tsquery('portuguese', p_termo)
    ORDER BY relevancia DESC, sv.data_abertura DESC
    LIMIT p_limite;
END;
$$ LANGUAGE plpgsql;

-- Função para atualizar timestamp automaticamente
CREATE OR REPLACE FUNCTION projections.update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para atualização automática de timestamp
CREATE TRIGGER trigger_sinistro_view_updated_at
    BEFORE UPDATE ON projections.sinistro_view
    FOR EACH ROW
    EXECUTE FUNCTION projections.update_timestamp();

CREATE TRIGGER trigger_detran_consulta_view_updated_at
    BEFORE UPDATE ON projections.detran_consulta_view
    FOR EACH ROW
    EXECUTE FUNCTION projections.update_timestamp();

-- Função para limpeza de dados antigos
CREATE OR REPLACE FUNCTION projections.cleanup_old_data()
RETURNS void AS $$
BEGIN
    -- Limpar eventos de timeline mais antigos que 1 ano
    DELETE FROM projections.evento_timeline
    WHERE occurred_at < NOW() - INTERVAL '1 year';
    
    -- Limpar métricas agregadas mais antigas que 2 anos
    DELETE FROM projections.metricas_agregadas
    WHERE periodo_inicio < NOW() - INTERVAL '2 years';
    
    -- Log da limpeza
    RAISE NOTICE 'Cleanup completed at %', NOW();
END;
$$ LANGUAGE plpgsql;

-- Log de inicialização
DO $$
BEGIN
    RAISE NOTICE 'Projections database initialized for Arquitetura Híbrida at %', NOW();
END $$;