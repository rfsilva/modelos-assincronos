package com.seguradora.hibrida.eventstore.replay.config;

import com.seguradora.hibrida.eventstore.replay.EventReplayer;
import com.seguradora.hibrida.eventstore.replay.ReplayStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link ReplayHealthIndicator}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReplayHealthIndicator Tests")
class ReplayHealthIndicatorTest {

    @Mock
    private EventReplayer eventReplayer;

    private ReplayHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        ReplayStatistics stats = ReplayStatistics.builder()
                .totalReplaysExecuted(10)
                .successfulReplays(9)
                .failedReplays(1)
                .build();

        when(eventReplayer.isHealthy()).thenReturn(true);
        when(eventReplayer.getStatistics()).thenReturn(stats);
        when(eventReplayer.getActiveReplays()).thenReturn(List.of());

        healthIndicator = new ReplayHealthIndicator(eventReplayer);
    }

    @Test
    @DisplayName("Deve implementar HealthIndicator")
    void shouldImplementHealthIndicator() {
        assertThat(HealthIndicator.class.isAssignableFrom(ReplayHealthIndicator.class)).isTrue();
    }

    @Test
    @DisplayName("health() deve retornar UP quando replayer está saudável")
    void healthShouldReturnUpWhenReplayerIsHealthy() {
        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    @DisplayName("health() deve retornar DOWN quando replayer não está saudável")
    void healthShouldReturnDownWhenReplayerIsNotHealthy() {
        when(eventReplayer.isHealthy()).thenReturn(false);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    @DisplayName("health() deve retornar DOWN quando lança exceção")
    void healthShouldReturnDownWhenExceptionIsThrown() {
        when(eventReplayer.isHealthy()).thenThrow(new RuntimeException("falha"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    @DisplayName("isHealthy() deve retornar true quando replayer está saudável")
    void isHealthyShouldReturnTrueWhenReplayerIsHealthy() {
        assertThat(healthIndicator.isHealthy()).isTrue();
    }

    @Test
    @DisplayName("isHealthy() deve retornar false quando replayer não está saudável")
    void isHealthyShouldReturnFalseWhenReplayerIsNotHealthy() {
        when(eventReplayer.isHealthy()).thenReturn(false);
        assertThat(healthIndicator.isHealthy()).isFalse();
    }

    @Test
    @DisplayName("health() deve conter detalhes sobre replays ativos")
    void healthShouldContainActiveReplaysDetails() {
        Health health = healthIndicator.health();

        assertThat(health.getDetails()).containsKey("active_replays_count");
    }

    @Test
    @DisplayName("health() deve conter detalhes sobre replayer_operational")
    void healthShouldContainReplayerOperationalDetails() {
        Health health = healthIndicator.health();

        assertThat(health.getDetails()).containsKey("replayer_operational");
    }
}
