package com.seguradora.hibrida.projection.consistency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ConsistencyIssue}.
 */
@DisplayName("ConsistencyIssue Tests")
class ConsistencyIssueTest {

    // =========================================================================
    // Construção via factory methods
    // =========================================================================

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("highLag com lag > 5*threshold deve criar issue CRITICAL")
        void highLagWithLagAboveFiveTimesThresholdShouldCreateCriticalIssue() {
            ConsistencyIssue issue = ConsistencyIssue.highLag("proj", 600L, 100L);

            assertThat(issue.severity()).isEqualTo(IssueSeverity.CRITICAL);
            assertThat(issue.type()).isEqualTo(IssueType.HIGH_LAG);
            assertThat(issue.projectionName()).isEqualTo("proj");
        }

        @Test
        @DisplayName("highLag com lag <= 5*threshold deve criar issue HIGH")
        void highLagWithLagBelowFiveTimesThresholdShouldCreateHighIssue() {
            ConsistencyIssue issue = ConsistencyIssue.highLag("proj", 200L, 100L);

            assertThat(issue.severity()).isEqualTo(IssueSeverity.HIGH);
        }

        @Test
        @DisplayName("highErrorRate com taxa > 2*threshold deve criar issue CRITICAL")
        void highErrorRateAboveTwoTimesThresholdShouldCreateCriticalIssue() {
            ConsistencyIssue issue = ConsistencyIssue.highErrorRate("proj", 0.25, 0.1);

            assertThat(issue.severity()).isEqualTo(IssueSeverity.CRITICAL);
            assertThat(issue.type()).isEqualTo(IssueType.HIGH_ERROR_RATE);
        }

        @Test
        @DisplayName("staleProjection com minutes > 60 deve criar issue CRITICAL")
        void staleProjectionAboveSixtyMinutesShouldCreateCriticalIssue() {
            ConsistencyIssue issue = ConsistencyIssue.staleProjection("proj", 90L);

            assertThat(issue.severity()).isEqualTo(IssueSeverity.CRITICAL);
            assertThat(issue.type()).isEqualTo(IssueType.STALE_PROJECTION);
        }

        @Test
        @DisplayName("persistentError deve criar issue HIGH")
        void persistentErrorShouldCreateHighIssue() {
            ConsistencyIssue issue = ConsistencyIssue.persistentError("proj", "DB error", 10L);

            assertThat(issue.severity()).isEqualTo(IssueSeverity.HIGH);
            assertThat(issue.type()).isEqualTo(IssueType.PERSISTENT_ERROR);
        }
    }

    // =========================================================================
    // Verificações de severidade
    // =========================================================================

    @Nested
    @DisplayName("Verificações de severidade")
    class VerificacoesSeveridade {

        @Test
        @DisplayName("isCritical deve retornar true para CRITICAL")
        void isCriticalShouldReturnTrueForCritical() {
            ConsistencyIssue issue = new ConsistencyIssue(
                    "proj", IssueType.HIGH_LAG, IssueSeverity.CRITICAL, "desc", 100L);

            assertThat(issue.isCritical()).isTrue();
            assertThat(issue.isHighPriority()).isFalse();
        }

        @Test
        @DisplayName("isHighPriority deve retornar true para HIGH")
        void isHighPriorityShouldReturnTrueForHigh() {
            ConsistencyIssue issue = new ConsistencyIssue(
                    "proj", IssueType.HIGH_LAG, IssueSeverity.HIGH, "desc", 100L);

            assertThat(issue.isCritical()).isFalse();
            assertThat(issue.isHighPriority()).isTrue();
        }
    }

    // =========================================================================
    // Conversão de valor
    // =========================================================================

    @Nested
    @DisplayName("Conversão de valor")
    class ConversaoValor {

        @Test
        @DisplayName("getValueAsLong deve retornar Long para Number")
        void getValueAsLongShouldReturnLongForNumber() {
            ConsistencyIssue issue = new ConsistencyIssue(
                    "proj", IssueType.HIGH_LAG, IssueSeverity.HIGH, "desc", 42L);

            assertThat(issue.getValueAsLong()).isEqualTo(42L);
        }

        @Test
        @DisplayName("getValueAsDouble deve retornar Double para Number")
        void getValueAsDoubleShouldReturnDoubleForNumber() {
            ConsistencyIssue issue = new ConsistencyIssue(
                    "proj", IssueType.HIGH_ERROR_RATE, IssueSeverity.HIGH, "desc", 0.15);

            assertThat(issue.getValueAsDouble()).isEqualTo(0.15);
        }

        @Test
        @DisplayName("getValueAsString deve retornar N/A para valor null")
        void getValueAsStringShouldReturnNAForNull() {
            ConsistencyIssue issue = new ConsistencyIssue(
                    "proj", IssueType.HIGH_LAG, IssueSeverity.HIGH, "desc", null);

            assertThat(issue.getValueAsString()).isEqualTo("N/A");
        }
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString deve retornar String não nula")
    void toStringShouldReturnNonNull() {
        ConsistencyIssue issue = new ConsistencyIssue(
                "proj", IssueType.HIGH_LAG, IssueSeverity.HIGH, "desc", 10L);

        assertThat(issue.toString()).isNotNull().isNotBlank();
    }

    // =========================================================================
    // getTimestamp
    // =========================================================================

    @Test
    @DisplayName("getTimestamp deve retornar Instant não nulo")
    void getTimestampShouldReturnNonNull() {
        ConsistencyIssue issue = new ConsistencyIssue(
                "proj", IssueType.HIGH_LAG, IssueSeverity.HIGH, "desc", 10L);

        assertThat(issue.getTimestamp()).isNotNull();
    }
}
