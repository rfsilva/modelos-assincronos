package com.seguradora.hibrida.eventbus.exception;

import com.seguradora.hibrida.eventstore.model.DomainEvent;

/**
 * Exceção lançada quando um handler de evento excede o timeout configurado.
 * 
 * <p>Esta exceção é lançada quando um handler demora mais tempo
 * que o configurado para processar um evento.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class EventHandlerTimeoutException extends EventHandlingException {
    
    private static final long serialVersionUID = 1L;
    
    private final int timeoutSeconds;
    private final long actualTimeMs;
    
    /**
     * Construtor com timeout e tempo real.
     * 
     * @param message Mensagem de erro
     * @param event Evento que causou timeout
     * @param handlerClass Nome da classe do handler
     * @param timeoutSeconds Timeout configurado em segundos
     * @param actualTimeMs Tempo real de execução em milissegundos
     */
    public EventHandlerTimeoutException(String message, DomainEvent event, String handlerClass,
                                       int timeoutSeconds, long actualTimeMs) {
        super(message, event, handlerClass, true); // Timeout é sempre retryable
        this.timeoutSeconds = timeoutSeconds;
        this.actualTimeMs = actualTimeMs;
    }
    
    /**
     * Obtém o timeout configurado em segundos.
     * 
     * @return Timeout em segundos
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
    
    /**
     * Obtém o tempo real de execução em milissegundos.
     * 
     * @return Tempo real em milissegundos
     */
    public long getActualTimeMs() {
        return actualTimeMs;
    }
    
    /**
     * Obtém o tempo real de execução em segundos.
     * 
     * @return Tempo real em segundos
     */
    public double getActualTimeSeconds() {
        return actualTimeMs / 1000.0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "EventHandlerTimeoutException{eventType=%s, aggregateId=%s, handlerClass=%s, " +
            "timeoutSeconds=%d, actualTimeMs=%d, message=%s}",
            getEventType(), getAggregateId(), getHandlerClass(), 
            timeoutSeconds, actualTimeMs, getMessage()
        );
    }
}