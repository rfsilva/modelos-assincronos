# 📡 EVENT BUS - PARTE 4: IMPLEMENTAÇÕES AVANÇADAS E KAFKA
## Roteiro Técnico para Analistas Java Junior

### 🎯 **OBJETIVO DESTA PARTE**
Compreender as implementações avançadas do Event Bus, incluindo integração com Kafka, configurações de produção e otimizações de performance.

---

## 🏗️ **IMPLEMENTAÇÕES DO EVENT BUS**

### **📋 Visão Geral das Implementações**

O projeto oferece duas implementações principais:

```
EventBus (Interface)
├── SimpleEventBus (Implementação em memória)
└── KafkaEventBus (Implementação distribuída)
```

### **🔧 SimpleEventBus - Implementação Local**

**Localização**: `com.seguradora.hibrida.eventbus.impl.SimpleEventBus`

**Características:**
- ✅ Processamento em memória
- ✅ Ideal para desenvolvimento e testes
- ✅ Baixa latência
- ❌ Não persiste eventos
- ❌ Limitado a uma instância

**Exemplo de Uso:**
```java
@Component
@ConditionalOnProperty(name = "eventbus.type", havingValue = "simple")
public class SimpleEventBusConfiguration {
    
    @Bean
    public EventBus eventBus(EventHandlerRegistry registry) {
        return new SimpleEventBus(registry);
    }
}
```

### **🚀 KafkaEventBus - Implementação Distribuída**

**Localização**: `com.seguradora.hibrida.eventbus.impl.KafkaEventBus`

**Características:**
- ✅ Processamento distribuído
- ✅ Persistência de eventos
- ✅ Alta disponibilidade
- ✅ Escalabilidade horizontal
- ⚠️ Maior complexidade

**Configuração Principal:**
```yaml
eventbus:
  type: kafka
  kafka:
    bootstrap-servers: localhost:9092
    default-topic: domain-events
    partitions: 3
    replication-factor: 1
```

---

## ⚙️ **CONFIGURAÇÕES AVANÇADAS**

### **📊 Configurações de Performance**

**Thread Pool Configuration:**
```yaml
eventbus:
  thread-pool:
    core-size: 10
    max-size: 50
    queue-capacity: 1000
    keep-alive-seconds: 60
```

**Retry Configuration:**
```yaml
eventbus:
  retry:
    max-attempts: 3
    initial-delay-ms: 1000
    max-delay-ms: 30000
    backoff-multiplier: 2.0
```

### **🔍 Monitoramento e Métricas**

**Configuração de Monitoramento:**
```yaml
eventbus:
  monitoring:
    enabled: true
    metrics-enabled: true
    detailed-logging: true
    error-rate-threshold: 0.05
```

**Métricas Disponíveis:**
- `eventbus.events.published` - Total de eventos publicados
- `eventbus.events.processed` - Total de eventos processados
- `eventbus.events.failed` - Total de falhas
- `eventbus.processing.time` - Tempo de processamento

---

## 🔄 **PROCESSAMENTO ASSÍNCRONO**

### **📤 Publicação Assíncrona**

**Implementação:**
```java
public class KafkaEventBus implements EventBus {
    
    public CompletableFuture<Void> publishAsync(DomainEvent event) {
        String correlationId = generateCorrelationId();
        
        return publishToKafka(event, correlationId)
            .thenRun(() -> {
                statistics.recordEventPublished(event.getEventType());
                log.debug("Event published async: {} [{}]", 
                    event.getEventType(), correlationId);
            })
            .exceptionally(throwable -> {
                log.error("Failed to publish event async: {} [{}]", 
                    event.getEventType(), correlationId, throwable);
                throw new EventPublishingException(event, throwable.getMessage());
            });
    }
}
```

### **⚡ Processamento em Lote**

**Batch Processing:**
```java
public void publishBatch(List<DomainEvent> events) {
    if (events.isEmpty()) return;
    
    String correlationId = generateCorrelationId();
    
    try {
        List<CompletableFuture<Void>> futures = events.stream()
            .map(event -> publishToKafka(event, correlationId))
            .collect(Collectors.toList());
            
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .get(30, TimeUnit.SECONDS);
            
        log.info("Published batch of {} events [{}]", events.size(), correlationId);
        
    } catch (Exception e) {
        log.error("Failed to publish event batch [{}]", correlationId, e);
        throw new EventPublishingException(null, "Batch publishing failed");
    }
}
```

---

## 🛡️ **TRATAMENTO DE ERROS AVANÇADO**

### **🔄 Estratégias de Retry**

**Configuração de Retry:**
```java
private void handleProcessingError(DomainEvent event, 
                                 EventHandler<DomainEvent> handler,
                                 String correlationId, 
                                 int attemptNumber,
                                 EventHandlingException exception) {
    
    if (attemptNumber < maxRetryAttempts && exception.isRetryable()) {
        long delay = calculateRetryDelay(attemptNumber);
        
        log.warn("Scheduling retry {} for event {} after {}ms [{}]",
            attemptNumber + 1, event.getEventType(), delay, correlationId);
            
        scheduleRetry(event, handler, correlationId, attemptNumber + 1);
        
    } else {
        log.error("Max retries exceeded for event {} [{}]",
            event.getEventType(), correlationId);
            
        sendToDeadLetterQueue(event, handler, exception, attemptNumber);
    }
}
```

