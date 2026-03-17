package com.seguradora.hibrida.projection;

import com.seguradora.hibrida.eventbus.example.TestEvent;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Testes unitários para {@link ProjectionEventProcessor}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProjectionEventProcessor Tests")
class ProjectionEventProcessorTest {

    private ProjectionRegistry registry;
    private ProjectionEventProcessor processor;

    @BeforeEach
    void setUp() {
        registry = new ProjectionRegistry();
        processor = new ProjectionEventProcessor(registry);
        // Não injetar trackerRepository → processamento sem tracking
    }

    // =========================================================================
    // Anotações
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(ProjectionEventProcessor.class.isAnnotationPresent(Component.class)).isTrue();
    }

    // =========================================================================
    // processEventAsync — @Async
    // =========================================================================

    @Test
    @DisplayName("processEventAsync() deve estar anotado com @Async")
    void processEventAsyncShouldBeAnnotatedWithAsync() throws NoSuchMethodException {
        Method m = ProjectionEventProcessor.class.getMethod("processEventAsync", DomainEvent.class, Long.class);
        assertThat(m.isAnnotationPresent(Async.class)).isTrue();
    }

    // =========================================================================
    // processEvent sem tracking (trackerRepository=null)
    // =========================================================================

    @Nested
    @DisplayName("processEvent() sem tracking")
    class ProcessEventSemTracking {

        @Test
        @DisplayName("Deve processar evento sem exceção quando não há handler")
        void shouldProcessEventWithoutExceptionWhenNoHandler() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);

            assertThatCode(() -> processor.processEvent(event, 1L)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve invocar handler quando registrado e trackerRepository é null")
        void shouldInvokeHandlerWhenRegisteredAndNoTracker() {
            boolean[] handled = {false};

            registry.registerHandler(new ProjectionHandler<TestEvent>() {
                @Override
                public void handle(TestEvent event) {
                    handled[0] = true;
                }

                @Override
                public Class<TestEvent> getEventType() {
                    return TestEvent.class;
                }

                @Override
                public String getProjectionName() {
                    return "TestProjection";
                }
            });

            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
            processor.processEvent(event, 1L);

            assertThat(handled[0]).isTrue();
        }
    }

    // =========================================================================
    // processEventAsync
    // =========================================================================

    @Test
    @DisplayName("processEventAsync() deve retornar CompletableFuture não nulo")
    void processEventAsyncShouldReturnNonNullFuture() {
        TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);

        CompletableFuture<Void> future = processor.processEventAsync(event, 1L);

        assertThat(future).isNotNull();
    }

    // =========================================================================
    // processBatch
    // =========================================================================

    @Test
    @DisplayName("processBatch() deve processar lista de eventos sem exceção")
    void processBatchShouldProcessListWithoutException() {
        List<ProjectionEventProcessor.EventWithId> events = List.of(
                new ProjectionEventProcessor.EventWithId(new TestEvent("a", "m", "c", 1), 1L),
                new ProjectionEventProcessor.EventWithId(new TestEvent("b", "m", "c", 1), 2L)
        );

        assertThatCode(() -> processor.processBatch(events)).doesNotThrowAnyException();
    }

    // =========================================================================
    // setTrackerRepository
    // =========================================================================

    @Test
    @DisplayName("setTrackerRepository() não deve lançar exceção")
    void setTrackerRepositoryShouldNotThrowException() {
        assertThatCode(() -> processor.setTrackerRepository(null)).doesNotThrowAnyException();
    }
}
