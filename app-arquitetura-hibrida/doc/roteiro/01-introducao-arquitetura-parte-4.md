# 📖 INTRODUÇÃO À ARQUITETURA - PARTE 4
## Fluxos de Dados e Comunicação entre Componentes

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender os fluxos de dados na arquitetura
- Entender a comunicação entre Command Side e Query Side
- Conhecer os padrões de integração implementados
- Visualizar o ciclo de vida completo de uma operação

---

## 🔄 **FLUXOS PRINCIPAIS DA ARQUITETURA**

### **📊 Visão Geral dos Fluxos**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FLUXOS DE DADOS                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐  │
│  │   Cliente   │    │  Command    │    │   Event     │    │   Query     │  │
│  │   (API)     │    │    Side     │    │    Bus      │    │    Side     │  │
│  └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘  │
│         │                   │                   │                   │       │
│         │ ①Command          │                   │                   │       │
│         ├──────────────────►│                   │                   │       │
│         │                   │ ②Events           │                   │       │
│         │                   ├──────────────────►│                   │       │
│         │                   │                   │ ③Events           │       │
│         │                   │                   ├──────────────────►│       │
│         │ ④Response         │                   │                   │       │
│         │◄──────────────────│                   │                   │       │
│         │                   │                   │                   │       │
│         │ ⑤Query            │                   │                   │       │
│         ├───────────────────┼───────────────────┼──────────────────►│       │
│         │                   │                   │                   │       │
│         │ ⑥Data             │                   │                   │       │
│         │◄──────────────────┼───────────────────┼───────────────────│       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📝 **FLUXO 1: PROCESSAMENTO DE COMANDOS**

### **🎯 Cenário: Criar um Sinistro**

#### **Passo a Passo Detalhado:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        FLUXO DE COMANDO                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1️⃣ HTTP Request                                                            │
│     POST /api/v1/commands/sinistros                                         │
│     {                                                                       │
│       "cpfSegurado": "12345678901",                                         │
│       "placaVeiculo": "ABC1234",                                            │
│       "descricaoOcorrencia": "Colisão traseira"                             │
│     }                                                                       │
│                                                                             │
│  2️⃣ Command Creation                                                         │
│     CriarSinistroCommand command = new CriarSinistroCommand(...)            │
│                                                                             │
│  3️⃣ Command Bus Routing                                                     │
│     commandBus.send(command) → CriarSinistroCommandHandler                  │
│                                                                             │
│  4️⃣ Business Logic                                                          │
│     - Validar dados do segurado                                             │
│     - Verificar vigência da apólice                                         │
│     - Aplicar regras de negócio                                             │
│     - Gerar número de protocolo                                             │
│                                                                             │
│  5️⃣ Aggregate Creation                                                      │
│     SinistroAggregate sinistro = new SinistroAggregate()                    │
│     sinistro.criar(cpf, placa, descricao)                                   │
│                                                                             │
│  6️⃣ Event Generation                                                        │
│     SinistroCriadoEvent event = new SinistroCriadoEvent(...)                │
│                                                                             │
│  7️⃣ Event Store Persistence                                                 │
│     eventStore.saveEvents(aggregateId, [event], expectedVersion)           │
│                                                                             │
│  8️⃣ Event Publishing                                                        │
│     eventBus.publish(event)                                                 │
│                                                                             │
│  9️⃣ Response                                                                │
│     HTTP 202 Accepted                                                       │
│     {                                                                       │
│       "commandId": "uuid",                                                  │
│       "status": "ACCEPTED",                                                 │
│       "aggregateId": "sinistro-uuid"                                        │
│     }                                                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### **Implementação no Código:**

```java
// 1. REST Controller
@RestController
@RequestMapping("/api/v1/commands/sinistros")
public class SinistroCommandController {
    
    @PostMapping
    public ResponseEntity<CommandResult> criarSinistro(@RequestBody CriarSinistroRequest request) {
        // Converter request para command
        CriarSinistroCommand command = CriarSinistroCommand.builder()
            .cpfSegurado(request.getCpfSegurado())
            .placaVeiculo(request.getPlacaVeiculo())
            .descricaoOcorrencia(request.getDescricaoOcorrencia())
            .build();
        
        // Enviar via Command Bus
        CommandResult result = commandBus.send(command);
        
        return ResponseEntity.accepted().body(result);
    }
}

// 2. Command Handler
@Component
public class CriarSinistroCommandHandler implements CommandHandler<CriarSinistroCommand> {
    
    private final AggregateRepository<SinistroAggregate> repository;
    
    @Override
    public CommandResult handle(CriarSinistroCommand command) {
        try {
            // Criar novo aggregate
            SinistroAggregate sinistro = new SinistroAggregate();
            
            // Aplicar comando
            sinistro.criar(
                command.getCpfSegurado(),
                command.getPlacaVeiculo(), 
                command.getDescricaoOcorrencia()
            );
            
            // Salvar (persiste eventos automaticamente)
            repository.save(sinistro);
            
            return CommandResult.success()
                .withData("aggregateId", sinistro.getId())
                .withData("protocolo", sinistro.getProtocolo());
                
        } catch (Exception e) {
            return CommandResult.failure(e);
        }
    }
}
```

