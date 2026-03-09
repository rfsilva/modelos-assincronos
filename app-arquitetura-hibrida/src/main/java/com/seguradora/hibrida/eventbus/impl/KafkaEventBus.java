package com.seguradora.hibrida.eventbus.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguradora.hibrida.eventbus.*;
import com.seguradora.hibrida.eventbus.config.EventBusProperties;
import com.seguradora.hibrida.eventbus.exception.*;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Implementação do Event Bus com integração Kafka para processamento assíncrono.
 * 
 * <p>Esta implementação fornece:
 * <ul>
 *   <li>Publicação assíncrona via Kafka com alta performance</li>
 *   <li>Particionamento por aggregate ID para ordenação</li>
 *   <li>Processamento paralelo com controle de concorrência</li>
 *   <li>Sistema de retry com dead letter queue</li>
 *   <li>Métricas detalhadas de throughput e latência</li>
 *   <li>Balanceamento automático de carga entre consumers</li>
 * </ul>
 * 
 * <p>O processamento mantém a ordem de eventos por aggregate ID através
 * do particionamento do Kafka, garantindo consistência de domínio.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(name = "event-bus.kafka.enabled", havingValue = "true")
public class KafkaEventBus implements EventBus {
    
    private static final Logger log = LoggerFactory.getLogger(KafkaEventBus.class);
    
    private final EventHandlerRegistry handlerRegistry;
    private final EventBusStatistics statistics;
    private final EventBusProperties properties;
    private final ObjectMapper objectMapper;
    
    // Kafka components
    private KafkaProducer<String, String> producer;
    private final Map<String, KafkaConsumer<String, String>> consumers = new ConcurrentHashMap<>();
    private final ExecutorService consumerExecutor;
    private final ScheduledExecutorService retryExecutor;
    
    // Estado
    private volatile boolean shutdown = false;
    private final Set<String> activeTopics = ConcurrentHashMap.newKeySet();
    
