# 📘 EVENT BUS - PARTE 2
## Implementações e Registro de Handlers

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender as implementações do Event Bus
- Dominar o registro automático de handlers
- Configurar propriedades do Event Bus
- Entender as diferenças entre SimpleEventBus e KafkaEventBus

---

## 🏗️ **IMPLEMENTAÇÕES DO EVENT BUS**

### **📊 Comparação das Implementações**

| **Aspecto** | **SimpleEventBus** | **KafkaEventBus** |
|-------------|-------------------|-------------------|
| **Complexidade** | Baixa | Alta |
| **Performance** | Boa para baixo volume | Excelente para alto volume |
| **Persistência** | Em memória | Durável (Kafka) |
| **Escalabilidade** | Limitada | Horizontal |
| **Garantias** | At-most-once | At-least-once |
| **Uso Recomendado** | Desenvolvimento/Testes | Produção |

---

## 🔧 **SIMPLE EVENT BUS**

### **📝 Implementação Básica**

Localização: `com.seguradora.hibrida.eventbus.impl.SimpleEventBus`

```java
@Component
@ConditionalOnProperty(name = "event-bus.type", havingValue = "simple", matchIfMissing = true)
@Slf4j
public class SimpleEventBus implements EventBus {
    
    private final EventHandlerRegistry handlerRegistry;
    private final EventBusStatistics statistics;
    private final ThreadPoolTaskExecutor executor;
    private final EventBusProperties properties;
    
    // Controle de shutdown
    private volatile boolean shutdown = false;
    
    public SimpleEventBus(EventHandlerRegistry handlerRegistry,
                         EventBusStatistics statistics,
                         ThreadPoolTaskExecutor executor,
                         EventBusProperties properties) {
        this.handlerRegistry = handlerRegistry;
        this.statistics = statistics;
        this.executor = executor;
        this.properties = properties;
    }
    
    @Override
    public void publish(DomainEvent event) {
        validateEvent(event);
        
        if (shutdown) {
            throw new EventBusException("Event Bus is shutdown");
        }
        
        String correlationId = generateCorrelationId();
        log.debug("Publishing event synchronously: {} with correlation: {}", 
                 event.getEventType(), correlationId);
        
        try {
            processEventSync(event, correlationId);
            statistics.recordEventPublished(event.getEventType());
            
        } catch (Exception e) {
            statistics.recordEventFailed(event.getEventType(), false);
            throw new EventPublishingException(event, "Failed to publish event", e);
        }
    }
    
    @Override
    public CompletableFuture<Void> publishAsync(DomainEvent event) {
        validateEvent(event);
        
        if (shutdown) {
            return CompletableFuture.failedFuture(
                new EventBusException("Event Bus is shutdown"));
        }
        
        String correlationId = generateCorrelationId();
        log.debug("Publishing event asynchronously: {} with correlation: {}", 
                 event.getEventType(), correlationId);
        
        return CompletableFuture
            .runAsync(() -> {
                try {
                    processEventSync(event, correlationId);
                    statistics.recordEventPublished(event.getEventType());
                } catch (Exception e) {
                    statistics.recordEventFailed(event.getEventType(), false);
                    log.error("Failed to process event asynchronously: {}", 
                             event.getEventId(), e);
                    throw new RuntimeException(e);
                }
            }, executor)
            .exceptionally(throwable -> {
                log.error("Async event processing failed: {}", 
                         event.getEventId(), throwable);
                return null;
            });
    }
    
    @Override
    public CompletableFuture<Void> publishBatchAsync(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("Events list cannot be null or empty");
        }
        
        if (shutdown) {
            return CompletableFuture.failedFuture(
                new EventBusException("Event Bus is shutdown"));
        }
        
        log.debug("Publishing {} events in batch", events.size());
        
        List<CompletableFuture<Void>> futures = events.stream()
            .map(this::publishAsync)
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    private void processEventSync(DomainEvent event, String correlationId) {
        List<EventHandler<DomainEvent>> handlers = getHandlersForEvent(event);
        
        if (handlers.isEmpty()) {
            log.debug("No handlers found for event type: {}", event.getEventType());
            return;
        }
        
        log.debug("Processing event {} with {} handlers", 
                 event.getEventId(), handlers.size());
        
        // Ordena handlers por prioridade (maior prioridade primeiro)
        handlers.sort((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()));
        
        for (EventHandler<DomainEvent> handler : handlers) {
            processWithHandler(event, handler, correlationId, 1);
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<EventHandler<DomainEvent>> getHandlersForEvent(DomainEvent event) {
        return handlerRegistry.getHandlers((Class<DomainEvent>) event.getClass());
    }
    
    private void processWithHandler(DomainEvent event, 
                                  EventHandler<DomainEvent> handler, 
                                  String correlationId, 
                                  int attemptNumber) {
        
        if (!handler.supports(event)) {
            log.debug("Handler {} does not support event {}", 
                     handler.getClass().getSimpleName(), event.getEventId());
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Processing event {} with handler {} (attempt {})", 
                     event.getEventId(), handler.getClass().getSimpleName(), attemptNumber);
            
            if (handler.isAsync()) {
                // Processamento assíncrono
                executor.execute(() -> {
                    try {
                        handler.handle(event);
                        statistics.recordEventProcessed(event.getEventType(), 
                                                      System.currentTimeMillis() - startTime);
                    } catch (Exception e) {
                        handleProcessingError(event, handler, correlationId, attemptNumber, 
                                            new EventHandlingException(event, 
                                                                      handler.getClass().getSimpleName(), e));
                    }
                });
            } else {
                // Processamento síncrono
                handler.handle(event);
                statistics.recordEventProcessed(event.getEventType(), 
                                              System.currentTimeMillis() - startTime);
            }
            
        } catch (Exception e) {
            handleProcessingError(event, handler, correlationId, attemptNumber, 
                                new EventHandlingException(event, 
                                                          handler.getClass().getSimpleName(), e));
        }
    }
    
    private void handleProcessingError(DomainEvent event, 
                                     EventHandler<DomainEvent> handler, 
                                     String correlationId, 
                                     int attemptNumber, 
                                     EventHandlingException exception) {
        
        log.error("Error processing event {} with handler {} (attempt {}): {}", 
                 event.getEventId(), handler.getClass().getSimpleName(), 
                 attemptNumber, exception.getMessage());
        
        statistics.recordEventFailed(event.getEventType(), handler.isRetryable());
        
        // Retry logic
        if (handler.isRetryable() && attemptNumber < properties.getRetry().getMaxAttempts()) {
            scheduleRetry(event, handler, correlationId, attemptNumber);
        } else {
            // Máximo de tentativas atingido ou handler não suporta retry
            sendToDeadLetterQueue(event, handler, exception, attemptNumber);
        }
    }
    
    private void scheduleRetry(DomainEvent event, 
                             EventHandler<DomainEvent> handler, 
                             String correlationId, 
                             int attemptNumber) {
        
        long delay = calculateRetryDelay(attemptNumber);
        
        log.info("Scheduling retry for event {} with handler {} in {}ms (attempt {})", 
                event.getEventId(), handler.getClass().getSimpleName(), delay, attemptNumber + 1);
        
        CompletableFuture
            .delayedExecutor(delay, TimeUnit.MILLISECONDS, executor)
            .execute(() -> {
                statistics.recordEventRetried(event.getEventType());
                processWithHandler(event, handler, correlationId, attemptNumber + 1);
            });
    }
    
    private long calculateRetryDelay(int attemptNumber) {
        // Exponential backoff com jitter
        long baseDelay = properties.getRetry().getInitialDelayMs();
        double multiplier = properties.getRetry().getBackoffMultiplier();
        double jitter = properties.getRetry().getJitterPercent();
        
        long delay = (long) (baseDelay * Math.pow(multiplier, attemptNumber - 1));
        
        // Adiciona jitter para evitar thundering herd
        if (jitter > 0) {
            double jitterAmount = delay * jitter / 100.0;
            delay += (long) (Math.random() * jitterAmount);
        }
        
        return Math.min(delay, properties.getRetry().getMaxDelayMs());
    }
    
    private void sendToDeadLetterQueue(DomainEvent event, 
                                     EventHandler<DomainEvent> handler, 
                                     EventHandlingException exception, 
                                     int totalAttempts) {
        
        log.error("Sending event {} to dead letter queue after {} attempts with handler {}: {}", 
                 event.getEventId(), totalAttempts, handler.getClass().getSimpleName(), 
                 exception.getMessage());
        
        // Aqui seria implementada a lógica de dead letter queue
        // Por exemplo, salvar em uma tabela de eventos com falha
        statistics.recordEventDeadLettered(event.getEventType());
    }
    
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
    
    private void validateEvent(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (event.getEventId() == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        if (event.getAggregateId() == null) {
            throw new IllegalArgumentException("Aggregate ID cannot be null");
        }
    }
    
    @Override
    public <T extends DomainEvent> void registerHandler(Class<T> eventType, EventHandler<T> handler) {
        handlerRegistry.registerHandler(eventType, handler);
    }
    
    @Override
    public <T extends DomainEvent> void unregisterHandler(Class<T> eventType, EventHandler<T> handler) {
        handlerRegistry.unregisterHandler(eventType, handler);
    }
    
    @Override
    public boolean hasHandlers(Class<? extends DomainEvent> eventType) {
        return handlerRegistry.hasHandlers(eventType);
    }
    
    @Override
    public EventBusStatistics getStatistics() {
        return statistics;
    }
    
    @Override
    public boolean isHealthy() {
        return !shutdown && 
               executor != null && 
               !executor.getThreadPoolExecutor().isShutdown() &&
               statistics.getErrorRate() < 0.1; // Menos de 10% de erro
    }
    
    @Override
    public boolean shutdown(int timeoutSeconds) {
        log.info("Shutting down SimpleEventBus with timeout of {} seconds", timeoutSeconds);
        
        shutdown = true;
        
        if (executor != null) {
            executor.shutdown();
            try {
                return executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        return true;
    }
}
```

