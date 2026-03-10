package com.seguradora.hibrida.domain.apolice.command;

import com.seguradora.hibrida.domain.apolice.event.ApoliceCanceladaEvent;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Comando para cancelamento de apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class CancelarApoliceCommand {
    
    @NotBlank(message = "ID da apólice não pode ser vazio")
    private final String apoliceId;
    
    @NotBlank(message = "Motivo do cancelamento não pode ser vazio")
    @Size(min = 10, max = 500, message = "Motivo deve ter entre 10 e 500 caracteres")
    private final String motivo;
    
    @NotNull(message = "Data de efeito não pode ser nula")
    @Future(message = "Data de efeito deve ser futura")
    private final LocalDate dataEfeito;
    
    @NotBlank(message = "ID do operador não pode ser vazio")
    private final String operadorId;
    
    @Size(max = 1000, message = "Observações não podem exceder 1000 caracteres")
    private final String observacoes;
    
    @NotNull(message = "Tipo de cancelamento não pode ser nulo")
    private final ApoliceCanceladaEvent.TipoCancelamento tipoCancelamento;
    
    private final boolean calcularReembolso;
    
    /**
     * Construtor para criação do comando.
     */
    public CancelarApoliceCommand(
            String apoliceId,
            String motivo,
            LocalDate dataEfeito,
            String operadorId,
            String observacoes,
            ApoliceCanceladaEvent.TipoCancelamento tipoCancelamento,
            boolean calcularReembolso) {
        
        this.apoliceId = apoliceId;
        this.motivo = motivo;
        this.dataEfeito = dataEfeito;
        this.operadorId = operadorId;
        this.observacoes = observacoes;
        this.tipoCancelamento = tipoCancelamento;
        this.calcularReembolso = calcularReembolso;
    }
    
    /**
     * Factory method para cancelamento por solicitação do segurado.
     */
    public static CancelarApoliceCommand porSolicitacaoSegurado(
            String apoliceId,
            String motivo,
            LocalDate dataEfeito,
            String operadorId,
            String observacoes) {
        
        return new CancelarApoliceCommand(
                apoliceId,
                motivo,
                dataEfeito,
                operadorId,
                observacoes,
                ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO,
                true
        );
    }
    
    /**
     * Factory method para cancelamento por inadimplência.
     */
    public static CancelarApoliceCommand porInadimplencia(
            String apoliceId,
            String motivo,
            LocalDate dataEfeito,
            String operadorId,
            String observacoes) {
        
        return new CancelarApoliceCommand(
                apoliceId,
                motivo,
                dataEfeito,
                operadorId,
                observacoes,
                ApoliceCanceladaEvent.TipoCancelamento.INADIMPLENCIA,
                false
        );
    }
    
    /**
     * Factory method para cancelamento por fraude.
     */
    public static CancelarApoliceCommand porFraude(
            String apoliceId,
            String motivo,
            LocalDate dataEfeito,
            String operadorId,
            String observacoes) {
        
        return new CancelarApoliceCommand(
                apoliceId,
                motivo,
                dataEfeito,
                operadorId,
                observacoes,
                ApoliceCanceladaEvent.TipoCancelamento.FRAUDE,
                false
        );
    }
    
    /**
     * Factory method para cancelamento por decisão da seguradora.
     */
    public static CancelarApoliceCommand porDecisaoSeguradora(
            String apoliceId,
            String motivo,
            LocalDate dataEfeito,
            String operadorId,
            String observacoes) {
        
        return new CancelarApoliceCommand(
                apoliceId,
                motivo,
                dataEfeito,
                operadorId,
                observacoes,
                ApoliceCanceladaEvent.TipoCancelamento.DECISAO_SEGURADORA,
                true
        );
    }
    
    /**
     * Factory method para cancelamento por venda do veículo.
     */
    public static CancelarApoliceCommand porVendaVeiculo(
            String apoliceId,
            String motivo,
            LocalDate dataEfeito,
            String operadorId,
            String observacoes) {
        
        return new CancelarApoliceCommand(
                apoliceId,
                motivo,
                dataEfeito,
                operadorId,
                observacoes,
                ApoliceCanceladaEvent.TipoCancelamento.VENDA_VEICULO,
                true
        );
    }
    
    /**
     * Factory method para cancelamento por perda total.
     */
    public static CancelarApoliceCommand porPerdaTotal(
            String apoliceId,
            String motivo,
            LocalDate dataEfeito,
            String operadorId,
            String observacoes) {
        
        return new CancelarApoliceCommand(
                apoliceId,
                motivo,
                dataEfeito,
                operadorId,
                observacoes,
                ApoliceCanceladaEvent.TipoCancelamento.PERDA_TOTAL,
                false
        );
    }
    
    // Getters
    public String getApoliceId() { return apoliceId; }
    public String getMotivo() { return motivo; }
    public LocalDate getDataEfeito() { return dataEfeito; }
    public String getOperadorId() { return operadorId; }
    public String getObservacoes() { return observacoes; }
    public ApoliceCanceladaEvent.TipoCancelamento getTipoCancelamento() { return tipoCancelamento; }
    public boolean isCalcularReembolso() { return calcularReembolso; }
    
    /**
     * Verifica se o cancelamento permite reembolso.
     */
    public boolean permiteReembolso() {
        return calcularReembolso && (
                tipoCancelamento == ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO ||
                tipoCancelamento == ApoliceCanceladaEvent.TipoCancelamento.DECISAO_SEGURADORA ||
                tipoCancelamento == ApoliceCanceladaEvent.TipoCancelamento.VENDA_VEICULO
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CancelarApoliceCommand that = (CancelarApoliceCommand) obj;
        return calcularReembolso == that.calcularReembolso &&
               Objects.equals(apoliceId, that.apoliceId) &&
               Objects.equals(motivo, that.motivo) &&
               Objects.equals(dataEfeito, that.dataEfeito) &&
               Objects.equals(operadorId, that.operadorId) &&
               Objects.equals(observacoes, that.observacoes) &&
               tipoCancelamento == that.tipoCancelamento;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(apoliceId, motivo, dataEfeito, operadorId, 
                           observacoes, tipoCancelamento, calcularReembolso);
    }
    
    @Override
    public String toString() {
        return String.format("CancelarApoliceCommand{apoliceId='%s', motivo='%s', " +
                           "dataEfeito=%s, tipoCancelamento=%s, operadorId='%s'}",
                           apoliceId, motivo, dataEfeito, tipoCancelamento, operadorId);
    }
}