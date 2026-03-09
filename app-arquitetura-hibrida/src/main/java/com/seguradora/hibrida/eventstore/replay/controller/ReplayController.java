package com.seguradora.hibrida.eventstore.replay.controller;

import com.seguradora.hibrida.eventstore.replay.*;
import com.seguradora.hibrida.eventstore.replay.config.ReplayHealthIndicator;
import com.seguradora.hibrida.eventstore.replay.config.ReplayMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Controller REST para operações de replay de eventos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/replay")
@RequiredArgsConstructor
@Tag(name = "Event Replay", description = "APIs para replay de eventos do Event Store")
public class ReplayController {
    
    private final EventReplayer eventReplayer;
    private final ReplayHealthIndicator healthIndicator;
    private final ReplayMetrics metrics;
    
    @Operation(summary = "Executa replay por período", 
               description = "Executa replay de todos os eventos em um período específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Replay iniciado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Configuração inválida"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/period")
    public ResponseEntity<Map<String, Object>> replayByPeriod(
            @Parameter(description = "Data/hora inicial (ISO format)", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime from,
            
            @Parameter(description = "Data/hora final (ISO format)", example = "2024-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime to,
            
            @Parameter(description = "Nome do replay", example = "Replay Janeiro 2024")
            @RequestParam(required = false) String name,
            
            @Parameter(description = "Modo simulação", example = "false")
            @RequestParam(defaultValue = "false") boolean simulationMode,
            
            @Parameter(description = "Velocidade (eventos/segundo, 0 = máxima)", example = "100")
            @RequestParam(defaultValue = "0") int eventsPerSecond) {
        
        try {
            Instant fromInstant = from.toInstant(ZoneOffset.UTC);
            Instant toInstant = to.toInstant(ZoneOffset.UTC);
            
            ReplayConfiguration config = ReplayConfiguration.builder()
                .name(name != null ? name : "Replay por período")
                .description(String.format("Replay de eventos entre %s e %s", from, to))
                .fromTimestamp(fromInstant)
                .toTimestamp(toInstant)
                .simulationMode(simulationMode)
                .eventsPerSecond(eventsPerSecond)
                .initiatedBy("API")
                .build();
            
            CompletableFuture<ReplayResult> future = eventReplayer.replayByPeriod(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("replayId", config.getReplayId());
            response.put("name", config.getName());
            response.put("status", "STARTED");
            response.put("configuration", config);
            response.put("timestamp", Instant.now());
            
            log.info("Replay por período iniciado: {} ({} - {})", 
                config.getReplayId(), from, to);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao iniciar replay por período", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erro ao iniciar replay");
            error.put("message", e.getMessage());
            error.put("timestamp", Instant.now());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @Operation(summary = "Executa replay por tipo de evento",
               description = "Executa replay de eventos de um tipo específico")
    @PostMapping("/event-type/{eventType}")
    public ResponseEntity<Map<String, Object>> replayByEventType(
            @Parameter(description = "Tipo do evento", example = "SinistroCriado")
            @PathVariable String eventType,
            
            @Parameter(description = "Data/hora inicial")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime from,
            
            @Parameter(description = "Data/hora final")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime to,
            
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "false") boolean simulationMode) {
        
        try {
            Instant fromInstant = from.toInstant(ZoneOffset.UTC);
            Instant toInstant = to.toInstant(ZoneOffset.UTC);
            
            ReplayConfiguration config = ReplayConfiguration.forEventType(eventType, fromInstant, toInstant)
                .toBuilder()
                .name(name != null ? name : "Replay por tipo de evento")
                .simulationMode(simulationMode)
                .initiatedBy("API")
                .build();
            
            CompletableFuture<ReplayResult> future = eventReplayer.replayByEventType(
                eventType, fromInstant, toInstant, config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("replayId", config.getReplayId());
            response.put("eventType", eventType);
            response.put("status", "STARTED");
            response.put("timestamp", Instant.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao iniciar replay por tipo de evento", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage(), "timestamp", Instant.now()));
        }
    }
    
    @Operation(summary = "Executa replay por aggregate",
               description = "Executa replay de todos os eventos de um aggregate específico")
    @PostMapping("/aggregate/{aggregateId}")
    public ResponseEntity<Map<String, Object>> replayByAggregate(
            @Parameter(description = "ID do aggregate")
            @PathVariable String aggregateId,
            
            @Parameter(description = "Versão inicial (opcional)")
            @RequestParam(required = false) Long fromVersion,
            
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "false") boolean simulationMode) {
        
        try {
            ReplayConfiguration config = ReplayConfiguration.forAggregate(aggregateId)
                .toBuilder()
                .name(name != null ? name : "Replay por aggregate")
                .simulationMode(simulationMode)
                .initiatedBy("API")
                .build();
            
            CompletableFuture<ReplayResult> future = eventReplayer.replayByAggregate(
                aggregateId, fromVersion, config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("replayId", config.getReplayId());
            response.put("aggregateId", aggregateId);
            response.put("fromVersion", fromVersion);
            response.put("status", "STARTED");
            response.put("timestamp", Instant.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao iniciar replay por aggregate", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage(), "timestamp", Instant.now()));
        }
    }
    
    @Operation(summary = "Pausa replay em execução")
    @PostMapping("/{replayId}/pause")
    public ResponseEntity<Map<String, Object>> pauseReplay(
            @Parameter(description = "ID do replay")
            @PathVariable UUID replayId) {
        
        boolean paused = eventReplayer.pauseReplay(replayId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("replayId", replayId);
        response.put("action", "pause");
        response.put("success", paused);
        response.put("timestamp", Instant.now());
        
        if (paused) {
            log.info("Replay pausado: {}", replayId);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Replay não encontrado ou não pode ser pausado");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "Retoma replay pausado")
    @PostMapping("/{replayId}/resume")
    public ResponseEntity<Map<String, Object>> resumeReplay(
            @Parameter(description = "ID do replay")
            @PathVariable UUID replayId) {
        
        boolean resumed = eventReplayer.resumeReplay(replayId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("replayId", replayId);
        response.put("action", "resume");
        response.put("success", resumed);
        response.put("timestamp", Instant.now());
        
        if (resumed) {
            log.info("Replay retomado: {}", replayId);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Replay não encontrado ou não pode ser retomado");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "Cancela replay em execução")
    @PostMapping("/{replayId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelReplay(
            @Parameter(description = "ID do replay")
            @PathVariable UUID replayId) {
        
        boolean cancelled = eventReplayer.cancelReplay(replayId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("replayId", replayId);
        response.put("action", "cancel");
        response.put("success", cancelled);
        response.put("timestamp", Instant.now());
        
        if (cancelled) {
            log.info("Replay cancelado: {}", replayId);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Replay não encontrado ou não pode ser cancelado");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "Obtém progresso de replay")
    @GetMapping("/{replayId}/progress")
    public ResponseEntity<ReplayProgress> getProgress(
            @Parameter(description = "ID do replay")
            @PathVariable UUID replayId) {
        
        ReplayProgress progress = eventReplayer.getProgress(replayId);
        
        if (progress != null) {
            return ResponseEntity.ok(progress);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Lista replays ativos")
    @GetMapping("/active")
    public ResponseEntity<List<ReplayProgress>> getActiveReplays() {
        List<ReplayProgress> activeReplays = eventReplayer.getActiveReplays();
        return ResponseEntity.ok(activeReplays);
    }
    
    @Operation(summary = "Obtém histórico de replays")
    @GetMapping("/history")
    public ResponseEntity<List<ReplayResult>> getHistory(
            @Parameter(description = "Número máximo de registros")
            @RequestParam(defaultValue = "50") int limit) {
        
        List<ReplayResult> history = eventReplayer.getReplayHistory(limit);
        return ResponseEntity.ok(history);
    }
    
    @Operation(summary = "Obtém estatísticas do sistema de replay")
    @GetMapping("/statistics")
    public ResponseEntity<ReplayStatistics> getStatistics() {
        ReplayStatistics statistics = eventReplayer.getStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(summary = "Health check do sistema de replay")
    @GetMapping("/health")
    public ResponseEntity<Health> healthCheck() {
        Health health = healthIndicator.health();
        return ResponseEntity.ok(health);
    }
    
    @Operation(summary = "Obtém métricas em tempo real")
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metricsData = new HashMap<>();
        
        metricsData.put("active_replays", metrics.getActiveReplaysCount());
        metricsData.put("success_rate", metrics.getSuccessRate());
        metricsData.put("error_rate", metrics.getErrorRate());
        metricsData.put("average_throughput", metrics.getAverageThroughput());
        metricsData.put("timestamp", Instant.now());
        
        return ResponseEntity.ok(metricsData);
    }
    
    @Operation(summary = "Executa simulação de replay")
    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateReplay(
            @RequestBody ReplayConfiguration baseConfig) {
        
        try {
            ReplayConfiguration simulationConfig = ReplayConfiguration.forSimulation(baseConfig);
            CompletableFuture<ReplayResult> future = eventReplayer.simulateReplay(simulationConfig);
            
            Map<String, Object> response = new HashMap<>();
            response.put("replayId", simulationConfig.getReplayId());
            response.put("mode", "SIMULATION");
            response.put("status", "STARTED");
            response.put("timestamp", Instant.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao iniciar simulação de replay", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage(), "timestamp", Instant.now()));
        }
    }
}