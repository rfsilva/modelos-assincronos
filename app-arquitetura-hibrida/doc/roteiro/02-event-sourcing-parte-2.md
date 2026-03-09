# 📖 CAPÍTULO 02: EVENT SOURCING - PARTE 2
## Implementação Prática do Event Store

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender a implementação do Event Store no projeto
- Entender serialização e deserialização de eventos
- Explorar controle de concorrência e versionamento
- Conhecer as estratégias de particionamento e performance

---

## 💾 **IMPLEMENTAÇÃO DO EVENT STORE**

### **🏗️ Arquitetura do Event Store**

```
┌─────────────────────────────────────────────────────────┐
│                    EVENT STORE                          │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────┐    ┌─────────────────┐            │
│  │   Interface     │    │  Implementation │            │
│  │   EventStore    │◄───│ PostgreSQLEvent │            │
│  │                 │    │     Store       │            │
│  └─────────────────┘    └─────────────────┘            │
│           ▲                       │                     │
│           │                       ▼                     │
│  ┌─────────────────┐    ┌─────────────────┐            │
│  │  EventStore     │    │  EventStore     │            │
│  │  Repository     │    │  Entry          │            │
│  │  (JPA)          │    │  (Entity)       │            │
│  └─────────────────┘    └─────────────────┘            │
│           │                       │                     │
│           ▼                       ▼                     │
│  ┌─────────────────────────────────────────────────────┤
│  │            PostgreSQL Database                      │
│  │         (eventstore.events table)                  │
│  └─────────────────────────────────────────────────────┘
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### **🗃️ Estrutura da Tabela de Eventos**

```sql
-- Localização: db/migration/V1__Create_Events_Table.sql
CREATE TABLE eventstore.events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_data JSONB NOT NULL,
    metadata JSONB,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    correlation_id UUID,
    user_id VARCHAR(255),
    data_size INTEGER,
    compressed BOOLEAN DEFAULT FALSE,
    
    -- Índices para performance
    CONSTRAINT events_aggregate_version_unique 
        UNIQUE (aggregate_id, version)
);

-- Índices otimizados
CREATE INDEX idx_events_aggregate_id ON eventstore.events (aggregate_id);
CREATE INDEX idx_events_timestamp ON eventstore.events (timestamp);
CREATE INDEX idx_events_event_type ON eventstore.events (event_type);
CREATE INDEX idx_events_correlation_id ON eventstore.events (correlation_id);
```

### **📊 Entity de Persistência**

```java
// Localização: eventstore/entity/EventStoreEntry.java
@Entity
@Table(name = "events", schema = "eventstore")
public class EventStoreEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;
    
    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;
    
    @Column(name = "version", nullable = false)
    private Long version;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(name = "event_data", nullable = false, columnDefinition = "jsonb")
    private String eventData;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;
    
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    
    @Column(name = "correlation_id")
    private UUID correlationId;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "data_size")
    private Integer dataSize;
    
    @Column(name = "compressed")
    private Boolean compressed = false;
    
    // Lifecycle callbacks
    @PrePersist
    public void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (eventData != null) {
            dataSize = eventData.length();
        }
    }
}
```

---

## 🔄 **SERIALIZAÇÃO DE EVENTOS**

### **🛠️ Event Serializer Interface**

```java
// Localização: eventstore/serialization/EventSerializer.java
public interface EventSerializer {
    
    /**
     * Serializa um evento para JSON
     */
    String serialize(DomainEvent event);
    
    /**
     * Deserializa JSON para evento
     */
    DomainEvent deserialize(String eventData, String eventType);
    
    /**
     * Serializa com compressão se necessário
     */
    SerializationResult serializeWithCompression(
        DomainEvent event, 
        int compressionThreshold
    );
    
    /**
     * Deserializa dados possivelmente comprimidos
     */
    DomainEvent deserializeCompressed(
        String eventData, 
        String eventType, 
        boolean compressed
    );
    
    /**
     * Verifica se suporta o tipo de evento
     */
    boolean supports(String eventType);
}
```

### **🔧 Implementação JSON**

```java
// Localização: eventstore/serialization/JsonEventSerializer.java
@Component
public class JsonEventSerializer implements EventSerializer {
    
    private final ObjectMapper objectMapper;
    
