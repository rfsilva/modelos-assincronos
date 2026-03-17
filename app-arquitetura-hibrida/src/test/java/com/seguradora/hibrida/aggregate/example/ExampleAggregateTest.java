package com.seguradora.hibrida.aggregate.example;

import com.seguradora.hibrida.aggregate.exception.AggregateException;
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
 * Testes unitários para {@link ExampleAggregate}.
 */
@DisplayName("ExampleAggregate Tests")
class ExampleAggregateTest {

    private ExampleAggregate aggregate;

    @BeforeEach
    void setUp() {
        aggregate = new ExampleAggregate(UUID.randomUUID().toString());
    }

    // =========================================================================
    // create()
    // =========================================================================

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Deve criar aggregate com nome e descrição válidos")
        void shouldCreateAggregateWithValidNameAndDescription() {
            // When
            aggregate.create("Nome Válido", "Descrição válida e longa");

            // Then
            assertThat(aggregate.getName()).isEqualTo("Nome Válido");
            assertThat(aggregate.getDescription()).isEqualTo("Descrição válida e longa");
            assertThat(aggregate.getStatus()).isEqualTo(ExampleAggregate.ExampleStatus.ACTIVE);
        }

        @Test
        @DisplayName("Deve gerar evento ExampleCreatedEvent ao criar")
        void shouldGenerateCreatedEventOnCreate() {
            // When
            aggregate.create("Nome Válido", "Descrição válida e longa");

            // Then
            List<DomainEvent> events = aggregate.getUncommittedEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(ExampleCreatedEvent.class);
        }

