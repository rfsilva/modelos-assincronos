-- =====================================================
-- Script Auxiliar: Validação Automática de Integridade
-- =====================================================
-- Autor: Principal Java Architect
-- Data: 2024-12-19
-- Descrição: Sistema completo de validação de integridade
--            para Event Store e projeções CQRS
-- 
-- Funcionalidades:
-- - Validação de consistência de eventos
-- - Verificação de integridade de snapshots
-- - Auditoria de projeções
-- - Detecção de anomalias
-- =====================================================

-- Definir schema padrão
SET search_path TO eventstore, public;

-- =====================================================
-- FUNÇÕES DE VALIDAÇÃO DE EVENTOS
-- =====================================================

-- Função para validar sequência de versões por aggregate
CREATE OR REPLACE FUNCTION validate_event_sequence()
RETURNS TABLE (
    aggregate_id VARCHAR(255),
    issue_type TEXT,
    expected_version BIGINT,
    actual_version BIGINT,
    event_count BIGINT,
    message TEXT
) AS $$
BEGIN
    RETURN QUERY
    WITH version_analysis AS (
        SELECT 
            e.aggregate_id,
            e.version,
            ROW_NUMBER() OVER (PARTITION BY e.aggregate_id ORDER BY e.timestamp, e.version) - 1 as expected_version,
            COUNT(*) OVER (PARTITION BY e.aggregate_id) as total_events
        FROM events e
        ORDER BY e.aggregate_id, e.timestamp, e.version
    )
    SELECT 
        va.aggregate_id,
        'VERSION_GAP'::TEXT as issue_type,
        va.expected_version,
        va.version as actual_version,
        va.total_events,
        format('Aggregate %s tem gap na versão. Esperado: %s, Encontrado: %s', 
               va.aggregate_id, va.expected_version, va.version) as message
    FROM version_analysis va
    WHERE va.version != va.expected_version;
    
    -- Verificar duplicatas de versão
    RETURN QUERY
    SELECT 
        e.aggregate_id,
        'DUPLICATE_VERSION'::TEXT as issue_type,
        e.version as expected_version,
        e.version as actual_version,
        COUNT(*) as event_count,
        format('Aggregate %s tem %s eventos com versão %s', 
               e.aggregate_id, COUNT(*), e.version) as message
    FROM events e
    GROUP BY e.aggregate_id, e.version
    HAVING COUNT(*) > 1;
    
    -- Verificar timestamps inconsistentes
    RETURN QUERY
    WITH timestamp_issues AS (
        SELECT 
            e1.aggregate_id,
            e1.version as version1,
            e2.version as version2,
            e1.timestamp as ts1,
            e2.timestamp as ts2
        FROM events e1
        JOIN events e2 ON e1.aggregate_id = e2.aggregate_id
        WHERE e1.version < e2.version AND e1.timestamp > e2.timestamp
    )
    SELECT 
        ti.aggregate_id,
        'TIMESTAMP_ORDER'::TEXT as issue_type,
        ti.version1 as expected_version,
        ti.version2 as actual_version,
        2::BIGINT as event_count,
        format('Aggregate %s: evento v%s (ts: %s) posterior ao v%s (ts: %s)', 
               ti.aggregate_id, ti.version1, ti.ts1, ti.version2, ti.ts2) as message
    FROM timestamp_issues ti;
END;
$$ LANGUAGE plpgsql;

