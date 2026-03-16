package com.seguradora.hibrida.domain.veiculo.query.model;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

/**
 * Modelo de consulta (projeção) para veículos.
 * 
 * <p>Esta classe representa a projeção desnormalizada dos dados de veículo
 * otimizada para consultas de leitura. Contém dados agregados de múltiplos
 * eventos para facilitar consultas complexas.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Entity
@Table(name = "veiculo_query_view", schema = "projections")
public class VeiculoQueryModel {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    // Dados básicos do veículo
    @Column(name = "placa", length = 8, nullable = false, unique = true)
    private String placa;
    
    @Column(name = "renavam", length = 11, nullable = false, unique = true)
    private String renavam;
    
    @Column(name = "chassi", length = 17, nullable = false, unique = true)
    private String chassi;
    
    @Column(name = "marca", length = 50, nullable = false)
    private String marca;
    
    @Column(name = "modelo", length = 100, nullable = false)
    private String modelo;
    
    @Column(name = "ano_fabricacao", nullable = false)
    private Integer anoFabricacao;
    
    @Column(name = "ano_modelo", nullable = false)
    private Integer anoModelo;
    
    // Especificações
    @Column(name = "cor", length = 50)
    private String cor;
    
    @Column(name = "tipo_combustivel", length = 20)
    private String tipoCombustivel;
    
    @Column(name = "categoria", length = 20)
    private String categoria;
    
    @Column(name = "cilindrada")
    private Integer cilindrada;
    
    // Proprietário
    @Column(name = "proprietario_cpf", length = 14, nullable = false)
    private String proprietarioCpf;
    
    @Column(name = "proprietario_nome", length = 100, nullable = false)
    private String proprietarioNome;
    
    @Column(name = "proprietario_tipo", length = 20, nullable = false)
    private String proprietarioTipo;
    
    // Status e relacionamentos
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusVeiculo status;
    
    @Column(name = "apolice_ativa", nullable = false)
    private Boolean apoliceAtiva = false;
    
    // Dados geográficos (para consultas por localização)
    @Column(name = "cidade", length = 100)
    private String cidade;
    
    @Column(name = "estado", length = 2)
    private String estado;
    
    @Column(name = "regiao", length = 20)
    private String regiao;
    
    // Metadados de controle
    @Column(name = "version", nullable = false)
    private Long version = 0L;
    
    @Column(name = "last_event_id")
    private Long lastEventId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    /**
     * Construtor padrão para JPA.
     */
    public VeiculoQueryModel() {
    }
    
    /**
     * Atualiza timestamp de modificação.
     */
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
    
    /**
     * Verifica se o veículo está ativo.
     */
    public boolean isAtivo() {
        return StatusVeiculo.ATIVO.equals(status);
    }
    
    /**
     * Verifica se o veículo tem apólice ativa.
     */
    public boolean temApoliceAtiva() {
        return Boolean.TRUE.equals(apoliceAtiva);
    }
    
    /**
     * Calcula a idade do veículo em anos.
     */
    public int getIdade() {
        return java.time.Year.now().getValue() - anoFabricacao;
    }
    
    /**
     * Retorna descrição completa do veículo.
     */
    public String getDescricaoCompleta() {
        return String.format("%s %s %d/%d - %s", marca, modelo, anoFabricacao, anoModelo, placa);
    }
    
    // Getters e Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPlaca() {
        return placa;
    }
    
    public void setPlaca(String placa) {
        this.placa = placa;
    }
    
    public String getRenavam() {
        return renavam;
    }
    
    public void setRenavam(String renavam) {
        this.renavam = renavam;
    }
    
    public String getChassi() {
        return chassi;
    }
    
    public void setChassi(String chassi) {
        this.chassi = chassi;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public Integer getAnoFabricacao() {
        return anoFabricacao;
    }
    
    public void setAnoFabricacao(Integer anoFabricacao) {
        this.anoFabricacao = anoFabricacao;
    }
    
    public Integer getAnoModelo() {
        return anoModelo;
    }
    
    public void setAnoModelo(Integer anoModelo) {
        this.anoModelo = anoModelo;
    }
    
    public String getCor() {
        return cor;
    }
    
    public void setCor(String cor) {
        this.cor = cor;
    }
    
    public String getTipoCombustivel() {
        return tipoCombustivel;
    }
    
    public void setTipoCombustivel(String tipoCombustivel) {
        this.tipoCombustivel = tipoCombustivel;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public Integer getCilindrada() {
        return cilindrada;
    }
    
    public void setCilindrada(Integer cilindrada) {
        this.cilindrada = cilindrada;
    }
    
    public String getProprietarioCpf() {
        return proprietarioCpf;
    }
    
    public void setProprietarioCpf(String proprietarioCpf) {
        this.proprietarioCpf = proprietarioCpf;
    }
    
    public String getProprietarioNome() {
        return proprietarioNome;
    }
    
    public void setProprietarioNome(String proprietarioNome) {
        this.proprietarioNome = proprietarioNome;
    }
    
    public String getProprietarioTipo() {
        return proprietarioTipo;
    }
    
    public void setProprietarioTipo(String proprietarioTipo) {
        this.proprietarioTipo = proprietarioTipo;
    }
    
    public StatusVeiculo getStatus() {
        return status;
    }
    
    public void setStatus(StatusVeiculo status) {
        this.status = status;
    }
    
    public Boolean getApoliceAtiva() {
        return apoliceAtiva;
    }
    
    public void setApoliceAtiva(Boolean apoliceAtiva) {
        this.apoliceAtiva = apoliceAtiva;
    }
    
    public String getCidade() {
        return cidade;
    }
    
    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getRegiao() {
        return regiao;
    }
    
    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    public Long getLastEventId() {
        return lastEventId;
    }
    
    public void setLastEventId(Long lastEventId) {
        this.lastEventId = lastEventId;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        VeiculoQueryModel that = (VeiculoQueryModel) obj;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("VeiculoQueryModel{id='%s', placa='%s', marca='%s', modelo='%s', status=%s}",
            id, placa, marca, modelo, status != null ? status.name() : "null");
    }
}