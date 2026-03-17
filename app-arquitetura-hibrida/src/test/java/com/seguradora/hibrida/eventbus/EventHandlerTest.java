package com.seguradora.hibrida.eventbus;

import com.seguradora.hibrida.eventbus.example.TestEvent;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de contrato para a interface {@link EventHandler}.
 */
@DisplayName("EventHandler Interface Tests")
class EventHandlerTest {

    // =========================================================================
    // Implementação de referência
    // =========================================================================

    private final EventHandler<TestEvent> handler = new EventHandler<TestEvent>() {
        @Override
        public void handle(TestEvent event) {}

        @Override
        public Class<TestEvent> getEventType() {
            return TestEvent.class;
        }
    };

    // =========================================================================
    // Métodos default
    // =========================================================================

    @Nested
    @DisplayName("Valores default")
    class ValoresDefault {

        @Test
        @DisplayName("isRetryable() deve retornar true por padrão")
        void isRetryableShouldReturnTrueByDefault() {
            assertThat(handler.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("getPriority() deve retornar 0 por padrão")
        void getPriorityShouldReturnZeroByDefault() {
            assertThat(handler.getPriority()).isZero();
        }

        @Test
        @DisplayName("isAsync() deve retornar true por padrão")
        void isAsyncShouldReturnTrueByDefault() {
            assertThat(handler.isAsync()).isTrue();
        }

        @Test
        @DisplayName("getTimeoutSeconds() deve retornar 30 por padrão")
        void getTimeoutSecondsShouldReturnThirtyByDefault() {
            assertThat(handler.getTimeoutSeconds()).isEqualTo(30);
        }

        @Test
        @DisplayName("supports() deve retornar true por padrão")
        void supportsShouldReturnTrueByDefault() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
            assertThat(handler.supports(event)).isTrue();
        }
    }

    // =========================================================================
    // Hierarquia de tipos
    // =========================================================================

    @Test
    @DisplayName("EventHandler deve ser interface")
    void eventHandlerShouldBeInterface() {
        assertThat(EventHandler.class.isInterface()).isTrue();
    }
}
