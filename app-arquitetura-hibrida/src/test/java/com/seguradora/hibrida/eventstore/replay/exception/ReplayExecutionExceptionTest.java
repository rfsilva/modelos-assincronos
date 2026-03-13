package com.seguradora.hibrida.eventstore.replay.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReplayExecutionException Tests")
class ReplayExecutionExceptionTest {

    @Test
    @DisplayName("Deve criar com replayId, name e mensagem")
    void shouldCreateWithReplayIdNameAndMessage() {
        UUID replayId = UUID.randomUUID();
        String replayName = "Test Replay";
        String message = "Execution error";

        ReplayExecutionException exception = new ReplayExecutionException(replayId, replayName, message);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getReplayId()).isEqualTo(replayId);
        assertThat(exception.getReplayName()).isEqualTo(replayName);
        assertThat(exception.hasEventContext()).isFalse();
    }

    @Test
    @DisplayName("Deve criar com replayId, name, mensagem e causa")
    void shouldCreateWithReplayIdNameMessageAndCause() {
        UUID replayId = UUID.randomUUID();
        String replayName = "Test Replay";
        String message = "Execution error";
        Throwable cause = new RuntimeException("Cause");

        ReplayExecutionException exception = new ReplayExecutionException(replayId, replayName, message, cause);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getReplayId()).isEqualTo(replayId);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.hasEventContext()).isFalse();
    }

    @Test
    @DisplayName("Deve criar com contexto completo de evento")
    void shouldCreateWithFullEventContext() {
        UUID replayId = UUID.randomUUID();
        String replayName = "Test Replay";
        String eventId = "EVT-123";
        String handlerName = "TestHandler";
        int attemptNumber = 3;
        String message = "Handler failed";
        Throwable cause = new RuntimeException("Processing error");

        ReplayExecutionException exception = new ReplayExecutionException(
            replayId, replayName, eventId, handlerName, attemptNumber, message, cause
        );

        assertThat(exception.getMessage()).contains(eventId);
        assertThat(exception.getMessage()).contains(handlerName);
        assertThat(exception.getMessage()).contains("3");
        assertThat(exception.getReplayId()).isEqualTo(replayId);
        assertThat(exception.getEventId()).isEqualTo(eventId);
        assertThat(exception.getHandlerName()).isEqualTo(handlerName);
        assertThat(exception.getAttemptNumber()).isEqualTo(attemptNumber);
        assertThat(exception.hasEventContext()).isTrue();
    }

    @Test
    @DisplayName("Deve ser uma ReplayException")
    void shouldBeAReplayException() {
        UUID replayId = UUID.randomUUID();
        ReplayExecutionException exception = new ReplayExecutionException(replayId, "Test", "Test message");
        assertThat(exception).isInstanceOf(ReplayException.class);
    }
}
