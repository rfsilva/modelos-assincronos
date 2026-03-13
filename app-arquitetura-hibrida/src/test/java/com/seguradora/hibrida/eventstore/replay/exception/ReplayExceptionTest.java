package com.seguradora.hibrida.eventstore.replay.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReplayException Tests")
class ReplayExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem")
    void shouldCreateWithMessage() {
        ReplayException exception = new ReplayException("Replay error");
        assertThat(exception.getMessage()).isEqualTo("Replay error");
    }

    @Test
    @DisplayName("Deve criar com mensagem e causa")
    void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        ReplayException exception = new ReplayException("Replay error", cause);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void shouldBeARuntimeException() {
        ReplayException exception = new ReplayException("Test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
