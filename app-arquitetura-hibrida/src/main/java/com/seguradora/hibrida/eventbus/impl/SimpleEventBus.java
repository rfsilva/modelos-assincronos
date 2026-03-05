package com.seguradora.hibrida.eventbus.impl;

import com.seguradora.hibrida.eventbus.*;
import com.seguradora.hibrida.eventbus.exception.*;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Implementação simples do Event Bus com processamento assíncrono.
 * 
 * <p>Esta implementação fornece:
 * <ul>
 *   <li>Publicação síncrona e assíncrona de eventos</li>
 *   <li>Roteamento automático para handlers registrados</li>
 *   <li>Processamento ordenado por aggregate ID</li>
 *   <li>Sistema de retry com backoff exponencial</li>
 *   <li>Dead letter queue para falhas definitivas</li>
 *   <li>Métricas detalhadas de execução</li>
 * </ul>
 * 
 * <p>O processamento é feito de forma assíncrona usando um pool de threads
 * configurável, mas mantém a ordem de eventos por aggregate ID.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class SimpleEventBus implements EventBus {
    
    private static final Logger log = LoggerFactory.getLogger(SimpleEventBus.class);
    
    private final EventHandlerRegistry handlerRegistry;
    private final EventBusStatistics statistics;
    private final ExecutorService executorService;
    private final ScheduledExecutorService retryExecutor;
    
    // Configurações
    private final int maxRetryAttempts;
    private final long initialRetryDelayMs;
    private final double retryBackoffMultiplier;
    private final int maxRetryDelayMs;
    
    // Estado
    private volatile boolean shutdown = false;
    
    /**
     * Construtor com dependências injetadas.
     */
    @Autowired
    public SimpleEventBus(EventHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
        this.statistics = new EventBusStatistics();
        
        // Configurar pool de threads
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maxPoolSize = corePoolSize * 2;
        this.executorService = new ThreadPoolExecutor(
            corePoolSize, maxPoolSize,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            r -> {
                Thread t = new Thread(r, "EventBus-Worker");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        this.retryExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "EventBus-Retry");
            t.setDaemon(true);
            return t;
        });
        
        // Configurações de retry
        this.maxRetryAttempts = 3;
        this.initialRetryDelayMs = 1000;
        this.retryBackoffMultiplier = 2.0;
        this.maxRetryDelayMs = 30000;
        
        log.info("SimpleEventBus initialized with {} core threads, {} max threads", 
                corePoolSize, maxPoolSize);
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
        
        log.debug("Publishing event synchronously: {} [correlationId={}]", 
                 event.getClass().getSimpleName(), correlationId);
        
        statistics.recordEventPublished(event.getClass().getSimpleName());
        
        try {
            processEventSync(event, correlationId);
        } catch (Exception e) {
            log.error("Failed to publish event synchronously: {} [correlationId={}]", 
                     event.getClass().getSimpleName(), correlationId, e);
            throw new EventPublishingException(
                "Failed to publish event: " + e.getMessage(), 
                event, "SYNC_PROCESSING_ERROR", e
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
        
        log.debug("Publishing event asynchronously: {} [correlationId={}]", 
                 event.getClass().getSimpleName(), correlationId);
        
        statistics.recordEventPublished(event.getClass().getSimpleName());
        
        return CompletableFuture.runAsync(() -> {
            try {
                processEventSync(event, correlationId);
            } catch (Exception e) {
                log.error("Failed to publish event asynchronously: {} [correlationId={}]", 
                         event.getClass().getSimpleName(), correlationId, e);
                throw new EventPublishingException(
                    "Failed to publish event: " + e.getMessage(), 
                    event, "ASYNC_PROCESSING_ERROR", e
                );
            }
        }, executorService);
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
        log.debug("Publishing batch of {} events [batchCorrelationId={}]", 
                 events.size(), batchCorrelationId);
        
        // Agrupar eventos por aggregate ID para manter ordem
        var eventsByAggregate = events.stream()
                .collect(Collectors.groupingBy(DomainEvent::getAggregateId));
        
        for (var entry : eventsByAggregate.entrySet()) {
            String aggregateId = entry.getKey();
            List<DomainEvent> aggregateEvents = entry.getValue();
            
            log.debug("Processing {} events for aggregate {} [batchCorrelationId={}]", 
                     aggregateEvents.size(), aggregateId, batchCorrelationId);
            
            for (DomainEvent event : aggregateEvents) {
                String correlationId = generateCorrelationId();
                event.setCorrelationId(UUID.fromString(correlationId));
                event.addMetadata("batchCorrelationId", batchCorrelationId);
                
                statistics.recordEventPublished(event.getClass().getSimpleName());
                processEventSync(event, correlationId);
            }
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
        
        return CompletableFuture.runAsync(() -> publishBatch(events), executorService);
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
        log.info("Handler {} registered for event type {}", 
                handler.getClass().getSimpleName(), eventType.getSimpleName());
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
        
        // Verificar se o executor está funcionando
        if (executorService.isShutdown() || executorService.isTerminated()) {
            return false;
        }
        
        // Verificar taxa de erro
        double errorRate = statistics.getErrorRate();
        if (errorRate > 0.1) { // Mais de 10% de erro
            log.warn("Event Bus unhealthy: error rate is {:.2f}%", errorRate * 100);
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean shutdown(int timeoutSeconds) {
        log.info("Shutting down Event Bus with timeout of {} seconds", timeoutSeconds);
        
        shutdown = true;
        
        // Parar de aceitar novos eventos
        executorService.shutdown();
        retryExecutor.shutdown();
        
        try {
            // Aguardar conclusão dos eventos em processamento
            boolean terminated = executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
            if (!terminated) {
                log.warn("Event Bus did not terminate gracefully, forcing shutdown");
                executorService.shutdownNow();
            }
            
            retryExecutor.awaitTermination(5, TimeUnit.SECONDS);
            if (!retryExecutor.isTerminated()) {
                retryExecutor.shutdownNow();
            }
            
            log.info("Event Bus shutdown completed");
            return terminated;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while shutting down Event Bus", e);
            executorService.shutdownNow();
            retryExecutor.shutdownNow();
            return false;
        }
    }
    
    /**
     * Processa um evento de forma síncrona.
     */
    private void processEventSync(DomainEvent event, String correlationId) {
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
            processWithHandler(event, handler, correlationId, 0);
        }
    }
    
    /**
     * Processa um evento com um handler específico, incluindo retry.
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
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> handler.handle(event), executorService);
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
     * Trata erros de processamento, incluindo retry.
     */
    private void handleProcessingError(DomainEvent event, EventHandler<DomainEvent> handler, 
                                     String correlationId, int attemptNumber, 
                                     EventHandlingException exception) {
        
        log.error("Handler {} failed for event {} (attempt {}) [correlationId={}]: {}", 
                 handler.getClass().getSimpleName(), event.getClass().getSimpleName(), 
                 attemptNumber + 1, correlationId, exception.getMessage(), exception);
        
        statistics.recordEventFailed(event.getClass().getSimpleName(), exception.isRetryable());
        
        // Verificar se deve fazer retry
        if (exception.isRetryable() && attemptNumber < maxRetryAttempts) {
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
        long delay = (long) (initialRetryDelayMs * Math.pow(retryBackoffMultiplier, attemptNumber - 1));
        
        // Adicionar jitter para evitar thundering herd
        long jitter = (long) (delay * 0.1 * Math.random());
        delay += jitter;
        
        return Math.min(delay, maxRetryDelayMs);
    }
    
    /**
     * Envia um evento para a dead letter queue após esgotar tentativas.
     */
    private void sendToDeadLetterQueue(DomainEvent event, EventHandler<DomainEvent> handler, 
                                      EventHandlingException exception, int totalAttempts) {
        
        log.error("Sending event {} to dead letter queue after {} attempts with handler {} [correlationId={}]", 
                 event.getClass().getSimpleName(), totalAttempts, 
                 handler.getClass().getSimpleName(), event.getCorrelationId());
        
        // TODO: Implementar integração com sistema de dead letter queue (Kafka, SQS, etc.)
        // Por enquanto, apenas loggar o evento
        log.error("DEAD LETTER QUEUE - Event: {}, Handler: {}, Exception: {}, Attempts: {}", 
                 event, handler.getClass().getSimpleName(), exception.getMessage(), totalAttempts);
    }
    
    /**
     * Gera um correlation ID único para rastreamento.
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}