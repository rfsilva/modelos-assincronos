-- =====================================================
-- Script Auxiliar: Migração de Dados Entre Versões
-- =====================================================
-- Autor: Principal Java Architect
-- Data: 2024-12-19
-- Descrição: Sistema completo de migração de dados entre versões
--            do Event Store com preservação de integridade
-- 
-- Funcionalidades:
-- - Migração segura de dados
-- - Backup automático
-- - Validação de integridade
-- - Rollback automático em caso de falha
-- =====================================================

-- Definir schema padrão
SET search_path TO eventstore, public;

-- =====================================================
-- TABELA DE CONTROLE DE MIGRAÇÃO
-- =====================================================

-- Tabela para controlar migrações de dados
CREATE TABLE IF NOT EXISTS data_migration_log (
    id SERIAL PRIMARY KEY,
    migration_name VARCHAR(100) NOT NULL,
    source_version VARCHAR(20) NOT NULL,
    target_version VARCHAR(20) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    records_processed BIGINT DEFAULT 0,
    records_failed BIGINT DEFAULT 0,
    backup_table_name VARCHAR(100),
    error_message TEXT,
    rollback_executed BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT chk_migration_status CHECK (
        status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED', 'ROLLED_BACK')
    )
);

-- Índices para consultas de migração
CREATE INDEX IF NOT EXISTS idx_data_migration_log_status 
    ON data_migration_log (status, started_at DESC);
CREATE INDEX IF NOT EXISTS idx_data_migration_log_name 
    ON data_migration_log (migration_name, started_at DESC);

-- =====================================================
-- FUNÇÕES DE BACKUP E RESTORE
-- =====================================================

-- Função para criar backup de tabela antes da migração
CREATE OR REPLACE FUNCTION create_migration_backup(
    p_table_name TEXT,
    p_migration_name TEXT
)
RETURNS TEXT AS $$
DECLARE
    backup_table_name TEXT;
    record_count BIGINT;
BEGIN
    -- Gerar nome único para backup
    backup_table_name := format('%s_backup_%s_%s', 
                                p_table_name, 
                                replace(p_migration_name, ' ', '_'),
                                to_char(CURRENT_TIMESTAMP, 'YYYYMMDD_HH24MISS'));
    
    -- Criar tabela de backup
    EXECUTE format('CREATE TABLE %I AS SELECT * FROM %I', backup_table_name, p_table_name);
    
    -- Contar registros
    EXECUTE format('SELECT COUNT(*) FROM %I', backup_table_name) INTO record_count;
    
    -- Adicionar comentário
    EXECUTE format('COMMENT ON TABLE %I IS ''Backup de %s para migração: %s - %s registros''', 
                  backup_table_name, p_table_name, p_migration_name, record_count);
    
    RAISE NOTICE 'Backup criado: % (% registros)', backup_table_name, record_count;
    RETURN backup_table_name;
END;
$$ LANGUAGE plpgsql;

-- Função para restaurar backup em caso de falha
CREATE OR REPLACE FUNCTION restore_from_backup(
    p_backup_table_name TEXT,
    p_target_table_name TEXT
)
RETURNS BOOLEAN AS $$
DECLARE
    backup_count BIGINT;
    target_count BIGINT;
BEGIN
    -- Verificar se backup existe
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_schema = 'eventstore' AND table_name = p_backup_table_name) THEN
        RAISE EXCEPTION 'Tabela de backup % não encontrada', p_backup_table_name;
    END IF;
    
    -- Contar registros no backup
    EXECUTE format('SELECT COUNT(*) FROM %I', p_backup_table_name) INTO backup_count;
    
    -- Truncar tabela alvo e restaurar dados
    EXECUTE format('TRUNCATE TABLE %I', p_target_table_name);
    EXECUTE format('INSERT INTO %I SELECT * FROM %I', p_target_table_name, p_backup_table_name);
    
    -- Verificar restauração
    EXECUTE format('SELECT COUNT(*) FROM %I', p_target_table_name) INTO target_count;
    
    IF backup_count != target_count THEN
        RAISE EXCEPTION 'Falha na restauração: backup tinha % registros, tabela tem %', 
                       backup_count, target_count;
    END IF;
    
    RAISE NOTICE 'Restauração concluída: % registros restaurados', target_count;
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- FUNÇÕES DE MIGRAÇÃO DE EVENTOS
-- =====================================================

