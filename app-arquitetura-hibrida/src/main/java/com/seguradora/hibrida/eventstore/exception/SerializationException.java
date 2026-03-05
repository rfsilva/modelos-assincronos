package com.seguradora.hibrida.eventstore.exception;

/**
 * Exceção para erros de serialização/deserialização de eventos.
 * 
 * Lançada quando há falhas na conversão de eventos para/de
 * formato persistível (JSON, compressão, etc.).
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class SerializationException extends EventStoreException {
    
    public SerializationException(String message) {
        super(message);
    }
    
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SerializationException(Throwable cause) {
        super(cause);
    }
}