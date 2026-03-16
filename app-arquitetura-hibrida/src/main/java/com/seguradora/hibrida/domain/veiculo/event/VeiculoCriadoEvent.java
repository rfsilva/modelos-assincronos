package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.domain.veiculo.model.*;

import java.time.Instant;
import java.util.Objects;

/**
 * Evento de domínio disparado quando um veículo é criado no sistema.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class VeiculoCriadoEvent extends DomainEvent {
    
    private static final String EVENT_TYPE = "VeiculoCriado";
    
    private final String placa;
    private final String renavam;
    private final String chassi;
    private final String marca;
    private final String modelo;
    private final int anoFabricacao;
    private final int anoModelo;
    private final String cor;
    private final String tipoCombustivel;
    private final String categoria;
    private final Integer cilindrada;
    private final String proprietarioCpfCnpj;
    private final String proprietarioNome;
    private final String proprietarioTipo;
    private final String operadorId;
    
    /**
     * Construtor do evento.
     */
    public VeiculoCriadoEvent(String aggregateId, long version, String placa, String renavam,
                             String chassi, String marca, String modelo, int anoFabricacao,
                             int anoModelo, String cor, String tipoCombustivel, String categoria,
                             Integer cilindrada, String proprietarioCpfCnpj, String proprietarioNome,
                             String proprietarioTipo, String operadorId) {
        super(aggregateId, "VeiculoAggregate", version);
        this.placa = validarPlaca(placa);
        this.renavam = validarRenavam(renavam);
        this.chassi = validarChassi(chassi);
        this.marca = validarMarca(marca);
        this.modelo = validarModelo(modelo);
        this.anoFabricacao = anoFabricacao;
        this.anoModelo = anoModelo;
        this.cor = cor;
        this.tipoCombustivel = tipoCombustivel;
        this.categoria = categoria;
        this.cilindrada = cilindrada;
        this.proprietarioCpfCnpj = proprietarioCpfCnpj;
        this.proprietarioNome = proprietarioNome;
        this.proprietarioTipo = proprietarioTipo;
        this.operadorId = operadorId;
    }
    
    /**
     * Factory method para criar o evento.
     */
    public static VeiculoCriadoEvent create(String aggregateId, long version, String placa,
                                           String renavam, String chassi, String marca, String modelo,
                                           int anoFabricacao, int anoModelo, Especificacao especificacao,
                                           Proprietario proprietario, String operadorId) {
        return new VeiculoCriadoEvent(
            aggregateId, version, placa, renavam, chassi, marca, modelo,
            anoFabricacao, anoModelo, especificacao.getCor(),
            especificacao.getTipoCombustivel().name(),
            especificacao.getCategoria().name(),
            especificacao.getCilindrada(),
            proprietario.getCpfCnpj(), proprietario.getNome(),
            proprietario.getTipoPessoa().name(),
            operadorId
        );
    }
    
    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
    
    // Getters
    public String getPlaca() { return placa; }
    public String getRenavam() { return renavam; }
    public String getChassi() { return chassi; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public int getAnoFabricacao() { return anoFabricacao; }
    public int getAnoModelo() { return anoModelo; }
    public String getCor() { return cor; }
    public String getTipoCombustivel() { return tipoCombustivel; }
    public String getCategoria() { return categoria; }
    public Integer getCilindrada() { return cilindrada; }
    public String getProprietarioCpfCnpj() { return proprietarioCpfCnpj; }
    public String getProprietarioNome() { return proprietarioNome; }
    public String getProprietarioTipo() { return proprietarioTipo; }
    public String getOperadorId() { return operadorId; }
    
    // Métodos auxiliares para projeções
    public Especificacao getEspecificacao() {
        return Especificacao.of(
            cor,
            TipoCombustivel.valueOf(tipoCombustivel),
            CategoriaVeiculo.valueOf(categoria),
            cilindrada
        );
    }
    
    public Proprietario getProprietario() {
        return Proprietario.of(
            proprietarioCpfCnpj,
            proprietarioNome,
            TipoPessoa.valueOf(proprietarioTipo)
        );
    }
    
    public StatusVeiculo getStatus() {
        return StatusVeiculo.ATIVO;
    }
    
    // Validações
    private String validarPlaca(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            throw new IllegalArgumentException("Placa não pode ser nula ou vazia");
        }
        return placa.trim().toUpperCase();
    }
    
    private String validarRenavam(String renavam) {
        if (renavam == null || renavam.trim().isEmpty()) {
            throw new IllegalArgumentException("RENAVAM não pode ser nulo ou vazio");
        }
        return renavam.replaceAll("\\D", "");
    }
    
    private String validarChassi(String chassi) {
        if (chassi == null || chassi.trim().isEmpty()) {
            throw new IllegalArgumentException("Chassi não pode ser nulo ou vazio");
        }
        return chassi.trim().toUpperCase();
    }
    
    private String validarMarca(String marca) {
        if (marca == null || marca.trim().isEmpty()) {
            throw new IllegalArgumentException("Marca não pode ser nula ou vazia");
        }
        if (marca.trim().length() > 50) {
            throw new IllegalArgumentException("Marca não pode ter mais de 50 caracteres");
        }
        return marca.trim();
    }
    
    private String validarModelo(String modelo) {
        if (modelo == null || modelo.trim().isEmpty()) {
            throw new IllegalArgumentException("Modelo não pode ser nulo ou vazio");
        }
        if (modelo.trim().length() > 100) {
            throw new IllegalArgumentException("Modelo não pode ter mais de 100 caracteres");
        }
        return modelo.trim();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        VeiculoCriadoEvent that = (VeiculoCriadoEvent) obj;
        return Objects.equals(getAggregateId(), that.getAggregateId()) &&
               getVersion() == that.getVersion() &&
               anoFabricacao == that.anoFabricacao &&
               anoModelo == that.anoModelo &&
               Objects.equals(placa, that.placa) &&
               Objects.equals(renavam, that.renavam) &&
               Objects.equals(chassi, that.chassi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAggregateId(), getVersion(), placa, renavam, chassi,
                           anoFabricacao, anoModelo);
    }
    
    @Override
    public String toString() {
        return String.format("VeiculoCriadoEvent{aggregateId='%s', placa='%s', marca='%s', modelo='%s'}",
            getAggregateId(), placa, marca, modelo);
    }
}