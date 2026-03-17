package com.seguradora.hibrida.projection.consistency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ConsistencyReport}.
 */
@DisplayName("ConsistencyReport Tests")
class ConsistencyReportTest {

    // =========================================================================
    // Relatório sem issues
    // =========================================================================

    @Nested
    @DisplayName("Relatório sem issues")
    class SemIssues {

        @Test
        @DisplayName("Deve ser saudável quando não há issues")
        void shouldBeHealthyWhenNoIssues() {
            ConsistencyReport report = new ConsistencyReport(5, List.of(), 100L, 50L, Instant.now());

            assertThat(report.isHealthy()).isTrue();
            assertThat(report.hasCriticalIssues()).isFalse();
            assertThat(report.getTotalIssues()).isZero();
        }

        @Test
        @DisplayName("getHealthScore deve retornar 100 quando sem issues")
        void healthScoreShouldBeHundredWhenNoIssues() {
            ConsistencyReport report = new ConsistencyReport(5, List.of(), 100L, 50L, Instant.now());

            assertThat(report.getHealthScore()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("getHealthScore deve retornar 100 quando totalProjections=0")
        void healthScoreShouldBeHundredWhenZeroProjections() {
            ConsistencyReport report = new ConsistencyReport(0, List.of(), 100L, 0L, Instant.now());

            assertThat(report.getHealthScore()).isEqualTo(100.0);
        }
    }

    // =========================================================================
    // Relatório com issues
    // =========================================================================

    @Nested
    @DisplayName("Relatório com issues")
    class ComIssues {

        private ConsistencyIssue criticalIssue() {
            return new ConsistencyIssue("proj1", IssueType.HIGH_LAG, IssueSeverity.CRITICAL, "desc", 100L);
        }

        private ConsistencyIssue highIssue() {
            return new ConsistencyIssue("proj2", IssueType.HIGH_ERROR_RATE, IssueSeverity.HIGH, "desc", 0.2);
        }

        @Test
        @DisplayName("Deve reportar hasCriticalIssues=true quando há issue crítico")
        void shouldReportHasCriticalIssuesTrueWhenCriticalIssuePresent() {
            ConsistencyReport report = new ConsistencyReport(
                    3, List.of(criticalIssue()), 100L, 50L, Instant.now());

            assertThat(report.hasCriticalIssues()).isTrue();
            assertThat(report.getCriticalIssuesCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("isHealthy deve retornar false quando há issues críticos")
        void isHealthyShouldReturnFalseWhenCriticalIssuesPresent() {
            ConsistencyReport report = new ConsistencyReport(
                    3, List.of(criticalIssue()), 100L, 50L, Instant.now());

            assertThat(report.isHealthy()).isFalse();
        }

        @Test
        @DisplayName("isHealthy deve retornar false quando há issues HIGH")
        void isHealthyShouldReturnFalseWhenHighIssuesPresent() {
            ConsistencyReport report = new ConsistencyReport(
                    3, List.of(highIssue()), 100L, 50L, Instant.now());

            assertThat(report.isHealthy()).isFalse();
        }

        @Test
        @DisplayName("getIssuesBySeverity deve agrupar issues por severidade")
        void getIssuesBySeverityShouldGroupBySeverity() {
            ConsistencyReport report = new ConsistencyReport(
                    3, List.of(criticalIssue(), highIssue()), 100L, 50L, Instant.now());

            assertThat(report.getIssuesBySeverity()).containsKey(IssueSeverity.CRITICAL);
            assertThat(report.getIssuesBySeverity()).containsKey(IssueSeverity.HIGH);
        }

        @Test
        @DisplayName("getProjectionsWithIssues deve listar projeções com issues")
        void getProjectionsWithIssuesShouldListProjectionsWithIssues() {
            ConsistencyReport report = new ConsistencyReport(
                    3, List.of(criticalIssue(), highIssue()), 100L, 50L, Instant.now());

            assertThat(report.getProjectionsWithIssues()).containsExactlyInAnyOrder("proj1", "proj2");
        }

        @Test
        @DisplayName("getHealthyProjectionsCount deve retornar contagem correta")
        void getHealthyProjectionsCountShouldReturnCorrectCount() {
            ConsistencyReport report = new ConsistencyReport(
                    3, List.of(criticalIssue()), 100L, 50L, Instant.now());

            // 3 total - 1 com issue = 2 saudáveis
            assertThat(report.getHealthyProjectionsCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("getSummary deve retornar String não nula")
        void getSummaryShouldReturnNonNull() {
            ConsistencyReport report = new ConsistencyReport(
                    3, List.of(criticalIssue()), 100L, 50L, Instant.now());

            assertThat(report.getSummary()).isNotNull().isNotBlank();
        }
    }

    // =========================================================================
    // Accessor methods
    // =========================================================================

    @Test
    @DisplayName("Deve expor todos os campos do record")
    void shouldExposeAllRecordFields() {
        Instant now = Instant.now();
        ConsistencyReport report = new ConsistencyReport(5, List.of(), 200L, 100L, now);

        assertThat(report.totalProjections()).isEqualTo(5);
        assertThat(report.issues()).isEmpty();
        assertThat(report.maxEventId()).isEqualTo(200L);
        assertThat(report.durationMs()).isEqualTo(100L);
        assertThat(report.timestamp()).isEqualTo(now);
    }
}
