package com.seguradora.hibrida.projection.config;

import com.seguradora.hibrida.projection.ProjectionEventProcessor;
import com.seguradora.hibrida.projection.ProjectionRegistry;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ProjectionConfiguration}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProjectionConfiguration Tests")
class ProjectionConfigurationTest {

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Nested
    @DisplayName("Anotações de classe")
    class AnotacoesDeClasse {

        @Test
        @DisplayName("Deve estar anotado com @Configuration")
        void shouldBeAnnotatedWithConfiguration() {
            assertThat(ProjectionConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableAsync")
        void shouldBeAnnotatedWithEnableAsync() {
            assertThat(ProjectionConfiguration.class.isAnnotationPresent(EnableAsync.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableScheduling")
        void shouldBeAnnotatedWithEnableScheduling() {
            assertThat(ProjectionConfiguration.class.isAnnotationPresent(EnableScheduling.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableConfigurationProperties para ProjectionProperties")
        void shouldBeAnnotatedWithEnableConfigurationProperties() {
            EnableConfigurationProperties annotation =
                    ProjectionConfiguration.class.getAnnotation(EnableConfigurationProperties.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).contains(ProjectionProperties.class);
        }
    }

    // =========================================================================
    // Beans declarados
    // =========================================================================

    @Nested
    @DisplayName("Declaração de @Bean")
    class DeclaracaoDeBeans {

        private Set<String> beanMethodNames() {
            return Arrays.stream(ProjectionConfiguration.class.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Bean.class))
                    .map(Method::getName)
                    .collect(Collectors.toSet());
        }

        @Test
        @DisplayName("Deve declarar bean projectionRegistry")
        void shouldDeclareProjectionRegistry() {
            assertThat(beanMethodNames()).contains("projectionRegistry");
        }

        @Test
        @DisplayName("Deve declarar bean projectionEventProcessor")
        void shouldDeclareProjectionEventProcessor() {
            assertThat(beanMethodNames()).contains("projectionEventProcessor");
        }

        @Test
        @DisplayName("Deve declarar bean projectionTaskExecutor")
        void shouldDeclareProjectionTaskExecutor() {
            assertThat(beanMethodNames()).contains("projectionTaskExecutor");
        }

        @Test
        @DisplayName("Deve declarar bean projectionHandlerRegistrar")
        void shouldDeclareProjectionHandlerRegistrar() {
            assertThat(beanMethodNames()).contains("projectionHandlerRegistrar");
        }
    }

    // =========================================================================
    // Bean projectionTaskExecutor com nome customizado
    // =========================================================================

    @Test
    @DisplayName("Bean projectionTaskExecutor deve ter nome customizado")
    void projectionTaskExecutorBeanShouldHaveCustomName() throws NoSuchMethodException {
        Method method = ProjectionConfiguration.class.getDeclaredMethod("projectionTaskExecutor");
        Bean beanAnnotation = method.getAnnotation(Bean.class);

        assertThat(beanAnnotation).isNotNull();
        String[] names = beanAnnotation.name().length > 0
                ? beanAnnotation.name()
                : beanAnnotation.value();
        assertThat(names).contains("projectionTaskExecutor");
    }

    // =========================================================================
    // Bean projectionRegistry — criação direta
    // =========================================================================

    @Test
    @DisplayName("projectionRegistry() deve criar instância não nula")
    void projectionRegistryBeanShouldCreateNonNullInstance() {
        ProjectionProperties props = new ProjectionProperties();
        ProjectionConfiguration config = new ProjectionConfiguration(props);

        assertThat(config.projectionRegistry()).isNotNull();
        assertThat(config.projectionRegistry()).isInstanceOf(ProjectionRegistry.class);
    }
}
