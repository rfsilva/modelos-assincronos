-- =====================================================
-- Script Auxiliar: Configurações de Sistema
-- =====================================================
-- Autor: Principal Java Architect
-- Data: 2024-12-19
-- Descrição: Configurações avançadas de sistema para otimização
--            do Event Store e sistema de particionamento
-- 
-- ATENÇÃO: Este script deve ser executado por um DBA
--          com privilégios de superusuário
-- =====================================================

-- =====================================================
-- CONFIGURAÇÕES DE PERFORMANCE DO POSTGRESQL
-- =====================================================

-- Configurações para particionamento
ALTER SYSTEM SET constraint_exclusion = partition;
ALTER SYSTEM SET enable_partition_pruning = on;
ALTER SYSTEM SET enable_partitionwise_join = on;
ALTER SYSTEM SET enable_partitionwise_aggregate = on;

-- Configurações de memória para workloads de alta inserção
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET work_mem = '16MB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';

-- Configurações de WAL para alta performance
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_writer_delay = '200ms';

-- Configurações de autovacuum otimizadas
ALTER SYSTEM SET autovacuum_max_workers = 4;
ALTER SYSTEM SET autovacuum_naptime = '30s';
ALTER SYSTEM SET autovacuum_vacuum_cost_delay = '10ms';

-- Configurações para JSON/JSONB
ALTER SYSTEM SET default_statistics_target = 100;

-- =====================================================
-- EXTENSÕES NECESSÁRIAS
-- =====================================================

-- Extensão para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Extensão para funções de texto (full-text search)
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- Extensão para estatísticas avançadas
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Extensão para compressão (se disponível)
-- CREATE EXTENSION IF NOT EXISTS "pg_squeeze";

-- =====================================================
-- CONFIGURAÇÕES DE MONITORAMENTO
-- =====================================================

-- Habilitar coleta de estatísticas
ALTER SYSTEM SET track_activities = on;
ALTER SYSTEM SET track_counts = on;
ALTER SYSTEM SET track_io_timing = on;
ALTER SYSTEM SET track_functions = 'all';

-- Configurações de log para monitoramento
ALTER SYSTEM SET log_min_duration_statement = '1000ms';
ALTER SYSTEM SET log_checkpoints = on;
ALTER SYSTEM SET log_connections = on;
ALTER SYSTEM SET log_disconnections = on;
ALTER SYSTEM SET log_lock_waits = on;

-- =====================================================
-- ROLES E PERMISSÕES
-- =====================================================

-- Criar role para aplicação (se não existir)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'eventstore_app') THEN
        CREATE ROLE eventstore_app WITH LOGIN PASSWORD 'change_me_in_production';
        RAISE NOTICE 'Role eventstore_app criada. ALTERE A SENHA EM PRODUÇÃO!';
    END IF;
END $$;

-- Criar role para leitura (projections)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'eventstore_reader') THEN
        CREATE ROLE eventstore_reader WITH LOGIN PASSWORD 'change_me_in_production';
        RAISE NOTICE 'Role eventstore_reader criada. ALTERE A SENHA EM PRODUÇÃO!';
    END IF;
END $$;

-- Conceder permissões ao schema eventstore
GRANT USAGE ON SCHEMA eventstore TO eventstore_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA eventstore TO eventstore_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA eventstore TO eventstore_app;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA eventstore TO eventstore_app;

-- Permissões de leitura para projections
GRANT USAGE ON SCHEMA eventstore TO eventstore_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA eventstore TO eventstore_reader;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA eventstore TO eventstore_reader;

-- Configurar permissões padrão para objetos futuros
ALTER DEFAULT PRIVILEGES IN SCHEMA eventstore 
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO eventstore_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA eventstore 
    GRANT USAGE, SELECT ON SEQUENCES TO eventstore_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA eventstore 
    GRANT EXECUTE ON FUNCTIONS TO eventstore_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA eventstore 
    GRANT SELECT ON TABLES TO eventstore_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA eventstore 
    GRANT EXECUTE ON FUNCTIONS TO eventstore_reader;

-- =====================================================
-- CONFIGURAÇÕES DE BACKUP E REPLICAÇÃO
-- =====================================================

-- Configurações para backup contínuo
ALTER SYSTEM SET archive_mode = on;
ALTER SYSTEM SET archive_command = 'test ! -f /backup/archive/%f && cp %p /backup/archive/%f';
ALTER SYSTEM SET max_wal_senders = 3;
ALTER SYSTEM SET wal_level = replica;

-- =====================================================
-- JOBS DE MANUTENÇÃO AUTOMÁTICA
-- =====================================================