    public JsonEventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // Configurar para lidar com tipos polimórficos
        this.objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }
    
    @Override
    public String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new SerializationException(
                "Erro ao serializar evento: " + event.getEventType(), e
            );
        }
    }
    
    @Override
    public DomainEvent deserialize(String eventData, String eventType) {
        try {
            // Usar reflexão para encontrar a classe do evento
            Class<?> eventClass = Class.forName(
                "com.seguradora.hibrida.eventbus.example." + eventType
            );
            
            return (DomainEvent) objectMapper.readValue(eventData, eventClass);
            
        } catch (Exception e) {
            throw new SerializationException(
                "Erro ao deserializar evento: " + eventType, e
            );
        }
    }
    
    @Override
    public SerializationResult serializeWithCompression(
            DomainEvent event, 
            int compressionThreshold) {
        
        String json = serialize(event);
        
        // Comprimir se exceder threshold
        if (json.length() > compressionThreshold) {
            String compressed = compressData(json);
            return new SerializationResult(
                compressed, 
                true, 
                json.length(), 
                compressed.length()
            );
        }
        
        return new SerializationResult(json, false, json.length(), json.length());
    }
    
    private String compressData(String data) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(data.getBytes(StandardCharsets.UTF_8));
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new SerializationException("Erro na compressão", e);
        }
    }
}
```

---

## 🔒 **CONTROLE DE CONCORRÊNCIA**

### **⚡ Optimistic Locking com Versioning**

```java
// Localização: eventstore/impl/PostgreSQLEventStore.java
@Override
public void saveEvents(String aggregateId, 
                      List<DomainEvent> events, 
                      long expectedVersion) {
    
    // 1. Verificar versão atual para controle de concorrência
    long currentVersion = getCurrentVersion(aggregateId);
    
    if (currentVersion != expectedVersion) {
        throw new ConcurrencyException(
            aggregateId, 
            expectedVersion, 
            currentVersion,
            String.format(
                "Conflito de concorrência para aggregate %s. " +
                "Versão esperada: %d, versão atual: %d",
                aggregateId, expectedVersion, currentVersion
            )
        );
    }
    
    // 2. Salvar eventos com versão incremental
    long nextVersion = currentVersion;
    
    for (DomainEvent event : events) {
        nextVersion++;
        event.setVersion(nextVersion);
        
        EventStoreEntry entry = convertToEntry(event);
        
        try {
            repository.save(entry);
        } catch (DataIntegrityViolationException e) {
            // Violação de constraint unique (aggregate_id, version)
            throw new ConcurrencyException(
                aggregateId, 
                expectedVersion, 
                getCurrentVersion(aggregateId),
                "Conflito detectado durante persistência"
            );
        }
    }
}
```

### **🔄 Exemplo de Conflito de Concorrência**

```java
// Cenário: Dois usuários modificam o mesmo sinistro simultaneamente

// Thread 1: Operador A atribui sinistro
SinistroAggregate sinistro1 = repository.findById("sinistro-123");
// sinistro1.version = 5

sinistro1.atribuirOperador("Operador A");
// Gera evento: SinistroAtribuidoEvent (version 6)

// Thread 2: Operador B também atribui o mesmo sinistro
SinistroAggregate sinistro2 = repository.findById("sinistro-123");
// sinistro2.version = 5 (mesma versão!)

sinistro2.atribuirOperador("Operador B");
// Gera evento: SinistroAtribuidoEvent (version 6)

// Salvamento:
repository.save(sinistro1); // ✅ Sucesso - version 5 → 6
repository.save(sinistro2); // ❌ ConcurrencyException!
                           // Expected: 5, Current: 6
```

### **🛡️ Tratamento de Conflitos**

```java
// Estratégia de retry com backoff exponencial
@Service
public class SinistroCommandHandler {
    
    private static final int MAX_RETRIES = 3;
    
    public CommandResult handle(AtribuirSinistroCommand command) {
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // Tentar executar comando
                SinistroAggregate sinistro = repository.findById(command.getSinistroId());
                sinistro.atribuirOperador(command.getOperadorId());
                repository.save(sinistro);
                
                return CommandResult.success();
                
            } catch (ConcurrencyException e) {
                if (attempt == MAX_RETRIES) {
                    return CommandResult.failure(
                        "Conflito de concorrência após " + MAX_RETRIES + " tentativas"
                    );
                }
                
                // Backoff exponencial: 100ms, 200ms, 400ms
                try {
                    Thread.sleep(100 * (1L << (attempt - 1)));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        return CommandResult.failure("Operação interrompida");
    }
}
```

---

## 📈 **PARTICIONAMENTO E PERFORMANCE**

### **🗂️ Estratégia de Particionamento**

```sql
-- Localização: db/migration/V4__Implement_Event_Partitioning.sql

-- Particionamento por data para melhor performance
CREATE TABLE eventstore.events_2024_01 PARTITION OF eventstore.events
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE eventstore.events_2024_02 PARTITION OF eventstore.events
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- Índices específicos por partição
CREATE INDEX idx_events_2024_01_aggregate_id 
    ON eventstore.events_2024_01 (aggregate_id);
```

### **⚡ Otimizações de Performance**

```java
// Localização: eventstore/partition/PartitionManager.java
@Component
public class PartitionManager {
    
