# 🎯 Guia Atualizado - Kafka UI e Interfaces (PORTAS CORRIGIDAS)

## 🚨 **PROBLEMA RESOLVIDO: Conflito de Portas**

O erro de porta já foi **corrigido**. Agora o ambiente usa portas que não conflitam com serviços locais.

## 📊 Interfaces Disponíveis (PORTAS ATUALIZADAS)

| Serviço | Interface | URL | Porta Externa | Porta Interna |
|---------|-----------|-----|---------------|---------------|
| **Kafka** | Kafka UI | http://localhost:8080 | 8080 | 8080 |
| **Redis** | Redis Commander | http://localhost:8081 | 8081 | 8081 |
| **PostgreSQL** | PgAdmin | http://localhost:8082 | 8082 | 80 |

## 🔧 Portas dos Serviços (SEM CONFLITO)

| Serviço | Porta Externa | Porta Interna | Observação |
|---------|---------------|---------------|------------|
| **Redis** | **6380** | 6379 | ✅ Evita conflito com Redis local (6379) |
| **Kafka** | **9093** | 9092 | ✅ Evita conflito com Kafka local (9092) |
| **PostgreSQL Write** | **5435** | 5432 | ✅ Evita conflito com PostgreSQL local (5432) |
| **PostgreSQL Read** | **5436** | 5432 | ✅ Evita conflito com PostgreSQL local (5432) |

## 🚀 Como Usar (ATUALIZADO)

### **1. Subir o Ambiente:**
```bash
cd app-arquitetura-hibrida
docker-compose down  # Para qualquer container anterior
docker-compose up -d
```

### **2. Verificar Status:**
```bash
docker-compose ps
```

**Resultado Esperado:**
```
NAME                     STATUS
hibrida-postgres-write   Up (healthy)
hibrida-postgres-read    Up (healthy)
hibrida-redis           Up (healthy)
hibrida-zookeeper       Up (healthy)
hibrida-kafka           Up (healthy)
hibrida-kafka-init      Exited (0)
hibrida-kafka-ui        Up (healthy)
hibrida-redis-commander Up (healthy)
hibrida-pgadmin         Up (healthy)
```

### **3. Acessar Kafka UI:**
```
http://localhost:8080
```

## 🎯 Kafka UI - Funcionalidades Completas

### **📋 O que você verá no Kafka UI:**

#### **1. Dashboard Principal**
- ✅ **Cluster "Hibrida Kafka Cluster"** conectado
- ✅ **1 Broker ativo** (hibrida-kafka)
- ✅ **Métricas em tempo real**
- ✅ **Throughput de mensagens**

#### **2. Topics (Tópicos)**
Você verá **todos os tópicos** criados automaticamente:

**Eventos de Domínio (Event Sourcing):**
```
segurado.criado          - 3 partições, compact, 7 dias
segurado.atualizado      - 3 partições, compact, 7 dias
segurado.desativado      - 3 partições, compact, 7 dias
segurado.reativado       - 3 partições, compact, 7 dias

apolice.criada          - 3 partições, compact, 7 dias
apolice.atualizada      - 3 partições, compact, 7 dias
apolice.cancelada       - 3 partições, compact, 7 dias
apolice.renovada        - 3 partições, compact, 7 dias

sinistro.criado         - 3 partições, compact, 7 dias
sinistro.atualizado     - 3 partições, compact, 7 dias
sinistro.aprovado       - 3 partições, compact, 7 dias
sinistro.rejeitado      - 3 partições, compact, 7 dias

veiculo.criado          - 3 partições, compact, 7 dias
veiculo.atualizado      - 3 partições, compact, 7 dias
veiculo.associado       - 3 partições, compact, 7 dias
veiculo.desassociado    - 3 partições, compact, 7 dias
```

**Eventos de Integração:**
```
detran.consulta.iniciada   - 3 partições, delete, 3 dias
detran.consulta.concluida  - 3 partições, delete, 3 dias
detran.consulta.erro       - 3 partições, delete, 3 dias
```

**Eventos de Notificação:**
```
notificacao.email       - 3 partições, delete, 1 dia
notificacao.sms         - 3 partições, delete, 1 dia
notificacao.push        - 3 partições, delete, 1 dia
```

### **🧪 Testando Produção de Mensagens**

#### **Via Kafka UI (Recomendado):**

1. **Acesse:** http://localhost:8080
2. **Vá em:** Topics → Selecione `segurado.criado`
3. **Clique:** "Produce Message"
4. **Configure:**
   ```json
   Key: "segurado-123"
   Value: {
     "eventId": "evt-001",
     "eventType": "SeguradoCriadoEvent",
     "aggregateId": "segurado-123",
     "aggregateType": "SeguradoAggregate",
     "version": 1,
     "timestamp": "2024-12-19T10:00:00Z",
     "data": {
       "nome": "João Silva",
       "cpf": "12345678901",
       "email": "joao@email.com",
       "telefone": "11987654321"
     },
     "metadata": {
       "source": "kafka-ui-test",
       "correlationId": "test-001"
     }
   }
   ```
