package com.seguradora.hibrida.snapshot.serialization;

import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para a interface {@link SnapshotSerializer}.
 */
@DisplayName("SnapshotSerializer Tests")
class SnapshotSerializerTest {

    // =========================================================================
    // Contrato da interface — tipo e métodos
    // =========================================================================

    @Test
    @DisplayName("Deve ser uma interface")
    void shouldBeAnInterface() {
        assertThat(SnapshotSerializer.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Deve declarar método serialize(AggregateSnapshot) retornando String")
    void shouldDeclareSerialize() throws NoSuchMethodException {
        Method m = SnapshotSerializer.class.getMethod("serialize", AggregateSnapshot.class);
        assertThat(m.getReturnType()).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Deve declarar método deserialize(String, String) retornando AggregateSnapshot")
    void shouldDeclareDeserialize() throws NoSuchMethodException {
        Method m = SnapshotSerializer.class.getMethod("deserialize", String.class, String.class);
        assertThat(m.getReturnType()).isEqualTo(AggregateSnapshot.class);
    }

    @Test
    @DisplayName("Deve declarar método serializeWithCompression(AggregateSnapshot, int) retornando SnapshotSerializationResult")
    void shouldDeclareSerializeWithCompression() throws NoSuchMethodException {
        Method m = SnapshotSerializer.class.getMethod(
                "serializeWithCompression", AggregateSnapshot.class, int.class);
        assertThat(m.getReturnType()).isEqualTo(SnapshotSerializationResult.class);
    }

    @Test
    @DisplayName("Deve declarar método deserializeCompressed(String, String, boolean, String) retornando AggregateSnapshot")
    void shouldDeclareDeserializeCompressed() throws NoSuchMethodException {
        Method m = SnapshotSerializer.class.getMethod(
                "deserializeCompressed", String.class, String.class, boolean.class, String.class);
        assertThat(m.getReturnType()).isEqualTo(AggregateSnapshot.class);
    }

    @Test
    @DisplayName("Deve declarar método supports(String) retornando boolean")
    void shouldDeclareSupports() throws NoSuchMethodException {
        Method m = SnapshotSerializer.class.getMethod("supports", String.class);
        assertThat(m.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Deve declarar método calculateHash(String) retornando String")
    void shouldDeclareCalculateHash() throws NoSuchMethodException {
        Method m = SnapshotSerializer.class.getMethod("calculateHash", String.class);
        assertThat(m.getReturnType()).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Deve declarar método validateIntegrity(String, String) retornando boolean")
    void shouldDeclareValidateIntegrity() throws NoSuchMethodException {
        Method m = SnapshotSerializer.class.getMethod("validateIntegrity", String.class, String.class);
        assertThat(m.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Deve ter exatamente 7 métodos na interface")
    void shouldHaveSevenMethods() {
        long count = java.util.Arrays.stream(SnapshotSerializer.class.getMethods())
                .filter(m -> m.getDeclaringClass() == SnapshotSerializer.class)
                .count();

        assertThat(count).isEqualTo(7L);
    }
}
