package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SinistroValidadoEvent Tests")
class SinistroValidadoEventTest {

    private SinistroValidadoEvent evento() {
        return new SinistroValidadoEvent(
                "agg-001", "SIN-001",
                new HashMap<>(),
                List.of("DOC-001"),
                "OP-001");
    }

    @Test
    @DisplayName("deve criar evento com campos corretos")
    void shouldCreateWithAllFields() {
        SinistroValidadoEvent e = evento();
        assertThat(e.getAggregateId()).isEqualTo("agg-001");
        assertThat(e.getSinistroId()).isEqualTo("SIN-001");
        assertThat(e.getDocumentosAnexados()).containsExactly("DOC-001");
        assertThat(e.getOperadorId()).isEqualTo("OP-001");
        assertThat(e.getDadosComplementares()).isNotNull();
    }

    @Test
    @DisplayName("getEventType deve retornar SinistroValidadoEvent")
    void getEventTypeShouldReturnCorrectType() {
        assertThat(evento().getEventType()).isEqualTo("SinistroValidadoEvent");
    }

    @Test
    @DisplayName("deve aceitar dadosComplementares e documentosAnexados vazios")
    void shouldAcceptEmptyCollections() {
        SinistroValidadoEvent e = new SinistroValidadoEvent(
                "agg-001", "SIN-001",
                Collections.emptyMap(),
                Collections.emptyList(),
                "OP-001");
        assertThat(e.getDadosComplementares()).isEmpty();
        assertThat(e.getDocumentosAnexados()).isEmpty();
    }

    @Test
    @DisplayName("deve lançar exceção para sinistroId nulo")
    void shouldThrowForNullSinistroId() {
        assertThatThrownBy(() -> new SinistroValidadoEvent(
                "agg-001", null, new HashMap<>(), List.of(), "OP-001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para dadosComplementares nulo")
    void shouldThrowForNullDados() {
        assertThatThrownBy(() -> new SinistroValidadoEvent(
                "agg-001", "SIN-001", null, List.of(), "OP-001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para documentosAnexados nulo")
    void shouldThrowForNullDocumentos() {
        assertThatThrownBy(() -> new SinistroValidadoEvent(
                "agg-001", "SIN-001", new HashMap<>(), null, "OP-001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para operadorId vazio")
    void shouldThrowForBlankOperadorId() {
        assertThatThrownBy(() -> new SinistroValidadoEvent(
                "agg-001", "SIN-001", new HashMap<>(), List.of(), ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toString deve conter sinistroId")
    void toStringShouldContainSinistroId() {
        assertThat(evento().toString()).contains("SIN-001");
    }
}
