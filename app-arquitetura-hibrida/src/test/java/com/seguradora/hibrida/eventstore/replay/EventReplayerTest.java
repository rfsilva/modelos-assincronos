package com.seguradora.hibrida.eventstore.replay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de contrato para {@link EventReplayer}.
 */
@DisplayName("EventReplayer Tests")
class EventReplayerTest {

    @Test
    @DisplayName("Deve ser uma interface")
    void shouldBeInterface() {
        assertThat(EventReplayer.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Deve declarar método replayByPeriod")
    void shouldDeclareReplayByPeriodMethod() throws NoSuchMethodException {
        assertThat(EventReplayer.class.getMethod("replayByPeriod", ReplayConfiguration.class))
                .isNotNull();
    }

    @Test
    @DisplayName("replayByPeriod deve retornar CompletableFuture")
    void replayByPeriodShouldReturnCompletableFuture() throws NoSuchMethodException {
        var method = EventReplayer.class.getMethod("replayByPeriod", ReplayConfiguration.class);
        assertThat(method.getReturnType()).isEqualTo(CompletableFuture.class);
    }

    @Test
    @DisplayName("Deve declarar método replayByEventType")
    void shouldDeclareReplayByEventTypeMethod() throws NoSuchMethodException {
        assertThat(EventReplayer.class.getMethod(
                "replayByEventType", String.class, Instant.class, Instant.class, ReplayConfiguration.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método replayByAggregate")
    void shouldDeclareReplayByAggregateMethod() throws NoSuchMethodException {
        assertThat(EventReplayer.class.getMethod(
                "replayByAggregate", String.class, Long.class, ReplayConfiguration.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método replayWithFilter")
    void shouldDeclareReplayWithFilterMethod() throws NoSuchMethodException {
        assertThat(EventReplayer.class.getMethod(
                "replayWithFilter", ReplayFilter.class, ReplayConfiguration.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método simulateReplay")
    void shouldDeclareSimulateReplayMethod() throws NoSuchMethodException {
        assertThat(EventReplayer.class.getMethod("simulateReplay", ReplayConfiguration.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método pauseReplay retornando boolean")
    void shouldDeclarePauseReplayMethodReturningBoolean() throws NoSuchMethodException {
        var method = EventReplayer.class.getMethod("pauseReplay", UUID.class);
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Deve declarar método resumeReplay retornando boolean")
    void shouldDeclareResumeReplayMethodReturningBoolean() throws NoSuchMethodException {
        var method = EventReplayer.class.getMethod("resumeReplay", UUID.class);
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Deve declarar método cancelReplay retornando boolean")
    void shouldDeclareCancelReplayMethodReturningBoolean() throws NoSuchMethodException {
        var method = EventReplayer.class.getMethod("cancelReplay", UUID.class);
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Deve declarar método getProgress")
    void shouldDeclareGetProgressMethod() throws NoSuchMethodException {
        var method = EventReplayer.class.getMethod("getProgress", UUID.class);
        assertThat(method.getReturnType()).isEqualTo(ReplayProgress.class);
    }

    @Test
    @DisplayName("Deve declarar método getActiveReplays retornando List")
    void shouldDeclareGetActiveReplaysMethod() throws NoSuchMethodException {
        var method = EventReplayer.class.getMethod("getActiveReplays");
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve declarar método getReplayHistory retornando List")
    void shouldDeclareGetReplayHistoryMethod() throws NoSuchMethodException {
        var method = EventReplayer.class.getMethod("getReplayHistory", int.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve declarar método isHealthy retornando boolean")
    void shouldDeclareIsHealthyMethodReturningBoolean() throws NoSuchMethodException {
        var method = EventReplayer.class.getMethod("isHealthy");
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Deve declarar método getStatistics")
    void shouldDeclareGetStatisticsMethod() throws NoSuchMethodException {
        var method = EventReplayer.class.getMethod("getStatistics");
        assertThat(method.getReturnType()).isEqualTo(ReplayStatistics.class);
    }
}
