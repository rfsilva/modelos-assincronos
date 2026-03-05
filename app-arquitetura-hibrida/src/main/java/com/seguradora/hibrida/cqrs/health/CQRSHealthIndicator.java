package com.seguradora.hibrida.cqrs.health;

import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionTracker;
import com.seguradora.hibrida.projection.tracking.ProjectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Health Indicator específico para CQRS.
 * 
 * <p>Monitora a saúde do sistema CQRS verificando:
 * <ul>
 *   <li>Lag entre Command Side e Query Side</li>
 *   <li>Status das projeções</li>
 *   <li>Taxa de erro das projeções</li>
 *   <li>Conectividade dos datasources</li>
 *   <li>Performance geral do sistema</li>
 * </ul>
 */
@Component
public class CQRSHealthIndicator implements HealthIndicator {
    
    private static final Logger log = LoggerFactory.getLogger(CQRSHealthIndicator.class);
    
    // Thresholds para determinar saúde
    private static final long HIGH_LAG_THRESHOLD = 1000; // 1000 eventos de lag
    private static final long CRITICAL_LAG_THRESHOLD = 5000; // 5000 eventos de lag
    private static final double HIGH_ERROR_RATE_THRESHOLD = 0.05; // 5% de erro
    private static final double CRITICAL_ERROR_RATE_THRESHOLD = 0.15; // 15% de erro
    private static final long STALE_PROJECTION_MINUTES = 30; // 30 minutos sem atualização
    
    private final EventStoreRepository eventStoreRepository;
    private final ProjectionTrackerRepository projectionTrackerRepository;
    
    public CQRSHealthIndicator(EventStoreRepository eventStoreRepository,
                              ProjectionTrackerRepository projectionTrackerRepository) {
        this.eventStoreRepository = eventStoreRepository;
        this.projectionTrackerRepository = projectionTrackerRepository;
    }
    
