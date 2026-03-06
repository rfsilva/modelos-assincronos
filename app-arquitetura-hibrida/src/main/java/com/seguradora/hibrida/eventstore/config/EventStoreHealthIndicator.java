package com.seguradora.hibrida.eventstore.config;

import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.archive.EventArchiver;
import com.seguradora.hibrida.eventstore.partition.PartitionManager;
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
 * através de operações básicas de teste, incluindo particionamento
 * e arquivamento.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Component("eventStoreHealthIndicator")
@RequiredArgsConstructor
public class EventStoreHealthIndicator {
    
    private final EventStore eventStore;
    private final PartitionManager partitionManager;
    private final EventArchiver eventArchiver;
    
    /**
     * Verifica a saúde do Event Store.
     * 
     * @return Map com informações de saúde
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Testa operação básica de leitura do Event Store
            long startTime = System.currentTimeMillis();
            
            String testAggregateId = "health-check-" + UUID.randomUUID();
            boolean exists = eventStore.aggregateExists(testAggregateId);
            
            long eventStoreResponseTime = System.currentTimeMillis() - startTime;
            
            // Verifica saúde das partições
            boolean partitionsHealthy = partitionManager.arePartitionsHealthy();
            
            // Obtém estatísticas de arquivamento
            var archiveStats = eventArchiver.getArchiveStatistics();
            
            // Determina status geral
            boolean isHealthy = partitionsHealthy;
            String status = isHealthy ? "UP" : "DOWN";
            
            health.put("status", status);
            health.put("timestamp", Instant.now());
            
            // Detalhes do Event Store
            health.put("eventStore", Map.of(
                "operational", true,
                "responseTime", eventStoreResponseTime + "ms",
                "testResult", !exists // Deve ser false para aggregate fictício
            ));
            
            // Detalhes das partições
            health.put("partitions", Map.of(
                "healthy", partitionsHealthy,
                "status", partitionsHealthy ? "OK" : "NEEDS_MAINTENANCE"
            ));
            
            // Detalhes do arquivamento
            health.put("archiving", Map.of(
                "totalArchives", archiveStats.getTotalArchives() != null ? archiveStats.getTotalArchives() : 0,
                "totalEvents", archiveStats.getTotalEvents() != null ? archiveStats.getTotalEvents() : 0,
                "operational", true
            ));
            
            if (isHealthy) {
                health.put("details", "Event Store completamente operacional");
            } else {
                health.put("details", "Event Store com problemas nas partições");
                health.put("recommendation", "Execute manutenção de partições");
            }
            
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