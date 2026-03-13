package com.seguradora.hibrida.eventstore.replay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ReplayConfiguration}.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("ReplayConfiguration - Testes Unitários")
class ReplayConfigurationTest {

    @Test
    @DisplayName("Deve criar instância com valores padrão corretos")
    void shouldCreateInstanceWithCorrectDefaults() {
        // When
        ReplayConfiguration config = ReplayConfiguration.builder().build();

        // Then
        assertThat(config.getReplayId()).isNotNull();
        assertThat(config.getEventTypes()).isEmpty();
        assertThat(config.getAggregateIds()).isEmpty();
        assertThat(config.getAggregateTypes()).isEmpty();
        assertThat(config.isSimulationMode()).isFalse();
        assertThat(config.getEventsPerSecond()).isZero();
        assertThat(config.getBatchSize()).isEqualTo(100);
        assertThat(config.getBatchTimeoutSeconds()).isEqualTo(30);
        assertThat(config.getMaxRetries()).isEqualTo(3);
        assertThat(config.getRetryDelayMs()).isEqualTo(1000L);
        assertThat(config.isStopOnError()).isFalse();
        assertThat(config.isIncludeArchivedEvents()).isFalse();
        assertThat(config.getMetadataFilters()).isEmpty();
        assertThat(config.getTargetHandlers()).isEmpty();
        assertThat(config.getExcludedHandlers()).isEmpty();
        assertThat(config.isGenerateDetailedReport()).isFalse();
        assertThat(config.getProgressNotificationInterval()).isEqualTo(1000);
        assertThat(config.getCreatedAt()).isNotNull();
        assertThat(config.getAdditionalMetadata()).isEmpty();
    }

    @Test
    @DisplayName("Deve criar instância com valores customizados")
    void shouldCreateInstanceWithCustomValues() {
        // Given
        UUID replayId = UUID.randomUUID();
        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = Instant.now();

        // When
        ReplayConfiguration config = ReplayConfiguration.builder()
            .replayId(replayId)
            .name("Test Replay")
            .description("Test Description")
            .fromTimestamp(from)
            .toTimestamp(to)
            .eventTypes(List.of("Event1", "Event2"))
            .aggregateIds(List.of("agg1", "agg2"))
            .aggregateTypes(List.of("Type1"))
            .simulationMode(true)
            .eventsPerSecond(100)
            .batchSize(50)
            .batchTimeoutSeconds(60)
            .maxRetries(5)
            .retryDelayMs(2000L)
            .stopOnError(true)
            .includeArchivedEvents(true)
            .metadataFilters(Map.of("key", "value"))
            .targetHandlers(List.of("handler1"))
            .excludedHandlers(List.of("handler2"))
            .generateDetailedReport(true)
            .progressNotificationInterval(500)
            .initiatedBy("test-user")
            .additionalMetadata(Map.of("meta", "data"))
            .build();

        // Then
        assertThat(config.getReplayId()).isEqualTo(replayId);
        assertThat(config.getName()).isEqualTo("Test Replay");
        assertThat(config.getDescription()).isEqualTo("Test Description");
        assertThat(config.getFromTimestamp()).isEqualTo(from);
        assertThat(config.getToTimestamp()).isEqualTo(to);
        assertThat(config.getEventTypes()).containsExactly("Event1", "Event2");
        assertThat(config.getAggregateIds()).containsExactly("agg1", "agg2");
        assertThat(config.getAggregateTypes()).containsExactly("Type1");
        assertThat(config.isSimulationMode()).isTrue();
        assertThat(config.getEventsPerSecond()).isEqualTo(100);
        assertThat(config.getBatchSize()).isEqualTo(50);
        assertThat(config.getBatchTimeoutSeconds()).isEqualTo(60);
        assertThat(config.getMaxRetries()).isEqualTo(5);
        assertThat(config.getRetryDelayMs()).isEqualTo(2000L);
        assertThat(config.isStopOnError()).isTrue();
        assertThat(config.isIncludeArchivedEvents()).isTrue();
        assertThat(config.getMetadataFilters()).containsEntry("key", "value");
        assertThat(config.getTargetHandlers()).containsExactly("handler1");
        assertThat(config.getExcludedHandlers()).containsExactly("handler2");
        assertThat(config.isGenerateDetailedReport()).isTrue();
        assertThat(config.getProgressNotificationInterval()).isEqualTo(500);
        assertThat(config.getInitiatedBy()).isEqualTo("test-user");
        assertThat(config.getAdditionalMetadata()).containsEntry("meta", "data");
    }

