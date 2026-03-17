package com.seguradora.hibrida.eventstore.impl;

import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.eventstore.serialization.EventSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link PostgreSQLEventStore}.
 *
 * <p>Testes de comportamento completos requerem banco de dados.
 * Esta classe verifica a estrutura e anotações do componente.
 */
@DisplayName("PostgreSQLEventStore Tests")
class PostgreSQLEventStoreTest {

    @Test
    @DisplayName("Deve estar anotado com @Service")
    void shouldBeAnnotatedWithService() {
        assertThat(PostgreSQLEventStore.class.isAnnotationPresent(Service.class)).isTrue();
    }

    @Test
    @DisplayName("Deve implementar EventStore")
    void shouldImplementEventStore() {
        assertThat(EventStore.class.isAssignableFrom(PostgreSQLEventStore.class)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar dependências corretas no construtor")
    void shouldAcceptCorrectDependenciesInConstructor() throws NoSuchMethodException {
        assertThat(PostgreSQLEventStore.class.getConstructor(
                EventStoreRepository.class, EventSerializer.class))
                .isNotNull();
    }

    @Test
    @DisplayName("saveEvents deve estar anotado com @Transactional")
    void saveEventsShouldBeAnnotatedWithTransactional() throws NoSuchMethodException {
        Method m = PostgreSQLEventStore.class.getMethod("saveEvents",
                String.class, java.util.List.class, long.class);
        assertThat(m.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    @DisplayName("loadEvents(String) deve estar anotado com @Transactional readOnly")
    void loadEventsShouldBeAnnotatedWithTransactionalReadOnly() throws NoSuchMethodException {
        Method m = PostgreSQLEventStore.class.getMethod("loadEvents", String.class);
        Transactional tx = m.getAnnotation(Transactional.class);
        assertThat(tx).isNotNull();
        assertThat(tx.readOnly()).isTrue();
    }

    @Test
    @DisplayName("Deve declarar método aggregateExists")
    void shouldDeclareAggregateExistsMethod() throws NoSuchMethodException {
        assertThat(PostgreSQLEventStore.class.getMethod("aggregateExists", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método getCurrentVersion")
    void shouldDeclareGetCurrentVersionMethod() throws NoSuchMethodException {
        assertThat(PostgreSQLEventStore.class.getMethod("getCurrentVersion", String.class))
                .isNotNull();
    }
}
