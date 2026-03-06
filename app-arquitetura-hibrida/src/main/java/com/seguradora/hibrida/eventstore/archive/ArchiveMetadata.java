package com.seguradora.hibrida.eventstore.archive;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Metadados de um arquivo de eventos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
public class ArchiveMetadata {
    
    private String partitionName;
    private String archiveKey;
    private Long eventCount;
    private Long compressedSize;
    private Instant archivedAt;
    private String status;
    
    /**
     * Verifica se o arquivo está ativo.
     */
    public boolean isActive() {
        return "ARCHIVED".equals(status);
    }
    
    /**
     * Verifica se foi restaurado.
     */
    public boolean isRestored() {
        return "RESTORED".equals(status);
    }
}