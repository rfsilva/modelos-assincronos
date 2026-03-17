package com.seguradora.hibrida.snapshot.config;

import com.seguradora.hibrida.snapshot.SnapshotProperties;
import com.seguradora.hibrida.snapshot.SnapshotStatistics;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link SnapshotHealthIndicator}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SnapshotHealthIndicator Tests")
class SnapshotHealthIndicatorTest {

    @Mock
    private SnapshotStore snapshotStore;

    @Mock
    private SnapshotProperties snapshotProperties;

    private SnapshotHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        when(snapshotProperties.getMaxConsecutiveFailures()).thenReturn(3);
        when(snapshotProperties.getSnapshotThreshold()).thenReturn(50);
        when(snapshotProperties.getMaxSnapshotsPerAggregate()).thenReturn(5);
        when(snapshotProperties.isCompressionEnabled()).thenReturn(true);
        when(snapshotProperties.isAutoCleanupEnabled()).thenReturn(true);
        indicator = new SnapshotHealthIndicator(snapshotStore, snapshotProperties);
    }

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(SnapshotHealthIndicator.class.isAnnotationPresent(Component.class)).isTrue();
    }

    // =========================================================================
    // health() — sistema operacional
    // =========================================================================

    @Nested
    @DisplayName("health() — sistema UP")
    class HealthUp {

        @Test
        @DisplayName("Deve retornar UP quando store está operacional")
        void shouldReturnUpWhenStoreIsOperational() {
            // Given
            when(snapshotStore.getGlobalStatistics()).thenReturn(healthyStats());

            // When
            SnapshotHealthIndicator.SnapshotHealthResult result = indicator.health();

            // Then
            assertThat(result.isUp()).isTrue();
            assertThat(result.getStatus()).isEqualTo("UP");
        }

        @Test
        @DisplayName("Deve incluir totalSnapshots nos detalhes")
        void shouldIncludeTotalSnapshotsInDetails() {
            when(snapshotStore.getGlobalStatistics()).thenReturn(healthyStats());

            SnapshotHealthIndicator.SnapshotHealthResult result = indicator.health();

            assertThat(result.getDetails()).containsKey("totalSnapshots");
        }

        @Test
        @DisplayName("Deve incluir responseTimeMs nos detalhes")
        void shouldIncludeResponseTimeInDetails() {
            when(snapshotStore.getGlobalStatistics()).thenReturn(healthyStats());

            SnapshotHealthIndicator.SnapshotHealthResult result = indicator.health();

            assertThat(result.getDetails()).containsKey("responseTimeMs");
        }
    }

    // =========================================================================
    // health() — exceção
    // =========================================================================

    @Nested
    @DisplayName("health() — sistema DOWN")
    class HealthDown {

        @Test
        @DisplayName("Deve retornar DOWN quando store lança exceção")
        void shouldReturnDownWhenStoreThrows() {
            when(snapshotStore.getGlobalStatistics()).thenThrow(new RuntimeException("Conexão perdida"));

            SnapshotHealthIndicator.SnapshotHealthResult result = indicator.health();

            assertThat(result.isDown()).isTrue();
            assertThat(result.getStatus()).isEqualTo("DOWN");
        }

        @Test
        @DisplayName("Deve incluir 'error' nos detalhes quando store lança exceção")
        void shouldIncludeErrorInDetailsWhenThrows() {
            when(snapshotStore.getGlobalStatistics()).thenThrow(new RuntimeException("Timeout"));

            SnapshotHealthIndicator.SnapshotHealthResult result = indicator.health();

            assertThat(result.getDetails()).containsKey("error");
        }

        @Test
        @DisplayName("Deve incluir consecutiveFailures nos detalhes")
        void shouldIncludeConsecutiveFailuresInDetails() {
            when(snapshotStore.getGlobalStatistics()).thenThrow(new RuntimeException("Erro"));

            SnapshotHealthIndicator.SnapshotHealthResult result = indicator.health();

            assertThat(result.getDetails()).containsKey("consecutiveFailures");
        }
    }

    // =========================================================================
    // isOperational()
    // =========================================================================

    @Nested
    @DisplayName("isOperational()")
    class IsOperational {

        @Test
        @DisplayName("Deve retornar true quando store está acessível")
        void shouldReturnTrueWhenStoreIsAccessible() {
            when(snapshotStore.getGlobalStatistics()).thenReturn(healthyStats());

            assertThat(indicator.isOperational()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando store lança exceção")
        void shouldReturnFalseWhenStoreThrows() {
            when(snapshotStore.getGlobalStatistics()).thenThrow(new RuntimeException("Erro"));

            assertThat(indicator.isOperational()).isFalse();
        }
    }

    // =========================================================================
    // getHealthSummary()
    // =========================================================================

    @Nested
    @DisplayName("getHealthSummary()")
    class GetHealthSummary {

        @Test
        @DisplayName("Deve retornar operational=true quando store está ok")
        void shouldReturnOperationalTrueWhenStoreOk() {
            when(snapshotStore.getGlobalStatistics()).thenReturn(healthyStats());

            Map<String, Object> summary = indicator.getHealthSummary();

            assertThat(summary.get("operational")).isEqualTo(true);
        }

        @Test
        @DisplayName("Deve retornar operational=false quando store falha")
        void shouldReturnOperationalFalseWhenStoreFails() {
            when(snapshotStore.getGlobalStatistics()).thenThrow(new RuntimeException("Falha"));

            Map<String, Object> summary = indicator.getHealthSummary();

            assertThat(summary.get("operational")).isEqualTo(false);
        }

        @Test
        @DisplayName("Deve incluir consecutiveFailures no sumário")
        void shouldIncludeConsecutiveFailuresInSummary() {
            when(snapshotStore.getGlobalStatistics()).thenReturn(healthyStats());

            Map<String, Object> summary = indicator.getHealthSummary();

            assertThat(summary).containsKey("consecutiveFailures");
        }
    }

    // =========================================================================
    // SnapshotHealthResult
    // =========================================================================

    @Nested
    @DisplayName("SnapshotHealthResult")
    class SnapshotHealthResultTest {

        @Test
        @DisplayName("up() deve criar resultado com status UP")
        void shouldCreateUpResult() {
            SnapshotHealthIndicator.SnapshotHealthResult result =
                    SnapshotHealthIndicator.SnapshotHealthResult.up(Map.of("key", "value"));

            assertThat(result.isUp()).isTrue();
            assertThat(result.isDown()).isFalse();
            assertThat(result.getStatus()).isEqualTo("UP");
        }

        @Test
        @DisplayName("down() deve criar resultado com status DOWN")
        void shouldCreateDownResult() {
            SnapshotHealthIndicator.SnapshotHealthResult result =
                    SnapshotHealthIndicator.SnapshotHealthResult.down(Map.of("error", "msg"));

            assertThat(result.isDown()).isTrue();
            assertThat(result.isUp()).isFalse();
            assertThat(result.getStatus()).isEqualTo("DOWN");
        }

        @Test
        @DisplayName("getDetails() deve retornar cópia defensiva dos detalhes")
        void shouldReturnDefensiveCopyOfDetails() {
            Map<String, Object> details = new java.util.HashMap<>();
            details.put("key", "value");
            SnapshotHealthIndicator.SnapshotHealthResult result =
                    SnapshotHealthIndicator.SnapshotHealthResult.up(details);

            Map<String, Object> returned = result.getDetails();
            returned.put("extra", "should not affect original");

            assertThat(result.getDetails()).doesNotContainKey("extra");
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private SnapshotStatistics healthyStats() {
        return SnapshotStatistics.builder()
                .totalSnapshots(100L)
                .compressedSnapshots(80L)
                .totalOriginalSize(100_000L)
                .totalCompressedSize(60_000L)
                .totalSpaceSaved(40_000L)
                .snapshotsLast24Hours(5L)
                .snapshotsLastWeek(30L)
                .build();
    }
}