---

## 🚀 **KAFKA EVENT BUS**

### **📝 Implementação com Kafka**

Localização: `com.seguradora.hibrida.eventbus.impl.KafkaEventBus`

```java
@Component
@ConditionalOnProperty(name = "event-bus.type", havingValue = "kafka")
@Slf4j
public class KafkaEventBus implements EventBus, DisposableBean {
    
    private final EventHandlerRegistry handlerRegistry;
    private final EventBusStatistics statistics;
    private final EventBusProperties properties;
    private final ObjectMapper objectMapper;
    
    // Kafka components
    private KafkaProducer<String, String> producer;
    private final Map<String, KafkaConsumer<String, String>> consumers = new ConcurrentHashMap<>();
    private final ExecutorService consumerExecutor = Executors.newCachedThreadPool();
    
    // Controle de lifecycle
    private volatile boolean initialized = false;
    private volatile boolean shutdown = false;
    
    @PostConstruct
    public void initialize() {
        if (initialized) {
            return;
        }
        
        log.info("Initializing KafkaEventBus with bootstrap servers: {}", 
                properties.getKafka().getBootstrapServers());
        
        initializeProducer();
        initializeConsumers();
        
        initialized = true;
        log.info("KafkaEventBus initialized successfully");
    }
    
    private void initializeProducer() {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                         properties.getKafka().getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
                         StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
                         StringSerializer.class.getName());
        producerProps.put(ProducerConfig.ACKS_CONFIG, properties.getKafka().getAcks());
        producerProps.put(ProducerConfig.RETRIES_CONFIG, properties.getKafka().getRetries());
        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, properties.getKafka().getBatchSize());
        producerProps.put(ProducerConfig.LINGER_MS_CONFIG, properties.getKafka().getLingerMs());
        producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, properties.getKafka().getBufferMemory());
        
        this.producer = new KafkaProducer<>(producerProps);
    }
    
    private void initializeConsumers() {
        // Cria consumers para cada tipo de evento registrado
        Set<Class<? extends DomainEvent>> eventTypes = handlerRegistry.getRegisteredEventTypes();
        
        for (Class<? extends DomainEvent> eventType : eventTypes) {
            String topic = getTopicForEventType(eventType);
            createConsumerForTopic(topic);
        }
    }
    
    private void createConsumerForTopic(String topic) {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                         properties.getKafka().getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, 
                         properties.getKafka().getGroupId());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
                         StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
                         StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, 
                         properties.getKafka().getAutoOffsetReset());
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, 
                         properties.getKafka().isEnableAutoCommit());
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 
                         properties.getKafka().getSessionTimeoutMs());
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 
                         properties.getKafka().getMaxPollRecords());
        
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(topic));
        
        consumers.put(topic, consumer);
        
        // Inicia thread de consumo
        consumerExecutor.submit(() -> consumeEvents(topic, consumer));
        
        log.info("Created Kafka consumer for topic: {}", topic);
    }
    
    private void consumeEvents(String topic, KafkaConsumer<String, String> consumer) {
        log.info("Starting event consumption for topic: {}", topic);
        
        while (!shutdown) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    processKafkaRecord(record);
                }
                
            } catch (Exception e) {
                log.error("Error consuming events from topic {}: {}", topic, e.getMessage(), e);
                
                // Backoff em caso de erro
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.info("Stopped event consumption for topic: {}", topic);
    }
    
    private void processKafkaRecord(ConsumerRecord<String, String> record) {
        try {
            String eventType = record.headers().lastHeader("eventType").value().toString();
            String eventJson = record.value();
            
            DomainEvent event = deserializeEvent(eventJson, eventType);
            String correlationId = record.key();
            
            log.debug("Processing Kafka event: {} from topic: {}", 
                     event.getEventId(), record.topic());
            
            processEventWithHandlers(event, correlationId, 1);
            
        } catch (Exception e) {
            log.error("Error processing Kafka record from topic {}: {}", 
                     record.topic(), e.getMessage(), e);
        }
    }
    
    @Override
    public void publish(DomainEvent event) {
        CompletableFuture<Void> future = publishAsync(event);
        
        try {
            future.get(properties.getTimeout().getPublishTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new EventPublishingException(event, "Failed to publish event synchronously", e);
        }
    }
    
    @Override
    public CompletableFuture<Void> publishAsync(DomainEvent event) {
        validateEvent(event);
        
        if (!initialized) {
            return CompletableFuture.failedFuture(
                new EventBusException("KafkaEventBus not initialized"));
        }
        
        String correlationId = generateCorrelationId();
        
        return publishToKafka(event, correlationId)
            .thenRun(() -> {
                statistics.recordEventPublished(event.getEventType());
                log.debug("Successfully published event to Kafka: {}", event.getEventId());
            })
            .exceptionally(throwable -> {
                statistics.recordEventFailed(event.getEventType(), false);
                log.error("Failed to publish event to Kafka: {}", event.getEventId(), throwable);
                return null;
            });
    }
    
    private CompletableFuture<Void> publishToKafka(DomainEvent event, String correlationId) {
        try {
            String topic = getTopicForEventType(event.getClass());
            String eventJson = objectMapper.writeValueAsString(event);
            
            ProducerRecord<String, String> record = new ProducerRecord<>(
                topic, correlationId, eventJson);
            
            // Adiciona headers
            record.headers().add("eventType", event.getEventType().getBytes());
            record.headers().add("aggregateId", event.getAggregateId().getBytes());
            record.headers().add("timestamp", event.getTimestamp().toString().getBytes());
            
            CompletableFuture<Void> future = new CompletableFuture<>();
            
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    future.completeExceptionally(exception);
                } else {
                    log.debug("Event published to Kafka topic {} partition {} offset {}", 
                             metadata.topic(), metadata.partition(), metadata.offset());
                    future.complete(null);
                }
            });
            
            return future;
            
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    private String getTopicForEventType(Class<? extends DomainEvent> eventType) {
        // Convenção: nome da classe em lowercase com hífen
        return eventType.getSimpleName().toLowerCase().replaceAll("([a-z])([A-Z])", "$1-$2");
    }
    
    private DomainEvent deserializeEvent(String eventJson, String eventType) {
        try {
            Class<?> eventClass = Class.forName("com.seguradora.hibrida.eventbus.example." + eventType);
            return (DomainEvent) objectMapper.readValue(eventJson, eventClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event: " + eventType, e);
        }
    }
    
    // Implementação dos demais métodos similar ao SimpleEventBus...
    
    @Override
    @PreDestroy
    public void destroy() {
        shutdown(30);
    }
    
    @Override
    public boolean shutdown(int timeoutSeconds) {
        log.info("Shutting down KafkaEventBus");
        
        shutdown = true;
        
        // Fecha consumers
        consumers.values().forEach(KafkaConsumer::close);
        consumers.clear();
        
        // Fecha producer
        if (producer != null) {
            producer.close(Duration.ofSeconds(timeoutSeconds));
        }
        
        // Para executor de consumers
        consumerExecutor.shutdown();
        try {
            return consumerExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
```

