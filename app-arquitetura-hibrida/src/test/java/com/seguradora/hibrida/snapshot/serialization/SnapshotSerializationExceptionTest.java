package com.seguradora.hibrida.snapshot.serialization;

import com.seguradora.hibrida.snapshot.exception.SnapshotException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SnapshotSerializationException Tests")
class SnapshotSerializationExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem e operação")
    void shouldCreateWithMessage() {
        String message = "Serialization error";
        String operation = "serialize";

        SnapshotSerializationException exception = new SnapshotSerializationException(message, operation);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getOperation()).isEqualTo(operation);
    }

    @Test
    @DisplayName("Deve criar com mensagem, causa e operação")
    void shouldCreateWithMessageAndCause() {
        String message = "Serialization error";
        Throwable cause = new RuntimeException("Cause");
        String operation = "deserialize";

        SnapshotSerializationException exception = new SnapshotSerializationException(message, cause, operation);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getOperation()).isEqualTo(operation);
    }

    @Test
    @DisplayName("Deve ser uma SnapshotException")
    void shouldBeASnapshotException() {
        SnapshotSerializationException exception = new SnapshotSerializationException("Test", "serialize");
        assertThat(exception).isInstanceOf(SnapshotException.class);
    }
}
