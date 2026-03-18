package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ConsultaDetranFalhadaEvent Tests")
class ConsultaDetranFalhadaEventTest {

    private ConsultaDetranFalhadaEvent evento() {
        return new ConsultaDetranFalhadaEvent(
                "agg-001", "SIN-001", "Timeout ao consultar Detran", 1, "2024-01-15T11:00:00Z");
    }

    @Test
    @DisplayName("deve criar evento com todos os campos incluindo próxima tentativa")
    void shouldCreateWithAllFields() {
        ConsultaDetranFalhadaEvent e = evento();
        assertThat(e.getAggregateId()).isEqualTo("agg-001");
        assertThat(e.getSinistroId()).isEqualTo("SIN-001");
        assertThat(e.getErro()).isEqualTo("Timeout ao consultar Detran");
        assertThat(e.getTentativa()).isEqualTo(1);
        assertThat(e.getProximaTentativa()).isEqualTo("2024-01-15T11:00:00Z");
    }

    @Test
    @DisplayName("deve aceitar proximaTentativa nula")
    void shouldAcceptNullProximaTentativa() {
        ConsultaDetranFalhadaEvent e = new ConsultaDetranFalhadaEvent(
                "agg-001", "SIN-001", "Erro", 3, null);
        assertThat(e.getProximaTentativa()).isNull();
    }

    @Test
    @DisplayName("getEventType deve retornar ConsultaDetranFalhadaEvent")
    void getEventTypeShouldReturnCorrectType() {
        assertThat(evento().getEventType()).isEqualTo("ConsultaDetranFalhadaEvent");
    }

    @Test
    @DisplayName("deve lançar exceção para erro nulo")
    void shouldThrowForNullErro() {
        assertThatThrownBy(() -> new ConsultaDetranFalhadaEvent(
                "agg-001", "SIN-001", null, 1, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para tentativa menor que 1")
    void shouldThrowForTentativaLessThan1() {
        assertThatThrownBy(() -> new ConsultaDetranFalhadaEvent(
                "agg-001", "SIN-001", "Erro", 0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toString deve conter erro e tentativa")
    void toStringShouldContainErroAndTentativa() {
        assertThat(evento().toString()).contains("SIN-001");
    }
}
