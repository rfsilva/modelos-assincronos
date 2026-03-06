package com.seguradora.hibrida.projection.controller;

import com.seguradora.hibrida.projection.ProjectionRegistry;
import com.seguradora.hibrida.projection.consistency.ConsistencyReport;
import com.seguradora.hibrida.projection.consistency.ProjectionConsistencyChecker;
import com.seguradora.hibrida.projection.rebuild.ProjectionRebuilder;
import com.seguradora.hibrida.projection.rebuild.RebuildResult;
import com.seguradora.hibrida.projection.scheduler.ProjectionMaintenanceScheduler;
import com.seguradora.hibrida.projection.tracking.ProjectionTracker;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller REST para gerenciamento de projeções.
 * 
 * <p>Endpoints disponíveis:
 * <ul>
 *   <li>Monitoramento de projeções</li>
 *   <li>Controle de rebuild</li>
 *   <li>Verificação de consistência</li>
 *   <li>Health checks</li>
 *   <li>Estatísticas e métricas</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/projections")
@Tag(name = "Projeções", description = "Gerenciamento e monitoramento de projeções CQRS")
public class ProjectionController {
    
    private final ProjectionRegistry projectionRegistry;
    private final ProjectionTrackerRepository trackerRepository;
    private final ProjectionRebuilder rebuilder;
    private final ProjectionConsistencyChecker consistencyChecker;
    private final ProjectionMaintenanceScheduler maintenanceScheduler;
    
    public ProjectionController(ProjectionRegistry projectionRegistry,
                              ProjectionTrackerRepository trackerRepository,
                              ProjectionRebuilder rebuilder,
                              ProjectionConsistencyChecker consistencyChecker,
                              ProjectionMaintenanceScheduler maintenanceScheduler) {
        this.projectionRegistry = projectionRegistry;
        this.trackerRepository = trackerRepository;
        this.rebuilder = rebuilder;
        this.consistencyChecker = consistencyChecker;
        this.maintenanceScheduler = maintenanceScheduler;
    }
    
