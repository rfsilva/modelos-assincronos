package com.seguradora.detran.controller;

import com.seguradora.detran.exception.DetranDadosInvalidosException;
import com.seguradora.detran.exception.DetranIndisponivelException;
import com.seguradora.detran.exception.DetranTimeoutException;
import com.seguradora.detran.model.DetranResponse;
import com.seguradora.detran.service.DetranSimulatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/veiculo")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "🚗 Consultas de Veículos", description = "Endpoints para consulta de dados de veículos")
public class DetranController {
    
    private final DetranSimulatorService detranService;
    
    @Operation(
        summary = "🔍 Consultar dados do veículo",
        description = """
            **Consulta dados de um veículo no sistema Detran**
            
            Este endpoint simula o comportamento do sistema legado do Detran, incluindo:
            - ✅ Retorno de dados quando encontrado
            - ❌ Validação de formato de placa e RENAVAM
            - ⏱️ Timeouts simulados (10% das consultas)
            - 🔴 Indisponibilidade simulada (15% das consultas)
            - 🐌 Respostas lentas simuladas (25% das consultas)
            
            ### 📋 Formatos Aceitos
            - **Placa antiga**: ABC1234
            - **Placa Mercosul**: ABC1D23
            - **RENAVAM**: 11 dígitos numéricos
            
            ### ⚠️ Comportamentos Simulados
            - Pode retornar diferentes códigos de status para simular instabilidades
            - Tempos de resposta variam entre 500ms e 8000ms
            - Alguns requests podem resultar em timeout (30s)
            """,
        operationId = "consultarVeiculo"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Veículo encontrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DetranResponse.class),
                examples = @ExampleObject(
                    name = "Veículo Regular",
                    value = """
                        {
                          "placa": "ABC1234",
                          "renavam": "12345678901",
                          "ano_fabricacao": "2020",
                          "ano_modelo": "2020",
                          "marca_modelo": "VOLKSWAGEN/GOL",
                          "cor": "BRANCO",
                          "combustivel": "FLEX",
                          "categoria": "PARTICULAR",
                          "carroceria": "HATCH",
                          "especie": "PASSAGEIRO",
                          "proprietario": "JOAO DA SILVA SANTOS",
                          "municipio": "SAO PAULO",
                          "situacao": "REGULAR",
                          "data_aquisicao": "2020-03-15"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ Dados inválidos (formato de placa ou RENAVAM incorreto)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Erro de Validação",
                    value = """
                        {
                          "timestamp": "2024-03-03T19:30:00.000Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Formato de placa inválido",
                          "path": "/detran-api/veiculo"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "408",
            description = "⏱️ Timeout simulado (10% das consultas)",
            content = @Content(
                examples = @ExampleObject(
                    name = "Timeout",
                    description = "Request timeout - sistema não respondeu no tempo esperado"
                )
            )
        ),
        @ApiResponse(
            responseCode = "503",
            description = "🔴 Sistema indisponível (15% das consultas)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Sistema Indisponível",
                    value = """
                        {
                          "timestamp": "2024-03-03T19:30:00.000Z",
                          "status": 503,
                          "error": "Service Unavailable",
                          "message": "Sistema Detran temporariamente indisponível",
                          "path": "/detran-api/veiculo"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "💥 Erro interno do servidor",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Erro Interno",
                    value = """
                        {
                          "timestamp": "2024-03-03T19:30:00.000Z",
                          "status": 500,
                          "error": "Internal Server Error",
                          "message": "Erro interno do sistema",
                          "path": "/detran-api/veiculo"
                        }
                        """
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<DetranResponse> consultarVeiculo(
            @Parameter(
                name = "placa",
                description = "Placa do veículo (formato antigo ABC1234 ou Mercosul ABC1D23)",
                required = true,
                example = "ABC1234",
                schema = @Schema(
                    type = "string",
                    pattern = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$",
                    minLength = 7,
                    maxLength = 7
                )
            )
            @RequestParam @NotBlank(message = "Placa é obrigatória") 
            @Pattern(regexp = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$", 
                    message = "Formato de placa inválido") 
            String placa,
            
            @Parameter(
                name = "renavam",
                description = "RENAVAM do veículo (11 dígitos numéricos)",
                required = true,
                example = "12345678901",
                schema = @Schema(
                    type = "string",
                    pattern = "^[0-9]{11}$",
                    minLength = 11,
                    maxLength = 11
                )
            )
            @RequestParam @NotBlank(message = "RENAVAM é obrigatório")
            @Pattern(regexp = "^[0-9]{11}$", message = "RENAVAM deve ter 11 dígitos")
            String renavam,
            
            HttpServletRequest request) {
        
        try {
            String clientIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            log.info("📥 Recebida consulta - Placa: {} RENAVAM: {} IP: {}", placa, renavam, clientIp);
            
            DetranResponse response = detranService.consultarVeiculo(placa, renavam, clientIp, userAgent);
            
            return ResponseEntity.ok(response);
            
        } catch (DetranDadosInvalidosException e) {
            log.warn("❌ Dados inválidos - Placa: {} RENAVAM: {} - {}", placa, renavam, e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (DetranTimeoutException e) {
            log.warn("⏱️ Timeout simulado - Placa: {} RENAVAM: {} - {}", placa, renavam, e.getMessage());
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
            
        } catch (DetranIndisponivelException e) {
            log.error("🔴 Indisponibilidade simulada - Placa: {} RENAVAM: {} - {}", placa, renavam, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            
        } catch (Exception e) {
            log.error("💥 Erro interno - Placa: {} RENAVAM: {}", placa, renavam, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(
        summary = "🟢 Verificar status do sistema",
        description = """
            **Verifica se o simulador está online e operacional**
            
            Este endpoint sempre retorna status 200 quando o sistema está funcionando.
            Útil para health checks e monitoramento.
            
            ### 📊 Informações Retornadas
            - Status do sistema (ONLINE/OFFLINE)
            - Versão do simulador
            - Timestamp atual
            - Informações sobre instabilidades ativas
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
                          "status": "ONLINE",
                          "sistema": "DETRAN Simulator",
                          "versao": "1.0.0",
                          "timestamp": 1709491800000,
                          "message": "Sistema operacional - instabilidades simuladas ativas"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/status")
    @Tag(name = "🔧 Sistema")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "status", "ONLINE",
            "sistema", "DETRAN Simulator",
            "versao", "1.0.0",
            "timestamp", System.currentTimeMillis(),
            "message", "Sistema operacional - instabilidades simuladas ativas"
        ));
    }
    
    @Operation(
        summary = "🔧 Simular manutenção do sistema",
        description = """
            **Endpoint que sempre retorna erro 503 para simular manutenção**
            
            Este endpoint é útil para testar como sua aplicação lida com 
            indisponibilidade total do sistema Detran.
            
            ### 🎯 Casos de Uso
            - Testar circuit breakers
            - Validar fallbacks
            - Simular janelas de manutenção
            - Testes de resiliência
            """,
        operationId = "simulacaoManutencao"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "503",
            description = "🔧 Sistema em manutenção (sempre retorna este status)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Sistema em Manutenção",
                    value = """
                        {
                          "erro": "SISTEMA_EM_MANUTENCAO",
                          "message": "Sistema em manutenção programada",
                          "previsao_retorno": "Indefinido"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/manutencao")
    @Tag(name = "🔧 Sistema")
    public ResponseEntity<Map<String, String>> manutencao() {
        log.warn("🔧 Endpoint de manutenção acessado - retornando 503");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "erro", "SISTEMA_EM_MANUTENCAO",
                "message", "Sistema em manutenção programada",
                "previsao_retorno", "Indefinido"
            ));
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}