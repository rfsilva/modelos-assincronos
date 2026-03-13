package com.seguradora.hibrida.eventstore.replay.controller;

import com.seguradora.hibrida.eventstore.replay.*;
import com.seguradora.hibrida.eventstore.replay.config.ReplayHealthIndicator;
import com.seguradora.hibrida.eventstore.replay.config.ReplayMetrics;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReplayController - Testes Unitários")
class ReplayControllerTest {

    @Mock
    private EventReplayer eventReplayer;

    @Mock
    private ReplayHealthIndicator healthIndicator;

    @Mock
    private ReplayMetrics metrics;

    @InjectMocks
    private ReplayController controller;

    private ReplayConfiguration mockConfig;
    private UUID replayId;

    @BeforeEach
    void setUp() {
        replayId = UUID.randomUUID();
        mockConfig = ReplayConfiguration.builder()
                .replayId(replayId)
                .name("Test Replay")
                .build();
    }

    @Test
    @DisplayName("Deve iniciar replay por período com sucesso")
    void shouldStartReplayByPeriodSuccessfully() {
        // Given
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 31, 23, 59);

        CompletableFuture<ReplayResult> future = CompletableFuture.completedFuture(mock(ReplayResult.class));
        when(eventReplayer.replayByPeriod(any(ReplayConfiguration.class))).thenReturn(future);

        // When
        ResponseEntity<Map<String, Object>> response = controller.replayByPeriod(
                from, to, "Test Replay", false, 100
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("STARTED");
        assertThat(response.getBody().get("replayId")).isNotNull();

        verify(eventReplayer).replayByPeriod(any(ReplayConfiguration.class));
    }

