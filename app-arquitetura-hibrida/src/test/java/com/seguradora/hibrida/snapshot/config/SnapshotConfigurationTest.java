package com.seguradora.hibrida.snapshot.config;

import com.seguradora.hibrida.snapshot.SnapshotProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
 * Testes unitários para {@link SnapshotConfiguration}.
 */
@DisplayName("SnapshotConfiguration Tests")
class SnapshotConfigurationTest {

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Nested
    @DisplayName("Anotações de classe")
    class AnotacoesDeClasse {

        @Test
        @DisplayName("Deve estar anotado com @Configuration")
        void shouldBeAnnotatedWithConfiguration() {
            assertThat(SnapshotConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableAsync")
        void shouldBeAnnotatedWithEnableAsync() {
            assertThat(SnapshotConfiguration.class.isAnnotationPresent(EnableAsync.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableScheduling")
        void shouldBeAnnotatedWithEnableScheduling() {
            assertThat(SnapshotConfiguration.class.isAnnotationPresent(EnableScheduling.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableConfigurationProperties contendo SnapshotProperties")
        void shouldBeAnnotatedWithEnableConfigurationProperties() {
            EnableConfigurationProperties annotation =
                    SnapshotConfiguration.class.getAnnotation(EnableConfigurationProperties.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).contains(SnapshotProperties.class);
        }
    }

    // =========================================================================
    // Beans declarados
    // =========================================================================

    @Nested
    @DisplayName("Declaração de métodos @Bean")
    class DeclaracaoDeBeans {

        private Set<String> beanMethodNames() {
            return Arrays.stream(SnapshotConfiguration.class.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Bean.class))
                    .map(Method::getName)
                    .collect(Collectors.toSet());
        }

        @Test
        @DisplayName("Deve declarar bean snapshotSerializer")
        void shouldDeclareSnapshotSerializer() {
            assertThat(beanMethodNames()).contains("snapshotSerializer");
        }

        @Test
        @DisplayName("Deve declarar bean snapshotStore")
        void shouldDeclareSnapshotStore() {
            assertThat(beanMethodNames()).contains("snapshotStore");
        }

        @Test
        @DisplayName("Deve declarar bean snapshotMetrics")
        void shouldDeclareSnapshotMetrics() {
            assertThat(beanMethodNames()).contains("snapshotMetrics");
        }

        @Test
        @DisplayName("Deve declarar bean snapshotHealthIndicator")
        void shouldDeclareSnapshotHealthIndicator() {
            assertThat(beanMethodNames()).contains("snapshotHealthIndicator");
        }

        @Test
        @DisplayName("Deve declarar bean snapshotCleanupScheduler")
        void shouldDeclareSnapshotCleanupScheduler() {
            assertThat(beanMethodNames()).contains("snapshotCleanupScheduler");
        }
    }

    // =========================================================================
    // Bean snapshotTaskExecutor com nome customizado
    // =========================================================================

    @Test
    @DisplayName("Deve declarar bean snapshotTaskExecutor com nome customizado")
    void shouldDeclareSnapshotTaskExecutorWithCustomName() throws NoSuchMethodException {
        Method method = SnapshotConfiguration.class.getDeclaredMethod("snapshotTaskExecutor");
        Bean beanAnnotation = method.getAnnotation(Bean.class);

        assertThat(beanAnnotation).isNotNull();
        // @Bean("name") popula value(); @Bean(name="name") popula name()
        String[] names = beanAnnotation.name().length > 0
                ? beanAnnotation.name()
                : beanAnnotation.value();
        assertThat(names).contains("snapshotTaskExecutor");
    }
}