-- Função para validar integridade de dados JSON
CREATE OR REPLACE FUNCTION validate_event_data_integrity()
RETURNS TABLE (
    event_id UUID,
    aggregate_id VARCHAR(255),
    issue_type TEXT,
    message TEXT,
    severity TEXT
) AS $$
BEGIN
    -- Verificar eventos com event_data NULL ou vazio
    RETURN QUERY
    SELECT 
        e.id,
        e.aggregate_id,
        'MISSING_EVENT_DATA'::TEXT as issue_type,
        'Evento sem dados (event_data é NULL ou vazio)' as message,
        'ERROR'::TEXT as severity
    FROM events e
    WHERE e.event_data IS NULL OR e.event_data = '{}'::jsonb;
    
    -- Verificar eventos com data_size inconsistente
    RETURN QUERY
    SELECT 
        e.id,
        e.aggregate_id,
        'INCONSISTENT_DATA_SIZE'::TEXT as issue_type,
        format('data_size (%s) não corresponde ao tamanho real (%s bytes)', 
               e.data_size, length(e.event_data::text)) as message,
        CASE 
            WHEN abs(e.data_size - length(e.event_data::text)) > 100 THEN 'ERROR'
            ELSE 'WARNING'
        END as severity
    FROM events e
    WHERE e.data_size > 0 
    AND abs(e.data_size - length(e.event_data::text)) > 10;
    
    -- Verificar eventos com timestamps futuros
    RETURN QUERY
    SELECT 
        e.id,
        e.aggregate_id,
        'FUTURE_TIMESTAMP'::TEXT as issue_type,
        format('Timestamp do evento (%s) está no futuro', e.timestamp) as message,
        'WARNING'::TEXT as severity
    FROM events e
    WHERE e.timestamp > CURRENT_TIMESTAMP + INTERVAL '1 hour';
    
    -- Verificar eventos muito antigos (possível problema de migração)
    RETURN QUERY
    SELECT 
        e.id,
        e.aggregate_id,
        'VERY_OLD_EVENT'::TEXT as issue_type,
        format('Evento muito antigo (%s) - possível problema de migração', e.timestamp) as message,
        'INFO'::TEXT as severity
    FROM events e
    WHERE e.timestamp < CURRENT_TIMESTAMP - INTERVAL '5 years';
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- FUNÇÕES DE VALIDAÇÃO DE SNAPSHOTS
-- =====================================================

-- Função para validar integridade de snapshots
CREATE OR REPLACE FUNCTION validate_snapshot_integrity()
RETURNS TABLE (
    snapshot_id UUID,
    aggregate_id VARCHAR(255),
    issue_type TEXT,
    message TEXT,
    severity TEXT
) AS $$
BEGIN
    -- Verificar snapshots sem eventos correspondentes
    RETURN QUERY
    SELECT 
        s.snapshot_id,
        s.aggregate_id,
        'ORPHANED_SNAPSHOT'::TEXT as issue_type,
        'Snapshot sem eventos correspondentes no Event Store' as message,
        'ERROR'::TEXT as severity
    FROM snapshots s
    WHERE NOT EXISTS (
        SELECT 1 FROM events e 
        WHERE e.aggregate_id = s.aggregate_id
    );
    
    -- Verificar snapshots com versão maior que eventos
    RETURN QUERY
    SELECT 
        s.snapshot_id,
        s.aggregate_id,
        'SNAPSHOT_VERSION_AHEAD'::TEXT as issue_type,
        format('Snapshot v%s maior que última versão de evento v%s', 
               s.version, COALESCE(max_event.max_version, 0)) as message,
        'ERROR'::TEXT as severity
    FROM snapshots s
    LEFT JOIN (
        SELECT aggregate_id, MAX(version) as max_version
        FROM events
        GROUP BY aggregate_id
    ) max_event ON max_event.aggregate_id = s.aggregate_id
    WHERE s.version > COALESCE(max_event.max_version, 0);
    
    -- Verificar integridade de hash (se disponível)
    RETURN QUERY
    SELECT 
        s.snapshot_id,
        s.aggregate_id,
        'INVALID_HASH'::TEXT as issue_type,
        'Hash do snapshot não corresponde aos dados' as message,
        'ERROR'::TEXT as severity
    FROM snapshots s
    WHERE s.data_hash IS NOT NULL 
    AND s.data_hash != encode(sha256(s.snapshot_data::text::bytea), 'hex');
    
    -- Verificar compressão inconsistente
    RETURN QUERY
    SELECT 
        s.snapshot_id,
        s.aggregate_id,
        'COMPRESSION_INCONSISTENT'::TEXT as issue_type,
        format('Dados de compressão inconsistentes: compressed=%s, original_size=%s, compressed_size=%s', 
               s.compressed, s.original_size, s.compressed_size) as message,
        'WARNING'::TEXT as severity
    FROM snapshots s
    WHERE (s.compressed = true AND (s.original_size IS NULL OR s.compressed_size IS NULL))
    OR (s.compressed = false AND (s.original_size IS NOT NULL OR s.compressed_size IS NOT NULL));
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- FUNÇÕES DE VALIDAÇÃO DE PROJEÇÕES
-- =====================================================

