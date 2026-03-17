package com.seguradora.hibrida.projection.scheduler;

import com.seguradora.hibrida.projection.consistency.ProjectionConsistencyChecker;
import com.seguradora.hibrida.projection.rebuild.ProjectionRebuilder;
import com.seguradora.hibrida.projection.rebuild.ProjectionRebuildProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ProjectionMaintenanceScheduler}.
 */
@DisplayName("ProjectionMaintenanceScheduler Tests")
class ProjectionMaintenanceSchedulerTest {

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(ProjectionMaintenanceScheduler.class.isAnnotationPresent(Component.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @ConditionalOnProperty para rebuild.enabled")
    void shouldBeAnnotatedWithConditionalOnPropertyForRebuildEnabled() {
        ConditionalOnProperty annotation =
                ProjectionMaintenanceScheduler.class.getAnnotation(ConditionalOnProperty.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.prefix()).isEqualTo("cqrs.projection.rebuild");
        assertThat(annotation.name()).contains("enabled");
    }

    // =========================================================================
    // Construção (necessita das dependências)
    // =========================================================================

    @Test
    @DisplayName("Deve aceitar os tipos de dependência corretos no construtor")
    void shouldAcceptCorrectDependencyTypesInConstructor() throws NoSuchMethodException {
        assertThat(ProjectionMaintenanceScheduler.class.getConstructor(
                ProjectionRebuilder.class,
                ProjectionConsistencyChecker.class,
                ProjectionRebuildProperties.class))
                .isNotNull();
    }
}
