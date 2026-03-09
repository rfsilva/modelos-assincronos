package com.seguradora.hibrida.aggregate.example;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de criação de exemplo.
 * 
 * <p>Disparado quando um novo ExampleAggregate é criado no sistema.
 * Contém todos os dados necessários para reconstruir o estado inicial.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ExampleCreatedEvent extends DomainEvent {
    
    private String name;
    private String description;
    private Instant creationTimestamp;
    
    public static ExampleCreatedEvent create(String aggregateId, String name, String description, Instant creationTimestamp) {
        return ExampleCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(aggregateId)
                .aggregateType("ExampleAggregate")
                .version(1L)
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID())
                .name(name)
                .description(description)
                .creationTimestamp(creationTimestamp)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("ExampleCreatedEvent{aggregateId='%s', name='%s', description='%s'}", 
                getAggregateId(), name, description);
    }
}