package com.seguradora.hibrida.eventstore.controller;

import com.seguradora.hibrida.eventstore.archive.ArchiveResult;
import com.seguradora.hibrida.eventstore.archive.ArchiveStatistics;
import com.seguradora.hibrida.eventstore.archive.ArchiveSummary;
import com.seguradora.hibrida.eventstore.archive.EventArchiver;
import com.seguradora.hibrida.eventstore.partition.PartitionManager;
import com.seguradora.hibrida.eventstore.partition.PartitionStatistics;
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

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EventStoreMaintenanceController - Testes Unitários")
class EventStoreMaintenanceControllerTest {

    @Mock
    private PartitionManager partitionManager;

    @Mock
    private EventArchiver eventArchiver;

    @InjectMocks
    private EventStoreMaintenanceController controller;

    @Test
    @DisplayName("Deve executar manutenção de partições com sucesso")
    void shouldMaintainPartitionsSuccessfully() {
        // Given
        when(partitionManager.maintainPartitions()).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = controller.maintainPartitions();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Manutenção executada com sucesso");

        verify(partitionManager).maintainPartitions();
    }

    @Test
    @DisplayName("Deve retornar falha quando manutenção não é bem-sucedida")
    void shouldReturnFailureWhenMaintenanceUnsuccessful() {
        // Given
        when(partitionManager.maintainPartitions()).thenReturn(false);

        // When
        ResponseEntity<Map<String, Object>> response = controller.maintainPartitions();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message")).isEqualTo("Falha na manutenção");
    }

    @Test
    @DisplayName("Deve criar partição específica com sucesso")
    void shouldCreateSpecificPartitionSuccessfully() {
        // Given
        String date = "2024-01-15";
        when(partitionManager.createMonthlyPartition(anyString(), any(LocalDate.class))).thenReturn(true);
        when(partitionManager.calculatePartitionName(any(LocalDate.class))).thenReturn("events_2024_01");

        // When
        ResponseEntity<Map<String, Object>> response = controller.createPartition(date);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("partitionName")).isEqualTo("events_2024_01");
        assertThat(response.getBody().get("date")).isEqualTo(date);

        verify(partitionManager).createMonthlyPartition(eq("events"), any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve retornar 400 quando data é inválida")
    void shouldReturn400WhenDateIsInvalid() {
        // Given
        String invalidDate = "invalid-date";

        // When
        ResponseEntity<Map<String, Object>> response = controller.createPartition(invalidDate);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("error")).isNotNull();
    }

    @Test
    @DisplayName("Deve listar estatísticas de partições")
    void shouldListPartitionStatistics() {
        // Given
        List<PartitionStatistics> stats = Arrays.asList(
                mock(PartitionStatistics.class),
                mock(PartitionStatistics.class)
        );
        when(partitionManager.getPartitionStatistics()).thenReturn(stats);

        // When
        ResponseEntity<List<PartitionStatistics>> response = controller.getPartitionStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);

