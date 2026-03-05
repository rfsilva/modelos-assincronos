package com.seguradora.detran.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "veiculos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String placa;
    
    @Column(unique = true, nullable = false)
    private String renavam;
    
    @Column(name = "ano_fabricacao")
    private String anoFabricacao;
    
    @Column(name = "ano_modelo")
    private String anoModelo;
    
    @Column(name = "marca_modelo")
    private String marcaModelo;
    
    private String cor;
    private String combustivel;
    private String categoria;
    private String carroceria;
    private String especie;
    private String proprietario;
    private String municipio;
    private String situacao;
    
    @Column(name = "data_aquisicao")
    private LocalDate dataAquisicao;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}