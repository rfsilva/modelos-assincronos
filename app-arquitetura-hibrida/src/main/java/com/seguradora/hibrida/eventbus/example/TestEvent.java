package com.seguradora.hibrida.eventbus.example;

import com.seguradora.hibrida.eventstore.model.DomainEvent;

/**
 * Evento de exemplo para demonstrar o uso do Event Bus.
 * 
 * <p>Este evento é usado para testes e demonstrações do sistema.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class TestEvent extends DomainEvent {
    
    private String message;
    private String category;
    private int priority;
    
    /**
     * Construtor padrão para serialização.
     */
    public TestEvent() {
        super();
    }
    
    /**
     * Construtor com parâmetros.
     * 
     * @param aggregateId ID do aggregate
     * @param message Mensagem do evento
     * @param category Categoria do evento
     * @param priority Prioridade do evento
     */
    public TestEvent(String aggregateId, String message, String category, int priority) {
        super(aggregateId, "TestAggregate", 1L);
        this.message = message;
        this.category = category;
        this.priority = priority;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    @Override
    public String toString() {
        return String.format(
            "TestEvent{aggregateId='%s', message='%s', category='%s', priority=%d, timestamp=%s}",
            getAggregateId(), message, category, priority, getTimestamp()
        );
    }
}