package com.seguradora.hibrida.domain.segurado.event;

import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Evento disparado quando um contato é adicionado ao segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Getter
@NoArgsConstructor
public class ContatoAdicionadoEvent extends DomainEvent {
    
    private TipoContato tipo;
    private String valor;
    private boolean principal;
    
    public ContatoAdicionadoEvent(String aggregateId, TipoContato tipo, String valor, boolean principal) {
        super(aggregateId, "SeguradoAggregate", 0);
        this.tipo = tipo;
        this.valor = valor;
        this.principal = principal;
    }
    
    @Override
    public String getEventType() {
        return "ContatoAdicionado";
    }
}