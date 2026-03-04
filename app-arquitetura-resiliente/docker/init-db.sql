-- Inicialização do banco de dados para Arquitetura Resiliente
-- Este script é executado automaticamente na criação do container PostgreSQL

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Configurações de performance
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;

-- Criar usuário específico para a aplicação (opcional)
-- CREATE USER resiliente_app WITH PASSWORD 'resiliente_pass';
-- GRANT ALL PRIVILEGES ON DATABASE sinistros_resiliente TO resiliente_app;

-- Log de inicialização
INSERT INTO pg_stat_statements_info VALUES ('Database initialized for Arquitetura Resiliente at ' || NOW());