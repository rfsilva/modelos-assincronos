package com.seguradora.hibrida.cqrs.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link CQRSConfiguration}.
 */
@DisplayName("CQRSConfiguration Tests")
class CQRSConfigurationTest {

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Nested
    @DisplayName("Anotações de classe")
    class AnotacoesDeClasse {

        @Test
        @DisplayName("Deve estar anotado com @Configuration")
        void shouldBeAnnotatedWithConfiguration() {
            assertThat(CQRSConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableScheduling")
        void shouldBeAnnotatedWithEnableScheduling() {
            assertThat(CQRSConfiguration.class.isAnnotationPresent(EnableScheduling.class)).isTrue();
        }
    }

    // =========================================================================
    // Beans declarados
    // =========================================================================

    @Nested
    @DisplayName("Declaração de métodos @Bean")
    class DeclaracaoDeBeans {

        private Set<String> beanMethodNames() {
            return Arrays.stream(CQRSConfiguration.class.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Bean.class))
                    .map(Method::getName)
                    .collect(Collectors.toSet());
        }

        @Test
        @DisplayName("Deve declarar bean cqrsHealthIndicator")
        void shouldDeclareCqrsHealthIndicatorBean() {
            assertThat(beanMethodNames()).contains("cqrsHealthIndicator");
        }

        @Test
        @DisplayName("Deve declarar bean cqrsMetrics")
        void shouldDeclareCqrsMetricsBean() {
            assertThat(beanMethodNames()).contains("cqrsMetrics");
        }
    }

    // =========================================================================
    // Método @Scheduled
    // =========================================================================

    @Test
    @DisplayName("Deve ter método updateCQRSMetrics anotado com @Scheduled")
    void shouldHaveScheduledUpdateMethod() throws NoSuchMethodException {
        Method method = CQRSConfiguration.class.getDeclaredMethod("updateCQRSMetrics");
        assertThat(method.isAnnotationPresent(org.springframework.scheduling.annotation.Scheduled.class)).isTrue();
    }
}
