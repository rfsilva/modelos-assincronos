package com.seguradora.hibrida.aggregate.health;

import com.seguradora.hibrida.aggregate.config.AggregateProperties;
import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Health Indicator para o sistema de Aggregates.
 * 
 * <p>Verifica a saúde dos componentes essenciais:
 * <ul>
 *   <li>Event Store - conectividade e performance</li>
 *   <li>Snapshot Store - disponibilidade e funcionalidade</li>
 *   <li>Sistema de validação - integridade</li>
 *   <li>Cache de handlers - eficiência</li>
 * </ul>
 * 
 * <p><strong>Status possíveis:</strong>
 * <ul>
 *   <li>UP - Todos os componentes funcionando normalmente</li>
 *   <li>DOWN - Falha crítica em componente essencial</li>
 *   <li>UNKNOWN - Não foi possível determinar o status</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
public class AggregateHealthIndicator implements HealthIndicator {
    
    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private final AggregateProperties properties;
    
    public AggregateHealthIndicator(EventStore eventStore, 
                                  SnapshotStore snapshotStore,
                                  AggregateProperties properties) {
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
        this.properties = properties;
    }
    
    @Override
    public Health health() {
        try {
            Map<String, Object> details = checkHealth();
            
            boolean isHealthy = isHealthy(details);
            String status = determineOverallStatus(details);
            
            Health.Builder builder = isHealthy ? Health.up() : Health.down();
            
            return builder
                    .withDetail("status", status)
                    .withDetails(details)
                    .build();
                    
        } catch (Exception e) {
            log.error("Erro ao verificar saúde dos aggregates: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", Instant.now())
                    .build();
        }
    }
    
    /**
     * Executa verificações de saúde de todos os componentes.
     */
    private Map<String, Object> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        
        // Verificar Event Store
        details.put("eventStore", checkEventStoreHealth());
        
        // Verificar Snapshot Store
        details.put("snapshotStore", checkSnapshotStoreHealth());
        
        // Verificar configurações
        details.put("configuration", checkConfigurationHealth());
        
        // Verificar performance
        details.put("performance", checkPerformanceHealth());
        
        // Informações gerais
        details.put("timestamp", Instant.now());
        details.put("version", "1.0.0");
        
