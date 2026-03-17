package com.seguradora.hibrida.snapshot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Testes unitários para {@link SnapshotEfficiencyMetrics}.
 */
@DisplayName("SnapshotEfficiencyMetrics Tests")
class SnapshotEfficiencyMetricsTest {

    private static final Instant PERIOD_START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant PERIOD_END   = Instant.parse("2026-01-11T00:00:00Z"); // 10 dias

    // =========================================================================
    // Builder e campos básicos
    // =========================================================================

    @Test
    @DisplayName("Deve construir instância com builder e retornar campos corretamente")
    void shouldBuildAndReturnFields() {
        SnapshotEfficiencyMetrics m = baseBuilder().build();

        assertThat(m.getAggregateId()).isEqualTo("agg-1");
        assertThat(m.getPeriodStart()).isEqualTo(PERIOD_START);
        assertThat(m.getPeriodEnd()).isEqualTo(PERIOD_END);
        assertThat(m.getReconstructionsWithSnapshot()).isEqualTo(80L);
        assertThat(m.getReconstructionsWithoutSnapshot()).isEqualTo(20L);
        assertThat(m.getSnapshotsCreated()).isEqualTo(10L);
        assertThat(m.getSnapshotsFailed()).isEqualTo(1L);
    }

    // =========================================================================
    // getPerformanceImprovement()
    // =========================================================================

    @Nested
    @DisplayName("getPerformanceImprovement()")
    class PerformanceImprovement {

        @Test
        @DisplayName("Deve calcular fator de melhoria corretamente")
        void shouldCalculateFactor() {
            // withoutSnapshot = 500ms, withSnapshot = 100ms → fator = 5.0
            SnapshotEfficiencyMetrics m = baseBuilder()
                    .averageReconstructionTimeWithSnapshot(100.0)
                    .averageReconstructionTimeWithoutSnapshot(500.0)
                    .build();

            assertThat(m.getPerformanceImprovement()).isCloseTo(5.0, within(0.001));
        }

        @Test
        @DisplayName("Deve retornar 0 quando tempo com snapshot é zero")
        void shouldReturnZeroWhenSnapshotTimeIsZero() {
            SnapshotEfficiencyMetrics m = baseBuilder()
                    .averageReconstructionTimeWithSnapshot(0.0)
                    .averageReconstructionTimeWithoutSnapshot(500.0)
                    .build();

            assertThat(m.getPerformanceImprovement()).isEqualTo(0.0);
        }
    }

    // =========================================================================
    // getTimeSavedPercentage()
    // =========================================================================

    @Nested
    @DisplayName("getTimeSavedPercentage()")
    class TimeSavedPercentage {

        @Test
        @DisplayName("Deve calcular porcentagem de tempo economizado corretamente")
        void shouldCalculatePercentage() {
            // without=500, with=100 → saved=400 → 80%
            SnapshotEfficiencyMetrics m = baseBuilder()
                    .averageReconstructionTimeWithSnapshot(100.0)
                    .averageReconstructionTimeWithoutSnapshot(500.0)
                    .build();

            assertThat(m.getTimeSavedPercentage()).isCloseTo(80.0, within(0.001));
        }

        @Test
        @DisplayName("Deve retornar 0 quando tempo sem snapshot é zero")
        void shouldReturnZeroWhenWithoutSnapshotTimeIsZero() {
            SnapshotEfficiencyMetrics m = baseBuilder()
                    .averageReconstructionTimeWithoutSnapshot(0.0)
                    .build();

            assertThat(m.getTimeSavedPercentage()).isEqualTo(0.0);
        }
    }

    // =========================================================================
    // getEventReductionPercentage()
    // =========================================================================

    @Test
    @DisplayName("Deve calcular redução de eventos corretamente")
    void shouldCalculateEventReduction() {
        // withoutSnapshot=200, withSnapshot=10 → reduced=190 → 95%
        SnapshotEfficiencyMetrics m = baseBuilder()
                .averageEventsWithSnapshot(10.0)
                .averageEventsWithoutSnapshot(200.0)
                .build();

        assertThat(m.getEventReductionPercentage()).isCloseTo(95.0, within(0.001));
    }

    @Test
    @DisplayName("Deve retornar 0 quando eventos sem snapshot é zero")
    void shouldReturnZeroEventReductionWhenWithoutIsZero() {
        SnapshotEfficiencyMetrics m = baseBuilder()
                .averageEventsWithoutSnapshot(0.0)
                .build();

        assertThat(m.getEventReductionPercentage()).isEqualTo(0.0);
    }

