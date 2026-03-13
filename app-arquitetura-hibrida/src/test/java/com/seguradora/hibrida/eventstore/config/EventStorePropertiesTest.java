package com.seguradora.hibrida.eventstore.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link EventStoreProperties}.
 */
@DisplayName("EventStoreProperties Tests")
class EventStorePropertiesTest {

    private EventStoreProperties properties;

    @BeforeEach
    void setUp() {
        properties = new EventStoreProperties();
    }

    @Test
    @DisplayName("Deve criar com valores padrão")
    void shouldCreateWithDefaultValues() {
        // Then
        assertThat(properties.getSerialization()).isNotNull();
        assertThat(properties.getSnapshot()).isNotNull();
        assertThat(properties.getPerformance()).isNotNull();
        assertThat(properties.getMonitoring()).isNotNull();
        assertThat(properties.getPartitioning()).isNotNull();
        assertThat(properties.getArchive()).isNotNull();
        assertThat(properties.getMaintenance()).isNotNull();
    }

    @Test
    @DisplayName("Deve ter valores padrão de serialização corretos")
    void shouldHaveCorrectSerializationDefaults() {
        // Given
        EventStoreProperties.Serialization serialization = properties.getSerialization();

        // Then
        assertThat(serialization.getFormat()).isEqualTo("json");
        assertThat(serialization.getCompression()).isEqualTo("gzip");
        assertThat(serialization.getCompressionThreshold()).isEqualTo(1024);
        assertThat(serialization.isVersioningEnabled()).isTrue();
    }

    @Test
    @DisplayName("Deve ter valores padrão de snapshot corretos")
    void shouldHaveCorrectSnapshotDefaults() {
        // Given
        EventStoreProperties.Snapshot snapshot = properties.getSnapshot();

        // Then
        assertThat(snapshot.getFrequency()).isEqualTo(50);
        assertThat(snapshot.isAsync()).isTrue();
        assertThat(snapshot.getMaxRetention()).isEqualTo(5);
        assertThat(snapshot.isAutoCleanup()).isTrue();
    }

    @Test
    @DisplayName("Deve ter valores padrão de performance corretos")
    void shouldHaveCorrectPerformanceDefaults() {
        // Given
        EventStoreProperties.Performance performance = properties.getPerformance();

        // Then
        assertThat(performance.getBatchSize()).isEqualTo(100);
        assertThat(performance.getWriteTimeout()).isEqualTo(30);
        assertThat(performance.getReadTimeout()).isEqualTo(15);
        assertThat(performance.isCacheEnabled()).isTrue();
        assertThat(performance.getCacheTtl()).isEqualTo(300);
    }

    @Test
    @DisplayName("Deve ter valores padrão de monitoring corretos")
    void shouldHaveCorrectMonitoringDefaults() {
        // Given
        EventStoreProperties.Monitoring monitoring = properties.getMonitoring();

        // Then
        assertThat(monitoring.isMetricsEnabled()).isTrue();
        assertThat(monitoring.isHealthCheckEnabled()).isTrue();
        assertThat(monitoring.getMetricsInterval()).isEqualTo(60);
        assertThat(monitoring.isPerformanceLogsEnabled()).isFalse();
    }

    @Test
    @DisplayName("Deve ter valores padrão de partitioning corretos")
    void shouldHaveCorrectPartitioningDefaults() {
        // Given
        EventStoreProperties.Partitioning partitioning = properties.getPartitioning();

        // Then
        assertThat(partitioning.isEnabled()).isTrue();
        assertThat(partitioning.getStrategy()).isEqualTo("monthly");
        assertThat(partitioning.getFuturePartitions()).isEqualTo(3);
        assertThat(partitioning.isAutoIndexes()).isTrue();
        assertThat(partitioning.isPerformanceOptimizations()).isTrue();
    }

    @Test
    @DisplayName("Deve ter valores padrão de archive corretos")
    void shouldHaveCorrectArchiveDefaults() {
        // Given
        EventStoreProperties.Archive archive = properties.getArchive();

        // Then
        assertThat(archive.isEnabled()).isTrue();
        assertThat(archive.getArchiveAfterYears()).isEqualTo(2);
        assertThat(archive.isDeleteAfterArchive()).isFalse();
        assertThat(archive.getArchivePauseMs()).isEqualTo(1000);
        assertThat(archive.getStorage()).isNotNull();
        assertThat(archive.getCompaction()).isNotNull();
    }

    @Test
    @DisplayName("Deve ter valores padrão de archive storage corretos")
    void shouldHaveCorrectArchiveStorageDefaults() {
        // Given
        EventStoreProperties.Archive.Storage storage = properties.getArchive().getStorage();

        // Then
        assertThat(storage.getType()).isEqualTo("filesystem");
        assertThat(storage.getBasePath()).isEqualTo("./data/archives");
        assertThat(storage.getBucket()).isEqualTo("eventstore-archives");
        assertThat(storage.getRegion()).isEqualTo("us-east-1");
    }

    @Test
    @DisplayName("Deve ter valores padrão de archive compaction corretos")
    void shouldHaveCorrectArchiveCompactionDefaults() {
        // Given
        EventStoreProperties.Archive.Compaction compaction = properties.getArchive().getCompaction();

        // Then
        assertThat(compaction.isEnabled()).isTrue();
        assertThat(compaction.getCompactAfterMonths()).isEqualTo(6);
        assertThat(compaction.getAlgorithm()).isEqualTo("gzip");
    }

    @Test
    @DisplayName("Deve ter valores padrão de maintenance corretos")
    void shouldHaveCorrectMaintenanceDefaults() {
        // Given
        EventStoreProperties.Maintenance maintenance = properties.getMaintenance();

        // Then
        assertThat(maintenance.isEnabled()).isTrue();
        assertThat(maintenance.isHealthCheckEnabled()).isTrue();
        assertThat(maintenance.getHealthCheckIntervalHours()).isEqualTo(6);
        assertThat(maintenance.isLogCleanupEnabled()).isTrue();
        assertThat(maintenance.getLogRetentionDays()).isEqualTo(90);
        assertThat(maintenance.isReportsEnabled()).isTrue();
    }

    @Test
    @DisplayName("Deve configurar serialização")
    void shouldConfigureSerialization() {
        // Given
        EventStoreProperties.Serialization serialization = new EventStoreProperties.Serialization();
        serialization.setFormat("avro");
        serialization.setCompression("lz4");
        serialization.setCompressionThreshold(2048);
        serialization.setVersioningEnabled(false);

        // When
        properties.setSerialization(serialization);

        // Then
        assertThat(properties.getSerialization().getFormat()).isEqualTo("avro");
        assertThat(properties.getSerialization().getCompression()).isEqualTo("lz4");
        assertThat(properties.getSerialization().getCompressionThreshold()).isEqualTo(2048);
        assertThat(properties.getSerialization().isVersioningEnabled()).isFalse();
    }
}
