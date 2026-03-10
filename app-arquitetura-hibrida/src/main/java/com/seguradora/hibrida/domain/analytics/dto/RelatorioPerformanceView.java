package com.seguradora.hibrida.domain.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * View para relatório de performance operacional.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Relatório de performance operacional")
public class RelatorioPerformanceView {
    
    @Schema(description = "Data de início do período", example = "2024-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodoInicio;
    
    @Schema(description = "Data de fim do período", example = "2024-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodoFim;
    
    @Schema(description = "Evolução da taxa de renovação ao longo do tempo")
    private List<EvolucaoTemporalView> evolucaoTaxaRenovacao;
    
    @Schema(description = "Top 5 regiões por número de apólices")
    private Map<String, Long> topRegioes;
    
    @Schema(description = "Top 5 produtos por receita")
    private Map<String, Long> topProdutos;
    
    @Schema(description = "Top 5 canais por volume de vendas")
    private Map<String, Long> topCanais;
}