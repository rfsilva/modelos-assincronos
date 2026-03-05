package com.seguradora.hibrida.snapshot.exception;

/**
 * Exceção específica para erros de compressão/descompressão de snapshots.
 * 
 * <p>Lançada quando ocorrem problemas durante:
 * <ul>
 *   <li>Compressão de dados do snapshot</li>
 *   <li>Descompressão de dados do snapshot</li>
 *   <li>Validação de integridade dos dados comprimidos</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class SnapshotCompressionException extends SnapshotException {
    
    /**
     * Algoritmo de compressão que falhou.
     */
    private final String compressionAlgorithm;
    
    /**
     * Tamanho original dos dados.
     */
    private final int originalSize;
    
    /**
     * Construtor básico.
     * 
     * @param message Mensagem de erro
     * @param compressionAlgorithm Algoritmo que falhou
     */
    public SnapshotCompressionException(String message, String compressionAlgorithm) {
        super(message);
        this.compressionAlgorithm = compressionAlgorithm;
        this.originalSize = -1;
    }
    
    /**
     * Construtor com causa.
     * 
     * @param message Mensagem de erro
     * @param cause Causa raiz
     * @param compressionAlgorithm Algoritmo que falhou
     */
    public SnapshotCompressionException(String message, Throwable cause, String compressionAlgorithm) {
        super(message, cause);
        this.compressionAlgorithm = compressionAlgorithm;
        this.originalSize = -1;
    }
    
    /**
     * Construtor com contexto completo.
     * 
     * @param message Mensagem de erro
     * @param cause Causa raiz
     * @param compressionAlgorithm Algoritmo que falhou
     * @param aggregateId ID do aggregate
     * @param originalSize Tamanho original dos dados
     */
    public SnapshotCompressionException(String message, Throwable cause, 
                                      String compressionAlgorithm, String aggregateId, int originalSize) {
        super(message, cause, aggregateId, null);
        this.compressionAlgorithm = compressionAlgorithm;
        this.originalSize = originalSize;
    }
    
    /**
     * Obtém o algoritmo de compressão que falhou.
     * 
     * @return Nome do algoritmo
     */
    public String getCompressionAlgorithm() {
        return compressionAlgorithm;
    }
    
    /**
     * Obtém o tamanho original dos dados.
     * 
     * @return Tamanho em bytes ou -1 se não disponível
     */
    public int getOriginalSize() {
        return originalSize;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        
        if (compressionAlgorithm != null) {
            sb.append(" [algorithm=").append(compressionAlgorithm);
            if (originalSize > 0) {
                sb.append(", originalSize=").append(originalSize).append(" bytes");
            }
            sb.append("]");
        }
        
        return sb.toString();
    }
}