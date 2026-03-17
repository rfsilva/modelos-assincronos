package com.seguradora.hibrida.eventstore.replay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ReplayFilter}.
 */
@DisplayName("ReplayFilter Tests")
class ReplayFilterTest {

    // =========================================================================
    // Defaults do builder
    // =========================================================================

    @Test
    @DisplayName("Builder deve criar instância com operator AND por padrão")
    void builderShouldDefaultToAndOperator() {
        ReplayFilter filter = ReplayFilter.builder().build();

        assertThat(filter.getOperator()).isEqualTo(ReplayFilter.LogicalOperator.AND);
    }

    @Test
    @DisplayName("Builder deve criar instância com listas vazias por padrão")
    void builderShouldDefaultToEmptyLists() {
        ReplayFilter filter = ReplayFilter.builder().build();

        assertThat(filter.getEventTypes()).isEmpty();
        assertThat(filter.getAggregateIds()).isEmpty();
        assertThat(filter.getAggregateTypes()).isEmpty();
        assertThat(filter.getCorrelationIds()).isEmpty();
        assertThat(filter.getUserIds()).isEmpty();
        assertThat(filter.getMetadataFilters()).isEmpty();
        assertThat(filter.getCustomPredicates()).isEmpty();
    }

    @Test
    @DisplayName("Builder deve ter onlyFailedEvents false por padrão")
    void builderShouldDefaultOnlyFailedEventsToFalse() {
        ReplayFilter filter = ReplayFilter.builder().build();
        assertThat(filter.isOnlyFailedEvents()).isFalse();
    }

    @Test
    @DisplayName("Builder deve ter onlyUnprocessedEvents false por padrão")
    void builderShouldDefaultOnlyUnprocessedEventsToFalse() {
        ReplayFilter filter = ReplayFilter.builder().build();
        assertThat(filter.isOnlyUnprocessedEvents()).isFalse();
    }

    // =========================================================================
    // LogicalOperator enum
    // =========================================================================

    @Test
    @DisplayName("LogicalOperator deve ter AND e OR")
    void logicalOperatorShouldHaveAndOrValues() {
        assertThat(ReplayFilter.LogicalOperator.values())
                .containsExactlyInAnyOrder(
                        ReplayFilter.LogicalOperator.AND,
                        ReplayFilter.LogicalOperator.OR);
    }

    // =========================================================================
    // Factory methods
    // =========================================================================

    @Test
    @DisplayName("forPeriod deve configurar timestamps")
    void forPeriodShouldConfigureTimestamps() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-31T23:59:59Z");

        ReplayFilter filter = ReplayFilter.forPeriod(from, to);

        assertThat(filter.getFromTimestamp()).isEqualTo(from);
        assertThat(filter.getToTimestamp()).isEqualTo(to);
    }

    @Test
    @DisplayName("forEventTypes deve configurar tipos de evento")
    void forEventTypesShouldConfigureEventTypes() {
        List<String> types = List.of("SinistroCriado", "ApoliceEmitida");
        ReplayFilter filter = ReplayFilter.forEventTypes(types);

        assertThat(filter.getEventTypes()).containsExactlyInAnyOrderElementsOf(types);
    }

    @Test
    @DisplayName("forAggregates deve configurar IDs de aggregate")
    void forAggregatesShouldConfigureAggregateIds() {
        List<String> ids = List.of("agg-1", "agg-2");
        ReplayFilter filter = ReplayFilter.forAggregates(ids);

        assertThat(filter.getAggregateIds()).containsExactlyInAnyOrderElementsOf(ids);
    }

    @Test
    @DisplayName("and() deve retornar filtro com operador AND")
    void andShouldReturnFilterWithAndOperator() {
        ReplayFilter base = ReplayFilter.forEventTypes(List.of("SinistroCriado"));
        ReplayFilter result = ReplayFilter.and(base);

        assertThat(result.getOperator()).isEqualTo(ReplayFilter.LogicalOperator.AND);
    }

    @Test
    @DisplayName("or() deve retornar filtro com operador OR")
    void orShouldReturnFilterWithOrOperator() {
        ReplayFilter base = ReplayFilter.forEventTypes(List.of("SinistroCriado"));
        ReplayFilter result = ReplayFilter.or(base);

        assertThat(result.getOperator()).isEqualTo(ReplayFilter.LogicalOperator.OR);
    }

    @Test
    @DisplayName("and() sem argumentos deve retornar filtro vazio")
    void andWithNoArgsShouldReturnEmptyFilter() {
        ReplayFilter result = ReplayFilter.and();
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("or() sem argumentos deve retornar filtro vazio")
    void orWithNoArgsShouldReturnEmptyFilter() {
        ReplayFilter result = ReplayFilter.or();
        assertThat(result).isNotNull();
    }

    // =========================================================================
    // matches() — filtro vazio passa tudo
    // =========================================================================

    @Nested
    @DisplayName("matches() com filtro vazio")
    class MatchesEmptyFilter {

        @Test
        @DisplayName("Filtro vazio deve aceitar evento sem timestamp")
        void emptyFilterShouldAcceptEventWithoutTimestamp() {
            ReplayFilter filter = ReplayFilter.builder().build();

            // Filtro vazio (sem timestamps, tipos, etc.) com operador AND deve passar
            assertThat(filter.getEventTypes()).isEmpty();
            assertThat(filter.getOperator()).isEqualTo(ReplayFilter.LogicalOperator.AND);
        }
    }
}
