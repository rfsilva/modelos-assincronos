package com.seguradora.hibrida.eventstore.replay.config;

import com.seguradora.hibrida.eventstore.replay.EventReplayer;
import com.seguradora.hibrida.eventstore.replay.ReplayProgress;
import com.seguradora.hibrida.eventstore.replay.ReplayStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Health indicator para o sistema de replay de eventos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ReplayHealthIndicator implements HealthIndicator {
    
    private final EventReplayer eventReplayer;
    
    @Override
    public Health health() {
        try {
            Map<String, Object> details = checkHealth();
            String status = determineOverallStatus(details);
            
            if ("UP".equals(status)) {
                return Health.up().withDetails(details).build();
            } else {
                return Health.down().withDetails(details).build();
            }
            
        } catch (Exception e) {
            log.error("Erro ao verificar saúde do sistema de replay", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("timestamp", java.time.Instant.now())
                .build();
        }
    }
    
    private Map<String, Object> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        
        // Verifica se o replayer está operacional
        boolean isHealthy = eventReplayer.isHealthy();
        details.put("replayer_operational", isHealthy);
        
        // Estatísticas gerais
        ReplayStatistics stats = eventReplayer.getStatistics();
        details.put("total_replays_executed", stats.getTotalReplaysExecuted());
        details.put("success_rate", String.format("%.2f%%", stats.getOverallSuccessRate()));
        details.put("error_rate", String.format("%.2f%%", stats.getOverallErrorRate()));
        
        // Replays ativos
        List<ReplayProgress> activeReplays = eventReplayer.getActiveReplays();
        details.put("active_replays_count", activeReplays.size());
        
        // Verifica se há replays com problemas
        long failedActiveReplays = activeReplays.stream()
            .mapToLong(progress -> progress.getStatus() == ReplayProgress.Status.FAILED ? 1 : 0)
            .sum();
        details.put("failed_active_replays", failedActiveReplays);
        
        // Verifica replays pausados há muito tempo
        long stalledReplays = activeReplays.stream()
            .filter(progress -> progress.getStatus() == ReplayProgress.Status.PAUSED)
            .filter(progress -> {
                java.time.Duration elapsed = java.time.Duration.between(
                    progress.getLastUpdatedAt(), 
                    java.time.Instant.now()
                );
                return elapsed.toMinutes() > 30; // Pausado há mais de 30 minutos
            })
            .count();
        details.put("stalled_replays", stalledReplays);
        
        // Status dos componentes
        details.put("components", checkComponentsHealth());
        
        // Timestamp da verificação
        details.put("check_timestamp", java.time.Instant.now());
        
        return details;
    }
    
    private Map<String, Object> checkComponentsHealth() {
        Map<String, Object> components = new HashMap<>();
        
        try {
            // Verifica componentes básicos
            components.put("event_replayer", eventReplayer != null ? "UP" : "DOWN");
            
            // Verifica se consegue obter estatísticas
            ReplayStatistics stats = eventReplayer.getStatistics();
            components.put("statistics_service", stats != null ? "UP" : "DOWN");
            
            // Verifica se consegue listar replays ativos
            List<ReplayProgress> activeReplays = eventReplayer.getActiveReplays();
            components.put("active_replays_service", activeReplays != null ? "UP" : "DOWN");
            
        } catch (Exception e) {
            log.warn("Erro ao verificar componentes do replay", e);
            components.put("error", e.getMessage());
        }
        
        return components;
    }
    
    private String determineOverallStatus(Map<String, Object> details) {
        // Verifica se o replayer está operacional
        Boolean replayerOperational = (Boolean) details.get("replayer_operational");
        if (replayerOperational == null || !replayerOperational) {
            return "DOWN";
        }
        
        // Verifica se há muitos replays falhados ativos
        Long failedActiveReplays = (Long) details.get("failed_active_replays");
        if (failedActiveReplays != null && failedActiveReplays > 0) {
            return "DOWN";
        }
        
        // Verifica se há replays travados
        Long stalledReplays = (Long) details.get("stalled_replays");
        if (stalledReplays != null && stalledReplays > 2) {
            return "DOWN";
        }
        
        // Verifica taxa de erro geral
        ReplayStatistics stats = eventReplayer.getStatistics();
        if (stats.getOverallErrorRate() > 50.0) { // Mais de 50% de erro
            return "DOWN";
        }
        
        return "UP";
    }
    
    /**
     * Verifica se o sistema está saudável.
     * 
     * @return true se saudável
     */
    public boolean isHealthy() {
        try {
            Map<String, Object> details = checkHealth();
            return "UP".equals(determineOverallStatus(details));
        } catch (Exception e) {
            log.error("Erro ao verificar saúde", e);
            return false;
        }
    }
}