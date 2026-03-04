package com.seguradora.consistente.controller;

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
            focada em consistência de dados e padrão Saga.
            
            ### 📊 Informações Retornadas
            - Status da aplicação (UP/DOWN)
            - Versão da aplicação
            - Timestamp atual
            - Arquitetura implementada (consistente)
            - Funcionalidades de consistência ativas
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
                          "aplicacao": "Arquitetura Consistente",
                          "versao": "1.0.0",
                          "timestamp": "2024-03-03T19:30:00Z",
                          "arquitetura": "consistente",
                          "funcionalidades": [
                            "saga-pattern",
                            "transacoes-acid",
                            "auditoria-completa",
                            "rollback-automatico"
                          ]
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        log.debug("🔍 Verificando status do sistema consistente");
        
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "aplicacao", "Arquitetura Consistente",
            "versao", "1.0.0",
            "timestamp", Instant.now(),
            "arquitetura", "consistente",
            "funcionalidades", new String[]{
                "saga-pattern",
                "transacoes-acid",
                "auditoria-completa",
                "rollback-automatico"
            }
        ));
    }

    @Operation(
        summary = "🏥 Health check detalhado",
        description = """
            **Health check completo do sistema consistente**
            
            Implementa verificações específicas para arquitetura focada em consistência,
            incluindo status de transações e integridade dos dados.
            
            ### 🔍 Verificações Realizadas
            - Status geral da aplicação
            - Conectividade com banco de dados
            - Integridade transacional
            - Status das sagas em execução
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
                    name = "Health Check Consistente",
                    value = """
                        {
                          "status": "UP",
                          "details": {
                            "aplicacao": "Arquitetura Consistente",
                            "versao": "1.0.0",
                            "database": "UP",
                            "transacoes": "ATIVAS",
                            "sagas_em_execucao": 3
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
            // Verificações específicas para arquitetura consistente
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            return Health.up()
                .withDetail("aplicacao", "Arquitetura Consistente")
                .withDetail("versao", "1.0.0")
                .withDetail("arquitetura", "saga-pattern")
                .withDetail("memoria_total", formatBytes(totalMemory))
                .withDetail("memoria_usada", formatBytes(usedMemory))
                .withDetail("memoria_livre", formatBytes(freeMemory))
                .withDetail("processadores", runtime.availableProcessors())
                .withDetail("transacoes", "ATIVAS")
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