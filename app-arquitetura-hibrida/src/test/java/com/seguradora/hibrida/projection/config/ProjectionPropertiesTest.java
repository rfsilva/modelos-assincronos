package com.seguradora.hibrida.projection.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ProjectionProperties}.
 */
@DisplayName("ProjectionProperties Tests")
class ProjectionPropertiesTest {

    private ProjectionProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ProjectionProperties();
    }

    @Test
    @DisplayName("Deve criar com valores padrão")
    void shouldCreateWithDefaultValues() {
        // Then
        assertThat(properties.getBatchSize()).isEqualTo(50);
        assertThat(properties.isParallel()).isTrue();
        assertThat(properties.getTimeoutSeconds()).isEqualTo(30);
        assertThat(properties.getThreadPool()).isNotNull();
        assertThat(properties.getRetry()).isNotNull();
        assertThat(properties.getMonitoring()).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar propriedades principais")
    void shouldConfigureMainProperties() {
        // When
        properties.setBatchSize(100);
        properties.setParallel(false);
        properties.setTimeoutSeconds(60);

        // Then
        assertThat(properties.getBatchSize()).isEqualTo(100);
        assertThat(properties.isParallel()).isFalse();
        assertThat(properties.getTimeoutSeconds()).isEqualTo(60);
    }

    @Test
    @DisplayName("Deve ter valores padrão de ThreadPool corretos")
    void shouldHaveCorrectThreadPoolDefaults() {
        // Given
        ProjectionProperties.ThreadPool threadPool = properties.getThreadPool();

        // Then
        assertThat(threadPool.getCoreSize()).isEqualTo(5);
        assertThat(threadPool.getMaxSize()).isEqualTo(20);
        assertThat(threadPool.getQueueCapacity()).isEqualTo(1000);
        assertThat(threadPool.getThreadNamePrefix()).isEqualTo("projection-");
        assertThat(threadPool.getKeepAliveSeconds()).isEqualTo(60);
    }

    @Test
    @DisplayName("Deve configurar ThreadPool")
    void shouldConfigureThreadPool() {
        // Given
        ProjectionProperties.ThreadPool threadPool = new ProjectionProperties.ThreadPool();
        threadPool.setCoreSize(10);
        threadPool.setMaxSize(40);
        threadPool.setQueueCapacity(2000);
        threadPool.setThreadNamePrefix("custom-");
        threadPool.setKeepAliveSeconds(120);

        // When
        properties.setThreadPool(threadPool);

        // Then
        assertThat(properties.getThreadPool().getCoreSize()).isEqualTo(10);
        assertThat(properties.getThreadPool().getMaxSize()).isEqualTo(40);
        assertThat(properties.getThreadPool().getQueueCapacity()).isEqualTo(2000);
        assertThat(properties.getThreadPool().getThreadNamePrefix()).isEqualTo("custom-");
        assertThat(properties.getThreadPool().getKeepAliveSeconds()).isEqualTo(120);
    }

    @Test
    @DisplayName("Deve ter valores padrão de Retry corretos")
    void shouldHaveCorrectRetryDefaults() {
        // Given
        ProjectionProperties.Retry retry = properties.getRetry();

        // Then
        assertThat(retry.getMaxAttempts()).isEqualTo(3);
        assertThat(retry.getBackoffMultiplier()).isEqualTo(2.0);
        assertThat(retry.getInitialDelayMs()).isEqualTo(1000);
        assertThat(retry.getMaxDelayMs()).isEqualTo(30000);
        assertThat(retry.getJitterPercent()).isEqualTo(0.1);
    }

    @Test
    @DisplayName("Deve configurar Retry")
    void shouldConfigureRetry() {
        // Given
        ProjectionProperties.Retry retry = new ProjectionProperties.Retry();
        retry.setMaxAttempts(5);
        retry.setBackoffMultiplier(3.0);
        retry.setInitialDelayMs(2000);
        retry.setMaxDelayMs(60000);
        retry.setJitterPercent(0.2);

        // When
        properties.setRetry(retry);

        // Then
        assertThat(properties.getRetry().getMaxAttempts()).isEqualTo(5);
        assertThat(properties.getRetry().getBackoffMultiplier()).isEqualTo(3.0);
        assertThat(properties.getRetry().getInitialDelayMs()).isEqualTo(2000);
        assertThat(properties.getRetry().getMaxDelayMs()).isEqualTo(60000);
        assertThat(properties.getRetry().getJitterPercent()).isEqualTo(0.2);
    }

    @Test
    @DisplayName("Deve ter valores padrão de Monitoring corretos")
    void shouldHaveCorrectMonitoringDefaults() {
        // Given
        ProjectionProperties.Monitoring monitoring = properties.getMonitoring();

        // Then
        assertThat(monitoring.isEnabled()).isTrue();
        assertThat(monitoring.getMetricsIntervalSeconds()).isEqualTo(60);
        assertThat(monitoring.getLagThreshold()).isEqualTo(1000);
        assertThat(monitoring.getErrorRateThreshold()).isEqualTo(0.05);
        assertThat(monitoring.isDetailedLogging()).isFalse();
    }

    @Test
    @DisplayName("Deve configurar Monitoring")
    void shouldConfigureMonitoring() {
        // Given
        ProjectionProperties.Monitoring monitoring = new ProjectionProperties.Monitoring();
        monitoring.setEnabled(false);
        monitoring.setMetricsIntervalSeconds(120);
        monitoring.setLagThreshold(2000);
        monitoring.setErrorRateThreshold(0.1);
        monitoring.setDetailedLogging(true);

        // When
        properties.setMonitoring(monitoring);

        // Then
        assertThat(properties.getMonitoring().isEnabled()).isFalse();
        assertThat(properties.getMonitoring().getMetricsIntervalSeconds()).isEqualTo(120);
        assertThat(properties.getMonitoring().getLagThreshold()).isEqualTo(2000);
        assertThat(properties.getMonitoring().getErrorRateThreshold()).isEqualTo(0.1);
        assertThat(properties.getMonitoring().isDetailedLogging()).isTrue();
    }
}
