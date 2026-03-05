package com.seguradora.hibrida.command.exception;

import com.seguradora.hibrida.command.Command;

/**
 * Exceção lançada quando ocorre erro durante a execução de um comando.
 * 
 * <p>Esta exceção é lançada quando:</p>
 * <ul>
 *   <li>Erro inesperado durante processamento</li>
 *   <li>Falha na persistência de dados</li>
 *   <li>Violação de regras de negócio</li>
 *   <li>Erro de infraestrutura (banco, rede, etc.)</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class CommandExecutionException extends CommandException {
    
    private final Class<? extends Command> commandType;
    private final String commandId;
    
    /**
     * Construtor com mensagem simples.
     * 
     * @param message Mensagem descritiva do erro
     */
    public CommandExecutionException(String message) {
        super(message);
        this.commandType = null;
        this.commandId = null;
    }
    
    /**
     * Construtor com tipo do comando e mensagem.
     * 
     * @param commandType Tipo do comando que falhou na execução
     * @param message Mensagem descritiva do erro
     */
    public CommandExecutionException(Class<? extends Command> commandType, String message) {
        super(String.format("Erro na execução do comando %s: %s", 
                          commandType.getSimpleName(), message));
        this.commandType = commandType;
        this.commandId = null;
    }
    
    /**
     * Construtor com tipo do comando, ID e mensagem.
     * 
     * @param commandType Tipo do comando que falhou na execução
     * @param commandId ID do comando específico
     * @param message Mensagem descritiva do erro
     */
    public CommandExecutionException(Class<? extends Command> commandType, String commandId, String message) {
        super(String.format("Erro na execução do comando %s [%s]: %s", 
                          commandType.getSimpleName(), commandId, message));
        this.commandType = commandType;
        this.commandId = commandId;
    }
    
    /**
     * Construtor com mensagem e causa.
     * 
     * @param message Mensagem descritiva do erro
     * @param cause Causa raiz da exceção
     */
    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.commandType = null;
        this.commandId = null;
    }
    
    /**
     * Construtor com tipo do comando, mensagem e causa.
     * 
     * @param commandType Tipo do comando que falhou na execução
     * @param message Mensagem descritiva do erro
     * @param cause Causa raiz da exceção
     */
    public CommandExecutionException(Class<? extends Command> commandType, String message, Throwable cause) {
        super(String.format("Erro na execução do comando %s: %s", 
                          commandType.getSimpleName(), message), cause);
        this.commandType = commandType;
        this.commandId = null;
    }
    
    /**
     * Construtor completo com todos os parâmetros.
     * 
     * @param commandType Tipo do comando que falhou na execução
     * @param commandId ID do comando específico
     * @param message Mensagem descritiva do erro
     * @param cause Causa raiz da exceção
     */
    public CommandExecutionException(Class<? extends Command> commandType, String commandId, 
                                   String message, Throwable cause) {
        super(String.format("Erro na execução do comando %s [%s]: %s", 
                          commandType.getSimpleName(), commandId, message), cause);
        this.commandType = commandType;
        this.commandId = commandId;
    }
    
    /**
     * Obtém o tipo do comando que falhou na execução.
     * 
     * @return Classe do comando ou null se não especificado
     */
    public Class<? extends Command> getCommandType() {
        return commandType;
    }
    
    /**
     * Obtém o ID do comando que falhou na execução.
     * 
     * @return ID do comando ou null se não especificado
     */
    public String getCommandId() {
        return commandId;
    }
}