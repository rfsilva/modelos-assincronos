package com.seguradora.hibrida.projection.rebuild;

import java.time.Instant;

/**
 * Resultado de uma operação de rebuild de projeção.
 * 
 * @param projectionName Nome da projeção
 * @param rebuildType Tipo do rebuild (completo ou incremental)
 * @param status Status do resultado
 * @param eventsProcessed Número de eventos processados
 * @param eventsFailed Número de eventos que falharam
 * @param durationMs Duração em milissegundos
 * @param errorMessage Mensagem de erro (se houver)
 */
public record RebuildResult(
    String projectionName,
    RebuildType rebuildType,
    RebuildStatus status,
    long eventsProcessed,
    long eventsFailed,
    long durationMs,
    String errorMessage
) {
    
    /**
     * Cria um resultado de sucesso.
     */
    public static RebuildResult success(String projectionName, RebuildType type, 
                                      long processed, long failed, long duration) {
        return new RebuildResult(projectionName, type, RebuildStatus.SUCCESS, 
                               processed, failed, duration, null);
    }
    
    /**
     * Cria um resultado de falha.
     */
    public static RebuildResult failure(String projectionName, RebuildType type, 
                                      long processed, long failed, long duration, String error) {
        return new RebuildResult(projectionName, type, RebuildStatus.FAILED, 
                               processed, failed, duration, error);
    }
    
    /**
     * Verifica se o rebuild foi bem-sucedido.
     */
    public boolean isSuccess() {
        return status == RebuildStatus.SUCCESS;
    }
    
    /**
     * Verifica se houve falhas.
     */
    public boolean hasFailed() {
        return status == RebuildStatus.FAILED;
    }
    
    /**
     * Obtém taxa de sucesso.
     */
    public double getSuccessRate() {
        long total = eventsProcessed + eventsFailed;
        return total > 0 ? (double) eventsProcessed / total : 1.0;
    }
    
    /**
     * Obtém throughput em eventos por segundo.
     */
    public double getThroughput() {
        return durationMs > 0 ? (double) eventsProcessed / (durationMs / 1000.0) : 0.0;
    }
    
    /**
     * Obtém timestamp de criação do resultado.
     */
    public Instant getTimestamp() {
        return Instant.now();
    }
    
    @Override
    public String toString() {
        return String.format("RebuildResult[%s %s: %s, processed=%d, failed=%d, duration=%dms, throughput=%.1f/s]",
                           projectionName, rebuildType, status, eventsProcessed, eventsFailed, 
                           durationMs, getThroughput());
    }
}