package com.seguradora.hibrida.eventstore.replay;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.Map;

/**
 * Estatísticas de execução do sistema de replay.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@Jacksonized
public class ReplayStatistics {
    
    /**
     * Timestamp das estatísticas.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    /**
     * Total de replays executados.
     */
    private long totalReplaysExecuted;
    
    /**
     * Replays bem-sucedidos.
     */
    private long successfulReplays;
    
    /**
     * Replays que falharam.
     */
    private long failedReplays;
    
    /**
     * Replays cancelados.
     */
    private long cancelledReplays;
    
    /**
     * Replays atualmente ativos.
     */
    private int activeReplays;
    
    /**
     * Total de eventos reprocessados.
     */
    private long totalEventsReprocessed;
    
    /**
     * Eventos reprocessados com sucesso.
     */
    private long successfulEventsReprocessed;
    
    /**
     * Eventos que falharam no reprocessamento.
     */
    private long failedEventsReprocessed;
    
    /**
     * Tempo médio de execução de replay (segundos).
     */
    private double averageReplayDuration;
    
    /**
     * Throughput médio (eventos/segundo).
     */
    private double averageThroughput;
    
    /**
     * Throughput máximo alcançado (eventos/segundo).
     */
    private double peakThroughput;
    
    /**
     * Taxa de sucesso geral (0-100).
     */
    public double getOverallSuccessRate() {
        if (totalReplaysExecuted == 0) {
            return 0.0;
        }
        return (double) successfulReplays / totalReplaysExecuted * 100.0;
    }
    
    /**
     * Taxa de erro geral (0-100).
     */
    public double getOverallErrorRate() {
        if (totalReplaysExecuted == 0) {
            return 0.0;
        }
        return (double) failedReplays / totalReplaysExecuted * 100.0;
    }
    
    /**
     * Taxa de sucesso de eventos (0-100).
     */
    public double getEventSuccessRate() {
        if (totalEventsReprocessed == 0) {
            return 0.0;
        }
        return (double) successfulEventsReprocessed / totalEventsReprocessed * 100.0;
    }
    
    /**
     * Estatísticas por tipo de replay.
     */
    @Builder.Default
    private Map<String, ReplayTypeStatistics> replayTypeStatistics = Map.of();
    
    /**
     * Estatísticas por período (últimas 24h, 7 dias, 30 dias).
     */
    @Builder.Default
    private Map<String, PeriodStatistics> periodStatistics = Map.of();
    
    /**
     * Estatísticas por tipo de replay.
     */
    @Data
    @Builder
    @Jacksonized
    public static class ReplayTypeStatistics {
        private String replayType;
        private long totalExecutions;
        private long successfulExecutions;
        private long failedExecutions;
        private double averageDuration;
        private double averageThroughput;
    }
    
    /**
     * Estatísticas por período.
     */
    @Data
    @Builder
    @Jacksonized
    public static class PeriodStatistics {
        private String period;
        private long replaysExecuted;
        private long eventsProcessed;
        private double averageThroughput;
        private double successRate;
    }
}