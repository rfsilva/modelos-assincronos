package com.seguradora.hibrida.eventbus.exception;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("EventPublishingException Tests")
class EventPublishingExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem, evento e razão")
    void shouldCreateWithMessageEventAndReason() {
        String message = "Publishing error";
        DomainEvent event = mock(DomainEvent.class);
        when(event.getAggregateId()).thenReturn("AGG-123");
        String reason = "target-bus-unavailable";

        EventPublishingException exception = new EventPublishingException(message, event, reason);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getEvent()).isEqualTo(event);
        assertThat(exception.getReason()).isEqualTo(reason);
        assertThat(exception.getAggregateId()).isEqualTo("AGG-123");
    }

    @Test
    @DisplayName("Deve criar com mensagem, evento, razão e causa")
    void shouldCreateWithMessageEventReasonAndCause() {
        String message = "Publishing error";
        DomainEvent event = mock(DomainEvent.class);
        when(event.getAggregateId()).thenReturn("AGG-456");
        String reason = "network-timeout";
        Throwable cause = new RuntimeException("Connection timeout");

        EventPublishingException exception = new EventPublishingException(message, event, reason, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getEvent()).isEqualTo(event);
        assertThat(exception.getReason()).isEqualTo(reason);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getAggregateId()).isEqualTo("AGG-456");
    }

    @Test
    @DisplayName("Deve obter tipo do evento")
    void shouldGetEventType() {
        DomainEvent event = mock(DomainEvent.class);
        EventPublishingException exception = new EventPublishingException("Error", event, "reason");

        String eventType = exception.getEventType();

        assertThat(eventType).isNotNull();
    }

    @Test
    @DisplayName("Deve ser uma EventBusException")
    void shouldBeAnEventBusException() {
        DomainEvent event = mock(DomainEvent.class);
        EventPublishingException exception = new EventPublishingException("Test", event, "reason");
        assertThat(exception).isInstanceOf(EventBusException.class);
    }
}
