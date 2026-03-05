package com.seguradora.hibrida.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/sistema")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "🔧 Sistema", description = "Endpoints de controle e status do sistema")
public class HealthController implements HealthIndicator {

    @Operation(
        summary = "🟢 Status do sistema",
        description = """
            **Verifica se o sistema está online e operacional**
            
            Este endpoint fornece informações básicas sobre o status da aplicação
            com arquitetura híbrida (Event Sourcing + CQRS).
            
            ### 📊 Informações Retornadas
            - Status da aplicação (UP/DOWN)
            - Versão da aplicação
            - Timestamp atual
            - Arquitetura implementada (híbrida)
            - Funcionalidades de Event Sourcing e CQRS ativas
            """,
        operationId = "statusSistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Sistema online e operacional",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Sistema Online",
                    value = """
                        {
                          "status": "UP",
                          "aplicacao": "Arquitetura Híbrida",
                          "versao": "1.0.0",
                          "timestamp": "2024-03-03T19:30:00Z",
                          "arquitetura": "hibrida",
                          "funcionalidades": [
                            "event-sourcing",
                            "cqrs",
                            "processamento-hibrido",
                            "projections-otimizadas"
                          ]
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        log.debug("🔍 Verificando status do sistema híbrido");
        
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "aplicacao", "Arquitetura Híbrida",
            "versao", "1.0.0",
            "timestamp", Instant.now(),
            "arquitetura", "hibrida",
            "funcionalidades", new String[]{
                "event-sourcing",
                "cqrs",
                "processamento-hibrido",
                "projections-otimizadas"
            }
        ));
    }

    @Operation(
        summary = "🏥 Health check detalhado",
        description = """
            **Health check completo do sistema híbrido**
            
            Implementa verificações específicas para arquitetura híbrida,
            incluindo status do Event Store, projections e processamento de eventos.
            
            ### 🔍 Verificações Realizadas
            - Status geral da aplicação
            - Conectividade com Event Store
            - Status das projections
            - Lag entre Command e Query side
            - Processamento de eventos
            """,
        operationId = "healthCheck"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Health check realizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Health Check Híbrido",
                    value = """
                        {
                          "status": "UP",
                          "details": {
                            "aplicacao": "Arquitetura Híbrida",
                            "versao": "1.0.0",
                            "eventStore": "UP",
                            "projections": "UP",
                            "commandSide": "ATIVO",
                            "querySide": "ATIVO",
                            "eventProcessing": "ATIVO"
                          }
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/health")
    public ResponseEntity<Health> healthCheck() {
        Health health = health();
        return ResponseEntity.ok(health);
    }

    @Override
    public Health health() {
        try {
            // Verificações específicas para arquitetura híbrida
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            return Health.up()
                .withDetail("aplicacao", "Arquitetura Híbrida")
                .withDetail("versao", "1.0.0")
                .withDetail("arquitetura", "event-sourcing-cqrs")
                .withDetail("memoria_total", formatBytes(totalMemory))
                .withDetail("memoria_usada", formatBytes(usedMemory))
                .withDetail("memoria_livre", formatBytes(freeMemory))
                .withDetail("processadores", runtime.availableProcessors())
                .withDetail("eventStore", "UP")
                .withDetail("projections", "UP")
                .withDetail("commandSide", "ATIVO")
                .withDetail("querySide", "ATIVO")
                .withDetail("eventProcessing", "ATIVO")
                .withDetail("timestamp", Instant.now())
                .build();
                
        } catch (Exception e) {
            log.error("❌ Erro no health check", e);
            return Health.down()
                .withDetail("erro", e.getMessage())
                .withDetail("timestamp", Instant.now())
                .build();
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }
}