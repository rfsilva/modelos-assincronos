package com.seguradora.hibrida.projection.consistency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProjectionConsistencyProperties Tests")
class ProjectionConsistencyPropertiesTest {

    private ProjectionConsistencyProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ProjectionConsistencyProperties();
    }

    @Test
    @DisplayName("Deve criar com valores padrão")
    void shouldCreateWithDefaultValues() {
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getCheckIntervalSeconds()).isEqualTo(300);
        assertThat(properties.getMaxAllowedLag()).isEqualTo(1000L);
        assertThat(properties.getCriticalLagThreshold()).isEqualTo(10000L);
        assertThat(properties.getMaxAllowedErrorRate()).isEqualTo(0.05);
        assertThat(properties.getCriticalErrorRate()).isEqualTo(0.2);
        assertThat(properties.getStaleThresholdMinutes()).isEqualTo(30);
        assertThat(properties.getMaxErrorDurationMinutes()).isEqualTo(60);
        assertThat(properties.getMaxPauseDurationMinutes()).isEqualTo(120);
        assertThat(properties.getOrphanThresholdHours()).isEqualTo(24);
        assertThat(properties.isAutoRestartOnHighLag()).isTrue();
        assertThat(properties.isAutoRestartOnStale()).isTrue();
        assertThat(properties.isAutoPauseOnHighErrorRate()).isTrue();
        assertThat(properties.isAutoRebuildOnPersistentError()).isTrue();
        assertThat(properties.isDetailedLogging()).isFalse();
        assertThat(properties.isAlertsEnabled()).isTrue();
        assertThat(properties.getHealthScoreAlertThreshold()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Deve configurar propriedades")
    void shouldConfigureProperties() {
        properties.setEnabled(false);
        properties.setCheckIntervalSeconds(600);
        properties.setMaxAllowedLag(2000L);
        properties.setDetailedLogging(true);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getCheckIntervalSeconds()).isEqualTo(600);
        assertThat(properties.getMaxAllowedLag()).isEqualTo(2000L);
        assertThat(properties.isDetailedLogging()).isTrue();
    }
}
