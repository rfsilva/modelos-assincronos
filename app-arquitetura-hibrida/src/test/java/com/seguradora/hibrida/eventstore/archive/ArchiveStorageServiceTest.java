package com.seguradora.hibrida.eventstore.archive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de contrato para {@link ArchiveStorageService}.
 */
@DisplayName("ArchiveStorageService Tests")
class ArchiveStorageServiceTest {

    @Test
    @DisplayName("Deve ser uma interface")
    void shouldBeInterface() {
        assertThat(ArchiveStorageService.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Deve declarar método store")
    void shouldDeclareStoreMethod() throws NoSuchMethodException {
        assertThat(ArchiveStorageService.class.getMethod("store", String.class, byte[].class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método retrieve")
    void shouldDeclareRetrieveMethod() throws NoSuchMethodException {
        assertThat(ArchiveStorageService.class.getMethod("retrieve", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método exists")
    void shouldDeclareExistsMethod() throws NoSuchMethodException {
        assertThat(ArchiveStorageService.class.getMethod("exists", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método delete")
    void shouldDeclareDeleteMethod() throws NoSuchMethodException {
        assertThat(ArchiveStorageService.class.getMethod("delete", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método getSize")
    void shouldDeclareGetSizeMethod() throws NoSuchMethodException {
        assertThat(ArchiveStorageService.class.getMethod("getSize", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("store deve retornar boolean")
    void storeShouldReturnBoolean() throws NoSuchMethodException {
        var method = ArchiveStorageService.class.getMethod("store", String.class, byte[].class);
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("retrieve deve retornar byte[]")
    void retrieveShouldReturnByteArray() throws NoSuchMethodException {
        var method = ArchiveStorageService.class.getMethod("retrieve", String.class);
        assertThat(method.getReturnType()).isEqualTo(byte[].class);
    }

    @Test
    @DisplayName("getSize deve retornar long")
    void getSizeShouldReturnLong() throws NoSuchMethodException {
        var method = ArchiveStorageService.class.getMethod("getSize", String.class);
        assertThat(method.getReturnType()).isEqualTo(long.class);
    }
}
