package com.seguradora.hibrida.domain.sinistro.query.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroDashboardView Tests")
class SinistroDashboardViewTest {

    @Test
    @DisplayName("calcularTaxaAprovacao deve retornar zero quando não há finalizados")
    void calcularTaxaAprovacaoShouldReturnZeroWhenNoFinished() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .totalSinistros(10)
                .sinistrosAprovados(0)
                .sinistrosReprovados(0)
                .build();

        assertThat(view.calcularTaxaAprovacao()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calcularTaxaAprovacao deve calcular corretamente")
    void calcularTaxaAprovacaoShouldCalculateCorrectly() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosAprovados(75)
                .sinistrosReprovados(25)
                .build();

        assertThat(view.calcularTaxaAprovacao()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    @DisplayName("calcularTaxaReprovacao deve calcular corretamente")
    void calcularTaxaReprovacaoShouldCalculateCorrectly() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosAprovados(75)
                .sinistrosReprovados(25)
                .build();

        assertThat(view.calcularTaxaReprovacao()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("calcularTaxaReprovacao deve retornar zero quando não há finalizados")
    void calcularTaxaReprovacaoShouldReturnZeroWhenNoFinished() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosAprovados(0)
                .sinistrosReprovados(0)
                .build();

        assertThat(view.calcularTaxaReprovacao()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getTempoMedioHoras deve retornar zero quando tempoMedioProcessamento é zero")
    void getTempoMedioHorasShouldReturnZeroForZeroProcessamento() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .tempoMedioProcessamento(0L)
                .build();

        assertThat(view.getTempoMedioHoras()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getTempoMedioHoras deve converter minutos para horas")
    void getTempoMedioHorasShouldConvertMinutesToHours() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .tempoMedioProcessamento(120L)
                .build();

        assertThat(view.getTempoMedioHoras()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("getTempoMedioDias deve converter minutos para dias")
    void getTempoMedioDiasShouldConvertMinutesToDays() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .tempoMedioProcessamento(1440L) // 24h = 1 dia
                .build();

        assertThat(view.getTempoMedioDias()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("getTempoMedioDias deve retornar zero quando tempoMedioProcessamento é zero")
    void getTempoMedioDiasShouldReturnZeroForZero() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .tempoMedioProcessamento(0L)
                .build();

        assertThat(view.getTempoMedioDias()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("isAcimaSla deve retornar false quando não há sinistros fora do SLA")
    void isAcimaSlaFalseWhenNoneOutsideSla() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosForaSla(0)
                .build();

        assertThat(view.isAcimaSla()).isFalse();
    }

    @Test
    @DisplayName("isAcimaSla deve retornar true quando há sinistros fora do SLA")
    void isAcimaSlatrueWhenSomeOutsideSla() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosForaSla(3)
                .build();

        assertThat(view.isAcimaSla()).isTrue();
    }

    @Test
    @DisplayName("getPercentualSla deve retornar 100 quando não há dados de SLA")
    void getPercentualSlaShouldReturn100WhenNoData() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosDentroSla(0)
                .sinistrosForaSla(0)
                .build();

        assertThat(view.getPercentualSla()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("getPercentualSla deve calcular percentual corretamente")
    void getPercentualSlaShouldCalculateCorrectly() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosDentroSla(80)
                .sinistrosForaSla(20)
                .build();

        assertThat(view.getPercentualSla()).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    @Test
    @DisplayName("hasAlertasCriticos deve retornar false quando não há alertas")
    void hasAlertasCriticosFalseWhenNone() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosUrgentes(0)
                .sinistrosForaSla(0)
                .build();

        assertThat(view.hasAlertasCriticos()).isFalse();
    }

    @Test
    @DisplayName("hasAlertasCriticos deve retornar true quando há sinistros urgentes")
    void hasAlertasCriticosTrueForUrgentes() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosUrgentes(1)
                .sinistrosForaSla(0)
                .build();

        assertThat(view.hasAlertasCriticos()).isTrue();
    }

    @Test
    @DisplayName("hasAlertasCriticos deve retornar true quando sinistrosForaSla > 5")
    void hasAlertasCriticosTrueForManyOutsideSla() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosUrgentes(0)
                .sinistrosForaSla(6)
                .build();

        assertThat(view.hasAlertasCriticos()).isTrue();
    }

    @Test
    @DisplayName("hasAlertasCriticos deve retornar false quando sinistrosForaSla == 5")
    void hasAlertasCriticosFalseForExactlyFiveOutsideSla() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosUrgentes(0)
                .sinistrosForaSla(5)
                .build();

        assertThat(view.hasAlertasCriticos()).isFalse();
    }

    @Test
    @DisplayName("incrementarStatus deve criar mapa e incrementar contador")
    void incrementarStatusShouldCreateAndIncrement() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .build();

        view.incrementarStatus("ABERTO");
        view.incrementarStatus("ABERTO");
        view.incrementarStatus("APROVADO");

        assertThat(view.getSinistrosPorStatus()).containsEntry("ABERTO", 2);
        assertThat(view.getSinistrosPorStatus()).containsEntry("APROVADO", 1);
    }

    @Test
    @DisplayName("incrementarTipo deve criar mapa e incrementar contador")
    void incrementarTipoShouldCreateAndIncrement() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .build();

        view.incrementarTipo("COLISAO");
        view.incrementarTipo("COLISAO");
        view.incrementarTipo("ROUBO");

        assertThat(view.getSinistrosPorTipo()).containsEntry("COLISAO", 2);
        assertThat(view.getSinistrosPorTipo()).containsEntry("ROUBO", 1);
    }

    @Test
    @DisplayName("incrementarRegiao deve criar mapa e incrementar contador")
    void incrementarRegiaoShouldCreateAndIncrement() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .build();

        view.incrementarRegiao("SP");
        view.incrementarRegiao("SP");
        view.incrementarRegiao("RJ");

        assertThat(view.getSinistrosPorRegiao()).containsEntry("SP", 2);
        assertThat(view.getSinistrosPorRegiao()).containsEntry("RJ", 1);
    }

    @Test
    @DisplayName("recalcularMetricas deve atualizar taxaAprovacao e taxaReprovacao")
    void recalcularMetricasShouldUpdateRates() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .sinistrosAprovados(60)
                .sinistrosReprovados(40)
                .build();

        view.recalcularMetricas();

        assertThat(view.getTaxaAprovacao()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(view.getTaxaReprovacao()).isEqualByComparingTo(new BigDecimal("40.00"));
    }

    @Test
    @DisplayName("recalcularMetricas deve calcular valorMedio corretamente")
    void recalcularMetricasShouldCalculateValorMedio() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .totalSinistros(4)
                .valorTotal(new BigDecimal("20000.00"))
                .build();

        view.recalcularMetricas();

        assertThat(view.getValorMedio()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    @DisplayName("toString deve conter período e tipo")
    void toStringShouldContainPeriodAndType() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .totalSinistros(10)
                .sinistrosAprovados(7)
                .sinistrosReprovados(3)
                .build();

        String str = view.toString();

        assertThat(str).contains("2024-01");
        assertThat(str).contains("MES");
    }
}
