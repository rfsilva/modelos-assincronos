# 📘 EVENT BUS - PARTE 3
## Configuração, Propriedades e Tratamento de Erros

### 🎯 **OBJETIVOS DESTA PARTE**
- Dominar a configuração do Event Bus
- Compreender as propriedades e suas implicações
- Implementar tratamento robusto de erros
- Configurar políticas de retry e dead letter queue

---

## ⚙️ **CONFIGURAÇÃO DO EVENT BUS**

### **📋 EventBusProperties**

Localização: `com.seguradora.hibrida.eventbus.config.EventBusProperties`

```java
@ConfigurationProperties(prefix = "event-bus")
@Data
@Validated
public class EventBusProperties {
    
    /**
     * Habilita ou desabilita o Event Bus.
     */
    private boolean enabled = true;
    
    /**
     * Tipo de implementação do Event Bus.
     * Valores: simple, kafka
     */
    private String type = "simple";
    
    /**
     * Configurações de thread pool.
     */
    private ThreadPool threadPool = new ThreadPool();
    
    /**
     * Configurações de retry.
     */
    private Retry retry = new Retry();
    
    /**
     * Configurações de timeout.
     */
    private Timeout timeout = new Timeout();
    
    /**
     * Configurações de monitoramento.
     */
    private Monitoring monitoring = new Monitoring();
    
    /**
     * Configurações específicas do Kafka.
     */
    private Kafka kafka = new Kafka();
    
    /**
     * Logging detalhado de eventos.
     */
    private boolean detailedLogging = false;
    
    /**
     * Timeout padrão para handlers em segundos.
     */
    @Min(1)
    @Max(300)
    private int defaultHandlerTimeoutSeconds = 30;
    
    /**
     * Timeout para shutdown em segundos.
     */
    @Min(1)
    @Max(60)
    private int shutdownTimeoutSeconds = 30;
    
    @Data
    public static class ThreadPool {
        /**
         * Número de threads core no pool.
         */
        @Min(1)
        private int coreSize = Runtime.getRuntime().availableProcessors();
        
        /**
         * Número máximo de threads no pool.
         */
        @Min(1)
        private int maxSize = Runtime.getRuntime().availableProcessors() * 2;
        
        /**
         * Capacidade da fila de tarefas.
         */
        @Min(1)
        private int queueCapacity = 1000;
        
        /**
         * Tempo de vida de threads ociosas em segundos.
         */
        @Min(1)
        private int keepAliveSeconds = 60;
        
        /**
         * Prefixo do nome das threads.
         */
        private String threadNamePrefix = "event-bus-";
    }
    
    @Data
    public static class Retry {
        /**
         * Número máximo de tentativas.
         */
        @Min(1)
        @Max(10)
        private int maxAttempts = 3;
        
        /**
         * Delay inicial em milissegundos.
         */
        @Min(100)
        private long initialDelayMs = 1000;
        
        /**
         * Delay máximo em milissegundos.
         */
        @Min(1000)
        private long maxDelayMs = 30000;
        
        /**
         * Multiplicador para backoff exponencial.
         */
        @DecimalMin("1.0")
        @DecimalMax("5.0")
        private double backoffMultiplier = 2.0;
        
        /**
         * Percentual de jitter para evitar thundering herd.
         */
        @DecimalMin("0.0")
        @DecimalMax("50.0")
        private double jitterPercent = 10.0;
    }
    
    @Data
    public static class Timeout {
        /**
         * Timeout para publicação síncrona em segundos.
         */
        @Min(1)
        private int publishTimeoutSeconds = 10;
        
        /**
         * Timeout para processamento de handler em segundos.
         */
        @Min(1)
        private int handlerTimeoutSeconds = 30;
        
        /**
         * Timeout para shutdown em segundos.
         */
        @Min(1)
        private int shutdownTimeoutSeconds = 30;
    }
    
    @Data
    public static class Monitoring {
        /**
         * Habilita health checks.
         */
        private boolean healthCheckEnabled = true;
        
        /**
         * Habilita coleta de métricas.
         */
        private boolean metricsEnabled = true;
        
        /**
         * Threshold de taxa de erro para alertas.
         */
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private double errorRateThreshold = 0.05;
        
        /**
         * Intervalo de coleta de métricas em segundos.
         */
        @Min(1)
        private int metricsIntervalSeconds = 60;
    }
    
    @Data
    public static class Kafka {
        /**
         * Servidores bootstrap do Kafka.
         */
        @NotBlank
        private String bootstrapServers = "localhost:9092";
        
        /**
         * Group ID para consumers.
         */
        @NotBlank
        private String groupId = "event-bus-consumers";
        
        /**
         * Tópico padrão para eventos.
         */
        private String defaultTopic = "domain-events";
        
        /**
         * Configurações do Producer.
         */
        private Producer producer = new Producer();
        
        /**
         * Configurações do Consumer.
         */
        private Consumer consumer = new Consumer();
        
        @Data
        public static class Producer {
            /**
             * Nível de acknowledgment.
             * Valores: 0, 1, all
             */
            private String acks = "all";
            
            /**
             * Número de retries.
             */
            @Min(0)
            private int retries = 3;
            
            /**
             * Tamanho do batch em bytes.
             */
            @Min(1)
            private int batchSize = 16384;
            
            /**
             * Tempo de espera para formar batch em ms.
             */
            @Min(0)
            private int lingerMs = 5;
            
            /**
             * Memória total disponível para buffering em bytes.
             */
            @Min(1024)
            private long bufferMemory = 33554432; // 32MB
            
            /**
             * Número de partições para novos tópicos.
             */
            @Min(1)
            private int partitions = 3;
            
            /**
             * Fator de replicação para novos tópicos.
             */
            @Min(1)
            private short replicationFactor = 1;
        }
        
        @Data
        public static class Consumer {
            /**
             * Estratégia de reset de offset.
             * Valores: earliest, latest, none
             */
            private String autoOffsetReset = "earliest";
            
            /**
             * Habilita auto commit de offset.
             */
            private boolean enableAutoCommit = true;
            
            /**
             * Timeout de sessão em ms.
             */
            @Min(1000)
            private int sessionTimeoutMs = 30000;
            
            /**
             * Máximo de registros por poll.
             */
            @Min(1)
            private int maxPollRecords = 500;
        }
    }
}
```

