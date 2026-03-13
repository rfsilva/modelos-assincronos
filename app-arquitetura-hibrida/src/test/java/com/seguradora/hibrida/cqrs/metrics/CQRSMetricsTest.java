package com.seguradora.hibrida.cqrs.metrics;

import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionStatus;
import com.seguradora.hibrida.projection.tracking.ProjectionTracker;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes do CQRSMetrics")
class CQRSMetricsTest {

    @Mock
    private EventStoreRepository eventStoreRepository;

    @Mock
    private ProjectionTrackerRepository projectionTrackerRepository;

    private MeterRegistry meterRegistry;
    private CQRSMetrics cqrsMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        cqrsMetrics = new CQRSMetrics(eventStoreRepository, projectionTrackerRepository);
    }

    @Test
    @DisplayName("Deve registrar métricas no registry")
    void shouldBindMetricsToRegistry() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);
        when(projectionTrackerRepository.count()).thenReturn(2L);
        when(projectionTrackerRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        cqrsMetrics.bindTo(meterRegistry);

        // Assert
        assertThat(meterRegistry.getMeters()).isNotEmpty();
        assertThat(meterRegistry.find("cqrs.command.side.events").gauge()).isNotNull();
        assertThat(meterRegistry.find("cqrs.query.side.events").gauge()).isNotNull();
        assertThat(meterRegistry.find("cqrs.lag.events").gauge()).isNotNull();
        assertThat(meterRegistry.find("cqrs.projections.total").gauge()).isNotNull();
        assertThat(meterRegistry.find("cqrs.projections.active").gauge()).isNotNull();
        assertThat(meterRegistry.find("cqrs.projections.error").gauge()).isNotNull();
        assertThat(meterRegistry.find("cqrs.health.score").gauge()).isNotNull();
    }

    @Test
    @DisplayName("Deve atualizar métricas do Command Side")
    void shouldUpdateCommandSideMetrics() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(500L);
        when(projectionTrackerRepository.findAll()).thenReturn(Collections.emptyList());

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();

        // Assert
        assertThat(cqrsMetrics.getCommandSideEvents()).isEqualTo(500.0);
    }

    @Test
    @DisplayName("Deve atualizar métricas do Query Side")
    void shouldUpdateQuerySideMetrics() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(1000L);

        ProjectionTracker tracker1 = createProjectionTracker("Projection1", 950L);
        ProjectionTracker tracker2 = createProjectionTracker("Projection2", 900L);
        when(projectionTrackerRepository.findAll()).thenReturn(Arrays.asList(tracker1, tracker2));

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();

        // Assert - Query Side deve ser a posição mínima (mais atrasada)
        assertThat(cqrsMetrics.getQuerySideEvents()).isEqualTo(900.0);
    }

    @Test
    @DisplayName("Deve calcular lag entre Command e Query Side")
    void shouldCalculateLagBetweenSides() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(1000L);

        ProjectionTracker tracker = createProjectionTracker("TestProjection", 750L);
        when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();

        // Assert
        assertThat(cqrsMetrics.getOverallLag()).isEqualTo(250.0);
    }

    @Test
    @DisplayName("Deve estimar lag em segundos")
    void shouldEstimateLagInSeconds() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(1000L);

        ProjectionTracker tracker = createProjectionTracker("TestProjection", 900L);
        when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();

        // Assert - Baseado em throughput de 10 eventos/segundo
        assertThat(cqrsMetrics.getEstimatedLagSeconds()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Deve contar total de projeções")
    void shouldCountTotalProjections() {
        // Arrange
        when(projectionTrackerRepository.count()).thenReturn(5L);
        when(projectionTrackerRepository.findAll()).thenReturn(Collections.emptyList());

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        double total = cqrsMetrics.getTotalProjections();

        // Assert
        assertThat(total).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Deve contar projeções ativas")
    void shouldCountActiveProjections() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);

        ProjectionTracker active1 = createProjectionTracker("Active1", 95L, ProjectionStatus.ACTIVE);
        ProjectionTracker active2 = createProjectionTracker("Active2", 90L, ProjectionStatus.ACTIVE);
        ProjectionTracker paused = createProjectionTracker("Paused", 80L, ProjectionStatus.PAUSED);
        when(projectionTrackerRepository.findAll()).thenReturn(Arrays.asList(active1, active2, paused));

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();

        // Assert
        assertThat(cqrsMetrics.getActiveProjections()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Deve contar projeções com erro")
    void shouldCountErrorProjections() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);

        ProjectionTracker active = createProjectionTracker("Active", 95L, ProjectionStatus.ACTIVE);
        ProjectionTracker error1 = createProjectionTracker("Error1", 50L, ProjectionStatus.ERROR);
        ProjectionTracker error2 = createProjectionTracker("Error2", 40L, ProjectionStatus.ERROR);
        when(projectionTrackerRepository.findAll()).thenReturn(Arrays.asList(active, error1, error2));

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();

        // Assert
        assertThat(cqrsMetrics.getErrorProjections()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Deve contar projeções obsoletas")
    void shouldCountStaleProjections() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);

        Instant now = Instant.now();
        Instant staleTime = now.minus(60, ChronoUnit.MINUTES);

        ProjectionTracker fresh = createProjectionTracker("Fresh", 95L, ProjectionStatus.ACTIVE);
        fresh.setUpdatedAt(now);

        ProjectionTracker stale = createProjectionTracker("Stale", 50L, ProjectionStatus.ACTIVE);
        stale.setUpdatedAt(staleTime);

        when(projectionTrackerRepository.findAll()).thenReturn(Arrays.asList(fresh, stale));

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();

        // Assert
        assertThat(cqrsMetrics.getStaleProjections()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Deve calcular taxa de erro das projeções")
    void shouldCalculateProjectionErrorRate() {
        // Arrange
        ProjectionTracker tracker1 = createProjectionTracker("Projection1", 100L);
        tracker1.setEventsProcessed(100L);
        tracker1.setEventsFailed(5L); // 5% erro

        ProjectionTracker tracker2 = createProjectionTracker("Projection2", 200L);
        tracker2.setEventsProcessed(200L);
        tracker2.setEventsFailed(10L); // 5% erro

        lenient().when(projectionTrackerRepository.findAll()).thenReturn(Arrays.asList(tracker1, tracker2));
        lenient().when(projectionTrackerRepository.count()).thenReturn(2L);

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        double errorRate = cqrsMetrics.getProjectionsErrorRate();

        // Assert
        assertThat(errorRate).isEqualTo(0.05); // 15/300 = 0.05
    }

    @Test
    @DisplayName("Deve retornar zero quando não há eventos processados")
    void shouldReturnZeroErrorRateWhenNoEventsProcessed() {
        // Arrange
        ProjectionTracker tracker = createProjectionTracker("Projection", 0L);
        tracker.setEventsProcessed(0L);
        tracker.setEventsFailed(0L);

        lenient().when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));
        lenient().when(projectionTrackerRepository.count()).thenReturn(1L);

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        double errorRate = cqrsMetrics.getProjectionsErrorRate();

        // Assert
        assertThat(errorRate).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Deve calcular throughput das projeções")
    void shouldCalculateProjectionsThroughput() {
        // Arrange
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

        ProjectionTracker tracker = createProjectionTracker("Projection", 3600L);
        tracker.setEventsProcessed(3600L);
        tracker.setCreatedAt(oneHourAgo);

        lenient().when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));
        lenient().when(projectionTrackerRepository.count()).thenReturn(1L);

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        double throughput = cqrsMetrics.getProjectionsThroughput();

        // Assert - Aproximadamente 1 evento/segundo (3600 eventos em 3600 segundos)
        assertThat(throughput).isCloseTo(1.0, within(0.1));
    }

    @Test
    @DisplayName("Deve calcular health score perfeito quando sistema está saudável")
    void shouldCalculatePerfectHealthScoreWhenHealthy() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);

        ProjectionTracker tracker = createProjectionTracker("Projection", 95L, ProjectionStatus.ACTIVE);
        tracker.setEventsProcessed(100L);
        tracker.setEventsFailed(0L);
        tracker.setUpdatedAt(Instant.now());

        when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));
        when(projectionTrackerRepository.count()).thenReturn(1L);

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();
        double score = cqrsMetrics.getHealthScore();

        // Assert
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Deve penalizar score por lag alto")
    void shouldPenalizeScoreForHighLag() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(3000L);

        ProjectionTracker tracker = createProjectionTracker("Projection", 1000L, ProjectionStatus.ACTIVE);
        tracker.setEventsProcessed(1000L);
        tracker.setEventsFailed(0L);

        when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));
        when(projectionTrackerRepository.count()).thenReturn(1L);

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();
        double score = cqrsMetrics.getHealthScore();

        // Assert - Lag > 1000 reduz 0.2
        assertThat(score).isEqualTo(0.8);
    }

    @Test
    @DisplayName("Deve penalizar score por lag crítico")
    void shouldPenalizeScoreForCriticalLag() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(10000L);

        ProjectionTracker tracker = createProjectionTracker("Projection", 1000L, ProjectionStatus.ACTIVE);
        tracker.setEventsProcessed(1000L);
        tracker.setEventsFailed(0L);

        when(projectionTrackerRepository.findAll()).thenReturn(List.of(tracker));
        when(projectionTrackerRepository.count()).thenReturn(1L);

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();
        double score = cqrsMetrics.getHealthScore();

        // Assert - Lag > 5000 reduz 0.5
        assertThat(score).isEqualTo(0.5);
    }

    @Test
    @DisplayName("Deve penalizar score por projeções com erro")
    void shouldPenalizeScoreForErrorProjections() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);

        ProjectionTracker active = createProjectionTracker("Active", 95L, ProjectionStatus.ACTIVE);
        active.setEventsProcessed(100L);
        active.setEventsFailed(0L);

        ProjectionTracker error = createProjectionTracker("Error", 50L, ProjectionStatus.ERROR);
        error.setEventsProcessed(50L);
        error.setEventsFailed(5L);

        when(projectionTrackerRepository.findAll()).thenReturn(Arrays.asList(active, error));
        when(projectionTrackerRepository.count()).thenReturn(2L);

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();
        double score = cqrsMetrics.getHealthScore();

        // Assert - 1 de 2 com erro = 50% = redução de 0.15
        assertThat(score).isCloseTo(0.85, within(0.01));
    }

    @Test
    @DisplayName("Deve garantir que score nunca seja negativo")
    void shouldEnsureScoreNeverNegative() {
        // Arrange - Cenário catastrófico
        when(eventStoreRepository.count()).thenReturn(20000L);

        ProjectionTracker error1 = createProjectionTracker("Error1", 1000L, ProjectionStatus.ERROR);
        error1.setEventsProcessed(100L);
        error1.setEventsFailed(50L); // 50% erro

        ProjectionTracker error2 = createProjectionTracker("Error2", 1000L, ProjectionStatus.ERROR);
        error2.setEventsProcessed(100L);
        error2.setEventsFailed(50L);

        when(projectionTrackerRepository.findAll()).thenReturn(Arrays.asList(error1, error2));
        when(projectionTrackerRepository.count()).thenReturn(2L);

        cqrsMetrics.bindTo(meterRegistry);

        // Act
        cqrsMetrics.updateMetrics();
        double score = cqrsMetrics.getHealthScore();

        // Assert
        assertThat(score).isGreaterThanOrEqualTo(0.0);
        assertThat(score).isLessThanOrEqualTo(1.0);
    }

    @Test
    @DisplayName("Deve permitir forçar atualização das métricas")
    void shouldAllowForceUpdate() {
        // Arrange
        when(eventStoreRepository.count()).thenReturn(100L);
        when(projectionTrackerRepository.findAll()).thenReturn(Collections.emptyList());

        cqrsMetrics.bindTo(meterRegistry);
        cqrsMetrics.updateMetrics();

        double initialValue = cqrsMetrics.getCommandSideEvents();
        assertThat(initialValue).isEqualTo(100.0);

        // Simula mudança no repositório
        when(eventStoreRepository.count()).thenReturn(200L);

        // Act
        cqrsMetrics.forceUpdate();

        // Assert
        assertThat(cqrsMetrics.getCommandSideEvents()).isEqualTo(200.0);
        assertThat(cqrsMetrics.getCommandSideEvents()).isNotEqualTo(initialValue);
    }

    @Test
    @DisplayName("Deve tratar erro ao atualizar métricas")
    void shouldHandleErrorWhenUpdatingMetrics() {
        // Arrange
        when(eventStoreRepository.count()).thenThrow(new RuntimeException("Database error"));

        cqrsMetrics.bindTo(meterRegistry);

        // Act & Assert - Não deve lançar exceção
        cqrsMetrics.updateMetrics();

        // Métricas devem permanecer em valores padrão
        assertThat(cqrsMetrics.getCommandSideEvents()).isEqualTo(0.0);
    }

    private ProjectionTracker createProjectionTracker(String name, Long lastProcessedEventId) {
        return createProjectionTracker(name, lastProcessedEventId, ProjectionStatus.ACTIVE);
    }

    private ProjectionTracker createProjectionTracker(String name, Long lastProcessedEventId,
            ProjectionStatus status) {
        ProjectionTracker tracker = new ProjectionTracker();
        tracker.setProjectionName(name);
        tracker.setLastProcessedEventId(lastProcessedEventId);
        tracker.setStatus(status);
        tracker.setUpdatedAt(Instant.now());
        tracker.setCreatedAt(Instant.now().minus(1, ChronoUnit.HOURS));
        tracker.setEventsProcessed(lastProcessedEventId);
        tracker.setEventsFailed(0L);
        return tracker;
    }
}
