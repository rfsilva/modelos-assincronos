package com.seguradora.hibrida.aggregate.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ExampleUpdatedEvent}.
 */
@DisplayName("ExampleUpdatedEvent Tests")
class ExampleUpdatedEventTest {

    @Nested
    @DisplayName("Criação via factory method")
    class CriacaoViaFactoryMethod {

        @Test
        @DisplayName("Deve criar evento com todos os campos preenchidos")
        void shouldCreateEventWithAllFieldsPopulated() {
            // Given
            String aggregateId = UUID.randomUUID().toString();
            long version = 2L;
            String newName = "Novo Nome";
            String newDescription = "Nova descrição";
            Instant ts = Instant.now();

            // When
            ExampleUpdatedEvent event = ExampleUpdatedEvent.create(
                    aggregateId, version, newName, newDescription, ts);

            // Then
            assertThat(event).isNotNull();
            assertThat(event.getAggregateId()).isEqualTo(aggregateId);
            assertThat(event.getVersion()).isEqualTo(version);
            assertThat(event.getNewName()).isEqualTo(newName);
            assertThat(event.getNewDescription()).isEqualTo(newDescription);
            assertThat(event.getUpdateTimestamp()).isEqualTo(ts);
        }

        @Test
        @DisplayName("Deve definir aggregateType como 'ExampleAggregate'")
        void shouldSetAggregateTypeAsExampleAggregate() {
            ExampleUpdatedEvent event = ExampleUpdatedEvent.create(
                    UUID.randomUUID().toString(), 1L, "Nome", "Desc", Instant.now());

            assertThat(event.getAggregateType()).isEqualTo("ExampleAggregate");
        }

        @Test
        @DisplayName("Deve gerar eventId único a cada chamada")
        void shouldGenerateUniqueEventIdOnEachCall() {
            ExampleUpdatedEvent e1 = ExampleUpdatedEvent.create(
                    UUID.randomUUID().toString(), 1L, "Nome", "Desc", Instant.now());
            ExampleUpdatedEvent e2 = ExampleUpdatedEvent.create(
                    UUID.randomUUID().toString(), 1L, "Nome", "Desc", Instant.now());

            assertThat(e1.getEventId()).isNotEqualTo(e2.getEventId());
        }

        @Test
        @DisplayName("Deve ter correlationId gerado")
        void shouldHaveGeneratedCorrelationId() {
            ExampleUpdatedEvent event = ExampleUpdatedEvent.create(
                    UUID.randomUUID().toString(), 1L, "Nome", "Desc", Instant.now());

            assertThat(event.getCorrelationId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("Deve conter aggregateId, newName e newDescription no toString")
        void shouldContainKeyFieldsInToString() {
            String id = UUID.randomUUID().toString();
            ExampleUpdatedEvent event = ExampleUpdatedEvent.create(
                    id, 2L, "NomeAtualizado", "DescricaoAtualizada", Instant.now());

            String str = event.toString();
            assertThat(str).contains(id).contains("NomeAtualizado").contains("DescricaoAtualizada");
        }
    }

    @Nested
    @DisplayName("Construtor padrão")
    class ConstrutorPadrao {

        @Test
        @DisplayName("Deve criar instância vazia sem lançar exceção")
        void shouldCreateEmptyInstanceWithoutException() {
            assertThat(new ExampleUpdatedEvent()).isNotNull();
        }
    }
}
