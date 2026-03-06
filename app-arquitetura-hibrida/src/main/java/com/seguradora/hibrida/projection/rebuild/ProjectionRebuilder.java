package com.seguradora.hibrida.projection.rebuild;

import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.projection.ProjectionEventProcessor;
import com.seguradora.hibrida.projection.ProjectionHandler;
import com.seguradora.hibrida.projection.ProjectionRegistry;
import com.seguradora.hibrida.projection.tracking.ProjectionStatus;
import com.seguradora.hibrida.projection.tracking.ProjectionTracker;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço responsável pelo rebuild automático de projeções.
 * 
 * <p>Funcionalidades principais:
 * <ul>
 *   <li>Rebuild completo de projeções</li>
 *   <li>Rebuild incremental para projeções grandes</li>
 *   <li>Detecção automática de inconsistências</li>
 *   <li>Processamento em background</li>
 *   <li>Controle de pausar/retomar</li>
 * </ul>
 */
@Service
public class ProjectionRebuilder {
    
    private static final Logger log = LoggerFactory.getLogger(ProjectionRebuilder.class);
    
    private final EventStore eventStore;
    private final ProjectionRegistry projectionRegistry;
    private final ProjectionTrackerRepository trackerRepository;
    private final ProjectionEventProcessor eventProcessor;
    private final ProjectionRebuildProperties properties;
    
    public ProjectionRebuilder(EventStore eventStore,
                             ProjectionRegistry projectionRegistry,
                             ProjectionTrackerRepository trackerRepository,
                             ProjectionEventProcessor eventProcessor,
                             ProjectionRebuildProperties properties) {
        this.eventStore = eventStore;
        this.projectionRegistry = projectionRegistry;
        this.trackerRepository = trackerRepository;
        this.eventProcessor = eventProcessor;
        this.properties = properties;
    }
    
