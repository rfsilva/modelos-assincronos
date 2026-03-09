package com.seguradora.hibrida.domain.segurado.query.model;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Modelo de leitura para Segurado (CQRS - Query Side).
 * 
 * <p>Entidade JPA otimizada para consultas, armazenada em banco de dados
 * separado (PostgreSQL) para garantir performance nas leituras.</p>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Entity
@Table(name = "segurado_query", indexes = {
    @Index(name = "idx_segurado_cpf", columnList = "cpf"),
    @Index(name = "idx_segurado_email", columnList = "email"),
    @Index(name = "idx_segurado_status", columnList = "status"),
    @Index(name = "idx_segurado_nome", columnList = "nome")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeguradoQueryModel {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;
    
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;
    
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Column(name = "telefone", nullable = false, length = 11)
    private String telefone;
    
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusSegurado status;
    
    // Endereço desnormalizado para performance
    @Column(name = "cep", length = 8)
    private String cep;
    
    @Column(name = "logradouro", length = 200)
    private String logradouro;
    
    @Column(name = "numero", length = 10)
    private String numero;
    
    @Column(name = "complemento", length = 100)
    private String complemento;
    
    @Column(name = "bairro", length = 100)
    private String bairro;
    
    @Column(name = "cidade", length = 100)
    private String cidade;
    
    @Column(name = "estado", length = 2)
    private String estado;
    
    // Metadados
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "version", nullable = false)
    private Long version;
    
    /**
     * Atualiza o timestamp de modificação automaticamente.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
    
    /**
     * Define o timestamp de criação automaticamente.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }
}
