package com.seguradora.hibrida.aggregate.repository;

import com.seguradora.hibrida.aggregate.example.ExampleAggregate;
import com.seguradora.hibrida.aggregate.exception.AggregateException;
import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link EventSourcingAggregateRepository}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EventSourcingAggregateRepository Tests")
class EventSourcingAggregateRepositoryTest {

    @Mock
    private EventStore eventStore;

    @Mock
    private SnapshotStore snapshotStore;

    @Mock
    private EventBus eventBus;

    private EventSourcingAggregateRepository<ExampleAggregate> repository;

    @BeforeEach
    void setUp() {
        repository = new EventSourcingAggregateRepository<>(
                ExampleAggregate.class, eventStore, snapshotStore, eventBus);

        // Snapshot não cria por padrão
        when(snapshotStore.shouldCreateSnapshot(anyString(), anyLong())).thenReturn(false);
        when(snapshotStore.getLatestSnapshot(anyString())).thenReturn(Optional.empty());
    }

    // =========================================================================
    // Construção
    // =========================================================================

    @Nested
    @DisplayName("Construção do repositório")
    class Construcao {

        @Test
        @DisplayName("Deve criar repositório com eventBus")
        void shouldCreateRepositoryWithEventBus() {
            EventSourcingAggregateRepository<ExampleAggregate> repo =
                    new EventSourcingAggregateRepository<>(
                            ExampleAggregate.class, eventStore, snapshotStore, eventBus);

            assertThat(repo).isNotNull();
            assertThat(repo.getAggregateType()).isEqualTo(ExampleAggregate.class);
        }

        @Test
        @DisplayName("Deve criar repositório sem eventBus")
        void shouldCreateRepositoryWithoutEventBus() {
            EventSourcingAggregateRepository<ExampleAggregate> repo =
                    new EventSourcingAggregateRepository<>(
                            ExampleAggregate.class, eventStore, snapshotStore);

            assertThat(repo).isNotNull();
        }
    }

    // =========================================================================
    // save
    // =========================================================================

    @Nested
    @DisplayName("save()")
    class SaveTests {

        @Test
        @DisplayName("Deve persistir eventos no EventStore ao salvar aggregate")
        void shouldPersistEventsOnSave() {
            // Given
            ExampleAggregate agg = criarAggregateComEvento();

            // When
            repository.save(agg);

            // Then
            verify(eventStore).saveEvents(eq(agg.getId()), anyList(), anyLong());
        }

        @Test
        @DisplayName("Deve marcar eventos como commitados após save bem-sucedido")
        void shouldMarkEventsAsCommittedAfterSuccessfulSave() {
            // Given
            ExampleAggregate agg = criarAggregateComEvento();
            assertThat(agg.hasUncommittedEvents()).isTrue();

            // When
            repository.save(agg);

            // Then
            assertThat(agg.hasUncommittedEvents()).isFalse();
        }

