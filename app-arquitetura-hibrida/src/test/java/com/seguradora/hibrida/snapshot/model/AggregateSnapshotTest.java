package com.seguradora.hibrida.snapshot.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link AggregateSnapshot}.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("AggregateSnapshot - Testes Unitários")
class AggregateSnapshotTest {

    @Test
    @DisplayName("Deve criar instância com valores corretos")
    void shouldCreateInstanceWithCorrectValues() {
        // Given
        String aggregateId = "test-123";
        String aggregateType = "TestAggregate";
        long version = 10L;
        Map<String, Object> data = Map.of("key", "value", "number", 42);

        // When
        AggregateSnapshot snapshot = new AggregateSnapshot(aggregateId, aggregateType, version, data);

        // Then
        assertThat(snapshot.getSnapshotId()).isNotNull();
        assertThat(snapshot.getAggregateId()).isEqualTo(aggregateId);
        assertThat(snapshot.getAggregateType()).isEqualTo(aggregateType);
        assertThat(snapshot.getVersion()).isEqualTo(version);
        assertThat(snapshot.getData()).containsEntry("key", "value");
        assertThat(snapshot.getData()).containsEntry("number", 42);
        assertThat(snapshot.getTimestamp()).isNotNull();
        assertThat(snapshot.getSchemaVersion()).isEqualTo(1);
        assertThat(snapshot.isCompressed()).isFalse();
        assertThat(snapshot.getOriginalSize()).isZero();
        assertThat(snapshot.getCompressedSize()).isZero();
    }

    @Test
    @DisplayName("Deve criar instância com metadados customizados")
    void shouldCreateInstanceWithCustomMetadata() {
        // Given
        String aggregateId = "test-123";
        String aggregateType = "TestAggregate";
        long version = 10L;
        Map<String, Object> data = Map.of("key", "value");
        Map<String, Object> metadata = Map.of("author", "test-user", "reason", "migration");

        // When
        AggregateSnapshot snapshot = new AggregateSnapshot(aggregateId, aggregateType, version, data, metadata);

        // Then
        assertThat(snapshot.getMetadata())
            .containsEntry("author", "test-user")
            .containsEntry("reason", "migration");
    }

    @Test
    @DisplayName("Deve criar instância via construtor completo (Jackson)")
    void shouldCreateInstanceViaCompleteConstructor() {
        // Given
        String snapshotId = "snap-123";
        String aggregateId = "test-123";
        String aggregateType = "TestAggregate";
        long version = 10L;
        Map<String, Object> data = Map.of("key", "value");
        Instant timestamp = Instant.now();
        Map<String, Object> metadata = Map.of("meta", "data");
        int schemaVersion = 2;
        boolean compressed = true;
        int originalSize = 1000;
        int compressedSize = 500;

        // When
        AggregateSnapshot snapshot = new AggregateSnapshot(
            snapshotId, aggregateId, aggregateType, version, data,
            timestamp, metadata, schemaVersion, compressed, originalSize, compressedSize
        );

        // Then
        assertThat(snapshot.getSnapshotId()).isEqualTo(snapshotId);
        assertThat(snapshot.getAggregateId()).isEqualTo(aggregateId);
        assertThat(snapshot.getAggregateType()).isEqualTo(aggregateType);
        assertThat(snapshot.getVersion()).isEqualTo(version);
        assertThat(snapshot.getData()).containsEntry("key", "value");
        assertThat(snapshot.getTimestamp()).isEqualTo(timestamp);
        assertThat(snapshot.getMetadata()).containsEntry("meta", "data");
        assertThat(snapshot.getSchemaVersion()).isEqualTo(schemaVersion);
        assertThat(snapshot.isCompressed()).isTrue();
        assertThat(snapshot.getOriginalSize()).isEqualTo(originalSize);
        assertThat(snapshot.getCompressedSize()).isEqualTo(compressedSize);
    }

