package com.seguradora.hibrida.domain.apolice.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Evento disparado quando uma apólice é atualizada.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ApoliceAtualizadaEvent extends DomainEvent {
    
    @JsonProperty("numeroApolice")
    private final String numeroApolice;
    
    @JsonProperty("seguradoId")
    private final String seguradoId;
    
    @JsonProperty("alteracoes")
    private final Map<String, Object> alteracoes;
    
    @JsonProperty("valoresAnteriores")
    private final Map<String, Object> valoresAnteriores;
    
    @JsonProperty("novosValores")
    private final Map<String, Object> novosValores;
    
    @JsonProperty("operadorId")
    private final String operadorId;
    
    @JsonProperty("motivo")
    private final String motivo;
    
    @JsonCreator
    public ApoliceAtualizadaEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("version") long version,
            @JsonProperty("numeroApolice") String numeroApolice,
            @JsonProperty("seguradoId") String seguradoId,
            @JsonProperty("alteracoes") Map<String, Object> alteracoes,
            @JsonProperty("valoresAnteriores") Map<String, Object> valoresAnteriores,
            @JsonProperty("novosValores") Map<String, Object> novosValores,
            @JsonProperty("operadorId") String operadorId,
            @JsonProperty("motivo") String motivo) {
        
        super(aggregateId, "ApoliceAggregate", version);
        
        this.numeroApolice = validarNumeroApolice(numeroApolice);
        this.seguradoId = validarSeguradoId(seguradoId);
        this.alteracoes = validarAlteracoes(alteracoes);
        this.valoresAnteriores = validarValoresAnteriores(valoresAnteriores);
        this.novosValores = validarNovosValores(novosValores);
        this.operadorId = validarOperadorId(operadorId);
        this.motivo = validarMotivo(motivo);
    }
    
    /**
     * Factory method para criar o evento.
     */
    public static ApoliceAtualizadaEvent create(
            String apoliceId,
            long version,
            String numeroApolice,
            String seguradoId,
            Map<String, Object> alteracoes,
            Map<String, Object> valoresAnteriores,
            Map<String, Object> novosValores,
            String operadorId,
            String motivo) {
        
        return new ApoliceAtualizadaEvent(
                apoliceId,
                version,
                numeroApolice,
                seguradoId,
                alteracoes,
                valoresAnteriores,
                novosValores,
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
    
    private Map<String, Object> validarAlteracoes(Map<String, Object> alteracoes) {
        if (alteracoes == null || alteracoes.isEmpty()) {
            throw new IllegalArgumentException("Mapa de alterações não pode ser nulo ou vazio");
        }
        return alteracoes;
    }
    
    private Map<String, Object> validarValoresAnteriores(Map<String, Object> valoresAnteriores) {
        if (valoresAnteriores == null) {
            throw new IllegalArgumentException("Valores anteriores não podem ser nulos");
        }
        return valoresAnteriores;
    }
    
    private Map<String, Object> validarNovosValores(Map<String, Object> novosValores) {
        if (novosValores == null) {
            throw new IllegalArgumentException("Novos valores não podem ser nulos");
        }
        return novosValores;
    }
    
    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }
    
    private String validarMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da alteração não pode ser nulo ou vazio");
        }
        return motivo.trim();
    }
    
    /**
     * Verifica se um campo específico foi alterado.
     */
    public boolean foiAlterado(String campo) {
        return alteracoes.containsKey(campo);
    }
    
    /**
     * Retorna o valor anterior de um campo.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValorAnterior(String campo) {
        return (T) valoresAnteriores.get(campo);
    }
    
    /**
     * Retorna o novo valor de um campo.
     */
    @SuppressWarnings("unchecked")
    public <T> T getNovoValor(String campo) {
        return (T) novosValores.get(campo);
    }
    
    /**
     * Retorna uma descrição das alterações realizadas.
     */
    public String getDescricaoAlteracoes() {
        StringBuilder sb = new StringBuilder();
        
        for (String campo : alteracoes.keySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            
            Object valorAnterior = valoresAnteriores.get(campo);
            Object novoValor = novosValores.get(campo);
            
            sb.append(String.format("%s: %s → %s", campo, valorAnterior, novoValor));
        }
        
        return sb.toString();
    }
    
    // Getters
    public String getNumeroApolice() { return numeroApolice; }
    public String getSeguradoId() { return seguradoId; }
    public Map<String, Object> getAlteracoes() { return alteracoes; }
    public Map<String, Object> getValoresAnteriores() { return valoresAnteriores; }
    public Map<String, Object> getNovosValores() { return novosValores; }
    public String getOperadorId() { return operadorId; }
    public String getMotivo() { return motivo; }
    
    @Override
    public String getEventType() {
        return "ApoliceAtualizadaEvent";
    }
    
    @Override
    public String toString() {
        return String.format("ApoliceAtualizadaEvent{aggregateId='%s', numeroApolice='%s', alteracoes=%s, operadorId='%s', timestamp=%s}",
                getAggregateId(), numeroApolice, alteracoes.keySet(), operadorId, getTimestamp());
    }
}