package com.seguradora.hibrida.domain.veiculo.relationship.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para dashboard de relacionamentos Veículo-Apólice.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
public class DashboardRelacionamentosDTO {

    private long totalRelacionamentosAtivos;
    private long totalRelacionamentosEncerrados;
    private long totalRelacionamentosCancelados;
    private long totalVeiculosSemCobertura;
    private long totalVencendoEm30Dias;
    private long totalComGapCobertura;
    private LocalDate dataAtualizacao;

    /**
     * Calcula taxa de cobertura (veículos com cobertura / total).
     */
    public double calcularTaxaCobertura() {
        long totalVeiculos = totalRelacionamentosAtivos + totalVeiculosSemCobertura;
        if (totalVeiculos == 0) {
            return 0.0;
        }
        return (double) totalRelacionamentosAtivos / totalVeiculos * 100.0;
    }

    /**
     * Calcula taxa de cancelamento.
     */
    public double calcularTaxaCancelamento() {
        long totalRelacionamentos = totalRelacionamentosAtivos +
                                    totalRelacionamentosEncerrados +
                                    totalRelacionamentosCancelados;
        if (totalRelacionamentos == 0) {
            return 0.0;
        }
        return (double) totalRelacionamentosCancelados / totalRelacionamentos * 100.0;
    }
}
