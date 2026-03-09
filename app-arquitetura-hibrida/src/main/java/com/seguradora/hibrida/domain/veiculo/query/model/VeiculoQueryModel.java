package com.seguradora.hibrida.domain.veiculo.query.model;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Query Model para consultas de veículo.
 * Contém dados desnormalizados otimizados para leitura.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Entity
@Table(name = "veiculo_query", indexes = {
    @Index(name = "idx_veiculo_placa", columnList = "placa", unique = true),
    @Index(name = "idx_veiculo_renavam", columnList = "renavam", unique = true),
    @Index(name = "idx_veiculo_proprietario_cpf", columnList = "proprietario_cpf"),
    @Index(name = "idx_veiculo_marca_modelo", columnList = "marca,modelo"),
    @Index(name = "idx_veiculo_cidade_estado", columnList = "cidade,estado"),
    @Index(name = "idx_veiculo_status_ano", columnList = "status,ano_fabricacao")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoQueryModel {

    @Id
    private String id;
    
    @Column(nullable = false, unique = true, length = 10)
    private String placa;
    
    @Column(nullable = false, unique = true, length = 11)
    private String renavam;
    
    @Column(nullable = false, unique = true, length = 17)
    private String chassi;
    
    @Column(nullable = false, length = 100)
    private String marca;
    
    @Column(nullable = false, length = 100)
    private String modelo;
    
    @Column(name = "ano_fabricacao", nullable = false)
    private Integer anoFabricacao;
    
    @Column(name = "ano_modelo", nullable = false)
    private Integer anoModelo;
    
    @Column(nullable = false, length = 50)
    private String cor;
    
    @Column(name = "tipo_combustivel", nullable = false, length = 20)
    private String tipoCombustivel;
    
    @Column(nullable = false, length = 20)
    private String categoria;
    
    @Column(nullable = false)
    private Integer cilindrada;
    
    @Column(name = "proprietario_cpf", nullable = false, length = 14)
    private String proprietarioCpf;
    
    @Column(name = "proprietario_nome", nullable = false, length = 200)
    private String proprietarioNome;
    
    @Column(name = "proprietario_tipo", nullable = false, length = 10)
    private String proprietarioTipo;
    
    @Column(length = 100)
    private String cidade;
    
    @Column(length = 2)
    private String estado;
    
    @Column(length = 20)
    private String regiao;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVeiculo status;
    
    @Column(name = "apolice_ativa")
    private Boolean apoliceAtiva;
    
    @Column(name = "quantidade_apolices")
    private Integer quantidadeApolices;
    
    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
    
    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @Version
    private Long version;
}
