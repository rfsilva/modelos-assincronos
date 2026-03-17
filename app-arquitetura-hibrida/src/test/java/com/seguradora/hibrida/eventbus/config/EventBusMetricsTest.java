package com.seguradora.hibrida.eventbus.config;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventBusStatistics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link EventBusMetrics}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EventBusMetrics Tests")
class EventBusMetricsTest {

    @Mock
    private EventBus eventBus;

    private MeterRegistry meterRegistry;
    private EventBusMetrics metrics;

    @BeforeEach
    void setUp() {
        EventBusStatistics stats = new EventBusStatistics();
        when(eventBus.getStatistics()).thenReturn(stats);

        meterRegistry = new SimpleMeterRegistry();
        metrics = new EventBusMetrics(eventBus);
    }

    // =========================================================================
    // Anotações
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(EventBusMetrics.class.isAnnotationPresent(Component.class)).isTrue();
    }

    @Test
    @DisplayName("Deve implementar MeterBinder")
    void shouldImplementMeterBinder() {
        assertThat(metrics).isInstanceOf(MeterBinder.class);
    }

    // =========================================================================
    // bindTo
    // =========================================================================

    @Nested
    @DisplayName("bindTo()")
    class BindTo {

        @Test
        @DisplayName("Deve registrar contadores de eventos no registry")
        void shouldRegisterEventCountersInRegistry() {
            metrics.bindTo(meterRegistry);

            assertThat(meterRegistry.find("eventbus.events.published").counter()).isNotNull();
            assertThat(meterRegistry.find("eventbus.events.processed").counter()).isNotNull();
            assertThat(meterRegistry.find("eventbus.events.failed").counter()).isNotNull();
            assertThat(meterRegistry.find("eventbus.events.retried").counter()).isNotNull();
            assertThat(meterRegistry.find("eventbus.events.deadlettered").counter()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar timer de processamento")
        void shouldRegisterProcessingTimer() {
            metrics.bindTo(meterRegistry);

            assertThat(meterRegistry.find("eventbus.processing.time").timer()).isNotNull();
        }
    }

    // =========================================================================
    // Métodos auxiliares
    // =========================================================================

    @Test
    @DisplayName("updateStatistics() não deve lançar exceção")
    void updateStatisticsShouldNotThrowException() {
        // Não deve lançar exceção ao chamar updateStatistics via construtor
        EventBusMetrics m = new EventBusMetrics(eventBus);
        assertThat(m).isNotNull();
    }
}