    @Override
    public Health health() {
        try {
            Map<String, Object> details = checkHealth();
            
            // Determinar status geral baseado nos detalhes
            String overallStatus = determineOverallStatus(details);
            
            if ("UP".equals(overallStatus)) {
                return Health.up().withDetails(details).build();
            } else if ("DEGRADED".equals(overallStatus)) {
                return Health.status("DEGRADED").withDetails(details).build();
            } else {
                return Health.down().withDetails(details).build();
            }
            
        } catch (Exception e) {
            log.error("Erro ao verificar saúde do CQRS", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", Instant.now())
                    .build();
        }
    }
    
    /**
     * Verifica a saúde detalhada do sistema CQRS.
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            // 1. Verificar lag entre Command e Query sides
            Map<String, Object> lagInfo = checkCommandQueryLag();
            details.put("lag", lagInfo);
            
            // 2. Verificar status das projeções
            Map<String, Object> projectionsInfo = checkProjectionsHealth();
            details.put("projections", projectionsInfo);
            
            // 3. Verificar conectividade dos datasources
            Map<String, Object> datasourcesInfo = checkDatasourcesHealth();
            details.put("datasources", datasourcesInfo);
            
            // 4. Métricas gerais
            Map<String, Object> metricsInfo = getGeneralMetrics();
            details.put("metrics", metricsInfo);
            
            // 5. Timestamp da verificação
            details.put("timestamp", Instant.now());
            details.put("healthy", isHealthy(details));
            
        } catch (Exception e) {
            log.error("Erro ao coletar informações de saúde do CQRS", e);
            details.put("error", e.getMessage());
            details.put("healthy", false);
        }
        
        return details;
    }
    
    /**
     * Verifica o lag entre Command Side e Query Side.
     */
    private Map<String, Object> checkCommandQueryLag() {
        Map<String, Object> lagInfo = new HashMap<>();
        
        try {
            // Obter total de eventos no Event Store (Command Side)
            // Usar count() em vez de findMaxEventId() que não existe
            Long totalEvents = eventStoreRepository.count();
            
            // Obter posições das projeções (Query Side)
            List<ProjectionTracker> trackers = projectionTrackerRepository.findAll();
            
            if (trackers.isEmpty()) {
                lagInfo.put("status", "NO_PROJECTIONS");
                lagInfo.put("commandSideEvents", totalEvents);
                lagInfo.put("querySideEvents", 0L);
                lagInfo.put("lag", totalEvents);
                return lagInfo;
            }
            
            // Calcular lag por projeção
            Map<String, Long> lagByProjection = new HashMap<>();
            long minQuerySidePosition = Long.MAX_VALUE;
            long maxQuerySidePosition = 0L;
            
            for (ProjectionTracker tracker : trackers) {
                Long lastProcessed = tracker.getLastProcessedEventId() != null ? 
                        tracker.getLastProcessedEventId() : 0L;
                long lag = totalEvents - lastProcessed;
                
                lagByProjection.put(tracker.getProjectionName(), lag);
                minQuerySidePosition = Math.min(minQuerySidePosition, lastProcessed);
                maxQuerySidePosition = Math.max(maxQuerySidePosition, lastProcessed);
            }
            
            // Lag geral (baseado na projeção mais atrasada)
            long overallLag = totalEvents - minQuerySidePosition;
            
            lagInfo.put("commandSideEvents", totalEvents);
            lagInfo.put("querySideMinPosition", minQuerySidePosition);
            lagInfo.put("querySideMaxPosition", maxQuerySidePosition);
            lagInfo.put("overallLag", overallLag);
            lagInfo.put("lagByProjection", lagByProjection);
            
            // Determinar status do lag
            if (overallLag <= HIGH_LAG_THRESHOLD) {
                lagInfo.put("status", "HEALTHY");
            } else if (overallLag <= CRITICAL_LAG_THRESHOLD) {
                lagInfo.put("status", "HIGH_LAG");
            } else {
                lagInfo.put("status", "CRITICAL_LAG");
            }
            
        } catch (Exception e) {
            log.error("Erro ao verificar lag CQRS", e);
            lagInfo.put("status", "ERROR");
            lagInfo.put("error", e.getMessage());
        }
        
        return lagInfo;
    }
    
    /**
     * Verifica a saúde das projeções.
     */
    private Map<String, Object> checkProjectionsHealth() {
        Map<String, Object> projectionsInfo = new HashMap<>();
        
        try {
            List<ProjectionTracker> trackers = projectionTrackerRepository.findAll();
            
            if (trackers.isEmpty()) {
                projectionsInfo.put("status", "NO_PROJECTIONS");
                projectionsInfo.put("totalProjections", 0);
                return projectionsInfo;
            }
            
            // Estatísticas por status
            Map<ProjectionStatus, Long> statusCounts = trackers.stream()
                    .collect(Collectors.groupingBy(
                            ProjectionTracker::getStatus,
                            Collectors.counting()
                    ));
            
            // Projeções com problemas
            List<String> errorProjections = trackers.stream()
                    .filter(t -> t.getStatus() == ProjectionStatus.ERROR)
                    .map(ProjectionTracker::getProjectionName)
                    .collect(Collectors.toList());
            
            // Projeções obsoletas (não atualizadas há muito tempo)
            Instant staleThreshold = Instant.now().minus(Duration.ofMinutes(STALE_PROJECTION_MINUTES));
            List<String> staleProjections = trackers.stream()
                    .filter(t -> t.getUpdatedAt().isBefore(staleThreshold))
                    .map(ProjectionTracker::getProjectionName)
                    .collect(Collectors.toList());
            
            // Taxa de erro geral
            double totalEvents = trackers.stream()
                    .mapToLong(t -> t.getEventsProcessed() != null ? t.getEventsProcessed() : 0L)
                    .sum();
            double totalErrors = trackers.stream()
                    .mapToLong(t -> t.getEventsFailed() != null ? t.getEventsFailed() : 0L)
                    .sum();
            double errorRate = totalEvents > 0 ? totalErrors / totalEvents : 0.0;
            
            projectionsInfo.put("totalProjections", trackers.size());
            projectionsInfo.put("statusCounts", statusCounts);
            projectionsInfo.put("errorProjections", errorProjections);
            projectionsInfo.put("staleProjections", staleProjections);
            projectionsInfo.put("errorRate", errorRate);
            
            // Determinar status geral das projeções
            if (!errorProjections.isEmpty() || errorRate > CRITICAL_ERROR_RATE_THRESHOLD) {
                projectionsInfo.put("status", "CRITICAL");
            } else if (!staleProjections.isEmpty() || errorRate > HIGH_ERROR_RATE_THRESHOLD) {
                projectionsInfo.put("status", "DEGRADED");
            } else {
                projectionsInfo.put("status", "HEALTHY");
            }
            
        } catch (Exception e) {
            log.error("Erro ao verificar saúde das projeções", e);
            projectionsInfo.put("status", "ERROR");
            projectionsInfo.put("error", e.getMessage());
        }
        
        return projectionsInfo;
    }
    
    /**
     * Verifica a saúde dos datasources.
     */
    private Map<String, Object> checkDatasourcesHealth() {
        Map<String, Object> datasourcesInfo = new HashMap<>();
        
        try {
            // Testar Write DataSource (Command Side)
            boolean writeHealthy = testWriteDataSource();
            datasourcesInfo.put("writeDataSource", writeHealthy ? "UP" : "DOWN");
            
            // Testar Read DataSource (Query Side)
            boolean readHealthy = testReadDataSource();
            datasourcesInfo.put("readDataSource", readHealthy ? "UP" : "DOWN");
            
            // Status geral dos datasources
            if (writeHealthy && readHealthy) {
                datasourcesInfo.put("status", "HEALTHY");
            } else if (writeHealthy || readHealthy) {
                datasourcesInfo.put("status", "DEGRADED");
            } else {
                datasourcesInfo.put("status", "DOWN");
            }
            
        } catch (Exception e) {
            log.error("Erro ao verificar saúde dos datasources", e);
            datasourcesInfo.put("status", "ERROR");
            datasourcesInfo.put("error", e.getMessage());
        }
        
        return datasourcesInfo;
    }
    
    /**
     * Obtém métricas gerais do sistema.
     */
    private Map<String, Object> getGeneralMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Métricas do Event Store
            long totalEvents = eventStoreRepository.count();
            // Usar uma query simples para contar aggregates distintos
            // em vez de countDistinctAggregateIds() que não existe
            long totalAggregates = eventStoreRepository.findAll().stream()
                    .map(entry -> entry.getAggregateId())
                    .distinct()
                    .count();
            
            metrics.put("totalEvents", totalEvents);
            metrics.put("totalAggregates", totalAggregates);
            
            // Métricas das projeções
            List<ProjectionTracker> trackers = projectionTrackerRepository.findAll();
            long totalProjections = trackers.size();
            long activeProjections = trackers.stream()
                    .mapToLong(t -> t.getStatus() == ProjectionStatus.ACTIVE ? 1L : 0L)
                    .sum();
            
            metrics.put("totalProjections", totalProjections);
            metrics.put("activeProjections", activeProjections);
            
        } catch (Exception e) {
            log.error("Erro ao obter métricas gerais", e);
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }
    
