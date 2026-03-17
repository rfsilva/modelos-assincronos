package com.seguradora.hibrida.snapshot.trigger;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.snapshot.SnapshotProperties;
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
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link SnapshotTrigger}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SnapshotTrigger Tests")
class SnapshotTriggerTest {

    @Mock
    private SnapshotStore snapshotStore;

    @Mock
    private SnapshotProperties snapshotProperties;

    @Mock
    private AggregateRoot aggregate;

    private SnapshotTrigger trigger;

    @BeforeEach
    void setUp() {
        when(snapshotProperties.isEnabled()).thenReturn(true);
        when(snapshotProperties.getSnapshotThreshold()).thenReturn(50);
        when(aggregate.getId()).thenReturn("agg-001");
        when(aggregate.getVersion()).thenReturn(50L);

        trigger = new SnapshotTrigger(snapshotStore, snapshotProperties);
    }

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(SnapshotTrigger.class.isAnnotationPresent(Component.class)).isTrue();
    }

    // =========================================================================
    // shouldTriggerSnapshot()
    // =========================================================================

    @Nested
    @DisplayName("shouldTriggerSnapshot()")
    class ShouldTriggerSnapshot {

        @Test
        @DisplayName("Deve retornar false quando sistema de snapshots está desabilitado")
        void shouldReturnFalseWhenSystemDisabled() {
            when(snapshotProperties.isEnabled()).thenReturn(false);

            assertThat(trigger.shouldTriggerSnapshot(aggregate)).isFalse();
            verify(snapshotStore, never()).shouldCreateSnapshot(anyString(), anyLong());
        }

        @Test
        @DisplayName("Deve retornar false quando aggregate é null")
        void shouldReturnFalseWhenAggregateIsNull() {
            assertThat(trigger.shouldTriggerSnapshot(null)).isFalse();
        }

        @Test
        @DisplayName("Deve delegar a decisão ao snapshotStore")
        void shouldDelegateToSnapshotStore() {
            when(snapshotStore.shouldCreateSnapshot("agg-001", 50L)).thenReturn(true);

            boolean result = trigger.shouldTriggerSnapshot(aggregate);

            assertThat(result).isTrue();
            verify(snapshotStore).shouldCreateSnapshot("agg-001", 50L);
        }

        @Test
        @DisplayName("Deve retornar false quando store lança exceção")
        void shouldReturnFalseWhenStoreThrows() {
            when(snapshotStore.shouldCreateSnapshot(anyString(), anyLong()))
                    .thenThrow(new RuntimeException("Erro de conexão"));

            assertThat(trigger.shouldTriggerSnapshot(aggregate)).isFalse();
        }
    }

    // =========================================================================
    // createSnapshot()
    // =========================================================================

    @Nested
    @DisplayName("createSnapshot()")
    class CreateSnapshot {

        @Test
        @DisplayName("Deve chamar snapshotStore.saveSnapshot quando aggregate é válido")
        void shouldCallSaveSnapshotWhenAggregateIsValid() {
            trigger.createSnapshot(aggregate);

            verify(snapshotStore).saveSnapshot(any(AggregateSnapshot.class));
        }

        @Test
        @DisplayName("Não deve chamar snapshotStore quando aggregate é null")
        void shouldNotCallStoreWhenAggregateIsNull() {
            trigger.createSnapshot(null);

            verify(snapshotStore, never()).saveSnapshot(any());
        }

        @Test
        @DisplayName("Não deve propagar exceção quando store falha")
        void shouldNotPropagateExceptionWhenStoreFails() {
            doThrow(new RuntimeException("DB error")).when(snapshotStore).saveSnapshot(any());

            // Não deve lançar exceção
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> trigger.createSnapshot(aggregate));
        }
    }

    // =========================================================================
    // tryCreateSnapshot()
    // =========================================================================

    @Nested
    @DisplayName("tryCreateSnapshot()")
    class TryCreateSnapshot {

        @Test
        @DisplayName("Deve retornar true e criar snapshot quando shouldTriggerSnapshot é true")
        void shouldReturnTrueAndCreateSnapshotWhenTriggered() {
            when(snapshotStore.shouldCreateSnapshot("agg-001", 50L)).thenReturn(true);

            boolean result = trigger.tryCreateSnapshot(aggregate);

            assertThat(result).isTrue();
            verify(snapshotStore).saveSnapshot(any(AggregateSnapshot.class));
        }

        @Test
        @DisplayName("Deve retornar false e não criar snapshot quando shouldTriggerSnapshot é false")
        void shouldReturnFalseWhenNotTriggered() {
            when(snapshotStore.shouldCreateSnapshot("agg-001", 50L)).thenReturn(false);

            boolean result = trigger.tryCreateSnapshot(aggregate);

            assertThat(result).isFalse();
            verify(snapshotStore, never()).saveSnapshot(any());
        }
    }

    // =========================================================================
    // forceSnapshot()
    // =========================================================================

    @Test
    @DisplayName("forceSnapshot() deve criar snapshot independente do threshold")
    void forceSnapshotShouldCreateSnapshot() {
        trigger.forceSnapshot(aggregate);

        verify(snapshotStore).saveSnapshot(any(AggregateSnapshot.class));
    }

    // =========================================================================
    // getConfiguredThreshold() e isSnapshotSystemEnabled()
    // =========================================================================

    @Test
    @DisplayName("getConfiguredThreshold() deve delegar a snapshotProperties")
    void shouldReturnConfiguredThreshold() {
        when(snapshotProperties.getSnapshotThreshold()).thenReturn(100);

        assertThat(trigger.getConfiguredThreshold()).isEqualTo(100);
    }

    @Test
    @DisplayName("isSnapshotSystemEnabled() deve delegar a snapshotProperties")
    void shouldReturnSnapshotSystemEnabled() {
        when(snapshotProperties.isEnabled()).thenReturn(true);

        assertThat(trigger.isSnapshotSystemEnabled()).isTrue();
    }

    @Test
    @DisplayName("isSnapshotSystemEnabled() deve retornar false quando desabilitado")
    void shouldReturnFalseWhenSystemDisabled() {
        when(snapshotProperties.isEnabled()).thenReturn(false);

        assertThat(trigger.isSnapshotSystemEnabled()).isFalse();
    }
}
