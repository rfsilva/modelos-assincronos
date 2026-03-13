package com.seguradora.hibrida.eventstore.controller;

import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.config.EventStoreHealthIndicator;
import com.seguradora.hibrida.eventstore.entity.EventStoreEntry;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventStoreController - Testes Unitários")
class EventStoreControllerTest {

    @Mock
    private EventStore eventStore;

    @Mock
    private EventStoreRepository repository;

    @Mock
    private EventStoreHealthIndicator healthIndicator;

    @InjectMocks
    private EventStoreController controller;

    @Test
    @DisplayName("Deve buscar eventos por aggregate ID")
    void shouldGetEventsByAggregateId() {
        // Given
        String aggregateId = "AGG-123";
        List<DomainEvent> events = Arrays.asList(
                mock(DomainEvent.class),
                mock(DomainEvent.class)
        );
        when(eventStore.loadEvents(aggregateId)).thenReturn(events);

        // When
        ResponseEntity<List<DomainEvent>> response = controller.getEventsByAggregateId(aggregateId, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);

        verify(eventStore).loadEvents(aggregateId);
    }

    @Test
    @DisplayName("Deve buscar eventos por aggregate ID a partir de versão específica")
    void shouldGetEventsByAggregateIdFromVersion() {
        // Given
        String aggregateId = "AGG-123";
        Long fromVersion = 5L;
        List<DomainEvent> events = Arrays.asList(mock(DomainEvent.class));
        when(eventStore.loadEvents(aggregateId, fromVersion)).thenReturn(events);

        // When
        ResponseEntity<List<DomainEvent>> response = controller.getEventsByAggregateId(aggregateId, fromVersion);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);

