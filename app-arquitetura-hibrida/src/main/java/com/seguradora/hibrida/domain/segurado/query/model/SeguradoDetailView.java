package com.seguradora.hibrida.domain.segurado.query.model;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * View detalhada para consulta completa de segurados.
 * 
 * <p>Contém todos os dados do segurado incluindo histórico
 * de alterações e relacionamentos.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Entity
@Table(name = "tb_segurado_detail_view")
@Data
@NoArgsConstructor
public class SeguradoDetailView {
    
    @Id
    private String id;
    
    // Dados básicos
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;
    
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;
    
    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;
    
    @Column(name = "telefone", length = 11)
    private String telefone;
    
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusSegurado status;
    
    // Endereço desnormalizado
    @Column(name = "endereco_logradouro", length = 200)
    private String enderecoLogradouro;
    
    @Column(name = "endereco_numero", length = 20)
    private String enderecoNumero;
    
    @Column(name = "endereco_complemento", length = 100)
    private String enderecoComplemento;
    
    @Column(name = "endereco_bairro", length = 100)
    private String enderecoBairro;
    
    @Column(name = "endereco_cidade", length = 100)
    private String enderecoCidade;
    
    @Column(name = "endereco_estado", length = 2)
    private String enderecoEstado;
    
    @Column(name = "endereco_cep", length = 8)
    private String enderecoCep;
    
    @Column(name = "endereco_completo", length = 500)
    private String enderecoCompleto;
    
    // Metadados
    @Column(name = "data_criacao", nullable = false)
    private Instant dataCriacao;
    
    @Column(name = "data_ultima_atualizacao")
    private Instant dataUltimaAtualizacao;
    
    @Column(name = "operador_responsavel", length = 50)
    private String operadorResponsavel;
    
    @Column(name = "idade")
    private Integer idade;
    
    @Column(name = "total_contatos")
    private Integer totalContatos;
    
    @Column(name = "contatos_principais", length = 1000)
    private String contatosPrincipais; // JSON com contatos principais
    
    // Relacionamentos
    @Column(name = "total_apolices_ativas")
    private Integer totalApolicesAtivas = 0;
    
    @Column(name = "valor_total_apolices")
    private java.math.BigDecimal valorTotalApolices = java.math.BigDecimal.ZERO;
    
    @Column(name = "data_ultima_apolice")
    private Instant dataUltimaApolice;
    
    // Histórico resumido
    @Column(name = "total_alteracoes")
    private Integer totalAlteracoes = 0;
    
    @Column(name = "data_ultima_alteracao")
    private Instant dataUltimaAlteracao;
    
    /**
     * Construtor para criação inicial.
     */
    public SeguradoDetailView(String id, String cpf, String nome, String email, String telefone,
                             LocalDate dataNascimento, StatusSegurado status, String enderecoCompleto,
                             Instant dataCriacao, Integer idade) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.status = status;
        this.enderecoCompleto = enderecoCompleto;
        this.dataCriacao = dataCriacao;
        this.dataUltimaAtualizacao = dataCriacao;
        this.idade = idade;
        this.totalContatos = 2; // Email + telefone iniciais
        this.totalAlteracoes = 0;
    }
    
    /**
     * Atualiza contador de alterações.
     */
    public void incrementarAlteracoes() {
        this.totalAlteracoes++;
        this.dataUltimaAlteracao = Instant.now();
        this.dataUltimaAtualizacao = Instant.now();
    }
}