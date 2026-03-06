package com.seguradora.hibrida.aggregate.example;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de atualização de exemplo.
 * 
 * <p>Disparado quando um ExampleAggregate é atualizado.
 * Contém os novos valores dos campos modificados.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ExampleUpdatedEvent extends DomainEvent {
    
    private String newName;
    private String newDescription;
    private Instant updateTimestamp;
    
    public static ExampleUpdatedEvent create(String aggregateId, long version, String newName, String newDescription, Instant updateTimestamp) {
        return ExampleUpdatedEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(aggregateId)
                .aggregateType("ExampleAggregate")
                .version(version)
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID())
                .newName(newName)
                .newDescription(newDescription)
                .updateTimestamp(updateTimestamp)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("ExampleUpdatedEvent{aggregateId='%s', newName='%s', newDescription='%s'}", 
                getAggregateId(), newName, newDescription);
    }
}