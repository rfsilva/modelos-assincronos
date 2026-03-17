package com.seguradora.hibrida.eventstore.archive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ArchiveResult}.
 */
@DisplayName("ArchiveResult Tests")
class ArchiveResultTest {

    // =========================================================================
    // Factory: success
    // =========================================================================

    @Nested
    @DisplayName("success()")
    class Success {

        @Test
        @DisplayName("Deve criar resultado de sucesso com campos corretos")
        void shouldCreateSuccessResultWithCorrectFields() {
            ArchiveResult result = ArchiveResult.success("events_2022_01", 500L, 25000L);

            assertThat(result.getPartitionName()).isEqualTo("events_2022_01");
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getEventCount()).isEqualTo(500L);
            assertThat(result.getCompressedSize()).isEqualTo(25000L);
            assertThat(result.getTimestamp()).isNotNull();
            assertThat(result.getErrorMessage()).isNull();
        }
    }

    // =========================================================================
    // Factory: error
    // =========================================================================

    @Nested
    @DisplayName("error()")
    class Error {

        @Test
        @DisplayName("Deve criar resultado de erro com campos corretos")
        void shouldCreateErrorResultWithCorrectFields() {
            ArchiveResult result = ArchiveResult.error("events_2022_01", "Partição não encontrada");

            assertThat(result.getPartitionName()).isEqualTo("events_2022_01");
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).isEqualTo("Partição não encontrada");
            assertThat(result.getTimestamp()).isNotNull();
        }
    }

    // =========================================================================
    // getCompressionRatio
    // =========================================================================

    @Nested
    @DisplayName("getCompressionRatio()")
    class GetCompressionRatio {

        @Test
        @DisplayName("Deve retornar 0.0 quando eventCount é null ou 0")
        void shouldReturnZeroWhenEventCountIsNull() {
            ArchiveResult result = ArchiveResult.builder()
                    .eventCount(null)
                    .compressedSize(1000L)
                    .build();

            assertThat(result.getCompressionRatio()).isZero();
        }

        @Test
        @DisplayName("Deve calcular ratio com base em 500 bytes estimados por evento")
        void shouldCalculateRatioBasedOnEstimate() {
            // 100 eventos * 500 bytes = 50000 bytes estimados
            // compressedSize = 25000 → ratio = 1 - (25000/50000) = 0.5
            ArchiveResult result = ArchiveResult.builder()
                    .eventCount(100L)
                    .compressedSize(25000L)
                    .build();

            assertThat(result.getCompressionRatio()).isEqualTo(0.5);
        }
    }
}
