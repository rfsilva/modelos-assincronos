-- Migration V4: Implementação de Particionamento Automático por Mês
-- US007 - Event Store com Particionamento e Arquivamento

-- Função para criar partições automaticamente
CREATE OR REPLACE FUNCTION create_monthly_partition(table_name text, start_date date)
RETURNS void AS $$
DECLARE
    partition_name text;
    end_date date;
BEGIN
    -- Calcular nome da partição e data final
    partition_name := table_name || '_' || to_char(start_date, 'YYYY_MM');
    end_date := start_date + interval '1 month';
    
    -- Criar partição se não existir
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I PARTITION OF %I
        FOR VALUES FROM (%L) TO (%L)',
        partition_name, table_name, start_date, end_date);
    
    -- Criar índices na nova partição
    EXECUTE format('
        CREATE INDEX IF NOT EXISTS %I ON %I (aggregate_id, version)',
        partition_name || '_aggregate_version_idx', partition_name);
    
    EXECUTE format('
        CREATE INDEX IF NOT EXISTS %I ON %I (aggregate_id, timestamp)',
        partition_name || '_aggregate_timestamp_idx', partition_name);
    
    EXECUTE format('
        CREATE INDEX IF NOT EXISTS %I ON %I (event_type, timestamp)',
        partition_name || '_event_type_timestamp_idx', partition_name);
    
    EXECUTE format('
        CREATE INDEX IF NOT EXISTS %I ON %I (correlation_id) WHERE correlation_id IS NOT NULL',
        partition_name || '_correlation_id_idx', partition_name);
    
    RAISE NOTICE 'Partição % criada com sucesso', partition_name;
END;
$$ LANGUAGE plpgsql;

-- Função para manutenção automática de partições
CREATE OR REPLACE FUNCTION maintain_event_partitions()
RETURNS void AS $$
DECLARE
    current_month date;
    future_month date;
    i integer;
BEGIN
    -- Criar partições para os próximos 3 meses
    current_month := date_trunc('month', CURRENT_DATE);
    
    FOR i IN 0..2 LOOP
        future_month := current_month + (i || ' months')::interval;
        PERFORM create_monthly_partition('events', future_month);
    END LOOP;
    
    RAISE NOTICE 'Manutenção de partições concluída';
END;
$$ LANGUAGE plpgsql;

-- Converter tabela existente para particionada
-- ATENÇÃO: Esta operação pode ser demorada em tabelas grandes

-- 1. Renomear tabela atual
ALTER TABLE events RENAME TO events_legacy;

-- 2. Criar nova tabela particionada
CREATE TABLE events (
    id UUID DEFAULT gen_random_uuid(),
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
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraint de particionamento
    CONSTRAINT events_pkey PRIMARY KEY (id, timestamp)
) PARTITION BY RANGE (timestamp);

-- 3. Criar partições para dados históricos e futuros
DO $$
DECLARE
    min_date date;
    max_date date;
    current_month date;
BEGIN
    -- Verificar se existem dados na tabela legacy
    SELECT 
        COALESCE(date_trunc('month', MIN(timestamp)), date_trunc('month', CURRENT_DATE)),
        COALESCE(date_trunc('month', MAX(timestamp)), date_trunc('month', CURRENT_DATE))
    INTO min_date, max_date
    FROM events_legacy;
    
    -- Criar partições para dados históricos
    current_month := min_date;
    WHILE current_month <= max_date + interval '3 months' LOOP
        PERFORM create_monthly_partition('events', current_month);
        current_month := current_month + interval '1 month';
    END LOOP;
END $$;

-- 4. Migrar dados da tabela legacy (se existirem)
INSERT INTO events 
SELECT * FROM events_legacy 
WHERE EXISTS (SELECT 1 FROM events_legacy LIMIT 1);

-- 5. Recriar constraint de unicidade
ALTER TABLE events ADD CONSTRAINT events_aggregate_version_unique 
    UNIQUE (aggregate_id, version);

-- 6. Remover tabela legacy após confirmação
-- DROP TABLE events_legacy; -- Descomentado após validação

-- Criar job de manutenção automática de partições
-- Executar mensalmente para criar partições futuras
CREATE OR REPLACE FUNCTION schedule_partition_maintenance()
RETURNS void AS $$
BEGIN
    -- Esta função será chamada por um scheduler externo (cron, pg_cron, etc.)
    PERFORM maintain_event_partitions();
    
    -- Log da execução
    INSERT INTO partition_maintenance_log (execution_time, status, message)
    VALUES (CURRENT_TIMESTAMP, 'SUCCESS', 'Partições mantidas automaticamente');
    
EXCEPTION WHEN OTHERS THEN
    -- Log de erro
    INSERT INTO partition_maintenance_log (execution_time, status, message, error_detail)
    VALUES (CURRENT_TIMESTAMP, 'ERROR', 'Erro na manutenção de partições', SQLERRM);
    
    RAISE;
END;
$$ LANGUAGE plpgsql;

-- Tabela para log de manutenção de partições
CREATE TABLE IF NOT EXISTS partition_maintenance_log (
    id SERIAL PRIMARY KEY,
    execution_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    message TEXT,
    error_detail TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Índice para consultas de log
CREATE INDEX IF NOT EXISTS idx_partition_log_execution_time 
    ON partition_maintenance_log (execution_time DESC);

-- Função para estatísticas de partições
CREATE OR REPLACE FUNCTION get_partition_statistics()
RETURNS TABLE (
    partition_name text,
    row_count bigint,
    size_bytes bigint,
    size_pretty text,
    min_timestamp timestamp with time zone,
    max_timestamp timestamp with time zone
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        schemaname || '.' || tablename as partition_name,
        n_tup_ins + n_tup_upd as row_count,
        pg_total_relation_size(schemaname||'.'||tablename) as size_bytes,
        pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size_pretty,
        NULL::timestamp with time zone as min_timestamp,
        NULL::timestamp with time zone as max_timestamp
    FROM pg_stat_user_tables 
    WHERE tablename LIKE 'events_%'
    ORDER BY tablename;
END;
$$ LANGUAGE plpgsql;

-- Executar manutenção inicial
SELECT maintain_event_partitions();

-- Comentários para documentação
COMMENT ON FUNCTION create_monthly_partition(text, date) IS 'Cria partição mensal para tabela especificada';
COMMENT ON FUNCTION maintain_event_partitions() IS 'Mantém partições criando as próximas 3 mensais';
COMMENT ON FUNCTION schedule_partition_maintenance() IS 'Função para agendamento de manutenção automática';
COMMENT ON FUNCTION get_partition_statistics() IS 'Retorna estatísticas das partições existentes';
COMMENT ON TABLE partition_maintenance_log IS 'Log de execuções da manutenção de partições';

-- Configurações de performance para partições
ALTER SYSTEM SET constraint_exclusion = partition;
ALTER SYSTEM SET enable_partition_pruning = on;
ALTER SYSTEM SET enable_partitionwise_join = on;
ALTER SYSTEM SET enable_partitionwise_aggregate = on;

-- Recarregar configurações (requer restart do PostgreSQL)
-- SELECT pg_reload_conf();

COMMIT;