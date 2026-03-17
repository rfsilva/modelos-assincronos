package com.seguradora.hibrida.snapshot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Testes unitários para {@link SnapshotStatistics}.
 */
@DisplayName("SnapshotStatistics Tests")
class SnapshotStatisticsTest {

    private static final Instant OLDEST = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant NEWEST = Instant.parse("2026-01-11T00:00:00Z"); // 10 dias depois

    // =========================================================================
    // Builder e campos básicos
    // =========================================================================

    @Test
    @DisplayName("Deve construir instância com builder e retornar campos corretamente")
    void shouldBuildAndReturnFields() {
        SnapshotStatistics s = baseBuilder().build();

        assertThat(s.getAggregateId()).isEqualTo("agg-1");
        assertThat(s.getTotalSnapshots()).isEqualTo(10L);
        assertThat(s.getCompressedSnapshots()).isEqualTo(8L);
        assertThat(s.getTotalOriginalSize()).isEqualTo(10_000L);
        assertThat(s.getTotalCompressedSize()).isEqualTo(6_000L);
        assertThat(s.getOldestSnapshot()).isEqualTo(OLDEST);
        assertThat(s.getNewestSnapshot()).isEqualTo(NEWEST);
    }

    // =========================================================================
    // getOverallCompressionRatio()
    // =========================================================================

    @Nested
    @DisplayName("getOverallCompressionRatio()")
    class OverallCompressionRatio {

        @Test
        @DisplayName("Deve calcular taxa de compressão geral corretamente")
        void shouldCalculateOverallRatio() {
            // original=10000, compressed=6000 → ratio = 1 - 6000/10000 = 0.4
            SnapshotStatistics s = baseBuilder().build();

            assertThat(s.getOverallCompressionRatio()).isCloseTo(0.4, within(0.001));
        }

        @Test
        @DisplayName("Deve retornar 0 quando tamanho original é zero")
        void shouldReturnZeroWhenOriginalSizeIsZero() {
            SnapshotStatistics s = baseBuilder()
                    .totalOriginalSize(0L)
                    .totalCompressedSize(0L)
                    .build();

            assertThat(s.getOverallCompressionRatio()).isEqualTo(0.0);
        }
    }

    // =========================================================================
    // getCompressionPercentage()
    // =========================================================================

    @Test
    @DisplayName("Deve calcular porcentagem de snapshots comprimidos")
    void shouldCalculateCompressionPercentage() {
        // 8 de 10 → 80%
        SnapshotStatistics s = baseBuilder().build();

        assertThat(s.getCompressionPercentage()).isCloseTo(80.0, within(0.001));
    }

    @Test
    @DisplayName("Deve retornar 0 quando não há snapshots")
    void shouldReturnZeroCompressionPercentageWhenNoSnapshots() {
        SnapshotStatistics s = baseBuilder()
                .totalSnapshots(0L)
                .compressedSnapshots(0L)
                .build();

        assertThat(s.getCompressionPercentage()).isEqualTo(0.0);
    }

    // =========================================================================
    // hasSnapshots()
    // =========================================================================

