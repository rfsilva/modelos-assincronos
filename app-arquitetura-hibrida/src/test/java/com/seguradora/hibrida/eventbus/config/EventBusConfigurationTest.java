package com.seguradora.hibrida.eventbus.config;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventHandlerRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link EventBusConfiguration}.
 */
@DisplayName("EventBusConfiguration Tests")
class EventBusConfigurationTest {

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Configuration")
    void shouldBeAnnotatedWithConfiguration() {
        assertThat(EventBusConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    // =========================================================================
    // Beans declarados
    // =========================================================================

    @Nested
    @DisplayName("Declaração de métodos @Bean")
    class DeclaracaoDeBeans {

        private Set<String> beanMethodNames() {
            return Arrays.stream(EventBusConfiguration.class.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Bean.class))
                    .map(Method::getName)
                    .collect(Collectors.toSet());
        }

        @Test
        @DisplayName("Deve declarar bean eventBus")
        void shouldDeclareEventBus() {
            assertThat(beanMethodNames()).contains("eventBus");
        }

        @Test
        @DisplayName("Deve declarar bean eventHandlerRegistry")
        void shouldDeclareEventHandlerRegistry() {
            assertThat(beanMethodNames()).contains("eventHandlerRegistry");
        }

        @Test
        @DisplayName("Deve declarar bean eventBusMetrics")
        void shouldDeclareEventBusMetrics() {
            assertThat(beanMethodNames()).contains("eventBusMetrics");
        }

        @Test
        @DisplayName("Deve declarar bean eventBusHealthIndicator")
        void shouldDeclareEventBusHealthIndicator() {
            assertThat(beanMethodNames()).contains("eventBusHealthIndicator");
        }
    }

    // =========================================================================
    // Assinatura dos beans
    // =========================================================================

    @Test
    @DisplayName("Bean eventBus deve retornar EventBus")
    void eventBusBeanShouldReturnEventBus() throws NoSuchMethodException {
        Method method = EventBusConfiguration.class.getDeclaredMethod("eventBus", EventHandlerRegistry.class);
        assertThat(EventBus.class.isAssignableFrom(method.getReturnType())).isTrue();
    }

    @Test
    @DisplayName("Bean eventHandlerRegistry deve retornar EventHandlerRegistry")
    void eventHandlerRegistryBeanShouldReturnRegistry() throws NoSuchMethodException {
        Method method = EventBusConfiguration.class.getDeclaredMethod("eventHandlerRegistry");
        assertThat(method.getReturnType()).isEqualTo(EventHandlerRegistry.class);
    }
}
