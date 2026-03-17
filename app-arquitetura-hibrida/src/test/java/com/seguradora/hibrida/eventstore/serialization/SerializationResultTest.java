package com.seguradora.hibrida.eventstore.serialization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link SerializationResult}.
 */
@DisplayName("SerializationResult Tests")
class SerializationResultTest {

    // =========================================================================
    // Builder
    // =========================================================================

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("Builder deve criar instância com campos corretos")
        void builderShouldCreateInstanceWithCorrectFields() {
            SerializationResult result = SerializationResult.builder()
                    .data("{\"key\":\"value\"}")
                    .compressed(true)
                    .originalSize(100)
                    .finalSize(40)
                    .compressionAlgorithm("GZIP")
                    .build();

            assertThat(result.getData()).isEqualTo("{\"key\":\"value\"}");
            assertThat(result.isCompressed()).isTrue();
            assertThat(result.getOriginalSize()).isEqualTo(100);
            assertThat(result.getFinalSize()).isEqualTo(40);
            assertThat(result.getCompressionAlgorithm()).isEqualTo("GZIP");
        }
    }

    // =========================================================================
    // getCompressionRatio
    // =========================================================================

    @Nested
    @DisplayName("getCompressionRatio()")
    class GetCompressionRatio {

        @Test
        @DisplayName("Deve retornar 0.0 quando originalSize é 0")
        void shouldReturnZeroWhenOriginalSizeIsZero() {
            SerializationResult result = SerializationResult.builder()
                    .originalSize(0)
                    .finalSize(0)
                    .build();

            assertThat(result.getCompressionRatio()).isZero();
        }

        @Test
        @DisplayName("Deve calcular corretamente: 1 - (finalSize / originalSize)")
        void shouldCalculateCompressionRatioCorrectly() {
            SerializationResult result = SerializationResult.builder()
                    .originalSize(100)
                    .finalSize(40)
                    .build();

            // 1.0 - (40/100) = 0.6
            assertThat(result.getCompressionRatio()).isEqualTo(0.6);
        }
    }

    // =========================================================================
    // getSpaceSaved
    // =========================================================================

    @Test
    @DisplayName("getSpaceSaved() deve retornar originalSize - finalSize")
    void getSpaceSavedShouldReturnDifference() {
        SerializationResult result = SerializationResult.builder()
                .originalSize(200)
                .finalSize(80)
                .build();

        assertThat(result.getSpaceSaved()).isEqualTo(120);
    }

    // =========================================================================
    // isCompressionEffective
    // =========================================================================

    @Nested
    @DisplayName("isCompressionEffective()")
    class IsCompressionEffective {

        @Test
        @DisplayName("Deve retornar true quando compressed=true e finalSize < originalSize")
        void shouldReturnTrueWhenCompressedAndSmaller() {
            SerializationResult result = SerializationResult.builder()
                    .compressed(true)
                    .originalSize(100)
                    .finalSize(50)
                    .build();

            assertThat(result.isCompressionEffective()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando compressed=false")
        void shouldReturnFalseWhenNotCompressed() {
            SerializationResult result = SerializationResult.builder()
                    .compressed(false)
                    .originalSize(100)
                    .finalSize(50)
                    .build();

            assertThat(result.isCompressionEffective()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando finalSize >= originalSize")
        void shouldReturnFalseWhenFinalSizeNotSmaller() {
            SerializationResult result = SerializationResult.builder()
                    .compressed(true)
                    .originalSize(100)
                    .finalSize(100)
                    .build();

            assertThat(result.isCompressionEffective()).isFalse();
        }
    }
}
