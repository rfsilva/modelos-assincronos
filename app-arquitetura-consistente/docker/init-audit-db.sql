-- Inicialização do banco de auditoria para Arquitetura Consistente
-- Banco separado para logs de auditoria e performance

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
CREATE EXTENSION IF NOT EXISTS "timescaledb" CASCADE;

-- Configurações otimizadas para logs (write-heavy)
ALTER SYSTEM SET synchronous_commit = 'off';
ALTER SYSTEM SET wal_buffers = '32MB';
ALTER SYSTEM SET checkpoint_segments = 32;
ALTER SYSTEM SET checkpoint_completion_target = 0.9;

-- Configurações de performance para logs
ALTER SYSTEM SET max_connections = 50;
ALTER SYSTEM SET shared_buffers = '128MB';
ALTER SYSTEM SET effective_cache_size = '512MB';
ALTER SYSTEM SET maintenance_work_mem = '32MB';

-- Schema para logs detalhados
CREATE SCHEMA IF NOT EXISTS logs;

-- Tabela de logs de saga
CREATE TABLE IF NOT EXISTS logs.saga_execution_log (
    id BIGSERIAL PRIMARY KEY,
    saga_id UUID NOT NULL,
    saga_type VARCHAR(100) NOT NULL,
    step_name VARCHAR(100),
    step_order INTEGER,
    status VARCHAR(50) NOT NULL,
    input_data JSONB,
    output_data JSONB,
    error_message TEXT,
    execution_time_ms BIGINT,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Converter para hypertable (TimescaleDB) se disponível
SELECT create_hypertable('logs.saga_execution_log', 'created_at', if_not_exists => TRUE);

-- Tabela de logs de transações
CREATE TABLE IF NOT EXISTS logs.transaction_log (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    table_name VARCHAR(100),
    record_id VARCHAR(100),
    before_data JSONB,
    after_data JSONB,
    user_id VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Converter para hypertable (TimescaleDB) se disponível
SELECT create_hypertable('logs.transaction_log', 'created_at', if_not_exists => TRUE);

-- Tabela de métricas de performance
CREATE TABLE IF NOT EXISTS logs.performance_metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_value NUMERIC NOT NULL,
    tags JSONB,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Converter para hypertable (TimescaleDB) se disponível
SELECT create_hypertable('logs.performance_metrics', 'recorded_at', if_not_exists => TRUE);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_saga_log_saga_id ON logs.saga_execution_log(saga_id);
CREATE INDEX IF NOT EXISTS idx_saga_log_status ON logs.saga_execution_log(status);
CREATE INDEX IF NOT EXISTS idx_saga_log_created_at ON logs.saga_execution_log(created_at);

CREATE INDEX IF NOT EXISTS idx_transaction_log_transaction_id ON logs.transaction_log(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaction_log_table_name ON logs.transaction_log(table_name);
CREATE INDEX IF NOT EXISTS idx_transaction_log_created_at ON logs.transaction_log(created_at);

CREATE INDEX IF NOT EXISTS idx_performance_metrics_name ON logs.performance_metrics(metric_name);
CREATE INDEX IF NOT EXISTS idx_performance_metrics_recorded_at ON logs.performance_metrics(recorded_at);

-- Política de retenção (manter logs por 1 ano)
SELECT add_retention_policy('logs.saga_execution_log', INTERVAL '1 year', if_not_exists => TRUE);
SELECT add_retention_policy('logs.transaction_log', INTERVAL '1 year', if_not_exists => TRUE);
SELECT add_retention_policy('logs.performance_metrics', INTERVAL '6 months', if_not_exists => TRUE);

-- Views para relatórios
CREATE OR REPLACE VIEW logs.saga_summary AS
SELECT 
    saga_type,
    status,
    COUNT(*) as total_executions,
    AVG(execution_time_ms) as avg_execution_time_ms,
    MIN(execution_time_ms) as min_execution_time_ms,
    MAX(execution_time_ms) as max_execution_time_ms,
    DATE_TRUNC('day', created_at) as execution_date
FROM logs.saga_execution_log
WHERE created_at >= NOW() - INTERVAL '30 days'
GROUP BY saga_type, status, DATE_TRUNC('day', created_at)
ORDER BY execution_date DESC, saga_type, status;

-- Log de inicialização
DO $$
BEGIN
    RAISE NOTICE 'Audit database initialized for Arquitetura Consistente at %', NOW();
END $$;