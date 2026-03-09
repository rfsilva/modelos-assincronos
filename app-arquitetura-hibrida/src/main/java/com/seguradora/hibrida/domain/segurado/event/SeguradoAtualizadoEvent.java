package com.seguradora.hibrida.domain.segurado.event;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Evento disparado quando dados de um segurado são atualizados.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Getter
@NoArgsConstructor
public class SeguradoAtualizadoEvent extends DomainEvent {
    
    private String nome;
    private String email;
    private String telefone;
    private LocalDate dataNascimento;
    private Endereco endereco;
    
    public SeguradoAtualizadoEvent(String aggregateId, String nome, 
                                   String email, String telefone, 
                                   LocalDate dataNascimento, Endereco endereco) {
        super(aggregateId, "SeguradoAggregate", 0);
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.endereco = endereco;
    }
    
    @Override
    public String getEventType() {
        return "SeguradoAtualizado";
    }
}
