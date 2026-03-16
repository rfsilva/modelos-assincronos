package com.seguradora.hibrida.domain.apolice.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Evento disparado quando uma apólice é cancelada.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ApoliceCanceladaEvent extends DomainEvent {
    
    @JsonProperty("numeroApolice")
    private final String numeroApolice;
    
    @JsonProperty("seguradoId")
    private final String seguradoId;
    
    @JsonProperty("valorSegurado")
    private final String valorSegurado;
    
    @JsonProperty("motivo")
    private final String motivo;
    
    @JsonProperty("dataEfeito")
    private final String dataEfeito;
    
    @JsonProperty("valorReembolso")
    private final String valorReembolso;
    
    @JsonProperty("operadorId")
    private final String operadorId;
    
    @JsonProperty("observacoes")
    private final String observacoes;
    
    @JsonProperty("tipoCancelamento")
    private final String tipoCancelamento;
    
    @JsonCreator
    public ApoliceCanceladaEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("version") long version,
            @JsonProperty("numeroApolice") String numeroApolice,
            @JsonProperty("seguradoId") String seguradoId,
            @JsonProperty("valorSegurado") String valorSegurado,
            @JsonProperty("motivo") String motivo,
            @JsonProperty("dataEfeito") String dataEfeito,
            @JsonProperty("valorReembolso") String valorReembolso,
            @JsonProperty("operadorId") String operadorId,
            @JsonProperty("observacoes") String observacoes,
            @JsonProperty("tipoCancelamento") String tipoCancelamento) {
        
        super(aggregateId, "ApoliceAggregate", version);
        
        this.numeroApolice = validarNumeroApolice(numeroApolice);
        this.seguradoId = validarSeguradoId(seguradoId);
        this.valorSegurado = validarValorSegurado(valorSegurado);
        this.motivo = validarMotivo(motivo);
        this.dataEfeito = validarDataEfeito(dataEfeito);
        this.valorReembolso = validarValorReembolso(valorReembolso);
        this.operadorId = validarOperadorId(operadorId);
        this.observacoes = observacoes; // Opcional
        this.tipoCancelamento = validarTipoCancelamento(tipoCancelamento);
    }
    
    /**
     * Factory method para criar o evento.
     */
    public static ApoliceCanceladaEvent create(
            String apoliceId,
            long version,
            String numeroApolice,
            String seguradoId,
            String valorSegurado,
            String motivo,
            LocalDate dataEfeito,
            String valorReembolso,
            String operadorId,
            String observacoes,
            TipoCancelamento tipoCancelamento) {
        
        return new ApoliceCanceladaEvent(
                apoliceId,
                version,
                numeroApolice,
                seguradoId,
                valorSegurado,
                motivo,
                dataEfeito.toString(),
                valorReembolso,
                operadorId,
                observacoes,
                tipoCancelamento.name()
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
    
    private String validarValorSegurado(String valorSegurado) {
        if (valorSegurado == null || valorSegurado.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor segurado não pode ser nulo ou vazio");
        }
        return valorSegurado.trim();
    }
    
    private String validarMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo do cancelamento não pode ser nulo ou vazio");
        }
        return motivo.trim();
    }
    
    private String validarDataEfeito(String dataEfeito) {
        if (dataEfeito == null || dataEfeito.trim().isEmpty()) {
            throw new IllegalArgumentException("Data de efeito do cancelamento não pode ser nula ou vazia");
        }
        return dataEfeito.trim();
    }
    
    private String validarValorReembolso(String valorReembolso) {
        if (valorReembolso == null || valorReembolso.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor de reembolso não pode ser nulo ou vazio");
        }
        return valorReembolso.trim();
    }
    
    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }
    
    private String validarTipoCancelamento(String tipoCancelamento) {
        if (tipoCancelamento == null || tipoCancelamento.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de cancelamento não pode ser nulo ou vazio");
        }
        return tipoCancelamento.trim();
    }
    
    /**
     * Verifica se o cancelamento foi por solicitação do segurado.
     */
    @JsonIgnore
    public boolean isCancelamentoPorSegurado() {
        return TipoCancelamento.SOLICITACAO_SEGURADO.name().equals(tipoCancelamento);
    }
    
    /**
     * Verifica se o cancelamento foi por inadimplência.
     */
    @JsonIgnore
    public boolean isCancelamentoPorInadimplencia() {
        return TipoCancelamento.INADIMPLENCIA.name().equals(tipoCancelamento);
    }
    
    /**
     * Verifica se há valor de reembolso.
     */
    @JsonIgnore
    public boolean temReembolso() {
        try {
            return Double.parseDouble(valorReembolso) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // Getters
    public String getNumeroApolice() { return numeroApolice; }
    public String getSeguradoId() { return seguradoId; }
    public String getValorSegurado() { return valorSegurado; }
    public String getMotivo() { return motivo; }
    public String getDataEfeito() { return dataEfeito; }
    public String getValorReembolso() { return valorReembolso; }
    public String getOperadorId() { return operadorId; }
    public String getObservacoes() { return observacoes; }
    public String getTipoCancelamento() { return tipoCancelamento; }
    
    @Override
    @JsonIgnore
    public String getEventType() {
        return "ApoliceCanceladaEvent";
    }
    
    @Override
    public String toString() {
        return String.format("ApoliceCanceladaEvent{aggregateId='%s', numeroApolice='%s', motivo='%s', dataEfeito='%s', operadorId='%s', timestamp=%s}",
                getAggregateId(), numeroApolice, motivo, dataEfeito, operadorId, getTimestamp());
    }
    
    /**
     * Enum para tipos de cancelamento.
     */
    public enum TipoCancelamento {
        SOLICITACAO_SEGURADO("Solicitação do Segurado"),
        INADIMPLENCIA("Inadimplência"),
        FRAUDE("Fraude Detectada"),
        DECISAO_SEGURADORA("Decisão da Seguradora"),
        VENDA_VEICULO("Venda do Veículo"),
        PERDA_TOTAL("Perda Total do Veículo");
        
        private final String descricao;
        
        TipoCancelamento(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
}