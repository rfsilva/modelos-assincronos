-- =====================================================
-- Migration V1: Criação do Schema de Projections
-- =====================================================
-- Autor: Principal Java Architect
-- Data: 2024-12-19
-- Descrição: Criação das tabelas para o Query Side (CQRS)
-- =====================================================

-- Criar schema se não existir
CREATE SCHEMA IF NOT EXISTS projections;

-- Definir schema padrão
SET search_path TO projections;

-- =====================================================
-- TABELA: sinistro_view
-- Descrição: Projeção desnormalizada de sinistros
-- =====================================================
CREATE TABLE sinistro_view (
    id UUID PRIMARY KEY,
    protocolo VARCHAR(20) NOT NULL UNIQUE,
    
    -- Dados do Segurado
    cpf_segurado VARCHAR(11) NOT NULL,
    nome_segurado VARCHAR(200) NOT NULL,
    email_segurado VARCHAR(100),
    telefone_segurado VARCHAR(20),
    
    -- Dados do Veículo
    placa VARCHAR(8) NOT NULL,
    renavam VARCHAR(11),
    chassi VARCHAR(17),
    marca VARCHAR(50),
    modelo VARCHAR(100),
    ano_fabricacao INTEGER,
    ano_modelo INTEGER,
    cor VARCHAR(30),
    
    -- Dados da Apólice
    apolice_numero VARCHAR(20) NOT NULL,
    apolice_vigencia_inicio DATE,
    apolice_vigencia_fim DATE,
    apolice_valor_segurado DECIMAL(15,2),
    
    -- Dados do Sinistro
    tipo_sinistro VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    data_ocorrencia TIMESTAMP NOT NULL,
    data_abertura TIMESTAMP NOT NULL,
    data_fechamento TIMESTAMP,
    operador_responsavel VARCHAR(100),
    descricao TEXT,
    valor_estimado DECIMAL(15,2),
    valor_franquia DECIMAL(15,2),
    
    -- Dados do DETRAN (JSON)
    dados_detran JSONB,
    consulta_detran_realizada BOOLEAN DEFAULT FALSE,
    consulta_detran_timestamp TIMESTAMP,
    consulta_detran_status VARCHAR(20),
    
    -- Localização
    cep_ocorrencia VARCHAR(8),
    endereco_ocorrencia TEXT,
    cidade_ocorrencia VARCHAR(100),
    estado_ocorrencia VARCHAR(2),
    
    -- Metadados
    tags TEXT[], -- Array de tags para busca
    prioridade VARCHAR(10) DEFAULT 'NORMAL',
    canal_abertura VARCHAR(30),
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_event_id BIGINT, -- ID do último evento processado
    version BIGINT NOT NULL DEFAULT 1
);

-- =====================================================
-- TABELA: detran_consulta_view
-- Descrição: Histórico de consultas ao DETRAN
-- =====================================================
CREATE TABLE detran_consulta_view (
    id UUID PRIMARY KEY,
    sinistro_id UUID NOT NULL,
    placa VARCHAR(8) NOT NULL,
    renavam VARCHAR(11) NOT NULL,
    
    -- Dados da Consulta
    consulta_timestamp TIMESTAMP NOT NULL,
    consulta_status VARCHAR(20) NOT NULL, -- SUCCESS, ERROR, TIMEOUT
    response_time_ms INTEGER,
    
    -- Dados Retornados
    veiculo_situacao VARCHAR(50),
    veiculo_restricoes TEXT,
    veiculo_impedimentos TEXT,
    
    -- Débitos e Multas (JSON para flexibilidade)
    debitos JSONB,
    multas JSONB,
    infracoes JSONB,
    
    -- Metadados
    user_agent VARCHAR(500),
    client_ip VARCHAR(45),
    correlation_id UUID,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key
    CONSTRAINT fk_detran_consulta_sinistro 
        FOREIGN KEY (sinistro_id) REFERENCES sinistro_view(id)
);

