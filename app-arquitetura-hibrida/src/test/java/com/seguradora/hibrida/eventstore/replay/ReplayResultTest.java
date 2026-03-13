package com.seguradora.hibrida.eventstore.replay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ReplayResult}.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("ReplayResult - Testes Unitários")
class ReplayResultTest {

    @Test
    @DisplayName("Deve criar instância com valores corretos")
    void shouldCreateInstanceWithCorrectValues() {
        // Given
        UUID replayId = UUID.randomUUID();
        Instant startedAt = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant completedAt = Instant.now();

        // When
        ReplayResult result = ReplayResult.builder()
            .replayId(replayId)
            .name("Test Replay")
            .description("Test Description")
            .status(ReplayResult.FinalStatus.SUCCESS)
            .startedAt(startedAt)
            .completedAt(completedAt)
            .totalEventsProcessed(1000L)
            .successfulEvents(950L)
            .failedEvents(50L)
            .skippedEvents(10L)
            .peakThroughput(100.0)
            .batchesProcessed(10)
            .initiatedBy("test-user")
            .build();

        // Then
        assertThat(result.getReplayId()).isEqualTo(replayId);
        assertThat(result.getName()).isEqualTo("Test Replay");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getStatus()).isEqualTo(ReplayResult.FinalStatus.SUCCESS);
        assertThat(result.getStartedAt()).isEqualTo(startedAt);
        assertThat(result.getCompletedAt()).isEqualTo(completedAt);
        assertThat(result.getTotalEventsProcessed()).isEqualTo(1000L);
        assertThat(result.getSuccessfulEvents()).isEqualTo(950L);
        assertThat(result.getFailedEvents()).isEqualTo(50L);
        assertThat(result.getSkippedEvents()).isEqualTo(10L);
        assertThat(result.getPeakThroughput()).isEqualTo(100.0);
        assertThat(result.getBatchesProcessed()).isEqualTo(10);
        assertThat(result.getInitiatedBy()).isEqualTo("test-user");
    }

    @Test
    @DisplayName("Deve calcular duração corretamente")
    void shouldCalculateDurationCorrectly() {
        // Given
        Instant startedAt = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant completedAt = Instant.now();

        ReplayResult result = ReplayResult.builder()
            .startedAt(startedAt)
            .completedAt(completedAt)
            .build();

        // When
        Duration duration = result.getDuration();

        // Then
        assertThat(duration).isGreaterThan(Duration.ofMinutes(59));
        assertThat(duration).isLessThanOrEqualTo(Duration.ofHours(1));
    }

    @Test
    @DisplayName("Deve retornar duração zero quando timestamps são null")
    void shouldReturnZeroDurationWhenTimestampsAreNull() {
        // Given
        ReplayResult result = ReplayResult.builder().build();

        // When
        Duration duration = result.getDuration();

        // Then
        assertThat(duration).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("Deve calcular taxa de sucesso corretamente")
    void shouldCalculateSuccessRateCorrectly() {
        // Given
        ReplayResult result = ReplayResult.builder()
            .totalEventsProcessed(100L)
            .successfulEvents(75L)
            .build();

        // When
        double successRate = result.getSuccessRate();

        // Then
        assertThat(successRate).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Deve retornar taxa de sucesso zero quando não há eventos")
    void shouldReturnZeroSuccessRateWhenNoEvents() {
        // Given
        ReplayResult result = ReplayResult.builder()
            .totalEventsProcessed(0L)
            .build();

        // When
        double successRate = result.getSuccessRate();

        // Then
        assertThat(successRate).isZero();
    }

    @Test
    @DisplayName("Deve calcular taxa de erro corretamente")
    void shouldCalculateErrorRateCorrectly() {
        // Given
        ReplayResult result = ReplayResult.builder()
            .totalEventsProcessed(100L)
            .failedEvents(25L)
            .build();

        // When
        double errorRate = result.getErrorRate();

        // Then
        assertThat(errorRate).isEqualTo(25.0);
    }

    @Test
    @DisplayName("Deve retornar taxa de erro zero quando não há eventos")
    void shouldReturnZeroErrorRateWhenNoEvents() {
        // Given
        ReplayResult result = ReplayResult.builder()
            .totalEventsProcessed(0L)
            .build();

        // When
        double errorRate = result.getErrorRate();

        // Then
        assertThat(errorRate).isZero();
    }

    @Test
    @DisplayName("Deve calcular throughput médio corretamente")
    void shouldCalculateAverageThroughputCorrectly() {
        // Given
        Instant startedAt = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant completedAt = Instant.now();

        ReplayResult result = ReplayResult.builder()
            .startedAt(startedAt)
            .completedAt(completedAt)
            .totalEventsProcessed(3600L)
            .build();

        // When
        double throughput = result.getAverageThroughput();

        // Then
        assertThat(throughput).isCloseTo(1.0, within(0.1)); // ~1 evento/segundo
    }

    @Test
    @DisplayName("Deve retornar throughput zero quando duração é zero")
    void shouldReturnZeroThroughputWhenDurationIsZero() {
        // Given
        ReplayResult result = ReplayResult.builder()
            .totalEventsProcessed(100L)
            .build();

        // When
        double throughput = result.getAverageThroughput();

        // Then
        assertThat(throughput).isZero();
    }

    @Test
    @DisplayName("Deve calcular tamanho médio de lote corretamente")
    void shouldCalculateAverageBatchSizeCorrectly() {
        // Given
        ReplayResult result = ReplayResult.builder()
            .totalEventsProcessed(1000L)
            .batchesProcessed(10)
            .build();

        // When
        double avgBatchSize = result.getAverageBatchSize();

        // Then
        assertThat(avgBatchSize).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Deve retornar tamanho de lote zero quando não há lotes")
    void shouldReturnZeroBatchSizeWhenNoBatches() {
        // Given
        ReplayResult result = ReplayResult.builder()
            .totalEventsProcessed(100L)
            .batchesProcessed(0)
            .build();

        // When
        double avgBatchSize = result.getAverageBatchSize();

        // Then
        assertThat(avgBatchSize).isZero();
    }

    @Test
    @DisplayName("Deve verificar se replay foi bem-sucedido")
    void shouldCheckIfReplayWasSuccessful() {
        // Given
        ReplayResult successResult = ReplayResult.builder()
            .status(ReplayResult.FinalStatus.SUCCESS)
            .build();

        ReplayResult failedResult = ReplayResult.builder()
            .status(ReplayResult.FinalStatus.FAILED)
            .build();

        // Then
        assertThat(successResult.isSuccessful()).isTrue();
        assertThat(failedResult.isSuccessful()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se replay teve sucesso parcial")
    void shouldCheckIfReplayHadPartialSuccess() {
        // Given
        ReplayResult partialResult = ReplayResult.builder()
            .status(ReplayResult.FinalStatus.PARTIAL)
            .build();

        ReplayResult successResult = ReplayResult.builder()
            .status(ReplayResult.FinalStatus.SUCCESS)
            .build();

        // Then
        assertThat(partialResult.isPartialSuccess()).isTrue();
        assertThat(successResult.isPartialSuccess()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se replay falhou")
    void shouldCheckIfReplayFailed() {
        // Given
        ReplayResult failedResult = ReplayResult.builder()
            .status(ReplayResult.FinalStatus.FAILED)
            .build();

        ReplayResult successResult = ReplayResult.builder()
            .status(ReplayResult.FinalStatus.SUCCESS)
            .build();

        // Then
        assertThat(failedResult.isFailed()).isTrue();
        assertThat(successResult.isFailed()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se replay foi cancelado")
    void shouldCheckIfReplayWasCancelled() {
        // Given
        ReplayResult cancelledResult = ReplayResult.builder()
            .status(ReplayResult.FinalStatus.CANCELLED)
            .build();

        ReplayResult successResult = ReplayResult.builder()
            .status(ReplayResult.FinalStatus.SUCCESS)
            .build();

        // Then
        assertThat(cancelledResult.isCancelled()).isTrue();
        assertThat(successResult.isCancelled()).isFalse();
    }

    @Test
    @DisplayName("Deve gerar resumo textual do resultado")
    void shouldGenerateSummary() {
        // Given
        Instant startedAt = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant completedAt = Instant.now();

        ReplayResult result = ReplayResult.builder()
            .name("Test Replay")
            .status(ReplayResult.FinalStatus.SUCCESS)
            .startedAt(startedAt)
            .completedAt(completedAt)
            .totalEventsProcessed(1000L)
            .successfulEvents(950L)
            .failedEvents(50L)
            .build();

        // When
        String summary = result.getSummary();

        // Then
        assertThat(summary)
            .contains("Test Replay")
            .contains("success")
            .contains("1000")
            .contains("950")
            .contains("50");
    }

    @Test
    @DisplayName("Deve criar resultado de sucesso a partir de progresso")
    void shouldCreateSuccessResultFromProgress() {
        // Given
        UUID replayId = UUID.randomUUID();
        ReplayConfiguration config = ReplayConfiguration.builder()
            .name("Test Replay")
            .description("Test Description")
            .initiatedBy("test-user")
            .build();

        ReplayProgress progress = ReplayProgress.builder()
            .replayId(replayId)
            .startedAt(Instant.now().minus(1, ChronoUnit.HOURS))
            .completedAt(Instant.now())
            .processedEvents(1000L)
            .successfulEvents(1000L)
            .failedEvents(0L)
            .skippedEvents(0L)
            .currentBatch(10)
            .build();

        // When
        ReplayResult result = ReplayResult.success(replayId, config, progress);

        // Then
        assertThat(result.getReplayId()).isEqualTo(replayId);
        assertThat(result.getName()).isEqualTo("Test Replay");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getStatus()).isEqualTo(ReplayResult.FinalStatus.SUCCESS);
        assertThat(result.getTotalEventsProcessed()).isEqualTo(1000L);
        assertThat(result.getSuccessfulEvents()).isEqualTo(1000L);
        assertThat(result.getInitiatedBy()).isEqualTo("test-user");
    }

    @Test
    @DisplayName("Deve criar resultado de falha a partir de progresso")
    void shouldCreateFailureResultFromProgress() {
        // Given
        UUID replayId = UUID.randomUUID();
        ReplayConfiguration config = ReplayConfiguration.builder()
            .name("Test Replay")
            .build();

        ReplayProgress progress = ReplayProgress.builder()
            .replayId(replayId)
            .startedAt(Instant.now().minus(1, ChronoUnit.HOURS))
            .completedAt(Instant.now())
            .processedEvents(1000L)
            .successfulEvents(0L)
            .failedEvents(1000L)
            .skippedEvents(0L)
            .currentBatch(10)
            .build();

        List<ReplayError> errors = List.of(
            ReplayError.builder()
                .errorType(ReplayError.ErrorType.EVENT_PROCESSING)
                .message("Test error")
                .build()
        );

        // When
        ReplayResult result = ReplayResult.failure(replayId, config, progress, errors);

        // Then
        assertThat(result.getStatus()).isEqualTo(ReplayResult.FinalStatus.FAILED);
        assertThat(result.getFailedEvents()).isEqualTo(1000L);
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    @DisplayName("Deve criar resultado de falha parcial quando há eventos com sucesso")
    void shouldCreatePartialFailureWhenThereAreSuccessfulEvents() {
        // Given
        UUID replayId = UUID.randomUUID();
        ReplayConfiguration config = ReplayConfiguration.builder()
            .name("Test Replay")
            .build();

        ReplayProgress progress = ReplayProgress.builder()
            .replayId(replayId)
            .startedAt(Instant.now().minus(1, ChronoUnit.HOURS))
            .completedAt(Instant.now())
            .processedEvents(1000L)
            .successfulEvents(500L)
            .failedEvents(500L)
            .skippedEvents(0L)
            .currentBatch(10)
            .build();

        // When
        ReplayResult result = ReplayResult.failure(replayId, config, progress, List.of());

        // Then
        assertThat(result.getStatus()).isEqualTo(ReplayResult.FinalStatus.PARTIAL);
        assertThat(result.getSuccessfulEvents()).isEqualTo(500L);
        assertThat(result.getFailedEvents()).isEqualTo(500L);
    }

    @Test
    @DisplayName("Deve criar resultado de cancelamento a partir de progresso")
    void shouldCreateCancelledResultFromProgress() {
        // Given
        UUID replayId = UUID.randomUUID();
        ReplayConfiguration config = ReplayConfiguration.builder()
            .name("Test Replay")
            .build();

        ReplayProgress progress = ReplayProgress.builder()
            .replayId(replayId)
            .startedAt(Instant.now().minus(1, ChronoUnit.HOURS))
            .completedAt(Instant.now())
            .processedEvents(500L)
            .successfulEvents(500L)
            .failedEvents(0L)
            .skippedEvents(0L)
            .currentBatch(5)
            .build();

        // When
        ReplayResult result = ReplayResult.cancelled(replayId, config, progress);

        // Then
        assertThat(result.getStatus()).isEqualTo(ReplayResult.FinalStatus.CANCELLED);
        assertThat(result.getTotalEventsProcessed()).isEqualTo(500L);
    }

    @Test
    @DisplayName("Deve ter equals e hashCode corretos")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        UUID replayId = UUID.randomUUID();
        Instant startedAt = Instant.now();

        ReplayResult result1 = ReplayResult.builder()
            .replayId(replayId)
            .name("Test")
            .startedAt(startedAt)
            .totalEventsProcessed(100L)
            .build();

        ReplayResult result2 = ReplayResult.builder()
            .replayId(replayId)
            .name("Test")
            .startedAt(startedAt)
            .totalEventsProcessed(100L)
            .build();

        ReplayResult result3 = ReplayResult.builder()
            .replayId(UUID.randomUUID())
            .name("Different")
            .startedAt(startedAt)
            .totalEventsProcessed(200L)
            .build();

        // Then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        assertThat(result1).isNotEqualTo(result3);
    }

    @Test
    @DisplayName("Deve ter toString útil")
    void shouldHaveUsefulToString() {
        // Given
        ReplayResult result = ReplayResult.builder()
            .name("Test Replay")
            .status(ReplayResult.FinalStatus.SUCCESS)
            .totalEventsProcessed(1000L)
            .build();

        // When
        String toString = result.toString();

        // Then
        assertThat(toString)
            .contains("ReplayResult")
            .contains("name")
            .contains("status");
    }
}
