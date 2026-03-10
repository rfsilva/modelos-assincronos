package com.seguradora.hibrida.domain.apolice.command;

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
 * Comando para criação de nova apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class CriarApoliceCommand {
    
    @NotBlank(message = "ID da apólice não pode ser vazio")
    private final String apoliceId;
    
    @NotBlank(message = "ID do segurado não pode ser vazio")
    private final String seguradoId;
    
    @NotBlank(message = "Produto não pode ser vazio")
    @Size(min = 3, max = 100, message = "Produto deve ter entre 3 e 100 caracteres")
    private final String produto;
    
    @NotNull(message = "Vigência não pode ser nula")
    @Valid
    private final Vigencia vigencia;
    
    @NotNull(message = "Valor segurado não pode ser nulo")
    @Valid
    private final Valor valorSegurado;
    
    @NotNull(message = "Forma de pagamento não pode ser nula")
    private final FormaPagamento formaPagamento;
    
    @NotEmpty(message = "Deve haver pelo menos uma cobertura")
    @Valid
    private final List<Cobertura> coberturas;
    
    @NotBlank(message = "ID do operador não pode ser vazio")
    private final String operadorId;
    
    @Size(max = 500, message = "Observações não podem exceder 500 caracteres")
    private final String observacoes;
    
    /**
     * Construtor para criação do comando.
     */
    public CriarApoliceCommand(
            String apoliceId,
            String seguradoId,
            String produto,
            Vigencia vigencia,
            Valor valorSegurado,
            FormaPagamento formaPagamento,
            List<Cobertura> coberturas,
            String operadorId,
            String observacoes) {
        
        this.apoliceId = apoliceId;
        this.seguradoId = seguradoId;
        this.produto = produto;
        this.vigencia = vigencia;
        this.valorSegurado = valorSegurado;
        this.formaPagamento = formaPagamento;
        this.coberturas = coberturas != null ? List.copyOf(coberturas) : List.of();
        this.operadorId = operadorId;
        this.observacoes = observacoes;
    }
    
    /**
     * Factory method para criação simplificada.
     */
    public static CriarApoliceCommand of(
            String apoliceId,
            String seguradoId,
            String produto,
            Vigencia vigencia,
            Valor valorSegurado,
            FormaPagamento formaPagamento,
            List<Cobertura> coberturas,
            String operadorId) {
        
        return new CriarApoliceCommand(
                apoliceId,
                seguradoId,
                produto,
                vigencia,
                valorSegurado,
                formaPagamento,
                coberturas,
                operadorId,
                null
        );
    }
    
    // Getters
    public String getApoliceId() { return apoliceId; }
    public String getSeguradoId() { return seguradoId; }
    public String getProduto() { return produto; }
    public Vigencia getVigencia() { return vigencia; }
    public Valor getValorSegurado() { return valorSegurado; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public List<Cobertura> getCoberturas() { return coberturas; }
    public String getOperadorId() { return operadorId; }
    public String getObservacoes() { return observacoes; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CriarApoliceCommand that = (CriarApoliceCommand) obj;
        return Objects.equals(apoliceId, that.apoliceId) &&
               Objects.equals(seguradoId, that.seguradoId) &&
               Objects.equals(produto, that.produto) &&
               Objects.equals(vigencia, that.vigencia) &&
               Objects.equals(valorSegurado, that.valorSegurado) &&
               formaPagamento == that.formaPagamento &&
               Objects.equals(coberturas, that.coberturas) &&
               Objects.equals(operadorId, that.operadorId) &&
               Objects.equals(observacoes, that.observacoes);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(apoliceId, seguradoId, produto, vigencia, valorSegurado, 
                           formaPagamento, coberturas, operadorId, observacoes);
    }
    
    @Override
    public String toString() {
        return String.format("CriarApoliceCommand{apoliceId='%s', seguradoId='%s', produto='%s', " +
                           "valorSegurado=%s, formaPagamento=%s, coberturas=%d, operadorId='%s'}",
                           apoliceId, seguradoId, produto, valorSegurado, formaPagamento, 
                           coberturas.size(), operadorId);
    }
}