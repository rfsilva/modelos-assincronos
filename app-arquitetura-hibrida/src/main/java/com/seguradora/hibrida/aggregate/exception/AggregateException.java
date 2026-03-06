package com.seguradora.hibrida.aggregate.exception;

/**
 * Exceção base para erros relacionados a operações de Aggregate.
 * 
 * <p>Esta exceção é lançada quando ocorrem erros durante:
 * <ul>
 *   <li>Aplicação de eventos ao aggregate</li>
 *   <li>Reconstrução de estado a partir do histórico</li>
 *   <li>Carregamento de snapshots</li>
 *   <li>Validação de handlers de eventos</li>
 *   <li>Operações de reflection para descoberta de handlers</li>
 * </ul>
 * 
 * <p><strong>Hierarquia de Exceções:</strong>
 * <pre>
 * AggregateException
 * ├── BusinessRuleViolationException
 * ├── EventHandlerNotFoundException
 * ├── SnapshotException
 * └── ConcurrencyException
 * </pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class AggregateException extends RuntimeException {
    
    /**
     * ID do aggregate onde ocorreu o erro (opcional).
     */
    private final String aggregateId;
    
    /**
     * Tipo do aggregate onde ocorreu o erro (opcional).
     */
    private final String aggregateType;
    
    /**
     * Versão do aggregate quando ocorreu o erro (opcional).
     */
    private final Long version;
    
    /**
     * Construtor básico com mensagem.
     * 
     * @param message Mensagem de erro
     */
    public AggregateException(String message) {
        super(message);
        this.aggregateId = null;
        this.aggregateType = null;
        this.version = null;
    }
    
    /**
     * Construtor com mensagem e causa.
     * 
     * @param message Mensagem de erro
     * @param cause Causa raiz do erro
     */
    public AggregateException(String message, Throwable cause) {
        super(message, cause);
        this.aggregateId = null;
        this.aggregateType = null;
        this.version = null;
    }
    
    /**
     * Construtor com contexto do aggregate.
     * 
     * @param message Mensagem de erro
     * @param aggregateId ID do aggregate
     * @param aggregateType Tipo do aggregate
     * @param version Versão do aggregate
     */
    public AggregateException(String message, String aggregateId, String aggregateType, Long version) {
        super(message);
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = version;
    }
    
    /**
     * Construtor completo com contexto e causa.
     * 
     * @param message Mensagem de erro
     * @param cause Causa raiz do erro
     * @param aggregateId ID do aggregate
     * @param aggregateType Tipo do aggregate
     * @param version Versão do aggregate
     */
    public AggregateException(String message, Throwable cause, String aggregateId, String aggregateType, Long version) {
        super(message, cause);
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = version;
    }
    
    /**
     * Retorna o ID do aggregate onde ocorreu o erro.
     * 
     * @return ID do aggregate ou null se não disponível
     */
    public String getAggregateId() {
        return aggregateId;
    }
    
    /**
     * Retorna o tipo do aggregate onde ocorreu o erro.
     * 
     * @return Tipo do aggregate ou null se não disponível
     */
    public String getAggregateType() {
        return aggregateType;
    }
    
    /**
     * Retorna a versão do aggregate quando ocorreu o erro.
     * 
     * @return Versão do aggregate ou null se não disponível
     */
    public Long getVersion() {
        return version;
    }
    
    /**
     * Verifica se a exceção tem contexto de aggregate.
     * 
     * @return true se tem informações do aggregate
     */
    public boolean hasAggregateContext() {
        return aggregateId != null || aggregateType != null;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        
        if (hasAggregateContext()) {
            sb.append(" [");
            if (aggregateType != null) {
                sb.append("type=").append(aggregateType);
            }
            if (aggregateId != null) {
                if (aggregateType != null) sb.append(", ");
                sb.append("id=").append(aggregateId);
            }
            if (version != null) {
                sb.append(", version=").append(version);
            }
            sb.append("]");
        }
        
        return sb.toString();
    }
}