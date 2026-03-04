# 🛡️ Docker Setup Essencial - Arquitetura Resiliente

## 📋 Visão Geral

Este docker-compose contém apenas os componentes **ESSENCIAIS** para a **Arquitetura Resiliente**: cache distribuído, mensageria e banco de dados.

## 🏗️ Serviços Essenciais

### 🗄️ **PostgreSQL** (porta 5432)
- Banco de dados principal
- Configuração padrão para desenvolvimento

### ⚡ **Redis** (porta 6379) 
- Cache distribuído **ESSENCIAL** para resiliência
- Configurado com LRU e 512MB de memória
- Política de eviction para otimizar performance

### 📨 **Kafka + Zookeeper** (porta 9092)
- Processamento assíncrono **ESSENCIAL**
- Retry automático e fallback
- Criação automática de tópicos

## 🚀 Como Usar

### 1. Iniciar Infraestrutura
```bash
cd app-arquitetura-resiliente
docker-compose up -d
```

### 2. Verificar Status
```bash
docker-compose ps
```

### 3. Aguardar Health Checks
```bash
# Verificar se todos estão healthy
docker-compose ps

# Verificar logs se necessário
docker-compose logs redis kafka postgres
```

### 4. Iniciar Aplicação
```bash
mvn spring-boot:run
```

## 🌐 URLs de Acesso

### Aplicação
- **API**: http://localhost:8081/api/v1/swagger-ui.html
- **Health**: http://localhost:8081/api/v1/sistema/health
- **Actuator**: http://localhost:8081/api/v1/actuator

### Infraestrutura
- **PostgreSQL**: localhost:5432 (postgres/postgres)
- **Redis**: localhost:6379
- **Kafka**: localhost:9092

## 🔧 Comandos Úteis

### Verificar Conectividade
```bash
# Redis
docker-compose exec redis redis-cli ping

# PostgreSQL
docker-compose exec postgres pg_isready -U postgres

# Kafka
docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Monitorar Logs
```bash
# Todos os serviços
docker-compose logs -f

# Serviço específico
docker-compose logs -f redis
```

### Parar e Limpar
```bash
# Parar serviços
docker-compose down

# Limpar volumes (CUIDADO!)
docker-compose down -v
```

## ⚙️ Configurações

### Redis
- **Memória**: 512MB com LRU
- **Persistência**: Dados em volume
- **Política**: allkeys-lru para cache

### Kafka
- **Tópicos**: Criação automática
- **Replicação**: Fator 1 (desenvolvimento)
- **Retenção**: Padrão do Kafka

### PostgreSQL
- **Banco**: sinistros_resiliente
- **Usuário**: postgres/postgres
- **Porta**: 5432

## 🎯 Componentes Removidos

Para manter apenas o essencial, foram removidos:
- ❌ Kafka UI
- ❌ Redis Commander  
- ❌ Prometheus
- ❌ Grafana
- ❌ PgAdmin
- ❌ Backup automático

**💡 Estes podem ser adicionados posteriormente conforme necessidade.**

---

**🚀 Infraestrutura mínima e essencial para máxima resiliência!**