package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SinistroAprovadoEvent Tests")
class SinistroAprovadoEventTest {

    private SinistroAprovadoEvent evento() {
        return new SinistroAprovadoEvent(
                "agg-001", "SIN-001", "10000.00",
                "Danos comprovados", "ANALISTA-01", List.of("DOC-001"));
    }

    @Test
    @DisplayName("deve criar evento com todos os campos")
    void shouldCreateWithAllFields() {
        SinistroAprovadoEvent e = evento();
        assertThat(e.getAggregateId()).isEqualTo("agg-001");
        assertThat(e.getSinistroId()).isEqualTo("SIN-001");
        assertThat(e.getValorIndenizacao()).isEqualTo("10000.00");
        assertThat(e.getJustificativa()).isEqualTo("Danos comprovados");
        assertThat(e.getAnalistaId()).isEqualTo("ANALISTA-01");
        assertThat(e.getDocumentosComprobatorios()).containsExactly("DOC-001");
    }

    @Test
    @DisplayName("getEventType deve retornar SinistroAprovadoEvent")
    void getEventTypeShouldReturnCorrectType() {
        assertThat(evento().getEventType()).isEqualTo("SinistroAprovadoEvent");
    }

    @Test
    @DisplayName("deve aceitar lista de documentos vazia")
    void shouldAcceptEmptyDocumentos() {
        SinistroAprovadoEvent e = new SinistroAprovadoEvent(
                "agg-001", "SIN-001", "10000.00", "Justificativa", "ANALISTA-01", List.of());
        assertThat(e.getDocumentosComprobatorios()).isEmpty();
    }

    @Test
    @DisplayName("deve lançar exceção para sinistroId nulo")
    void shouldThrowForNullSinistroId() {
        assertThatThrownBy(() -> new SinistroAprovadoEvent(
                "agg-001", null, "10000.00", "Just", "ANALISTA-01", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para valorIndenizacao vazio")
    void shouldThrowForBlankValor() {
        assertThatThrownBy(() -> new SinistroAprovadoEvent(
                "agg-001", "SIN-001", "", "Just", "ANALISTA-01", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para justificativa nula")
    void shouldThrowForNullJustificativa() {
        assertThatThrownBy(() -> new SinistroAprovadoEvent(
                "agg-001", "SIN-001", "10000.00", null, "ANALISTA-01", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para documentos nulo")
    void shouldThrowForNullDocumentos() {
        assertThatThrownBy(() -> new SinistroAprovadoEvent(
                "agg-001", "SIN-001", "10000.00", "Just", "ANALISTA-01", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toString deve conter sinistroId e valorIndenizacao")
    void toStringShouldContainSinistroAndValor() {
        assertThat(evento().toString()).contains("SIN-001").contains("10000.00");
    }
}
