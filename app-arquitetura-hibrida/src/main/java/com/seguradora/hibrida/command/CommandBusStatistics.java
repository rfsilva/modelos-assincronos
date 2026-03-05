package com.seguradora.hibrida.command;

import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Estatísticas de execução do Command Bus.
 * 
 * <p>Esta classe fornece métricas detalhadas sobre o desempenho
 * e uso do Command Bus, incluindo contadores, tempos de execução
 * e taxas de erro.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
public class CommandBusStatistics {
    
    /**
     * Total de comandos processados com sucesso.
     */
    private final AtomicLong totalCommandsProcessed;
    
    /**
     * Total de comandos que falharam.
     */
    private final AtomicLong totalCommandsFailed;
    
    /**
     * Total de comandos que excederam timeout.
     */
    private final AtomicLong totalCommandsTimedOut;
    
    /**
     * Total de comandos rejeitados por validação.
     */
    private final AtomicLong totalCommandsRejected;
    
    /**
     * Tempo médio de execução em milissegundos.
     */
    private volatile double averageExecutionTimeMs;
    
    /**
     * Tempo mínimo de execução em milissegundos.
     */
    private volatile long minExecutionTimeMs;
    
    /**
     * Tempo máximo de execução em milissegundos.
     */
    private volatile long maxExecutionTimeMs;
    
    /**
     * Timestamp da última atualização das estatísticas.
     */
    private volatile Instant lastUpdated;
    
    /**
     * Timestamp de início da coleta de estatísticas.
     */
    private final Instant startedAt;
    
    /**
     * Estatísticas por tipo de comando.
     */
    private final Map<String, CommandTypeStatistics> commandTypeStats;
    
    /**
     * Número de handlers registrados.
     */
    private volatile int registeredHandlers;
    
    /**
     * Construtor padrão para inicialização.
     */
    public CommandBusStatistics() {
        this.totalCommandsProcessed = new AtomicLong(0);
        this.totalCommandsFailed = new AtomicLong(0);
        this.totalCommandsTimedOut = new AtomicLong(0);
        this.totalCommandsRejected = new AtomicLong(0);
        this.averageExecutionTimeMs = 0.0;
        this.minExecutionTimeMs = Long.MAX_VALUE;
        this.maxExecutionTimeMs = 0L;
        this.lastUpdated = Instant.now();
        this.startedAt = Instant.now();
        this.commandTypeStats = new ConcurrentHashMap<>();
        this.registeredHandlers = 0;
    }
    
    /**
     * Obtém o total de comandos processados (sucesso + falha).
     * 
     * @return Total de comandos processados
     */
    public long getTotalCommands() {
        return totalCommandsProcessed.get() + totalCommandsFailed.get() + 
               totalCommandsTimedOut.get() + totalCommandsRejected.get();
    }
    
    /**
     * Calcula a taxa de sucesso em percentual.
     * 
     * @return Taxa de sucesso (0-100)
     */
    public double getSuccessRate() {
        long total = getTotalCommands();
        if (total == 0) {
            return 0.0;
        }
        return (totalCommandsProcessed.get() * 100.0) / total;
    }
    
    /**
     * Calcula a taxa de erro em percentual.
     * 
     * @return Taxa de erro (0-100)
     */
    public double getErrorRate() {
        long total = getTotalCommands();
        if (total == 0) {
            return 0.0;
        }
        long errors = totalCommandsFailed.get() + totalCommandsTimedOut.get() + totalCommandsRejected.get();
        return (errors * 100.0) / total;
    }
    
    /**
     * Calcula o throughput em comandos por segundo.
     * 
     * @return Comandos por segundo
     */
    public double getThroughputPerSecond() {
        long durationSeconds = java.time.Duration.between(startedAt, Instant.now()).getSeconds();
        if (durationSeconds == 0) {
            return 0.0;
        }
        return (double) getTotalCommands() / durationSeconds;
    }
    
    /**
     * Obtém estatísticas para um tipo específico de comando.
     * 
     * @param commandType Nome do tipo do comando
     * @return Estatísticas do tipo ou null se não existe
     */
    public CommandTypeStatistics getCommandTypeStatistics(String commandType) {
        return commandTypeStats.get(commandType);
    }
    
    /**
     * Adiciona ou atualiza estatísticas para um tipo de comando.
     * 
     * @param commandType Nome do tipo do comando
     * @param stats Estatísticas do tipo
     */
    public void updateCommandTypeStatistics(String commandType, CommandTypeStatistics stats) {
        commandTypeStats.put(commandType, stats);
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Incrementa contador de comandos processados com sucesso.
     */
    public void incrementProcessed() {
        totalCommandsProcessed.incrementAndGet();
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Incrementa contador de comandos que falharam.
     */
    public void incrementFailed() {
        totalCommandsFailed.incrementAndGet();
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Incrementa contador de comandos que excederam timeout.
     */
    public void incrementTimedOut() {
        totalCommandsTimedOut.incrementAndGet();
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Incrementa contador de comandos rejeitados por validação.
     */
    public void incrementRejected() {
        totalCommandsRejected.incrementAndGet();
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Atualiza tempos de execução.
     * 
     * @param executionTimeMs Tempo de execução em milissegundos
     */
    public void updateExecutionTime(long executionTimeMs) {
        // Atualizar min/max
        if (executionTimeMs < minExecutionTimeMs) {
            minExecutionTimeMs = executionTimeMs;
        }
        if (executionTimeMs > maxExecutionTimeMs) {
            maxExecutionTimeMs = executionTimeMs;
        }
        
        // Calcular nova média (aproximação simples)
        long total = getTotalCommands();
        if (total > 0) {
            averageExecutionTimeMs = ((averageExecutionTimeMs * (total - 1)) + executionTimeMs) / total;
        }
        
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Atualiza número de handlers registrados.
     * 
     * @param count Número de handlers
     */
    public void updateRegisteredHandlers(int count) {
        this.registeredHandlers = count;
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Reseta todas as estatísticas.
     */
    public void reset() {
        totalCommandsProcessed.set(0);
        totalCommandsFailed.set(0);
        totalCommandsTimedOut.set(0);
        totalCommandsRejected.set(0);
        averageExecutionTimeMs = 0.0;
        minExecutionTimeMs = Long.MAX_VALUE;
        maxExecutionTimeMs = 0L;
        commandTypeStats.clear();
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Estatísticas específicas por tipo de comando.
     */
    @Data
    public static class CommandTypeStatistics {
        private final AtomicLong processed;
        private final AtomicLong failed;
        private final AtomicLong timedOut;
        private final AtomicLong rejected;
        private volatile double averageExecutionTimeMs;
        private volatile long minExecutionTimeMs;
        private volatile long maxExecutionTimeMs;
        private volatile Instant lastExecuted;
        
        public CommandTypeStatistics() {
            this.processed = new AtomicLong(0);
            this.failed = new AtomicLong(0);
            this.timedOut = new AtomicLong(0);
            this.rejected = new AtomicLong(0);
            this.averageExecutionTimeMs = 0.0;
            this.minExecutionTimeMs = Long.MAX_VALUE;
            this.maxExecutionTimeMs = 0L;
            this.lastExecuted = Instant.now();
        }
        
        public long getTotal() {
            return processed.get() + failed.get() + timedOut.get() + rejected.get();
        }
        
        public double getSuccessRate() {
            long total = getTotal();
            return total == 0 ? 0.0 : (processed.get() * 100.0) / total;
        }
    }
}