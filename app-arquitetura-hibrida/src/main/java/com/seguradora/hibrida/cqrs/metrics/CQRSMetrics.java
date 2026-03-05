package com.seguradora.hibrida.cqrs.metrics;

import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionTracker;
import com.seguradora.hibrida.projection.tracking.ProjectionStatus;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Métricas customizadas para CQRS.
 * 
 * <p>Coleta e expõe métricas específicas do padrão CQRS:
 * <ul>
 *   <li>Lag entre Command Side e Query Side</li>
 *   <li>Throughput de eventos por projeção</li>
 *   <li>Taxa de erro das projeções</li>
 *   <li>Latência de processamento</li>
 *   <li>Status das projeções</li>
 * </ul>
 */
@Component
public class CQRSMetrics implements MeterBinder {
    
    private static final Logger log = LoggerFactory.getLogger(CQRSMetrics.class);
    
    private final EventStoreRepository eventStoreRepository;
    private final ProjectionTrackerRepository projectionTrackerRepository;
    
    // Métricas em cache para performance
    private final AtomicLong commandSideEvents = new AtomicLong(0);
    private final AtomicLong querySideEvents = new AtomicLong(0);
    private final AtomicLong overallLag = new AtomicLong(0);
    private final AtomicLong activeProjections = new AtomicLong(0);
    private final AtomicLong errorProjections = new AtomicLong(0);
    private final AtomicLong staleProjections = new AtomicLong(0);
    
    public CQRSMetrics(EventStoreRepository eventStoreRepository,
                      ProjectionTrackerRepository projectionTrackerRepository) {
        this.eventStoreRepository = eventStoreRepository;
        this.projectionTrackerRepository = projectionTrackerRepository;
    }
    
    @Override
    public void bindTo(MeterRegistry registry) {
        log.info("Binding CQRS metrics to registry: {}", registry.getClass().getSimpleName());
        
        // Métricas de lag CQRS
        Gauge.builder("cqrs.command.side.events", this, CQRSMetrics::getCommandSideEvents)
                .description("Total events in command side (Event Store)")
                .register(registry);
        
        Gauge.builder("cqrs.query.side.events", this, CQRSMetrics::getQuerySideEvents)
                .description("Minimum processed events in query side (projections)")
                .register(registry);
        
        Gauge.builder("cqrs.lag.events", this, CQRSMetrics::getOverallLag)
                .description("Lag between command and query side in events")
                .register(registry);
        
        Gauge.builder("cqrs.lag.seconds", this, CQRSMetrics::getEstimatedLagSeconds)
                .description("Estimated lag in seconds based on event rate")
                .register(registry);
        
        // Métricas de projeções
        Gauge.builder("cqrs.projections.total", this, CQRSMetrics::getTotalProjections)
                .description("Total number of projections")
                .register(registry);
        
        Gauge.builder("cqrs.projections.active", this, CQRSMetrics::getActiveProjections)
                .description("Number of active projections")
                .register(registry);
        
        Gauge.builder("cqrs.projections.error", this, CQRSMetrics::getErrorProjections)
                .description("Number of projections in error state")
                .register(registry);
        
        Gauge.builder("cqrs.projections.stale", this, CQRSMetrics::getStaleProjections)
                .description("Number of stale projections (not updated recently)")
                .register(registry);
        
        // Métricas de performance
        Gauge.builder("cqrs.projections.throughput", this, CQRSMetrics::getProjectionsThroughput)
                .description("Average events processed per second across all projections")
                .register(registry);
        
        Gauge.builder("cqrs.projections.error.rate", this, CQRSMetrics::getProjectionsErrorRate)
                .description("Overall error rate across all projections")
                .register(registry);
        
        // Métricas de saúde
        Gauge.builder("cqrs.health.score", this, CQRSMetrics::getHealthScore)
                .description("Overall CQRS health score (0-1)")
                .register(registry);
        
        log.info("CQRS metrics bound successfully");
        
        // Inicializar métricas
        updateMetrics();
    }
    
    /**
     * Atualiza todas as métricas.
     * 
     * <p>Este método deve ser chamado periodicamente para manter
     * as métricas atualizadas.
     */
    public void updateMetrics() {
        try {
            updateCommandSideMetrics();
            updateQuerySideMetrics();
            updateLagMetrics();
            updateProjectionMetrics();
            
        } catch (Exception e) {
            log.error("Erro ao atualizar métricas CQRS", e);
        }
    }
    
    /**
     * Atualiza métricas do Command Side.
     */
    private void updateCommandSideMetrics() {
        try {
            // Usar count() em vez de findMaxEventId() para ter um número sequencial
            long totalEvents = eventStoreRepository.count();
            commandSideEvents.set(totalEvents);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar métricas do Command Side", e);
        }
    }
    
    /**
     * Atualiza métricas do Query Side.
     */
    private void updateQuerySideMetrics() {
        try {
            List<ProjectionTracker> trackers = projectionTrackerRepository.findAll();
            
            if (trackers.isEmpty()) {
                querySideEvents.set(0);
                return;
            }
            
            // Posição mínima (projeção mais atrasada)
            long minPosition = trackers.stream()
                    .mapToLong(t -> t.getLastProcessedEventId() != null ? t.getLastProcessedEventId() : 0L)
                    .min()
                    .orElse(0L);
            
            querySideEvents.set(minPosition);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar métricas do Query Side", e);
        }
    }
    
