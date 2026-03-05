-- Migration V1: Criação da tabela de eventos do Event Store
-- Implementa estrutura otimizada para Event Sourcing com PostgreSQL

-- Criação do schema eventstore se não existir
CREATE SCHEMA IF NOT EXISTS eventstore;

-- Criação da tabela principal de eventos
CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id UUID,
    user_id VARCHAR(100),
    event_data JSONB NOT NULL,
    metadata JSONB,
    compressed BOOLEAN NOT NULL DEFAULT FALSE,
    data_size INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para otimização de consultas
CREATE UNIQUE INDEX IF NOT EXISTS idx_aggregate_version 
    ON events (aggregate_id, version);

CREATE INDEX IF NOT EXISTS idx_aggregate_timestamp 
    ON events (aggregate_id, timestamp);

CREATE INDEX IF NOT EXISTS idx_event_type_timestamp 
    ON events (event_type, timestamp);

CREATE INDEX IF NOT EXISTS idx_correlation_id 
    ON events (correlation_id) WHERE correlation_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_timestamp 
    ON events (timestamp);

CREATE INDEX IF NOT EXISTS idx_aggregate_type 
    ON events (aggregate_type);

CREATE INDEX IF NOT EXISTS idx_user_id 
    ON events (user_id) WHERE user_id IS NOT NULL;

-- Índice para consultas de eventos recentes por aggregate
CREATE INDEX IF NOT EXISTS idx_aggregate_recent 
    ON events (aggregate_id, timestamp DESC, version DESC);

-- Índice para estatísticas e monitoramento
CREATE INDEX IF NOT EXISTS idx_stats_daily 
    ON events (DATE(timestamp), event_type);

-- Constraint para garantir versões sequenciais por aggregate
-- (será implementada via trigger para melhor performance)

-- Comentários para documentação
COMMENT ON TABLE events IS 'Tabela principal do Event Store - armazena todos os eventos de domínio';
COMMENT ON COLUMN events.id IS 'ID único do evento (UUID)';
COMMENT ON COLUMN events.aggregate_id IS 'ID do aggregate que gerou o evento';
COMMENT ON COLUMN events.aggregate_type IS 'Tipo do aggregate (classe)';
COMMENT ON COLUMN events.event_type IS 'Tipo do evento (classe)';
COMMENT ON COLUMN events.version IS 'Versão do aggregate quando o evento foi gerado';
COMMENT ON COLUMN events.timestamp IS 'Timestamp de criação do evento';
COMMENT ON COLUMN events.correlation_id IS 'ID de correlação para rastreamento';
COMMENT ON COLUMN events.user_id IS 'ID do usuário que originou o evento';
COMMENT ON COLUMN events.event_data IS 'Dados do evento em formato JSON';
COMMENT ON COLUMN events.metadata IS 'Metadados adicionais do evento';
COMMENT ON COLUMN events.compressed IS 'Indica se os dados estão comprimidos';
COMMENT ON COLUMN events.data_size IS 'Tamanho dos dados em bytes';
COMMENT ON COLUMN events.created_at IS 'Timestamp de inserção no banco';

-- Configuração de particionamento por data (mensal)
-- Será implementado em migration separada para não impactar esta versão inicial