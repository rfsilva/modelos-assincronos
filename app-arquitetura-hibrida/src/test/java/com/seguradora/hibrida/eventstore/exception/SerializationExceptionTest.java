package com.seguradora.hibrida.eventstore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SerializationException Tests")
class SerializationExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem")
    void shouldCreateWithMessage() {
        SerializationException exception = new SerializationException("Serialization error");
        assertThat(exception.getMessage()).isEqualTo("Serialization error");
    }

    @Test
    @DisplayName("Deve criar com mensagem e causa")
    void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        SerializationException exception = new SerializationException("Serialization error", cause);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Deve ser uma EventStoreException")
    void shouldBeAnEventStoreException() {
        SerializationException exception = new SerializationException("Test");
        assertThat(exception).isInstanceOf(EventStoreException.class);
    }
}
