# 📡 EVENT BUS - PARTE 5: TESTES E TROUBLESHOOTING
## Roteiro Técnico para Analistas Java Junior

### 🎯 **OBJETIVO DESTA PARTE**
Dominar estratégias de teste, debugging e troubleshooting do Event Bus, garantindo qualidade e confiabilidade na implementação.

---

## 🧪 **ESTRATÉGIAS DE TESTE**

### **📋 Tipos de Teste Implementados**

```
Testes do Event Bus
├── Testes Unitários (Handlers isolados)
├── Testes de Integração (Event Bus completo)
├── Testes de Performance (Carga e stress)
└── Testes de Resiliência (Falhas e recovery)
```

### **🔬 Testes Unitários de Handlers**

**Exemplo de Teste de Handler:**
```java
@ExtendWith(MockitoExtension.class)
class SinistroEventHandlerTest {
    
    @Mock
    private SinistroService sinistroService;
    
    @InjectMocks
    private SinistroEventHandler handler;
    
    @Test
    void deveProcessarEventoSinistroCriado() {
        // Given
        SinistroEvent evento = SinistroEvent.sinistroCriado(
            "sinistro-123", "SIN-2024-001", "Colisão", 5000.0);
        
        // When
        assertDoesNotThrow(() -> handler.handle(evento));
        
        // Then
        verify(sinistroService).processarSinistroCriado(
            eq("sinistro-123"), eq("SIN-2024-001"), any());
    }
    
    @Test
    void deveSerIdempotente() {
        // Given
        SinistroEvent evento = SinistroEvent.sinistroCriado(
            "sinistro-123", "SIN-2024-001", "Colisão", 5000.0);
        
        // When - processar duas vezes
        handler.handle(evento);
        handler.handle(evento);
        
        // Then - deve processar apenas uma vez
        verify(sinistroService, times(1))
            .processarSinistroCriado(any(), any(), any());
    }
}
```

### **🔗 Testes de Integração**

**Teste Completo do Event Bus:**
```java
@SpringBootTest
@TestPropertySource(properties = {
    "eventbus.type=simple",
    "eventbus.enabled=true"
})
class EventBusIntegrationTest {
    
    @Autowired
    private EventBus eventBus;
    
    @MockBean
    private SinistroService sinistroService;
    
    @Test
    void devePublicarEProcessarEvento() throws InterruptedException {
        // Given
        SinistroEvent evento = SinistroEvent.sinistroCriado(
            "sinistro-123", "SIN-2024-001", "Colisão", 5000.0);
        
        // When
        eventBus.publish(evento);
        
        // Then - aguardar processamento assíncrono
        Thread.sleep(1000);
        
        verify(sinistroService).processarSinistroCriado(
            eq("sinistro-123"), eq("SIN-2024-001"), any());
    }
    
    @Test
    void deveColetarEstatisticas() {
        // Given
        SinistroEvent evento = SinistroEvent.sinistroCriado(
            "sinistro-123", "SIN-2024-001", "Colisão", 5000.0);
        
        // When
        eventBus.publish(evento);
        
        // Then
        EventBusStatistics stats = eventBus.getStatistics();
        assertThat(stats.getEventsPublished()).isGreaterThan(0);
    }
}
```

---

## 🧪 **TESTES COM TESTCONTAINERS**

### **🐳 Configuração Kafka para Testes**

**Setup com Testcontainers:**
```java
@SpringBootTest
@Testcontainers
class KafkaEventBusIntegrationTest {
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:latest"))
        .withEmbeddedZookeeper();
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("eventbus.kafka.bootstrap-servers", 
            kafka::getBootstrapServers);
    }
    
    @Autowired
    private EventBus eventBus;
    
    @Test
    void devePublicarEventoNoKafka() {
        // Given
        SinistroEvent evento = SinistroEvent.sinistroCriado(
            "sinistro-123", "SIN-2024-001", "Colisão", 5000.0);
        
        // When & Then
        assertDoesNotThrow(() -> eventBus.publish(evento));
        
        // Verificar se foi publicado no Kafka
        EventBusStatistics stats = eventBus.getStatistics();
        assertThat(stats.getEventsPublished()).isEqualTo(1);
    }
}
```

### **📊 Testes de Performance**

**Teste de Carga:**
```java
@Test
@Timeout(30)
void deveProcessarAltaVolumeDeEventos() {
    // Given
    int numeroEventos = 1000;
    List<SinistroEvent> eventos = IntStream.range(0, numeroEventos)
        .mapToObj(i -> SinistroEvent.sinistroCriado(
            "sinistro-" + i, "SIN-2024-" + i, "Teste", 1000.0))
        .collect(Collectors.toList());
    
    // When
    long inicio = System.currentTimeMillis();
    
    eventos.forEach(eventBus::publish);
    
    // Then
    long duracao = System.currentTimeMillis() - inicio;
    double throughput = (double) numeroEventos / (duracao / 1000.0);
    
    assertThat(throughput).isGreaterThan(100); // > 100 eventos/segundo
    
    log.info("Throughput: {} eventos/segundo", throughput);
}
```

