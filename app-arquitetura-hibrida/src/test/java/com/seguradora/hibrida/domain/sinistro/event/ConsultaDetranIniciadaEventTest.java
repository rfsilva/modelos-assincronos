package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ConsultaDetranIniciadaEvent Tests")
class ConsultaDetranIniciadaEventTest {

    private ConsultaDetranIniciadaEvent evento() {
        return new ConsultaDetranIniciadaEvent(
                "agg-001", "SIN-001", "abc1234", "12345678901", 1);
    }

    @Test
    @DisplayName("deve criar evento com todos os campos e placa em maiúsculas")
    void shouldCreateWithAllFieldsAndUppercasePlaca() {
        ConsultaDetranIniciadaEvent e = evento();
        assertThat(e.getAggregateId()).isEqualTo("agg-001");
        assertThat(e.getSinistroId()).isEqualTo("SIN-001");
        assertThat(e.getPlaca()).isEqualTo("ABC1234");
        assertThat(e.getRenavam()).isEqualTo("12345678901");
        assertThat(e.getTentativa()).isEqualTo(1);
    }

    @Test
    @DisplayName("getEventType deve retornar ConsultaDetranIniciadaEvent")
    void getEventTypeShouldReturnCorrectType() {
        assertThat(evento().getEventType()).isEqualTo("ConsultaDetranIniciadaEvent");
    }

    @Test
    @DisplayName("deve lançar exceção para placa nula")
    void shouldThrowForNullPlaca() {
        assertThatThrownBy(() -> new ConsultaDetranIniciadaEvent(
                "agg-001", "SIN-001", null, "12345678901", 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para renavam vazio")
    void shouldThrowForBlankRenavam() {
        assertThatThrownBy(() -> new ConsultaDetranIniciadaEvent(
                "agg-001", "SIN-001", "ABC1234", "", 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para tentativa zero")
    void shouldThrowForZeroTentativa() {
        assertThatThrownBy(() -> new ConsultaDetranIniciadaEvent(
                "agg-001", "SIN-001", "ABC1234", "12345678901", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para tentativa nula")
    void shouldThrowForNullTentativa() {
        assertThatThrownBy(() -> new ConsultaDetranIniciadaEvent(
                "agg-001", "SIN-001", "ABC1234", "12345678901", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toString deve conter placa e sinistroId")
    void toStringShouldContainPlacaAndSinistro() {
        assertThat(evento().toString()).contains("ABC1234").contains("SIN-001");
    }
}
