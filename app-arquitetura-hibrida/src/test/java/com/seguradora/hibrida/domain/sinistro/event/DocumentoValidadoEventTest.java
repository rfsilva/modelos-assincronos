package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DocumentoValidadoEvent Tests")
class DocumentoValidadoEventTest {

    private DocumentoValidadoEvent evento() {
        return new DocumentoValidadoEvent(
                "agg-001", "SIN-001", "DOC-001", "VALID-01", "2024-01-15T10:00:00Z");
    }

    @Test
    @DisplayName("deve criar evento com todos os campos")
    void shouldCreateWithAllFields() {
        DocumentoValidadoEvent e = evento();
        assertThat(e.getAggregateId()).isEqualTo("agg-001");
        assertThat(e.getSinistroId()).isEqualTo("SIN-001");
        assertThat(e.getDocumentoId()).isEqualTo("DOC-001");
        assertThat(e.getValidadorId()).isEqualTo("VALID-01");
        assertThat(e.getTimestampValidacao()).isEqualTo("2024-01-15T10:00:00Z");
    }

    @Test
    @DisplayName("getEventType deve retornar DocumentoValidadoEvent")
    void getEventTypeShouldReturnCorrectType() {
        assertThat(evento().getEventType()).isEqualTo("DocumentoValidadoEvent");
    }

    @Test
    @DisplayName("deve lançar exceção para validadorId nulo")
    void shouldThrowForNullValidadorId() {
        assertThatThrownBy(() -> new DocumentoValidadoEvent(
                "agg-001", "SIN-001", "DOC-001", null, "2024-01-15T10:00:00Z"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para timestampValidacao vazio")
    void shouldThrowForBlankTimestamp() {
        assertThatThrownBy(() -> new DocumentoValidadoEvent(
                "agg-001", "SIN-001", "DOC-001", "VALID-01", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toString deve conter documentoId e validadorId")
    void toStringShouldContainDocumentoAndValidador() {
        assertThat(evento().toString()).contains("DOC-001").contains("VALID-01");
    }
}
