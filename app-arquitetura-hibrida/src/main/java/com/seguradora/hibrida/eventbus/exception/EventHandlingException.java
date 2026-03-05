package com.seguradora.hibrida.eventbus.exception;

import com.seguradora.hibrida.eventstore.model.DomainEvent;

/**
 * Exceção lançada quando ocorre erro no processamento de um evento por um handler.
 * 
 * <p>Esta exceção encapsula informações sobre o evento que falhou,
 * o handler responsável e a causa do erro.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class EventHandlingException extends EventBusException {
    
    private static final long serialVersionUID = 1L;
    
    private final DomainEvent event;
    private final String handlerClass;
    private final boolean retryable;
    
    /**
     * Construtor com evento e handler.
     * 
     * @param message Mensagem de erro
     * @param event Evento que falhou
     * @param handlerClass Nome da classe do handler
     * @param retryable Se o erro permite retry
     */
    public EventHandlingException(String message, DomainEvent event, String handlerClass, boolean retryable) {
        super(message);
        this.event = event;
        this.handlerClass = handlerClass;
        this.retryable = retryable;
    }
    
    /**
     * Construtor com evento, handler e causa.
     * 
     * @param message Mensagem de erro
     * @param event Evento que falhou
     * @param handlerClass Nome da classe do handler
     * @param retryable Se o erro permite retry
     * @param cause Causa raiz
     */
    public EventHandlingException(String message, DomainEvent event, String handlerClass, 
                                 boolean retryable, Throwable cause) {
        super(message, cause);
        this.event = event;
        this.handlerClass = handlerClass;
        this.retryable = retryable;
    }
    
    /**
     * Obtém o evento que falhou.
     * 
     * @return Evento que causou a falha
     */
    public DomainEvent getEvent() {
        return event;
    }
    
    /**
     * Obtém o nome da classe do handler que falhou.
     * 
     * @return Nome da classe do handler
     */
    public String getHandlerClass() {
        return handlerClass;
    }
    
    /**
     * Verifica se o erro permite retry.
     * 
     * @return true se permite retry, false caso contrário
     */
    public boolean isRetryable() {
        return retryable;
    }
    
    /**
     * Obtém o tipo do evento que falhou.
     * 
     * @return Tipo do evento
     */
    public String getEventType() {
        return event != null ? event.getClass().getSimpleName() : "Unknown";
    }
    
    /**
     * Obtém o ID do aggregate do evento que falhou.
     * 
     * @return ID do aggregate ou null se não disponível
     */
    public String getAggregateId() {
        return event != null ? event.getAggregateId() : null;
    }
    
    @Override
    public String toString() {
        return String.format(
            "EventHandlingException{eventType=%s, aggregateId=%s, handlerClass=%s, " +
            "retryable=%s, message=%s}",
            getEventType(), getAggregateId(), handlerClass, retryable, getMessage()
        );
    }
}