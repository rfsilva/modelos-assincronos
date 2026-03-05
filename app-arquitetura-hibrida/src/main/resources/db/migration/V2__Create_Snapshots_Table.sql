-- Migration V2: Criar tabela de snapshots
-- Autor: Principal Java Architect
-- Data: 2024-12-19
-- Descrição: Criação da tabela snapshots para armazenamento otimizado de snapshots de aggregates

-- Criar tabela principal de snapshots
CREATE TABLE snapshots (
    snapshot_id VARCHAR(36) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL,
    snapshot_data JSONB NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    schema_version INTEGER NOT NULL DEFAULT 1,
    compressed BOOLEAN NOT NULL DEFAULT FALSE,
    original_size INTEGER,
    compressed_size INTEGER,
    compression_algorithm VARCHAR(20),
    data_hash VARCHAR(64),
    created_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT pk_snapshots PRIMARY KEY (snapshot_id),
    CONSTRAINT uk_snapshots_aggregate_version UNIQUE (aggregate_id, version),
    CONSTRAINT chk_snapshots_version_positive CHECK (version >= 0),
    CONSTRAINT chk_snapshots_schema_version_positive CHECK (schema_version > 0),
    CONSTRAINT chk_snapshots_sizes_valid CHECK (
        (compressed = FALSE AND original_size IS NULL AND compressed_size IS NULL) OR
        (compressed = TRUE AND original_size IS NOT NULL AND compressed_size IS NOT NULL AND compressed_size <= original_size)
    )
);

-- Índices para performance otimizada
CREATE INDEX idx_snapshots_aggregate_version ON snapshots (aggregate_id, version DESC);
CREATE INDEX idx_snapshots_aggregate_timestamp ON snapshots (aggregate_id, timestamp DESC);
CREATE INDEX idx_snapshots_type_timestamp ON snapshots (aggregate_type, timestamp DESC);
CREATE INDEX idx_snapshots_timestamp ON snapshots (timestamp DESC);
CREATE INDEX idx_snapshots_compressed ON snapshots (compressed) WHERE compressed = TRUE;
CREATE INDEX idx_snapshots_created_by ON snapshots (created_by) WHERE created_by IS NOT NULL;

-- Índice parcial para snapshots recentes (últimos 30 dias)
CREATE INDEX idx_snapshots_recent ON snapshots (aggregate_id, version DESC) 
WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '30 days';

-- Índice GIN para consultas em metadados JSON
CREATE INDEX idx_snapshots_metadata_gin ON snapshots USING GIN (metadata);

-- Comentários para documentação
COMMENT ON TABLE snapshots IS 'Armazena snapshots de aggregates para otimização de reconstrução';
COMMENT ON COLUMN snapshots.snapshot_id IS 'Identificador único do snapshot (UUID)';
COMMENT ON COLUMN snapshots.aggregate_id IS 'ID do aggregate que este snapshot representa';
COMMENT ON COLUMN snapshots.aggregate_type IS 'Tipo do aggregate (nome da classe)';
COMMENT ON COLUMN snapshots.version IS 'Versão do aggregate no momento do snapshot';
COMMENT ON COLUMN snapshots.snapshot_data IS 'Dados serializados do aggregate em formato JSON';
COMMENT ON COLUMN snapshots.timestamp IS 'Timestamp de criação do snapshot';
COMMENT ON COLUMN snapshots.metadata IS 'Metadados adicionais do snapshot';
COMMENT ON COLUMN snapshots.schema_version IS 'Versão do schema do snapshot para evolução';
COMMENT ON COLUMN snapshots.compressed IS 'Indica se os dados estão comprimidos';
COMMENT ON COLUMN snapshots.original_size IS 'Tamanho original dos dados (antes da compressão)';
COMMENT ON COLUMN snapshots.compressed_size IS 'Tamanho comprimido dos dados';
COMMENT ON COLUMN snapshots.compression_algorithm IS 'Algoritmo de compressão utilizado';
COMMENT ON COLUMN snapshots.data_hash IS 'Hash dos dados para verificação de integridade';
COMMENT ON COLUMN snapshots.created_by IS 'Usuário que criou o snapshot (para auditoria)';

-- Configurar autovacuum otimizado para a tabela
ALTER TABLE snapshots SET (
    autovacuum_vacuum_scale_factor = 0.1,
    autovacuum_analyze_scale_factor = 0.05,
    autovacuum_vacuum_cost_delay = 10
);

-- Preparar para particionamento futuro (comentado por enquanto)
-- A tabela está preparada para particionamento por timestamp quando necessário
-- 
-- Exemplo de comando para converter para particionamento:
-- ALTER TABLE snapshots RENAME TO snapshots_old;
-- CREATE TABLE snapshots (LIKE snapshots_old INCLUDING ALL) PARTITION BY RANGE (timestamp);
-- CREATE TABLE snapshots_y2024m12 PARTITION OF snapshots FOR VALUES FROM ('2024-12-01') TO ('2025-01-01');

-- Estatísticas iniciais para o otimizador
ANALYZE snapshots;