package com.seguradora.hibrida.projection;

import com.seguradora.hibrida.eventbus.example.TestEvent;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para {@link AbstractProjectionHandler}.
 */
@DisplayName("AbstractProjectionHandler Tests")
class AbstractProjectionHandlerTest {

    // =========================================================================
    // Implementação concreta para testes
    // =========================================================================

    private static class TestHandler extends AbstractProjectionHandler<TestEvent> {
        boolean doHandleCalled = false;
        boolean shouldFail = false;

        @Override
        protected void doHandle(TestEvent event) throws Exception {
            if (shouldFail) {
                throw new RuntimeException("Falha simulada");
            }
            doHandleCalled = true;
        }
    }

    // =========================================================================
    // Construção
    // =========================================================================

    @Nested
    @DisplayName("Construção")
    class Construcao {

        @Test
        @DisplayName("Deve detectar tipo de evento automaticamente via reflection")
        void shouldDetectEventTypeAutomatically() {
            TestHandler handler = new TestHandler();
            assertThat(handler.getEventType()).isEqualTo(TestEvent.class);
        }

        @Test
        @DisplayName("Deve derivar projectionName do nome da classe")
        void shouldDeriveProjectionNameFromClassName() {
            TestHandler handler = new TestHandler();
            // "TestHandler" → replace("Handler", "") → "Test"
            assertThat(handler.getProjectionName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("Deve implementar ProjectionHandler")
        void shouldImplementProjectionHandler() {
            TestHandler handler = new TestHandler();
            assertThat(handler).isInstanceOf(ProjectionHandler.class);
        }
    }

    // =========================================================================
    // handle() — anotação @Transactional
    // =========================================================================

    @Test
    @DisplayName("handle() deve estar anotado com @Transactional")
    void handleShouldBeAnnotatedWithTransactional() throws NoSuchMethodException {
        Method m = AbstractProjectionHandler.class.getMethod("handle", DomainEvent.class);
        assertThat(m.isAnnotationPresent(Transactional.class)).isTrue();
    }

    // =========================================================================
    // handle() — comportamento
    // =========================================================================

    @Nested
    @DisplayName("handle() — comportamento")
    class HandleComportamento {

        @Test
        @DisplayName("Deve chamar doHandle para evento válido")
        void shouldCallDoHandleForValidEvent() {
            TestHandler handler = new TestHandler();
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);

            handler.handle(event);

            assertThat(handler.doHandleCalled).isTrue();
        }

        @Test
        @DisplayName("Deve lançar ProjectionException quando doHandle falha")
        void shouldThrowProjectionExceptionWhenDoHandleFails() {
            TestHandler handler = new TestHandler();
            handler.shouldFail = true;
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);

            assertThatThrownBy(() -> handler.handle(event))
                    .isInstanceOf(ProjectionException.class);
        }
    }

    // =========================================================================
    // Métodos utilitários
    // =========================================================================

    @Nested
    @DisplayName("Métodos utilitários")
    class MetodosUtilitarios {

        @Test
        @DisplayName("formatEventForLog() deve retornar String não nula")
        void formatEventForLogShouldReturnNonNull() {
            TestHandler handler = new TestHandler();
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);

            String formatted = handler.formatEventForLog(event);
            assertThat(formatted).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("isEventNewer() deve retornar true quando lastProcessedTimestamp é null")
        void isEventNewerShouldReturnTrueWhenLastTimestampIsNull() {
            TestHandler handler = new TestHandler();
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);

            assertThat(handler.isEventNewer(event, null)).isTrue();
        }

        @Test
        @DisplayName("isEventNewer() deve retornar true quando não há timestamp no metadata")
        void isEventNewerShouldReturnTrueWhenNoTimestampInMetadata() {
            TestHandler handler = new TestHandler();
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
            Instant past = Instant.now().minusSeconds(3600);

            // TestEvent não tem timestamp no metadata → assume mais recente
            assertThat(handler.isEventNewer(event, past)).isTrue();
        }
    }
}
