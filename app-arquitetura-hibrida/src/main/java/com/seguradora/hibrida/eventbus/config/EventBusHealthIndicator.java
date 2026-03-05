package com.seguradora.hibrida.eventbus.config;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventBusStatistics;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health indicator para o Event Bus.
 * 
 * <p>Verifica a saúde do Event Bus baseado em:
 * <ul>
 *   <li>Status geral do Event Bus</li>
 *   <li>Taxa de erro</li>
 *   <li>Tempo desde o último evento</li>
 *   <li>Número de handlers ativos</li>
 *   <li>Throughput atual</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class EventBusHealthIndicator {
    
    private final EventBus eventBus;
    private final EventBusProperties properties;
    
    public EventBusHealthIndicator(EventBus eventBus, EventBusProperties properties) {
        this.eventBus = eventBus;
        this.properties = properties;
    }
    
    /**
     * Verifica a saúde do Event Bus.
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        boolean isHealthy = true;
        
        try {
            // Verificar status básico do Event Bus
            boolean basicHealth = eventBus.isHealthy();
            health.put("basicHealth", basicHealth);
            if (!basicHealth) {
                isHealthy = false;
                health.put("issue", "Event Bus reports unhealthy status");
            }
            
            // Obter estatísticas
            EventBusStatistics stats = eventBus.getStatistics();
            if (stats != null) {
                addStatisticsDetails(health, stats);
                
                // Verificar taxa de erro
                double errorRate = stats.getErrorRate();
                double errorThreshold = properties.getMonitoring().getErrorRateThreshold();
                health.put("errorRate", String.format("%.2f%%", errorRate * 100));
                health.put("errorThreshold", String.format("%.2f%%", errorThreshold * 100));
                
                if (errorRate > errorThreshold) {
                    isHealthy = false;
                    health.put("issue", String.format(
                        "Error rate %.2f%% exceeds threshold %.2f%%", 
                        errorRate * 100, errorThreshold * 100
                    ));
                }
                
                // Verificar se há atividade recente (apenas se houver eventos publicados)
                if (stats.getEventsPublished() > 0) {
                    Instant lastEventTime = stats.getLastEventTime();
                    Duration timeSinceLastEvent = Duration.between(lastEventTime, Instant.now());
                    long minutesSinceLastEvent = timeSinceLastEvent.toMinutes();
                    
                    health.put("minutesSinceLastEvent", minutesSinceLastEvent);
                    
                    // Considerar unhealthy se não há atividade há mais de 30 minutos
                    // (apenas em ambiente de produção com tráfego esperado)
                    if (minutesSinceLastEvent > 30 && stats.getEventsPublished() > 100) {
                        health.put("warning", String.format(
                            "No event activity for %d minutes", minutesSinceLastEvent
                        ));
                    }
                }
                
                // Verificar throughput
                double throughput = stats.getThroughput();
                health.put("throughputPerSecond", String.format("%.2f", throughput));
                
                // Verificar handlers ativos vs máximo
                long activeHandlers = stats.getActiveHandlers();
                long maxConcurrentHandlers = stats.getMaxConcurrentHandlers();
                health.put("activeHandlers", activeHandlers);
                health.put("maxConcurrentHandlers", maxConcurrentHandlers);
                
                // Se há muitos handlers ativos, pode indicar problema
                if (maxConcurrentHandlers > 0 && activeHandlers > maxConcurrentHandlers * 0.9) {
                    health.put("warning", String.format(
                        "High handler concurrency: %d active (max seen: %d)", 
                        activeHandlers, maxConcurrentHandlers
                    ));
                }
            } else {
                health.put("statistics", "Not available");
                health.put("warning", "Event Bus statistics not available");
            }
            
            // Adicionar detalhes de configuração
            addConfigurationDetails(health);
            
            // Status final
            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("healthy", isHealthy);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("healthy", false);
            health.put("error", "Failed to check Event Bus health: " + e.getMessage());
        }
        
        return health;
    }
    
    /**
     * Adiciona detalhes das estatísticas ao health check.
     */
    private void addStatisticsDetails(Map<String, Object> details, EventBusStatistics stats) {
        details.put("eventsPublished", stats.getEventsPublished());
        details.put("eventsProcessed", stats.getEventsProcessed());
        details.put("eventsFailed", stats.getEventsFailed());
        details.put("eventsRetried", stats.getEventsRetried());
        details.put("eventsDeadLettered", stats.getEventsDeadLettered());
        
        details.put("successRate", String.format("%.2f%%", stats.getSuccessRate() * 100));
        details.put("averageProcessingTimeMs", String.format("%.2f", stats.getAverageProcessingTime()));
        details.put("minProcessingTimeMs", stats.getMinProcessingTime());
        details.put("maxProcessingTimeMs", stats.getMaxProcessingTime());
        
        details.put("startTime", stats.getStartTime().toString());
        details.put("lastEventTime", stats.getLastEventTime().toString());
        
        // Estatísticas por tipo de evento (limitado para não sobrecarregar)
        Map<String, Long> eventsByType = stats.getEventsByType();
        if (!eventsByType.isEmpty()) {
            Map<String, Long> topEventTypes = eventsByType.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            java.util.LinkedHashMap::new
                    ));
            details.put("topEventTypes", topEventTypes);
        }
        
        Map<String, Long> failuresByType = stats.getFailuresByType();
        if (!failuresByType.isEmpty()) {
            details.put("failuresByType", failuresByType);
        }
    }
    
    /**
     * Adiciona detalhes de configuração ao health check.
     */
    private void addConfigurationDetails(Map<String, Object> details) {
        Map<String, Object> config = new HashMap<>();
        
        // Configurações de thread pool
        EventBusProperties.ThreadPool threadPool = properties.getThreadPool();
        config.put("threadPoolCoreSize", threadPool.getCoreSize());
        config.put("threadPoolMaxSize", threadPool.getMaxSize());
        config.put("threadPoolQueueCapacity", threadPool.getQueueCapacity());
        
        // Configurações de retry
        EventBusProperties.Retry retry = properties.getRetry();
        config.put("retryEnabled", retry.isEnabled());
        config.put("retryMaxAttempts", retry.getMaxAttempts());
        config.put("retryInitialDelayMs", retry.getInitialDelayMs());
        
        // Configurações de monitoramento
        EventBusProperties.Monitoring monitoring = properties.getMonitoring();
        config.put("metricsEnabled", monitoring.isMetricsEnabled());
        config.put("healthCheckEnabled", monitoring.isHealthCheckEnabled());
        config.put("errorRateThreshold", monitoring.getErrorRateThreshold());
        
        // Configurações de Kafka (se habilitado)
        EventBusProperties.Kafka kafka = properties.getKafka();
        config.put("kafkaEnabled", kafka.isEnabled());
        if (kafka.isEnabled()) {
            config.put("kafkaBootstrapServers", kafka.getBootstrapServers());
            config.put("kafkaDefaultTopic", kafka.getDefaultTopic());
        }
        
        details.put("configuration", config);
    }
    
    /**
     * Verifica se o Event Bus está operacional para processamento.
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
     * Obtém um resumo rápido do status do Event Bus.
     * 
     * @return Mapa com informações básicas de status
     */
    public Map<String, Object> getQuickStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            status.put("healthy", eventBus.isHealthy());
            
            EventBusStatistics stats = eventBus.getStatistics();
            if (stats != null) {
                status.put("eventsPublished", stats.getEventsPublished());
                status.put("eventsProcessed", stats.getEventsProcessed());
                status.put("errorRate", String.format("%.2f%%", stats.getErrorRate() * 100));
                status.put("throughput", String.format("%.2f/s", stats.getThroughput()));
                status.put("activeHandlers", stats.getActiveHandlers());
            }
            
        } catch (Exception e) {
            status.put("error", "Failed to get status: " + e.getMessage());
        }
        
        return status;
    }
}