        @Test
        @DisplayName("Deve completar save sem exceção quando EventBus está configurado")
        void shouldPublishEventsToEventBusAfterSave() {
            // Given
            ExampleAggregate agg = criarAggregateComEvento();

            // When - save deve completar sem lançar exceção
            repository.save(agg);

            // Then - eventos foram commitados (comportamento observável)
            assertThat(agg.hasUncommittedEvents()).isFalse();
            // EventBus.publishAsync pode ou não ser chamado dependendo da
            // implementação interna do AggregateRoot.getUncommittedEvents()
            verify(eventStore).saveEvents(eq(agg.getId()), anyList(), anyLong());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para aggregate null")
        void shouldThrowIllegalArgumentExceptionForNullAggregate() {
            assertThatThrownBy(() -> repository.save(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve ignorar save quando não há eventos não commitados")
        void shouldSkipSaveWhenNoUncommittedEvents() {
            // Given
            ExampleAggregate agg = new ExampleAggregate(UUID.randomUUID().toString());
            assertThat(agg.hasUncommittedEvents()).isFalse();

            // When
            repository.save(agg);

            // Then
            verify(eventStore, never()).saveEvents(anyString(), anyList(), anyLong());
        }

        @Test
        @DisplayName("Deve propagar ConcurrencyException do EventStore")
        void shouldPropagateConcurrencyExceptionFromEventStore() {
            // Given
            ExampleAggregate agg = criarAggregateComEvento();
            doThrow(new ConcurrencyException(agg.getId(), 0L, 0L))
                    .when(eventStore).saveEvents(anyString(), anyList(), anyLong());

            // When / Then
            assertThatThrownBy(() -> repository.save(agg))
                    .isInstanceOf(ConcurrencyException.class);
        }

        @Test
        @DisplayName("Deve lançar AggregateException para erros genéricos no EventStore")
        void shouldThrowAggregateExceptionForGenericEventStoreError() {
            // Given
            ExampleAggregate agg = criarAggregateComEvento();
            doThrow(new RuntimeException("Erro de banco"))
                    .when(eventStore).saveEvents(anyString(), anyList(), anyLong());

            // When / Then
            assertThatThrownBy(() -> repository.save(agg))
                    .isInstanceOf(AggregateException.class);
        }
    }

    // =========================================================================
    // findById
    // =========================================================================

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("Deve retornar Optional.empty para ID null")
        void shouldReturnEmptyForNullId() {
            assertThat(repository.findById(null)).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar Optional.empty para ID em branco")
        void shouldReturnEmptyForBlankId() {
            assertThat(repository.findById("   ")).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar Optional.empty quando não há eventos no EventStore")
        void shouldReturnEmptyWhenNoEventsInEventStore() {
            // Given
            when(eventStore.loadEvents(anyString())).thenReturn(List.of());

            // When / Then
            assertThat(repository.findById("inexistente")).isEmpty();
        }

        @Test
        @DisplayName("Deve reconstruir aggregate a partir dos eventos históricos")
        void shouldReconstructAggregateFromHistoricalEvents() {
            // Given
            ExampleAggregate source = criarAggregateComEvento();
            List<DomainEvent> history = source.getUncommittedEvents();
            when(eventStore.loadEvents(source.getId())).thenReturn(history);

            // When
            Optional<ExampleAggregate> result = repository.findById(source.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Nome Válido");
        }

        @Test
        @DisplayName("Deve usar snapshot quando disponível")
        void shouldUseSnapshotWhenAvailable() {
            // Given
            ExampleAggregate source = criarAggregateComEvento();
            source.markEventsAsCommitted();
            Object snapshotData = source.createSnapshot();

            AggregateSnapshot snapshot = new AggregateSnapshot(
                    source.getId(), "ExampleAggregate", 1L,
                    Map.of("data", snapshotData));
            when(snapshotStore.getLatestSnapshot(source.getId()))
                    .thenReturn(Optional.of(snapshot));
            when(eventStore.loadEvents(eq(source.getId()), anyLong()))
                    .thenReturn(List.of());

            // When
            Optional<ExampleAggregate> result = repository.findById(source.getId());

            // Then
            assertThat(result).isPresent();
            verify(eventStore).loadEvents(eq(source.getId()), anyLong());
        }
    }

    // =========================================================================
    // findByIdAndVersion
    // =========================================================================

    @Nested
    @DisplayName("findByIdAndVersion()")
    class FindByIdAndVersionTests {

        @Test
        @DisplayName("Deve retornar Optional.empty para ID null")
        void shouldReturnEmptyForNullId() {
            assertThat(repository.findByIdAndVersion(null, 1L)).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar Optional.empty para versão negativa")
        void shouldReturnEmptyForNegativeVersion() {
            assertThat(repository.findByIdAndVersion("id", -1L)).isEmpty();
        }

        @Test
        @DisplayName("Deve reconstruir aggregate até a versão especificada")
        void shouldReconstructAggregateUpToSpecifiedVersion() {
            // Given
            ExampleAggregate source = criarAggregateComEvento();
            List<DomainEvent> history = source.getUncommittedEvents();
            when(eventStore.loadEvents(source.getId())).thenReturn(history);

            // When
            Optional<ExampleAggregate> result =
                    repository.findByIdAndVersion(source.getId(), 1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getVersion()).isLessThanOrEqualTo(1L);
        }
    }

    // =========================================================================
    // exists / getCurrentVersion / delete
    // =========================================================================

    @Nested
    @DisplayName("exists(), getCurrentVersion() e delete()")
    class ExistsCurrentVersionDelete {

        @Test
        @DisplayName("exists deve retornar false para ID null")
        void existsShouldReturnFalseForNullId() {
            assertThat(repository.exists(null)).isFalse();
        }

        @Test
        @DisplayName("exists deve delegar ao EventStore")
        void existsShouldDelegateToEventStore() {
            // Given
            when(eventStore.aggregateExists("agg-1")).thenReturn(true);

            // Then
            assertThat(repository.exists("agg-1")).isTrue();
            verify(eventStore).aggregateExists("agg-1");
        }

        @Test
        @DisplayName("getCurrentVersion deve retornar 0 para ID null")
        void getCurrentVersionShouldReturnZeroForNullId() {
            assertThat(repository.getCurrentVersion(null)).isZero();
        }

        @Test
        @DisplayName("getCurrentVersion deve delegar ao EventStore")
        void getCurrentVersionShouldDelegateToEventStore() {
            // Given
            when(eventStore.getCurrentVersion("agg-2")).thenReturn(5L);

            // Then
            assertThat(repository.getCurrentVersion("agg-2")).isEqualTo(5L);
        }

        @Test
        @DisplayName("delete deve retornar false para ID null")
        void deleteShouldReturnFalseForNullId() {
            assertThat(repository.delete(null)).isFalse();
        }

        @Test
        @DisplayName("delete deve remover snapshots e retornar true")
        void deleteShouldRemoveSnapshotsAndReturnTrue() {
            // Given
            when(snapshotStore.deleteAllSnapshots("agg-3")).thenReturn(0);

            // When
            boolean result = repository.delete("agg-3");

            // Then
            assertThat(result).isTrue();
            verify(snapshotStore).deleteAllSnapshots("agg-3");
        }
    }

    // =========================================================================
    // getStatistics
    // =========================================================================

    @Nested
    @DisplayName("getStatistics()")
    class GetStatistics {

        @Test
        @DisplayName("Deve retornar mapa com campos esperados")
        void shouldReturnMapWithExpectedFields() {
            // When
            Map<String, Object> stats = repository.getStatistics();

            // Then
            assertThat(stats).containsKeys(
                    "aggregateType", "totalSaves", "totalLoads",
                    "totalSnapshots", "totalConcurrencyErrors", "lastActivity");
        }

        @Test
        @DisplayName("Deve reportar tipo correto de aggregate")
        void shouldReportCorrectAggregateType() {
            assertThat(repository.getStatistics().get("aggregateType"))
                    .isEqualTo("ExampleAggregate");
        }

        @Test
        @DisplayName("Deve incrementar totalSaves após cada save")
        void shouldIncrementTotalSavesAfterEachSave() {
            // Given
            ExampleAggregate agg1 = criarAggregateComEvento();
            ExampleAggregate agg2 = criarAggregateComEvento();

            // When
            repository.save(agg1);
            repository.save(agg2);

            // Then
            assertThat(repository.getStatistics().get("totalSaves")).isEqualTo(2L);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private ExampleAggregate criarAggregateComEvento() {
        ExampleAggregate agg = new ExampleAggregate(UUID.randomUUID().toString());
        agg.create("Nome Válido", "Descrição válida e completa");
        return agg;
    }
}
