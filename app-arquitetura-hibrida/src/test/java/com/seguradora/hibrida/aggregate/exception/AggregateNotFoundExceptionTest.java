package com.seguradora.hibrida.aggregate.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AggregateNotFoundException Tests")
class AggregateNotFoundExceptionTest {

    @Test
    @DisplayName("Deve criar com aggregateId e aggregateType")
    void shouldCreateWithAggregateIdAndType() {
        String aggregateId = "AGG-123";
        String aggregateType = "SinistroAggregate";

        AggregateNotFoundException exception = new AggregateNotFoundException(aggregateId, aggregateType);

        assertThat(exception.getMessage()).contains(aggregateId);
        assertThat(exception.getMessage()).contains(aggregateType);
        assertThat(exception.getAggregateId()).isEqualTo(aggregateId);
        assertThat(exception.getAggregateType()).isEqualTo(aggregateType);
    }

    @Test
    @DisplayName("Deve criar com aggregateId e classe")
    void shouldCreateWithAggregateIdAndClass() {
        String aggregateId = "AGG-456";

        AggregateNotFoundException exception = new AggregateNotFoundException(aggregateId, String.class);

        assertThat(exception.getMessage()).contains(aggregateId);
        assertThat(exception.getMessage()).contains("String");
    }

    @Test
    @DisplayName("Deve criar com aggregateId, type e versão")
    void shouldCreateWithAggregateIdTypeAndVersion() {
        String aggregateId = "AGG-789";
        String aggregateType = "ApoliceAggregate";
        Long version = 5L;

        AggregateNotFoundException exception = new AggregateNotFoundException(aggregateId, aggregateType, version);

        assertThat(exception.getMessage()).contains(aggregateId);
        assertThat(exception.getMessage()).contains(aggregateType);
        assertThat(exception.getMessage()).contains("5");
        assertThat(exception.getVersion()).isEqualTo(version);
    }

    @Test
    @DisplayName("Deve criar com mensagem customizada")
    void shouldCreateWithCustomMessage() {
        String message = "Custom error message";
        String aggregateId = "AGG-999";
        String aggregateType = "CustomAggregate";

        AggregateNotFoundException exception = new AggregateNotFoundException(message, aggregateId, aggregateType);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getAggregateId()).isEqualTo(aggregateId);
        assertThat(exception.getAggregateType()).isEqualTo(aggregateType);
    }

    @Test
    @DisplayName("Deve ser uma AggregateException")
    void shouldBeAnAggregateException() {
        AggregateNotFoundException exception = new AggregateNotFoundException("TEST-123", "TestAggregate");
        assertThat(exception).isInstanceOf(AggregateException.class);
    }
}
