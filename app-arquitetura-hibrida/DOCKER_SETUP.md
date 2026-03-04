# 🔄 Docker Setup Essencial - Arquitetura Híbrida

## 📋 Visão Geral

Este docker-compose contém apenas os componentes **ESSENCIAIS** para a **Arquitetura Híbrida**: separação completa Command/Query com Event Store, Projections, Cache e Event Bus.

## 🏗️ Serviços Essenciais

### 📝 **PostgreSQL Write** (porta 5435)
- Event Store **ESSENCIAL** para Command Side
- Otimizado para escrita de eventos
- Snapshots automáticos
- Índices específicos para aggregates

### 🔍 **PostgreSQL Read** (porta 5436)
- Projections **ESSENCIAIS** para Query Side
- Otimizado para leitura
- Full-text search configurado
- Views materializadas para relatórios

### ⚡ **Redis** (porta 6380)
- Cache **ESSENCIAL** para Query Side
- 16 databases separados por função
- 1GB de memória com LRU
- Cache em múltiplas camadas

### 📨 **Kafka + Zookeeper** (porta 9093)
- Event Bus **ESSENCIAL** para CQRS
- Configurado para Event Sourcing
- Log compaction habilitada
- Retenção de 7 dias

## 🚀 Como Usar

### 1. Iniciar Infraestrutura
```bash
cd app-arquitetura-hibrida
docker-compose up -d
```

### 2. Verificar Status
```bash
docker-compose ps
```

### 3. Aguardar Inicialização (Importante!)
```bash
# Aguardar bancos
docker-compose logs -f postgres-write postgres-read

# Aguardar Kafka
docker-compose logs -f kafka

# Verificar Redis
docker-compose logs redis
```

### 4. Iniciar Aplicação
```bash
mvn spring-boot:run
```

## 🌐 URLs de Acesso

### Aplicação
- **API**: http://localhost:8083/api/v1/swagger-ui.html
- **Health**: http://localhost:8083/api/v1/sistema/health
- **Actuator**: http://localhost:8083/api/v1/actuator

### Infraestrutura
- **PostgreSQL Write**: localhost:5435 (postgres/postgres)
- **PostgreSQL Read**: localhost:5436 (postgres/postgres)
- **Redis**: localhost:6380
- **Kafka**: localhost:9093

## 🔧 Comandos Úteis

### Verificar Conectividade
```bash
# PostgreSQL Write (Event Store)
docker-compose exec postgres-write pg_isready -U postgres

# PostgreSQL Read (Projections)
docker-compose exec postgres-read pg_isready -U postgres

# Redis
docker-compose exec redis redis-cli ping

# Kafka
docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Acessar Bancos
```bash
# Event Store
docker-compose exec postgres-write psql -U postgres -d sinistros_eventstore

# Projections
docker-compose exec postgres-read psql -U postgres -d sinistros_projections
```

### Verificar Event Store
```bash
# Ver eventos
docker-compose exec postgres-write psql -U postgres -d sinistros_eventstore -c "SELECT * FROM eventstore.events LIMIT 5;"

# Ver snapshots
docker-compose exec postgres-write psql -U postgres -d sinistros_eventstore -c "SELECT * FROM eventstore.snapshots LIMIT 5;"

# Ver status das projeções
docker-compose exec postgres-write psql -U postgres -d sinistros_eventstore -c "SELECT * FROM eventstore.projection_status;"
```

### Verificar Projections
```bash
# Ver projeções de sinistros
docker-compose exec postgres-read psql -U postgres -d sinistros_projections -c "SELECT * FROM projections.sinistro_view LIMIT 5;"

# Ver timeline de eventos
docker-compose exec postgres-read psql -U postgres -d sinistros_projections -c "SELECT * FROM projections.evento_timeline LIMIT 5;"
```

### Verificar Redis por Database
```bash
# Database 0 (Cache de queries)
docker-compose exec redis redis-cli -n 0 dbsize

# Database 1 (Sessões)
docker-compose exec redis redis-cli -n 1 dbsize

# Database 2 (Cache de projeções)
docker-compose exec redis redis-cli -n 2 dbsize
```

## ⚙️ Configurações

### PostgreSQL Write (Event Store)
- **Banco**: sinistros_eventstore
- **Foco**: Escrita de eventos
- **Snapshots**: A cada 50 eventos
- **Schemas**: eventstore

### PostgreSQL Read (Projections)
- **Banco**: sinistros_projections
- **Foco**: Leitura otimizada
- **Full-text**: Busca configurada
- **Schemas**: projections

### Redis
- **Memória**: 1GB com LRU
- **Databases**: 16 separados
- **Uso**: Cache multi-camada

### Kafka
- **Retenção**: 7 dias
- **Compactação**: Log compaction
- **Tópicos**: Criação automática

## 🎯 Componentes Removidos

Para manter apenas o essencial, foram removidos:
- ❌ Elasticsearch
- ❌ Kibana
- ❌ Kafka UI
- ❌ Redis Commander
- ❌ PgAdmin
- ❌ Prometheus
- ❌ Grafana
- ❌ Jaeger

**💡 Estes podem ser adicionados posteriormente conforme necessidade.**

## 🔍 Verificações CQRS

### Command Side
```bash
# Verificar eventos por aggregate
docker-compose exec postgres-write psql -U postgres -d sinistros_eventstore -c "
SELECT aggregate_type, COUNT(*) as total_events 
FROM eventstore.events 
GROUP BY aggregate_type;"
```

### Query Side
```bash
# Verificar lag das projeções
docker-compose exec postgres-read psql -U postgres -d sinistros_projections -c "
SELECT projection_name, events_behind, last_processed_at 
FROM eventstore.projection_status;"
```

### Event Processing
```bash
# Verificar tópicos Kafka
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Verificar consumer groups
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

## 📊 Schemas Implementados

### Event Store (Write)
- **eventstore.events**: Eventos principais
- **eventstore.snapshots**: Snapshots de agregados
- **eventstore.commands**: Log de comandos
- **eventstore.projection_tracking**: Controle de projeções

### Projections (Read)
- **projections.sinistro_view**: View otimizada de sinistros
- **projections.detran_consulta_view**: View de consultas Detran
- **projections.evento_timeline**: Timeline de eventos
- **projections.metricas_agregadas**: Métricas consolidadas

---

**🔄 Infraestrutura mínima e essencial para CQRS e Event Sourcing!**