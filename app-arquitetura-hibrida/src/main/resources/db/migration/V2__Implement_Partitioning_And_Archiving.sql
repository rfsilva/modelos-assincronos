-- =====================================================
-- Migration V2: Particionamento e Sistema de Arquivamento
-- =====================================================
-- Autor: Principal Java Architect
-- Data: 2024-12-19
-- Versão: 2.0 (Consolidada)
-- Descrição: Implementa particionamento automático por mês e sistema
--            completo de arquivamento para otimização de performance
-- 
-- Dependências: V1__Create_EventStore_Foundation.sql
-- Rollback: Ver seção de instruções de rollback no final
-- 
-- Validações de Pré-requisitos:
-- - Tabela events deve existir
-- - PostgreSQL 12+ com suporte a particionamento
-- =====================================================

-- Definir schema padrão
SET search_path TO eventstore, public;

-- Verificar pré-requisitos
DO $$
BEGIN
    -- Verificar se tabela events existe
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_schema = 'eventstore' AND table_name = 'events') THEN
        RAISE EXCEPTION 'Tabela events não encontrada. Execute V1 primeiro.';
    END IF;
    
    -- Verificar suporte a particionamento
    IF current_setting('server_version_num')::integer < 120000 THEN
        RAISE EXCEPTION 'PostgreSQL 12+ é obrigatório para particionamento.';
    END IF;
    
    RAISE NOTICE 'Pré-requisitos para particionamento validados';
END $$;

-- =====================================================
-- SISTEMA DE PARTICIONAMENTO AUTOMÁTICO
-- =====================================================

-- Função para criar partições mensais automaticamente
CREATE OR REPLACE FUNCTION create_monthly_partition(
    p_table_name TEXT, 
    p_start_date DATE
)
RETURNS TEXT AS $$
DECLARE
    partition_name TEXT;
    end_date DATE;
    sql_command TEXT;
