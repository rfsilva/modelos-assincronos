package com.seguradora.hibrida.eventstore.replay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ReplayError}.
 */
@DisplayName("ReplayError Tests")
class ReplayErrorTest {

    // =========================================================================
    // ErrorType enum
    // =========================================================================

    @Test
    @DisplayName("ErrorType deve ter 7 valores")
    void errorTypeShouldHaveSevenValues() {
        assertThat(ReplayError.ErrorType.values()).hasSize(7);
    }

    @Test
    @DisplayName("ErrorType deve conter os valores esperados")
    void errorTypeShouldContainExpectedValues() {
        assertThat(ReplayError.ErrorType.values()).contains(
                ReplayError.ErrorType.EVENT_PROCESSING,
                ReplayError.ErrorType.HANDLER_EXECUTION,
                ReplayError.ErrorType.SERIALIZATION,
                ReplayError.ErrorType.TIMEOUT,
                ReplayError.ErrorType.VALIDATION,
                ReplayError.ErrorType.INFRASTRUCTURE,
                ReplayError.ErrorType.UNKNOWN
        );
    }

    // =========================================================================
    // Builder
    // =========================================================================

    @Test
    @DisplayName("Builder deve criar instância com timestamp padrão")
    void builderShouldCreateInstanceWithDefaultTimestamp() {
        ReplayError error = ReplayError.builder()
                .errorType(ReplayError.ErrorType.UNKNOWN)
                .message("erro genérico")
                .build();

        assertThat(error.getTimestamp()).isNotNull();
        assertThat(error.getErrorType()).isEqualTo(ReplayError.ErrorType.UNKNOWN);
    }

    // =========================================================================
    // Factory methods
    // =========================================================================

    @Nested
    @DisplayName("eventProcessingError()")
    class EventProcessingError {

        @Test
        @DisplayName("Deve criar erro com tipo EVENT_PROCESSING")
        void shouldCreateErrorWithEventProcessingType() {
            ReplayError error = ReplayError.eventProcessingError("evt-1", "SinistroCriado", "falha");

            assertThat(error.getErrorType()).isEqualTo(ReplayError.ErrorType.EVENT_PROCESSING);
        }

        @Test
        @DisplayName("Deve preencher eventId e eventType")
        void shouldFillEventIdAndEventType() {
            ReplayError error = ReplayError.eventProcessingError("evt-1", "SinistroCriado", "falha");

            assertThat(error.getEventId()).isEqualTo("evt-1");
            assertThat(error.getEventType()).isEqualTo("SinistroCriado");
            assertThat(error.getMessage()).isEqualTo("falha");
        }

        @Test
        @DisplayName("Deve ser recuperável")
        void shouldBeRecoverable() {
            ReplayError error = ReplayError.eventProcessingError("evt-1", "SinistroCriado", "falha");
            assertThat(error.isRecoverable()).isTrue();
        }
    }

    @Nested
    @DisplayName("handlerExecutionError()")
    class HandlerExecutionError {

        @Test
        @DisplayName("Deve criar erro com tipo HANDLER_EXECUTION")
        void shouldCreateErrorWithHandlerExecutionType() {
            Exception ex = new RuntimeException("handler error");
            ReplayError error = ReplayError.handlerExecutionError("MyHandler", "evt-1", ex);

            assertThat(error.getErrorType()).isEqualTo(ReplayError.ErrorType.HANDLER_EXECUTION);
        }

        @Test
        @DisplayName("Deve preencher handlerName e eventId")
        void shouldFillHandlerNameAndEventId() {
            Exception ex = new RuntimeException("handler error");
            ReplayError error = ReplayError.handlerExecutionError("MyHandler", "evt-1", ex);

            assertThat(error.getHandlerName()).isEqualTo("MyHandler");
            assertThat(error.getEventId()).isEqualTo("evt-1");
        }

        @Test
        @DisplayName("Deve ser recuperável")
        void shouldBeRecoverable() {
            ReplayError error = ReplayError.handlerExecutionError("MyHandler", "evt-1", new RuntimeException());
            assertThat(error.isRecoverable()).isTrue();
        }
    }

    @Nested
    @DisplayName("timeoutError()")
    class TimeoutError {

        @Test
        @DisplayName("Deve criar erro com tipo TIMEOUT")
        void shouldCreateErrorWithTimeoutType() {
            ReplayError error = ReplayError.timeoutError("MyHandler", "evt-1", 30);

            assertThat(error.getErrorType()).isEqualTo(ReplayError.ErrorType.TIMEOUT);
        }

        @Test
        @DisplayName("Deve conter timeout na mensagem")
        void shouldContainTimeoutInMessage() {
            ReplayError error = ReplayError.timeoutError("MyHandler", "evt-1", 30);

            assertThat(error.getMessage()).contains("30");
        }

        @Test
        @DisplayName("Não deve ser recuperável")
        void shouldNotBeRecoverable() {
            ReplayError error = ReplayError.timeoutError("MyHandler", "evt-1", 30);
            assertThat(error.isRecoverable()).isFalse();
        }
    }

    @Nested
    @DisplayName("infrastructureError()")
    class InfrastructureError {

        @Test
        @DisplayName("Deve criar erro com tipo INFRASTRUCTURE")
        void shouldCreateErrorWithInfrastructureType() {
            ReplayError error = ReplayError.infrastructureError("DB down", new RuntimeException());

            assertThat(error.getErrorType()).isEqualTo(ReplayError.ErrorType.INFRASTRUCTURE);
        }

        @Test
        @DisplayName("Deve preencher mensagem")
        void shouldFillMessage() {
            ReplayError error = ReplayError.infrastructureError("DB down", new RuntimeException());

            assertThat(error.getMessage()).isEqualTo("DB down");
        }

        @Test
        @DisplayName("Não deve ser recuperável")
        void shouldNotBeRecoverable() {
            ReplayError error = ReplayError.infrastructureError("DB down", new RuntimeException());
            assertThat(error.isRecoverable()).isFalse();
        }
    }
}
