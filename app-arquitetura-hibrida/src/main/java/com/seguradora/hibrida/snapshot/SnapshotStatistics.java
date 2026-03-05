package com.seguradora.hibrida.snapshot;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

/**
 * Estatísticas detalhadas de snapshots para monitoramento e análise.
 * 
 * <p>Fornece métricas importantes para:
 * <ul>
 *   <li>Monitoramento de performance</li>
 *   <li>Análise de eficiência de compressão</li>
 *   <li>Planejamento de capacidade</li>
 *   <li>Otimização de configurações</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
public class SnapshotStatistics {
    
    /**
     * ID do aggregate (null para estatísticas globais).
     */
    private final String aggregateId;
    
    /**
     * Tipo do aggregate (null para estatísticas globais).
     */
    private final String aggregateType;
    
    /**
     * Número total de snapshots.
     */
    private final long totalSnapshots;
    
    /**
     * Número de snapshots comprimidos.
     */
    private final long compressedSnapshots;
    
    /**
     * Tamanho total original (bytes).
     */
    private final long totalOriginalSize;
    
    /**
     * Tamanho total após compressão (bytes).
     */
    private final long totalCompressedSize;
    
    /**
     * Snapshot mais antigo.
     */
    private final Instant oldestSnapshot;
    
    /**
     * Snapshot mais recente.
     */
    private final Instant newestSnapshot;
    
    /**
     * Versão do snapshot mais recente.
     */
    private final long latestVersion;
    
    /**
     * Tempo médio entre snapshots (segundos).
     */
    private final double averageTimeBetweenSnapshots;
    
    /**
     * Tamanho médio dos snapshots (bytes).
     */
    private final double averageSnapshotSize;
    
    /**
     * Taxa média de compressão.
     */
    private final double averageCompressionRatio;
    
    /**
     * Número de snapshots criados nas últimas 24 horas.
     */
    private final long snapshotsLast24Hours;
    
    /**
     * Número de snapshots criados na última semana.
     */
    private final long snapshotsLastWeek;
    
    /**
     * Espaço total economizado pela compressão (bytes).
     */
    private final long totalSpaceSaved;
    
    /**
     * Calcula a taxa de compressão geral.
     * 
     * @return Taxa de compressão (0.0 a 1.0)
     */
    public double getOverallCompressionRatio() {
        if (totalOriginalSize == 0) {
            return 0.0;
        }
        return 1.0 - ((double) totalCompressedSize / totalOriginalSize);
    }
    
    /**
     * Calcula a porcentagem de snapshots comprimidos.
     * 
     * @return Porcentagem (0.0 a 100.0)
     */
    public double getCompressionPercentage() {
        if (totalSnapshots == 0) {
            return 0.0;
        }
        return ((double) compressedSnapshots / totalSnapshots) * 100.0;
    }
    
    /**
     * Verifica se há snapshots disponíveis.
     * 
     * @return true se existem snapshots, false caso contrário
     */
    public boolean hasSnapshots() {
        return totalSnapshots > 0;
    }
    
    /**
     * Calcula a eficiência de armazenamento.
     * 
     * @return Eficiência (0.0 a 1.0) - quanto maior, melhor
     */
    public double getStorageEfficiency() {
        if (totalOriginalSize == 0) {
            return 1.0;
        }
        return 1.0 - ((double) (totalOriginalSize - totalSpaceSaved) / totalOriginalSize);
    }
    
    /**
     * Calcula a taxa de crescimento de snapshots (por dia).
     * 
     * @return Snapshots por dia
     */
    public double getSnapshotGrowthRate() {
        if (oldestSnapshot == null || newestSnapshot == null) {
            return 0.0;
        }
        
        long daysBetween = java.time.Duration.between(oldestSnapshot, newestSnapshot).toDays();
        if (daysBetween == 0) {
            return totalSnapshots;
        }
        
        return (double) totalSnapshots / daysBetween;
    }
    
    /**
     * Verifica se a compressão está sendo efetiva.
     * 
     * @return true se compressão economiza pelo menos 20% do espaço
     */
    public boolean isCompressionEffective() {
        return getOverallCompressionRatio() >= 0.2;
    }
    
    /**
     * Calcula o tamanho médio dos snapshots comprimidos.
     * 
     * @return Tamanho médio em bytes
     */
    public double getAverageCompressedSize() {
        if (compressedSnapshots == 0) {
            return 0.0;
        }
        return (double) totalCompressedSize / compressedSnapshots;
    }
    
    /**
     * Calcula o tamanho médio dos snapshots não comprimidos.
     * 
     * @return Tamanho médio em bytes
     */
    public double getAverageUncompressedSize() {
        long uncompressedSnapshots = totalSnapshots - compressedSnapshots;
        if (uncompressedSnapshots == 0) {
            return 0.0;
        }
        
        long uncompressedSize = totalOriginalSize - totalCompressedSize;
        return (double) uncompressedSize / uncompressedSnapshots;
    }
}