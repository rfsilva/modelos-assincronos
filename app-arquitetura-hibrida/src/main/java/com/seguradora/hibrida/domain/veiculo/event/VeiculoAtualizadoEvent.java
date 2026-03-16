package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.domain.veiculo.model.Especificacao;

import java.util.Objects;

/**
 * Evento disparado quando um veículo é atualizado.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class VeiculoAtualizadoEvent extends DomainEvent {
    
    private static final String EVENT_TYPE = "VeiculoAtualizado";
    
    private final Especificacao especificacaoAnterior;
    private final Especificacao novaEspecificacao;
    private final String operadorId;
    private final String motivoAlteracao;
    
    public VeiculoAtualizadoEvent(String aggregateId, long version, 
                                 Especificacao especificacaoAnterior, Especificacao novaEspecificacao,
                                 String operadorId, String motivoAlteracao) {
        super(aggregateId, "VeiculoAggregate", version);
        this.especificacaoAnterior = especificacaoAnterior;
        this.novaEspecificacao = novaEspecificacao;
        this.operadorId = operadorId;
        this.motivoAlteracao = motivoAlteracao;
    }
    
    public static VeiculoAtualizadoEvent create(String aggregateId, long version,
                                               Especificacao especificacaoAnterior, 
                                               Especificacao novaEspecificacao,
                                               String operadorId, String motivoAlteracao) {
        return new VeiculoAtualizadoEvent(aggregateId, version, especificacaoAnterior, 
                                         novaEspecificacao, operadorId, motivoAlteracao);
    }
    
    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
    
    public Especificacao getEspecificacaoAnterior() {
        return especificacaoAnterior;
    }
    
    public Especificacao getNovaEspecificacao() {
        return novaEspecificacao;
    }
    
    public String getOperadorId() {
        return operadorId;
    }
    
    public String getMotivoAlteracao() {
        return motivoAlteracao;
    }
    
    public String getDescricaoAlteracoes() {
        StringBuilder sb = new StringBuilder();
        
        if (!Objects.equals(especificacaoAnterior.getCor(), novaEspecificacao.getCor())) {
            sb.append("Cor: ").append(especificacaoAnterior.getCor())
              .append(" → ").append(novaEspecificacao.getCor()).append("; ");
        }
        
        if (!Objects.equals(especificacaoAnterior.getTipoCombustivel(), novaEspecificacao.getTipoCombustivel())) {
            sb.append("Combustível: ").append(especificacaoAnterior.getTipoCombustivel())
              .append(" → ").append(novaEspecificacao.getTipoCombustivel()).append("; ");
        }
        
        if (!Objects.equals(especificacaoAnterior.getCilindrada(), novaEspecificacao.getCilindrada())) {
            sb.append("Cilindrada: ").append(especificacaoAnterior.getCilindrada())
              .append(" → ").append(novaEspecificacao.getCilindrada()).append("; ");
        }
        
        return sb.length() > 0 ? sb.toString() : "Nenhuma alteração detectada";
    }
    
    public boolean houveAlteracaoEm(String campo) {
        return switch (campo.toLowerCase()) {
            case "cor" -> !Objects.equals(especificacaoAnterior.getCor(), novaEspecificacao.getCor());
            case "combustivel" -> !Objects.equals(especificacaoAnterior.getTipoCombustivel(), novaEspecificacao.getTipoCombustivel());
            case "cilindrada" -> !Objects.equals(especificacaoAnterior.getCilindrada(), novaEspecificacao.getCilindrada());
            default -> false;
        };
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        VeiculoAtualizadoEvent that = (VeiculoAtualizadoEvent) obj;
        return Objects.equals(getAggregateId(), that.getAggregateId()) &&
               getVersion() == that.getVersion() &&
               Objects.equals(especificacaoAnterior, that.especificacaoAnterior) &&
               Objects.equals(novaEspecificacao, that.novaEspecificacao) &&
               Objects.equals(operadorId, that.operadorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAggregateId(), getVersion(), especificacaoAnterior,
                           novaEspecificacao, operadorId);
    }
    
    @Override
    public String toString() {
        return String.format("VeiculoAtualizadoEvent{aggregateId='%s', operador='%s', alteracoes='%s'}",
            getAggregateId(), operadorId, getDescricaoAlteracoes());
    }
}