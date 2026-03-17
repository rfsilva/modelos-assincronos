package com.seguradora.hibrida.snapshot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Testes unitários para {@link SnapshotEntry}.
 */
@DisplayName("SnapshotEntry Tests")
class SnapshotEntryTest {

    private static final String SNAPSHOT_ID   = "snap-001";
    private static final String AGGREGATE_ID  = "agg-001";
    private static final String AGGREGATE_TYPE = "SeguradoAggregate";
    private static final Long VERSION         = 50L;
    private static final Instant TIMESTAMP    = Instant.parse("2026-01-10T12:00:00Z");

    private SnapshotEntry entry;

    @BeforeEach
    void setUp() {
        entry = new SnapshotEntry(SNAPSHOT_ID, AGGREGATE_ID, AGGREGATE_TYPE, VERSION,
                Map.of("name", "João"), TIMESTAMP);
    }

    // =========================================================================
    // Anotações JPA
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Entity")
    void shouldBeAnnotatedWithEntity() {
        assertThat(SnapshotEntry.class.isAnnotationPresent(Entity.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @Table(name='snapshots')")
    void shouldBeAnnotatedWithTableNameSnapshots() {
        Table table = SnapshotEntry.class.getAnnotation(Table.class);
        assertThat(table).isNotNull();
        assertThat(table.name()).isEqualTo("snapshots");
    }

    // =========================================================================
    // Construtor com parâmetros
    // =========================================================================

    @Nested
    @DisplayName("Construtor com parâmetros")
    class ConstrutorComParametros {

        @Test
        @DisplayName("Deve preencher os campos corretamente")
        void shouldFillFieldsCorrectly() {
            assertThat(entry.getSnapshotId()).isEqualTo(SNAPSHOT_ID);
            assertThat(entry.getAggregateId()).isEqualTo(AGGREGATE_ID);
            assertThat(entry.getAggregateType()).isEqualTo(AGGREGATE_TYPE);
            assertThat(entry.getVersion()).isEqualTo(VERSION);
            assertThat(entry.getTimestamp()).isEqualTo(TIMESTAMP);
        }

        @Test
        @DisplayName("Deve inicializar schemaVersion = 1")
        void shouldInitializeSchemaVersionToOne() {
            assertThat(entry.getSchemaVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("Deve inicializar compressed = false")
        void shouldInitializeCompressedToFalse() {
            assertThat(entry.isCompressed()).isFalse();
        }
    }

    // =========================================================================
    // No-args constructor
    // =========================================================================

    @Test
    @DisplayName("No-args constructor deve criar instância válida")
    void noArgsConstructorShouldWork() {
        SnapshotEntry e = new SnapshotEntry();
        assertThat(e).isNotNull();
    }

    // =========================================================================
    // isCompressed()
    // =========================================================================

    @Test
    @DisplayName("isCompressed() deve retornar false quando compressed é null")
    void shouldReturnFalseWhenCompressedIsNull() {
        entry.setCompressed(null);
        assertThat(entry.isCompressed()).isFalse();
    }

    @Test
    @DisplayName("isCompressed() deve retornar true quando compressed = true")
    void shouldReturnTrueWhenCompressedIsTrue() {
        entry.setCompressed(true);
        assertThat(entry.isCompressed()).isTrue();
    }

    // =========================================================================
    // getCompressionRatio()
    // =========================================================================

    @Nested
    @DisplayName("getCompressionRatio()")
    class CompressionRatio {

        @Test
        @DisplayName("Deve retornar 0 quando não comprimido")
        void shouldReturnZeroWhenNotCompressed() {
            assertThat(entry.getCompressionRatio()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Deve retornar 0 quando originalSize é null")
        void shouldReturnZeroWhenOriginalSizeIsNull() {
            entry.setCompressed(true);
            entry.setOriginalSize(null);
            assertThat(entry.getCompressionRatio()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Deve calcular taxa corretamente")
        void shouldCalculateRatioCorrectly() {
            // original=1000, compressed=600 → ratio = 0.4
            entry.setCompressed(true);
            entry.setOriginalSize(1000);
            entry.setCompressedSize(600);

            assertThat(entry.getCompressionRatio()).isCloseTo(0.4, within(0.001));
        }
    }

    // =========================================================================
    // getSpaceSaved()
    // =========================================================================

    @Test
    @DisplayName("getSpaceSaved() deve retornar 0 quando não comprimido")
    void shouldReturnZeroSpaceSavedWhenNotCompressed() {
        assertThat(entry.getSpaceSaved()).isEqualTo(0);
    }

    @Test
    @DisplayName("getSpaceSaved() deve calcular espaço economizado")
    void shouldCalculateSpaceSaved() {
        entry.setCompressed(true);
        entry.setOriginalSize(1000);
        entry.setCompressedSize(400);

        assertThat(entry.getSpaceSaved()).isEqualTo(600);
    }

    // =========================================================================
    // isCompressionEffective()
    // =========================================================================

    @Test
    @DisplayName("isCompressionEffective() deve retornar false quando não comprimido")
    void shouldReturnFalseWhenNotCompressed() {
        assertThat(entry.isCompressionEffective()).isFalse();
    }

    @Test
    @DisplayName("isCompressionEffective() deve retornar true quando ratio >= 10%")
    void shouldReturnTrueWhenRatioAtLeast10Percent() {
        // original=1000, compressed=850 → ratio = 15%
        entry.setCompressed(true);
        entry.setOriginalSize(1000);
        entry.setCompressedSize(850);

        assertThat(entry.isCompressionEffective()).isTrue();
    }

    @Test
    @DisplayName("isCompressionEffective() deve retornar false quando ratio < 10%")
    void shouldReturnFalseWhenRatioBelow10Percent() {
        // original=1000, compressed=950 → ratio = 5%
        entry.setCompressed(true);
        entry.setOriginalSize(1000);
        entry.setCompressedSize(950);

        assertThat(entry.isCompressionEffective()).isFalse();
    }

    // =========================================================================
    // getEffectiveSize()
    // =========================================================================

    @Nested
    @DisplayName("getEffectiveSize()")
    class EffectiveSize {

        @Test
        @DisplayName("Deve retornar compressedSize quando comprimido")
        void shouldReturnCompressedSizeWhenCompressed() {
            entry.setCompressed(true);
            entry.setCompressedSize(600);
            entry.setOriginalSize(1000);

            assertThat(entry.getEffectiveSize()).isEqualTo(600);
        }

        @Test
        @DisplayName("Deve retornar originalSize quando não comprimido")
        void shouldReturnOriginalSizeWhenNotCompressed() {
            entry.setOriginalSize(1000);
            entry.setCompressed(false);

            assertThat(entry.getEffectiveSize()).isEqualTo(1000);
        }

        @Test
        @DisplayName("Deve retornar 0 quando originalSize é null e não comprimido")
        void shouldReturnZeroWhenOriginalSizeNullAndNotCompressed() {
            entry.setCompressed(false);
            entry.setOriginalSize(null);

            assertThat(entry.getEffectiveSize()).isEqualTo(0);
        }
    }

    // =========================================================================
    // Setters (Lombok @Setter)
    // =========================================================================

    @Test
    @DisplayName("Deve aceitar setters e retornar valores atualizados")
    void shouldAcceptSetters() {
        entry.setDataHash("hash123");
        entry.setCompressionAlgorithm("GZIP");
        entry.setCreatedBy("system");

        assertThat(entry.getDataHash()).isEqualTo("hash123");
        assertThat(entry.getCompressionAlgorithm()).isEqualTo("GZIP");
        assertThat(entry.getCreatedBy()).isEqualTo("system");
    }
}
