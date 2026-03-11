package com.seguradora.hibrida.domain.sinistro.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa o valor de indenização de um sinistro.
 *
 * <p>Calcula o valor líquido a pagar considerando:
 * <ul>
 *   <li>Valor bruto da indenização</li>
 *   <li>Franquia aplicável</li>
 *   <li>Impostos (IOF, IR)</li>
 *   <li>Descontos (salvados, depreciação)</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
public class ValorIndenizacao {

    private final BigDecimal valorBruto;
    private final BigDecimal franquia;
    private final BigDecimal iof;
    private final BigDecimal ir;
    private final BigDecimal descontos;
    private final String moeda;

    /**
     * Calcula o valor líquido a pagar.
     *
     * Fórmula: Valor Líquido = Valor Bruto - Franquia - IOF - IR - Descontos
     */
    public BigDecimal getValorLiquido() {
        BigDecimal valor = valorBruto != null ? valorBruto : BigDecimal.ZERO;

        if (franquia != null) {
            valor = valor.subtract(franquia);
        }

        if (iof != null) {
            valor = valor.subtract(iof);
        }

        if (ir != null) {
            valor = valor.subtract(ir);
        }

        if (descontos != null) {
            valor = valor.subtract(descontos);
        }

        // Não pode ser negativo
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            valor = BigDecimal.ZERO;
        }

        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o total de deduções.
     */
    public BigDecimal getTotalDeducoes() {
        BigDecimal total = BigDecimal.ZERO;

        if (franquia != null) {
            total = total.add(franquia);
        }

        if (iof != null) {
            total = total.add(iof);
        }

        if (ir != null) {
            total = total.add(ir);
        }

        if (descontos != null) {
            total = total.add(descontos);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Verifica se o valor é válido (maior que zero após deduções).
     */
    public boolean isValido() {
        return getValorLiquido().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Calcula a alíquota IOF (0,38% para seguros).
     */
    public static BigDecimal calcularIOF(BigDecimal valorBase) {
        if (valorBase == null || valorBase.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal aliquota = new BigDecimal("0.0038"); // 0,38%
        return valorBase.multiply(aliquota).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula IR quando aplicável (valores acima de R$ 10.000).
     */
    public static BigDecimal calcularIR(BigDecimal valorBase) {
        if (valorBase == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal limiteIsencao = new BigDecimal("10000.00");

        if (valorBase.compareTo(limiteIsencao) <= 0) {
            return BigDecimal.ZERO;
        }

        // Alíquota progressiva simplificada (15% sobre o excedente)
        BigDecimal valorExcedente = valorBase.subtract(limiteIsencao);
        BigDecimal aliquota = new BigDecimal("0.15");

        return valorExcedente.multiply(aliquota).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValorIndenizacao that = (ValorIndenizacao) o;
        return Objects.equals(valorBruto, that.valorBruto) &&
               Objects.equals(franquia, that.franquia) &&
               Objects.equals(iof, that.iof) &&
               Objects.equals(ir, that.ir) &&
               Objects.equals(descontos, that.descontos) &&
               Objects.equals(moeda, that.moeda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valorBruto, franquia, iof, ir, descontos, moeda);
    }

    @Override
    public String toString() {
        return String.format("%s %.2f (Líquido: %.2f)",
            moeda != null ? moeda : "BRL",
            valorBruto != null ? valorBruto : BigDecimal.ZERO,
            getValorLiquido()
        );
    }
}