---

## 📡 **FLUXO 2: COMUNICAÇÃO VIA EVENT BUS**

### **🔄 Transporte de Eventos**

#### **Implementações Disponíveis:**

```java
// Localização: com.seguradora.hibrida.eventbus.impl

// 1. SimpleEventBus (Desenvolvimento/Testes)
public class SimpleEventBus implements EventBus {
    // Processamento síncrono em memória
    // Ideal para: desenvolvimento, testes unitários
}

// 2. KafkaEventBus (Produção)
public class KafkaEventBus implements EventBus {
    // Processamento assíncrono via Kafka
    // Ideal para: produção, alta disponibilidade
}
```

#### **Configuração Automática:**

```yaml
# application.yml
app:
  event-bus:
    type: simple  # ou kafka
    
    # Configurações Simple
    simple:
      thread-pool:
        core-size: 5
        max-size: 20
    
    # Configurações Kafka  
    kafka:
      bootstrap-servers: localhost:9092
      default-topic: domain-events
```

#### **Fluxo de Publicação:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       EVENT BUS FLOW                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1️⃣ Event Generation (Command Side)                                         │
│     SinistroCriadoEvent event = new SinistroCriadoEvent(...)                │
│                                                                             │
│  2️⃣ Event Store Persistence                                                 │
│     eventStore.saveEvents(aggregateId, [event], version)                   │
│                                                                             │
│  3️⃣ Event Bus Publishing                                                    │
│     eventBus.publish(event)                                                 │
│                                                                             │
│  4️⃣ Handler Registration Lookup                                             │
│     List<EventHandler> handlers = registry.getHandlers(event.getClass())   │
│                                                                             │
│  5️⃣ Parallel Processing                                                     │
│     handlers.parallelStream().forEach(handler -> {                         │
│         try {                                                               │
│             handler.handle(event)                                           │
│         } catch (Exception e) {                                             │
│             // Error handling & retry logic                                 │
│         }                                                                   │
│     })                                                                      │
│                                                                             │
│  6️⃣ Projection Updates (Query Side)                                         │
│     - SinistroProjectionHandler.handle(event)                              │
│     - NotificationHandler.handle(event)                                     │
│     - AuditHandler.handle(event)                                            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔍 **FLUXO 3: ATUALIZAÇÃO DE PROJEÇÕES**

### **📊 Processamento de Eventos no Query Side**

#### **Projection Handler Implementation:**

```java
// Localização: com.seguradora.hibrida.projection.example
@Component
public class SinistroProjectionHandler extends AbstractProjectionHandler<SinistroEvent> {
    
    private final SinistroQueryRepository repository;
    
    @Override
    public void doHandle(SinistroEvent event) {
        switch (event.getEventType()) {
            case "SinistroCriado":
                handleSinistroCriado(event);
                break;
            case "SinistroAtualizado":
                handleSinistroAtualizado(event);
                break;
            // ... outros eventos
        }
    }
    
    private void handleSinistroCriado(SinistroEvent event) {
        // Criar nova entrada na projeção
        SinistroQueryModel model = new SinistroQueryModel();
        model.setId(UUID.fromString(event.getAggregateId()));
        model.setProtocolo(event.getProtocolo());
        model.setCpfSegurado(event.getCpfSegurado());
        model.setStatus("ABERTO");
        model.setDataAbertura(event.getTimestamp());
        
        repository.save(model);
    }
}
```

#### **Tracking de Posição:**

```java
// Localização: com.seguradora.hibrida.projection.tracking
@Entity
@Table(name = "projection_tracking")
public class ProjectionTracker {
    
    @Id
    private String projectionName;           // Nome da projeção
    
    private Long lastProcessedEventId;       // Último evento processado
    private Long eventsProcessed;            // Total de eventos processados
    private Long eventsFailed;               // Total de falhas
    private ProjectionStatus status;         // ACTIVE, PAUSED, ERROR
    private Instant lastProcessedAt;         // Timestamp do último processamento
    
    // Métodos para controle de posição
    public void updatePosition(Long eventId) {
        this.lastProcessedEventId = eventId;
        this.eventsProcessed++;
        this.lastProcessedAt = Instant.now();
    }
    
    public void recordFailure(String errorMessage) {
        this.eventsFailed++;
        this.status = ProjectionStatus.ERROR;
        // ... logging e notificação
    }
}
```