### **📝 Arquivo de Configuração**

```yaml
# application.yml
event-bus:
  enabled: true
  type: simple  # ou kafka
  detailed-logging: false
  default-handler-timeout-seconds: 30
  shutdown-timeout-seconds: 30
  
  thread-pool:
    core-size: 4
    max-size: 8
    queue-capacity: 1000
    keep-alive-seconds: 60
    thread-name-prefix: "event-bus-"
  
  retry:
    max-attempts: 3
    initial-delay-ms: 1000
    max-delay-ms: 30000
    backoff-multiplier: 2.0
    jitter-percent: 10.0
  
  timeout:
    publish-timeout-seconds: 10
    handler-timeout-seconds: 30
    shutdown-timeout-seconds: 30
  
  monitoring:
    health-check-enabled: true
    metrics-enabled: true
    error-rate-threshold: 0.05
    metrics-interval-seconds: 60
  
  kafka:
    bootstrap-servers: "localhost:9092"
    group-id: "event-bus-consumers"
    default-topic: "domain-events"
    
    producer:
      acks: "all"
      retries: 3
      batch-size: 16384
      linger-ms: 5
      buffer-memory: 33554432
      partitions: 3
      replication-factor: 1
    
    consumer:
      auto-offset-reset: "earliest"
      enable-auto-commit: true
      session-timeout-ms: 30000
      max-poll-records: 500

# Configuração específica para produção
---
spring:
  profiles: production

event-bus:
  type: kafka
  detailed-logging: false
  
  thread-pool:
    core-size: 8
    max-size: 16
    queue-capacity: 2000
  
  retry:
    max-attempts: 5
    initial-delay-ms: 2000
    max-delay-ms: 60000
  
  kafka:
    bootstrap-servers: "${KAFKA_BOOTSTRAP_SERVERS:kafka-cluster:9092}"
    group-id: "${KAFKA_GROUP_ID:seguradora-event-bus}"
    
    producer:
      acks: "all"
      retries: 5
      batch-size: 32768
      linger-ms: 10
      partitions: 6
      replication-factor: 3
```

