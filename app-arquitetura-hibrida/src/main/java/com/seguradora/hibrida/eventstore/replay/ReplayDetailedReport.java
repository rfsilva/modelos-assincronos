package com.seguradora.hibrida.eventstore.replay;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Relatório detalhado de execução de replay.
 * 
 * <p>Contém informações detalhadas sobre o processamento
 * de cada evento e handler durante o replay.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@Jacksonized
public class ReplayDetailedReport {
    
    /**
     * Timestamp de geração do relatório.
     */
    @Builder.Default
    private Instant generatedAt = Instant.now();
    
    /**
     * Resumo executivo do replay.
     */
    private String executiveSummary;
    
    /**
     * Detalhes de processamento por lote.
     */
    @Builder.Default
    private List<BatchProcessingDetail> batchDetails = List.of();
    
    /**
     * Análise de performance por tipo de evento.
     */
    @Builder.Default
    private Map<String, EventTypeAnalysis> eventTypeAnalysis = Map.of();
    
    /**
     * Análise de performance por handler.
     */
    @Builder.Default
    private Map<String, HandlerAnalysis> handlerAnalysis = Map.of();
    
    /**
     * Eventos que falharam com detalhes.
     */
    @Builder.Default
    private List<FailedEventDetail> failedEvents = List.of();
    
    /**
     * Eventos que foram ignorados.
     */
    @Builder.Default
    private List<SkippedEventDetail> skippedEvents = List.of();
    
    /**
     * Comparação de estados antes/depois (modo simulação).
     */
    private StateComparisonReport stateComparison;
    
    /**
     * Recomendações baseadas na análise.
     */
    @Builder.Default
    private List<String> recommendations = List.of();
    
    /**
     * Detalhe de processamento de um lote.
     */
    @Data
    @Builder
    @Jacksonized
    public static class BatchProcessingDetail {
        private int batchNumber;
        private Instant startTime;
        private Instant endTime;
        private int eventsInBatch;
        private int successfulEvents;
        private int failedEvents;
        private double averageProcessingTime;
        private List<String> errors;
    }
    
    /**
     * Análise de performance por tipo de evento.
     */
    @Data
    @Builder
    @Jacksonized
    public static class EventTypeAnalysis {
        private String eventType;
        private long totalEvents;
        private long successfulEvents;
        private long failedEvents;
        private double averageProcessingTime;
        private double minProcessingTime;
        private double maxProcessingTime;
        private double standardDeviation;
        private List<String> commonErrors;
        private String performanceAssessment;
    }
    
    /**
     * Análise de performance por handler.
     */
    @Data
    @Builder
    @Jacksonized
    public static class HandlerAnalysis {
        private String handlerName;
        private long eventsProcessed;
        private long successfulEvents;
        private long failedEvents;
        private double totalProcessingTime;
        private double averageProcessingTime;
        private double throughput;
        private List<String> bottlenecks;
        private String performanceRating;
    }
    
    /**
     * Detalhe de evento que falhou.
     */
    @Data
    @Builder
    @Jacksonized
    public static class FailedEventDetail {
        private String eventId;
        private String eventType;
        private String aggregateId;
        private Instant eventTimestamp;
        private String handlerName;
        private String errorMessage;
        private String errorType;
        private int attemptNumber;
        private String eventData;
    }
    
    /**
     * Detalhe de evento que foi ignorado.
     */
    @Data
    @Builder
    @Jacksonized
    public static class SkippedEventDetail {
        private String eventId;
        private String eventType;
        private String aggregateId;
        private Instant eventTimestamp;
        private String skipReason;
        private String filterApplied;
    }
    
    /**
     * Relatório de comparação de estados (modo simulação).
     */
    @Data
    @Builder
    @Jacksonized
    public static class StateComparisonReport {
        private Map<String, Object> stateBefore;
        private Map<String, Object> stateAfter;
        private List<String> differences;
        private String impactAssessment;
        private List<String> potentialIssues;
    }
}