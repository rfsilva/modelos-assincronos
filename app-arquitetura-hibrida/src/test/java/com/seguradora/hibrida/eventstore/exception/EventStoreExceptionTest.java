package com.seguradora.hibrida.eventstore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventStoreException Tests")
class EventStoreExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem")
    void shouldCreateWithMessage() {
        EventStoreException exception = new EventStoreException("EventStore error");
        assertThat(exception.getMessage()).isEqualTo("EventStore error");
    }

    @Test
    @DisplayName("Deve criar com mensagem e causa")
    void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        EventStoreException exception = new EventStoreException("EventStore error", cause);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void shouldBeARuntimeException() {
        EventStoreException exception = new EventStoreException("Test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
