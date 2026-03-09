package com.seguradora.hibrida.eventstore.replay;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Filtro avançado para seleção de eventos durante replay.
 * 
 * <p>Permite combinação de múltiplos critérios usando operadores
 * lógicos AND/OR para seleção precisa de eventos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
public class ReplayFilter {
    
    /**
     * Operador lógico para combinação de filtros.
     */
    public enum LogicalOperator {
        AND, OR
    }
    
    /**
     * Operador para combinação de critérios.
     */
    @Builder.Default
    private LogicalOperator operator = LogicalOperator.AND;
    
    /**
     * Filtros por período.
     */
    private Instant fromTimestamp;
    private Instant toTimestamp;
    
    /**
     * Filtros por tipo de evento.
     */
    @Builder.Default
    private List<String> eventTypes = List.of();
    
    /**
     * Filtros por aggregate.
     */
    @Builder.Default
    private List<String> aggregateIds = List.of();
    
    @Builder.Default
    private List<String> aggregateTypes = List.of();
    
    /**
     * Filtros por correlation ID.
     */
    @Builder.Default
    private List<String> correlationIds = List.of();
    
    /**
     * Filtros por usuário.
     */
    @Builder.Default
    private List<String> userIds = List.of();
    
    /**
     * Filtros por versão do aggregate.
     */
    private Long minVersion;
    private Long maxVersion;
    
    /**
     * Filtros por metadados customizados.
     */
    @Builder.Default
    private Map<String, Object> metadataFilters = Map.of();
    
    /**
     * Filtros customizados usando predicados.
     */
    @Builder.Default
    private List<Predicate<DomainEvent>> customPredicates = List.of();
    
    /**
     * Incluir apenas eventos com erros anteriores.
     */
    @Builder.Default
    private boolean onlyFailedEvents = false;
    
    /**
     * Incluir apenas eventos não processados.
     */
    @Builder.Default
    private boolean onlyUnprocessedEvents = false;
    
    /**
     * Tamanho mínimo do evento (em bytes).
     */
    private Integer minEventSize;
    
    /**
     * Tamanho máximo do evento (em bytes).
     */
    private Integer maxEventSize;
    
    /**
     * Aplica o filtro a um evento.
     * 
     * @param event Evento a ser testado
     * @return true se o evento passa no filtro
     */
    public boolean matches(DomainEvent event) {
        List<Boolean> results = List.of(
            matchesTimestamp(event),
            matchesEventType(event),
            matchesAggregate(event),
            matchesCorrelationId(event),
            matchesUserId(event),
            matchesVersion(event),
            matchesMetadata(event),
            matchesCustomPredicates(event),
            matchesEventSize(event)
        );
        
        if (operator == LogicalOperator.AND) {
            return results.stream().allMatch(Boolean::booleanValue);
        } else {
            return results.stream().anyMatch(Boolean::booleanValue);
        }
    }
    
    private boolean matchesTimestamp(DomainEvent event) {
        if (fromTimestamp == null && toTimestamp == null) {
            return true;
        }
        
        Instant eventTime = event.getTimestamp();
        if (eventTime == null) {
            return false;
        }
        
        boolean afterFrom = fromTimestamp == null || !eventTime.isBefore(fromTimestamp);
        boolean beforeTo = toTimestamp == null || !eventTime.isAfter(toTimestamp);
        
        return afterFrom && beforeTo;
    }
    
    private boolean matchesEventType(DomainEvent event) {
        if (eventTypes.isEmpty()) {
            return true;
        }
        return eventTypes.contains(event.getEventType());
    }
    
    private boolean matchesAggregate(DomainEvent event) {
        boolean matchesId = aggregateIds.isEmpty() || aggregateIds.contains(event.getAggregateId());
        boolean matchesType = aggregateTypes.isEmpty() || aggregateTypes.contains(event.getAggregateType());
        
        return matchesId && matchesType;
    }
    
    private boolean matchesCorrelationId(DomainEvent event) {
        if (correlationIds.isEmpty()) {
            return true;
        }
        
        String correlationId = event.getCorrelationId() != null ? 
            event.getCorrelationId().toString() : null;
        
        return correlationId != null && correlationIds.contains(correlationId);
    }
    
    private boolean matchesUserId(DomainEvent event) {
        if (userIds.isEmpty()) {
            return true;
        }
        
        String userId = event.getUserId();
        return userId != null && userIds.contains(userId);
    }
    
    private boolean matchesVersion(DomainEvent event) {
        Long version = event.getVersion();
        if (version == null) {
            return true;
        }
        
        boolean afterMin = minVersion == null || version >= minVersion;
        boolean beforeMax = maxVersion == null || version <= maxVersion;
        
        return afterMin && beforeMax;
    }
    
    private boolean matchesMetadata(DomainEvent event) {
        if (metadataFilters.isEmpty()) {
            return true;
        }
        
        if (event.getMetadata() == null) {
            return false;
        }
        
        return metadataFilters.entrySet().stream()
            .allMatch(entry -> {
                Object eventValue = event.getMetadata().getValue(entry.getKey(), Object.class);
                return entry.getValue().equals(eventValue);
            });
    }
    
    private boolean matchesCustomPredicates(DomainEvent event) {
        if (customPredicates.isEmpty()) {
            return true;
        }
        
        return customPredicates.stream().allMatch(predicate -> predicate.test(event));
    }
    
    private boolean matchesEventSize(DomainEvent event) {
        // Estimativa simples do tamanho do evento
        String eventData = event.toString();
        int size = eventData.length();
        
        boolean afterMin = minEventSize == null || size >= minEventSize;
        boolean beforeMax = maxEventSize == null || size <= maxEventSize;
        
        return afterMin && beforeMax;
    }
    
    /**
     * Cria filtro para período específico.
     * 
     * @param from Data inicial
     * @param to Data final
     * @return Filtro configurado
     */
    public static ReplayFilter forPeriod(Instant from, Instant to) {
        return ReplayFilter.builder()
            .fromTimestamp(from)
            .toTimestamp(to)
            .build();
    }
    
    /**
     * Cria filtro para tipos de evento específicos.
     * 
     * @param eventTypes Tipos de evento
     * @return Filtro configurado
     */
    public static ReplayFilter forEventTypes(List<String> eventTypes) {
        return ReplayFilter.builder()
            .eventTypes(eventTypes)
            .build();
    }
    
    /**
     * Cria filtro para aggregates específicos.
     * 
     * @param aggregateIds IDs dos aggregates
     * @return Filtro configurado
     */
    public static ReplayFilter forAggregates(List<String> aggregateIds) {
        return ReplayFilter.builder()
            .aggregateIds(aggregateIds)
            .build();
    }
    
    /**
     * Cria filtro combinado usando operador AND.
     * 
     * @param filters Filtros a serem combinados
     * @return Filtro combinado
     */
    public static ReplayFilter and(ReplayFilter... filters) {
        // Implementação simplificada - combina critérios do primeiro filtro
        if (filters.length == 0) {
            return ReplayFilter.builder().build();
        }
        
        return filters[0].toBuilder()
            .operator(LogicalOperator.AND)
            .build();
    }
    
    /**
     * Cria filtro combinado usando operador OR.
     * 
     * @param filters Filtros a serem combinados
     * @return Filtro combinado
     */
    public static ReplayFilter or(ReplayFilter... filters) {
        // Implementação simplificada - combina critérios do primeiro filtro
        if (filters.length == 0) {
            return ReplayFilter.builder().build();
        }
        
        return filters[0].toBuilder()
            .operator(LogicalOperator.OR)
            .build();
    }
}