---

## 🔧 **REGISTRO DE HANDLERS**

### **📝 EventHandlerRegistry**

Localização: `com.seguradora.hibrida.eventbus.EventHandlerRegistry`

```java
@Component
@Slf4j
public class EventHandlerRegistry {
    
    // Mapa de tipo de evento para lista de handlers
    private final Map<Class<? extends DomainEvent>, List<EventHandler<? extends DomainEvent>>> 
        handlersByEventType = new ConcurrentHashMap<>();
    
    // Cache para lookup rápido
    private final Map<String, List<EventHandler<? extends DomainEvent>>> 
        handlerCache = new ConcurrentHashMap<>();
    
    // Estatísticas
    private volatile long lastRegistrationTime = System.currentTimeMillis();
    
    /**
     * Registra um handler para um tipo específico de evento.
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void registerHandler(Class<T> eventType, EventHandler<T> handler) {
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(handler, "Handler cannot be null");
        
        log.info("Registering handler {} for event type {}", 
                handler.getClass().getSimpleName(), eventType.getSimpleName());
        
        handlersByEventType
            .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
            .add((EventHandler<? extends DomainEvent>) handler);
        
        // Limpa cache para forçar recálculo
        handlerCache.clear();
        lastRegistrationTime = System.currentTimeMillis();
        
        log.debug("Handler registered successfully. Total handlers for {}: {}", 
                 eventType.getSimpleName(), 
                 handlersByEventType.get(eventType).size());
    }
    
    /**
     * Remove um handler registrado.
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> boolean unregisterHandler(Class<T> eventType, EventHandler<T> handler) {
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(handler, "Handler cannot be null");
        
        List<EventHandler<? extends DomainEvent>> handlers = handlersByEventType.get(eventType);
        if (handlers != null) {
            boolean removed = handlers.remove(handler);
            if (removed) {
                log.info("Unregistered handler {} for event type {}", 
                        handler.getClass().getSimpleName(), eventType.getSimpleName());
                
                // Remove entrada se lista ficar vazia
                if (handlers.isEmpty()) {
                    handlersByEventType.remove(eventType);
                }
                
                // Limpa cache
                handlerCache.clear();
                return true;
            }
        }
        
        log.warn("Handler {} not found for event type {}", 
                handler.getClass().getSimpleName(), eventType.getSimpleName());
        return false;
    }
    
    /**
     * Obtém todos os handlers para um tipo de evento.
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> List<EventHandler<T>> getHandlers(Class<T> eventType) {
        String cacheKey = eventType.getName();
        
        List<EventHandler<? extends DomainEvent>> cachedHandlers = handlerCache.get(cacheKey);
        if (cachedHandlers != null) {
            return (List<EventHandler<T>>) cachedHandlers;
        }
        
        List<EventHandler<? extends DomainEvent>> handlers = 
            handlersByEventType.getOrDefault(eventType, Collections.emptyList());
        
        // Ordena por prioridade (maior primeiro)
        List<EventHandler<? extends DomainEvent>> sortedHandlers = handlers.stream()
            .sorted((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()))
            .collect(Collectors.toList());
        
        handlerCache.put(cacheKey, sortedHandlers);
        
        return (List<EventHandler<T>>) sortedHandlers;
    }
    
    /**
     * Verifica se existem handlers para um tipo de evento.
     */
    public boolean hasHandlers(Class<? extends DomainEvent> eventType) {
        return handlersByEventType.containsKey(eventType) && 
               !handlersByEventType.get(eventType).isEmpty();
    }
    
    /**
     * Obtém todos os tipos de evento registrados.
     */
    public Set<Class<? extends DomainEvent>> getRegisteredEventTypes() {
        return new HashSet<>(handlersByEventType.keySet());
    }
    
    /**
     * Obtém estatísticas do registry.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalEventTypes", getEventTypesCount());
        stats.put("totalHandlers", getTotalHandlers());
        stats.put("lastRegistrationTime", lastRegistrationTime);
        
        // Estatísticas por tipo de evento
        Map<String, Integer> handlersByType = new HashMap<>();
        handlersByEventType.forEach((eventType, handlers) -> 
            handlersByType.put(eventType.getSimpleName(), handlers.size()));
        stats.put("handlersByEventType", handlersByType);
        
        return stats;
    }
    
    /**
     * Valida configuração do registry.
     */
    public List<String> validateConfiguration() {
        List<String> issues = new ArrayList<>();
        
        // Verifica se há tipos de evento sem handlers
        for (Class<? extends DomainEvent> eventType : handlersByEventType.keySet()) {
            List<EventHandler<? extends DomainEvent>> handlers = handlersByEventType.get(eventType);
            if (handlers.isEmpty()) {
                issues.add("Event type " + eventType.getSimpleName() + " has no handlers");
            }
        }
        
        // Verifica handlers duplicados
        for (Map.Entry<Class<? extends DomainEvent>, List<EventHandler<? extends DomainEvent>>> entry : 
             handlersByEventType.entrySet()) {
            
            Set<Class<?>> handlerClasses = new HashSet<>();
            for (EventHandler<? extends DomainEvent> handler : entry.getValue()) {
                Class<?> handlerClass = handler.getClass();
                if (!handlerClasses.add(handlerClass)) {
                    issues.add("Duplicate handler " + handlerClass.getSimpleName() + 
                              " for event type " + entry.getKey().getSimpleName());
                }
            }
        }
        
        return issues;
    }
    
    /**
     * Limpa todos os handlers registrados.
     */
    public void clear() {
        log.info("Clearing all registered handlers");
        handlersByEventType.clear();
        handlerCache.clear();
    }
    
    // Métodos auxiliares
    public int getEventTypesCount() {
        return handlersByEventType.size();
    }
    
    public int getTotalHandlers() {
        return handlersByEventType.values().stream()
            .mapToInt(List::size)
            .sum();
    }
    
    public long getLastRegistrationTime() {
        return lastRegistrationTime;
    }
}
```

