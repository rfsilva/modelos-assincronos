package com.seguradora.hibrida.eventstore.archive;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Estatísticas gerais de arquivamento.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
public class ArchiveStatistics {
    
    private Long totalArchives;
    private Long totalEvents;
    private Long totalSize;
    private Instant oldestArchive;
    private Instant newestArchive;
    
    /**
     * Tamanho médio por arquivo.
     */
    public double getAverageArchiveSize() {
        if (totalArchives == null || totalArchives == 0) {
            return 0.0;
        }
        return (double) (totalSize != null ? totalSize : 0) / totalArchives;
    }
    
    /**
     * Eventos médios por arquivo.
     */
    public double getAverageEventsPerArchive() {
        if (totalArchives == null || totalArchives == 0) {
            return 0.0;
        }
        return (double) (totalEvents != null ? totalEvents : 0) / totalArchives;
    }
    
    /**
     * Tamanho formatado.
     */
    public String getFormattedSize() {
        if (totalSize == null || totalSize == 0) {
            return "0 B";
        }
        
        long size = totalSize;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", (double) size, units[unitIndex]);
    }
}