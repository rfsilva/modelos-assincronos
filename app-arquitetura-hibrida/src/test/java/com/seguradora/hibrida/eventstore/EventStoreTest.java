package com.seguradora.hibrida.eventstore;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de contrato para {@link EventStore}.
 */
@DisplayName("EventStore Tests")
class EventStoreTest {

    @Test
    @DisplayName("Deve ser uma interface")
    void shouldBeInterface() {
        assertThat(EventStore.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Deve declarar método saveEvents")
    void shouldDeclareSaveEventsMethod() throws NoSuchMethodException {
        assertThat(EventStore.class.getMethod("saveEvents", String.class, List.class, long.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método loadEvents(String)")
    void shouldDeclareLoadEventsMethod() throws NoSuchMethodException {
        assertThat(EventStore.class.getMethod("loadEvents", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método loadEvents(String, long)")
    void shouldDeclareLoadEventsWithVersionMethod() throws NoSuchMethodException {
        assertThat(EventStore.class.getMethod("loadEvents", String.class, long.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método loadEventsByType")
    void shouldDeclareLoadEventsByTypeMethod() throws NoSuchMethodException {
        assertThat(EventStore.class.getMethod("loadEventsByType", String.class, Instant.class, Instant.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método loadEventsByCorrelationId")
    void shouldDeclareLoadEventsByCorrelationIdMethod() throws NoSuchMethodException {
        assertThat(EventStore.class.getMethod("loadEventsByCorrelationId", UUID.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método getCurrentVersion")
    void shouldDeclareGetCurrentVersionMethod() throws NoSuchMethodException {
        assertThat(EventStore.class.getMethod("getCurrentVersion", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método aggregateExists")
    void shouldDeclareAggregateExistsMethod() throws NoSuchMethodException {
        assertThat(EventStore.class.getMethod("aggregateExists", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("loadEvents deve retornar List")
    void loadEventsShouldReturnList() throws NoSuchMethodException {
        var method = EventStore.class.getMethod("loadEvents", String.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("getCurrentVersion deve retornar long")
    void getCurrentVersionShouldReturnLong() throws NoSuchMethodException {
        var method = EventStore.class.getMethod("getCurrentVersion", String.class);
        assertThat(method.getReturnType()).isEqualTo(long.class);
    }

    @Test
    @DisplayName("aggregateExists deve retornar boolean")
    void aggregateExistsShouldReturnBoolean() throws NoSuchMethodException {
        var method = EventStore.class.getMethod("aggregateExists", String.class);
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }
}
