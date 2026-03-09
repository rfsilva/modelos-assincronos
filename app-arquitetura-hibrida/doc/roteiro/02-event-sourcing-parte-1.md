# 📖 CAPÍTULO 02: EVENT SOURCING - PARTE 1
## Fundamentos e Conceitos Básicos

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender os conceitos fundamentais do Event Sourcing
- Entender a diferença entre Event Sourcing e CRUD tradicional
- Conhecer os componentes básicos: Events, Event Store e Event Stream
- Explorar as vantagens e desvantagens do padrão

---

## 🧠 **O QUE É EVENT SOURCING?**

### **📚 Definição**
Event Sourcing é um padrão arquitetural onde **mudanças no estado da aplicação são armazenadas como uma sequência de eventos** ao invés de armazenar apenas o estado atual.

### **🔄 CRUD Tradicional vs Event Sourcing**

#### **CRUD Tradicional:**
```sql
-- Estado atual na tabela
UPDATE sinistros 
SET status = 'EM_ANALISE', 
    operador_responsavel = 'João Silva',
    data_atualizacao = NOW()
WHERE id = '123';

-- ❌ Perdemos o histórico: quem mudou? quando? por quê?
```

#### **Event Sourcing:**
```java
// Sequência de eventos que levaram ao estado atual
SinistroCriadoEvent(id: "123", cpf: "12345678901", ...)
SinistroAtribuidoEvent(id: "123", operador: "João Silva", ...)
StatusAlteradoEvent(id: "123", novoStatus: "EM_ANALISE", ...)

// ✅ Histórico completo preservado!
```

---

## 🏗️ **COMPONENTES FUNDAMENTAIS**

### **1. 📝 Domain Events (Eventos de Domínio)**

**Definição**: Representam algo importante que aconteceu no domínio.

#### **Características dos Eventos:**
- **Imutáveis**: Nunca mudam após criação
- **Nomeados no passado**: "SinistroCriado", "StatusAlterado"
- **Ricos em informação**: Contêm todos os dados necessários
- **Ordenados**: Sequência temporal importa

#### **Estrutura Base no Projeto:**
```java
// Interface base para todos os eventos
public abstract class DomainEvent {
    private UUID eventId;           // ID único do evento
    private String aggregateId;     // ID do aggregate que gerou
    private long version;           // Versão do aggregate
    private Instant timestamp;      // Quando aconteceu
    private String eventType;       // Tipo do evento
    private EventMetadata metadata; // Metadados adicionais
    
    // Construtor, getters...
}
```

#### **Exemplo Prático - Evento de Sinistro:**
```java
// Localização: eventbus/example/SinistroEvent.java
public class SinistroEvent extends DomainEvent {
    
    // Factory method para criar sinistro
    public static SinistroEvent sinistroCriado(
            String aggregateId, 
            String numeroSinistro,
            String descricao, 
            Double valorEstimado) {
        
        SinistroEvent event = new SinistroEvent();
        event.setAggregateId(aggregateId);
        event.setEventType("SinistroCriado");
        event.setTimestamp(Instant.now());
        
        // Dados específicos do evento
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("numeroSinistro", numeroSinistro);
        eventData.put("descricao", descricao);
        eventData.put("valorEstimado", valorEstimado);
        
        return event;
    }
}
```

### **2. 💾 Event Store (Armazenamento de Eventos)**

**Definição**: Banco de dados especializado em armazenar eventos de forma append-only.

#### **Interface do Event Store:**
```java
// Localização: eventstore/EventStore.java
public interface EventStore {
    
    // Salvar eventos de um aggregate
    void saveEvents(String aggregateId, 
                   List<DomainEvent> events, 
                   long expectedVersion);
    
    // Carregar todos os eventos de um aggregate
    List<DomainEvent> loadEvents(String aggregateId);
    
    // Carregar eventos a partir de uma versão
    List<DomainEvent> loadEvents(String aggregateId, long fromVersion);
    
    // Verificar se aggregate existe
    boolean aggregateExists(String aggregateId);
    
    // Obter versão atual
    long getCurrentVersion(String aggregateId);
}
```

