package com.seguradora.hibrida.domain.analytics.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link EvolucaoTemporalView}.
 */
@DisplayName("EvolucaoTemporalView Tests")
class EvolucaoTemporalViewTest {

    @Test
    @DisplayName("Builder deve criar instância com data e valor")
    void builderShouldCreateInstanceWithDataAndValor() {
        LocalDate data = LocalDate.of(2024, 6, 15);

        EvolucaoTemporalView view = EvolucaoTemporalView.builder()
                .data(data)
                .valor(1250L)
                .build();

        assertThat(view.getData()).isEqualTo(data);
        assertThat(view.getValor()).isEqualTo(1250L);
    }

    @Test
    @DisplayName("Construtor padrão deve funcionar")
    void defaultConstructorShouldWork() {
        EvolucaoTemporalView view = new EvolucaoTemporalView();
        assertThat(view.getData()).isNull();
        assertThat(view.getValor()).isNull();
    }

    @Test
    @DisplayName("AllArgsConstructor deve funcionar")
    void allArgsConstructorShouldWork() {
        LocalDate data = LocalDate.of(2024, 1, 1);
        EvolucaoTemporalView view = new EvolucaoTemporalView(data, 500L);

        assertThat(view.getData()).isEqualTo(data);
        assertThat(view.getValor()).isEqualTo(500L);
    }

    @Test
    @DisplayName("Setters gerados pelo Lombok devem funcionar")
    void lombokSettersShouldWork() {
        EvolucaoTemporalView view = new EvolucaoTemporalView();
        view.setData(LocalDate.of(2024, 3, 1));
        view.setValor(999L);

        assertThat(view.getData()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(view.getValor()).isEqualTo(999L);
    }
}
