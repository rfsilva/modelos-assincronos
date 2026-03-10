package com.seguradora.hibrida.domain.apolice.command;

import com.seguradora.hibrida.domain.apolice.model.Cobertura;
import com.seguradora.hibrida.domain.apolice.model.Valor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;

/**
 * Comando para atualização de dados da apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class AtualizarApoliceCommand {
    
    @NotBlank(message = "ID da apólice não pode ser vazio")
    private final String apoliceId;
    
    @NotNull(message = "Novo valor segurado não pode ser nulo")
    @Valid
    private final Valor novoValorSegurado;
    
    @NotEmpty(message = "Deve haver pelo menos uma cobertura")
    @Valid
    private final List<Cobertura> novasCoberturas;
    
    @NotBlank(message = "ID do operador não pode ser vazio")
    private final String operadorId;
    
    @NotBlank(message = "Motivo da alteração não pode ser vazio")
    @Size(min = 10, max = 500, message = "Motivo deve ter entre 10 e 500 caracteres")
    private final String motivo;
    
    @Min(value = 0, message = "Versão esperada deve ser não negativa")
    private final Long versaoEsperada;
    
    @Size(max = 1000, message = "Observações não podem exceder 1000 caracteres")
    private final String observacoes;
    
    /**
     * Construtor para criação do comando.
     */
    public AtualizarApoliceCommand(
            String apoliceId,
            Valor novoValorSegurado,
            List<Cobertura> novasCoberturas,
            String operadorId,
            String motivo,
            Long versaoEsperada,
            String observacoes) {
        
        this.apoliceId = apoliceId;
        this.novoValorSegurado = novoValorSegurado;
        this.novasCoberturas = novasCoberturas != null ? List.copyOf(novasCoberturas) : List.of();
        this.operadorId = operadorId;
        this.motivo = motivo;
        this.versaoEsperada = versaoEsperada;
        this.observacoes = observacoes;
    }
    
    /**
     * Factory method para criação simplificada.
     */
    public static AtualizarApoliceCommand of(
            String apoliceId,
            Valor novoValorSegurado,
            List<Cobertura> novasCoberturas,
            String operadorId,
            String motivo) {
        
        return new AtualizarApoliceCommand(
                apoliceId,
                novoValorSegurado,
                novasCoberturas,
                operadorId,
                motivo,
                null,
                null
        );
    }
    
    /**
     * Factory method com controle de versão.
     */
    public static AtualizarApoliceCommand withVersion(
            String apoliceId,
            Valor novoValorSegurado,
            List<Cobertura> novasCoberturas,
            String operadorId,
            String motivo,
            Long versaoEsperada) {
        
        return new AtualizarApoliceCommand(
                apoliceId,
                novoValorSegurado,
                novasCoberturas,
                operadorId,
                motivo,
                versaoEsperada,
                null
        );
    }
    
    // Getters
    public String getApoliceId() { return apoliceId; }
    public Valor getNovoValorSegurado() { return novoValorSegurado; }
    public List<Cobertura> getNovasCoberturas() { return novasCoberturas; }
    public String getOperadorId() { return operadorId; }
    public String getMotivo() { return motivo; }
    public Long getVersaoEsperada() { return versaoEsperada; }
    public String getObservacoes() { return observacoes; }
    
    /**
     * Verifica se há controle de versão.
     */
    public boolean hasVersionControl() {
        return versaoEsperada != null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AtualizarApoliceCommand that = (AtualizarApoliceCommand) obj;
        return Objects.equals(apoliceId, that.apoliceId) &&
               Objects.equals(novoValorSegurado, that.novoValorSegurado) &&
               Objects.equals(novasCoberturas, that.novasCoberturas) &&
               Objects.equals(operadorId, that.operadorId) &&
               Objects.equals(motivo, that.motivo) &&
               Objects.equals(versaoEsperada, that.versaoEsperada) &&
               Objects.equals(observacoes, that.observacoes);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(apoliceId, novoValorSegurado, novasCoberturas, 
                           operadorId, motivo, versaoEsperada, observacoes);
    }
    
    @Override
    public String toString() {
        return String.format("AtualizarApoliceCommand{apoliceId='%s', novoValorSegurado=%s, " +
                           "coberturas=%d, operadorId='%s', motivo='%s', versaoEsperada=%d}",
                           apoliceId, novoValorSegurado, novasCoberturas.size(), 
                           operadorId, motivo, versaoEsperada);
    }
}