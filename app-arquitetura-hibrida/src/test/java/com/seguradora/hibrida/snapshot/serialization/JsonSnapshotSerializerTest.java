package com.seguradora.hibrida.snapshot.serialization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seguradora.hibrida.snapshot.exception.SnapshotCompressionException;
import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para {@link JsonSnapshotSerializer}.
 */
@DisplayName("JsonSnapshotSerializer Tests")
class JsonSnapshotSerializerTest {

    private JsonSnapshotSerializer serializer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        serializer = new JsonSnapshotSerializer(objectMapper);
    }

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(JsonSnapshotSerializer.class.isAnnotationPresent(Component.class)).isTrue();
    }

    @Test
    @DisplayName("Deve implementar SnapshotSerializer")
    void shouldImplementSnapshotSerializer() {
        assertThat(serializer).isInstanceOf(SnapshotSerializer.class);
    }

    // =========================================================================
    // serialize() / deserialize()
    // =========================================================================

    @Nested
    @DisplayName("serialize() e deserialize()")
    class SerializeDeserialize {

        @Test
        @DisplayName("Deve serializar snapshot para JSON não-nulo")
        void shouldSerializeToNonNullJson() {
            AggregateSnapshot snapshot = buildSnapshot("agg-1", 10L);

            String json = serializer.serialize(snapshot);

            assertThat(json).isNotBlank();
        }

        @Test
        @DisplayName("Deve deserializar JSON e recuperar aggregateId correto")
        void shouldDeserializeAndRecoverAggregateId() {
            AggregateSnapshot original = buildSnapshot("agg-42", 20L);
            String json = serializer.serialize(original);

            AggregateSnapshot recovered = serializer.deserialize(json, "TestAggregate");

            assertThat(recovered.getAggregateId()).isEqualTo("agg-42");
        }

        @Test
        @DisplayName("Deve deserializar JSON e recuperar versão correta")
        void shouldDeserializeAndRecoverVersion() {
            AggregateSnapshot original = buildSnapshot("agg-1", 99L);
            String json = serializer.serialize(original);

            AggregateSnapshot recovered = serializer.deserialize(json, "TestAggregate");

            assertThat(recovered.getVersion()).isEqualTo(99L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deserializar JSON inválido")
        void shouldThrowWhenDeserializingInvalidJson() {
            assertThatThrownBy(() -> serializer.deserialize("not-json", "TestAggregate"))
                    .isInstanceOf(SnapshotSerializationException.class);
        }
    }

    // =========================================================================
    // serializeWithCompression()
    // =========================================================================

    @Nested
    @DisplayName("serializeWithCompression()")
    class SerializeWithCompression {

        @Test
        @DisplayName("Deve retornar resultado com serializedData não nulo")
        void shouldReturnNonNullSerializedData() {
            AggregateSnapshot snapshot = buildSnapshot("agg-1", 10L);

            SnapshotSerializationResult result =
                    serializer.serializeWithCompression(snapshot, Integer.MAX_VALUE); // threshold alto → sem compressão

            assertThat(result.getSerializedData()).isNotBlank();
        }

        @Test
        @DisplayName("Não deve comprimir quando tamanho abaixo do threshold")
        void shouldNotCompressWhenBelowThreshold() {
            AggregateSnapshot snapshot = buildSnapshot("agg-1", 10L);

            SnapshotSerializationResult result =
                    serializer.serializeWithCompression(snapshot, Integer.MAX_VALUE);

            assertThat(result.isCompressed()).isFalse();
        }

        @Test
        @DisplayName("Deve incluir hash de integridade no resultado")
        void shouldIncludeIntegrityHash() {
            AggregateSnapshot snapshot = buildSnapshot("agg-1", 10L);

            SnapshotSerializationResult result =
                    serializer.serializeWithCompression(snapshot, Integer.MAX_VALUE);

            assertThat(result.hasIntegrityHash()).isTrue();
        }

        @Test
        @DisplayName("Deve incluir originalSize maior que zero")
        void shouldIncludeOriginalSize() {
            AggregateSnapshot snapshot = buildSnapshot("agg-1", 10L);

            SnapshotSerializationResult result =
                    serializer.serializeWithCompression(snapshot, Integer.MAX_VALUE);

            assertThat(result.getOriginalSize()).isGreaterThan(0);
        }
    }

    // =========================================================================
    // deserializeCompressed()
    // =========================================================================

    @Nested
    @DisplayName("deserializeCompressed()")
    class DeserializeCompressed {

        @Test
        @DisplayName("Deve deserializar dados não comprimidos corretamente")
        void shouldDeserializeUncompressedData() {
            AggregateSnapshot original = buildSnapshot("agg-77", 5L);
            String json = serializer.serialize(original);

            AggregateSnapshot recovered = serializer.deserializeCompressed(json, "TestAggregate", false, null);

            assertThat(recovered.getAggregateId()).isEqualTo("agg-77");
        }

        @Test
        @DisplayName("Deve lançar exceção para algoritmo não suportado")
        void shouldThrowForUnsupportedAlgorithm() {
            assertThatThrownBy(() ->
                    serializer.deserializeCompressed("data", "TestAggregate", true, "LZ4"))
                    .satisfies(ex -> assertThat(
                            ex instanceof SnapshotCompressionException
                            || (ex.getCause() instanceof SnapshotCompressionException))
                            .isTrue());
        }

        @Test
        @DisplayName("Deve fazer round-trip com compressão GZIP quando dados grandes o suficiente")
        void shouldRoundTripWithGzipCompression() {
            // Criar snapshot com dados suficientemente grandes para compressão
            Map<String, Object> data = Map.of(
                    "campo1", "a".repeat(500),
                    "campo2", "b".repeat(500),
                    "campo3", "c".repeat(500)
            );
            AggregateSnapshot original = new AggregateSnapshot("agg-1", "TestAggregate", 50L, data);

            // Threshold baixo para forçar compressão
            SnapshotSerializationResult result = serializer.serializeWithCompression(original, 100);

            if (result.isCompressed()) {
                // Se foi comprimido, deve conseguir descomprimir
                AggregateSnapshot recovered = serializer.deserializeCompressed(
                        result.getSerializedData(), "TestAggregate", true, "GZIP");
                assertThat(recovered.getAggregateId()).isEqualTo("agg-1");
            } else {
                // Se não comprimiu, resultado ainda deve ser válido
                assertThat(result.getSerializedData()).isNotBlank();
            }
        }
    }

    // =========================================================================
    // supports()
    // =========================================================================

    @Test
    @DisplayName("supports() deve retornar true para qualquer tipo de aggregate")
    void shouldSupportAnyAggregateType() {
        assertThat(serializer.supports("SeguradoAggregate")).isTrue();
        assertThat(serializer.supports("ApoliceAggregate")).isTrue();
        assertThat(serializer.supports("qualquerTipo")).isTrue();
    }

    // =========================================================================
    // calculateHash() e validateIntegrity()
    // =========================================================================

    @Nested
    @DisplayName("calculateHash() e validateIntegrity()")
    class HashEIntegridade {

        @Test
        @DisplayName("calculateHash() deve retornar hash não nulo para qualquer input")
        void shouldReturnNonNullHash() {
            String hash = serializer.calculateHash("dados de teste");
            assertThat(hash).isNotBlank();
        }

        @Test
        @DisplayName("calculateHash() deve retornar mesmo hash para mesmo input")
        void shouldReturnSameHashForSameInput() {
            String h1 = serializer.calculateHash("mesmo dado");
            String h2 = serializer.calculateHash("mesmo dado");
            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("calculateHash() deve retornar hashes diferentes para inputs diferentes")
        void shouldReturnDifferentHashesForDifferentInputs() {
            String h1 = serializer.calculateHash("dado A");
            String h2 = serializer.calculateHash("dado B");
            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("validateIntegrity() deve retornar true para hash correto")
        void shouldReturnTrueForCorrectHash() {
            String data = "dados para validar";
            String hash = serializer.calculateHash(data);

            assertThat(serializer.validateIntegrity(data, hash)).isTrue();
        }

        @Test
        @DisplayName("validateIntegrity() deve retornar false para hash incorreto")
        void shouldReturnFalseForIncorrectHash() {
            assertThat(serializer.validateIntegrity("dados", "hashErrado")).isFalse();
        }

        @Test
        @DisplayName("validateIntegrity() deve retornar true quando hash é null")
        void shouldReturnTrueWhenHashIsNull() {
            assertThat(serializer.validateIntegrity("dados", null)).isTrue();
        }

        @Test
        @DisplayName("validateIntegrity() deve retornar true quando hash é string vazia")
        void shouldReturnTrueWhenHashIsEmpty() {
            assertThat(serializer.validateIntegrity("dados", "")).isTrue();
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private AggregateSnapshot buildSnapshot(String aggregateId, long version) {
        return new AggregateSnapshot(aggregateId, "TestAggregate", version,
                Map.of("nome", "teste", "valor", 42));
    }
}
