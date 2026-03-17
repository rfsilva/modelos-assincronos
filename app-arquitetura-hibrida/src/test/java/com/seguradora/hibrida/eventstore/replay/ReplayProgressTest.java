package com.seguradora.hibrida.eventstore.replay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ReplayProgress}.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("ReplayProgress - Testes Unitários")
class ReplayProgressTest {

    @Test
    @DisplayName("Deve criar instância com valores corretos")
    void shouldCreateInstanceWithCorrectValues() {
        // Given
        UUID replayId = UUID.randomUUID();
        Instant startedAt = Instant.now();

        // When
        ReplayProgress progress = ReplayProgress.builder()
            .replayId(replayId)
            .name("Test Replay")
            .status(ReplayProgress.Status.RUNNING)
            .startedAt(startedAt)
            .totalEvents(1000L)
            .processedEvents(500L)
            .successfulEvents(450L)
            .failedEvents(50L)
            .skippedEvents(10L)
            .currentThroughput(10.0)
            .currentBatch(5)
            .totalBatches(10)
            .build();

        // Then
        assertThat(progress.getReplayId()).isEqualTo(replayId);
        assertThat(progress.getName()).isEqualTo("Test Replay");
        assertThat(progress.getStatus()).isEqualTo(ReplayProgress.Status.RUNNING);
        assertThat(progress.getStartedAt()).isEqualTo(startedAt);
        assertThat(progress.getTotalEvents()).isEqualTo(1000L);
        assertThat(progress.getProcessedEvents()).isEqualTo(500L);
        assertThat(progress.getSuccessfulEvents()).isEqualTo(450L);
        assertThat(progress.getFailedEvents()).isEqualTo(50L);
        assertThat(progress.getSkippedEvents()).isEqualTo(10L);
        assertThat(progress.getCurrentThroughput()).isEqualTo(10.0);
        assertThat(progress.getCurrentBatch()).isEqualTo(5);
        assertThat(progress.getTotalBatches()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve calcular percentual de progresso corretamente")
    void shouldCalculateProgressPercentageCorrectly() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .totalEvents(1000L)
            .processedEvents(750L)
            .build();

        // When
        double percentage = progress.getProgressPercentage();

        // Then
        assertThat(percentage).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Deve retornar percentual zero quando não há eventos")
    void shouldReturnZeroPercentageWhenNoEvents() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .totalEvents(0L)
            .processedEvents(0L)
            .build();

        // When
        double percentage = progress.getProgressPercentage();

        // Then
        assertThat(percentage).isZero();
    }

    @Test
    @DisplayName("Deve calcular taxa de sucesso corretamente")
    void shouldCalculateSuccessRateCorrectly() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .processedEvents(100L)
            .successfulEvents(90L)
            .build();

        // When
        double successRate = progress.getSuccessRate();

        // Then
        assertThat(successRate).isEqualTo(90.0);
    }

    @Test
    @DisplayName("Deve retornar taxa de sucesso zero quando não há eventos processados")
    void shouldReturnZeroSuccessRateWhenNoProcessedEvents() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .processedEvents(0L)
            .build();

        // When
        double successRate = progress.getSuccessRate();

