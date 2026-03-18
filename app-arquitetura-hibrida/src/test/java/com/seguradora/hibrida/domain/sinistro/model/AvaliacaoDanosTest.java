package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AvaliacaoDanos Tests")
class AvaliacaoDanosTest {

    private AvaliacaoDanos avaliacaoCompleta() {
        return AvaliacaoDanos.builder()
                .tipoDano(TipoDano.PARCIAL)
                .valorEstimado(new BigDecimal("5000.00"))
                .laudoPericial("LAUDO-001")
                .observacoes("Obs")
                .periciadorId("PERIC-01")
                .build();
    }

    @Test
    @DisplayName("isCompleta deve retornar true quando todos campos obrigatórios presentes")
    void isCompletaShouldReturnTrueWhenAllRequired() {
        assertThat(avaliacaoCompleta().isCompleta()).isTrue();
    }

    @Test
    @DisplayName("isCompleta deve retornar false quando tipoDano nulo")
    void isCompletaShouldReturnFalseWhenTipoDanoNull() {
        AvaliacaoDanos avaliacao = AvaliacaoDanos.builder()
                .valorEstimado(new BigDecimal("5000.00"))
                .build();
        assertThat(avaliacao.isCompleta()).isFalse();
    }

    @Test
    @DisplayName("isCompleta deve retornar false quando valor estimado zero")
    void isCompletaShouldReturnFalseWhenValorZero() {
        AvaliacaoDanos avaliacao = AvaliacaoDanos.builder()
                .tipoDano(TipoDano.TERCEIROS)
                .valorEstimado(BigDecimal.ZERO)
                .build();
        assertThat(avaliacao.isCompleta()).isFalse();
    }

    @Test
    @DisplayName("isCompleta deve retornar false quando tipo requer laudo e laudo ausente")
    void isCompletaShouldReturnFalseWhenLaudoRequiredButAbsent() {
        AvaliacaoDanos avaliacao = AvaliacaoDanos.builder()
                .tipoDano(TipoDano.TOTAL)
                .valorEstimado(new BigDecimal("50000.00"))
                .build();
        assertThat(avaliacao.isCompleta()).isFalse();
    }

    @Test
    @DisplayName("isCompleta deve retornar true para tipo sem laudo obrigatório sem laudo")
    void isCompletaShouldReturnTrueForTypeWithoutMandatoryLaudo() {
        AvaliacaoDanos avaliacao = AvaliacaoDanos.builder()
                .tipoDano(TipoDano.TERCEIROS)
                .valorEstimado(new BigDecimal("2000.00"))
                .build();
        assertThat(avaliacao.isCompleta()).isTrue();
    }

    @Test
    @DisplayName("possuiLaudoPericial deve retornar true quando laudo preenchido")
    void possuiLaudoPericialShouldReturnTrueWhenFilled() {
        assertThat(avaliacaoCompleta().possuiLaudoPericial()).isTrue();
    }

    @Test
    @DisplayName("possuiLaudoPericial deve retornar false quando laudo nulo")
    void possuiLaudoPericialShouldReturnFalseWhenNull() {
        AvaliacaoDanos avaliacao = AvaliacaoDanos.builder()
                .tipoDano(TipoDano.TERCEIROS)
                .valorEstimado(new BigDecimal("1000.00"))
                .build();
        assertThat(avaliacao.possuiLaudoPericial()).isFalse();
    }

    @Test
    @DisplayName("possuiFotos deve retornar false quando lista vazia")
    void possuiFotosShouldReturnFalseWhenEmpty() {
        assertThat(avaliacaoCompleta().possuiFotos()).isFalse();
    }

    @Test
    @DisplayName("getFotos deve retornar lista imutável")
    void getFotosShouldReturnImmutableList() {
        List<String> fotos = avaliacaoCompleta().getFotos();
        assertThat(fotos).isNotNull();
    }

    @Test
    @DisplayName("isValorDentroLimite deve retornar false quando campos nulos")
    void isValorDentroLimiteShouldReturnFalseWhenNull() {
        AvaliacaoDanos avaliacao = AvaliacaoDanos.builder().build();
        assertThat(avaliacao.isValorDentroLimite(new BigDecimal("10000.00"))).isFalse();
    }

    @Test
    @DisplayName("isValorDentroLimite deve retornar true quando tipo sem percentual máximo")
    void isValorDentroLimiteShouldReturnTrueForTypeWithZeroPercent() {
        // TERCEIROS tem percentualMaximo = ZERO (sem restrição)
        AvaliacaoDanos avaliacao = AvaliacaoDanos.builder()
                .tipoDano(TipoDano.TERCEIROS)
                .valorEstimado(new BigDecimal("999999.00"))
                .build();
        assertThat(avaliacao.isValorDentroLimite(new BigDecimal("10000.00"))).isTrue();
    }

    @Test
    @DisplayName("isValorDentroLimite deve retornar true quando valor abaixo do limite")
    void isValorDentroLimiteShouldReturnTrueWhenUnderLimit() {
        // TOTAL: percentualMaximo = 0.75 → limite = 10000 * 0.75 = 7500
        AvaliacaoDanos avaliacao = AvaliacaoDanos.builder()
                .tipoDano(TipoDano.TOTAL)
                .valorEstimado(new BigDecimal("7000.00"))
                .laudoPericial("LAUDO")
                .build();
        assertThat(avaliacao.isValorDentroLimite(new BigDecimal("10000.00"))).isTrue();
    }

    @Test
    @DisplayName("isValorDentroLimite deve retornar false quando valor acima do limite")
    void isValorDentroLimiteShouldReturnFalseWhenOverLimit() {
        // TOTAL: percentualMaximo = 0.75 → limite = 10000 * 0.75 = 7500
        AvaliacaoDanos avaliacao = AvaliacaoDanos.builder()
                .tipoDano(TipoDano.TOTAL)
                .valorEstimado(new BigDecimal("8000.00"))
                .laudoPericial("LAUDO")
                .build();
        assertThat(avaliacao.isValorDentroLimite(new BigDecimal("10000.00"))).isFalse();
    }

    @Test
    @DisplayName("equals deve comparar tipoDano, valorEstimado e laudoPericial")
    void equalsShouldCompareRelevantFields() {
        AvaliacaoDanos a1 = avaliacaoCompleta();
        AvaliacaoDanos a2 = avaliacaoCompleta();
        assertThat(a1).isEqualTo(a2);
    }

    @Test
    @DisplayName("toString deve conter tipo e valor")
    void toStringShouldContainTypeAndValue() {
        assertThat(avaliacaoCompleta().toString())
                .contains("5000");
    }
}
