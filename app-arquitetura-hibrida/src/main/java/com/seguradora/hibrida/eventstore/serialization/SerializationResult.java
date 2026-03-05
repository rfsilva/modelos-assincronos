package com.seguradora.hibrida.eventstore.serialization;

import lombok.Builder;
import lombok.Data;

/**
 * Resultado da serialização de um evento.
 * 
 * Contém os dados serializados e metadados sobre o processo
 * de serialização, incluindo informações de compressão.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
public class SerializationResult {
    
    /**
     * Dados serializados do evento.
     */
    private final String data;
    
    /**
     * Indica se os dados foram comprimidos.
     */
    private final boolean compressed;
    
    /**
     * Tamanho original dos dados (antes da compressão).
     */
    private final int originalSize;
    
    /**
     * Tamanho final dos dados (após compressão, se aplicável).
     */
    private final int finalSize;
    
    /**
     * Algoritmo de compressão utilizado (se aplicável).
     */
    private final String compressionAlgorithm;
    
    /**
     * Calcula a taxa de compressão.
     * 
     * @return Taxa de compressão (0.0 a 1.0)
     */
    public double getCompressionRatio() {
        if (originalSize == 0) {
            return 0.0;
        }
        return 1.0 - ((double) finalSize / originalSize);
    }
    
    /**
     * Calcula a economia de espaço em bytes.
     * 
     * @return Bytes economizados
     */
    public int getSpaceSaved() {
        return originalSize - finalSize;
    }
    
    /**
     * Verifica se a compressão foi efetiva.
     * 
     * @return true se houve economia de espaço
     */
    public boolean isCompressionEffective() {
        return compressed && finalSize < originalSize;
    }
}