package com.seguradora.hibrida.eventbus.impl;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventBusStatistics;
import com.seguradora.hibrida.eventbus.EventHandler;
import com.seguradora.hibrida.eventbus.EventHandlerRegistry;
import com.seguradora.hibrida.eventbus.example.TestEvent;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Testes unitários para {@link SimpleEventBus}.
 */
@DisplayName("SimpleEventBus Tests")
class SimpleEventBusTest {

    private EventHandlerRegistry registry;
    private SimpleEventBus eventBus;

    @BeforeEach
    void setUp() {
        registry = new EventHandlerRegistry();
        eventBus = new SimpleEventBus(registry);
    }

    @AfterEach
    void tearDown() {
        eventBus.shutdown(5);
    }

    // =========================================================================
    // Anotações
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(SimpleEventBus.class.isAnnotationPresent(Component.class)).isTrue();
    }

    @Test
    @DisplayName("Deve implementar EventBus")
    void shouldImplementEventBus() {
        assertThat(eventBus).isInstanceOf(EventBus.class);
    }

    // =========================================================================
    // isHealthy
    // =========================================================================

    @Test
    @DisplayName("isHealthy() deve retornar true quando recém-criado")
    void isHealthyShouldReturnTrueWhenJustCreated() {
        assertThat(eventBus.isHealthy()).isTrue();
    }

    @Test
    @DisplayName("isHealthy() deve retornar false após shutdown")
    void isHealthyShouldReturnFalseAfterShutdown() {
        eventBus.shutdown(5);
        assertThat(eventBus.isHealthy()).isFalse();
    }

    // =========================================================================
    // getStatistics
    // =========================================================================

    @Test
    @DisplayName("getStatistics() deve retornar objeto não nulo")
    void getStatisticsShouldReturnNonNull() {
        assertThat(eventBus.getStatistics()).isNotNull();
        assertThat(eventBus.getStatistics()).isInstanceOf(EventBusStatistics.class);
    }

    // =========================================================================
    // registerHandler / unregisterHandler / hasHandlers
    // =========================================================================

    @Nested
    @DisplayName("registerHandler() e hasHandlers()")
    class RegistrarHandler {

        @Test
        @DisplayName("Deve registrar handler e retornar hasHandlers=true")
        void shouldRegisterHandlerAndReturnHasHandlersTrue() {
            EventHandler<TestEvent> handler = buildTestHandler();
            eventBus.registerHandler(TestEvent.class, handler);

            assertThat(eventBus.hasHandlers(TestEvent.class)).isTrue();
        }

        @Test
        @DisplayName("hasHandlers deve retornar false para evento sem handler")
        void hasHandlersShouldReturnFalseForEventWithNoHandler() {
            assertThat(eventBus.hasHandlers(TestEvent.class)).isFalse();
        }

        @Test
        @DisplayName("Deve remover handler com unregisterHandler")
        void shouldRemoveHandlerWithUnregisterHandler() {
            EventHandler<TestEvent> handler = buildTestHandler();
            eventBus.registerHandler(TestEvent.class, handler);
            eventBus.unregisterHandler(TestEvent.class, handler);

            assertThat(eventBus.hasHandlers(TestEvent.class)).isFalse();
        }
    }

    // =========================================================================
    // publish
    // =========================================================================

    @Nested
    @DisplayName("publish()")
    class Publish {

        @Test
        @DisplayName("Deve publicar evento sem lançar exceção")
        void shouldPublishEventWithoutException() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);

            assertThatCode(() -> eventBus.publish(event)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve atualizar estatísticas após publish")
        void shouldUpdateStatisticsAfterPublish() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);

            eventBus.publish(event);

            assertThat(eventBus.getStatistics().getEventsPublished()).isGreaterThanOrEqualTo(1L);
        }
    }

    // =========================================================================
    // publishAsync
    // =========================================================================

    @Test
    @DisplayName("publishAsync() deve retornar CompletableFuture não nulo")
    void publishAsyncShouldReturnNonNullFuture() {
        TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);

        CompletableFuture<Void> future = eventBus.publishAsync(event);

        assertThat(future).isNotNull();
    }

    // =========================================================================
    // publishBatch
    // =========================================================================

    @Test
    @DisplayName("publishBatch() deve publicar múltiplos eventos sem exceção")
    void publishBatchShouldPublishMultipleEventsWithoutException() {
        List<DomainEvent> events = List.of(
                new TestEvent("agg-1", "msg1", "cat", 1),
                new TestEvent("agg-2", "msg2", "cat", 1)
        );

        assertThatCode(() -> eventBus.publishBatch(events)).doesNotThrowAnyException();
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private EventHandler<TestEvent> buildTestHandler() {
        return new EventHandler<TestEvent>() {
            @Override
            public void handle(TestEvent event) {}

            @Override
            public Class<TestEvent> getEventType() {
                return TestEvent.class;
            }
        };
    }
}
