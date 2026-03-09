package com.seguradora.hibrida.eventstore.replay;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Progresso de execução de um replay de eventos.
 * 
 * <p>Fornece informações em tempo real sobre o status
 * e progresso de um replay em execução.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
public class ReplayProgress {
    
    /**
     * Status do replay.
     */
    public enum Status {
        PENDING,     // Aguardando início
        RUNNING,     // Em execução
        PAUSED,      // Pausado
        COMPLETED,   // Concluído com sucesso
        FAILED,      // Falhou
        CANCELLED    // Cancelado
    }
    
    /**
     * ID único do replay.
     */
    private UUID replayId;
    
    /**
     * Nome do replay.
     */
    private String name;
    
    /**
     * Status atual do replay.
     */
    private Status status;
    
    /**
     * Timestamp de início do replay.
     */
    private Instant startedAt;
    
    /**
     * Timestamp de última atualização.
     */
    @Builder.Default
    private Instant lastUpdatedAt = Instant.now();
    
    /**
     * Timestamp de conclusão (se aplicável).
     */
    private Instant completedAt;
    
    /**
     * Total de eventos a serem processados.
     */
    private long totalEvents;
    
    /**
     * Número de eventos processados.
     */
    private long processedEvents;
    
    /**
     * Número de eventos processados com sucesso.
     */
    private long successfulEvents;
    
    /**
     * Número de eventos que falharam.
     */
    private long failedEvents;
    
    /**
     * Número de eventos ignorados (filtrados).
     */
    private long skippedEvents;
    
    /**
     * Percentual de progresso (0-100).
     */
    public double getProgressPercentage() {
        if (totalEvents == 0) {
            return 0.0;
        }
        return (double) processedEvents / totalEvents * 100.0;
    }
    
    /**
     * Taxa de sucesso (0-100).
     */
    public double getSuccessRate() {
        if (processedEvents == 0) {
            return 0.0;
        }
        return (double) successfulEvents / processedEvents * 100.0;
    }
    
    /**
     * Taxa de erro (0-100).
     */
    public double getErrorRate() {
        if (processedEvents == 0) {
            return 0.0;
        }
        return (double) failedEvents / processedEvents * 100.0;
    }
    
    /**
     * Velocidade atual de processamento (eventos/segundo).
     */
    private double currentThroughput;
    
    /**
     * Velocidade média de processamento (eventos/segundo).
     */
    public double getAverageThroughput() {
        if (startedAt == null || processedEvents == 0) {
            return 0.0;
        }
        
        Duration elapsed = Duration.between(startedAt, 
            completedAt != null ? completedAt : Instant.now());
        
        if (elapsed.isZero()) {
            return 0.0;
        }
        
        return (double) processedEvents / elapsed.toSeconds();
    }
    
    /**
     * Tempo estimado para conclusão.
     */
    public Duration getEstimatedTimeRemaining() {
        if (currentThroughput <= 0 || totalEvents <= processedEvents) {
            return Duration.ZERO;
        }
        
        long remainingEvents = totalEvents - processedEvents;
        long secondsRemaining = (long) (remainingEvents / currentThroughput);
        
        return Duration.ofSeconds(secondsRemaining);
    }
    
    /**
     * Tempo decorrido desde o início.
     */
    public Duration getElapsedTime() {
        if (startedAt == null) {
            return Duration.ZERO;
        }
        
        Instant endTime = completedAt != null ? completedAt : Instant.now();
        return Duration.between(startedAt, endTime);
    }
    
    /**
     * Lote atual sendo processado.
     */
    private int currentBatch;
    
    /**
     * Total de lotes a serem processados.
     */
    private int totalBatches;
    
    /**
     * Último evento processado.
     */
    private String lastProcessedEventId;
    
    /**
     * Último tipo de evento processado.
     */
    private String lastProcessedEventType;
    
    /**
     * Mensagem de erro atual (se houver).
     */
    private String currentError;
    
    /**
     * Lista de erros encontrados durante o processamento.
     */
    @Builder.Default
    private List<String> errors = List.of();
    
    /**
     * Lista de avisos durante o processamento.
     */
    @Builder.Default
    private List<String> warnings = List.of();
    
    /**
     * Configuração do replay.
     */
    private ReplayConfiguration configuration;
    