---

## 🔍 **DEBUGGING E TROUBLESHOOTING**

### **📝 Logging Estruturado**

**Configuração de Logs:**
```yaml
logging:
  level:
    com.seguradora.hibrida.eventbus: DEBUG
    org.apache.kafka: WARN
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"
```

**Exemplo de Log Estruturado:**
```java
@Component
public class SinistroEventHandler implements EventHandler<SinistroEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(SinistroEventHandler.class);
    
    @Override
    public void handle(SinistroEvent event) {
        String correlationId = event.getCorrelationId().toString();
        
        try (MDCCloseable mdc = MDCCloseable.put("correlationId", correlationId)) {
            log.info("Processing sinistro event: type={}, aggregateId={}", 
                event.getEventType(), event.getAggregateId());
            
            // Processamento do evento
            processEvent(event);
            
            log.info("Successfully processed sinistro event: type={}", 
                event.getEventType());
                
        } catch (Exception e) {
            log.error("Failed to process sinistro event: type={}, error={}", 
                event.getEventType(), e.getMessage(), e);
            throw new EventHandlingException(event, e.getMessage(), e);
        }
    }
}
```

### **🔧 Ferramentas de Debug**

**1. Event Bus Controller para Debug:**
```java
@RestController
@RequestMapping("/debug/eventbus")
public class EventBusDebugController {
    
    private final EventBus eventBus;
    
    @GetMapping("/statistics")
    public ResponseEntity<EventBusStatistics> getStatistics() {
        return ResponseEntity.ok(eventBus.getStatistics());
    }
    
    @GetMapping("/handlers")
    public ResponseEntity<Map<String, Object>> getHandlers() {
        // Retornar informações dos handlers registrados
        return ResponseEntity.ok(getHandlerInfo());
    }
    
    @PostMapping("/test-event")
    public ResponseEntity<String> publishTestEvent(@RequestBody Map<String, Object> eventData) {
        try {
            TestEvent testEvent = new TestEvent(eventData);
            eventBus.publish(testEvent);
            return ResponseEntity.ok("Event published successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed: " + e.getMessage());
        }
    }
}
```

**2. Health Check Detalhado:**
```java
@Component
public class EventBusHealthIndicator implements HealthIndicator {
    
    private final EventBus eventBus;
    
    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();
            
            // Verificar se Event Bus está operacional
            boolean isHealthy = eventBus.isHealthy();
            details.put("operational", isHealthy);
            
            // Adicionar estatísticas
            EventBusStatistics stats = eventBus.getStatistics();
            details.put("eventsPublished", stats.getEventsPublished());
            details.put("eventsProcessed", stats.getEventsProcessed());
            details.put("errorRate", stats.getErrorRate());
            details.put("throughput", stats.getThroughput());
            
            // Verificar handlers registrados
            details.put("registeredHandlers", getRegisteredHandlersCount());
            
            return isHealthy ? 
                Health.up().withDetails(details).build() :
                Health.down().withDetails(details).build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

---

## 🚨 **PROBLEMAS COMUNS E SOLUÇÕES**

### **❌ Problema 1: Eventos Não Processados**

**Sintomas:**
- Eventos publicados mas handlers não executam
- Estatísticas mostram 0 eventos processados

**Diagnóstico:**
```java
// Verificar se handlers estão registrados
@GetMapping("/debug/handlers")
public Map<String, Object> debugHandlers() {
    Map<String, Object> info = new HashMap<>();
    
    // Verificar registry
    EventHandlerRegistry registry = getEventHandlerRegistry();
    info.put("registeredEventTypes", registry.getRegisteredEventTypes());
    info.put("totalHandlers", registry.getTotalHandlers());
    
    return info;
}
```

**Soluções:**
1. Verificar se handlers estão anotados com `@Component`
2. Confirmar se pacotes estão sendo escaneados
3. Verificar logs de inicialização

### **❌ Problema 2: Performance Degradada**

**Sintomas:**
- Processamento lento de eventos
- Timeout em handlers
- Alta utilização de CPU/memória

**Diagnóstico:**
```java
@GetMapping("/debug/performance")
public Map<String, Object> debugPerformance() {
    Map<String, Object> metrics = new HashMap<>();
    
    EventBusStatistics stats = eventBus.getStatistics();
    metrics.put("averageProcessingTime", stats.getAverageProcessingTime());
    metrics.put("maxProcessingTime", stats.getMaxProcessingTime());
    metrics.put("activeHandlers", stats.getActiveHandlers());
    metrics.put("maxConcurrentHandlers", stats.getMaxConcurrentHandlers());
    
    return metrics;
}
```

**Soluções:**
1. Otimizar handlers lentos
2. Ajustar configuração de thread pool
3. Implementar processamento em lote

### **❌ Problema 3: Falhas de Conectividade Kafka**

**Sintomas:**
- Exceções de conexão
- Eventos não persistidos
- Health check falhando

**Diagnóstico:**
```java
public boolean testKafkaConnectivity() {
    try {
        // Testar conectividade básica
        kafkaProducer.partitionsFor("test-topic");
        return true;
    } catch (Exception e) {
        log.error("Kafka connectivity test failed", e);
        return false;
    }
}
```

**Soluções:**
1. Verificar configuração de bootstrap servers
2. Confirmar conectividade de rede
3. Validar credenciais de autenticação

---

## 📊 **MONITORAMENTO EM PRODUÇÃO**

### **📈 Métricas Essenciais**

**Dashboard de Monitoramento:**
```yaml
# Prometheus metrics
eventbus_events_published_total
eventbus_events_processed_total
eventbus_events_failed_total
eventbus_processing_time_seconds
eventbus_error_rate
eventbus_throughput_events_per_second
```

**Alertas Recomendados:**
```yaml
# Alerta para alta taxa de erro
- alert: EventBusHighErrorRate
  expr: eventbus_error_rate > 0.05
  for: 5m
  
