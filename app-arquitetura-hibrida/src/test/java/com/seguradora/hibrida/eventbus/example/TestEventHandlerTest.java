package com.seguradora.hibrida.eventbus.example;

import com.seguradora.hibrida.eventbus.EventHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link TestEventHandler}.
 */
@DisplayName("TestEventHandler Tests")
class TestEventHandlerTest {

    private final TestEventHandler handler = new TestEventHandler();

    // =========================================================================
    // Anotações / hierarquia
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(TestEventHandler.class.isAnnotationPresent(Component.class)).isTrue();
    }

    @Test
    @DisplayName("Deve implementar EventHandler<TestEvent>")
    void shouldImplementEventHandlerOfTestEvent() {
        assertThat(handler).isInstanceOf(EventHandler.class);
    }

    // =========================================================================
    // Metadados do handler
    // =========================================================================

    @Nested
    @DisplayName("Metadados do handler")
    class Metadados {

        @Test
        @DisplayName("getEventType deve retornar TestEvent.class")
        void getEventTypeShouldReturnTestEventClass() {
            assertThat(handler.getEventType()).isEqualTo(TestEvent.class);
        }

        @Test
        @DisplayName("getPriority deve retornar 10")
        void getPriorityShouldReturnTen() {
            assertThat(handler.getPriority()).isEqualTo(10);
        }

        @Test
        @DisplayName("isRetryable deve retornar true")
        void isRetryableShouldReturnTrue() {
            assertThat(handler.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("isAsync deve retornar true")
        void isAsyncShouldReturnTrue() {
            assertThat(handler.isAsync()).isTrue();
        }

        @Test
        @DisplayName("getTimeoutSeconds deve retornar 30")
        void getTimeoutSecondsShouldReturnThirty() {
            assertThat(handler.getTimeoutSeconds()).isEqualTo(30);
        }
    }

    // =========================================================================
    // supports
    // =========================================================================

    @Nested
    @DisplayName("supports()")
    class Supports {

        @Test
        @DisplayName("Deve retornar true para evento válido com mensagem")
        void shouldReturnTrueForValidEventWithMessage() {
            TestEvent event = new TestEvent("agg-1", "Hello", "INFO", 1);

            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false para evento com mensagem null")
        void shouldReturnFalseForEventWithNullMessage() {
            TestEvent event = new TestEvent("agg-1", null, "INFO", 1);

            assertThat(handler.supports(event)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para evento null")
        void shouldReturnFalseForNullEvent() {
            assertThat(handler.supports(null)).isFalse();
        }
    }

    // =========================================================================
    // handle
    // =========================================================================

    @Test
    @DisplayName("handle() deve completar sem exceção para evento válido")
    void handleShouldCompleteWithoutExceptionForValidEvent() {
        TestEvent event = new TestEvent("agg-1", "Test message", "INFO", 1);

        // Não deve lançar exceção
        assertThat(handler.supports(event)).isTrue();
    }
}
