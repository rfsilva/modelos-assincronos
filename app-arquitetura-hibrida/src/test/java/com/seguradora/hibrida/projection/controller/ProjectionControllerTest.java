package com.seguradora.hibrida.projection.controller;

import com.seguradora.hibrida.projection.ProjectionRegistry;
import com.seguradora.hibrida.projection.consistency.ConsistencyIssue;
import com.seguradora.hibrida.projection.consistency.ConsistencyReport;
import com.seguradora.hibrida.projection.consistency.ProjectionConsistencyChecker;
import com.seguradora.hibrida.projection.rebuild.ProjectionRebuilder;
import com.seguradora.hibrida.projection.rebuild.RebuildResult;
import com.seguradora.hibrida.projection.scheduler.ProjectionMaintenanceScheduler;
import com.seguradora.hibrida.projection.tracking.ProjectionStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProjectionController - Testes Unitários")
class ProjectionControllerTest {

    @Mock
    private ProjectionRegistry projectionRegistry;

    @Mock
    private ProjectionTrackerRepository trackerRepository;

    @Mock
    private ProjectionRebuilder rebuilder;

    @Mock
    private ProjectionConsistencyChecker consistencyChecker;

    @Mock
    private ProjectionMaintenanceScheduler maintenanceScheduler;

    @InjectMocks
    private ProjectionController controller;

    private ConsistencyReport mockConsistencyReport;

    @BeforeEach
    void setUp() {
        mockConsistencyReport = mock(ConsistencyReport.class);
        when(mockConsistencyReport.totalProjections()).thenReturn(10);
        when(mockConsistencyReport.getTotalIssues()).thenReturn(2);
        when(mockConsistencyReport.getCriticalIssuesCount()).thenReturn(0L);
        when(mockConsistencyReport.getHealthScore()).thenReturn(0.95);
        when(mockConsistencyReport.timestamp()).thenReturn(Instant.now());
    }

    @Test
    @DisplayName("Deve retornar health check com status UP")
    void shouldReturnHealthCheckWithStatusUp() {
        // Given
        when(maintenanceScheduler.isSystemHealthy()).thenReturn(true);
        when(maintenanceScheduler.getCurrentHealthScore()).thenReturn(0.95);
        when(projectionRegistry.getStatistics()).thenReturn(Map.of("total", 10));
        when(maintenanceScheduler.getLastMaintenanceRun()).thenReturn(Instant.now());
        when(maintenanceScheduler.getLastConsistencyCheck()).thenReturn(Instant.now());
        when(maintenanceScheduler.getLastConsistencyReport()).thenReturn(mockConsistencyReport);

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("healthScore")).isEqualTo(0.95);

