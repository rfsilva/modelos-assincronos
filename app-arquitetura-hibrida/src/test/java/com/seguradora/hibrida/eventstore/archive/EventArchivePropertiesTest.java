package com.seguradora.hibrida.eventstore.archive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventArchiveProperties Tests")
class EventArchivePropertiesTest {

    private EventArchiveProperties properties;

    @BeforeEach
    void setUp() {
        properties = new EventArchiveProperties();
    }

    @Test
    @DisplayName("Deve criar com valores padrão")
    void shouldCreateWithDefaultValues() {
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getArchiveAfterYears()).isEqualTo(2);
        assertThat(properties.isDeleteAfterArchive()).isFalse();
        assertThat(properties.getArchivePauseMs()).isEqualTo(1000);
        assertThat(properties.getStorage()).isNotNull();
        assertThat(properties.getCompaction()).isNotNull();
        assertThat(properties.getMonitoring()).isNotNull();
    }

    @Test
    @DisplayName("Deve ter valores padrão de Storage corretos")
    void shouldHaveCorrectStorageDefaults() {
        EventArchiveProperties.Storage storage = properties.getStorage();
        assertThat(storage.getType()).isEqualTo("filesystem");
        assertThat(storage.getBasePath()).isEqualTo("./data/archives");
        assertThat(storage.getBucket()).isEqualTo("eventstore-archives");
        assertThat(storage.getRegion()).isEqualTo("us-east-1");
    }

    @Test
    @DisplayName("Deve ter valores padrão de Compaction corretos")
    void shouldHaveCorrectCompactionDefaults() {
        EventArchiveProperties.Compaction compaction = properties.getCompaction();
        assertThat(compaction.isEnabled()).isTrue();
        assertThat(compaction.getCompactAfterMonths()).isEqualTo(6);
        assertThat(compaction.getAlgorithm()).isEqualTo("gzip");
    }

    @Test
    @DisplayName("Deve ter valores padrão de Monitoring corretos")
    void shouldHaveCorrectMonitoringDefaults() {
        EventArchiveProperties.Monitoring monitoring = properties.getMonitoring();
        assertThat(monitoring.isMetricsEnabled()).isTrue();
        assertThat(monitoring.isAlertsEnabled()).isTrue();
        assertThat(monitoring.getFailureThreshold()).isEqualTo(0.1);
    }

    @Test
    @DisplayName("Deve configurar propriedades principais")
    void shouldConfigureMainProperties() {
        properties.setEnabled(false);
        properties.setArchiveAfterYears(5);
        properties.setDeleteAfterArchive(true);
        properties.setArchivePauseMs(2000);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getArchiveAfterYears()).isEqualTo(5);
        assertThat(properties.isDeleteAfterArchive()).isTrue();
        assertThat(properties.getArchivePauseMs()).isEqualTo(2000);
    }
}