-- Função para validar consistência das projeções
CREATE OR REPLACE FUNCTION validate_projection_consistency()
RETURNS TABLE (
    projection_name VARCHAR(100),
    issue_type TEXT,
    message TEXT,
    severity TEXT,
    last_processed_event_id BIGINT,
    max_available_event_id BIGINT,
    lag_events BIGINT
) AS $$
DECLARE
    max_event_id BIGINT;
BEGIN
    -- Obter ID máximo de evento disponível
    SELECT COALESCE(MAX(EXTRACT(EPOCH FROM timestamp) * 1000000), 0)::BIGINT 
    INTO max_event_id 
    FROM events;
    
    -- Verificar projeções com lag alto
    RETURN QUERY
    SELECT 
        pt.projection_name,
        'HIGH_LAG'::TEXT as issue_type,
        format('Projeção com lag de %s eventos (%s horas atrás)', 
               max_event_id - pt.last_processed_event_id,
               ROUND(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - pt.last_processed_at)) / 3600, 2)) as message,
        CASE 
            WHEN pt.last_processed_at < CURRENT_TIMESTAMP - INTERVAL '1 day' THEN 'ERROR'
            WHEN pt.last_processed_at < CURRENT_TIMESTAMP - INTERVAL '1 hour' THEN 'WARNING'
            ELSE 'INFO'
        END as severity,
        pt.last_processed_event_id,
        max_event_id,
        max_event_id - pt.last_processed_event_id as lag_events
    FROM projection_tracking pt
    WHERE pt.status = 'ACTIVE'
    AND (max_event_id - pt.last_processed_event_id) > 100;
    
    -- Verificar projeções com alta taxa de erro
    RETURN QUERY
    SELECT 
        pt.projection_name,
        'HIGH_ERROR_RATE'::TEXT as issue_type,
        format('Taxa de erro alta: %.2f%% (%s falhas de %s total)', 
               (pt.events_failed::FLOAT / NULLIF(pt.events_processed + pt.events_failed, 0)) * 100,
               pt.events_failed,
               pt.events_processed + pt.events_failed) as message,
        CASE 
            WHEN (pt.events_failed::FLOAT / NULLIF(pt.events_processed + pt.events_failed, 0)) > 0.1 THEN 'ERROR'
            WHEN (pt.events_failed::FLOAT / NULLIF(pt.events_processed + pt.events_failed, 0)) > 0.05 THEN 'WARNING'
            ELSE 'INFO'
        END as severity,
        pt.last_processed_event_id,
        max_event_id,
        max_event_id - pt.last_processed_event_id as lag_events
    FROM projection_tracking pt
    WHERE pt.events_processed + pt.events_failed > 0
    AND (pt.events_failed::FLOAT / (pt.events_processed + pt.events_failed)) > 0.01;
    
    -- Verificar projeções paradas
    RETURN QUERY
    SELECT 
        pt.projection_name,
        'PROJECTION_STOPPED'::TEXT as issue_type,
        format('Projeção parada há %s horas (status: %s)', 
               ROUND(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - pt.last_processed_at)) / 3600, 2),
               pt.status) as message,
        'ERROR'::TEXT as severity,
        pt.last_processed_event_id,
        max_event_id,
        max_event_id - pt.last_processed_event_id as lag_events
    FROM projection_tracking pt
    WHERE pt.status IN ('ERROR', 'PAUSED', 'DISABLED')
    OR pt.last_processed_at < CURRENT_TIMESTAMP - INTERVAL '2 hours';
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- FUNÇÃO PRINCIPAL DE VALIDAÇÃO
-- =====================================================

