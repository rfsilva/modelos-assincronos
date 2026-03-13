package com.seguradora.hibrida.aggregate.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link AggregateProperties}.
 */
@DisplayName("AggregateProperties Tests")
class AggregatePropertiesTest {

    private AggregateProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AggregateProperties();
    }

    @Test
    @DisplayName("Deve criar com valores padrão")
    void shouldCreateWithDefaultValues() {
        // Then
        assertThat(properties).isNotNull();
        assertThat(properties.getMetrics()).isNotNull();
        assertThat(properties.getHealthCheck()).isNotNull();
        assertThat(properties.getValidation()).isNotNull();
        assertThat(properties.getPerformance()).isNotNull();
        assertThat(properties.getSnapshot()).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar métricas")
    void shouldConfigureMetrics() {
        // Given
        AggregateProperties.Metrics metrics = new AggregateProperties.Metrics();
        metrics.setEnabled(false);
        metrics.setDetailedLogging(true);
        metrics.setPrefix("custom-prefix");

        // When
        properties.setMetrics(metrics);

        // Then
        assertThat(properties.getMetrics().isEnabled()).isFalse();
        assertThat(properties.getMetrics().isDetailedLogging()).isTrue();
        assertThat(properties.getMetrics().getPrefix()).isEqualTo("custom-prefix");
    }

    @Test
    @DisplayName("Deve ter valores padrão de métricas corretos")
    void shouldHaveCorrectMetricsDefaults() {
        // Given
        AggregateProperties.Metrics metrics = properties.getMetrics();

        // Then
        assertThat(metrics.isEnabled()).isTrue();
        assertThat(metrics.isDetailedLogging()).isFalse();
        assertThat(metrics.getPrefix()).isEqualTo("aggregate");
    }

    @Test
    @DisplayName("Deve configurar health check")
    void shouldConfigureHealthCheck() {
        // Given
        AggregateProperties.HealthCheck healthCheck = new AggregateProperties.HealthCheck();
        healthCheck.setEnabled(false);
        healthCheck.setTimeoutSeconds(10);
        healthCheck.setIntervalSeconds(60);

        // When
        properties.setHealthCheck(healthCheck);

        // Then
        assertThat(properties.getHealthCheck().isEnabled()).isFalse();
        assertThat(properties.getHealthCheck().getTimeoutSeconds()).isEqualTo(10);
        assertThat(properties.getHealthCheck().getIntervalSeconds()).isEqualTo(60);
    }

    @Test
    @DisplayName("Deve ter valores padrão de health check corretos")
    void shouldHaveCorrectHealthCheckDefaults() {
        // Given
        AggregateProperties.HealthCheck healthCheck = properties.getHealthCheck();

        // Then
        assertThat(healthCheck.isEnabled()).isTrue();
        assertThat(healthCheck.getTimeoutSeconds()).isEqualTo(5);
        assertThat(healthCheck.getIntervalSeconds()).isEqualTo(30);
    }

    @Test
    @DisplayName("Deve configurar validação")
    void shouldConfigureValidation() {
        // Given
        AggregateProperties.Validation validation = new AggregateProperties.Validation();
        validation.setEnabled(false);
        validation.setFailFast(true);
        validation.setMaxViolations(20);
        validation.setTimeoutMs(2000);

        // When
        properties.setValidation(validation);

        // Then
        assertThat(properties.getValidation().isEnabled()).isFalse();
        assertThat(properties.getValidation().isFailFast()).isTrue();
        assertThat(properties.getValidation().getMaxViolations()).isEqualTo(20);
        assertThat(properties.getValidation().getTimeoutMs()).isEqualTo(2000);
    }

    @Test
    @DisplayName("Deve ter valores padrão de validação corretos")
    void shouldHaveCorrectValidationDefaults() {
        // Given
        AggregateProperties.Validation validation = properties.getValidation();

        // Then
        assertThat(validation.isEnabled()).isTrue();
        assertThat(validation.isFailFast()).isFalse();
        assertThat(validation.getMaxViolations()).isEqualTo(10);
        assertThat(validation.getTimeoutMs()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Deve configurar performance")
    void shouldConfigurePerformance() {
        // Given
        AggregateProperties.Performance performance = new AggregateProperties.Performance();
        performance.setCacheHandlers(false);
        performance.setCacheSize(2000);
        performance.setParallelValidation(true);
        performance.setMaxValidationThreads(8);
        performance.setOptimizeReflection(false);

        // When
        properties.setPerformance(performance);

        // Then
        assertThat(properties.getPerformance().isCacheHandlers()).isFalse();
        assertThat(properties.getPerformance().getCacheSize()).isEqualTo(2000);
        assertThat(properties.getPerformance().isParallelValidation()).isTrue();
        assertThat(properties.getPerformance().getMaxValidationThreads()).isEqualTo(8);
        assertThat(properties.getPerformance().isOptimizeReflection()).isFalse();
    }

    @Test
    @DisplayName("Deve ter valores padrão de performance corretos")
    void shouldHaveCorrectPerformanceDefaults() {
        // Given
        AggregateProperties.Performance performance = properties.getPerformance();

        // Then
        assertThat(performance.isCacheHandlers()).isTrue();
        assertThat(performance.getCacheSize()).isEqualTo(1000);
        assertThat(performance.isParallelValidation()).isFalse();
        assertThat(performance.getMaxValidationThreads()).isEqualTo(4);
        assertThat(performance.isOptimizeReflection()).isTrue();
    }

    @Test
    @DisplayName("Deve configurar snapshot")
    void shouldConfigureSnapshot() {
        // Given
        AggregateProperties.Snapshot snapshot = new AggregateProperties.Snapshot();
        snapshot.setAutoCreate(false);
        snapshot.setThresholdEvents(100);
        snapshot.setCompression(false);
        snapshot.setCompressionAlgorithm("lz4");

        // When
        properties.setSnapshot(snapshot);

        // Then
        assertThat(properties.getSnapshot().isAutoCreate()).isFalse();
        assertThat(properties.getSnapshot().getThresholdEvents()).isEqualTo(100);
        assertThat(properties.getSnapshot().isCompression()).isFalse();
        assertThat(properties.getSnapshot().getCompressionAlgorithm()).isEqualTo("lz4");
    }

    @Test
    @DisplayName("Deve ter valores padrão de snapshot corretos")
    void shouldHaveCorrectSnapshotDefaults() {
        // Given
        AggregateProperties.Snapshot snapshot = properties.getSnapshot();

        // Then
        assertThat(snapshot.isAutoCreate()).isTrue();
        assertThat(snapshot.getThresholdEvents()).isEqualTo(50);
        assertThat(snapshot.isCompression()).isTrue();
        assertThat(snapshot.getCompressionAlgorithm()).isEqualTo("gzip");
    }

    @Test
    @DisplayName("Deve criar instâncias independentes de inner classes")
    void shouldCreateIndependentInnerClassInstances() {
        // Given
        AggregateProperties.Metrics metrics1 = new AggregateProperties.Metrics();
        AggregateProperties.Metrics metrics2 = new AggregateProperties.Metrics();

        // When
        metrics1.setPrefix("prefix1");
        metrics2.setPrefix("prefix2");

        // Then
        assertThat(metrics1.getPrefix()).isEqualTo("prefix1");
        assertThat(metrics2.getPrefix()).isEqualTo("prefix2");
    }
}
