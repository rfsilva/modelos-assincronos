package com.seguradora.hibrida.aggregate.metrics;

import com.seguradora.hibrida.aggregate.config.AggregateProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AggregateMetrics")
class AggregateMetricsTest {

    @Mock
    private AggregateProperties properties;

    @Mock
    private AggregateProperties.Metrics metricsProperties;

    private MeterRegistry meterRegistry;
    private AggregateMetrics aggregateMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        lenient().when(properties.getMetrics()).thenReturn(metricsProperties);
        lenient().when(metricsProperties.getPrefix()).thenReturn("aggregate");
        lenient().when(metricsProperties.isDetailedLogging()).thenReturn(false);

        aggregateMetrics = new AggregateMetrics(meterRegistry, properties);
    }

    @Test
    @DisplayName("Deve registrar contadores no registry")
    void shouldRegisterCountersInRegistry() {
        // Assert
        assertThat(meterRegistry.find("aggregate_saves_total").counter()).isNotNull();
        assertThat(meterRegistry.find("aggregate_loads_total").counter()).isNotNull();
        assertThat(meterRegistry.find("aggregate_snapshots_used_total").counter()).isNotNull();
        assertThat(meterRegistry.find("aggregate_validations_total").counter()).isNotNull();
        assertThat(meterRegistry.find("aggregate_errors_total").counter()).isNotNull();
    }

    @Test
    @DisplayName("Deve registrar timers no registry")
    void shouldRegisterTimersInRegistry() {
        // Assert
        assertThat(meterRegistry.find("aggregate_save_seconds").timer()).isNotNull();
        assertThat(meterRegistry.find("aggregate_load_seconds").timer()).isNotNull();
        assertThat(meterRegistry.find("aggregate_reconstruction_seconds").timer()).isNotNull();
        assertThat(meterRegistry.find("aggregate_validation_seconds").timer()).isNotNull();
    }

    @Test
    @DisplayName("Deve registrar gauge no registry")
    void shouldRegisterGaugeInRegistry() {
        // Assert
        assertThat(meterRegistry.find("aggregate_active_count").gauge()).isNotNull();
    }

    @Test
    @DisplayName("Deve iniciar e parar timer de save")
    void shouldStartAndStopSaveTimer() {
        // Arrange & Act
        Timer.Sample sample = aggregateMetrics.startSaveTimer();
        simulateWork(50);
        aggregateMetrics.stopSaveTimer(sample);

        // Assert
        Timer saveTimer = meterRegistry.find("aggregate_save_seconds").timer();
        assertThat(saveTimer).isNotNull();
        assertThat(saveTimer.count()).isEqualTo(1);
        assertThat(saveTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThan(0);

        assertThat(meterRegistry.find("aggregate_saves_total").counter().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve iniciar e parar timer de load")
    void shouldStartAndStopLoadTimer() {
        // Arrange & Act
        Timer.Sample sample = aggregateMetrics.startLoadTimer();
        simulateWork(30);
        aggregateMetrics.stopLoadTimer(sample);

        // Assert
        Timer loadTimer = meterRegistry.find("aggregate_load_seconds").timer();
        assertThat(loadTimer).isNotNull();
        assertThat(loadTimer.count()).isEqualTo(1);

        assertThat(meterRegistry.find("aggregate_loads_total").counter().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve iniciar e parar timer de reconstrução")
    void shouldStartAndStopReconstructionTimer() {
        // Arrange & Act
        Timer.Sample sample = aggregateMetrics.startReconstructionTimer();
        simulateWork(100);
        aggregateMetrics.stopReconstructionTimer(sample);

        // Assert
        Timer reconstructionTimer = meterRegistry.find("aggregate_reconstruction_seconds").timer();
        assertThat(reconstructionTimer).isNotNull();
        assertThat(reconstructionTimer.count()).isEqualTo(1);
        assertThat(reconstructionTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThan(0);
    }

    @Test
    @DisplayName("Deve iniciar e parar timer de validação")
    void shouldStartAndStopValidationTimer() {
        // Arrange & Act
        Timer.Sample sample = aggregateMetrics.startValidationTimer();
        simulateWork(20);
        aggregateMetrics.stopValidationTimer(sample);

        // Assert
        Timer validationTimer = meterRegistry.find("aggregate_validation_seconds").timer();
        assertThat(validationTimer).isNotNull();
        assertThat(validationTimer.count()).isEqualTo(1);

        assertThat(meterRegistry.find("aggregate_validations_total").counter().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve incrementar contador de snapshots")
    void shouldIncrementSnapshotsCounter() {
        // Act
        aggregateMetrics.incrementSnapshotsUsed();
        aggregateMetrics.incrementSnapshotsUsed();
        aggregateMetrics.incrementSnapshotsUsed();

        // Assert
        assertThat(meterRegistry.find("aggregate_snapshots_used_total").counter().count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve incrementar contador de erros com tipo")
    void shouldIncrementErrorsCounterWithType() {
        // Act
        aggregateMetrics.incrementErrors("ValidationError");
        aggregateMetrics.incrementErrors("ValidationError");
        aggregateMetrics.incrementErrors("ConcurrencyError");

        // Assert
        assertThat(meterRegistry.find("aggregate_errors_total")
                .tag("type", "ValidationError")
                .counter()
                .count()).isEqualTo(2);

        assertThat(meterRegistry.find("aggregate_errors_total")
                .tag("type", "ConcurrencyError")
                .counter()
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve registrar timer customizado")
    void shouldRecordCustomTimer() {
        // Act
        aggregateMetrics.recordCustomTimer("custom_operation", 150L);

        // Assert
        Timer customTimer = meterRegistry.find("aggregate_custom_operation_seconds").timer();
        assertThat(customTimer).isNotNull();
        assertThat(customTimer.count()).isEqualTo(1);
        assertThat(customTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(150);
    }

    @Test
    @DisplayName("Deve obter estatísticas corretamente")
    void shouldGetStatisticsCorrectly() {
        // Arrange
        Timer.Sample saveSample = aggregateMetrics.startSaveTimer();
        simulateWork(10);
        aggregateMetrics.stopSaveTimer(saveSample);

        Timer.Sample loadSample = aggregateMetrics.startLoadTimer();
        simulateWork(20);
        aggregateMetrics.stopLoadTimer(loadSample);

        aggregateMetrics.incrementSnapshotsUsed();
        aggregateMetrics.incrementErrors("TestError");

        // Act
        AggregateMetrics.MetricsStatistics stats = aggregateMetrics.getStatistics();

        // Assert
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalSaves()).isEqualTo(1);
        assertThat(stats.getTotalLoads()).isEqualTo(1);
        assertThat(stats.getTotalSnapshots()).isEqualTo(1);
        assertThat(stats.getTotalErrors()).isEqualTo(1);
        assertThat(stats.getAverageSaveTime()).isGreaterThan(0);
        assertThat(stats.getAverageLoadTime()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Deve acumular múltiplas operações de save")
    void shouldAccumulateMultipleSaveOperations() {
        // Act
        for (int i = 0; i < 5; i++) {
            Timer.Sample sample = aggregateMetrics.startSaveTimer();
            simulateWork(10);
            aggregateMetrics.stopSaveTimer(sample);
        }

        // Assert
        AggregateMetrics.MetricsStatistics stats = aggregateMetrics.getStatistics();
        assertThat(stats.getTotalSaves()).isEqualTo(5);

        Timer saveTimer = meterRegistry.find("aggregate_save_seconds").timer();
        assertThat(saveTimer.count()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve acumular múltiplas operações de load")
    void shouldAccumulateMultipleLoadOperations() {
        // Act
        for (int i = 0; i < 3; i++) {
            Timer.Sample sample = aggregateMetrics.startLoadTimer();
            simulateWork(15);
            aggregateMetrics.stopLoadTimer(sample);
        }

        // Assert
        AggregateMetrics.MetricsStatistics stats = aggregateMetrics.getStatistics();
        assertThat(stats.getTotalLoads()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve acumular múltiplas validações")
    void shouldAccumulateMultipleValidations() {
        // Act
        for (int i = 0; i < 10; i++) {
            Timer.Sample sample = aggregateMetrics.startValidationTimer();
            simulateWork(5);
            aggregateMetrics.stopValidationTimer(sample);
        }

        // Assert
        AggregateMetrics.MetricsStatistics stats = aggregateMetrics.getStatistics();
        assertThat(stats.getTotalValidations()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve calcular tempo médio corretamente")
    void shouldCalculateAverageTimeCorrectly() {
        // Arrange
        Timer.Sample sample1 = aggregateMetrics.startSaveTimer();
        simulateWork(10);
        aggregateMetrics.stopSaveTimer(sample1);

        Timer.Sample sample2 = aggregateMetrics.startSaveTimer();
        simulateWork(20);
        aggregateMetrics.stopSaveTimer(sample2);

        Timer.Sample sample3 = aggregateMetrics.startSaveTimer();
        simulateWork(30);
        aggregateMetrics.stopSaveTimer(sample3);

        // Act
        AggregateMetrics.MetricsStatistics stats = aggregateMetrics.getStatistics();

        // Assert
        assertThat(stats.getAverageSaveTime()).isGreaterThan(0);
        assertThat(stats.getAverageSaveTime()).isLessThan(100); // Sanity check
    }

    @Test
    @DisplayName("Deve usar prefixo customizado das properties")
    void shouldUseCustomPrefixFromProperties() {
        // Arrange
        MeterRegistry customRegistry = new SimpleMeterRegistry();
        when(metricsProperties.getPrefix()).thenReturn("custom_prefix");

        // Act
        AggregateMetrics customMetrics = new AggregateMetrics(customRegistry, properties);

        // Assert
        assertThat(customRegistry.find("custom_prefix_saves_total").counter()).isNotNull();
        assertThat(customRegistry.find("custom_prefix_loads_total").counter()).isNotNull();
        assertThat(customRegistry.find("custom_prefix_save_seconds").timer()).isNotNull();
    }

    @Test
    @DisplayName("MetricsStatistics deve ter getters funcionais")
    void metricsStatisticsShouldHaveFunctionalGetters() {
        // Arrange
        AggregateMetrics.MetricsStatistics stats = AggregateMetrics.MetricsStatistics.builder()
                .totalSaves(10L)
                .totalLoads(20L)
                .totalSnapshots(5L)
                .totalValidations(30L)
                .totalErrors(2L)
                .averageSaveTime(15.5)
                .averageLoadTime(25.3)
                .averageReconstructionTime(100.7)
                .averageValidationTime(5.2)
                .build();

        // Assert
        assertThat(stats.getTotalSaves()).isEqualTo(10L);
        assertThat(stats.getTotalLoads()).isEqualTo(20L);
        assertThat(stats.getTotalSnapshots()).isEqualTo(5L);
        assertThat(stats.getTotalValidations()).isEqualTo(30L);
        assertThat(stats.getTotalErrors()).isEqualTo(2L);
        assertThat(stats.getAverageSaveTime()).isEqualTo(15.5);
        assertThat(stats.getAverageLoadTime()).isEqualTo(25.3);
        assertThat(stats.getAverageReconstructionTime()).isEqualTo(100.7);
        assertThat(stats.getAverageValidationTime()).isEqualTo(5.2);
    }

    @Test
    @DisplayName("Builder deve permitir construção fluente")
    void builderShouldAllowFluentConstruction() {
        // Act
        AggregateMetrics.MetricsStatistics stats = AggregateMetrics.MetricsStatistics.builder()
                .totalSaves(5L)
                .totalLoads(10L)
                .build();

        // Assert
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalSaves()).isEqualTo(5L);
        assertThat(stats.getTotalLoads()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Gauge de active count deve estar registrado")
    void activeCountGaugeShouldBeRegistered() {
        // Assert
        assertThat(meterRegistry.find("aggregate_active_count").gauge()).isNotNull();
        assertThat(meterRegistry.find("aggregate_active_count").gauge().value()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Deve permitir múltiplos registros de diferentes tipos de erro")
    void shouldAllowMultipleErrorTypeRegistrations() {
        // Act
        aggregateMetrics.incrementErrors("ValidationError");
        aggregateMetrics.incrementErrors("ConcurrencyError");
        aggregateMetrics.incrementErrors("NotFoundError");
        aggregateMetrics.incrementErrors("ValidationError");

        // Assert
        assertThat(meterRegistry.find("aggregate_errors_total")
                .tag("type", "ValidationError")
                .counter()
                .count()).isEqualTo(2);

        assertThat(meterRegistry.find("aggregate_errors_total")
                .tag("type", "ConcurrencyError")
                .counter()
                .count()).isEqualTo(1);

        assertThat(meterRegistry.find("aggregate_errors_total")
                .tag("type", "NotFoundError")
                .counter()
                .count()).isEqualTo(1);
    }

    /**
     * Simula trabalho com sleep.
     */
    private void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
