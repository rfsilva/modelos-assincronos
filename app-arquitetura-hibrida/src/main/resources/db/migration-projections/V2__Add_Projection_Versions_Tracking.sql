-- =====================================================
-- Migration V2: Tabela de Versionamento de Projeções
-- =====================================================
-- Autor: Principal Java Architect
-- Data: 2026-03-11
-- Descrição: Adiciona suporte a versionamento de projeções
--            para permitir evolução de schema e rebuild
-- =====================================================

-- Definir schema padrão
SET search_path TO eventstore, projections;

-- =====================================================
-- TABELA: projection_versions
-- Descrição: Controle de versão de projeções para rebuild
-- =====================================================
CREATE TABLE IF NOT EXISTS eventstore.projection_versions (
    projection_name VARCHAR(100) NOT NULL,
    version INTEGER NOT NULL,
    schema_hash VARCHAR(64) NOT NULL,  -- SHA-256 hash do schema
    description TEXT,

    -- Metadados da versão
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',

    -- Status da migração
    migration_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, FAILED
    migration_started_at TIMESTAMP,
    migration_completed_at TIMESTAMP,
    migration_error TEXT,

    -- Informações adicionais
    backward_compatible BOOLEAN DEFAULT FALSE,
    requires_rebuild BOOLEAN DEFAULT TRUE,
    estimated_rebuild_time_seconds INTEGER,

    -- Campos de rastreamento
    events_to_process BIGINT DEFAULT 0,
    events_processed BIGINT DEFAULT 0,

    -- Primary key composta
    PRIMARY KEY (projection_name, version)
);

-- =====================================================
-- TABELA: projection_rebuild_history
-- Descrição: Histórico de rebuilds de projeções
-- =====================================================
CREATE TABLE IF NOT EXISTS eventstore.projection_rebuild_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    projection_name VARCHAR(100) NOT NULL,
    from_version INTEGER,
    to_version INTEGER NOT NULL,
    rebuild_type VARCHAR(20) NOT NULL, -- FULL, INCREMENTAL, SCHEMA_CHANGE

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED

    -- Timing
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    paused_at TIMESTAMP,
    resumed_at TIMESTAMP,

    -- Progress
    total_events BIGINT DEFAULT 0,
    processed_events BIGINT DEFAULT 0,
    failed_events BIGINT DEFAULT 0,
    progress_percentage DECIMAL(5,2) DEFAULT 0.0,

    -- Performance
    events_per_second DECIMAL(10,2),
    estimated_time_remaining_seconds INTEGER,

    -- Error tracking
    error_message TEXT,
    error_details JSONB,
    retry_count INTEGER DEFAULT 0,

    -- Metadata
    triggered_by VARCHAR(100),
    rebuild_reason TEXT,
    configuration JSONB, -- Configurações específicas do rebuild

    -- Auditoria
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TABELA: projection_schema_changes
-- Descrição: Log de mudanças de schema detectadas
-- =====================================================
CREATE TABLE IF NOT EXISTS eventstore.projection_schema_changes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    projection_name VARCHAR(100) NOT NULL,
    old_version INTEGER,
    new_version INTEGER NOT NULL,

    -- Detalhes da mudança
    change_type VARCHAR(30) NOT NULL, -- COLUMN_ADDED, COLUMN_REMOVED, COLUMN_MODIFIED, INDEX_ADDED, etc.
    change_description TEXT NOT NULL,
    change_details JSONB,

    -- Impacto
    breaking_change BOOLEAN DEFAULT FALSE,
    requires_data_migration BOOLEAN DEFAULT FALSE,
    affects_existing_queries BOOLEAN DEFAULT FALSE,

    -- Timestamps
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    applied_at TIMESTAMP,

    -- Metadata
    detected_by VARCHAR(100) DEFAULT 'AUTO_DETECTION',
    impact_assessment TEXT
);

-- =====================================================
-- ÍNDICES PARA PERFORMANCE
-- =====================================================

-- Índices para projection_versions
CREATE INDEX idx_projection_versions_name ON eventstore.projection_versions(projection_name);
CREATE INDEX idx_projection_versions_status ON eventstore.projection_versions(migration_status);
CREATE INDEX idx_projection_versions_created_at ON eventstore.projection_versions(created_at DESC);
CREATE INDEX idx_projection_versions_rebuild_required ON eventstore.projection_versions(requires_rebuild)
    WHERE requires_rebuild = true;

