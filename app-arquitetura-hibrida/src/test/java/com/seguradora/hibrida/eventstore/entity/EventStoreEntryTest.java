package com.seguradora.hibrida.eventstore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link EventStoreEntry}.
 */
@DisplayName("EventStoreEntry Tests")
class EventStoreEntryTest {

    // =========================================================================
    // Anotações JPA
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Entity")
    void shouldBeAnnotatedWithEntity() {
        assertThat(EventStoreEntry.class.isAnnotationPresent(Entity.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @Table")
    void shouldBeAnnotatedWithTable() {
        assertThat(EventStoreEntry.class.isAnnotationPresent(Table.class)).isTrue();
    }

    @Test
    @DisplayName("Tabela deve se chamar 'events'")
    void tableShouldBeNamedEvents() {
        Table table = EventStoreEntry.class.getAnnotation(Table.class);
        assertThat(table.name()).isEqualTo("events");
    }

    // =========================================================================
    // Builder
    // =========================================================================

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("Builder deve criar instância com campos corretos")
        void builderShouldCreateInstanceWithCorrectFields() {
            UUID id = UUID.randomUUID();
            UUID corrId = UUID.randomUUID();

            EventStoreEntry entry = EventStoreEntry.builder()
                    .id(id)
                    .aggregateId("agg-1")
                    .aggregateType("TestAggregate")
                    .eventType("TestEvent")
                    .version(1L)
                    .timestamp(Instant.now())
                    .correlationId(corrId)
                    .eventData("{}")
                    .build();

            assertThat(entry.getId()).isEqualTo(id);
            assertThat(entry.getAggregateId()).isEqualTo("agg-1");
            assertThat(entry.getAggregateType()).isEqualTo("TestAggregate");
            assertThat(entry.getEventType()).isEqualTo("TestEvent");
            assertThat(entry.getVersion()).isEqualTo(1L);
            assertThat(entry.getCorrelationId()).isEqualTo(corrId);
            assertThat(entry.getEventData()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Builder deve usar compressed=false por padrão")
        void builderShouldUseCompressedFalseByDefault() {
            EventStoreEntry entry = EventStoreEntry.builder()
                    .aggregateId("agg-1")
                    .build();

            assertThat(entry.getCompressed()).isFalse();
        }

        @Test
        @DisplayName("Builder deve usar dataSize=0 por padrão")
        void builderShouldUseDataSizeZeroByDefault() {
            EventStoreEntry entry = EventStoreEntry.builder()
                    .aggregateId("agg-1")
                    .build();

            assertThat(entry.getDataSize()).isEqualTo(0);
        }
    }
}