---

## ⚠️ **TRATAMENTO DE ERROS**

### **🎯 Hierarquia de Exceções**

```java
// Exceção base para Event Bus
public class EventBusException extends RuntimeException {
    public EventBusException(String message) {
        super(message);
    }
    
    public EventBusException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Exceção para falhas na publicação
public class EventPublishingException extends EventBusException {
    private final DomainEvent event;
    private final String reason;
    
    public EventPublishingException(DomainEvent event, String reason) {
        super(String.format("Failed to publish event %s: %s", 
                           event.getEventId(), reason));
        this.event = event;
        this.reason = reason;
    }
    
    public EventPublishingException(DomainEvent event, String reason, Throwable cause) {
        super(String.format("Failed to publish event %s: %s", 
                           event.getEventId(), reason), cause);
        this.event = event;
        this.reason = reason;
    }
    
    public DomainEvent getEvent() { return event; }
    public String getReason() { return reason; }
    public String getEventType() { return event.getEventType(); }
    public String getAggregateId() { return event.getAggregateId(); }
}

// Exceção para falhas no processamento
public class EventHandlingException extends EventBusException {
    private final DomainEvent event;
    private final String handlerClass;
    private final boolean retryable;
    
    public EventHandlingException(DomainEvent event, String handlerClass, Throwable cause) {
        super(String.format("Handler %s failed to process event %s: %s", 
                           handlerClass, event.getEventId(), cause.getMessage()), cause);
        this.event = event;
        this.handlerClass = handlerClass;
        this.retryable = true; // Por padrão, permite retry
    }
    
    public EventHandlingException(DomainEvent event, String handlerClass, 
                                String message, boolean retryable) {
        super(String.format("Handler %s failed to process event %s: %s", 
                           handlerClass, event.getEventId(), message));
        this.event = event;
        this.handlerClass = handlerClass;
        this.retryable = retryable;
    }
    
    public DomainEvent getEvent() { return event; }
    public String getHandlerClass() { return handlerClass; }
    public boolean isRetryable() { return retryable; }
    public String getEventType() { return event.getEventType(); }
    public String getAggregateId() { return event.getAggregateId(); }
}

// Exceção para timeout de handler
public class EventHandlerTimeoutException extends EventHandlingException {
    private final int timeoutSeconds;
    private final long actualTimeMs;
    
    public EventHandlerTimeoutException(DomainEvent event, String handlerClass, 
                                      int timeoutSeconds, long actualTimeMs) {
        super(event, handlerClass, 
              String.format("Handler timeout after %d seconds (actual: %.2f seconds)", 
                           timeoutSeconds, actualTimeMs / 1000.0), true);
        this.timeoutSeconds = timeoutSeconds;
        this.actualTimeMs = actualTimeMs;
    }
    
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public long getActualTimeMs() { return actualTimeMs; }
    public double getActualTimeSeconds() { return actualTimeMs / 1000.0; }
}
```

### **🔄 Estratégias de Retry**

