package com.seguradora.hibrida.snapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para {@link SnapshotProperties}.
 */
@DisplayName("SnapshotProperties Tests")
class SnapshotPropertiesTest {

    private SnapshotProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SnapshotProperties();
    }

    @Test
    @DisplayName("Deve criar com valores padrão")
    void shouldCreateWithDefaultValues() {
        // Then
        assertThat(properties.getSnapshotThreshold()).isEqualTo(50);
        assertThat(properties.getMaxSnapshotsPerAggregate()).isEqualTo(5);
        assertThat(properties.getCompressionThreshold()).isEqualTo(1024);
        assertThat(properties.getCompressionAlgorithm()).isEqualTo("GZIP");
        assertThat(properties.isCompressionEnabled()).isTrue();
        assertThat(properties.isAutoCleanupEnabled()).isTrue();
        assertThat(properties.getCleanupIntervalHours()).isEqualTo(24);
        assertThat(properties.isAsyncSnapshotCreation()).isTrue();
        assertThat(properties.getOperationTimeoutSeconds()).isEqualTo(30);
        assertThat(properties.isIntegrityValidationEnabled()).isTrue();
        assertThat(properties.isMetricsEnabled()).isTrue();
        assertThat(properties.getRetentionDays()).isEqualTo(365);
        assertThat(properties.getAsyncThreadPoolSize()).isEqualTo(5);
        assertThat(properties.getAsyncQueueCapacity()).isEqualTo(100);
        assertThat(properties.getAsyncThreadNamePrefix()).isEqualTo("snapshot-");
        assertThat(properties.isHealthCheckEnabled()).isTrue();
        assertThat(properties.getHealthCheckIntervalSeconds()).isEqualTo(60);
        assertThat(properties.getMaxConsecutiveFailures()).isEqualTo(3);
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.isCacheEnabled()).isFalse();
        assertThat(properties.getCacheMaxSize()).isEqualTo(100);
        assertThat(properties.getCacheTtlMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("Deve configurar todas as propriedades")
    void shouldConfigureAllProperties() {
        // When
        properties.setSnapshotThreshold(100);
        properties.setMaxSnapshotsPerAggregate(10);
        properties.setCompressionThreshold(2048);
        properties.setCompressionAlgorithm("LZ4");
        properties.setCompressionEnabled(false);
        properties.setAutoCleanupEnabled(false);
        properties.setCleanupIntervalHours(48);
        properties.setAsyncSnapshotCreation(false);
        properties.setOperationTimeoutSeconds(60);
        properties.setIntegrityValidationEnabled(false);
        properties.setMetricsEnabled(false);
        properties.setRetentionDays(730);
        properties.setAsyncThreadPoolSize(10);
        properties.setAsyncQueueCapacity(200);
        properties.setAsyncThreadNamePrefix("custom-");
        properties.setHealthCheckEnabled(false);
        properties.setHealthCheckIntervalSeconds(120);
        properties.setMaxConsecutiveFailures(5);
        properties.setEnabled(false);
        properties.setCacheEnabled(true);
        properties.setCacheMaxSize(200);
        properties.setCacheTtlMinutes(60);

        // Then
        assertThat(properties.getSnapshotThreshold()).isEqualTo(100);
        assertThat(properties.getMaxSnapshotsPerAggregate()).isEqualTo(10);
        assertThat(properties.getCompressionThreshold()).isEqualTo(2048);
        assertThat(properties.getCompressionAlgorithm()).isEqualTo("LZ4");
        assertThat(properties.isCompressionEnabled()).isFalse();
        assertThat(properties.isAutoCleanupEnabled()).isFalse();
        assertThat(properties.getCleanupIntervalHours()).isEqualTo(48);
        assertThat(properties.isAsyncSnapshotCreation()).isFalse();
        assertThat(properties.getOperationTimeoutSeconds()).isEqualTo(60);
        assertThat(properties.isIntegrityValidationEnabled()).isFalse();
        assertThat(properties.isMetricsEnabled()).isFalse();
        assertThat(properties.getRetentionDays()).isEqualTo(730);
        assertThat(properties.getAsyncThreadPoolSize()).isEqualTo(10);
        assertThat(properties.getAsyncQueueCapacity()).isEqualTo(200);
        assertThat(properties.getAsyncThreadNamePrefix()).isEqualTo("custom-");
        assertThat(properties.isHealthCheckEnabled()).isFalse();
        assertThat(properties.getHealthCheckIntervalSeconds()).isEqualTo(120);
        assertThat(properties.getMaxConsecutiveFailures()).isEqualTo(5);
        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.isCacheEnabled()).isTrue();
        assertThat(properties.getCacheMaxSize()).isEqualTo(200);
        assertThat(properties.getCacheTtlMinutes()).isEqualTo(60);
    }

    @Test
    @DisplayName("Deve validar configurações com sucesso")
    void shouldValidateSuccessfully() {
        // Given
        properties.setSnapshotThreshold(50);
        properties.setMaxSnapshotsPerAggregate(5);

        // When/Then - não deve lançar exceção
        properties.validate();
    }

    @Test
    @DisplayName("Deve falhar validação com snapshotThreshold inválido")
    void shouldFailValidationWithInvalidSnapshotThreshold() {
        // Given
        properties.setSnapshotThreshold(0);

        // When/Then
        assertThatThrownBy(() -> properties.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("snapshotThreshold must be positive");
    }

    @Test
    @DisplayName("Deve falhar validação com maxSnapshotsPerAggregate inválido")
    void shouldFailValidationWithInvalidMaxSnapshots() {
        // Given
        properties.setMaxSnapshotsPerAggregate(-1);

        // When/Then
        assertThatThrownBy(() -> properties.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxSnapshotsPerAggregate must be positive");
    }

    @Test
    @DisplayName("Deve determinar quando comprimir baseado no tamanho")
    void shouldDetermineWhenToCompress() {
        // Given
        properties.setCompressionEnabled(true);
        properties.setCompressionThreshold(1024);

        // Then
        assertThat(properties.shouldCompress(500)).isFalse();
        assertThat(properties.shouldCompress(1024)).isTrue();
        assertThat(properties.shouldCompress(2048)).isTrue();
    }

    @Test
    @DisplayName("Não deve comprimir quando compressão desabilitada")
    void shouldNotCompressWhenDisabled() {
        // Given
        properties.setCompressionEnabled(false);
        properties.setCompressionThreshold(1024);

        // Then
        assertThat(properties.shouldCompress(2048)).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se async está habilitado")
    void shouldCheckIfAsyncEnabled() {
        // Given
        properties.setAsyncSnapshotCreation(true);

        // Then
        assertThat(properties.isAsyncEnabled()).isTrue();

        // When
        properties.setAsyncSnapshotCreation(false);

        // Then
        assertThat(properties.isAsyncEnabled()).isFalse();
    }
}