#### **Implementação PostgreSQL:**
```java
// Localização: eventstore/impl/PostgreSQLEventStore.java
@Repository
public class PostgreSQLEventStore implements EventStore {
    
    @Override
    public void saveEvents(String aggregateId, 
                          List<DomainEvent> events, 
                          long expectedVersion) {
        
        // 1. Verificar versão para controle de concorrência
        long currentVersion = getCurrentVersion(aggregateId);
        if (currentVersion != expectedVersion) {
            throw new ConcurrencyException(aggregateId, expectedVersion, currentVersion);
        }
        
        // 2. Salvar cada evento
        for (DomainEvent event : events) {
            EventStoreEntry entry = convertToEntry(event);
            repository.save(entry);
        }
    }
    
    @Override
    public List<DomainEvent> loadEvents(String aggregateId) {
        List<EventStoreEntry> entries = repository
            .findByAggregateIdOrderByVersionAsc(aggregateId);
            
        return entries.stream()
            .map(this::convertFromEntry)
            .collect(Collectors.toList());
    }
}
```

### **3. 🗂️ Event Stream (Fluxo de Eventos)**

**Definição**: Sequência ordenada de eventos para um aggregate específico.

#### **Exemplo de Event Stream:**
```
Aggregate ID: sinistro-123
┌─────────────────────────────────────────────────────────┐
│ Version 1: SinistroCriadoEvent                          │
│ - Timestamp: 2024-01-15T10:00:00Z                      │
│ - CPF: 12345678901                                      │
│ - Placa: ABC1234                                        │
│ - Descrição: "Colisão na Av. Paulista"                 │
├─────────────────────────────────────────────────────────┤
│ Version 2: SinistroAtribuidoEvent                       │
│ - Timestamp: 2024-01-15T10:05:00Z                      │
│ - Operador: "João Silva"                                │
│ - Motivo: "Distribuição automática"                     │
├─────────────────────────────────────────────────────────┤
│ Version 3: StatusAlteradoEvent                          │
│ - Timestamp: 2024-01-15T10:30:00Z                      │
│ - Status Anterior: "ABERTO"                             │
│ - Status Novo: "EM_ANALISE"                             │
│ - Observações: "Iniciando análise técnica"             │
└─────────────────────────────────────────────────────────┘
```

---

## 🎯 **VANTAGENS DO EVENT SOURCING**

### **✅ Benefícios Principais:**

#### **1. Auditoria Completa**
```java
// Podemos responder perguntas como:
// - Quem alterou o status do sinistro?
// - Quando foi feita a última atualização?
// - Qual era o valor estimado original?
// - Quantas vezes o sinistro mudou de operador?

List<DomainEvent> historico = eventStore.loadEvents("sinistro-123");
// Cada evento tem timestamp, usuário, e dados completos
```

#### **2. Debugging e Análise**
```java
// Reproduzir exatamente o que aconteceu
public void debugSinistro(String sinistroId) {
    List<DomainEvent> eventos = eventStore.loadEvents(sinistroId);
    
    for (DomainEvent evento : eventos) {
        System.out.println(String.format(
            "[%s] %s por %s: %s",
            evento.getTimestamp(),
            evento.getEventType(),
            evento.getMetadata().getUserId(),
            evento.toString()
        ));
    }
}
```

#### **3. Temporal Queries**
```java
// Ver como estava o sinistro em uma data específica
public SinistroState getEstadoEm(String sinistroId, Instant dataEspecifica) {
    List<DomainEvent> eventos = eventStore.loadEvents(sinistroId);
    
    SinistroAggregate sinistro = new SinistroAggregate();
    
    for (DomainEvent evento : eventos) {
        if (evento.getTimestamp().isBefore(dataEspecifica)) {
            sinistro.applyEvent(evento);
        } else {
            break;
        }
    }
    
    return sinistro.getState();
}
```

