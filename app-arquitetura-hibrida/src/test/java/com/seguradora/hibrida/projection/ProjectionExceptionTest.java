package com.seguradora.hibrida.projection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProjectionException Tests")
class ProjectionExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem e nome da projeção")
    void shouldCreateWithMessageAndProjectionName() {
        String message = "Projection error";
        String projectionName = "TestProjection";

        ProjectionException exception = new ProjectionException(message, projectionName);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getMessage()).contains(projectionName);
        assertThat(exception.getProjectionName()).isEqualTo(projectionName);
    }

    @Test
    @DisplayName("Deve criar com mensagem, nome da projeção e causa")
    void shouldCreateWithMessageProjectionNameAndCause() {
        String message = "Projection error";
        String projectionName = "TestProjection";
        Throwable cause = new RuntimeException("Cause");

        ProjectionException exception = new ProjectionException(message, projectionName, cause);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getProjectionName()).isEqualTo(projectionName);
    }

    @Test
    @DisplayName("Deve criar com contexto completo")
    void shouldCreateWithFullContext() {
        String message = "Projection error";
        String projectionName = "TestProjection";
        String eventType = "TestEvent";
        boolean retryable = false;

        ProjectionException exception = new ProjectionException(message, projectionName, eventType, retryable);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getProjectionName()).isEqualTo(projectionName);
        assertThat(exception.getEventType()).isEqualTo(eventType);
        assertThat(exception.isRetryable()).isFalse();
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void shouldBeARuntimeException() {
        ProjectionException exception = new ProjectionException("Test", "TestProjection");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