-- Função para migrar eventos com transformação de schema
CREATE OR REPLACE FUNCTION migrate_events_schema(
    p_migration_name TEXT,
    p_source_version TEXT,
    p_target_version TEXT,
    p_transformation_function TEXT DEFAULT NULL
)
RETURNS BIGINT AS $$
DECLARE
    migration_id INTEGER;
    backup_table_name TEXT;
    processed_count BIGINT := 0;
    failed_count BIGINT := 0;
    event_record RECORD;
    transformed_data JSONB;
BEGIN
    -- Registrar início da migração
    INSERT INTO data_migration_log (
        migration_name, source_version, target_version, status
    ) VALUES (
        p_migration_name, p_source_version, p_target_version, 'IN_PROGRESS'
    ) RETURNING id INTO migration_id;
    
    RAISE NOTICE 'Iniciando migração: % (ID: %)', p_migration_name, migration_id;
    
    -- Criar backup
    backup_table_name := create_migration_backup('events', p_migration_name);
    
    -- Atualizar log com nome do backup
    UPDATE data_migration_log 
    SET backup_table_name = backup_table_name 
    WHERE id = migration_id;
    
    -- Processar eventos se há função de transformação
    IF p_transformation_function IS NOT NULL THEN
        FOR event_record IN 
            SELECT id, event_data, metadata 
            FROM events 
            ORDER BY timestamp, version
        LOOP
            BEGIN
                -- Aplicar transformação (exemplo: adicionar campo schema_version)
                IF p_transformation_function = 'add_schema_version' THEN
                    transformed_data := event_record.event_data || 
                                      jsonb_build_object('schema_version', p_target_version);
                    
                    UPDATE events 
                    SET event_data = transformed_data,
                        metadata = COALESCE(metadata, '{}'::jsonb) || 
                                 jsonb_build_object('migrated_from', p_source_version,
                                                  'migrated_at', CURRENT_TIMESTAMP)
                    WHERE id = event_record.id;
                    
                ELSIF p_transformation_function = 'normalize_timestamps' THEN
                    -- Normalizar timestamps no event_data
                    transformed_data := event_record.event_data;
                    
                    -- Converter timestamps para ISO format se necessário
                    IF transformed_data ? 'timestamp' THEN
                        transformed_data := jsonb_set(
                            transformed_data,
                            '{timestamp}',
                            to_jsonb(to_char((transformed_data->>'timestamp')::timestamp, 'YYYY-MM-DD"T"HH24:MI:SS"Z"'))
                        );
                    END IF;
                    
                    UPDATE events 
                    SET event_data = transformed_data
                    WHERE id = event_record.id;
                END IF;
                
                processed_count := processed_count + 1;
                
                -- Log progresso a cada 1000 registros
                IF processed_count % 1000 = 0 THEN
                    RAISE NOTICE 'Processados % eventos...', processed_count;
                END IF;
                
            EXCEPTION WHEN OTHERS THEN
                failed_count := failed_count + 1;
                RAISE WARNING 'Falha ao processar evento %: %', event_record.id, SQLERRM;
                
                -- Se muitas falhas, abortar
                IF failed_count > 100 THEN
                    RAISE EXCEPTION 'Muitas falhas na migração (%). Abortando.', failed_count;
                END IF;
            END;
        END LOOP;
    END IF;
    
    -- Atualizar log de sucesso
    UPDATE data_migration_log 
    SET completed_at = CURRENT_TIMESTAMP,
        status = 'COMPLETED',
        records_processed = processed_count,
        records_failed = failed_count
    WHERE id = migration_id;
    
    RAISE NOTICE 'Migração concluída: % processados, % falharam', processed_count, failed_count;
    RETURN processed_count;
    
EXCEPTION WHEN OTHERS THEN
    -- Registrar falha
    UPDATE data_migration_log 
    SET status = 'FAILED',
        error_message = SQLERRM,
        records_processed = processed_count,
        records_failed = failed_count
    WHERE id = migration_id;
    
    -- Tentar rollback automático
    BEGIN
        PERFORM restore_from_backup(backup_table_name, 'events');
        
        UPDATE data_migration_log 
        SET rollback_executed = TRUE 
        WHERE id = migration_id;
        
        RAISE NOTICE 'Rollback automático executado com sucesso';
    EXCEPTION WHEN OTHERS THEN
        RAISE WARNING 'Falha no rollback automático: %', SQLERRM;
    END;
    
    RAISE;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- FUNÇÕES DE MIGRAÇÃO DE SNAPSHOTS
-- =====================================================

