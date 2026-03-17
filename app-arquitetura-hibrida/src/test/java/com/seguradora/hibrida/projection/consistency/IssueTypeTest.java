package com.seguradora.hibrida.projection.consistency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link IssueType}.
 */
@DisplayName("IssueType Tests")
class IssueTypeTest {

    @Test
    @DisplayName("Deve conter os tipos esperados")
    void shouldContainExpectedTypes() {
        assertThat(IssueType.values()).containsExactlyInAnyOrder(
                IssueType.HIGH_LAG,
                IssueType.STALE_PROJECTION,
                IssueType.HIGH_ERROR_RATE,
                IssueType.PERSISTENT_ERROR,
                IssueType.LONG_PAUSED,
                IssueType.ORPHANED_PROJECTION,
                IssueType.PROJECTION_NOT_FOUND,
                IssueType.SYSTEM_ERROR,
                IssueType.DATA_INCONSISTENCY,
                IssueType.VERSION_MISMATCH
        );
    }

    @Test
    @DisplayName("requiresImmediateAction deve retornar true para tipos críticos")
    void requiresImmediateActionShouldReturnTrueForCriticalTypes() {
        assertThat(IssueType.HIGH_LAG.requiresImmediateAction()).isTrue();
        assertThat(IssueType.STALE_PROJECTION.requiresImmediateAction()).isTrue();
        assertThat(IssueType.PERSISTENT_ERROR.requiresImmediateAction()).isTrue();
        assertThat(IssueType.SYSTEM_ERROR.requiresImmediateAction()).isTrue();
    }

    @Test
    @DisplayName("canBeAutoResolved deve retornar true para tipos resolvíveis automaticamente")
    void canBeAutoResolvedShouldReturnTrueForAutoResolvableTypes() {
        assertThat(IssueType.HIGH_LAG.canBeAutoResolved()).isTrue();
        assertThat(IssueType.STALE_PROJECTION.canBeAutoResolved()).isTrue();
        assertThat(IssueType.HIGH_ERROR_RATE.canBeAutoResolved()).isTrue();
        assertThat(IssueType.LONG_PAUSED.canBeAutoResolved()).isTrue();
    }

    @Test
    @DisplayName("getDisplayName deve retornar texto não nulo para cada tipo")
    void getDisplayNameShouldReturnNonNullForEachType() {
        for (IssueType type : IssueType.values()) {
            assertThat(type.getDisplayName()).isNotNull().isNotBlank();
        }
    }
}
