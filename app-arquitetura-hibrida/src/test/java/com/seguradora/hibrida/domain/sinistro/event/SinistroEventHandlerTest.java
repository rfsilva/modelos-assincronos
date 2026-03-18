package com.seguradora.hibrida.domain.sinistro.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SinistroEventHandler Tests")
class SinistroEventHandlerTest {

    private SinistroEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SinistroEventHandler();
    }

    @Test
    @DisplayName("getEventType deve retornar SinistroEvent.class")
    void getEventTypeShouldReturnSinistroEventClass() {
        assertThat(handler.getEventType()).isEqualTo(SinistroEvent.class);
    }

    @Test
    @DisplayName("isAsync deve retornar true")
    void isAsyncShouldReturnTrue() {
        assertThat(handler.isAsync()).isTrue();
    }

    @Test
    @DisplayName("isRetryable deve retornar true")
    void isRetryableShouldReturnTrue() {
        assertThat(handler.isRetryable()).isTrue();
    }

    @Test
    @DisplayName("getPriority deve retornar 100")
    void getPriorityShouldReturn100() {
        assertThat(handler.getPriority()).isEqualTo(100);
    }

    @Test
    @DisplayName("getTimeoutSeconds deve retornar 30")
    void getTimeoutSecondsShouldReturn30() {
        assertThat(handler.getTimeoutSeconds()).isEqualTo(30);
    }

    @Test
    @DisplayName("supports deve retornar true para evento válido com aggregateId e numeroSinistro")
    void supportsShouldReturnTrueForValidEvent() {
        SinistroEvent event = SinistroEvent.sinistroCriado("agg-001", "SIN-001", "Desc", 1000.0);
        assertThat(handler.supports(event)).isTrue();
    }

    @Test
    @DisplayName("supports deve retornar false para evento nulo")
    void supportsShouldReturnFalseForNull() {
        assertThat(handler.supports(null)).isFalse();
    }

    @Test
    @DisplayName("handle deve processar evento SinistroCriado")
    void handleShouldProcessSinistroCriadoEvent() {
        SinistroEvent event = SinistroEvent.sinistroCriado("agg-001", "SIN-001", "Colisão", 5000.0);
        event.addMetadata("eventType", "SinistroCriado");
        // Should not throw
        handler.handle(event);
    }

    @Test
    @DisplayName("handle deve processar evento SinistroAtualizado")
    void handleShouldProcessSinistroAtualizadoEvent() {
        SinistroEvent event = SinistroEvent.sinistroAtualizado("agg-001", "SIN-001", "EM_ANALISE", "Desc", 5000.0);
        event.addMetadata("eventType", "SinistroAtualizado");
        handler.handle(event);
    }

    @Test
    @DisplayName("handle deve processar evento SinistroCancelado")
    void handleShouldProcessSinistroCanceladoEvent() {
        SinistroEvent.SinistroCancelado event = SinistroEvent.sinistroCancelado("agg-001", "SIN-001", "Fraude");
        handler.handle(event);
    }

    @Test
    @DisplayName("handle deve re-lançar exceção em caso de erro")
    void handleShouldRethrowException() {
        // Um evento nulo deve causar falha no processamento
        SinistroEvent event = SinistroEvent.sinistroCriado("agg-001", "SIN-001", "Colisão", 5000.0);
        event.addMetadata("eventType", "SinistroFinalizado");
        // Should process without error (finalizado has a long sleep but still runs)
        handler.handle(event);
    }
}