    /**
     * Executa rebuild completo de uma projeção específica.
     * 
     * @param projectionName Nome da projeção
     * @return CompletableFuture com resultado do rebuild
     */
    @Async("projectionTaskExecutor")
    public CompletableFuture<RebuildResult> rebuildProjection(String projectionName) {
        log.info("Iniciando rebuild completo da projeção: {}", projectionName);
        
        try {
            ProjectionHandler<? extends DomainEvent> handler = projectionRegistry.getHandler(projectionName);
            if (handler == null) {
                throw new ProjectionRebuildException("Handler não encontrado: " + projectionName);
            }
            
            ProjectionTracker tracker = getOrCreateTracker(projectionName);
            
            // Marcar como rebuilding
            tracker.setStatus(ProjectionStatus.REBUILDING);
            tracker.setUpdatedAt(Instant.now());
            trackerRepository.save(tracker);
            
            RebuildResult result = executeRebuild(handler, tracker, false);
            
            log.info("Rebuild completo da projeção {} concluído: {}", projectionName, result);
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            log.error("Erro no rebuild da projeção {}: {}", projectionName, e.getMessage(), e);
            
            // Marcar como erro
            ProjectionTracker tracker = getOrCreateTracker(projectionName);
            tracker.recordFailure("Erro no rebuild: " + e.getMessage());
            trackerRepository.save(tracker);
            
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Executa rebuild incremental de uma projeção específica.
     * 
     * @param projectionName Nome da projeção
     * @return CompletableFuture com resultado do rebuild
     */
    @Async("projectionTaskExecutor")
    public CompletableFuture<RebuildResult> rebuildProjectionIncremental(String projectionName) {
        log.info("Iniciando rebuild incremental da projeção: {}", projectionName);
        
        try {
            ProjectionHandler<? extends DomainEvent> handler = projectionRegistry.getHandler(projectionName);
            if (handler == null) {
                throw new ProjectionRebuildException("Handler não encontrado: " + projectionName);
            }
            
            ProjectionTracker tracker = getOrCreateTracker(projectionName);
            
            // Marcar como rebuilding
            tracker.setStatus(ProjectionStatus.REBUILDING);
            tracker.setUpdatedAt(Instant.now());
            trackerRepository.save(tracker);
            
            RebuildResult result = executeRebuild(handler, tracker, true);
            
            log.info("Rebuild incremental da projeção {} concluído: {}", projectionName, result);
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            log.error("Erro no rebuild incremental da projeção {}: {}", projectionName, e.getMessage(), e);
            
            // Marcar como erro
            ProjectionTracker tracker = getOrCreateTracker(projectionName);
            tracker.recordFailure("Erro no rebuild incremental: " + e.getMessage());
            trackerRepository.save(tracker);
            
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Executa rebuild automático de todas as projeções que precisam.
     * 
     * @return CompletableFuture com lista de resultados
     */
    @Async("projectionTaskExecutor")
    public CompletableFuture<List<RebuildResult>> rebuildProjectionsNeedingRebuild() {
        log.info("Iniciando rebuild automático de projeções que precisam");
        
        try {
            // Buscar projeções que precisam de rebuild
            List<ProjectionTracker> projectionsNeedingRebuild = findProjectionsNeedingRebuild();
            
            if (projectionsNeedingRebuild.isEmpty()) {
                log.info("Nenhuma projeção precisa de rebuild no momento");
                return CompletableFuture.completedFuture(List.of());
            }
            
            log.info("Encontradas {} projeções que precisam de rebuild", projectionsNeedingRebuild.size());
            
            List<CompletableFuture<RebuildResult>> futures = projectionsNeedingRebuild.stream()
                .map(tracker -> {
                    // Decidir se é rebuild completo ou incremental
                    boolean isIncremental = shouldUseIncrementalRebuild(tracker);
                    
                    if (isIncremental) {
                        return rebuildProjectionIncremental(tracker.getProjectionName());
                    } else {
                        return rebuildProjection(tracker.getProjectionName());
                    }
                })
                .toList();
            
            // Aguardar todos os rebuilds
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            
            return allOf.thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .toList());
            
        } catch (Exception e) {
            log.error("Erro no rebuild automático: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Pausa o rebuild de uma projeção específica.
     * 
     * @param projectionName Nome da projeção
     * @return true se foi pausado com sucesso
     */
    @Transactional("writeTransactionManager")
    public boolean pauseRebuild(String projectionName) {
        log.info("Pausando rebuild da projeção: {}", projectionName);
        
        ProjectionTracker tracker = trackerRepository.findById(projectionName).orElse(null);
        if (tracker == null) {
            log.warn("Projeção não encontrada para pausar: {}", projectionName);
            return false;
        }
        
        if (tracker.getStatus() == ProjectionStatus.REBUILDING) {
            tracker.pause();
            trackerRepository.save(tracker);
            log.info("Rebuild da projeção {} pausado", projectionName);
            return true;
        }
        
        log.warn("Projeção {} não está em rebuild para ser pausada. Status atual: {}", 
                projectionName, tracker.getStatus());
        return false;
    }
    
    /**
     * Retoma o rebuild de uma projeção pausada.
     * 
     * @param projectionName Nome da projeção
     * @return CompletableFuture com resultado do rebuild
     */
    public CompletableFuture<RebuildResult> resumeRebuild(String projectionName) {
        log.info("Retomando rebuild da projeção: {}", projectionName);
        
        ProjectionTracker tracker = trackerRepository.findById(projectionName).orElse(null);
        if (tracker == null) {
            log.warn("Projeção não encontrada para retomar: {}", projectionName);
            return CompletableFuture.failedFuture(
                new ProjectionRebuildException("Projeção não encontrada: " + projectionName));
        }
        
        if (tracker.getStatus() != ProjectionStatus.PAUSED) {
            log.warn("Projeção {} não está pausada para ser retomada. Status atual: {}", 
                    projectionName, tracker.getStatus());
            return CompletableFuture.failedFuture(
                new ProjectionRebuildException("Projeção não está pausada: " + projectionName));
        }
        
        // Retomar como rebuild incremental
        return rebuildProjectionIncremental(projectionName);
    }
    
    /**
     * Executa o rebuild propriamente dito.
     */
    @SuppressWarnings("unchecked")
    private RebuildResult executeRebuild(ProjectionHandler<? extends DomainEvent> handler,
                                       ProjectionTracker tracker,
                                       boolean incremental) throws Exception {
        
        String projectionName = handler.getProjectionName();
        Class<? extends DomainEvent> eventType = handler.getEventType();
        
        long startTime = System.currentTimeMillis();
        AtomicLong processedEvents = new AtomicLong(0);
        AtomicLong failedEvents = new AtomicLong(0);
        
        try {
            // Determinar ponto de início
            long fromEventId = incremental ? tracker.getLastProcessedEventId() + 1 : 0L;
            
            log.info("Executando rebuild {} da projeção {} a partir do evento {}", 
                    incremental ? "incremental" : "completo", projectionName, fromEventId);
            
            // Buscar eventos por tipo
            Instant fromTime = incremental ? 
                tracker.getLastProcessedAt().minus(1, ChronoUnit.HOURS) : // Buffer de 1 hora
                Instant.EPOCH;
            
            List<DomainEvent> events = eventStore.loadEventsByType(
                eventType.getSimpleName(), fromTime, Instant.now());
            
            log.info("Encontrados {} eventos do tipo {} para rebuild", events.size(), eventType.getSimpleName());
            
            // Processar eventos em lotes
            int batchSize = properties.getBatchSize();
            for (int i = 0; i < events.size(); i += batchSize) {
                // Verificar se foi pausado
                ProjectionTracker currentTracker = trackerRepository.findById(projectionName).orElse(tracker);
                if (currentTracker.getStatus() == ProjectionStatus.PAUSED) {
                    log.info("Rebuild pausado para projeção: {}", projectionName);
                    break;
                }
                
                int endIndex = Math.min(i + batchSize, events.size());
                List<DomainEvent> batch = events.subList(i, endIndex);
                
                // Processar lote
                for (DomainEvent event : batch) {
                    try {
                        // Simular ID sequencial (em produção viria do event store)
                        long eventId = fromEventId + processedEvents.get();
                        
                        // Cast seguro para o handler
                        @SuppressWarnings("unchecked")
                        ProjectionHandler<DomainEvent> typedHandler = (ProjectionHandler<DomainEvent>) handler;
                        
                        if (typedHandler.supports(event)) {
                            eventProcessor.processEvent(event, eventId);
                            processedEvents.incrementAndGet();
                            
                            // Atualizar tracker periodicamente
                            if (processedEvents.get() % 100 == 0) {
                                tracker.updatePosition(eventId);
                                trackerRepository.save(tracker);
                            }
                        }
                        
                    } catch (Exception e) {
                        log.error("Erro ao processar evento no rebuild: {}", e.getMessage(), e);
                        failedEvents.incrementAndGet();
                        
                        // Se muitos erros, parar o rebuild
                        if (failedEvents.get() > properties.getMaxErrorsBeforeStop()) {
                            throw new ProjectionRebuildException(
                                "Muitos erros no rebuild (" + failedEvents.get() + "), parando");
                        }
                    }
                }
                
                // Log de progresso
                if (i % (batchSize * 10) == 0) {
                    double progress = (double) (i + batch.size()) / events.size() * 100;
                    log.info("Progresso do rebuild {}: {:.1f}% ({}/{} eventos)", 
                            projectionName, progress, i + batch.size(), events.size());
                }
            }
            
            // Finalizar rebuild
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // Atualizar status final
            tracker.setStatus(ProjectionStatus.ACTIVE);
            tracker.setUpdatedAt(Instant.now());
            trackerRepository.save(tracker);
            
            RebuildResult result = new RebuildResult(
                projectionName,
                incremental ? RebuildType.INCREMENTAL : RebuildType.FULL,
                RebuildStatus.SUCCESS,
                processedEvents.get(),
                failedEvents.get(),
                duration,
                null
            );
            
            log.info("Rebuild {} da projeção {} concluído com sucesso: {} eventos processados, {} falhas, {}ms", 
                    incremental ? "incremental" : "completo", projectionName, 
                    processedEvents.get(), failedEvents.get(), duration);
            
            return result;
            
        } catch (Exception e) {
            // Marcar como erro
            tracker.recordFailure("Erro no rebuild: " + e.getMessage());
            trackerRepository.save(tracker);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            return new RebuildResult(
                projectionName,
                incremental ? RebuildType.INCREMENTAL : RebuildType.FULL,
                RebuildStatus.FAILED,
                processedEvents.get(),
                failedEvents.get(),
                duration,
                e.getMessage()
            );
        }
    }
    
    /**
     * Busca projeções que precisam de rebuild.
     */
    private List<ProjectionTracker> findProjectionsNeedingRebuild() {
        // Buscar último evento disponível (simulado - em produção viria do event store)
        Long maxEventId = 1000000L; // Placeholder
        
        return trackerRepository.findProjectionsNeedingRebuild(
            maxEventId,
            properties.getLagThresholdForRebuild(),
            (long) (properties.getErrorThresholdForRebuild() * 1000) // Converter double para long
        );
    }
    
    /**
     * Decide se deve usar rebuild incremental.
     */
    private boolean shouldUseIncrementalRebuild(ProjectionTracker tracker) {
        // Usar incremental se:
        // 1. Projeção já processou eventos antes
        // 2. Lag não é muito alto
        // 3. Taxa de erro não é muito alta
        
        if (tracker.getLastProcessedEventId() == 0) {
            return false; // Primeira vez, usar completo
        }
        
        if (tracker.getErrorRate() > properties.getErrorRateThresholdForFullRebuild()) {
            return false; // Muitos erros, usar completo
        }
        
        // Simular cálculo de lag
        long estimatedLag = 1000L; // Placeholder
        if (estimatedLag > properties.getLagThresholdForFullRebuild()) {
            return false; // Lag muito alto, usar completo
        }
        
        return true;
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
}