package com.seguradora.hibrida.domain.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * View para relatório de apólices.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Relatório detalhado de apólices por período")
public class RelatorioApolicesView {
    
    @Schema(description = "Data de início do período", example = "2024-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodoInicio;
    
    @Schema(description = "Data de fim do período", example = "2024-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodoFim;
    
    @Schema(description = "Total de apólices no período", example = "12350")
    private Long totalApolices;
    
    @Schema(description = "Prêmio médio das apólices", example = "1250.00")
    private BigDecimal premioMedio;
    
    @Schema(description = "Taxa de renovação (%)", example = "85.5")
    private BigDecimal taxaRenovacao;
    
    @Schema(description = "Distribuição de apólices por produto")
    private Map<String, Long> distribuicaoPorProduto;
    
    @Schema(description = "Distribuição de apólices por canal de venda")
    private Map<String, Long> distribuicaoPorCanal;
    
    @Schema(description = "Evolução da receita ao longo do tempo")
    private List<EvolucaoTemporalView> evolucaoReceita;
}