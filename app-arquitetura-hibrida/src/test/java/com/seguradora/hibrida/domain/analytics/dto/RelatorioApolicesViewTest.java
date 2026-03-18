package com.seguradora.hibrida.domain.analytics.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link RelatorioApolicesView}.
 */
@DisplayName("RelatorioApolicesView Tests")
class RelatorioApolicesViewTest {

    @Test
    @DisplayName("Builder deve criar instância com todos os campos")
    void builderShouldCreateInstanceWithAllFields() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim = LocalDate.of(2024, 12, 31);

        RelatorioApolicesView view = RelatorioApolicesView.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalApolices(300L)
                .premioMedio(new BigDecimal("1500.00"))
                .taxaRenovacao(new BigDecimal("85.50"))
                .distribuicaoPorProduto(Map.of("Seguro Auto", 150L, "Seguro Vida", 150L))
                .distribuicaoPorCanal(Map.of("ONLINE", 200L, "TELEFONE", 100L))
                .evolucaoReceita(List.of())
                .build();

        assertThat(view.getPeriodoInicio()).isEqualTo(inicio);
        assertThat(view.getPeriodoFim()).isEqualTo(fim);
        assertThat(view.getTotalApolices()).isEqualTo(300L);
        assertThat(view.getPremioMedio()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(view.getTaxaRenovacao()).isEqualByComparingTo(new BigDecimal("85.50"));
        assertThat(view.getDistribuicaoPorProduto()).hasSize(2);
        assertThat(view.getDistribuicaoPorCanal()).hasSize(2);
        assertThat(view.getEvolucaoReceita()).isEmpty();
    }
}
