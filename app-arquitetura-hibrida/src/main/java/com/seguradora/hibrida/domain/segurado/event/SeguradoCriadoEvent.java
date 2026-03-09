package com.seguradora.hibrida.domain.segurado.event;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Evento disparado quando um novo segurado é criado no sistema.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Getter
@NoArgsConstructor
public class SeguradoCriadoEvent extends DomainEvent {
    
    private String cpf;
    private String nome;
    private String email;
    private String telefone;
    private LocalDate dataNascimento;
    private Endereco endereco;
    
    public SeguradoCriadoEvent(String aggregateId, String cpf, String nome, 
                               String email, String telefone, LocalDate dataNascimento,
                               Endereco endereco) {
        super(aggregateId, "SeguradoAggregate", 0);
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.endereco = endereco;
    }
    
    @Override
    public String getEventType() {
        return "SeguradoCriado";
    }
}
