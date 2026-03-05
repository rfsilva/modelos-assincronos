package com.seguradora.hibrida.snapshot;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;

/**
 * Métricas de eficiência de snapshots para análise de performance.
 * 
 * <p>Fornece dados detalhados sobre:
 * <ul>
 *   <li>Tempo economizado na reconstrução de aggregates</li>
 *   <li>Eficiência da compressão</li>
 *   <li>Impacto na performance do sistema</li>
 *   <li>Recomendações de otimização</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
public class SnapshotEfficiencyMetrics {
    
    /**
     * ID do aggregate analisado.
     */
    private final String aggregateId;
    
    /**
     * Período de análise (início).
     */
    private final Instant periodStart;
    
    /**
     * Período de análise (fim).
     */
    private final Instant periodEnd;
    
    /**
     * Número de reconstruções usando snapshot.
     */
    private final long reconstructionsWithSnapshot;
    
    /**
     * Número de reconstruções sem snapshot (replay completo).
     */
    private final long reconstructionsWithoutSnapshot;
    
    /**
     * Tempo médio de reconstrução com snapshot (milissegundos).
     */
    private final double averageReconstructionTimeWithSnapshot;
    
    /**
     * Tempo médio de reconstrução sem snapshot (milissegundos).
     */
    private final double averageReconstructionTimeWithoutSnapshot;
    
    /**
     * Número médio de eventos processados com snapshot.
     */
    private final double averageEventsWithSnapshot;
    
    /**
     * Número médio de eventos processados sem snapshot.
     */
    private final double averageEventsWithoutSnapshot;
    
    /**
     * Espaço total economizado pela compressão (bytes).
     */
    private final long totalSpaceSaved;
    
    /**
     * Número de snapshots criados no período.
     */
    private final long snapshotsCreated;
    
    /**
     * Número de snapshots que falharam.
     */
    private final long snapshotsFailed;
    
    /**
     * Tempo médio para criar um snapshot (milissegundos).
     */
    private final double averageSnapshotCreationTime;
    
    /**
     * Taxa de compressão média dos snapshots.
     */
    private final double averageCompressionRatio;
    
    /**
     * Calcula a melhoria de performance proporcionada pelos snapshots.
     * 
     * @return Fator de melhoria (ex: 5.0 = 5x mais rápido)
     */
    public double getPerformanceImprovement() {
        if (averageReconstructionTimeWithSnapshot == 0) {
            return 0.0;
        }
        return averageReconstructionTimeWithoutSnapshot / averageReconstructionTimeWithSnapshot;
    }
    
    /**
     * Calcula a porcentagem de tempo economizado.
     * 
     * @return Porcentagem (0.0 a 100.0)
     */
    public double getTimeSavedPercentage() {
        if (averageReconstructionTimeWithoutSnapshot == 0) {
            return 0.0;
        }
        
        double timeSaved = averageReconstructionTimeWithoutSnapshot - averageReconstructionTimeWithSnapshot;
        return (timeSaved / averageReconstructionTimeWithoutSnapshot) * 100.0;
    }
    
    /**
     * Calcula a redução no número de eventos processados.
     * 
     * @return Porcentagem de eventos economizados (0.0 a 100.0)
     */
    public double getEventReductionPercentage() {
        if (averageEventsWithoutSnapshot == 0) {
            return 0.0;
        }
        
        double eventsReduced = averageEventsWithoutSnapshot - averageEventsWithSnapshot;
        return (eventsReduced / averageEventsWithoutSnapshot) * 100.0;
    }
    
    /**
     * Calcula a taxa de sucesso na criação de snapshots.
     * 
     * @return Taxa de sucesso (0.0 a 1.0)
     */
    public double getSnapshotSuccessRate() {
        long totalAttempts = snapshotsCreated + snapshotsFailed;
        if (totalAttempts == 0) {
            return 1.0;
        }
        return (double) snapshotsCreated / totalAttempts;
    }
    
    /**
     * Calcula o número total de reconstruções.
     * 
     * @return Total de reconstruções
     */
    public long getTotalReconstructions() {
        return reconstructionsWithSnapshot + reconstructionsWithoutSnapshot;
    }
    
    /**
     * Calcula a porcentagem de reconstruções que usaram snapshot.
     * 
     * @return Porcentagem (0.0 a 100.0)
     */
    public double getSnapshotUsagePercentage() {
        long total = getTotalReconstructions();
        if (total == 0) {
            return 0.0;
        }
        return ((double) reconstructionsWithSnapshot / total) * 100.0;
    }
    
    /**
     * Verifica se os snapshots estão sendo efetivos.
     * 
     * @return true se snapshots melhoram performance em pelo menos 50%
     */
    public boolean areSnapshotsEffective() {
        return getPerformanceImprovement() >= 1.5;
    }
    
    /**
     * Calcula o tempo total economizado no período.
     * 
     * @return Tempo economizado em milissegundos
     */
    public double getTotalTimeSaved() {
        double timeSavedPerReconstruction = averageReconstructionTimeWithoutSnapshot - averageReconstructionTimeWithSnapshot;
        return timeSavedPerReconstruction * reconstructionsWithSnapshot;
    }
    
    /**
     * Calcula a duração do período de análise.
     * 
     * @return Duração em dias
     */
    public long getAnalysisPeriodDays() {
        return Duration.between(periodStart, periodEnd).toDays();
    }
    
    /**
     * Calcula a frequência média de reconstruções por dia.
     * 
     * @return Reconstruções por dia
     */
    public double getReconstructionFrequency() {
        long days = getAnalysisPeriodDays();
        if (days == 0) {
            return getTotalReconstructions();
        }
        return (double) getTotalReconstructions() / days;
    }
    
    /**
     * Gera recomendação baseada nas métricas.
     * 
     * @return Texto com recomendação
     */
    public String getRecommendation() {
        if (snapshotsCreated == 0) {
            return "Nenhum snapshot criado no período. Considere habilitar snapshots automáticos.";
        }
        
        if (getSnapshotSuccessRate() < 0.9) {
            return "Taxa de falha alta na criação de snapshots. Verifique logs e configurações.";
        }
        
        if (!areSnapshotsEffective()) {
            return "Snapshots não estão proporcionando melhoria significativa. Considere ajustar threshold.";
        }
        
        if (getSnapshotUsagePercentage() < 50) {
            return "Baixo uso de snapshots. Considere reduzir threshold ou aumentar frequência.";
        }
        
        if (averageCompressionRatio < 0.2) {
            return "Compressão pouco efetiva. Considere ajustar algoritmo ou threshold.";
        }
        
        return "Snapshots funcionando adequadamente. Performance otimizada.";
    }
}