    @Test
    @DisplayName("Deve retornar 400 quando ocorre erro ao iniciar replay por período")
    void shouldReturn400WhenErrorStartingReplayByPeriod() {
        // Given
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(eventReplayer.replayByPeriod(any(ReplayConfiguration.class)))
                .thenThrow(new RuntimeException("Invalid configuration"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.replayByPeriod(
                from, to, null, false, 0
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Erro ao iniciar replay");

        verify(eventReplayer).replayByPeriod(any(ReplayConfiguration.class));
    }

    @Test
    @DisplayName("Deve iniciar replay por tipo de evento")
    void shouldStartReplayByEventType() {
        // Given
        String eventType = "SinistroCriado";
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 31, 23, 59);

        CompletableFuture<ReplayResult> future = CompletableFuture.completedFuture(mock(ReplayResult.class));
        when(eventReplayer.replayByEventType(anyString(), any(), any(), any(ReplayConfiguration.class)))
                .thenReturn(future);

        // When
        ResponseEntity<Map<String, Object>> response = controller.replayByEventType(
                eventType, from, to, null, false
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("eventType")).isEqualTo(eventType);
        assertThat(response.getBody().get("status")).isEqualTo("STARTED");

        verify(eventReplayer).replayByEventType(anyString(), any(), any(), any(ReplayConfiguration.class));
    }

    @Test
    @DisplayName("Deve iniciar replay por aggregate")
    void shouldStartReplayByAggregate() {
        // Given
        String aggregateId = "AGG-123";
        Long fromVersion = 5L;

        CompletableFuture<ReplayResult> future = CompletableFuture.completedFuture(mock(ReplayResult.class));
        when(eventReplayer.replayByAggregate(anyString(), any(), any(ReplayConfiguration.class)))
                .thenReturn(future);

        // When
        ResponseEntity<Map<String, Object>> response = controller.replayByAggregate(
                aggregateId, fromVersion, null, false
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("aggregateId")).isEqualTo(aggregateId);
        assertThat(response.getBody().get("fromVersion")).isEqualTo(fromVersion);
        assertThat(response.getBody().get("status")).isEqualTo("STARTED");

        verify(eventReplayer).replayByAggregate(anyString(), any(), any(ReplayConfiguration.class));
    }

    @Test
    @DisplayName("Deve pausar replay com sucesso")
    void shouldPauseReplaySuccessfully() {
        // Given
        when(eventReplayer.pauseReplay(replayId)).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = controller.pauseReplay(replayId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("replayId")).isEqualTo(replayId);
        assertThat(response.getBody().get("action")).isEqualTo("pause");
        assertThat(response.getBody().get("success")).isEqualTo(true);

        verify(eventReplayer).pauseReplay(replayId);
    }

    @Test
    @DisplayName("Deve retornar 400 quando não é possível pausar replay")
    void shouldReturn400WhenCannotPauseReplay() {
        // Given
        when(eventReplayer.pauseReplay(replayId)).thenReturn(false);

        // When
        ResponseEntity<Map<String, Object>> response = controller.pauseReplay(replayId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message")).asString()
                .contains("não pode ser pausado");
    }

    @Test
    @DisplayName("Deve retomar replay com sucesso")
    void shouldResumeReplaySuccessfully() {
        // Given
        when(eventReplayer.resumeReplay(replayId)).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = controller.resumeReplay(replayId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("replayId")).isEqualTo(replayId);
        assertThat(response.getBody().get("action")).isEqualTo("resume");
        assertThat(response.getBody().get("success")).isEqualTo(true);

        verify(eventReplayer).resumeReplay(replayId);
    }

    @Test
    @DisplayName("Deve cancelar replay com sucesso")
    void shouldCancelReplaySuccessfully() {
        // Given
        when(eventReplayer.cancelReplay(replayId)).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = controller.cancelReplay(replayId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("replayId")).isEqualTo(replayId);
        assertThat(response.getBody().get("action")).isEqualTo("cancel");
        assertThat(response.getBody().get("success")).isEqualTo(true);

        verify(eventReplayer).cancelReplay(replayId);
    }

    @Test
    @DisplayName("Deve obter progresso do replay")
    void shouldGetReplayProgress() {
        // Given
        ReplayProgress progress = mock(ReplayProgress.class);
        when(eventReplayer.getProgress(replayId)).thenReturn(progress);

        // When
        ResponseEntity<ReplayProgress> response = controller.getProgress(replayId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(eventReplayer).getProgress(replayId);
    }

    @Test
    @DisplayName("Deve retornar 404 quando progresso não é encontrado")
    void shouldReturn404WhenProgressNotFound() {
        // Given
        when(eventReplayer.getProgress(replayId)).thenReturn(null);

        // When
        ResponseEntity<ReplayProgress> response = controller.getProgress(replayId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Deve listar replays ativos")
    void shouldListActiveReplays() {
        // Given
        List<ReplayProgress> activeReplays = Arrays.asList(
                mock(ReplayProgress.class),
                mock(ReplayProgress.class)
        );
        when(eventReplayer.getActiveReplays()).thenReturn(activeReplays);

        // When
        ResponseEntity<List<ReplayProgress>> response = controller.getActiveReplays();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);

        verify(eventReplayer).getActiveReplays();
    }

    @Test
    @DisplayName("Deve obter histórico de replays")
    void shouldGetReplayHistory() {
        // Given
        List<ReplayResult> history = Arrays.asList(
                mock(ReplayResult.class),
                mock(ReplayResult.class)
        );
        when(eventReplayer.getReplayHistory(50)).thenReturn(history);

        // When
        ResponseEntity<List<ReplayResult>> response = controller.getHistory(50);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);

        verify(eventReplayer).getReplayHistory(50);
    }

    @Test
    @DisplayName("Deve obter estatísticas do sistema de replay")
    void shouldGetReplayStatistics() {
        // Given
        ReplayStatistics statistics = mock(ReplayStatistics.class);
        when(eventReplayer.getStatistics()).thenReturn(statistics);

        // When
        ResponseEntity<ReplayStatistics> response = controller.getStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(eventReplayer).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar health check do sistema de replay")
    void shouldReturnHealthCheck() {
        // Given
        Health health = Health.up()
                .withDetail("replay", "operational")
                .build();
        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Health> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(Status.UP);

        verify(healthIndicator).health();
    }

    @Test
    @DisplayName("Deve obter métricas em tempo real")
    void shouldGetRealtimeMetrics() {
        // Given
        when(metrics.getActiveReplaysCount()).thenReturn(3.0);
        when(metrics.getSuccessRate()).thenReturn(0.95);
        when(metrics.getErrorRate()).thenReturn(0.05);
        when(metrics.getAverageThroughput()).thenReturn(500.0);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getMetrics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("active_replays")).isEqualTo(3.0);
        assertThat(response.getBody().get("success_rate")).isEqualTo(0.95);
        assertThat(response.getBody().get("error_rate")).isEqualTo(0.05);
        assertThat(response.getBody().get("average_throughput")).isEqualTo(500.0);

        verify(metrics).getActiveReplaysCount();
        verify(metrics).getSuccessRate();
        verify(metrics).getErrorRate();
        verify(metrics).getAverageThroughput();
    }

    @Test
    @DisplayName("Deve executar simulação de replay")
    void shouldExecuteReplaySimulation() {
        // Given
        ReplayConfiguration baseConfig = ReplayConfiguration.builder()
                .name("Base Config")
                .build();

        CompletableFuture<ReplayResult> future = CompletableFuture.completedFuture(mock(ReplayResult.class));
        when(eventReplayer.simulateReplay(any(ReplayConfiguration.class))).thenReturn(future);

        // When
        ResponseEntity<Map<String, Object>> response = controller.simulateReplay(baseConfig);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("mode")).isEqualTo("SIMULATION");
        assertThat(response.getBody().get("status")).isEqualTo("STARTED");

        verify(eventReplayer).simulateReplay(any(ReplayConfiguration.class));
    }

    @Test
    @DisplayName("Deve retornar 400 quando erro ao simular replay")
    void shouldReturn400WhenErrorSimulating() {
        // Given
        ReplayConfiguration baseConfig = ReplayConfiguration.builder().build();
        when(eventReplayer.simulateReplay(any(ReplayConfiguration.class)))
                .thenThrow(new RuntimeException("Simulation error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.simulateReplay(baseConfig);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Simulation error");
    }

    @Test
    @DisplayName("Deve incluir configuração na resposta de replay por período")
    void shouldIncludeConfigurationInPeriodReplayResponse() {
        // Given
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 31, 23, 59);

        CompletableFuture<ReplayResult> future = CompletableFuture.completedFuture(mock(ReplayResult.class));
        when(eventReplayer.replayByPeriod(any(ReplayConfiguration.class))).thenReturn(future);

        // When
        ResponseEntity<Map<String, Object>> response = controller.replayByPeriod(
                from, to, "Test", false, 100
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("configuration")).isNotNull();
    }
}
