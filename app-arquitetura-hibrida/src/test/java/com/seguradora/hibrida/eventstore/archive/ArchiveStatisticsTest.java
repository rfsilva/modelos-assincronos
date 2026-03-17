package com.seguradora.hibrida.eventstore.archive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ArchiveStatistics}.
 */
@DisplayName("ArchiveStatistics Tests")
class ArchiveStatisticsTest {

    // =========================================================================
    // getAverageArchiveSize
    // =========================================================================

    @Nested
    @DisplayName("getAverageArchiveSize()")
    class GetAverageArchiveSize {

        @Test
        @DisplayName("Deve retornar 0.0 quando totalArchives é null")
        void shouldReturnZeroWhenTotalArchivesIsNull() {
            ArchiveStatistics stats = ArchiveStatistics.builder()
                    .totalArchives(null)
                    .totalSize(1000L)
                    .build();

            assertThat(stats.getAverageArchiveSize()).isZero();
        }

        @Test
        @DisplayName("Deve retornar 0.0 quando totalArchives é 0")
        void shouldReturnZeroWhenTotalArchivesIsZero() {
            ArchiveStatistics stats = ArchiveStatistics.builder()
                    .totalArchives(0L)
                    .totalSize(1000L)
                    .build();

            assertThat(stats.getAverageArchiveSize()).isZero();
        }

        @Test
        @DisplayName("Deve calcular média corretamente")
        void shouldCalculateAverageCorrectly() {
            ArchiveStatistics stats = ArchiveStatistics.builder()
                    .totalArchives(4L)
                    .totalSize(200L)
                    .build();

            assertThat(stats.getAverageArchiveSize()).isEqualTo(50.0);
        }
    }

    // =========================================================================
    // getAverageEventsPerArchive
    // =========================================================================

    @Test
    @DisplayName("getAverageEventsPerArchive deve calcular corretamente")
    void getAverageEventsPerArchiveShouldCalculateCorrectly() {
        ArchiveStatistics stats = ArchiveStatistics.builder()
                .totalArchives(2L)
                .totalEvents(1000L)
                .build();

        assertThat(stats.getAverageEventsPerArchive()).isEqualTo(500.0);
    }

    // =========================================================================
    // getFormattedSize
    // =========================================================================

    @Nested
    @DisplayName("getFormattedSize()")
    class GetFormattedSize {

        @Test
        @DisplayName("Deve retornar '0 B' quando totalSize é null")
        void shouldReturnZeroWhenTotalSizeIsNull() {
            ArchiveStatistics stats = ArchiveStatistics.builder().totalSize(null).build();
            assertThat(stats.getFormattedSize()).isEqualTo("0 B");
        }

        @Test
        @DisplayName("Deve retornar '0 B' quando totalSize é 0")
        void shouldReturnZeroWhenTotalSizeIsZero() {
            ArchiveStatistics stats = ArchiveStatistics.builder().totalSize(0L).build();
            assertThat(stats.getFormattedSize()).isEqualTo("0 B");
        }

        @Test
        @DisplayName("Deve formatar bytes corretamente")
        void shouldFormatBytesCorrectly() {
            ArchiveStatistics stats = ArchiveStatistics.builder().totalSize(500L).build();
            assertThat(stats.getFormattedSize()).contains("B");
        }

        @Test
        @DisplayName("Deve formatar KB corretamente")
        void shouldFormatKBCorrectly() {
            ArchiveStatistics stats = ArchiveStatistics.builder().totalSize(2048L).build();
            assertThat(stats.getFormattedSize()).contains("KB");
        }

        @Test
        @DisplayName("Deve formatar MB corretamente")
        void shouldFormatMBCorrectly() {
            ArchiveStatistics stats = ArchiveStatistics.builder().totalSize(2L * 1024L * 1024L).build();
            assertThat(stats.getFormattedSize()).contains("MB");
        }
    }
}
