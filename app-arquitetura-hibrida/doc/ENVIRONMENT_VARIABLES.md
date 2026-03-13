# 🔧 Guia de Variáveis de Ambiente

Este documento descreve todas as variáveis de ambiente suportadas pela aplicação **app-arquitetura-hibrida**.

## 📋 Índice

- [Como Usar](#como-usar)
- [Variáveis por Categoria](#variáveis-por-categoria)
  - [Aplicação](#aplicação)
  - [PostgreSQL Write (Event Store)](#postgresql-write-event-store)
  - [PostgreSQL Read (Projections)](#postgresql-read-projections)
  - [Redis](#redis)
  - [Kafka](#kafka)
  - [Segurança](#segurança)
  - [Event Store](#event-store)
  - [Snapshots](#snapshots)
  - [CQRS](#cqrs)
  - [Integrações Externas](#integrações-externas)
  - [Monitoramento](#monitoramento)
  - [Logging](#logging)

---

## 🚀 Como Usar

### 1. Arquivo .env (Recomendado para Docker)

```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Edite as variáveis necessárias
nano .env  # ou vim .env

# Use com docker-compose
docker-compose --env-file .env up -d
```

### 2. Variáveis de Sistema

```bash
# Linux/Mac
export WRITE_DB_PASSWORD="senha_segura"
export REDIS_PASSWORD="senha_redis"

# Windows (PowerShell)
$env:WRITE_DB_PASSWORD="senha_segura"
$env:REDIS_PASSWORD="senha_redis"

# Windows (CMD)
set WRITE_DB_PASSWORD=senha_segura
set REDIS_PASSWORD=senha_redis
```

### 3. application.yml com Variáveis

As variáveis de ambiente já estão configuradas no `application.yml` usando a sintaxe `${VAR_NAME:default_value}`.

---

## 📦 Variáveis por Categoria

### 🎯 Aplicação

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `SPRING_PROFILES_ACTIVE` | `local` | Perfil Spring ativo (`local`, `docker`, `production`) |
| `SERVER_PORT` | `8083` | Porta HTTP da aplicação |
| `TZ` | `America/Sao_Paulo` | Timezone da aplicação |

**Exemplo:**
```bash
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8083
TZ=America/Sao_Paulo
```

---

### 🗄️ PostgreSQL Write (Event Store)

Banco de dados para **operações de escrita** (Command Side - Event Sourcing).

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `WRITE_DB_HOST` | `localhost` | Host do PostgreSQL Write |
| `WRITE_DB_PORT` | `5435` | Porta do PostgreSQL Write |
| `WRITE_DB_NAME` | `sinistros_eventstore` | Nome do banco de dados |
| `WRITE_DB_USERNAME` | `postgres` | Usuário do banco |
| `WRITE_DB_PASSWORD` | `postgres` | **⚠️ SENHA** (alterar em produção) |
| `WRITE_DB_URL` | (auto) | URL JDBC completa (opcional) |
| `WRITE_DB_POOL_SIZE` | `20` | Tamanho máximo do pool de conexões |
| `WRITE_DB_POOL_MIN_IDLE` | `5` | Mínimo de conexões idle |
| `WRITE_DB_CONNECTION_TIMEOUT` | `30000` | Timeout de conexão (ms) |

**URL JDBC completa (opcional):**
```bash
WRITE_DB_URL=jdbc:postgresql://postgres-write:5432/sinistros_eventstore
```

**Uso em application.yml:**
```yaml
app:
  datasource:
    write:
      url: ${WRITE_DB_URL:jdbc:postgresql://${WRITE_DB_HOST:localhost}:${WRITE_DB_PORT:5435}/${WRITE_DB_NAME:sinistros_eventstore}}
      username: ${WRITE_DB_USERNAME:postgres}
      password: ${WRITE_DB_PASSWORD:postgres}
```

---

### 📊 PostgreSQL Read (Projections)

Banco de dados para **operações de leitura** (Query Side - Projections).

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `READ_DB_HOST` | `localhost` | Host do PostgreSQL Read |
| `READ_DB_PORT` | `5436` | Porta do PostgreSQL Read |
| `READ_DB_NAME` | `sinistros_projections` | Nome do banco de dados |
| `READ_DB_USERNAME` | `postgres` | Usuário do banco |
| `READ_DB_PASSWORD` | `postgres` | **⚠️ SENHA** (alterar em produção) |
| `READ_DB_URL` | (auto) | URL JDBC completa (opcional) |
| `READ_DB_POOL_SIZE` | `50` | Tamanho máximo do pool (maior para leitura) |
| `READ_DB_POOL_MIN_IDLE` | `10` | Mínimo de conexões idle |
| `READ_DB_CONNECTION_TIMEOUT` | `20000` | Timeout de conexão (ms) |

---

### 🔴 Redis

Cache distribuído para **query side** e **performance**.

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `REDIS_HOST` | `localhost` | Host do Redis |
| `REDIS_PORT` | `6379` | Porta do Redis |
| `REDIS_PASSWORD` | (vazio) | **⚠️ SENHA** (recomendado em produção) |
| `REDIS_TIMEOUT` | `2000` | Timeout de conexão (ms) |
| `REDIS_POOL_MAX_ACTIVE` | `20` | Máximo de conexões ativas |
| `REDIS_POOL_MAX_IDLE` | `10` | Máximo de conexões idle |
| `REDIS_POOL_MIN_IDLE` | `5` | Mínimo de conexões idle |
| `CACHE_TTL_HOURS` | `24` | TTL padrão do cache (horas) |
| `CACHE_QUERY_TTL_MINUTES` | `5` | TTL do cache de queries (minutos) |

**Exemplo de produção:**
```bash
REDIS_HOST=redis.production.internal
REDIS_PORT=6379
REDIS_PASSWORD=strong_redis_password_here
REDIS_TIMEOUT=3000
```

---

### 🚀 Kafka

Event Bus para **processamento assíncrono** e **integrações**.

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Servidores Kafka (separados por vírgula) |
| `KAFKA_GROUP_ID` | `sinistros-hibrida` | Consumer group ID |
| `KAFKA_AUTO_OFFSET_RESET` | `earliest` | Estratégia de offset (`earliest`, `latest`) |
| `KAFKA_TOPIC_DOMAIN_EVENTS` | `domain-events-topic` | Tópico de eventos de domínio |
| `KAFKA_TOPIC_INTEGRATION_EVENTS` | `integration-events-topic` | Tópico de eventos de integração |
| `KAFKA_TOPIC_NOTIFICATION_EVENTS` | `notification-events-topic` | Tópico de notificações |
| `KAFKA_PRODUCER_ACKS` | `all` | Confirmação de escrita (`all`, `1`, `0`) |
| `KAFKA_PRODUCER_RETRIES` | `3` | Tentativas de reenvio |
| `KAFKA_CONSUMER_MAX_POLL_RECORDS` | `500` | Máximo de registros por poll |

**Cluster Kafka:**
```bash
KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092,kafka3:9092
```

---

### 🔐 Segurança

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `SECURITY_ENABLED` | `true` | Habilitar Spring Security |
| `JWT_ISSUER_URI` | (opcional) | URI do emissor JWT (OAuth2/Keycloak) |
| `JWT_JWK_SET_URI` | (opcional) | URI do JWK Set para validação |

**Exemplo com Keycloak:**
```bash
JWT_ISSUER_URI=https://keycloak.company.com/auth/realms/sinistros
JWT_JWK_SET_URI=https://keycloak.company.com/auth/realms/sinistros/protocol/openid-connect/certs
```

---

### 📚 Event Store

Configurações do **Event Sourcing**.

#### Serialização

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `EVENTSTORE_SERIALIZATION_FORMAT` | `json` | Formato de serialização (`json`, `xml`) |
| `EVENTSTORE_COMPRESSION` | `gzip` | Algoritmo de compressão (`gzip`, `none`) |
| `EVENTSTORE_COMPRESSION_THRESHOLD` | `1024` | Tamanho mínimo para comprimir (bytes) |

#### Performance

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `EVENTSTORE_BATCH_SIZE` | `100` | Tamanho do lote para operações batch |
| `EVENTSTORE_WRITE_TIMEOUT` | `30` | Timeout de escrita (segundos) |
| `EVENTSTORE_READ_TIMEOUT` | `15` | Timeout de leitura (segundos) |
| `EVENTSTORE_CACHE_ENABLED` | `true` | Habilitar cache de eventos |
| `EVENTSTORE_CACHE_TTL` | `300` | TTL do cache (segundos) |

#### Particionamento

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `EVENTSTORE_PARTITIONING_ENABLED` | `true` | Habilitar particionamento de tabelas |
| `EVENTSTORE_PARTITIONING_STRATEGY` | `monthly` | Estratégia (`daily`, `monthly`, `yearly`) |
| `EVENTSTORE_PARTITIONING_FUTURE_PARTITIONS` | `3` | Partições futuras a criar |

#### Arquivamento

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `EVENTSTORE_ARCHIVE_ENABLED` | `true` | Habilitar arquivamento automático |
| `EVENTSTORE_ARCHIVE_AFTER_YEARS` | `2` | Arquivar eventos após N anos |
| `EVENTSTORE_ARCHIVE_DELETE_AFTER` | `false` | Deletar após arquivar |
| `EVENTSTORE_ARCHIVE_BASE_PATH` | `./data/archives` | Diretório de arquivos |

**Produção com S3:**
```bash
EVENTSTORE_ARCHIVE_BASE_PATH=s3://my-bucket/event-archives
```

#### Replay

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `EVENTSTORE_REPLAY_ENABLED` | `true` | Habilitar sistema de replay |
| `EVENTSTORE_REPLAY_BATCH_SIZE` | `100` | Tamanho do lote de replay |
| `EVENTSTORE_REPLAY_MAX_CONCURRENT` | `5` | Máximo de replays simultâneos |

---

### 📸 Snapshots

Otimização de **reconstrução de aggregates**.

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `SNAPSHOT_THRESHOLD` | `50` | Criar snapshot a cada N eventos |
| `SNAPSHOT_MAX_PER_AGGREGATE` | `5` | Máximo de snapshots por aggregate |
| `SNAPSHOT_COMPRESSION_ENABLED` | `true` | Habilitar compressão |
| `SNAPSHOT_COMPRESSION_ALGORITHM` | `GZIP` | Algoritmo (`GZIP`, `NONE`) |
| `SNAPSHOT_AUTO_CLEANUP_ENABLED` | `true` | Limpeza automática de snapshots antigos |
| `SNAPSHOT_CLEANUP_INTERVAL_HOURS` | `24` | Intervalo de limpeza (horas) |
| `SNAPSHOT_ASYNC_CREATION` | `true` | Criar snapshots assincronamente |
| `SNAPSHOT_RETENTION_DAYS` | `365` | Dias para manter snapshots |

---

### 🔄 CQRS

Configurações de **Command Query Responsibility Segregation**.

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `CQRS_COMMAND_TIMEOUT` | `30s` | Timeout de comandos |
| `CQRS_COMMAND_RETRY_MAX_ATTEMPTS` | `3` | Tentativas de retry |
| `CQRS_QUERY_CACHE_TTL` | `300s` | TTL do cache de queries |
| `CQRS_QUERY_CACHE_MAX_SIZE` | `10000` | Tamanho máximo do cache |
| `CQRS_PROJECTION_BATCH_SIZE` | `50` | Tamanho do lote de projeção |
| `CQRS_PROJECTION_PARALLEL` | `true` | Processamento paralelo |

---

### 🌐 Integrações Externas

#### DETRAN API (Exemplo)

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `DETRAN_BASE_URL` | `http://localhost:8080/detran-api` | URL base da API DETRAN |
| `DETRAN_TIMEOUT` | `30s` | Timeout de requisições |
| `DETRAN_RETRY_MAX_ATTEMPTS` | `3` | Tentativas de retry |
| `DETRAN_RETRY_BACKOFF_DELAY` | `2s` | Delay entre retries |

#### ViaCEP

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `VIACEP_BASE_URL` | `https://viacep.com.br/ws` | URL base ViaCEP |
| `VIACEP_TIMEOUT` | `10s` | Timeout de requisições |

---

### 📊 Monitoramento

Spring Boot Actuator e Observabilidade.

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `MANAGEMENT_ENDPOINTS_ENABLED` | `true` | Habilitar endpoints Actuator |
| `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` | `health,info,...` | Endpoints expostos |
| `MANAGEMENT_HEALTH_SHOW_DETAILS` | `always` | Mostrar detalhes de health |
| `MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED` | `true` | Exportar para Prometheus |
| `MANAGEMENT_TRACING_SAMPLING_PROBABILITY` | `0.1` | Probabilidade de tracing (0.0-1.0) |

**Produção (menos verboso):**
```bash
MANAGEMENT_HEALTH_SHOW_DETAILS=when-authorized
MANAGEMENT_TRACING_SAMPLING_PROBABILITY=0.01
```

---

### 📝 Logging

Configuração de logs.

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `LOGGING_LEVEL_ROOT` | `INFO` | Nível global (`DEBUG`, `INFO`, `WARN`, `ERROR`) |
| `LOGGING_LEVEL_COM_SEGURADORA_HIBRIDA` | `DEBUG` | Nível da aplicação |
| `LOGGING_LEVEL_ORG_SPRINGFRAMEWORK` | `INFO` | Nível do Spring |
| `LOGGING_LEVEL_ORG_AXONFRAMEWORK` | `DEBUG` | Nível do Axon |
| `LOGGING_LEVEL_ORG_HIBERNATE` | `WARN` | Nível do Hibernate |
| `LOGGING_FILE_NAME` | `logs/arquitetura-hibrida.log` | Arquivo de log |
| `LOGGING_FILE_MAX_SIZE` | `10MB` | Tamanho máximo do arquivo |
| `LOGGING_FILE_MAX_HISTORY` | `30` | Dias de retenção |

**Produção:**
```bash
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_COM_SEGURADORA_HIBRIDA=INFO
LOGGING_LEVEL_ORG_AXONFRAMEWORK=INFO
```

---

## 🎯 Perfis de Configuração

A aplicação suporta múltiplos perfis Spring:

### 1. `local` (Desenvolvimento)
- H2 em memória (sem PostgreSQL)
- Redis/Kafka opcionais
- Logs verbosos

```bash
SPRING_PROFILES_ACTIVE=local
```

### 2. `docker` (Docker Compose)
- PostgreSQL dual
- Redis + Kafka
- Configurações otimizadas

```bash
SPRING_PROFILES_ACTIVE=docker
```

### 3. `production` (Produção)
- Todas as otimizações
- Segurança reforçada
- Logs reduzidos

```bash
SPRING_PROFILES_ACTIVE=production
```

---

## 🔒 Boas Práticas de Segurança

### ⚠️ NUNCA commite .env no Git

```bash
# Adicione ao .gitignore
.env
.env.local
.env.*.local
```

### 🔐 Senhas Seguras

```bash
# Gerar senha segura (Linux/Mac)
openssl rand -base64 32

# Ou
pwgen -s 32 1
```

### 🎭 Gerenciamento de Secrets

**Desenvolvimento:**
- Usar `.env` local (não versionado)

**Produção:**
- **Kubernetes Secrets**
- **AWS Secrets Manager**
- **Azure Key Vault**
- **HashiCorp Vault**
- **Docker Swarm Secrets**

### 📋 Checklist de Produção

- [ ] Alterar TODAS as senhas padrão
- [ ] Configurar `REDIS_PASSWORD`
- [ ] Configurar `WRITE_DB_PASSWORD` e `READ_DB_PASSWORD`
- [ ] Ajustar `LOGGING_LEVEL_*` para `INFO` ou `WARN`
- [ ] Configurar `JWT_ISSUER_URI` se usar autenticação
- [ ] Definir `MANAGEMENT_HEALTH_SHOW_DETAILS=when-authorized`
- [ ] Ajustar `MANAGEMENT_TRACING_SAMPLING_PROBABILITY` (0.01 para prod)
- [ ] Configurar backup para `EVENTSTORE_ARCHIVE_BASE_PATH`
- [ ] Revisar todos os timeouts e pool sizes

---

## 📚 Referências

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Docker Compose Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [12-Factor App: Config](https://12factor.net/config)

---

**Última atualização:** 13/03/2026
**Versão:** 1.0
