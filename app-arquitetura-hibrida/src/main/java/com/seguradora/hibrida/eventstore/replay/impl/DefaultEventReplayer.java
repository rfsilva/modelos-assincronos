package com.seguradora.hibrida.eventstore.replay.impl;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventHandler;
import com.seguradora.hibrida.eventbus.EventHandlerRegistry;
import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.eventstore.replay.*;
import com.seguradora.hibrida.eventstore.replay.exception.ReplayConfigurationException;
import com.seguradora.hibrida.eventstore.replay.exception.ReplayExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Implementação padrão do sistema de replay de eventos.
 * 
 * <p>Executa replay de eventos com controle de velocidade,
 * filtros avançados e modo simulação.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultEventReplayer implements EventReplayer {
    
    private final EventStore eventStore;
    private final EventBus eventBus;
    private final EventHandlerRegistry handlerRegistry;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    // Controle de replays ativos
    private final Map<UUID, ReplayExecution> activeReplays = new ConcurrentHashMap<>();
    private final List<ReplayResult> replayHistory = new CopyOnWriteArrayList<>();
    
    // Estatísticas
    private final AtomicLong totalReplaysExecuted = new AtomicLong(0);
    private final AtomicLong successfulReplays = new AtomicLong(0);
    private final AtomicLong failedReplays = new AtomicLong(0);
    private final AtomicLong cancelledReplays = new AtomicLong(0);
    
    @Override
    public CompletableFuture<ReplayResult> replayByPeriod(ReplayConfiguration configuration) {
        validateConfiguration(configuration);
        
        if (configuration.getFromTimestamp() == null || configuration.getToTimestamp() == null) {
            throw new ReplayConfigurationException("Período deve ser especificado para replay por período", configuration);
        }
        
        return executeReplay(configuration, () -> 
            eventStore.loadEventsByType(null, configuration.getFromTimestamp(), configuration.getToTimestamp())
        );
    }
    
    @Override
    public CompletableFuture<ReplayResult> replayByEventType(String eventType, Instant from, Instant to, 
                                                           ReplayConfiguration configuration) {
        validateConfiguration(configuration);
        
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new ReplayConfigurationException("Tipo de evento deve ser especificado", configuration);
        }
        
        return executeReplay(configuration, () -> 
            eventStore.loadEventsByType(eventType, from, to)
        );
    }
    
    @Override
    public CompletableFuture<ReplayResult> replayByAggregate(String aggregateId, Long fromVersion,
                                                           ReplayConfiguration configuration) {
        validateConfiguration(configuration);
        
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            throw new ReplayConfigurationException("ID do aggregate deve ser especificado", configuration);
        }
        
        return executeReplay(configuration, () -> {
            if (fromVersion != null) {
                return eventStore.loadEvents(aggregateId, fromVersion);
            } else {
                return eventStore.loadEvents(aggregateId);
            }
        });
    }
    
    @Override
    public CompletableFuture<ReplayResult> replayWithFilter(ReplayFilter filter, ReplayConfiguration configuration) {
        validateConfiguration(configuration);
        
        if (filter == null) {
            throw new ReplayConfigurationException("Filtro deve ser especificado", configuration);
        }
        
        return executeReplay(configuration, () -> {
            // Carrega eventos base e aplica filtro
            List<DomainEvent> allEvents = loadEventsForFilter(filter);
            return allEvents.stream()
                .filter(filter::matches)
                .collect(Collectors.toList());
        });
    }
    
    @Override
    public CompletableFuture<ReplayResult> simulateReplay(ReplayConfiguration configuration) {
        if (!configuration.isSimulationMode()) {
            configuration = ReplayConfiguration.forSimulation(configuration);
        }
        
        return replayByPeriod(configuration);
    }
    
    @Override
    public boolean pauseReplay(UUID replayId) {
        ReplayExecution execution = activeReplays.get(replayId);
        if (execution != null && execution.getProgress().canBePaused()) {
            execution.pause();
            log.info("Replay pausado: {}", replayId);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean resumeReplay(UUID replayId) {
        ReplayExecution execution = activeReplays.get(replayId);
        if (execution != null && execution.getProgress().canBeResumed()) {
            execution.resume();
            log.info("Replay retomado: {}", replayId);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean cancelReplay(UUID replayId) {
        ReplayExecution execution = activeReplays.get(replayId);
        if (execution != null && execution.getProgress().canBeCancelled()) {
            execution.cancel();
            log.info("Replay cancelado: {}", replayId);
            return true;
        }
        return false;
    }
    
    @Override
    public ReplayProgress getProgress(UUID replayId) {
        ReplayExecution execution = activeReplays.get(replayId);
        return execution != null ? execution.getProgress() : null;
    }
    
    @Override
    public List<ReplayProgress> getActiveReplays() {
        return activeReplays.values().stream()
            .map(ReplayExecution::getProgress)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ReplayResult> getReplayHistory(int limit) {
        return replayHistory.stream()
            .sorted((r1, r2) -> r2.getCompletedAt().compareTo(r1.getCompletedAt()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean isHealthy() {
        try {
            // Verifica se os componentes dependentes estão saudáveis
            return eventStore != null && eventBus != null && handlerRegistry != null;
        } catch (Exception e) {
            log.error("Erro ao verificar saúde do replayer", e);
            return false;
        }
    }
    
    @Override
    public ReplayStatistics getStatistics() {
        return ReplayStatistics.builder()
            .totalReplaysExecuted(totalReplaysExecuted.get())
            .successfulReplays(successfulReplays.get())
            .failedReplays(failedReplays.get())
            .cancelledReplays(cancelledReplays.get())
            .activeReplays(activeReplays.size())
            .build();
    }
    
    private CompletableFuture<ReplayResult> executeReplay(ReplayConfiguration configuration, 
                                                        EventLoader eventLoader) {
        UUID replayId = configuration.getReplayId();
        
        return CompletableFuture.supplyAsync(() -> {
            totalReplaysExecuted.incrementAndGet();
            
            try {
                log.info("Iniciando replay: {} ({})", configuration.getName(), replayId);
                
                // Carrega eventos
                List<DomainEvent> events = eventLoader.load();
                log.info("Carregados {} eventos para replay {}", events.size(), replayId);
                
                // Cria execução
                ReplayExecution execution = new ReplayExecution(configuration, events);
                activeReplays.put(replayId, execution);
                
                // Executa replay
                ReplayResult result = execution.execute();
                
                // Atualiza estatísticas
                if (result.isSuccessful()) {
                    successfulReplays.incrementAndGet();
                } else if (result.isCancelled()) {
                    cancelledReplays.incrementAndGet();
                } else {
                    failedReplays.incrementAndGet();
                }
                
                // Adiciona ao histórico
                replayHistory.add(result);
                
                log.info("Replay concluído: {} - {}", replayId, result.getStatus());
                return result;
                
            } catch (Exception e) {
                failedReplays.incrementAndGet();
                log.error("Erro na execução do replay: {}", replayId, e);
                throw new ReplayExecutionException(replayId, configuration.getName(), 
                    "Erro na execução do replay", e);
            } finally {
                activeReplays.remove(replayId);
            }
        }, executorService);
    }
    
    private List<DomainEvent> loadEventsForFilter(ReplayFilter filter) {
        // Implementação simplificada - carrega por período se especificado
        if (filter.getFromTimestamp() != null && filter.getToTimestamp() != null) {
            return eventStore.loadEventsByType(null, filter.getFromTimestamp(), filter.getToTimestamp());
        }
        
        // Para outros casos, seria necessário implementar consultas mais específicas
        throw new UnsupportedOperationException("Filtro não suportado ainda");
    }
    
    private void validateConfiguration(ReplayConfiguration configuration) {
        if (configuration == null) {
            throw new ReplayConfigurationException("Configuração não pode ser nula", null);
        }
        
        if (!configuration.isValid()) {
            throw new ReplayConfigurationException("Configuração inválida", configuration);
        }
        
        if (activeReplays.containsKey(configuration.getReplayId())) {
            throw new ReplayConfigurationException("Replay já está em execução", configuration);
        }
    }
    
    @FunctionalInterface
    private interface EventLoader {
        List<DomainEvent> load();
    }
    
    /**
     * Classe interna para controle de execução de replay.
     */
    private class ReplayExecution {
        private final ReplayConfiguration configuration;
        private final List<DomainEvent> events;
        private volatile ReplayProgress progress;
        private volatile boolean paused = false;
        private volatile boolean cancelled = false;
        
        public ReplayExecution(ReplayConfiguration configuration, List<DomainEvent> events) {
            this.configuration = configuration;
            this.events = events;
            this.progress = ReplayProgress.initial(configuration.getReplayId(), configuration, events.size());
        }
        
        public ReplayResult execute() {
            progress = progress.markAsStarted();
            
            List<ReplayError> errors = new ArrayList<>();
            long successCount = 0;
            long failureCount = 0;
            
            try {
                // Processa eventos em lotes
                List<List<DomainEvent>> batches = createBatches(events, configuration.getBatchSize());
                
                for (int i = 0; i < batches.size() && !cancelled; i++) {
                    // Verifica se está pausado
                    while (paused && !cancelled) {
                        Thread.sleep(100);
                    }
                    
                    if (cancelled) {
                        break;
                    }
                    
                    List<DomainEvent> batch = batches.get(i);
                    
                    try {
                        BatchResult batchResult = processBatch(batch, i + 1);
                        successCount += batchResult.successCount;
                        failureCount += batchResult.failureCount;
                        errors.addAll(batchResult.errors);
                        
                        // Atualiza progresso
                        progress = progress.updateProgress(
                            successCount + failureCount,
                            successCount,
                            failureCount,
                            calculateCurrentThroughput()
                        );
                        
                        // Controle de velocidade
                        if (configuration.getEventsPerSecond() > 0) {
                            applyThrottling(batch.size());
                        }
                        
                    } catch (Exception e) {
                        log.error("Erro no processamento do lote {}", i + 1, e);
                        errors.add(ReplayError.infrastructureError("Erro no lote " + (i + 1), e));
                        
                        if (configuration.isStopOnError()) {
                            break;
                        }
                    }
                }
                
                // Finaliza execução
                if (cancelled) {
                    progress = progress.markAsCancelled();
                    return ReplayResult.cancelled(configuration.getReplayId(), configuration, progress);
                } else if (!errors.isEmpty() && successCount == 0) {
                    progress = progress.markAsFailed("Todos os eventos falharam");
                    return ReplayResult.failure(configuration.getReplayId(), configuration, progress, errors);
                } else {
                    progress = progress.markAsCompleted();
                    return ReplayResult.success(configuration.getReplayId(), configuration, progress);
                }
                
            } catch (Exception e) {
                progress = progress.markAsFailed(e.getMessage());
                errors.add(ReplayError.infrastructureError("Erro geral na execução", e));
                return ReplayResult.failure(configuration.getReplayId(), configuration, progress, errors);
            }
        }
        
        private BatchResult processBatch(List<DomainEvent> batch, int batchNumber) {
            long successCount = 0;
            long failureCount = 0;
            List<ReplayError> errors = new ArrayList<>();
            
            for (DomainEvent event : batch) {
                try {
                    if (configuration.isSimulationMode()) {
                        // Modo simulação - não executa handlers reais
                        simulateEventProcessing(event);
                        successCount++;
                    } else {
                        // Modo normal - executa handlers
                        processEvent(event);
                        successCount++;
                    }
                } catch (Exception e) {
                    failureCount++;
                    errors.add(ReplayError.eventProcessingError(
                        event.getEventId().toString(),
                        event.getEventType(),
                        e.getMessage()
                    ));
                    
                    log.warn("Erro no processamento do evento {}: {}", 
                        event.getEventId(), e.getMessage());
                }
            }
            
            return new BatchResult(successCount, failureCount, errors);
        }
        
        private void simulateEventProcessing(DomainEvent event) {
            // Simula processamento sem efeitos colaterais
            log.debug("Simulando processamento do evento: {} ({})", 
                event.getEventId(), event.getEventType());
            
            // Simula tempo de processamento
            try {
                Thread.sleep(1); // 1ms de simulação
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void processEvent(DomainEvent event) {
            // Processa evento usando o event bus
            eventBus.publish(event);
        }
        
        private List<List<DomainEvent>> createBatches(List<DomainEvent> events, int batchSize) {
            List<List<DomainEvent>> batches = new ArrayList<>();
            for (int i = 0; i < events.size(); i += batchSize) {
                int end = Math.min(i + batchSize, events.size());
                batches.add(events.subList(i, end));
            }
            return batches;
        }
        
        private double calculateCurrentThroughput() {
            if (progress.getStartedAt() == null) {
                return 0.0;
            }
            
            Duration elapsed = Duration.between(progress.getStartedAt(), Instant.now());
            if (elapsed.isZero()) {
                return 0.0;
            }
            
            return (double) progress.getProcessedEvents() / elapsed.toSeconds();
        }
        
        private void applyThrottling(int eventsProcessed) {
            if (configuration.getEventsPerSecond() <= 0) {
                return;
            }
            
            long delayMs = (eventsProcessed * 1000L) / configuration.getEventsPerSecond();
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        public void pause() {
            this.paused = true;
            this.progress = progress.markAsPaused();
        }
        
        public void resume() {
            this.paused = false;
            this.progress = progress.toBuilder().status(ReplayProgress.Status.RUNNING).build();
        }
        
        public void cancel() {
            this.cancelled = true;
        }
        
        public ReplayProgress getProgress() {
            return progress;
        }
    }
    
    private static class BatchResult {
        final long successCount;
        final long failureCount;
        final List<ReplayError> errors;
        
        BatchResult(long successCount, long failureCount, List<ReplayError> errors) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.errors = errors;
        }
    }
}