-- Função para job de manutenção diária
CREATE OR REPLACE FUNCTION daily_maintenance_job()
RETURNS void AS $$
BEGIN
    -- Manter partições
    PERFORM maintain_event_partitions();
    
    -- Calcular estatísticas de arquivamento
    PERFORM calculate_archive_statistics();
    
    -- Limpar logs antigos (manter 90 dias)
    PERFORM cleanup_archive_logs(90);
    
    -- Limpar snapshots antigos (manter 90 dias, 5 por aggregate)
    PERFORM cleanup_old_snapshots(90, 5);
    
    -- Atualizar estatísticas das tabelas
    ANALYZE events;
    ANALYZE snapshots;
    ANALYZE projection_tracking;
    
    RAISE NOTICE 'Manutenção diária executada com sucesso';
END;
$$ LANGUAGE plpgsql;

-- Função para job de manutenção semanal
CREATE OR REPLACE FUNCTION weekly_maintenance_job()
RETURNS void AS $$
BEGIN
    -- Executar manutenção diária
    PERFORM daily_maintenance_job();
    
    -- Vacuum completo nas tabelas principais
    VACUUM ANALYZE events;
    VACUUM ANALYZE snapshots;
    VACUUM ANALYZE projection_tracking;
    
    -- Reindexar se necessário (apenas se fragmentação > 20%)
    -- REINDEX INDEX CONCURRENTLY IF EXISTS idx_events_aggregate_version;
    
    RAISE NOTICE 'Manutenção semanal executada com sucesso';
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- CONFIGURAÇÕES DE ALERTAS E MONITORAMENTO
-- =====================================================

-- Função para verificar saúde do sistema
CREATE OR REPLACE FUNCTION check_system_health()
RETURNS TABLE (
    check_name TEXT,
    status TEXT,
    message TEXT,
    value NUMERIC
) AS $$
BEGIN
    -- Verificar tamanho das partições
    RETURN QUERY
    SELECT 
        'partition_size'::TEXT,
        CASE WHEN ps.size_bytes > 1073741824 THEN 'WARNING' ELSE 'OK' END,
        format('Partição %s: %s', ps.partition_name, ps.size_pretty),
        ps.size_bytes::NUMERIC
    FROM get_partition_statistics() ps
    WHERE ps.row_count > 0;
    
    -- Verificar lag das projeções
    RETURN QUERY
    SELECT 
        'projection_lag'::TEXT,
        CASE 
            WHEN pt.last_processed_at < CURRENT_TIMESTAMP - INTERVAL '1 hour' THEN 'ERROR'
            WHEN pt.last_processed_at < CURRENT_TIMESTAMP - INTERVAL '15 minutes' THEN 'WARNING'
            ELSE 'OK'
        END,
        format('Projeção %s: última atualização %s', pt.projection_name, pt.last_processed_at),
        EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - pt.last_processed_at))::NUMERIC
    FROM projection_tracking pt
    WHERE pt.status = 'ACTIVE';
    
    -- Verificar taxa de erro das projeções
    RETURN QUERY
    SELECT 
        'projection_error_rate'::TEXT,
        CASE 
            WHEN (pt.events_failed::FLOAT / NULLIF(pt.events_processed + pt.events_failed, 0)) > 0.05 THEN 'ERROR'
            WHEN (pt.events_failed::FLOAT / NULLIF(pt.events_processed + pt.events_failed, 0)) > 0.01 THEN 'WARNING'
            ELSE 'OK'
        END,
        format('Projeção %s: %.2f%% de erro', 
               pt.projection_name, 
               (pt.events_failed::FLOAT / NULLIF(pt.events_processed + pt.events_failed, 0)) * 100),
        (pt.events_failed::FLOAT / NULLIF(pt.events_processed + pt.events_failed, 0))::NUMERIC
    FROM projection_tracking pt
    WHERE pt.events_processed + pt.events_failed > 0;
    
    -- Verificar conexões ativas
    RETURN QUERY
    SELECT 
        'active_connections'::TEXT,
        CASE 
            WHEN COUNT(*) > 80 THEN 'ERROR'
            WHEN COUNT(*) > 60 THEN 'WARNING'
            ELSE 'OK'
        END,
        format('%s conexões ativas', COUNT(*)),
        COUNT(*)::NUMERIC
    FROM pg_stat_activity 
    WHERE state = 'active';
    
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- CONFIGURAÇÕES DE SEGURANÇA
-- =====================================================

-- Revogar permissões públicas desnecessárias
REVOKE ALL ON SCHEMA eventstore FROM public;
REVOKE ALL ON ALL TABLES IN SCHEMA eventstore FROM public;

-- Configurar row level security (se necessário)
-- ALTER TABLE events ENABLE ROW LEVEL SECURITY;
-- CREATE POLICY events_tenant_policy ON events FOR ALL TO eventstore_app 
--     USING (metadata->>'tenant_id' = current_setting('app.tenant_id', true));

