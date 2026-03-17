package com.seguradora.hibrida.eventstore.replay.example;

import com.seguradora.hibrida.eventstore.replay.EventReplayer;
import com.seguradora.hibrida.eventstore.replay.ReplayResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link ReplayExampleService}.
 */
@DisplayName("ReplayExampleService Tests")
class ReplayExampleServiceTest {

    @Test
    @DisplayName("Deve estar anotado com @Service")
    void shouldBeAnnotatedWithService() {
        assertThat(ReplayExampleService.class.isAnnotationPresent(Service.class)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar EventReplayer no construtor")
    void shouldAcceptEventReplayerInConstructor() throws NoSuchMethodException {
        assertThat(ReplayExampleService.class.getConstructor(EventReplayer.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método replayLast24Hours")
    void shouldDeclareReplayLast24HoursMethod() throws NoSuchMethodException {
        var method = ReplayExampleService.class.getMethod("replayLast24Hours");
        assertThat(method.getReturnType()).isEqualTo(CompletableFuture.class);
    }

    @Test
    @DisplayName("Deve declarar método replaySinistroEvents")
    void shouldDeclareReplaySinistroEventsMethod() throws NoSuchMethodException {
        var method = ReplayExampleService.class.getMethod("replaySinistroEvents");
        assertThat(method.getReturnType()).isEqualTo(CompletableFuture.class);
    }

    @Test
    @DisplayName("Deve declarar método simulateReplayValidation")
    void shouldDeclareSimulateReplayValidationMethod() throws NoSuchMethodException {
        var method = ReplayExampleService.class.getMethod("simulateReplayValidation");
        assertThat(method.getReturnType()).isEqualTo(CompletableFuture.class);
    }

    @Test
    @DisplayName("Deve declarar método replayWithAdvancedFilters")
    void shouldDeclareReplayWithAdvancedFiltersMethod() throws NoSuchMethodException {
        var method = ReplayExampleService.class.getMethod("replayWithAdvancedFilters");
        assertThat(method.getReturnType()).isEqualTo(CompletableFuture.class);
    }

    @Test
    @DisplayName("Deve declarar método monitorActiveReplays")
    void shouldDeclareMonitorActiveReplaysMethod() throws NoSuchMethodException {
        var method = ReplayExampleService.class.getMethod("monitorActiveReplays");
        assertThat(method.getReturnType()).isEqualTo(void.class);
    }

    @Test
    @DisplayName("Deve declarar método analyzeReplayStatistics")
    void shouldDeclareAnalyzeReplayStatisticsMethod() throws NoSuchMethodException {
        var method = ReplayExampleService.class.getMethod("analyzeReplayStatistics");
        assertThat(method.getReturnType()).isEqualTo(void.class);
    }

    @Test
    @DisplayName("Deve declarar método replaySpecificAggregate")
    void shouldDeclareReplaySpecificAggregateMethod() throws NoSuchMethodException {
        var method = ReplayExampleService.class.getMethod("replaySpecificAggregate", String.class);
        assertThat(method.getReturnType()).isEqualTo(CompletableFuture.class);
    }
}
