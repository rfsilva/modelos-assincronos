package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Evento de domínio disparado quando um veículo é associado a uma apólice de seguro.
 * Contém informações sobre a apólice, data de início da cobertura e operador responsável.
 */
@Getter
@NoArgsConstructor
public class VeiculoAssociadoEvent extends DomainEvent {
    
    private String apoliceId;
    private LocalDate dataInicio;
    private String operadorId;

    public VeiculoAssociadoEvent(
            String aggregateId,
            String apoliceId,
            LocalDate dataInicio,
            String operadorId) {
        
        super(aggregateId, "VeiculoAggregate", 0);
        this.apoliceId = validarApoliceId(apoliceId);
        this.dataInicio = validarDataInicio(dataInicio);
        this.operadorId = validarOperadorId(operadorId);
    }

    private String validarApoliceId(String apoliceId) {
        if (apoliceId == null || apoliceId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da apólice não pode ser nulo ou vazio");
        }
        return apoliceId.trim();
    }

    private LocalDate validarDataInicio(LocalDate dataInicio) {
        if (dataInicio == null) {
            throw new IllegalArgumentException("Data de início não pode ser nula");
        }
        if (dataInicio.isBefore(LocalDate.now().minusYears(1))) {
            throw new IllegalArgumentException("Data de início não pode ser anterior a 1 ano atrás");
        }
        return dataInicio;
    }

    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }

    @Override
    public String getEventType() {
        return "VeiculoAssociado";
    }
}
