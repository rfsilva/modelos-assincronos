package com.seguradora.hibrida.eventstore.replay;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resultado de execução de um replay de eventos.
 * 
 * <p>Contém informações completas sobre o resultado
 * de um replay, incluindo estatísticas, erros e relatórios.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
public class ReplayResult {
    
    /**
     * Status final do replay.
     */
    public enum FinalStatus {
        SUCCESS,     // Concluído com sucesso
        PARTIAL,     // Concluído com alguns erros
        FAILED,      // Falhou completamente
        CANCELLED    // Cancelado pelo usuário
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
     * Descrição do replay.
     */
    private String description;
    
    /**
     * Status final do replay.
     */
    private FinalStatus status;
    
    /**
     * Timestamp de início.
     */
    private Instant startedAt;
    
    /**
     * Timestamp de conclusão.
     */
    private Instant completedAt;
    
    /**
     * Duração total da execução.
     */
    public Duration getDuration() {
        if (startedAt == null || completedAt == null) {
            return Duration.ZERO;
        }
        return Duration.between(startedAt, completedAt);
    }
    
    /**
     * Total de eventos processados.
     */
    private long totalEventsProcessed;
    
    /**
     * Eventos processados com sucesso.
     */
    private long successfulEvents;
    
    /**
     * Eventos que falharam.
     */
    private long failedEvents;
    
    /**
     * Eventos ignorados (filtrados).
     */
    private long skippedEvents;
    
    /**
     * Taxa de sucesso (0-100).
     */
    public double getSuccessRate() {
        if (totalEventsProcessed == 0) {
            return 0.0;
        }
        return (double) successfulEvents / totalEventsProcessed * 100.0;
    }
    
    /**
     * Taxa de erro (0-100).
     */
    public double getErrorRate() {
        if (totalEventsProcessed == 0) {
            return 0.0;
        }
        return (double) failedEvents / totalEventsProcessed * 100.0;
    }
    
    /**
     * Throughput médio (eventos/segundo).
     */
    public double getAverageThroughput() {
        Duration duration = getDuration();
        if (duration.isZero() || totalEventsProcessed == 0) {
            return 0.0;
        }
        return (double) totalEventsProcessed / duration.toSeconds();
    }
    
    /**
     * Throughput máximo alcançado (eventos/segundo).
     */
    private double peakThroughput;
    
    /**
     * Número de lotes processados.
     */
    private int batchesProcessed;
    
    /**
     * Tamanho médio dos lotes.
     */
    public double getAverageBatchSize() {
        if (batchesProcessed == 0) {
            return 0.0;
        }
        return (double) totalEventsProcessed / batchesProcessed;
    }
    
    /**
     * Lista de erros encontrados durante o processamento.
     */
    @Builder.Default
    private List<ReplayError> errors = List.of();
    
    /**
     * Lista de avisos durante o processamento.
     */
    @Builder.Default
    private List<String> warnings = List.of();
    
    /**
     * Estatísticas por tipo de evento.
     */
    @Builder.Default
    private Map<String, EventTypeStatistics> eventTypeStatistics = Map.of();
    
    /**
     * Estatísticas por handler.
     */
    @Builder.Default
    private Map<String, HandlerStatistics> handlerStatistics = Map.of();
    
    /**
     * Configuração utilizada no replay.
     */
    private ReplayConfiguration configuration;
    
    /**
     * Relatório detalhado (se solicitado).
     */
    private ReplayDetailedReport detailedReport;
    
    /**
     * Metadados adicionais do resultado.
     */
    @Builder.Default
    private Map<String, Object> metadata = Map.of();
    
    /**
     * Usuário que iniciou o replay.
     */
    private String initiatedBy;
    
    /**
     * Verifica se o replay foi bem-sucedido.
     * 
     * @return true se bem-sucedido
     */
    public boolean isSuccessful() {
        return status == FinalStatus.SUCCESS;
    }
    
    /**
     * Verifica se o replay teve sucesso parcial.
     * 
     * @return true se teve sucesso parcial
     */
    public boolean isPartialSuccess() {
        return status == FinalStatus.PARTIAL;
    }
    
    /**
     * Verifica se o replay falhou completamente.
     * 
     * @return true se falhou
     */
    public boolean isFailed() {
        return status == FinalStatus.FAILED;
    }
    
    /**
     * Verifica se o replay foi cancelado.
     * 
     * @return true se cancelado
     */
    public boolean isCancelled() {
        return status == FinalStatus.CANCELLED;
    }
    
