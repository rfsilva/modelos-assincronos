package com.seguradora.hibrida.domain.sinistro.query.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroAnalyticsView Tests")
class SinistroAnalyticsViewTest {

    @Test
    @DisplayName("calcularTaxaAprovacao deve retornar zero quando não há finalizados")
    void calcularTaxaAprovacaoZeroWhenNoFinished() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidade(10)
                .quantidadeAprovados(0)
                .quantidadeReprovados(0)
                .build();

        assertThat(view.calcularTaxaAprovacao()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calcularTaxaAprovacao deve calcular corretamente")
    void calcularTaxaAprovacaoShouldCalculate() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidadeAprovados(80)
                .quantidadeReprovados(20)
                .build();

        assertThat(view.calcularTaxaAprovacao()).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    @Test
    @DisplayName("calcularTaxaReprovacao deve calcular corretamente")
    void calcularTaxaReprovacaoShouldCalculate() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidadeAprovados(80)
                .quantidadeReprovados(20)
                .build();

        assertThat(view.calcularTaxaReprovacao()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("calcularTaxaConversao deve retornar zero quando quantidade é zero")
    void calcularTaxaConversaoZeroForZeroQuantidade() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidade(0)
                .build();

        assertThat(view.calcularTaxaConversao()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calcularTaxaConversao deve calcular percentual de finalizados sobre total")
    void calcularTaxaConversaoShouldCalculate() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidade(100)
                .quantidadeAprovados(60)
                .quantidadeReprovados(20)
                .build();

        assertThat(view.calcularTaxaConversao()).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    @Test
    @DisplayName("calcularPercentualSla deve retornar 100 quando não há dados de SLA")
    void calcularPercentualSlaReturn100WhenNoData() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidadeDentroSla(0)
                .quantidadeForaSla(0)
                .build();

        assertThat(view.calcularPercentualSla()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("calcularPercentualSla deve calcular percentual corretamente")
    void calcularPercentualSlaShouldCalculate() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidadeDentroSla(90)
                .quantidadeForaSla(10)
                .build();

        assertThat(view.calcularPercentualSla()).isEqualByComparingTo(new BigDecimal("90.00"));
    }

    @Test
    @DisplayName("calcularScoreQualidade deve retornar 100 para caso ideal")
    void calcularScoreQualidadePerfect() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidade(100)
                .quantidadeDentroSla(100)
                .quantidadeForaSla(0)
                .quantidadeRetrabalho(0)
                .tempoMedioProcessamento(60L) // 1h, abaixo do limite de 48h
                .build();

        // SLA=100%, retrabalho=100%, tempo=100% → média ponderada = 100
        assertThat(view.calcularScoreQualidade()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("calcularScoreQualidade deve penalizar SLA ruim")
    void calcularScoreQualidadeShouldPenalizeBadSla() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidade(10)
                .quantidadeDentroSla(5)
                .quantidadeForaSla(5)   // SLA = 50%
                .quantidadeRetrabalho(0)
                .tempoMedioProcessamento(60L)
                .build();

        BigDecimal score = view.calcularScoreQualidade();
        // SLA=50%*0.5 + 100%*0.3 + 100%*0.2 = 25 + 30 + 20 = 75
        assertThat(score).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    @DisplayName("calcularScoreQualidade deve penalizar retrabalho")
    void calcularScoreQualidadeShouldPenalizeRetrabalho() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidade(10)
                .quantidadeDentroSla(10)
                .quantidadeForaSla(0)
                .quantidadeRetrabalho(2) // 20% de retrabalho → penalidade: 0.20 * 50 = 10 → scoreRetrabalho = 90
                .tempoMedioProcessamento(60L)
                .build();

        BigDecimal score = view.calcularScoreQualidade();
        // SLA=100%*0.5 + 90%*0.3 + 100%*0.2 = 50 + 27 + 20 = 97
        assertThat(score).isEqualByComparingTo(new BigDecimal("97.00"));
    }

    @Test
    @DisplayName("determinarTendencia deve retornar ESTAVEL quando variacao é null")
    void determinarTendenciaEstavelForNull() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .build();

        assertThat(view.determinarTendencia()).isEqualTo("ESTAVEL");
    }

    @Test
    @DisplayName("determinarTendencia deve retornar CRESCENTE quando variação > 5%")
    void determinarTendenciaCrescenteAbove5() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .variacaoPeriodoAnterior(new BigDecimal("10.00"))
                .build();

        assertThat(view.determinarTendencia()).isEqualTo("CRESCENTE");
    }

    @Test
    @DisplayName("determinarTendencia deve retornar DECRESCENTE quando variação < -5%")
    void determinarTendenciaDecrescenteBelow5() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .variacaoPeriodoAnterior(new BigDecimal("-10.00"))
                .build();

        assertThat(view.determinarTendencia()).isEqualTo("DECRESCENTE");
    }

