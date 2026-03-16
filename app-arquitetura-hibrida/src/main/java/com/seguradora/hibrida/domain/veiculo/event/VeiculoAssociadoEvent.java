package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Evento disparado quando um veículo é associado a uma apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class VeiculoAssociadoEvent extends DomainEvent {
    
    private static final String EVENT_TYPE = "VeiculoAssociado";
    
    private final String apoliceId;
    private final LocalDate dataInicio;
    private final String operadorId;
    
    public VeiculoAssociadoEvent(String aggregateId, long version, String apoliceId, 
                                LocalDate dataInicio, String operadorId) {
        super(aggregateId, "VeiculoAggregate", version);
        this.apoliceId = validarApoliceId(apoliceId);
        this.dataInicio = validarDataInicio(dataInicio);
        this.operadorId = validarOperadorId(operadorId);
    }
    
    public static VeiculoAssociadoEvent create(String aggregateId, long version, String apoliceId,
                                              LocalDate dataInicio, String operadorId) {
        return new VeiculoAssociadoEvent(aggregateId, version, apoliceId, dataInicio, operadorId);
    }
    
    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
    
    public String getApoliceId() {
        return apoliceId;
    }
    
    public LocalDate getDataInicio() {
        return dataInicio;
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
    
    private LocalDate validarDataInicio(LocalDate dataInicio) {
        if (dataInicio == null) {
            throw new IllegalArgumentException("Data de início não pode ser nula");
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        VeiculoAssociadoEvent that = (VeiculoAssociadoEvent) obj;
        return Objects.equals(getAggregateId(), that.getAggregateId()) &&
               getVersion() == that.getVersion() &&
               Objects.equals(apoliceId, that.apoliceId) &&
               Objects.equals(dataInicio, that.dataInicio) &&
               Objects.equals(operadorId, that.operadorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAggregateId(), getVersion(), apoliceId, dataInicio, operadorId);
    }
    
    @Override
    public String toString() {
        return String.format("VeiculoAssociadoEvent{aggregateId='%s', apoliceId='%s', dataInicio=%s}",
            getAggregateId(), apoliceId, dataInicio);
    }
}