    @Test
    @DisplayName("Deve criar cópia com compressão")
    void shouldCreateCopyWithCompression() {
        // Given
        AggregateSnapshot original = new AggregateSnapshot(
            "test-123", "TestAggregate", 10L, Map.of("key", "value")
        );

        Map<String, Object> compressedData = Map.of("compressed", true);
        int originalSize = 1000;
        int compressedSize = 500;

        // When
        AggregateSnapshot compressed = original.withCompression(compressedData, originalSize, compressedSize);

        // Then
        assertThat(compressed.getSnapshotId()).isEqualTo(original.getSnapshotId());
        assertThat(compressed.getAggregateId()).isEqualTo(original.getAggregateId());
        assertThat(compressed.isCompressed()).isTrue();
        assertThat(compressed.getOriginalSize()).isEqualTo(originalSize);
        assertThat(compressed.getCompressedSize()).isEqualTo(compressedSize);
        assertThat(compressed.getData()).containsEntry("compressed", true);
    }

    @Test
    @DisplayName("Deve adicionar metadado ao snapshot")
    void shouldAddMetadataToSnapshot() {
        // Given
        AggregateSnapshot snapshot = new AggregateSnapshot(
            "test-123", "TestAggregate", 10L, Map.of("key", "value")
        );

        // When
        AggregateSnapshot withMetadata = snapshot.withMetadata("author", "test-user");

        // Then
        assertThat(withMetadata.getMetadata()).containsEntry("author", "test-user");
        assertThat(withMetadata.getSnapshotId()).isEqualTo(snapshot.getSnapshotId());
        assertThat(withMetadata.getAggregateId()).isEqualTo(snapshot.getAggregateId());
    }

    @Test
    @DisplayName("Deve obter valor de metadado")
    void shouldGetMetadataValue() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("author", "test-user");
        metadata.put("count", 42);

        AggregateSnapshot snapshot = new AggregateSnapshot(
            "test-123", "TestAggregate", 10L, Map.of("key", "value"), metadata
        );

