package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Evento disparado quando um veículo é desassociado de uma apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class VeiculoDesassociadoEvent extends DomainEvent {
    
    private static final String EVENT_TYPE = "VeiculoDesassociado";
    
    private final String apoliceId;
    private final LocalDate dataFim;
    private final String motivo;
    private final String operadorId;
    
    public VeiculoDesassociadoEvent(String aggregateId, long version, String apoliceId,
                                   LocalDate dataFim, String motivo, String operadorId) {
        super(aggregateId, "VeiculoAggregate", version);
        this.apoliceId = validarApoliceId(apoliceId);
        this.dataFim = validarDataFim(dataFim);
        this.motivo = validarMotivo(motivo);
        this.operadorId = validarOperadorId(operadorId);
    }
    
    public static VeiculoDesassociadoEvent create(String aggregateId, long version, String apoliceId,
                                                 LocalDate dataFim, String motivo, String operadorId) {
        return new VeiculoDesassociadoEvent(aggregateId, version, apoliceId, dataFim, 
                                           motivo, operadorId);
    }
    
    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
    
    public String getApoliceId() {
        return apoliceId;
    }
    
    public LocalDate getDataFim() {
        return dataFim;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public String getOperadorId() {
        return operadorId;
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
        return motivo.trim();
    }
    
    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        VeiculoDesassociadoEvent that = (VeiculoDesassociadoEvent) obj;
        return Objects.equals(getAggregateId(), that.getAggregateId()) &&
               getVersion() == that.getVersion() &&
               Objects.equals(apoliceId, that.apoliceId) &&
               Objects.equals(dataFim, that.dataFim) &&
               Objects.equals(motivo, that.motivo) &&
               Objects.equals(operadorId, that.operadorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAggregateId(), getVersion(), apoliceId, dataFim,
                           motivo, operadorId);
    }
    
    @Override
    public String toString() {
        return String.format("VeiculoDesassociadoEvent{aggregateId='%s', apoliceId='%s', motivo='%s'}",
            getAggregateId(), apoliceId, motivo);
    }
}