package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ConsultaDetranConcluidaEvent Tests")
class ConsultaDetranConcluidaEventTest {

    private Map<String, Object> dadosDetran() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("placa", "ABC1234");
        dados.put("proprietario", "João Silva");
        return dados;
    }

    private ConsultaDetranConcluidaEvent evento() {
        return new ConsultaDetranConcluidaEvent(
                "agg-001", "SIN-001", dadosDetran(), "2024-01-15T10:00:00Z");
    }

    @Test
    @DisplayName("deve criar evento com todos os campos")
    void shouldCreateWithAllFields() {
        ConsultaDetranConcluidaEvent e = evento();
        assertThat(e.getAggregateId()).isEqualTo("agg-001");
        assertThat(e.getSinistroId()).isEqualTo("SIN-001");
        assertThat(e.getDadosDetran()).containsKey("placa");
        assertThat(e.getTimestampConsulta()).isEqualTo("2024-01-15T10:00:00Z");
    }

    @Test
    @DisplayName("getEventType deve retornar ConsultaDetranConcluidaEvent")
    void getEventTypeShouldReturnCorrectType() {
        assertThat(evento().getEventType()).isEqualTo("ConsultaDetranConcluidaEvent");
    }

    @Test
    @DisplayName("deve lançar exceção para dadosDetran nulo")
    void shouldThrowForNullDados() {
        assertThatThrownBy(() -> new ConsultaDetranConcluidaEvent(
                "agg-001", "SIN-001", null, "2024-01-15T10:00:00Z"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para dadosDetran vazio")
    void shouldThrowForEmptyDados() {
        assertThatThrownBy(() -> new ConsultaDetranConcluidaEvent(
                "agg-001", "SIN-001", new HashMap<>(), "2024-01-15T10:00:00Z"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para timestampConsulta vazio")
    void shouldThrowForBlankTimestamp() {
        assertThatThrownBy(() -> new ConsultaDetranConcluidaEvent(
                "agg-001", "SIN-001", dadosDetran(), ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toString deve conter sinistroId e contagem de dados")
    void toStringShouldContainSinistroAndDataCount() {
        assertThat(evento().toString()).contains("SIN-001");
    }
}
