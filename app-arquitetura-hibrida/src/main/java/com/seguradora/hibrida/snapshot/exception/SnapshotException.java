package com.seguradora.hibrida.snapshot.exception;

/**
 * Exceção base para operações de snapshot.
 * 
 * <p>Representa erros que podem ocorrer durante:
 * <ul>
 *   <li>Criação de snapshots</li>
 *   <li>Recuperação de snapshots</li>
 *   <li>Compressão/descompressão</li>
 *   <li>Limpeza de snapshots antigos</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class SnapshotException extends RuntimeException {
    
    /**
     * ID do aggregate relacionado ao erro (opcional).
     */
    private final String aggregateId;
    
    /**
     * Versão do aggregate relacionada ao erro (opcional).
     */
    private final Long version;
    
    /**
     * Construtor básico.
     * 
     * @param message Mensagem de erro
     */
    public SnapshotException(String message) {
        super(message);
        this.aggregateId = null;
        this.version = null;
    }
    
    /**
     * Construtor com causa.
     * 
     * @param message Mensagem de erro
     * @param cause Causa raiz
     */
    public SnapshotException(String message, Throwable cause) {
        super(message, cause);
        this.aggregateId = null;
        this.version = null;
    }
    
    /**
     * Construtor com contexto de aggregate.
     * 
     * @param message Mensagem de erro
     * @param aggregateId ID do aggregate
     */
    public SnapshotException(String message, String aggregateId) {
        super(message);
        this.aggregateId = aggregateId;
        this.version = null;
    }
    
    /**
     * Construtor com contexto completo.
     * 
     * @param message Mensagem de erro
     * @param aggregateId ID do aggregate
     * @param version Versão do aggregate
     */
    public SnapshotException(String message, String aggregateId, Long version) {
        super(message);
        this.aggregateId = aggregateId;
        this.version = version;
    }
    
    /**
     * Construtor completo com causa.
     * 
     * @param message Mensagem de erro
     * @param cause Causa raiz
     * @param aggregateId ID do aggregate
     * @param version Versão do aggregate
     */
    public SnapshotException(String message, Throwable cause, String aggregateId, Long version) {
        super(message, cause);
        this.aggregateId = aggregateId;
        this.version = version;
    }
    
    /**
     * Obtém o ID do aggregate relacionado ao erro.
     * 
     * @return ID do aggregate ou null se não aplicável
     */
    public String getAggregateId() {
        return aggregateId;
    }
    
    /**
     * Obtém a versão do aggregate relacionada ao erro.
     * 
     * @return Versão do aggregate ou null se não aplicável
     */
    public Long getVersion() {
        return version;
    }
    
    /**
     * Verifica se o erro está relacionado a um aggregate específico.
     * 
     * @return true se tem contexto de aggregate, false caso contrário
     */
    public boolean hasAggregateContext() {
        return aggregateId != null;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        
        if (aggregateId != null) {
            sb.append(" [aggregateId=").append(aggregateId);
            if (version != null) {
                sb.append(", version=").append(version);
            }
            sb.append("]");
        }
        
        return sb.toString();
    }
}