        return details;
    }
    
    /**
     * Verifica saúde do Event Store.
     */
    private Map<String, Object> checkEventStoreHealth() {
        Map<String, Object> eventStoreHealth = new HashMap<>();
        
        try {
            Instant start = Instant.now();
            
            // Teste básico de conectividade
            CompletableFuture<Boolean> connectivityTest = CompletableFuture.supplyAsync(() -> {
                try {
                    // Tentar operação simples
                    eventStore.aggregateExists("health-check-test");
                    return true;
                } catch (Exception e) {
                    log.warn("Falha no teste de conectividade do Event Store: {}", e.getMessage());
                    return false;
                }
            });
            
            boolean isConnected = connectivityTest.get(
                    properties.getHealthCheck().getTimeoutSeconds(), 
                    TimeUnit.SECONDS
            );
            
            Duration responseTime = Duration.between(start, Instant.now());
            
            eventStoreHealth.put("connected", isConnected);
            eventStoreHealth.put("responseTimeMs", responseTime.toMillis());
            eventStoreHealth.put("status", isConnected ? "UP" : "DOWN");
            
            if (responseTime.toMillis() > 1000) {
                eventStoreHealth.put("warning", "Response time alto: " + responseTime.toMillis() + "ms");
            }
            
        } catch (Exception e) {
            eventStoreHealth.put("connected", false);
            eventStoreHealth.put("status", "DOWN");
            eventStoreHealth.put("error", e.getMessage());
        }
        
        return eventStoreHealth;
    }
    
    /**
     * Verifica saúde do Snapshot Store.
     */
    private Map<String, Object> checkSnapshotStoreHealth() {
        Map<String, Object> snapshotHealth = new HashMap<>();
        
        try {
            Instant start = Instant.now();
            
            // Teste básico de funcionalidade
            CompletableFuture<Boolean> functionalityTest = CompletableFuture.supplyAsync(() -> {
                try {
                    // Tentar operação simples
                    snapshotStore.hasSnapshots("health-check-test");
                    return true;
                } catch (Exception e) {
                    log.warn("Falha no teste de funcionalidade do Snapshot Store: {}", e.getMessage());
                    return false;
                }
            });
            
            boolean isFunctional = functionalityTest.get(
                    properties.getHealthCheck().getTimeoutSeconds(), 
                    TimeUnit.SECONDS
            );
            
            Duration responseTime = Duration.between(start, Instant.now());
            
            snapshotHealth.put("functional", isFunctional);
            snapshotHealth.put("responseTimeMs", responseTime.toMillis());
            snapshotHealth.put("status", isFunctional ? "UP" : "DOWN");
            
            // Verificar estatísticas se disponível
            try {
                var stats = snapshotStore.getGlobalStatistics();
                snapshotHealth.put("totalSnapshots", stats.getTotalSnapshots());
                snapshotHealth.put("compressionRatio", stats.getOverallCompressionRatio());
            } catch (Exception e) {
                snapshotHealth.put("statsError", "Não foi possível obter estatísticas");
            }
            
        } catch (Exception e) {
            snapshotHealth.put("functional", false);
            snapshotHealth.put("status", "DOWN");
            snapshotHealth.put("error", e.getMessage());
        }
        
        return snapshotHealth;
    }
    
    /**
     * Verifica saúde das configurações.
     */
    private Map<String, Object> checkConfigurationHealth() {
        Map<String, Object> configHealth = new HashMap<>();
        
        try {
            // Verificar propriedades essenciais
            configHealth.put("metricsEnabled", properties.getMetrics().isEnabled());
            configHealth.put("healthCheckEnabled", properties.getHealthCheck().isEnabled());
            configHealth.put("validationEnabled", properties.getValidation().isEnabled());
            configHealth.put("cacheEnabled", properties.getPerformance().isCacheHandlers());
            configHealth.put("snapshotAutoCreate", properties.getSnapshot().isAutoCreate());
            
            // Verificar valores críticos
            int timeoutSeconds = properties.getHealthCheck().getTimeoutSeconds();
            if (timeoutSeconds < 1 || timeoutSeconds > 30) {
                configHealth.put("warning", "Timeout de health check fora do range recomendado: " + timeoutSeconds);
            }
            
            int thresholdEvents = properties.getSnapshot().getThresholdEvents();
            if (thresholdEvents < 10 || thresholdEvents > 1000) {
                configHealth.put("warning", "Threshold de snapshot fora do range recomendado: " + thresholdEvents);
            }
            
            configHealth.put("status", "UP");
            
        } catch (Exception e) {
            configHealth.put("status", "DOWN");
            configHealth.put("error", e.getMessage());
        }
        
        return configHealth;
    }
    
    /**
     * Verifica saúde da performance.
     */
    private Map<String, Object> checkPerformanceHealth() {
        Map<String, Object> performanceHealth = new HashMap<>();
        
        try {
            // Verificar uso de memória
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
            
            performanceHealth.put("memoryUsagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0);
            performanceHealth.put("totalMemoryMB", totalMemory / 1024 / 1024);
            performanceHealth.put("usedMemoryMB", usedMemory / 1024 / 1024);
            
            // Alertas de performance
            if (memoryUsagePercent > 85) {
                performanceHealth.put("warning", "Uso de memória alto: " + memoryUsagePercent + "%");
            }
            
            // Verificar configurações de performance
            performanceHealth.put("cacheEnabled", properties.getPerformance().isCacheHandlers());
            performanceHealth.put("parallelValidation", properties.getPerformance().isParallelValidation());
            performanceHealth.put("optimizeReflection", properties.getPerformance().isOptimizeReflection());
            
            performanceHealth.put("status", "UP");
            
        } catch (Exception e) {
            performanceHealth.put("status", "DOWN");
            performanceHealth.put("error", e.getMessage());
        }
        
        return performanceHealth;
    }
    
    /**
     * Determina se o sistema está saudável baseado nos detalhes.
     */
    private boolean isHealthy(Map<String, Object> details) {
        try {
            // Event Store deve estar UP
            @SuppressWarnings("unchecked")
            Map<String, Object> eventStoreHealth = (Map<String, Object>) details.get("eventStore");
            if (!"UP".equals(eventStoreHealth.get("status"))) {
                return false;
            }
            
            // Snapshot Store deve estar UP (não crítico se DOWN)
            @SuppressWarnings("unchecked")
            Map<String, Object> snapshotHealth = (Map<String, Object>) details.get("snapshotStore");
            if (!"UP".equals(snapshotHealth.get("status"))) {
                log.warn("Snapshot Store não está saudável, mas não é crítico");
            }
            
            // Configuração deve estar UP
            @SuppressWarnings("unchecked")
            Map<String, Object> configHealth = (Map<String, Object>) details.get("configuration");
            if (!"UP".equals(configHealth.get("status"))) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao determinar saúde do sistema: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Determina o status geral do sistema.
     */
    private String determineOverallStatus(Map<String, Object> details) {
        if (isHealthy(details)) {
            return "UP";
        }
        
        // Verificar se é falha crítica ou não crítica
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> eventStoreHealth = (Map<String, Object>) details.get("eventStore");
            if (!"UP".equals(eventStoreHealth.get("status"))) {
                return "DOWN"; // Event Store é crítico
            }
            
            return "DEGRADED"; // Outros componentes com problema
            
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}