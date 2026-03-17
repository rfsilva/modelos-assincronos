package com.seguradora.hibrida.aggregate.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ExampleDeactivatedEvent}.
 */
@DisplayName("ExampleDeactivatedEvent Tests")
class ExampleDeactivatedEventTest {

    @Nested
    @DisplayName("Criação via factory method")
    class CriacaoViaFactoryMethod {

        @Test
        @DisplayName("Deve criar evento com todos os campos preenchidos")
        void shouldCreateEventWithAllFieldsPopulated() {
            // Given
            String aggregateId = UUID.randomUUID().toString();
            long version = 3L;
            Instant ts = Instant.now();

            // When
            ExampleDeactivatedEvent event = ExampleDeactivatedEvent.create(aggregateId, version, ts);

            // Then
            assertThat(event).isNotNull();
            assertThat(event.getAggregateId()).isEqualTo(aggregateId);
            assertThat(event.getVersion()).isEqualTo(version);
            assertThat(event.getDeactivationTimestamp()).isEqualTo(ts);
        }

        @Test
        @DisplayName("Deve definir aggregateType como 'ExampleAggregate'")
        void shouldSetAggregateTypeAsExampleAggregate() {
            ExampleDeactivatedEvent event = ExampleDeactivatedEvent.create(
                    UUID.randomUUID().toString(), 1L, Instant.now());

            assertThat(event.getAggregateType()).isEqualTo("ExampleAggregate");
        }

        @Test
        @DisplayName("Deve gerar eventId único a cada chamada")
        void shouldGenerateUniqueEventIdOnEachCall() {
            ExampleDeactivatedEvent e1 = ExampleDeactivatedEvent.create(
                    UUID.randomUUID().toString(), 1L, Instant.now());
            ExampleDeactivatedEvent e2 = ExampleDeactivatedEvent.create(
                    UUID.randomUUID().toString(), 1L, Instant.now());

            assertThat(e1.getEventId()).isNotEqualTo(e2.getEventId());
        }

        @Test
        @DisplayName("Deve ter correlationId gerado")
        void shouldHaveGeneratedCorrelationId() {
            ExampleDeactivatedEvent event = ExampleDeactivatedEvent.create(
                    UUID.randomUUID().toString(), 1L, Instant.now());

            assertThat(event.getCorrelationId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("Deve conter aggregateId e deactivationTimestamp no toString")
        void shouldContainAggregateIdAndDeactivationTimestampInToString() {
            String id = UUID.randomUUID().toString();
            Instant ts = Instant.now();
            ExampleDeactivatedEvent event = ExampleDeactivatedEvent.create(id, 1L, ts);

            String str = event.toString();
            assertThat(str).contains(id).contains(ts.toString());
        }
    }

    @Nested
    @DisplayName("Construtor padrão")
    class ConstrutorPadrao {

        @Test
        @DisplayName("Deve criar instância vazia sem lançar exceção")
        void shouldCreateEmptyInstanceWithoutException() {
            assertThat(new ExampleDeactivatedEvent()).isNotNull();
        }
    }
}
