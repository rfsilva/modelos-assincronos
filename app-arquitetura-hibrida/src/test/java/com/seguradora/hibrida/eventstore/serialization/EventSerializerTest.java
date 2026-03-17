package com.seguradora.hibrida.eventstore.serialization;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de contrato para {@link EventSerializer}.
 */
@DisplayName("EventSerializer Tests")
class EventSerializerTest {

    @Test
    @DisplayName("Deve ser uma interface")
    void shouldBeInterface() {
        assertThat(EventSerializer.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Deve declarar método serialize")
    void shouldDeclareSerializeMethod() throws NoSuchMethodException {
        assertThat(EventSerializer.class.getMethod("serialize", DomainEvent.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método deserialize")
    void shouldDeclareDeserializeMethod() throws NoSuchMethodException {
        assertThat(EventSerializer.class.getMethod("deserialize", String.class, String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método serializeWithCompression")
    void shouldDeclareSerializeWithCompressionMethod() throws NoSuchMethodException {
        assertThat(EventSerializer.class.getMethod("serializeWithCompression", DomainEvent.class, int.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método deserializeCompressed")
    void shouldDeclareDeserializeCompressedMethod() throws NoSuchMethodException {
        assertThat(EventSerializer.class.getMethod("deserializeCompressed", String.class, String.class, boolean.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método supports")
    void shouldDeclareSupportsMethod() throws NoSuchMethodException {
        assertThat(EventSerializer.class.getMethod("supports", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("serialize deve retornar String")
    void serializeShouldReturnString() throws NoSuchMethodException {
        var method = EventSerializer.class.getMethod("serialize", DomainEvent.class);
        assertThat(method.getReturnType()).isEqualTo(String.class);
    }

    @Test
    @DisplayName("deserialize deve retornar DomainEvent")
    void deserializeShouldReturnDomainEvent() throws NoSuchMethodException {
        var method = EventSerializer.class.getMethod("deserialize", String.class, String.class);
        assertThat(method.getReturnType()).isEqualTo(DomainEvent.class);
    }

    @Test
    @DisplayName("supports deve retornar boolean")
    void supportsShouldReturnBoolean() throws NoSuchMethodException {
        var method = EventSerializer.class.getMethod("supports", String.class);
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }
}
