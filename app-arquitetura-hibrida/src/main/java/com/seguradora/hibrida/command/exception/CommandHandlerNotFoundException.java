package com.seguradora.hibrida.command.exception;

import com.seguradora.hibrida.command.Command;

/**
 * Exceção lançada quando não é encontrado handler para um comando específico.
 * 
 * <p>Esta exceção indica que o Command Bus não conseguiu encontrar
 * um handler registrado para processar o tipo de comando solicitado.</p>
 * 
 * <p><strong>Cenários Comuns:</strong></p>
 * <ul>
 *   <li>Handler não foi registrado no Command Bus</li>
 *   <li>Handler não está anotado com @Component</li>
 *   <li>Erro na configuração do Spring</li>
 *   <li>Comando enviado para tipo não implementado</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class CommandHandlerNotFoundException extends CommandException {
    
    private final Class<? extends Command> commandType;
    
    /**
     * Construtor com tipo do comando.
     * 
     * @param commandType Tipo do comando que não possui handler
     */
    public CommandHandlerNotFoundException(Class<? extends Command> commandType) {
        super(String.format("Nenhum handler encontrado para o comando: %s", 
                          commandType.getSimpleName()));
        this.commandType = commandType;
    }
    
    /**
     * Construtor com tipo do comando e mensagem customizada.
     * 
     * @param commandType Tipo do comando que não possui handler
     * @param message Mensagem customizada
     */
    public CommandHandlerNotFoundException(Class<? extends Command> commandType, String message) {
        super(message);
        this.commandType = commandType;
    }
    
    /**
     * Construtor com tipo do comando, mensagem e causa.
     * 
     * @param commandType Tipo do comando que não possui handler
     * @param message Mensagem customizada
     * @param cause Causa raiz da exceção
     */
    public CommandHandlerNotFoundException(Class<? extends Command> commandType, String message, Throwable cause) {
        super(message, cause);
        this.commandType = commandType;
    }
    
    /**
     * Obtém o tipo do comando que não possui handler.
     * 
     * @return Classe do comando
     */
    public Class<? extends Command> getCommandType() {
        return commandType;
    }
}