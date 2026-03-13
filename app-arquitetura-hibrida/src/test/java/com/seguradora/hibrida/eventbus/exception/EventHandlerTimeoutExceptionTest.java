package com.seguradora.hibrida.eventbus.exception;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("EventHandlerTimeoutException Tests")
class EventHandlerTimeoutExceptionTest {

    @Test
    @DisplayName("Deve criar com timeout completo")
    void shouldCreateWithFullTimeout() {
        String message = "Handler timeout";
        DomainEvent event = mock(DomainEvent.class);
        when(event.getAggregateId()).thenReturn("AGG-123");
        String handlerClass = "TestEventHandler";
        int timeoutSeconds = 30;
        long actualTimeMs = 35000;

        EventHandlerTimeoutException exception = new EventHandlerTimeoutException(
            message, event, handlerClass, timeoutSeconds, actualTimeMs
        );

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getEvent()).isEqualTo(event);
        assertThat(exception.getHandlerClass()).isEqualTo(handlerClass);
        assertThat(exception.getTimeoutSeconds()).isEqualTo(timeoutSeconds);
        assertThat(exception.getActualTimeMs()).isEqualTo(actualTimeMs);
        assertThat(exception.getActualTimeSeconds()).isCloseTo(35.0, within(0.01));
        assertThat(exception.isRetryable()).isTrue();
    }

    @Test
    @DisplayName("Deve calcular tempo em segundos corretamente")
    void shouldCalculateTimeInSeconds() {
        DomainEvent event = mock(DomainEvent.class);
        long actualTimeMs = 5500;

        EventHandlerTimeoutException exception = new EventHandlerTimeoutException(
            "Timeout", event, "Handler", 5, actualTimeMs
        );

        assertThat(exception.getActualTimeSeconds()).isCloseTo(5.5, within(0.01));
    }

    @Test
    @DisplayName("Deve ser uma EventHandlingException")
    void shouldBeAnEventHandlingException() {
        DomainEvent event = mock(DomainEvent.class);
        EventHandlerTimeoutException exception = new EventHandlerTimeoutException(
            "Test", event, "TestHandler", 10, 15000
        );
        assertThat(exception).isInstanceOf(EventHandlingException.class);
    }
}
