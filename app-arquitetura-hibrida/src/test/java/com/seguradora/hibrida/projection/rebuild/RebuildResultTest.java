package com.seguradora.hibrida.projection.rebuild;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link RebuildResult}.
 */
@DisplayName("RebuildResult Tests")
class RebuildResultTest {

    // =========================================================================
    // Factory methods
    // =========================================================================

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("success() deve criar resultado com status SUCCESS")
        void successShouldCreateResultWithSuccessStatus() {
            RebuildResult result = RebuildResult.success("proj", RebuildType.FULL, 100L, 0L, 5000L);

            assertThat(result.status()).isEqualTo(RebuildStatus.SUCCESS);
            assertThat(result.projectionName()).isEqualTo("proj");
            assertThat(result.rebuildType()).isEqualTo(RebuildType.FULL);
            assertThat(result.eventsProcessed()).isEqualTo(100L);
            assertThat(result.eventsFailed()).isZero();
            assertThat(result.durationMs()).isEqualTo(5000L);
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("failure() deve criar resultado com status FAILED e mensagem de erro")
        void failureShouldCreateResultWithFailedStatusAndErrorMessage() {
            RebuildResult result = RebuildResult.failure("proj", RebuildType.INCREMENTAL,
                    50L, 10L, 2000L, "DB error");

            assertThat(result.status()).isEqualTo(RebuildStatus.FAILED);
            assertThat(result.errorMessage()).isEqualTo("DB error");
        }
    }

    // =========================================================================
    // Verificações de status
    // =========================================================================

    @Nested
    @DisplayName("Verificações de status")
    class VerificacoesStatus {

        @Test
        @DisplayName("isSuccess() deve retornar true para status SUCCESS")
        void isSuccessShouldReturnTrueForSuccessStatus() {
            RebuildResult result = RebuildResult.success("proj", RebuildType.FULL, 100L, 0L, 5000L);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.hasFailed()).isFalse();
        }

        @Test
        @DisplayName("hasFailed() deve retornar true para status FAILED")
        void hasFailedShouldReturnTrueForFailedStatus() {
            RebuildResult result = RebuildResult.failure("proj", RebuildType.FULL, 0L, 1L, 1000L, "err");
            assertThat(result.hasFailed()).isTrue();
            assertThat(result.isSuccess()).isFalse();
        }
    }

    // =========================================================================
    // Métricas calculadas
    // =========================================================================

    @Nested
    @DisplayName("Métricas calculadas")
    class MetricasCalculadas {

        @Test
        @DisplayName("getSuccessRate() deve retornar 1.0 quando não há falhas")
        void getSuccessRateShouldReturnOneWhenNoFailures() {
            RebuildResult result = RebuildResult.success("proj", RebuildType.FULL, 100L, 0L, 5000L);
            assertThat(result.getSuccessRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("getSuccessRate() deve retornar 1.0 quando total=0")
        void getSuccessRateShouldReturnOneWhenTotalIsZero() {
            RebuildResult result = RebuildResult.success("proj", RebuildType.FULL, 0L, 0L, 0L);
            assertThat(result.getSuccessRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("getSuccessRate() deve calcular corretamente")
        void getSuccessRateShouldCalculateCorrectly() {
            RebuildResult result = new RebuildResult("proj", RebuildType.FULL,
                    RebuildStatus.SUCCESS, 80L, 20L, 5000L, null);
            // 80 / (80 + 20) = 0.8
            assertThat(result.getSuccessRate()).isEqualTo(0.8);
        }

        @Test
        @DisplayName("getThroughput() deve retornar 0 quando durationMs=0")
        void getThroughputShouldReturnZeroWhenDurationIsZero() {
            RebuildResult result = RebuildResult.success("proj", RebuildType.FULL, 100L, 0L, 0L);
            assertThat(result.getThroughput()).isZero();
        }

        @Test
        @DisplayName("getThroughput() deve calcular corretamente")
        void getThroughputShouldCalculateCorrectly() {
            // 100 events in 1000ms = 100 events/sec
            RebuildResult result = RebuildResult.success("proj", RebuildType.FULL, 100L, 0L, 1000L);
            assertThat(result.getThroughput()).isCloseTo(100.0, org.assertj.core.data.Offset.offset(0.1));
        }
    }

    // =========================================================================
    // getTimestamp / toString
    // =========================================================================

    @Test
    @DisplayName("getTimestamp() deve retornar Instant não nulo")
    void getTimestampShouldReturnNonNull() {
        RebuildResult result = RebuildResult.success("proj", RebuildType.FULL, 100L, 0L, 5000L);
        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("toString() deve retornar String não nula com nome da projeção")
    void toStringShouldReturnNonNull() {
        RebuildResult result = RebuildResult.success("MyProjection", RebuildType.FULL, 100L, 0L, 5000L);
        assertThat(result.toString()).isNotNull().contains("MyProjection");
    }
}
