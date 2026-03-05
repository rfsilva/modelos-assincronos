package com.seguradora.hibrida.command.exception;

/**
 * Exceção base para todas as exceções relacionadas ao Command Bus.
 * 
 * <p>Esta classe serve como base para todas as exceções específicas
 * do sistema de comandos, permitindo tratamento unificado quando necessário.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public abstract class CommandException extends RuntimeException {
    
    /**
     * Construtor com mensagem.
     * 
     * @param message Mensagem descritiva do erro
     */
    public CommandException(String message) {
        super(message);
    }
    
    /**
     * Construtor com mensagem e causa.
     * 
     * @param message Mensagem descritiva do erro
     * @param cause Causa raiz da exceção
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Construtor com causa.
     * 
     * @param cause Causa raiz da exceção
     */
    public CommandException(Throwable cause) {
        super(cause);
    }
}