-- Função principal que executa todas as validações
CREATE OR REPLACE FUNCTION run_integrity_validation()
RETURNS TABLE (
    validation_type TEXT,
    entity_id TEXT,
    issue_type TEXT,
    message TEXT,
    severity TEXT,
    timestamp TIMESTAMP WITH TIME ZONE
) AS $$
BEGIN
    -- Validações de eventos
    RETURN QUERY
    SELECT 
        'EVENT_SEQUENCE'::TEXT as validation_type,
        ves.aggregate_id::TEXT as entity_id,
        ves.issue_type,
        ves.message,
        'ERROR'::TEXT as severity,
        CURRENT_TIMESTAMP as timestamp
    FROM validate_event_sequence() ves;
    
    RETURN QUERY
    SELECT 
        'EVENT_DATA'::TEXT as validation_type,
        vedi.event_id::TEXT as entity_id,
        vedi.issue_type,
        vedi.message,
        vedi.severity,
        CURRENT_TIMESTAMP as timestamp
    FROM validate_event_data_integrity() vedi;
    
    -- Validações de snapshots
    RETURN QUERY
    SELECT 
        'SNAPSHOT'::TEXT as validation_type,
        vsi.snapshot_id::TEXT as entity_id,
        vsi.issue_type,
        vsi.message,
        vsi.severity,
        CURRENT_TIMESTAMP as timestamp
    FROM validate_snapshot_integrity() vsi;
    
    -- Validações de projeções
    RETURN QUERY
    SELECT 
        'PROJECTION'::TEXT as validation_type,
        vpc.projection_name::TEXT as entity_id,
        vpc.issue_type,
        vpc.message,
        vpc.severity,
        CURRENT_TIMESTAMP as timestamp
    FROM validate_projection_consistency() vpc;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- SISTEMA DE RELATÓRIOS DE INTEGRIDADE
-- =====================================================

-- Tabela para armazenar histórico de validações
CREATE TABLE IF NOT EXISTS integrity_validation_log (
    id SERIAL PRIMARY KEY,
    validation_run_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    validation_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(255),
    issue_type VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolution_notes TEXT,
    
    CONSTRAINT chk_integrity_severity CHECK (
        severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')
    )
);

-- Índices para consultas de relatórios
CREATE INDEX IF NOT EXISTS idx_integrity_log_validation_run 
    ON integrity_validation_log (validation_run_id);
CREATE INDEX IF NOT EXISTS idx_integrity_log_detected_at 
    ON integrity_validation_log (detected_at DESC);
CREATE INDEX IF NOT EXISTS idx_integrity_log_severity 
    ON integrity_validation_log (severity, detected_at DESC);
CREATE INDEX IF NOT EXISTS idx_integrity_log_unresolved 
    ON integrity_validation_log (resolved_at) 
    WHERE resolved_at IS NULL;

-- Função para executar validação completa e salvar resultados
CREATE OR REPLACE FUNCTION run_and_log_integrity_validation()
RETURNS UUID AS $$
DECLARE
    run_id UUID := uuid_generate_v4();
    validation_record RECORD;
    total_issues INTEGER := 0;
    critical_issues INTEGER := 0;
BEGIN
    RAISE NOTICE 'Iniciando validação de integridade (ID: %)', run_id;
    
    -- Executar validações e salvar resultados
    FOR validation_record IN 
        SELECT * FROM run_integrity_validation()
    LOOP
        INSERT INTO integrity_validation_log (
            validation_run_id, validation_type, entity_id, 
            issue_type, message, severity, detected_at
        ) VALUES (
            run_id, validation_record.validation_type, validation_record.entity_id,
            validation_record.issue_type, validation_record.message, 
            validation_record.severity, validation_record.timestamp
        );
        
        total_issues := total_issues + 1;
        
        IF validation_record.severity = 'ERROR' THEN
            critical_issues := critical_issues + 1;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Validação concluída: % problemas encontrados (% críticos)', 
                 total_issues, critical_issues;
    
    RETURN run_id;
END;
$$ LANGUAGE plpgsql;

-- Função para gerar relatório de integridade
CREATE OR REPLACE FUNCTION generate_integrity_report(
    p_run_id UUID DEFAULT NULL,
    p_days_back INTEGER DEFAULT 7
)
RETURNS TABLE (
    summary_type TEXT,
    count BIGINT,
    details TEXT
) AS $$
DECLARE
    target_run_id UUID;
    start_date TIMESTAMP WITH TIME ZONE;