-- Índices para projection_rebuild_history
CREATE INDEX idx_rebuild_history_projection ON eventstore.projection_rebuild_history(projection_name);
CREATE INDEX idx_rebuild_history_status ON eventstore.projection_rebuild_history(status);
CREATE INDEX idx_rebuild_history_started_at ON eventstore.projection_rebuild_history(started_at DESC);
CREATE INDEX idx_rebuild_history_active ON eventstore.projection_rebuild_history(status, projection_name)
    WHERE status IN ('RUNNING', 'PAUSED');

-- Índices para projection_schema_changes
CREATE INDEX idx_schema_changes_projection ON eventstore.projection_schema_changes(projection_name);
CREATE INDEX idx_schema_changes_version ON eventstore.projection_schema_changes(new_version);
CREATE INDEX idx_schema_changes_detected_at ON eventstore.projection_schema_changes(detected_at DESC);
CREATE INDEX idx_schema_changes_breaking ON eventstore.projection_schema_changes(breaking_change)
    WHERE breaking_change = true;

-- =====================================================
-- FUNÇÕES AUXILIARES
-- =====================================================

-- Função para atualizar timestamp updated_at automaticamente
CREATE OR REPLACE FUNCTION update_projection_rebuild_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Função para calcular progresso percentage
CREATE OR REPLACE FUNCTION calculate_rebuild_progress()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.total_events > 0 THEN
        NEW.progress_percentage = ROUND((NEW.processed_events::DECIMAL / NEW.total_events::DECIMAL) * 100, 2);
    ELSE
        NEW.progress_percentage = 0.0;
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Função para calcular events per second
CREATE OR REPLACE FUNCTION calculate_rebuild_performance()
RETURNS TRIGGER AS $$
DECLARE
    elapsed_seconds DECIMAL;
BEGIN
    IF NEW.status = 'RUNNING' AND NEW.started_at IS NOT NULL THEN
        elapsed_seconds = EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - NEW.started_at));
        IF elapsed_seconds > 0 THEN
            NEW.events_per_second = ROUND(NEW.processed_events::DECIMAL / elapsed_seconds, 2);

            -- Calcular tempo estimado restante
            IF NEW.events_per_second > 0 AND NEW.total_events > NEW.processed_events THEN
                NEW.estimated_time_remaining_seconds = ROUND(
                    (NEW.total_events - NEW.processed_events)::DECIMAL / NEW.events_per_second
                )::INTEGER;
            END IF;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Trigger para atualizar updated_at em projection_rebuild_history
CREATE TRIGGER trigger_rebuild_history_updated_at
    BEFORE UPDATE ON eventstore.projection_rebuild_history
    FOR EACH ROW
    EXECUTE FUNCTION update_projection_rebuild_updated_at();

-- Trigger para calcular progresso automaticamente
CREATE TRIGGER trigger_rebuild_calculate_progress
    BEFORE UPDATE ON eventstore.projection_rebuild_history
    FOR EACH ROW
    WHEN (OLD.processed_events IS DISTINCT FROM NEW.processed_events)
    EXECUTE FUNCTION calculate_rebuild_progress();

