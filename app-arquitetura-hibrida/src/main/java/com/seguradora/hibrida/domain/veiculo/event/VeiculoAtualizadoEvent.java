package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.domain.veiculo.model.Especificacao;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Evento de domínio disparado quando as especificações de um veículo são atualizadas.
 * Contém as especificações alteradas, valores anteriores e novos valores para auditoria.
 */
@Getter
@NoArgsConstructor
public class VeiculoAtualizadoEvent extends DomainEvent {
    
    private Especificacao novaEspecificacao;
    private Map<String, Object> valoresAnteriores;
    private Map<String, Object> novosValores;
    private String operadorId;

    public VeiculoAtualizadoEvent(
            String aggregateId,
            Especificacao novaEspecificacao,
            Map<String, Object> valoresAnteriores,
            Map<String, Object> novosValores,
            String operadorId) {
        
        super(aggregateId, "VeiculoAggregate", 0);
        this.novaEspecificacao = validarEspecificacao(novaEspecificacao);
        this.valoresAnteriores = valoresAnteriores != null ? new HashMap<>(valoresAnteriores) : new HashMap<>();
        this.novosValores = novosValores != null ? new HashMap<>(novosValores) : new HashMap<>();
        this.operadorId = validarOperadorId(operadorId);
    }

    private Especificacao validarEspecificacao(Especificacao especificacao) {
        if (especificacao == null) {
            throw new IllegalArgumentException("Nova especificação não pode ser nula");
        }
        return especificacao;
    }

    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }

    /**
     * Verifica se houve mudança em um campo específico.
     */
    public boolean houveAlteracaoEm(String campo) {
        return novosValores.containsKey(campo) && 
               !novosValores.get(campo).equals(valoresAnteriores.get(campo));
    }

    /**
     * Retorna a descrição das alterações realizadas.
     */
    public String getDescricaoAlteracoes() {
        StringBuilder sb = new StringBuilder();
        novosValores.forEach((campo, novoValor) -> {
            Object valorAnterior = valoresAnteriores.get(campo);
            sb.append(String.format("%s: %s → %s; ", campo, valorAnterior, novoValor));
        });
        return sb.toString();
    }

    @Override
    public String getEventType() {
        return "VeiculoAtualizado";
    }
}