---

## ⚙️ **CONFIGURAÇÃO AUTOMÁTICA**

### **📝 EventBusConfiguration**

Localização: `com.seguradora.hibrida.eventbus.config.EventBusConfiguration`

```java
@Configuration
@EnableConfigurationProperties(EventBusProperties.class)
@ConditionalOnProperty(name = "event-bus.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class EventBusConfiguration {
    
    @Bean
    public EventHandlerRegistry eventHandlerRegistry() {
        return new EventHandlerRegistry();
    }
    
    @Bean
    @ConditionalOnProperty(name = "event-bus.type", havingValue = "simple", matchIfMissing = true)
    public EventBus eventBus(EventHandlerRegistry handlerRegistry) {
        log.info("Creating SimpleEventBus");
        return new SimpleEventBus(handlerRegistry);
    }
    
    @Bean
    @ConditionalOnProperty(name = "event-bus.type", havingValue = "kafka")
    public EventBus kafkaEventBus(EventHandlerRegistry handlerRegistry) {
        log.info("Creating KafkaEventBus");
        return new KafkaEventBus(handlerRegistry);
    }
    
    /**
     * Registra automaticamente todos os handlers encontrados no contexto.
     */
    @EventListener
    @Order(Ordered.LOWEST_PRECEDENCE)
    public void registerEventHandlers(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        EventHandlerRegistry registry = context.getBean(EventHandlerRegistry.class);
        
        log.info("Auto-registering event handlers...");
        
        // Busca todos os beans que implementam EventHandler
        Map<String, EventHandler> handlers = context.getBeansOfType(EventHandler.class);
        
        for (Map.Entry<String, EventHandler> entry : handlers.entrySet()) {
            EventHandler<? extends DomainEvent> handler = entry.getValue();
            
            try {
                Class<? extends DomainEvent> eventType = extractEventType(handler);
                registerHandlerSafely(registry, eventType, handler);
                
            } catch (Exception e) {
                log.error("Failed to register handler {}: {}", 
                         entry.getKey(), e.getMessage(), e);
            }
        }
        
        log.info("Auto-registration completed. Registered {} handlers for {} event types", 
                registry.getTotalHandlers(), registry.getEventTypesCount());
        
        // Valida configuração
        List<String> issues = registry.validateConfiguration();
        if (!issues.isEmpty()) {
            log.warn("Configuration issues found: {}", issues);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends DomainEvent> extractEventType(EventHandler<? extends DomainEvent> handler) {
        // Tenta obter do método getEventType() primeiro
        try {
            return handler.getEventType();
        } catch (Exception e) {
            log.debug("Could not get event type from getEventType() method, trying reflection");
        }
        
        // Fallback para reflexão
        return extractEventTypeFromGenericInterface(handler);
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends DomainEvent> extractEventTypeFromGenericInterface(
            EventHandler<? extends DomainEvent> handler) {
        
        Type[] genericInterfaces = handler.getClass().getGenericInterfaces();
        
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                
                if (EventHandler.class.equals(parameterizedType.getRawType())) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                        return (Class<? extends DomainEvent>) typeArguments[0];
                    }
                }
            }
        }
        
        throw new IllegalArgumentException(
            "Could not determine event type for handler: " + handler.getClass().getName());
    }
    
    @SuppressWarnings("unchecked")
    private <T extends DomainEvent> void registerHandlerSafely(
            EventHandlerRegistry registry, 
            Class<T> eventType, 
            EventHandler<? extends DomainEvent> handler) {
        
        try {
            registry.registerHandler(eventType, (EventHandler<T>) handler);
            log.debug("Successfully registered handler {} for event type {}", 
                     handler.getClass().getSimpleName(), eventType.getSimpleName());
                     
        } catch (Exception e) {
            log.error("Failed to register handler {} for event type {}: {}", 
                     handler.getClass().getSimpleName(), eventType.getSimpleName(), 
                     e.getMessage());
        }
    }
}
```

