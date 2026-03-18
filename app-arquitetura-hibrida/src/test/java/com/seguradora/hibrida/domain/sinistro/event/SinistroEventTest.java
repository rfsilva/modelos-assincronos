package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroEvent Tests")
class SinistroEventTest {

    @Test
    @DisplayName("sinistroCriado deve criar evento com status ABERTO")
    void sinistroCriadoShouldCreateWithStatusAberto() {
        SinistroEvent event = SinistroEvent.sinistroCriado("agg-001", "SIN-001", "Colisão", 5000.0);
        assertThat(event.getAggregateId()).isEqualTo("agg-001");
        assertThat(event.getNumeroSinistro()).isEqualTo("SIN-001");
        assertThat(event.getStatus()).isEqualTo("ABERTO");
        assertThat(event.getDescricao()).isEqualTo("Colisão");
        assertThat(event.getValorEstimado()).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("sinistroAtualizado deve criar evento com status fornecido")
    void sinistroAtualizadoShouldCreateWithProvidedStatus() {
        SinistroEvent event = SinistroEvent.sinistroAtualizado("agg-001", "SIN-001", "EM_ANALISE", "Desc", 3000.0);
        assertThat(event.getStatus()).isEqualTo("EM_ANALISE");
        assertThat(event.getNumeroSinistro()).isEqualTo("SIN-001");
    }

    @Test
    @DisplayName("sinistroFinalizado deve criar evento com status FINALIZADO")
    void sinistroFinalizadoShouldCreateWithStatusFinalizado() {
        SinistroEvent event = SinistroEvent.sinistroFinalizado("agg-001", "SIN-001", 10000.0);
        assertThat(event.getStatus()).isEqualTo("FINALIZADO");
        assertThat(event.getValorEstimado()).isEqualTo(10000.0);
    }

    @Test
    @DisplayName("sinistroCancelado deve criar SinistroCancelado com motivo")
    void sinistroCanceladoShouldCreateWithMotivo() {
        SinistroEvent.SinistroCancelado cancelado = SinistroEvent.sinistroCancelado(
                "agg-001", "SIN-001", "Fraude detectada");
        assertThat(cancelado.getMotivoCancelamento()).isEqualTo("Fraude detectada");
        assertThat(cancelado.getStatus()).isEqualTo("CANCELADO");
        assertThat(cancelado.getNumeroSinistro()).isEqualTo("SIN-001");
    }

    @Test
    @DisplayName("toString deve conter dados do evento")
    void toStringShouldContainEventData() {
        SinistroEvent event = SinistroEvent.sinistroCriado("agg-001", "SIN-001", "Colisão", 5000.0);
        assertThat(event.toString()).contains("SIN-001");
    }

    @Test
    @DisplayName("SinistroCancelado toString deve conter motivo")
    void sinistroCanceladoToStringShouldContainMotivo() {
        SinistroEvent.SinistroCancelado cancelado = SinistroEvent.sinistroCancelado(
                "agg-001", "SIN-001", "Fraude");
        assertThat(cancelado.toString()).contains("Fraude");
    }
}
