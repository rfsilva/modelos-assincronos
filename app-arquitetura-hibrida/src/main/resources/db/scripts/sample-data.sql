-- =====================================================
-- Script Auxiliar: Dados de Exemplo
-- =====================================================
-- Autor: Principal Java Architect
-- Data: 2024-12-19
-- Descrição: Dados de exemplo para desenvolvimento e testes
--            do Event Store e sistema de projeções
-- 
-- ATENÇÃO: Este script é apenas para desenvolvimento/testes
--          NÃO execute em produção!
-- =====================================================

-- Verificar se estamos em ambiente de desenvolvimento
DO $$
BEGIN
    IF current_setting('server_version_num')::integer >= 140000 
       AND current_database() NOT LIKE '%dev%' 
       AND current_database() NOT LIKE '%test%' THEN
        RAISE EXCEPTION 'Este script só deve ser executado em ambiente de desenvolvimento/teste!';
    END IF;
    
    RAISE NOTICE 'Inserindo dados de exemplo para desenvolvimento...';
END $$;

-- Definir schema padrão
SET search_path TO eventstore, public;

-- =====================================================
-- DADOS DE EXEMPLO: EVENTOS DE SEGURADO
-- =====================================================

-- Segurado 1: João Silva
INSERT INTO events (
    aggregate_id, aggregate_type, event_type, version, timestamp,
    correlation_id, user_id, event_data, metadata, data_size
) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440001',
    'SeguradoAggregate',
    'SeguradoCriadoEvent',
    0,
    CURRENT_TIMESTAMP - INTERVAL '30 days',
    uuid_generate_v4(),
    'system',
    '{
        "seguradoId": "550e8400-e29b-41d4-a716-446655440001",
        "nome": "João Silva",
        "cpf": "12345678901",
        "email": "joao.silva@email.com",
        "telefone": "11987654321",
        "dataNascimento": "1985-03-15",
        "endereco": {
            "logradouro": "Rua das Flores, 123",
            "bairro": "Centro",
            "cidade": "São Paulo",
            "estado": "SP",
            "cep": "01234567"
        }
    }'::jsonb,
    '{
        "source": "web-portal",
        "userAgent": "Mozilla/5.0",
        "ipAddress": "192.168.1.100"
    }'::jsonb,
    512
),
(
    '550e8400-e29b-41d4-a716-446655440001',
    'SeguradoAggregate',
    'EnderecoAtualizadoEvent',
    1,
    CURRENT_TIMESTAMP - INTERVAL '25 days',
    uuid_generate_v4(),
    'user_123',
    '{
        "seguradoId": "550e8400-e29b-41d4-a716-446655440001",
        "novoEndereco": {
            "logradouro": "Av. Paulista, 1000",
            "bairro": "Bela Vista",
            "cidade": "São Paulo",
            "estado": "SP",
            "cep": "01310100"
        },
        "enderecoAnterior": {
            "logradouro": "Rua das Flores, 123",
            "bairro": "Centro",
            "cidade": "São Paulo",
            "estado": "SP",
            "cep": "01234567"
        }
    }'::jsonb,
    '{
        "source": "mobile-app",
        "reason": "mudanca-residencia"
    }'::jsonb,
    384
);

-- Segurado 2: Maria Santos
INSERT INTO events (
    aggregate_id, aggregate_type, event_type, version, timestamp,
    correlation_id, user_id, event_data, metadata, data_size
) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440002',
    'SeguradoAggregate',
    'SeguradoCriadoEvent',
    0,
    CURRENT_TIMESTAMP - INTERVAL '20 days',
    uuid_generate_v4(),
    'system',
    '{
        "seguradoId": "550e8400-e29b-41d4-a716-446655440002",
        "nome": "Maria Santos",
        "cpf": "98765432109",
        "email": "maria.santos@email.com",
        "telefone": "11876543210",
        "dataNascimento": "1990-07-22",
        "endereco": {
            "logradouro": "Rua Augusta, 456",
            "bairro": "Consolação",
            "cidade": "São Paulo",
            "estado": "SP",
            "cep": "01305000"
        }
    }'::jsonb,
    '{
        "source": "call-center",
        "operator": "op_456"
    }'::jsonb,
    498
);

-- =====================================================
-- DADOS DE EXEMPLO: EVENTOS DE SINISTRO
-- =====================================================

