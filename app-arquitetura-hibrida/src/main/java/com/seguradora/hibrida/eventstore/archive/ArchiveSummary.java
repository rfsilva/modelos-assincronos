package com.seguradora.hibrida.eventstore.archive;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Resumo de uma operação de arquivamento em lote.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
public class ArchiveSummary {
    
    private List<ArchiveResult> results = new ArrayList<>();
    private Instant startTime = Instant.now();
    private Instant endTime;
    
    /**
     * Adiciona resultado de arquivamento.
     */
    public void addResult(ArchiveResult result) {
        results.add(result);
    }
    
    /**
     * Finaliza o resumo.
     */
    public void finish() {
        this.endTime = Instant.now();
    }
    
    /**
     * Conta sucessos.
     */
    public long getSuccessCount() {
        return results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
    }
    
    /**
     * Conta erros.
     */
    public long getErrorCount() {
        return results.stream().mapToLong(r -> r.isSuccess() ? 0 : 1).sum();
    }
    
    /**
     * Total de eventos arquivados.
     */
    public long getTotalEvents() {
        return results.stream()
            .filter(ArchiveResult::isSuccess)
            .mapToLong(r -> r.getEventCount() != null ? r.getEventCount() : 0)
            .sum();
    }
    
    /**
     * Total de bytes comprimidos.
     */
    public long getTotalCompressedSize() {
        return results.stream()
            .filter(ArchiveResult::isSuccess)
            .mapToLong(r -> r.getCompressedSize() != null ? r.getCompressedSize() : 0)
            .sum();
    }
    
    /**
     * Duração total da operação.
     */
    public long getDurationMs() {
        if (endTime == null) {
            return Instant.now().toEpochMilli() - startTime.toEpochMilli();
        }
        return endTime.toEpochMilli() - startTime.toEpochMilli();
    }
    
    /**
     * Taxa de sucesso.
     */
    public double getSuccessRate() {
        if (results.isEmpty()) {
            return 0.0;
        }
        return (double) getSuccessCount() / results.size();
    }
}