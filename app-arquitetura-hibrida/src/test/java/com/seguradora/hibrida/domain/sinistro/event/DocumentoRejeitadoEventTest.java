package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DocumentoRejeitadoEvent Tests")
class DocumentoRejeitadoEventTest {

    private DocumentoRejeitadoEvent evento() {
        return new DocumentoRejeitadoEvent(
                "agg-001", "SIN-001", "DOC-001", "Documento ilegível", "VALID-01");
    }

    @Test
    @DisplayName("deve criar evento com todos os campos")
    void shouldCreateWithAllFields() {
        DocumentoRejeitadoEvent e = evento();
        assertThat(e.getAggregateId()).isEqualTo("agg-001");
        assertThat(e.getSinistroId()).isEqualTo("SIN-001");
        assertThat(e.getDocumentoId()).isEqualTo("DOC-001");
        assertThat(e.getMotivo()).isEqualTo("Documento ilegível");
        assertThat(e.getValidadorId()).isEqualTo("VALID-01");
    }

    @Test
    @DisplayName("getEventType deve retornar DocumentoRejeitadoEvent")
    void getEventTypeShouldReturnCorrectType() {
        assertThat(evento().getEventType()).isEqualTo("DocumentoRejeitadoEvent");
    }

    @Test
    @DisplayName("deve lançar exceção para motivo nulo")
    void shouldThrowForNullMotivo() {
        assertThatThrownBy(() -> new DocumentoRejeitadoEvent(
                "agg-001", "SIN-001", "DOC-001", null, "VALID-01"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para validadorId vazio")
    void shouldThrowForBlankValidadorId() {
        assertThatThrownBy(() -> new DocumentoRejeitadoEvent(
                "agg-001", "SIN-001", "DOC-001", "Motivo", "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toString deve conter documentoId e motivo")
    void toStringShouldContainDocumentoAndMotivo() {
        assertThat(evento().toString()).contains("DOC-001").contains("ilegível");
    }
}
