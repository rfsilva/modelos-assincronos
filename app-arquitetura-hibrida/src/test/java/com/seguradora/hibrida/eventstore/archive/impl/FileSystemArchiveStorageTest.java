package com.seguradora.hibrida.eventstore.archive.impl;

import com.seguradora.hibrida.eventstore.archive.ArchiveStorageService;
import com.seguradora.hibrida.eventstore.archive.EventArchiveProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link FileSystemArchiveStorage}.
 */
@DisplayName("FileSystemArchiveStorage Tests")
class FileSystemArchiveStorageTest {

    @Test
    @DisplayName("Deve estar anotado com @Service")
    void shouldBeAnnotatedWithService() {
        assertThat(FileSystemArchiveStorage.class.isAnnotationPresent(Service.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @ConditionalOnProperty para filesystem")
    void shouldBeAnnotatedWithConditionalOnPropertyForFilesystem() {
        assertThat(FileSystemArchiveStorage.class.isAnnotationPresent(ConditionalOnProperty.class)).isTrue();
        ConditionalOnProperty prop = FileSystemArchiveStorage.class.getAnnotation(ConditionalOnProperty.class);
        assertThat(prop.havingValue()).isEqualTo("filesystem");
    }

    @Test
    @DisplayName("Deve implementar ArchiveStorageService")
    void shouldImplementArchiveStorageService() {
        assertThat(ArchiveStorageService.class.isAssignableFrom(FileSystemArchiveStorage.class)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar EventArchiveProperties no construtor")
    void shouldAcceptEventArchivePropertiesInConstructor() throws NoSuchMethodException {
        assertThat(FileSystemArchiveStorage.class.getConstructor(EventArchiveProperties.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método store")
    void shouldDeclareStoreMethod() throws NoSuchMethodException {
        assertThat(FileSystemArchiveStorage.class.getMethod("store", String.class, byte[].class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método retrieve")
    void shouldDeclareRetrieveMethod() throws NoSuchMethodException {
        assertThat(FileSystemArchiveStorage.class.getMethod("retrieve", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método exists")
    void shouldDeclareExistsMethod() throws NoSuchMethodException {
        assertThat(FileSystemArchiveStorage.class.getMethod("exists", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método delete")
    void shouldDeclareDeleteMethod() throws NoSuchMethodException {
        assertThat(FileSystemArchiveStorage.class.getMethod("delete", String.class))
                .isNotNull();
    }
}
