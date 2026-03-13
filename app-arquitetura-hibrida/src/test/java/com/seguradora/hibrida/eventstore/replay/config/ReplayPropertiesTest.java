package com.seguradora.hibrida.eventstore.replay.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReplayProperties Tests")
class ReplayPropertiesTest {

    private ReplayProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ReplayProperties();
    }

    @Test
    @DisplayName("Deve criar com valores padrão")
    void shouldCreateWithDefaultValues() {
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getDefaults()).isNotNull();
        assertThat(properties.getPerformance()).isNotNull();
        assertThat(properties.getMonitoring()).isNotNull();
    }

    @Test
    @DisplayName("Deve ter valores padrão de DefaultSettings corretos")
    void shouldHaveCorrectDefaultSettingsDefaults() {
        ReplayProperties.DefaultSettings defaults = properties.getDefaults();
        assertThat(defaults.getBatchSize()).isEqualTo(100);
        assertThat(defaults.getBatchTimeoutSeconds()).isEqualTo(30);
        assertThat(defaults.getMaxRetries()).isEqualTo(3);
        assertThat(defaults.getRetryDelayMs()).isEqualTo(1000);
        assertThat(defaults.isStopOnError()).isFalse();
        assertThat(defaults.isGenerateDetailedReport()).isFalse();
        assertThat(defaults.getProgressNotificationInterval()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Deve ter valores padrão de Performance corretos")
    void shouldHaveCorrectPerformanceDefaults() {
        ReplayProperties.Performance performance = properties.getPerformance();
        assertThat(performance.getMaxConcurrentReplays()).isEqualTo(5);
        assertThat(performance.getThreadPoolSize()).isEqualTo(10);
        assertThat(performance.getOperationTimeoutSeconds()).isEqualTo(3600);
        assertThat(performance.getMaxQueueSize()).isEqualTo(100);
        assertThat(performance.isEnableEventCache()).isTrue();
        assertThat(performance.getEventCacheTtlSeconds()).isEqualTo(300);
    }

    @Test
    @DisplayName("Deve ter valores padrão de Monitoring corretos")
    void shouldHaveCorrectMonitoringDefaults() {
        ReplayProperties.Monitoring monitoring = properties.getMonitoring();
        assertThat(monitoring.isEnableDetailedMetrics()).isTrue();
        assertThat(monitoring.isEnableHealthChecks()).isTrue();
        assertThat(monitoring.getMetricsCollectionIntervalSeconds()).isEqualTo(60);
        assertThat(monitoring.getMaxHistorySize()).isEqualTo(1000);
        assertThat(monitoring.isEnableDetailedLogging()).isFalse();
        assertThat(monitoring.getLogLevel()).isEqualTo("INFO");
    }

    @Test
    @DisplayName("Deve configurar propriedades principais")
    void shouldConfigureMainProperties() {
        properties.setEnabled(false);
        assertThat(properties.isEnabled()).isFalse();
    }
}