    /**
     * Cria partições mensais automaticamente
     */
    @Scheduled(cron = "0 0 1 * * ?") // Todo dia 1 às 00:00
    public void createMonthlyPartitions() {
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        String partitionName = "events_" + nextMonth.format(
            DateTimeFormatter.ofPattern("yyyy_MM")
        );
        
        if (!partitionExists(partitionName)) {
            createMonthlyPartition("events", nextMonth);
            log.info("Partição criada: {}", partitionName);
        }
    }
    
    /**
     * Estatísticas de partições para monitoramento
     */
    public List<PartitionStatistics> getPartitionStatistics() {
        String sql = """
            SELECT 
                schemaname,
                tablename,
                n_tup_ins as total_events,
                pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
            FROM pg_stat_user_tables 
            WHERE schemaname = 'eventstore' 
            AND tablename LIKE 'events_%'
            ORDER BY tablename;
        """;
        
        return jdbcTemplate.query(sql, this::mapPartitionStatistics);
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Implementar e testar Event Store

#### **Passo 1: Explorar Serialização**
```java
@Test
public void testarSerializacaoEvento() {
    // 1. Criar evento
    SinistroEvent evento = SinistroEvent.sinistroCriado(
        "sinistro-456",
        "SIN-2024-002",
        "Teste serialização",
        3000.0
    );
    
    // 2. Serializar
    EventSerializer serializer = new JsonEventSerializer(objectMapper);
    String json = serializer.serialize(evento);
    
    System.out.println("JSON: " + json);
    
    // 3. Deserializar
    DomainEvent eventoDeserializado = serializer.deserialize(
        json, 
        "SinistroEvent"
    );
    
    // 4. Verificar integridade
    assertThat(eventoDeserializado.getAggregateId())
        .isEqualTo(evento.getAggregateId());
}
```

#### **Passo 2: Testar Controle de Concorrência**
```java
@Test
public void testarConcorrencia() {
    String aggregateId = "sinistro-concorrencia-test";
    
    // Simular dois saves simultâneos
    List<DomainEvent> eventos1 = Arrays.asList(
        SinistroEvent.sinistroCriado(aggregateId, "SIN-001", "Teste 1", 1000.0)
    );
    
    List<DomainEvent> eventos2 = Arrays.asList(
        SinistroEvent.sinistroAtualizado(aggregateId, "SIN-001", "ABERTO", "Teste 2", 2000.0)
    );
    
    // Primeiro save deve funcionar
    eventStore.saveEvents(aggregateId, eventos1, 0);
    
    // Segundo save com versão desatualizada deve falhar
    assertThatThrownBy(() -> {
        eventStore.saveEvents(aggregateId, eventos2, 0);
    }).isInstanceOf(ConcurrencyException.class);
}
```

#### **Passo 3: Verificar Particionamento**
```bash
# Conectar no banco
docker exec -it postgres-write psql -U postgres -d sinistros_eventstore

# Ver partições existentes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE schemaname = 'eventstore' 
ORDER BY tablename;

# Ver distribuição de eventos por partição
SELECT 
    tableoid::regclass as partition_name,
    COUNT(*) as event_count
FROM eventstore.events 
GROUP BY tableoid::regclass;
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Explicar** como eventos são persistidos no PostgreSQL
2. **Descrever** o processo de serialização/deserialização
3. **Entender** como funciona o controle de concorrência
4. **Identificar** estratégias de particionamento
5. **Implementar** tratamento de conflitos de versão

### **❓ Perguntas para Reflexão:**

1. Por que usar JSONB ao invés de colunas relacionais?
2. Como garantir que eventos nunca sejam perdidos?
3. Qual o impacto de compressão na performance?
4. Como lidar com evolução de schema de eventos?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 3**, vamos explorar:
- Snapshots para otimização de performance
- Estratégias de replay de eventos
- Arquivamento de eventos antigos
- Monitoramento e métricas do Event Store

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 50 minutos  
**📋 Pré-requisitos:** Event Sourcing Parte 1