    /**
     * Health check do sistema de projeções.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check das projeções", description = "Verifica saúde geral do sistema de projeções")
    @ApiResponse(responseCode = "200", description = "Status de saúde retornado com sucesso")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Status geral
            boolean isHealthy = maintenanceScheduler.isSystemHealthy();
            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("healthScore", maintenanceScheduler.getCurrentHealthScore());
            
            // Estatísticas básicas
            Map<String, Object> statistics = projectionRegistry.getStatistics();
            health.put("statistics", statistics);
            
            // Informações de manutenção
            Map<String, Object> maintenance = new HashMap<>();
            maintenance.put("lastMaintenanceRun", maintenanceScheduler.getLastMaintenanceRun());
            maintenance.put("lastConsistencyCheck", maintenanceScheduler.getLastConsistencyCheck());
            health.put("maintenance", maintenance);
            
            // Último relatório de consistência
            ConsistencyReport lastReport = maintenanceScheduler.getLastConsistencyReport();
            if (lastReport != null) {
                Map<String, Object> consistency = new HashMap<>();
                consistency.put("totalProjections", lastReport.totalProjections());
                consistency.put("totalIssues", lastReport.getTotalIssues());
                consistency.put("criticalIssues", lastReport.getCriticalIssuesCount());
                consistency.put("healthScore", lastReport.getHealthScore());
                consistency.put("timestamp", lastReport.timestamp());
                health.put("consistency", consistency);
            }
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }
    
    /**
     * Lista todas as projeções com paginação.
     */
    @GetMapping
    @Operation(summary = "Lista projeções", description = "Lista todas as projeções com informações de status")
    public ResponseEntity<Page<ProjectionTracker>> listProjections(
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "projectionName") Pageable pageable) {
        
        Page<ProjectionTracker> projections = trackerRepository.findAll(pageable);
        return ResponseEntity.ok(projections);
    }
    
    /**
     * Obtém detalhes de uma projeção específica.
     */
    @GetMapping("/{projectionName}")
    @Operation(summary = "Detalhes da projeção", description = "Obtém informações detalhadas de uma projeção")
    public ResponseEntity<ProjectionTracker> getProjection(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        return trackerRepository.findById(projectionName)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Executa rebuild completo de uma projeção.
     */
    @PostMapping("/{projectionName}/rebuild")
    @Operation(summary = "Rebuild completo", description = "Executa rebuild completo de uma projeção específica")
    public ResponseEntity<Map<String, Object>> rebuildProjection(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        try {
            CompletableFuture<RebuildResult> future = rebuilder.rebuildProjection(projectionName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("projectionName", projectionName);
            response.put("status", "STARTED");
            response.put("message", "Rebuild iniciado com sucesso");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("projectionName", projectionName);
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Executa rebuild incremental de uma projeção.
     */
    @PostMapping("/{projectionName}/rebuild/incremental")
    @Operation(summary = "Rebuild incremental", description = "Executa rebuild incremental de uma projeção específica")
    public ResponseEntity<Map<String, Object>> rebuildProjectionIncremental(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        try {
            CompletableFuture<RebuildResult> future = rebuilder.rebuildProjectionIncremental(projectionName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("projectionName", projectionName);
            response.put("status", "STARTED");
            response.put("type", "INCREMENTAL");
            response.put("message", "Rebuild incremental iniciado com sucesso");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("projectionName", projectionName);
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Pausa uma projeção.
     */
    @PostMapping("/{projectionName}/pause")
    @Operation(summary = "Pausar projeção", description = "Pausa o processamento de uma projeção")
    public ResponseEntity<Map<String, Object>> pauseProjection(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        boolean paused = rebuilder.pauseRebuild(projectionName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("projectionName", projectionName);
        response.put("status", paused ? "PAUSED" : "NOT_PAUSED");
        response.put("message", paused ? "Projeção pausada com sucesso" : "Projeção não estava em execução");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retoma uma projeção pausada.
     */
    @PostMapping("/{projectionName}/resume")
    @Operation(summary = "Retomar projeção", description = "Retoma o processamento de uma projeção pausada")
    public ResponseEntity<Map<String, Object>> resumeProjection(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        try {
            CompletableFuture<RebuildResult> future = rebuilder.resumeRebuild(projectionName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("projectionName", projectionName);
            response.put("status", "RESUMED");
            response.put("message", "Projeção retomada com sucesso");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("projectionName", projectionName);
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Executa verificação de consistência de todas as projeções.
     */
    @PostMapping("/consistency/check")
    @Operation(summary = "Verificar consistência", description = "Executa verificação de consistência de todas as projeções")
    public ResponseEntity<ConsistencyReport> checkConsistency() {
        try {
            ConsistencyReport report = consistencyChecker.checkAllProjections();
            return ResponseEntity.ok(report);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Verifica consistência de uma projeção específica.
     */
    @PostMapping("/{projectionName}/consistency/check")
    @Operation(summary = "Verificar consistência específica", description = "Verifica consistência de uma projeção específica")
    public ResponseEntity<Map<String, Object>> checkProjectionConsistency(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        try {
            var issues = consistencyChecker.checkProjectionConsistency(projectionName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("projectionName", projectionName);
            response.put("issues", issues);
            response.put("issueCount", issues.size());
            response.put("isHealthy", issues.isEmpty());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("projectionName", projectionName);
            error.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Obtém estatísticas detalhadas do sistema de projeções.
     */
    @GetMapping("/statistics")
    @Operation(summary = "Estatísticas das projeções", description = "Obtém estatísticas detalhadas do sistema de projeções")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // Estatísticas do registry
            statistics.put("registry", projectionRegistry.getStatistics());
            
            // Estatísticas dos trackers
            Object[] projectionStats = trackerRepository.getProjectionStatistics();
            if (projectionStats != null && projectionStats.length >= 5) {
                Map<String, Object> trackerStats = new HashMap<>();
                trackerStats.put("total", projectionStats[0]);
                trackerStats.put("active", projectionStats[1]);
                trackerStats.put("error", projectionStats[2]);
                trackerStats.put("paused", projectionStats[3]);
                trackerStats.put("disabled", projectionStats[4]);
                statistics.put("trackers", trackerStats);
            }
            
            // Último relatório de consistência
            ConsistencyReport lastReport = maintenanceScheduler.getLastConsistencyReport();
            if (lastReport != null) {
                Map<String, Object> consistency = new HashMap<>();
                consistency.put("healthScore", lastReport.getHealthScore());
                consistency.put("totalIssues", lastReport.getTotalIssues());
                consistency.put("criticalIssues", lastReport.getCriticalIssuesCount());
                consistency.put("lastCheck", lastReport.timestamp());
                statistics.put("consistency", consistency);
            }
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            statistics.put("error", e.getMessage());
            return ResponseEntity.status(500).body(statistics);
        }
    }
    
    /**
     * Obtém dashboard com informações resumidas.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard das projeções", description = "Obtém informações resumidas para dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            // Status geral
            dashboard.put("systemHealthy", maintenanceScheduler.isSystemHealthy());
            dashboard.put("healthScore", maintenanceScheduler.getCurrentHealthScore());
            
            // Contadores básicos
            long totalProjections = trackerRepository.count();
            dashboard.put("totalProjections", totalProjections);
            
            // Projeções por status
            Map<String, Long> statusCounts = new HashMap<>();
            for (var status : com.seguradora.hibrida.projection.tracking.ProjectionStatus.values()) {
                statusCounts.put(status.name(), trackerRepository.countByStatus(status));
            }
            dashboard.put("statusCounts", statusCounts);
            
            // Último relatório de consistência
            ConsistencyReport lastReport = maintenanceScheduler.getLastConsistencyReport();
            if (lastReport != null) {
                Map<String, Object> consistency = new HashMap<>();
                consistency.put("totalIssues", lastReport.getTotalIssues());
                consistency.put("criticalIssues", lastReport.getCriticalIssuesCount());
                consistency.put("highPriorityIssues", lastReport.getHighPriorityIssuesCount());
                consistency.put("healthyProjections", lastReport.getHealthyProjectionsCount());
                dashboard.put("consistency", consistency);
            }
            
            // Timestamps importantes
            Map<String, Object> timestamps = new HashMap<>();
            timestamps.put("lastMaintenance", maintenanceScheduler.getLastMaintenanceRun());
            timestamps.put("lastConsistencyCheck", maintenanceScheduler.getLastConsistencyCheck());
            dashboard.put("timestamps", timestamps);
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            dashboard.put("error", e.getMessage());
            return ResponseEntity.status(500).body(dashboard);
        }
    }
}