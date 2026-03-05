package com.seguradora.hibrida.snapshot.config;

import com.seguradora.hibrida.snapshot.SnapshotProperties;
import com.seguradora.hibrida.snapshot.SnapshotStatistics;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Health indicator para o sistema de snapshots.
 * 
 * <p>Verifica:
 * <ul>
 *   <li>Conectividade com o store de snapshots</li>
 *   <li>Performance das operações</li>
 *   <li>Estatísticas de saúde do sistema</li>
 *   <li>Alertas para problemas detectados</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SnapshotHealthIndicator {
    
    private final SnapshotStore snapshotStore;
    private final SnapshotProperties snapshotProperties;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    
    /**
     * Executa verificação de saúde do sistema de snapshots.
     * 
     * @return Resultado da verificação de saúde
     */
    public SnapshotHealthResult health() {
        try {
            Map<String, Object> details = checkHealth();
            
            boolean isHealthy = isHealthy(details);
            
            if (isHealthy) {
                consecutiveFailures.set(0);
                return SnapshotHealthResult.up(details);
            } else {
                int failures = consecutiveFailures.incrementAndGet();
                details.put("consecutiveFailures", failures);
                
                if (failures >= snapshotProperties.getMaxConsecutiveFailures()) {
                    return SnapshotHealthResult.down(details);
                } else {
                    details.put("warning", "Some health checks failed but within tolerance");
                    return SnapshotHealthResult.up(details);
                }
            }
        } catch (Exception e) {
            int failures = consecutiveFailures.incrementAndGet();
            
            log.error("Snapshot health check failed (failure #{}) ", failures, e);
            
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getMessage());
            details.put("consecutiveFailures", failures);
            details.put("maxFailures", snapshotProperties.getMaxConsecutiveFailures());
            
            return SnapshotHealthResult.down(details);
        }
    }
    
    /**
     * Executa verificações de saúde detalhadas.
     * 
     * @return Mapa com detalhes da verificação
     */
    private Map<String, Object> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        
        // Verificar conectividade básica
        long startTime = System.currentTimeMillis();
        SnapshotStatistics globalStats = snapshotStore.getGlobalStatistics();
        long responseTime = System.currentTimeMillis() - startTime;
        
        details.put("responseTimeMs", responseTime);
        details.put("totalSnapshots", globalStats.getTotalSnapshots());
        details.put("totalAggregates", globalStats.hasSnapshots() ? "available" : "none");
        
        // Verificar performance
        boolean performanceOk = responseTime < 5000; // 5 segundos
        details.put("performanceOk", performanceOk);
        
        // Verificar compressão
        double compressionRatio = globalStats.getOverallCompressionRatio();
        boolean compressionEffective = compressionRatio > 0.1; // Pelo menos 10%
        details.put("compressionRatio", Math.round(compressionRatio * 100) + "%");
        details.put("compressionEffective", compressionEffective);
        
        // Verificar atividade recente
        long recentSnapshots = globalStats.getSnapshotsLast24Hours();
        details.put("snapshotsLast24Hours", recentSnapshots);
        
        // Verificar espaço economizado
        long spaceSaved = globalStats.getTotalSpaceSaved();
        details.put("spaceSavedBytes", spaceSaved);
        details.put("spaceSavedMB", spaceSaved / (1024 * 1024));
        
        // Verificar configuração
        details.put("snapshotThreshold", snapshotProperties.getSnapshotThreshold());
        details.put("maxSnapshotsPerAggregate", snapshotProperties.getMaxSnapshotsPerAggregate());
        details.put("compressionEnabled", snapshotProperties.isCompressionEnabled());
        details.put("autoCleanupEnabled", snapshotProperties.isAutoCleanupEnabled());
        
        // Status geral
        details.put("status", "operational");
        details.put("lastCheck", Instant.now().toString());
        
        return details;
    }
    
    /**
     * Verifica se o sistema está saudável baseado nos detalhes.
     * 
     * @param details Detalhes da verificação
     * @return true se saudável, false caso contrário
     */
    private boolean isHealthy(Map<String, Object> details) {
        // Verificar tempo de resposta
        Long responseTime = (Long) details.get("responseTimeMs");
        if (responseTime == null || responseTime > 10000) { // 10 segundos
            log.warn("Snapshot health check: slow response time {}ms", responseTime);
            return false;
        }
        
        // Verificar se performance está OK
        Boolean performanceOk = (Boolean) details.get("performanceOk");
        if (performanceOk == null || !performanceOk) {
            log.warn("Snapshot health check: performance issues detected");
            return false;
        }
        
        // Verificar se há snapshots (se esperado)
        Long totalSnapshots = (Long) details.get("totalSnapshots");
        if (totalSnapshots == null) {
            log.warn("Snapshot health check: unable to retrieve snapshot count");
            return false;
        }
        
        // Tudo OK
        return true;
    }
    
    /**
     * Verifica se o sistema está operacional.
     * 
     * @return true se operacional, false caso contrário
     */
    public boolean isOperational() {
        try {
            SnapshotStatistics stats = snapshotStore.getGlobalStatistics();
            return stats != null;
        } catch (Exception e) {
            log.debug("Snapshot system not operational", e);
            return false;
        }
    }
    
    /**
     * Obtém estatísticas de saúde resumidas.
     * 
     * @return Mapa com estatísticas resumidas
     */
    public Map<String, Object> getHealthSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            SnapshotStatistics stats = snapshotStore.getGlobalStatistics();
            
            summary.put("operational", true);
            summary.put("totalSnapshots", stats.getTotalSnapshots());
            summary.put("compressionRatio", Math.round(stats.getOverallCompressionRatio() * 100) + "%");
            summary.put("spaceSavedMB", stats.getTotalSpaceSaved() / (1024 * 1024));
            summary.put("consecutiveFailures", consecutiveFailures.get());
            
        } catch (Exception e) {
            summary.put("operational", false);
            summary.put("error", e.getMessage());
            summary.put("consecutiveFailures", consecutiveFailures.get());
        }
        
        return summary;
    }
    
    /**
     * Classe para representar o resultado de health check.
     */
    public static class SnapshotHealthResult {
        private final String status;
        private final Map<String, Object> details;
        
        private SnapshotHealthResult(String status, Map<String, Object> details) {
            this.status = status;
            this.details = new HashMap<>(details);
        }
        
        public static SnapshotHealthResult up(Map<String, Object> details) {
            return new SnapshotHealthResult("UP", details);
        }
        
        public static SnapshotHealthResult down(Map<String, Object> details) {
            return new SnapshotHealthResult("DOWN", details);
        }
        
        public String getStatus() {
            return status;
        }
        
        public Map<String, Object> getDetails() {
            return new HashMap<>(details);
        }
        
        public boolean isUp() {
            return "UP".equals(status);
        }
        
        public boolean isDown() {
            return "DOWN".equals(status);
        }
    }
}