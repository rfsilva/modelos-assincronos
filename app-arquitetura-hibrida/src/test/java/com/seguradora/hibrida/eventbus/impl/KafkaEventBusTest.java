package com.seguradora.hibrida.eventbus.impl;

import com.seguradora.hibrida.eventbus.EventBus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link KafkaEventBus}.
 *
 * <p>A inicialização completa do KafkaEventBus requer um broker Kafka ativo
 * (conexão real para producer/consumer). Estes testes verificam apenas as
 * meta-informações estáticas da classe sem instanciar o componente.
 */
@DisplayName("KafkaEventBus Tests")
class KafkaEventBusTest {

    // =========================================================================
    // Meta-anotações
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(KafkaEventBus.class.isAnnotationPresent(Component.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @ConditionalOnProperty para kafka.enabled")
    void shouldBeAnnotatedWithConditionalOnPropertyForKafkaEnabled() {
        ConditionalOnProperty annotation = KafkaEventBus.class.getAnnotation(ConditionalOnProperty.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).contains("event-bus.kafka.enabled");
        assertThat(annotation.havingValue()).isEqualTo("true");
    }

    @Test
    @DisplayName("Deve implementar EventBus")
    void shouldImplementEventBus() {
        assertThat(EventBus.class.isAssignableFrom(KafkaEventBus.class)).isTrue();
    }
}