-- Sinistro 1: Colisão
INSERT INTO events (
    aggregate_id, aggregate_type, event_type, version, timestamp,
    correlation_id, user_id, event_data, metadata, data_size
) VALUES 
(
    '660e8400-e29b-41d4-a716-446655440001',
    'SinistroAggregate',
    'SinistroCriadoEvent',
    0,
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    uuid_generate_v4(),
    'user_123',
    '{
        "sinistroId": "660e8400-e29b-41d4-a716-446655440001",
        "protocolo": "SIN-2024-001234",
        "seguradoId": "550e8400-e29b-41d4-a716-446655440001",
        "apoliceId": "770e8400-e29b-41d4-a716-446655440001",
        "veiculoId": "880e8400-e29b-41d4-a716-446655440001",
        "tipoSinistro": "COLISAO",
        "dataOcorrencia": "2024-12-04T14:30:00Z",
        "localOcorrencia": {
            "endereco": "Av. Paulista, 1000 - Bela Vista, São Paulo - SP",
            "cep": "01310100",
            "latitude": -23.5613,
            "longitude": -46.6565
        },
        "descricao": "Colisão traseira em semáforo",
        "valorEstimado": 5000.00,
        "prioridade": "NORMAL"
    }'::jsonb,
    '{
        "source": "mobile-app",
        "gpsLocation": "enabled",
        "photos": ["photo1.jpg", "photo2.jpg"]
    }'::jsonb,
    756
),
(
    '660e8400-e29b-41d4-a716-446655440001',
    'SinistroAggregate',
    'ConsultaDetranIniciada',
    1,
    CURRENT_TIMESTAMP - INTERVAL '14 days',
    uuid_generate_v4(),
    'system',
    '{
        "sinistroId": "660e8400-e29b-41d4-a716-446655440001",
        "placa": "ABC1234",
        "renavam": "12345678901",
        "consultaId": "det_001",
        "timestampInicio": "2024-12-05T09:00:00Z"
    }'::jsonb,
    '{
        "source": "detran-integration",
        "requestId": "req_12345"
    }'::jsonb,
    256
),
(
    '660e8400-e29b-41d4-a716-446655440001',
    'SinistroAggregate',
    'ConsultaDetranConcluida',
    2,
    CURRENT_TIMESTAMP - INTERVAL '14 days' + INTERVAL '5 minutes',
    uuid_generate_v4(),
    'system',
    '{
        "sinistroId": "660e8400-e29b-41d4-a716-446655440001",
        "consultaId": "det_001",
        "status": "SUCCESS",
        "dadosRetornados": {
            "situacao": "REGULAR",
            "restricoes": [],
            "debitos": [
                {
                    "tipo": "IPVA",
                    "valor": 1200.50,
                    "vencimento": "2024-03-31"
                }
            ],
            "multas": []
        },
        "timestampConclusao": "2024-12-05T09:05:00Z"
    }'::jsonb,
    '{
        "source": "detran-integration",
        "responseTime": 5000,
        "requestId": "req_12345"
    }'::jsonb,
    512
);

-- Sinistro 2: Roubo
INSERT INTO events (
    aggregate_id, aggregate_type, event_type, version, timestamp,
    correlation_id, user_id, event_data, metadata, data_size
) VALUES 
(
    '660e8400-e29b-41d4-a716-446655440002',
    'SinistroAggregate',
    'SinistroCriadoEvent',
    0,
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    uuid_generate_v4(),
    'user_456',
    '{
        "sinistroId": "660e8400-e29b-41d4-a716-446655440002",
        "protocolo": "SIN-2024-001235",
        "seguradoId": "550e8400-e29b-41d4-a716-446655440002",
        "apoliceId": "770e8400-e29b-41d4-a716-446655440002",
        "veiculoId": "880e8400-e29b-41d4-a716-446655440002",
        "tipoSinistro": "ROUBO",
        "dataOcorrencia": "2024-12-09T22:15:00Z",
        "localOcorrencia": {
            "endereco": "Rua Augusta, 456 - Consolação, São Paulo - SP",
            "cep": "01305000",
            "latitude": -23.5505,
            "longitude": -46.6333
        },
        "descricao": "Veículo roubado em via pública",
        "valorEstimado": 45000.00,
        "prioridade": "ALTA",
        "boletimOcorrencia": "BO-2024-123456"
    }'::jsonb,
    '{
        "source": "call-center",
        "operator": "op_789",
        "urgency": "high"
    }'::jsonb,
    892
);

