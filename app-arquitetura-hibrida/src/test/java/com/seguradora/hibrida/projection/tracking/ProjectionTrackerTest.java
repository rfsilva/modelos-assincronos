package com.seguradora.hibrida.projection.tracking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ProjectionTracker}.
 */
@DisplayName("ProjectionTracker Tests")
class ProjectionTrackerTest {

    private ProjectionTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new ProjectionTracker("TestProjection");
    }

    // =========================================================================
    // Construção
    // =========================================================================

    @Nested
    @DisplayName("Construção")
    class Construcao {

        @Test
        @DisplayName("Construtor com nome deve inicializar campos corretamente")
        void constructorShouldInitializeFields() {
            assertThat(tracker.getProjectionName()).isEqualTo("TestProjection");
            assertThat(tracker.getLastProcessedEventId()).isZero();
            assertThat(tracker.getStatus()).isEqualTo(ProjectionStatus.ACTIVE);
            assertThat(tracker.getEventsProcessed()).isZero();
            assertThat(tracker.getEventsFailed()).isZero();
        }

        @Test
        @DisplayName("Construtor no-args deve criar instância")
        void noArgsConstructorShouldCreateInstance() {
            ProjectionTracker t = new ProjectionTracker();
            assertThat(t).isNotNull();
        }

        @Test
        @DisplayName("Construtor deve definir createdAt e updatedAt")
        void constructorShouldSetCreatedAtAndUpdatedAt() {
            assertThat(tracker.getCreatedAt()).isNotNull();
            assertThat(tracker.getUpdatedAt()).isNotNull();
        }
    }

    // =========================================================================
    // updatePosition
    // =========================================================================

    @Nested
    @DisplayName("updatePosition()")
    class UpdatePosition {

        @Test
        @DisplayName("Deve atualizar lastProcessedEventId")
        void shouldUpdateLastProcessedEventId() {
            tracker.updatePosition(42L);

            assertThat(tracker.getLastProcessedEventId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("Deve incrementar eventsProcessed")
        void shouldIncrementEventsProcessed() {
            tracker.updatePosition(1L);
            tracker.updatePosition(2L);

            assertThat(tracker.getEventsProcessed()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Deve limpar status de ERROR ao processar com sucesso")
        void shouldClearErrorStatusOnSuccessfulProcessing() {
            tracker.recordFailure("some error");
            assertThat(tracker.getStatus()).isEqualTo(ProjectionStatus.ERROR);

            tracker.updatePosition(10L);

            assertThat(tracker.getStatus()).isEqualTo(ProjectionStatus.ACTIVE);
            assertThat(tracker.getLastErrorMessage()).isNull();
        }
    }

    // =========================================================================
    // recordFailure
    // =========================================================================

    @Nested
    @DisplayName("recordFailure()")
    class RecordFailure {

        @Test
        @DisplayName("Deve definir status como ERROR")
        void shouldSetStatusToError() {
            tracker.recordFailure("Connection error");

            assertThat(tracker.getStatus()).isEqualTo(ProjectionStatus.ERROR);
        }

        @Test
        @DisplayName("Deve incrementar eventsFailed")
        void shouldIncrementEventsFailed() {
            tracker.recordFailure("err1");
            tracker.recordFailure("err2");

            assertThat(tracker.getEventsFailed()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Deve armazenar lastErrorMessage")
        void shouldStoreLastErrorMessage() {
            tracker.recordFailure("DB connection failed");

            assertThat(tracker.getLastErrorMessage()).isEqualTo("DB connection failed");
        }
    }

    // =========================================================================
    // pause / resume
    // =========================================================================

    @Nested
    @DisplayName("pause() e resume()")
    class PauseResume {

        @Test
        @DisplayName("pause() deve definir status como PAUSED")
        void pauseShouldSetStatusToPaused() {
            tracker.pause();
            assertThat(tracker.getStatus()).isEqualTo(ProjectionStatus.PAUSED);
        }

        @Test
        @DisplayName("resume() deve definir status como ACTIVE")
        void resumeShouldSetStatusToActive() {
            tracker.pause();
            tracker.resume();
            assertThat(tracker.getStatus()).isEqualTo(ProjectionStatus.ACTIVE);
        }
    }

    // =========================================================================
    // calculateLag
    // =========================================================================

    @Nested
    @DisplayName("calculateLag()")
    class CalculateLag {

        @Test
        @DisplayName("Deve calcular lag corretamente")
        void shouldCalculateLagCorrectly() {
            tracker.updatePosition(50L);
            assertThat(tracker.calculateLag(100L)).isEqualTo(50L);
        }

        @Test
        @DisplayName("Deve retornar 0 quando maxAvailableEventId é null")
        void shouldReturnZeroWhenMaxEventIdIsNull() {
            assertThat(tracker.calculateLag(null)).isZero();
        }

        @Test
        @DisplayName("Deve retornar 0 quando não há lag")
        void shouldReturnZeroWhenNoLag() {
            tracker.updatePosition(100L);
            assertThat(tracker.calculateLag(100L)).isZero();
        }
    }

    // =========================================================================
    // isHealthy / getErrorRate
    // =========================================================================

    @Nested
    @DisplayName("isHealthy() e getErrorRate()")
    class HealthAndErrorRate {

        @Test
        @DisplayName("isHealthy deve retornar true quando ACTIVE e sem erros recentes")
        void isHealthyShouldReturnTrueWhenActiveWithNoRecentErrors() {
            assertThat(tracker.isHealthy()).isTrue();
        }

        @Test
        @DisplayName("isHealthy deve retornar false quando status é ERROR")
        void isHealthyShouldReturnFalseWhenStatusIsError() {
            tracker.recordFailure("error");
            assertThat(tracker.isHealthy()).isFalse();
        }

        @Test
        @DisplayName("getErrorRate deve retornar 0 quando não há eventos")
        void getErrorRateShouldReturnZeroWhenNoEvents() {
            assertThat(tracker.getErrorRate()).isZero();
        }

        @Test
        @DisplayName("getErrorRate deve calcular corretamente")
        void getErrorRateShouldCalculateCorrectly() {
            tracker.updatePosition(1L);
            tracker.updatePosition(2L);
            tracker.recordFailure("err");
            // 1 failed / (2 processed + 1 failed) = 0.333...
            assertThat(tracker.getErrorRate()).isCloseTo(1.0 / 3, org.assertj.core.data.Offset.offset(0.01));
        }
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString() deve retornar String não nula")
    void toStringShouldReturnNonNull() {
        assertThat(tracker.toString()).isNotNull().isNotBlank().contains("TestProjection");
    }
}
