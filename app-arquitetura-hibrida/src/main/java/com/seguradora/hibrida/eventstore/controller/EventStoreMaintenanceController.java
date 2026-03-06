package com.seguradora.hibrida.eventstore.controller;

import com.seguradora.hibrida.eventstore.archive.ArchiveStatistics;
import com.seguradora.hibrida.eventstore.archive.ArchiveSummary;
import com.seguradora.hibrida.eventstore.archive.EventArchiver;
import com.seguradora.hibrida.eventstore.partition.PartitionManager;
import com.seguradora.hibrida.eventstore.partition.PartitionStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para operações de manutenção do Event Store.
 * 
 * <p>Fornece endpoints para:
 * <ul>
 *   <li>Gerenciamento de partições</li>
 *   <li>Operações de arquivamento</li>
 *   <li>Monitoramento e estatísticas</li>
 *   <li>Manutenção manual</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/eventstore/maintenance")
@RequiredArgsConstructor
@Tag(name = "Event Store Maintenance", description = "Operações de manutenção do Event Store")
public class EventStoreMaintenanceController {
    
    private final PartitionManager partitionManager;
    private final EventArchiver eventArchiver;
    
    /**
     * Executa manutenção manual de partições.
     */
    @PostMapping("/partitions/maintain")
    @Operation(summary = "Executar manutenção de partições", 
               description = "Cria partições futuras e verifica integridade")
    public ResponseEntity<Map<String, Object>> maintainPartitions() {
        log.info("Executando manutenção manual de partições");
        
        boolean success = partitionManager.maintainPartitions();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Manutenção executada com sucesso" : "Falha na manutenção");
        response.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cria partição específica.
     */
    @PostMapping("/partitions/create")
    @Operation(summary = "Criar partição específica", 
               description = "Cria partição para data específica")
    public ResponseEntity<Map<String, Object>> createPartition(
            @Parameter(description = "Data da partição (YYYY-MM-DD)")
            @RequestParam String date) {
        
        try {
            LocalDate partitionDate = LocalDate.parse(date);
            boolean success = partitionManager.createMonthlyPartition("events", partitionDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("partitionName", partitionManager.calculatePartitionName(partitionDate));
            response.put("date", date);
            response.put("timestamp", java.time.Instant.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao criar partição para data {}: {}", date, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("date", date);
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lista estatísticas de partições.
     */
    @GetMapping("/partitions/statistics")
    @Operation(summary = "Obter estatísticas de partições", 
               description = "Retorna estatísticas detalhadas de todas as partições")
    public ResponseEntity<List<PartitionStatistics>> getPartitionStatistics() {
        List<PartitionStatistics> statistics = partitionManager.getPartitionStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Lista partições existentes.
     */
    @GetMapping("/partitions/list")
    @Operation(summary = "Listar partições", 
               description = "Lista todas as partições existentes")
    public ResponseEntity<Map<String, Object>> listPartitions() {
        List<String> partitions = partitionManager.listPartitions();
        boolean healthy = partitionManager.arePartitionsHealthy();
        
        Map<String, Object> response = new HashMap<>();
        response.put("partitions", partitions);
        response.put("count", partitions.size());
        response.put("healthy", healthy);
        response.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Verifica saúde das partições.
     */
    @GetMapping("/partitions/health")
    @Operation(summary = "Verificar saúde das partições", 
               description = "Verifica se todas as partições necessárias existem")
    public ResponseEntity<Map<String, Object>> checkPartitionHealth() {
        boolean healthy = partitionManager.arePartitionsHealthy();
        
        Map<String, Object> response = new HashMap<>();
        response.put("healthy", healthy);
        response.put("status", healthy ? "OK" : "UNHEALTHY");
        response.put("timestamp", java.time.Instant.now());
        
        if (!healthy) {
            response.put("recommendation", "Execute manutenção de partições");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Executa arquivamento automático.
     */
    @PostMapping("/archive/execute")
    @Operation(summary = "Executar arquivamento automático", 
               description = "Arquiva todas as partições elegíveis")
    public ResponseEntity<ArchiveSummary> executeArchiving() {
        log.info("Executando arquivamento manual");
        
        ArchiveSummary summary = eventArchiver.executeAutoArchiving();
        summary.finish();
        
        log.info("Arquivamento manual concluído. Sucessos: {}, Erros: {}", 
            summary.getSuccessCount(), summary.getErrorCount());
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Arquiva partição específica.
     */
    @PostMapping("/archive/partition/{partitionName}")
    @Operation(summary = "Arquivar partição específica", 
               description = "Arquiva uma partição específica")
    public ResponseEntity<Map<String, Object>> archivePartition(
            @Parameter(description = "Nome da partição")
            @PathVariable String partitionName) {
        
        log.info("Arquivando partição específica: {}", partitionName);
        
        var result = eventArchiver.archivePartition(partitionName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("partitionName", result.getPartitionName());
        response.put("eventCount", result.getEventCount());
        response.put("compressedSize", result.getCompressedSize());
        response.put("timestamp", result.getTimestamp());
        
        if (!result.isSuccess()) {
            response.put("error", result.getErrorMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Restaura partição arquivada.
     */
    @PostMapping("/archive/restore/{partitionName}")
    @Operation(summary = "Restaurar partição arquivada", 
               description = "Restaura uma partição do arquivo")
    public ResponseEntity<Map<String, Object>> restorePartition(
            @Parameter(description = "Nome da partição")
            @PathVariable String partitionName) {
        
        log.info("Restaurando partição: {}", partitionName);
        
        boolean success = eventArchiver.restorePartition(partitionName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("partitionName", partitionName);
        response.put("message", success ? "Partição restaurada com sucesso" : "Falha na restauração");
        response.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lista partições elegíveis para arquivamento.
     */
    @GetMapping("/archive/eligible")
    @Operation(summary = "Listar partições elegíveis", 
               description = "Lista partições que podem ser arquivadas")
    public ResponseEntity<Map<String, Object>> getEligiblePartitions() {
        List<String> eligible = eventArchiver.findPartitionsForArchiving();
        
        Map<String, Object> response = new HashMap<>();
        response.put("partitions", eligible);
        response.put("count", eligible.size());
        response.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtém estatísticas de arquivamento.
     */
    @GetMapping("/archive/statistics")
    @Operation(summary = "Obter estatísticas de arquivamento", 
               description = "Retorna estatísticas detalhadas dos arquivos")
    public ResponseEntity<ArchiveStatistics> getArchiveStatistics() {
        ArchiveStatistics statistics = eventArchiver.getArchiveStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Dashboard de manutenção.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard de manutenção", 
               description = "Visão geral do status de manutenção")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Estatísticas de partições
        var partitionStats = partitionManager.getPartitionStatistics();
        dashboard.put("partitions", Map.of(
            "total", partitionStats.size(),
            "healthy", partitionManager.arePartitionsHealthy(),
            "statistics", partitionStats
        ));
        
        // Estatísticas de arquivamento
        var archiveStats = eventArchiver.getArchiveStatistics();
        dashboard.put("archives", Map.of(
            "totalArchives", archiveStats.getTotalArchives(),
            "totalEvents", archiveStats.getTotalEvents(),
            "totalSize", archiveStats.getFormattedSize(),
            "statistics", archiveStats
        ));
        
        // Partições elegíveis para arquivamento
        var eligible = eventArchiver.findPartitionsForArchiving();
        dashboard.put("eligible", Map.of(
            "count", eligible.size(),
            "partitions", eligible
        ));
        
        dashboard.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(dashboard);
    }
}