```java
@Component
public class RetryPolicyManager {
    
    private final EventBusProperties properties;
    
    public RetryPolicyManager(EventBusProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Determina se um evento deve ser reprocessado.
     */
    public boolean shouldRetry(DomainEvent event, EventHandler<?> handler, 
                             Exception exception, int attemptNumber) {
        
        // Verifica se handler suporta retry
        if (!handler.isRetryable()) {
            return false;
        }
        
        // Verifica número máximo de tentativas
        if (attemptNumber >= properties.getRetry().getMaxAttempts()) {
            return false;
        }
        
        // Verifica tipo de exceção
        if (exception instanceof EventHandlerTimeoutException) {
            return true; // Timeout pode ser temporário
        }
        
        if (exception instanceof EventHandlingException) {
            EventHandlingException ehe = (EventHandlingException) exception;
            return ehe.isRetryable();
        }
        
        // Exceções de infraestrutura geralmente são retryable
        if (exception instanceof SQLException || 
            exception instanceof ConnectException ||
            exception instanceof SocketTimeoutException) {
            return true;
        }
        
        // Exceções de validação geralmente não são retryable
        if (exception instanceof IllegalArgumentException ||
            exception instanceof ValidationException) {
            return false;
        }
        
        // Por padrão, permite retry
        return true;
    }
    
    /**
     * Calcula delay para próxima tentativa.
     */
    public long calculateRetryDelay(int attemptNumber) {
        long baseDelay = properties.getRetry().getInitialDelayMs();
        double multiplier = properties.getRetry().getBackoffMultiplier();
        double jitter = properties.getRetry().getJitterPercent();
        
        // Exponential backoff
        long delay = (long) (baseDelay * Math.pow(multiplier, attemptNumber - 1));
        
        // Aplica jitter para evitar thundering herd
        if (jitter > 0) {
            double jitterAmount = delay * jitter / 100.0;
            double randomJitter = (Math.random() - 0.5) * 2 * jitterAmount;
            delay += (long) randomJitter;
        }
        
        // Garante que não exceda o máximo
        return Math.min(delay, properties.getRetry().getMaxDelayMs());
    }
    
    /**
     * Cria política de retry customizada para um handler específico.
     */
    public RetryPolicy createRetryPolicy(EventHandler<?> handler) {
        return RetryPolicy.builder()
            .maxAttempts(properties.getRetry().getMaxAttempts())
            .initialDelay(Duration.ofMillis(properties.getRetry().getInitialDelayMs()))
            .maxDelay(Duration.ofMillis(properties.getRetry().getMaxDelayMs()))
            .backoffMultiplier(properties.getRetry().getBackoffMultiplier())
            .jitterPercent(properties.getRetry().getJitterPercent())
            .retryableExceptions(getRetryableExceptions())
            .nonRetryableExceptions(getNonRetryableExceptions())
            .build();
    }
    
    private Set<Class<? extends Exception>> getRetryableExceptions() {
        return Set.of(
            EventHandlerTimeoutException.class,
            SQLException.class,
            ConnectException.class,
            SocketTimeoutException.class,
            TransientDataAccessException.class
        );
    }
    
    private Set<Class<? extends Exception>> getNonRetryableExceptions() {
        return Set.of(
            IllegalArgumentException.class,
            ValidationException.class,
            SecurityException.class,
            AuthenticationException.class
        );
    }
}

// Classe para configuração de retry
@Data
@Builder
public class RetryPolicy {
    private final int maxAttempts;
    private final Duration initialDelay;
    private final Duration maxDelay;
    private final double backoffMultiplier;
    private final double jitterPercent;
    private final Set<Class<? extends Exception>> retryableExceptions;
    private final Set<Class<? extends Exception>> nonRetryableExceptions;
    
    public boolean isRetryable(Exception exception) {
        // Verifica exceções explicitamente não retryable
        for (Class<? extends Exception> nonRetryableType : nonRetryableExceptions) {
            if (nonRetryableType.isAssignableFrom(exception.getClass())) {
                return false;
            }
        }
        
        // Verifica exceções explicitamente retryable
        for (Class<? extends Exception> retryableType : retryableExceptions) {
            if (retryableType.isAssignableFrom(exception.getClass())) {
                return true;
            }
        }
        
        // Por padrão, não é retryable
        return false;
    }
}
```

---

## 💀 **DEAD LETTER QUEUE**

### **📝 Implementação de DLQ**