    /**
     * Testa conectividade do Write DataSource.
     */
    private boolean testWriteDataSource() {
        try {
            eventStoreRepository.count();
            return true;
        } catch (Exception e) {
            log.warn("Write DataSource não está saudável: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Testa conectividade do Read DataSource.
     */
    private boolean testReadDataSource() {
        try {
            projectionTrackerRepository.count();
            return true;
        } catch (Exception e) {
            log.warn("Read DataSource não está saudável: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Determina se o sistema está saudável baseado nos detalhes.
     */
    private boolean isHealthy(Map<String, Object> details) {
        try {
            // Verificar lag
            @SuppressWarnings("unchecked")
            Map<String, Object> lagInfo = (Map<String, Object>) details.get("lag");
            String lagStatus = (String) lagInfo.get("status");
            if ("CRITICAL_LAG".equals(lagStatus) || "ERROR".equals(lagStatus)) {
                return false;
            }
            
            // Verificar projeções
            @SuppressWarnings("unchecked")
            Map<String, Object> projectionsInfo = (Map<String, Object>) details.get("projections");
            String projectionsStatus = (String) projectionsInfo.get("status");
            if ("CRITICAL".equals(projectionsStatus) || "ERROR".equals(projectionsStatus)) {
                return false;
            }
            
            // Verificar datasources
            @SuppressWarnings("unchecked")
            Map<String, Object> datasourcesInfo = (Map<String, Object>) details.get("datasources");
            String datasourcesStatus = (String) datasourcesInfo.get("status");
            if ("DOWN".equals(datasourcesStatus) || "ERROR".equals(datasourcesStatus)) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao determinar saúde geral", e);
            return false;
        }
    }
    
    /**
     * Determina o status geral do sistema.
     */
    private String determineOverallStatus(Map<String, Object> details) {
        if (!isHealthy(details)) {
            return "DOWN";
        }
        
        try {
            // Verificar se há degradação
            @SuppressWarnings("unchecked")
            Map<String, Object> lagInfo = (Map<String, Object>) details.get("lag");
            String lagStatus = (String) lagInfo.get("status");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> projectionsInfo = (Map<String, Object>) details.get("projections");
            String projectionsStatus = (String) projectionsInfo.get("status");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> datasourcesInfo = (Map<String, Object>) details.get("datasources");
            String datasourcesStatus = (String) datasourcesInfo.get("status");
            
            if ("HIGH_LAG".equals(lagStatus) || 
                "DEGRADED".equals(projectionsStatus) || 
                "DEGRADED".equals(datasourcesStatus)) {
                return "DEGRADED";
            }
            
            return "UP";
            
        } catch (Exception e) {
            log.error("Erro ao determinar status geral", e);
            return "DOWN";
        }
    }
}