-- =====================================================
-- TABELA: event_timeline_view
-- Descrição: Timeline de eventos para auditoria
-- =====================================================
CREATE TABLE event_timeline_view (
    id UUID PRIMARY KEY,
    aggregate_id VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    
    -- Dados do Evento
    event_data JSONB NOT NULL,
    event_metadata JSONB,
    
    -- Contexto
    user_id VARCHAR(100),
    correlation_id UUID,
    
    -- Classificação
    category VARCHAR(30), -- BUSINESS, INTEGRATION, SYSTEM
    severity VARCHAR(10), -- INFO, WARN, ERROR
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TABELA: metricas_agregadas_view
-- Descrição: Métricas pré-calculadas para dashboard
-- =====================================================
CREATE TABLE metricas_agregadas_view (
    id UUID PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_type VARCHAR(20) NOT NULL, -- COUNTER, GAUGE, HISTOGRAM
    
    -- Dimensões
    period_type VARCHAR(10) NOT NULL, -- HOUR, DAY, WEEK, MONTH
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    
    -- Valores
    metric_value DECIMAL(20,4) NOT NULL,
    metric_count BIGINT DEFAULT 0,
    
    -- Metadados
    dimensions JSONB, -- Dimensões adicionais (status, tipo, etc.)
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraint de unicidade
    CONSTRAINT uk_metricas_agregadas 
        UNIQUE (metric_name, period_type, period_start, dimensions)
);

-- =====================================================
-- ÍNDICES PARA PERFORMANCE
-- =====================================================

-- Índices para sinistro_view
CREATE INDEX idx_sinistro_view_protocolo ON sinistro_view(protocolo);
CREATE INDEX idx_sinistro_view_cpf_segurado ON sinistro_view(cpf_segurado);
CREATE INDEX idx_sinistro_view_placa ON sinistro_view(placa);
CREATE INDEX idx_sinistro_view_apolice ON sinistro_view(apolice_numero);
CREATE INDEX idx_sinistro_view_status ON sinistro_view(status);
CREATE INDEX idx_sinistro_view_tipo ON sinistro_view(tipo_sinistro);
CREATE INDEX idx_sinistro_view_data_ocorrencia ON sinistro_view(data_ocorrencia);
CREATE INDEX idx_sinistro_view_data_abertura ON sinistro_view(data_abertura);
CREATE INDEX idx_sinistro_view_operador ON sinistro_view(operador_responsavel);
CREATE INDEX idx_sinistro_view_updated_at ON sinistro_view(updated_at);
CREATE INDEX idx_sinistro_view_tags ON sinistro_view USING GIN(tags);

-- Índice composto para consultas frequentes
CREATE INDEX idx_sinistro_view_status_data ON sinistro_view(status, data_abertura DESC);
CREATE INDEX idx_sinistro_view_segurado_status ON sinistro_view(cpf_segurado, status);

-- Índice para full-text search
CREATE INDEX idx_sinistro_view_fulltext ON sinistro_view 
    USING GIN(to_tsvector('portuguese', 
        COALESCE(protocolo, '') || ' ' || 
        COALESCE(nome_segurado, '') || ' ' || 
        COALESCE(descricao, '') || ' ' ||
        COALESCE(placa, '')
    ));

-- Índices para detran_consulta_view
CREATE INDEX idx_detran_consulta_sinistro_id ON detran_consulta_view(sinistro_id);
CREATE INDEX idx_detran_consulta_placa ON detran_consulta_view(placa);
CREATE INDEX idx_detran_consulta_timestamp ON detran_consulta_view(consulta_timestamp);
CREATE INDEX idx_detran_consulta_status ON detran_consulta_view(consulta_status);

-- Índices para event_timeline_view
CREATE INDEX idx_event_timeline_aggregate_id ON event_timeline_view(aggregate_id);
CREATE INDEX idx_event_timeline_aggregate_type ON event_timeline_view(aggregate_type);
CREATE INDEX idx_event_timeline_event_type ON event_timeline_view(event_type);
CREATE INDEX idx_event_timeline_timestamp ON event_timeline_view(event_timestamp);
CREATE INDEX idx_event_timeline_category ON event_timeline_view(category);
CREATE INDEX idx_event_timeline_user_id ON event_timeline_view(user_id);
CREATE INDEX idx_event_timeline_correlation_id ON event_timeline_view(correlation_id);

-- Índice composto para consultas de timeline
CREATE INDEX idx_event_timeline_aggregate_timestamp ON event_timeline_view(aggregate_id, event_timestamp DESC);

-- Índices para metricas_agregadas_view
CREATE INDEX idx_metricas_agregadas_name ON metricas_agregadas_view(metric_name);
CREATE INDEX idx_metricas_agregadas_period ON metricas_agregadas_view(period_type, period_start);
CREATE INDEX idx_metricas_agregadas_updated ON metricas_agregadas_view(updated_at);

-- Índice GIN para dimensões JSON
CREATE INDEX idx_metricas_agregadas_dimensions ON metricas_agregadas_view USING GIN(dimensions);

-- =====================================================
-- TRIGGERS PARA AUDITORIA
-- =====================================================

-- Função para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger para sinistro_view
CREATE TRIGGER trigger_sinistro_view_updated_at
    BEFORE UPDATE ON sinistro_view
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger para metricas_agregadas_view
CREATE TRIGGER trigger_metricas_agregadas_updated_at
    BEFORE UPDATE ON metricas_agregadas_view
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- COMENTÁRIOS PARA DOCUMENTAÇÃO
-- =====================================================

COMMENT ON SCHEMA projections IS 'Schema para projeções do Query Side (CQRS)';

COMMENT ON TABLE sinistro_view IS 'Projeção desnormalizada de sinistros para consultas otimizadas';
COMMENT ON COLUMN sinistro_view.dados_detran IS 'Dados completos retornados pela consulta ao DETRAN em formato JSON';
COMMENT ON COLUMN sinistro_view.tags IS 'Array de tags para categorização e busca';
COMMENT ON COLUMN sinistro_view.last_event_id IS 'ID do último evento processado para esta projeção';

COMMENT ON TABLE detran_consulta_view IS 'Histórico de todas as consultas realizadas ao DETRAN';
COMMENT ON COLUMN detran_consulta_view.debitos IS 'Lista de débitos do veículo em formato JSON';
COMMENT ON COLUMN detran_consulta_view.multas IS 'Lista de multas do veículo em formato JSON';

COMMENT ON TABLE event_timeline_view IS 'Timeline de eventos para auditoria e rastreabilidade';
COMMENT ON COLUMN event_timeline_view.category IS 'Categoria do evento: BUSINESS, INTEGRATION, SYSTEM';
COMMENT ON COLUMN event_timeline_view.severity IS 'Severidade do evento: INFO, WARN, ERROR';

COMMENT ON TABLE metricas_agregadas_view IS 'Métricas pré-calculadas para dashboard e relatórios';
COMMENT ON COLUMN metricas_agregadas_view.dimensions IS 'Dimensões adicionais da métrica em formato JSON';

-- =====================================================
-- DADOS INICIAIS (se necessário)
-- =====================================================

-- Inserir métricas iniciais para dashboard
INSERT INTO metricas_agregadas_view (id, metric_name, metric_type, period_type, period_start, period_end, metric_value, dimensions)
VALUES 
    (gen_random_uuid(), 'sinistros_total', 'COUNTER', 'DAY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 day', 0, '{}'),
    (gen_random_uuid(), 'sinistros_abertos', 'GAUGE', 'DAY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 day', 0, '{"status": "ABERTO"}'),
    (gen_random_uuid(), 'consultas_detran_total', 'COUNTER', 'DAY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 day', 0, '{}');

-- =====================================================
-- GRANTS E PERMISSÕES
-- =====================================================

-- Conceder permissões ao usuário da aplicação (se necessário)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA projections TO app_user;
-- GRANT USAGE ON SCHEMA projections TO app_user;

-- =====================================================
-- FIM DA MIGRATION V1
-- =====================================================