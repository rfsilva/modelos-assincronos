package com.seguradora.hibrida.snapshot.serialization;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Resultado da serialização de snapshot com informações de compressão.
 * 
 * <p>Contém:
 * <ul>
 *   <li>Dados serializados (possivelmente comprimidos)</li>
 *   <li>Informações sobre compressão aplicada</li>
 *   <li>Métricas de eficiência</li>
 *   <li>Hash para verificação de integridade</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
public class SnapshotSerializationResult {
    
    /**
     * Dados serializados (JSON).
     */
    private final String serializedData;
    
    /**
     * Indica se os dados foram comprimidos.
     */
    private final boolean compressed;
    
    /**
     * Algoritmo de compressão utilizado.
     */
    private final String compressionAlgorithm;
    
    /**
     * Tamanho original dos dados (bytes).
     */
    private final int originalSize;
    
    /**
     * Tamanho após compressão (bytes).
     */
    private final int compressedSize;
    
    /**
     * Hash SHA-256 dos dados para verificação de integridade.
     */
    private final String dataHash;
    
    /**
     * Tempo gasto na serialização (milissegundos).
     */
    private final long serializationTimeMs;
    
    /**
     * Tempo gasto na compressão (milissegundos).
     */
    private final long compressionTimeMs;
    
    /**
     * Calcula a taxa de compressão.
     * 
     * @return Taxa de compressão (0.0 a 1.0) ou 0.0 se não comprimido
     */
    public double getCompressionRatio() {
        if (!compressed || originalSize == 0) {
            return 0.0;
        }
        return 1.0 - ((double) compressedSize / originalSize);
    }
    
    /**
     * Calcula o espaço economizado pela compressão.
     * 
     * @return Bytes economizados ou 0 se não comprimido
     */
    public int getSpaceSaved() {
        if (!compressed) {
            return 0;
        }
        return originalSize - compressedSize;
    }
    
    /**
     * Verifica se a compressão foi efetiva (economizou pelo menos 10%).
     * 
     * @return true se compressão foi efetiva, false caso contrário
     */
    public boolean isCompressionEffective() {
        return compressed && getCompressionRatio() >= 0.1;
    }
    
    /**
     * Obtém o tamanho efetivo dos dados.
     * 
     * @return Tamanho comprimido se aplicável, senão tamanho original
     */
    public int getEffectiveSize() {
        return compressed ? compressedSize : originalSize;
    }
    
    /**
     * Calcula o tempo total de processamento.
     * 
     * @return Tempo total em milissegundos
     */
    public long getTotalProcessingTime() {
        return serializationTimeMs + compressionTimeMs;
    }
    
    /**
     * Calcula a taxa de compressão por segundo.
     * 
     * @return Bytes por segundo ou 0 se não comprimido
     */
    public double getCompressionThroughput() {
        if (!compressed || compressionTimeMs == 0) {
            return 0.0;
        }
        return ((double) originalSize / compressionTimeMs) * 1000.0; // bytes/segundo
    }
    
    /**
     * Verifica se o resultado tem hash de integridade.
     * 
     * @return true se tem hash, false caso contrário
     */
    public boolean hasIntegrityHash() {
        return dataHash != null && !dataHash.trim().isEmpty();
    }
}