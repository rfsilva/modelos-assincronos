package com.seguradora.hibrida.command.exception;

import com.seguradora.hibrida.command.Command;

import java.util.Set;

/**
 * Exceção lançada quando um comando falha na validação.
 * 
 * <p>Esta exceção é lançada quando:</p>
 * <ul>
 *   <li>Validações customizadas de negócio falham</li>
 *   <li>Dados obrigatórios estão ausentes</li>
 *   <li>Formato de dados é inválido</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class CommandValidationException extends CommandException {
    
    private final Class<? extends Command> commandType;
    private final Set<String> violations;
    
    /**
     * Construtor com mensagem simples.
     * 
     * @param message Mensagem descritiva do erro de validação
     */
    public CommandValidationException(String message) {
        super(message);
        this.commandType = null;
        this.violations = null;
    }
    
    /**
     * Construtor com tipo do comando e mensagem.
     * 
     * @param commandType Tipo do comando que falhou na validação
     * @param message Mensagem descritiva do erro
     */
    public CommandValidationException(Class<? extends Command> commandType, String message) {
        super(String.format("Validação falhou para comando %s: %s", 
                          commandType.getSimpleName(), message));
        this.commandType = commandType;
        this.violations = null;
    }
    
    /**
     * Construtor com violações customizadas.
     * 
     * @param commandType Tipo do comando que falhou na validação
     * @param violations Conjunto de violações encontradas
     */
    public CommandValidationException(Class<? extends Command> commandType, 
                                    Set<String> violations) {
        super(buildViolationMessage(commandType, violations));
        this.commandType = commandType;
        this.violations = violations;
    }
    
    /**
     * Construtor com tipo do comando, mensagem e causa.
     * 
     * @param commandType Tipo do comando que falhou na validação
     * @param message Mensagem customizada
     * @param cause Causa raiz da exceção
     */
    public CommandValidationException(Class<? extends Command> commandType, String message, Throwable cause) {
        super(message, cause);
        this.commandType = commandType;
        this.violations = null;
    }
    
    /**
     * Obtém o tipo do comando que falhou na validação.
     * 
     * @return Classe do comando ou null se não especificado
     */
    public Class<? extends Command> getCommandType() {
        return commandType;
    }
    
    /**
     * Obtém as violações de validação encontradas.
     * 
     * @return Conjunto de violações ou null se não aplicável
     */
    public Set<String> getViolations() {
        return violations;
    }
    
    /**
     * Verifica se existem violações de validação.
     * 
     * @return true se existem violações, false caso contrário
     */
    public boolean hasViolations() {
        return violations != null && !violations.isEmpty();
    }
    
    /**
     * Obtém lista de mensagens de violação.
     * 
     * @return Lista de mensagens ou lista vazia se não há violações
     */
    public Set<String> getViolationMessages() {
        if (violations == null) {
            return Set.of();
        }
        
        return violations;
    }
    
    /**
     * Constrói mensagem de erro a partir das violações.
     * 
     * @param commandType Tipo do comando
     * @param violations Violações encontradas
     * @return Mensagem formatada
     */
    private static String buildViolationMessage(Class<? extends Command> commandType,
                                              Set<String> violations) {
        if (violations == null || violations.isEmpty()) {
            return String.format("Validação falhou para comando %s", commandType.getSimpleName());
        }
        
        String violationDetails = String.join(", ", violations);
        
        return String.format("Validação falhou para comando %s. Violações: [%s]", 
                           commandType.getSimpleName(), violationDetails);
    }
}