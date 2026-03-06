package com.seguradora.hibrida.projection.consistency;

import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionStatus;
import com.seguradora.hibrida.projection.tracking.ProjectionTracker;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável por detectar inconsistências em projeções.
 * 
 * <p>Funcionalidades:
 * <ul>
 *   <li>Detecção automática de lag excessivo</li>
 *   <li>Identificação de projeções travadas</li>
 *   <li>Verificação de taxa de erro alta</li>
 *   <li>Monitoramento de projeções órfãs</li>
 *   <li>Alertas automáticos</li>
 * </ul>
 */
@Service
public class ProjectionConsistencyChecker {
    
    private static final Logger log = LoggerFactory.getLogger(ProjectionConsistencyChecker.class);
    
    private final ProjectionTrackerRepository trackerRepository;
    private final EventStoreRepository eventStoreRepository;
    private final ProjectionConsistencyProperties properties;
    
    public ProjectionConsistencyChecker(ProjectionTrackerRepository trackerRepository,
                                      EventStoreRepository eventStoreRepository,
                                      ProjectionConsistencyProperties properties) {
        this.trackerRepository = trackerRepository;
        this.eventStoreRepository = eventStoreRepository;
        this.properties = properties;
    }
    
    /**
     * Executa verificação completa de consistência de todas as projeções.
     * 
     * @return Relatório de consistência
     */
    public ConsistencyReport checkAllProjections() {
        log.info("Iniciando verificação de consistência de todas as projeções");
        
        long startTime = System.currentTimeMillis();
        List<ConsistencyIssue> issues = new ArrayList<>();
        
        try {
            // Obter último evento disponível
            Long maxEventId = eventStoreRepository.findMaxEventId();
            if (maxEventId == null) {
                maxEventId = 0L;
            }
            
            // Buscar todas as projeções
            List<ProjectionTracker> allProjections = trackerRepository.findAll();
            
            log.debug("Verificando {} projeções contra evento máximo {}", allProjections.size(), maxEventId);
            
            for (ProjectionTracker tracker : allProjections) {
                issues.addAll(checkProjectionConsistency(tracker, maxEventId));
            }
            
            // Verificar projeções órfãs (sem eventos recentes)
            issues.addAll(checkOrphanedProjections(allProjections));
            
            long duration = System.currentTimeMillis() - startTime;
            
            ConsistencyReport report = new ConsistencyReport(
                allProjections.size(),
                issues,
                maxEventId,
                duration,
                Instant.now()
            );
            
            log.info("Verificação de consistência concluída: {} projeções, {} issues, {}ms", 
                    allProjections.size(), issues.size(), duration);
            
            // Log de issues críticos
            long criticalIssues = issues.stream()
                .filter(issue -> issue.severity() == IssueSeverity.CRITICAL)
                .count();
            
            if (criticalIssues > 0) {
                log.warn("Encontrados {} issues críticos de consistência", criticalIssues);
            }
            
            return report;
            
        } catch (Exception e) {
            log.error("Erro na verificação de consistência: {}", e.getMessage(), e);
            
            long duration = System.currentTimeMillis() - startTime;
            return new ConsistencyReport(
                0,
                List.of(new ConsistencyIssue(
                    "SYSTEM_ERROR",
                    IssueType.SYSTEM_ERROR,
                    IssueSeverity.CRITICAL,
                    "Erro na verificação: " + e.getMessage(),
                    null
                )),
                0L,
                duration,
                Instant.now()
            );
        }
    }
    
    /**
     * Verifica consistência de uma projeção específica.
     * 
     * @param projectionName Nome da projeção
     * @return Lista de issues encontrados
     */
    public List<ConsistencyIssue> checkProjectionConsistency(String projectionName) {
        Optional<ProjectionTracker> trackerOpt = trackerRepository.findById(projectionName);
        if (trackerOpt.isEmpty()) {
            return List.of(new ConsistencyIssue(
                projectionName,
                IssueType.PROJECTION_NOT_FOUND,
                IssueSeverity.HIGH,
                "Projeção não encontrada no tracker",
                null
            ));
        }
        
        Long maxEventId = eventStoreRepository.findMaxEventId();
        if (maxEventId == null) {
            maxEventId = 0L;
        }
        
        return checkProjectionConsistency(trackerOpt.get(), maxEventId);
    }
    
