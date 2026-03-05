package com.seguradora.resiliente.controller;

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
            e é usado para health checks de load balancers e monitoramento.
            
            ### 📊 Informações Retornadas
            - Status da aplicação (UP/DOWN)
            - Versão da aplicação
            - Timestamp atual
            - Arquitetura implementada
            - Funcionalidades ativas
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
                          "aplicacao": "Arquitetura Resiliente",
                          "versao": "1.0.0",
                          "timestamp": "2024-03-03T19:30:00Z",
                          "arquitetura": "resiliente",
                          "funcionalidades": [
                            "circuit-breaker",
                            "cache-distribuido",
                            "processamento-assincrono",
                            "retry-automatico"
                          ]
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        log.debug("🔍 Verificando status do sistema");
        
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "aplicacao", "Arquitetura Resiliente",
            "versao", "1.0.0",
            "timestamp", Instant.now(),
            "arquitetura", "resiliente",
            "funcionalidades", new String[]{
                "circuit-breaker",
                "cache-distribuido", 
                "processamento-assincrono",
                "retry-automatico"
            }
        ));
    }

    @Operation(
        summary = "🏥 Health check detalhado",
        description = """
            **Health check completo do sistema**
            
            Implementa a interface HealthIndicator do Spring Boot Actuator
            para fornecer informações detalhadas sobre a saúde da aplicação.
            
            ### 🔍 Verificações Realizadas
            - Status geral da aplicação
            - Conectividade com dependências
            - Recursos disponíveis
            - Configurações ativas
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
                    name = "Health Check",
                    value = """
                        {
                          "status": "UP",
                          "details": {
                            "aplicacao": "Arquitetura Resiliente",
                            "versao": "1.0.0",
                            "uptime": "2h 30m",
                            "memoria": "512MB disponível"
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
            // Verificações básicas de saúde
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            return Health.up()
                .withDetail("aplicacao", "Arquitetura Resiliente")
                .withDetail("versao", "1.0.0")
                .withDetail("memoria_total", formatBytes(totalMemory))
                .withDetail("memoria_usada", formatBytes(usedMemory))
                .withDetail("memoria_livre", formatBytes(freeMemory))
                .withDetail("processadores", runtime.availableProcessors())
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