        // When/Then
        assertThat(snapshot.getMetadataValue("author")).isEqualTo("test-user");
        assertThat(snapshot.getMetadataValue("count")).isEqualTo(42);
        assertThat(snapshot.getMetadataValue("nonexistent")).isNull();
    }

    @Test
    @DisplayName("Deve obter valor de metadado com tipo específico")
    void shouldGetMetadataValueWithType() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("author", "test-user");
        metadata.put("count", 42);

        AggregateSnapshot snapshot = new AggregateSnapshot(
            "test-123", "TestAggregate", 10L, Map.of("key", "value"), metadata
        );

        // When/Then
        assertThat(snapshot.getMetadataValue("author", String.class)).isEqualTo("test-user");
        assertThat(snapshot.getMetadataValue("count", Integer.class)).isEqualTo(42);
        assertThat(snapshot.getMetadataValue("count", String.class)).isNull(); // Tipo incorreto
        assertThat(snapshot.getMetadataValue("nonexistent", String.class)).isNull();
    }

    @Test
    @DisplayName("Deve verificar se tem metadado específico")
    void shouldCheckIfHasMetadata() {
        // Given
        Map<String, Object> metadata = Map.of("author", "test-user");
        AggregateSnapshot snapshot = new AggregateSnapshot(
            "test-123", "TestAggregate", 10L, Map.of("key", "value"), metadata
        );

        // When/Then
        assertThat(snapshot.hasMetadata("author")).isTrue();
        assertThat(snapshot.hasMetadata("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Deve calcular taxa de compressão corretamente")
    void shouldCalculateCompressionRatioCorrectly() {
        // Given
        AggregateSnapshot snapshot = new AggregateSnapshot(
            "snap-123", "test-123", "TestAggregate", 10L, Map.of("key", "value"),
            Instant.now(), Map.of(), 1, true, 1000, 500
        );

        // When
        double ratio = snapshot.getCompressionRatio();

        // Then
        assertThat(ratio).isEqualTo(0.5); // 50% de compressão
    }

    @Test
    @DisplayName("Deve retornar taxa de compressão zero quando não comprimido")
    void shouldReturnZeroCompressionRatioWhenNotCompressed() {
        // Given
        AggregateSnapshot snapshot = new AggregateSnapshot(
            "test-123", "TestAggregate", 10L, Map.of("key", "value")
        );

        // When
        double ratio = snapshot.getCompressionRatio();

        // Then
        assertThat(ratio).isZero();
    }

    @Test
    @DisplayName("Deve retornar taxa de compressão zero quando tamanho original é zero")
    void shouldReturnZeroCompressionRatioWhenOriginalSizeIsZero() {
        // Given
        AggregateSnapshot snapshot = new AggregateSnapshot(
            "snap-123", "test-123", "TestAggregate", 10L, Map.of("key", "value"),
            Instant.now(), Map.of(), 1, true, 0, 0
        );

        // When
        double ratio = snapshot.getCompressionRatio();

        // Then
        assertThat(ratio).isZero();
    }

    @Test
    @DisplayName("Deve calcular espaço economizado pela compressão")
    void shouldCalculateSpaceSaved() {
        // Given
        AggregateSnapshot snapshot = new AggregateSnapshot(
            "snap-123", "test-123", "TestAggregate", 10L, Map.of("key", "value"),
            Instant.now(), Map.of(), 1, true, 1000, 400
        );

        // When
        int spaceSaved = snapshot.getSpaceSaved();

        // Then
        assertThat(spaceSaved).isEqualTo(600);
    }

    @Test
    @DisplayName("Deve retornar espaço economizado zero quando não comprimido")
    void shouldReturnZeroSpaceSavedWhenNotCompressed() {
        // Given
        AggregateSnapshot snapshot = new AggregateSnapshot(
            "test-123", "TestAggregate", 10L, Map.of("key", "value")
        );

        // When
        int spaceSaved = snapshot.getSpaceSaved();

        // Then
        assertThat(spaceSaved).isZero();
    }

    @Test
    @DisplayName("Deve verificar se compressão foi efetiva")
    void shouldCheckIfCompressionIsEffective() {
        // Given - compressão efetiva (>= 10%)
        AggregateSnapshot effective = new AggregateSnapshot(
            "snap-123", "test-123", "TestAggregate", 10L, Map.of("key", "value"),
            Instant.now(), Map.of(), 1, true, 1000, 800
        );

        // Given - compressão inefetiva (< 10%)
        AggregateSnapshot ineffective = new AggregateSnapshot(
            "snap-123", "test-123", "TestAggregate", 10L, Map.of("key", "value"),
            Instant.now(), Map.of(), 1, true, 1000, 950
        );

        // When/Then
        assertThat(effective.isCompressionEffective()).isTrue();
        assertThat(ineffective.isCompressionEffective()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar cópia imutável dos dados")
    void shouldReturnImmutableCopyOfData() {
        // Given
        Map<String, Object> originalData = new HashMap<>();
        originalData.put("key", "value");

        AggregateSnapshot snapshot = new AggregateSnapshot(
            "test-123", "TestAggregate", 10L, originalData
        );

        // When
        Map<String, Object> dataCopy = snapshot.getDataCopy();
        dataCopy.put("newKey", "newValue");

        // Then
        assertThat(snapshot.getData()).doesNotContainKey("newKey");
        assertThat(snapshot.getData()).containsEntry("key", "value");
    }

    @Test
    @DisplayName("Deve retornar cópia imutável dos metadados")
    void shouldReturnImmutableCopyOfMetadata() {
        // Given
        Map<String, Object> originalMetadata = new HashMap<>();
        originalMetadata.put("author", "test-user");

        AggregateSnapshot snapshot = new AggregateSnapshot(
            "test-123", "TestAggregate", 10L, Map.of("key", "value"), originalMetadata
        );

        // When
        Map<String, Object> metadataCopy = snapshot.getMetadataCopy();
        metadataCopy.put("newMeta", "newValue");

        // Then
        assertThat(snapshot.getMetadata()).doesNotContainKey("newMeta");
        assertThat(snapshot.getMetadata()).containsEntry("author", "test-user");
    }

    @Test
    @DisplayName("Deve ter equals e hashCode corretos")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        String aggregateId = "test-123";
        String aggregateType = "TestAggregate";
        long version = 10L;
        Map<String, Object> data = Map.of("key", "value");

        AggregateSnapshot snapshot1 = new AggregateSnapshot(aggregateId, aggregateType, version, data);
        AggregateSnapshot snapshot2 = new AggregateSnapshot(aggregateId, aggregateType, version, data);
        AggregateSnapshot snapshot3 = new AggregateSnapshot("different-id", aggregateType, version, data);

        // Then
        // Como snapshotId é gerado automaticamente, snapshots diferentes nunca são iguais
        assertThat(snapshot1).isNotEqualTo(snapshot2);
        assertThat(snapshot1).isNotEqualTo(snapshot3);
    }

    @Test
    @DisplayName("Deve ter equals correto quando snapshotId é igual")
    void shouldHaveCorrectEqualsWhenSnapshotIdIsEqual() {
        // Given
        String snapshotId = "snap-123";
        Instant timestamp = Instant.now();

        AggregateSnapshot snapshot1 = new AggregateSnapshot(
            snapshotId, "test-123", "TestAggregate", 10L, Map.of("key", "value"),
            timestamp, Map.of(), 1, false, 0, 0
        );

        AggregateSnapshot snapshot2 = new AggregateSnapshot(
            snapshotId, "test-123", "TestAggregate", 10L, Map.of("key", "value"),
            timestamp, Map.of(), 1, false, 0, 0
        );

        AggregateSnapshot snapshot3 = new AggregateSnapshot(
            "different-id", "test-123", "TestAggregate", 10L, Map.of("key", "value"),
            timestamp, Map.of(), 1, false, 0, 0
        );

        // Then
        assertThat(snapshot1).isEqualTo(snapshot2);
        assertThat(snapshot1.hashCode()).isEqualTo(snapshot2.hashCode());
        assertThat(snapshot1).isNotEqualTo(snapshot3);
    }

    @Test
    @DisplayName("Deve ter toString útil")
    void shouldHaveUsefulToString() {
        // Given
        AggregateSnapshot snapshot = new AggregateSnapshot(
            "test-123", "TestAggregate", 10L, Map.of("key", "value")
        );

        // When
        String toString = snapshot.toString();

        // Then
        assertThat(toString)
            .contains("AggregateSnapshot")
            .contains("aggregateId")
            .contains("aggregateType")
            .contains("version");
    }

    @Test
    @DisplayName("Deve lidar com dados null no construtor Jackson")
    void shouldHandleNullDataInJacksonConstructor() {
        // Given/When
        AggregateSnapshot snapshot = new AggregateSnapshot(
            "snap-123", "test-123", "TestAggregate", 10L, null,
            Instant.now(), null, 1, false, 0, 0
        );

        // Then
        assertThat(snapshot.getData()).isEmpty();
        assertThat(snapshot.getMetadata()).isEmpty();
    }

    @Test
    @DisplayName("Deve proteger dados internos contra modificações externas")
    void shouldProtectInternalDataFromExternalModifications() {
        // Given
        Map<String, Object> externalData = new HashMap<>();
        externalData.put("key", "value");

        AggregateSnapshot snapshot = new AggregateSnapshot(
            "test-123", "TestAggregate", 10L, externalData
        );

        // When - tentar modificar mapa original
        externalData.put("newKey", "newValue");

        // Then - snapshot não deve ser afetado
        assertThat(snapshot.getData()).doesNotContainKey("newKey");
        assertThat(snapshot.getData()).hasSize(1);
    }
}
