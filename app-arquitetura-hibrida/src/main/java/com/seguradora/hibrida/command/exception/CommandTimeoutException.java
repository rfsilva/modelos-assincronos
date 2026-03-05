package com.seguradora.hibrida.command.exception;

import com.seguradora.hibrida.command.Command;

/**
 * Exceção lançada quando um comando excede o timeout configurado.
 * 
 * <p>Esta exceção é lançada quando:</p>
 * <ul>
 *   <li>Comando demora mais que o timeout configurado</li>
 *   <li>Handler não responde dentro do prazo</li>
 *   <li>Operações de I/O demoram muito</li>
 *   <li>Deadlocks ou contenção de recursos</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class CommandTimeoutException extends CommandException {
    
    private final Class<? extends Command> commandType;
    private final String commandId;
    private final int timeoutSeconds;
    private final long actualExecutionTimeMs;
    
    /**
     * Construtor com tipo do comando e timeout.
     * 
     * @param commandType Tipo do comando que excedeu o timeout
     * @param timeoutSeconds Timeout configurado em segundos
     */
    public CommandTimeoutException(Class<? extends Command> commandType, int timeoutSeconds) {
        super(String.format("Comando %s excedeu timeout de %d segundos", 
                          commandType.getSimpleName(), timeoutSeconds));
        this.commandType = commandType;
        this.commandId = null;
        this.timeoutSeconds = timeoutSeconds;
        this.actualExecutionTimeMs = 0;
    }
    
    /**
     * Construtor com tipo do comando, ID e timeout.
     * 
     * @param commandType Tipo do comando que excedeu o timeout
     * @param commandId ID do comando específico
     * @param timeoutSeconds Timeout configurado em segundos
     */
    public CommandTimeoutException(Class<? extends Command> commandType, String commandId, int timeoutSeconds) {
        super(String.format("Comando %s [%s] excedeu timeout de %d segundos", 
                          commandType.getSimpleName(), commandId, timeoutSeconds));
        this.commandType = commandType;
        this.commandId = commandId;
        this.timeoutSeconds = timeoutSeconds;
        this.actualExecutionTimeMs = 0;
    }
    
    /**
     * Construtor completo com tempo de execução real.
     * 
     * @param commandType Tipo do comando que excedeu o timeout
     * @param commandId ID do comando específico
     * @param timeoutSeconds Timeout configurado em segundos
     * @param actualExecutionTimeMs Tempo real de execução em milissegundos
     */
    public CommandTimeoutException(Class<? extends Command> commandType, String commandId, 
                                 int timeoutSeconds, long actualExecutionTimeMs) {
        super(String.format("Comando %s [%s] excedeu timeout de %d segundos (executou por %d ms)", 
                          commandType.getSimpleName(), commandId, timeoutSeconds, actualExecutionTimeMs));
        this.commandType = commandType;
        this.commandId = commandId;
        this.timeoutSeconds = timeoutSeconds;
        this.actualExecutionTimeMs = actualExecutionTimeMs;
    }
    
    /**
     * Construtor com mensagem customizada e causa.
     * 
     * @param commandType Tipo do comando que excedeu o timeout
     * @param message Mensagem customizada
     * @param cause Causa raiz da exceção
     */
    public CommandTimeoutException(Class<? extends Command> commandType, String message, Throwable cause) {
        super(message, cause);
        this.commandType = commandType;
        this.commandId = null;
        this.timeoutSeconds = 0;
        this.actualExecutionTimeMs = 0;
    }
    
    /**
     * Obtém o tipo do comando que excedeu o timeout.
     * 
     * @return Classe do comando
     */
    public Class<? extends Command> getCommandType() {
        return commandType;
    }
    
    /**
     * Obtém o ID do comando que excedeu o timeout.
     * 
     * @return ID do comando ou null se não especificado
     */
    public String getCommandId() {
        return commandId;
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
     * @return Tempo de execução em milissegundos
     */
    public long getActualExecutionTimeMs() {
        return actualExecutionTimeMs;
    }
}