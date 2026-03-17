package com.seguradora.hibrida.eventstore.replay.config;

import com.seguradora.hibrida.eventstore.replay.EventReplayer;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link ReplayConfiguration}.
 */
@DisplayName("ReplayConfiguration (Spring Config) Tests")
class ReplayConfigurationTest {

    @Test
    @DisplayName("Deve estar anotado com @Configuration")
    void shouldBeAnnotatedWithConfiguration() {
        assertThat(ReplayConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @EnableConfigurationProperties para ReplayProperties")
    void shouldBeAnnotatedWithEnableConfigurationProperties() {
        assertThat(ReplayConfiguration.class.isAnnotationPresent(EnableConfigurationProperties.class)).isTrue();
        EnableConfigurationProperties annotation =
                ReplayConfiguration.class.getAnnotation(EnableConfigurationProperties.class);
        assertThat(annotation.value()).contains(ReplayProperties.class);
    }

    @Test
    @DisplayName("Deve estar anotado com @ConditionalOnProperty para eventstore.replay.enabled")
    void shouldBeAnnotatedWithConditionalOnProperty() {
        assertThat(ReplayConfiguration.class.isAnnotationPresent(ConditionalOnProperty.class)).isTrue();
        ConditionalOnProperty prop = ReplayConfiguration.class.getAnnotation(ConditionalOnProperty.class);
        assertThat(prop.prefix()).isEqualTo("eventstore.replay");
        assertThat(prop.name()).contains("enabled");
        assertThat(prop.havingValue()).isEqualTo("true");
        assertThat(prop.matchIfMissing()).isTrue();
    }

    @Test
    @DisplayName("Deve declarar bean eventReplayer")
    void shouldDeclareEventReplayerBean() throws NoSuchMethodException {
        var method = ReplayConfiguration.class.getMethod(
                "eventReplayer",
                com.seguradora.hibrida.eventstore.EventStore.class,
                com.seguradora.hibrida.eventbus.EventBus.class,
                com.seguradora.hibrida.eventbus.EventHandlerRegistry.class);
        assertThat(method.isAnnotationPresent(Bean.class)).isTrue();
        assertThat(method.getReturnType()).isEqualTo(EventReplayer.class);
    }

    @Test
    @DisplayName("Deve declarar bean replayHealthIndicator")
    void shouldDeclareReplayHealthIndicatorBean() throws NoSuchMethodException {
        var method = ReplayConfiguration.class.getMethod("replayHealthIndicator", EventReplayer.class);
        assertThat(method.isAnnotationPresent(Bean.class)).isTrue();
        assertThat(method.getReturnType()).isEqualTo(ReplayHealthIndicator.class);
    }

    @Test
    @DisplayName("Deve declarar bean replayMetrics")
    void shouldDeclareReplayMetricsBean() throws NoSuchMethodException {
        var method = ReplayConfiguration.class.getMethod("replayMetrics", EventReplayer.class, MeterRegistry.class);
        assertThat(method.isAnnotationPresent(Bean.class)).isTrue();
        assertThat(method.getReturnType()).isEqualTo(ReplayMetrics.class);
    }
}