    /**
     * Construtor com dependências injetadas.
     */
    @Autowired
    public KafkaEventBus(EventHandlerRegistry handlerRegistry, 
                        EventBusProperties properties,
                        ObjectMapper objectMapper) {
        this.handlerRegistry = handlerRegistry;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.statistics = new EventBusStatistics();
        
        // Configurar executors
        int consumerThreads = properties.getThreadPool().getCoreSize();
        this.consumerExecutor = Executors.newFixedThreadPool(consumerThreads, r -> {
            Thread t = new Thread(r, "KafkaEventBus-Consumer");
            t.setDaemon(true);
            return t;
        });
        
        this.retryExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "KafkaEventBus-Retry");
            t.setDaemon(true);
            return t;
        });
        
        log.info("KafkaEventBus initialized with {} consumer threads", consumerThreads);
    }
    
    @PostConstruct
    public void initialize() {
        if (!properties.getKafka().isEnabled()) {
            log.info("Kafka integration is disabled");
            return;
        }
        
        log.info("Initializing Kafka Event Bus...");
        
        // Configurar producer
        initializeProducer();
        
        // Inicializar consumers para tipos de eventos registrados
        initializeConsumers();
        
        log.info("Kafka Event Bus initialized successfully");
    }
    
    @PreDestroy
    public void destroy() {
        shutdown(30);
    }
    
    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        if (shutdown) {
            throw new EventBusException("Event Bus is shutdown");
        }
        
        String correlationId = generateCorrelationId();
        event.setCorrelationId(UUID.fromString(correlationId));
        
        log.debug("Publishing event synchronously to Kafka: {} [correlationId={}]", 
                 event.getClass().getSimpleName(), correlationId);
        
        statistics.recordEventPublished(event.getClass().getSimpleName());
        
        try {
            publishToKafka(event, correlationId).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to publish event synchronously: {} [correlationId={}]", 
                     event.getClass().getSimpleName(), correlationId, e);
            throw new EventPublishingException(
                "Failed to publish event to Kafka: " + e.getMessage(), 
                event, "KAFKA_SYNC_ERROR", e
            );
        }
    }
    
    @Override
    public CompletableFuture<Void> publishAsync(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        if (shutdown) {
            throw new EventBusException("Event Bus is shutdown");
        }
        
        String correlationId = generateCorrelationId();
        event.setCorrelationId(UUID.fromString(correlationId));
        
        log.debug("Publishing event asynchronously to Kafka: {} [correlationId={}]", 
                 event.getClass().getSimpleName(), correlationId);
        
        statistics.recordEventPublished(event.getClass().getSimpleName());
        
        return publishToKafka(event, correlationId)
                .exceptionally(throwable -> {
                    log.error("Failed to publish event asynchronously: {} [correlationId={}]", 
                             event.getClass().getSimpleName(), correlationId, throwable);
                    throw new EventPublishingException(
                        "Failed to publish event to Kafka: " + throwable.getMessage(), 
                        event, "KAFKA_ASYNC_ERROR", throwable
                    );
                });
    }
    
    @Override
    public void publishBatch(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("Events list cannot be null or empty");
        }
        
        if (shutdown) {
            throw new EventBusException("Event Bus is shutdown");
        }
        
        String batchCorrelationId = generateCorrelationId();
        log.debug("Publishing batch of {} events to Kafka [batchCorrelationId={}]", 
                 events.size(), batchCorrelationId);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (DomainEvent event : events) {
            String correlationId = generateCorrelationId();
            event.setCorrelationId(UUID.fromString(correlationId));
            event.addMetadata("batchCorrelationId", batchCorrelationId);
            
            statistics.recordEventPublished(event.getClass().getSimpleName());
            futures.add(publishToKafka(event, correlationId));
        }
        
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to publish batch [batchCorrelationId={}]", batchCorrelationId, e);
            throw new EventPublishingException(
                "Failed to publish batch to Kafka: " + e.getMessage(), 
                null, "KAFKA_BATCH_ERROR", e
            );
        }
    }
    
    @Override
    public CompletableFuture<Void> publishBatchAsync(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("Events list cannot be null or empty");
        }
        
        if (shutdown) {
            throw new EventBusException("Event Bus is shutdown");
        }
        
        return CompletableFuture.runAsync(() -> publishBatch(events), consumerExecutor);
    }
    
    @Override
    public <T extends DomainEvent> void registerHandler(Class<T> eventType, EventHandler<T> handler) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null");
        }
        
        handlerRegistry.registerHandler(eventType, handler);
        
        // Criar consumer para este tipo de evento se não existir
        String topic = getTopicForEventType(eventType);
        if (!consumers.containsKey(topic)) {
            createConsumerForTopic(topic);
        }
        
        log.info("Handler {} registered for event type {} on topic {}", 
                handler.getClass().getSimpleName(), eventType.getSimpleName(), topic);
    }
    
    @Override
    public <T extends DomainEvent> void unregisterHandler(Class<T> eventType, EventHandler<T> handler) {
        if (eventType == null || handler == null) {
            return;
        }
        
        boolean removed = handlerRegistry.unregisterHandler(eventType, handler);
        if (removed) {
            log.info("Handler {} unregistered for event type {}", 
                    handler.getClass().getSimpleName(), eventType.getSimpleName());
        }
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
        if (shutdown) {
            return false;
        }
        
        // Verificar se o producer está funcionando
        if (producer == null) {
            return false;
        }
        
        // Verificar taxa de erro
        double errorRate = statistics.getErrorRate();
        if (errorRate > properties.getMonitoring().getErrorRateThreshold()) {
            log.warn("Kafka Event Bus unhealthy: error rate is {:.2f}%", errorRate * 100);
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean shutdown(int timeoutSeconds) {
        log.info("Shutting down Kafka Event Bus with timeout of {} seconds", timeoutSeconds);
        
        shutdown = true;
        
        try {
            // Parar consumers
            for (KafkaConsumer<String, String> consumer : consumers.values()) {
                consumer.wakeup();
            }
            
            // Parar executors
            consumerExecutor.shutdown();
            retryExecutor.shutdown();
            
            boolean terminated = consumerExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
            if (!terminated) {
                log.warn("Consumer executor did not terminate gracefully, forcing shutdown");
                consumerExecutor.shutdownNow();
            }
            
            retryExecutor.awaitTermination(5, TimeUnit.SECONDS);
            if (!retryExecutor.isTerminated()) {
                retryExecutor.shutdownNow();
            }
            
            // Fechar producer
            if (producer != null) {
                producer.close(Duration.ofSeconds(10));
            }
            
            // Fechar consumers
            for (KafkaConsumer<String, String> consumer : consumers.values()) {
                consumer.close(Duration.ofSeconds(5));
            }
            
            log.info("Kafka Event Bus shutdown completed");
            return terminated;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while shutting down Kafka Event Bus", e);
            consumerExecutor.shutdownNow();
            retryExecutor.shutdownNow();
            return false;
        }
    }
    
    /**
     * Inicializa o producer Kafka.
     */
    private void initializeProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, properties.getKafka().getProducer().getAcks());
        props.put(ProducerConfig.RETRIES_CONFIG, properties.getKafka().getProducer().getRetries());
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, properties.getKafka().getProducer().getBatchSize());
        props.put(ProducerConfig.LINGER_MS_CONFIG, properties.getKafka().getProducer().getLingerMs());
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, properties.getKafka().getProducer().getBufferMemory());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        this.producer = new KafkaProducer<>(props);
        
        log.info("Kafka producer initialized with servers: {}", properties.getKafka().getBootstrapServers());
    }
    
    /**
     * Inicializa consumers para tipos de eventos registrados.
     */
    private void initializeConsumers() {
        // Criar consumer para tópico padrão
        String defaultTopic = properties.getKafka().getDefaultTopic();
        createConsumerForTopic(defaultTopic);
        
        log.info("Kafka consumers initialized");
    }
    
    /**
     * Cria um consumer para um tópico específico.
     */
    private void createConsumerForTopic(String topic) {
        if (consumers.containsKey(topic)) {
            return;
        }
        
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getKafka().getConsumer().getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getKafka().getConsumer().getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.getKafka().getConsumer().isEnableAutoCommit());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getKafka().getConsumer().getMaxPollRecords());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, properties.getKafka().getConsumer().getSessionTimeoutMs());
        
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        
        consumers.put(topic, consumer);
        activeTopics.add(topic);
        
        // Iniciar thread de consumo
        consumerExecutor.submit(() -> consumeEvents(topic, consumer));
        
        log.info("Created Kafka consumer for topic: {}", topic);
    }
    
    /**
     * Publica um evento para o Kafka.
     */
    private CompletableFuture<Void> publishToKafka(DomainEvent event, String correlationId) {
        String topic = getTopicForEventType(event.getClass());
        String key = event.getAggregateId(); // Usar aggregate ID como chave para particionamento
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, eventJson);
            record.headers().add("eventType", event.getClass().getSimpleName().getBytes());
            record.headers().add("correlationId", correlationId.getBytes());
            record.headers().add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes());
            
            CompletableFuture<Void> future = new CompletableFuture<>();
            
            producer.send(record, (RecordMetadata metadata, Exception exception) -> {
                if (exception != null) {
                    log.error("Failed to send event {} to Kafka topic {} [correlationId={}]", 
                             event.getClass().getSimpleName(), topic, correlationId, exception);
                    future.completeExceptionally(exception);
                } else {
                    log.debug("Event {} sent to Kafka topic {} partition {} offset {} [correlationId={}]", 
                             event.getClass().getSimpleName(), metadata.topic(), 
                             metadata.partition(), metadata.offset(), correlationId);
                    future.complete(null);
                }
            });
            
            return future;
            
        } catch (Exception e) {
            log.error("Failed to serialize event {} [correlationId={}]", 
                     event.getClass().getSimpleName(), correlationId, e);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Consome eventos de um tópico específico.
     */
    private void consumeEvents(String topic, KafkaConsumer<String, String> consumer) {
        log.info("Starting event consumption for topic: {}", topic);
        
        try {
            while (!shutdown) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    processKafkaRecord(record);
                }
                
                if (!records.isEmpty() && !properties.getKafka().getConsumer().isEnableAutoCommit()) {
                    consumer.commitSync();
                }
            }
        } catch (Exception e) {
            if (!shutdown) {
                log.error("Error consuming events from topic {}", topic, e);
            }
        } finally {
            log.info("Stopped consuming events from topic: {}", topic);
        }
    }
    
    /**
     * Processa um record do Kafka.
     */
    private void processKafkaRecord(ConsumerRecord<String, String> record) {
        String eventTypeHeader = new String(record.headers().lastHeader("eventType").value());
        String correlationId = new String(record.headers().lastHeader("correlationId").value());
        
        log.debug("Processing Kafka record: eventType={}, partition={}, offset={} [correlationId={}]", 
                 eventTypeHeader, record.partition(), record.offset(), correlationId);
        
        try {
            // Deserializar evento
            DomainEvent event = deserializeEvent(record.value(), eventTypeHeader);
            
            // Processar com handlers
            processEventWithHandlers(event, correlationId, 0);
            
        } catch (Exception e) {
            log.error("Failed to process Kafka record: eventType={}, partition={}, offset={} [correlationId={}]", 
                     eventTypeHeader, record.partition(), record.offset(), correlationId, e);
            
            // TODO: Enviar para dead letter queue
        }
    }
    
    /**
     * Processa um evento com todos os handlers registrados.
     */
    private void processEventWithHandlers(DomainEvent event, String correlationId, int attemptNumber) {
        Class<? extends DomainEvent> eventType = event.getClass();
        List<EventHandler<DomainEvent>> handlers = handlerRegistry.getHandlers((Class<DomainEvent>) eventType);
        
        if (handlers.isEmpty()) {
            log.debug("No handlers found for event type: {} [correlationId={}]", 
                     eventType.getSimpleName(), correlationId);
            return;
        }
        
        log.debug("Processing event {} with {} handlers [correlationId={}]", 
                 eventType.getSimpleName(), handlers.size(), correlationId);
        
        for (EventHandler<DomainEvent> handler : handlers) {
            processWithHandler(event, handler, correlationId, attemptNumber);
        }
    }
    
    /**
     * Processa um evento com um handler específico.
     */
    private void processWithHandler(DomainEvent event, EventHandler<DomainEvent> handler, 
                                   String correlationId, int attemptNumber) {
        
        if (!handler.supports(event)) {
            log.debug("Handler {} does not support event {} [correlationId={}]", 
                     handler.getClass().getSimpleName(), event.getClass().getSimpleName(), correlationId);
            return;
        }
        
        statistics.recordHandlerStarted();
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Executing handler {} for event {} (attempt {}) [correlationId={}]", 
                     handler.getClass().getSimpleName(), event.getClass().getSimpleName(), 
                     attemptNumber + 1, correlationId);
            
            if (handler.isAsync()) {
                // Executar de forma assíncrona com timeout
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> handler.handle(event), consumerExecutor);
                future.get(handler.getTimeoutSeconds(), TimeUnit.SECONDS);
            } else {
                // Executar de forma síncrona
                handler.handle(event);
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            statistics.recordEventProcessed(event.getClass().getSimpleName(), processingTime);
            
            log.debug("Handler {} completed successfully for event {} in {}ms [correlationId={}]", 
                     handler.getClass().getSimpleName(), event.getClass().getSimpleName(), 
                     processingTime, correlationId);
            
        } catch (TimeoutException e) {
            long actualTime = System.currentTimeMillis() - startTime;
            handleProcessingError(event, handler, correlationId, attemptNumber, 
                new EventHandlerTimeoutException(
                    "Handler timeout exceeded", event, handler.getClass().getSimpleName(),
                    handler.getTimeoutSeconds(), actualTime
                )
            );
            
        } catch (Exception e) {
            handleProcessingError(event, handler, correlationId, attemptNumber, 
                new EventHandlingException(
                    "Handler execution failed: " + e.getMessage(), 
                    event, handler.getClass().getSimpleName(), handler.isRetryable(), e
                )
            );
            
        } finally {
            statistics.recordHandlerFinished();
        }
    }
    
    /**
     * Trata erros de processamento com retry.
     */
    private void handleProcessingError(DomainEvent event, EventHandler<DomainEvent> handler, 
                                     String correlationId, int attemptNumber, 
                                     EventHandlingException exception) {
        
        log.error("Handler {} failed for event {} (attempt {}) [correlationId={}]: {}", 
                 handler.getClass().getSimpleName(), event.getClass().getSimpleName(), 
                 attemptNumber + 1, correlationId, exception.getMessage(), exception);
        
        statistics.recordEventFailed(event.getClass().getSimpleName(), exception.isRetryable());
        
        // Verificar se deve fazer retry
        if (exception.isRetryable() && attemptNumber < properties.getRetry().getMaxAttempts()) {
            scheduleRetry(event, handler, correlationId, attemptNumber + 1);
        } else {
            // Enviar para dead letter queue
            sendToDeadLetterQueue(event, handler, exception, attemptNumber + 1);
        }
    }
    
    /**
     * Agenda um retry para um evento que falhou.
     */
    private void scheduleRetry(DomainEvent event, EventHandler<DomainEvent> handler, 
                              String correlationId, int attemptNumber) {
        
        long delay = calculateRetryDelay(attemptNumber);
        
        log.info("Scheduling retry {} for event {} with handler {} in {}ms [correlationId={}]", 
                attemptNumber, event.getClass().getSimpleName(), 
                handler.getClass().getSimpleName(), delay, correlationId);
        
        statistics.recordEventRetried(event.getClass().getSimpleName());
        
        retryExecutor.schedule(() -> {
            processWithHandler(event, handler, correlationId, attemptNumber);
        }, delay, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Calcula o delay para retry com backoff exponencial.
     */
    private long calculateRetryDelay(int attemptNumber) {
        long delay = (long) (properties.getRetry().getInitialDelayMs() * 
                            Math.pow(properties.getRetry().getBackoffMultiplier(), attemptNumber - 1));
        
        // Adicionar jitter para evitar thundering herd
        long jitter = (long) (delay * properties.getRetry().getJitterPercent() * Math.random());
        delay += jitter;
        
        return Math.min(delay, properties.getRetry().getMaxDelayMs());
    }
    
    /**
     * Envia um evento para a dead letter queue.
     */
    private void sendToDeadLetterQueue(DomainEvent event, EventHandler<DomainEvent> handler, 
                                      EventHandlingException exception, int totalAttempts) {
        
        log.error("Sending event {} to dead letter queue after {} attempts with handler {} [correlationId={}]", 
                 event.getClass().getSimpleName(), totalAttempts, 
                 handler.getClass().getSimpleName(), event.getCorrelationId());
        
        // Publicar para tópico de dead letter queue
        String dlqTopic = getTopicForEventType(event.getClass()) + "-dlq";
        
        try {
            Map<String, Object> dlqEvent = new HashMap<>();
            dlqEvent.put("originalEvent", event);
            dlqEvent.put("handlerClass", handler.getClass().getSimpleName());
            dlqEvent.put("exception", exception.getMessage());
            dlqEvent.put("totalAttempts", totalAttempts);
            dlqEvent.put("timestamp", System.currentTimeMillis());
            
            String dlqJson = objectMapper.writeValueAsString(dlqEvent);
            
            ProducerRecord<String, String> dlqRecord = new ProducerRecord<>(dlqTopic, event.getAggregateId(), dlqJson);
            producer.send(dlqRecord);
            
            log.info("Event {} sent to dead letter queue topic {}", 
                    event.getClass().getSimpleName(), dlqTopic);
            
        } catch (Exception e) {
            log.error("Failed to send event to dead letter queue", e);
        }
    }
    
    /**
     * Deserializa um evento a partir do JSON.
     */
    private DomainEvent deserializeEvent(String eventJson, String eventType) throws Exception {
        // TODO: Implementar deserialização baseada no tipo do evento
        // Por enquanto, usar deserialização genérica
        return objectMapper.readValue(eventJson, DomainEvent.class);
    }
    
    /**
     * Obtém o tópico para um tipo de evento.
     */
    private String getTopicForEventType(Class<? extends DomainEvent> eventType) {
        // Por padrão, usar o nome da classe como tópico
        // Pode ser customizado com anotações ou configuração
        return eventType.getSimpleName().toLowerCase().replace("event", "");
    }
    
    /**
     * Gera um correlation ID único.
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}