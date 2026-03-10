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
 * View para dashboard executivo com métricas principais.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dashboard executivo com métricas principais do negócio")
public class DashboardExecutivoView {
    
    @Schema(description = "Data de referência das métricas", example = "2024-12-19")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataReferencia;
    
    // === MÉTRICAS PRINCIPAIS ===
    
    @Schema(description = "Total de segurados ativos", example = "15420")
    private Long totalSegurados;
    
    @Schema(description = "Total de apólices ativas", example = "12350")
    private Long totalApolices;
    
    @Schema(description = "Receita total acumulada", example = "2450000.00")
    private BigDecimal receitaTotal;
    
    @Schema(description = "Taxa média de renovação (%)", example = "85.5")
    private Double taxaRenovacaoMedia;
    
    @Schema(description = "Taxa média de cancelamento (%)", example = "12.3")
    private Double taxaCancelamentoMedia;
    
    // === MÉTRICAS DO PERÍODO ===
    
    @Schema(description = "Novos segurados no período", example = "245")
    private Long novosSegurados;
    
    @Schema(description = "Novas apólices no período", example = "189")
    private Long novasApolices;
    
    @Schema(description = "Renovações no período", example = "156")
    private Long renovacoes;
    
    @Schema(description = "Cancelamentos no período", example = "23")
    private Long cancelamentos;
    
    @Schema(description = "Prêmio médio das apólices", example = "1250.00")
    private BigDecimal premioMedio;
    
    @Schema(description = "Score médio de renovação", example = "78.5")
    private BigDecimal scoreMedioRenovacao;
    
    // === INDICADORES DE CRESCIMENTO ===
    
    @Schema(description = "Crescimento de segurados (%)", example = "12.5")
    private BigDecimal crescimentoSegurados;
    
    @Schema(description = "Crescimento de apólices (%)", example = "8.7")
    private BigDecimal crescimentoApolices;
    
    // === MÉTODOS DE CONVENIÊNCIA ===
    
    /**
     * Calcula ticket médio por segurado.
     */
    public BigDecimal getTicketMedioPorSegurado() {
        if (totalSegurados == null || totalSegurados == 0 || receitaTotal == null) {
            return BigDecimal.ZERO;
        }
        return receitaTotal.divide(BigDecimal.valueOf(totalSegurados), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Calcula taxa de conversão segurado -> apólice.
     */
    public BigDecimal getTaxaConversao() {
        if (totalSegurados == null || totalSegurados == 0 || totalApolices == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(totalApolices)
            .divide(BigDecimal.valueOf(totalSegurados), 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Verifica se as métricas indicam crescimento saudável.
     */
    public boolean isCrescimentoSaudavel() {
        return crescimentoSegurados != null && crescimentoSegurados.compareTo(BigDecimal.ZERO) > 0 &&
               crescimentoApolices != null && crescimentoApolices.compareTo(BigDecimal.ZERO) > 0 &&
               taxaRenovacaoMedia != null && taxaRenovacaoMedia > 70.0 &&
               taxaCancelamentoMedia != null && taxaCancelamentoMedia < 20.0;
    }
    
    /**
     * Obtém status geral do negócio.
     */
    public String getStatusNegocio() {
        if (isCrescimentoSaudavel()) {
            return "EXCELENTE";
        } else if (taxaRenovacaoMedia != null && taxaRenovacaoMedia > 60.0) {
            return "BOM";
        } else if (taxaRenovacaoMedia != null && taxaRenovacaoMedia > 40.0) {
            return "REGULAR";
        } else {
            return "ATENÇÃO";
        }
    }
}