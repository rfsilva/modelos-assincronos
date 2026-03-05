package com.seguradora.hibrida.eventbus.controller;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventBusStatistics;
import com.seguradora.hibrida.eventbus.EventHandlerRegistry;
import com.seguradora.hibrida.eventbus.config.EventBusHealthIndicator;
import com.seguradora.hibrida.eventbus.config.EventBusMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para monitoramento e administração do Event Bus.
 * 
 * <p>Fornece endpoints para:
 * <ul>
 *   <li>Consultar estatísticas de execução</li>
 *   <li>Verificar saúde do sistema</li>
 *   <li>Listar handlers registrados</li>
 *   <li>Administrar configurações</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/event-bus")
@Tag(name = "Event Bus", description = "APIs para monitoramento e administração do Event Bus")
public class EventBusController {
    
    private static final Logger log = LoggerFactory.getLogger(EventBusController.class);
    
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private EventHandlerRegistry handlerRegistry;
    
    @Autowired(required = false)
    private EventBusHealthIndicator healthIndicator;
    
    @Autowired(required = false)
    private EventBusMetrics metrics;
    
    /**
     * Obtém estatísticas gerais do Event Bus.
     */
    @GetMapping("/statistics")
    @Operation(summary = "Obter estatísticas gerais", 
               description = "Retorna estatísticas detalhadas de execução do Event Bus")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            EventBusStatistics stats = eventBus.getStatistics();
            Map<String, Object> response = new HashMap<>();
            
            // Estatísticas básicas
            response.put("eventsPublished", stats.getEventsPublished());
            response.put("eventsProcessed", stats.getEventsProcessed());
            response.put("eventsFailed", stats.getEventsFailed());
            response.put("eventsRetried", stats.getEventsRetried());
            response.put("eventsDeadLettered", stats.getEventsDeadLettered());
            
            // Taxas
            response.put("successRate", stats.getSuccessRate());
            response.put("errorRate", stats.getErrorRate());
            response.put("throughput", stats.getThroughput());
            
            // Tempos de processamento
            response.put("averageProcessingTimeMs", stats.getAverageProcessingTime());
            response.put("minProcessingTimeMs", stats.getMinProcessingTime());
            response.put("maxProcessingTimeMs", stats.getMaxProcessingTime());
            
            // Handlers
            response.put("activeHandlers", stats.getActiveHandlers());
            response.put("maxConcurrentHandlers", stats.getMaxConcurrentHandlers());
            
            // Timestamps
            response.put("startTime", stats.getStartTime());
            response.put("lastEventTime", stats.getLastEventTime());
            
