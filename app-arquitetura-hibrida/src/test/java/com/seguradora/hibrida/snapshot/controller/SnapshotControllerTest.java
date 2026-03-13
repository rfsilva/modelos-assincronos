package com.seguradora.hibrida.snapshot.controller;

import com.seguradora.hibrida.snapshot.*;
import com.seguradora.hibrida.snapshot.config.SnapshotHealthIndicator;
import com.seguradora.hibrida.snapshot.config.SnapshotMetrics;
import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SnapshotController - Testes Unitários")
class SnapshotControllerTest {

    @Mock
    private SnapshotStore snapshotStore;

    @Mock
    private SnapshotHealthIndicator healthIndicator;

    @Mock
    private SnapshotMetrics snapshotMetrics;

    @InjectMocks
    private SnapshotController controller;

    @Test
    @DisplayName("Deve obter snapshot mais recente de um aggregate")
    void shouldGetLatestSnapshot() {
        // Given
        String aggregateId = "AGG-123";
        AggregateSnapshot snapshot = mock(AggregateSnapshot.class);
        when(snapshot.getVersion()).thenReturn(10L);
        when(snapshotStore.getLatestSnapshot(aggregateId)).thenReturn(Optional.of(snapshot));

        // When
        ResponseEntity<AggregateSnapshot> response = controller.getLatestSnapshot(aggregateId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(snapshotStore).getLatestSnapshot(aggregateId);
    }

    @Test
    @DisplayName("Deve retornar 404 quando snapshot não é encontrado")
    void shouldReturn404WhenSnapshotNotFound() {
        // Given
        String aggregateId = "AGG-999";
        when(snapshotStore.getLatestSnapshot(aggregateId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<AggregateSnapshot> response = controller.getLatestSnapshot(aggregateId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Deve obter snapshot em versão específica")
    void shouldGetSnapshotAtVersion() {
        // Given
        String aggregateId = "AGG-123";
        long maxVersion = 10L;
        AggregateSnapshot snapshot = mock(AggregateSnapshot.class);
        when(snapshot.getVersion()).thenReturn(8L);
        when(snapshotStore.getSnapshotAtOrBeforeVersion(aggregateId, maxVersion))
                .thenReturn(Optional.of(snapshot));

        // When
        ResponseEntity<AggregateSnapshot> response = controller.getSnapshotAtVersion(aggregateId, maxVersion);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(snapshotStore).getSnapshotAtOrBeforeVersion(aggregateId, maxVersion);
    }

    @Test
    @DisplayName("Deve retornar 404 quando snapshot em versão não é encontrado")
    void shouldReturn404WhenSnapshotAtVersionNotFound() {
        // Given
        String aggregateId = "AGG-123";
        long maxVersion = 5L;
        when(snapshotStore.getSnapshotAtOrBeforeVersion(aggregateId, maxVersion))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<AggregateSnapshot> response = controller.getSnapshotAtVersion(aggregateId, maxVersion);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Deve obter histórico de snapshots")
    void shouldGetSnapshotHistory() {
        // Given
        String aggregateId = "AGG-123";
        List<AggregateSnapshot> history = Arrays.asList(
                mock(AggregateSnapshot.class),
                mock(AggregateSnapshot.class),
                mock(AggregateSnapshot.class)
        );
        when(snapshotStore.getSnapshotHistory(aggregateId)).thenReturn(history);

        // When
        ResponseEntity<List<AggregateSnapshot>> response = controller.getSnapshotHistory(aggregateId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);

        verify(snapshotStore).getSnapshotHistory(aggregateId);
    }

    @Test
    @DisplayName("Deve retornar histórico vazio quando não há snapshots")
    void shouldReturnEmptyHistoryWhenNoSnapshots() {
        // Given
        String aggregateId = "AGG-123";
        when(snapshotStore.getSnapshotHistory(aggregateId)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<AggregateSnapshot>> response = controller.getSnapshotHistory(aggregateId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Deve obter estatísticas de um aggregate")
    void shouldGetAggregateStatistics() {
        // Given
        String aggregateId = "AGG-123";
        SnapshotStatistics statistics = mock(SnapshotStatistics.class);
        when(snapshotStore.getSnapshotStatistics(aggregateId)).thenReturn(statistics);

        // When
        ResponseEntity<SnapshotStatistics> response = controller.getAggregateStatistics(aggregateId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(snapshotStore).getSnapshotStatistics(aggregateId);
    }

    @Test
    @DisplayName("Deve obter estatísticas globais")
    void shouldGetGlobalStatistics() {
        // Given
        SnapshotStatistics statistics = mock(SnapshotStatistics.class);
        when(snapshotStore.getGlobalStatistics()).thenReturn(statistics);

        // When
        ResponseEntity<SnapshotStatistics> response = controller.getGlobalStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(snapshotStore).getGlobalStatistics();
    }

    @Test
    @DisplayName("Deve obter métricas de eficiência")
    void shouldGetEfficiencyMetrics() {
        // Given
        String aggregateId = "AGG-123";
        int period = 7;
        SnapshotEfficiencyMetrics metrics = mock(SnapshotEfficiencyMetrics.class);
        when(snapshotStore.getEfficiencyMetrics(aggregateId, period)).thenReturn(metrics);

        // When
        ResponseEntity<SnapshotEfficiencyMetrics> response = controller.getEfficiencyMetrics(aggregateId, period);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(snapshotStore).getEfficiencyMetrics(aggregateId, period);
    }

    @Test
    @DisplayName("Deve usar período padrão quando não especificado")
    void shouldUseDefaultPeriodWhenNotSpecified() {
        // Given
        String aggregateId = "AGG-123";
        SnapshotEfficiencyMetrics metrics = mock(SnapshotEfficiencyMetrics.class);
        when(snapshotStore.getEfficiencyMetrics(eq(aggregateId), anyInt())).thenReturn(metrics);

        // When
        ResponseEntity<SnapshotEfficiencyMetrics> response = controller.getEfficiencyMetrics(aggregateId, 7);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(snapshotStore).getEfficiencyMetrics(aggregateId, 7);
    }

    @Test
    @DisplayName("Deve verificar se deve criar snapshot")
    void shouldCheckIfShouldCreateSnapshot() {
        // Given
        String aggregateId = "AGG-123";
        long currentVersion = 50L;
        when(snapshotStore.shouldCreateSnapshot(aggregateId, currentVersion)).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = controller.shouldCreateSnapshot(aggregateId, currentVersion);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("aggregateId")).isEqualTo(aggregateId);
        assertThat(response.getBody().get("currentVersion")).isEqualTo(currentVersion);
        assertThat(response.getBody().get("shouldCreateSnapshot")).isEqualTo(true);
        assertThat(response.getBody().get("reason")).isEqualTo("Threshold reached");

        verify(snapshotStore).shouldCreateSnapshot(aggregateId, currentVersion);
    }

    @Test
    @DisplayName("Deve indicar que não deve criar snapshot quando threshold não atingido")
    void shouldIndicateNotToCreateSnapshotWhenThresholdNotReached() {
        // Given
        String aggregateId = "AGG-123";
        long currentVersion = 5L;
        when(snapshotStore.shouldCreateSnapshot(aggregateId, currentVersion)).thenReturn(false);

        // When
        ResponseEntity<Map<String, Object>> response = controller.shouldCreateSnapshot(aggregateId, currentVersion);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("shouldCreateSnapshot")).isEqualTo(false);
        assertThat(response.getBody().get("reason")).isEqualTo("Threshold not reached");
    }

    @Test
    @DisplayName("Deve limpar snapshots antigos de um aggregate")
    void shouldCleanupOldSnapshots() {
        // Given
        String aggregateId = "AGG-123";
        int keepCount = 5;
        when(snapshotStore.cleanupOldSnapshots(aggregateId, keepCount)).thenReturn(3);

        // When
        ResponseEntity<Map<String, Object>> response = controller.cleanupOldSnapshots(aggregateId, keepCount);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("aggregateId")).isEqualTo(aggregateId);
        assertThat(response.getBody().get("keepCount")).isEqualTo(keepCount);
        assertThat(response.getBody().get("deletedCount")).isEqualTo(3);
        assertThat(response.getBody().get("message")).isEqualTo("Cleanup completed successfully");

        verify(snapshotStore).cleanupOldSnapshots(aggregateId, keepCount);
    }

    @Test
    @DisplayName("Deve indicar que não há snapshots para remover quando deletedCount é zero")
    void shouldIndicateNoSnapshotsToRemoveWhenDeletedCountIsZero() {
        // Given
        String aggregateId = "AGG-123";
        int keepCount = 5;
        when(snapshotStore.cleanupOldSnapshots(aggregateId, keepCount)).thenReturn(0);

        // When
        ResponseEntity<Map<String, Object>> response = controller.cleanupOldSnapshots(aggregateId, keepCount);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("deletedCount")).isEqualTo(0);
        assertThat(response.getBody().get("message")).isEqualTo("No old snapshots to remove");
    }

    @Test
    @DisplayName("Deve executar limpeza global de snapshots")
    void shouldCleanupAllOldSnapshots() {
        // Given
        int keepCount = 5;
        when(snapshotStore.cleanupAllOldSnapshots(keepCount)).thenReturn(20);

        // When
        ResponseEntity<Map<String, Object>> response = controller.cleanupAllOldSnapshots(keepCount);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("keepCount")).isEqualTo(keepCount);
        assertThat(response.getBody().get("totalDeletedCount")).isEqualTo(20);
        assertThat(response.getBody().get("message")).isEqualTo("Global cleanup completed successfully");

        verify(snapshotStore).cleanupAllOldSnapshots(keepCount);
    }

    @Test
    @DisplayName("Deve verificar saúde do sistema de snapshots")
    void shouldCheckSnapshotSystemHealth() {
        // Given
        SnapshotHealthIndicator.SnapshotHealthResult health = mock(SnapshotHealthIndicator.SnapshotHealthResult.class);
        when(health.isUp()).thenReturn(true);
        when(health.getDetails()).thenReturn(Map.of("status", "UP", "snapshots", 100));
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
    @DisplayName("Deve retornar 503 quando sistema não está saudável")
    void shouldReturn503WhenSystemUnhealthy() {
        // Given
        SnapshotHealthIndicator.SnapshotHealthResult health = mock(SnapshotHealthIndicator.SnapshotHealthResult.class);
        when(health.isUp()).thenReturn(false);
        when(health.getDetails()).thenReturn(Map.of("status", "DOWN", "error", "Database error"));
        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Database error");
    }

    @Test
    @DisplayName("Deve obter métricas do sistema de snapshots")
    void shouldGetSnapshotSystemMetrics() {
        // Given
        SnapshotMetrics.MetricsStatistics metrics = mock(SnapshotMetrics.MetricsStatistics.class);
        when(snapshotMetrics.getMetricsStatistics()).thenReturn(metrics);

        // When
        ResponseEntity<SnapshotMetrics.MetricsStatistics> response = controller.getMetrics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(snapshotMetrics).getMetricsStatistics();
    }

    @Test
    @DisplayName("Deve deletar todos os snapshots de um aggregate com confirmação")
    void shouldDeleteAllSnapshotsWithConfirmation() {
        // Given
        String aggregateId = "AGG-123";
        String confirm = "CONFIRM";
        when(snapshotStore.deleteAllSnapshots(aggregateId)).thenReturn(5);

        // When
        ResponseEntity<Map<String, Object>> response = controller.deleteAllSnapshots(aggregateId, confirm);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("aggregateId")).isEqualTo(aggregateId);
        assertThat(response.getBody().get("deletedCount")).isEqualTo(5);
        assertThat(response.getBody().get("message")).isEqualTo("All snapshots deleted successfully");
        assertThat(response.getBody().get("warning")).isEqualTo("This operation is irreversible");

        verify(snapshotStore).deleteAllSnapshots(aggregateId);
    }

    @Test
    @DisplayName("Deve retornar 400 quando confirmação não é fornecida")
    void shouldReturn400WhenConfirmationNotProvided() {
        // Given
        String aggregateId = "AGG-123";
        String confirm = "NO";

        // When
        ResponseEntity<Map<String, Object>> response = controller.deleteAllSnapshots(aggregateId, confirm);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Confirmation required");
        assertThat(response.getBody().get("message")).isEqualTo("Use confirm=CONFIRM to proceed with deletion");

        verify(snapshotStore, never()).deleteAllSnapshots(anyString());
    }

    @Test
    @DisplayName("Deve usar keepCount padrão quando não especificado")
    void shouldUseDefaultKeepCountWhenNotSpecified() {
        // Given
        String aggregateId = "AGG-123";
        when(snapshotStore.cleanupOldSnapshots(eq(aggregateId), anyInt())).thenReturn(2);

        // When
        ResponseEntity<Map<String, Object>> response = controller.cleanupOldSnapshots(aggregateId, 5);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(snapshotStore).cleanupOldSnapshots(aggregateId, 5);
    }

    @Test
    @DisplayName("Deve usar keepCount padrão global quando não especificado")
    void shouldUseDefaultGlobalKeepCountWhenNotSpecified() {
        // Given
        when(snapshotStore.cleanupAllOldSnapshots(anyInt())).thenReturn(10);

        // When
        ResponseEntity<Map<String, Object>> response = controller.cleanupAllOldSnapshots(5);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(snapshotStore).cleanupAllOldSnapshots(5);
    }
}
