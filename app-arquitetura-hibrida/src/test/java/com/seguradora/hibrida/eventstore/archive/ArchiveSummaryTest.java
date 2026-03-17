package com.seguradora.hibrida.eventstore.archive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ArchiveSummary}.
 */
@DisplayName("ArchiveSummary Tests")
class ArchiveSummaryTest {

    // =========================================================================
    // Construção
    // =========================================================================

    @Test
    @DisplayName("Novo ArchiveSummary deve ter lista de resultados vazia")
    void newSummaryShouldHaveEmptyResults() {
        ArchiveSummary summary = new ArchiveSummary();
        assertThat(summary.getResults()).isEmpty();
    }

    @Test
    @DisplayName("Novo ArchiveSummary deve ter startTime não nulo")
    void newSummaryShouldHaveNonNullStartTime() {
        ArchiveSummary summary = new ArchiveSummary();
        assertThat(summary.getStartTime()).isNotNull();
    }

    // =========================================================================
    // addResult / contadores
    // =========================================================================

    @Nested
    @DisplayName("Contadores de sucesso/erro")
    class Contadores {

        @Test
        @DisplayName("getSuccessCount deve contar apenas resultados de sucesso")
        void getSuccessCountShouldCountOnlySuccessResults() {
            ArchiveSummary summary = new ArchiveSummary();
            summary.addResult(ArchiveResult.success("p1", 100L, 5000L));
            summary.addResult(ArchiveResult.success("p2", 200L, 8000L));
            summary.addResult(ArchiveResult.error("p3", "erro"));

            assertThat(summary.getSuccessCount()).isEqualTo(2L);
        }

        @Test
        @DisplayName("getErrorCount deve contar apenas resultados de erro")
        void getErrorCountShouldCountOnlyErrorResults() {
            ArchiveSummary summary = new ArchiveSummary();
            summary.addResult(ArchiveResult.success("p1", 100L, 5000L));
            summary.addResult(ArchiveResult.error("p2", "erro"));

            assertThat(summary.getErrorCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getTotalEvents deve somar eventos dos resultados com sucesso")
        void getTotalEventsShouldSumSuccessEvents() {
            ArchiveSummary summary = new ArchiveSummary();
            summary.addResult(ArchiveResult.success("p1", 100L, 5000L));
            summary.addResult(ArchiveResult.success("p2", 300L, 8000L));
            summary.addResult(ArchiveResult.error("p3", "erro"));

            assertThat(summary.getTotalEvents()).isEqualTo(400L);
        }
    }

    // =========================================================================
    // getDurationMs
    // =========================================================================

    @Test
    @DisplayName("getDurationMs deve retornar valor positivo após finish()")
    void getDurationMsShouldReturnPositiveValueAfterFinish() throws InterruptedException {
        ArchiveSummary summary = new ArchiveSummary();
        Thread.sleep(5);
        summary.finish();

        assertThat(summary.getDurationMs()).isPositive();
    }

    @Test
    @DisplayName("getDurationMs deve funcionar mesmo sem finish() chamado")
    void getDurationMsShouldWorkWithoutFinish() {
        ArchiveSummary summary = new ArchiveSummary();
        assertThat(summary.getDurationMs()).isGreaterThanOrEqualTo(0L);
    }

    // =========================================================================
    // getSuccessRate
    // =========================================================================

    @Nested
    @DisplayName("getSuccessRate()")
    class GetSuccessRate {

        @Test
        @DisplayName("Deve retornar 0.0 quando results está vazio")
        void shouldReturnZeroWhenResultsEmpty() {
            ArchiveSummary summary = new ArchiveSummary();
            assertThat(summary.getSuccessRate()).isZero();
        }

        @Test
        @DisplayName("Deve calcular taxa de sucesso corretamente")
        void shouldCalculateSuccessRateCorrectly() {
            ArchiveSummary summary = new ArchiveSummary();
            summary.addResult(ArchiveResult.success("p1", 100L, 5000L));
            summary.addResult(ArchiveResult.success("p2", 100L, 5000L));
            summary.addResult(ArchiveResult.error("p3", "erro"));

            // 2 sucesso / 3 total = 0.666...
            assertThat(summary.getSuccessRate()).isCloseTo(2.0 / 3.0, org.assertj.core.data.Offset.offset(0.001));
        }
    }
}
