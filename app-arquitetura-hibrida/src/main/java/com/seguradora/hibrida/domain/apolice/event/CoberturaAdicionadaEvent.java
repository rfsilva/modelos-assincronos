package com.seguradora.hibrida.domain.apolice.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Evento disparado quando uma nova cobertura é adicionada a uma apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class CoberturaAdicionadaEvent extends DomainEvent {
    
    @JsonProperty("numeroApolice")
    private final String numeroApolice;
    
    @JsonProperty("seguradoId")
    private final String seguradoId;
    
    @JsonProperty("tipoCobertura")
    private final String tipoCobertura;
    
    @JsonProperty("valorCobertura")
    private final String valorCobertura;
    
    @JsonProperty("franquia")
    private final String franquia;
    
    @JsonProperty("carenciaDias")
    private final int carenciaDias;
    
    @JsonProperty("valorAdicionalPremio")
    private final String valorAdicionalPremio;
    
    @JsonProperty("dataEfeito")
    private final String dataEfeito;
    
    @JsonProperty("operadorId")
    private final String operadorId;
    
    @JsonProperty("motivo")
    private final String motivo;
    
    @JsonCreator
    public CoberturaAdicionadaEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("version") long version,
            @JsonProperty("numeroApolice") String numeroApolice,
            @JsonProperty("seguradoId") String seguradoId,
            @JsonProperty("tipoCobertura") String tipoCobertura,
            @JsonProperty("valorCobertura") String valorCobertura,
            @JsonProperty("franquia") String franquia,
            @JsonProperty("carenciaDias") int carenciaDias,
            @JsonProperty("valorAdicionalPremio") String valorAdicionalPremio,
            @JsonProperty("dataEfeito") String dataEfeito,
            @JsonProperty("operadorId") String operadorId,
            @JsonProperty("motivo") String motivo) {
        
        super(aggregateId, "ApoliceAggregate", version);
        
        this.numeroApolice = validarNumeroApolice(numeroApolice);
        this.seguradoId = validarSeguradoId(seguradoId);
        this.tipoCobertura = validarTipoCobertura(tipoCobertura);
        this.valorCobertura = validarValorCobertura(valorCobertura);
        this.franquia = validarFranquia(franquia);
        this.carenciaDias = validarCarenciaDias(carenciaDias);
        this.valorAdicionalPremio = validarValorAdicionalPremio(valorAdicionalPremio);
        this.dataEfeito = validarDataEfeito(dataEfeito);
        this.operadorId = validarOperadorId(operadorId);
        this.motivo = validarMotivo(motivo);
    }
    
    /**
     * Factory method para criar o evento.
     */
    public static CoberturaAdicionadaEvent create(
            String apoliceId,
            long version,
            String numeroApolice,
            String seguradoId,
            String tipoCobertura,
            String valorCobertura,
            String franquia,
            int carenciaDias,
            String valorAdicionalPremio,
            String dataEfeito,
            String operadorId,
            String motivo) {
        
        return new CoberturaAdicionadaEvent(
                apoliceId,
                version,
                numeroApolice,
                seguradoId,
                tipoCobertura,
                valorCobertura,
                franquia,
                carenciaDias,
                valorAdicionalPremio,
                dataEfeito,
                operadorId,
                motivo
        );
    }
    
    private String validarNumeroApolice(String numeroApolice) {
        if (numeroApolice == null || numeroApolice.trim().isEmpty()) {
            throw new IllegalArgumentException("Número da apólice não pode ser nulo ou vazio");
        }
        return numeroApolice.trim();
    }
    
    private String validarSeguradoId(String seguradoId) {
        if (seguradoId == null || seguradoId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do segurado não pode ser nulo ou vazio");
        }
        return seguradoId.trim();
    }
    
    private String validarTipoCobertura(String tipoCobertura) {
        if (tipoCobertura == null || tipoCobertura.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de cobertura não pode ser nulo ou vazio");
        }
        return tipoCobertura.trim();
    }
    
    private String validarValorCobertura(String valorCobertura) {
        if (valorCobertura == null || valorCobertura.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor da cobertura não pode ser nulo ou vazio");
        }
        return valorCobertura.trim();
    }
    
    private String validarFranquia(String franquia) {
        if (franquia == null || franquia.trim().isEmpty()) {
            throw new IllegalArgumentException("Franquia não pode ser nula ou vazia");
        }
        return franquia.trim();
    }
    
    private int validarCarenciaDias(int carenciaDias) {
        if (carenciaDias < 0 || carenciaDias > 365) {
            throw new IllegalArgumentException("Carência deve estar entre 0 e 365 dias");
        }
        return carenciaDias;
    }
    
    private String validarValorAdicionalPremio(String valorAdicionalPremio) {
        if (valorAdicionalPremio == null || valorAdicionalPremio.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor adicional do prêmio não pode ser nulo ou vazio");
        }
        return valorAdicionalPremio.trim();
    }
    
    private String validarDataEfeito(String dataEfeito) {
        if (dataEfeito == null || dataEfeito.trim().isEmpty()) {
            throw new IllegalArgumentException("Data de efeito não pode ser nula ou vazia");
        }
        return dataEfeito.trim();
    }
    
    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }
    
    private String validarMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da adição não pode ser nulo ou vazio");
        }
        return motivo.trim();
    }
    
    /**
     * Verifica se a cobertura tem carência.
     */
    public boolean temCarencia() {
        return carenciaDias > 0;
    }
    
    /**
     * Verifica se há valor adicional no prêmio.
     */
    public boolean temValorAdicional() {
        try {
            return Double.parseDouble(valorAdicionalPremio) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // Getters
    public String getNumeroApolice() { return numeroApolice; }
    public String getSeguradoId() { return seguradoId; }
    public String getTipoCobertura() { return tipoCobertura; }
    public String getValorCobertura() { return valorCobertura; }
    public String getFranquia() { return franquia; }
    public int getCarenciaDias() { return carenciaDias; }
    public String getValorAdicionalPremio() { return valorAdicionalPremio; }
    public String getDataEfeito() { return dataEfeito; }
    public String getOperadorId() { return operadorId; }
    public String getMotivo() { return motivo; }
    
    @Override
    public String getEventType() {
        return "CoberturaAdicionadaEvent";
    }
    
    @Override
    public String toString() {
        return String.format("CoberturaAdicionadaEvent{aggregateId='%s', numeroApolice='%s', tipoCobertura='%s', valorCobertura='%s', dataEfeito='%s', operadorId='%s', timestamp=%s}",
                super.getAggregateId(), numeroApolice, tipoCobertura, valorCobertura, dataEfeito, operadorId, super.getTimestamp());
    }
}