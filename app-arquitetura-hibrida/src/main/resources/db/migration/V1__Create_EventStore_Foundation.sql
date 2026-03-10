-- =====================================================
-- Migration V1: Fundação do Event Store
-- =====================================================
-- Autor: Principal Java Architect
-- Data: 2024-12-19
-- Versão: 2.0 (Consolidada)
-- Descrição: Criação consolidada da estrutura base do Event Store
--            com otimizações de performance e preparação para particionamento
-- 
-- Dependências: Nenhuma
-- Rollback: DROP SCHEMA eventstore CASCADE;
-- 
-- Validações de Pré-requisitos:
-- - PostgreSQL 12+ com extensão uuid-ossp
-- - Usuário com privilégios CREATE SCHEMA
-- =====================================================

-- Verificar pré-requisitos
DO $$
BEGIN
    -- Verificar versão do PostgreSQL
    IF current_setting('server_version_num')::integer < 120000 THEN
        RAISE EXCEPTION 'PostgreSQL 12+ é obrigatório. Versão atual: %', version();
    END IF;
    
    -- Verificar extensão uuid-ossp
    IF NOT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'uuid-ossp') THEN
        RAISE NOTICE 'Criando extensão uuid-ossp...';
        CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    END IF;
    
    RAISE NOTICE 'Pré-requisitos validados com sucesso';
END $$;

-- =====================================================
-- CRIAÇÃO DO SCHEMA E ESTRUTURAS BASE
-- =====================================================

-- Criar schema eventstore se não existir
CREATE SCHEMA IF NOT EXISTS eventstore;

-- Definir schema padrão para esta migration
SET search_path TO eventstore, public;

-- =====================================================
-- TABELA PRINCIPAL: events
-- Descrição: Armazena todos os eventos de domínio do sistema
-- Estratégia: Preparada para particionamento por timestamp
-- =====================================================

CREATE TABLE events (
    -- Identificação única
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- Identificação do aggregate
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    
    -- Identificação do evento
    event_type VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL,
    
    -- Timestamps
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Contexto e rastreabilidade
    correlation_id UUID,
    user_id VARCHAR(100),
    
    -- Dados do evento
    event_data JSONB NOT NULL,
    metadata JSONB,
    
    -- Otimizações de armazenamento
    compressed BOOLEAN NOT NULL DEFAULT FALSE,
    data_size INTEGER NOT NULL DEFAULT 0,
    
    -- Constraints
    CONSTRAINT chk_events_version_positive CHECK (version >= 0),
    CONSTRAINT chk_events_data_size_positive CHECK (data_size >= 0),
    CONSTRAINT chk_events_timestamp_valid CHECK (timestamp <= CURRENT_TIMESTAMP + INTERVAL '1 hour')
);

-- =====================================================
-- ÍNDICES OTIMIZADOS PARA PERFORMANCE
-- =====================================================

-- Índice único para garantir versões sequenciais por aggregate
CREATE UNIQUE INDEX idx_events_aggregate_version 
    ON events (aggregate_id, version);

-- Índices para consultas por aggregate
CREATE INDEX idx_events_aggregate_timestamp 
    ON events (aggregate_id, timestamp DESC);

CREATE INDEX idx_events_aggregate_recent 
    ON events (aggregate_id, timestamp DESC, version DESC)
    WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '30 days';

-- Índices para consultas por tipo de evento
CREATE INDEX idx_events_event_type_timestamp 
    ON events (event_type, timestamp DESC);

CREATE INDEX idx_events_aggregate_type_timestamp 
    ON events (aggregate_type, timestamp DESC);

-- Índices para rastreabilidade
CREATE INDEX idx_events_correlation_id 
    ON events (correlation_id) 
    WHERE correlation_id IS NOT NULL;

CREATE INDEX idx_events_user_id 
    ON events (user_id) 
    WHERE user_id IS NOT NULL;

-- Índices para consultas temporais
CREATE INDEX idx_events_timestamp 
    ON events (timestamp DESC);

CREATE INDEX idx_events_created_at 
    ON events (created_at DESC);

-- Índice para estatísticas e monitoramento
CREATE INDEX idx_events_stats_daily 
    ON events (DATE(timestamp), event_type, aggregate_type);

-- Índice GIN para consultas em metadados JSON
CREATE INDEX idx_events_metadata_gin 
    ON events USING GIN (metadata)
    WHERE metadata IS NOT NULL;

-- Índice para eventos comprimidos
CREATE INDEX idx_events_compressed 
    ON events (compressed, data_size) 
    WHERE compressed = TRUE;