-- Função para migrar snapshots com versionamento de schema
CREATE OR REPLACE FUNCTION migrate_snapshots_schema(
    p_migration_name TEXT,
    p_source_schema_version INTEGER,
    p_target_schema_version INTEGER
)
RETURNS BIGINT AS $$
DECLARE
    migration_id INTEGER;
    backup_table_name TEXT;
    processed_count BIGINT := 0;
    failed_count BIGINT := 0;
    snapshot_record RECORD;
    transformed_data JSONB;
BEGIN
    -- Registrar início da migração
    INSERT INTO data_migration_log (
        migration_name, 
        source_version, 
        target_version, 
        status
    ) VALUES (
        p_migration_name, 
        p_source_schema_version::TEXT, 
        p_target_schema_version::TEXT, 
        'IN_PROGRESS'
    ) RETURNING id INTO migration_id;
    
    RAISE NOTICE 'Iniciando migração de snapshots: % (ID: %)', p_migration_name, migration_id;
    
    -- Criar backup
    backup_table_name := create_migration_backup('snapshots', p_migration_name);
    
    -- Atualizar log com nome do backup
    UPDATE data_migration_log 
    SET backup_table_name = backup_table_name 
    WHERE id = migration_id;
    
    -- Processar snapshots que precisam de migração
    FOR snapshot_record IN 
        SELECT snapshot_id, aggregate_id, snapshot_data, schema_version
        FROM snapshots 
        WHERE schema_version = p_source_schema_version
        ORDER BY timestamp
    LOOP
        BEGIN
            -- Aplicar transformações baseadas na versão
            transformed_data := snapshot_record.snapshot_data;
            
            -- Exemplo de transformação v1 -> v2
            IF p_source_schema_version = 1 AND p_target_schema_version = 2 THEN
                -- Adicionar campos obrigatórios da v2
                IF NOT (transformed_data ? 'version') THEN
                    transformed_data := transformed_data || jsonb_build_object('version', 0);
                END IF;
                
                IF NOT (transformed_data ? 'createdAt') THEN
                    transformed_data := transformed_data || 
                                      jsonb_build_object('createdAt', CURRENT_TIMESTAMP);
                END IF;
                
            -- Exemplo de transformação v2 -> v3
            ELSIF p_source_schema_version = 2 AND p_target_schema_version = 3 THEN
                -- Reestruturar dados para novo formato
                IF transformed_data ? 'endereco' THEN
                    transformed_data := jsonb_set(
                        transformed_data,
                        '{enderecos}',
                        jsonb_build_array(transformed_data->'endereco')
                    );
                    transformed_data := transformed_data - 'endereco';
                END IF;
            END IF;
            
            -- Atualizar snapshot
            UPDATE snapshots 
            SET snapshot_data = transformed_data,
                schema_version = p_target_schema_version,
                metadata = COALESCE(metadata, '{}'::jsonb) || 
                          jsonb_build_object(
                              'migrated_from_schema', p_source_schema_version,
                              'migrated_at', CURRENT_TIMESTAMP
                          )
            WHERE snapshot_id = snapshot_record.snapshot_id;
            
            processed_count := processed_count + 1;
            
            -- Log progresso
            IF processed_count % 100 = 0 THEN
                RAISE NOTICE 'Processados % snapshots...', processed_count;
            END IF;
            
        EXCEPTION WHEN OTHERS THEN
            failed_count := failed_count + 1;
            RAISE WARNING 'Falha ao processar snapshot %: %', 
                         snapshot_record.snapshot_id, SQLERRM;
            
            -- Se muitas falhas, abortar
            IF failed_count > 10 THEN
                RAISE EXCEPTION 'Muitas falhas na migração de snapshots (%). Abortando.', failed_count;
            END IF;
        END;
    END LOOP;
    
    -- Atualizar log de sucesso
    UPDATE data_migration_log 
    SET completed_at = CURRENT_TIMESTAMP,
        status = 'COMPLETED',
        records_processed = processed_count,
        records_failed = failed_count
    WHERE id = migration_id;
    
    RAISE NOTICE 'Migração de snapshots concluída: % processados, % falharam', 
                 processed_count, failed_count;
    RETURN processed_count;
    
EXCEPTION WHEN OTHERS THEN
    -- Registrar falha e tentar rollback
    UPDATE data_migration_log 
    SET status = 'FAILED',
        error_message = SQLERRM,
        records_processed = processed_count,
        records_failed = failed_count
    WHERE id = migration_id;
    
    BEGIN
        PERFORM restore_from_backup(backup_table_name, 'snapshots');
        UPDATE data_migration_log SET rollback_executed = TRUE WHERE id = migration_id;
        RAISE NOTICE 'Rollback de snapshots executado com sucesso';
    EXCEPTION WHEN OTHERS THEN
        RAISE WARNING 'Falha no rollback de snapshots: %', SQLERRM;
    END;
    
    RAISE;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- FUNÇÕES DE VALIDAÇÃO PÓS-MIGRAÇÃO
