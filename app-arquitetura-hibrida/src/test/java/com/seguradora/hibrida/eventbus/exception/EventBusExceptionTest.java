package com.seguradora.hibrida.eventbus.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventBusException Tests")
class EventBusExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem")
    void shouldCreateWithMessage() {
        EventBusException exception = new EventBusException("Event bus error");
        assertThat(exception.getMessage()).isEqualTo("Event bus error");
    }

    @Test
    @DisplayName("Deve criar com mensagem e causa")
    void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        EventBusException exception = new EventBusException("Event bus error", cause);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void shouldBeARuntimeException() {
        EventBusException exception = new EventBusException("Test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
