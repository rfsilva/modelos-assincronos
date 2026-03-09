package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Evento de domínio disparado quando um veículo é desassociado de uma apólice de seguro.
 * Contém informações sobre a apólice, data de fim da cobertura, motivo e operador responsável.
 */
@Getter
@NoArgsConstructor
public class VeiculoDesassociadoEvent extends DomainEvent {
    
    private String apoliceId;
    private LocalDate dataFim;
    private String motivo;
    private String operadorId;

    public VeiculoDesassociadoEvent(
            String aggregateId,
            String apoliceId,
            LocalDate dataFim,
            String motivo,
            String operadorId) {
        
        super(aggregateId, "VeiculoAggregate", 0);
        this.apoliceId = validarApoliceId(apoliceId);
        this.dataFim = validarDataFim(dataFim);
        this.motivo = validarMotivo(motivo);
        this.operadorId = validarOperadorId(operadorId);
    }

    private String validarApoliceId(String apoliceId) {
        if (apoliceId == null || apoliceId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da apólice não pode ser nulo ou vazio");
        }
        return apoliceId.trim();
    }

    private LocalDate validarDataFim(LocalDate dataFim) {
        if (dataFim == null) {
            throw new IllegalArgumentException("Data de fim não pode ser nula");
        }
        return dataFim;
    }

    private String validarMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo não pode ser nulo ou vazio");
        }
        if (motivo.length() > 500) {
            throw new IllegalArgumentException("Motivo não pode ter mais de 500 caracteres");
        }
        return motivo.trim();
    }

    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }

    @Override
    public String getEventType() {
        return "VeiculoDesassociado";
    }
}
