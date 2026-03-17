package com.seguradora.hibrida.aggregate.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ExampleCreatedEvent}.
 */
@DisplayName("ExampleCreatedEvent Tests")
class ExampleCreatedEventTest {

    @Nested
    @DisplayName("Criação via factory method")
    class CriacaoViaFactoryMethod {

        @Test
        @DisplayName("Deve criar evento com todos os campos preenchidos")
        void shouldCreateEventWithAllFieldsPopulated() {
            // Given
            String aggregateId = UUID.randomUUID().toString();
            String name = "Teste";
            String description = "Descrição de teste";
            Instant now = Instant.now();

            // When
            ExampleCreatedEvent event = ExampleCreatedEvent.create(aggregateId, name, description, now);

            // Then
            assertThat(event).isNotNull();
            assertThat(event.getAggregateId()).isEqualTo(aggregateId);
            assertThat(event.getName()).isEqualTo(name);
            assertThat(event.getDescription()).isEqualTo(description);
            assertThat(event.getCreationTimestamp()).isEqualTo(now);
        }

        @Test
        @DisplayName("Deve gerar eventId único a cada chamada")
        void shouldGenerateUniqueEventIdOnEachCall() {
            // When
            ExampleCreatedEvent e1 = ExampleCreatedEvent.create(
                    UUID.randomUUID().toString(), "Nome", "Descrição", Instant.now());
            ExampleCreatedEvent e2 = ExampleCreatedEvent.create(
                    UUID.randomUUID().toString(), "Nome", "Descrição", Instant.now());

            // Then
            assertThat(e1.getEventId()).isNotEqualTo(e2.getEventId());
        }

        @Test
        @DisplayName("Deve definir aggregateType como 'ExampleAggregate'")
        void shouldSetAggregateTypeAsExampleAggregate() {
            ExampleCreatedEvent event = ExampleCreatedEvent.create(
                    UUID.randomUUID().toString(), "Nome", "Descrição", Instant.now());

            assertThat(event.getAggregateType()).isEqualTo("ExampleAggregate");
        }

        @Test
        @DisplayName("Deve ter versão 1 no evento de criação")
        void shouldHaveVersionOneOnCreationEvent() {
            ExampleCreatedEvent event = ExampleCreatedEvent.create(
                    UUID.randomUUID().toString(), "Nome", "Descrição", Instant.now());

            assertThat(event.getVersion()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve ter correlationId gerado")
        void shouldHaveGeneratedCorrelationId() {
            ExampleCreatedEvent event = ExampleCreatedEvent.create(
                    UUID.randomUUID().toString(), "Nome", "Descrição", Instant.now());

            assertThat(event.getCorrelationId()).isNotNull();
        }

        @Test
        @DisplayName("Deve ter timestamp de evento preenchido")
        void shouldHaveEventTimestampFilled() {
            ExampleCreatedEvent event = ExampleCreatedEvent.create(
                    UUID.randomUUID().toString(), "Nome", "Descrição", Instant.now());

            assertThat(event.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("Deve conter aggregateId, name e description no toString")
        void shouldContainAggregateIdNameAndDescriptionInToString() {
            // Given
            String id = UUID.randomUUID().toString();
            ExampleCreatedEvent event = ExampleCreatedEvent.create(id, "MeuNome", "MinhaDesc", Instant.now());

            // Then
            String str = event.toString();
            assertThat(str).contains(id).contains("MeuNome").contains("MinhaDesc");
        }
    }

    @Nested
    @DisplayName("Construtor padrão (NoArgsConstructor)")
    class ConstrutorPadrao {

        @Test
        @DisplayName("Deve criar instância vazia sem lançar exceção")
        void shouldCreateEmptyInstanceWithoutException() {
            ExampleCreatedEvent event = new ExampleCreatedEvent();
            assertThat(event).isNotNull();
        }
    }
}
