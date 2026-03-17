package com.seguradora.hibrida.eventstore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ConcurrencyException}.
 */
@DisplayName("ConcurrencyException Tests")
class ConcurrencyExceptionTest {

    @Test
    @DisplayName("Deve armazenar aggregateId, expectedVersion e actualVersion")
    void shouldStoreAggregateIdAndVersions() {
        ConcurrencyException ex = new ConcurrencyException("agg-1", 3L, 5L);

        assertThat(ex.getAggregateId()).isEqualTo("agg-1");
        assertThat(ex.getExpectedVersion()).isEqualTo(3L);
        assertThat(ex.getActualVersion()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Mensagem deve conter aggregateId e versões")
    void messageShouldContainAggregateIdAndVersions() {
        ConcurrencyException ex = new ConcurrencyException("agg-42", 2L, 7L);

        assertThat(ex.getMessage())
                .contains("agg-42")
                .contains("2")
                .contains("7");
    }

    @Test
    @DisplayName("Deve estender EventStoreException")
    void shouldExtendEventStoreException() {
        ConcurrencyException ex = new ConcurrencyException("agg-1", 0L, 1L);
        assertThat(ex).isInstanceOf(EventStoreException.class);
    }

    @Test
    @DisplayName("Deve estender RuntimeException")
    void shouldExtendRuntimeException() {
        ConcurrencyException ex = new ConcurrencyException("agg-1", 0L, 1L);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
