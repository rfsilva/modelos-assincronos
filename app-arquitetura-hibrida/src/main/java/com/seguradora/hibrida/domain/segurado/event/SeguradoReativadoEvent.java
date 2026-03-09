package com.seguradora.hibrida.domain.segurado.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Evento disparado quando um segurado é reativado no sistema.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Getter
@NoArgsConstructor
public class SeguradoReativadoEvent extends DomainEvent {
    
    private String motivo;
    
    public SeguradoReativadoEvent(String aggregateId, String motivo) {
        super(aggregateId, "SeguradoAggregate", 0);
        this.motivo = motivo;
    }
    
    @Override
    public String getEventType() {
        return "SeguradoReativado";
    }
}
