-- Criação da tabela de tracking de projeções
-- Esta tabela mantém o controle de posição de cada projeção

CREATE TABLE IF NOT EXISTS eventstore.projection_tracking (
    projection_name VARCHAR(100) PRIMARY KEY,
    last_processed_event_id BIGINT NOT NULL DEFAULT 0,
    last_processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    events_processed BIGINT NOT NULL DEFAULT 0,
    events_failed BIGINT NOT NULL DEFAULT 0,
    last_error_message VARCHAR(1000),
    last_error_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_projection_tracking_status ON eventstore.projection_tracking(status);
CREATE INDEX IF NOT EXISTS idx_projection_tracking_last_processed ON eventstore.projection_tracking(last_processed_event_id);
CREATE INDEX IF NOT EXISTS idx_projection_tracking_updated_at ON eventstore.projection_tracking(updated_at);

-- Inserir projeções iniciais
INSERT INTO eventstore.projection_tracking (projection_name, status) VALUES
('SinistroProjection', 'ACTIVE'),
('DetranConsultaProjection', 'ACTIVE'),
('EventTimelineProjection', 'ACTIVE')
ON CONFLICT (projection_name) DO NOTHING;

-- Comentários para documentação
COMMENT ON TABLE eventstore.projection_tracking IS 'Tabela para rastreamento de posição das projeções CQRS';
COMMENT ON COLUMN eventstore.projection_tracking.projection_name IS 'Nome único da projeção';
COMMENT ON COLUMN eventstore.projection_tracking.last_processed_event_id IS 'ID do último evento processado';
COMMENT ON COLUMN eventstore.projection_tracking.last_processed_at IS 'Timestamp do último processamento';
COMMENT ON COLUMN eventstore.projection_tracking.status IS 'Status da projeção (ACTIVE, PAUSED, ERROR, REBUILDING, DISABLED)';
COMMENT ON COLUMN eventstore.projection_tracking.events_processed IS 'Total de eventos processados com sucesso';
COMMENT ON COLUMN eventstore.projection_tracking.events_failed IS 'Total de eventos que falharam';
COMMENT ON COLUMN eventstore.projection_tracking.last_error_message IS 'Mensagem do último erro';
COMMENT ON COLUMN eventstore.projection_tracking.last_error_at IS 'Timestamp do último erro';
COMMENT ON COLUMN eventstore.projection_tracking.version IS 'Versão para controle de concorrência otimista';