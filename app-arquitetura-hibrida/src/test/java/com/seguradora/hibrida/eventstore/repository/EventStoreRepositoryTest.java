package com.seguradora.hibrida.eventstore.repository;

import com.seguradora.hibrida.eventstore.entity.EventStoreEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de contrato para {@link EventStoreRepository}.
 */
@DisplayName("EventStoreRepository Tests")
class EventStoreRepositoryTest {

    @Test
    @DisplayName("Deve ser uma interface")
    void shouldBeInterface() {
        assertThat(EventStoreRepository.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @Repository")
    void shouldBeAnnotatedWithRepository() {
        assertThat(EventStoreRepository.class.isAnnotationPresent(Repository.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estender JpaRepository")
    void shouldExtendJpaRepository() {
        boolean extendsJpa = false;
        for (Class<?> iface : EventStoreRepository.class.getInterfaces()) {
            if (JpaRepository.class.isAssignableFrom(iface)) {
                extendsJpa = true;
                break;
            }
        }
        assertThat(extendsJpa).isTrue();
    }

    @Test
    @DisplayName("Deve declarar findByAggregateIdOrderByVersionAsc")
    void shouldDeclareFindByAggregateIdOrderByVersionAsc() throws NoSuchMethodException {
        assertThat(EventStoreRepository.class.getMethod(
                "findByAggregateIdOrderByVersionAsc", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar findByAggregateIdAndVersionGreaterThanEqualOrderByVersionAsc")
    void shouldDeclareFindByAggregateIdAndVersionGreaterThanEqual() throws NoSuchMethodException {
        assertThat(EventStoreRepository.class.getMethod(
                "findByAggregateIdAndVersionGreaterThanEqualOrderByVersionAsc",
                String.class, Long.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar findByEventTypeAndTimestampBetweenOrderByTimestampAsc")
    void shouldDeclareFindByEventTypeAndTimestampBetween() throws NoSuchMethodException {
        assertThat(EventStoreRepository.class.getMethod(
                "findByEventTypeAndTimestampBetweenOrderByTimestampAsc",
                String.class, Instant.class, Instant.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar findByCorrelationIdOrderByTimestampAsc")
    void shouldDeclareFindByCorrelationIdOrderByTimestampAsc() throws NoSuchMethodException {
        assertThat(EventStoreRepository.class.getMethod(
                "findByCorrelationIdOrderByTimestampAsc", UUID.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar existsByAggregateId")
    void shouldDeclareExistsByAggregateId() throws NoSuchMethodException {
        assertThat(EventStoreRepository.class.getMethod("existsByAggregateId", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar countByAggregateId")
    void shouldDeclareCountByAggregateId() throws NoSuchMethodException {
        assertThat(EventStoreRepository.class.getMethod("countByAggregateId", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("findByAggregateIdOrderByVersionAsc deve retornar List")
    void findByAggregateIdShouldReturnList() throws NoSuchMethodException {
        var method = EventStoreRepository.class.getMethod(
                "findByAggregateIdOrderByVersionAsc", String.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }
}
