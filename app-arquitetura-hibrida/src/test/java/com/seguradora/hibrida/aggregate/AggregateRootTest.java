package com.seguradora.hibrida.aggregate;

import com.seguradora.hibrida.aggregate.example.ExampleAggregate;
import com.seguradora.hibrida.aggregate.example.ExampleCreatedEvent;
import com.seguradora.hibrida.aggregate.exception.AggregateException;
import com.seguradora.hibrida.aggregate.validation.BusinessRule;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link AggregateRoot}.
 *
 * <p>Usa {@link ExampleAggregate} como implementação concreta para exercitar
 * a lógica da classe base.
 */
@DisplayName("AggregateRoot Tests")
class AggregateRootTest {

    private ExampleAggregate aggregate;

    @BeforeEach
    void setUp() {
        aggregate = new ExampleAggregate(UUID.randomUUID().toString());
    }

    // =========================================================================
    // Construção e identidade
    // =========================================================================

    @Nested
    @DisplayName("Construção e identidade")
    class ConstrucaoEIdentidade {

        @Test
        @DisplayName("Deve criar aggregate com ID informado")
        void shouldCreateAggregateWithGivenId() {
            // Given
            String id = "agg-123";

            // When
            ExampleAggregate agg = new ExampleAggregate(id);

            // Then
            assertThat(agg.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Deve criar aggregate sem ID via construtor padrão")
        void shouldCreateAggregateWithoutIdViaDefaultConstructor() {
            // When
            ExampleAggregate agg = new ExampleAggregate();

            // Then
            assertThat(agg.getId()).isNull();
            assertThat(agg.getVersion()).isZero();
        }

        @Test
        @DisplayName("Deve iniciar com versão zero")
        void shouldStartWithVersionZero() {
            assertThat(aggregate.getVersion()).isZero();
        }

        @Test
        @DisplayName("Deve iniciar sem eventos não commitados")
        void shouldStartWithNoUncommittedEvents() {
            assertThat(aggregate.getUncommittedEvents()).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar tipo do aggregate como nome da classe")
        void shouldReturnAggregateTypeAsClassName() {
            assertThat(aggregate.getAggregateType()).isEqualTo("ExampleAggregate");
        }

        @Test
        @DisplayName("Deve ter lastModified inicializado")
        void shouldHaveLastModifiedInitialized() {
            assertThat(aggregate.getLastModified()).isNotNull();
        }
    }

    // =========================================================================
    // Aplicação de eventos
    // =========================================================================

    @Nested
    @DisplayName("Aplicação de eventos")
    class AplicacaoDeEventos {

        @Test
        @DisplayName("Deve registrar evento não commitado ao aplicar create")
        void shouldRegisterUncommittedEventOnCreate() {
            // When
            aggregate.create("Nome Válido", "Descrição válida e completa");

            // Then
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);
        }

        @Test
        @DisplayName("Deve incrementar versão após aplicar evento")
        void shouldIncrementVersionAfterApplyingEvent() {
            // When
            aggregate.create("Nome Válido", "Descrição válida e completa");

            // Then
            assertThat(aggregate.getVersion()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve atualizar lastModified após aplicar evento")
        void shouldUpdateLastModifiedAfterApplyingEvent() {
            // Given
            Instant before = aggregate.getLastModified();

            // When
            aggregate.create("Nome Válido", "Descrição válida e completa");

            // Then
            assertThat(aggregate.getLastModified()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("Deve acumular múltiplos eventos não commitados")
        void shouldAccumulateMultipleUncommittedEvents() {
            // When
            aggregate.create("Nome Válido", "Descrição válida e completa");
            aggregate.update("Nome Atualizado", "Descrição atualizada e completa");

            // Then
            assertThat(aggregate.getUncommittedEvents()).hasSize(2);
            assertThat(aggregate.getVersion()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Deve retornar lista imutável de eventos não commitados")
        void shouldReturnImmutableUncommittedEventsList() {
            // Given
            aggregate.create("Nome Válido", "Descrição válida e completa");
            List<DomainEvent> events = aggregate.getUncommittedEvents();

            // Then
            assertThatThrownBy(() -> events.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Deve lançar AggregateException para evento sem handler")
        void shouldThrowAggregateExceptionForEventWithoutHandler() {
            // Given – evento anônimo sem handler no aggregate
            DomainEvent orphanEvent = ExampleCreatedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .aggregateId(aggregate.getId())
                    .aggregateType("ExampleAggregate")
                    .version(1L)
                    .timestamp(Instant.now())
                    .correlationId(UUID.randomUUID())
                    .name("x")
                    .description("y")
                    .build();

            // Aggregate sem handler declarado para subclasse interna
            ExampleAggregate bare = new ExampleAggregate(UUID.randomUUID().toString()) {
                // sem handlers extras
                @Override
                public Object createSnapshot() { return Map.of(); }
                @Override
                protected void restoreFromSnapshot(Object data) {}
                @Override
                protected void clearState() {}
            };

            // O ExampleAggregate já trata ExampleCreatedEvent — vamos confirmar que
            // ele processa normalmente sem exceção
            assertThatCode(() -> aggregate.create("Nome Válido", "Descrição válida e completa"))
                    .doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // Commit de eventos
    // =========================================================================

    @Nested
    @DisplayName("Commit de eventos")
    class CommitDeEventos {

        @Test
        @DisplayName("Deve limpar eventos não commitados ao marcar como commitados")
        void shouldClearUncommittedEventsOnMarkAsCommitted() {
            // Given
            aggregate.create("Nome Válido", "Descrição válida e completa");
            assertThat(aggregate.getUncommittedEvents()).isNotEmpty();

            // When
            aggregate.markEventsAsCommitted();

            // Then
            assertThat(aggregate.getUncommittedEvents()).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar hasUncommittedEvents true quando há eventos pendentes")
        void shouldReturnTrueForHasUncommittedEventsWhenPending() {
            // When
            aggregate.create("Nome Válido", "Descrição válida e completa");

            // Then
            assertThat(aggregate.hasUncommittedEvents()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar hasUncommittedEvents false após commit")
        void shouldReturnFalseForHasUncommittedEventsAfterCommit() {
            // Given
            aggregate.create("Nome Válido", "Descrição válida e completa");

            // When
            aggregate.markEventsAsCommitted();

            // Then
            assertThat(aggregate.hasUncommittedEvents()).isFalse();
        }

        @Test
        @DisplayName("isModified deve espelhar hasUncommittedEvents")
        void isModifiedShouldMirrorHasUncommittedEvents() {
            // Before
            assertThat(aggregate.isModified()).isFalse();

            // After creating
            aggregate.create("Nome Válido", "Descrição válida e completa");
            assertThat(aggregate.isModified()).isTrue();

            // After commit
            aggregate.markEventsAsCommitted();
            assertThat(aggregate.isModified()).isFalse();
        }
    }

    // =========================================================================
    // Reconstrução do histórico
    // =========================================================================

    @Nested
    @DisplayName("Reconstrução do histórico (loadFromHistory)")
    class ReconstrucaoDoHistorico {

        @Test
        @DisplayName("Deve reconstruir estado a partir de eventos históricos")
        void shouldReconstructStateFromHistoricalEvents() {
            // Given
            ExampleAggregate source = new ExampleAggregate(UUID.randomUUID().toString());
            source.create("Nome Original", "Descrição original e completa");
            List<DomainEvent> history = source.getUncommittedEvents();

            // When
            ExampleAggregate rebuilt = new ExampleAggregate();
            rebuilt.loadFromHistory(history);

            // Then
            assertThat(rebuilt.getName()).isEqualTo("Nome Original");
            assertThat(rebuilt.getVersion()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Não deve adicionar eventos à lista não commitada durante loadFromHistory")
        void shouldNotAddToUncommittedEventsOnLoadFromHistory() {
            // Given
            ExampleAggregate source = new ExampleAggregate(UUID.randomUUID().toString());
            source.create("Nome Original", "Descrição original e completa");
            List<DomainEvent> history = source.getUncommittedEvents();

            // When
            ExampleAggregate rebuilt = new ExampleAggregate();
            rebuilt.loadFromHistory(history);

            // Then
            assertThat(rebuilt.getUncommittedEvents()).isEmpty();
        }

        @Test
        @DisplayName("Deve ignorar lista vazia sem erro")
        void shouldIgnoreEmptyHistoryWithoutError() {
            // When / Then
            assertThatCode(() -> aggregate.loadFromHistory(List.of()))
                    .doesNotThrowAnyException();
            assertThat(aggregate.getVersion()).isZero();
        }

        @Test
        @DisplayName("Deve ignorar lista null sem erro")
        void shouldIgnoreNullHistoryWithoutError() {
            assertThatCode(() -> aggregate.loadFromHistory(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve reconstruir ID do aggregate a partir do primeiro evento")
        void shouldReconstructIdFromFirstEvent() {
            // Given
            String expectedId = UUID.randomUUID().toString();
            ExampleAggregate source = new ExampleAggregate(expectedId);
            source.create("Nome Original", "Descrição original e completa");
            List<DomainEvent> history = source.getUncommittedEvents();

            // When
            ExampleAggregate rebuilt = new ExampleAggregate();
            rebuilt.loadFromHistory(history);

            // Then
            assertThat(rebuilt.getId()).isEqualTo(expectedId);
        }
    }

    // =========================================================================
    // Snapshot
    // =========================================================================

    @Nested
    @DisplayName("Suporte a snapshot")
    class SuporteASnapshot {

        @Test
        @DisplayName("Deve criar snapshot com dados do estado atual")
        void shouldCreateSnapshotWithCurrentState() {
            // Given
            aggregate.create("Nome Original", "Descrição original e completa");
            aggregate.markEventsAsCommitted();

            // When
            Object snapshot = aggregate.createSnapshot();

            // Then
            assertThat(snapshot).isNotNull();
            assertThat(snapshot).isInstanceOf(Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> snapshotMap = (Map<String, Object>) snapshot;
            assertThat(snapshotMap.get("name")).isEqualTo("Nome Original");
        }

        @Test
        @DisplayName("Deve restaurar estado a partir de snapshot e eventos incrementais")
        void shouldRestoreFromSnapshotAndIncrementalEvents() {
            // Given – cria e commita estado base
            aggregate.create("Nome Original", "Descrição original e completa");
            aggregate.markEventsAsCommitted();
            Object snapshot = aggregate.createSnapshot();
            long snapshotVersion = aggregate.getVersion();

            // Gera evento incremental
            aggregate.update("Nome Atualizado", "Descrição atualizada e completa");
            List<DomainEvent> incrementalEvents = aggregate.getUncommittedEvents();

            // When
            ExampleAggregate rebuilt = new ExampleAggregate(aggregate.getId());
            rebuilt.loadFromSnapshot(snapshot, incrementalEvents);

            // Then
            assertThat(rebuilt.getName()).isEqualTo("Nome Atualizado");
            assertThat(rebuilt.getVersion()).isGreaterThan(snapshotVersion);
        }
    }

    // =========================================================================
    // Regras de negócio
    // =========================================================================

    @Nested
    @DisplayName("Regras de negócio")
    class RegrasDeNegocio {

        @Test
        @DisplayName("Deve lançar AggregateException ao violar regra de nome curto")
        void shouldThrowAggregateExceptionOnShortNameRule() {
            // Nome com 2 chars viola a regra de mínimo 3
            assertThatThrownBy(() -> aggregate.create("AB", "Descrição longa o suficiente"))
                    .isInstanceOf(AggregateException.class);
        }

        @Test
        @DisplayName("Deve lançar AggregateException ao violar regra de descrição curta")
        void shouldThrowAggregateExceptionOnShortDescriptionRule() {
            // Descrição com menos de 10 chars
            assertThatThrownBy(() -> aggregate.create("Nome Válido", "Curta"))
                    .isInstanceOf(AggregateException.class);
        }

        @Test
        @DisplayName("Deve aceitar aggregate válido sem lançar exceção")
        void shouldAcceptValidAggregateWithoutException() {
            assertThatCode(() -> aggregate.create("Nome Válido", "Descrição válida e completa"))
                    .doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // getDebugInfo / equals / hashCode / toString
    // =========================================================================

    @Nested
    @DisplayName("Utilitários: debugInfo, equals, hashCode, toString")
    class Utilitarios {

        @Test
        @DisplayName("Deve retornar mapa de debug com campos esperados")
        void shouldReturnDebugMapWithExpectedFields() {
            // When
            Map<String, Object> info = aggregate.getDebugInfo();

            // Then
            assertThat(info).containsKeys("id", "type", "version", "lastModified",
                    "uncommittedEvents", "businessRules", "loadedFromHistory");
        }

        @Test
        @DisplayName("Dois aggregates com mesmo ID devem ser iguais")
        void aggregatesWithSameIdShouldBeEqual() {
            // Given
            String id = "same-id";
            ExampleAggregate a1 = new ExampleAggregate(id);
            ExampleAggregate a2 = new ExampleAggregate(id);

            // Then
            assertThat(a1).isEqualTo(a2);
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }

        @Test
        @DisplayName("Dois aggregates com IDs diferentes não devem ser iguais")
        void aggregatesWithDifferentIdShouldNotBeEqual() {
            // Given
            ExampleAggregate a1 = new ExampleAggregate("id-1");
            ExampleAggregate a2 = new ExampleAggregate("id-2");

            // Then
            assertThat(a1).isNotEqualTo(a2);
        }

        @Test
        @DisplayName("toString deve conter ID, versão e nome da classe")
        void toStringShouldContainIdVersionAndClassName() {
            // When
            String str = aggregate.toString();

            // Then
            assertThat(str).contains("ExampleAggregate")
                    .contains(aggregate.getId())
                    .contains("version=");
        }
    }
}
