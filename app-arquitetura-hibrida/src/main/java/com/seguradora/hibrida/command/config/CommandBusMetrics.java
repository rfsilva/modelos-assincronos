package com.seguradora.hibrida.command.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Métricas customizadas para o Command Bus.
 * 
 * <p>Esta classe expõe métricas detalhadas sobre o desempenho
 * do Command Bus para monitoramento via Prometheus/Grafana.</p>
 * 
 * <p><strong>Métricas Expostas:</strong></p>
 * <ul>
 *   <li>commandbus_commands_total - Total de comandos processados</li>
 *   <li>commandbus_commands_failed_total - Total de comandos falhados</li>
 *   <li>commandbus_commands_timeout_total - Total de comandos com timeout</li>
 *   <li>commandbus_commands_rejected_total - Total de comandos rejeitados</li>
 *   <li>commandbus_execution_time - Tempo de execução dos comandos</li>
 *   <li>commandbus_handlers_registered - Número de handlers registrados</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@Slf4j
public class CommandBusMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // Contadores
    private Counter commandsProcessedCounter;
    private Counter commandsFailedCounter;
    private Counter commandsTimeoutCounter;
    private Counter commandsRejectedCounter;
    
    // Timers
    private Timer executionTimer;
    private Timer validationTimer;
    
    // Gauges
    private final AtomicLong registeredHandlers = new AtomicLong(0);
    private final AtomicLong activeCommands = new AtomicLong(0);
    
    /**
     * Construtor com injeção de dependências.
     * 
     * @param meterRegistry Registry de métricas do Micrometer
     */
    @Autowired
    public CommandBusMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Inicializa as métricas.
     */
    @PostConstruct
    public void initializeMetrics() {
        log.info("Inicializando métricas do Command Bus");
        
        // Contadores
        commandsProcessedCounter = Counter.builder("commandbus_commands_processed_total")
                .description("Total de comandos processados com sucesso")
                .register(meterRegistry);
        
        commandsFailedCounter = Counter.builder("commandbus_commands_failed_total")
                .description("Total de comandos que falharam")
                .register(meterRegistry);
        
        commandsTimeoutCounter = Counter.builder("commandbus_commands_timeout_total")
                .description("Total de comandos que excederam timeout")
                .register(meterRegistry);
        
        commandsRejectedCounter = Counter.builder("commandbus_commands_rejected_total")
                .description("Total de comandos rejeitados por validação")
                .register(meterRegistry);
        
        // Timers
        executionTimer = Timer.builder("commandbus_execution_time")
                .description("Tempo de execução dos comandos")
                .register(meterRegistry);
        
        validationTimer = Timer.builder("commandbus_validation_time")
                .description("Tempo de validação dos comandos")
                .register(meterRegistry);
        
        // Gauges usando a sintaxe correta do Micrometer
        meterRegistry.gauge("commandbus_handlers_registered", registeredHandlers, AtomicLong::doubleValue);
        meterRegistry.gauge("commandbus_active_commands", activeCommands, AtomicLong::doubleValue);
        
        log.info("Métricas do Command Bus inicializadas com sucesso");
    }
    
    /**
     * Incrementa contador de comandos processados.
     */
    public void incrementCommandsProcessed() {
        commandsProcessedCounter.increment();
    }
    
    /**
     * Incrementa contador de comandos processados por tipo.
     * 
     * @param commandType Tipo do comando
     */
    public void incrementCommandsProcessed(String commandType) {
        Counter.builder("commandbus_commands_processed_total")
                .tag("command_type", commandType)
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Incrementa contador de comandos falhados.
     */
    public void incrementCommandsFailed() {
        commandsFailedCounter.increment();
    }
    
    /**
     * Incrementa contador de comandos falhados por tipo.
     * 
     * @param commandType Tipo do comando
     * @param errorType Tipo do erro
     */
    public void incrementCommandsFailed(String commandType, String errorType) {
        Counter.builder("commandbus_commands_failed_total")
                .tag("command_type", commandType)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Incrementa contador de comandos com timeout.
     */
    public void incrementCommandsTimeout() {
        commandsTimeoutCounter.increment();
    }
    
    /**
     * Incrementa contador de comandos com timeout por tipo.
     * 
     * @param commandType Tipo do comando
     */
    public void incrementCommandsTimeout(String commandType) {
        Counter.builder("commandbus_commands_timeout_total")
                .tag("command_type", commandType)
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Incrementa contador de comandos rejeitados.
     */
    public void incrementCommandsRejected() {
        commandsRejectedCounter.increment();
    }
    
    /**
     * Incrementa contador de comandos rejeitados por tipo.
     * 
     * @param commandType Tipo do comando
     */
    public void incrementCommandsRejected(String commandType) {
        Counter.builder("commandbus_commands_rejected_total")
                .tag("command_type", commandType)
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Inicia timer de execução.
     * 
     * @return Sample do timer
     */
    public Timer.Sample startExecutionTimer() {
        activeCommands.incrementAndGet();
        return Timer.start(meterRegistry);
    }
    
    /**
     * Para timer de execução.
     * 
     * @param sample Sample do timer
     */
    public void stopExecutionTimer(Timer.Sample sample) {
        sample.stop(executionTimer);
        activeCommands.decrementAndGet();
    }
    
    /**
     * Para timer de execução por tipo de comando.
     * 
     * @param sample Sample do timer
     * @param commandType Tipo do comando
     */
    public void stopExecutionTimer(Timer.Sample sample, String commandType) {
        Timer commandTimer = Timer.builder("commandbus_execution_time")
                .tag("command_type", commandType)
                .register(meterRegistry);
        
        sample.stop(commandTimer);
        activeCommands.decrementAndGet();
    }
    
    /**
     * Inicia timer de validação.
     * 
     * @return Sample do timer
     */
    public Timer.Sample startValidationTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Para timer de validação.
     * 
     * @param sample Sample do timer
     */
    public void stopValidationTimer(Timer.Sample sample) {
        sample.stop(validationTimer);
    }
    
    /**
     * Atualiza número de handlers registrados.
     * 
     * @param count Número de handlers
     */
    public void updateRegisteredHandlers(int count) {
        registeredHandlers.set(count);
    }
    
    /**
     * Obtém número de handlers registrados.
     * 
     * @return Número de handlers
     */
    public double getRegisteredHandlersCount() {
        return registeredHandlers.get();
    }
    
    /**
     * Obtém número de comandos ativos.
     * 
     * @return Número de comandos sendo executados
     */
    public double getActiveCommandsCount() {
        return activeCommands.get();
    }
    
    /**
     * Registra tempo de execução customizado.
     * 
     * @param commandType Tipo do comando
     * @param executionTimeMs Tempo de execução em milissegundos
     */
    public void recordExecutionTime(String commandType, long executionTimeMs) {
        Timer.builder("commandbus_execution_time")
                .tag("command_type", commandType)
                .register(meterRegistry)
                .record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}