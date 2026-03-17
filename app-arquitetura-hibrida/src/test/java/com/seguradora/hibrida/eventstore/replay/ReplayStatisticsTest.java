package com.seguradora.hibrida.eventstore.replay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ReplayStatistics}.
 */
@DisplayName("ReplayStatistics Tests")
class ReplayStatisticsTest {

    @Test
    @DisplayName("Builder deve criar instância com timestamp não nulo")
    void builderShouldCreateInstanceWithNonNullTimestamp() {
        ReplayStatistics stats = ReplayStatistics.builder().build();
        assertThat(stats.getTimestamp()).isNotNull();
    }

    // =========================================================================
    // getOverallSuccessRate
    // =========================================================================

    @Nested
    @DisplayName("getOverallSuccessRate()")
    class GetOverallSuccessRate {

        @Test
        @DisplayName("Deve retornar 0.0 quando totalReplaysExecuted é 0")
        void shouldReturnZeroWhenNoReplays() {
            ReplayStatistics stats = ReplayStatistics.builder()
                    .totalReplaysExecuted(0)
                    .successfulReplays(0)
                    .build();

            assertThat(stats.getOverallSuccessRate()).isZero();
        }

        @Test
        @DisplayName("Deve calcular taxa de sucesso corretamente")
        void shouldCalculateSuccessRateCorrectly() {
            ReplayStatistics stats = ReplayStatistics.builder()
                    .totalReplaysExecuted(10)
                    .successfulReplays(8)
                    .build();

            assertThat(stats.getOverallSuccessRate()).isEqualTo(80.0);
        }

        @Test
        @DisplayName("Deve retornar 100.0 quando todos bem-sucedidos")
        void shouldReturn100WhenAllSuccessful() {
            ReplayStatistics stats = ReplayStatistics.builder()
                    .totalReplaysExecuted(5)
                    .successfulReplays(5)
                    .build();

            assertThat(stats.getOverallSuccessRate()).isEqualTo(100.0);
        }
    }

    // =========================================================================
    // getOverallErrorRate
    // =========================================================================

    @Nested
    @DisplayName("getOverallErrorRate()")
    class GetOverallErrorRate {

        @Test
        @DisplayName("Deve retornar 0.0 quando totalReplaysExecuted é 0")
        void shouldReturnZeroWhenNoReplays() {
            ReplayStatistics stats = ReplayStatistics.builder()
                    .totalReplaysExecuted(0)
                    .failedReplays(0)
                    .build();

            assertThat(stats.getOverallErrorRate()).isZero();
        }

        @Test
        @DisplayName("Deve calcular taxa de erro corretamente")
        void shouldCalculateErrorRateCorrectly() {
            ReplayStatistics stats = ReplayStatistics.builder()
                    .totalReplaysExecuted(10)
                    .failedReplays(3)
                    .build();

            assertThat(stats.getOverallErrorRate()).isEqualTo(30.0);
        }
    }

    // =========================================================================
    // getEventSuccessRate
    // =========================================================================

    @Nested
    @DisplayName("getEventSuccessRate()")
    class GetEventSuccessRate {

        @Test
        @DisplayName("Deve retornar 0.0 quando totalEventsReprocessed é 0")
        void shouldReturnZeroWhenNoEventsReprocessed() {
            ReplayStatistics stats = ReplayStatistics.builder()
                    .totalEventsReprocessed(0)
                    .successfulEventsReprocessed(0)
                    .build();

            assertThat(stats.getEventSuccessRate()).isZero();
        }

        @Test
        @DisplayName("Deve calcular taxa de sucesso de eventos corretamente")
        void shouldCalculateEventSuccessRateCorrectly() {
            ReplayStatistics stats = ReplayStatistics.builder()
                    .totalEventsReprocessed(1000)
                    .successfulEventsReprocessed(950)
                    .build();

            assertThat(stats.getEventSuccessRate()).isEqualTo(95.0);
        }
    }

    // =========================================================================
    // Inner classes
    // =========================================================================

    @Test
    @DisplayName("ReplayTypeStatistics deve ser classe interna pública")
    void replayTypeStatisticsShouldBePublicInnerClass() {
        assertThat(ReplayStatistics.ReplayTypeStatistics.class.getEnclosingClass())
                .isEqualTo(ReplayStatistics.class);
    }

    @Test
    @DisplayName("PeriodStatistics deve ser classe interna pública")
    void periodStatisticsShouldBePublicInnerClass() {
        assertThat(ReplayStatistics.PeriodStatistics.class.getEnclosingClass())
                .isEqualTo(ReplayStatistics.class);
    }

    @Test
    @DisplayName("ReplayTypeStatistics builder deve funcionar")
    void replayTypeStatisticsBuilderShouldWork() {
        ReplayStatistics.ReplayTypeStatistics typeStats = ReplayStatistics.ReplayTypeStatistics.builder()
                .replayType("BY_PERIOD")
                .totalExecutions(10)
                .successfulExecutions(8)
                .failedExecutions(2)
                .build();

        assertThat(typeStats.getReplayType()).isEqualTo("BY_PERIOD");
        assertThat(typeStats.getTotalExecutions()).isEqualTo(10);
    }

    @Test
    @DisplayName("Builder deve ter maps de estatísticas vazio por padrão")
    void builderShouldDefaultToEmptyMaps() {
        ReplayStatistics stats = ReplayStatistics.builder().build();

        assertThat(stats.getReplayTypeStatistics()).isEmpty();
        assertThat(stats.getPeriodStatistics()).isEmpty();
    }
}
