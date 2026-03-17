package com.seguradora.hibrida.snapshot.impl;

import com.seguradora.hibrida.snapshot.SnapshotProperties;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import com.seguradora.hibrida.snapshot.entity.SnapshotEntry;
import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import com.seguradora.hibrida.snapshot.repository.SnapshotRepository;
import com.seguradora.hibrida.snapshot.serialization.SnapshotSerializationResult;
import com.seguradora.hibrida.snapshot.serialization.SnapshotSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link PostgreSQLSnapshotStore}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PostgreSQLSnapshotStore Tests")
class PostgreSQLSnapshotStoreTest {

    @Mock
    private SnapshotRepository snapshotRepository;

    @Mock
    private SnapshotSerializer snapshotSerializer;

    @Mock
    private SnapshotProperties snapshotProperties;

    private PostgreSQLSnapshotStore store;

    @BeforeEach
    void setUp() {
        when(snapshotProperties.getCompressionThreshold()).thenReturn(1024);
        when(snapshotProperties.getMaxSnapshotsPerAggregate()).thenReturn(5);
        when(snapshotProperties.getSnapshotThreshold()).thenReturn(50);

        store = new PostgreSQLSnapshotStore(snapshotRepository, snapshotSerializer, snapshotProperties);
    }

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Service")
    void shouldBeAnnotatedWithService() {
        assertThat(PostgreSQLSnapshotStore.class.isAnnotationPresent(Service.class)).isTrue();
    }

    @Test
    @DisplayName("Deve implementar SnapshotStore")
    void shouldImplementSnapshotStore() {
        assertThat(store).isInstanceOf(SnapshotStore.class);
    }

    // =========================================================================
    // saveSnapshot() — @Async
    // =========================================================================

    @Test
    @DisplayName("saveSnapshot() deve estar anotado com @Async('snapshotTaskExecutor')")
    void saveSnapshotShouldBeAsync() throws NoSuchMethodException {
        Method m = PostgreSQLSnapshotStore.class.getMethod("saveSnapshot", AggregateSnapshot.class);
        Async asyncAnnotation = m.getAnnotation(Async.class);

        assertThat(asyncAnnotation).isNotNull();
        assertThat(asyncAnnotation.value()).contains("snapshotTaskExecutor");
    }

    // =========================================================================
    // getLatestSnapshot()
    // =========================================================================

    @Nested
    @DisplayName("getLatestSnapshot()")
    class GetLatestSnapshot {

        @Test
        @DisplayName("Deve retornar Optional.empty() quando não há snapshot")
        void shouldReturnEmptyWhenNoSnapshot() {
            when(snapshotRepository.findFirstByAggregateIdOrderByVersionDesc("agg-1"))
                    .thenReturn(Optional.empty());

            Optional<AggregateSnapshot> result = store.getLatestSnapshot("agg-1");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar snapshot quando encontrado no repositório")
        void shouldReturnSnapshotWhenFound() {
            SnapshotEntry entry = buildEntry("snap-1", "agg-1", 10L);
            when(snapshotRepository.findFirstByAggregateIdOrderByVersionDesc("agg-1"))
                    .thenReturn(Optional.of(entry));

            AggregateSnapshot expected = buildSnapshot("agg-1", 10L);
            when(snapshotSerializer.deserializeCompressed(any(), any(), anyBoolean(), any()))
                    .thenReturn(expected);

            Optional<AggregateSnapshot> result = store.getLatestSnapshot("agg-1");

            assertThat(result).isPresent();
            assertThat(result.get().getAggregateId()).isEqualTo("agg-1");
        }

        @Test
        @DisplayName("getLatestSnapshot() deve ter @Transactional(readOnly=true)")
        void shouldBeTransactionalReadOnly() throws NoSuchMethodException {
            Method m = PostgreSQLSnapshotStore.class.getMethod("getLatestSnapshot", String.class);
            Transactional tx = m.getAnnotation(Transactional.class);

            assertThat(tx).isNotNull();
            assertThat(tx.readOnly()).isTrue();
        }
    }

    // =========================================================================
    // hasSnapshots()
    // =========================================================================

    @Nested
    @DisplayName("hasSnapshots()")
    class HasSnapshots {