    /**
     * Execução automática da verificação de consistência.
     */
    @Scheduled(fixedDelayString = "#{@projectionConsistencyProperties.checkIntervalSeconds * 1000}")
    public void scheduledConsistencyCheck() {
        if (!properties.isEnabled()) {
            return;
        }
        
        log.debug("Executando verificação automática de consistência");
        
        try {
            ConsistencyReport report = checkAllProjections();
            
            // Processar issues críticos
            List<ConsistencyIssue> criticalIssues = report.issues().stream()
                .filter(issue -> issue.severity() == IssueSeverity.CRITICAL)
                .toList();
            
            if (!criticalIssues.isEmpty()) {
                handleCriticalIssues(criticalIssues);
            }
            
        } catch (Exception e) {
            log.error("Erro na verificação automática de consistência: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Verifica consistência de uma projeção específica.
     */
    private List<ConsistencyIssue> checkProjectionConsistency(ProjectionTracker tracker, Long maxEventId) {
        List<ConsistencyIssue> issues = new ArrayList<>();
        String projectionName = tracker.getProjectionName();
        
        // 1. Verificar lag excessivo
        long lag = tracker.calculateLag(maxEventId);
        if (lag > properties.getMaxAllowedLag()) {
            IssueSeverity severity = lag > properties.getCriticalLagThreshold() ? 
                IssueSeverity.CRITICAL : IssueSeverity.HIGH;
            
            issues.add(new ConsistencyIssue(
                projectionName,
                IssueType.HIGH_LAG,
                severity,
                String.format("Lag excessivo: %d eventos atrás (máximo permitido: %d)", 
                             lag, properties.getMaxAllowedLag()),
                lag
            ));
        }
        
        // 2. Verificar projeção travada
        Instant staleThreshold = Instant.now().minus(properties.getStaleThresholdMinutes(), ChronoUnit.MINUTES);
        if (tracker.getLastProcessedAt().isBefore(staleThreshold) && 
            tracker.getStatus() == ProjectionStatus.ACTIVE) {
            
            issues.add(new ConsistencyIssue(
                projectionName,
                IssueType.STALE_PROJECTION,
                IssueSeverity.HIGH,
                String.format("Projeção não processa eventos há %d minutos", 
                             ChronoUnit.MINUTES.between(tracker.getLastProcessedAt(), Instant.now())),
                ChronoUnit.MINUTES.between(tracker.getLastProcessedAt(), Instant.now())
            ));
        }
        
        // 3. Verificar taxa de erro alta
        double errorRate = tracker.getErrorRate();
        if (errorRate > properties.getMaxAllowedErrorRate()) {
            IssueSeverity severity = errorRate > properties.getCriticalErrorRate() ? 
                IssueSeverity.CRITICAL : IssueSeverity.MEDIUM;
            
            issues.add(new ConsistencyIssue(
                projectionName,
                IssueType.HIGH_ERROR_RATE,
                severity,
                String.format("Taxa de erro alta: %.2f%% (máximo permitido: %.2f%%)", 
                             errorRate * 100, properties.getMaxAllowedErrorRate() * 100),
                errorRate
            ));
        }
        
        // 4. Verificar status de erro persistente
        if (tracker.getStatus() == ProjectionStatus.ERROR) {
            Instant errorThreshold = Instant.now().minus(properties.getMaxErrorDurationMinutes(), ChronoUnit.MINUTES);
            if (tracker.getLastErrorAt() != null && tracker.getLastErrorAt().isBefore(errorThreshold)) {
                issues.add(new ConsistencyIssue(
                    projectionName,
                    IssueType.PERSISTENT_ERROR,
                    IssueSeverity.HIGH,
                    String.format("Projeção em erro há %d minutos: %s", 
                                 ChronoUnit.MINUTES.between(tracker.getLastErrorAt(), Instant.now()),
                                 tracker.getLastErrorMessage()),
                    ChronoUnit.MINUTES.between(tracker.getLastErrorAt(), Instant.now())
                ));
            }
        }
        
        // 5. Verificar projeção pausada por muito tempo
        if (tracker.getStatus() == ProjectionStatus.PAUSED) {
            Instant pauseThreshold = Instant.now().minus(properties.getMaxPauseDurationMinutes(), ChronoUnit.MINUTES);
            if (tracker.getUpdatedAt().isBefore(pauseThreshold)) {
                issues.add(new ConsistencyIssue(
                    projectionName,
                    IssueType.LONG_PAUSED,
                    IssueSeverity.MEDIUM,
                    String.format("Projeção pausada há %d minutos", 
                                 ChronoUnit.MINUTES.between(tracker.getUpdatedAt(), Instant.now())),
                    ChronoUnit.MINUTES.between(tracker.getUpdatedAt(), Instant.now())
                ));
            }
        }
        
        return issues;
    }
    
    /**
     * Verifica projeções órfãs (sem atividade recente).
     */
    private List<ConsistencyIssue> checkOrphanedProjections(List<ProjectionTracker> allProjections) {
        List<ConsistencyIssue> issues = new ArrayList<>();
        
        Instant orphanThreshold = Instant.now().minus(properties.getOrphanThresholdHours(), ChronoUnit.HOURS);
        
        for (ProjectionTracker tracker : allProjections) {
            if (tracker.getLastProcessedAt().isBefore(orphanThreshold) && 
                tracker.getEventsProcessed() == 0) {
                
                issues.add(new ConsistencyIssue(
                    tracker.getProjectionName(),
                    IssueType.ORPHANED_PROJECTION,
                    IssueSeverity.LOW,
                    String.format("Projeção órfã: nunca processou eventos e foi criada há %d horas", 
                                 ChronoUnit.HOURS.between(tracker.getCreatedAt(), Instant.now())),
                    ChronoUnit.HOURS.between(tracker.getCreatedAt(), Instant.now())
                ));
            }
        }
        
        return issues;
    }
    
    /**
     * Trata issues críticos automaticamente.
     */
    private void handleCriticalIssues(List<ConsistencyIssue> criticalIssues) {
        log.warn("Tratando {} issues críticos de consistência", criticalIssues.size());
        
        for (ConsistencyIssue issue : criticalIssues) {
            try {
                switch (issue.type()) {
                    case HIGH_LAG:
                        handleHighLagIssue(issue);
                        break;
                    case STALE_PROJECTION:
                        handleStaleProjectionIssue(issue);
                        break;
                    case HIGH_ERROR_RATE:
                        handleHighErrorRateIssue(issue);
                        break;
                    case PERSISTENT_ERROR:
                        handlePersistentErrorIssue(issue);
                        break;
                    default:
                        log.warn("Tipo de issue crítico não tratado automaticamente: {}", issue.type());
                }
            } catch (Exception e) {
                log.error("Erro ao tratar issue crítico {}: {}", issue.projectionName(), e.getMessage(), e);
            }
        }
    }
    
    private void handleHighLagIssue(ConsistencyIssue issue) {
        log.warn("Tratando lag alto na projeção: {}", issue.projectionName());
        
        if (properties.isAutoRestartOnHighLag()) {
            // Marcar para rebuild automático
            Optional<ProjectionTracker> trackerOpt = trackerRepository.findById(issue.projectionName());
            if (trackerOpt.isPresent()) {
                ProjectionTracker tracker = trackerOpt.get();
                tracker.recordFailure("Auto-restart devido a lag alto: " + issue.description());
                trackerRepository.save(tracker);
                
                log.info("Projeção {} marcada para rebuild devido a lag alto", issue.projectionName());
            }
        }
    }
    
    private void handleStaleProjectionIssue(ConsistencyIssue issue) {
        log.warn("Tratando projeção travada: {}", issue.projectionName());
        
        if (properties.isAutoRestartOnStale()) {
            // Resetar status para ativo
            Optional<ProjectionTracker> trackerOpt = trackerRepository.findById(issue.projectionName());
            if (trackerOpt.isPresent()) {
                ProjectionTracker tracker = trackerOpt.get();
                tracker.resume();
                tracker.recordFailure("Auto-restart devido a projeção travada");
                trackerRepository.save(tracker);
                
                log.info("Projeção {} reiniciada automaticamente", issue.projectionName());
            }
        }
    }
    
    private void handleHighErrorRateIssue(ConsistencyIssue issue) {
        log.warn("Tratando taxa de erro alta na projeção: {}", issue.projectionName());
        
        if (properties.isAutoPauseOnHighErrorRate()) {
            // Pausar projeção temporariamente
            Optional<ProjectionTracker> trackerOpt = trackerRepository.findById(issue.projectionName());
            if (trackerOpt.isPresent()) {
                ProjectionTracker tracker = trackerOpt.get();
                tracker.pause();
                trackerRepository.save(tracker);
                
                log.info("Projeção {} pausada automaticamente devido a taxa de erro alta", issue.projectionName());
            }
        }
    }
    
    private void handlePersistentErrorIssue(ConsistencyIssue issue) {
        log.warn("Tratando erro persistente na projeção: {}", issue.projectionName());
        
        if (properties.isAutoRebuildOnPersistentError()) {
            // Marcar para rebuild completo
            Optional<ProjectionTracker> trackerOpt = trackerRepository.findById(issue.projectionName());
            if (trackerOpt.isPresent()) {
                ProjectionTracker tracker = trackerOpt.get();
                tracker.setLastProcessedEventId(0L); // Forçar rebuild completo
                tracker.recordFailure("Auto-rebuild devido a erro persistente");
                trackerRepository.save(tracker);
                
                log.info("Projeção {} marcada para rebuild completo devido a erro persistente", issue.projectionName());
            }
        }
    }
}