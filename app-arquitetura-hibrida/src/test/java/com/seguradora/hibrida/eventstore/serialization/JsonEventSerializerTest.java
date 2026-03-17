package com.seguradora.hibrida.eventstore.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seguradora.hibrida.eventbus.example.TestEvent;
import com.seguradora.hibrida.eventstore.exception.SerializationException;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para {@link JsonEventSerializer}.
 */
@DisplayName("JsonEventSerializer Tests")
class JsonEventSerializerTest {

    private JsonEventSerializer serializer;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        serializer = new JsonEventSerializer(mapper);
    }

    // =========================================================================
    // Anotações
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(JsonEventSerializer.class.isAnnotationPresent(Component.class)).isTrue();
    }

    @Test
    @DisplayName("Deve implementar EventSerializer")
    void shouldImplementEventSerializer() {
        assertThat(serializer).isInstanceOf(EventSerializer.class);
    }

    // =========================================================================
    // serialize
    // =========================================================================

    @Nested
    @DisplayName("serialize()")
    class Serialize {

        @Test
        @DisplayName("Deve serializar evento para JSON não nulo")
        void shouldSerializeEventToNonNullJson() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
            String json = serializer.serialize(event);

            assertThat(json).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("JSON deve conter o tipo do evento")
        void jsonShouldContainEventType() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
            String json = serializer.serialize(event);

            assertThat(json).contains("TestEvent");
        }
    }

    // =========================================================================
    // serializeWithCompression
    // =========================================================================

    @Nested
    @DisplayName("serializeWithCompression()")
    class SerializeWithCompression {

        @Test
        @DisplayName("Deve retornar result não comprimido quando abaixo do threshold")
        void shouldReturnUncompressedWhenBelowThreshold() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
            // Threshold alto (1MB) → não comprime
            SerializationResult result = serializer.serializeWithCompression(event, 1_000_000);

            assertThat(result.isCompressed()).isFalse();
            assertThat(result.getData()).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar SerializationResult válido com originalSize preenchido")
        void shouldReturnResultWithOriginalSize() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
            SerializationResult result = serializer.serializeWithCompression(event, 1_000_000);

            assertThat(result.getOriginalSize()).isPositive();
            assertThat(result.getFinalSize()).isPositive();
        }
    }

    // =========================================================================
    // supports
    // =========================================================================

    @Nested
    @DisplayName("supports()")
    class Supports {

        @Test
        @DisplayName("Deve retornar true para classe DomainEvent válida")
        void shouldReturnTrueForValidDomainEventClass() {
            assertThat(serializer.supports(TestEvent.class.getName())).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false para classe não encontrada")
        void shouldReturnFalseForUnknownClass() {
            assertThat(serializer.supports("com.example.NonExistentEvent")).isFalse();
        }
    }

    // =========================================================================
    // deserialize (erro)
    // =========================================================================

    @Test
    @DisplayName("deserialize deve lançar SerializationException para tipo inexistente")
    void deserializeShouldThrowForUnknownType() {
        assertThatThrownBy(() -> serializer.deserialize("{}", "com.example.Inexistente"))
                .isInstanceOf(SerializationException.class);
    }

    // =========================================================================
    // deserializeCompressed (não comprimido)
    // =========================================================================

    @Test
    @DisplayName("deserializeCompressed com compressed=false deve deserializar normalmente")
    void deserializeCompressedWithFalseShouldDeserializeNormally() {
        TestEvent original = new TestEvent("agg-1", "msg", "cat", 1);
        String json = serializer.serialize(original);

        DomainEvent result = serializer.deserializeCompressed(json, TestEvent.class.getName(), false);
        assertThat(result).isNotNull();
        assertThat(result.getAggregateId()).isEqualTo("agg-1");
    }
}
