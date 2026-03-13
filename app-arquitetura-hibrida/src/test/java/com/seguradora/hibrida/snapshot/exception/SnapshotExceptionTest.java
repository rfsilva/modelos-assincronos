package com.seguradora.hibrida.snapshot.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SnapshotException Tests")
class SnapshotExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem")
    void shouldCreateWithMessage() {
        SnapshotException exception = new SnapshotException("Snapshot error");
        assertThat(exception.getMessage()).isEqualTo("Snapshot error");
    }

    @Test
    @DisplayName("Deve criar com mensagem e causa")
    void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        SnapshotException exception = new SnapshotException("Snapshot error", cause);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void shouldBeARuntimeException() {
        SnapshotException exception = new SnapshotException("Test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
