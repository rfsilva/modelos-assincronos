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
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link SnapshotCleanupScheduler}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SnapshotCleanupScheduler Tests")
class SnapshotCleanupSchedulerTest {

    @Mock
    private SnapshotStore snapshotStore;

    @Mock
    private SnapshotProperties snapshotProperties;

    private SnapshotCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(snapshotProperties.getMaxSnapshotsPerAggregate()).thenReturn(5);
        when(snapshotProperties.isAutoCleanupEnabled()).thenReturn(true);
        when(snapshotProperties.isMetricsEnabled()).thenReturn(true);
        when(snapshotProperties.isHealthCheckEnabled()).thenReturn(true);

        scheduler = new SnapshotCleanupScheduler(snapshotStore, snapshotProperties);
    }

    // =========================================================================
    // Anotações @Scheduled nos métodos
    // =========================================================================

    @Nested
    @DisplayName("Anotações @Scheduled")
    class AnotacoesScheduled {

        @Test
        @DisplayName("cleanupOldSnapshots deve estar anotado com @Scheduled")
        void cleanupShouldBeScheduled() throws NoSuchMethodException {
            Method m = SnapshotCleanupScheduler.class.getDeclaredMethod("cleanupOldSnapshots");
            assertThat(m.isAnnotationPresent(Scheduled.class)).isTrue();
        }

        @Test
        @DisplayName("generateUsageReport deve estar anotado com @Scheduled com cron '0 0 2 * * ?'")
        void generateUsageReportShouldBeScheduledWithCron() throws NoSuchMethodException {
            Method m = SnapshotCleanupScheduler.class.getDeclaredMethod("generateUsageReport");
            Scheduled scheduled = m.getAnnotation(Scheduled.class);

            assertThat(scheduled).isNotNull();
            assertThat(scheduled.cron()).isEqualTo("0 0 2 * * ?");
        }

        @Test
        @DisplayName("healthCheck deve estar anotado com @Scheduled fixedRate=1800000")
        void healthCheckShouldBeScheduledWithFixedRate() throws NoSuchMethodException {
            Method m = SnapshotCleanupScheduler.class.getDeclaredMethod("healthCheck");
            Scheduled scheduled = m.getAnnotation(Scheduled.class);

            assertThat(scheduled).isNotNull();
            assertThat(scheduled.fixedRate()).isEqualTo(1800000L);
        }

        @Test
        @DisplayName("performanceOptimization deve estar anotado com @Scheduled com cron '0 0 3 ? * SUN'")
        void performanceOptimizationShouldBeScheduledWithCron() throws NoSuchMethodException {
            Method m = SnapshotCleanupScheduler.class.getDeclaredMethod("performanceOptimization");
            Scheduled scheduled = m.getAnnotation(Scheduled.class);

            assertThat(scheduled).isNotNull();
            assertThat(scheduled.cron()).isEqualTo("0 0 3 ? * SUN");
        }
    }

    // =========================================================================
    // cleanupOldSnapshots()
    // =========================================================================

    @Nested
    @DisplayName("cleanupOldSnapshots()")
    class CleanupOldSnapshots {

        @Test
        @DisplayName("Deve chamar cleanupAllOldSnapshots quando autoCleanup habilitado")
        void shouldCallCleanupWhenEnabled() {
            when(snapshotStore.cleanupAllOldSnapshots(anyInt())).thenReturn(3);

            scheduler.cleanupOldSnapshots();

            verify(snapshotStore).cleanupAllOldSnapshots(5);
        }

        @Test
        @DisplayName("Não deve chamar store quando autoCleanup desabilitado")
        void shouldNotCallStoreWhenDisabled() {
            when(snapshotProperties.isAutoCleanupEnabled()).thenReturn(false);

            scheduler.cleanupOldSnapshots();

            verify(snapshotStore, never()).cleanupAllOldSnapshots(anyInt());
        }

        @Test
        @DisplayName("Não deve lançar exceção quando store lança RuntimeException")
        void shouldNotThrowWhenStoreFails() {
            when(snapshotStore.cleanupAllOldSnapshots(anyInt()))
                    .thenThrow(new RuntimeException("DB error"));

            // deve absorver a exceção
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> scheduler.cleanupOldSnapshots());
        }
    }

    // =========================================================================
    // generateUsageReport()
    // =========================================================================

    @Nested
    @DisplayName("generateUsageReport()")
    class GenerateUsageReport {

        @Test
        @DisplayName("Deve chamar getGlobalStatistics quando metrics habilitado")
        void shouldCallGetGlobalStatisticsWhenEnabled() {
            when(snapshotStore.getGlobalStatistics()).thenReturn(minimalStats());

            scheduler.generateUsageReport();

            verify(snapshotStore).getGlobalStatistics();
        }

        @Test
        @DisplayName("Não deve chamar store quando metrics desabilitado")
        void shouldNotCallStoreWhenMetricsDisabled() {
            when(snapshotProperties.isMetricsEnabled()).thenReturn(false);

            scheduler.generateUsageReport();

            verify(snapshotStore, never()).getGlobalStatistics();
        }

        @Test
        @DisplayName("Não deve lançar exceção quando store falha")
        void shouldNotThrowWhenStoreFails() {
            when(snapshotStore.getGlobalStatistics()).thenThrow(new RuntimeException("DB error"));

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> scheduler.generateUsageReport());
        }
    }

    // =========================================================================
    // healthCheck()
    // =========================================================================

    @Nested
    @DisplayName("healthCheck()")
    class HealthCheck {

        @Test
        @DisplayName("Deve chamar getGlobalStatistics quando healthCheck habilitado")
        void shouldCallGetGlobalStatisticsWhenEnabled() {
            when(snapshotStore.getGlobalStatistics()).thenReturn(minimalStats());

            scheduler.healthCheck();

            verify(snapshotStore).getGlobalStatistics();
        }

        @Test
        @DisplayName("Não deve chamar store quando healthCheck desabilitado")
        void shouldNotCallStoreWhenDisabled() {
            when(snapshotProperties.isHealthCheckEnabled()).thenReturn(false);

            scheduler.healthCheck();

            verify(snapshotStore, never()).getGlobalStatistics();
        }

        @Test
        @DisplayName("Não deve lançar exceção quando store falha")
        void shouldNotThrowWhenStoreFails() {
            when(snapshotStore.getGlobalStatistics()).thenThrow(new RuntimeException("Erro"));

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> scheduler.healthCheck());
        }
    }

    // =========================================================================
    // performanceOptimization()
    // =========================================================================

    @Test
    @DisplayName("performanceOptimization() não deve lançar exceção")
    void performanceOptimizationShouldNotThrow() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> scheduler.performanceOptimization());
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private SnapshotStatistics minimalStats() {
        return SnapshotStatistics.builder()
                .totalSnapshots(10L)
                .compressedSnapshots(8L)
                .totalOriginalSize(10_000L)
                .totalCompressedSize(6_000L)
                .totalSpaceSaved(4_000L)
                .snapshotsLast24Hours(2L)
                .snapshotsLastWeek(10L)
                .build();
    }
}
