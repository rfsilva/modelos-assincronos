package com.seguradora.hibrida.domain.segurado.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;

/**
 * Value Object representando dados básicos de um Segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
public class Segurado {
    
    private String cpf;
    private String nome;
    private String email;
    private String telefone;
    private LocalDate dataNascimento;
    private Endereco endereco;
    private StatusSegurado status;
    private Instant dataCadastro;
    private Instant dataUltimaAtualizacao;
    
    public Segurado(String cpf, String nome, String email, String telefone, 
                    LocalDate dataNascimento, Endereco endereco) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.endereco = endereco;
        this.status = StatusSegurado.ATIVO;
        this.dataCadastro = Instant.now();
        this.dataUltimaAtualizacao = Instant.now();
    }
    
    public boolean isAtivo() {
        return this.status == StatusSegurado.ATIVO;
    }
    
    public boolean isMaiorIdade() {
        return dataNascimento != null && 
               LocalDate.now().minusYears(18).isAfter(dataNascimento);
    }
}