5. **Clique:** "Produce"
6. **Vá em:** Messages → Veja sua mensagem aparecer!

#### **Via Linha de Comando:**

```bash
# Produzir mensagem (porta externa 9093)
docker exec -it hibrida-kafka kafka-console-producer \
  --topic segurado.criado \
  --bootstrap-server localhost:9092

# Consumir mensagens (porta externa 9093)
docker exec -it hibrida-kafka kafka-console-consumer \
  --topic segurado.criado \
  --from-beginning \
  --bootstrap-server localhost:9092
```

## 🗄️ Redis Commander

### **Acessar:**
```
http://localhost:8081
```

### **Testar Redis:**
```bash
# Via container (porta interna)
docker exec -it hibrida-redis redis-cli ping

# Via aplicação local (porta externa 6380)
redis-cli -h localhost -p 6380 ping
```

### **Comandos Úteis:**
```redis
# Testar cache
SET test:key "Hello from Docker Redis"
GET test:key
TTL test:key

# Ver configurações
CONFIG GET maxmemory
CONFIG GET maxmemory-policy
INFO memory
```

## 🐘 PgAdmin

### **Acessar:**
```
http://localhost:8082
```

**Credenciais:**
- Email: `admin@hibrida.com`
- Senha: `admin123`

### **Servidores Pré-configurados:**

#### **Event Store (Write DB):**
- **Nome:** Event Store (Write DB)
- **Host:** postgres-write:5432
- **Database:** sinistros_eventstore
- **Schema:** eventstore

#### **Projections (Read DB):**
- **Nome:** Projections (Read DB)
- **Host:** postgres-read:5432
- **Database:** sinistros_projections
- **Schema:** projections

### **Validar Estruturas Flyway:**

```sql
-- Event Store
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'eventstore';

-- Deve mostrar: events, snapshots, projection_tracking, etc.

-- Projections
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'projections';

-- Deve mostrar: sinistro_view, detran_consulta_view, etc.
```

## 🔗 Conectividade da Aplicação

### **Para Desenvolvimento Local:**

Se você quiser rodar a aplicação **localmente** conectando à infraestrutura Docker:

```yaml
# application-local.yml
spring:
  data:
    redis:
      host: localhost
      port: 6380  # Porta EXTERNA do Docker
  kafka:
    bootstrap-servers: localhost:9093  # Porta EXTERNA do Docker

app:
  datasource:
    write:
      url: jdbc:postgresql://localhost:5435/sinistros_eventstore  # Porta EXTERNA
    read:
      url: jdbc:postgresql://localhost:5436/sinistros_projections  # Porta EXTERNA
```

### **Para Aplicação no Docker:**

```yaml
# application-docker.yml (já configurado)
spring:
  data:
    redis:
      host: redis
      port: 6379  # Porta INTERNA do container
  kafka:
    bootstrap-servers: kafka:29092  # Porta INTERNA do container

app:
  datasource:
    write:
      url: jdbc:postgresql://postgres-write:5432/sinistros_eventstore  # Porta INTERNA
    read:
      url: jdbc:postgresql://postgres-read:5432/sinistros_projections  # Porta INTERNA
```

## 🎯 Fluxo de Validação Completo

### **1. Subir e Verificar:**
```bash
docker-compose up -d
docker-compose ps  # Todos "healthy"
```

### **2. Acessar Interfaces:**
- ✅ http://localhost:8080 (Kafka UI)
- ✅ http://localhost:8081 (Redis Commander)
- ✅ http://localhost:8082 (PgAdmin)

### **3. Validar Kafka:**
- Ver todos os tópicos listados
- Produzir mensagem de teste
- Verificar configurações de retenção

### **4. Validar Redis:**
- Executar comandos básicos
- Verificar configurações de memória

### **5. Validar PostgreSQL:**
- Conectar aos dois bancos
- Verificar tabelas criadas pelo Flyway
- Executar queries de teste

## 🎉 Resultado Final

Agora você tem um ambiente **100% funcional** com:

- ✅ **Sem conflitos de porta** com serviços locais
- ✅ **Kafka UI** mostrando todos os tópicos
- ✅ **Redis Commander** para gestão de cache
- ✅ **PgAdmin** com bancos pré-configurados
- ✅ **Todas as estruturas** criadas pelo Flyway
- ✅ **Pronto para desenvolvimento** e demonstrações

**O ambiente está perfeito para usar o Kafka UI e monitorar toda a arquitetura!** 🚀

---

**Última atualização**: 2024-12-19  
**Status**: ✅ **FUNCIONANDO PERFEITAMENTE**