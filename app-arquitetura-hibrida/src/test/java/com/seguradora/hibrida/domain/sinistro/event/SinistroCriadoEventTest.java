package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SinistroCriadoEvent Tests")
class SinistroCriadoEventTest {

    private SinistroCriadoEvent evento() {
        return new SinistroCriadoEvent(
                "agg-001", "2024-000001", "SEG-001", "VEI-001",
                "APO-001", "COLISAO", "Colisão na via pública", "OP-001");
    }

    @Test
    @DisplayName("deve criar evento com todos os campos")
    void shouldCreateWithAllFields() {
        SinistroCriadoEvent e = evento();
        assertThat(e.getAggregateId()).isEqualTo("agg-001");
        assertThat(e.getProtocolo()).isEqualTo("2024-000001");
        assertThat(e.getSeguradoId()).isEqualTo("SEG-001");
        assertThat(e.getVeiculoId()).isEqualTo("VEI-001");
        assertThat(e.getApoliceId()).isEqualTo("APO-001");
        assertThat(e.getTipoSinistro()).isEqualTo("COLISAO");
        assertThat(e.getOcorrencia()).isEqualTo("Colisão na via pública");
        assertThat(e.getOperadorId()).isEqualTo("OP-001");
    }

    @Test
    @DisplayName("getEventType deve retornar SinistroCriadoEvent")
    void getEventTypeShouldReturnCorrectType() {
        assertThat(evento().getEventType()).isEqualTo("SinistroCriadoEvent");
    }

    @Test
    @DisplayName("deve lançar exceção para protocolo nulo")
    void shouldThrowForNullProtocolo() {
        assertThatThrownBy(() -> new SinistroCriadoEvent(
                "agg-001", null, "SEG-001", "VEI-001", "APO-001", "COLISAO", "Ocorrência", "OP-001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para seguradoId vazio")
    void shouldThrowForBlankSeguradoId() {
        assertThatThrownBy(() -> new SinistroCriadoEvent(
                "agg-001", "2024-000001", "  ", "VEI-001", "APO-001", "COLISAO", "Ocorrência", "OP-001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para veiculoId nulo")
    void shouldThrowForNullVeiculoId() {
        assertThatThrownBy(() -> new SinistroCriadoEvent(
                "agg-001", "2024-000001", "SEG-001", null, "APO-001", "COLISAO", "Ocorrência", "OP-001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para operadorId vazio")
    void shouldThrowForBlankOperadorId() {
        assertThatThrownBy(() -> new SinistroCriadoEvent(
                "agg-001", "2024-000001", "SEG-001", "VEI-001", "APO-001", "COLISAO", "Ocorrência", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve fazer trim nos campos")
    void shouldTrimFields() {
        SinistroCriadoEvent e = new SinistroCriadoEvent(
                "agg-001", "  2024-000001  ", "  SEG-001  ", "VEI-001",
                "APO-001", "COLISAO", "Ocorrência válida", "OP-001");
        assertThat(e.getProtocolo()).isEqualTo("2024-000001");
        assertThat(e.getSeguradoId()).isEqualTo("SEG-001");
    }

    @Test
    @DisplayName("toString deve conter agregateId e protocolo")
    void toStringShouldContainAggregateAndProtocolo() {
        assertThat(evento().toString()).contains("agg-001").contains("2024-000001");
    }
}
