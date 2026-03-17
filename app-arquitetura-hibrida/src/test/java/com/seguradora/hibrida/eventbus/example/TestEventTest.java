package com.seguradora.hibrida.eventbus.example;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link TestEvent}.
 */
@DisplayName("TestEvent Tests")
class TestEventTest {

    // =========================================================================
    // Construção
    // =========================================================================

    @Nested
    @DisplayName("Construção")
    class Construcao {

        @Test
        @DisplayName("Deve criar evento com campos corretos")
        void shouldCreateEventWithCorrectFields() {
            TestEvent event = new TestEvent("agg-1", "Hello", "INFO", 1);

            assertThat(event.getMessage()).isEqualTo("Hello");
            assertThat(event.getCategory()).isEqualTo("INFO");
            assertThat(event.getPriority()).isEqualTo(1);
        }

        @Test
        @DisplayName("Deve herdar de DomainEvent")
        void shouldExtendDomainEvent() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 0);

            assertThat(event).isInstanceOf(DomainEvent.class);
        }

        @Test
        @DisplayName("Deve propagar aggregateId para DomainEvent")
        void shouldPropagateAggregateId() {
            TestEvent event = new TestEvent("agg-42", "msg", "cat", 0);

            assertThat(event.getAggregateId()).isEqualTo("agg-42");
        }

        @Test
        @DisplayName("Deve definir aggregateType como 'TestAggregate'")
        void shouldSetAggregateTypeAsTestAggregate() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 0);

            assertThat(event.getAggregateType()).isEqualTo("TestAggregate");
        }

        @Test
        @DisplayName("Deve definir version como 1")
        void shouldSetVersionAsOne() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 0);

            assertThat(event.getVersion()).isEqualTo(1L);
        }
    }

    // =========================================================================
    // getEventType
    // =========================================================================

    @Test
    @DisplayName("getEventType deve retornar 'TestEvent'")
    void getEventTypeShouldReturnTestEvent() {
        TestEvent event = new TestEvent("agg-1", "msg", "cat", 0);

        assertThat(event.getEventType()).isEqualTo("TestEvent");
    }

    // =========================================================================
    // Diferentes prioridades
    // =========================================================================

    @Nested
    @DisplayName("Prioridades")
    class Prioridades {

        @Test
        @DisplayName("Deve aceitar prioridade alta (> 0)")
        void shouldAcceptHighPriority() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 10);
            assertThat(event.getPriority()).isEqualTo(10);
        }

        @Test
        @DisplayName("Deve aceitar prioridade zero")
        void shouldAcceptZeroPriority() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 0);
            assertThat(event.getPriority()).isZero();
        }

        @Test
        @DisplayName("Deve aceitar prioridade negativa")
        void shouldAcceptNegativePriority() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", -1);
            assertThat(event.getPriority()).isEqualTo(-1);
        }
    }
}
