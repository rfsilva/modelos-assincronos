package com.seguradora.hibrida.domain.segurado.event;

import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Evento disparado quando um contato é removido do segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Getter
@NoArgsConstructor
public class ContatoRemovidoEvent extends DomainEvent {
    
    private TipoContato tipo;
    private String valor;
    
    public ContatoRemovidoEvent(String aggregateId, TipoContato tipo, String valor) {
        super(aggregateId, "SeguradoAggregate", 0);
        this.tipo = tipo;
        this.valor = valor;
    }
    
    @Override
    public String getEventType() {
        return "ContatoRemovido";
    }
}