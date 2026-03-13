package com.seguradora.hibrida.cqrs.controller;

import com.seguradora.hibrida.cqrs.health.CQRSHealthIndicator;
import com.seguradora.hibrida.cqrs.metrics.CQRSMetrics;
import com.seguradora.hibrida.projection.tracking.ProjectionTracker;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CQRSController - Testes Unitários")
class CQRSControllerTest {

    @Mock
    private CQRSHealthIndicator healthIndicator;

    @Mock
    private CQRSMetrics metrics;

    @Mock
    private ProjectionTrackerRepository projectionTrackerRepository;

    @InjectMocks
    private CQRSController controller;

    @BeforeEach
    void setUp() {
        // Configurar métricas mock
        when(metrics.getCommandSideEvents()).thenReturn(1000.0);
        when(metrics.getQuerySideEvents()).thenReturn(980.0);
        when(metrics.getOverallLag()).thenReturn(20.0);
        when(metrics.getEstimatedLagSeconds()).thenReturn(2.5);
        when(metrics.getTotalProjections()).thenReturn(10.0);
        when(metrics.getActiveProjections()).thenReturn(9.0);
        when(metrics.getErrorProjections()).thenReturn(1.0);
        when(metrics.getStaleProjections()).thenReturn(0.0);
        when(metrics.getProjectionsThroughput()).thenReturn(50.0);
        when(metrics.getProjectionsErrorRate()).thenReturn(0.1);
        when(metrics.getHealthScore()).thenReturn(0.85);
    }