    @Test
    @DisplayName("Deve validar configuração válida")
    void shouldValidateValidConfiguration() {
        // Given
        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = Instant.now();

        ReplayConfiguration config = ReplayConfiguration.builder()
            .fromTimestamp(from)
            .toTimestamp(to)
            .eventsPerSecond(100)
            .batchSize(50)
            .batchTimeoutSeconds(30)
            .maxRetries(3)
            .retryDelayMs(1000L)
            .build();

        // When
        boolean valid = config.isValid();

        // Then
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("Deve invalidar quando fromTimestamp é depois de toTimestamp")
    void shouldInvalidateWhenFromIsAfterTo() {
        // Given
        Instant from = Instant.now();
        Instant to = Instant.now().minus(1, ChronoUnit.DAYS);

        ReplayConfiguration config = ReplayConfiguration.builder()
            .fromTimestamp(from)
            .toTimestamp(to)
            .build();

        // When
        boolean valid = config.isValid();

        // Then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar quando eventsPerSecond é negativo")
    void shouldInvalidateWhenEventsPerSecondIsNegative() {
        // Given
        ReplayConfiguration config = ReplayConfiguration.builder()
            .eventsPerSecond(-1)
            .build();

        // When
        boolean valid = config.isValid();

        // Then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar quando batchSize é zero ou negativo")
    void shouldInvalidateWhenBatchSizeIsZeroOrNegative() {
        // Given
        ReplayConfiguration config1 = ReplayConfiguration.builder()
            .batchSize(0)
            .build();

        ReplayConfiguration config2 = ReplayConfiguration.builder()
            .batchSize(-1)
            .build();

        // When/Then
        assertThat(config1.isValid()).isFalse();
        assertThat(config2.isValid()).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar quando batchTimeoutSeconds é zero ou negativo")
    void shouldInvalidateWhenBatchTimeoutIsZeroOrNegative() {
        // Given
        ReplayConfiguration config1 = ReplayConfiguration.builder()
            .batchTimeoutSeconds(0)
            .build();

        ReplayConfiguration config2 = ReplayConfiguration.builder()
            .batchTimeoutSeconds(-1)
            .build();

        // When/Then
        assertThat(config1.isValid()).isFalse();
        assertThat(config2.isValid()).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar quando maxRetries é negativo")
    void shouldInvalidateWhenMaxRetriesIsNegative() {
        // Given
        ReplayConfiguration config = ReplayConfiguration.builder()
            .maxRetries(-1)
            .build();

        // When
        boolean valid = config.isValid();

        // Then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar quando retryDelayMs é negativo")
    void shouldInvalidateWhenRetryDelayIsNegative() {
        // Given
        ReplayConfiguration config = ReplayConfiguration.builder()
            .retryDelayMs(-1L)
            .build();

        // When
        boolean valid = config.isValid();

        // Then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Deve criar configuração para período")
    void shouldCreateConfigurationForPeriod() {
        // Given
        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = Instant.now();

        // When
        ReplayConfiguration config = ReplayConfiguration.forPeriod(from, to);

        // Then
        assertThat(config.getFromTimestamp()).isEqualTo(from);
        assertThat(config.getToTimestamp()).isEqualTo(to);
        assertThat(config.getName()).isEqualTo("Replay por período");
        assertThat(config.getDescription()).contains(from.toString(), to.toString());
    }

    @Test
    @DisplayName("Deve criar configuração para tipo de evento")
    void shouldCreateConfigurationForEventType() {
        // Given
        String eventType = "TestEvent";
        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = Instant.now();

        // When
        ReplayConfiguration config = ReplayConfiguration.forEventType(eventType, from, to);

        // Then
        assertThat(config.getEventTypes()).containsExactly(eventType);
        assertThat(config.getFromTimestamp()).isEqualTo(from);
        assertThat(config.getToTimestamp()).isEqualTo(to);
        assertThat(config.getName()).isEqualTo("Replay por tipo de evento");
        assertThat(config.getDescription()).contains(eventType, from.toString(), to.toString());
    }

    @Test
    @DisplayName("Deve criar configuração para aggregate")
    void shouldCreateConfigurationForAggregate() {
        // Given
        String aggregateId = "agg-123";

        // When
        ReplayConfiguration config = ReplayConfiguration.forAggregate(aggregateId);

        // Then
        assertThat(config.getAggregateIds()).containsExactly(aggregateId);
        assertThat(config.getName()).isEqualTo("Replay por aggregate");
        assertThat(config.getDescription()).contains(aggregateId);
    }

    @Test
    @DisplayName("Deve criar configuração para simulação")
    void shouldCreateConfigurationForSimulation() {
        // Given
        ReplayConfiguration baseConfig = ReplayConfiguration.builder()
            .name("Original")
            .description("Original Description")
            .build();

        // When
        ReplayConfiguration simConfig = ReplayConfiguration.forSimulation(baseConfig);

        // Then
        assertThat(simConfig.isSimulationMode()).isTrue();
        assertThat(simConfig.getName()).isEqualTo("Simulação - Original");
        assertThat(simConfig.getDescription()).isEqualTo("Simulação: Original Description");
        assertThat(simConfig.isGenerateDetailedReport()).isTrue();
    }

    @Test
    @DisplayName("Deve usar toBuilder para criar cópia modificada")
    void shouldUseToBuilderToCreateModifiedCopy() {
        // Given
        ReplayConfiguration original = ReplayConfiguration.builder()
            .name("Original")
            .batchSize(100)
            .build();

        // When
        ReplayConfiguration modified = original.toBuilder()
            .name("Modified")
            .batchSize(200)
            .build();

        // Then
        assertThat(original.getName()).isEqualTo("Original");
        assertThat(original.getBatchSize()).isEqualTo(100);
        assertThat(modified.getName()).isEqualTo("Modified");
        assertThat(modified.getBatchSize()).isEqualTo(200);
    }

    @Test
    @DisplayName("Deve ter equals e hashCode corretos")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = Instant.now();
        UUID replayId = UUID.randomUUID();

        ReplayConfiguration config1 = ReplayConfiguration.builder()
            .replayId(replayId)
            .name("Test")
            .fromTimestamp(from)
            .toTimestamp(to)
            .build();

        ReplayConfiguration config2 = ReplayConfiguration.builder()
            .replayId(replayId)
            .name("Test")
            .fromTimestamp(from)
            .toTimestamp(to)
            .build();

        ReplayConfiguration config3 = ReplayConfiguration.builder()
            .replayId(UUID.randomUUID())
            .name("Different")
            .fromTimestamp(from)
            .toTimestamp(to)
            .build();

        // Then
        assertThat(config1).isEqualTo(config2);
        assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        assertThat(config1).isNotEqualTo(config3);
    }

    @Test
    @DisplayName("Deve ter toString útil")
    void shouldHaveUsefulToString() {
        // Given
        ReplayConfiguration config = ReplayConfiguration.builder()
            .name("Test Replay")
            .description("Test Description")
            .batchSize(50)
            .build();

        // When
        String toString = config.toString();

        // Then
        assertThat(toString)
            .contains("ReplayConfiguration")
            .contains("name")
            .contains("batchSize");
    }
}