-- =====================================================
-- TABELA: snapshots
-- Descrição: Armazena snapshots de aggregates para otimização
-- =====================================================

CREATE TABLE snapshots (
    -- Identificação
    snapshot_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    
    -- Versão e dados
    version BIGINT NOT NULL,
    snapshot_data JSONB NOT NULL,
    
    -- Timestamps
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Metadados e versionamento
    metadata JSONB,
    schema_version INTEGER NOT NULL DEFAULT 1,
    
    -- Compressão e otimização
    compressed BOOLEAN NOT NULL DEFAULT FALSE,
    original_size INTEGER,
    compressed_size INTEGER,
    compression_algorithm VARCHAR(20),
    
    -- Integridade
    data_hash VARCHAR(64),
    
    -- Auditoria
    created_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT chk_snapshots_version_positive CHECK (version >= 0),
    CONSTRAINT chk_snapshots_schema_version_positive CHECK (schema_version > 0),
    CONSTRAINT chk_snapshots_sizes_valid CHECK (
        (compressed = FALSE AND original_size IS NULL AND compressed_size IS NULL) OR
        (compressed = TRUE AND original_size IS NOT NULL AND compressed_size IS NOT NULL 
         AND compressed_size <= original_size)
    )
);

-- Índice único para snapshots por aggregate e versão
CREATE UNIQUE INDEX idx_snapshots_aggregate_version 
    ON snapshots (aggregate_id, version);

-- Índices para consultas otimizadas
CREATE INDEX idx_snapshots_aggregate_timestamp 
    ON snapshots (aggregate_id, timestamp DESC);

CREATE INDEX idx_snapshots_type_timestamp 
    ON snapshots (aggregate_type, timestamp DESC);

CREATE INDEX idx_snapshots_timestamp 
    ON snapshots (timestamp DESC);

-- Índice parcial para snapshots recentes
CREATE INDEX idx_snapshots_recent 
    ON snapshots (aggregate_id, version DESC) 
    WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '30 days';

-- Índice GIN para metadados
CREATE INDEX idx_snapshots_metadata_gin 
    ON snapshots USING GIN (metadata)
    WHERE metadata IS NOT NULL;

-- =====================================================
-- TABELA: projection_tracking
-- Descrição: Controla o progresso das projeções CQRS
-- =====================================================

CREATE TABLE projection_tracking (
    -- Identificação
    projection_name VARCHAR(100) PRIMARY KEY,
    
    -- Posição e progresso
    last_processed_event_id BIGINT NOT NULL DEFAULT 0,
    last_processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Status e controle
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Estatísticas
    events_processed BIGINT NOT NULL DEFAULT 0,
    events_failed BIGINT NOT NULL DEFAULT 0,
    
    -- Controle de erros
    last_error_message VARCHAR(1000),
    last_error_at TIMESTAMP WITH TIME ZONE,
    
    -- Auditoria
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Constraints
    CONSTRAINT chk_projection_tracking_status CHECK (
        status IN ('ACTIVE', 'PAUSED', 'ERROR', 'REBUILDING', 'DISABLED')
    ),
    CONSTRAINT chk_projection_tracking_events_positive CHECK (
        events_processed >= 0 AND events_failed >= 0
    )
);

-- Índices para consultas de tracking
CREATE INDEX idx_projection_tracking_status 
    ON projection_tracking (status);

CREATE INDEX idx_projection_tracking_last_processed 
    ON projection_tracking (last_processed_event_id);

CREATE INDEX idx_projection_tracking_updated_at 
    ON projection_tracking (updated_at DESC);

CREATE INDEX idx_projection_tracking_error_status 
    ON projection_tracking (status, last_error_at DESC) 
    WHERE status = 'ERROR';

-- =====================================================
-- FUNÇÕES AUXILIARES
-- =====================================================

-- Função para obter próximo ID de evento (para métricas)
CREATE OR REPLACE FUNCTION get_next_event_sequence()
RETURNS BIGINT AS $$
BEGIN
    -- Retorna um número sequencial baseado no timestamp
    -- Usado para ordenação e métricas, não como PK
    RETURN EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000000 + 
           EXTRACT(MICROSECONDS FROM CURRENT_TIMESTAMP);
END;
$$ LANGUAGE plpgsql;