-- =====================================================
-- DADOS DE EXEMPLO: EVENTOS DE APÓLICE
-- =====================================================

-- Apólice 1
INSERT INTO events (
    aggregate_id, aggregate_type, event_type, version, timestamp,
    correlation_id, user_id, event_data, metadata, data_size
) VALUES 
(
    '770e8400-e29b-41d4-a716-446655440001',
    'ApoliceAggregate',
    'ApoliceCriadaEvent',
    0,
    CURRENT_TIMESTAMP - INTERVAL '35 days',
    uuid_generate_v4(),
    'system',
    '{
        "apoliceId": "770e8400-e29b-41d4-a716-446655440001",
        "numero": "AP-2024-001234",
        "seguradoId": "550e8400-e29b-41d4-a716-446655440001",
        "veiculoId": "880e8400-e29b-41d4-a716-446655440001",
        "produto": "Seguro Auto Completo",
        "vigencia": {
            "inicio": "2024-11-15",
            "fim": "2025-11-15"
        },
        "valorSegurado": 50000.00,
        "premio": 2400.00,
        "coberturas": [
            {
                "tipo": "COLISAO",
                "valorCobertura": 50000.00,
                "franquia": 1500.00
            },
            {
                "tipo": "ROUBO_FURTO",
                "valorCobertura": 50000.00,
                "franquia": 1000.00
            },
            {
                "tipo": "TERCEIROS",
                "valorCobertura": 100000.00,
                "franquia": 0.00
            }
        ],
        "formaPagamento": {
            "tipo": "CARTAO_CREDITO",
            "parcelas": 12
        }
    }'::jsonb,
    '{
        "source": "web-portal",
        "channel": "online",
        "broker": "broker_123"
    }'::jsonb,
    1024
);

-- =====================================================
-- DADOS DE EXEMPLO: SNAPSHOTS
-- =====================================================

-- Snapshot do Segurado 1
INSERT INTO snapshots (
    aggregate_id, aggregate_type, version, snapshot_data, 
    timestamp, schema_version, created_by, data_size
) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440001',
    'SeguradoAggregate',
    1,
    '{
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "nome": "João Silva",
        "cpf": "12345678901",
        "email": "joao.silva@email.com",
        "telefone": "11987654321",
        "dataNascimento": "1985-03-15",
        "endereco": {
            "logradouro": "Av. Paulista, 1000",
            "bairro": "Bela Vista",
            "cidade": "São Paulo",
            "estado": "SP",
            "cep": "01310100"
        },
        "status": "ATIVO",
        "contatos": [
            {
                "tipo": "EMAIL",
                "valor": "joao.silva@email.com",
                "principal": true
            },
            {
                "tipo": "TELEFONE",
                "valor": "11987654321",
                "principal": true
            }
        ],
        "version": 1,
        "createdAt": "2024-11-19T10:00:00Z",
        "updatedAt": "2024-11-24T15:30:00Z"
    }'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '25 days',
    1,
    'system',
    896
);

-- =====================================================
-- DADOS DE EXEMPLO: MÉTRICAS E ESTATÍSTICAS
-- =====================================================

-- Simular algumas estatísticas de eventos por dia
INSERT INTO events (
    aggregate_id, aggregate_type, event_type, version, timestamp,
    correlation_id, user_id, event_data, metadata, data_size
)
SELECT 
    uuid_generate_v4(),
    'TestAggregate',
    'TestEvent',
    0,
    CURRENT_TIMESTAMP - (random() * INTERVAL '30 days'),
    uuid_generate_v4(),
    'test_user',
    '{"test": true, "value": ' || (random() * 1000)::int || '}'::jsonb,
    '{"generated": true}'::jsonb,
    (random() * 500 + 100)::int
FROM generate_series(1, 100);

-- =====================================================
-- ATUALIZAR TRACKING DE PROJEÇÕES
-- =====================================================

-- Simular processamento de eventos pelas projeções
UPDATE projection_tracking 
SET 
    last_processed_event_id = (SELECT MAX(id::text)::bigint FROM events WHERE id::text ~ '^[0-9]+$' LIMIT 1),
    last_processed_at = CURRENT_TIMESTAMP - INTERVAL '5 minutes',
    events_processed = (SELECT COUNT(*) FROM events),
    events_failed = (random() * 5)::int,
    updated_at = CURRENT_TIMESTAMP
WHERE projection_name IN ('SinistroProjection', 'SeguradoProjection');