```java
@Component
@Slf4j
public class DeadLetterQueueManager {
    
    private final DeadLetterEventRepository repository;
    private final EventBusProperties properties;
    private final MeterRegistry meterRegistry;
    
    // Métricas
    private final Counter deadLetterCounter;
    
    public DeadLetterQueueManager(DeadLetterEventRepository repository,
                                EventBusProperties properties,
                                MeterRegistry meterRegistry) {
        this.repository = repository;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        
        this.deadLetterCounter = Counter.builder("event.bus.dead.letter")
            .description("Number of events sent to dead letter queue")
            .register(meterRegistry);
    }
    
    /**
     * Envia evento para dead letter queue.
     */
    public void sendToDeadLetterQueue(DomainEvent event, 
                                    EventHandler<?> handler, 
                                    Exception lastException, 
                                    int totalAttempts) {
        
        log.error("Sending event {} to dead letter queue after {} attempts with handler {}: {}", 
                 event.getEventId(), totalAttempts, handler.getClass().getSimpleName(), 
                 lastException.getMessage());
        
        DeadLetterEvent dlqEvent = DeadLetterEvent.builder()
            .eventId(event.getEventId())
            .aggregateId(event.getAggregateId())
            .eventType(event.getEventType())
            .eventData(serializeEvent(event))
            .handlerClass(handler.getClass().getName())
            .failureReason(lastException.getMessage())
            .stackTrace(getStackTrace(lastException))
            .totalAttempts(totalAttempts)
            .firstFailureAt(calculateFirstFailureTime(totalAttempts))
            .lastFailureAt(Instant.now())
            .status(DeadLetterStatus.FAILED)
            .build();
        
        try {
            repository.save(dlqEvent);
            deadLetterCounter.increment(
                Tags.of(
                    "event.type", event.getEventType(),
                    "handler.class", handler.getClass().getSimpleName(),
                    "failure.reason", classifyFailureReason(lastException)
                )
            );
            
            log.info("Event {} successfully saved to dead letter queue", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to save event {} to dead letter queue: {}", 
                     event.getEventId(), e.getMessage(), e);
        }
    }
    
    /**
     * Reprocessa eventos da dead letter queue.
     */
    public ReprocessingResult reprocessDeadLetterEvents(String eventType, int maxEvents) {
        log.info("Starting reprocessing of dead letter events for type: {}", eventType);
        
        List<DeadLetterEvent> events = repository.findByEventTypeAndStatus(
            eventType, DeadLetterStatus.FAILED, PageRequest.of(0, maxEvents));
        
        ReprocessingResult result = new ReprocessingResult();
        
        for (DeadLetterEvent dlqEvent : events) {
            try {
                boolean success = reprocessSingleEvent(dlqEvent);
                if (success) {
                    dlqEvent.setStatus(DeadLetterStatus.REPROCESSED);
                    dlqEvent.setReprocessedAt(Instant.now());
                    repository.save(dlqEvent);
                    result.incrementSuccess();
                } else {
                    result.incrementFailed();
                }
                
            } catch (Exception e) {
                log.error("Failed to reprocess dead letter event {}: {}", 
                         dlqEvent.getEventId(), e.getMessage(), e);
                result.incrementFailed();
            }
        }
        
        log.info("Reprocessing completed: {} success, {} failed", 
                result.getSuccessCount(), result.getFailedCount());
        
        return result;
    }
    
    private boolean reprocessSingleEvent(DeadLetterEvent dlqEvent) {
        try {
            // Deserializa evento original
            DomainEvent originalEvent = deserializeEvent(dlqEvent.getEventData(), 
                                                       dlqEvent.getEventType());
            
            // Busca handler original
            Class<?> handlerClass = Class.forName(dlqEvent.getHandlerClass());
            EventHandler<DomainEvent> handler = findHandlerInstance(handlerClass);
            
            if (handler == null) {
                log.warn("Handler {} not found for reprocessing event {}", 
                        dlqEvent.getHandlerClass(), dlqEvent.getEventId());
                return false;
            }
            
            // Reprocessa evento
            handler.handle(originalEvent);
            
            log.info("Successfully reprocessed dead letter event: {}", dlqEvent.getEventId());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to reprocess dead letter event {}: {}", 
                     dlqEvent.getEventId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Obtém estatísticas da dead letter queue.
     */
    public DeadLetterStatistics getStatistics() {
        return DeadLetterStatistics.builder()
            .totalEvents(repository.count())
            .failedEvents(repository.countByStatus(DeadLetterStatus.FAILED))
            .reprocessedEvents(repository.countByStatus(DeadLetterStatus.REPROCESSED))
            .eventsByType(repository.countEventsByType())
            .eventsByHandler(repository.countEventsByHandler())
            .oldestFailure(repository.findOldestFailure())
            .recentFailures(repository.findRecentFailures(Duration.ofHours(24)))
            .build();
    }
    
    // Métodos auxiliares
    private String serializeEvent(DomainEvent event) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(event);
        } catch (Exception e) {
            log.error("Failed to serialize event {}: {}", event.getEventId(), e.getMessage());
            return "{}";
        }
    }
    
    private DomainEvent deserializeEvent(String eventData, String eventType) {
        // Implementação de deserialização
        // ...
        return null;
    }
    
    private String getStackTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
    
    private Instant calculateFirstFailureTime(int totalAttempts) {
        // Estima quando foi a primeira falha baseado no número de tentativas
        long estimatedDuration = 0;
        for (int i = 1; i < totalAttempts; i++) {
            estimatedDuration += calculateRetryDelay(i);
        }
        return Instant.now().minusMillis(estimatedDuration);
    }
    
    private String classifyFailureReason(Exception exception) {
        if (exception instanceof EventHandlerTimeoutException) {
            return "timeout";
        } else if (exception instanceof SQLException) {
            return "database";
        } else if (exception instanceof ConnectException) {
            return "network";
        } else if (exception instanceof ValidationException) {
            return "validation";
        } else {
            return "unknown";
        }
    }
}

// Entidade para dead letter events
@Entity
@Table(name = "dead_letter_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadLetterEvent {
    
    @Id
    private UUID eventId;
    
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData;
    
    @Column(name = "handler_class", nullable = false)
    private String handlerClass;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;
    
    @Column(name = "total_attempts")
    private int totalAttempts;
    
    @Column(name = "first_failure_at")
    private Instant firstFailureAt;
    
    @Column(name = "last_failure_at")
    private Instant lastFailureAt;
    
    @Column(name = "reprocessed_at")
    private Instant reprocessedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DeadLetterStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}

// Status da dead letter queue
public enum DeadLetterStatus {
    FAILED,
    REPROCESSED,
    IGNORED
}
```

