package com.seguradora.hibrida.command.impl;

import com.seguradora.hibrida.command.*;
import com.seguradora.hibrida.command.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Implementação simples do Command Bus com roteamento automático.
 * 
 * <p>Esta implementação fornece:</p>
 * <ul>
 *   <li>Roteamento automático por tipo de comando</li>
 *   <li>Execução síncrona e assíncrona</li>
 *   <li>Controle de timeout por comando</li>
 *   <li>Coleta de métricas detalhadas</li>
 *   <li>Logs estruturados com correlation ID</li>
 * </ul>
 * 
 * <p><strong>Thread Safety:</strong></p>
 * <p>Esta classe é thread-safe e pode processar comandos concorrentemente.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@Slf4j
public class SimpleCommandBus implements CommandBus {
    
    private final CommandHandlerRegistry handlerRegistry;
    private final CommandBusStatistics statistics;
    private final ExecutorService executorService;
    
    /**
     * Construtor com injeção de dependências.
     * 
     * @param handlerRegistry Registry de handlers
     */
    @Autowired
    public SimpleCommandBus(CommandHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
        this.statistics = new CommandBusStatistics();
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "CommandBus-Async-" + System.currentTimeMillis());
            thread.setDaemon(true);
            return thread;
        });
        
        log.info("SimpleCommandBus inicializado com sucesso");
    }
    
    @Override
    public CommandResult send(Command command) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Processando comando: {} [{}]", 
                     command.getCommandType(), command.getCommandId());
            
            // Validar comando básico
            validateCommand(command);
            
            // Obter handler
            CommandHandler<Command> handler = getHandlerForCommand(command);
            
            // Executar com timeout
            CommandResult result = executeWithTimeout(command, handler);
            
            // Atualizar métricas de sucesso
            long executionTime = System.currentTimeMillis() - startTime;
            updateSuccessMetrics(command, executionTime);
            
            log.debug("Comando processado com sucesso: {} [{}] em {}ms", 
                     command.getCommandType(), command.getCommandId(), executionTime);
            
            return result.withCorrelationId(command.getCorrelationId())
                        .withExecutionTime(executionTime);
            
        } catch (CommandValidationException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            updateRejectedMetrics(command, executionTime);
            
            log.warn("Comando rejeitado por validação: {} [{}] - {}", 
                    command.getCommandType(), command.getCommandId(), e.getMessage());
            
            return CommandResult.failure(e.getMessage(), "VALIDATION_ERROR")
                               .withCorrelationId(command.getCorrelationId())
                               .withExecutionTime(executionTime);
            
        } catch (CommandTimeoutException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            updateTimeoutMetrics(command, executionTime);
            
            log.error("Comando excedeu timeout: {} [{}] - {}", 
                     command.getCommandType(), command.getCommandId(), e.getMessage());
            
            return CommandResult.failure(e.getMessage(), "TIMEOUT_ERROR")
                               .withCorrelationId(command.getCorrelationId())
                               .withExecutionTime(executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            updateFailureMetrics(command, executionTime);
            
            log.error("Erro inesperado ao processar comando: {} [{}]", 
                     command.getCommandType(), command.getCommandId(), e);
            
            return CommandResult.failure(e.getMessage(), "EXECUTION_ERROR")
                               .withCorrelationId(command.getCorrelationId())
                               .withExecutionTime(executionTime);
        }
    }
    
    @Override
    public CompletableFuture<CommandResult> sendAsync(Command command) {
        return CompletableFuture.supplyAsync(() -> send(command), executorService);
    }
    
    @Override
    public <T extends Command> void registerHandler(CommandHandler<T> handler) {
        handlerRegistry.registerHandler(handler);
        statistics.updateRegisteredHandlers(handlerRegistry.getHandlerCount());
        
        log.info("Handler registrado: {} -> {}", 
                handler.getCommandType().getSimpleName(), 
                handler.getClass().getSimpleName());
    }
    
    @Override
    public void unregisterHandler(Class<? extends Command> commandType) {
        handlerRegistry.unregisterHandler(commandType);
        statistics.updateRegisteredHandlers(handlerRegistry.getHandlerCount());
        
        log.info("Handler removido para comando: {}", commandType.getSimpleName());
    }
    
    @Override
    public boolean hasHandler(Class<? extends Command> commandType) {
        return handlerRegistry.hasHandler(commandType);
    }
    
    @Override
    public CommandBusStatistics getStatistics() {
        return statistics;
    }
    
    /**
     * Valida o comando básico.
     * 
     * @param command Comando a ser validado
     * @throws CommandValidationException se a validação falhar
     */
    private void validateCommand(Command command) {
        if (command == null) {
            throw new CommandValidationException("Comando não pode ser null");
        }
        
        if (command.getCommandId() == null) {
            throw new CommandValidationException(command.getClass(), "CommandId é obrigatório");
        }
        
        if (command.getTimestamp() == null) {
            throw new CommandValidationException(command.getClass(), "Timestamp é obrigatório");
        }
    }
    
    /**
     * Obtém o handler apropriado para o comando.
     * 
     * @param command Comando a ser processado
     * @return Handler para o comando
     * @throws CommandHandlerNotFoundException se não existe handler
     */
    @SuppressWarnings("unchecked")
    private CommandHandler<Command> getHandlerForCommand(Command command) {
        Class<? extends Command> commandType = command.getClass();
        return (CommandHandler<Command>) handlerRegistry.getHandler(commandType);
    }
    
    /**
     * Executa o comando com controle de timeout.
     * 
     * @param command Comando a ser executado
     * @param handler Handler para executar o comando
     * @return Resultado da execução
     * @throws CommandTimeoutException se exceder o timeout
     * @throws CommandExecutionException se houver erro na execução
     */
    private CommandResult executeWithTimeout(Command command, CommandHandler<Command> handler) {
        int timeoutSeconds = handler.getTimeoutSeconds();
        
        CompletableFuture<CommandResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                return handler.handle(command);
            } catch (Exception e) {
                throw new CommandExecutionException(command.getClass(), 
                                                  command.getCommandId().toString(), 
                                                  e.getMessage(), e);
            }
        }, executorService);
        
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new CommandTimeoutException(command.getClass(), 
                                            command.getCommandId().toString(), 
                                            timeoutSeconds);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof CommandExecutionException) {
                throw (CommandExecutionException) cause;
            }
            throw new CommandExecutionException(command.getClass(), 
                                              command.getCommandId().toString(), 
                                              e.getMessage(), e);
        }
    }
    
    /**
     * Atualiza métricas para comando executado com sucesso.
     */
    private void updateSuccessMetrics(Command command, long executionTime) {
        statistics.incrementProcessed();
        statistics.updateExecutionTime(executionTime);
        updateCommandTypeMetrics(command.getCommandType(), true, executionTime);
    }
    
    /**
     * Atualiza métricas para comando que falhou.
     */
    private void updateFailureMetrics(Command command, long executionTime) {
        statistics.incrementFailed();
        statistics.updateExecutionTime(executionTime);
        updateCommandTypeMetrics(command.getCommandType(), false, executionTime);
    }
    
    /**
     * Atualiza métricas para comando que excedeu timeout.
     */
    private void updateTimeoutMetrics(Command command, long executionTime) {
        statistics.incrementTimedOut();
        statistics.updateExecutionTime(executionTime);
        updateCommandTypeMetrics(command.getCommandType(), false, executionTime);
    }
    
    /**
     * Atualiza métricas para comando rejeitado por validação.
     */
    private void updateRejectedMetrics(Command command, long executionTime) {
        statistics.incrementRejected();
        updateCommandTypeMetrics(command.getCommandType(), false, executionTime);
    }
    
    /**
     * Atualiza métricas específicas por tipo de comando.
     */
    private void updateCommandTypeMetrics(String commandType, boolean success, long executionTime) {
        CommandBusStatistics.CommandTypeStatistics typeStats = 
                statistics.getCommandTypeStatistics(commandType);
        
        if (typeStats == null) {
            typeStats = new CommandBusStatistics.CommandTypeStatistics();
            statistics.updateCommandTypeStatistics(commandType, typeStats);
        }
        
        if (success) {
            typeStats.getProcessed().incrementAndGet();
        } else {
            typeStats.getFailed().incrementAndGet();
        }
        
        // Atualizar tempos de execução do tipo
        if (executionTime < typeStats.getMinExecutionTimeMs()) {
            typeStats.setMinExecutionTimeMs(executionTime);
        }
        if (executionTime > typeStats.getMaxExecutionTimeMs()) {
            typeStats.setMaxExecutionTimeMs(executionTime);
        }
        
        long total = typeStats.getTotal();
        if (total > 0) {
            double newAverage = ((typeStats.getAverageExecutionTimeMs() * (total - 1)) + executionTime) / total;
            typeStats.setAverageExecutionTimeMs(newAverage);
        }
        
        typeStats.setLastExecuted(Instant.now());
    }
    
    /**
     * Finaliza recursos do Command Bus.
     */
    public void shutdown() {
        log.info("Finalizando SimpleCommandBus...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("SimpleCommandBus finalizado");
    }
}