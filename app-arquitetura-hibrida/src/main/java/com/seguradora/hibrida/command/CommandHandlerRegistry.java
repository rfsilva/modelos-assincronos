package com.seguradora.hibrida.command;

import com.seguradora.hibrida.command.exception.CommandHandlerNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry para gerenciar handlers de comando registrados no sistema.
 * 
 * <p>Esta classe é responsável por:</p>
 * <ul>
 *   <li>Registrar handlers automaticamente via Spring</li>
 *   <li>Manter mapeamento tipo de comando -> handler</li>
 *   <li>Fornecer lookup rápido de handlers</li>
 *   <li>Validar unicidade de handlers por tipo</li>
 * </ul>
 * 
 * <p><strong>Thread Safety:</strong></p>
 * <p>Esta classe é thread-safe e pode ser usada concorrentemente.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@Slf4j
public class CommandHandlerRegistry {
    
    private final Map<Class<? extends Command>, CommandHandler<? extends Command>> handlers = 
            new ConcurrentHashMap<>();
    
    /**
     * Registra um handler para um tipo específico de comando.
     * 
     * @param handler Handler a ser registrado
     * @param <T> Tipo do comando processado pelo handler
     * @throws IllegalArgumentException se já existe handler para o tipo
     */
    @SuppressWarnings("unchecked")
    public <T extends Command> void registerHandler(CommandHandler<T> handler) {
        Class<T> commandType = handler.getCommandType();
        
        if (handlers.containsKey(commandType)) {
            throw new IllegalArgumentException(
                String.format("Handler já registrado para comando: %s", commandType.getSimpleName())
            );
        }
        
        handlers.put(commandType, handler);
        log.info("Handler registrado para comando: {} -> {}", 
                commandType.getSimpleName(), handler.getClass().getSimpleName());
    }
    
    /**
     * Remove o handler para um tipo específico de comando.
     * 
     * @param commandType Tipo do comando cujo handler deve ser removido
     * @return Handler removido ou null se não existia
     */
    public CommandHandler<? extends Command> unregisterHandler(Class<? extends Command> commandType) {
        CommandHandler<? extends Command> removed = handlers.remove(commandType);
        if (removed != null) {
            log.info("Handler removido para comando: {}", commandType.getSimpleName());
        }
        return removed;
    }
    
    /**
     * Obtém o handler para um tipo específico de comando.
     * 
     * @param commandType Tipo do comando
     * @param <T> Tipo do comando
     * @return Handler para o comando
     * @throws CommandHandlerNotFoundException se não existe handler
     */
    @SuppressWarnings("unchecked")
    public <T extends Command> CommandHandler<T> getHandler(Class<T> commandType) {
        CommandHandler<? extends Command> handler = handlers.get(commandType);
        
        if (handler == null) {
            throw new CommandHandlerNotFoundException(commandType);
        }
        
        return (CommandHandler<T>) handler;
    }
    
    /**
     * Verifica se existe handler registrado para o tipo de comando.
     * 
     * @param commandType Tipo do comando a verificar
     * @return true se existe handler, false caso contrário
     */
    public boolean hasHandler(Class<? extends Command> commandType) {
        return handlers.containsKey(commandType);
    }
    
    /**
     * Obtém todos os tipos de comando registrados.
     * 
     * @return Conjunto de tipos de comando com handlers registrados
     */
    public java.util.Set<Class<? extends Command>> getRegisteredCommandTypes() {
        return java.util.Set.copyOf(handlers.keySet());
    }
    
    /**
     * Obtém o número total de handlers registrados.
     * 
     * @return Número de handlers registrados
     */
    public int getHandlerCount() {
        return handlers.size();
    }
    
    /**
     * Remove todos os handlers registrados.
     * 
     * <p>Usado principalmente para testes.</p>
     */
    public void clear() {
        int count = handlers.size();
        handlers.clear();
        log.info("Registry limpo. {} handlers removidos", count);
    }
    
    /**
     * Obtém informações de debug sobre handlers registrados.
     * 
     * @return Mapa com informações de debug
     */
    public Map<String, String> getDebugInfo() {
        Map<String, String> info = new java.util.HashMap<>();
        
        handlers.forEach((commandType, handler) -> {
            info.put(commandType.getSimpleName(), handler.getClass().getSimpleName());
        });
        
        return info;
    }
}