        verify(eventStore).loadEvents(aggregateId, fromVersion);
    }

    @Test
    @DisplayName("Deve buscar eventos por tipo")
    void shouldGetEventsByType() {
        // Given
        String eventType = "SinistroCriado";
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 23, 59);

        List<DomainEvent> events = Arrays.asList(mock(DomainEvent.class));
        when(eventStore.loadEventsByType(eq(eventType), any(Instant.class), any(Instant.class)))
                .thenReturn(events);

        // When
        ResponseEntity<List<DomainEvent>> response = controller.getEventsByType(eventType, from, to);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);

        verify(eventStore).loadEventsByType(eq(eventType), any(Instant.class), any(Instant.class));
    }

    @Test
    @DisplayName("Deve buscar eventos por correlation ID")
    void shouldGetEventsByCorrelationId() {
        // Given
        UUID correlationId = UUID.randomUUID();
        List<DomainEvent> events = Arrays.asList(
                mock(DomainEvent.class),
                mock(DomainEvent.class)
        );
        when(eventStore.loadEventsByCorrelationId(correlationId)).thenReturn(events);

        // When
        ResponseEntity<List<DomainEvent>> response = controller.getEventsByCorrelationId(correlationId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);

        verify(eventStore).loadEventsByCorrelationId(correlationId);
    }

    @Test
    @DisplayName("Deve retornar versão do aggregate")
    void shouldGetAggregateVersion() {
        // Given
        String aggregateId = "AGG-123";
        when(eventStore.getCurrentVersion(aggregateId)).thenReturn(10L);
        when(eventStore.aggregateExists(aggregateId)).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getAggregateVersion(aggregateId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("aggregateId")).isEqualTo(aggregateId);
        assertThat(response.getBody().get("version")).isEqualTo(10L);
        assertThat(response.getBody().get("exists")).isEqualTo(true);

        verify(eventStore).getCurrentVersion(aggregateId);
        verify(eventStore).aggregateExists(aggregateId);
    }

    @Test
    @DisplayName("Deve retornar versão 0 quando aggregate não existe")
    void shouldReturnVersion0WhenAggregateDoesNotExist() {
        // Given
        String aggregateId = "AGG-999";
        when(eventStore.getCurrentVersion(aggregateId)).thenReturn(0L);
        when(eventStore.aggregateExists(aggregateId)).thenReturn(false);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getAggregateVersion(aggregateId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("version")).isEqualTo(0L);
        assertThat(response.getBody().get("exists")).isEqualTo(false);
    }

    @Test
    @DisplayName("Deve retornar estatísticas do Event Store")
    void shouldGetStatistics() {
        // Given
        int hours = 24;
        List<Object[]> stats = Arrays.asList(
                new Object[]{"EventType1", 100L, 500.0},
                new Object[]{"EventType2", 200L, 750.0}
        );
        when(repository.getEventStatistics(any(Instant.class), any(Instant.class))).thenReturn(stats);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics(hours);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("period")).isEqualTo("24 horas");
        assertThat(response.getBody().get("totalEvents")).isEqualTo(300L);
        assertThat(response.getBody().get("totalSize")).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> eventStats =
                (Map<String, Map<String, Object>>) response.getBody().get("eventsByType");
        assertThat(eventStats).containsKeys("EventType1", "EventType2");

        verify(repository).getEventStatistics(any(Instant.class), any(Instant.class));
    }

    @Test
    @DisplayName("Deve calcular tamanho médio de eventos corretamente")
    void shouldCalculateAverageEventSizeCorrectly() {
        // Given
        List<Object[]> stats = Arrays.asList(
                new Object[]{"EventType1", 100L, 500.0},
                new Object[]{"EventType2", 100L, 500.0}
        );
        when(repository.getEventStatistics(any(Instant.class), any(Instant.class))).thenReturn(stats);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics(24);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        long totalEvents = (Long) response.getBody().get("totalEvents");
        long totalSize = (Long) response.getBody().get("totalSize");
        long averageSize = (Long) response.getBody().get("averageEventSize");

        assertThat(totalEvents).isEqualTo(200L);
        assertThat(averageSize).isEqualTo(totalSize / totalEvents);
    }

    @Test
    @DisplayName("Deve retornar eventos recentes com paginação")
    void shouldGetRecentEventsWithPagination() {
        // Given
        int page = 0;
        int size = 20;
        int hours = 24;

        List<EventStoreEntry> entries = Arrays.asList(
                mock(EventStoreEntry.class),
                mock(EventStoreEntry.class)
        );
        Page<EventStoreEntry> pageResult = new PageImpl<>(entries);

        when(repository.findEventsByPeriod(any(Instant.class), any(Instant.class), any(PageRequest.class)))
                .thenReturn(pageResult);

        // When
        ResponseEntity<Page<EventStoreEntry>> response = controller.getRecentEvents(page, size, hours);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);

        verify(repository).findEventsByPeriod(any(Instant.class), any(Instant.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("Deve retornar health check com status OK")
    void shouldReturnHealthCheckWithStatusOk() {
        // Given
        Map<String, Object> health = Map.of(
                "status", "UP",
                "details", Map.of("database", "connected")
        );
        when(healthIndicator.checkHealth()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");

        verify(healthIndicator).checkHealth();
    }

    @Test
    @DisplayName("Deve retornar 503 quando health check indica DOWN")
    void shouldReturn503WhenHealthCheckIndicatesDown() {
        // Given
        Map<String, Object> health = Map.of(
                "status", "DOWN",
                "error", "Database connection failed"
        );
        when(healthIndicator.checkHealth()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("DOWN");

        verify(healthIndicator).checkHealth();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há eventos")
    void shouldReturnEmptyListWhenNoEvents() {
        // Given
        String aggregateId = "AGG-123";
        when(eventStore.loadEvents(aggregateId)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<DomainEvent>> response = controller.getEventsByAggregateId(aggregateId, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar estatísticas vazias quando não há eventos no período")
    void shouldReturnEmptyStatisticsWhenNoEventsInPeriod() {
        // Given
        when(repository.getEventStatistics(any(Instant.class), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics(24);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalEvents")).isEqualTo(0L);
        assertThat(response.getBody().get("totalSize")).isEqualTo(0L);
        assertThat(response.getBody().get("averageEventSize")).isEqualTo(0L);
    }

    @Test
    @DisplayName("Deve tratar valores null em estatísticas")
    void shouldHandleNullValuesInStatistics() {
        // Given
        List<Object[]> stats = new ArrayList<>();
        stats.add(new Object[]{"EventType1", 100L, null});
        when(repository.getEventStatistics(any(Instant.class), any(Instant.class))).thenReturn(stats);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics(24);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> eventStats =
                (Map<String, Map<String, Object>>) response.getBody().get("eventsByType");

        Map<String, Object> type1Stats = eventStats.get("EventType1");
        assertThat(type1Stats.get("averageSize")).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve aceitar diferentes períodos de horas para estatísticas")
    void shouldAcceptDifferentHourPeriodsForStatistics() {
        // Given
        when(repository.getEventStatistics(any(Instant.class), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<Map<String, Object>> response1h = controller.getStatistics(1);
        ResponseEntity<Map<String, Object>> response24h = controller.getStatistics(24);
        ResponseEntity<Map<String, Object>> response168h = controller.getStatistics(168); // 7 days

        // Then
        assertThat(response1h.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1h.getBody().get("period")).isEqualTo("1 horas");

        assertThat(response24h.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response24h.getBody().get("period")).isEqualTo("24 horas");

        assertThat(response168h.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response168h.getBody().get("period")).isEqualTo("168 horas");
    }
}
