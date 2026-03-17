package com.seguradora.hibrida.projection.consistency;

import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link ProjectionConsistencyChecker}.
 *
 * <p>Testes de comportamento completos requerem banco de dados.
 * Esta classe verifica a estrutura e anotações do componente.
 */
@DisplayName("ProjectionConsistencyChecker Tests")
class ProjectionConsistencyCheckerTest {

    @Test
    @DisplayName("Deve estar anotado com @Service")
    void shouldBeAnnotatedWithService() {
        assertThat(ProjectionConsistencyChecker.class.isAnnotationPresent(Service.class)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar dependências corretas no construtor")
    void shouldAcceptCorrectDependenciesInConstructor() throws NoSuchMethodException {
        assertThat(ProjectionConsistencyChecker.class.getConstructor(
                ProjectionTrackerRepository.class,
                EventStoreRepository.class,
                ProjectionConsistencyProperties.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método checkAllProjections")
    void shouldDeclareCheckAllProjectionsMethod() throws NoSuchMethodException {
        assertThat(ProjectionConsistencyChecker.class.getMethod("checkAllProjections"))
                .isNotNull();
    }

    @Test
    @DisplayName("checkAllProjections deve retornar ConsistencyReport")
    void checkAllProjectionsShouldReturnConsistencyReport() throws NoSuchMethodException {
        var method = ProjectionConsistencyChecker.class.getMethod("checkAllProjections");
        assertThat(method.getReturnType()).isEqualTo(ConsistencyReport.class);
    }
}