    // =========================================================================
    // getSnapshotSuccessRate()
    // =========================================================================

    @Nested
    @DisplayName("getSnapshotSuccessRate()")
    class SnapshotSuccessRate {

        @Test
        @DisplayName("Deve retornar 1.0 quando não houve tentativas")
        void shouldReturnOneWhenNoAttempts() {
            SnapshotEfficiencyMetrics m = baseBuilder()
                    .snapshotsCreated(0L)
                    .snapshotsFailed(0L)
                    .build();

            assertThat(m.getSnapshotSuccessRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Deve calcular taxa de sucesso corretamente")
        void shouldCalculateSuccessRate() {
            // 9 criados, 1 falhou → 9/10 = 0.9
            SnapshotEfficiencyMetrics m = baseBuilder()
                    .snapshotsCreated(9L)
                    .snapshotsFailed(1L)
                    .build();

            assertThat(m.getSnapshotSuccessRate()).isCloseTo(0.9, within(0.001));
        }
    }

    // =========================================================================
    // getTotalReconstructions() e getSnapshotUsagePercentage()
    // =========================================================================

    @Test
    @DisplayName("Deve calcular total de reconstruções")
    void shouldCalculateTotalReconstructions() {
        SnapshotEfficiencyMetrics m = baseBuilder()
                .reconstructionsWithSnapshot(80L)
                .reconstructionsWithoutSnapshot(20L)
                .build();

        assertThat(m.getTotalReconstructions()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Deve calcular porcentagem de uso de snapshots")
    void shouldCalculateSnapshotUsagePercentage() {
        // 80 com snapshot, 20 sem → 80%
        SnapshotEfficiencyMetrics m = baseBuilder()
                .reconstructionsWithSnapshot(80L)
                .reconstructionsWithoutSnapshot(20L)
                .build();

        assertThat(m.getSnapshotUsagePercentage()).isCloseTo(80.0, within(0.001));
    }

    @Test
    @DisplayName("Deve retornar 0 quando não há reconstruções")
    void shouldReturnZeroUsageWhenNoReconstructions() {
        SnapshotEfficiencyMetrics m = baseBuilder()
                .reconstructionsWithSnapshot(0L)
                .reconstructionsWithoutSnapshot(0L)
                .build();

        assertThat(m.getSnapshotUsagePercentage()).isEqualTo(0.0);
    }

    // =========================================================================
    // areSnapshotsEffective()
    // =========================================================================

    @Test
    @DisplayName("Deve retornar true quando melhoria >= 1.5x")
    void shouldReturnTrueWhenEffective() {
        SnapshotEfficiencyMetrics m = baseBuilder()
                .averageReconstructionTimeWithSnapshot(100.0)
                .averageReconstructionTimeWithoutSnapshot(300.0) // 3x
                .build();

        assertThat(m.areSnapshotsEffective()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando melhoria < 1.5x")
    void shouldReturnFalseWhenNotEffective() {
        SnapshotEfficiencyMetrics m = baseBuilder()
                .averageReconstructionTimeWithSnapshot(100.0)
                .averageReconstructionTimeWithoutSnapshot(120.0) // 1.2x
                .build();

        assertThat(m.areSnapshotsEffective()).isFalse();
    }

    // =========================================================================
    // getTotalTimeSaved()
    // =========================================================================

    @Test
    @DisplayName("Deve calcular tempo total economizado")
    void shouldCalculateTotalTimeSaved() {
        // without=500, with=100, 80 reconstruções → saved=(500-100)*80=32000
        SnapshotEfficiencyMetrics m = baseBuilder()
                .averageReconstructionTimeWithSnapshot(100.0)
                .averageReconstructionTimeWithoutSnapshot(500.0)
                .reconstructionsWithSnapshot(80L)
                .build();

        assertThat(m.getTotalTimeSaved()).isCloseTo(32000.0, within(0.001));
    }

    // =========================================================================
    // getAnalysisPeriodDays() e getReconstructionFrequency()
    // =========================================================================

    @Test
    @DisplayName("Deve calcular dias do período de análise")
    void shouldCalculateAnalysisPeriodDays() {
        SnapshotEfficiencyMetrics m = baseBuilder().build(); // 10 dias

        assertThat(m.getAnalysisPeriodDays()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve calcular frequência de reconstruções por dia")
    void shouldCalculateReconstructionFrequency() {
        // total=100 reconstruções, 10 dias → 10/dia
        SnapshotEfficiencyMetrics m = baseBuilder()
                .reconstructionsWithSnapshot(80L)
                .reconstructionsWithoutSnapshot(20L)
                .build();

        assertThat(m.getReconstructionFrequency()).isCloseTo(10.0, within(0.001));
    }

    @Test
    @DisplayName("Deve retornar total quando período é zero dias")
    void shouldReturnTotalWhenPeriodIsZero() {
        SnapshotEfficiencyMetrics m = baseBuilder()
                .periodStart(PERIOD_START)
                .periodEnd(PERIOD_START) // mesmo instante → 0 dias
                .reconstructionsWithSnapshot(5L)
                .reconstructionsWithoutSnapshot(3L)
                .build();

        // 0 dias → retorna getTotalReconstructions()
        assertThat(m.getReconstructionFrequency()).isEqualTo(8.0);
    }

    // =========================================================================
    // getRecommendation()
    // =========================================================================

    @Nested
    @DisplayName("getRecommendation()")
    class Recommendation {

        @Test
        @DisplayName("Deve recomendar habilitar snapshots quando nenhum foi criado")
        void shouldRecommendEnableWhenNoSnapshots() {
            SnapshotEfficiencyMetrics m = baseBuilder()
                    .snapshotsCreated(0L)
                    .snapshotsFailed(0L)
                    .build();

            assertThat(m.getRecommendation()).contains("Nenhum snapshot");
        }

        @Test
        @DisplayName("Deve recomendar verificar logs quando taxa de falha é alta")
        void shouldRecommendCheckLogsWhenHighFailureRate() {
            // taxa de sucesso = 5/10 = 0.5 < 0.9
            SnapshotEfficiencyMetrics m = baseBuilder()
                    .snapshotsCreated(5L)
                    .snapshotsFailed(5L)
                    .build();

            assertThat(m.getRecommendation()).contains("Taxa de falha");
        }

        @Test
        @DisplayName("Deve recomendar ajustar threshold quando snapshots não são efetivos")
        void shouldRecommendAdjustThresholdWhenNotEffective() {
            SnapshotEfficiencyMetrics m = baseBuilder()
                    .snapshotsCreated(10L)
                    .snapshotsFailed(0L)
                    .averageReconstructionTimeWithSnapshot(100.0)
                    .averageReconstructionTimeWithoutSnapshot(120.0) // 1.2x < 1.5x
                    .reconstructionsWithSnapshot(80L)
                    .reconstructionsWithoutSnapshot(20L)
                    .averageCompressionRatio(0.3)
                    .build();

            assertThat(m.getRecommendation()).contains("threshold");
        }

        @Test
        @DisplayName("Deve retornar mensagem de funcionamento adequado quando tudo está bem")
        void shouldReturnAdequateWhenEverythingIsOk() {
            SnapshotEfficiencyMetrics m = baseBuilder()
                    .snapshotsCreated(10L)
                    .snapshotsFailed(0L)
                    .averageReconstructionTimeWithSnapshot(100.0)
                    .averageReconstructionTimeWithoutSnapshot(500.0) // 5x
                    .reconstructionsWithSnapshot(80L)
                    .reconstructionsWithoutSnapshot(10L) // 80/90 ≈ 89% > 50%
                    .averageCompressionRatio(0.4)        // ≥ 0.2
                    .build();

            assertThat(m.getRecommendation()).contains("adequadamente");
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private SnapshotEfficiencyMetrics.SnapshotEfficiencyMetricsBuilder baseBuilder() {
        return SnapshotEfficiencyMetrics.builder()
                .aggregateId("agg-1")
                .periodStart(PERIOD_START)
                .periodEnd(PERIOD_END)
                .reconstructionsWithSnapshot(80L)
                .reconstructionsWithoutSnapshot(20L)
                .averageReconstructionTimeWithSnapshot(100.0)
                .averageReconstructionTimeWithoutSnapshot(500.0)
                .averageEventsWithSnapshot(10.0)
                .averageEventsWithoutSnapshot(200.0)
                .totalSpaceSaved(1024L)
                .snapshotsCreated(10L)
                .snapshotsFailed(1L)
                .averageSnapshotCreationTime(50.0)
                .averageCompressionRatio(0.3);
    }
}
