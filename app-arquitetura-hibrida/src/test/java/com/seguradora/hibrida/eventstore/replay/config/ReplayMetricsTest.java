package com.seguradora.hibrida.eventstore.replay.config;

import com.seguradora.hibrida.eventstore.replay.EventReplayer;
import com.seguradora.hibrida.eventstore.replay.ReplayStatistics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link ReplayMetrics}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReplayMetrics Tests")
class ReplayMetricsTest {

    @Mock
    private EventReplayer eventReplayer;

    private SimpleMeterRegistry meterRegistry;
    private ReplayMetrics replayMetrics;

    @BeforeEach
    void setUp() {
        ReplayStatistics stats = ReplayStatistics.builder()
                .totalReplaysExecuted(0)
                .successfulReplays(0)
                .failedReplays(0)
                .cancelledReplays(0)
                .totalEventsReprocessed(0)
                .averageThroughput(0.0)
                .build();

        when(eventReplayer.getStatistics()).thenReturn(stats);
        when(eventReplayer.getActiveReplays()).thenReturn(List.of());

        meterRegistry = new SimpleMeterRegistry();
        replayMetrics = new ReplayMetrics(eventReplayer, meterRegistry);
    }

    @Test
    @DisplayName("Deve aceitar EventReplayer e MeterRegistry no construtor")
    void shouldAcceptEventReplayerAndMeterRegistryInConstructor() throws NoSuchMethodException {
        assertThat(ReplayMetrics.class.getConstructor(EventReplayer.class, io.micrometer.core.instrument.MeterRegistry.class))
                .isNotNull();
    }

    @Test
    @DisplayName("recordReplayStarted deve incrementar contador")
    void recordReplayStartedShouldIncrementCounter() {
        replayMetrics.recordReplayStarted();
        replayMetrics.recordReplayStarted();

        assertThat(meterRegistry.get("replay.total.started").gauge().value()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("recordReplayCompleted deve incrementar contador")
    void recordReplayCompletedShouldIncrementCounter() {
        replayMetrics.recordReplayCompleted();

        assertThat(meterRegistry.get("replay.total.completed").gauge().value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("recordReplayFailed deve incrementar contador")
    void recordReplayFailedShouldIncrementCounter() {
        replayMetrics.recordReplayFailed();

        assertThat(meterRegistry.get("replay.total.failed").gauge().value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("recordReplayCancelled deve incrementar contador")
    void recordReplayCancelledShouldIncrementCounter() {
        replayMetrics.recordReplayCancelled();

        assertThat(meterRegistry.get("replay.total.cancelled").gauge().value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("recordEventsReplayed deve acumular contagem")
    void recordEventsReplayedShouldAccumulateCount() {
        replayMetrics.recordEventsReplayed(100);
        replayMetrics.recordEventsReplayed(200);

        assertThat(meterRegistry.get("replay.events.total").gauge().value()).isEqualTo(300.0);
    }

    @Test
    @DisplayName("reset deve zerar todos os contadores")
    void resetShouldZeroAllCounters() {
        replayMetrics.recordReplayStarted();
        replayMetrics.recordReplayCompleted();
        replayMetrics.recordReplayFailed();
        replayMetrics.recordReplayCancelled();
        replayMetrics.recordEventsReplayed(500);

        replayMetrics.reset();

        assertThat(meterRegistry.get("replay.total.started").gauge().value()).isZero();
        assertThat(meterRegistry.get("replay.total.completed").gauge().value()).isZero();
        assertThat(meterRegistry.get("replay.total.failed").gauge().value()).isZero();
        assertThat(meterRegistry.get("replay.total.cancelled").gauge().value()).isZero();
        assertThat(meterRegistry.get("replay.events.total").gauge().value()).isZero();
    }

    @Test
    @DisplayName("startReplayTimer deve retornar Sample não nulo")
    void startReplayTimerShouldReturnNonNullSample() {
        var sample = replayMetrics.startReplayTimer();
        assertThat(sample).isNotNull();
    }

    @Test
    @DisplayName("startEventProcessingTimer deve retornar Sample não nulo")
    void startEventProcessingTimerShouldReturnNonNullSample() {
        var sample = replayMetrics.startEventProcessingTimer();
        assertThat(sample).isNotNull();
    }
}
