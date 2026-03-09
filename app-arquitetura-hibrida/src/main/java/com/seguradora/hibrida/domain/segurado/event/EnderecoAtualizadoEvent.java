package com.seguradora.hibrida.domain.segurado.event;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Evento disparado quando o endereço de um segurado é atualizado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Getter
@NoArgsConstructor
public class EnderecoAtualizadoEvent extends DomainEvent {
    
    private Endereco enderecoAnterior;
    private Endereco novoEndereco;
    
    public EnderecoAtualizadoEvent(String aggregateId, Endereco enderecoAnterior, Endereco novoEndereco) {
        super(aggregateId, "SeguradoAggregate", 0);
        this.enderecoAnterior = enderecoAnterior;
        this.novoEndereco = novoEndereco;
    }
    
    @Override
    public String getEventType() {
        return "EnderecoAtualizado";
    }
}