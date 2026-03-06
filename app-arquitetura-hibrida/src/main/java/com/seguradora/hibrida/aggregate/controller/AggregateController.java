package com.seguradora.hibrida.aggregate.controller;

import com.seguradora.hibrida.aggregate.health.AggregateHealthIndicator;
import com.seguradora.hibrida.aggregate.metrics.AggregateMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para monitoramento e administração do sistema de Aggregates.
 * 
 * <p>Fornece endpoints para:
 * <ul>
 *   <li>Verificação de saúde dos componentes</li>
 *   <li>Consulta de métricas de performance</li>
 *   <li>Estatísticas de uso</li>
 *   <li>Informações de configuração</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/aggregates")
@Tag(name = "Aggregate System", description = "APIs para monitoramento do sistema de Aggregates")
public class AggregateController {
    
    private final AggregateHealthIndicator healthIndicator;
    private final AggregateMetrics metrics;
    
    @Autowired
    public AggregateController(AggregateHealthIndicator healthIndicator,
                             AggregateMetrics metrics) {
        this.healthIndicator = healthIndicator;
        this.metrics = metrics;
    }
    
    /**
     * Verifica a saúde geral do sistema de aggregates.
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health Check do Sistema de Aggregates",
            description = "Verifica a saúde de todos os componentes do sistema de aggregates"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status de saúde obtido com sucesso"),
            @ApiResponse(responseCode = "503", description = "Sistema não está saudável")
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Health health = healthIndicator.health();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", health.getStatus().getCode());
            response.put("details", health.getDetails());
            
            // Retornar 503 se não estiver UP
            if (!"UP".equals(health.getStatus().getCode())) {
                return ResponseEntity.status(503).body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao verificar saúde dos aggregates: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Obtém métricas detalhadas do sistema de aggregates.
     */
    @GetMapping("/metrics")
    @Operation(
            summary = "Métricas do Sistema de Aggregates",
            description = "Retorna métricas detalhadas de performance e uso"
    )
    @ApiResponse(responseCode = "200", description = "Métricas obtidas com sucesso")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        try {
            AggregateMetrics.MetricsStatistics stats = metrics.getStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("statistics", Map.of(
                    "totalSaves", stats.getTotalSaves(),
                    "totalLoads", stats.getTotalLoads(),
                    "totalSnapshots", stats.getTotalSnapshots(),
                    "totalValidations", stats.getTotalValidations(),
                    "totalErrors", stats.getTotalErrors()
            ));
            
            response.put("performance", Map.of(
                    "averageSaveTimeMs", stats.getAverageSaveTime(),
                    "averageLoadTimeMs", stats.getAverageLoadTime(),
                    "averageReconstructionTimeMs", stats.getAverageReconstructionTime(),
                    "averageValidationTimeMs", stats.getAverageValidationTime()
            ));
            
            response.put("timestamp", java.time.Instant.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao obter métricas dos aggregates: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Obtém status rápido do sistema.
     */
    @GetMapping("/status")
    @Operation(
            summary = "Status Rápido",
            description = "Retorna status resumido do sistema de aggregates"
    )
    @ApiResponse(responseCode = "200", description = "Status obtido com sucesso")
    public ResponseEntity<Map<String, Object>> getQuickStatus() {
        try {
            Health health = healthIndicator.health();
            AggregateMetrics.MetricsStatistics stats = metrics.getStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", health.getStatus().getCode());
            response.put("totalOperations", stats.getTotalSaves() + stats.getTotalLoads());
            response.put("errorRate", calculateErrorRate(stats));
            response.put("uptime", getUptime());
            response.put("timestamp", java.time.Instant.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao obter status rápido: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Obtém informações de configuração.
     */
    @GetMapping("/configuration")
    @Operation(
            summary = "Configuração do Sistema",
            description = "Retorna informações sobre a configuração atual"
    )
    @ApiResponse(responseCode = "200", description = "Configuração obtida com sucesso")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Informações básicas (sem dados sensíveis)
            response.put("version", "1.0.0");
            response.put("javaVersion", System.getProperty("java.version"));
            response.put("springBootVersion", org.springframework.boot.SpringBootVersion.getVersion());
            
            // Configurações gerais (sem valores específicos)
            response.put("features", Map.of(
                    "metricsEnabled", true,
                    "healthCheckEnabled", true,
                    "snapshotSupport", true,
                    "validationEnabled", true,
                    "cacheEnabled", true
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao obter configuração: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Calcula taxa de erro baseada nas estatísticas.
     */
    private double calculateErrorRate(AggregateMetrics.MetricsStatistics stats) {
        long totalOperations = stats.getTotalSaves() + stats.getTotalLoads();
        if (totalOperations == 0) {
            return 0.0;
        }
        
        return (double) stats.getTotalErrors() / totalOperations * 100;
    }
    
    /**
     * Obtém tempo de atividade da aplicação.
     */
    private String getUptime() {
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else {
            return String.format("%dm %ds", minutes, seconds % 60);
        }
    }
}