-- =====================================================

-- Função para validar migração de dados
CREATE OR REPLACE FUNCTION validate_data_migration(
    p_migration_id INTEGER
)
RETURNS TABLE (
    validation_type TEXT,
    status TEXT,
    message TEXT,
    details JSONB
) AS $$
DECLARE
    migration_record RECORD;
    backup_count BIGINT;
    current_count BIGINT;
    validation_errors INTEGER := 0;
BEGIN
    -- Obter informações da migração
    SELECT * INTO migration_record 
    FROM data_migration_log 
    WHERE id = p_migration_id;
    
    IF NOT FOUND THEN
        RETURN QUERY SELECT 
            'MIGRATION_NOT_FOUND'::TEXT,
            'ERROR'::TEXT,
            format('Migração com ID % não encontrada', p_migration_id)::TEXT,
            '{}'::JSONB;
        RETURN;
    END IF;
    
    -- Validar contagem de registros
    IF migration_record.backup_table_name IS NOT NULL THEN
        EXECUTE format('SELECT COUNT(*) FROM %I', migration_record.backup_table_name) 
        INTO backup_count;
        
        SELECT COUNT(*) INTO current_count FROM events;
        
        RETURN QUERY SELECT 
            'RECORD_COUNT'::TEXT,
            CASE WHEN backup_count = current_count THEN 'OK' ELSE 'WARNING' END,
            format('Backup: % registros, Atual: % registros', backup_count, current_count)::TEXT,
            jsonb_build_object('backup_count', backup_count, 'current_count', current_count);
    END IF;
    
    -- Validar integridade de eventos
    RETURN QUERY
    SELECT 
        'EVENT_INTEGRITY'::TEXT,
        CASE WHEN COUNT(*) = 0 THEN 'OK' ELSE 'ERROR' END,
        format('% problemas de integridade encontrados', COUNT(*))::TEXT,
        jsonb_agg(
            jsonb_build_object(
                'aggregate_id', ves.aggregate_id,
                'issue', ves.issue_type,
                'message', ves.message
            )
        )
    FROM validate_event_sequence() ves;
    
    -- Validar snapshots se aplicável
    IF migration_record.migration_name LIKE '%snapshot%' THEN
        RETURN QUERY
        SELECT 
            'SNAPSHOT_INTEGRITY'::TEXT,
            CASE WHEN COUNT(*) = 0 THEN 'OK' ELSE 'ERROR' END,
            format('% problemas de snapshots encontrados', COUNT(*))::TEXT,
            jsonb_agg(
                jsonb_build_object(
                    'snapshot_id', vsi.snapshot_id,
                    'issue', vsi.issue_type,
                    'message', vsi.message
                )
            )
        FROM validate_snapshot_integrity() vsi;
    END IF;
    
    -- Validar projeções
    RETURN QUERY
    SELECT 
        'PROJECTION_STATUS'::TEXT,
        CASE WHEN COUNT(*) = 0 THEN 'OK' ELSE 'WARNING' END,
        format('% projeções com problemas', COUNT(*))::TEXT,
        jsonb_agg(
            jsonb_build_object(
                'projection_name', vpc.projection_name,
                'issue', vpc.issue_type,
                'message', vpc.message
            )
        )
    FROM validate_projection_consistency() vpc
    WHERE vpc.severity IN ('ERROR', 'WARNING');
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- FUNÇÕES DE LIMPEZA
-- =====================================================

-- Função para limpar backups antigos
CREATE OR REPLACE FUNCTION cleanup_migration_backups(
    p_retention_days INTEGER DEFAULT 30
)
RETURNS INTEGER AS $$
DECLARE
    backup_table RECORD;
    dropped_count INTEGER := 0;
BEGIN
    -- Buscar backups antigos
    FOR backup_table IN
        SELECT backup_table_name, started_at
        FROM data_migration_log
        WHERE backup_table_name IS NOT NULL
        AND started_at < CURRENT_TIMESTAMP - (p_retention_days || ' days')::INTERVAL
        AND status = 'COMPLETED'
    LOOP
        -- Verificar se tabela ainda existe
        IF EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_schema = 'eventstore' 
                   AND table_name = backup_table.backup_table_name) THEN
            
            EXECUTE format('DROP TABLE %I', backup_table.backup_table_name);
            dropped_count := dropped_count + 1;
            
            RAISE NOTICE 'Backup removido: % (criado em %)', 
                        backup_table.backup_table_name, backup_table.started_at;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Limpeza concluída: % backups removidos', dropped_count;
    RETURN dropped_count;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- VIEWS PARA MONITORAMENTO