    /**
     * Atualiza métricas de lag.
     */
    private void updateLagMetrics() {
        long commandEvents = commandSideEvents.get();
        long queryEvents = querySideEvents.get();
        long lag = Math.max(0, commandEvents - queryEvents);
        
        overallLag.set(lag);
    }
    
    /**
     * Atualiza métricas das projeções.
     */
    private void updateProjectionMetrics() {
        try {
            List<ProjectionTracker> trackers = projectionTrackerRepository.findAll();
            
            // Contar por status
            long active = trackers.stream()
                    .mapToLong(t -> t.getStatus() == ProjectionStatus.ACTIVE ? 1L : 0L)
                    .sum();
            
            long error = trackers.stream()
                    .mapToLong(t -> t.getStatus() == ProjectionStatus.ERROR ? 1L : 0L)
                    .sum();
            
            // Contar projeções obsoletas (não atualizadas há 30 minutos)
            Instant staleThreshold = Instant.now().minus(Duration.ofMinutes(30));
            long stale = trackers.stream()
                    .mapToLong(t -> t.getUpdatedAt().isBefore(staleThreshold) ? 1L : 0L)
                    .sum();
            
            activeProjections.set(active);
            errorProjections.set(error);
            staleProjections.set(stale);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar métricas das projeções", e);
        }
    }
    
    // === GETTERS PARA MÉTRICAS ===
    
    public double getCommandSideEvents() {
        return commandSideEvents.get();
    }
    
    public double getQuerySideEvents() {
        return querySideEvents.get();
    }
    
    public double getOverallLag() {
        return overallLag.get();
    }
    
    public double getEstimatedLagSeconds() {
        // Estimativa baseada em throughput médio de 10 eventos/segundo
        return overallLag.get() / 10.0;
    }
    
    public double getTotalProjections() {
        try {
            return projectionTrackerRepository.count();
        } catch (Exception e) {
            log.error("Erro ao obter total de projeções", e);
            return 0.0;
        }
    }
    
    public double getActiveProjections() {
        return activeProjections.get();
    }
    
    public double getErrorProjections() {
        return errorProjections.get();
    }
    
    public double getStaleProjections() {
        return staleProjections.get();
    }
    
    public double getProjectionsThroughput() {
        try {
            List<ProjectionTracker> trackers = projectionTrackerRepository.findAll();
            
            if (trackers.isEmpty()) {
                return 0.0;
            }
            
            // Calcular throughput médio baseado nos últimos eventos processados
            double totalThroughput = trackers.stream()
                    .mapToDouble(this::calculateProjectionThroughput)
                    .sum();
            
            return totalThroughput / trackers.size();
            
        } catch (Exception e) {
            log.error("Erro ao calcular throughput das projeções", e);
            return 0.0;
        }
    }
    
    public double getProjectionsErrorRate() {
        try {
            List<ProjectionTracker> trackers = projectionTrackerRepository.findAll();
            
            if (trackers.isEmpty()) {
                return 0.0;
            }
            
            double totalEvents = trackers.stream()
                    .mapToDouble(t -> t.getEventsProcessed() != null ? t.getEventsProcessed() : 0.0)
                    .sum();
            
            double totalErrors = trackers.stream()
                    .mapToDouble(t -> t.getEventsFailed() != null ? t.getEventsFailed() : 0.0)
                    .sum();
            
            return totalEvents > 0 ? totalErrors / totalEvents : 0.0;
            
        } catch (Exception e) {
            log.error("Erro ao calcular taxa de erro das projeções", e);
            return 0.0;
        }
    }
    
    public double getHealthScore() {
        try {
            double score = 1.0;
            
            // Penalizar por lag alto
            long lag = overallLag.get();
            if (lag > 5000) {
                score -= 0.5; // Lag crítico
            } else if (lag > 1000) {
                score -= 0.2; // Lag alto
            }
            
            // Penalizar por projeções com erro
            double totalProjections = getTotalProjections();
            if (totalProjections > 0) {
                double errorRate = errorProjections.get() / totalProjections;
                score -= errorRate * 0.3;
                
                double staleRate = staleProjections.get() / totalProjections;
                score -= staleRate * 0.2;
            }
            
            // Penalizar por taxa de erro alta
            double projectionErrorRate = getProjectionsErrorRate();
            if (projectionErrorRate > 0.15) {
                score -= 0.3; // Taxa crítica
            } else if (projectionErrorRate > 0.05) {
                score -= 0.1; // Taxa alta
            }
            
            return Math.max(0.0, Math.min(1.0, score));
            
        } catch (Exception e) {
            log.error("Erro ao calcular score de saúde", e);
            return 0.0;
        }
    }
    
    /**
     * Calcula throughput de uma projeção específica.
     */
    private double calculateProjectionThroughput(ProjectionTracker tracker) {
        try {
            if (tracker.getEventsProcessed() == null || tracker.getCreatedAt() == null) {
                return 0.0;
            }
            
            long eventsProcessed = tracker.getEventsProcessed();
            Duration duration = Duration.between(tracker.getCreatedAt(), Instant.now());
            
            if (duration.getSeconds() == 0) {
                return 0.0;
            }
            
            return (double) eventsProcessed / duration.getSeconds();
            
        } catch (Exception e) {
            log.error("Erro ao calcular throughput da projeção {}", tracker.getProjectionName(), e);
            return 0.0;
        }
    }
    
    /**
     * Força atualização das métricas.
     */
    public void forceUpdate() {
        updateMetrics();
    }
}