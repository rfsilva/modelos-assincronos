# 📖 INTRODUÇÃO À ARQUITETURA - PARTE 5
## Exercícios Práticos e Checkpoint de Aprendizado

### 🎯 **OBJETIVOS DESTA PARTE**
- Aplicar conhecimentos através de exercícios práticos
- Explorar o código existente de forma guiada
- Validar compreensão dos conceitos fundamentais
- Preparar para os próximos módulos do roteiro

---

## 🧪 **EXERCÍCIOS PRÁTICOS**

### **🔍 Exercício 1: Exploração do Event Store**

#### **Objetivo:** Compreender como eventos são armazenados e recuperados

#### **Passos:**

**1. Conectar no Event Store Database:**
```bash
# Conectar no PostgreSQL Write
docker exec -it postgres-write psql -U postgres -d sinistros_eventstore

# Explorar estrutura
\dn                          # Listar schemas
\dt eventstore.*             # Listar tabelas do Event Store
```

**2. Examinar Estrutura da Tabela de Eventos:**
```sql
-- Descrever estrutura da tabela events
\d eventstore.events

-- Resultado esperado:
-- id (UUID) - Identificador único do evento
-- aggregate_id (VARCHAR) - ID do agregado
-- event_type (VARCHAR) - Tipo do evento
-- event_data (TEXT) - Dados do evento em JSON
-- version (BIGINT) - Versão do agregado
-- timestamp (TIMESTAMP) - Quando o evento ocorreu
-- correlation_id (UUID) - ID de correlação
-- metadata (JSONB) - Metadados adicionais
```

**3. Verificar Dados Existentes:**
```sql
-- Contar eventos por tipo
SELECT event_type, COUNT(*) 
FROM eventstore.events 
GROUP BY event_type;

-- Ver últimos eventos (se houver)
SELECT aggregate_id, event_type, timestamp, version
FROM eventstore.events 
ORDER BY timestamp DESC 
LIMIT 10;

-- Examinar estrutura de um evento
SELECT event_data 
FROM eventstore.events 
LIMIT 1;
```

**4. Explorar Tabela de Snapshots:**
```sql
-- Verificar estrutura de snapshots
\d eventstore.snapshots

-- Contar snapshots por agregado
SELECT aggregate_type, COUNT(*) 
FROM eventstore.snapshots 
GROUP BY aggregate_type;
```

#### **Questões para Reflexão:**
- Como a versão (version) garante consistência?
- Por que eventos são imutáveis?
- Qual a vantagem dos snapshots?

---

### **🔍 Exercício 2: Exploração das Projeções**

#### **Objetivo:** Entender como dados são desnormalizados para consultas

#### **Passos:**

**1. Conectar no Read Database:**
```bash
# Conectar no PostgreSQL Read
docker exec -it postgres-read psql -U postgres -d sinistros_projections
```

**2. Explorar Schema de Projeções:**
```sql
-- Listar schemas
\dn

-- Listar tabelas de projeções
\dt projections.*

-- Examinar estrutura da view de sinistros
\d projections.sinistro_view
```

**3. Verificar Tracking de Projeções:**
```sql
-- Ver status das projeções
SELECT projection_name, status, last_processed_event_id, 
       events_processed, events_failed, last_processed_at
FROM projections.projection_tracking;

-- Resultado esperado:
-- SinistroProjectionHandler | ACTIVE | 0 | 0 | 0 | timestamp
-- SeguradoProjectionHandler | ACTIVE | 0 | 0 | 0 | timestamp
```

**4. Analisar Índices Otimizados:**
```sql
-- Ver índices da tabela sinistro_view
\d+ projections.sinistro_view

-- Verificar índices específicos
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'sinistro_view' 
AND schemaname = 'projections';
```

#### **Questões para Reflexão:**
- Por que dados são desnormalizados no Query Side?
- Como o tracking garante que nenhum evento seja perdido?
- Qual a vantagem de ter índices específicos para consultas?

---

### **🔍 Exercício 3: Testando APIs de Monitoramento**

#### **Objetivo:** Familiarizar-se com as APIs de observabilidade

#### **Passos:**

**1. Health Check Geral:**
```bash
# Verificar saúde geral
curl -s http://localhost:8083/api/v1/actuator/health | jq

# Examinar cada componente
curl -s http://localhost:8083/api/v1/actuator/health | jq '.components'
```

**2. Monitoramento CQRS:**
```bash
# Status do CQRS
curl -s http://localhost:8083/api/v1/actuator/cqrs | jq

# Resposta esperada:
{
  "status": "HEALTHY",
  "commandSide": {
    "status": "UP",
    "totalEvents": 0,
    "totalAggregates": 0
  },
  "querySide": {
    "status": "UP", 
    "totalProjections": 2,
    "activeProjections": 2,
    "errorProjections": 0
  },
  "lag": {
    "maxLag": 0,
    "avgLag": 0,
    "status": "HEALTHY"
  }
}
```

