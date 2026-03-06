-- Migration V5: Criação das tabelas para sistema de arquivamento
-- US007 - Event Store com Particionamento e Arquivamento

-- Tabela para metadados de arquivos
CREATE TABLE IF NOT EXISTS event_archives (
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
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Índices para consultas otimizadas
CREATE INDEX IF NOT EXISTS idx_event_archives_status 
    ON event_archives (status);

CREATE INDEX IF NOT EXISTS idx_event_archives_archived_at 
    ON event_archives (archived_at);

CREATE INDEX IF NOT EXISTS idx_event_archives_partition_name 
    ON event_archives (partition_name);

CREATE INDEX IF NOT EXISTS idx_event_archives_storage_type 
    ON event_archives (storage_type, status);

-- Tabela para log de operações de arquivamento
CREATE TABLE IF NOT EXISTS archive_operations_log (
    id SERIAL PRIMARY KEY,
    operation_type VARCHAR(20) NOT NULL, -- ARCHIVE, RESTORE, DELETE, COMPACT
    partition_name VARCHAR(100),
    status VARCHAR(20) NOT NULL, -- SUCCESS, ERROR, IN_PROGRESS
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    duration_ms BIGINT,
    events_processed BIGINT,
    bytes_processed BIGINT,
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Índices para log de operações
CREATE INDEX IF NOT EXISTS idx_archive_ops_log_operation_type 
    ON archive_operations_log (operation_type, started_at DESC);

CREATE INDEX IF NOT EXISTS idx_archive_ops_log_status 
    ON archive_operations_log (status, started_at DESC);

CREATE INDEX IF NOT EXISTS idx_archive_ops_log_partition 
    ON archive_operations_log (partition_name, started_at DESC);

-- Tabela para estatísticas de arquivamento
CREATE TABLE IF NOT EXISTS archive_statistics (
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
CREATE INDEX IF NOT EXISTS idx_archive_stats_snapshot_date 
    ON archive_statistics (snapshot_date DESC);

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
        snapshot_date,
        total_archives,
        total_events,
        total_original_size,
        total_compressed_size,
        average_compression_ratio,
        oldest_archive_date,
        newest_archive_date,
        storage_efficiency
    ) VALUES (
        stats_date,
        total_archives,
        total_events,
        total_original_size,
        total_compressed_size,
        avg_compression_ratio,
        oldest_date,
        newest_date,
        storage_efficiency
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
CREATE OR REPLACE FUNCTION cleanup_archive_logs(retention_days INTEGER DEFAULT 90)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM archive_operations_log 
    WHERE created_at < CURRENT_TIMESTAMP - (retention_days || ' days')::INTERVAL;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    RAISE NOTICE 'Removidos % registros de log de arquivamento', deleted_count;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Função para obter resumo de arquivamento
CREATE OR REPLACE FUNCTION get_archive_summary()
RETURNS TABLE (
    metric_name TEXT,
    metric_value TEXT,
    metric_description TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        'total_archives'::TEXT,
        COUNT(*)::TEXT,
        'Total de partições arquivadas'::TEXT
    FROM event_archives WHERE status = 'ARCHIVED'
    
    UNION ALL
    
    SELECT 
        'total_events'::TEXT,
        COALESCE(SUM(event_count), 0)::TEXT,
        'Total de eventos arquivados'::TEXT
    FROM event_archives WHERE status = 'ARCHIVED'
    
    UNION ALL
    
    SELECT 
        'total_size_mb'::TEXT,
        ROUND(COALESCE(SUM(compressed_size), 0) / 1024.0 / 1024.0, 2)::TEXT,
        'Tamanho total comprimido (MB)'::TEXT
    FROM event_archives WHERE status = 'ARCHIVED'
    
    UNION ALL
    
    SELECT 
        'avg_compression_ratio'::TEXT,
        ROUND(COALESCE(AVG(compression_ratio), 0) * 100, 2)::TEXT || '%',
        'Taxa média de compressão'::TEXT
    FROM event_archives WHERE status = 'ARCHIVED'
    
    UNION ALL
    
    SELECT 
        'oldest_archive'::TEXT,
        COALESCE(MIN(archived_at)::DATE::TEXT, 'N/A'),
        'Arquivo mais antigo'::TEXT
    FROM event_archives WHERE status = 'ARCHIVED'
    
    UNION ALL
    
    SELECT 
        'newest_archive'::TEXT,
        COALESCE(MAX(archived_at)::DATE::TEXT, 'N/A'),
        'Arquivo mais recente'::TEXT
    FROM event_archives WHERE status = 'ARCHIVED';
END;
$$ LANGUAGE plpgsql;

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

-- View para consultas simplificadas de arquivos
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

-- Comentários para documentação
COMMENT ON TABLE event_archives IS 'Metadados de partições arquivadas';
COMMENT ON TABLE archive_operations_log IS 'Log de operações de arquivamento';
COMMENT ON TABLE archive_statistics IS 'Estatísticas diárias de arquivamento';
COMMENT ON VIEW v_archive_summary IS 'View simplificada para consulta de arquivos';

COMMENT ON FUNCTION calculate_archive_statistics() IS 'Calcula e armazena estatísticas diárias de arquivamento';
COMMENT ON FUNCTION cleanup_archive_logs(INTEGER) IS 'Remove logs de arquivamento antigos';
COMMENT ON FUNCTION get_archive_summary() IS 'Retorna resumo executivo de arquivamento';

-- Executar cálculo inicial de estatísticas
SELECT calculate_archive_statistics();

COMMIT;