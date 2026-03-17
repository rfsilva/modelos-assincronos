package com.seguradora.hibrida.eventstore.partition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link PartitionStatistics}.
 */
@DisplayName("PartitionStatistics Tests")
class PartitionStatisticsTest {

    // =========================================================================
    // getDataDensity
    // =========================================================================

    @Nested
    @DisplayName("getDataDensity()")
    class GetDataDensity {

        @Test
        @DisplayName("Deve retornar 0.0 quando sizeBytes é null")
        void shouldReturnZeroWhenSizeBytesIsNull() {
            PartitionStatistics stats = PartitionStatistics.builder()
                    .partitionName("events_2024_01")
                    .rowCount(100L)
                    .sizeBytes(null)
                    .build();

            assertThat(stats.getDataDensity()).isZero();
        }

        @Test
        @DisplayName("Deve retornar 0.0 quando sizeBytes é 0")
        void shouldReturnZeroWhenSizeBytesIsZero() {
            PartitionStatistics stats = PartitionStatistics.builder()
                    .rowCount(100L)
                    .sizeBytes(0L)
                    .build();

            assertThat(stats.getDataDensity()).isZero();
        }

        @Test
        @DisplayName("Deve calcular density corretamente quando há dados")
        void shouldCalculateDensityWhenDataPresent() {
            PartitionStatistics stats = PartitionStatistics.builder()
                    .rowCount(1024L)
                    .sizeBytes(1024L * 1024L) // 1 MB
                    .build();

            // 1024 rows / 1 MB = 1024 rows/MB
            assertThat(stats.getDataDensity()).isEqualTo(1024.0);
        }
    }

    // =========================================================================
    // isEmpty
    // =========================================================================

    @Nested
    @DisplayName("isEmpty()")
    class IsEmpty {

        @Test
        @DisplayName("Deve retornar true quando rowCount é null")
        void shouldReturnTrueWhenRowCountIsNull() {
            PartitionStatistics stats = PartitionStatistics.builder().rowCount(null).build();
            assertThat(stats.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar true quando rowCount é 0")
        void shouldReturnTrueWhenRowCountIsZero() {
            PartitionStatistics stats = PartitionStatistics.builder().rowCount(0L).build();
            assertThat(stats.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando rowCount > 0")
        void shouldReturnFalseWhenRowCountPositive() {
            PartitionStatistics stats = PartitionStatistics.builder().rowCount(50L).build();
            assertThat(stats.isEmpty()).isFalse();
        }
    }

    // =========================================================================
    // isLarge
    // =========================================================================

    @Test
    @DisplayName("isLarge() deve retornar true quando sizeBytes > 100MB")
    void isLargeShouldReturnTrueWhenSizeAbove100MB() {
        PartitionStatistics stats = PartitionStatistics.builder()
                .sizeBytes(101L * 1024L * 1024L) // 101 MB
                .build();

        assertThat(stats.isLarge()).isTrue();
    }

    @Test
    @DisplayName("isLarge() deve retornar false quando sizeBytes <= 100MB")
    void isLargeShouldReturnFalseWhenSizeNotAbove100MB() {
        PartitionStatistics stats = PartitionStatistics.builder()
                .sizeBytes(50L * 1024L * 1024L) // 50 MB
                .build();

        assertThat(stats.isLarge()).isFalse();
    }

    // =========================================================================
    // getYearMonth
    // =========================================================================

    @Nested
    @DisplayName("getYearMonth()")
    class GetYearMonth {

        @Test
        @DisplayName("Deve extrair ano-mês do nome da partição events_2024_01")
        void shouldExtractYearMonthFromPartitionName() {
            PartitionStatistics stats = PartitionStatistics.builder()
                    .partitionName("events_2024_01")
                    .build();

            assertThat(stats.getYearMonth()).isEqualTo("2024-01");
        }

        @Test
        @DisplayName("Deve retornar null quando partitionName é null")
        void shouldReturnNullWhenPartitionNameIsNull() {
            PartitionStatistics stats = PartitionStatistics.builder().partitionName(null).build();
            assertThat(stats.getYearMonth()).isNull();
        }

        @Test
        @DisplayName("Deve retornar null quando partitionName não contém underscore")
        void shouldReturnNullWhenNoUnderscore() {
            PartitionStatistics stats = PartitionStatistics.builder().partitionName("events").build();
            assertThat(stats.getYearMonth()).isNull();
        }
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString() deve retornar String não nula")
    void toStringShouldReturnNonNull() {
        PartitionStatistics stats = PartitionStatistics.builder()
                .partitionName("events_2024_01")
                .rowCount(100L)
                .sizePretty("1.2 MB")
                .build();

        assertThat(stats.toString()).isNotNull().isNotBlank().contains("events_2024_01");
    }
}
