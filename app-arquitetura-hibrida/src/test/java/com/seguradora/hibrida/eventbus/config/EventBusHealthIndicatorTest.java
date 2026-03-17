package com.seguradora.hibrida.eventbus.config;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventBusStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link EventBusHealthIndicator}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EventBusHealthIndicator Tests")
class EventBusHealthIndicatorTest {

    @Mock
    private EventBus eventBus;

    private EventBusProperties properties;
    private EventBusHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        properties = new EventBusProperties();
        indicator = new EventBusHealthIndicator(eventBus, properties);
    }

    // =========================================================================
    // Anotações
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(EventBusHealthIndicator.class.isAnnotationPresent(Component.class)).isTrue();
    }

    // =========================================================================
    // checkHealth()
    // =========================================================================

    @Nested
    @DisplayName("checkHealth()")
    class CheckHealth {

        @Test
        @DisplayName("Deve retornar status UP quando EventBus está saudável")
        void shouldReturnUpWhenEventBusIsHealthy() {
            EventBusStatistics stats = new EventBusStatistics();
            when(eventBus.isHealthy()).thenReturn(true);
            when(eventBus.getStatistics()).thenReturn(stats);

            Map<String, Object> health = indicator.checkHealth();

            assertThat(health.get("status")).isEqualTo("UP");
            assertThat(health.get("healthy")).isEqualTo(true);
        }

        @Test
        @DisplayName("Deve retornar status DOWN quando EventBus não está saudável")
        void shouldReturnDownWhenEventBusIsUnhealthy() {
            when(eventBus.isHealthy()).thenReturn(false);
            when(eventBus.getStatistics()).thenReturn(new EventBusStatistics());

            Map<String, Object> health = indicator.checkHealth();

            assertThat(health.get("status")).isEqualTo("DOWN");
            assertThat(health.get("healthy")).isEqualTo(false);
        }

        @Test
        @DisplayName("Deve retornar status DOWN quando taxa de erro excede threshold")
        void shouldReturnDownWhenErrorRateExceedsThreshold() {
            EventBusStatistics stats = new EventBusStatistics();
            // Publicar 10 eventos, 5 falhados -> errorRate = 0.5 > threshold(0.1)
            for (int i = 0; i < 10; i++) stats.recordEventPublished("E");
            for (int i = 0; i < 5; i++) stats.recordEventFailed("E", true);

            when(eventBus.isHealthy()).thenReturn(true);
            when(eventBus.getStatistics()).thenReturn(stats);

            Map<String, Object> health = indicator.checkHealth();

            assertThat(health.get("status")).isEqualTo("DOWN");
        }

        @Test
        @DisplayName("Deve retornar status DOWN quando exception é lançada")
        void shouldReturnDownWhenExceptionThrown() {
            when(eventBus.isHealthy()).thenThrow(new RuntimeException("Erro"));

            Map<String, Object> health = indicator.checkHealth();

            assertThat(health.get("status")).isEqualTo("DOWN");
            assertThat(health.get("healthy")).isEqualTo(false);
        }

        @Test
        @DisplayName("Deve incluir campo configuration no resultado")
        void shouldIncludeConfigurationField() {
            when(eventBus.isHealthy()).thenReturn(true);
            when(eventBus.getStatistics()).thenReturn(new EventBusStatistics());

            Map<String, Object> health = indicator.checkHealth();

            assertThat(health).containsKey("configuration");
        }
    }

    // =========================================================================
    // isOperational()
    // =========================================================================

    @Test
    @DisplayName("isOperational() deve retornar true quando saudável")
    void isOperationalShouldReturnTrueWhenHealthy() {
        when(eventBus.isHealthy()).thenReturn(true);
        when(eventBus.getStatistics()).thenReturn(new EventBusStatistics());

        assertThat(indicator.isOperational()).isTrue();
    }

    @Test
    @DisplayName("isOperational() deve retornar false quando não saudável")
    void isOperationalShouldReturnFalseWhenUnhealthy() {
        when(eventBus.isHealthy()).thenReturn(false);
        when(eventBus.getStatistics()).thenReturn(new EventBusStatistics());

        assertThat(indicator.isOperational()).isFalse();
    }

    // =========================================================================
    // getQuickStatus()
    // =========================================================================

    @Test
    @DisplayName("getQuickStatus() deve retornar mapa com campos esperados")
    void getQuickStatusShouldReturnMapWithExpectedFields() {
        when(eventBus.isHealthy()).thenReturn(true);
        when(eventBus.getStatistics()).thenReturn(new EventBusStatistics());

        Map<String, Object> status = indicator.getQuickStatus();

        assertThat(status).containsKeys("healthy", "eventsPublished", "eventsProcessed");
    }
}
