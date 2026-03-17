package com.seguradora.hibrida.eventbus.config;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventHandlerRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link KafkaEventBusConfiguration}.
 */
@DisplayName("KafkaEventBusConfiguration Tests")
class KafkaEventBusConfigurationTest {

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Configuration")
    void shouldBeAnnotatedWithConfiguration() {
        assertThat(KafkaEventBusConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @ConditionalOnProperty para kafka.enabled")
    void shouldBeAnnotatedWithConditionalOnPropertyForKafkaEnabled() {
        ConditionalOnProperty annotation =
                KafkaEventBusConfiguration.class.getAnnotation(ConditionalOnProperty.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).contains("event-bus.kafka.enabled");
        assertThat(annotation.havingValue()).isEqualTo("true");
    }

    // =========================================================================
    // Beans declarados
    // =========================================================================

    @Nested
    @DisplayName("Declaração de @Bean")
    class DeclaracaoDeBeans {

        private Set<String> beanMethodNames() {
            return Arrays.stream(KafkaEventBusConfiguration.class.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Bean.class))
                    .map(Method::getName)
                    .collect(Collectors.toSet());
        }

        @Test
        @DisplayName("Deve declarar bean kafkaEventBus")
        void shouldDeclareKafkaEventBus() {
            assertThat(beanMethodNames()).contains("kafkaEventBus");
        }

        @Test
        @DisplayName("Deve declarar bean eventBusObjectMapper")
        void shouldDeclareEventBusObjectMapper() {
            assertThat(beanMethodNames()).contains("eventBusObjectMapper");
        }
    }

    // =========================================================================
    // Bean kafkaEventBus com @Primary
    // =========================================================================

    @Test
    @DisplayName("Bean kafkaEventBus deve estar anotado com @Primary")
    void kafkaEventBusBeanShouldBeAnnotatedWithPrimary() throws NoSuchMethodException {
        Method method = KafkaEventBusConfiguration.class.getDeclaredMethod(
                "kafkaEventBus", EventHandlerRegistry.class, com.fasterxml.jackson.databind.ObjectMapper.class);

        assertThat(method.isAnnotationPresent(Primary.class)).isTrue();
    }

    @Test
    @DisplayName("Bean kafkaEventBus deve retornar EventBus")
    void kafkaEventBusBeanShouldReturnEventBus() throws NoSuchMethodException {
        Method method = KafkaEventBusConfiguration.class.getDeclaredMethod(
                "kafkaEventBus", EventHandlerRegistry.class, com.fasterxml.jackson.databind.ObjectMapper.class);

        assertThat(EventBus.class.isAssignableFrom(method.getReturnType())).isTrue();
    }
}
