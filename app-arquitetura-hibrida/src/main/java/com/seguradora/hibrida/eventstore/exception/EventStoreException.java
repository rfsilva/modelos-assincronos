package com.seguradora.hibrida.eventstore.exception;

/**
 * Exceção base para erros do Event Store.
 * 
 * Representa falhas gerais na persistência ou recuperação
 * de eventos do Event Store.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class EventStoreException extends RuntimeException {
    
    public EventStoreException(String message) {
        super(message);
    }
    
    public EventStoreException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EventStoreException(Throwable cause) {
        super(cause);
    }
}