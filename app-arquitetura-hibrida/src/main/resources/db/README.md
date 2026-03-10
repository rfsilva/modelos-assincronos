# 📋 Sistema de Migrations - Event Store

## 🎯 Visão Geral

Este diretório contém o sistema completo de migrations do Event Store, implementando as melhores práticas de versionamento de schema e manutenção de banco de dados.

## 📁 Estrutura de Diretórios

```
db/
├── migration/                          # Migrations principais (Write DB)
│   ├── V1__Create_EventStore_Foundation.sql
│   ├── V2__Implement_Partitioning_And_Archiving.sql
│   └── *.sql.backup                   # Migrations antigas (backup)
├── migration-projections/             # Migrations de projeções (Read DB)
│   └── V1__Create_Projections_Schema.sql
└── scripts/                          # Scripts auxiliares
    ├── system-configuration.sql      # Configurações de sistema
    ├── sample-data.sql               # Dados de exemplo
    └── integrity-validation.sql      # Validação de integridade
```

## 🚀 Melhorias Implementadas

### ✅ Imediatas (Baixo Risco)

#### 📝 Cabeçalhos Padronizados
Todas as migrations agora possuem cabeçalhos completos com:
- Autor e data
- Descrição detalhada
- Dependências
- Instruções de rollback
- Validações de pré-requisitos

```sql
-- =====================================================
-- Migration V1: Fundação do Event Store
-- =====================================================
-- Autor: Principal Java Architect
-- Data: 2024-12-19
-- Versão: 2.0 (Consolidada)
-- Descrição: Criação consolidada da estrutura base...
-- Dependências: Nenhuma
-- Rollback: DROP SCHEMA eventstore CASCADE;
-- =====================================================
```

#### 🔄 Instruções de Rollback
Cada migration inclui instruções detalhadas de rollback:

```sql
-- =====================================================
-- INSTRUÇÕES DE ROLLBACK
-- =====================================================
/*
Para fazer rollback desta migration:
1. Parar a aplicação
2. Executar: DROP SCHEMA eventstore CASCADE;
3. Remover entrada da tabela flyway_schema_history
4. Reiniciar aplicação
*/
```

#### 🔗 Documentação de Dependências
Dependências entre migrations claramente documentadas:

```sql
-- Dependências: V1__Create_EventStore_Foundation.sql
-- Validações de Pré-requisitos:
-- - Tabela events deve existir
-- - PostgreSQL 12+ com suporte a particionamento
```

#### ✅ Validações de Pré-requisitos
Validações automáticas antes da execução:

```sql
DO $$
BEGIN
    -- Verificar versão do PostgreSQL
    IF current_setting('server_version_num')::integer < 120000 THEN
        RAISE EXCEPTION 'PostgreSQL 12+ é obrigatório';
    END IF;
    
    -- Verificar extensões necessárias
    IF NOT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'uuid-ossp') THEN
        CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    END IF;
END $$;
```

### ✅ Médio Prazo (Médio Risco)

#### 🔄 Consolidação V1-V3
As migrations V1, V2 e V3 foram consolidadas em:
- **V1__Create_EventStore_Foundation.sql**: Estrutura base completa
- **V2__Implement_Partitioning_And_Archiving.sql**: Particionamento e arquivamento

#### 📁 Scripts Auxiliares Separados
Configurações de sistema movidas para scripts auxiliares:
- `system-configuration.sql`: Configurações de performance e segurança
- `sample-data.sql`: Dados de exemplo para desenvolvimento
- `integrity-validation.sql`: Sistema de validação automática

### ✅ Longo Prazo (Alto Risco)

#### 🔄 Versionamento Automático de Schema
Sistema implementado com:
- Tracking automático de versões
- Validação de sequência de eventos
- Detecção de inconsistências

#### 📊 Migração de Dados Entre Versões
Sistema completo de migração:
- Backup automático antes de mudanças
- Conversão de dados preservando integridade
- Rollback seguro com validações

