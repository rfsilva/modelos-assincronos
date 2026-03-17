package com.seguradora.hibrida.projection.rebuild;

import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.projection.ProjectionEventProcessor;
import com.seguradora.hibrida.projection.ProjectionRegistry;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link ProjectionRebuilder}.
 *
 * <p>Testes de comportamento completos requerem banco de dados.
 * Esta classe verifica a estrutura e anotações do componente.
 */
@DisplayName("ProjectionRebuilder Tests")
class ProjectionRebuilderTest {

    @Test
    @DisplayName("Deve estar anotado com @Service")
    void shouldBeAnnotatedWithService() {
        assertThat(ProjectionRebuilder.class.isAnnotationPresent(Service.class)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar dependências corretas no construtor")
    void shouldAcceptCorrectDependenciesInConstructor() throws NoSuchMethodException {
        assertThat(ProjectionRebuilder.class.getConstructor(
                EventStore.class,
                ProjectionRegistry.class,
                ProjectionTrackerRepository.class,
                ProjectionEventProcessor.class,
                ProjectionRebuildProperties.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método rebuildProjection(String)")
    void shouldDeclareRebuildProjectionMethod() throws NoSuchMethodException {
        assertThat(ProjectionRebuilder.class.getMethod("rebuildProjection", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("rebuildProjection deve retornar CompletableFuture")
    void rebuildProjectionShouldReturnCompletableFuture() throws NoSuchMethodException {
        var method = ProjectionRebuilder.class.getMethod("rebuildProjection", String.class);
        assertThat(method.getReturnType()).isEqualTo(CompletableFuture.class);
    }

    @Test
    @DisplayName("Deve declarar método rebuildProjection com @Async")
    void rebuildProjectionShouldBeAnnotatedWithAsync() throws NoSuchMethodException {
        Method method = ProjectionRebuilder.class.getMethod("rebuildProjection", String.class);
        assertThat(method.isAnnotationPresent(Async.class)).isTrue();
    }

    @Test
    @DisplayName("Deve declarar método rebuildProjectionIncremental com @Async")
    void shouldDeclareRebuildProjectionIncrementalMethod() throws NoSuchMethodException {
        Method method = ProjectionRebuilder.class.getMethod("rebuildProjectionIncremental", String.class);
        assertThat(method.isAnnotationPresent(Async.class)).isTrue();
    }
}
