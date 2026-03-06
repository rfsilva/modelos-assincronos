package com.seguradora.hibrida.eventstore.partition;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Estatísticas de uma partição do Event Store.
 * 
 * <p>Contém informações sobre:
 * <ul>
 *   <li>Nome da partição</li>
 *   <li>Número de registros</li>
 *   <li>Tamanho em bytes e formato legível</li>
 *   <li>Período de dados (min/max timestamp)</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
public class PartitionStatistics {
    
    /**
     * Nome da partição (ex: events_2024_01).
     */
    private String partitionName;
    
    /**
     * Número total de registros na partição.
     */
    private Long rowCount;
    
    /**
     * Tamanho da partição em bytes.
     */
    private Long sizeBytes;
    
    /**
     * Tamanho da partição em formato legível (ex: 1.2 MB).
     */
    private String sizePretty;
    
    /**
     * Timestamp mínimo dos dados na partição.
     */
    private Instant minTimestamp;
    
    /**
     * Timestamp máximo dos dados na partição.
     */
    private Instant maxTimestamp;
    
    /**
     * Calcula a densidade de dados (registros por MB).
     * 
     * @return Densidade ou 0 se não houver dados
     */
    public double getDataDensity() {
        if (sizeBytes == null || sizeBytes == 0 || rowCount == null || rowCount == 0) {
            return 0.0;
        }
        
        double sizeMB = sizeBytes / (1024.0 * 1024.0);
        return rowCount / sizeMB;
    }
    
    /**
     * Verifica se a partição está vazia.
     * 
     * @return true se não há registros
     */
    public boolean isEmpty() {
        return rowCount == null || rowCount == 0;
    }
    
    /**
     * Verifica se a partição é grande (> 100MB).
     * 
     * @return true se a partição é considerada grande
     */
    public boolean isLarge() {
        return sizeBytes != null && sizeBytes > 100 * 1024 * 1024; // 100MB
    }
    
    /**
     * Extrai o ano/mês da partição do nome.
     * 
     * @return String no formato "YYYY-MM" ou null se inválido
     */
    public String getYearMonth() {
        if (partitionName == null || !partitionName.contains("_")) {
            return null;
        }
        
        String[] parts = partitionName.split("_");
        if (parts.length >= 3) {
            return parts[1] + "-" + parts[2];
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("Partition[%s: %d rows, %s]", 
            partitionName, rowCount, sizePretty);
    }
}