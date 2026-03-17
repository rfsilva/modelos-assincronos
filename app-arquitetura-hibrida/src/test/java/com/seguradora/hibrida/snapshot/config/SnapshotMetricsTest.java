package com.seguradora.hibrida.snapshot.config;

import com.seguradora.hibrida.snapshot.SnapshotStatistics;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link SnapshotMetrics}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SnapshotMetrics Tests")
class SnapshotMetricsTest {

    @Mock
    private SnapshotStore snapshotStore;

    private MeterRegistry meterRegistry;
    private SnapshotMetrics metrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        when(snapshotStore.getGlobalStatistics()).thenReturn(SnapshotStatistics.builder()
                .totalSnapshots(50L)
                .compressedSnapshots(40L)
                .totalOriginalSize(100_000L)
                .totalCompressedSize(60_000L)
                .totalSpaceSaved(40_000L)
                .snapshotsLast24Hours(3L)
                .snapshotsLastWeek(20L)
                .build());
        metrics = new SnapshotMetrics(meterRegistry, snapshotStore);
        metrics.initializeGauges();
    }

    // =========================================================================
    // initializeGauges — contadores
    // =========================================================================

    @Nested
    @DisplayName("initializeGauges() — contadores e timers")
    class InitializeGauges {

        @Test
        @DisplayName("Deve registrar contador snapshots.created.total")
        void shouldRegisterCreatedCounter() {
            assertThat(meterRegistry.find("snapshots.created.total").counter()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar contador snapshots.failed.total")
        void shouldRegisterFailedCounter() {
            assertThat(meterRegistry.find("snapshots.failed.total").counter()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar contador snapshots.loaded.total")
        void shouldRegisterLoadedCounter() {
            assertThat(meterRegistry.find("snapshots.loaded.total").counter()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar contador snapshots.deleted.total")
        void shouldRegisterDeletedCounter() {
            assertThat(meterRegistry.find("snapshots.deleted.total").counter()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar timer snapshots.creation.time")
        void shouldRegisterCreationTimer() {
            assertThat(meterRegistry.find("snapshots.creation.time").timer()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar timer snapshots.load.time")
        void shouldRegisterLoadTimer() {
            assertThat(meterRegistry.find("snapshots.load.time").timer()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar timer snapshots.compression.time")
        void shouldRegisterCompressionTimer() {
            assertThat(meterRegistry.find("snapshots.compression.time").timer()).isNotNull();
        }
    }

    // =========================================================================
    // Incrementos de contador
    // =========================================================================

    @Nested
    @DisplayName("Incrementos de contador")
    class IncrementsDeContador {

        @Test
        @DisplayName("stopCreationTimer() deve incrementar snapshotsCreated")
        void shouldIncrementSnapshotsCreatedOnStopCreationTimer() {
            Timer.Sample sample = metrics.startCreationTimer();
            metrics.stopCreationTimer(sample);

            double count = meterRegistry.find("snapshots.created.total").counter().count();
            assertThat(count).isEqualTo(1.0);
        }

        @Test
        @DisplayName("stopLoadTimer() deve incrementar snapshotsLoaded")
        void shouldIncrementSnapshotsLoadedOnStopLoadTimer() {
            Timer.Sample sample = metrics.startLoadTimer();
            metrics.stopLoadTimer(sample);

            double count = meterRegistry.find("snapshots.loaded.total").counter().count();
            assertThat(count).isEqualTo(1.0);
        }

        @Test
        @DisplayName("incrementFailures() deve incrementar snapshotsFailed")
        void shouldIncrementFailures() {
            metrics.incrementFailures();
            metrics.incrementFailures();

            double count = meterRegistry.find("snapshots.failed.total").counter().count();
            assertThat(count).isEqualTo(2.0);
        }

        @Test
        @DisplayName("incrementDeleted() deve incrementar snapshotsDeleted pelo valor informado")
        void shouldIncrementDeleted() {
            metrics.incrementDeleted(5);

            double count = meterRegistry.find("snapshots.deleted.total").counter().count();
            assertThat(count).isEqualTo(5.0);
        }
    }

    // =========================================================================
    // Gauges de armazenamento
    // =========================================================================

    @Nested
    @DisplayName("Gauges de armazenamento")
    class GaugesDeArmazenamento {

        @Test
        @DisplayName("updateStorageMetrics() deve atualizar totais de storage")
        void shouldUpdateStorageMetrics() {
            metrics.updateStorageMetrics(50_000L, 20_000L);

            double storageUsed = meterRegistry.find("snapshots.storage.used.bytes").gauge().value();
            double spaceSaved  = meterRegistry.find("snapshots.storage.saved.bytes").gauge().value();

            assertThat(storageUsed).isEqualTo(50_000.0);
            assertThat(spaceSaved).isEqualTo(20_000.0);
        }

        @Test
        @DisplayName("updateTotals() deve atualizar snapshots.total e snapshots.aggregates.total")
        void shouldUpdateTotals() {
            metrics.updateTotals(200L, 10L);

            double totalSnaps = meterRegistry.find("snapshots.total").gauge().value();
            double totalAggs  = meterRegistry.find("snapshots.aggregates.total").gauge().value();

            assertThat(totalSnaps).isEqualTo(200.0);
            assertThat(totalAggs).isEqualTo(10.0);
        }
    }

    // =========================================================================
    // getMetricsStatistics()
    // =========================================================================

    @Nested
    @DisplayName("getMetricsStatistics()")
    class GetMetricsStatistics {

        @Test
        @DisplayName("Deve retornar instância de MetricsStatistics")
        void shouldReturnMetricsStatistics() {
            SnapshotMetrics.MetricsStatistics stats = metrics.getMetricsStatistics();
            assertThat(stats).isNotNull();
        }

        @Test
        @DisplayName("Deve refletir totais atualizados via updateTotals")
        void shouldReflectUpdatedTotals() {
            metrics.updateTotals(100L, 5L);
            SnapshotMetrics.MetricsStatistics stats = metrics.getMetricsStatistics();

            assertThat(stats.totalSnapshots).isEqualTo(100L);
            assertThat(stats.totalAggregates).isEqualTo(5L);
        }

        @Test
        @DisplayName("Deve refletir storage atualizado via updateStorageMetrics")
        void shouldReflectUpdatedStorage() {
            metrics.updateStorageMetrics(70_000L, 30_000L);
            SnapshotMetrics.MetricsStatistics stats = metrics.getMetricsStatistics();

            assertThat(stats.totalStorageUsed).isEqualTo(70_000L);
            assertThat(stats.totalSpaceSaved).isEqualTo(30_000L);
        }

        @Test
        @DisplayName("Deve contabilizar snapshots criados via stopCreationTimer")
        void shouldCountCreatedSnapshots() {
            Timer.Sample s = metrics.startCreationTimer();
            metrics.stopCreationTimer(s);

            SnapshotMetrics.MetricsStatistics stats = metrics.getMetricsStatistics();
            assertThat(stats.snapshotsCreated).isEqualTo(1.0);
        }
    }

    // =========================================================================
    // MetricsStatistics inner class — builder
    // =========================================================================

    @Test
    @DisplayName("MetricsStatistics.builder() deve criar instância via builder")
    void shouldBuildMetricsStatistics() {
        SnapshotMetrics.MetricsStatistics stats = SnapshotMetrics.MetricsStatistics.builder()
                .totalSnapshots(10L)
                .totalAggregates(3L)
                .snapshotsCreated(5.0)
                .snapshotsFailed(1.0)
                .compressionRatio(0.4)
                .storageEfficiency(0.6)
                .build();

        assertThat(stats.totalSnapshots).isEqualTo(10L);
        assertThat(stats.totalAggregates).isEqualTo(3L);
        assertThat(stats.snapshotsCreated).isEqualTo(5.0);
        assertThat(stats.compressionRatio).isEqualTo(0.4);
    }

    // =========================================================================
    // @Scheduled
    // =========================================================================

    @Test
    @DisplayName("Deve ter método updateMetrics anotado com @Scheduled")
    void shouldHaveScheduledUpdateMetrics() throws NoSuchMethodException {
        Method method = SnapshotMetrics.class.getDeclaredMethod("updateMetrics");
        assertThat(method.isAnnotationPresent(Scheduled.class)).isTrue();
    }

    // =========================================================================
    // Timers — startCompressionTimer / stopCompressionTimer
    // =========================================================================

    @Test
    @DisplayName("startCompressionTimer() não deve lançar exceção")
    void startCompressionTimerShouldNotThrow() {
        Timer.Sample sample = metrics.startCompressionTimer();
        assertThat(sample).isNotNull();
        metrics.stopCompressionTimer(sample);
    }
}
