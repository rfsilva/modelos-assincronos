package com.seguradora.hibrida.eventbus.exception;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("EventHandlingException Tests")
class EventHandlingExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem, evento e handler")
    void shouldCreateWithMessageEventAndHandler() {
        String message = "Handling error";
        DomainEvent event = mock(DomainEvent.class);
        when(event.getAggregateId()).thenReturn("agg-123");
        String handlerClass = "TestHandler";
        boolean retryable = true;

        EventHandlingException exception = new EventHandlingException(message, event, handlerClass, retryable);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getEvent()).isEqualTo(event);
        assertThat(exception.getHandlerClass()).isEqualTo(handlerClass);
        assertThat(exception.isRetryable()).isTrue();
        assertThat(exception.getAggregateId()).isEqualTo("agg-123");
    }

    @Test
    @DisplayName("Deve criar com mensagem, evento, handler e causa")
    void shouldCreateWithMessageEventHandlerAndCause() {
        String message = "Handling error";
        DomainEvent event = mock(DomainEvent.class);
        String handlerClass = "TestHandler";
        boolean retryable = false;
        Throwable cause = new RuntimeException("Cause");

        EventHandlingException exception = new EventHandlingException(message, event, handlerClass, retryable, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.isRetryable()).isFalse();
    }

    @Test
    @DisplayName("Deve ser uma EventBusException")
    void shouldBeAnEventBusException() {
        DomainEvent event = mock(DomainEvent.class);
        EventHandlingException exception = new EventHandlingException("Test", event, "Handler", true);
        assertThat(exception).isInstanceOf(EventBusException.class);
    }

    @Test
    @DisplayName("Deve obter tipo do evento")
    void shouldGetEventType() {
        DomainEvent event = mock(DomainEvent.class);
        EventHandlingException exception = new EventHandlingException("Test", event, "Handler", true);

        String eventType = exception.getEventType();
        assertThat(eventType).isNotNull();
    }
}
