package com.seguradora.hibrida.eventstore.archive;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Resultado de uma operação de arquivamento.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
public class ArchiveResult {
    
    private String partitionName;
    private boolean success;
    private String errorMessage;
    private Long eventCount;
    private Long compressedSize;
    private Instant timestamp;
    
    /**
     * Cria resultado de sucesso.
     */
    public static ArchiveResult success(String partitionName, long eventCount, long compressedSize) {
        return ArchiveResult.builder()
            .partitionName(partitionName)
            .success(true)
            .eventCount(eventCount)
            .compressedSize(compressedSize)
            .timestamp(Instant.now())
            .build();
    }
    
    /**
     * Cria resultado de erro.
     */
    public static ArchiveResult error(String partitionName, String errorMessage) {
        return ArchiveResult.builder()
            .partitionName(partitionName)
            .success(false)
            .errorMessage(errorMessage)
            .timestamp(Instant.now())
            .build();
    }
    
    /**
     * Calcula taxa de compressão.
     */
    public double getCompressionRatio() {
        if (eventCount == null || compressedSize == null || eventCount == 0) {
            return 0.0;
        }
        
        // Estimativa: ~500 bytes por evento não comprimido
        long estimatedOriginalSize = eventCount * 500;
        return 1.0 - ((double) compressedSize / estimatedOriginalSize);
    }
}