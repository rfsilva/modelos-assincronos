package com.seguradora.hibrida.eventbus.exception;

/**
 * Exceção base para erros relacionados ao Event Bus.
 * 
 * <p>Esta é a exceção raiz da hierarquia de exceções do Event Bus.
 * Todas as outras exceções específicas estendem desta classe.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class EventBusException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Construtor padrão.
     */
    public EventBusException() {
        super();
    }
    
    /**
     * Construtor com mensagem.
     * 
     * @param message Mensagem de erro
     */
    public EventBusException(String message) {
        super(message);
    }
    
    /**
     * Construtor com mensagem e causa.
     * 
     * @param message Mensagem de erro
     * @param cause Causa raiz
     */
    public EventBusException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Construtor com causa.
     * 
     * @param cause Causa raiz
     */
    public EventBusException(Throwable cause) {
        super(cause);
    }
}