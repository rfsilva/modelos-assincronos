package com.seguradora.hibrida.projection.rebuild;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProjectionRebuildProperties Tests")
class ProjectionRebuildPropertiesTest {

    private ProjectionRebuildProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ProjectionRebuildProperties();
    }

    @Test
    @DisplayName("Deve criar com valores padrão")
    void shouldCreateWithDefaultValues() {
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getBatchSize()).isEqualTo(100);
        assertThat(properties.getLagThresholdForRebuild()).isEqualTo(10000L);
        assertThat(properties.getLagThresholdForFullRebuild()).isEqualTo(50000L);
        assertThat(properties.getErrorThresholdForRebuild()).isEqualTo(0.1);
        assertThat(properties.getErrorRateThresholdForFullRebuild()).isEqualTo(0.2);
        assertThat(properties.getMaxErrorsBeforeStop()).isEqualTo(1000);
        assertThat(properties.getAutoCheckIntervalSeconds()).isEqualTo(300);
        assertThat(properties.getTimeoutSeconds()).isEqualTo(3600);
        assertThat(properties.isDetailedLogging()).isFalse();
        assertThat(properties.getMaxConcurrentRebuilds()).isEqualTo(3);
        assertThat(properties.isAutoPauseOnErrors()).isTrue();
        assertThat(properties.isAutoRetryAfterFailure()).isTrue();
        assertThat(properties.getRetryDelaySeconds()).isEqualTo(1800);
    }

    @Test
    @DisplayName("Deve configurar propriedades")
    void shouldConfigureProperties() {
        properties.setEnabled(false);
        properties.setBatchSize(200);
        properties.setLagThresholdForRebuild(20000L);
        properties.setDetailedLogging(true);
        properties.setMaxConcurrentRebuilds(5);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getBatchSize()).isEqualTo(200);
        assertThat(properties.getLagThresholdForRebuild()).isEqualTo(20000L);
        assertThat(properties.isDetailedLogging()).isTrue();
        assertThat(properties.getMaxConcurrentRebuilds()).isEqualTo(5);
    }
}