BEGIN
    -- Calcular nome da partição e data final
    partition_name := p_table_name || '_' || to_char(p_start_date, 'YYYY_MM');
    end_date := p_start_date + INTERVAL '1 month';
    
    -- Verificar se partição já existe
    IF EXISTS (SELECT 1 FROM information_schema.tables 
               WHERE table_schema = 'eventstore' AND table_name = partition_name) THEN
        RAISE NOTICE 'Partição % já existe', partition_name;
        RETURN partition_name;
    END IF;
    
    -- Criar partição
    sql_command := format('
        CREATE TABLE %I PARTITION OF %I
        FOR VALUES FROM (%L) TO (%L)',
        partition_name, p_table_name, p_start_date, end_date);
    
    EXECUTE sql_command;
    
    -- Criar índices específicos da partição
    EXECUTE format('
        CREATE INDEX %I ON %I (aggregate_id, version)',
        partition_name || '_aggregate_version_idx', partition_name);
    
    EXECUTE format('
        CREATE INDEX %I ON %I (aggregate_id, timestamp DESC)',
        partition_name || '_aggregate_timestamp_idx', partition_name);
    
    EXECUTE format('
        CREATE INDEX %I ON %I (event_type, timestamp DESC)',
        partition_name || '_event_type_timestamp_idx', partition_name);
    
    EXECUTE format('
        CREATE INDEX %I ON %I (correlation_id) WHERE correlation_id IS NOT NULL',
        partition_name || '_correlation_id_idx', partition_name);
    
    -- Configurar autovacuum otimizado para a partição
    EXECUTE format('
        ALTER TABLE %I SET (
            autovacuum_vacuum_scale_factor = 0.05,
            autovacuum_analyze_scale_factor = 0.02,
            fillfactor = 90
        )', partition_name);
    
    RAISE NOTICE 'Partição % criada com sucesso', partition_name;
    RETURN partition_name;
END;
$$ LANGUAGE plpgsql;

-- Função para manutenção automática de partições
CREATE OR REPLACE FUNCTION maintain_event_partitions()
RETURNS TABLE (
    partition_name TEXT,
    status TEXT,
    message TEXT
) AS $$
DECLARE
    current_month DATE;
    future_month DATE;
    i INTEGER;
    created_partition TEXT;
BEGIN
    -- Criar partições para os próximos 6 meses
    current_month := date_trunc('month', CURRENT_DATE);
    
    FOR i IN 0..5 LOOP
        future_month := current_month + (i || ' months')::INTERVAL;
        
        BEGIN
            created_partition := create_monthly_partition('events', future_month);
            
            RETURN QUERY SELECT 
                created_partition,
                'SUCCESS'::TEXT,
                format('Partição criada para %s', to_char(future_month, 'YYYY-MM'));
                
        EXCEPTION WHEN OTHERS THEN
            RETURN QUERY SELECT 
                format('events_%s', to_char(future_month, 'YYYY_MM')),
                'ERROR'::TEXT,
                SQLERRM;
        END;
    END LOOP;
    
    -- Log da execução
    INSERT INTO partition_maintenance_log (execution_time, status, message)
    VALUES (CURRENT_TIMESTAMP, 'SUCCESS', 'Manutenção de partições executada');
    
EXCEPTION WHEN OTHERS THEN
    INSERT INTO partition_maintenance_log (execution_time, status, message, error_detail)
    VALUES (CURRENT_TIMESTAMP, 'ERROR', 'Erro na manutenção de partições', SQLERRM);
    RAISE;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- CONVERSÃO PARA TABELA PARTICIONADA
-- =====================================================

-- Verificar se há dados na tabela atual
DO $$
DECLARE
    event_count BIGINT;
    min_date DATE;
    max_date DATE;
    current_month DATE;
    backup_table_name TEXT := 'events_backup_' || to_char(CURRENT_TIMESTAMP, 'YYYYMMDD_HH24MISS');
BEGIN
    -- Contar eventos existentes
    SELECT COUNT(*), 
           COALESCE(date_trunc('month', MIN(timestamp)), date_trunc('month', CURRENT_DATE)),
           COALESCE(date_trunc('month', MAX(timestamp)), date_trunc('month', CURRENT_DATE))
    INTO event_count, min_date, max_date
    FROM events;
    
    RAISE NOTICE 'Encontrados % eventos entre % e %', event_count, min_date, max_date;
    
    -- Se há dados, fazer backup e conversão
    IF event_count > 0 THEN
        RAISE NOTICE 'Criando backup da tabela events como %', backup_table_name;
        
        -- Criar tabela de backup
        EXECUTE format('CREATE TABLE %I AS SELECT * FROM events', backup_table_name);
        
        -- Adicionar comentário ao backup
        EXECUTE format('COMMENT ON TABLE %I IS ''Backup da tabela events antes do particionamento - %s''', 
                      backup_table_name, CURRENT_TIMESTAMP);
        
        RAISE NOTICE 'Backup criado com % registros', event_count;
    END IF;
    
    -- Renomear tabela atual
    ALTER TABLE events RENAME TO events_legacy;
    
    -- Criar nova tabela particionada
    CREATE TABLE events (
        id UUID DEFAULT uuid_generate_v4(),
        aggregate_id VARCHAR(255) NOT NULL,
        aggregate_type VARCHAR(100) NOT NULL,
        event_type VARCHAR(100) NOT NULL,
        version BIGINT NOT NULL,
        timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
        correlation_id UUID,
        user_id VARCHAR(100),
        event_data JSONB NOT NULL,
        metadata JSONB,
        compressed BOOLEAN NOT NULL DEFAULT FALSE,
        data_size INTEGER NOT NULL DEFAULT 0,
        
        -- Constraints
        CONSTRAINT events_pkey PRIMARY KEY (id, timestamp),
        CONSTRAINT chk_events_version_positive CHECK (version >= 0),
        CONSTRAINT chk_events_data_size_positive CHECK (data_size >= 0),
        CONSTRAINT chk_events_timestamp_valid CHECK (timestamp <= CURRENT_TIMESTAMP + INTERVAL '1 hour')
    ) PARTITION BY RANGE (timestamp);
    
    -- Criar partições para dados históricos e futuros
    current_month := min_date;
    WHILE current_month <= max_date + INTERVAL '6 months' LOOP
        PERFORM create_monthly_partition('events', current_month);
        current_month := current_month + INTERVAL '1 month';
    END LOOP;
    
    -- Migrar dados se existirem
    IF event_count > 0 THEN
        RAISE NOTICE 'Migrando % eventos para tabela particionada...', event_count;
        
        INSERT INTO events 
        SELECT * FROM events_legacy;
        
        RAISE NOTICE 'Migração concluída com sucesso';
        
        -- Manter tabela legacy por segurança (será removida em migration futura)
        COMMENT ON TABLE events_legacy IS 'Tabela legacy mantida para rollback - será removida em V3';
    END IF;
    
    -- Recriar constraint de unicidade
    ALTER TABLE events ADD CONSTRAINT events_aggregate_version_unique 
        UNIQUE (aggregate_id, version);
    
    RAISE NOTICE 'Conversão para particionamento concluída';
END $$;

-- =====================================================
-- SISTEMA DE ARQUIVAMENTO
-- =====================================================

-- Tabela para metadados de arquivos
CREATE TABLE event_archives (
    id SERIAL PRIMARY KEY,
    partition_name VARCHAR(100) NOT NULL UNIQUE,
    archive_key VARCHAR(500) NOT NULL,
    event_count BIGINT NOT NULL,
    original_size BIGINT,
    compressed_size BIGINT NOT NULL,
    compression_ratio DECIMAL(5,4),
    archived_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ARCHIVED',
    storage_type VARCHAR(20) DEFAULT 'filesystem',
    checksum VARCHAR(64),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_event_archives_status CHECK (
        status IN ('ARCHIVED', 'RESTORING', 'ERROR', 'DELETED')
    ),
    CONSTRAINT chk_event_archives_storage_type CHECK (
        storage_type IN ('filesystem', 's3', 'gcs', 'azure')
    ),
    CONSTRAINT chk_event_archives_sizes CHECK (
        original_size IS NULL OR (compressed_size <= original_size)
    )
);

-- Índices para consultas otimizadas de arquivos
CREATE INDEX idx_event_archives_status ON event_archives (status);
CREATE INDEX idx_event_archives_archived_at ON event_archives (archived_at);
CREATE INDEX idx_event_archives_partition_name ON event_archives (partition_name);
CREATE INDEX idx_event_archives_storage_type ON event_archives (storage_type, status);

-- Tabela para log de operações de arquivamento
CREATE TABLE archive_operations_log (
    id SERIAL PRIMARY KEY,
    operation_type VARCHAR(20) NOT NULL,
    partition_name VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    duration_ms BIGINT,
    events_processed BIGINT,
    bytes_processed BIGINT,
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_archive_ops_operation_type CHECK (
        operation_type IN ('ARCHIVE', 'RESTORE', 'DELETE', 'COMPACT', 'VERIFY')
    ),
    CONSTRAINT chk_archive_ops_status CHECK (
        status IN ('SUCCESS', 'ERROR', 'IN_PROGRESS', 'CANCELLED')
    )
);

-- Índices para log de operações
CREATE INDEX idx_archive_ops_log_operation_type ON archive_operations_log (operation_type, started_at DESC);
CREATE INDEX idx_archive_ops_log_status ON archive_operations_log (status, started_at DESC);
CREATE INDEX idx_archive_ops_log_partition ON archive_operations_log (partition_name, started_at DESC);

-- Tabela para estatísticas de arquivamento
CREATE TABLE archive_statistics (
    id SERIAL PRIMARY KEY,
    snapshot_date DATE NOT NULL UNIQUE,
    total_archives INTEGER NOT NULL DEFAULT 0,
    total_events BIGINT NOT NULL DEFAULT 0,
    total_original_size BIGINT NOT NULL DEFAULT 0,
    total_compressed_size BIGINT NOT NULL DEFAULT 0,
    average_compression_ratio DECIMAL(5,4),
    oldest_archive_date DATE,
    newest_archive_date DATE,
    storage_efficiency DECIMAL(5,4),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Índice para estatísticas
CREATE INDEX idx_archive_stats_snapshot_date ON archive_statistics (snapshot_date DESC);

-- Tabela para log de manutenção de partições
CREATE TABLE partition_maintenance_log (
    id SERIAL PRIMARY KEY,
    execution_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    message TEXT,
    error_detail TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_partition_log_status CHECK (
        status IN ('SUCCESS', 'ERROR', 'WARNING')
    )
);

-- Índice para consultas de log
CREATE INDEX idx_partition_log_execution_time ON partition_maintenance_log (execution_time DESC);
CREATE INDEX idx_partition_log_status ON partition_maintenance_log (status, execution_time DESC);

-- =====================================================
-- FUNÇÕES DE ARQUIVAMENTO
-- =====================================================

-- Função para calcular estatísticas de arquivamento
CREATE OR REPLACE FUNCTION calculate_archive_statistics()
RETURNS void AS $$
DECLARE
    stats_date DATE := CURRENT_DATE;
    total_archives INTEGER;
    total_events BIGINT;
    total_original_size BIGINT;
    total_compressed_size BIGINT;
    avg_compression_ratio DECIMAL(5,4);
    oldest_date DATE;
    newest_date DATE;
    storage_efficiency DECIMAL(5,4);
BEGIN
    -- Calcular estatísticas
    SELECT 
        COUNT(*),
        COALESCE(SUM(event_count), 0),
        COALESCE(SUM(original_size), 0),
        COALESCE(SUM(compressed_size), 0),
        COALESCE(AVG(compression_ratio), 0),
        MIN(DATE(archived_at)),
        MAX(DATE(archived_at))
    INTO 
        total_archives,
        total_events,
        total_original_size,
        total_compressed_size,
        avg_compression_ratio,
        oldest_date,
        newest_date
    FROM event_archives 
    WHERE status = 'ARCHIVED';
    
    -- Calcular eficiência de storage
    IF total_original_size > 0 THEN
        storage_efficiency := 1.0 - (CAST(total_compressed_size AS DECIMAL) / total_original_size);
    ELSE
        storage_efficiency := 0.0;
    END IF;
    
    -- Inserir ou atualizar estatísticas
    INSERT INTO archive_statistics (
        snapshot_date, total_archives, total_events, total_original_size,
        total_compressed_size, average_compression_ratio, oldest_archive_date,
        newest_archive_date, storage_efficiency
    ) VALUES (
        stats_date, total_archives, total_events, total_original_size,
        total_compressed_size, avg_compression_ratio, oldest_date,
        newest_date, storage_efficiency
    )
    ON CONFLICT (snapshot_date) DO UPDATE SET
        total_archives = EXCLUDED.total_archives,
        total_events = EXCLUDED.total_events,
        total_original_size = EXCLUDED.total_original_size,
        total_compressed_size = EXCLUDED.total_compressed_size,
        average_compression_ratio = EXCLUDED.average_compression_ratio,
        oldest_archive_date = EXCLUDED.oldest_archive_date,
        newest_archive_date = EXCLUDED.newest_archive_date,
        storage_efficiency = EXCLUDED.storage_efficiency,
        created_at = CURRENT_TIMESTAMP;
    
    RAISE NOTICE 'Estatísticas de arquivamento atualizadas para %', stats_date;
END;
$$ LANGUAGE plpgsql;

-- Função para limpeza de logs antigos
CREATE OR REPLACE FUNCTION cleanup_archive_logs(
    p_retention_days INTEGER DEFAULT 90
)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM archive_operations_log 
    WHERE created_at < CURRENT_TIMESTAMP - (p_retention_days || ' days')::INTERVAL;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    RAISE NOTICE 'Removidos % registros de log de arquivamento', deleted_count;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Função para obter estatísticas de partições
CREATE OR REPLACE FUNCTION get_partition_statistics()
RETURNS TABLE (
    partition_name TEXT,
    row_count BIGINT,
    size_bytes BIGINT,
    size_pretty TEXT,
    min_timestamp TIMESTAMP WITH TIME ZONE,
    max_timestamp TIMESTAMP WITH TIME ZONE,
    is_archived BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.schemaname || '.' || t.tablename as partition_name,
        COALESCE(t.n_tup_ins + t.n_tup_upd, 0) as row_count,
        pg_total_relation_size(t.schemaname||'.'||t.tablename) as size_bytes,
        pg_size_pretty(pg_total_relation_size(t.schemaname||'.'||t.tablename)) as size_pretty,
        NULL::TIMESTAMP WITH TIME ZONE as min_timestamp,
        NULL::TIMESTAMP WITH TIME ZONE as max_timestamp,
        EXISTS(SELECT 1 FROM event_archives ea WHERE ea.partition_name = t.tablename) as is_archived
    FROM pg_stat_user_tables t
    WHERE t.schemaname = 'eventstore' 
    AND t.tablename LIKE 'events_%'
    AND t.tablename != 'events_legacy'
    ORDER BY t.tablename;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- TRIGGERS PARA MANUTENÇÃO AUTOMÁTICA
-- =====================================================

-- Trigger para atualizar compression_ratio automaticamente
CREATE OR REPLACE FUNCTION update_compression_ratio()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.original_size IS NOT NULL AND NEW.original_size > 0 THEN
        NEW.compression_ratio := 1.0 - (CAST(NEW.compressed_size AS DECIMAL) / NEW.original_size);
    ELSE
        NEW.compression_ratio := 0.0;
    END IF;
    
    NEW.updated_at := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_compression_ratio
    BEFORE INSERT OR UPDATE ON event_archives
    FOR EACH ROW
    EXECUTE FUNCTION update_compression_ratio();

-- =====================================================
-- VIEWS PARA CONSULTAS SIMPLIFICADAS
-- =====================================================

-- View para resumo de arquivos
CREATE OR REPLACE VIEW v_archive_summary AS
SELECT 
    ea.partition_name,
    ea.event_count,
    ea.compressed_size,
    ea.compression_ratio,
    ea.archived_at,
    ea.status,
    ea.storage_type,
    CASE 
        WHEN ea.compressed_size < 1024 THEN ea.compressed_size::TEXT || ' B'
        WHEN ea.compressed_size < 1024*1024 THEN ROUND(ea.compressed_size/1024.0, 1)::TEXT || ' KB'
        WHEN ea.compressed_size < 1024*1024*1024 THEN ROUND(ea.compressed_size/1024.0/1024.0, 1)::TEXT || ' MB'
        ELSE ROUND(ea.compressed_size/1024.0/1024.0/1024.0, 1)::TEXT || ' GB'
    END as size_formatted,
    ROUND(ea.compression_ratio * 100, 1)::TEXT || '%' as compression_formatted
FROM event_archives ea
WHERE ea.status = 'ARCHIVED'
ORDER BY ea.archived_at DESC;

-- View para status das partições
CREATE OR REPLACE VIEW v_partition_status AS
SELECT 
    ps.partition_name,
    ps.row_count,
    ps.size_pretty,
    ps.is_archived,
    CASE 
        WHEN ps.is_archived THEN 'ARCHIVED'
        WHEN ps.row_count = 0 THEN 'EMPTY'
        ELSE 'ACTIVE'
    END as status,
    ea.archived_at,
    ea.compression_ratio
FROM get_partition_statistics() ps
LEFT JOIN event_archives ea ON ea.partition_name = REPLACE(ps.partition_name, 'eventstore.', '');

-- =====================================================
-- CONFIGURAÇÕES DE PERFORMANCE
-- =====================================================

-- Configurar parâmetros do PostgreSQL para particionamento
ALTER SYSTEM SET constraint_exclusion = partition;
ALTER SYSTEM SET enable_partition_pruning = on;
ALTER SYSTEM SET enable_partitionwise_join = on;
ALTER SYSTEM SET enable_partitionwise_aggregate = on;

-- =====================================================
-- EXECUÇÃO INICIAL
-- =====================================================

-- Executar manutenção inicial de partições
SELECT maintain_event_partitions();

-- Calcular estatísticas iniciais
SELECT calculate_archive_statistics();

-- =====================================================
-- COMENTÁRIOS PARA DOCUMENTAÇÃO
-- =====================================================

COMMENT ON FUNCTION create_monthly_partition(TEXT, DATE) IS 'Cria partição mensal para tabela especificada com índices otimizados';
COMMENT ON FUNCTION maintain_event_partitions() IS 'Mantém partições criando as próximas 6 mensais automaticamente';
COMMENT ON FUNCTION calculate_archive_statistics() IS 'Calcula e armazena estatísticas diárias de arquivamento';
COMMENT ON FUNCTION cleanup_archive_logs(INTEGER) IS 'Remove logs de arquivamento antigos baseado em retenção';
COMMENT ON FUNCTION get_partition_statistics() IS 'Retorna estatísticas detalhadas das partições existentes';

COMMENT ON TABLE event_archives IS 'Metadados de partições arquivadas com informações de compressão';
COMMENT ON TABLE archive_operations_log IS 'Log detalhado de todas as operações de arquivamento';
COMMENT ON TABLE archive_statistics IS 'Estatísticas diárias agregadas do sistema de arquivamento';
COMMENT ON TABLE partition_maintenance_log IS 'Log de execuções da manutenção automática de partições';

COMMENT ON VIEW v_archive_summary IS 'View simplificada para consulta de arquivos com formatação amigável';
COMMENT ON VIEW v_partition_status IS 'View consolidada do status de todas as partições';

-- =====================================================
-- VALIDAÇÕES FINAIS
-- =====================================================

-- Verificar se particionamento foi aplicado corretamente
DO $$
DECLARE
    partition_count INTEGER;
    archive_table_count INTEGER;
BEGIN
    -- Contar partições criadas
    SELECT COUNT(*) INTO partition_count
    FROM information_schema.tables 
    WHERE table_schema = 'eventstore' 
    AND table_name LIKE 'events_%'
    AND table_name != 'events_legacy';
    
    -- Contar tabelas de arquivamento
    SELECT COUNT(*) INTO archive_table_count
    FROM information_schema.tables 
    WHERE table_schema = 'eventstore' 
    AND table_name IN ('event_archives', 'archive_operations_log', 'archive_statistics', 'partition_maintenance_log');
    
    RAISE NOTICE 'Migration V2 concluída com sucesso:';
    RAISE NOTICE '- Partições criadas: %', partition_count;
    RAISE NOTICE '- Tabelas de arquivamento: %', archive_table_count;
    RAISE NOTICE '- Sistema de particionamento ativo';
    RAISE NOTICE '- Sistema de arquivamento configurado';
    
    IF partition_count < 6 THEN
        RAISE WARNING 'Menos partições que o esperado foram criadas';
    END IF;
    
    IF archive_table_count < 4 THEN
        RAISE EXCEPTION 'Falha na criação das tabelas de arquivamento';
    END IF;
END $$;

-- =====================================================
-- INSTRUÇÕES DE ROLLBACK
-- =====================================================
/*
Para fazer rollback desta migration:

1. Parar a aplicação
2. Executar os comandos na ordem:
   
   -- Restaurar tabela original se existir backup
   DROP TABLE IF EXISTS events CASCADE;
   ALTER TABLE events_legacy RENAME TO events;
   
   -- Remover tabelas de arquivamento
   DROP TABLE IF EXISTS event_archives CASCADE;
   DROP TABLE IF EXISTS archive_operations_log CASCADE;
   DROP TABLE IF EXISTS archive_statistics CASCADE;
   DROP TABLE IF EXISTS partition_maintenance_log CASCADE;
   
   -- Remover funções
   DROP FUNCTION IF EXISTS create_monthly_partition(TEXT, DATE);
   DROP FUNCTION IF EXISTS maintain_event_partitions();
   DROP FUNCTION IF EXISTS calculate_archive_statistics();
   DROP FUNCTION IF EXISTS cleanup_archive_logs(INTEGER);
   DROP FUNCTION IF EXISTS get_partition_statistics();
   DROP FUNCTION IF EXISTS update_compression_ratio();
   
   -- Remover views
   DROP VIEW IF EXISTS v_archive_summary;
   DROP VIEW IF EXISTS v_partition_status;

3. Remover entrada da tabela flyway_schema_history
4. Reiniciar aplicação

ATENÇÃO: Verifique se há dados importantes antes do rollback!
*/

-- =====================================================
-- FIM DA MIGRATION V2
-- =====================================================