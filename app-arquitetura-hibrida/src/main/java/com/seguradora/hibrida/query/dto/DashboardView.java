package com.seguradora.hibrida.query.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO para view do dashboard de sinistros.
 * 
 * <p>Contém métricas agregadas e estatísticas para
 * exibição em dashboards e relatórios.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dados agregados para dashboard de sinistros")
public record DashboardView(
        
        @Schema(description = "Total de sinistros no sistema", example = "1250")
        Long totalSinistros,
        
        @Schema(description = "Número de sinistros abertos", example = "85")
        Long sinistrosAbertos,
        
        @Schema(description = "Número de consultas DETRAN pendentes", example = "12")
        Long consultasPendentes,
        
        @Schema(description = "Estatísticas por status do sinistro")
        Map<String, Long> estatisticasPorStatus,
        
        @Schema(description = "Estatísticas diárias dos últimos 30 dias")
        Map<LocalDate, Long> estatisticasDiarias,
        
        @Schema(description = "Estatísticas por tipo de sinistro")
        Map<String, Long> estatisticasPorTipo,
        
        @Schema(description = "Estatísticas por operador")
        Map<String, Long> estatisticasPorOperador,
        
        @Schema(description = "Estatísticas por prioridade")
        Map<String, Long> estatisticasPorPrioridade,
        
        @Schema(description = "Taxa de resolução (sinistros fechados / total)")
        Double taxaResolucao,
        
        @Schema(description = "Tempo médio de resolução em dias")
        Double tempoMedioResolucao,
        
        @Schema(description = "Valor total estimado dos sinistros abertos")
        java.math.BigDecimal valorTotalAbertos
) {}