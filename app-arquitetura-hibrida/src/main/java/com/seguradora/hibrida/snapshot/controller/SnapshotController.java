package com.seguradora.hibrida.snapshot.controller;

import com.seguradora.hibrida.snapshot.*;
import com.seguradora.hibrida.snapshot.config.SnapshotHealthIndicator;
import com.seguradora.hibrida.snapshot.config.SnapshotMetrics;
import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller REST para operações de snapshot.
 * 
 * <p>Fornece endpoints para:
 * <ul>
 *   <li>Consulta de snapshots por aggregate</li>
 *   <li>Estatísticas e métricas</li>
 *   <li>Operações de manutenção</li>
 *   <li>Monitoramento de saúde</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/snapshots")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Snapshots", description = "APIs para gerenciamento de snapshots de aggregates")
public class SnapshotController {
    
    private final SnapshotStore snapshotStore;
    private final SnapshotHealthIndicator healthIndicator;
    private final SnapshotMetrics snapshotMetrics;
    
    /**
     * Obtém o snapshot mais recente de um aggregate.
     */
    @GetMapping("/aggregates/{aggregateId}/latest")
    @Operation(summary = "Obter snapshot mais recente", 
               description = "Recupera o snapshot mais recente de um aggregate específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Snapshot encontrado"),
        @ApiResponse(responseCode = "404", description = "Snapshot não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<AggregateSnapshot> getLatestSnapshot(
            @Parameter(description = "ID do aggregate") 
            @PathVariable String aggregateId) {
        
        log.debug("Getting latest snapshot for aggregate {}", aggregateId);
        
        Optional<AggregateSnapshot> snapshot = snapshotStore.getLatestSnapshot(aggregateId);
        
        if (snapshot.isPresent()) {
            log.debug("Found latest snapshot for aggregate {} at version {}", 
                     aggregateId, snapshot.get().getVersion());
            return ResponseEntity.ok(snapshot.get());
        } else {
            log.debug("No snapshot found for aggregate {}", aggregateId);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtém snapshot em uma versão específica ou anterior.
     */
    @GetMapping("/aggregates/{aggregateId}/version/{maxVersion}")
    @Operation(summary = "Obter snapshot por versão", 
               description = "Recupera snapshot em uma versão específica ou a mais recente anterior")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Snapshot encontrado"),
        @ApiResponse(responseCode = "404", description = "Snapshot não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<AggregateSnapshot> getSnapshotAtVersion(
            @Parameter(description = "ID do aggregate") 
            @PathVariable String aggregateId,
            @Parameter(description = "Versão máxima (inclusive)")
            @PathVariable long maxVersion) {
        
        log.debug("Getting snapshot for aggregate {} at or before version {}", aggregateId, maxVersion);
        
        Optional<AggregateSnapshot> snapshot = snapshotStore.getSnapshotAtOrBeforeVersion(aggregateId, maxVersion);
        
        if (snapshot.isPresent()) {
            log.debug("Found snapshot for aggregate {} at version {} (requested max: {})", 
                     aggregateId, snapshot.get().getVersion(), maxVersion);
            return ResponseEntity.ok(snapshot.get());
        } else {
            log.debug("No snapshot found for aggregate {} at or before version {}", aggregateId, maxVersion);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtém histórico de snapshots de um aggregate.
     */
    @GetMapping("/aggregates/{aggregateId}/history")
    @Operation(summary = "Obter histórico de snapshots", 
               description = "Lista todos os snapshots de um aggregate ordenados por versão")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico recuperado com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<AggregateSnapshot>> getSnapshotHistory(
            @Parameter(description = "ID do aggregate") 
            @PathVariable String aggregateId) {
        
        log.debug("Getting snapshot history for aggregate {}", aggregateId);
        
        List<AggregateSnapshot> history = snapshotStore.getSnapshotHistory(aggregateId);
        
        log.debug("Found {} snapshots in history for aggregate {}", history.size(), aggregateId);
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Obtém estatísticas de snapshots de um aggregate.
     */
    @GetMapping("/aggregates/{aggregateId}/statistics")
    @Operation(summary = "Obter estatísticas do aggregate", 
               description = "Recupera estatísticas detalhadas de snapshots de um aggregate")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estatísticas recuperadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<SnapshotStatistics> getAggregateStatistics(
            @Parameter(description = "ID do aggregate") 
            @PathVariable String aggregateId) {
        
        log.debug("Getting statistics for aggregate {}", aggregateId);
        
        SnapshotStatistics statistics = snapshotStore.getSnapshotStatistics(aggregateId);
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Obtém estatísticas globais de snapshots.
     */
    @GetMapping("/statistics")
    @Operation(summary = "Obter estatísticas globais", 
               description = "Recupera estatísticas globais do sistema de snapshots")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estatísticas recuperadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<SnapshotStatistics> getGlobalStatistics() {
        
        log.debug("Getting global snapshot statistics");
        
        SnapshotStatistics statistics = snapshotStore.getGlobalStatistics();
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Obtém métricas de eficiência de um aggregate.
     */
    @GetMapping("/aggregates/{aggregateId}/efficiency")
    @Operation(summary = "Obter métricas de eficiência", 
               description = "Recupera métricas de eficiência de snapshots de um aggregate")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Métricas recuperadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<SnapshotEfficiencyMetrics> getEfficiencyMetrics(
            @Parameter(description = "ID do aggregate") 
            @PathVariable String aggregateId,
            @Parameter(description = "Período em dias para análise (padrão: 7)")
            @RequestParam(defaultValue = "7") int period) {
        
        log.debug("Getting efficiency metrics for aggregate {} over {} days", aggregateId, period);
        
        SnapshotEfficiencyMetrics metrics = snapshotStore.getEfficiencyMetrics(aggregateId, period);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Verifica se deve criar snapshot para um aggregate.
     */
    @GetMapping("/aggregates/{aggregateId}/should-create")
    @Operation(summary = "Verificar necessidade de snapshot", 
               description = "Verifica se um snapshot deve ser criado para o aggregate na versão atual")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> shouldCreateSnapshot(
            @Parameter(description = "ID do aggregate") 
            @PathVariable String aggregateId,
            @Parameter(description = "Versão atual do aggregate")
            @RequestParam long currentVersion) {
        
        log.debug("Checking if should create snapshot for aggregate {} at version {}", 
                 aggregateId, currentVersion);
        
        boolean shouldCreate = snapshotStore.shouldCreateSnapshot(aggregateId, currentVersion);
        
        Map<String, Object> response = Map.of(
            "aggregateId", aggregateId,
            "currentVersion", currentVersion,
            "shouldCreateSnapshot", shouldCreate,
            "reason", shouldCreate ? "Threshold reached" : "Threshold not reached"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Executa limpeza de snapshots antigos de um aggregate.
     */
    @DeleteMapping("/aggregates/{aggregateId}/cleanup")
    @Operation(summary = "Limpar snapshots antigos", 
               description = "Remove snapshots antigos mantendo apenas os N mais recentes")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Limpeza executada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> cleanupOldSnapshots(
            @Parameter(description = "ID do aggregate") 
            @PathVariable String aggregateId,
            @Parameter(description = "Número de snapshots a manter (padrão: 5)")
            @RequestParam(defaultValue = "5") int keepCount) {
        
        log.info("Cleaning up old snapshots for aggregate {} (keeping {})", aggregateId, keepCount);
        
        int deletedCount = snapshotStore.cleanupOldSnapshots(aggregateId, keepCount);
        
        Map<String, Object> response = Map.of(
            "aggregateId", aggregateId,
            "keepCount", keepCount,
            "deletedCount", deletedCount,
            "message", deletedCount > 0 ? 
                "Cleanup completed successfully" : "No old snapshots to remove"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Executa limpeza global de snapshots antigos.
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "Limpeza global de snapshots", 
               description = "Remove snapshots antigos de todos os aggregates")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Limpeza global executada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> cleanupAllOldSnapshots(
            @Parameter(description = "Número de snapshots a manter por aggregate (padrão: 5)")
            @RequestParam(defaultValue = "5") int keepCount) {
        
        log.info("Starting global cleanup of old snapshots (keeping {} per aggregate)", keepCount);
        
        int deletedCount = snapshotStore.cleanupAllOldSnapshots(keepCount);
        
        Map<String, Object> response = Map.of(
            "keepCount", keepCount,
            "totalDeletedCount", deletedCount,
            "message", deletedCount > 0 ? 
                "Global cleanup completed successfully" : "No old snapshots to remove"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Verifica saúde do sistema de snapshots.
     */
    @GetMapping("/health")
    @Operation(summary = "Verificar saúde do sistema", 
               description = "Verifica a saúde e status do sistema de snapshots")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Verificação de saúde realizada"),
        @ApiResponse(responseCode = "503", description = "Sistema não saudável")
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        SnapshotHealthIndicator.SnapshotHealthResult health = healthIndicator.health();
        Map<String, Object> healthDetails = health.getDetails();
        
        if (health.isUp()) {
            return ResponseEntity.ok(healthDetails);
        } else {
            return ResponseEntity.status(503).body(healthDetails);
        }
    }
    
    /**
     * Obtém métricas do sistema de snapshots.
     */
    @GetMapping("/metrics")
    @Operation(summary = "Obter métricas do sistema", 
               description = "Recupera métricas detalhadas do sistema de snapshots")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Métricas recuperadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<SnapshotMetrics.MetricsStatistics> getMetrics() {
        
        log.debug("Getting snapshot system metrics");
        
        SnapshotMetrics.MetricsStatistics metrics = snapshotMetrics.getMetricsStatistics();
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Remove todos os snapshots de um aggregate (operação perigosa).
     */
    @DeleteMapping("/aggregates/{aggregateId}")
    @Operation(summary = "Remover todos os snapshots", 
               description = "Remove TODOS os snapshots de um aggregate (operação irreversível)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Snapshots removidos com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> deleteAllSnapshots(
            @Parameter(description = "ID do aggregate") 
            @PathVariable String aggregateId,
            @Parameter(description = "Confirmação (deve ser 'CONFIRM')")
            @RequestParam String confirm) {
        
        if (!"CONFIRM".equals(confirm)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Confirmation required",
                "message", "Use confirm=CONFIRM to proceed with deletion"
            ));
        }
        
        log.warn("Deleting ALL snapshots for aggregate {} (confirmed by user)", aggregateId);
        
        int deletedCount = snapshotStore.deleteAllSnapshots(aggregateId);
        
        Map<String, Object> response = Map.of(
            "aggregateId", aggregateId,
            "deletedCount", deletedCount,
            "message", "All snapshots deleted successfully",
            "warning", "This operation is irreversible"
        );
        
        return ResponseEntity.ok(response);
    }
}