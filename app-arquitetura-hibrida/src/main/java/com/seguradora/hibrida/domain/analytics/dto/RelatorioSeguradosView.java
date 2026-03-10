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
 * View para relatório de segurados.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Relatório detalhado de segurados por período")
public class RelatorioSeguradosView {
    
    @Schema(description = "Data de início do período", example = "2024-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodoInicio;
    
    @Schema(description = "Data de fim do período", example = "2024-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodoFim;
    
    @Schema(description = "Total de segurados no período", example = "15420")
    private Long totalSegurados;
    
    @Schema(description = "Distribuição de segurados por região")
    private Map<String, Long> distribuicaoPorRegiao;
    
    @Schema(description = "Distribuição de segurados por faixa etária")
    private Map<String, Long> distribuicaoPorFaixaEtaria;
    
    @Schema(description = "Evolução temporal do número de segurados")
    private List<EvolucaoTemporalView> evolucaoTemporal;
}