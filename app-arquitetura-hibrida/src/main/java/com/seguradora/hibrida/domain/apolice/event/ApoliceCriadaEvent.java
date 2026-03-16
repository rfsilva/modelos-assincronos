package com.seguradora.hibrida.domain.apolice.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.domain.apolice.model.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Evento disparado quando uma nova apólice é criada.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ApoliceCriadaEvent extends DomainEvent {
    
    @JsonProperty("numeroApolice")
    private final String numeroApolice;
    
    @JsonProperty("seguradoId")
    private final String seguradoId;
    
    @JsonProperty("produto")
    private final String produto;
    
    @JsonProperty("vigenciaInicio")
    private final String vigenciaInicio;
    
    @JsonProperty("vigenciaFim")
    private final String vigenciaFim;
    
    @JsonProperty("valorSegurado")
    private final String valorSegurado;
    
    @JsonProperty("formaPagamento")
    private final String formaPagamento;
    
    @JsonProperty("coberturas")
    private final List<Map<String, Object>> coberturas;
    
    @JsonProperty("premioTotal")
    private final String premioTotal;
    
    @JsonProperty("operadorId")
    private final String operadorId;
    
    @JsonCreator
    public ApoliceCriadaEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("numeroApolice") String numeroApolice,
            @JsonProperty("seguradoId") String seguradoId,
            @JsonProperty("produto") String produto,
            @JsonProperty("vigenciaInicio") String vigenciaInicio,
            @JsonProperty("vigenciaFim") String vigenciaFim,
            @JsonProperty("valorSegurado") String valorSegurado,
            @JsonProperty("formaPagamento") String formaPagamento,
            @JsonProperty("coberturas") List<Map<String, Object>> coberturas,
            @JsonProperty("premioTotal") String premioTotal,
            @JsonProperty("operadorId") String operadorId) {
        
        super(aggregateId, "ApoliceAggregate", 1);
        
        this.numeroApolice = validarNumeroApolice(numeroApolice);
        this.seguradoId = validarSeguradoId(seguradoId);
        this.produto = validarProduto(produto);
        this.vigenciaInicio = validarVigenciaInicio(vigenciaInicio);
        this.vigenciaFim = validarVigenciaFim(vigenciaFim);
        this.valorSegurado = validarValorSegurado(valorSegurado);
        this.formaPagamento = validarFormaPagamento(formaPagamento);
        this.coberturas = validarCoberturas(coberturas);
        this.premioTotal = validarPremioTotal(premioTotal);
        this.operadorId = validarOperadorId(operadorId);
    }
    
    /**
     * Factory method para criar o evento com objetos de domínio.
     */
    public static ApoliceCriadaEvent create(
            String apoliceId,
            NumeroApolice numeroApolice,
            String seguradoId,
            String produto,
            Vigencia vigencia,
            Valor valorSegurado,
            FormaPagamento formaPagamento,
            List<Cobertura> coberturas,
            Premio premio,
            String operadorId) {
        
        List<Map<String, Object>> coberturasData = coberturas.stream()
                .map(ApoliceCriadaEvent::serializarCobertura)
                .toList();
        
        return new ApoliceCriadaEvent(
                apoliceId,
                numeroApolice.getNumero(),
                seguradoId,
                produto,
                vigencia.getInicio().toString(),
                vigencia.getFim().toString(),
                valorSegurado.getQuantia().toString(),
                formaPagamento.name(),
                coberturasData,
                premio.getValorTotal().getQuantia().toString(),
                operadorId
        );
    }
    
    private static Map<String, Object> serializarCobertura(Cobertura cobertura) {
        return Map.of(
                "tipo", cobertura.getTipo().name(),
                "valorCobertura", cobertura.getValorCobertura().getQuantia().toString(),
                "franquia", cobertura.getFranquia().getQuantia().toString(),
                "carenciaDias", cobertura.getCarenciaDias(),
                "ativa", cobertura.isAtiva()
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
    
    private String validarProduto(String produto) {
        if (produto == null || produto.trim().isEmpty()) {
            throw new IllegalArgumentException("Produto não pode ser nulo ou vazio");
        }
        return produto.trim();
    }
    
    private String validarVigenciaInicio(String vigenciaInicio) {
        if (vigenciaInicio == null || vigenciaInicio.trim().isEmpty()) {
            throw new IllegalArgumentException("Data de início da vigência não pode ser nula ou vazia");
        }
        return vigenciaInicio.trim();
    }
    
    private String validarVigenciaFim(String vigenciaFim) {
        if (vigenciaFim == null || vigenciaFim.trim().isEmpty()) {
            throw new IllegalArgumentException("Data de fim da vigência não pode ser nula ou vazia");
        }
        return vigenciaFim.trim();
    }
    
    private String validarValorSegurado(String valorSegurado) {
        if (valorSegurado == null || valorSegurado.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor segurado não pode ser nulo ou vazio");
        }
        return valorSegurado.trim();
    }
    
    private String validarFormaPagamento(String formaPagamento) {
        if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
            throw new IllegalArgumentException("Forma de pagamento não pode ser nula ou vazia");
        }
        return formaPagamento.trim();
    }
    
    private List<Map<String, Object>> validarCoberturas(List<Map<String, Object>> coberturas) {
        if (coberturas == null || coberturas.isEmpty()) {
            throw new IllegalArgumentException("Lista de coberturas não pode ser nula ou vazia");
        }
        return coberturas;
    }
    
    private String validarPremioTotal(String premioTotal) {
        if (premioTotal == null || premioTotal.trim().isEmpty()) {
            throw new IllegalArgumentException("Prêmio total não pode ser nulo ou vazio");
        }
        return premioTotal.trim();
    }
    
    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }
    
    // Getters
    public String getNumeroApolice() { return numeroApolice; }
    public String getSeguradoId() { return seguradoId; }
    public String getProduto() { return produto; }
    public String getVigenciaInicio() { return vigenciaInicio; }
    public String getVigenciaFim() { return vigenciaFim; }
    public String getValorSegurado() { return valorSegurado; }
    public String getFormaPagamento() { return formaPagamento; }
    public List<Map<String, Object>> getCoberturas() { return coberturas; }
    public String getPremioTotal() { return premioTotal; }
    public String getOperadorId() { return operadorId; }
    
    @Override
    @JsonIgnore
    public String getEventType() {
        return "ApoliceCriadaEvent";
    }
    
    @Override
    public String toString() {
        return String.format("ApoliceCriadaEvent{aggregateId='%s', numeroApolice='%s', seguradoId='%s', produto='%s', timestamp=%s}",
                getAggregateId(), numeroApolice, seguradoId, produto, getTimestamp());
    }
}