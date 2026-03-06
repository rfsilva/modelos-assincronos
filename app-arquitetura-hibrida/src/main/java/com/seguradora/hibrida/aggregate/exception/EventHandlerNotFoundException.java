package com.seguradora.hibrida.aggregate.exception;

import com.seguradora.hibrida.eventstore.model.DomainEvent;

/**
 * Exceção lançada quando não é encontrado um handler para um evento específico.
 * 
 * <p>Esta exceção ocorre quando:
 * <ul>
 *   <li>Um evento é aplicado a um aggregate que não possui handler correspondente</li>
 *   <li>O método handler não está anotado com @EventSourcingHandler</li>
 *   <li>O método handler não tem a assinatura correta</li>
 *   <li>O método handler não é acessível (private sem reflection)</li>
 * </ul>
 * 
 * <p><strong>Resolução típica:</strong>
 * <pre>{@code
 * // Adicionar handler no aggregate
 * @EventSourcingHandler
 * protected void on(MeuEventoEvent event) {
 *     // Aplicar mudanças de estado
 *     this.campo = event.getValor();
 * }
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class EventHandlerNotFoundException extends AggregateException {
    
    /**
     * Tipo do evento que não possui handler.
     */
    private final Class<? extends DomainEvent> eventType;
    
    /**
     * Construtor com tipo do evento.
     * 
     * @param eventType Tipo do evento sem handler
     * @param aggregateType Tipo do aggregate
     */
    public EventHandlerNotFoundException(Class<? extends DomainEvent> eventType, String aggregateType) {
        super(String.format("Nenhum handler encontrado para evento %s no aggregate %s", 
                eventType.getSimpleName(), aggregateType));
        this.eventType = eventType;
    }
    
    /**
     * Construtor com contexto completo.
     * 
     * @param eventType Tipo do evento sem handler
     * @param aggregateId ID do aggregate
     * @param aggregateType Tipo do aggregate
     * @param version Versão do aggregate
     */
    public EventHandlerNotFoundException(Class<? extends DomainEvent> eventType, 
                                       String aggregateId, String aggregateType, Long version) {
        super(String.format("Nenhum handler encontrado para evento %s no aggregate %s", 
                eventType.getSimpleName(), aggregateType), 
                aggregateId, aggregateType, version);
        this.eventType = eventType;
    }
    
    /**
     * Construtor com mensagem customizada.
     * 
     * @param message Mensagem de erro customizada
     * @param eventType Tipo do evento sem handler
     */
    public EventHandlerNotFoundException(String message, Class<? extends DomainEvent> eventType) {
        super(message);
        this.eventType = eventType;
    }
    
    /**
     * Retorna o tipo do evento que não possui handler.
     * 
     * @return Classe do evento
     */
    public Class<? extends DomainEvent> getEventType() {
        return eventType;
    }
    
    /**
     * Retorna o nome simples do tipo do evento.
     * 
     * @return Nome da classe do evento
     */
    public String getEventTypeName() {
        return eventType != null ? eventType.getSimpleName() : "Unknown";
    }
}