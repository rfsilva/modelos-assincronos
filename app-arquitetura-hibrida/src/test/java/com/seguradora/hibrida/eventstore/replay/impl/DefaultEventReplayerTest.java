package com.seguradora.hibrida.eventstore.replay.impl;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventHandlerRegistry;
import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.replay.EventReplayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link DefaultEventReplayer}.
 */
@DisplayName("DefaultEventReplayer Tests")
class DefaultEventReplayerTest {

    @Test
    @DisplayName("Deve estar anotado com @Service")
    void shouldBeAnnotatedWithService() {
        assertThat(DefaultEventReplayer.class.isAnnotationPresent(Service.class)).isTrue();
    }

    @Test
    @DisplayName("Deve implementar EventReplayer")
    void shouldImplementEventReplayer() {
        assertThat(EventReplayer.class.isAssignableFrom(DefaultEventReplayer.class)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar EventStore, EventBus e EventHandlerRegistry no construtor")
    void shouldAcceptCorrectDependenciesInConstructor() throws NoSuchMethodException {
        assertThat(DefaultEventReplayer.class.getConstructor(
                EventStore.class,
                EventBus.class,
                EventHandlerRegistry.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método replayByPeriod")
    void shouldDeclareReplayByPeriodMethod() throws NoSuchMethodException {
        assertThat(DefaultEventReplayer.class.getMethod(
                "replayByPeriod",
                com.seguradora.hibrida.eventstore.replay.ReplayConfiguration.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método pauseReplay")
    void shouldDeclarePauseReplayMethod() throws NoSuchMethodException {
        assertThat(DefaultEventReplayer.class.getMethod("pauseReplay", java.util.UUID.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método resumeReplay")
    void shouldDeclareResumeReplayMethod() throws NoSuchMethodException {
        assertThat(DefaultEventReplayer.class.getMethod("resumeReplay", java.util.UUID.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método cancelReplay")
    void shouldDeclareCancelReplayMethod() throws NoSuchMethodException {
        assertThat(DefaultEventReplayer.class.getMethod("cancelReplay", java.util.UUID.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método isHealthy")
    void shouldDeclareIsHealthyMethod() throws NoSuchMethodException {
        var method = DefaultEventReplayer.class.getMethod("isHealthy");
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Deve declarar método getStatistics")
    void shouldDeclareGetStatisticsMethod() throws NoSuchMethodException {
        var method = DefaultEventReplayer.class.getMethod("getStatistics");
        assertThat(method.getReturnType())
                .isEqualTo(com.seguradora.hibrida.eventstore.replay.ReplayStatistics.class);
    }

    @Test
    @DisplayName("Deve declarar método getActiveReplays")
    void shouldDeclareGetActiveReplaysMethod() throws NoSuchMethodException {
        var method = DefaultEventReplayer.class.getMethod("getActiveReplays");
        assertThat(method.getReturnType()).isEqualTo(java.util.List.class);
    }
}
