package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValorIndenizacao Tests")
class ValorIndenizacaoTest {

    @Test
    @DisplayName("getValorLiquido deve subtrair todas as deduções")
    void getValorLiquidoShouldSubtractAllDeductions() {
        ValorIndenizacao vi = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("10000.00"))
                .franquia(new BigDecimal("1000.00"))
                .iof(new BigDecimal("38.00"))
                .ir(BigDecimal.ZERO)
                .descontos(BigDecimal.ZERO)
                .build();

        assertThat(vi.getValorLiquido()).isEqualByComparingTo("8962.00");
    }

    @Test
    @DisplayName("getValorLiquido não pode ser negativo")
    void getValorLiquidoShouldNotBeNegative() {
        ValorIndenizacao vi = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("100.00"))
                .franquia(new BigDecimal("500.00"))
                .build();

        assertThat(vi.getValorLiquido()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getValorLiquido com todos nulos deve retornar zero")
    void getValorLiquidoWithAllNullsShouldReturnZero() {
        ValorIndenizacao vi = ValorIndenizacao.builder().build();
        assertThat(vi.getValorLiquido()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getTotalDeducoes deve somar todas as deduções")
    void getTotalDeducoesShouldSumAllDeductions() {
        ValorIndenizacao vi = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("10000.00"))
                .franquia(new BigDecimal("1000.00"))
                .iof(new BigDecimal("38.00"))
                .ir(new BigDecimal("100.00"))
                .descontos(new BigDecimal("50.00"))
                .build();

        assertThat(vi.getTotalDeducoes()).isEqualByComparingTo("1188.00");
    }

    @Test
    @DisplayName("isValido deve retornar true quando valor liquido > 0")
    void isValidoShouldReturnTrueWhenPositive() {
        ValorIndenizacao vi = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("5000.00"))
                .build();
        assertThat(vi.isValido()).isTrue();
    }

    @Test
    @DisplayName("isValido deve retornar false quando valor liquido é zero")
    void isValidoShouldReturnFalseWhenZero() {
        ValorIndenizacao vi = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("100.00"))
                .franquia(new BigDecimal("100.00"))
                .build();
        assertThat(vi.isValido()).isFalse();
    }

    @Test
    @DisplayName("calcularIOF deve calcular 0.38% do valor")
    void calcularIOFShouldCalculate038Percent() {
        BigDecimal iof = ValorIndenizacao.calcularIOF(new BigDecimal("10000.00"));
        assertThat(iof).isEqualByComparingTo("38.00");
    }

    @Test
    @DisplayName("calcularIOF com valor nulo ou zero deve retornar zero")
    void calcularIOFWithNullOrZeroShouldReturnZero() {
        assertThat(ValorIndenizacao.calcularIOF(null)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ValorIndenizacao.calcularIOF(BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calcularIR deve retornar zero para valores até R$10000")
    void calcularIRShouldReturnZeroForValuesUnder10000() {
        assertThat(ValorIndenizacao.calcularIR(new BigDecimal("10000.00"))).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ValorIndenizacao.calcularIR(new BigDecimal("5000.00"))).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calcularIR deve calcular 15% sobre o excedente de R$10000")
    void calcularIRShouldCalculate15PercentOverExcess() {
        // Excedente = 15000 - 10000 = 5000. IR = 5000 * 0.15 = 750
        BigDecimal ir = ValorIndenizacao.calcularIR(new BigDecimal("15000.00"));
        assertThat(ir).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("calcularIR com nulo deve retornar zero")
    void calcularIRWithNullShouldReturnZero() {
        assertThat(ValorIndenizacao.calcularIR(null)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("equals deve comparar todos os campos relevantes")
    void equalsShouldCompareAllRelevantFields() {
        ValorIndenizacao v1 = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("5000.00"))
                .franquia(new BigDecimal("500.00"))
                .moeda("BRL")
                .build();
        ValorIndenizacao v2 = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("5000.00"))
                .franquia(new BigDecimal("500.00"))
                .moeda("BRL")
                .build();

        assertThat(v1).isEqualTo(v2);
    }

    @Test
    @DisplayName("toString deve incluir moeda e valor líquido")
    void toStringShouldIncludeCurrencyAndLiquidValue() {
        ValorIndenizacao vi = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("5000.00"))
                .moeda("BRL")
                .build();
        assertThat(vi.toString()).contains("BRL").contains("5000");
    }
}
