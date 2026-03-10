package com.seguradora.hibrida.domain.apolice.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Evento disparado quando uma apólice é renovada.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ApoliceRenovadaEvent extends DomainEvent {
    
    @JsonProperty("numeroApolice")
    private final String numeroApolice;
    
    @JsonProperty("seguradoId")
    private final String seguradoId;
    
    @JsonProperty("novaVigenciaInicio")
    private final String novaVigenciaInicio;
    
    @JsonProperty("novaVigenciaFim")
    private final String novaVigenciaFim;
    
    @JsonProperty("novoValorSegurado")
    private final String novoValorSegurado;
    
    @JsonProperty("novoPremioTotal")
    private final String novoPremioTotal;
    
    @JsonProperty("alteracoesCoberturas")
    private final List<Map<String, Object>> alteracoesCoberturas;
    
    @JsonProperty("novaFormaPagamento")
    private final String novaFormaPagamento;
    
    @JsonProperty("operadorId")
    private final String operadorId;
    
    @JsonProperty("tipoRenovacao")
    private final String tipoRenovacao;
    
    @JsonProperty("observacoes")
    private final String observacoes;
    
    @JsonCreator
    public ApoliceRenovadaEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("version") long version,
            @JsonProperty("numeroApolice") String numeroApolice,
            @JsonProperty("seguradoId") String seguradoId,
            @JsonProperty("novaVigenciaInicio") String novaVigenciaInicio,
            @JsonProperty("novaVigenciaFim") String novaVigenciaFim,
            @JsonProperty("novoValorSegurado") String novoValorSegurado,
            @JsonProperty("novoPremioTotal") String novoPremioTotal,
            @JsonProperty("alteracoesCoberturas") List<Map<String, Object>> alteracoesCoberturas,
            @JsonProperty("novaFormaPagamento") String novaFormaPagamento,
            @JsonProperty("operadorId") String operadorId,
            @JsonProperty("tipoRenovacao") String tipoRenovacao,
            @JsonProperty("observacoes") String observacoes) {
        
        super(aggregateId, "ApoliceAggregate", version);
        
        this.numeroApolice = validarNumeroApolice(numeroApolice);
        this.seguradoId = validarSeguradoId(seguradoId);
        this.novaVigenciaInicio = validarNovaVigenciaInicio(novaVigenciaInicio);
        this.novaVigenciaFim = validarNovaVigenciaFim(novaVigenciaFim);
        this.novoValorSegurado = validarNovoValorSegurado(novoValorSegurado);
        this.novoPremioTotal = validarNovoPremioTotal(novoPremioTotal);
        this.alteracoesCoberturas = alteracoesCoberturas; // Pode ser vazio
        this.novaFormaPagamento = validarNovaFormaPagamento(novaFormaPagamento);
        this.operadorId = validarOperadorId(operadorId);
        this.tipoRenovacao = validarTipoRenovacao(tipoRenovacao);
        this.observacoes = observacoes; // Opcional
    }
    
    /**
     * Factory method para criar o evento.
     */
    public static ApoliceRenovadaEvent create(
            String apoliceId,
            long version,
            String numeroApolice,
            String seguradoId,
            String novaVigenciaInicio,
            String novaVigenciaFim,
            String novoValorSegurado,
            String novoPremioTotal,
            List<Map<String, Object>> alteracoesCoberturas,
            String novaFormaPagamento,
            String operadorId,
            TipoRenovacao tipoRenovacao,
            String observacoes) {
        
        return new ApoliceRenovadaEvent(
                apoliceId,
                version,
                numeroApolice,
                seguradoId,
                novaVigenciaInicio,
                novaVigenciaFim,
                novoValorSegurado,
                novoPremioTotal,
                alteracoesCoberturas,
                novaFormaPagamento,
                operadorId,
                tipoRenovacao.name(),
                observacoes
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
    
    private String validarNovaVigenciaInicio(String novaVigenciaInicio) {
        if (novaVigenciaInicio == null || novaVigenciaInicio.trim().isEmpty()) {
            throw new IllegalArgumentException("Nova data de início da vigência não pode ser nula ou vazia");
        }
        return novaVigenciaInicio.trim();
    }
    
    private String validarNovaVigenciaFim(String novaVigenciaFim) {
        if (novaVigenciaFim == null || novaVigenciaFim.trim().isEmpty()) {
            throw new IllegalArgumentException("Nova data de fim da vigência não pode ser nula ou vazia");
        }
        return novaVigenciaFim.trim();
    }
    
    private String validarNovoValorSegurado(String novoValorSegurado) {
        if (novoValorSegurado == null || novoValorSegurado.trim().isEmpty()) {
            throw new IllegalArgumentException("Novo valor segurado não pode ser nulo ou vazio");
        }
        return novoValorSegurado.trim();
    }
    
    private String validarNovoPremioTotal(String novoPremioTotal) {
        if (novoPremioTotal == null || novoPremioTotal.trim().isEmpty()) {
            throw new IllegalArgumentException("Novo prêmio total não pode ser nulo ou vazio");
        }
        return novoPremioTotal.trim();
    }
    
    private String validarNovaFormaPagamento(String novaFormaPagamento) {
        if (novaFormaPagamento == null || novaFormaPagamento.trim().isEmpty()) {
            throw new IllegalArgumentException("Nova forma de pagamento não pode ser nula ou vazia");
        }
        return novaFormaPagamento.trim();
    }
    
    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }
    
    private String validarTipoRenovacao(String tipoRenovacao) {
        if (tipoRenovacao == null || tipoRenovacao.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de renovação não pode ser nulo ou vazio");
        }
        return tipoRenovacao.trim();
    }
    
    /**
     * Verifica se houve alterações nas coberturas.
     */
    public boolean houveAlteracoesCoberturas() {
        return alteracoesCoberturas != null && !alteracoesCoberturas.isEmpty();
    }
    
    /**
     * Verifica se foi renovação automática.
     */
    public boolean isRenovacaoAutomatica() {
        return TipoRenovacao.AUTOMATICA.name().equals(tipoRenovacao);
    }
    
    /**
     * Verifica se foi renovação manual.
     */
    public boolean isRenovacaoManual() {
        return TipoRenovacao.MANUAL.name().equals(tipoRenovacao);
    }
    
    // Getters
    public String getNumeroApolice() { return numeroApolice; }
    public String getSeguradoId() { return seguradoId; }
    public String getNovaVigenciaInicio() { return novaVigenciaInicio; }
    public String getNovaVigenciaFim() { return novaVigenciaFim; }
    public String getNovoValorSegurado() { return novoValorSegurado; }
    public String getNovoPremioTotal() { return novoPremioTotal; }
    public List<Map<String, Object>> getAlteracoesCoberturas() { return alteracoesCoberturas; }
    public String getNovaFormaPagamento() { return novaFormaPagamento; }
    public String getOperadorId() { return operadorId; }
    public String getTipoRenovacao() { return tipoRenovacao; }
    public String getObservacoes() { return observacoes; }
    
    @Override
    public String getEventType() {
        return "ApoliceRenovadaEvent";
    }
    
    @Override
    public String toString() {
        return String.format("ApoliceRenovadaEvent{aggregateId='%s', numeroApolice='%s', novaVigencia='%s a %s', tipoRenovacao='%s', operadorId='%s', timestamp=%s}",
                getAggregateId(), numeroApolice, novaVigenciaInicio, novaVigenciaFim, tipoRenovacao, operadorId, getTimestamp());
    }
    
    /**
     * Enum para tipos de renovação.
     */
    public enum TipoRenovacao {
        AUTOMATICA("Renovação Automática"),
        MANUAL("Renovação Manual"),
        ANTECIPADA("Renovação Antecipada"),
        COM_ALTERACOES("Renovação com Alterações"),
        NEGOCIADA("Renovação Negociada");
        
        private final String descricao;
        
        TipoRenovacao(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
}