package com.seguradora.hibrida.aggregate.controller;

import com.seguradora.hibrida.aggregate.health.AggregateHealthIndicator;
import com.seguradora.hibrida.aggregate.metrics.AggregateMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AggregateController - Testes Unitários")
class AggregateControllerTest {

    @Mock
    private AggregateHealthIndicator healthIndicator;

    @Mock
    private AggregateMetrics metrics;

    @InjectMocks
    private AggregateController controller;

    private AggregateMetrics.MetricsStatistics mockStats;

    @BeforeEach
    void setUp() {
        mockStats = AggregateMetrics.MetricsStatistics.builder()
                .totalSaves(100L)
                .totalLoads(200L)
                .totalSnapshots(10L)
                .totalValidations(300L)
                .totalErrors(5L)
                .averageSaveTime(50.0)
                .averageLoadTime(30.0)
                .averageReconstructionTime(80.0)
                .averageValidationTime(20.0)
                .build();
    }

    @Test
    @DisplayName("Deve retornar health check com status UP")
    void shouldReturnHealthCheckWithStatusUp() {
        // Given
        Health health = Health.up()
                .withDetail("component", "aggregates")
                .build();
        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        verify(healthIndicator).health();
    }

    @Test
    @DisplayName("Deve retornar 503 quando status não é UP")
    void shouldReturn503WhenStatusIsNotUp() {
        // Given
        Health health = Health.down()
                .withDetail("error", "Service unavailable")
                .build();
        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("DOWN");
        verify(healthIndicator).health();
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorre exceção no health check")
    void shouldReturn500WhenExceptionOccurs() {
        // Given
        when(healthIndicator.health()).thenThrow(new RuntimeException("Health check failed"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("ERROR");
        assertThat(response.getBody().get("message")).isEqualTo("Health check failed");
        verify(healthIndicator).health();
    }

    @Test
    @DisplayName("Deve retornar métricas com sucesso")
    void shouldReturnMetricsSuccessfully() {
        // Given
        when(metrics.getStatistics()).thenReturn(mockStats);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getMetrics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> statistics = (Map<String, Object>) response.getBody().get("statistics");
        assertThat(statistics.get("totalSaves")).isEqualTo(100L);
        assertThat(statistics.get("totalLoads")).isEqualTo(200L);
        assertThat(statistics.get("totalSnapshots")).isEqualTo(10L);
        assertThat(statistics.get("totalValidations")).isEqualTo(300L);
        assertThat(statistics.get("totalErrors")).isEqualTo(5L);

        @SuppressWarnings("unchecked")
        Map<String, Object> performance = (Map<String, Object>) response.getBody().get("performance");
        assertThat(performance.get("averageSaveTimeMs")).isEqualTo(50.0);
        assertThat(performance.get("averageLoadTimeMs")).isEqualTo(30.0);

        verify(metrics).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorre erro ao obter métricas")
    void shouldReturn500WhenErrorGettingMetrics() {
        // Given
        when(metrics.getStatistics()).thenThrow(new RuntimeException("Metrics error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.getMetrics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Metrics error");
        verify(metrics).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar status rápido com sucesso")
    void shouldReturnQuickStatusSuccessfully() {
        // Given
        Health health = Health.up().build();
        when(healthIndicator.health()).thenReturn(health);
        when(metrics.getStatistics()).thenReturn(mockStats);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getQuickStatus();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("totalOperations")).isEqualTo(300L); // 100 saves + 200 loads
        assertThat(response.getBody().get("errorRate")).isNotNull();
        assertThat(response.getBody().get("uptime")).isNotNull();

        verify(healthIndicator).health();
        verify(metrics).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorre erro ao obter status rápido")
    void shouldReturn500WhenErrorGettingQuickStatus() {
        // Given
        when(healthIndicator.health()).thenThrow(new RuntimeException("Status error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.getQuickStatus();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("ERROR");
        verify(healthIndicator).health();
    }

    @Test
    @DisplayName("Deve retornar configuração com sucesso")
    void shouldReturnConfigurationSuccessfully() {
        // When
        ResponseEntity<Map<String, Object>> response = controller.getConfiguration();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("version")).isEqualTo("1.0.0");
        assertThat(response.getBody().get("javaVersion")).isNotNull();
        assertThat(response.getBody().get("springBootVersion")).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> features = (Map<String, Object>) response.getBody().get("features");
        assertThat(features.get("metricsEnabled")).isEqualTo(true);
        assertThat(features.get("healthCheckEnabled")).isEqualTo(true);
        assertThat(features.get("snapshotSupport")).isEqualTo(true);
        assertThat(features.get("validationEnabled")).isEqualTo(true);
        assertThat(features.get("cacheEnabled")).isEqualTo(true);
    }

    @Test
    @DisplayName("Deve calcular taxa de erro corretamente")
    void shouldCalculateErrorRateCorrectly() {
        // Given
        Health health = Health.up().build();
        when(healthIndicator.health()).thenReturn(health);
        when(metrics.getStatistics()).thenReturn(mockStats);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getQuickStatus();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Double errorRate = (Double) response.getBody().get("errorRate");
        // errorRate = (5 errors / 300 total operations) * 100 = 1.67%
        assertThat(errorRate).isCloseTo(1.67, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("Deve retornar taxa de erro zero quando não há operações")
    void shouldReturnZeroErrorRateWhenNoOperations() {
        // Given
        AggregateMetrics.MetricsStatistics emptyStats = AggregateMetrics.MetricsStatistics.builder()
                .totalSaves(0L)
                .totalLoads(0L)
                .totalSnapshots(0L)
                .totalValidations(0L)
                .totalErrors(0L)
                .averageSaveTime(0.0)
                .averageLoadTime(0.0)
                .averageReconstructionTime(0.0)
                .averageValidationTime(0.0)
                .build();

        Health health = Health.up().build();
        when(healthIndicator.health()).thenReturn(health);
        when(metrics.getStatistics()).thenReturn(emptyStats);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getQuickStatus();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Double errorRate = (Double) response.getBody().get("errorRate");
        assertThat(errorRate).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Deve formatar uptime corretamente")
    void shouldFormatUptimeCorrectly() {
        // Given
        Health health = Health.up().build();
        when(healthIndicator.health()).thenReturn(health);
        when(metrics.getStatistics()).thenReturn(mockStats);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getQuickStatus();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String uptime = (String) response.getBody().get("uptime");
        assertThat(uptime).isNotNull();
        assertThat(uptime).matches(".*[dhms].*"); // Deve conter d, h, m ou s
    }

    @Test
    @DisplayName("Deve retornar health check com status DEGRADED")
    void shouldReturnHealthCheckWithStatusDegraded() {
        // Given
        Health health = Health.status(new Status("DEGRADED"))
                .withDetail("warning", "Performance degraded")
                .build();
        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("DEGRADED");
        verify(healthIndicator).health();
    }
}