        verify(partitionManager).getPartitionStatistics();
    }

    @Test
    @DisplayName("Deve listar partições existentes")
    void shouldListExistingPartitions() {
        // Given
        List<String> partitions = Arrays.asList("events_2024_01", "events_2024_02", "events_2024_03");
        when(partitionManager.listPartitions()).thenReturn(partitions);
        when(partitionManager.arePartitionsHealthy()).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = controller.listPartitions();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("partitions")).isEqualTo(partitions);
        assertThat(response.getBody().get("count")).isEqualTo(3);
        assertThat(response.getBody().get("healthy")).isEqualTo(true);

        verify(partitionManager).listPartitions();
        verify(partitionManager).arePartitionsHealthy();
    }

    @Test
    @DisplayName("Deve verificar saúde das partições")
    void shouldCheckPartitionHealth() {
        // Given
        when(partitionManager.arePartitionsHealthy()).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = controller.checkPartitionHealth();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("healthy")).isEqualTo(true);
        assertThat(response.getBody().get("status")).isEqualTo("OK");

        verify(partitionManager).arePartitionsHealthy();
    }

    @Test
    @DisplayName("Deve incluir recomendação quando partições não estão saudáveis")
    void shouldIncludeRecommendationWhenPartitionsUnhealthy() {
        // Given
        when(partitionManager.arePartitionsHealthy()).thenReturn(false);

        // When
        ResponseEntity<Map<String, Object>> response = controller.checkPartitionHealth();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("healthy")).isEqualTo(false);
        assertThat(response.getBody().get("status")).isEqualTo("UNHEALTHY");
        assertThat(response.getBody().get("recommendation")).isEqualTo("Execute manutenção de partições");
    }

    @Test
    @DisplayName("Deve executar arquivamento automático com sucesso")
    void shouldExecuteArchivingSuccessfully() {
        // Given
        ArchiveSummary summary = mock(ArchiveSummary.class);
        when(summary.getSuccessCount()).thenReturn(3L);
        when(summary.getErrorCount()).thenReturn(0L);
        when(eventArchiver.executeAutoArchiving()).thenReturn(summary);
        doNothing().when(summary).finish();

        // When
        ResponseEntity<ArchiveSummary> response = controller.executeArchiving();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(eventArchiver).executeAutoArchiving();
        verify(summary).finish();
    }

    @Test
    @DisplayName("Deve arquivar partição específica")
    void shouldArchiveSpecificPartition() {
        // Given
        String partitionName = "events_2023_01";
        ArchiveResult result = mock(ArchiveResult.class);
        when(result.isSuccess()).thenReturn(true);
        when(result.getPartitionName()).thenReturn(partitionName);
        when(result.getEventCount()).thenReturn(10000L);
        when(result.getCompressedSize()).thenReturn(1024L * 1024L); // 1MB
        when(result.getTimestamp()).thenReturn(java.time.Instant.now());
        when(eventArchiver.archivePartition(partitionName)).thenReturn(result);

        // When
        ResponseEntity<Map<String, Object>> response = controller.archivePartition(partitionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("partitionName")).isEqualTo(partitionName);
        assertThat(response.getBody().get("eventCount")).isEqualTo(10000L);

        verify(eventArchiver).archivePartition(partitionName);
    }

    @Test
    @DisplayName("Deve incluir mensagem de erro quando arquivamento falha")
    void shouldIncludeErrorMessageWhenArchivingFails() {
        // Given
        String partitionName = "events_2023_01";
        ArchiveResult result = mock(ArchiveResult.class);
        when(result.isSuccess()).thenReturn(false);
        when(result.getPartitionName()).thenReturn(partitionName);
        when(result.getErrorMessage()).thenReturn("Partition not found");
        when(eventArchiver.archivePartition(partitionName)).thenReturn(result);

        // When
        ResponseEntity<Map<String, Object>> response = controller.archivePartition(partitionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("error")).isEqualTo("Partition not found");
    }

    @Test
    @DisplayName("Deve restaurar partição arquivada")
    void shouldRestoreArchivedPartition() {
        // Given
        String partitionName = "events_2023_01";
        when(eventArchiver.restorePartition(partitionName)).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = controller.restorePartition(partitionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("partitionName")).isEqualTo(partitionName);
        assertThat(response.getBody().get("message")).isEqualTo("Partição restaurada com sucesso");

        verify(eventArchiver).restorePartition(partitionName);
    }

    @Test
    @DisplayName("Deve retornar falha quando restauração não é bem-sucedida")
    void shouldReturnFailureWhenRestoreUnsuccessful() {
        // Given
        String partitionName = "events_2023_01";
        when(eventArchiver.restorePartition(partitionName)).thenReturn(false);

        // When
        ResponseEntity<Map<String, Object>> response = controller.restorePartition(partitionName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message")).isEqualTo("Falha na restauração");
    }

    @Test
    @DisplayName("Deve listar partições elegíveis para arquivamento")
    void shouldListEligiblePartitions() {
        // Given
        List<String> eligible = Arrays.asList("events_2022_01", "events_2022_02");
        when(eventArchiver.findPartitionsForArchiving()).thenReturn(eligible);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getEligiblePartitions();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("partitions")).isEqualTo(eligible);
        assertThat(response.getBody().get("count")).isEqualTo(2);

        verify(eventArchiver).findPartitionsForArchiving();
    }

    @Test
    @DisplayName("Deve obter estatísticas de arquivamento")
    void shouldGetArchiveStatistics() {
        // Given
        ArchiveStatistics statistics = mock(ArchiveStatistics.class);
        when(statistics.getTotalArchives()).thenReturn(10L);
        when(statistics.getTotalEvents()).thenReturn(100000L);
        when(statistics.getFormattedSize()).thenReturn("50 MB");
        when(eventArchiver.getArchiveStatistics()).thenReturn(statistics);

        // When
        ResponseEntity<ArchiveStatistics> response = controller.getArchiveStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(eventArchiver).getArchiveStatistics();
    }

    @Test
    @DisplayName("Deve retornar dashboard de manutenção completo")
    void shouldReturnCompleteDashboard() {
        // Given
        List<PartitionStatistics> partitionStats = Arrays.asList(mock(PartitionStatistics.class));
        when(partitionManager.getPartitionStatistics()).thenReturn(partitionStats);
        when(partitionManager.arePartitionsHealthy()).thenReturn(true);

        ArchiveStatistics archiveStats = mock(ArchiveStatistics.class);
        when(archiveStats.getTotalArchives()).thenReturn(5L);
        when(archiveStats.getTotalEvents()).thenReturn(50000L);
        when(archiveStats.getFormattedSize()).thenReturn("25 MB");
        when(eventArchiver.getArchiveStatistics()).thenReturn(archiveStats);

        List<String> eligible = Arrays.asList("events_2022_01");
        when(eventArchiver.findPartitionsForArchiving()).thenReturn(eligible);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getDashboard();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("partitions", "archives", "eligible", "timestamp");

        @SuppressWarnings("unchecked")
        Map<String, Object> partitions = (Map<String, Object>) response.getBody().get("partitions");
        assertThat(partitions.get("total")).isEqualTo(1);
        assertThat(partitions.get("healthy")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> archives = (Map<String, Object>) response.getBody().get("archives");
        assertThat(archives.get("totalArchives")).isEqualTo(5L);

        @SuppressWarnings("unchecked")
        Map<String, Object> eligibleMap = (Map<String, Object>) response.getBody().get("eligible");
        assertThat(eligibleMap.get("count")).isEqualTo(1);

        verify(partitionManager).getPartitionStatistics();
        verify(eventArchiver).getArchiveStatistics();
        verify(eventArchiver).findPartitionsForArchiving();
    }
}