# Alerta para baixo throughput
- alert: EventBusLowThroughput
  expr: eventbus_throughput_events_per_second < 10
  for: 10m
```

### **🔍 Observabilidade**

**Tracing Distribuído:**
```java
@Component
public class TracedSinistroEventHandler implements EventHandler<SinistroEvent> {
    
    @Override
    @Traced(operationName = "handle-sinistro-event")
    public void handle(SinistroEvent event) {
        Span span = tracer.activeSpan();
        if (span != null) {
            span.setTag("event.type", event.getEventType());
            span.setTag("event.aggregateId", event.getAggregateId());
        }
        
        // Processamento do evento
        processEvent(event);
    }
}
```

---

## 🎯 **CHECKLIST DE QUALIDADE**

### **✅ Antes de Deploy**

- [ ] Todos os testes unitários passando
- [ ] Testes de integração executados
- [ ] Performance testada com carga esperada
- [ ] Logs estruturados implementados
- [ ] Métricas configuradas
- [ ] Health checks funcionando
- [ ] Documentação atualizada

### **✅ Pós Deploy**

- [ ] Monitoramento ativo
- [ ] Alertas configurados
- [ ] Dashboards funcionando
- [ ] Logs sendo coletados
- [ ] Métricas sendo reportadas

---

## 🎓 **EXERCÍCIO FINAL**

### **📝 Implementação Completa**

Implemente um handler completo que:

1. **Processe eventos de sinistro**
2. **Inclua logging estruturado**
3. **Tenha testes unitários e de integração**
4. **Implemente métricas customizadas**
5. **Trate erros adequadamente**

**Template Base:**
```java
@Component
@Slf4j
public class CompleteSinistroEventHandler implements EventHandler<SinistroEvent> {
    
    private final MeterRegistry meterRegistry;
    private final Counter processedCounter;
    private final Timer processingTimer;
    
    // Sua implementação completa aqui
}
```

---

## 📚 **RECURSOS PARA TROUBLESHOOTING**

### **🔗 Links Úteis**
- **Kafka Tools**: Kafka Manager, Kafdrop
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Tracing**: Jaeger, Zipkin

### **📖 Documentação de Referência**
- Spring Boot Actuator
- Apache Kafka Documentation
- Micrometer Metrics
- SLF4J Logging

---

## 🎯 **RESUMO DO MÓDULO EVENT BUS**

Após completar as 5 partes do Event Bus, você deve ser capaz de:

✅ **Compreender** a arquitetura e componentes do Event Bus  
✅ **Implementar** handlers seguindo os padrões estabelecidos  
✅ **Configurar** Event Bus para diferentes ambientes  
✅ **Integrar** com Kafka para processamento distribuído  
✅ **Testar** e debuggar implementações  
✅ **Monitorar** e troubleshootar em produção  

---

**📍 Próximo Módulo**: [Projection Handlers - Parte 1](./07-projections-parte-1.md)

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Testes e troubleshooting  
**⏱️ Tempo estimado:** 60 minutos  
**🔧 Hands-on:** Implementação de testes completos