---

## 🎯 **CIRCUIT BREAKER**

### **📝 Implementação de Circuit Breaker**

```java
@Component
public class EventHandlerCircuitBreaker {
    
    private final Map<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();
    private final EventBusProperties properties;
    
    public EventHandlerCircuitBreaker(EventBusProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Executa handler com circuit breaker.
     */
    public void executeWithCircuitBreaker(EventHandler<?> handler, 
                                        DomainEvent event, 
                                        Runnable execution) {
        
        String handlerKey = handler.getClass().getName();
        CircuitBreakerState state = circuitBreakers.computeIfAbsent(
            handlerKey, k -> new CircuitBreakerState());
        
        if (state.isOpen()) {
            if (state.shouldAttemptReset()) {
                // Tenta resetar circuit breaker
                state.halfOpen();
                try {
                    execution.run();
                    state.recordSuccess();
                    log.info("Circuit breaker reset for handler: {}", handlerKey);
                } catch (Exception e) {
                    state.recordFailure();
                    throw new CircuitBreakerOpenException(handlerKey, e);
                }
            } else {
                throw new CircuitBreakerOpenException(handlerKey);
            }
        } else {
            try {
                execution.run();
                state.recordSuccess();
            } catch (Exception e) {
                state.recordFailure();
                
                if (state.shouldOpen()) {
                    log.warn("Opening circuit breaker for handler: {} after {} failures", 
                            handlerKey, state.getFailureCount());
                    state.open();
                }
                
                throw e;
            }
        }
    }
    
    /**
     * Estado do circuit breaker para um handler.
     */
    private static class CircuitBreakerState {
        private volatile CircuitState state = CircuitState.CLOSED;
        private volatile int failureCount = 0;
        private volatile int successCount = 0;
        private volatile Instant lastFailureTime;
        private volatile Instant openedAt;
        
        private static final int FAILURE_THRESHOLD = 5;
        private static final Duration TIMEOUT = Duration.ofMinutes(1);
        
        public boolean isOpen() {
            return state == CircuitState.OPEN;
        }
        
        public boolean shouldAttemptReset() {
            return isOpen() && 
                   openedAt != null && 
                   Duration.between(openedAt, Instant.now()).compareTo(TIMEOUT) > 0;
        }
        
        public void halfOpen() {
            this.state = CircuitState.HALF_OPEN;
        }
        
        public void recordSuccess() {
            this.successCount++;
            this.failureCount = 0;
            
            if (state == CircuitState.HALF_OPEN) {
                this.state = CircuitState.CLOSED;
            }
        }
        
        public void recordFailure() {
            this.failureCount++;
            this.lastFailureTime = Instant.now();
            
            if (state == CircuitState.HALF_OPEN) {
                open();
            }
        }
        
        public boolean shouldOpen() {
            return failureCount >= FAILURE_THRESHOLD;
        }
        
        public void open() {
            this.state = CircuitState.OPEN;
            this.openedAt = Instant.now();
        }
        
        public int getFailureCount() {
            return failureCount;
        }
    }
    
    private enum CircuitState {
        CLOSED, OPEN, HALF_OPEN
    }
}

// Exceção para circuit breaker aberto
public class CircuitBreakerOpenException extends EventHandlingException {
    private final String handlerClass;
    
    public CircuitBreakerOpenException(String handlerClass) {
        super(null, handlerClass, "Circuit breaker is open", false);
        this.handlerClass = handlerClass;
    }
    
    public CircuitBreakerOpenException(String handlerClass, Throwable cause) {
        super(null, handlerClass, "Circuit breaker is open: " + cause.getMessage(), false);
        this.handlerClass = handlerClass;
    }
}
```