    @Test
    @DisplayName("Deve retornar health check com status UP")
    void shouldReturnHealthCheckWithStatusUp() {
        // Given
        Health health = Health.up()
                .withDetail("cqrs", "operational")
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
    @DisplayName("Deve retornar 503 quando status é DOWN")
    void shouldReturn503WhenStatusIsDown() {
        // Given
        Health health = Health.down()
                .withDetail("error", "CQRS not available")
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
    @DisplayName("Deve retornar status OK quando status é DEGRADED")
    void shouldReturnOkWhenStatusIsDegraded() {
        // Given
        Health health = Health.status(new Status("DEGRADED"))
                .withDetail("warning", "High lag")
                .build();
        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("DEGRADED");

        verify(healthIndicator).health();
    }

    @Test
    @DisplayName("Deve retornar 503 quando ocorre exceção no health check")
    void shouldReturn503WhenExceptionOccurs() {
        // Given
        when(healthIndicator.health()).thenThrow(new RuntimeException("Health check error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("DOWN");
        assertThat(response.getBody().get("error")).isEqualTo("Health check error");
    }

    @Test
    @DisplayName("Deve retornar status do CQRS com sucesso")
    void shouldReturnCqrsStatusSuccessfully() {
        // Given
        doNothing().when(metrics).forceUpdate();

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("commandSideEvents")).isEqualTo(1000L);
        assertThat(response.getBody().get("querySideEvents")).isEqualTo(980L);
        assertThat(response.getBody().get("lag")).isEqualTo(20L);
        assertThat(response.getBody().get("overallStatus")).isEqualTo("HEALTHY");

        verify(metrics).forceUpdate();
    }

    @Test
    @DisplayName("Deve retornar status DEGRADED quando health score entre 0.5 e 0.8")
    void shouldReturnDegradedStatusWhenHealthScoreBetween05And08() {
        // Given
        doNothing().when(metrics).forceUpdate();
        when(metrics.getHealthScore()).thenReturn(0.65);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("overallStatus")).isEqualTo("DEGRADED");
    }

    @Test
    @DisplayName("Deve retornar status UNHEALTHY quando health score menor que 0.5")
    void shouldReturnUnhealthyStatusWhenHealthScoreLessThan05() {
        // Given
        doNothing().when(metrics).forceUpdate();
        when(metrics.getHealthScore()).thenReturn(0.3);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("overallStatus")).isEqualTo("UNHEALTHY");
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorre erro ao obter status")
    void shouldReturn500WhenErrorGettingStatus() {
        // Given
        doThrow(new RuntimeException("Status error")).when(metrics).forceUpdate();

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("ERROR");
        assertThat(response.getBody().get("error")).isEqualTo("Status error");
    }

    @Test
    @DisplayName("Deve listar projeções com paginação")
    void shouldListProjectionsWithPagination() {
        // Given
        List<ProjectionTracker> projections = Arrays.asList(
                mock(ProjectionTracker.class),
                mock(ProjectionTracker.class)
        );
        Page<ProjectionTracker> page = new PageImpl<>(projections);
        when(projectionTrackerRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        ResponseEntity<Page<ProjectionTracker>> response = controller.getProjections(Pageable.unpaged());

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);

        verify(projectionTrackerRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorre erro ao listar projeções")
    void shouldReturn500WhenErrorListingProjections() {
        // Given
        when(projectionTrackerRepository.findAll(any(Pageable.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<Page<ProjectionTracker>> response = controller.getProjections(Pageable.unpaged());

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Deve retornar projeção específica quando encontrada")
    void shouldReturnProjectionWhenFound() {
        // Given
        ProjectionTracker projection = mock(ProjectionTracker.class);
        when(projectionTrackerRepository.findByProjectionName("SinistroProjection"))
                .thenReturn(Optional.of(projection));

        // When
        ResponseEntity<ProjectionTracker> response = controller.getProjection("SinistroProjection");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(projectionTrackerRepository).findByProjectionName("SinistroProjection");
    }

    @Test
    @DisplayName("Deve retornar 404 quando projeção não é encontrada")
    void shouldReturn404WhenProjectionNotFound() {
        // Given
        when(projectionTrackerRepository.findByProjectionName("NonExistent"))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<ProjectionTracker> response = controller.getProjection("NonExistent");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Deve retornar métricas do CQRS com sucesso")
    void shouldReturnCqrsMetricsSuccessfully() {
        // Given
        doNothing().when(metrics).forceUpdate();

        // When
        ResponseEntity<Map<String, Object>> response = controller.getMetrics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("lag", "projections", "health");

        @SuppressWarnings("unchecked")
        Map<String, Object> lagMetrics = (Map<String, Object>) response.getBody().get("lag");
        assertThat(lagMetrics.get("commandSideEvents")).isEqualTo(1000L);
        assertThat(lagMetrics.get("querySideEvents")).isEqualTo(980L);
        assertThat(lagMetrics.get("overallLag")).isEqualTo(20L);

        verify(metrics).forceUpdate();
    }

    @Test
    @DisplayName("Deve atualizar métricas com sucesso")
    void shouldRefreshMetricsSuccessfully() {
        // Given
        doNothing().when(metrics).forceUpdate();

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshMetrics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Métricas atualizadas com sucesso");

        verify(metrics).forceUpdate();
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorre erro ao atualizar métricas")
    void shouldReturn500WhenErrorRefreshingMetrics() {
        // Given
        doThrow(new RuntimeException("Refresh error")).when(metrics).forceUpdate();

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshMetrics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Refresh error");
    }

    @Test
    @DisplayName("Deve retornar dashboard com alertas de lag crítico")
    void shouldReturnDashboardWithCriticalLagAlerts() {
        // Given
        doNothing().when(metrics).forceUpdate();
        when(metrics.getOverallLag()).thenReturn(6000.0); // > 5000

        // When
        ResponseEntity<Map<String, Object>> response = controller.getDashboard();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        List<String> alerts = (List<String>) response.getBody().get("alerts");
        assertThat(alerts).anyMatch(alert -> alert.contains("LAG_CRÍTICO"));
    }

    @Test
    @DisplayName("Deve retornar dashboard com alertas de lag alto")
    void shouldReturnDashboardWithHighLagAlerts() {
        // Given
        doNothing().when(metrics).forceUpdate();
        when(metrics.getOverallLag()).thenReturn(2000.0); // > 1000 e < 5000

        // When
        ResponseEntity<Map<String, Object>> response = controller.getDashboard();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        List<String> alerts = (List<String>) response.getBody().get("alerts");
        assertThat(alerts).anyMatch(alert -> alert.contains("LAG_ALTO"));
    }

    @Test
    @DisplayName("Deve retornar dashboard com alertas de projeções com erro")
    void shouldReturnDashboardWithErrorProjectionAlerts() {
        // Given
        doNothing().when(metrics).forceUpdate();
        when(metrics.getErrorProjections()).thenReturn(3.0);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getDashboard();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        List<String> alerts = (List<String>) response.getBody().get("alerts");
        assertThat(alerts).anyMatch(alert -> alert.contains("PROJEÇÕES_COM_ERRO"));
    }

    @Test
    @DisplayName("Deve retornar dashboard com alertas de alta taxa de erro")
    void shouldReturnDashboardWithHighErrorRateAlerts() {
        // Given
        doNothing().when(metrics).forceUpdate();
        when(metrics.getProjectionsErrorRate()).thenReturn(0.20); // > 0.15

        // When
        ResponseEntity<Map<String, Object>> response = controller.getDashboard();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        List<String> alerts = (List<String>) response.getBody().get("alerts");
        assertThat(alerts).anyMatch(alert -> alert.contains("TAXA_ERRO_ALTA"));
    }

    @Test
    @DisplayName("Deve retornar dashboard sem alertas quando sistema saudável")
    void shouldReturnDashboardWithoutAlertsWhenSystemHealthy() {
        // Given
        doNothing().when(metrics).forceUpdate();
        when(metrics.getOverallLag()).thenReturn(50.0); // < 1000
        when(metrics.getErrorProjections()).thenReturn(0.0);
        when(metrics.getProjectionsErrorRate()).thenReturn(0.05); // < 0.15

        // When
        ResponseEntity<Map<String, Object>> response = controller.getDashboard();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        List<String> alerts = (List<String>) response.getBody().get("alerts");
        assertThat(alerts).isEmpty();
    }
}
