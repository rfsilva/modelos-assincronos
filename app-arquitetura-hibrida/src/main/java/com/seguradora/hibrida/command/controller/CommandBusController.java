package com.seguradora.hibrida.command.controller;

import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandBusStatistics;
import com.seguradora.hibrida.command.CommandHandlerRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para monitoramento e administração do Command Bus.
 * 
 * <p>Fornece endpoints para:</p>
 * <ul>
 *   <li>Consultar estatísticas de execução</li>
 *   <li>Verificar handlers registrados</li>
 *   <li>Monitorar saúde do sistema</li>
 *   <li>Obter métricas de performance</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/command-bus")
@Tag(name = "Command Bus", description = "APIs para monitoramento do Command Bus")
@Slf4j
public class CommandBusController {
    
    private final CommandBus commandBus;
    private final CommandHandlerRegistry handlerRegistry;
    
    /**
     * Construtor com injeção de dependências.
     * 
     * @param commandBus Command Bus principal
     * @param handlerRegistry Registry de handlers
     */
    @Autowired
    public CommandBusController(CommandBus commandBus, CommandHandlerRegistry handlerRegistry) {
        this.commandBus = commandBus;
        this.handlerRegistry = handlerRegistry;
    }
    
    /**
     * Obtém estatísticas gerais do Command Bus.
     * 
     * @return Estatísticas de execução
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Obter estatísticas do Command Bus",
        description = "Retorna estatísticas detalhadas sobre execução de comandos, performance e throughput"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            CommandBusStatistics stats = commandBus.getStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalCommands", stats.getTotalCommands());
            response.put("commandsProcessed", stats.getTotalCommandsProcessed().get());
            response.put("commandsFailed", stats.getTotalCommandsFailed().get());
            response.put("commandsTimedOut", stats.getTotalCommandsTimedOut().get());
            response.put("commandsRejected", stats.getTotalCommandsRejected().get());
            response.put("successRate", String.format("%.2f%%", stats.getSuccessRate()));
            response.put("errorRate", String.format("%.2f%%", stats.getErrorRate()));
            response.put("averageExecutionTimeMs", String.format("%.2f", stats.getAverageExecutionTimeMs()));
            response.put("minExecutionTimeMs", stats.getMinExecutionTimeMs());
            response.put("maxExecutionTimeMs", stats.getMaxExecutionTimeMs());
            response.put("throughputPerSecond", String.format("%.2f", stats.getThroughputPerSecond()));
            response.put("registeredHandlers", stats.getRegisteredHandlers());
            response.put("lastUpdated", stats.getLastUpdated());
            response.put("startedAt", stats.getStartedAt());
            response.put("timestamp", Instant.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao obter estatísticas do Command Bus", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage(), "timestamp", Instant.now()));
        }
    }
    
    /**
     * Obtém estatísticas por tipo de comando.
     * 
     * @return Estatísticas detalhadas por tipo
     */
    @GetMapping("/statistics/by-type")
    @Operation(
        summary = "Obter estatísticas por tipo de comando",
        description = "Retorna estatísticas detalhadas agrupadas por tipo de comando"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estatísticas por tipo obtidas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> getStatisticsByType() {
        try {
            CommandBusStatistics stats = commandBus.getStatistics();
            Map<String, Object> response = new HashMap<>();
            
            stats.getCommandTypeStats().forEach((type, typeStats) -> {
                Map<String, Object> typeInfo = new HashMap<>();
                typeInfo.put("total", typeStats.getTotal());
                typeInfo.put("processed", typeStats.getProcessed().get());
                typeInfo.put("failed", typeStats.getFailed().get());
                typeInfo.put("timedOut", typeStats.getTimedOut().get());
                typeInfo.put("rejected", typeStats.getRejected().get());
                typeInfo.put("successRate", String.format("%.2f%%", typeStats.getSuccessRate()));
                typeInfo.put("averageExecutionTimeMs", String.format("%.2f", typeStats.getAverageExecutionTimeMs()));
                typeInfo.put("minExecutionTimeMs", typeStats.getMinExecutionTimeMs());
                typeInfo.put("maxExecutionTimeMs", typeStats.getMaxExecutionTimeMs());
                typeInfo.put("lastExecuted", typeStats.getLastExecuted());
                response.put(type, typeInfo);
            });
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao obter estatísticas por tipo do Command Bus", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage(), "timestamp", Instant.now()));
        }
    }
    
    /**
     * Obtém informações sobre handlers registrados.
     * 
     * @return Lista de handlers registrados
     */
    @GetMapping("/handlers")
    @Operation(
        summary = "Listar handlers registrados",
        description = "Retorna lista de todos os command handlers registrados no sistema"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Handlers listados com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> getRegisteredHandlers() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("totalHandlers", handlerRegistry.getHandlerCount());
            response.put("registeredTypes", handlerRegistry.getRegisteredCommandTypes());
            response.put("handlers", handlerRegistry.getDebugInfo());
            response.put("timestamp", Instant.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao obter handlers registrados", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage(), "timestamp", Instant.now()));
        }
    }
    
    /**
     * Verifica se existe handler para um tipo de comando.
     * 
     * @param commandType Nome do tipo do comando
     * @return Status da verificação
     */
    @GetMapping("/handlers/{commandType}")
    @Operation(
        summary = "Verificar handler específico",
        description = "Verifica se existe handler registrado para um tipo específico de comando"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Tipo de comando inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> checkHandler(
            @Parameter(description = "Nome do tipo do comando", example = "CriarSeguradoCommand")
            @PathVariable String commandType) {
        
        try {
            // Tentar encontrar a classe do comando
            Class<?> clazz = Class.forName("com.seguradora.hibrida.command." + commandType);
            
            if (!com.seguradora.hibrida.command.Command.class.isAssignableFrom(clazz)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Tipo especificado não é um comando válido", 
                                   "timestamp", Instant.now()));
            }
            
            @SuppressWarnings("unchecked")
            Class<? extends com.seguradora.hibrida.command.Command> commandClass = 
                    (Class<? extends com.seguradora.hibrida.command.Command>) clazz;
            
            boolean hasHandler = commandBus.hasHandler(commandClass);
            
            Map<String, Object> response = new HashMap<>();
            response.put("commandType", commandType);
            response.put("hasHandler", hasHandler);
            response.put("timestamp", Instant.now());
            
            if (hasHandler) {
                // Adicionar informações do handler se existir
                String handlerInfo = handlerRegistry.getDebugInfo().get(commandType);
                if (handlerInfo != null) {
                    response.put("handlerClass", handlerInfo);
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tipo de comando não encontrado: " + commandType, 
                               "timestamp", Instant.now()));
        } catch (Exception e) {
            log.error("Erro ao verificar handler para comando: {}", commandType, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage(), "timestamp", Instant.now()));
        }
    }
    
    /**
     * Obtém status de saúde do Command Bus.
     * 
     * @return Status de saúde
     */
    @GetMapping("/health")
    @Operation(
        summary = "Verificar saúde do Command Bus",
        description = "Retorna informações sobre a saúde e status operacional do Command Bus"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status de saúde obtido com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> getHealth() {
        try {
            CommandBusStatistics stats = commandBus.getStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("registeredHandlers", stats.getRegisteredHandlers());
            response.put("totalCommands", stats.getTotalCommands());
            response.put("successRate", String.format("%.2f%%", stats.getSuccessRate()));
            response.put("errorRate", String.format("%.2f%%", stats.getErrorRate()));
            response.put("averageExecutionTimeMs", String.format("%.2f", stats.getAverageExecutionTimeMs()));
            response.put("lastUpdated", stats.getLastUpdated());
            response.put("uptime", java.time.Duration.between(stats.getStartedAt(), Instant.now()).toString());
            response.put("timestamp", Instant.now());
            
            // Verificar condições de saúde
            boolean isHealthy = true;
            if (stats.getRegisteredHandlers() == 0) {
                isHealthy = false;
                response.put("warning", "Nenhum handler registrado");
            }
            
            if (stats.getErrorRate() > 10.0) {
                isHealthy = false;
                response.put("warning", "Taxa de erro alta");
            }
            
            if (!isHealthy) {
                response.put("status", "DEGRADED");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao verificar saúde do Command Bus", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "DOWN",
                        "error", e.getMessage(),
                        "timestamp", Instant.now()
                    ));
        }
    }
    
    /**
     * Reseta estatísticas do Command Bus.
     * 
     * @return Confirmação do reset
     */
    @PostMapping("/statistics/reset")
    @Operation(
        summary = "Resetar estatísticas",
        description = "Reseta todas as estatísticas coletadas do Command Bus"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estatísticas resetadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> resetStatistics() {
        try {
            CommandBusStatistics stats = commandBus.getStatistics();
            stats.reset();
            
            log.info("Estatísticas do Command Bus resetadas");
            
            return ResponseEntity.ok(Map.of(
                "message", "Estatísticas resetadas com sucesso",
                "timestamp", Instant.now()
            ));
            
        } catch (Exception e) {
            log.error("Erro ao resetar estatísticas do Command Bus", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage(), "timestamp", Instant.now()));
        }
    }
}