-- =====================================================

-- View para status das migrações
CREATE OR REPLACE VIEW v_migration_status AS
SELECT 
    dml.id,
    dml.migration_name,
    dml.source_version,
    dml.target_version,
    dml.status,
    dml.started_at,
    dml.completed_at,
    EXTRACT(EPOCH FROM (COALESCE(dml.completed_at, CURRENT_TIMESTAMP) - dml.started_at)) / 60 as duration_minutes,
    dml.records_processed,
    dml.records_failed,
    CASE 
        WHEN dml.records_processed + dml.records_failed > 0 
        THEN ROUND((dml.records_processed::FLOAT / (dml.records_processed + dml.records_failed)) * 100, 2)
        ELSE 0 
    END as success_rate_percent,
    dml.backup_table_name,
    dml.rollback_executed,
    dml.error_message
FROM data_migration_log dml
ORDER BY dml.started_at DESC;

-- =====================================================
-- EXEMPLOS DE USO
-- =====================================================

-- Função de exemplo: migração para adicionar schema_version
CREATE OR REPLACE FUNCTION example_migrate_add_schema_version()
RETURNS BIGINT AS $$
BEGIN
    RETURN migrate_events_schema(
        'Add Schema Version to Events',
        '1.0',
        '1.1',
        'add_schema_version'
    );
END;
$$ LANGUAGE plpgsql;

-- Função de exemplo: migração de snapshots v1 para v2
CREATE OR REPLACE FUNCTION example_migrate_snapshots_v1_to_v2()
RETURNS BIGINT AS $$
BEGIN
    RETURN migrate_snapshots_schema(
        'Migrate Snapshots Schema v1 to v2',
        1,
        2
    );
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- COMENTÁRIOS PARA DOCUMENTAÇÃO
-- =====================================================

COMMENT ON FUNCTION create_migration_backup(TEXT, TEXT) IS 'Cria backup de tabela antes de migração de dados';
COMMENT ON FUNCTION restore_from_backup(TEXT, TEXT) IS 'Restaura dados de backup em caso de falha na migração';
COMMENT ON FUNCTION migrate_events_schema(TEXT, TEXT, TEXT, TEXT) IS 'Migra eventos aplicando transformações de schema';
COMMENT ON FUNCTION migrate_snapshots_schema(TEXT, INTEGER, INTEGER) IS 'Migra snapshots entre versões de schema';
COMMENT ON FUNCTION validate_data_migration(INTEGER) IS 'Valida integridade após migração de dados';
COMMENT ON FUNCTION cleanup_migration_backups(INTEGER) IS 'Remove backups de migração antigos';

COMMENT ON TABLE data_migration_log IS 'Log de todas as migrações de dados executadas';
COMMENT ON VIEW v_migration_status IS 'View de status e estatísticas das migrações';

-- =====================================================
-- INSTRUÇÕES DE USO
-- =====================================================

/*
INSTRUÇÕES DE USO:

1. MIGRAÇÃO SIMPLES DE EVENTOS:
   SELECT migrate_events_schema('Migration Name', '1.0', '1.1', 'add_schema_version');

2. MIGRAÇÃO DE SNAPSHOTS:
   SELECT migrate_snapshots_schema('Snapshot Migration', 1, 2);

3. VALIDAR MIGRAÇÃO:
   SELECT * FROM validate_data_migration(migration_id);

4. VER STATUS DAS MIGRAÇÕES:
   SELECT * FROM v_migration_status;

5. LIMPEZA DE BACKUPS:
   SELECT cleanup_migration_backups(30); -- Remove backups > 30 dias

6. ROLLBACK MANUAL:
   SELECT restore_from_backup('backup_table_name', 'target_table');

EXEMPLO COMPLETO:

-- 1. Executar migração
SELECT migrate_events_schema('Add Schema Version', '1.0', '1.1', 'add_schema_version');

-- 2. Validar resultado
SELECT * FROM validate_data_migration(currval('data_migration_log_id_seq'));

-- 3. Se tudo OK, limpar backups antigos
SELECT cleanup_migration_backups(7);
*/

-- =====================================================
-- FIM DO SCRIPT DE MIGRAÇÃO DE DADOS
-- =====================================================