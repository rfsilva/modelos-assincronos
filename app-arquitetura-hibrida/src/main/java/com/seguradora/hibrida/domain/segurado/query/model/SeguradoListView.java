package com.seguradora.hibrida.domain.segurado.query.model;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * View otimizada para listagem de segurados.
 * 
 * <p>Contém apenas os campos essenciais para listagens,
 * otimizando performance de consultas.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Entity
@Table(name = "tb_segurado_list_view", indexes = {
    @Index(name = "idx_segurado_list_cpf", columnList = "cpf", unique = true),
    @Index(name = "idx_segurado_list_email", columnList = "email", unique = true),
    @Index(name = "idx_segurado_list_nome_status", columnList = "nome, status"),
    @Index(name = "idx_segurado_list_cidade_status", columnList = "cidade, status"),
    @Index(name = "idx_segurado_list_data_criacao", columnList = "data_criacao")
})
@Data
@NoArgsConstructor
public class SeguradoListView {
    
    @Id
    private String id;
    
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;
    
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;
    
    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusSegurado status;
    
    @Column(name = "cidade", length = 100)
    private String cidade;
    
    @Column(name = "estado", length = 2)
    private String estado;
    
    @Column(name = "data_criacao", nullable = false)
    private Instant dataCriacao;
    
    @Column(name = "data_ultima_atualizacao")
    private Instant dataUltimaAtualizacao;
    
    @Column(name = "idade")
    private Integer idade;
    
    @Column(name = "total_contatos")
    private Integer totalContatos;
    
    @Column(name = "tem_whatsapp")
    private Boolean temWhatsapp;
    
    public SeguradoListView(String id, String cpf, String nome, String email, 
                           StatusSegurado status, String cidade, String estado,
                           Instant dataCriacao, Integer idade) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.status = status;
        this.cidade = cidade;
        this.estado = estado;
        this.dataCriacao = dataCriacao;
        this.dataUltimaAtualizacao = dataCriacao;
        this.idade = idade;
        this.totalContatos = 2; // Email + telefone iniciais
        this.temWhatsapp = false;
    }
}