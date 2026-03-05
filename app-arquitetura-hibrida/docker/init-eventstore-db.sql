-- Inicialização do Event Store para Arquitetura Híbrida
-- Banco otimizado para escrita de eventos (Command Side)

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Configurações otimizadas para Event Store (write-heavy)
ALTER SYSTEM SET synchronous_commit = 'on';
ALTER SYSTEM SET wal_sync_method = 'fsync';
ALTER SYSTEM SET full_page_writes = 'on';
ALTER SYSTEM SET wal_buffers = '32MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;

-- Configurações de performance para escritas
ALTER SYSTEM SET max_connections = 100;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET work_mem = '8MB';

-- Schema para Event Store
CREATE SCHEMA IF NOT EXISTS eventstore;

-- Tabela principal de eventos
CREATE TABLE IF NOT EXISTS eventstore.events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_version INTEGER NOT NULL,
    event_data JSONB NOT NULL,
    metadata JSONB,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Constraint para garantir ordem dos eventos por aggregate
    CONSTRAINT unique_aggregate_version UNIQUE (aggregate_id, event_version)
);

-- Tabela de snapshots para performance
CREATE TABLE IF NOT EXISTS eventstore.snapshots (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    version INTEGER NOT NULL,
    snapshot_data JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Constraint para garantir um snapshot por versão
    CONSTRAINT unique_aggregate_snapshot UNIQUE (aggregate_id, version)
);

-- Tabela de projeções de eventos (para tracking)
CREATE TABLE IF NOT EXISTS eventstore.projection_tracking (
    projection_name VARCHAR(100) PRIMARY KEY,
    last_processed_event_id BIGINT NOT NULL DEFAULT 0,
    last_processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0
);

-- Tabela de comandos (para auditoria)
CREATE TABLE IF NOT EXISTS eventstore.commands (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    command_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    command_data JSONB NOT NULL,
    metadata JSONB,
    issued_by VARCHAR(100),
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    result JSONB
);

-- Índices otimizados para Event Store
CREATE INDEX IF NOT EXISTS idx_events_aggregate_id ON eventstore.events(aggregate_id);
CREATE INDEX IF NOT EXISTS idx_events_aggregate_type ON eventstore.events(aggregate_type);
CREATE INDEX IF NOT EXISTS idx_events_event_type ON eventstore.events(event_type);
CREATE INDEX IF NOT EXISTS idx_events_occurred_at ON eventstore.events(occurred_at);
CREATE INDEX IF NOT EXISTS idx_events_aggregate_id_version ON eventstore.events(aggregate_id, event_version);

CREATE INDEX IF NOT EXISTS idx_snapshots_aggregate_id ON eventstore.snapshots(aggregate_id);
CREATE INDEX IF NOT EXISTS idx_snapshots_created_at ON eventstore.snapshots(created_at);

CREATE INDEX IF NOT EXISTS idx_commands_aggregate_id ON eventstore.commands(aggregate_id);
CREATE INDEX IF NOT EXISTS idx_commands_status ON eventstore.commands(status);
CREATE INDEX IF NOT EXISTS idx_commands_issued_at ON eventstore.commands(issued_at);

-- Função para obter próxima versão do evento
CREATE OR REPLACE FUNCTION eventstore.get_next_event_version(p_aggregate_id UUID)
RETURNS INTEGER AS $$
DECLARE
    next_version INTEGER;
BEGIN
    SELECT COALESCE(MAX(event_version), 0) + 1
    INTO next_version
    FROM eventstore.events
    WHERE aggregate_id = p_aggregate_id;
    
    RETURN next_version;
END;
$$ LANGUAGE plpgsql;

-- Função para criar snapshot automático
CREATE OR REPLACE FUNCTION eventstore.create_snapshot_if_needed()
RETURNS TRIGGER AS $$
DECLARE
    event_count INTEGER;
    snapshot_frequency INTEGER := 50; -- Criar snapshot a cada 50 eventos
BEGIN
    -- Contar eventos para este aggregate
    SELECT COUNT(*)
    INTO event_count
    FROM eventstore.events
    WHERE aggregate_id = NEW.aggregate_id;
    
    -- Criar snapshot se necessário
    IF event_count % snapshot_frequency = 0 THEN
        -- Aqui seria chamada a função da aplicação para criar snapshot
        -- Por enquanto, apenas log
        RAISE NOTICE 'Snapshot needed for aggregate % at version %', NEW.aggregate_id, NEW.event_version;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para snapshot automático
CREATE TRIGGER trigger_create_snapshot
    AFTER INSERT ON eventstore.events
    FOR EACH ROW
    EXECUTE FUNCTION eventstore.create_snapshot_if_needed();

-- View para estatísticas de eventos
CREATE OR REPLACE VIEW eventstore.event_statistics AS
SELECT 
    aggregate_type,
    event_type,
    COUNT(*) as event_count,
    MIN(occurred_at) as first_event,
    MAX(occurred_at) as last_event,
    COUNT(DISTINCT aggregate_id) as unique_aggregates
FROM eventstore.events
GROUP BY aggregate_type, event_type
ORDER BY event_count DESC;

-- View para status das projeções
CREATE OR REPLACE VIEW eventstore.projection_status AS
SELECT 
    pt.projection_name,
    pt.last_processed_event_id,
    pt.last_processed_at,
    pt.status,
    pt.error_message,
    pt.retry_count,
    COALESCE(e.max_event_id, 0) as max_available_event_id,
    COALESCE(e.max_event_id, 0) - pt.last_processed_event_id as events_behind
FROM eventstore.projection_tracking pt
LEFT JOIN (
    SELECT MAX(id) as max_event_id FROM eventstore.events
) e ON true;

-- Inserir projeções iniciais
INSERT INTO eventstore.projection_tracking (projection_name, status) VALUES
('sinistro-projection', 'ACTIVE'),
('detran-integration-projection', 'ACTIVE'),
('notification-projection', 'ACTIVE')
ON CONFLICT (projection_name) DO NOTHING;

-- Log de inicialização
DO $$
BEGIN
    RAISE NOTICE 'Event Store initialized for Arquitetura Híbrida at %', NOW();
END $$;