    /**
     * Metadados adicionais do progresso.
     */
    @Builder.Default
    private java.util.Map<String, Object> metadata = java.util.Map.of();
    
    /**
     * Verifica se o replay está ativo (rodando ou pausado).
     * 
     * @return true se ativo
     */
    public boolean isActive() {
        return status == Status.RUNNING || status == Status.PAUSED;
    }
    
    /**
     * Verifica se o replay foi concluído (sucesso, falha ou cancelado).
     * 
     * @return true se concluído
     */
    public boolean isCompleted() {
        return status == Status.COMPLETED || status == Status.FAILED || status == Status.CANCELLED;
    }
    
    /**
     * Verifica se o replay pode ser pausado.
     * 
     * @return true se pode ser pausado
     */
    public boolean canBePaused() {
        return status == Status.RUNNING;
    }
    
    /**
     * Verifica se o replay pode ser retomado.
     * 
     * @return true se pode ser retomado
     */
    public boolean canBeResumed() {
        return status == Status.PAUSED;
    }
    
    /**
     * Verifica se o replay pode ser cancelado.
     * 
     * @return true se pode ser cancelado
     */
    public boolean canBeCancelled() {
        return status == Status.RUNNING || status == Status.PAUSED || status == Status.PENDING;
    }
    
    /**
     * Cria progresso inicial para um replay.
     * 
     * @param replayId ID do replay
     * @param configuration Configuração do replay
     * @param totalEvents Total de eventos a processar
     * @return Progresso inicial
     */
    public static ReplayProgress initial(UUID replayId, ReplayConfiguration configuration, long totalEvents) {
        return ReplayProgress.builder()
            .replayId(replayId)
            .name(configuration.getName())
            .status(Status.PENDING)
            .configuration(configuration)
            .totalEvents(totalEvents)
            .processedEvents(0)
            .successfulEvents(0)
            .failedEvents(0)
            .skippedEvents(0)
            .currentBatch(0)
            .totalBatches((int) Math.ceil((double) totalEvents / configuration.getBatchSize()))
            .build();
    }
    
    /**
     * Atualiza progresso com novos valores.
     * 
     * @param processedEvents Eventos processados
     * @param successfulEvents Eventos com sucesso
     * @param failedEvents Eventos com falha
     * @param currentThroughput Throughput atual
     * @return Progresso atualizado
     */
    public ReplayProgress updateProgress(long processedEvents, long successfulEvents, 
                                       long failedEvents, double currentThroughput) {
        return this.toBuilder()
            .processedEvents(processedEvents)
            .successfulEvents(successfulEvents)
            .failedEvents(failedEvents)
            .currentThroughput(currentThroughput)
            .lastUpdatedAt(Instant.now())
            .build();
    }
    
    /**
     * Marca replay como iniciado.
     * 
     * @return Progresso atualizado
     */
    public ReplayProgress markAsStarted() {
        return this.toBuilder()
            .status(Status.RUNNING)
            .startedAt(Instant.now())
            .lastUpdatedAt(Instant.now())
            .build();
    }
    
    /**
     * Marca replay como pausado.
     * 
     * @return Progresso atualizado
     */
    public ReplayProgress markAsPaused() {
        return this.toBuilder()
            .status(Status.PAUSED)
            .lastUpdatedAt(Instant.now())
            .build();
    }
    
    /**
     * Marca replay como concluído.
     * 
     * @return Progresso atualizado
     */
    public ReplayProgress markAsCompleted() {
        return this.toBuilder()
            .status(Status.COMPLETED)
            .completedAt(Instant.now())
            .lastUpdatedAt(Instant.now())
            .build();
    }
    
    /**
     * Marca replay como falhado.
     * 
     * @param error Mensagem de erro
     * @return Progresso atualizado
     */
    public ReplayProgress markAsFailed(String error) {
        return this.toBuilder()
            .status(Status.FAILED)
            .currentError(error)
            .completedAt(Instant.now())
            .lastUpdatedAt(Instant.now())
            .build();
    }
    
    /**
     * Marca replay como cancelado.
     * 
     * @return Progresso atualizado
     */
    public ReplayProgress markAsCancelled() {
        return this.toBuilder()
            .status(Status.CANCELLED)
            .completedAt(Instant.now())
            .lastUpdatedAt(Instant.now())
            .build();
    }
}