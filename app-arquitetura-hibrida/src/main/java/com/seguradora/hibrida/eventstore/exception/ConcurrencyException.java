package com.seguradora.hibrida.eventstore.exception;

/**
 * Exceção para conflitos de concorrência no Event Store.
 * 
 * Lançada quando há tentativa de salvar eventos com versão
 * incorreta, indicando modificação concorrente do aggregate.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ConcurrencyException extends EventStoreException {
    
    private final String aggregateId;
    private final long expectedVersion;
    private final long actualVersion;
    
    public ConcurrencyException(String aggregateId, long expectedVersion, long actualVersion) {
        super(String.format("Conflito de concorrência para aggregate %s. Versão esperada: %d, versão atual: %d", 
                           aggregateId, expectedVersion, actualVersion));
        this.aggregateId = aggregateId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    public long getExpectedVersion() {
        return expectedVersion;
    }
    
    public long getActualVersion() {
        return actualVersion;
    }
}