        @Test
        @DisplayName("Deve retornar true quando repositório indica existência")
        void shouldReturnTrueWhenExists() {
            when(snapshotRepository.existsByAggregateId("agg-1")).thenReturn(true);

            assertThat(store.hasSnapshots("agg-1")).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando repositório indica inexistência")
        void shouldReturnFalseWhenNotExists() {
            when(snapshotRepository.existsByAggregateId("agg-999")).thenReturn(false);

            assertThat(store.hasSnapshots("agg-999")).isFalse();
        }
    }

    // =========================================================================
    // cleanupOldSnapshots()
    // =========================================================================

    @Test
    @DisplayName("cleanupOldSnapshots() deve delegar ao repositório")
    void cleanupOldSnapshotsShouldDelegateToRepository() {
        when(snapshotRepository.deleteOldSnapshots("agg-1", 5)).thenReturn(3);

        int removed = store.cleanupOldSnapshots("agg-1", 5);

        assertThat(removed).isEqualTo(3);
        verify(snapshotRepository).deleteOldSnapshots("agg-1", 5);
    }

    // =========================================================================
    // deleteAllSnapshots()
    // =========================================================================

    @Test
    @DisplayName("deleteAllSnapshots() deve delegar ao repositório")
    void deleteAllSnapshotsShouldDelegateToRepository() {
        when(snapshotRepository.deleteByAggregateId("agg-1")).thenReturn(7);

        int removed = store.deleteAllSnapshots("agg-1");

        assertThat(removed).isEqualTo(7);
        verify(snapshotRepository).deleteByAggregateId("agg-1");
    }

    // =========================================================================
    // getSnapshotHistory()
    // =========================================================================

    @Test
    @DisplayName("getSnapshotHistory() deve retornar lista deserializada")
    void getSnapshotHistoryShouldReturnDeserializedList() {
        SnapshotEntry entry = buildEntry("snap-1", "agg-1", 10L);
        when(snapshotRepository.findByAggregateIdOrderByVersionDesc("agg-1"))
                .thenReturn(List.of(entry));

        AggregateSnapshot snapshot = buildSnapshot("agg-1", 10L);
        when(snapshotSerializer.deserializeCompressed(any(), any(), anyBoolean(), any()))
                .thenReturn(snapshot);

        List<AggregateSnapshot> history = store.getSnapshotHistory("agg-1");

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getAggregateId()).isEqualTo("agg-1");
    }

    // =========================================================================
    // shouldCreateSnapshot()
    // =========================================================================

    @Nested
    @DisplayName("shouldCreateSnapshot()")
    class ShouldCreateSnapshot {

        @Test
        @DisplayName("Deve retornar true quando versão é múltipla do threshold e há snapshot anterior")
        void shouldReturnTrueWhenVersionIsMultipleOfThresholdWithPriorSnapshot() {
            // threshold = 50, version = 100 → 100 % 50 == 0 → true
            when(snapshotRepository.findFirstByAggregateIdOrderByVersionDesc("agg-1"))
                    .thenReturn(Optional.of(buildEntry("snap-1", "agg-1", 50L)));

            boolean result = store.shouldCreateSnapshot("agg-1", 100L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando eventos desde último snapshot são menos que o threshold")
        void shouldReturnFalseWhenVersionIsNotMultiple() {
            // Com snapshot anterior na versão 30 e versão atual 55: eventsSince = 25 < 50 → false
            when(snapshotRepository.findFirstByAggregateIdOrderByVersionDesc("agg-1"))
                    .thenReturn(Optional.of(buildEntry("snap-0", "agg-1", 30L)));

            boolean result = store.shouldCreateSnapshot("agg-1", 55L);

            assertThat(result).isFalse();
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private SnapshotEntry buildEntry(String snapshotId, String aggregateId, Long version) {
        return new SnapshotEntry(snapshotId, aggregateId, "TestAggregate",
                version, Map.of("key", "value"), Instant.now());
    }

    private AggregateSnapshot buildSnapshot(String aggregateId, long version) {
        return new AggregateSnapshot(aggregateId, "TestAggregate", version,
                Map.of("key", "value"));
    }
}