### **💀 Dead Letter Queue**

**Implementação DLQ:**
```java
private void sendToDeadLetterQueue(DomainEvent event,
                                 EventHandler<DomainEvent> handler,
                                 EventHandlingException exception,
                                 int totalAttempts) {
    
    try {
        DeadLetterEvent dlqEvent = DeadLetterEvent.builder()
            .originalEvent(event)
            .handlerClass(handler.getClass().getName())
            .errorMessage(exception.getMessage())
            .totalAttempts(totalAttempts)
            .timestamp(Instant.now())
            .build();
            
        // Publicar no tópico DLQ
        publishToDeadLetterTopic(dlqEvent);
        
        statistics.recordEventDeadLettered();
        
        log.error("Event sent to DLQ: {} after {} attempts",
            event.getEventType(), totalAttempts);
            
    } catch (Exception e) {
        log.error("Failed to send event to DLQ: {}", event.getEventType(), e);
    }
}
```

---

## 📊 **ESTATÍSTICAS E MONITORAMENTO**

### **📈 EventBusStatistics**

**Implementação de Métricas:**
```java
public class EventBusStatistics {
    private final AtomicLong eventsPublished = new AtomicLong(0);
    private final AtomicLong eventsProcessed = new AtomicLong(0);
    private final AtomicLong eventsFailed = new AtomicLong(0);
    private final AtomicLong eventsRetried = new AtomicLong(0);
    private final AtomicLong eventsDeadLettered = new AtomicLong(0);
    
    private final Map<String, Long> eventsByType = new ConcurrentHashMap<>();
    private final Map<String, Long> failuresByType = new ConcurrentHashMap<>();
    
    public double getErrorRate() {
        long total = eventsProcessed.get() + eventsFailed.get();
        return total > 0 ? (double) eventsFailed.get() / total : 0.0;
    }
    
    public double getThroughput() {
        long duration = Duration.between(startTime, Instant.now()).toSeconds();
        return duration > 0 ? (double) eventsProcessed.get() / duration : 0.0;
    }
}
```

### **🔍 Health Check**

**Verificação de Saúde:**
```java
public boolean isHealthy() {
    try {
        // Verificar conectividade com Kafka
        if (kafkaProducer != null) {
            kafkaProducer.partitionsFor("health-check");
        }
        
        // Verificar estatísticas
        EventBusStatistics stats = getStatistics();
        double errorRate = stats.getErrorRate();
        
        return errorRate < 0.05; // Menos de 5% de erro
        
    } catch (Exception e) {
        log.error("Health check failed", e);
        return false;
    }
}
```

---

## 🎯 **BOAS PRÁTICAS DE IMPLEMENTAÇÃO**

### **✅ Padrões Recomendados**

1. **Idempotência**: Handlers devem ser idempotentes
2. **Timeout**: Configurar timeouts apropriados
3. **Logging**: Log detalhado para troubleshooting
4. **Métricas**: Monitorar performance continuamente
5. **Graceful Shutdown**: Finalizar processamento adequadamente

### **⚠️ Armadilhas Comuns**

1. **Não tratar exceções**: Sempre implementar tratamento de erro
2. **Handlers lentos**: Otimizar processamento de eventos
3. **Memory leaks**: Limpar recursos adequadamente
4. **Configuração inadequada**: Ajustar para ambiente de produção

---

## 🔧 **CONFIGURAÇÃO PARA PRODUÇÃO**

### **🚀 Kafka Production Settings**

```yaml
eventbus:
  kafka:
    bootstrap-servers: kafka-cluster:9092
    producer:
      acks: all
      retries: 3
      batch-size: 16384
      linger-ms: 5
      buffer-memory: 33554432
    consumer:
      group-id: sinistro-core-consumers
      auto-offset-reset: earliest
      enable-auto-commit: false
      max-poll-records: 500
      session-timeout-ms: 30000
```

### **📊 Monitoring Configuration**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,eventbus
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 🎓 **EXERCÍCIO PRÁTICO**

### **📝 Implementar Handler com Retry**

Crie um handler que:
1. Processe eventos de sinistro
2. Implemente retry automático
3. Registre métricas personalizadas
4. Trate erros adequadamente

**Template:**
```java
@Component
public class SinistroEventHandlerWithRetry implements EventHandler<SinistroEvent> {
    
    @Override
    public void handle(SinistroEvent event) {
        // Sua implementação aqui
    }
    
    @Override
    public boolean isRetryable() {
        return true;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 30;
    }
}
```

---

## 📚 **REFERÊNCIAS**

- **Código**: `com.seguradora.hibrida.eventbus.impl`
- **Configuração**: `event-bus.yml`
- **Testes**: `EventBusIntegrationTest`
- **Documentação**: Apache Kafka Documentation

---

**📍 Próxima Parte**: [Event Bus - Parte 5: Testes e Troubleshooting](./06-event-bus-parte-5.md)

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Implementações avançadas e Kafka  
**⏱️ Tempo estimado:** 45 minutos  
**🔧 Hands-on:** Configuração de produção