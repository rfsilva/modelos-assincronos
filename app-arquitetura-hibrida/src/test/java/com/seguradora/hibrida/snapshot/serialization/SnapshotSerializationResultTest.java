package com.seguradora.hibrida.snapshot.serialization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Testes unitários para {@link SnapshotSerializationResult}.
 */
@DisplayName("SnapshotSerializationResult Tests")
class SnapshotSerializationResultTest {

    // =========================================================================
    // Builder e campos básicos
    // =========================================================================

    @Test
    @DisplayName("Deve construir instância via builder e retornar campos corretamente")
    void shouldBuildAndReturnFields() {
        SnapshotSerializationResult result = baseBuilder().build();

        assertThat(result.getSerializedData()).isEqualTo("{\"key\":\"value\"}");
        assertThat(result.isCompressed()).isTrue();
        assertThat(result.getCompressionAlgorithm()).isEqualTo("GZIP");
        assertThat(result.getOriginalSize()).isEqualTo(1000);
        assertThat(result.getCompressedSize()).isEqualTo(600);
        assertThat(result.getDataHash()).isEqualTo("sha256hash");
        assertThat(result.getSerializationTimeMs()).isEqualTo(10L);
        assertThat(result.getCompressionTimeMs()).isEqualTo(5L);
    }

    // =========================================================================
    // getCompressionRatio()
    // =========================================================================

    @Nested
    @DisplayName("getCompressionRatio()")
    class CompressionRatio {

        @Test
        @DisplayName("Deve calcular taxa de compressão corretamente")
        void shouldCalculateRatio() {
            // original=1000, compressed=600 → ratio = 0.4
            SnapshotSerializationResult r = baseBuilder().build();

            assertThat(r.getCompressionRatio()).isCloseTo(0.4, within(0.001));
        }

        @Test
        @DisplayName("Deve retornar 0 quando não comprimido")
        void shouldReturnZeroWhenNotCompressed() {
            SnapshotSerializationResult r = baseBuilder().compressed(false).build();
            assertThat(r.getCompressionRatio()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Deve retornar 0 quando originalSize é zero")
        void shouldReturnZeroWhenOriginalSizeIsZero() {
            SnapshotSerializationResult r = baseBuilder().originalSize(0).build();
            assertThat(r.getCompressionRatio()).isEqualTo(0.0);
        }
    }

    // =========================================================================
    // getSpaceSaved()
    // =========================================================================

    @Test
    @DisplayName("Deve calcular espaço economizado corretamente")
    void shouldCalculateSpaceSaved() {
        // original=1000, compressed=600 → saved=400
        SnapshotSerializationResult r = baseBuilder().build();

        assertThat(r.getSpaceSaved()).isEqualTo(400);
    }

    @Test
    @DisplayName("Deve retornar 0 quando não comprimido")
    void shouldReturnZeroSpaceSavedWhenNotCompressed() {
        SnapshotSerializationResult r = baseBuilder().compressed(false).build();

        assertThat(r.getSpaceSaved()).isEqualTo(0);
    }

    // =========================================================================
    // isCompressionEffective()
    // =========================================================================

    @Test
    @DisplayName("Deve retornar true quando ratio >= 10%")
    void shouldReturnTrueWhenEffective() {
        // ratio = 40% >= 10%
        SnapshotSerializationResult r = baseBuilder().build();
        assertThat(r.isCompressionEffective()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não comprimido")
    void shouldReturnFalseWhenNotCompressed() {
        SnapshotSerializationResult r = baseBuilder().compressed(false).build();
        assertThat(r.isCompressionEffective()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando ratio < 10%")
    void shouldReturnFalseWhenRatioBelow10() {
        // original=1000, compressed=950 → ratio = 5%
        SnapshotSerializationResult r = baseBuilder()
                .originalSize(1000)
                .compressedSize(950)
                .build();

        assertThat(r.isCompressionEffective()).isFalse();
    }

    // =========================================================================
    // getEffectiveSize()
    // =========================================================================

    @Test
    @DisplayName("Deve retornar compressedSize quando comprimido")
    void shouldReturnCompressedSizeWhenCompressed() {
        SnapshotSerializationResult r = baseBuilder().build();
        assertThat(r.getEffectiveSize()).isEqualTo(600);
    }

    @Test
    @DisplayName("Deve retornar originalSize quando não comprimido")
    void shouldReturnOriginalSizeWhenNotCompressed() {
        SnapshotSerializationResult r = baseBuilder().compressed(false).build();
        assertThat(r.getEffectiveSize()).isEqualTo(1000);
    }

    // =========================================================================
    // getTotalProcessingTime()
    // =========================================================================

    @Test
    @DisplayName("Deve somar serializationTimeMs + compressionTimeMs")
    void shouldSumProcessingTimes() {
        // serialization=10, compression=5 → total=15
        SnapshotSerializationResult r = baseBuilder().build();
        assertThat(r.getTotalProcessingTime()).isEqualTo(15L);
    }

    // =========================================================================
    // getCompressionThroughput()
    // =========================================================================

    @Test
    @DisplayName("Deve calcular throughput de compressão em bytes/s")
    void shouldCalculateCompressionThroughput() {
        // original=1000, compressionTimeMs=5 → throughput = (1000/5) * 1000 = 200000
        SnapshotSerializationResult r = baseBuilder().build();

        assertThat(r.getCompressionThroughput()).isCloseTo(200_000.0, within(1.0));
    }

    @Test
    @DisplayName("Deve retornar 0 quando não comprimido")
    void shouldReturnZeroThroughputWhenNotCompressed() {
        SnapshotSerializationResult r = baseBuilder().compressed(false).build();
        assertThat(r.getCompressionThroughput()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Deve retornar 0 quando compressionTimeMs é zero")
    void shouldReturnZeroThroughputWhenCompressionTimeIsZero() {
        SnapshotSerializationResult r = baseBuilder().compressionTimeMs(0L).build();
        assertThat(r.getCompressionThroughput()).isEqualTo(0.0);
    }

    // =========================================================================
    // hasIntegrityHash()
    // =========================================================================

    @Test
    @DisplayName("Deve retornar true quando dataHash não é nulo")
    void shouldReturnTrueWhenHashIsNotNull() {
        assertThat(baseBuilder().build().hasIntegrityHash()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando dataHash é null")
    void shouldReturnFalseWhenHashIsNull() {
        SnapshotSerializationResult r = baseBuilder().dataHash(null).build();
        assertThat(r.hasIntegrityHash()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando dataHash é string em branco")
    void shouldReturnFalseWhenHashIsBlank() {
        SnapshotSerializationResult r = baseBuilder().dataHash("  ").build();
        assertThat(r.hasIntegrityHash()).isFalse();
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private SnapshotSerializationResult.SnapshotSerializationResultBuilder baseBuilder() {
        return SnapshotSerializationResult.builder()
                .serializedData("{\"key\":\"value\"}")
                .compressed(true)
                .compressionAlgorithm("GZIP")
                .originalSize(1000)
                .compressedSize(600)
                .dataHash("sha256hash")
                .serializationTimeMs(10L)
                .compressionTimeMs(5L);
    }
}