BEGIN
    -- Determinar run_id alvo
    IF p_run_id IS NULL THEN
        SELECT MAX(validation_run_id) INTO target_run_id
        FROM integrity_validation_log;
    ELSE
        target_run_id := p_run_id;
    END IF;
    
    start_date := CURRENT_TIMESTAMP - (p_days_back || ' days')::INTERVAL;
    
    -- Resumo por severidade
    RETURN QUERY
    SELECT 
        'SEVERITY_' || severity as summary_type,
        COUNT(*) as count,
        format('Problemas de severidade %s', severity) as details
    FROM integrity_validation_log
    WHERE (p_run_id IS NULL OR validation_run_id = target_run_id)
    AND detected_at >= start_date
    GROUP BY severity
    ORDER BY 
        CASE severity 
            WHEN 'CRITICAL' THEN 1
            WHEN 'ERROR' THEN 2
            WHEN 'WARNING' THEN 3
            WHEN 'INFO' THEN 4
        END;
    
    -- Resumo por tipo de validação
    RETURN QUERY
    SELECT 
        'TYPE_' || validation_type as summary_type,
        COUNT(*) as count,
        format('Problemas em %s', validation_type) as details
    FROM integrity_validation_log
    WHERE (p_run_id IS NULL OR validation_run_id = target_run_id)
    AND detected_at >= start_date
    GROUP BY validation_type
    ORDER BY COUNT(*) DESC;
    
    -- Top 5 tipos de problema mais comuns
    RETURN QUERY
    SELECT 
        'ISSUE_' || issue_type as summary_type,
        COUNT(*) as count,
        format('Ocorrências de %s', issue_type) as details
    FROM integrity_validation_log
    WHERE (p_run_id IS NULL OR validation_run_id = target_run_id)
    AND detected_at >= start_date
    GROUP BY issue_type
    ORDER BY COUNT(*) DESC
    LIMIT 5;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- FUNÇÕES DE CORREÇÃO AUTOMÁTICA
-- =====================================================

-- Função para tentar corrigir problemas simples automaticamente
CREATE OR REPLACE FUNCTION auto_fix_integrity_issues(
    p_run_id UUID,
    p_dry_run BOOLEAN DEFAULT TRUE
)
RETURNS TABLE (
    issue_id INTEGER,
    issue_type TEXT,
    action_taken TEXT,
    success BOOLEAN,
    message TEXT
) AS $$
DECLARE
    issue_record RECORD;
    fix_result BOOLEAN;
    fix_message TEXT;
BEGIN
    FOR issue_record IN 
        SELECT id, validation_type, entity_id, issue_type, message
        FROM integrity_validation_log
        WHERE validation_run_id = p_run_id
        AND resolved_at IS NULL
        AND severity IN ('WARNING', 'ERROR')
    LOOP
        fix_result := FALSE;
        fix_message := 'Correção não implementada para este tipo de problema';
        
        -- Tentar corrigir data_size inconsistente
        IF issue_record.issue_type = 'INCONSISTENT_DATA_SIZE' THEN
            IF NOT p_dry_run THEN
                BEGIN
                    UPDATE events 
                    SET data_size = length(event_data::text)
                    WHERE id = issue_record.entity_id::UUID;
                    
                    fix_result := TRUE;
                    fix_message := 'data_size corrigido automaticamente';
                EXCEPTION WHEN OTHERS THEN
                    fix_result := FALSE;
                    fix_message := 'Erro ao corrigir data_size: ' || SQLERRM;
                END;
            ELSE
                fix_result := TRUE;
                fix_message := 'DRY RUN: data_size seria corrigido';
            END IF;
        END IF;
        
        -- Marcar como resolvido se correção foi bem-sucedida
        IF fix_result AND NOT p_dry_run THEN
            UPDATE integrity_validation_log
            SET resolved_at = CURRENT_TIMESTAMP,
                resolution_notes = fix_message
            WHERE id = issue_record.id;
        END IF;
        
        RETURN QUERY SELECT 
            issue_record.id,
            issue_record.issue_type,
            CASE WHEN p_dry_run THEN 'DRY_RUN' ELSE 'EXECUTE' END,
            fix_result,
            fix_message;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- VIEWS PARA MONITORAMENTO
-- =====================================================

-- View para problemas não resolvidos
CREATE OR REPLACE VIEW v_unresolved_integrity_issues AS
SELECT 
    ivl.validation_type,
    ivl.entity_id,
    ivl.issue_type,
    ivl.message,
    ivl.severity,
    ivl.detected_at,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - ivl.detected_at)) / 3600 as hours_open
FROM integrity_validation_log ivl
WHERE ivl.resolved_at IS NULL
ORDER BY 
    CASE ivl.severity 
        WHEN 'CRITICAL' THEN 1
        WHEN 'ERROR' THEN 2
        WHEN 'WARNING' THEN 3
        WHEN 'INFO' THEN 4
    END,
    ivl.detected_at DESC;