-- =====================================================
-- DADOS PARA TESTES DE PERFORMANCE
-- =====================================================

-- Função para gerar dados de teste em massa
CREATE OR REPLACE FUNCTION generate_test_events(
    p_count INTEGER DEFAULT 1000,
    p_days_back INTEGER DEFAULT 7
)
RETURNS void AS $$
DECLARE
    i INTEGER;
    test_aggregate_id UUID;
    test_timestamp TIMESTAMP WITH TIME ZONE;
BEGIN
    RAISE NOTICE 'Gerando % eventos de teste...', p_count;
    
    FOR i IN 1..p_count LOOP
        test_aggregate_id := uuid_generate_v4();
        test_timestamp := CURRENT_TIMESTAMP - (random() * (p_days_back || ' days')::INTERVAL);
        
        INSERT INTO events (
            aggregate_id, aggregate_type, event_type, version, timestamp,
            correlation_id, user_id, event_data, metadata, data_size
        ) VALUES (
            test_aggregate_id,
            'TestAggregate',
            CASE (random() * 4)::int
                WHEN 0 THEN 'TestCreatedEvent'
                WHEN 1 THEN 'TestUpdatedEvent'
                WHEN 2 THEN 'TestDeletedEvent'
                ELSE 'TestProcessedEvent'
            END,
            0,
            test_timestamp,
            uuid_generate_v4(),
            'test_user_' || (random() * 100)::int,
            jsonb_build_object(
                'testId', test_aggregate_id,
                'value', (random() * 1000)::int,
                'category', CASE (random() * 3)::int
                    WHEN 0 THEN 'A'
                    WHEN 1 THEN 'B'
                    ELSE 'C'
                END,
                'timestamp', test_timestamp
            ),
            jsonb_build_object(
                'generated', true,
                'batch', 'sample_data',
                'sequence', i
            ),
            (random() * 1000 + 200)::int
        );
        
        -- Log progresso a cada 100 eventos
        IF i % 100 = 0 THEN
            RAISE NOTICE 'Gerados % eventos...', i;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Geração de eventos de teste concluída!';
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- VALIDAÇÃO DOS DADOS INSERIDOS
-- =====================================================

DO $$
DECLARE
    event_count INTEGER;
    snapshot_count INTEGER;
    aggregate_count INTEGER;
BEGIN
    -- Contar eventos inseridos
    SELECT COUNT(*) INTO event_count FROM events;
    
    -- Contar snapshots inseridos
    SELECT COUNT(*) INTO snapshot_count FROM snapshots;
    
    -- Contar aggregates únicos
    SELECT COUNT(DISTINCT aggregate_id) INTO aggregate_count FROM events;
    
    RAISE NOTICE '=== DADOS DE EXEMPLO INSERIDOS ===';
    RAISE NOTICE 'Total de eventos: %', event_count;
    RAISE NOTICE 'Total de snapshots: %', snapshot_count;
    RAISE NOTICE 'Aggregates únicos: %', aggregate_count;
    RAISE NOTICE '';
    RAISE NOTICE 'Dados disponíveis para:';
    RAISE NOTICE '- 2 Segurados (João Silva, Maria Santos)';
    RAISE NOTICE '- 2 Sinistros (Colisão, Roubo)';
    RAISE NOTICE '- 1 Apólice';
    RAISE NOTICE '- 100+ eventos de teste';
    RAISE NOTICE '';
    RAISE NOTICE 'Para gerar mais dados de teste:';
    RAISE NOTICE 'SELECT generate_test_events(5000, 30);';
END $$;

-- =====================================================
-- COMENTÁRIOS PARA DOCUMENTAÇÃO
-- =====================================================

COMMENT ON FUNCTION generate_test_events(INTEGER, INTEGER) IS 'Gera eventos de teste para performance e desenvolvimento';

-- =====================================================
-- LIMPEZA (OPCIONAL)
-- =====================================================

/*
Para limpar os dados de exemplo:

DELETE FROM snapshots WHERE created_by = 'system';
DELETE FROM events WHERE user_id IN ('system', 'user_123', 'user_456') OR user_id LIKE 'test_user%';
UPDATE projection_tracking SET 
    last_processed_event_id = 0,
    events_processed = 0,
    events_failed = 0,
    last_processed_at = CURRENT_TIMESTAMP;
*/

-- =====================================================
-- FIM DO SCRIPT DE DADOS DE EXEMPLO
-- =====================================================