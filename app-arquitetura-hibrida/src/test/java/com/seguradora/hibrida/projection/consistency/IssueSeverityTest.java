package com.seguradora.hibrida.projection.consistency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link IssueSeverity}.
 */
@DisplayName("IssueSeverity Tests")
class IssueSeverityTest {

    @Test
    @DisplayName("Deve conter os 4 níveis de severidade")
    void shouldContainFourSeverityLevels() {
        assertThat(IssueSeverity.values()).containsExactlyInAnyOrder(
                IssueSeverity.CRITICAL,
                IssueSeverity.HIGH,
                IssueSeverity.MEDIUM,
                IssueSeverity.LOW
        );
    }

    @Test
    @DisplayName("CRITICAL deve ter prioridade 4")
    void criticalShouldHavePriorityFour() {
        assertThat(IssueSeverity.CRITICAL.getPriority()).isEqualTo(4);
    }

    @Test
    @DisplayName("HIGH deve ter prioridade 3")
    void highShouldHavePriorityThree() {
        assertThat(IssueSeverity.HIGH.getPriority()).isEqualTo(3);
    }

    @Test
    @DisplayName("isHighPriority deve retornar true para CRITICAL e HIGH")
    void isHighPriorityShouldReturnTrueForCriticalAndHigh() {
        assertThat(IssueSeverity.CRITICAL.isHighPriority()).isTrue();
        assertThat(IssueSeverity.HIGH.isHighPriority()).isTrue();
        assertThat(IssueSeverity.MEDIUM.isHighPriority()).isFalse();
        assertThat(IssueSeverity.LOW.isHighPriority()).isFalse();
    }

    @Test
    @DisplayName("requiresImmediateAction deve retornar true apenas para CRITICAL")
    void requiresImmediateActionShouldReturnTrueOnlyForCritical() {
        assertThat(IssueSeverity.CRITICAL.requiresImmediateAction()).isTrue();
        assertThat(IssueSeverity.HIGH.requiresImmediateAction()).isFalse();
        assertThat(IssueSeverity.MEDIUM.requiresImmediateAction()).isFalse();
        assertThat(IssueSeverity.LOW.requiresImmediateAction()).isFalse();
    }

    @Test
    @DisplayName("getColor deve retornar cor não nula para cada severidade")
    void getColorShouldReturnNonNullForEachSeverity() {
        for (IssueSeverity severity : IssueSeverity.values()) {
            assertThat(severity.getColor()).isNotNull().isNotBlank();
        }
    }

    @Test
    @DisplayName("getDisplayName deve retornar texto não nulo")
    void getDisplayNameShouldReturnNonNull() {
        for (IssueSeverity severity : IssueSeverity.values()) {
            assertThat(severity.getDisplayName()).isNotNull().isNotBlank();
        }
    }
}
