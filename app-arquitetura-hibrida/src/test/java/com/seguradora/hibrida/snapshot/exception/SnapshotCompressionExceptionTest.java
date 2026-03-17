package com.seguradora.hibrida.snapshot.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link SnapshotCompressionException}.
 */
@DisplayName("SnapshotCompressionException Tests")
class SnapshotCompressionExceptionTest {

    // =========================================================================
    // Hierarquia
    // =========================================================================

    @Test
    @DisplayName("Deve herdar de SnapshotException")
    void shouldExtendSnapshotException() {
        SnapshotCompressionException ex = new SnapshotCompressionException("msg", "GZIP");
        assertThat(ex).isInstanceOf(SnapshotException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    // =========================================================================
    // Construtor básico (message, algorithm)
    // =========================================================================

    @Nested
    @DisplayName("Construtor (message, compressionAlgorithm)")
    class ConstrutorBasico {

        @Test
        @DisplayName("Deve armazenar compressionAlgorithm")
        void shouldStoreAlgorithm() {
            SnapshotCompressionException ex = new SnapshotCompressionException("Erro", "GZIP");
            assertThat(ex.getCompressionAlgorithm()).isEqualTo("GZIP");
        }

        @Test
        @DisplayName("Deve retornar originalSize = -1")
        void shouldReturnMinusOneForOriginalSize() {
            SnapshotCompressionException ex = new SnapshotCompressionException("Erro", "GZIP");
            assertThat(ex.getOriginalSize()).isEqualTo(-1);
        }

        @Test
        @DisplayName("getMessage() deve incluir sufixo com algoritmo")
        void shouldIncludeAlgorithmInMessage() {
            SnapshotCompressionException ex = new SnapshotCompressionException("Falhou", "LZ4");
            assertThat(ex.getMessage()).contains("Falhou");
            assertThat(ex.getMessage()).contains("[algorithm=LZ4]");
        }
    }

    // =========================================================================
    // Construtor com causa (message, cause, algorithm)
    // =========================================================================

    @Nested
    @DisplayName("Construtor (message, cause, compressionAlgorithm)")
    class ConstrutorComCausa {

        @Test
        @DisplayName("Deve armazenar a causa")
        void shouldStoreCause() {
            RuntimeException cause = new RuntimeException("IO error");
            SnapshotCompressionException ex = new SnapshotCompressionException("Erro", cause, "GZIP");

            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Deve retornar originalSize = -1")
        void shouldReturnMinusOneForOriginalSize() {
            SnapshotCompressionException ex = new SnapshotCompressionException("Erro", new RuntimeException(), "GZIP");
            assertThat(ex.getOriginalSize()).isEqualTo(-1);
        }
    }

    // =========================================================================
    // Construtor completo (message, cause, algorithm, aggregateId, originalSize)
    // =========================================================================

    @Nested
    @DisplayName("Construtor completo")
    class ConstrutorCompleto {

        @Test
        @DisplayName("Deve armazenar originalSize")
        void shouldStoreOriginalSize() {
            SnapshotCompressionException ex = new SnapshotCompressionException(
                    "Falhou", new RuntimeException(), "GZIP", "agg-1", 2048);

            assertThat(ex.getOriginalSize()).isEqualTo(2048);
        }

        @Test
        @DisplayName("Deve armazenar aggregateId via SnapshotException")
        void shouldStoreAggregateId() {
            SnapshotCompressionException ex = new SnapshotCompressionException(
                    "Falhou", new RuntimeException(), "GZIP", "agg-42", 1024);

            assertThat(ex.getAggregateId()).isEqualTo("agg-42");
        }

        @Test
        @DisplayName("getMessage() deve incluir algoritmo e originalSize quando > 0")
        void shouldIncludeAlgorithmAndOriginalSizeInMessage() {
            SnapshotCompressionException ex = new SnapshotCompressionException(
                    "Erro", new RuntimeException(), "GZIP", "agg-1", 512);

            String msg = ex.getMessage();
            assertThat(msg).contains("[algorithm=GZIP");
            assertThat(msg).contains("originalSize=512 bytes]");
        }

        @Test
        @DisplayName("getMessage() não deve incluir originalSize quando <= 0")
        void shouldNotIncludeOriginalSizeWhenZeroOrNegative() {
            // Construtor (message, algorithm) usa originalSize = -1
            SnapshotCompressionException ex = new SnapshotCompressionException("Erro", "GZIP");

            assertThat(ex.getMessage()).doesNotContain("originalSize");
        }
    }

    // =========================================================================
    // getMessage() quando algorithm é null
    // =========================================================================

    @Test
    @DisplayName("getMessage() não deve acrescentar sufixo quando algorithm é null")
    void shouldNotAddSuffixWhenAlgorithmIsNull() {
        // Força null via construtor da superclasse (workaround)
        SnapshotCompressionException ex = new SnapshotCompressionException("Mensagem", (String) null);
        // algorithm = null → sufixo não é adicionado
        assertThat(ex.getMessage()).isEqualTo("Mensagem");
    }
}
