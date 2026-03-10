package com.seguradora.hibrida.domain.apolice.command;

import com.seguradora.hibrida.domain.apolice.event.ApoliceRenovadaEvent;
import com.seguradora.hibrida.domain.apolice.model.Cobertura;
import com.seguradora.hibrida.domain.apolice.model.FormaPagamento;
import com.seguradora.hibrida.domain.apolice.model.Valor;
import com.seguradora.hibrida.domain.apolice.model.Vigencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;

/**
 * Comando para renovação de apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class RenovarApoliceCommand {
    
    @NotBlank(message = "ID da apólice não pode ser vazio")
    private final String apoliceId;
    
    @NotNull(message = "Nova vigência não pode ser nula")
    @Valid
    private final Vigencia novaVigencia;
    
    @NotNull(message = "Novo valor segurado não pode ser nulo")
    @Valid
    private final Valor novoValorSegurado;
    
    @NotEmpty(message = "Deve haver pelo menos uma cobertura")
    @Valid
    private final List<Cobertura> novasCoberturas;
    
    @NotNull(message = "Nova forma de pagamento não pode ser nula")
    private final FormaPagamento novaFormaPagamento;
    
    @NotBlank(message = "ID do operador não pode ser vazio")
    private final String operadorId;
    
    @NotNull(message = "Tipo de renovação não pode ser nulo")
    private final ApoliceRenovadaEvent.TipoRenovacao tipoRenovacao;
    
    @Size(max = 1000, message = "Observações não podem exceder 1000 caracteres")
    private final String observacoes;
    
    private final boolean aplicarDesconto;
    private final Double percentualDesconto;
    
    /**
     * Construtor para criação do comando.
     */
    public RenovarApoliceCommand(
            String apoliceId,
            Vigencia novaVigencia,
            Valor novoValorSegurado,
            List<Cobertura> novasCoberturas,
            FormaPagamento novaFormaPagamento,
            String operadorId,
            ApoliceRenovadaEvent.TipoRenovacao tipoRenovacao,
            String observacoes,
            boolean aplicarDesconto,
            Double percentualDesconto) {
        
        this.apoliceId = apoliceId;
        this.novaVigencia = novaVigencia;
        this.novoValorSegurado = novoValorSegurado;
        this.novasCoberturas = novasCoberturas != null ? List.copyOf(novasCoberturas) : List.of();
        this.novaFormaPagamento = novaFormaPagamento;
        this.operadorId = operadorId;
        this.tipoRenovacao = tipoRenovacao;
        this.observacoes = observacoes;
        this.aplicarDesconto = aplicarDesconto;
        this.percentualDesconto = percentualDesconto;
    }
    
    /**
     * Factory method para renovação automática.
     */
    public static RenovarApoliceCommand automatica(
            String apoliceId,
            Vigencia novaVigencia,
            Valor novoValorSegurado,
            List<Cobertura> novasCoberturas,
            FormaPagamento novaFormaPagamento,
            String operadorId) {
        
        return new RenovarApoliceCommand(
                apoliceId,
                novaVigencia,
                novoValorSegurado,
                novasCoberturas,
                novaFormaPagamento,
                operadorId,
                ApoliceRenovadaEvent.TipoRenovacao.AUTOMATICA,
                "Renovação automática processada pelo sistema",
                true,
                5.0 // 5% de desconto para renovação automática
        );
    }
    
    /**
     * Factory method para renovação manual.
     */
    public static RenovarApoliceCommand manual(
            String apoliceId,
            Vigencia novaVigencia,
            Valor novoValorSegurado,
            List<Cobertura> novasCoberturas,
            FormaPagamento novaFormaPagamento,
            String operadorId,
            String observacoes) {
        
        return new RenovarApoliceCommand(
                apoliceId,
                novaVigencia,
                novoValorSegurado,
                novasCoberturas,
                novaFormaPagamento,
                operadorId,
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                observacoes,
                false,
                null
        );
    }
    
    /**
     * Factory method para renovação antecipada.
     */
    public static RenovarApoliceCommand antecipada(
            String apoliceId,
            Vigencia novaVigencia,
            Valor novoValorSegurado,
            List<Cobertura> novasCoberturas,
            FormaPagamento novaFormaPagamento,
            String operadorId,
            String observacoes,
            Double percentualDesconto) {
        
        return new RenovarApoliceCommand(
                apoliceId,
                novaVigencia,
                novoValorSegurado,
                novasCoberturas,
                novaFormaPagamento,
                operadorId,
                ApoliceRenovadaEvent.TipoRenovacao.ANTECIPADA,
                observacoes,
                percentualDesconto != null && percentualDesconto > 0,
                percentualDesconto
        );
    }
    
    /**
     * Factory method para renovação com alterações.
     */
    public static RenovarApoliceCommand comAlteracoes(
            String apoliceId,
            Vigencia novaVigencia,
            Valor novoValorSegurado,
            List<Cobertura> novasCoberturas,
            FormaPagamento novaFormaPagamento,
            String operadorId,
            String observacoes) {
        
        return new RenovarApoliceCommand(
                apoliceId,
                novaVigencia,
                novoValorSegurado,
                novasCoberturas,
                novaFormaPagamento,
                operadorId,
                ApoliceRenovadaEvent.TipoRenovacao.COM_ALTERACOES,
                observacoes,
                false,
                null
        );
    }
    
    // Getters
    public String getApoliceId() { return apoliceId; }
    public Vigencia getNovaVigencia() { return novaVigencia; }
    public Valor getNovoValorSegurado() { return novoValorSegurado; }
    public List<Cobertura> getNovasCoberturas() { return novasCoberturas; }
    public FormaPagamento getNovaFormaPagamento() { return novaFormaPagamento; }
    public String getOperadorId() { return operadorId; }
    public ApoliceRenovadaEvent.TipoRenovacao getTipoRenovacao() { return tipoRenovacao; }
    public String getObservacoes() { return observacoes; }
    public boolean isAplicarDesconto() { return aplicarDesconto; }
    public Double getPercentualDesconto() { return percentualDesconto; }
    
    /**
     * Verifica se é renovação automática.
     */
    public boolean isRenovacaoAutomatica() {
        return tipoRenovacao == ApoliceRenovadaEvent.TipoRenovacao.AUTOMATICA;
    }
    
    /**
     * Verifica se há alterações nas coberturas.
     */
    public boolean hasAlteracoesCoberturas() {
        return tipoRenovacao == ApoliceRenovadaEvent.TipoRenovacao.COM_ALTERACOES;
    }
    
    /**
     * Calcula o valor do desconto se aplicável.
     */
    public Valor calcularDesconto() {
        if (!aplicarDesconto || percentualDesconto == null || percentualDesconto <= 0) {
            return Valor.zero();
        }
        
        return novoValorSegurado.porcentagem(percentualDesconto);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RenovarApoliceCommand that = (RenovarApoliceCommand) obj;
        return aplicarDesconto == that.aplicarDesconto &&
               Objects.equals(apoliceId, that.apoliceId) &&
               Objects.equals(novaVigencia, that.novaVigencia) &&
               Objects.equals(novoValorSegurado, that.novoValorSegurado) &&
               Objects.equals(novasCoberturas, that.novasCoberturas) &&
               novaFormaPagamento == that.novaFormaPagamento &&
               Objects.equals(operadorId, that.operadorId) &&
               tipoRenovacao == that.tipoRenovacao &&
               Objects.equals(observacoes, that.observacoes) &&
               Objects.equals(percentualDesconto, that.percentualDesconto);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(apoliceId, novaVigencia, novoValorSegurado, novasCoberturas,
                           novaFormaPagamento, operadorId, tipoRenovacao, observacoes,
                           aplicarDesconto, percentualDesconto);
    }
    
    @Override
    public String toString() {
        return String.format("RenovarApoliceCommand{apoliceId='%s', novaVigencia=%s, " +
                           "novoValorSegurado=%s, tipoRenovacao=%s, operadorId='%s'}",
                           apoliceId, novaVigencia, novoValorSegurado, tipoRenovacao, operadorId);
    }
}