package com.seguradora.hibrida.domain.analytics.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link RelatorioSeguradosView}.
 */
@DisplayName("RelatorioSeguradosView Tests")
class RelatorioSeguradosViewTest {

    @Test
    @DisplayName("Builder deve criar instância com todos os campos")
    void builderShouldCreateInstanceWithAllFields() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim = LocalDate.of(2024, 12, 31);

        RelatorioSeguradosView view = RelatorioSeguradosView.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalSegurados(15420L)
                .distribuicaoPorRegiao(Map.of("SP", 5000L, "RJ", 3000L))
                .distribuicaoPorFaixaEtaria(Map.of("26-35", 4000L, "36-45", 3500L))
                .evolucaoTemporal(List.of(
                        EvolucaoTemporalView.builder().data(inicio).valor(100L).build()
                ))
                .build();

        assertThat(view.getPeriodoInicio()).isEqualTo(inicio);
        assertThat(view.getPeriodoFim()).isEqualTo(fim);
        assertThat(view.getTotalSegurados()).isEqualTo(15420L);
        assertThat(view.getDistribuicaoPorRegiao()).containsKey("SP");
        assertThat(view.getDistribuicaoPorFaixaEtaria()).containsKey("26-35");
        assertThat(view.getEvolucaoTemporal()).hasSize(1);
    }
}