        // Then
        assertThat(successRate).isZero();
    }

    @Test
    @DisplayName("Deve calcular taxa de erro corretamente")
    void shouldCalculateErrorRateCorrectly() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .processedEvents(100L)
            .failedEvents(10L)
            .build();

        // When
        double errorRate = progress.getErrorRate();

        // Then
        assertThat(errorRate).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Deve retornar taxa de erro zero quando não há eventos processados")
    void shouldReturnZeroErrorRateWhenNoProcessedEvents() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .processedEvents(0L)
            .build();

        // When
        double errorRate = progress.getErrorRate();

        // Then
        assertThat(errorRate).isZero();
    }

    @Test
    @DisplayName("Deve calcular throughput médio corretamente")
    void shouldCalculateAverageThroughputCorrectly() {
        // Given
        Instant startedAt = Instant.now().minus(10, ChronoUnit.SECONDS);

        ReplayProgress progress = ReplayProgress.builder()
            .startedAt(startedAt)
            .processedEvents(100L)
            .build();

        // When
        double avgThroughput = progress.getAverageThroughput();

        // Then
        assertThat(avgThroughput).isGreaterThan(9.0).isLessThan(11.0); // ~10 eventos/segundo
    }

    @Test
    @DisplayName("Deve retornar throughput zero quando não há tempo decorrido")
    void shouldReturnZeroThroughputWhenNoElapsedTime() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .processedEvents(100L)
            .build();

        // When
        double avgThroughput = progress.getAverageThroughput();

        // Then
        assertThat(avgThroughput).isZero();
    }

    @Test
    @DisplayName("Deve calcular tempo restante estimado")
    void shouldCalculateEstimatedTimeRemaining() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .totalEvents(1000L)
            .processedEvents(500L)
            .currentThroughput(10.0) // 10 eventos/segundo
            .build();

        // When
        Duration remaining = progress.getEstimatedTimeRemaining();

        // Then
        // 500 eventos restantes / 10 eventos/segundo = 50 segundos
        assertThat(remaining).isEqualTo(Duration.ofSeconds(50));
    }

    @Test
    @DisplayName("Deve retornar tempo restante zero quando throughput é zero")
    void shouldReturnZeroTimeRemainingWhenThroughputIsZero() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .totalEvents(1000L)
            .processedEvents(500L)
            .currentThroughput(0.0)
            .build();

        // When
        Duration remaining = progress.getEstimatedTimeRemaining();

        // Then
        assertThat(remaining).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("Deve retornar tempo restante zero quando todos os eventos foram processados")
    void shouldReturnZeroTimeRemainingWhenAllEventsProcessed() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .totalEvents(1000L)
            .processedEvents(1000L)
            .currentThroughput(10.0)
            .build();

        // When
        Duration remaining = progress.getEstimatedTimeRemaining();

        // Then
        assertThat(remaining).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("Deve calcular tempo decorrido corretamente")
    void shouldCalculateElapsedTimeCorrectly() {
        // Given
        Instant now = Instant.now();
        Instant startedAt = now.minus(1, ChronoUnit.HOURS);
        Instant completedAt = now;

        ReplayProgress progress = ReplayProgress.builder()
            .startedAt(startedAt)
            .completedAt(completedAt)
            .build();

        // When
        Duration elapsed = progress.getElapsedTime();

        // Then
        assertThat(elapsed).isGreaterThan(Duration.ofMinutes(59));
        assertThat(elapsed).isLessThanOrEqualTo(Duration.ofHours(1));
    }

    @Test
    @DisplayName("Deve retornar tempo decorrido zero quando não iniciado")
    void shouldReturnZeroElapsedTimeWhenNotStarted() {
        // Given
        ReplayProgress progress = ReplayProgress.builder().build();

        // When
        Duration elapsed = progress.getElapsedTime();

        // Then
        assertThat(elapsed).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("Deve verificar se replay está ativo")
    void shouldCheckIfReplayIsActive() {
        // Given
        ReplayProgress running = ReplayProgress.builder()
            .status(ReplayProgress.Status.RUNNING)
            .build();

        ReplayProgress paused = ReplayProgress.builder()
            .status(ReplayProgress.Status.PAUSED)
            .build();

        ReplayProgress completed = ReplayProgress.builder()
            .status(ReplayProgress.Status.COMPLETED)
            .build();

        // Then
        assertThat(running.isActive()).isTrue();
        assertThat(paused.isActive()).isTrue();
        assertThat(completed.isActive()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se replay está concluído")
    void shouldCheckIfReplayIsCompleted() {
        // Given
        ReplayProgress completed = ReplayProgress.builder()
            .status(ReplayProgress.Status.COMPLETED)
            .build();

        ReplayProgress failed = ReplayProgress.builder()
            .status(ReplayProgress.Status.FAILED)
            .build();

        ReplayProgress cancelled = ReplayProgress.builder()
            .status(ReplayProgress.Status.CANCELLED)
            .build();

        ReplayProgress running = ReplayProgress.builder()
            .status(ReplayProgress.Status.RUNNING)
            .build();

        // Then
        assertThat(completed.isCompleted()).isTrue();
        assertThat(failed.isCompleted()).isTrue();
        assertThat(cancelled.isCompleted()).isTrue();
        assertThat(running.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se replay pode ser pausado")
    void shouldCheckIfReplayCanBePaused() {
        // Given
        ReplayProgress running = ReplayProgress.builder()
            .status(ReplayProgress.Status.RUNNING)
            .build();

        ReplayProgress pending = ReplayProgress.builder()
            .status(ReplayProgress.Status.PENDING)
            .build();

        // Then
        assertThat(running.canBePaused()).isTrue();
        assertThat(pending.canBePaused()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se replay pode ser retomado")
    void shouldCheckIfReplayCanBeResumed() {
        // Given
        ReplayProgress paused = ReplayProgress.builder()
            .status(ReplayProgress.Status.PAUSED)
            .build();

        ReplayProgress running = ReplayProgress.builder()
            .status(ReplayProgress.Status.RUNNING)
            .build();

        // Then
        assertThat(paused.canBeResumed()).isTrue();
        assertThat(running.canBeResumed()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se replay pode ser cancelado")
    void shouldCheckIfReplayCanBeCancelled() {
        // Given
        ReplayProgress running = ReplayProgress.builder()
            .status(ReplayProgress.Status.RUNNING)
            .build();

        ReplayProgress paused = ReplayProgress.builder()
            .status(ReplayProgress.Status.PAUSED)
            .build();

        ReplayProgress pending = ReplayProgress.builder()
            .status(ReplayProgress.Status.PENDING)
            .build();

        ReplayProgress completed = ReplayProgress.builder()
            .status(ReplayProgress.Status.COMPLETED)
            .build();

        // Then
        assertThat(running.canBeCancelled()).isTrue();
        assertThat(paused.canBeCancelled()).isTrue();
        assertThat(pending.canBeCancelled()).isTrue();
        assertThat(completed.canBeCancelled()).isFalse();
    }

    @Test
    @DisplayName("Deve criar progresso inicial corretamente")
    void shouldCreateInitialProgressCorrectly() {
        // Given
        UUID replayId = UUID.randomUUID();
        ReplayConfiguration config = ReplayConfiguration.builder()
            .name("Test Replay")
            .batchSize(100)
            .build();
        long totalEvents = 1000L;

        // When
        ReplayProgress progress = ReplayProgress.initial(replayId, config, totalEvents);

        // Then
        assertThat(progress.getReplayId()).isEqualTo(replayId);
        assertThat(progress.getName()).isEqualTo("Test Replay");
        assertThat(progress.getStatus()).isEqualTo(ReplayProgress.Status.PENDING);
        assertThat(progress.getTotalEvents()).isEqualTo(totalEvents);
        assertThat(progress.getProcessedEvents()).isZero();
        assertThat(progress.getSuccessfulEvents()).isZero();
        assertThat(progress.getFailedEvents()).isZero();
        assertThat(progress.getSkippedEvents()).isZero();
        assertThat(progress.getCurrentBatch()).isZero();
        assertThat(progress.getTotalBatches()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve atualizar progresso corretamente")
    void shouldUpdateProgressCorrectly() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .processedEvents(100L)
            .successfulEvents(90L)
            .failedEvents(10L)
            .build();

        // When
        ReplayProgress updated = progress.updateProgress(200L, 180L, 20L, 15.0);

        // Then
        assertThat(updated.getProcessedEvents()).isEqualTo(200L);
        assertThat(updated.getSuccessfulEvents()).isEqualTo(180L);
        assertThat(updated.getFailedEvents()).isEqualTo(20L);
        assertThat(updated.getCurrentThroughput()).isEqualTo(15.0);
        assertThat(updated.getLastUpdatedAt()).isAfterOrEqualTo(progress.getLastUpdatedAt());
    }

    @Test
    @DisplayName("Deve marcar replay como iniciado")
    void shouldMarkAsStarted() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .status(ReplayProgress.Status.PENDING)
            .build();

        // When
        ReplayProgress started = progress.markAsStarted();

        // Then
        assertThat(started.getStatus()).isEqualTo(ReplayProgress.Status.RUNNING);
        assertThat(started.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve marcar replay como pausado")
    void shouldMarkAsPaused() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .status(ReplayProgress.Status.RUNNING)
            .build();

        // When
        ReplayProgress paused = progress.markAsPaused();

        // Then
        assertThat(paused.getStatus()).isEqualTo(ReplayProgress.Status.PAUSED);
    }

    @Test
    @DisplayName("Deve marcar replay como concluído")
    void shouldMarkAsCompleted() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .status(ReplayProgress.Status.RUNNING)
            .build();

        // When
        ReplayProgress completed = progress.markAsCompleted();

        // Then
        assertThat(completed.getStatus()).isEqualTo(ReplayProgress.Status.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve marcar replay como falhado")
    void shouldMarkAsFailed() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .status(ReplayProgress.Status.RUNNING)
            .build();

        String errorMessage = "Test error";

        // When
        ReplayProgress failed = progress.markAsFailed(errorMessage);

        // Then
        assertThat(failed.getStatus()).isEqualTo(ReplayProgress.Status.FAILED);
        assertThat(failed.getCurrentError()).isEqualTo(errorMessage);
        assertThat(failed.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve marcar replay como cancelado")
    void shouldMarkAsCancelled() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .status(ReplayProgress.Status.RUNNING)
            .build();

        // When
        ReplayProgress cancelled = progress.markAsCancelled();

        // Then
        assertThat(cancelled.getStatus()).isEqualTo(ReplayProgress.Status.CANCELLED);
        assertThat(cancelled.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve ter equals e hashCode corretos")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        UUID replayId = UUID.randomUUID();
        Instant startedAt = Instant.now();

        ReplayProgress progress1 = ReplayProgress.builder()
            .replayId(replayId)
            .name("Test")
            .startedAt(startedAt)
            .processedEvents(100L)
            .build();

        ReplayProgress progress2 = ReplayProgress.builder()
            .replayId(replayId)
            .name("Test")
            .startedAt(startedAt)
            .processedEvents(100L)
            .build();

        ReplayProgress progress3 = ReplayProgress.builder()
            .replayId(UUID.randomUUID())
            .name("Different")
            .startedAt(startedAt)
            .processedEvents(200L)
            .build();

        // Then
        assertThat(progress1).isEqualTo(progress2);
        assertThat(progress1.hashCode()).isEqualTo(progress2.hashCode());
        assertThat(progress1).isNotEqualTo(progress3);
    }

    @Test
    @DisplayName("Deve ter toString útil")
    void shouldHaveUsefulToString() {
        // Given
        ReplayProgress progress = ReplayProgress.builder()
            .name("Test Replay")
            .status(ReplayProgress.Status.RUNNING)
            .processedEvents(500L)
            .build();

        // When
        String toString = progress.toString();

        // Then
        assertThat(toString)
            .contains("ReplayProgress")
            .contains("name")
            .contains("status");
    }
}
