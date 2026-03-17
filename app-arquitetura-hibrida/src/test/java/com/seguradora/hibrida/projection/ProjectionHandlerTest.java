package com.seguradora.hibrida.projection;

import com.seguradora.hibrida.eventbus.example.TestEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de contrato para a interface {@link ProjectionHandler}.
 */
@DisplayName("ProjectionHandler Interface Tests")
class ProjectionHandlerTest {

    private final ProjectionHandler<TestEvent> handler = new ProjectionHandler<TestEvent>() {
        @Override
        public void handle(TestEvent event) {}

        @Override
        public Class<TestEvent> getEventType() {
            return TestEvent.class;
        }

        @Override
        public String getProjectionName() {
            return "TestProjection";
        }
    };

    // =========================================================================
    // Estrutura da interface
    // =========================================================================

    @Test
    @DisplayName("ProjectionHandler deve ser interface")
    void projectionHandlerShouldBeInterface() {
        assertThat(ProjectionHandler.class.isInterface()).isTrue();
    }

    // =========================================================================
    // Valores default
    // =========================================================================

    @Nested
    @DisplayName("Valores default")
    class ValoresDefault {

        @Test
        @DisplayName("supports() deve retornar true por padrão")
        void supportsShouldReturnTrueByDefault() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("getOrder() deve retornar 100 por padrão")
        void getOrderShouldReturnHundredByDefault() {
            assertThat(handler.getOrder()).isEqualTo(100);
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
        @DisplayName("isRetryable() deve retornar true por padrão")
        void isRetryableShouldReturnTrueByDefault() {
            assertThat(handler.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("getMaxRetries() deve retornar 3 por padrão")
        void getMaxRetriesShouldReturnThreeByDefault() {
            assertThat(handler.getMaxRetries()).isEqualTo(3);
        }
    }
}
