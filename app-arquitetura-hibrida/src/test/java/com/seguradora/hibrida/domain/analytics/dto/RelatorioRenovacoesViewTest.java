package com.seguradora.hibrida.domain.analytics.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link RelatorioRenovacoesView}.
 */
@DisplayName("RelatorioRenovacoesView Tests")
class RelatorioRenovacoesViewTest {

    @Test
    @DisplayName("Builder deve criar instância com todos os campos")
    void builderShouldCreateInstanceWithAllFields() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim = LocalDate.of(2024, 12, 31);

        RelatorioRenovacoesView view = RelatorioRenovacoesView.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalRenovacoes(1250L)
                .taxaRenovacaoMedia(new BigDecimal("85.50"))
                .receitaRenovacoes(new BigDecimal("1875000.00"))
                .build();

        assertThat(view.getPeriodoInicio()).isEqualTo(inicio);
        assertThat(view.getPeriodoFim()).isEqualTo(fim);
        assertThat(view.getTotalRenovacoes()).isEqualTo(1250L);
        assertThat(view.getTaxaRenovacaoMedia()).isEqualByComparingTo(new BigDecimal("85.50"));
        assertThat(view.getReceitaRenovacoes()).isEqualByComparingTo(new BigDecimal("1875000.00"));
    }
}
