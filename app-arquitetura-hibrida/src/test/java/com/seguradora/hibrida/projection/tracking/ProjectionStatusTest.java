package com.seguradora.hibrida.projection.tracking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ProjectionStatus}.
 */
@DisplayName("ProjectionStatus Tests")
class ProjectionStatusTest {

    @Test
    @DisplayName("Deve conter os valores esperados")
    void shouldContainExpectedValues() {
        assertThat(ProjectionStatus.values()).containsExactlyInAnyOrder(
                ProjectionStatus.ACTIVE,
                ProjectionStatus.PAUSED,
                ProjectionStatus.ERROR,
                ProjectionStatus.REBUILDING,
                ProjectionStatus.DISABLED
        );
    }

    @Test
    @DisplayName("ACTIVE deve ser parseado por name")
    void activeShouldBeParsedByName() {
        assertThat(ProjectionStatus.valueOf("ACTIVE")).isEqualTo(ProjectionStatus.ACTIVE);
    }

    @Test
    @DisplayName("DISABLED deve ser parseado por name")
    void disabledShouldBeParsedByName() {
        assertThat(ProjectionStatus.valueOf("DISABLED")).isEqualTo(ProjectionStatus.DISABLED);
    }
}