        verify(maintenanceScheduler).isSystemHealthy();
    }

    @Test
    @DisplayName("Deve retornar 503 quando sistema não está saudável")
    void shouldReturn503WhenSystemUnhealthy() {
        // Given
        when(maintenanceScheduler.isSystemHealthy())
                .thenThrow(new RuntimeException("System error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("DOWN");
        assertThat(response.getBody().get("error")).isEqualTo("System error");
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
        when(trackerRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        ResponseEntity<Page<ProjectionTracker>> response = controller.listProjections(Pageable.unpaged());

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);

        verify(trackerRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve retornar detalhes de uma projeção específica")
    void shouldGetProjectionDetails() {
        // Given
        String projectionName = "SinistroProjection";
        ProjectionTracker tracker = mock(ProjectionTracker.class);
        when(trackerRepository.findById(projectionName)).thenReturn(Optional.of(tracker));

        // When
        ResponseEntity<ProjectionTracker> response = controller.getProjection(projectionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(trackerRepository).findById(projectionName);
    }

    @Test
    @DisplayName("Deve retornar 404 quando projeção não é encontrada")
    void shouldReturn404WhenProjectionNotFound() {
        // Given
        when(trackerRepository.findById(anyString())).thenReturn(Optional.empty());

        // When
        ResponseEntity<ProjectionTracker> response = controller.getProjection("NonExistent");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Deve executar rebuild completo de uma projeção")
    void shouldRebuildProjectionCompletely() {
        // Given
        String projectionName = "SinistroProjection";
        CompletableFuture<RebuildResult> future = CompletableFuture.completedFuture(mock(RebuildResult.class));
        when(rebuilder.rebuildProjection(projectionName)).thenReturn(future);

        // When
        ResponseEntity<Map<String, Object>> response = controller.rebuildProjection(projectionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("projectionName")).isEqualTo(projectionName);
        assertThat(response.getBody().get("status")).isEqualTo("STARTED");

        verify(rebuilder).rebuildProjection(projectionName);
    }

    @Test
    @DisplayName("Deve retornar 400 quando erro ao iniciar rebuild")
    void shouldReturn400WhenErrorStartingRebuild() {
        // Given
        String projectionName = "InvalidProjection";
        when(rebuilder.rebuildProjection(projectionName))
                .thenThrow(new RuntimeException("Projection not found"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.rebuildProjection(projectionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("ERROR");
        assertThat(response.getBody().get("message")).isEqualTo("Projection not found");
    }

    @Test
    @DisplayName("Deve executar rebuild incremental")
    void shouldRebuildProjectionIncrementally() {
        // Given
        String projectionName = "SinistroProjection";
        CompletableFuture<RebuildResult> future = CompletableFuture.completedFuture(mock(RebuildResult.class));
        when(rebuilder.rebuildProjectionIncremental(projectionName)).thenReturn(future);

        // When
        ResponseEntity<Map<String, Object>> response = controller.rebuildProjectionIncremental(projectionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("type")).isEqualTo("INCREMENTAL");
        assertThat(response.getBody().get("status")).isEqualTo("STARTED");

        verify(rebuilder).rebuildProjectionIncremental(projectionName);
    }

    @Test
    @DisplayName("Deve pausar uma projeção")
    void shouldPauseProjection() {
        // Given
        String projectionName = "SinistroProjection";
        when(rebuilder.pauseRebuild(projectionName)).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = controller.pauseProjection(projectionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("PAUSED");

        verify(rebuilder).pauseRebuild(projectionName);
    }

    @Test
    @DisplayName("Deve retomar uma projeção pausada")
    void shouldResumeProjection() {
        // Given
        String projectionName = "SinistroProjection";
        CompletableFuture<RebuildResult> future = CompletableFuture.completedFuture(mock(RebuildResult.class));
        when(rebuilder.resumeRebuild(projectionName)).thenReturn(future);

        // When
        ResponseEntity<Map<String, Object>> response = controller.resumeProjection(projectionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("RESUMED");

        verify(rebuilder).resumeRebuild(projectionName);
    }

    @Test
    @DisplayName("Deve verificar consistência de todas as projeções")
    void shouldCheckConsistencyOfAllProjections() {
        // Given
        when(consistencyChecker.checkAllProjections()).thenReturn(mockConsistencyReport);

        // When
        ResponseEntity<ConsistencyReport> response = controller.checkConsistency();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(consistencyChecker).checkAllProjections();
    }

    @Test
    @DisplayName("Deve retornar 500 quando erro ao verificar consistência")
    void shouldReturn500WhenErrorCheckingConsistency() {
        // Given
        when(consistencyChecker.checkAllProjections())
                .thenThrow(new RuntimeException("Consistency check error"));

        // When
        ResponseEntity<ConsistencyReport> response = controller.checkConsistency();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Deve verificar consistência de uma projeção específica")
    void shouldCheckConsistencyOfSpecificProjection() {
        // Given
        String projectionName = "SinistroProjection";
        List<ConsistencyIssue> issues = Arrays.asList(mock(ConsistencyIssue.class));
        when(consistencyChecker.checkProjectionConsistency(projectionName)).thenReturn(issues);

        // When
        ResponseEntity<Map<String, Object>> response = controller.checkProjectionConsistency(projectionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("projectionName")).isEqualTo(projectionName);
        assertThat(response.getBody().get("issueCount")).isEqualTo(1);
        assertThat(response.getBody().get("isHealthy")).isEqualTo(false);

        verify(consistencyChecker).checkProjectionConsistency(projectionName);
    }

    @Test
    @DisplayName("Deve retornar estatísticas do sistema de projeções")
    void shouldGetProjectionStatistics() {
        // Given
        when(projectionRegistry.getStatistics()).thenReturn(Map.of("total", 10));
        Object[] projectionStats = {10L, 9L, 1L, 0L, 0L};
        when(trackerRepository.getProjectionStatistics()).thenReturn(projectionStats);
        when(maintenanceScheduler.getLastConsistencyReport()).thenReturn(mockConsistencyReport);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("registry", "trackers", "consistency");

        verify(projectionRegistry).getStatistics();
        verify(trackerRepository).getProjectionStatistics();
    }

    @Test
    @DisplayName("Deve retornar dashboard com informações resumidas")
    void shouldGetDashboard() {
        // Given
        when(maintenanceScheduler.isSystemHealthy()).thenReturn(true);
        when(maintenanceScheduler.getCurrentHealthScore()).thenReturn(0.95);
        when(trackerRepository.count()).thenReturn(10L);
        when(trackerRepository.countByStatus(any(ProjectionStatus.class))).thenReturn(5L);
        when(maintenanceScheduler.getLastConsistencyReport()).thenReturn(mockConsistencyReport);
        when(maintenanceScheduler.getLastMaintenanceRun()).thenReturn(Instant.now());
        when(maintenanceScheduler.getLastConsistencyCheck()).thenReturn(Instant.now());
        when(mockConsistencyReport.getHealthyProjectionsCount()).thenReturn(8);
        when(mockConsistencyReport.getHighPriorityIssuesCount()).thenReturn(1L);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getDashboard();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("systemHealthy")).isEqualTo(true);
        assertThat(response.getBody().get("healthScore")).isEqualTo(0.95);
        assertThat(response.getBody().get("totalProjections")).isEqualTo(10L);

        verify(maintenanceScheduler).isSystemHealthy();
        verify(trackerRepository).count();
    }

    @Test
    @DisplayName("Deve retornar 500 quando erro ao obter dashboard")
    void shouldReturn500WhenErrorGettingDashboard() {
        // Given
        when(maintenanceScheduler.isSystemHealthy())
                .thenThrow(new RuntimeException("Dashboard error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.getDashboard();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Dashboard error");
    }

    @Test
    @DisplayName("Deve incluir relatório de consistência no health check")
    void shouldIncludeConsistencyReportInHealthCheck() {
        // Given
        when(maintenanceScheduler.isSystemHealthy()).thenReturn(true);
        when(maintenanceScheduler.getCurrentHealthScore()).thenReturn(0.95);
        when(projectionRegistry.getStatistics()).thenReturn(Map.of());
        when(maintenanceScheduler.getLastMaintenanceRun()).thenReturn(Instant.now());
        when(maintenanceScheduler.getLastConsistencyCheck()).thenReturn(Instant.now());
        when(maintenanceScheduler.getLastConsistencyReport()).thenReturn(mockConsistencyReport);

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("consistency");

        @SuppressWarnings("unchecked")
        Map<String, Object> consistency = (Map<String, Object>) response.getBody().get("consistency");
        assertThat(consistency.get("totalProjections")).isEqualTo(10);
        assertThat(consistency.get("totalIssues")).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve indicar projeção saudável quando não há issues")
    void shouldIndicateHealthyProjectionWhenNoIssues() {
        // Given
        String projectionName = "HealthyProjection";
        when(consistencyChecker.checkProjectionConsistency(projectionName))
                .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<Map<String, Object>> response = controller.checkProjectionConsistency(projectionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("isHealthy")).isEqualTo(true);
        assertThat(response.getBody().get("issueCount")).isEqualTo(0);
    }
}