    /**
     * Obtém resumo textual do resultado.
     * 
     * @return Resumo do resultado
     */
    public String getSummary() {
        return String.format(
            "Replay '%s' %s: %d eventos processados (%d sucesso, %d falhas) em %s - %.2f eventos/s",
            name,
            status.name().toLowerCase(),
            totalEventsProcessed,
            successfulEvents,
            failedEvents,
            formatDuration(getDuration()),
            getAverageThroughput()
        );
    }
    
    private String formatDuration(Duration duration) {
        long seconds = duration.toSeconds();
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
    
    /**
     * Cria resultado de sucesso.
     * 
     * @param replayId ID do replay
     * @param configuration Configuração utilizada
     * @param progress Progresso final
     * @return Resultado de sucesso
     */
    public static ReplayResult success(UUID replayId, ReplayConfiguration configuration, 
                                     ReplayProgress progress) {
        return ReplayResult.builder()
            .replayId(replayId)
            .name(configuration.getName())
            .description(configuration.getDescription())
            .status(FinalStatus.SUCCESS)
            .startedAt(progress.getStartedAt())
            .completedAt(progress.getCompletedAt())
            .totalEventsProcessed(progress.getProcessedEvents())
            .successfulEvents(progress.getSuccessfulEvents())
            .failedEvents(progress.getFailedEvents())
            .skippedEvents(progress.getSkippedEvents())
            .batchesProcessed(progress.getCurrentBatch())
            .configuration(configuration)
            .initiatedBy(configuration.getInitiatedBy())
            .build();
    }
    
    /**
     * Cria resultado de falha.
     * 
     * @param replayId ID do replay
     * @param configuration Configuração utilizada
     * @param progress Progresso final
     * @param errors Lista de erros
     * @return Resultado de falha
     */
    public static ReplayResult failure(UUID replayId, ReplayConfiguration configuration,
                                     ReplayProgress progress, List<ReplayError> errors) {
        FinalStatus status = progress.getSuccessfulEvents() > 0 ? 
            FinalStatus.PARTIAL : FinalStatus.FAILED;
            
        return ReplayResult.builder()
            .replayId(replayId)
            .name(configuration.getName())
            .description(configuration.getDescription())
            .status(status)
            .startedAt(progress.getStartedAt())
            .completedAt(progress.getCompletedAt())
            .totalEventsProcessed(progress.getProcessedEvents())
            .successfulEvents(progress.getSuccessfulEvents())
            .failedEvents(progress.getFailedEvents())
            .skippedEvents(progress.getSkippedEvents())
            .batchesProcessed(progress.getCurrentBatch())
            .errors(errors)
            .configuration(configuration)
            .initiatedBy(configuration.getInitiatedBy())
            .build();
    }
    
    /**
     * Cria resultado de cancelamento.
     * 
     * @param replayId ID do replay
     * @param configuration Configuração utilizada
     * @param progress Progresso final
     * @return Resultado de cancelamento
     */
    public static ReplayResult cancelled(UUID replayId, ReplayConfiguration configuration,
                                       ReplayProgress progress) {
        return ReplayResult.builder()
            .replayId(replayId)
            .name(configuration.getName())
            .description(configuration.getDescription())
            .status(FinalStatus.CANCELLED)
            .startedAt(progress.getStartedAt())
            .completedAt(progress.getCompletedAt())
            .totalEventsProcessed(progress.getProcessedEvents())
            .successfulEvents(progress.getSuccessfulEvents())
            .failedEvents(progress.getFailedEvents())
            .skippedEvents(progress.getSkippedEvents())
            .batchesProcessed(progress.getCurrentBatch())
            .configuration(configuration)
            .initiatedBy(configuration.getInitiatedBy())
            .build();
    }
    
    /**
     * Estatísticas por tipo de evento.
     */
    @Data
    @Builder
    @Jacksonized
    public static class EventTypeStatistics {
        private String eventType;
        private long totalEvents;
        private long successfulEvents;
        private long failedEvents;
        private double averageProcessingTime;
        private long maxProcessingTime;
        private long minProcessingTime;
    }
    
    /**
     * Estatísticas por handler.
     */
    @Data
    @Builder
    @Jacksonized
    public static class HandlerStatistics {
        private String handlerName;
        private long eventsProcessed;
        private long successfulEvents;
        private long failedEvents;
        private double averageProcessingTime;
        private long totalProcessingTime;
    }
}