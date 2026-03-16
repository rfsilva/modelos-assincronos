package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.domain.veiculo.model.Proprietario;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Evento disparado quando a propriedade de um veículo é transferida.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class PropriedadeTransferidaEvent extends DomainEvent {
    
    private static final String EVENT_TYPE = "PropriedadeTransferida";
    
    private final Proprietario proprietarioAnterior;
    private final Proprietario novoProprietario;
    private final LocalDate dataTransferencia;
    private final String operadorId;
    private final String observacoes;
    
    public PropriedadeTransferidaEvent(String aggregateId, long version, 
                                      Proprietario proprietarioAnterior, Proprietario novoProprietario,
                                      LocalDate dataTransferencia, String operadorId, 
                                      String observacoes) {
        super(aggregateId, "VeiculoAggregate", version);
        this.proprietarioAnterior = validarProprietarioAnterior(proprietarioAnterior);
        this.novoProprietario = validarNovoProprietario(novoProprietario);
        this.dataTransferencia = validarDataTransferencia(dataTransferencia);
        this.operadorId = validarOperadorId(operadorId);
        this.observacoes = observacoes;
    }
    
    public static PropriedadeTransferidaEvent create(String aggregateId, long version,
                                                    Proprietario proprietarioAnterior, 
                                                    Proprietario novoProprietario,
                                                    LocalDate dataTransferencia, String operadorId,
                                                    String observacoes) {
        return new PropriedadeTransferidaEvent(aggregateId, version, proprietarioAnterior, 
                                              novoProprietario, dataTransferencia, operadorId,
                                              observacoes);
    }
    
    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
    
    public Proprietario getProprietarioAnterior() {
        return proprietarioAnterior;
    }
    
    public Proprietario getNovoProprietario() {
        return novoProprietario;
    }
    
    public LocalDate getDataTransferencia() {
        return dataTransferencia;
    }
    
    public String getOperadorId() {
        return operadorId;
    }
    
    public String getObservacoes() {
        return observacoes;
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        PropriedadeTransferidaEvent that = (PropriedadeTransferidaEvent) obj;
        return Objects.equals(getAggregateId(), that.getAggregateId()) &&
               getVersion() == that.getVersion() &&
               Objects.equals(proprietarioAnterior, that.proprietarioAnterior) &&
               Objects.equals(novoProprietario, that.novoProprietario) &&
               Objects.equals(dataTransferencia, that.dataTransferencia) &&
               Objects.equals(operadorId, that.operadorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAggregateId(), getVersion(), proprietarioAnterior,
                           novoProprietario, dataTransferencia, operadorId);
    }
    
    @Override
    public String toString() {
        return String.format("PropriedadeTransferidaEvent{aggregateId='%s', de='%s', para='%s', data=%s}",
            getAggregateId(), proprietarioAnterior.getNome(), novoProprietario.getNome(), dataTransferencia);
    }
}