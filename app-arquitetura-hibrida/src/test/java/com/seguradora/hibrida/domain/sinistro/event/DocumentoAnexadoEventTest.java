package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DocumentoAnexadoEvent Tests")
class DocumentoAnexadoEventTest {

    private DocumentoAnexadoEvent evento() {
        return new DocumentoAnexadoEvent(
                "agg-001", "SIN-001", "DOC-001", "BOLETIM_OCORRENCIA", "OP-001", "Obs opcionais");
    }

    @Test
    @DisplayName("deve criar evento com todos os campos")
    void shouldCreateWithAllFields() {
        DocumentoAnexadoEvent e = evento();
        assertThat(e.getAggregateId()).isEqualTo("agg-001");
        assertThat(e.getSinistroId()).isEqualTo("SIN-001");
        assertThat(e.getDocumentoId()).isEqualTo("DOC-001");
        assertThat(e.getTipoDocumento()).isEqualTo("BOLETIM_OCORRENCIA");
        assertThat(e.getOperadorId()).isEqualTo("OP-001");
        assertThat(e.getObservacoes()).isEqualTo("Obs opcionais");
    }

    @Test
    @DisplayName("deve aceitar observacoes nulas")
    void shouldAcceptNullObservacoes() {
        DocumentoAnexadoEvent e = new DocumentoAnexadoEvent(
                "agg-001", "SIN-001", "DOC-001", "FOTO", "OP-001", null);
        assertThat(e.getObservacoes()).isNull();
    }

    @Test
    @DisplayName("getEventType deve retornar DocumentoAnexadoEvent")
    void getEventTypeShouldReturnCorrectType() {
        assertThat(evento().getEventType()).isEqualTo("DocumentoAnexadoEvent");
    }

    @Test
    @DisplayName("deve lançar exceção para documentoId nulo")
    void shouldThrowForNullDocumentoId() {
        assertThatThrownBy(() -> new DocumentoAnexadoEvent(
                "agg-001", "SIN-001", null, "FOTO", "OP-001", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para tipoDocumento vazio")
    void shouldThrowForBlankTipo() {
        assertThatThrownBy(() -> new DocumentoAnexadoEvent(
                "agg-001", "SIN-001", "DOC-001", "", "OP-001", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toString deve conter sinistroId e documentoId")
    void toStringShouldContainSinistroAndDocumento() {
        assertThat(evento().toString()).contains("SIN-001").contains("DOC-001");
    }
}
