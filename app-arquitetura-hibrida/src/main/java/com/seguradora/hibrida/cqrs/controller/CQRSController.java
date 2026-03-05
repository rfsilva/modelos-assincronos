package com.seguradora.hibrida.cqrs.controller;

import com.seguradora.hibrida.cqrs.health.CQRSHealthIndicator;
import com.seguradora.hibrida.cqrs.metrics.CQRSMetrics;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionTracker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para monitoramento e administração do CQRS.
 * 
 * <p>Fornece endpoints para:
 * <ul>
 *   <li>Verificação de saúde do CQRS</li>
 *   <li>Métricas de lag e performance</li>
 *   <li>Status das projeções</li>
 *   <li>Administração das projeções</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/cqrs")
@Tag(name = "🔍 CQRS Monitoring", description = "APIs para monitoramento e administração do CQRS")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CQRSController {
    
    private static final Logger log = LoggerFactory.getLogger(CQRSController.class);
    
    private final CQRSHealthIndicator healthIndicator;
    private final CQRSMetrics metrics;
    private final ProjectionTrackerRepository projectionTrackerRepository;
    
    public CQRSController(CQRSHealthIndicator healthIndicator,
                         CQRSMetrics metrics,
                         ProjectionTrackerRepository projectionTrackerRepository) {
        this.healthIndicator = healthIndicator;
        this.metrics = metrics;
        this.projectionTrackerRepository = projectionTrackerRepository;
    }
    
    /**
     * Health check completo do CQRS.
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check do CQRS",
        description = "Verifica a saúde completa do sistema CQRS incluindo lag, projeções e datasources"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status de saúde retornado com sucesso"),
        @ApiResponse(responseCode = "503", description = "Sistema CQRS com problemas")
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        log.debug("GET /api/v1/cqrs/health");
        
        try {
            Health health = healthIndicator.health();
            Map<String, Object> response = new HashMap<>();
            
            response.put("status", health.getStatus().getCode());
            response.put("details", health.getDetails());
            response.put("timestamp", Instant.now());
            
            if ("UP".equals(health.getStatus().getCode()) || "DEGRADED".equals(health.getStatus().getCode())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(503).body(response);
            }
            
        } catch (Exception e) {
            log.error("Erro no health check do CQRS", e);
            
            Map<String, Object> errorResponse = Map.of(
                "status", "DOWN",
                "error", e.getMessage(),
                "timestamp", Instant.now()
            );
            
            return ResponseEntity.status(503).body(errorResponse);
        }
    }
    
    /**
     * Status resumido do CQRS.
     */
    @GetMapping("/status")
    @Operation(
        summary = "Status resumido do CQRS",
        description = "Retorna informações resumidas sobre o status do CQRS"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status retornado com sucesso")
    })
    public ResponseEntity<Map<String, Object>> getStatus() {
        
        log.debug("GET /api/v1/cqrs/status");
        
        try {
            // Forçar atualização das métricas
            metrics.forceUpdate();
            
            Map<String, Object> status = new HashMap<>();
            
            // Métricas básicas
            status.put("commandSideEvents", (long) metrics.getCommandSideEvents());
            status.put("querySideEvents", (long) metrics.getQuerySideEvents());
            status.put("lag", (long) metrics.getOverallLag());
            status.put("estimatedLagSeconds", metrics.getEstimatedLagSeconds());
            
            // Status das projeções
            status.put("totalProjections", (long) metrics.getTotalProjections());
            status.put("activeProjections", (long) metrics.getActiveProjections());
            status.put("errorProjections", (long) metrics.getErrorProjections());
            status.put("staleProjections", (long) metrics.getStaleProjections());
            
            // Performance
            status.put("throughput", metrics.getProjectionsThroughput());
            status.put("errorRate", metrics.getProjectionsErrorRate());
            status.put("healthScore", metrics.getHealthScore());
            
            // Status geral
            double healthScore = metrics.getHealthScore();
            String overallStatus;
            if (healthScore >= 0.8) {
                overallStatus = "HEALTHY";
            } else if (healthScore >= 0.5) {
                overallStatus = "DEGRADED";
            } else {
                overallStatus = "UNHEALTHY";
            }
            
            status.put("overallStatus", overallStatus);
            status.put("timestamp", Instant.now());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Erro ao obter status do CQRS", e);
            
            Map<String, Object> errorResponse = Map.of(
                "status", "ERROR",
                "error", e.getMessage(),
                "timestamp", Instant.now()
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Lista todas as projeções e seus status.
     */
    @GetMapping("/projections")
    @Operation(
        summary = "Listar projeções",
        description = "Lista todas as projeções registradas e seus status atuais"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de projeções retornada com sucesso")
    })
    public ResponseEntity<Page<ProjectionTracker>> getProjections(
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "projectionName") Pageable pageable) {
        
        log.debug("GET /api/v1/cqrs/projections");
        
        try {
            Page<ProjectionTracker> projections = projectionTrackerRepository.findAll(pageable);
            return ResponseEntity.ok(projections);
            
        } catch (Exception e) {
            log.error("Erro ao listar projeções", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Obtém detalhes de uma projeção específica.
     */
    @GetMapping("/projections/{projectionName}")
    @Operation(
        summary = "Obter detalhes de projeção",
        description = "Retorna informações detalhadas sobre uma projeção específica"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalhes da projeção retornados"),
        @ApiResponse(responseCode = "404", description = "Projeção não encontrada")
    })
    public ResponseEntity<ProjectionTracker> getProjection(
            @Parameter(description = "Nome da projeção", example = "SinistroProjectionHandler")
            @PathVariable String projectionName) {
        
        log.debug("GET /api/v1/cqrs/projections/{}", projectionName);
        
        try {
            return projectionTrackerRepository.findByProjectionName(projectionName)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            log.error("Erro ao obter projeção {}", projectionName, e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Obtém métricas detalhadas do CQRS.
     */
    @GetMapping("/metrics")
    @Operation(
        summary = "Obter métricas do CQRS",
        description = "Retorna métricas detalhadas de performance e saúde do CQRS"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Métricas retornadas com sucesso")
    })
    public ResponseEntity<Map<String, Object>> getMetrics() {
        
        log.debug("GET /api/v1/cqrs/metrics");
        
        try {
            // Forçar atualização das métricas
            metrics.forceUpdate();
            
            Map<String, Object> metricsData = new HashMap<>();
            
            // Métricas de lag
            Map<String, Object> lagMetrics = new HashMap<>();
            lagMetrics.put("commandSideEvents", (long) metrics.getCommandSideEvents());
            lagMetrics.put("querySideEvents", (long) metrics.getQuerySideEvents());
            lagMetrics.put("overallLag", (long) metrics.getOverallLag());
            lagMetrics.put("estimatedLagSeconds", metrics.getEstimatedLagSeconds());
            metricsData.put("lag", lagMetrics);
            
            // Métricas de projeções
            Map<String, Object> projectionMetrics = new HashMap<>();
            projectionMetrics.put("total", (long) metrics.getTotalProjections());
            projectionMetrics.put("active", (long) metrics.getActiveProjections());
            projectionMetrics.put("error", (long) metrics.getErrorProjections());
            projectionMetrics.put("stale", (long) metrics.getStaleProjections());
            projectionMetrics.put("throughput", metrics.getProjectionsThroughput());
            projectionMetrics.put("errorRate", metrics.getProjectionsErrorRate());
            metricsData.put("projections", projectionMetrics);
            
            // Métricas de saúde
            Map<String, Object> healthMetrics = new HashMap<>();
            healthMetrics.put("score", metrics.getHealthScore());
            healthMetrics.put("timestamp", Instant.now());
            metricsData.put("health", healthMetrics);
            
            return ResponseEntity.ok(metricsData);
            
        } catch (Exception e) {
            log.error("Erro ao obter métricas do CQRS", e);
            
            Map<String, Object> errorResponse = Map.of(
                "error", e.getMessage(),
                "timestamp", Instant.now()
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Força atualização das métricas.
     */
    @PostMapping("/metrics/refresh")
    @Operation(
        summary = "Atualizar métricas",
        description = "Força a atualização imediata de todas as métricas do CQRS"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Métricas atualizadas com sucesso")
    })
    public ResponseEntity<Map<String, Object>> refreshMetrics() {
        
        log.debug("POST /api/v1/cqrs/metrics/refresh");
        
        try {
            metrics.forceUpdate();
            
            Map<String, Object> response = Map.of(
                "message", "Métricas atualizadas com sucesso",
                "timestamp", Instant.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar métricas", e);
            
            Map<String, Object> errorResponse = Map.of(
                "error", e.getMessage(),
                "timestamp", Instant.now()
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Dashboard com informações resumidas.
     */
    @GetMapping("/dashboard")
    @Operation(
        summary = "Dashboard do CQRS",
        description = "Retorna informações resumidas para dashboard de monitoramento"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dados do dashboard retornados")
    })
    public ResponseEntity<Map<String, Object>> getDashboard() {
        
        log.debug("GET /api/v1/cqrs/dashboard");
        
        try {
            // Forçar atualização das métricas
            metrics.forceUpdate();
            
            Map<String, Object> dashboard = new HashMap<>();
            
            // Status geral
            double healthScore = metrics.getHealthScore();
            String status;
            if (healthScore >= 0.8) {
                status = "HEALTHY";
            } else if (healthScore >= 0.5) {
                status = "DEGRADED";
            } else {
                status = "UNHEALTHY";
            }
            
            dashboard.put("status", status);
            dashboard.put("healthScore", healthScore);
            
            // Métricas principais
            dashboard.put("lag", (long) metrics.getOverallLag());
            dashboard.put("estimatedLagSeconds", metrics.getEstimatedLagSeconds());
            dashboard.put("totalProjections", (long) metrics.getTotalProjections());
            dashboard.put("activeProjections", (long) metrics.getActiveProjections());
            dashboard.put("errorProjections", (long) metrics.getErrorProjections());
            dashboard.put("throughput", metrics.getProjectionsThroughput());
            dashboard.put("errorRate", metrics.getProjectionsErrorRate());
            
            // Alertas
            List<String> alerts = new java.util.ArrayList<>();
            if (metrics.getOverallLag() > 5000) {
                alerts.add("LAG_CRÍTICO: Lag de " + (long) metrics.getOverallLag() + " eventos");
            } else if (metrics.getOverallLag() > 1000) {
                alerts.add("LAG_ALTO: Lag de " + (long) metrics.getOverallLag() + " eventos");
            }
            
            if (metrics.getErrorProjections() > 0) {
                alerts.add("PROJEÇÕES_COM_ERRO: " + (long) metrics.getErrorProjections() + " projeções com erro");
            }
            
            if (metrics.getProjectionsErrorRate() > 0.15) {
                alerts.add("TAXA_ERRO_ALTA: " + String.format("%.2f%%", metrics.getProjectionsErrorRate() * 100));
            }
            
            dashboard.put("alerts", alerts);
            dashboard.put("timestamp", Instant.now());
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("Erro ao obter dashboard do CQRS", e);
            
            Map<String, Object> errorResponse = Map.of(
                "status", "ERROR",
                "error", e.getMessage(),
                "timestamp", Instant.now()
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}