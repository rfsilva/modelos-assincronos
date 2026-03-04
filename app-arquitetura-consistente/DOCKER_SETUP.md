# ⚖️ Docker Setup Essencial - Arquitetura Consistente

## 📋 Visão Geral

Este docker-compose contém apenas os componentes **ESSENCIAIS** para a **Arquitetura Consistente**: dois bancos PostgreSQL otimizados para transações ACID e auditoria.

## 🏗️ Serviços Essenciais

### 🗄️ **PostgreSQL Principal** (porta 5433)
- Banco transacional **ESSENCIAL** para Saga Pattern
- Configurações ACID rigorosas
- Isolamento READ_COMMITTED
- Timeouts configurados para transações longas

### 📊 **PostgreSQL Auditoria** (porta 5434)
- Banco separado **ESSENCIAL** para logs de saga
- Otimizado para escritas de auditoria
- Schemas específicos para logs e métricas
- Funções de auditoria automática

## 🚀 Como Usar

### 1. Iniciar Infraestrutura
```bash
cd app-arquitetura-consistente
docker-compose up -d
```

### 2. Verificar Status
```bash
docker-compose ps
```

### 3. Aguardar Inicialização
```bash
# Verificar logs dos bancos
docker-compose logs -f postgres postgres-audit

# Aguardar "database system is ready to accept connections"
```

### 4. Iniciar Aplicação
```bash
mvn spring-boot:run
```

## 🌐 URLs de Acesso

### Aplicação
- **API**: http://localhost:8082/api/v1/swagger-ui.html
- **Health**: http://localhost:8082/api/v1/sistema/health
- **Actuator**: http://localhost:8082/api/v1/actuator

### Infraestrutura
- **PostgreSQL Principal**: localhost:5433 (postgres/postgres)
- **PostgreSQL Auditoria**: localhost:5434 (postgres/postgres)

## 🔧 Comandos Úteis

### Verificar Conectividade
```bash
# Banco principal
docker-compose exec postgres pg_isready -U postgres

# Banco de auditoria
docker-compose exec postgres-audit pg_isready -U postgres
```

### Acessar Bancos
```bash
# Banco principal
docker-compose exec postgres psql -U postgres -d sinistros_consistente

# Banco de auditoria
docker-compose exec postgres-audit psql -U postgres -d sinistros_audit
```

### Verificar Schemas
```bash
# Schemas do banco principal
docker-compose exec postgres psql -U postgres -d sinistros_consistente -c "\dn"

# Schemas do banco de auditoria
docker-compose exec postgres-audit psql -U postgres -d sinistros_audit -c "\dn"
```

### Parar e Limpar
```bash
# Parar serviços
docker-compose down

# Limpar volumes (CUIDADO!)
docker-compose down -v
```

## ⚙️ Configurações

### PostgreSQL Principal
- **Banco**: sinistros_consistente
- **Foco**: Transações ACID rigorosas
- **Timeout**: 300s para sagas
- **Isolamento**: READ_COMMITTED

### PostgreSQL Auditoria
- **Banco**: sinistros_audit
- **Foco**: Logs e auditoria
- **Schemas**: audit, logs
- **Funções**: Triggers de auditoria automática

## 📊 Schemas Criados

### Banco Principal
- **saga**: Tabelas de controle de saga
- **audit**: Logs de auditoria transacional

### Banco Auditoria
- **logs**: Logs detalhados de execução
- **Views**: Relatórios de saga e performance

## 🎯 Componentes Removidos

Para manter apenas o essencial, foram removidos:
- ❌ PgAdmin
- ❌ Prometheus
- ❌ Grafana
- ❌ Jaeger
- ❌ PostgreSQL Exporter
- ❌ Backup automático

**💡 Estes podem ser adicionados posteriormente conforme necessidade.**

## 🔍 Verificações de Saúde

### Verificar Auditoria
```bash
# Ver logs de auditoria
docker-compose exec postgres psql -U postgres -d sinistros_consistente -c "SELECT * FROM audit.audit_log LIMIT 5;"

# Ver estatísticas de saga
docker-compose exec postgres-audit psql -U postgres -d sinistros_audit -c "SELECT * FROM logs.saga_summary LIMIT 5;"
```

### Verificar Configurações ACID
```bash
# Ver configurações de isolamento
docker-compose exec postgres psql -U postgres -c "SHOW default_transaction_isolation;"

# Ver configurações de timeout
docker-compose exec postgres psql -U postgres -c "SHOW statement_timeout;"
```

---

**⚖️ Infraestrutura mínima e essencial para máxima consistência!**