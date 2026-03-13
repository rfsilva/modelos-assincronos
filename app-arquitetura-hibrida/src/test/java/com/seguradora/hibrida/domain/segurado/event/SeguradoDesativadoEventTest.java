package com.seguradora.hibrida.domain.segurado.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link SeguradoDesativadoEvent}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoDesativadoEvent - Testes Unitários")
class SeguradoDesativadoEventTest {

    @Test
    @DisplayName("Deve criar evento com motivo")
    void shouldCreateEventWithMotivo() {
        // Given
        String aggregateId = "SEG-001";
        String motivo = "Cancelamento solicitado pelo cliente";

        // When
        SeguradoDesativadoEvent event = new SeguradoDesativadoEvent(aggregateId, motivo);

        // Then
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getMotivo()).isEqualTo(motivo);
        assertThat(event.getSeguradoId()).isEqualTo(aggregateId);
        assertThat(event.getEventType()).isEqualTo("SeguradoDesativado");
    }

    @Test
    @DisplayName("Deve criar evento com construtor vazio")
    void shouldCreateEventWithNoArgsConstructor() {
        // When
        SeguradoDesativadoEvent event = new SeguradoDesativadoEvent();

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getMotivo()).isNull();
    }

    @Test
    @DisplayName("Deve ter eventType correto")
    void shouldHaveCorrectEventType() {
        // Given
        SeguradoDesativadoEvent event = new SeguradoDesativadoEvent("SEG-001", "Motivo teste");

        // Then
        assertThat(event.getEventType()).isEqualTo("SeguradoDesativado");
    }

    @Test
    @DisplayName("Deve ter getSeguradoId igual ao aggregateId")
    void shouldHaveGetSeguradoIdEqualToAggregateId() {
        // Given
        String aggregateId = "SEG-001";
        SeguradoDesativadoEvent event = new SeguradoDesativadoEvent(aggregateId, "Motivo");

        // Then
        assertThat(event.getSeguradoId()).isEqualTo(event.getAggregateId());
        assertThat(event.getSeguradoId()).isEqualTo(aggregateId);
    }
}
