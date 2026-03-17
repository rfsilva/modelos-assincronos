package com.seguradora.hibrida.projection.rebuild;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link RebuildType}.
 */
@DisplayName("RebuildType Tests")
class RebuildTypeTest {

    @Test
    @DisplayName("Deve conter FULL e INCREMENTAL")
    void shouldContainFullAndIncremental() {
        assertThat(RebuildType.values()).containsExactlyInAnyOrder(
                RebuildType.FULL,
                RebuildType.INCREMENTAL
        );
    }

    @Test
    @DisplayName("getDisplayName deve retornar texto não nulo para cada tipo")
    void getDisplayNameShouldReturnNonNullForEachType() {
        for (RebuildType type : RebuildType.values()) {
            assertThat(type.getDisplayName()).isNotNull().isNotBlank();
        }
    }

    @Test
    @DisplayName("toString deve retornar displayName")
    void toStringShouldReturnDisplayName() {
        assertThat(RebuildType.FULL.toString()).isEqualTo(RebuildType.FULL.getDisplayName());
        assertThat(RebuildType.INCREMENTAL.toString()).isEqualTo(RebuildType.INCREMENTAL.getDisplayName());
    }
}
