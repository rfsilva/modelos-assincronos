package com.seguradora.hibrida.domain.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * View para relatório de renovações.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Relatório específico de renovações de apólices")
public class RelatorioRenovacoesView {
    
    @Schema(description = "Data de início do período", example = "2024-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodoInicio;
    
    @Schema(description = "Data de fim do período", example = "2024-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodoFim;
    
    @Schema(description = "Total de renovações no período", example = "1250")
    private Long totalRenovacoes;
    
    @Schema(description = "Taxa média de renovação (%)", example = "85.5")
    private BigDecimal taxaRenovacaoMedia;
    
    @Schema(description = "Receita total das renovações", example = "1875000.00")
    private BigDecimal receitaRenovacoes;
}