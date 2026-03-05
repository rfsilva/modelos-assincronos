package com.seguradora.hibrida.command.config;

import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandBusStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health indicator para o Command Bus.
 * 
 * <p>Monitora a saúde do Command Bus verificando:</p>
 * <ul>
 *   <li>Número de handlers registrados</li>
 *   <li>Taxa de erro dos comandos</li>
 *   <li>Tempo médio de execução</li>
 *   <li>Comandos processados recentemente</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@Slf4j
public class CommandBusHealthIndicator {
    
    private final CommandBus commandBus;
    
    // Thresholds para determinar saúde
    private static final double MAX_ERROR_RATE = 10.0; // 10%
    private static final long MAX_AVG_EXECUTION_TIME_MS = 5000; // 5 segundos
    private static final int MIN_HANDLERS = 1;
    
    /**
     * Construtor com injeção de dependências.
     * 
     * @param commandBus Command Bus a ser monitorado
     */
    public CommandBusHealthIndicator(CommandBus commandBus) {
        this.commandBus = commandBus;
    }
    
    /**
     * Verifica a saúde do Command Bus.
     * 
     * @return Mapa com status de saúde
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            CommandBusStatistics stats = commandBus.getStatistics();
            
            // Informações básicas
            health.put("registeredHandlers", stats.getRegisteredHandlers());
            health.put("totalCommands", stats.getTotalCommands());
            health.put("commandsProcessed", stats.getTotalCommandsProcessed().get());
            health.put("commandsFailed", stats.getTotalCommandsFailed().get());
            health.put("commandsTimedOut", stats.getTotalCommandsTimedOut().get());
            health.put("commandsRejected", stats.getTotalCommandsRejected().get());
            
            // Métricas de performance
            health.put("successRate", String.format("%.2f%%", stats.getSuccessRate()));
            health.put("errorRate", String.format("%.2f%%", stats.getErrorRate()));
            health.put("averageExecutionTimeMs", String.format("%.2f", stats.getAverageExecutionTimeMs()));
            health.put("minExecutionTimeMs", stats.getMinExecutionTimeMs());
            health.put("maxExecutionTimeMs", stats.getMaxExecutionTimeMs());
            health.put("throughputPerSecond", String.format("%.2f", stats.getThroughputPerSecond()));
            
            // Timestamps
            health.put("lastUpdated", stats.getLastUpdated());
            health.put("startedAt", stats.getStartedAt());
            health.put("uptime", formatDuration(Duration.between(stats.getStartedAt(), Instant.now())));
            
            // Verificar condições de saúde
            boolean isHealthy = true;
            StringBuilder issues = new StringBuilder();
            
            // Verificar se há handlers registrados
            if (stats.getRegisteredHandlers() < MIN_HANDLERS) {
                isHealthy = false;
                issues.append("Poucos handlers registrados (").append(stats.getRegisteredHandlers()).append("); ");
            }
            
            // Verificar taxa de erro
            double errorRate = stats.getErrorRate();
            if (errorRate > MAX_ERROR_RATE) {
                isHealthy = false;
                issues.append("Taxa de erro alta (").append(String.format("%.2f%%", errorRate)).append("); ");
            }
            
            // Verificar tempo médio de execução
            double avgExecutionTime = stats.getAverageExecutionTimeMs();
            if (avgExecutionTime > MAX_AVG_EXECUTION_TIME_MS && stats.getTotalCommands() > 0) {
                isHealthy = false;
                issues.append("Tempo médio de execução alto (")
                       .append(String.format("%.2fms", avgExecutionTime)).append("); ");
            }
            
            // Verificar se há atividade recente (últimos 5 minutos)
            if (stats.getLastUpdated() != null) {
                Duration timeSinceLastUpdate = Duration.between(stats.getLastUpdated(), Instant.now());
                if (timeSinceLastUpdate.toMinutes() > 5 && stats.getTotalCommands() > 0) {
                    health.put("warning", "Nenhuma atividade nos últimos 5 minutos");
                }
            }
            
            // Adicionar detalhes sobre problemas encontrados
            if (!isHealthy) {
                health.put("issues", issues.toString());
            }
            
            // Status final
            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("healthy", isHealthy);
            
        } catch (Exception e) {
            log.error("Erro ao verificar saúde do Command Bus", e);
            health.put("status", "DOWN");
            health.put("healthy", false);
            health.put("error", e.getMessage());
            health.put("timestamp", Instant.now());
        }
        
        return health;
    }
    
    /**
     * Verifica se o Command Bus está operacional.
     * 
     * @return true se operacional, false caso contrário
     */
    public boolean isOperational() {
        try {
            Map<String, Object> health = checkHealth();
            return Boolean.TRUE.equals(health.get("healthy"));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Formata duração para exibição.
     * 
     * @param duration Duração a ser formatada
     * @return String formatada
     */
    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        
        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}