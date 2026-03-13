package com.seguradora.hibrida.aggregate.exception;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventHandlerNotFoundException Tests")
class EventHandlerNotFoundExceptionTest {

    // Classe de teste para DomainEvent
    private static class TestDomainEvent extends DomainEvent {
        public TestDomainEvent() {
            super();
        }
    }

    @Test
    @DisplayName("Deve criar com eventType e aggregateType")
    void shouldCreateWithEventTypeAndAggregateType() {
        Class<? extends DomainEvent> eventType = TestDomainEvent.class;
        String aggregateType = "SinistroAggregate";

        EventHandlerNotFoundException exception = new EventHandlerNotFoundException(eventType, aggregateType);

        assertThat(exception.getMessage()).contains("TestDomainEvent");
        assertThat(exception.getMessage()).contains(aggregateType);
        assertThat(exception.getEventType()).isEqualTo(eventType);
        assertThat(exception.getEventTypeName()).isEqualTo("TestDomainEvent");
    }

    @Test
    @DisplayName("Deve criar com contexto completo")
    void shouldCreateWithFullContext() {
        Class<? extends DomainEvent> eventType = TestDomainEvent.class;
        String aggregateId = "AGG-456";
        String aggregateType = "ApoliceAggregate";
        Long version = 3L;

        EventHandlerNotFoundException exception = new EventHandlerNotFoundException(
            eventType, aggregateId, aggregateType, version
        );

        assertThat(exception.getMessage()).contains("TestDomainEvent");
        assertThat(exception.getMessage()).contains(aggregateType);
        assertThat(exception.getEventType()).isEqualTo(eventType);
        assertThat(exception.getAggregateId()).isEqualTo(aggregateId);
        assertThat(exception.getAggregateType()).isEqualTo(aggregateType);
        assertThat(exception.getVersion()).isEqualTo(version);
    }

    @Test
    @DisplayName("Deve criar com mensagem customizada")
    void shouldCreateWithCustomMessage() {
        String message = "Custom handler error";
        Class<? extends DomainEvent> eventType = TestDomainEvent.class;

        EventHandlerNotFoundException exception = new EventHandlerNotFoundException(message, eventType);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getEventType()).isEqualTo(eventType);
    }

    @Test
    @DisplayName("Deve ser uma AggregateException")
    void shouldBeAnAggregateException() {
        EventHandlerNotFoundException exception = new EventHandlerNotFoundException(
            TestDomainEvent.class, "TestAggregate"
        );
        assertThat(exception).isInstanceOf(AggregateException.class);
    }
}
