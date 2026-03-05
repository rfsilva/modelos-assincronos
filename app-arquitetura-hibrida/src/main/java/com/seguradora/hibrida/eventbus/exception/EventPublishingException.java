package com.seguradora.hibrida.eventbus.exception;

import com.seguradora.hibrida.eventstore.model.DomainEvent;

/**
 * Exceção lançada quando ocorre erro na publicação de um evento.
 * 
 * <p>Esta exceção é lançada quando o Event Bus não consegue
 * publicar um evento devido a problemas de infraestrutura,
 * configuração ou validação.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class EventPublishingException extends EventBusException {
    
    private static final long serialVersionUID = 1L;
    
    private final DomainEvent event;
    private final String reason;
    
    /**
     * Construtor com evento e razão.
     * 
     * @param message Mensagem de erro
     * @param event Evento que falhou na publicação
     * @param reason Razão da falha
     */
    public EventPublishingException(String message, DomainEvent event, String reason) {
        super(message);
        this.event = event;
        this.reason = reason;
    }
    
    /**
     * Construtor com evento, razão e causa.
     * 
     * @param message Mensagem de erro
     * @param event Evento que falhou na publicação
     * @param reason Razão da falha
     * @param cause Causa raiz
     */
    public EventPublishingException(String message, DomainEvent event, String reason, Throwable cause) {
        super(message, cause);
        this.event = event;
        this.reason = reason;
    }
    
    /**
     * Obtém o evento que falhou na publicação.
     * 
     * @return Evento que causou a falha
     */
    public DomainEvent getEvent() {
        return event;
    }
    
    /**
     * Obtém a razão da falha na publicação.
     * 
     * @return Razão da falha
     */
    public String getReason() {
        return reason;
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
            "EventPublishingException{eventType=%s, aggregateId=%s, reason=%s, message=%s}",
            getEventType(), getAggregateId(), reason, getMessage()
        );
    }
}