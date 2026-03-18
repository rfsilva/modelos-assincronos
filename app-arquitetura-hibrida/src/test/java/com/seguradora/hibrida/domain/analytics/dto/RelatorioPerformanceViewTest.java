package com.seguradora.hibrida.domain.analytics.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link RelatorioPerformanceView}.
 */
@DisplayName("RelatorioPerformanceView Tests")
class RelatorioPerformanceViewTest {

    @Test
    @DisplayName("Builder deve criar instância com todos os campos")
    void builderShouldCreateInstanceWithAllFields() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim = LocalDate.of(2024, 12, 31);

        RelatorioPerformanceView view = RelatorioPerformanceView.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .evolucaoTaxaRenovacao(List.of())
                .topRegioes(Map.of("SP", 500L, "RJ", 300L))
                .topProdutos(Map.of("Seguro Auto", 800L))
                .topCanais(Map.of("ONLINE", 600L))
                .build();

        assertThat(view.getPeriodoInicio()).isEqualTo(inicio);
        assertThat(view.getPeriodoFim()).isEqualTo(fim);
        assertThat(view.getEvolucaoTaxaRenovacao()).isEmpty();
        assertThat(view.getTopRegioes()).containsKey("SP");
        assertThat(view.getTopProdutos()).containsKey("Seguro Auto");
        assertThat(view.getTopCanais()).containsKey("ONLINE");
    }
}
