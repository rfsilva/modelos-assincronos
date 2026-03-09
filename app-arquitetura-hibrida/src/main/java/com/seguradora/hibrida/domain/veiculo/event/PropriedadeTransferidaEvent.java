package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.domain.veiculo.model.Proprietario;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Evento de domínio disparado quando a propriedade de um veículo é transferida.
 * Contém informações sobre o proprietário anterior, novo proprietário e data da transferência.
 */
@Getter
@NoArgsConstructor
public class PropriedadeTransferidaEvent extends DomainEvent {
    
    private Proprietario proprietarioAnterior;
    private Proprietario novoProprietario;
    private LocalDate dataTransferencia;
    private String operadorId;

    public PropriedadeTransferidaEvent(
            String aggregateId,
            Proprietario proprietarioAnterior,
            Proprietario novoProprietario,
            LocalDate dataTransferencia,
            String operadorId) {
        
        super(aggregateId, "VeiculoAggregate", 0);
        this.proprietarioAnterior = validarProprietarioAnterior(proprietarioAnterior);
        this.novoProprietario = validarNovoProprietario(novoProprietario);
        this.dataTransferencia = validarDataTransferencia(dataTransferencia);
        this.operadorId = validarOperadorId(operadorId);
    }

    private Proprietario validarProprietarioAnterior(Proprietario proprietario) {
        if (proprietario == null) {
            throw new IllegalArgumentException("Proprietário anterior não pode ser nulo");
        }
        return proprietario;
    }

    private Proprietario validarNovoProprietario(Proprietario proprietario) {
        if (proprietario == null) {
            throw new IllegalArgumentException("Novo proprietário não pode ser nulo");
        }
        return proprietario;
    }

    private LocalDate validarDataTransferencia(LocalDate dataTransferencia) {
        if (dataTransferencia == null) {
            throw new IllegalArgumentException("Data de transferência não pode ser nula");
        }
        if (dataTransferencia.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de transferência não pode ser futura");
        }
        return dataTransferencia;
    }

    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }

    @Override
    public String getEventType() {
        return "PropriedadeTransferida";
    }
}
