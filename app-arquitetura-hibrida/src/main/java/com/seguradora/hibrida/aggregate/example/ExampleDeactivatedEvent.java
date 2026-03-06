package com.seguradora.hibrida.aggregate.example;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de desativação de exemplo.
 * 
 * <p>Disparado quando um ExampleAggregate é desativado.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ExampleDeactivatedEvent extends DomainEvent {
    
    private Instant deactivationTimestamp;
    
    public static ExampleDeactivatedEvent create(String aggregateId, long version, Instant deactivationTimestamp) {
        return ExampleDeactivatedEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(aggregateId)
                .aggregateType("ExampleAggregate")
                .version(version)
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID())
                .deactivationTimestamp(deactivationTimestamp)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("ExampleDeactivatedEvent{aggregateId='%s', deactivationTimestamp='%s'}", 
                getAggregateId(), deactivationTimestamp);
    }
}