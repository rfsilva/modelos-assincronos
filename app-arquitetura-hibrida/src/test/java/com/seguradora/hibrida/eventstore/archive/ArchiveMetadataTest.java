package com.seguradora.hibrida.eventstore.archive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ArchiveMetadata}.
 */
@DisplayName("ArchiveMetadata Tests")
class ArchiveMetadataTest {

    @Test
    @DisplayName("Builder deve criar instância com campos corretos")
    void builderShouldCreateInstanceWithCorrectFields() {
        Instant now = Instant.now();
        ArchiveMetadata metadata = ArchiveMetadata.builder()
                .partitionName("events_2022_01")
                .archiveKey("eventstore/archives/2022/events_2022_01.json.gz")
                .eventCount(1000L)
                .compressedSize(50000L)
                .archivedAt(now)
                .status("ARCHIVED")
                .build();

        assertThat(metadata.getPartitionName()).isEqualTo("events_2022_01");
        assertThat(metadata.getArchiveKey()).contains("events_2022_01");
        assertThat(metadata.getEventCount()).isEqualTo(1000L);
        assertThat(metadata.getCompressedSize()).isEqualTo(50000L);
        assertThat(metadata.getArchivedAt()).isEqualTo(now);
        assertThat(metadata.getStatus()).isEqualTo("ARCHIVED");
    }

    @Test
    @DisplayName("isActive deve retornar true quando status é ARCHIVED")
    void isActiveShouldReturnTrueWhenStatusIsArchived() {
        ArchiveMetadata metadata = ArchiveMetadata.builder()
                .status("ARCHIVED")
                .build();

        assertThat(metadata.isActive()).isTrue();
    }

    @Test
    @DisplayName("isActive deve retornar false quando status não é ARCHIVED")
    void isActiveShouldReturnFalseWhenStatusNotArchived() {
        ArchiveMetadata metadata = ArchiveMetadata.builder()
                .status("RESTORED")
                .build();

        assertThat(metadata.isActive()).isFalse();
    }

    @Test
    @DisplayName("isRestored deve retornar true quando status é RESTORED")
    void isRestoredShouldReturnTrueWhenStatusIsRestored() {
        ArchiveMetadata metadata = ArchiveMetadata.builder()
                .status("RESTORED")
                .build();

        assertThat(metadata.isRestored()).isTrue();
    }

    @Test
    @DisplayName("isRestored deve retornar false quando status não é RESTORED")
    void isRestoredShouldReturnFalseWhenStatusNotRestored() {
        ArchiveMetadata metadata = ArchiveMetadata.builder()
                .status("ARCHIVED")
                .build();

        assertThat(metadata.isRestored()).isFalse();
    }
}
