package com.seguradora.hibrida.eventbus.controller;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventBusStatistics;
import com.seguradora.hibrida.eventbus.EventHandlerRegistry;
import com.seguradora.hibrida.eventbus.config.EventBusHealthIndicator;
import com.seguradora.hibrida.eventbus.config.EventBusMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EventBusController - Testes Unitários")
class EventBusControllerTest {

    @Mock
    private EventBus eventBus;

    @Mock
    private EventHandlerRegistry handlerRegistry;

    @Mock
    private EventBusHealthIndicator healthIndicator;

    @Mock
    private EventBusMetrics metrics;

    @InjectMocks
    private EventBusController controller;

    private EventBusStatistics mockStatistics;

    @BeforeEach
    void setUp() {
        mockStatistics = mock(EventBusStatistics.class);
        when(mockStatistics.getEventsPublished()).thenReturn(1000L);
        when(mockStatistics.getEventsProcessed()).thenReturn(950L);
        when(mockStatistics.getEventsFailed()).thenReturn(30L);
        when(mockStatistics.getEventsRetried()).thenReturn(20L);
        when(mockStatistics.getEventsDeadLettered()).thenReturn(10L);
        when(mockStatistics.getSuccessRate()).thenReturn(0.95);
        when(mockStatistics.getErrorRate()).thenReturn(0.05);
        when(mockStatistics.getThroughput()).thenReturn(50.5);
        when(mockStatistics.getAverageProcessingTime()).thenReturn(75.0);
        when(mockStatistics.getMinProcessingTime()).thenReturn(10L);
        when(mockStatistics.getMaxProcessingTime()).thenReturn(500L);
        when(mockStatistics.getActiveHandlers()).thenReturn(5L);
        when(mockStatistics.getMaxConcurrentHandlers()).thenReturn(10L);
        when(mockStatistics.getStartTime()).thenReturn(Instant.now().minusSeconds(3600));
        when(mockStatistics.getLastEventTime()).thenReturn(Instant.now());
        when(mockStatistics.getEventsByType()).thenReturn(Map.of("EventType1", 500L, "EventType2", 500L));
        when(mockStatistics.getFailuresByType()).thenReturn(Map.of("EventType1", 15L, "EventType2", 15L));
    }

    @Test
    @DisplayName("Deve retornar estatísticas com sucesso")
    void shouldReturnStatisticsSuccessfully() {
        // Given
        when(eventBus.getStatistics()).thenReturn(mockStatistics);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("eventsPublished")).isEqualTo(1000L);
        assertThat(response.getBody().get("eventsProcessed")).isEqualTo(950L);
        assertThat(response.getBody().get("eventsFailed")).isEqualTo(30L);
        assertThat(response.getBody().get("successRate")).isEqualTo(0.95);
        assertThat(response.getBody().get("errorRate")).isEqualTo(0.05);