    @Test
    @DisplayName("determinarTendencia deve retornar ESTAVEL quando variação entre -5 e 5")
    void determinarTendenciaEstavelWithinRange() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .variacaoPeriodoAnterior(new BigDecimal("3.00"))
                .build();

        assertThat(view.determinarTendencia()).isEqualTo("ESTAVEL");
    }

    @Test
    @DisplayName("isPerformanceAcimaMedia deve retornar true quando score >= 80")
    void isPerformanceAcimaMediaTrueForHighScore() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .scoreQualidade(new BigDecimal("85.00"))
                .build();

        assertThat(view.isPerformanceAcimaMedia()).isTrue();
    }

    @Test
    @DisplayName("isPerformanceAcimaMedia deve retornar false quando score < 80")
    void isPerformanceAcimaMediaFalseForLowScore() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .scoreQualidade(new BigDecimal("75.00"))
                .build();

        assertThat(view.isPerformanceAcimaMedia()).isFalse();
    }

    @Test
    @DisplayName("hasProblemasQualidade deve retornar true quando score < 60")
    void hasProblemasQualidadeTrueForLowScore() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .scoreQualidade(new BigDecimal("50.00"))
                .percentualSla(new BigDecimal("80.00"))
                .build();

        assertThat(view.hasProblemasQualidade()).isTrue();
    }

    @Test
    @DisplayName("hasProblemasQualidade deve retornar true quando percentualSla < 70")
    void hasProblemasQualidadeTrueForLowSla() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .scoreQualidade(new BigDecimal("75.00"))
                .percentualSla(new BigDecimal("60.00"))
                .build();

        assertThat(view.hasProblemasQualidade()).isTrue();
    }

    @Test
    @DisplayName("hasProblemasQualidade deve retornar false quando score e sla são adequados")
    void hasProblemasQualidadeFalseForGoodMetrics() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .scoreQualidade(new BigDecimal("75.00"))
                .percentualSla(new BigDecimal("80.00"))
                .build();

        assertThat(view.hasProblemasQualidade()).isFalse();
    }

    @Test
    @DisplayName("compararCom deve retornar zero quando outro período é null")
    void compararComReturnZeroForNull() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidade(100)
                .build();

        assertThat(view.compararCom(null)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("compararCom deve calcular variação percentual")
    void compararComShouldCalculateVariation() {
        SinistroAnalyticsView atual = SinistroAnalyticsView.builder()
                .periodo("2024-02")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 2, 1))
                .quantidade(110)
                .build();

        SinistroAnalyticsView anterior = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidade(100)
                .build();

        // (110 - 100) / 100 * 100 = 10%
        assertThat(atual.compararCom(anterior)).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("recalcularMetricas deve atualizar todas as métricas derivadas")
    void recalcularMetricasShouldUpdateAll() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidade(100)
                .quantidadeAprovados(70)
                .quantidadeReprovados(30)
                .quantidadeDentroSla(90)
                .quantidadeForaSla(10)
                .quantidadeRetrabalho(0)
                .tempoMedioProcessamento(60L)
                .valorTotal(new BigDecimal("500000.00"))
                .build();

        view.recalcularMetricas();

        assertThat(view.getTaxaAprovacao()).isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(view.getTaxaReprovacao()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(view.getTaxaConversao()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(view.getPercentualSla()).isEqualByComparingTo(new BigDecimal("90.00"));
        assertThat(view.getValorMedio()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    @DisplayName("toString deve conter período e tipo de sinistro")
    void toStringShouldContainPeriodAndType() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .tipoSinistro("COLISAO")
                .quantidade(50)
                .scoreQualidade(new BigDecimal("85.00"))
                .build();

        String str = view.toString();

        assertThat(str).contains("2024-01");
        assertThat(str).contains("COLISAO");
    }
}