    @Test
    @DisplayName("Deve retornar true quando há snapshots")
    void shouldReturnTrueWhenHasSnapshots() {
        assertThat(baseBuilder().build().hasSnapshots()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não há snapshots")
    void shouldReturnFalseWhenNoSnapshots() {
        SnapshotStatistics s = baseBuilder().totalSnapshots(0L).build();
        assertThat(s.hasSnapshots()).isFalse();
    }

    // =========================================================================
    // getStorageEfficiency()
    // =========================================================================

    @Test
    @DisplayName("Deve calcular eficiência de armazenamento")
    void shouldCalculateStorageEfficiency() {
        // totalOriginalSize=10000, totalSpaceSaved=4000
        // efficiency = 1 - (10000-4000)/10000 = 1 - 0.6 = 0.4
        SnapshotStatistics s = baseBuilder()
                .totalOriginalSize(10_000L)
                .totalSpaceSaved(4_000L)
                .build();

        assertThat(s.getStorageEfficiency()).isCloseTo(0.4, within(0.001));
    }

    @Test
    @DisplayName("Deve retornar 1.0 quando tamanho original é zero")
    void shouldReturnOneWhenOriginalSizeIsZero() {
        SnapshotStatistics s = baseBuilder()
                .totalOriginalSize(0L)
                .totalSpaceSaved(0L)
                .build();

        assertThat(s.getStorageEfficiency()).isEqualTo(1.0);
    }

    // =========================================================================
    // getSnapshotGrowthRate()
    // =========================================================================

    @Test
    @DisplayName("Deve calcular taxa de crescimento por dia")
    void shouldCalculateGrowthRate() {
        // 10 snapshots em 10 dias → 1.0/dia
        SnapshotStatistics s = baseBuilder().build();

        assertThat(s.getSnapshotGrowthRate()).isCloseTo(1.0, within(0.001));
    }

    @Test
    @DisplayName("Deve retornar 0 quando timestamps são nulos")
    void shouldReturnZeroWhenTimestampsAreNull() {
        SnapshotStatistics s = baseBuilder()
                .oldestSnapshot(null)
                .newestSnapshot(null)
                .build();

        assertThat(s.getSnapshotGrowthRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Deve retornar totalSnapshots quando período é zero dias")
    void shouldReturnTotalWhenPeriodIsZero() {
        SnapshotStatistics s = baseBuilder()
                .oldestSnapshot(OLDEST)
                .newestSnapshot(OLDEST) // mesmo instante
                .totalSnapshots(5L)
                .build();

        assertThat(s.getSnapshotGrowthRate()).isEqualTo(5.0);
    }

    // =========================================================================
    // isCompressionEffective()
    // =========================================================================

    @Test
    @DisplayName("Deve retornar true quando compressão >= 20%")
    void shouldReturnTrueWhenCompressionIsEffective() {
        // original=10000, compressed=7000 → ratio = 0.3 >= 0.2
        SnapshotStatistics s = baseBuilder()
                .totalOriginalSize(10_000L)
                .totalCompressedSize(7_000L)
                .build();

        assertThat(s.isCompressionEffective()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando compressão < 20%")
    void shouldReturnFalseWhenCompressionIsNotEffective() {
        // original=10000, compressed=9000 → ratio = 0.1 < 0.2
        SnapshotStatistics s = baseBuilder()
                .totalOriginalSize(10_000L)
                .totalCompressedSize(9_000L)
                .build();

        assertThat(s.isCompressionEffective()).isFalse();
    }

    // =========================================================================
    // getAverageCompressedSize() e getAverageUncompressedSize()
    // =========================================================================

    @Test
    @DisplayName("Deve calcular tamanho médio dos snapshots comprimidos")
    void shouldCalculateAverageCompressedSize() {
        // totalCompressedSize=6000, compressedSnapshots=8 → 750
        SnapshotStatistics s = baseBuilder().build();

        assertThat(s.getAverageCompressedSize()).isCloseTo(750.0, within(0.001));
    }

    @Test
    @DisplayName("Deve retornar 0 quando não há snapshots comprimidos")
    void shouldReturnZeroAverageCompressedWhenNone() {
        SnapshotStatistics s = baseBuilder().compressedSnapshots(0L).build();

        assertThat(s.getAverageCompressedSize()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Deve calcular tamanho médio dos snapshots não comprimidos")
    void shouldCalculateAverageUncompressedSize() {
        // totalSnapshots=10, compressedSnapshots=8 → uncompressed=2
        // totalOriginalSize=10000, totalCompressedSize=6000 → uncompressedSize=4000
        // avg = 4000/2 = 2000
        SnapshotStatistics s = baseBuilder().build();

        assertThat(s.getAverageUncompressedSize()).isCloseTo(2000.0, within(0.001));
    }

    @Test
    @DisplayName("Deve retornar 0 quando todos os snapshots são comprimidos")
    void shouldReturnZeroAverageUncompressedWhenAllCompressed() {
        SnapshotStatistics s = baseBuilder()
                .totalSnapshots(8L)
                .compressedSnapshots(8L)
                .build();

        assertThat(s.getAverageUncompressedSize()).isEqualTo(0.0);
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private SnapshotStatistics.SnapshotStatisticsBuilder baseBuilder() {
        return SnapshotStatistics.builder()
                .aggregateId("agg-1")
                .aggregateType("SeguradoAggregate")
                .totalSnapshots(10L)
                .compressedSnapshots(8L)
                .totalOriginalSize(10_000L)
                .totalCompressedSize(6_000L)
                .oldestSnapshot(OLDEST)
                .newestSnapshot(NEWEST)
                .latestVersion(50L)
                .averageTimeBetweenSnapshots(86400.0)
                .averageSnapshotSize(1000.0)
                .averageCompressionRatio(0.4)
                .snapshotsLast24Hours(1L)
                .snapshotsLastWeek(5L)
                .totalSpaceSaved(4_000L);
    }
}