        @Test
        @DisplayName("Deve definir createdAt ao criar")
        void shouldSetCreatedAtOnCreate() {
            // When
            aggregate.create("Nome Válido", "Descrição válida e longa");

            // Then
            assertThat(aggregate.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para nome null")
        void shouldThrowIllegalArgumentExceptionForNullName() {
            assertThatThrownBy(() -> aggregate.create(null, "Descrição válida e longa"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome");
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para nome em branco")
        void shouldThrowIllegalArgumentExceptionForBlankName() {
            assertThatThrownBy(() -> aggregate.create("  ", "Descrição válida e longa"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para descrição null")
        void shouldThrowIllegalArgumentExceptionForNullDescription() {
            assertThatThrownBy(() -> aggregate.create("Nome Válido", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Descrição");
        }

        @Test
        @DisplayName("Deve lançar AggregateException ao violar regra de nome curto (< 3 chars)")
        void shouldThrowAggregateExceptionOnShortName() {
            assertThatThrownBy(() -> aggregate.create("AB", "Descrição válida e longa"))
                    .isInstanceOf(AggregateException.class);
        }

        @Test
        @DisplayName("Deve lançar AggregateException ao violar regra de descrição curta (< 10 chars)")
        void shouldThrowAggregateExceptionOnShortDescription() {
            assertThatThrownBy(() -> aggregate.create("Nome Válido", "Curta"))
                    .isInstanceOf(AggregateException.class);
        }
    }

    // =========================================================================
    // update()
    // =========================================================================

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @BeforeEach
        void criar() {
            aggregate.create("Nome Original", "Descrição original e longa");
            aggregate.markEventsAsCommitted();
        }

        @Test
        @DisplayName("Deve atualizar nome e descrição quando há mudança")
        void shouldUpdateNameAndDescriptionWhenChanged() {
            // When
            aggregate.update("Nome Novo", "Descrição nova e suficientemente longa");

            // Then
            assertThat(aggregate.getName()).isEqualTo("Nome Novo");
            assertThat(aggregate.getDescription()).isEqualTo("Descrição nova e suficientemente longa");
        }

        @Test
        @DisplayName("Deve gerar ExampleUpdatedEvent ao atualizar")
        void shouldGenerateUpdatedEventOnUpdate() {
            // When
            aggregate.update("Nome Novo", "Descrição nova e suficientemente longa");

            // Then
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);
            assertThat(aggregate.getUncommittedEvents().get(0))
                    .isInstanceOf(ExampleUpdatedEvent.class);
        }

        @Test
        @DisplayName("Não deve gerar evento quando nome e descrição são iguais")
        void shouldNotGenerateEventWhenNothingChanged() {
            // When
            aggregate.update("Nome Original", "Descrição original e longa");

            // Then
            assertThat(aggregate.getUncommittedEvents()).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar IllegalStateException ao atualizar aggregate inativo")
        void shouldThrowIllegalStateExceptionWhenUpdatingInactiveAggregate() {
            // Given
            aggregate.deactivate();
            aggregate.markEventsAsCommitted();

            // Then
            assertThatThrownBy(() -> aggregate.update("Novo Nome", "Nova descrição suficientemente longa"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para novo nome null")
        void shouldThrowIllegalArgumentExceptionForNullNewName() {
            assertThatThrownBy(() -> aggregate.update(null, "Descrição nova"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // =========================================================================
    // activate() / deactivate()
    // =========================================================================

    @Nested
    @DisplayName("activate() e deactivate()")
    class ActivateDeactivateTests {

        @BeforeEach
        void criar() {
            aggregate.create("Nome Original", "Descrição original e longa");
            aggregate.markEventsAsCommitted();
        }

        @Test
        @DisplayName("Deve gerar ExampleActivatedEvent ao ativar aggregate inativo")
        void shouldGenerateActivatedEventOnActivate() {
            // Given – desativar primeiro
            aggregate.deactivate();
            aggregate.markEventsAsCommitted();

            // When
            aggregate.activate();

            // Then
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);
            assertThat(aggregate.getUncommittedEvents().get(0))
                    .isInstanceOf(ExampleActivatedEvent.class);
            assertThat(aggregate.getStatus())
                    .isEqualTo(ExampleAggregate.ExampleStatus.ACTIVE);
        }

        @Test
        @DisplayName("Não deve gerar evento ao ativar aggregate já ativo")
        void shouldNotGenerateEventWhenAlreadyActive() {
            // Aggregate está ACTIVE após create
            aggregate.activate();

            assertThat(aggregate.getUncommittedEvents()).isEmpty();
        }

        @Test
        @DisplayName("Deve gerar ExampleDeactivatedEvent ao desativar aggregate ativo")
        void shouldGenerateDeactivatedEventOnDeactivate() {
            // When
            aggregate.deactivate();

            // Then
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);
            assertThat(aggregate.getUncommittedEvents().get(0))
                    .isInstanceOf(ExampleDeactivatedEvent.class);
            assertThat(aggregate.getStatus())
                    .isEqualTo(ExampleAggregate.ExampleStatus.INACTIVE);
        }

        @Test
        @DisplayName("Não deve gerar evento ao desativar aggregate já inativo")
        void shouldNotGenerateEventWhenAlreadyInactive() {
            // Given
            aggregate.deactivate();
            aggregate.markEventsAsCommitted();

            // When
            aggregate.deactivate();

            assertThat(aggregate.getUncommittedEvents()).isEmpty();
        }
    }

    // =========================================================================
    // addMetadata()
    // =========================================================================

    @Nested
    @DisplayName("addMetadata()")
    class AddMetadataTests {

        @Test
        @DisplayName("Deve adicionar chave-valor ao metadata")
        void shouldAddKeyValueToMetadata() {
            aggregate.addMetadata("chave", "valor");

            assertThat(aggregate.getMetadata()).containsEntry("chave", "valor");
        }

        @Test
        @DisplayName("Deve ignorar chave null")
        void shouldIgnoreNullKey() {
            assertThatCode(() -> aggregate.addMetadata(null, "valor"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve ignorar chave em branco")
        void shouldIgnoreBlankKey() {
            assertThatCode(() -> aggregate.addMetadata("  ", "valor"))
                    .doesNotThrowAnyException();
            assertThat(aggregate.getMetadata()).doesNotContainKey("  ");
        }
    }

    // =========================================================================
    // Snapshot
    // =========================================================================

    @Nested
    @DisplayName("Snapshot: createSnapshot e restoreFromSnapshot")
    class SnapshotTests {

        @BeforeEach
        void criar() {
            aggregate.create("Nome Original", "Descrição original e longa");
            aggregate.markEventsAsCommitted();
        }

        @Test
        @DisplayName("Deve criar snapshot com estado atual")
        void shouldCreateSnapshotWithCurrentState() {
            // When
            Object snapshot = aggregate.createSnapshot();

            // Then
            assertThat(snapshot).isInstanceOf(Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) snapshot;
            assertThat(map.get("name")).isEqualTo("Nome Original");
            assertThat(map.get("status")).isEqualTo("ACTIVE");
            assertThat(map.get("version")).isEqualTo(aggregate.getVersion());
        }

        @Test
        @DisplayName("Deve restaurar estado a partir de snapshot")
        void shouldRestoreStateFromSnapshot() {
            // Given
            Object snapshot = aggregate.createSnapshot();

            // When
            ExampleAggregate rebuilt = new ExampleAggregate(aggregate.getId());
            rebuilt.loadFromSnapshot(snapshot, List.of());

            // Then
            assertThat(rebuilt.getName()).isEqualTo("Nome Original");
            assertThat(rebuilt.getStatus()).isEqualTo(ExampleAggregate.ExampleStatus.ACTIVE);
        }

        @Test
        @DisplayName("Deve lançar exceção para snapshot com tipo inválido")
        void shouldThrowExceptionForInvalidSnapshotType() {
            ExampleAggregate agg = new ExampleAggregate(UUID.randomUUID().toString());

            assertThatThrownBy(() -> agg.loadFromSnapshot("tipo_inválido", List.of()))
                    .isInstanceOf(Exception.class);
        }
    }

    // =========================================================================
    // clearState()
    // =========================================================================

    @Nested
    @DisplayName("clearState()")
    class ClearStateTests {

        @Test
        @DisplayName("Deve limpar estado ao reconstruir do histórico")
        void shouldClearStateOnLoadFromHistory() {
            // Given – criar e commitar
            aggregate.create("Nome Original", "Descrição original e longa");
            List<DomainEvent> events = aggregate.getUncommittedEvents();

            // When – reconstruir em novo aggregate
            ExampleAggregate rebuilt = new ExampleAggregate();
            rebuilt.loadFromHistory(events);

            // Then
            assertThat(rebuilt.getName()).isEqualTo("Nome Original");
            assertThat(rebuilt.getUncommittedEvents()).isEmpty();
        }
    }

    // =========================================================================
    // isActive / isRecentlyCreated / getAgeInDays
    // =========================================================================

    @Nested
    @DisplayName("Métodos utilitários")
    class MetodosUtilitarios {

        @BeforeEach
        void criar() {
            aggregate.create("Nome Original", "Descrição original e longa");
        }

        @Test
        @DisplayName("isActive deve retornar true para aggregate ACTIVE")
        void isActiveShouldReturnTrueForActiveAggregate() {
            assertThat(aggregate.isActive()).isTrue();
        }

        @Test
        @DisplayName("isActive deve retornar false após desativar")
        void isActiveShouldReturnFalseAfterDeactivation() {
            aggregate.markEventsAsCommitted();
            aggregate.deactivate();
            assertThat(aggregate.isActive()).isFalse();
        }

        @Test
        @DisplayName("isRecentlyCreated deve retornar true para aggregate recém criado")
        void isRecentlyCreatedShouldReturnTrueForNewlyCreatedAggregate() {
            assertThat(aggregate.isRecentlyCreated()).isTrue();
        }

        @Test
        @DisplayName("getAgeInDays deve retornar 0 para aggregate recém criado")
        void getAgeInDaysShouldReturnZeroForNewAggregate() {
            assertThat(aggregate.getAgeInDays()).isZero();
        }

        @Test
        @DisplayName("isRecentlyCreated deve retornar false quando createdAt é null")
        void isRecentlyCreatedShouldReturnFalseWhenCreatedAtIsNull() {
            ExampleAggregate agg = new ExampleAggregate("sem-create");
            assertThat(agg.isRecentlyCreated()).isFalse();
        }

        @Test
        @DisplayName("getAgeInDays deve retornar 0 quando createdAt é null")
        void getAgeInDaysShouldReturnZeroWhenCreatedAtIsNull() {
            ExampleAggregate agg = new ExampleAggregate("sem-create");
            assertThat(agg.getAgeInDays()).isZero();
        }

        @Test
        @DisplayName("toString deve conter ID, nome, status e versão")
        void toStringShouldContainIdNameStatusAndVersion() {
            String str = aggregate.toString();
            assertThat(str)
                    .contains("ExampleAggregate")
                    .contains(aggregate.getId())
                    .contains("ACTIVE");
        }
    }
}