-- Trigger para calcular performance automaticamente
CREATE TRIGGER trigger_rebuild_calculate_performance
    BEFORE UPDATE ON eventstore.projection_rebuild_history
    FOR EACH ROW
    WHEN (OLD.processed_events IS DISTINCT FROM NEW.processed_events OR OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE FUNCTION calculate_rebuild_performance();

-- =====================================================
-- VIEWS ÚTEIS
-- =====================================================

-- View para projeções que precisam de rebuild
CREATE OR REPLACE VIEW eventstore.projections_needing_rebuild AS
SELECT
    pv.projection_name,
    pv.version,
    pv.schema_hash,
    pv.description,
    pv.created_at,
    pv.requires_rebuild,
    pv.estimated_rebuild_time_seconds,
    pt.last_processed_event_id,
    pt.status as projection_status,
    pt.events_processed,
    pt.events_failed
FROM eventstore.projection_versions pv
LEFT JOIN eventstore.projection_tracking pt ON pv.projection_name = pt.projection_name
WHERE pv.requires_rebuild = true
  AND pv.migration_status IN ('PENDING', 'FAILED')
ORDER BY pv.created_at DESC;

-- View para rebuilds ativos
CREATE OR REPLACE VIEW eventstore.active_rebuilds AS
SELECT
    prh.id,
    prh.projection_name,
    prh.rebuild_type,
    prh.status,
    prh.started_at,
    prh.paused_at,
    prh.total_events,
    prh.processed_events,
    prh.failed_events,
    prh.progress_percentage,
    prh.events_per_second,
    prh.estimated_time_remaining_seconds,
    CASE
        WHEN prh.status = 'PAUSED' THEN 'Pausado'
        WHEN prh.estimated_time_remaining_seconds < 60 THEN 'Quase concluído'
        ELSE CONCAT(ROUND(prh.estimated_time_remaining_seconds / 60.0), ' minutos restantes')
    END as time_remaining_display
FROM eventstore.projection_rebuild_history prh
WHERE prh.status IN ('RUNNING', 'PAUSED')
ORDER BY prh.started_at DESC;

-- View para histórico de mudanças de schema
CREATE OR REPLACE VIEW eventstore.schema_changes_summary AS
SELECT
    psc.projection_name,
    COUNT(*) as total_changes,
    COUNT(*) FILTER (WHERE psc.breaking_change = true) as breaking_changes,
    COUNT(*) FILTER (WHERE psc.requires_data_migration = true) as migration_required,
    MAX(psc.detected_at) as last_change_detected,
    MAX(psc.new_version) as latest_version
FROM eventstore.projection_schema_changes psc
GROUP BY psc.projection_name
ORDER BY last_change_detected DESC;

-- =====================================================
-- COMENTÁRIOS PARA DOCUMENTAÇÃO
-- =====================================================

COMMENT ON TABLE eventstore.projection_versions IS 'Controle de versão de projeções para gerenciamento de evolução de schema';
COMMENT ON COLUMN eventstore.projection_versions.schema_hash IS 'Hash SHA-256 do schema da projeção para detectar mudanças';
COMMENT ON COLUMN eventstore.projection_versions.backward_compatible IS 'Indica se a mudança é compatível com versões anteriores';
COMMENT ON COLUMN eventstore.projection_versions.requires_rebuild IS 'Indica se a projeção precisa ser reconstruída';

COMMENT ON TABLE eventstore.projection_rebuild_history IS 'Histórico completo de todos os rebuilds executados';
COMMENT ON COLUMN eventstore.projection_rebuild_history.rebuild_type IS 'Tipo de rebuild: FULL (completo), INCREMENTAL (desde último checkpoint), SCHEMA_CHANGE (apenas schema)';

COMMENT ON TABLE eventstore.projection_schema_changes IS 'Log de todas as mudanças de schema detectadas automaticamente';
COMMENT ON COLUMN eventstore.projection_schema_changes.breaking_change IS 'Indica se a mudança quebra compatibilidade com versão anterior';

COMMENT ON VIEW eventstore.projections_needing_rebuild IS 'Projeções que precisam ser reconstruídas';
COMMENT ON VIEW eventstore.active_rebuilds IS 'Rebuilds atualmente em execução ou pausados';
COMMENT ON VIEW eventstore.schema_changes_summary IS 'Sumário de mudanças de schema por projeção';

-- =====================================================
-- DADOS INICIAIS
-- =====================================================

-- Registrar versões iniciais das projeções existentes
INSERT INTO eventstore.projection_versions (projection_name, version, schema_hash, description, backward_compatible, requires_rebuild)
VALUES
    ('SinistroProjection', 1, 'initial', 'Versão inicial da projeção de sinistros', true, false),
    ('DetranConsultaProjection', 1, 'initial', 'Versão inicial da projeção de consultas DETRAN', true, false),
    ('EventTimelineProjection', 1, 'initial', 'Versão inicial da timeline de eventos', true, false),
    ('MetricasAgregadasProjection', 1, 'initial', 'Versão inicial de métricas agregadas', true, false)
ON CONFLICT (projection_name, version) DO NOTHING;

-- =====================================================
-- FIM DA MIGRATION V2
-- =====================================================
