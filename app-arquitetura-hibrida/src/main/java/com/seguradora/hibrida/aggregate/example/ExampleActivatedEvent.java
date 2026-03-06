package com.seguradora.hibrida.aggregate.example;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de ativação de exemplo.
 * 
 * <p>Disparado quando um ExampleAggregate é ativado.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ExampleActivatedEvent extends DomainEvent {
    
    private Instant activationTimestamp;
    
    public static ExampleActivatedEvent create(String aggregateId, long version, Instant activationTimestamp) {
        return ExampleActivatedEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(aggregateId)
                .aggregateType("ExampleAggregate")
                .version(version)
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID())
                .activationTimestamp(activationTimestamp)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("ExampleActivatedEvent{aggregateId='%s', activationTimestamp='%s'}", 
                getAggregateId(), activationTimestamp);
    }
}