-- =====================================================
-- CONFIGURAÇÕES DE RETENÇÃO DE DADOS
-- =====================================================

-- Função para política de retenção de dados
CREATE OR REPLACE FUNCTION apply_data_retention_policy(
    p_retention_months INTEGER DEFAULT 24
)
RETURNS TABLE (
    partition_name TEXT,
    action TEXT,
    message TEXT
) AS $$
DECLARE
    cutoff_date DATE;
    partition_record RECORD;
BEGIN
    cutoff_date := CURRENT_DATE - (p_retention_months || ' months')::INTERVAL;
    
    RAISE NOTICE 'Aplicando política de retenção: dados anteriores a %', cutoff_date;
    
    -- Identificar partições antigas
    FOR partition_record IN 
        SELECT t.tablename
        FROM information_schema.tables t
        WHERE t.table_schema = 'eventstore' 
        AND t.table_name LIKE 'events_%'
        AND t.table_name != 'events_legacy'
        AND to_date(SUBSTRING(t.table_name FROM 'events_(\d{4}_\d{2})'), 'YYYY_MM') < cutoff_date
    LOOP
        -- Verificar se partição já está arquivada
        IF EXISTS (SELECT 1 FROM event_archives WHERE partition_name = partition_record.tablename) THEN
            RETURN QUERY SELECT 
                partition_record.tablename,
                'SKIP'::TEXT,
                'Partição já arquivada'::TEXT;
        ELSE
            RETURN QUERY SELECT 
                partition_record.tablename,
                'ARCHIVE_CANDIDATE'::TEXT,
                format('Partição elegível para arquivamento (anterior a %s)', cutoff_date)::TEXT;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- COMENTÁRIOS PARA DOCUMENTAÇÃO
-- =====================================================

COMMENT ON FUNCTION daily_maintenance_job() IS 'Job de manutenção diária - executar via cron ou scheduler';
COMMENT ON FUNCTION weekly_maintenance_job() IS 'Job de manutenção semanal - executar aos domingos';
COMMENT ON FUNCTION check_system_health() IS 'Verifica saúde geral do sistema Event Store';
COMMENT ON FUNCTION apply_data_retention_policy(INTEGER) IS 'Identifica dados elegíveis para arquivamento baseado em retenção';

-- =====================================================
-- INSTRUÇÕES DE USO
-- =====================================================

/*
INSTRUÇÕES PARA CONFIGURAÇÃO EM PRODUÇÃO:

1. CONFIGURAÇÕES DE SISTEMA:
   - Execute este script como superusuário PostgreSQL
   - Ajuste os valores de memória conforme seu ambiente
   - Reinicie o PostgreSQL após as alterações de sistema

2. SENHAS:
   - ALTERE as senhas padrão dos roles criados
   - Use senhas fortes em produção

3. BACKUP:
   - Configure o diretório /backup/archive/
   - Implemente estratégia de backup completo

4. MONITORAMENTO:
   - Configure alertas baseados na função check_system_health()
   - Monitore logs de erro do PostgreSQL

5. JOBS DE MANUTENÇÃO:
   - Configure cron job para daily_maintenance_job() (diário às 02:00)
   - Configure cron job para weekly_maintenance_job() (domingo às 03:00)

6. EXEMPLO DE CRON:
   0 2 * * * psql -d eventstore -c "SELECT daily_maintenance_job();"
   0 3 * * 0 psql -d eventstore -c "SELECT weekly_maintenance_job();"

7. MONITORAMENTO DE SAÚDE:
   */5 * * * * psql -d eventstore -c "SELECT * FROM check_system_health() WHERE status != 'OK';"
*/

-- =====================================================
-- VALIDAÇÃO FINAL
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE '=== CONFIGURAÇÃO DE SISTEMA CONCLUÍDA ===';
    RAISE NOTICE 'Extensões instaladas: uuid-ossp, unaccent, pg_stat_statements';
    RAISE NOTICE 'Roles criados: eventstore_app, eventstore_reader';
    RAISE NOTICE 'Funções de manutenção: daily_maintenance_job, weekly_maintenance_job';
    RAISE NOTICE 'Função de monitoramento: check_system_health';
    RAISE NOTICE '';
    RAISE NOTICE 'PRÓXIMOS PASSOS:';
    RAISE NOTICE '1. Alterar senhas dos roles criados';
    RAISE NOTICE '2. Configurar jobs de manutenção no cron';
    RAISE NOTICE '3. Configurar monitoramento de saúde';
    RAISE NOTICE '4. Reiniciar PostgreSQL para aplicar configurações de sistema';
END $$;

-- =====================================================
-- FIM DO SCRIPT DE CONFIGURAÇÃO
-- =====================================================