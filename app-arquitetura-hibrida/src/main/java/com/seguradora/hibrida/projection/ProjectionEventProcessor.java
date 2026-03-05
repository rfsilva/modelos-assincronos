package com.seguradora.hibrida.projection;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.projection.tracking.ProjectionTracker;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Processador de eventos para projeções.
 * 
 * <p>Responsável por:
 * <ul>
 *   <li>Processar eventos de domínio de forma assíncrona</li>
 *   <li>Rotear eventos para handlers apropriados</li>
 *   <li>Controlar ordem de processamento por aggregate</li>
 *   <li>Implementar retry policy para falhas</li>
 *   <li>Manter tracking de posição</li>
 * </ul>
 */
@Component
public class ProjectionEventProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(ProjectionEventProcessor.class);
    
    private final ProjectionRegistry projectionRegistry;
    private ProjectionTrackerRepository trackerRepository; // Será injetado via setter
    
    public ProjectionEventProcessor(ProjectionRegistry projectionRegistry) {
        this.projectionRegistry = projectionRegistry;
    }
    
    // Setter para injeção do repository (evita dependência circular)
    public void setTrackerRepository(ProjectionTrackerRepository trackerRepository) {
        this.trackerRepository = trackerRepository;
    }
    
    /**
     * Processa um evento de domínio de forma síncrona.
     * 
     * @param event Evento a ser processado
     * @param eventId ID sequencial do evento
     */
    @Transactional("writeTransactionManager")
    public void processEvent(DomainEvent event, Long eventId) {
        log.debug("Processando evento {} (ID: {}) para aggregate {}", 
                 event.getEventType(), eventId, event.getAggregateId());
        
        @SuppressWarnings("unchecked")
        List<ProjectionHandler<DomainEvent>> handlers = 
            (List<ProjectionHandler<DomainEvent>>) (List<?>) projectionRegistry.getHandlers(event.getClass());
        
        if (handlers.isEmpty()) {
            log.debug("Nenhum handler encontrado para evento {}", event.getEventType());
            return;
        }
        
        for (ProjectionHandler<DomainEvent> handler : handlers) {
            processWithHandler(event, eventId, handler, 1);
        }
    }
    
    /**
     * Processa um evento de domínio de forma assíncrona.
     * 
     * @param event Evento a ser processado
     * @param eventId ID sequencial do evento
     * @return CompletableFuture para acompanhar o processamento
     */
    @Async("projectionTaskExecutor")
    public CompletableFuture<Void> processEventAsync(DomainEvent event, Long eventId) {
        try {
            processEvent(event, eventId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Erro no processamento assíncrono do evento {} (ID: {}): {}", 
                     event.getEventType(), eventId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Processa uma lista de eventos em lote.
     * 
     * @param events Lista de eventos com seus IDs
     */
    @Transactional("writeTransactionManager")
    public void processBatch(List<EventWithId> events) {
        log.debug("Processando lote de {} eventos", events.size());
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int errorCount = 0;
        
        for (EventWithId eventWithId : events) {
            try {
                processEvent(eventWithId.event(), eventWithId.eventId());
                successCount++;
            } catch (Exception e) {
                errorCount++;
                log.error("Erro ao processar evento {} no lote: {}", 
                         eventWithId.event().getEventType(), e.getMessage(), e);
            }
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        log.info("Lote processado: {} sucessos, {} erros em {}ms", 
                successCount, errorCount, processingTime);
    }
    
    /**
     * Processa um evento com um handler específico, incluindo retry.
     */
    private void processWithHandler(DomainEvent event, Long eventId, 
                                  ProjectionHandler<DomainEvent> handler, 
                                  int attemptNumber) {
        
        String projectionName = handler.getProjectionName();
        
        // Se não temos repository, apenas processa sem tracking
        if (trackerRepository == null) {
            log.debug("Tracker repository não disponível, processando sem tracking");
            processWithoutTracking(event, handler);
            return;
        }
        
        ProjectionTracker tracker = getOrCreateTracker(projectionName);
        
        // Verificar se a projeção está ativa
        if (tracker.getStatus() == ProjectionStatus.DISABLED) {
            log.debug("Projeção {} está desabilitada, ignorando evento", projectionName);
            return;
        }
        
        // Verificar se o evento já foi processado (idempotência)
        if (eventId <= tracker.getLastProcessedEventId()) {
            log.debug("Evento {} já foi processado pela projeção {}, ignorando", 
                     eventId, projectionName);
            return;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Verificar se o handler suporta o evento
            if (!handler.supports(event)) {
                log.debug("Handler {} não suporta evento {}, ignorando", 
                         projectionName, event.getEventType());
                return;
            }
            
            // Processar o evento com timeout
            processWithTimeout(handler, event);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Atualizar posição do tracker
            tracker.updatePosition(eventId);
            trackerRepository.save(tracker);
            
            log.debug("Evento {} processado com sucesso pela projeção {} em {}ms", 
                     eventId, projectionName, processingTime);
            
        } catch (Exception e) {
            handleProcessingError(event, eventId, handler, tracker, attemptNumber, e);
        }
    }
    
    /**
     * Processa evento sem tracking (fallback quando repository não está disponível).
     */
    private void processWithoutTracking(DomainEvent event, ProjectionHandler<DomainEvent> handler) {
        try {
            if (handler.supports(event)) {
                handler.handle(event);
                log.debug("Evento {} processado sem tracking pela projeção {}", 
                         event.getEventType(), handler.getProjectionName());
            }
        } catch (Exception e) {
            log.error("Erro ao processar evento {} sem tracking na projeção {}: {}", 
                     event.getEventType(), handler.getProjectionName(), e.getMessage(), e);
        }
    }
    
    /**
     * Processa evento com timeout configurável.
     */
    private void processWithTimeout(ProjectionHandler<DomainEvent> handler, 
                                  DomainEvent event) throws Exception {
        
        if (handler.getTimeoutSeconds() <= 0) {
            // Sem timeout, processar diretamente
            handler.handle(event);
            return;
        }
        
        // Processar com timeout
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                handler.handle(event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        try {
            future.get(handler.getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            throw new ProjectionException(
                "Timeout ao processar evento " + event.getEventType(), 
                handler.getProjectionName(), e);
        }
    }
    
    /**
     * Trata erros de processamento com retry policy.
     */
    private void handleProcessingError(DomainEvent event, Long eventId,
                                     ProjectionHandler<DomainEvent> handler,
                                     ProjectionTracker tracker, int attemptNumber, 
                                     Exception error) {
        
        String projectionName = handler.getProjectionName();
        
        log.error("Erro ao processar evento {} (tentativa {}) na projeção {}: {}", 
                 eventId, attemptNumber, projectionName, error.getMessage(), error);
        
        // Registrar falha no tracker
        tracker.recordFailure(error.getMessage());
        trackerRepository.save(tracker);
        
        // Verificar se deve tentar novamente
        if (handler.isRetryable() && attemptNumber < handler.getMaxRetries()) {
            scheduleRetry(event, eventId, handler, attemptNumber + 1);
        } else {
            // Enviar para dead letter queue
            sendToDeadLetterQueue(event, eventId, handler, error, attemptNumber);
        }
    }
    
    /**
     * Agenda retry para processamento posterior.
     */
    private void scheduleRetry(DomainEvent event, Long eventId,
                             ProjectionHandler<DomainEvent> handler,
                             int attemptNumber) {
        
        long delayMs = calculateRetryDelay(attemptNumber);
        
        log.info("Agendando retry {} para evento {} na projeção {} em {}ms", 
                attemptNumber, eventId, handler.getProjectionName(), delayMs);
        
        // Implementar retry assíncrono (pode usar @Scheduled ou sistema de filas)
        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
            .execute(() -> processWithHandler(event, eventId, handler, attemptNumber));
    }
    
    /**
     * Calcula delay para retry com backoff exponencial.
     */
    private long calculateRetryDelay(int attemptNumber) {
        // Backoff exponencial: 1s, 2s, 4s, 8s, etc.
        return Math.min(1000L * (1L << (attemptNumber - 1)), 30000L);
    }
    
    /**
     * Envia evento para dead letter queue após esgotar tentativas.
     */
    private void sendToDeadLetterQueue(DomainEvent event, Long eventId,
                                     ProjectionHandler<DomainEvent> handler,
                                     Exception error, int totalAttempts) {
        
        log.error("Enviando evento {} para dead letter queue após {} tentativas na projeção {}", 
                 eventId, totalAttempts, handler.getProjectionName());
        
        // TODO: Implementar dead letter queue (pode ser tabela no banco, Kafka topic, etc.)
        // Por enquanto, apenas log do erro
        log.error("DEAD LETTER: Evento {} - Projeção {} - Erro: {}", 
                 eventId, handler.getProjectionName(), error.getMessage());
    }
    
    /**
     * Obtém ou cria tracker para uma projeção.
     */
    private ProjectionTracker getOrCreateTracker(String projectionName) {
        return trackerRepository.findById(projectionName)
            .orElseGet(() -> {
                ProjectionTracker tracker = new ProjectionTracker(projectionName);
                return trackerRepository.save(tracker);
            });
    }
    
    /**
     * Record para encapsular evento com seu ID.
     */
    public record EventWithId(DomainEvent event, Long eventId) {}
}