**3. Command Bus Statistics:**
```bash
# Estatísticas do Command Bus
curl -s http://localhost:8083/api/v1/actuator/commandbus | jq

# Handlers registrados
curl -s http://localhost:8083/api/v1/actuator/commandbus/handlers | jq
```

**4. Event Bus Statistics:**
```bash
# Estatísticas do Event Bus
curl -s http://localhost:8083/api/v1/actuator/eventbus | jq

# Handlers de eventos registrados
curl -s http://localhost:8083/api/v1/actuator/eventbus/handlers | jq
```

**5. Projeções:**
```bash
# Status das projeções
curl -s http://localhost:8083/api/v1/actuator/projections | jq

# Detalhes de uma projeção específica
curl -s http://localhost:8083/api/v1/actuator/projections/SinistroProjectionHandler | jq
```

#### **Análise dos Resultados:**
- Quantos handlers estão registrados?
- Qual o status de cada projeção?
- Há algum lag entre Command e Query Side?

---

### **🔍 Exercício 4: Explorando o Código**

#### **Objetivo:** Navegar pelo código para entender implementações

#### **Passos:**

**1. Examinar Command Handler:**
```java
// Localizar: src/main/java/com/seguradora/hibrida/command/example/TestCommandHandler.java

// Questões:
// - Como o handler é registrado automaticamente?
// - Qual o padrão de nomenclatura?
// - Como erros são tratados?
```

**2. Examinar Event Handler:**
```java
// Localizar: src/main/java/com/seguradora/hibrida/eventbus/example/SinistroEventHandler.java

// Questões:
// - Como eventos são roteados para handlers?
// - Qual a diferença entre processamento síncrono e assíncrono?
// - Como retry é implementado?
```

**3. Examinar Projection Handler:**
```java
// Localizar: src/main/java/com/seguradora/hibrida/projection/example/SinistroProjectionHandler.java

// Questões:
// - Como a posição é controlada?
// - O que acontece em caso de erro?
// - Como rebuild funciona?
```

**4. Examinar Query Repository:**
```java
// Localizar: src/main/java/com/seguradora/hibrida/query/repository/SinistroQueryRepository.java

// Questões:
// - Quais tipos de consulta estão implementadas?
// - Como full-text search funciona?
// - Quais otimizações estão aplicadas?
```

#### **Tarefa Prática:**
Criar um diagrama simples mostrando como os componentes se conectam.

---

### **🔍 Exercício 5: Simulando Operações**

#### **Objetivo:** Entender o fluxo completo através de simulação

#### **Passos:**

**1. Preparar Ambiente de Teste:**
```bash
# Verificar se aplicação está rodando
curl http://localhost:8083/api/v1/actuator/health

# Limpar dados anteriores (se necessário)
# Conectar nos bancos e truncar tabelas de teste
```

**2. Simular Criação de Comando (via logs):**
```bash
# Monitorar logs da aplicação
tail -f logs/hibrida.log | grep -E "(Command|Event|Projection)"

# Em outro terminal, simular comando via API (se implementado)
# Ou examinar como seria o fluxo baseado no código
```

**3. Verificar Propagação:**
```bash
# Verificar se evento foi persistido
docker exec -it postgres-write psql -U postgres -d sinistros_eventstore -c "SELECT COUNT(*) FROM eventstore.events;"

# Verificar se projeção foi atualizada
docker exec -it postgres-read psql -U postgres -d sinistros_projections -c "SELECT COUNT(*) FROM projections.sinistro_view;"

# Verificar tracking
curl -s http://localhost:8083/api/v1/actuator/projections | jq '.projections[].eventsProcessed'
```

**4. Analisar Métricas:**
```bash
# Verificar métricas após operação
curl -s http://localhost:8083/api/v1/actuator/prometheus | grep -E "(command|event|projection)"
```

#### **Observações:**
- Qual foi o tempo de propagação?
- Houve algum erro no processamento?
- As métricas foram atualizadas corretamente?

---

## ✅ **CHECKPOINT DE APRENDIZADO**

### **📋 Autoavaliação**

#### **Conceitos Fundamentais:**
- [ ] Consigo explicar a diferença entre Command Side e Query Side
- [ ] Entendo como eventos conectam os dois lados
- [ ] Compreendo o papel do Event Store
- [ ] Sei como projeções são mantidas atualizadas

#### **Arquitetura:**
- [ ] Consigo desenhar o fluxo de dados da arquitetura
- [ ] Entendo a organização de pacotes do projeto
- [ ] Sei onde encontrar cada tipo de componente
- [ ] Compreendo as responsabilidades de cada módulo

#### **Implementação:**
- [ ] Sei como navegar pelo código do projeto
- [ ] Entendo as convenções de nomenclatura
- [ ] Consigo identificar padrões implementados
- [ ] Sei usar as APIs de monitoramento

