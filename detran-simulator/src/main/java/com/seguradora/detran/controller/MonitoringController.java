package com.seguradora.detran.controller;

import com.seguradora.detran.model.ConsultaLog;
import com.seguradora.detran.repository.ConsultaLogRepository;
import com.seguradora.detran.repository.VeiculoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
@Tag(name = "📊 Monitoramento", description = "Endpoints para monitoramento e métricas do simulador")
public class MonitoringController {
    
    private final ConsultaLogRepository consultaLogRepository;
    private final VeiculoRepository veiculoRepository;
    
    @Operation(
        summary = "📈 Dashboard de estatísticas",
        description = """
            **Retorna estatísticas completas do simulador**
            
            Este endpoint fornece uma visão geral do comportamento do simulador, incluindo:
            
            ### 📊 Métricas Incluídas
            - **Total de consultas** realizadas
            - **Total de veículos** cadastrados
            - **Estatísticas da última hora**:
              - Total de consultas
              - Sucessos, falhas, timeouts
              - Dados inválidos
            - **Tempo médio de resposta** das últimas 24 horas
            
            ### 🎯 Casos de Uso
            - Monitoramento em tempo real
            - Análise de performance
            - Validação de comportamento
            - Dashboards de observabilidade
            """,
        operationId = "dashboardEstatisticas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Estatísticas retornadas com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Dashboard Completo",
                    value = """
                        {
                          "total_consultas": 150,
                          "total_veiculos_cadastrados": 14,
                          "ultima_hora": {
                            "total": 25,
                            "sucessos": 21,
                            "falhas": 2,
                            "timeouts": 2,
                            "dados_invalidos": 0
                          },
                          "tempo_medio_resposta_ms": 2150
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        LocalDateTime ultimaHora = LocalDateTime.now().minusHours(1);
        LocalDateTime ultimoDia = LocalDateTime.now().minusDays(1);
        
        Map<String, Object> stats = new HashMap<>();
        
        // Estatísticas gerais
        stats.put("total_consultas", consultaLogRepository.count());
        stats.put("total_veiculos_cadastrados", veiculoRepository.count());
        
        // Estatísticas da última hora
        Map<String, Object> ultimaHoraStats = new HashMap<>();
        ultimaHoraStats.put("total", consultaLogRepository.findByPeriodo(ultimaHora, LocalDateTime.now()).size());
        ultimaHoraStats.put("sucessos", consultaLogRepository.countByStatusAndTimestampAfter(
            ConsultaLog.StatusConsulta.SUCESSO, ultimaHora));
        ultimaHoraStats.put("falhas", consultaLogRepository.countByStatusAndTimestampAfter(
            ConsultaLog.StatusConsulta.INDISPONIBILIDADE_SIMULADA, ultimaHora));
        ultimaHoraStats.put("timeouts", consultaLogRepository.countByStatusAndTimestampAfter(
            ConsultaLog.StatusConsulta.TIMEOUT_SIMULADO, ultimaHora));
        ultimaHoraStats.put("dados_invalidos", consultaLogRepository.countByStatusAndTimestampAfter(
            ConsultaLog.StatusConsulta.DADOS_INVALIDOS, ultimaHora));
        
        stats.put("ultima_hora", ultimaHoraStats);
        
        // Tempo médio de resposta das últimas 24 horas
        Double tempoMedio = consultaLogRepository.averageResponseTimeAfter(ultimoDia);
        stats.put("tempo_medio_resposta_ms", tempoMedio != null ? tempoMedio.longValue() : 0);
        
        return ResponseEntity.ok(stats);
    }
    
    @Operation(
        summary = "📋 Histórico de consultas",
        description = """
            **Retorna histórico paginado de todas as consultas realizadas**
            
            Este endpoint permite navegar pelo histórico completo de consultas,
            ordenadas da mais recente para a mais antiga.
            
            ### 📄 Paginação
            - **page**: Número da página (inicia em 0)
            - **size**: Quantidade de registros por página
            - **Ordenação**: Por timestamp decrescente (mais recente primeiro)
            
            ### 📊 Informações por Consulta
            - Placa e RENAVAM consultados
            - IP e User-Agent do cliente
            - Status da consulta (sucesso, erro, timeout)
            - Tempo de resposta em millisegundos
            - Comportamento simulado aplicado
            - Timestamp da consulta
            """,
        operationId = "historicoConsultas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Histórico retornado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class),
                examples = @ExampleObject(
                    name = "Página de Consultas",
                    value = """
                        {
                          "content": [
                            {
                              "id": 1,
                              "placa": "ABC1234",
                              "renavam": "12345678901",
                              "clientIp": "192.168.1.100",
                              "userAgent": "Sistema-Sinistros/1.0",
                              "status": "SUCESSO",
                              "responseTimeMs": 1250,
                              "errorMessage": null,
                              "simulatedBehavior": "NORMAL",
                              "consultaTimestamp": "2024-03-03T19:30:00"
                            }
                          ],
                          "totalElements": 150,
                          "totalPages": 15,
                          "size": 10,
                          "number": 0,
                          "first": true,
                          "last": false
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/consultas")
    public ResponseEntity<Page<ConsultaLog>> consultasRecentes(
            @Parameter(
                name = "page",
                description = "Número da página (inicia em 0)",
                example = "0",
                schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(
                name = "size",
                description = "Quantidade de registros por página",
                example = "20",
                schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "20")
            )
            @RequestParam(defaultValue = "20") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.DESC, "consultaTimestamp"));
        
        Page<ConsultaLog> consultas = consultaLogRepository.findAll(pageRequest);
        
        return ResponseEntity.ok(consultas);
    }
    
