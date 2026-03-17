package com.seguradora.hibrida.eventstore.config;

import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.archive.ArchiveStatistics;
import com.seguradora.hibrida.eventstore.archive.EventArchiver;
import com.seguradora.hibrida.eventstore.partition.PartitionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link EventStoreHealthIndicator}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EventStoreHealthIndicator Tests")
class EventStoreHealthIndicatorTest {

    @Mock
    private EventStore eventStore;

    @Mock
    private PartitionManager partitionManager;

    @Mock
    private EventArchiver eventArchiver;

    private EventStoreHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new EventStoreHealthIndicator(eventStore, partitionManager, eventArchiver);

        when(eventStore.aggregateExists(anyString())).thenReturn(false);
        when(partitionManager.arePartitionsHealthy()).thenReturn(true);
        when(eventArchiver.getArchiveStatistics()).thenReturn(
                ArchiveStatistics.builder()
                        .totalArchives(0L)
                        .totalEvents(0L)
                        .totalSize(0L)
                        .build());
    }

    // =========================================================================
    // Anotações
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(EventStoreHealthIndicator.class.isAnnotationPresent(Component.class)).isTrue();
    }

    // =========================================================================
    // checkHealth
    // =========================================================================

    @Test
    @DisplayName("checkHealth deve retornar status UP quando partições saudáveis")
    void checkHealthShouldReturnUpWhenPartitionsHealthy() {
        Map<String, Object> health = indicator.checkHealth();
        assertThat(health.get("status")).isEqualTo("UP");
    }

    @Test
    @DisplayName("checkHealth deve retornar status DOWN quando partições não saudáveis")
    void checkHealthShouldReturnDownWhenPartitionsUnhealthy() {
        when(partitionManager.arePartitionsHealthy()).thenReturn(false);

        Map<String, Object> health = indicator.checkHealth();
        assertThat(health.get("status")).isEqualTo("DOWN");
    }

    @Test
    @DisplayName("checkHealth deve retornar status DOWN quando ocorre exceção")
    void checkHealthShouldReturnDownWhenExceptionOccurs() {
        when(partitionManager.arePartitionsHealthy()).thenThrow(new RuntimeException("DB error"));

        Map<String, Object> health = indicator.checkHealth();
        assertThat(health.get("status")).isEqualTo("DOWN");
    }

    @Test
    @DisplayName("checkHealth deve conter chave 'eventStore'")
    void checkHealthShouldContainEventStoreKey() {
        Map<String, Object> health = indicator.checkHealth();
        assertThat(health).containsKey("eventStore");
    }

    // =========================================================================
    // isHealthy
    // =========================================================================

    @Test
    @DisplayName("isHealthy deve retornar true quando partições saudáveis")
    void isHealthyShouldReturnTrueWhenPartitionsHealthy() {
        assertThat(indicator.isHealthy()).isTrue();
    }

    @Test
    @DisplayName("isHealthy deve retornar false quando partições não saudáveis")
    void isHealthyShouldReturnFalseWhenPartitionsUnhealthy() {
        when(partitionManager.arePartitionsHealthy()).thenReturn(false);
        assertThat(indicator.isHealthy()).isFalse();
    }
}