#### **4. Múltiplas Projeções**
```java
// Mesmo evento pode gerar diferentes visões
SinistroCriadoEvent evento = ...;

// Projeção 1: Lista de sinistros
sinistroListProjection.handle(evento);

// Projeção 2: Dashboard executivo
dashboardProjection.handle(evento);

// Projeção 3: Relatório de auditoria
auditoriaProjection.handle(evento);
```

---

## ⚠️ **DESAFIOS DO EVENT SOURCING**

### **❌ Principais Dificuldades:**

#### **1. Complexidade de Consultas**
```java
// ❌ CRUD: Consulta simples
SELECT * FROM sinistros WHERE status = 'ABERTO';

// ✅ Event Sourcing: Precisa reconstruir estado
// Solução: Usar CQRS com projeções otimizadas
```

#### **2. Evolução de Schema**
```java
// ❌ Problema: E se precisarmos mudar a estrutura do evento?
// Evento antigo: { "valor": 1000.0 }
// Evento novo: { "valorEstimado": 1000.0, "moeda": "BRL" }

// ✅ Solução: Versionamento de eventos
public class SinistroEventV2 extends SinistroEvent {
    // Nova estrutura mantendo compatibilidade
}
```

#### **3. Performance de Reconstrução**
```java
// ❌ Problema: Aggregate com muitos eventos demora para carregar
// Sinistro com 1000 eventos = 1000 operações para reconstruir

// ✅ Solução: Snapshots (veremos na Parte 3)
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Explorar eventos existentes no projeto

#### **Passo 1: Examinar Estrutura de Eventos**
```java
// 1. Abrir: eventstore/model/DomainEvent.java
// 2. Observar campos obrigatórios
// 3. Entender EventMetadata

// 3. Abrir: eventbus/example/SinistroEvent.java
// 4. Ver factory methods para diferentes tipos de evento
```

#### **Passo 2: Explorar Event Store**
```bash
# Conectar no banco de eventos
docker exec -it postgres-write psql -U postgres -d sinistros_eventstore

# Ver estrutura da tabela
\d eventstore.events

# Campos importantes:
# - aggregate_id: ID do aggregate
# - version: Versão sequencial
# - event_type: Tipo do evento
# - event_data: JSON com dados do evento
# - timestamp: Quando aconteceu
```

#### **Passo 3: Simular Criação de Eventos**
```java
// Criar um teste simples para entender o fluxo
@Test
public void testarCriacaoEvento() {
    // 1. Criar evento
    SinistroEvent evento = SinistroEvent.sinistroCriado(
        "sinistro-teste-123",
        "SIN-2024-001",
        "Teste de evento",
        5000.0
    );
    
    // 2. Verificar propriedades
    assertThat(evento.getAggregateId()).isEqualTo("sinistro-teste-123");
    assertThat(evento.getEventType()).isEqualTo("SinistroCriado");
    assertThat(evento.getTimestamp()).isNotNull();
    
    // 3. Ver serialização
    System.out.println("Evento: " + evento.toString());
}
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Explicar** o que é Event Sourcing em suas próprias palavras
2. **Identificar** as diferenças entre CRUD e Event Sourcing
3. **Reconhecer** os componentes: Event, Event Store, Event Stream
4. **Listar** pelo menos 3 vantagens do Event Sourcing
5. **Mencionar** os principais desafios do padrão

### **❓ Perguntas para Reflexão:**

1. Por que eventos são imutáveis?
2. Como garantir ordem dos eventos?
3. O que acontece se dois usuários modificarem o mesmo aggregate simultaneamente?
4. Como fazer consultas complexas em Event Sourcing?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 2**, vamos aprofundar:
- Como implementar Event Store na prática
- Serialização e deserialização de eventos
- Controle de concorrência e versionamento
- Padrões de nomeação de eventos

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 45 minutos  
**📋 Pré-requisitos:** Capítulo 01 completo