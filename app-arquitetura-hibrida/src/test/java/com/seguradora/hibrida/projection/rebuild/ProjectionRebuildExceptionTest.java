package com.seguradora.hibrida.projection.rebuild;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProjectionRebuildException Tests")
class ProjectionRebuildExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem")
    void shouldCreateWithMessage() {
        ProjectionRebuildException exception = new ProjectionRebuildException("Rebuild error");
        assertThat(exception.getMessage()).isEqualTo("Rebuild error");
    }

    @Test
    @DisplayName("Deve criar com mensagem e causa")
    void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        ProjectionRebuildException exception = new ProjectionRebuildException("Rebuild error", cause);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void shouldBeARuntimeException() {
        ProjectionRebuildException exception = new ProjectionRebuildException("Test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
