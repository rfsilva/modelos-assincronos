package com.seguradora.hibrida.projection.rebuild;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link RebuildStatus}.
 */
@DisplayName("RebuildStatus Tests")
class RebuildStatusTest {

    @Test
    @DisplayName("Deve conter os valores esperados")
    void shouldContainExpectedValues() {
        assertThat(RebuildStatus.values()).containsExactlyInAnyOrder(
                RebuildStatus.SUCCESS,
                RebuildStatus.FAILED,
                RebuildStatus.PAUSED,
                RebuildStatus.RUNNING
        );
    }

    @Test
    @DisplayName("getDisplayName deve retornar texto não nulo para cada status")
    void getDisplayNameShouldReturnNonNullForEachStatus() {
        for (RebuildStatus status : RebuildStatus.values()) {
            assertThat(status.getDisplayName()).isNotNull().isNotBlank();
        }
    }

    @Test
    @DisplayName("toString deve retornar displayName")
    void toStringShouldReturnDisplayName() {
        assertThat(RebuildStatus.SUCCESS.toString()).isEqualTo(RebuildStatus.SUCCESS.getDisplayName());
    }
}
