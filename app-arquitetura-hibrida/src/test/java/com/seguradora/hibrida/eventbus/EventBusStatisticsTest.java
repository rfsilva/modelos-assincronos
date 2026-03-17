package com.seguradora.hibrida.eventbus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link EventBusStatistics}.
 */
@DisplayName("EventBusStatistics Tests")
class EventBusStatisticsTest {

    private EventBusStatistics stats;

    @BeforeEach
    void setUp() {
        stats = new EventBusStatistics();
    }

    // =========================================================================
    // Estado inicial
    // =========================================================================

    @Nested
    @DisplayName("Estado inicial")
    class EstadoInicial {

        @Test
        @DisplayName("Deve iniciar com todos os contadores zerados")
        void shouldStartWithZeroCounters() {
            assertThat(stats.getEventsPublished()).isZero();
            assertThat(stats.getEventsProcessed()).isZero();
            assertThat(stats.getEventsFailed()).isZero();
            assertThat(stats.getEventsRetried()).isZero();
            assertThat(stats.getEventsDeadLettered()).isZero();
            assertThat(stats.getActiveHandlers()).isZero();
        }

        @Test
        @DisplayName("Deve retornar successRate=1.0 quando não há eventos")
        void shouldReturnSuccessRateOneWhenNoEvents() {
            assertThat(stats.getSuccessRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Deve retornar errorRate=0.0 quando não há eventos")
        void shouldReturnErrorRateZeroWhenNoEvents() {
            assertThat(stats.getErrorRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Deve retornar minProcessingTime=0 quando não há eventos")
        void shouldReturnMinProcessingTimeZeroWhenNoEvents() {
            assertThat(stats.getMinProcessingTime()).isZero();
        }

        @Test
        @DisplayName("Deve inicializar startTime e lastEventTime")
        void shouldInitializeTimestamps() {
            assertThat(stats.getStartTime()).isNotNull();
            assertThat(stats.getLastEventTime()).isNotNull();
        }
    }

    // =========================================================================
    // recordEventPublished
    // =========================================================================

    @Nested
    @DisplayName("recordEventPublished()")
    class RecordEventPublished {

        @Test
        @DisplayName("Deve incrementar eventsPublished")
        void shouldIncrementEventsPublished() {
            stats.recordEventPublished("TestEvent");
            stats.recordEventPublished("TestEvent");

            assertThat(stats.getEventsPublished()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Deve rastrear contagem por tipo de evento")
        void shouldTrackCountByEventType() {
            stats.recordEventPublished("TypeA");
            stats.recordEventPublished("TypeA");
            stats.recordEventPublished("TypeB");

            assertThat(stats.getEventsByType()).containsEntry("TypeA", 2L);
            assertThat(stats.getEventsByType()).containsEntry("TypeB", 1L);
        }
    }

    // =========================================================================
    // recordEventProcessed
    // =========================================================================

    @Nested
    @DisplayName("recordEventProcessed()")
    class RecordEventProcessed {

        @Test
        @DisplayName("Deve incrementar eventsProcessed")
        void shouldIncrementEventsProcessed() {
            stats.recordEventPublished("TestEvent");
            stats.recordEventProcessed("TestEvent", 10L);

            assertThat(stats.getEventsProcessed()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve atualizar tempo mínimo de processamento")
        void shouldUpdateMinProcessingTime() {
            stats.recordEventProcessed("E", 100L);
            stats.recordEventProcessed("E", 50L);

            assertThat(stats.getMinProcessingTime()).isEqualTo(50L);
        }

        @Test
        @DisplayName("Deve atualizar tempo máximo de processamento")
        void shouldUpdateMaxProcessingTime() {
            stats.recordEventProcessed("E", 100L);
            stats.recordEventProcessed("E", 200L);

            assertThat(stats.getMaxProcessingTime()).isEqualTo(200L);
        }

        @Test
        @DisplayName("Deve calcular tempo médio corretamente")
        void shouldCalculateAverageProcessingTimeCorrectly() {
            stats.recordEventProcessed("E", 100L);
            stats.recordEventProcessed("E", 200L);

            assertThat(stats.getAverageProcessingTime()).isEqualTo(150.0);
        }
    }

    // =========================================================================
    // recordEventFailed
    // =========================================================================

    @Nested
    @DisplayName("recordEventFailed()")
    class RecordEventFailed {

        @Test
        @DisplayName("Deve incrementar eventsFailed")
        void shouldIncrementEventsFailed() {
            stats.recordEventPublished("TestEvent");
            stats.recordEventFailed("TestEvent", false);

            assertThat(stats.getEventsFailed()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve incrementar eventsDeadLettered quando não é retryable")
        void shouldIncrementDeadLetteredWhenNotRetryable() {
            stats.recordEventFailed("TestEvent", false);

            assertThat(stats.getEventsDeadLettered()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Não deve incrementar eventsDeadLettered quando é retryable")
        void shouldNotIncrementDeadLetteredWhenRetryable() {
            stats.recordEventFailed("TestEvent", true);

            assertThat(stats.getEventsDeadLettered()).isZero();
        }

        @Test
        @DisplayName("Deve rastrear falhas por tipo de evento")
        void shouldTrackFailuresByEventType() {
            stats.recordEventFailed("TypeA", true);
            stats.recordEventFailed("TypeA", false);
            stats.recordEventFailed("TypeB", true);

            assertThat(stats.getFailuresByType()).containsEntry("TypeA", 2L);
            assertThat(stats.getFailuresByType()).containsEntry("TypeB", 1L);
        }
    }

    // =========================================================================
    // Taxas calculadas
    // =========================================================================

    @Nested
    @DisplayName("Taxas calculadas")
    class TaxasCalculadas {

        @Test
        @DisplayName("Deve calcular successRate corretamente")
        void shouldCalculateSuccessRateCorrectly() {
            stats.recordEventPublished("E");
            stats.recordEventPublished("E");
            stats.recordEventProcessed("E", 10L);

            // 1 processed / 2 published = 0.5
            assertThat(stats.getSuccessRate()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("Deve calcular errorRate corretamente")
        void shouldCalculateErrorRateCorrectly() {
            stats.recordEventPublished("E");
            stats.recordEventPublished("E");
            stats.recordEventPublished("E");
            stats.recordEventPublished("E");
            stats.recordEventFailed("E", true);

            // 1 failed / 4 published = 0.25
            assertThat(stats.getErrorRate()).isEqualTo(0.25);
        }
    }

    // =========================================================================
    // Handler tracking
    // =========================================================================

    @Nested
    @DisplayName("Handler tracking")
    class HandlerTracking {

        @Test
        @DisplayName("Deve incrementar e decrementar activeHandlers")
        void shouldIncrementAndDecrementActiveHandlers() {
            stats.recordHandlerStarted();
            stats.recordHandlerStarted();
            assertThat(stats.getActiveHandlers()).isEqualTo(2L);

            stats.recordHandlerFinished();
            assertThat(stats.getActiveHandlers()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve manter maxConcurrentHandlers")
        void shouldMaintainMaxConcurrentHandlers() {
            stats.recordHandlerStarted();
            stats.recordHandlerStarted();
            stats.recordHandlerStarted();
            stats.recordHandlerFinished();

            assertThat(stats.getMaxConcurrentHandlers()).isEqualTo(3L);
        }
    }

    // =========================================================================
    // reset
    // =========================================================================

    @Test
    @DisplayName("reset() deve zerar todos os contadores")
    void resetShouldClearAllCounters() {
        stats.recordEventPublished("E");
        stats.recordEventProcessed("E", 50L);
        stats.recordEventFailed("E", false);
        stats.recordHandlerStarted();

        stats.reset();

        assertThat(stats.getEventsPublished()).isZero();
        assertThat(stats.getEventsProcessed()).isZero();
        assertThat(stats.getEventsFailed()).isZero();
        assertThat(stats.getEventsDeadLettered()).isZero();
        assertThat(stats.getEventsByType()).isEmpty();
        assertThat(stats.getFailuresByType()).isEmpty();
        assertThat(stats.getMinProcessingTime()).isZero();
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString() deve retornar String não nula")
    void toStringShouldReturnNonNull() {
        assertThat(stats.toString()).isNotNull().isNotBlank();
    }
}