---

## 🎯 **EXERCÍCIOS PRÁTICOS**

### **📝 Exercício 1: Comparar Implementações**
Analise as diferenças entre `SimpleEventBus` e `KafkaEventBus`:
- Identifique vantagens e desvantagens de cada um
- Quando usar cada implementação
- Como configurar cada uma

### **📝 Exercício 2: Handler Customizado**
Crie um handler que:
- Processe eventos de múltiplos tipos
- Tenha lógica de retry customizada
- Implemente logging detalhado
- Configure prioridade e timeout específicos

### **📝 Exercício 3: Registry Avançado**
Implemente funcionalidades no `EventHandlerRegistry`:
- Cache com TTL para handlers
- Métricas de uso por handler
- Validação de dependências entre handlers

---

## 🔗 **PRÓXIMOS PASSOS**

Na **Parte 3** do Event Bus, abordaremos:
- **Configuração avançada** e propriedades
- **Tratamento de erros** e retry policies
- **Monitoramento** e métricas
- **Testes** de handlers e event bus

---

## 📚 **REFERÊNCIAS**

### **📖 Documentação Técnica**
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Event-Driven Microservices](https://microservices.io/patterns/data/event-driven-architecture.html)

### **🔧 Código de Referência**
- `SimpleEventBus.java` - Implementação simples
- `KafkaEventBus.java` - Implementação com Kafka
- `EventHandlerRegistry.java` - Registro de handlers
- `EventBusConfiguration.java` - Configuração automática

---

**📘 Capítulo:** 06 - Event Bus - Parte 2  
**⏱️ Tempo Estimado:** 50 minutos  
**🎯 Próximo:** [06 - Event Bus - Parte 3](./06-event-bus-parte-3.md)  
**📋 Checklist:** Implementações ✅ | Registro ✅ | Configuração ✅