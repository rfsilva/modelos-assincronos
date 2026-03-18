package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SinistroEmAnaliseEvent Tests")
class SinistroEmAnaliseEventTest {

    private SinistroEmAnaliseEvent evento() {
        return new SinistroEmAnaliseEvent(
                "agg-001", "SIN-001", "ANALISTA-01", "2024-12-31", "ALTA");
    }

    @Test
    @DisplayName("deve criar evento com todos os campos")
    void shouldCreateWithAllFields() {
        SinistroEmAnaliseEvent e = evento();
        assertThat(e.getAggregateId()).isEqualTo("agg-001");
        assertThat(e.getSinistroId()).isEqualTo("SIN-001");
        assertThat(e.getAnalistaId()).isEqualTo("ANALISTA-01");
        assertThat(e.getPrazoAnalise()).isEqualTo("2024-12-31");
        assertThat(e.getPrioridadeAnalise()).isEqualTo("ALTA");
    }

    @Test
    @DisplayName("getEventType deve retornar SinistroEmAnaliseEvent")
    void getEventTypeShouldReturnCorrectType() {
        assertThat(evento().getEventType()).isEqualTo("SinistroEmAnaliseEvent");
    }

    @Test
    @DisplayName("deve lançar exceção para sinistroId nulo")
    void shouldThrowForNullSinistroId() {
        assertThatThrownBy(() -> new SinistroEmAnaliseEvent(
                "agg-001", null, "ANALISTA-01", "2024-12-31", "ALTA"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para analistaId vazio")
    void shouldThrowForBlankAnalistaId() {
        assertThatThrownBy(() -> new SinistroEmAnaliseEvent(
                "agg-001", "SIN-001", "", "2024-12-31", "ALTA"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para prazoAnalise nulo")
    void shouldThrowForNullPrazo() {
        assertThatThrownBy(() -> new SinistroEmAnaliseEvent(
                "agg-001", "SIN-001", "ANALISTA-01", null, "ALTA"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para prioridade vazia")
    void shouldThrowForBlankPrioridade() {
        assertThatThrownBy(() -> new SinistroEmAnaliseEvent(
                "agg-001", "SIN-001", "ANALISTA-01", "2024-12-31", "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toString deve conter sinistroId e analistaId")
    void toStringShouldContainSinistroAndAnalista() {
        assertThat(evento().toString()).contains("SIN-001").contains("ANALISTA-01");
    }
}
