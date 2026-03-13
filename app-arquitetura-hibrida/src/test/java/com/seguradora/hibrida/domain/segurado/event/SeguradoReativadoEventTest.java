package com.seguradora.hibrida.domain.segurado.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link SeguradoReativadoEvent}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoReativadoEvent - Testes Unitários")
class SeguradoReativadoEventTest {

    @Test
    @DisplayName("Deve criar evento com motivo")
    void shouldCreateEventWithMotivo() {
        // Given
        String aggregateId = "SEG-001";
        String motivo = "Cliente solicitou reativação";

        // When
        SeguradoReativadoEvent event = new SeguradoReativadoEvent(aggregateId, motivo);

        // Then
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getMotivo()).isEqualTo(motivo);
        assertThat(event.getEventType()).isEqualTo("SeguradoReativado");
    }

    @Test
    @DisplayName("Deve criar evento com construtor vazio")
    void shouldCreateEventWithNoArgsConstructor() {
        // When
        SeguradoReativadoEvent event = new SeguradoReativadoEvent();

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getMotivo()).isNull();
    }

    @Test
    @DisplayName("Deve ter eventType correto")
    void shouldHaveCorrectEventType() {
        // Given
        SeguradoReativadoEvent event = new SeguradoReativadoEvent("SEG-001", "Motivo teste");

        // Then
        assertThat(event.getEventType()).isEqualTo("SeguradoReativado");
    }
}