-- Função para calcular estatísticas de eventos
CREATE OR REPLACE FUNCTION get_event_statistics(
    p_start_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_DATE,
    p_end_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_DATE + INTERVAL '1 day'
)
RETURNS TABLE (
    aggregate_type VARCHAR(100),
    event_type VARCHAR(100),
    event_count BIGINT,
    avg_data_size NUMERIC,
    total_data_size BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        e.aggregate_type,
        e.event_type,
        COUNT(*) as event_count,
        ROUND(AVG(e.data_size), 2) as avg_data_size,
        SUM(e.data_size) as total_data_size
    FROM events e
    WHERE e.timestamp BETWEEN p_start_date AND p_end_date
    GROUP BY e.aggregate_type, e.event_type
    ORDER BY event_count DESC;
END;
$$ LANGUAGE plpgsql;

-- Função para limpeza de snapshots antigos
CREATE OR REPLACE FUNCTION cleanup_old_snapshots(
    p_retention_days INTEGER DEFAULT 90,
    p_keep_latest_per_aggregate INTEGER DEFAULT 3
)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER := 0;
BEGIN
    -- Remove snapshots antigos mantendo os mais recentes por aggregate
    WITH snapshots_to_keep AS (
        SELECT snapshot_id
        FROM (
            SELECT 
                snapshot_id,
                ROW_NUMBER() OVER (
                    PARTITION BY aggregate_id 
                    ORDER BY version DESC, timestamp DESC
                ) as rn
            FROM snapshots
            WHERE timestamp >= CURRENT_TIMESTAMP - (p_retention_days || ' days')::INTERVAL
        ) ranked
        WHERE rn <= p_keep_latest_per_aggregate
    )
    DELETE FROM snapshots 
    WHERE snapshot_id NOT IN (SELECT snapshot_id FROM snapshots_to_keep)
    AND timestamp < CURRENT_TIMESTAMP - (p_retention_days || ' days')::INTERVAL;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    RAISE NOTICE 'Removidos % snapshots antigos', deleted_count;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- TRIGGERS PARA AUDITORIA E MANUTENÇÃO
-- =====================================================

-- Função para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para projection_tracking
CREATE TRIGGER trigger_projection_tracking_updated_at
    BEFORE UPDATE ON projection_tracking
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Função para validar versões sequenciais de eventos
CREATE OR REPLACE FUNCTION validate_event_version()
RETURNS TRIGGER AS $$
DECLARE
    expected_version BIGINT;
BEGIN
    -- Obter próxima versão esperada para o aggregate
    SELECT COALESCE(MAX(version), -1) + 1 
    INTO expected_version
    FROM events 
    WHERE aggregate_id = NEW.aggregate_id;
    
    -- Validar se a versão está correta
    IF NEW.version != expected_version THEN
        RAISE EXCEPTION 'Versão inválida para aggregate %. Esperado: %, Recebido: %', 
            NEW.aggregate_id, expected_version, NEW.version;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para validação de versões (pode ser desabilitado em alta carga)
CREATE TRIGGER trigger_validate_event_version
    BEFORE INSERT ON events
    FOR EACH ROW
    EXECUTE FUNCTION validate_event_version();

-- =====================================================
-- CONFIGURAÇÕES DE PERFORMANCE
-- =====================================================

-- Configurar autovacuum otimizado para tabelas de alta inserção
ALTER TABLE events SET (
    autovacuum_vacuum_scale_factor = 0.05,
    autovacuum_analyze_scale_factor = 0.02,
    autovacuum_vacuum_cost_delay = 5,
    fillfactor = 90
);

ALTER TABLE snapshots SET (
    autovacuum_vacuum_scale_factor = 0.1,
    autovacuum_analyze_scale_factor = 0.05,
    autovacuum_vacuum_cost_delay = 10,
    fillfactor = 85
);

-- =====================================================
-- INSERÇÃO DE DADOS INICIAIS
-- =====================================================

-- Inserir projeções iniciais no tracking
INSERT INTO projection_tracking (projection_name, status) VALUES
    ('SinistroProjection', 'ACTIVE'),
    ('SeguradoProjection', 'ACTIVE'),
    ('ApoliceProjection', 'ACTIVE'),
    ('VeiculoProjection', 'ACTIVE'),
    ('DetranConsultaProjection', 'ACTIVE'),
    ('EventTimelineProjection', 'ACTIVE'),
    ('MetricasAgregadasProjection', 'ACTIVE')
ON CONFLICT (projection_name) DO NOTHING;

-- =====================================================
-- COMENTÁRIOS PARA DOCUMENTAÇÃO
-- =====================================================

COMMENT ON SCHEMA eventstore IS 'Schema principal do Event Store - contém eventos, snapshots e controle de projeções';

COMMENT ON TABLE events IS 'Tabela principal do Event Store - armazena todos os eventos de domínio do sistema';
COMMENT ON COLUMN events.id IS 'Identificador único do evento (UUID v4)';
COMMENT ON COLUMN events.aggregate_id IS 'Identificador do aggregate que gerou o evento';
COMMENT ON COLUMN events.aggregate_type IS 'Tipo do aggregate (nome da classe)';
COMMENT ON COLUMN events.event_type IS 'Tipo do evento (nome da classe)';
COMMENT ON COLUMN events.version IS 'Versão sequencial do aggregate (0-based)';
COMMENT ON COLUMN events.timestamp IS 'Timestamp de ocorrência do evento no domínio';
COMMENT ON COLUMN events.created_at IS 'Timestamp de inserção no banco de dados';
COMMENT ON COLUMN events.correlation_id IS 'ID de correlação para rastreamento de fluxos';
COMMENT ON COLUMN events.user_id IS 'Identificador do usuário que originou o evento';
COMMENT ON COLUMN events.event_data IS 'Dados do evento serializados em JSON';
COMMENT ON COLUMN events.metadata IS 'Metadados adicionais (headers, contexto, etc.)';
COMMENT ON COLUMN events.compressed IS 'Indica se os dados estão comprimidos (para otimização)';
COMMENT ON COLUMN events.data_size IS 'Tamanho dos dados em bytes (para métricas)';

COMMENT ON TABLE snapshots IS 'Armazena snapshots de aggregates para otimização de reconstrução';
COMMENT ON COLUMN snapshots.snapshot_id IS 'Identificador único do snapshot (UUID v4)';
COMMENT ON COLUMN snapshots.aggregate_id IS 'Identificador do aggregate';
COMMENT ON COLUMN snapshots.version IS 'Versão do aggregate no momento do snapshot';
COMMENT ON COLUMN snapshots.snapshot_data IS 'Estado completo do aggregate serializado';
COMMENT ON COLUMN snapshots.schema_version IS 'Versão do schema para evolução de snapshots';
COMMENT ON COLUMN snapshots.data_hash IS 'Hash SHA-256 dos dados para verificação de integridade';

COMMENT ON TABLE projection_tracking IS 'Controla o progresso e status das projeções CQRS';
COMMENT ON COLUMN projection_tracking.projection_name IS 'Nome único da projeção';
COMMENT ON COLUMN projection_tracking.last_processed_event_id IS 'ID do último evento processado';
COMMENT ON COLUMN projection_tracking.status IS 'Status atual: ACTIVE, PAUSED, ERROR, REBUILDING, DISABLED';
COMMENT ON COLUMN projection_tracking.events_processed IS 'Total de eventos processados com sucesso';
COMMENT ON COLUMN projection_tracking.events_failed IS 'Total de eventos que falharam no processamento';

-- =====================================================
-- VALIDAÇÕES FINAIS
-- =====================================================

-- Executar análise inicial das tabelas
ANALYZE events;
ANALYZE snapshots;
ANALYZE projection_tracking;

-- Verificar integridade das estruturas criadas
DO $$
DECLARE
    table_count INTEGER;
    index_count INTEGER;
    function_count INTEGER;
BEGIN
    -- Contar tabelas criadas
    SELECT COUNT(*) INTO table_count
    FROM information_schema.tables 
    WHERE table_schema = 'eventstore' 
    AND table_type = 'BASE TABLE';
    
    -- Contar índices criados
    SELECT COUNT(*) INTO index_count
    FROM pg_indexes 
    WHERE schemaname = 'eventstore';
    
    -- Contar funções criadas
    SELECT COUNT(*) INTO function_count
    FROM information_schema.routines 
    WHERE routine_schema = 'eventstore';
    
    RAISE NOTICE 'Migration V1 concluída com sucesso:';
    RAISE NOTICE '- Tabelas criadas: %', table_count;
    RAISE NOTICE '- Índices criados: %', index_count;
    RAISE NOTICE '- Funções criadas: %', function_count;
    
    IF table_count < 3 THEN
        RAISE EXCEPTION 'Falha na criação das tabelas principais';
    END IF;
END $$;

-- =====================================================
-- INSTRUÇÕES DE ROLLBACK
-- =====================================================
/*
Para fazer rollback desta migration:

1. Parar a aplicação
2. Executar: DROP SCHEMA eventstore CASCADE;
3. Remover entrada da tabela flyway_schema_history
4. Reiniciar aplicação

ATENÇÃO: Isso removerá TODOS os dados do Event Store!
*/

-- =====================================================
-- FIM DA MIGRATION V1
-- =====================================================