#### **Fluxo de Atualização:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      PROJECTION UPDATE FLOW                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1️⃣ Event Reception                                                         │
│     SinistroCriadoEvent received by SinistroProjectionHandler              │
│                                                                             │
│  2️⃣ Position Check                                                          │
│     tracker = getTracker("SinistroProjectionHandler")                      │
│     if (event.eventId <= tracker.lastProcessedEventId) return; // Skip     │
│                                                                             │
│  3️⃣ Business Logic                                                          │
│     - Extract data from event                                               │
│     - Transform to query model format                                       │
│     - Apply business rules for read side                                    │
│                                                                             │
│  4️⃣ Database Update                                                         │
│     - Insert/Update in projections database                                 │
│     - Handle denormalization                                                │
│     - Update indexes and views                                              │
│                                                                             │
│  5️⃣ Position Update                                                         │
│     tracker.updatePosition(event.eventId)                                  │
│     trackerRepository.save(tracker)                                         │
│                                                                             │
│  6️⃣ Cache Invalidation                                                      │
│     - Clear related cache entries                                           │
│     - Update cache with new data                                            │
│                                                                             │
│  7️⃣ Metrics & Monitoring                                                    │
│     - Update processing metrics                                             │
│     - Check lag thresholds                                                  │
│     - Trigger alerts if needed                                              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔍 **FLUXO 4: CONSULTAS OTIMIZADAS**

### **📊 Query Processing**

#### **Query Service Implementation:**

```java
// Localização: com.seguradora.hibrida.query.service
@Service
@Transactional(readOnly = true)
public class SinistroQueryServiceImpl implements SinistroQueryService {
    
    private final SinistroQueryRepository repository;
    
    @Override
    @Cacheable(value = "sinistros-by-id", key = "#id")
    public Optional<SinistroDetailView> buscarPorId(UUID id) {
        return repository.findById(id)
            .map(this::toDetailView);
    }
    
    @Override
    @Cacheable(value = "sinistros-list", key = "#filter.hashCode() + '-' + #pageable.hashCode()")
    public Page<SinistroListView> listar(SinistroFilter filter, Pageable pageable) {
        Specification<SinistroQueryModel> spec = buildSpecification(filter);
        
        return repository.findAll(spec, pageable)
            .map(this::toListView);
    }
}
```

#### **Repository com Consultas Otimizadas:**

```java
// Localização: com.seguradora.hibrida.query.repository
@Repository
public interface SinistroQueryRepository extends JpaRepository<SinistroQueryModel, UUID>, 
                                               JpaSpecificationExecutor<SinistroQueryModel> {
    
    // Consultas básicas
    Optional<SinistroQueryModel> findByProtocolo(String protocolo);
    List<SinistroQueryModel> findByCpfSeguradoOrderByDataAberturaDesc(String cpfSegurado);
    
    // Full-text search
    @Query(value = """
        SELECT * FROM projections.sinistro_view s 
        WHERE to_tsvector('portuguese', 
            COALESCE(s.protocolo, '') || ' ' || 
            COALESCE(s.nome_segurado, '') || ' ' || 
            COALESCE(s.descricao, '')
        ) @@ plainto_tsquery('portuguese', :termo)
        ORDER BY s.data_abertura DESC
        """, nativeQuery = true)
    List<SinistroQueryModel> findByFullTextSearch(@Param("termo") String termo);
    
    // Consultas de dashboard
    @Query(value = """
        SELECT 
            COUNT(*) as total,
            COUNT(CASE WHEN status IN ('ABERTO', 'EM_ANALISE') THEN 1 END) as abertos,
            COUNT(CASE WHEN status IN ('FECHADO', 'CANCELADO') THEN 1 END) as fechados,
            AVG(CASE WHEN valor_estimado IS NOT NULL THEN valor_estimado ELSE 0 END) as valor_medio
        FROM projections.sinistro_view
        WHERE data_abertura >= :desde
        """, nativeQuery = true)
    Object[] getResumoExecutivo(@Param("desde") Instant desde);
}
```

