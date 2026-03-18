package com.seguradora.hibrida.domain.sinistro.query.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DashboardView Tests")
class DashboardViewTest {

    @Test
    @DisplayName("deve criar DashboardView com todos os campos")
    void shouldCreateWithAllFields() {
        Map<String, Long> porStatus = Map.of("ABERTO", 10L, "APROVADO", 5L);
        Map<String, Long> porTipo = Map.of("COLISAO", 8L, "ROUBO_FURTO", 7L);

        DashboardView view = new DashboardView(
                100L,
                20L,
                5L,
                porStatus,
                null,
                porTipo,
                null,
                null,
                0.75,
                3.2,
                new BigDecimal("500000.00")
        );

        assertThat(view.totalSinistros()).isEqualTo(100L);
        assertThat(view.sinistrosAbertos()).isEqualTo(20L);
        assertThat(view.consultasPendentes()).isEqualTo(5L);
        assertThat(view.estatisticasPorStatus()).containsKey("ABERTO");
        assertThat(view.estatisticasPorTipo()).containsKey("COLISAO");
        assertThat(view.taxaResolucao()).isEqualTo(0.75);
        assertThat(view.tempoMedioResolucao()).isEqualTo(3.2);
        assertThat(view.valorTotalAbertos()).isEqualByComparingTo("500000.00");
    }

    @Test
    @DisplayName("deve aceitar campos nulos")
    void shouldAcceptNullFields() {
        DashboardView view = new DashboardView(
                null, null, null, null, null, null, null, null, null, null, null
        );

        assertThat(view.totalSinistros()).isNull();
        assertThat(view.sinistrosAbertos()).isNull();
        assertThat(view.consultasPendentes()).isNull();
    }

    @Test
    @DisplayName("deve ser igual quando campos são iguais")
    void shouldBeEqualWhenFieldsAreEqual() {
        DashboardView view1 = new DashboardView(50L, 10L, 2L, null, null, null, null, null, null, null, null);
        DashboardView view2 = new DashboardView(50L, 10L, 2L, null, null, null, null, null, null, null, null);

        assertThat(view1).isEqualTo(view2);
    }

    @Test
    @DisplayName("deve ter hashCode consistente")
    void shouldHaveConsistentHashCode() {
        DashboardView view1 = new DashboardView(50L, 10L, 2L, null, null, null, null, null, null, null, null);
        DashboardView view2 = new DashboardView(50L, 10L, 2L, null, null, null, null, null, null, null, null);

        assertThat(view1.hashCode()).isEqualTo(view2.hashCode());
    }

    @Test
    @DisplayName("toString deve conter informações dos campos")
    void toStringShouldContainFields() {
        DashboardView view = new DashboardView(100L, 20L, 5L, null, null, null, null, null, null, null, null);

        String str = view.toString();

        assertThat(str).contains("100");
        assertThat(str).contains("20");
    }

    @Test
    @DisplayName("estatisticasDiarias deve aceitar mapa por LocalDate")
    void estatisticasDiariasShouldAcceptLocalDateMap() {
        Map<LocalDate, Long> diarias = Map.of(
                LocalDate.of(2024, 3, 1), 10L,
                LocalDate.of(2024, 3, 2), 15L
        );

        DashboardView view = new DashboardView(
                100L, 20L, 5L, null, diarias, null, null, null, null, null, null
        );

        assertThat(view.estatisticasDiarias()).containsKey(LocalDate.of(2024, 3, 1));
        assertThat(view.estatisticasDiarias().get(LocalDate.of(2024, 3, 1))).isEqualTo(10L);
    }

    @Test
    @DisplayName("estatisticasPorOperador e porPrioridade devem funcionar")
    void estatisticasPorOperadorEPrioridadeShouldWork() {
        Map<String, Long> porOperador = Map.of("João", 5L);
        Map<String, Long> porPrioridade = Map.of("ALTA", 3L, "NORMAL", 7L);

        DashboardView view = new DashboardView(
                10L, 3L, 0L, null, null, null, porOperador, porPrioridade, null, null, null
        );

        assertThat(view.estatisticasPorOperador()).containsKey("João");
        assertThat(view.estatisticasPorPrioridade()).containsKey("ALTA");
    }
}
