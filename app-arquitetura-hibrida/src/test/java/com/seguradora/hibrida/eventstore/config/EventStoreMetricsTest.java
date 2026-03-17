package com.seguradora.hibrida.eventstore.config;

import com.seguradora.hibrida.eventstore.EventStore;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link EventStoreMetrics}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventStoreMetrics Tests")
class EventStoreMetricsTest {

    @Mock
    private EventStore eventStore;

    private MeterRegistry meterRegistry;
    private EventStoreMetrics metrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new EventStoreMetrics(meterRegistry, eventStore);
    }

    // =========================================================================
    // Anotações
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(EventStoreMetrics.class.isAnnotationPresent(Component.class)).isTrue();
    }

    // =========================================================================
    // Contadores
    // =========================================================================

    @Nested
    @DisplayName("Contadores")
    class Contadores {

        @Test
        @DisplayName("incrementEventsWritten deve incrementar totalEvents")
        void incrementEventsWrittenShouldUpdateTotalEvents() {
            metrics.incrementEventsWritten(5);
            assertThat(metrics.getTotalEvents()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("incrementEventsWritten deve acumular chamadas")
        void incrementEventsWrittenShouldAccumulate() {
            metrics.incrementEventsWritten(3);
            metrics.incrementEventsWritten(7);
            assertThat(metrics.getTotalEvents()).isEqualTo(10.0);
        }

        @Test
        @DisplayName("setTotalAggregates deve definir o gauge de aggregates")
        void setTotalAggregatesShouldUpdateGauge() {
            metrics.setTotalAggregates(42L);
            assertThat(metrics.getTotalAggregates()).isEqualTo(42.0);
        }
    }

    // =========================================================================
    // Métricas registradas
    // =========================================================================

    @Test
    @DisplayName("Deve registrar métrica eventstore.events.written")
    void shouldRegisterEventsWrittenMetric() {
        assertThat(meterRegistry.find("eventstore.events.written").counter()).isNotNull();
    }

    @Test
    @DisplayName("Deve registrar métrica eventstore.events.read")
    void shouldRegisterEventsReadMetric() {
        assertThat(meterRegistry.find("eventstore.events.read").counter()).isNotNull();
    }

    @Test
    @DisplayName("Deve registrar métrica eventstore.errors.concurrency")
    void shouldRegisterConcurrencyErrorsMetric() {
        assertThat(meterRegistry.find("eventstore.errors.concurrency").counter()).isNotNull();
    }

    @Test
    @DisplayName("Deve registrar métrica eventstore.errors.serialization")
    void shouldRegisterSerializationErrorsMetric() {
        assertThat(meterRegistry.find("eventstore.errors.serialization").counter()).isNotNull();
    }

    @Test
    @DisplayName("Deve registrar timer eventstore.operations.write")
    void shouldRegisterWriteTimer() {
        assertThat(meterRegistry.find("eventstore.operations.write").timer()).isNotNull();
    }

    @Test
    @DisplayName("Deve registrar timer eventstore.operations.read")
    void shouldRegisterReadTimer() {
        assertThat(meterRegistry.find("eventstore.operations.read").timer()).isNotNull();
    }

    // =========================================================================
    // Timer samples
    // =========================================================================

    @Test
    @DisplayName("startWriteTimer deve retornar sample não nulo")
    void startWriteTimerShouldReturnNonNullSample() {
        var sample = metrics.startWriteTimer();
        assertThat(sample).isNotNull();
    }

    @Test
    @DisplayName("startReadTimer deve retornar sample não nulo")
    void startReadTimerShouldReturnNonNullSample() {
        var sample = metrics.startReadTimer();
        assertThat(sample).isNotNull();
    }
}
