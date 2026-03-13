package com.seguradora.hibrida.aggregate.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AggregateException Tests")
class AggregateExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem")
    void shouldCreateWithMessage() {
        AggregateException exception = new AggregateException("Test message");

        assertThat(exception.getMessage()).isEqualTo("Test message");
        assertThat(exception.getAggregateId()).isNull();
        assertThat(exception.getAggregateType()).isNull();
        assertThat(exception.getVersion()).isNull();
        assertThat(exception.hasAggregateContext()).isFalse();
    }

    @Test
    @DisplayName("Deve criar com mensagem e causa")
    void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        AggregateException exception = new AggregateException("Test message", cause);

        assertThat(exception.getMessage()).isEqualTo("Test message");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Deve criar com contexto do aggregate")
    void shouldCreateWithAggregateContext() {
        AggregateException exception = new AggregateException(
            "Test message", "agg-123", "TestAggregate", 5L
        );

        assertThat(exception.getAggregateId()).isEqualTo("agg-123");
        assertThat(exception.getAggregateType()).isEqualTo("TestAggregate");
        assertThat(exception.getVersion()).isEqualTo(5L);
        assertThat(exception.hasAggregateContext()).isTrue();
    }

    @Test
    @DisplayName("Deve criar com contexto completo")
    void shouldCreateWithFullContext() {
        Throwable cause = new RuntimeException("Cause");
        AggregateException exception = new AggregateException(
            "Test message", cause, "agg-123", "TestAggregate", 5L
        );

        assertThat(exception.getMessage()).contains("Test message");
        assertThat(exception.getMessage()).contains("type=TestAggregate");
        assertThat(exception.getMessage()).contains("id=agg-123");
        assertThat(exception.getMessage()).contains("version=5");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Deve formatar mensagem com contexto parcial")
    void shouldFormatMessageWithPartialContext() {
        AggregateException exception = new AggregateException(
            "Test message", null, "TestAggregate", null
        );

        assertThat(exception.getMessage()).contains("Test message");
        assertThat(exception.getMessage()).contains("type=TestAggregate");
        assertThat(exception.hasAggregateContext()).isTrue();
    }
}
