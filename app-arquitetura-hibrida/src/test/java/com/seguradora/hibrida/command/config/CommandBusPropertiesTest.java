package com.seguradora.hibrida.command.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link CommandBusProperties}.
 */
@DisplayName("CommandBusProperties Tests")
class CommandBusPropertiesTest {

    private CommandBusProperties properties;

    @BeforeEach
    void setUp() {
        properties = new CommandBusProperties();
    }

    @Test
    @DisplayName("Deve criar com valores padrão")
    void shouldCreateWithDefaultValues() {
        // Then
        assertThat(properties).isNotNull();
        assertThat(properties.getDefaultTimeout()).isEqualTo(30);
        assertThat(properties.getAsyncPoolSize()).isEqualTo(10);
        assertThat(properties.isMetricsEnabled()).isTrue();
        assertThat(properties.isValidationEnabled()).isTrue();
        assertThat(properties.isDetailedLogging()).isFalse();
        assertThat(properties.getRetry()).isNotNull();
        assertThat(properties.getCircuitBreaker()).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar propriedades principais")
    void shouldConfigureMainProperties() {
        // When
        properties.setDefaultTimeout(60);
        properties.setAsyncPoolSize(20);
        properties.setMetricsEnabled(false);
        properties.setValidationEnabled(false);
        properties.setDetailedLogging(true);

        // Then
        assertThat(properties.getDefaultTimeout()).isEqualTo(60);
        assertThat(properties.getAsyncPoolSize()).isEqualTo(20);
        assertThat(properties.isMetricsEnabled()).isFalse();
        assertThat(properties.isValidationEnabled()).isFalse();
        assertThat(properties.isDetailedLogging()).isTrue();
    }

    @Test
    @DisplayName("Deve configurar retry")
    void shouldConfigureRetry() {
        // Given
        CommandBusProperties.Retry retry = new CommandBusProperties.Retry();
        retry.setEnabled(true);
        retry.setMaxAttempts(5);
        retry.setDelayMs(2000);
        retry.setBackoffMultiplier(3.0);
        retry.setMaxDelayMs(60000);

        // When
        properties.setRetry(retry);

        // Then
        assertThat(properties.getRetry().isEnabled()).isTrue();
        assertThat(properties.getRetry().getMaxAttempts()).isEqualTo(5);
        assertThat(properties.getRetry().getDelayMs()).isEqualTo(2000);
        assertThat(properties.getRetry().getBackoffMultiplier()).isEqualTo(3.0);
        assertThat(properties.getRetry().getMaxDelayMs()).isEqualTo(60000);
    }

    @Test
    @DisplayName("Deve ter valores padrão de retry corretos")
    void shouldHaveCorrectRetryDefaults() {
        // Given
        CommandBusProperties.Retry retry = properties.getRetry();

        // Then
        assertThat(retry.isEnabled()).isFalse();
        assertThat(retry.getMaxAttempts()).isEqualTo(3);
        assertThat(retry.getDelayMs()).isEqualTo(1000);
        assertThat(retry.getBackoffMultiplier()).isEqualTo(2.0);
        assertThat(retry.getMaxDelayMs()).isEqualTo(30000);
    }

    @Test
    @DisplayName("Deve configurar circuit breaker")
    void shouldConfigureCircuitBreaker() {
        // Given
        CommandBusProperties.CircuitBreaker circuitBreaker = new CommandBusProperties.CircuitBreaker();
        circuitBreaker.setEnabled(true);
        circuitBreaker.setFailureThreshold(10);
        circuitBreaker.setRecoveryTimeoutMs(120000);
        circuitBreaker.setSuccessThreshold(0.9);

        // When
        properties.setCircuitBreaker(circuitBreaker);

        // Then
        assertThat(properties.getCircuitBreaker().isEnabled()).isTrue();
        assertThat(properties.getCircuitBreaker().getFailureThreshold()).isEqualTo(10);
        assertThat(properties.getCircuitBreaker().getRecoveryTimeoutMs()).isEqualTo(120000);
        assertThat(properties.getCircuitBreaker().getSuccessThreshold()).isEqualTo(0.9);
    }

    @Test
    @DisplayName("Deve ter valores padrão de circuit breaker corretos")
    void shouldHaveCorrectCircuitBreakerDefaults() {
        // Given
        CommandBusProperties.CircuitBreaker circuitBreaker = properties.getCircuitBreaker();

        // Then
        assertThat(circuitBreaker.isEnabled()).isFalse();
        assertThat(circuitBreaker.getFailureThreshold()).isEqualTo(5);
        assertThat(circuitBreaker.getRecoveryTimeoutMs()).isEqualTo(60000);
        assertThat(circuitBreaker.getSuccessThreshold()).isEqualTo(0.8);
    }
}
