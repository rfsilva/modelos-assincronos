package com.seguradora.hibrida.cqrs.health;

import com.seguradora.hibrida.eventstore.entity.EventStoreEntry;
import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionStatus;
import com.seguradora.hibrida.projection.tracking.ProjectionTracker;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CQRSHealthIndicator")
class CQRSHealthIndicatorTest {

    @Mock
    private EventStoreRepository eventStoreRepository;

    @Mock
    private ProjectionTrackerRepository projectionTrackerRepository;

    private CQRSHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new CQRSHealthIndicator(eventStoreRepository, projectionTrackerRepository);
    }

    @Test
    @DisplayName("Deve retornar UP quando sistema está saudável")
    void shouldReturnUpWhenSystemIsHealthy() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);

        ProjectionTracker tracker = createProjectionTracker("TestProjection", 95L, ProjectionStatus.ACTIVE,
                Instant.now(), 100L, 0L);
        when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));

        EventStoreEntry entry = new EventStoreEntry();
        entry.setAggregateId("agg-1");
        when(eventStoreRepository.findAll()).thenReturn(List.of(entry));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("lag");
        assertThat(health.getDetails()).containsKey("projections");
        assertThat(health.getDetails()).containsKey("datasources");
    }

    @Test
    @DisplayName("Deve retornar DOWN quando há lag crítico")
    void shouldReturnDownWhenCriticalLag() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(10000L);

        ProjectionTracker tracker = createProjectionTracker("TestProjection", 100L, ProjectionStatus.ACTIVE,
                Instant.now(), 100L, 0L);
        when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));

        EventStoreEntry entry = new EventStoreEntry();
        entry.setAggregateId("agg-1");
        when(eventStoreRepository.findAll()).thenReturn(List.of(entry));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);

        @SuppressWarnings("unchecked")
        Map<String, Object> lagInfo = (Map<String, Object>) health.getDetails().get("lag");
        assertThat(lagInfo.get("status")).isEqualTo("CRITICAL_LAG");
        assertThat(lagInfo.get("overallLag")).isEqualTo(9900L);
    }

    @Test
    @DisplayName("Deve retornar DEGRADED quando há lag alto mas não crítico")
    void shouldReturnDegradedWhenHighLag() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(2000L);

        ProjectionTracker tracker = createProjectionTracker("TestProjection", 500L, ProjectionStatus.ACTIVE,
                Instant.now(), 500L, 0L);
        when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));

        EventStoreEntry entry = new EventStoreEntry();
        entry.setAggregateId("agg-1");
        when(eventStoreRepository.findAll()).thenReturn(List.of(entry));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus().getCode()).isEqualTo("DEGRADED");

        @SuppressWarnings("unchecked")
        Map<String, Object> lagInfo = (Map<String, Object>) health.getDetails().get("lag");
        assertThat(lagInfo.get("status")).isEqualTo("HIGH_LAG");
    }

    @Test
    @DisplayName("Deve retornar DOWN quando projeções estão em erro")
    void shouldReturnDownWhenProjectionsInError() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);

        ProjectionTracker errorTracker = createProjectionTracker("ErrorProjection", 50L, ProjectionStatus.ERROR,
                Instant.now(), 50L, 10L);
        when(projectionTrackerRepository.findAll()).thenReturn(List.of(errorTracker));

        EventStoreEntry entry = new EventStoreEntry();
        entry.setAggregateId("agg-1");
        when(eventStoreRepository.findAll()).thenReturn(List.of(entry));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);

        @SuppressWarnings("unchecked")
        Map<String, Object> projectionsInfo = (Map<String, Object>) health.getDetails().get("projections");
        assertThat(projectionsInfo.get("status")).isEqualTo("CRITICAL");

        @SuppressWarnings("unchecked")
        List<String> errorProjections = (List<String>) projectionsInfo.get("errorProjections");
        assertThat(errorProjections).contains("ErrorProjection");
    }

    @Test
    @DisplayName("Deve detectar projeções obsoletas")
    void shouldDetectStaleProjections() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);

        Instant staleTime = Instant.now().minus(60, ChronoUnit.MINUTES);
        ProjectionTracker staleTracker = createProjectionTracker("StaleProjection", 90L, ProjectionStatus.ACTIVE,
                staleTime, 90L, 0L);
        when(projectionTrackerRepository.findAll()).thenReturn(List.of(staleTracker));

        EventStoreEntry entry = new EventStoreEntry();
        entry.setAggregateId("agg-1");
        when(eventStoreRepository.findAll()).thenReturn(List.of(entry));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> projectionsInfo = (Map<String, Object>) health.getDetails().get("projections");

        @SuppressWarnings("unchecked")
        List<String> staleProjections = (List<String>) projectionsInfo.get("staleProjections");
        assertThat(staleProjections).contains("StaleProjection");
    }

    @Test
    @DisplayName("Deve calcular taxa de erro das projeções")
    void shouldCalculateProjectionErrorRate() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);

        ProjectionTracker tracker = createProjectionTracker("TestProjection", 95L, ProjectionStatus.ACTIVE,
                Instant.now(), 100L, 20L); // 20% de erro
        when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));

        EventStoreEntry entry = new EventStoreEntry();
        entry.setAggregateId("agg-1");
        when(eventStoreRepository.findAll()).thenReturn(List.of(entry));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> projectionsInfo = (Map<String, Object>) health.getDetails().get("projections");
        assertThat(projectionsInfo.get("errorRate")).isEqualTo(0.2);
        assertThat(projectionsInfo.get("status")).isEqualTo("CRITICAL"); // > 15%
    }

    @Test
    @DisplayName("Deve retornar NO_PROJECTIONS quando não há projeções")
    void shouldReturnNoProjectionsWhenEmpty() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);
        when(projectionTrackerRepository.findAll()).thenReturn(Collections.emptyList());
        when(eventStoreRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> lagInfo = (Map<String, Object>) health.getDetails().get("lag");
        assertThat(lagInfo.get("status")).isEqualTo("NO_PROJECTIONS");

        @SuppressWarnings("unchecked")
        Map<String, Object> projectionsInfo = (Map<String, Object>) health.getDetails().get("projections");
        assertThat(projectionsInfo.get("status")).isEqualTo("NO_PROJECTIONS");
    }

    @Test
    @DisplayName("Deve testar conectividade dos datasources")
    void shouldTestDatasourcesConnectivity() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);
        when(projectionTrackerRepository.count()).thenReturn(2L);

        ProjectionTracker tracker = createProjectionTracker("TestProjection", 95L, ProjectionStatus.ACTIVE,
                Instant.now(), 100L, 0L);
        when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));

        EventStoreEntry entry = new EventStoreEntry();
        entry.setAggregateId("agg-1");
        when(eventStoreRepository.findAll()).thenReturn(List.of(entry));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> datasourcesInfo = (Map<String, Object>) health.getDetails().get("datasources");
        assertThat(datasourcesInfo.get("writeDataSource")).isEqualTo("UP");
        assertThat(datasourcesInfo.get("readDataSource")).isEqualTo("UP");
        assertThat(datasourcesInfo.get("status")).isEqualTo("HEALTHY");
    }

    @Test
    @DisplayName("Deve detectar falha no write datasource")
    void shouldDetectWriteDataSourceFailure() {
        // Arrange
        when(eventStoreRepository.count()).thenThrow(new RuntimeException("Database connection failed"));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        // O erro fica dentro dos detalhes, não no nível raiz quando há exceção no checkHealth
        @SuppressWarnings("unchecked")
        Map<String, Object> lagInfo = (Map<String, Object>) health.getDetails().get("lag");
        assertThat(lagInfo.get("status")).isEqualTo("ERROR");
    }

    @Test
    @DisplayName("Deve calcular métricas gerais do sistema")
    void shouldCalculateGeneralMetrics() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(500L);

        EventStoreEntry entry1 = new EventStoreEntry();
        entry1.setAggregateId("agg-1");
        EventStoreEntry entry2 = new EventStoreEntry();
        entry2.setAggregateId("agg-2");
        EventStoreEntry entry3 = new EventStoreEntry();
        entry3.setAggregateId("agg-1");
        when(eventStoreRepository.findAll()).thenReturn(Arrays.asList(entry1, entry2, entry3));

        ProjectionTracker activeTracker = createProjectionTracker("ActiveProjection", 495L, ProjectionStatus.ACTIVE,
                Instant.now(), 500L, 0L);
        ProjectionTracker pausedTracker = createProjectionTracker("PausedProjection", 400L, ProjectionStatus.PAUSED,
                Instant.now(), 400L, 0L);
        when(projectionTrackerRepository.findAll()).thenReturn(Arrays.asList(activeTracker, pausedTracker));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) health.getDetails().get("metrics");
        assertThat(metrics.get("totalEvents")).isEqualTo(500L);
        assertThat(metrics.get("totalAggregates")).isEqualTo(2L); // agg-1 e agg-2
        assertThat(metrics.get("totalProjections")).isEqualTo(2L);
        assertThat(metrics.get("activeProjections")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve calcular lag por projeção")
    void shouldCalculateLagByProjection() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(1000L);

        ProjectionTracker tracker1 = createProjectionTracker("Projection1", 950L, ProjectionStatus.ACTIVE,
                Instant.now(), 950L, 0L);
        ProjectionTracker tracker2 = createProjectionTracker("Projection2", 900L, ProjectionStatus.ACTIVE,
                Instant.now(), 900L, 0L);
        when(projectionTrackerRepository.findAll()).thenReturn(Arrays.asList(tracker1, tracker2));

        EventStoreEntry entry = new EventStoreEntry();
        entry.setAggregateId("agg-1");
        when(eventStoreRepository.findAll()).thenReturn(List.of(entry));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> lagInfo = (Map<String, Object>) health.getDetails().get("lag");

        @SuppressWarnings("unchecked")
        Map<String, Long> lagByProjection = (Map<String, Long>) lagInfo.get("lagByProjection");
        assertThat(lagByProjection).containsEntry("Projection1", 50L);
        assertThat(lagByProjection).containsEntry("Projection2", 100L);
        assertThat(lagInfo.get("overallLag")).isEqualTo(100L); // baseado na mais atrasada
    }

    @Test
    @DisplayName("Deve incluir timestamp na verificação de saúde")
    void shouldIncludeTimestampInHealthCheck() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);
        when(projectionTrackerRepository.findAll()).thenReturn(Collections.emptyList());
        when(eventStoreRepository.findAll()).thenReturn(Collections.emptyList());

        Instant beforeCheck = Instant.now();

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getDetails()).containsKey("timestamp");
        Instant timestamp = (Instant) health.getDetails().get("timestamp");
        assertThat(timestamp).isAfterOrEqualTo(beforeCheck);
        assertThat(timestamp).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("Deve incluir indicador healthy nos detalhes")
    void shouldIncludeHealthyIndicatorInDetails() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);

        ProjectionTracker tracker = createProjectionTracker("TestProjection", 95L, ProjectionStatus.ACTIVE,
                Instant.now(), 100L, 0L);
        when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));

        EventStoreEntry entry = new EventStoreEntry();
        entry.setAggregateId("agg-1");
        when(eventStoreRepository.findAll()).thenReturn(List.of(entry));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getDetails()).containsKey("healthy");
        assertThat(health.getDetails().get("healthy")).isEqualTo(true);
    }

    private ProjectionTracker createProjectionTracker(String name, Long lastProcessedEventId,
            ProjectionStatus status, Instant updatedAt, Long eventsProcessed, Long eventsFailed) {
        ProjectionTracker tracker = new ProjectionTracker();
        tracker.setProjectionName(name);
        tracker.setLastProcessedEventId(lastProcessedEventId);
        tracker.setStatus(status);
        tracker.setUpdatedAt(updatedAt);
        tracker.setCreatedAt(Instant.now().minus(1, ChronoUnit.HOURS));
        tracker.setEventsProcessed(eventsProcessed);
        tracker.setEventsFailed(eventsFailed);
        return tracker;
    }
}