    @Operation(
        summary = "🔍 Consultas por placa específica",
        description = """
            **Retorna histórico de consultas para uma placa específica**
            
            Este endpoint permite analisar o comportamento de consultas
            para um veículo específico, útil para debugging e análise.
            
            ### 📊 Informações Retornadas
            - Últimas 50 consultas da placa
            - Ordenadas da mais recente para a mais antiga
            - Inclui todos os detalhes de cada consulta
            - Diferentes IPs e User-Agents que consultaram
            
            ### 🎯 Casos de Uso
            - Debugging de problemas específicos
            - Análise de padrões de consulta
            - Auditoria de acessos
            - Monitoramento de veículos específicos
            """,
        operationId = "consultasPorPlaca"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Consultas da placa retornadas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "array", implementation = ConsultaLog.class),
                examples = @ExampleObject(
                    name = "Consultas da Placa ABC1234",
                    value = """
                        [
                          {
                            "id": 15,
                            "placa": "ABC1234",
                            "renavam": "12345678901",
                            "clientIp": "192.168.1.100",
                            "userAgent": "Sistema-Sinistros/1.0",
                            "status": "SUCESSO",
                            "responseTimeMs": 1250,
                            "errorMessage": null,
                            "simulatedBehavior": "NORMAL",
                            "consultaTimestamp": "2024-03-03T19:30:00"
                          },
                          {
                            "id": 12,
                            "placa": "ABC1234",
                            "renavam": "12345678901",
                            "clientIp": "10.0.0.50",
                            "userAgent": "Sistema-Vendas/2.1",
                            "status": "TIMEOUT_SIMULADO",
                            "responseTimeMs": 30000,
                            "errorMessage": "Timeout na consulta ao Detran",
                            "simulatedBehavior": "TIMEOUT",
                            "consultaTimestamp": "2024-03-03T18:45:00"
                          }
                        ]
                        """
                )
            )
        )
    })
    @GetMapping("/consultas/{placa}")
    public ResponseEntity<List<ConsultaLog>> consultasPorPlaca(
            @Parameter(
                name = "placa",
                description = "Placa do veículo para consultar histórico",
                required = true,
                example = "ABC1234",
                schema = @Schema(
                    type = "string",
                    pattern = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$"
                )
            )
            @PathVariable String placa) {
        
        List<ConsultaLog> consultas = consultaLogRepository.findAll()
            .stream()
            .filter(c -> c.getPlaca().equals(placa))
            .sorted((c1, c2) -> c2.getConsultaTimestamp().compareTo(c1.getConsultaTimestamp()))
            .limit(50)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(consultas);
    }
    
    @Operation(
        summary = "📊 Métricas customizadas",
        description = """
            **Retorna métricas específicas para integração com Prometheus/Grafana**
            
            Este endpoint fornece métricas no formato adequado para
            sistemas de monitoramento e alertas.
            
            ### 📈 Métricas Incluídas
            - **Taxa de sucesso** da última hora (0.0 a 1.0)
            - **Total de requests** da última hora
            - **Requests bem-sucedidos** da última hora
            - **Requests com falha** da última hora
            - **Requests com timeout** da última hora
            
            ### 🎯 Integração
            - Prometheus scraping
            - Dashboards Grafana
            - Alertas automáticos
            - SLA monitoring
            """,
        operationId = "metricas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Métricas retornadas com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Métricas Customizadas",
                    value = """
                        {
                          "detran_success_rate": 0.85,
                          "detran_total_requests_last_hour": 100,
                          "detran_successful_requests_last_hour": 85,
                          "detran_failed_requests_last_hour": 10,
                          "detran_timeout_requests_last_hour": 5
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/metrics/custom")
    public ResponseEntity<Map<String, Object>> customMetrics() {
        LocalDateTime ultimaHora = LocalDateTime.now().minusHours(1);
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Taxa de sucesso
        long totalConsultas = consultaLogRepository.findByPeriodo(ultimaHora, LocalDateTime.now()).size();
        long sucessos = consultaLogRepository.countByStatusAndTimestampAfter(
            ConsultaLog.StatusConsulta.SUCESSO, ultimaHora);
        
        double taxaSucesso = totalConsultas > 0 ? (double) sucessos / totalConsultas : 0.0;
        
        metrics.put("detran_success_rate", taxaSucesso);
        metrics.put("detran_total_requests_last_hour", totalConsultas);
        metrics.put("detran_successful_requests_last_hour", sucessos);
        metrics.put("detran_failed_requests_last_hour", 
            consultaLogRepository.countByStatusAndTimestampAfter(
                ConsultaLog.StatusConsulta.INDISPONIBILIDADE_SIMULADA, ultimaHora));
        metrics.put("detran_timeout_requests_last_hour",
            consultaLogRepository.countByStatusAndTimestampAfter(
                ConsultaLog.StatusConsulta.TIMEOUT_SIMULADO, ultimaHora));
        
        return ResponseEntity.ok(metrics);
    }
    
    @Operation(
        summary = "🗑️ Limpar logs antigos",
        description = """
            **Remove logs de consultas mais antigos que o período especificado**
            
            Este endpoint é útil para manutenção e limpeza da base de dados
            de logs, evitando acúmulo excessivo de dados históricos.
            
            ### ⚠️ Atenção
            - Esta operação é **irreversível**
            - Use com cuidado em ambiente de produção
            - Recomendado para ambientes de teste
            
            ### 🎯 Casos de Uso
            - Limpeza após testes de carga
            - Manutenção periódica
            - Reset de ambiente de desenvolvimento
            - Controle de crescimento da base
            """,
        operationId = "limparLogs"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Limpeza realizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Resultado da Limpeza",
                    value = """
                        {
                          "message": "Logs limpos com sucesso",
                          "registros_removidos": "45",
                          "cutoff_date": "2024-02-25T19:30:00"
                        }
                        """
                )
            )
        )
    })
    @DeleteMapping("/consultas/cleanup")
    public ResponseEntity<Map<String, String>> cleanupLogs(
            @Parameter(
                name = "diasAtras",
                description = "Número de dias atrás para manter os logs (remove logs mais antigos)",
                example = "7",
                schema = @Schema(type = "integer", minimum = "1", defaultValue = "7")
            )
            @RequestParam(defaultValue = "7") int diasAtras) {
        
        LocalDateTime cutoff = LocalDateTime.now().minusDays(diasAtras);
        
        List<ConsultaLog> logsAntigos = consultaLogRepository.findAll()
            .stream()
            .filter(log -> log.getConsultaTimestamp().isBefore(cutoff))
            .collect(Collectors.toList());
        
        consultaLogRepository.deleteAll(logsAntigos);
        
        return ResponseEntity.ok(Map.of(
            "message", "Logs limpos com sucesso",
            "registros_removidos", String.valueOf(logsAntigos.size()),
            "cutoff_date", cutoff.toString()
        ));
    }
}