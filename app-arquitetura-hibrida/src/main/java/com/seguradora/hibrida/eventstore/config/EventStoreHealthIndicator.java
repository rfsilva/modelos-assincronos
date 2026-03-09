package com.seguradora.hibrida.eventstore.config;

import com.seguradora.hibrida.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Health indicator para monitoramento da saúde do Event Store.
 * 
 * Verifica se o Event Store está funcionando corretamente
 * através de operações básicas de teste.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Component("eventStoreHealthIndicator")
@RequiredArgsConstructor
public class EventStoreHealthIndicator {
    
    private final EventStore eventStore;
    
    /**
     * Verifica a saúde do Event Store.
     * 
     * @return Map com informações de saúde
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Testa operação básica de leitura
            long startTime = System.currentTimeMillis();
            
            // Tenta verificar se um aggregate fictício existe
            String testAggregateId = "health-check-" + UUID.randomUUID();
            boolean exists = eventStore.aggregateExists(testAggregateId);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            health.put("status", "UP");
            health.put("details", "Event Store operacional");
            health.put("responseTime", responseTime + "ms");
            health.put("timestamp", Instant.now());
            health.put("testAggregateExists", exists);
            
            return health;
                    
        } catch (Exception e) {
            log.error("Falha no health check do Event Store", e);
            
            health.put("status", "DOWN");
            health.put("details", "Event Store indisponível");
            health.put("error", e.getMessage());
            health.put("timestamp", Instant.now());
            
            return health;
        }
    }
    
    /**
     * Verifica se o Event Store está saudável.
     * 
     * @return true se saudável, false caso contrário
     */
    public boolean isHealthy() {
        try {
            Map<String, Object> health = checkHealth();
            return "UP".equals(health.get("status"));
        } catch (Exception e) {
            log.error("Erro ao verificar saúde do Event Store", e);
            return false;
        }
    }
}