#### ✅ Validação Automática de Integridade
Sistema abrangente de validação:
- Validação de sequência de eventos
- Verificação de integridade de snapshots
- Auditoria de projeções CQRS
- Correção automática de problemas simples

## 🛠️ Como Usar

### 1. Execução Normal (Flyway)
```bash
# Flyway executará automaticamente as migrations na ordem
mvn flyway:migrate
```

### 2. Configuração de Sistema (DBA)
```sql
-- Executar como superusuário PostgreSQL
\i db/scripts/system-configuration.sql
```

### 3. Dados de Exemplo (Desenvolvimento)
```sql
-- Apenas em ambiente de desenvolvimento
\i db/scripts/sample-data.sql
```

### 4. Validação de Integridade
```sql
-- Executar validação completa
SELECT * FROM run_integrity_validation();

-- Gerar relatório
SELECT * FROM generate_integrity_report();
```

## 📋 Checklist de Validação

### Antes de Executar em Produção:
- [ ] Backup completo do banco de dados
- [ ] Validação em ambiente de teste
- [ ] Verificação de espaço em disco
- [ ] Janela de manutenção agendada
- [ ] Plano de rollback testado

### Após Execução:
- [ ] Validação de integridade executada
- [ ] Performance verificada
- [ ] Logs analisados
- [ ] Aplicação testada
- [ ] Monitoramento ativo

## 🔧 Configurações Recomendadas

### PostgreSQL (postgresql.conf)
```ini
# Particionamento
constraint_exclusion = partition
enable_partition_pruning = on
enable_partitionwise_join = on
enable_partitionwise_aggregate = on

# Performance
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 16MB
maintenance_work_mem = 64MB

# WAL
wal_buffers = 16MB
checkpoint_completion_target = 0.9
```

### Jobs de Manutenção (Cron)
```bash
# Manutenção diária (02:00)
0 2 * * * psql -d eventstore -c "SELECT daily_maintenance_job();"

# Manutenção semanal (domingo 03:00)
0 3 * * 0 psql -d eventstore -c "SELECT weekly_maintenance_job();"

# Validação de integridade (06:00)
0 6 * * * psql -d eventstore -c "SELECT run_and_log_integrity_validation();"
```

## 🚨 Troubleshooting

### Problemas Comuns

#### Migration Falha por Timeout
```sql
-- Aumentar timeout
SET statement_timeout = '30min';
```

#### Espaço em Disco Insuficiente
```sql
-- Verificar uso de espaço
SELECT * FROM get_partition_statistics();

-- Arquivar partições antigas
SELECT apply_data_retention_policy(24); -- 24 meses
```

#### Projeções com Lag Alto
```sql
-- Verificar status das projeções
SELECT * FROM validate_projection_consistency();

-- Reiniciar projeção específica
UPDATE projection_tracking 
SET status = 'ACTIVE', last_error_message = NULL 
WHERE projection_name = 'SinistroProjection';
```

## 📊 Monitoramento

### Métricas Importantes
- Tamanho das partições
- Lag das projeções
- Taxa de erro das validações
- Performance das consultas

### Alertas Recomendados
- Partição > 1GB
- Projeção com lag > 1 hora
- Taxa de erro > 5%
- Validação de integridade com falhas

## 🔐 Segurança

### Roles e Permissões
```sql
-- Role da aplicação
GRANT USAGE ON SCHEMA eventstore TO eventstore_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA eventstore TO eventstore_app;

-- Role de leitura (projeções)
GRANT USAGE ON SCHEMA eventstore TO eventstore_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA eventstore TO eventstore_reader;
```

### Auditoria
- Todas as operações são logadas
- Histórico de validações mantido
- Tracking de mudanças de schema

## 📚 Referências

- [PostgreSQL Partitioning](https://www.postgresql.org/docs/current/ddl-partitioning.html)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Event Sourcing Patterns](https://martinfowler.com/eaaDev/EventSourcing.html)
- [CQRS Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/cqrs)

---

**Última atualização**: 2024-12-19  
**Versão**: 2.0  
**Autor**: Principal Java Architect