            // Estatísticas por tipo
            response.put("eventsByType", stats.getEventsByType());
            response.put("failuresByType", stats.getFailuresByType());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting Event Bus statistics", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Obtém estatísticas resumidas do Event Bus.
     */
    @GetMapping("/statistics/summary")
    @Operation(summary = "Obter resumo das estatísticas", 
               description = "Retorna um resumo das principais métricas do Event Bus")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resumo obtido com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> getStatisticsSummary() {
        try {
            EventBusStatistics stats = eventBus.getStatistics();
            Map<String, Object> summary = new HashMap<>();
            
            summary.put("totalEvents", stats.getEventsPublished());
            summary.put("successfulEvents", stats.getEventsProcessed());
            summary.put("failedEvents", stats.getEventsFailed());
            summary.put("successRate", String.format("%.2f%%", stats.getSuccessRate() * 100));
            summary.put("throughput", String.format("%.2f events/sec", stats.getThroughput()));
            summary.put("avgProcessingTime", String.format("%.2f ms", stats.getAverageProcessingTime()));
            summary.put("activeHandlers", stats.getActiveHandlers());
            summary.put("healthy", eventBus.isHealthy());
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error getting Event Bus statistics summary", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get statistics summary: " + e.getMessage()));
        }
    }
    
    /**
     * Verifica a saúde do Event Bus.
     */
    @GetMapping("/health")
    @Operation(summary = "Verificar saúde do Event Bus", 
               description = "Retorna informações detalhadas sobre a saúde do Event Bus")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status de saúde obtido com sucesso"),
        @ApiResponse(responseCode = "503", description = "Event Bus não está saudável"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            
            if (healthIndicator != null) {
                Map<String, Object> healthStatus = healthIndicator.checkHealth();
                health.putAll(healthStatus);
                
                boolean isHealthy = Boolean.TRUE.equals(healthStatus.get("healthy"));
                
                return isHealthy ? 
                    ResponseEntity.ok(health) : 
                    ResponseEntity.status(503).body(health);
                    
            } else {
                // Fallback para verificação básica
                boolean isHealthy = eventBus.isHealthy();
                health.put("status", isHealthy ? "UP" : "DOWN");
                health.put("healthy", isHealthy);
                
                return isHealthy ? 
                    ResponseEntity.ok(health) : 
                    ResponseEntity.status(503).body(health);
            }
            
        } catch (Exception e) {
            log.error("Error checking Event Bus health", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "DOWN",
                        "healthy", false,
                        "error", "Failed to check health: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * Obtém status rápido do Event Bus.
     */
    @GetMapping("/status")
    @Operation(summary = "Obter status rápido", 
               description = "Retorna um status rápido do Event Bus para dashboards")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status obtido com sucesso")
    })
    public ResponseEntity<Map<String, Object>> getQuickStatus() {
        try {
            if (healthIndicator != null) {
                return ResponseEntity.ok(healthIndicator.getQuickStatus());
            } else {
                // Fallback básico
                Map<String, Object> status = new HashMap<>();
                status.put("healthy", eventBus.isHealthy());
                
                EventBusStatistics stats = eventBus.getStatistics();
                if (stats != null) {
                    status.put("eventsProcessed", stats.getEventsProcessed());
                    status.put("errorRate", String.format("%.2f%%", stats.getErrorRate() * 100));
                }
                
                return ResponseEntity.ok(status);
            }
            
        } catch (Exception e) {
            log.error("Error getting Event Bus quick status", e);
            return ResponseEntity.ok(Map.of(
                "healthy", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Lista todos os handlers registrados.
     */
    @GetMapping("/handlers")
    @Operation(summary = "Listar handlers registrados", 
               description = "Retorna informações sobre todos os handlers de eventos registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Handlers listados com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> getRegisteredHandlers() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Estatísticas do registry
            response.put("totalHandlers", handlerRegistry.getTotalHandlers());
            response.put("eventTypesCount", handlerRegistry.getEventTypesCount());
            response.put("lastRegistrationTime", handlerRegistry.getLastRegistrationTime());
            
            // Tipos de eventos registrados
            response.put("registeredEventTypes", 
                handlerRegistry.getRegisteredEventTypes().stream()
                    .map(Class::getSimpleName)
                    .sorted()
                    .toList()
            );
            
            // Estatísticas detalhadas
            response.put("statistics", handlerRegistry.getStatistics());
            
            // Validação da configuração
            var issues = handlerRegistry.validateConfiguration();
            if (!issues.isEmpty()) {
                response.put("configurationIssues", issues);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting registered handlers", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get handlers: " + e.getMessage()));
        }
    }
    
    /**
     * Reseta as estatísticas do Event Bus.
     */
    @PostMapping("/statistics/reset")
    @Operation(summary = "Resetar estatísticas", 
               description = "Reseta todas as estatísticas do Event Bus")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estatísticas resetadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> resetStatistics() {
        try {
            EventBusStatistics stats = eventBus.getStatistics();
            if (stats != null) {
                stats.reset();
            }
            
            if (metrics != null) {
                metrics.reset();
            }
            
            log.info("Event Bus statistics reset by admin request");
            
            return ResponseEntity.ok(Map.of(
                "message", "Statistics reset successfully",
                "timestamp", java.time.Instant.now()
            ));
            
        } catch (Exception e) {
            log.error("Error resetting Event Bus statistics", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to reset statistics: " + e.getMessage()));
        }
    }
}