#### **Operação:**
- [ ] Consigo configurar o ambiente local
- [ ] Sei verificar a saúde dos componentes
- [ ] Entendo como monitorar o sistema
- [ ] Consigo interpretar métricas básicas

### **❓ Perguntas de Validação**

#### **1. Conceitual:**
- Por que separamos Command Side e Query Side?
- Qual a vantagem do Event Sourcing sobre CRUD tradicional?
- Como garantimos que Query Side está sempre atualizado?
- Quando usar essa arquitetura vs arquitetura tradicional?

#### **2. Técnica:**
- Onde são armazenados os eventos no projeto?
- Como um comando é roteado para seu handler?
- Como projeções controlam sua posição no stream de eventos?
- Quais implementações de Event Bus estão disponíveis?

#### **3. Prática:**
- Como verificar se o sistema está saudável?
- Onde encontrar logs de uma operação específica?
- Como identificar lag entre Command e Query Side?
- Como fazer rebuild de uma projeção?

### **🎯 Critérios de Aprovação**

Para prosseguir para o próximo módulo, você deve:

#### **✅ Obrigatório:**
- [ ] Conseguir executar a aplicação localmente
- [ ] Acessar todas as APIs de monitoramento
- [ ] Navegar pela estrutura do código
- [ ] Explicar o fluxo básico de dados

#### **✅ Recomendado:**
- [ ] Entender diferenças entre implementações (Simple vs Kafka)
- [ ] Interpretar métricas de performance
- [ ] Identificar possíveis problemas via health checks
- [ ] Relacionar código com conceitos teóricos

---

## 🚀 **PRÓXIMOS PASSOS**

### **📚 Preparação para Módulos Avançados**

#### **Módulo 2 - Event Sourcing:**
- Modelagem de eventos
- Versionamento de eventos  
- Snapshots e otimizações
- Replay e reconstrução de estado

#### **Módulo 3 - CQRS:**
- Projeções avançadas
- Consistency patterns
- Performance tuning
- Monitoring e alertas

#### **Módulo 4 - Domain Driven Design:**
- Agregados e bounded contexts
- Business rules
- Domain events
- Tactical patterns

### **🔧 Configurações Recomendadas**

#### **IDE Setup:**
```properties
# IntelliJ IDEA - Live Templates para acelerar desenvolvimento
# File > Settings > Editor > Live Templates

# Criar templates para:
# - Command Handler
# - Event Handler  
# - Projection Handler
# - Query Repository methods
```

#### **Debugging Setup:**
```yaml
# application-dev.yml - Para debugging detalhado
logging:
  level:
    com.seguradora.hibrida: DEBUG
    org.springframework.transaction: DEBUG
    
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

---

## 📚 **RECURSOS COMPLEMENTARES**

### **🔗 Leitura Adicional:**
- [Event Sourcing Pattern](https://martinfowler.com/eaaDev/EventSourcing.html)
- [CQRS Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/cqrs)
- [Domain Events](https://martinfowler.com/eaaDev/DomainEvent.html)
- [Microservices Patterns](https://microservices.io/patterns/)

### **🎥 Vídeos Recomendados:**
- "Event Sourcing and CQRS" - Greg Young
- "Building Event-Driven Microservices" - Adam Bellemare
- "Domain-Driven Design" - Eric Evans

### **📖 Livros:**
- "Implementing Domain-Driven Design" - Vaughn Vernon
- "Building Event-Driven Microservices" - Adam Bellemare
- "Microservices Patterns" - Chris Richardson

### **🛠️ Ferramentas Úteis:**
- **EventStore DB**: Para Event Sourcing avançado
- **Apache Kafka**: Para Event Streaming
- **Axon Framework**: Framework para CQRS/ES em Java
- **Prometheus + Grafana**: Para monitoramento avançado

---

## 🎓 **CERTIFICAÇÃO DE CONCLUSÃO**

### **📜 Critérios Atendidos:**

- [x] **Conceitos**: Compreensão dos fundamentos da arquitetura híbrida
- [x] **Estrutura**: Conhecimento da organização do projeto
- [x] **Ambiente**: Capacidade de configurar e executar localmente
- [x] **Fluxos**: Entendimento da comunicação entre componentes
- [x] **Prática**: Experiência hands-on com o código e APIs

### **🏆 Próximo Nível:**
Você está preparado para avançar para módulos específicos:
- **Event Sourcing** (Módulo 2)
- **CQRS** (Módulo 3) 
- **Domain Driven Design** (Módulo 4)

---

**📝 Parte 5 de 5 - Exercícios Práticos e Checkpoint**  
**⏱️ Tempo estimado**: 90 minutos  
**🎯 Conclusão**: Fundamentos da Arquitetura Híbrida dominados!

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Aplicação prática dos conceitos fundamentais  
**⏱️ Tempo total do módulo:** 5 horas  
**🔧 Hands-on:** Exploração completa da arquitetura implementada