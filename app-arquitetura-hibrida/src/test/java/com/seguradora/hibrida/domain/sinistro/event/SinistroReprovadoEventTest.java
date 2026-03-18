package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SinistroReprovadoEvent Tests")
class SinistroReprovadoEventTest {

    private SinistroReprovadoEvent evento() {
        return new SinistroReprovadoEvent(
                "agg-001", "SIN-001", "COBERTURA_NAO_APLICAVEL",
                "Veículo não coberto pela apólice", "ANALISTA-01", "Art. 5, Cláusula 3");
    }

    @Test
    @DisplayName("deve criar evento com todos os campos")
    void shouldCreateWithAllFields() {
        SinistroReprovadoEvent e = evento();
        assertThat(e.getAggregateId()).isEqualTo("agg-001");
        assertThat(e.getSinistroId()).isEqualTo("SIN-001");
        assertThat(e.getMotivo()).isEqualTo("COBERTURA_NAO_APLICAVEL");
        assertThat(e.getJustificativa()).isEqualTo("Veículo não coberto pela apólice");
        assertThat(e.getAnalistaId()).isEqualTo("ANALISTA-01");
        assertThat(e.getFundamentoLegal()).isEqualTo("Art. 5, Cláusula 3");
    }

    @Test
    @DisplayName("getEventType deve retornar SinistroReprovadoEvent")
    void getEventTypeShouldReturnCorrectType() {
        assertThat(evento().getEventType()).isEqualTo("SinistroReprovadoEvent");
    }

    @Test
    @DisplayName("deve lançar exceção para motivo nulo")
    void shouldThrowForNullMotivo() {
        assertThatThrownBy(() -> new SinistroReprovadoEvent(
                "agg-001", "SIN-001", null, "Just", "ANALISTA-01", "Art. 1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para justificativa vazia")
    void shouldThrowForBlankJustificativa() {
        assertThatThrownBy(() -> new SinistroReprovadoEvent(
                "agg-001", "SIN-001", "MOTIVO", "", "ANALISTA-01", "Art. 1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para fundamentoLegal nulo")
    void shouldThrowForNullFundamento() {
        assertThatThrownBy(() -> new SinistroReprovadoEvent(
                "agg-001", "SIN-001", "MOTIVO", "Just", "ANALISTA-01", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toString deve conter sinistroId e motivo")
    void toStringShouldContainSinistroAndMotivo() {
        assertThat(evento().toString())
                .contains("SIN-001")
                .contains("COBERTURA_NAO_APLICAVEL");
    }
}