#### **Fluxo de Consulta:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         QUERY FLOW                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1️⃣ HTTP Request                                                            │
│     GET /api/v1/query/sinistros?cpf=12345678901&status=ABERTO              │
│                                                                             │
│  2️⃣ Controller Processing                                                   │
│     SinistroQueryController.listar(filter, pageable)                       │
│                                                                             │
│  3️⃣ Cache Check                                                             │
│     @Cacheable - Check Redis for cached result                             │
│     if (cached) return cached;                                              │
│                                                                             │
│  4️⃣ Service Layer                                                           │
│     SinistroQueryService.listar(filter, pageable)                          │
│                                                                             │
│  5️⃣ Specification Building                                                  │
│     Specification<SinistroQueryModel> spec = buildSpecification(filter)    │
│                                                                             │
│  6️⃣ Database Query                                                          │
│     - Execute optimized SQL on read database                                │
│     - Use indexes and materialized views                                    │
│     - Apply pagination and sorting                                          │
│                                                                             │
│  7️⃣ Result Transformation                                                   │
│     - Convert entities to DTOs                                              │
│     - Apply business formatting                                             │
│     - Prepare response structure                                            │
│                                                                             │
│  8️⃣ Cache Storage                                                           │
│     - Store result in Redis cache                                           │
│     - Set appropriate TTL                                                   │
│                                                                             │
│  9️⃣ HTTP Response                                                           │
│     HTTP 200 OK                                                             │
│     {                                                                       │
│       "content": [...],                                                     │
│       "pageable": {...},                                                    │
│       "totalElements": 150                                                  │
│     }                                                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 **FLUXO COMPLETO: CICLO DE VIDA**

### **📊 Exemplo Completo: Criar e Consultar Sinistro**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    COMPLETE LIFECYCLE FLOW                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ⏰ T+0ms    │ POST /commands/sinistros                                      │
│             │ ├─ Command validation                                         │
│             │ ├─ Business rules application                                 │
│             │ ├─ Event generation: SinistroCriadoEvent                      │
│             │ ├─ Event Store persistence                                    │
│             │ └─ HTTP 202 Accepted                                          │
│                                                                             │
│  ⏰ T+10ms   │ Event Bus processing                                          │
│             │ ├─ Event published to handlers                                │
│             │ ├─ SinistroProjectionHandler triggered                       │
│             │ ├─ NotificationHandler triggered                              │
│             │ └─ AuditHandler triggered                                     │
│                                                                             │
│  ⏰ T+50ms   │ Projection updates                                            │
│             │ ├─ Insert into projections.sinistro_view                     │
│             │ ├─ Update projection tracking                                 │
│             │ ├─ Clear related caches                                       │
│             │ └─ Update metrics                                             │
│                                                                             │
│  ⏰ T+100ms  │ GET /query/sinistros/{id}                                     │
│             │ ├─ Cache miss (new data)                                      │
│             │ ├─ Database query on read DB                                  │
│             │ ├─ Result transformation                                      │
│             │ ├─ Cache storage                                              │
│             │ └─ HTTP 200 OK with data                                      │
│                                                                             │
│  ⏰ T+200ms  │ GET /query/sinistros/{id} (second call)                       │
│             │ ├─ Cache hit                                                  │
│             │ └─ HTTP 200 OK (fast response)                                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 **MONITORAMENTO DE FLUXOS**

### **🔍 Observabilidade Implementada**

#### **Métricas Disponíveis:**
```bash
# Command Side
curl http://localhost:8083/api/v1/actuator/commandbus/statistics

# Event Bus
curl http://localhost:8083/api/v1/actuator/eventbus/statistics

# Projections
curl http://localhost:8083/api/v1/actuator/projections

# CQRS Lag
curl http://localhost:8083/api/v1/actuator/cqrs
```

#### **Health Checks:**
```bash
# Verificar saúde de cada componente
curl http://localhost:8083/api/v1/actuator/health

# Resposta esperada:
{
  "status": "UP",
  "components": {
    "commandSide": {"status": "UP", "commandsProcessed": 1250},
    "querySide": {"status": "UP", "projectionsActive": 3},
    "eventBus": {"status": "UP", "eventsPublished": 1250},
    "lag": {"status": "UP", "maxLag": 15, "avgLag": 5}
  }
}
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Event-Driven Architecture](https://martinfowler.com/articles/201701-event-driven.html)
- [CQRS Journey](https://docs.microsoft.com/en-us/previous-versions/msp-n-p/jj554200(v=pandp.10))
- [Microservices Communication](https://microservices.io/patterns/communication-style/)

### **📖 Próxima Parte:**
- **Parte 5**: Exercícios Práticos e Checkpoint de Aprendizado

---

**📝 Parte 4 de 5 - Fluxos de Dados**  
**⏱️ Tempo estimado**: 55 minutos  
**🎯 Próximo**: [Parte 5 - Exercícios Práticos](./01-introducao-arquitetura-parte-5.md)