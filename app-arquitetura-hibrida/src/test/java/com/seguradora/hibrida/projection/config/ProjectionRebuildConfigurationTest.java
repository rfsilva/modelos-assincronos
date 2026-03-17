package com.seguradora.hibrida.projection.config;

import com.seguradora.hibrida.projection.rebuild.ProjectionRebuildProperties;
import com.seguradora.hibrida.projection.consistency.ProjectionConsistencyProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ProjectionRebuildConfiguration}.
 */
@DisplayName("ProjectionRebuildConfiguration Tests")
class ProjectionRebuildConfigurationTest {

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Configuration")
    void shouldBeAnnotatedWithConfiguration() {
        assertThat(ProjectionRebuildConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @EnableConfigurationProperties")
    void shouldBeAnnotatedWithEnableConfigurationProperties() {
        EnableConfigurationProperties annotation =
                ProjectionRebuildConfiguration.class.getAnnotation(EnableConfigurationProperties.class);

        assertThat(annotation).isNotNull();
        assertThat(Arrays.asList(annotation.value()))
                .contains(ProjectionRebuildProperties.class, ProjectionConsistencyProperties.class);
    }

    // =========================================================================
    // Beans declarados
    // =========================================================================

    @Nested
    @DisplayName("Declaração de @Bean")
    class DeclaracaoDeBeans {

        private Set<String> beanMethodNames() {
            return Arrays.stream(ProjectionRebuildConfiguration.class.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Bean.class))
                    .map(Method::getName)
                    .collect(Collectors.toSet());
        }

        @Test
        @DisplayName("Deve declarar bean projectionRebuilder")
        void shouldDeclareProjectionRebuilder() {
            assertThat(beanMethodNames()).contains("projectionRebuilder");
        }

        @Test
        @DisplayName("Deve declarar bean projectionConsistencyChecker")
        void shouldDeclareProjectionConsistencyChecker() {
            assertThat(beanMethodNames()).contains("projectionConsistencyChecker");
        }

        @Test
        @DisplayName("Deve declarar bean projectionMaintenanceScheduler")
        void shouldDeclareProjectionMaintenanceScheduler() {
            assertThat(beanMethodNames()).contains("projectionMaintenanceScheduler");
        }
    }

    // =========================================================================
    // @ConditionalOnProperty nos beans
    // =========================================================================

    @Test
    @DisplayName("Bean projectionRebuilder deve ter @ConditionalOnProperty")
    void projectionRebuilderBeanShouldHaveConditionalOnProperty() throws Exception {
        Method[] methods = ProjectionRebuildConfiguration.class.getDeclaredMethods();
        Method rebuilderMethod = Arrays.stream(methods)
                .filter(m -> m.getName().equals("projectionRebuilder"))
                .findFirst()
                .orElseThrow();

        assertThat(rebuilderMethod.isAnnotationPresent(ConditionalOnProperty.class)).isTrue();
    }
}