---

## 🎯 **EXERCÍCIOS PRÁTICOS**

### **📝 Exercício 1: Configuração Customizada**
Configure o Event Bus para um cenário específico:
- Alta disponibilidade (Kafka)
- Retry agressivo para falhas temporárias
- Circuit breaker para handlers problemáticos
- Dead letter queue com reprocessamento automático

### **📝 Exercício 2: Tratamento de Erros**
Implemente um handler que:
- Simule diferentes tipos de falha
- Teste políticas de retry
- Verifique comportamento do circuit breaker
- Analise eventos na dead letter queue

### **📝 Exercício 3: Monitoramento**
Crie dashboards para:
- Taxa de erro por handler
- Latência de processamento
- Status dos circuit breakers
- Estatísticas da dead letter queue

---

## 🔗 **PRÓXIMOS PASSOS**

Na **Parte 4** do Event Bus, abordaremos:
- **Métricas avançadas** e monitoramento
- **Testes** de integração e performance
- **Otimização** de throughput
- **Integração** com sistemas externos

---

## 📚 **REFERÊNCIAS**

### **📖 Documentação Técnica**
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Dead Letter Queue Pattern](https://www.enterpriseintegrationpatterns.com/patterns/messaging/DeadLetterChannel.html)
- [Retry Patterns](https://docs.microsoft.com/en-us/azure/architecture/patterns/retry)

### **🔧 Código de Referência**
- `EventBusProperties.java` - Configurações
- `RetryPolicyManager.java` - Políticas de retry
- `DeadLetterQueueManager.java` - Dead letter queue
- `EventHandlerCircuitBreaker.java` - Circuit breaker

---

**📘 Capítulo:** 06 - Event Bus - Parte 3  
**⏱️ Tempo Estimado:** 55 minutos  
**🎯 Próximo:** [06 - Event Bus - Parte 4](./06-event-bus-parte-4.md)  
**📋 Checklist:** Configuração ✅ | Tratamento de Erros ✅ | DLQ ✅ | Circuit Breaker ✅