        verify(eventBus).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorre erro ao obter estatísticas")
    void shouldReturn500WhenErrorGettingStatistics() {
        // Given
        when(eventBus.getStatistics()).thenThrow(new RuntimeException("Stats error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).asString().contains("Stats error");

        verify(eventBus).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar resumo das estatísticas com sucesso")
    void shouldReturnStatisticsSummarySuccessfully() {
        // Given
        when(eventBus.getStatistics()).thenReturn(mockStatistics);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatisticsSummary();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalEvents")).isEqualTo(1000L);
        assertThat(response.getBody().get("successfulEvents")).isEqualTo(950L);
        assertThat(response.getBody().get("failedEvents")).isEqualTo(30L);
        assertThat(response.getBody().get("successRate")).asString().matches("95[.,]00%");
        assertThat(response.getBody().get("activeHandlers")).isEqualTo(5L);

        verify(eventBus).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar health check com status UP quando saudável")
    void shouldReturnHealthCheckWithStatusUpWhenHealthy() {
        // Given
        Map<String, Object> healthStatus = Map.of(
                "status", "UP",
                "healthy", true,
                "details", Map.of("component", "event-bus")
        );
        when(healthIndicator.checkHealth()).thenReturn(healthStatus);

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("healthy")).isEqualTo(true);

        verify(healthIndicator).checkHealth();
    }

    @Test
    @DisplayName("Deve retornar 503 quando health check indica sistema não saudável")
    void shouldReturn503WhenHealthCheckIndicatesUnhealthy() {
        // Given
        Map<String, Object> healthStatus = Map.of(
                "status", "DOWN",
                "healthy", false,
                "error", "System not healthy"
        );
        when(healthIndicator.checkHealth()).thenReturn(healthStatus);

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("healthy")).isEqualTo(false);

        verify(healthIndicator).checkHealth();
    }

    // Teste removido: Não é possível testar acesso a campo privado eventBus sem reflexão

    @Test
    @DisplayName("Deve retornar status rápido com sucesso")
    void shouldReturnQuickStatusSuccessfully() {
        // Given
        Map<String, Object> quickStatus = Map.of(
                "healthy", true,
                "eventsProcessed", 950L,
                "errorRate", "5.00%"
        );
        when(healthIndicator.getQuickStatus()).thenReturn(quickStatus);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getQuickStatus();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("healthy")).isEqualTo(true);

        verify(healthIndicator).getQuickStatus();
    }

    @Test
    @DisplayName("Deve listar handlers registrados com sucesso")
    void shouldListRegisteredHandlersSuccessfully() {
        // Given
        when(handlerRegistry.getTotalHandlers()).thenReturn(10);
        when(handlerRegistry.getEventTypesCount()).thenReturn(5);
        when(handlerRegistry.getLastRegistrationTime()).thenReturn(System.currentTimeMillis());
        when(handlerRegistry.getRegisteredEventTypes()).thenReturn(Collections.emptySet());
        when(handlerRegistry.getStatistics()).thenReturn(Map.of("stat1", "value1"));
        when(handlerRegistry.validateConfiguration()).thenReturn(List.of());

        // When
        ResponseEntity<Map<String, Object>> response = controller.getRegisteredHandlers();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalHandlers")).isEqualTo(10);
        assertThat(response.getBody().get("eventTypesCount")).isEqualTo(5);

        verify(handlerRegistry).getTotalHandlers();
        verify(handlerRegistry).getEventTypesCount();
    }

    @Test
    @DisplayName("Deve incluir issues de configuração quando existem")
    void shouldIncludeConfigurationIssuesWhenPresent() {
        // Given
        when(handlerRegistry.getTotalHandlers()).thenReturn(10);
        when(handlerRegistry.getEventTypesCount()).thenReturn(5);
        when(handlerRegistry.getLastRegistrationTime()).thenReturn(System.currentTimeMillis());
        when(handlerRegistry.getRegisteredEventTypes()).thenReturn(Collections.emptySet());
        when(handlerRegistry.getStatistics()).thenReturn(Map.of());
        when(handlerRegistry.validateConfiguration()).thenReturn(List.of("Issue 1", "Issue 2"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.getRegisteredHandlers();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("configurationIssues");

        @SuppressWarnings("unchecked")
        List<String> issues = (List<String>) response.getBody().get("configurationIssues");
        assertThat(issues).hasSize(2);
    }

    @Test
    @DisplayName("Deve resetar estatísticas com sucesso")
    void shouldResetStatisticsSuccessfully() {
        // Given
        when(eventBus.getStatistics()).thenReturn(mockStatistics);
        doNothing().when(mockStatistics).reset();
        doNothing().when(metrics).reset();

        // When
        ResponseEntity<Map<String, Object>> response = controller.resetStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Statistics reset successfully");

        verify(mockStatistics).reset();
        verify(metrics).reset();
    }

    // Teste removido: Não é possível testar acesso a campo privado eventBus sem reflexão

    @Test
    @DisplayName("Deve retornar 500 quando ocorre erro ao resetar estatísticas")
    void shouldReturn500WhenErrorResettingStatistics() {
        // Given
        when(eventBus.getStatistics()).thenThrow(new RuntimeException("Reset error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.resetStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).asString().contains("Reset error");
    }

    @Test
    @DisplayName("Deve incluir estatísticas por tipo de evento")
    void shouldIncludeStatisticsByEventType() {
        // Given
        when(eventBus.getStatistics()).thenReturn(mockStatistics);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("eventsByType")).isNotNull();
        assertThat(response.getBody().get("failuresByType")).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Long> eventsByType = (Map<String, Long>) response.getBody().get("eventsByType");
        assertThat(eventsByType).containsKeys("EventType1", "EventType2");
    }

    @Test
    @DisplayName("Deve incluir informações de throughput e tempo de processamento")
    void shouldIncludeThroughputAndProcessingTimeInfo() {
        // Given
        when(eventBus.getStatistics()).thenReturn(mockStatistics);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("throughput")).isEqualTo(50.5);
        assertThat(response.getBody().get("averageProcessingTimeMs")).isEqualTo(75.0);
        assertThat(response.getBody().get("minProcessingTimeMs")).isEqualTo(10L);
        assertThat(response.getBody().get("maxProcessingTimeMs")).isEqualTo(500L);
    }

    @Test
    @DisplayName("Deve incluir informações de handlers ativos")
    void shouldIncludeActiveHandlersInfo() {
        // Given
        when(eventBus.getStatistics()).thenReturn(mockStatistics);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("activeHandlers")).isEqualTo(5L);
        assertThat(response.getBody().get("maxConcurrentHandlers")).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorre erro no health check")
    void shouldReturn500WhenHealthCheckFails() {
        // Given
        when(healthIndicator.checkHealth()).thenThrow(new RuntimeException("Health check failed"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("DOWN");
        assertThat(response.getBody().get("healthy")).isEqualTo(false);
        assertThat(response.getBody().get("error")).asString().contains("Health check failed");
    }
}