-- View para resumo de saúde do sistema
CREATE OR REPLACE VIEW v_system_health_summary AS
SELECT 
    'EVENTS' as component,
    COUNT(*) as total_count,
    COUNT(*) FILTER (WHERE timestamp >= CURRENT_DATE) as today_count,
    pg_size_pretty(pg_total_relation_size('events')) as size
FROM events
UNION ALL
SELECT 
    'SNAPSHOTS' as component,
    COUNT(*) as total_count,
    COUNT(*) FILTER (WHERE timestamp >= CURRENT_DATE) as today_count,
    pg_size_pretty(pg_total_relation_size('snapshots')) as size
FROM snapshots
UNION ALL
SELECT 
    'PROJECTIONS' as component,
    COUNT(*) as total_count,
    COUNT(*) FILTER (WHERE status = 'ACTIVE') as today_count,
    'N/A' as size
FROM projection_tracking;

-- =====================================================
-- COMENTÁRIOS PARA DOCUMENTAÇÃO
-- =====================================================

COMMENT ON FUNCTION validate_event_sequence() IS 'Valida sequência e consistência de versões de eventos por aggregate';
COMMENT ON FUNCTION validate_event_data_integrity() IS 'Valida integridade dos dados JSON e metadados dos eventos';
COMMENT ON FUNCTION validate_snapshot_integrity() IS 'Valida integridade e consistência dos snapshots';
COMMENT ON FUNCTION validate_projection_consistency() IS 'Valida consistência e performance das projeções CQRS';
COMMENT ON FUNCTION run_integrity_validation() IS 'Executa todas as validações de integridade do sistema';
COMMENT ON FUNCTION run_and_log_integrity_validation() IS 'Executa validações e salva resultados no log para auditoria';
COMMENT ON FUNCTION generate_integrity_report(UUID, INTEGER) IS 'Gera relatório consolidado de integridade';
COMMENT ON FUNCTION auto_fix_integrity_issues(UUID, BOOLEAN) IS 'Tenta corrigir automaticamente problemas simples de integridade';

COMMENT ON TABLE integrity_validation_log IS 'Log histórico de todas as validações de integridade executadas';
COMMENT ON VIEW v_unresolved_integrity_issues IS 'View de problemas de integridade não resolvidos';
COMMENT ON VIEW v_system_health_summary IS 'View de resumo geral da saúde do sistema';

-- =====================================================
-- EXECUÇÃO INICIAL
-- =====================================================

-- Executar validação inicial
DO $$
DECLARE
    initial_run_id UUID;
BEGIN
    RAISE NOTICE 'Executando validação inicial de integridade...';
    initial_run_id := run_and_log_integrity_validation();
    RAISE NOTICE 'Validação inicial concluída. Run ID: %', initial_run_id;
    RAISE NOTICE 'Para ver o relatório: SELECT * FROM generate_integrity_report(''%'');', initial_run_id;
END $$;

-- =====================================================
-- INSTRUÇÕES DE USO
-- =====================================================

/*
INSTRUÇÕES DE USO:

1. VALIDAÇÃO MANUAL:
   SELECT * FROM run_integrity_validation();

2. VALIDAÇÃO COM LOG:
   SELECT run_and_log_integrity_validation();

3. RELATÓRIO DE INTEGRIDADE:
   SELECT * FROM generate_integrity_report();

4. VER PROBLEMAS NÃO RESOLVIDOS:
   SELECT * FROM v_unresolved_integrity_issues;

5. CORREÇÃO AUTOMÁTICA (DRY RUN):
   SELECT * FROM auto_fix_integrity_issues('run_id', true);

6. CORREÇÃO AUTOMÁTICA (EXECUTAR):
   SELECT * FROM auto_fix_integrity_issues('run_id', false);

7. AGENDAR VALIDAÇÃO DIÁRIA (CRON):
   0 1 * * * psql -d eventstore -c "SELECT run_and_log_integrity_validation();"

8. MONITORAMENTO CONTÍNUO:
   SELECT * FROM v_system_health_summary;
*/

-- =====================================================
-- FIM DO SCRIPT DE VALIDAÇÃO DE INTEGRIDADE
-- =====================================================