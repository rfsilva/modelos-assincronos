package com.seguradora.hibrida.snapshot.serialization;

import com.seguradora.hibrida.snapshot.exception.SnapshotException;

/**
 * Exceção específica para erros de serialização/deserialização de snapshots.
 * 
 * <p>Lançada quando ocorrem problemas durante:
 * <ul>
 *   <li>Serialização de snapshots para JSON</li>
 *   <li>Deserialização de JSON para snapshots</li>
 *   <li>Validação de integridade de dados</li>
 *   <li>Processamento de metadados</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class SnapshotSerializationException extends SnapshotException {
    
    /**
     * Tipo de operação que falhou.
     */
    private final String operation;
    
    /**
     * Tipo do aggregate relacionado.
     */
    private final String aggregateType;
    
    /**
     * Construtor básico.
     * 
     * @param message Mensagem de erro
     * @param operation Operação que falhou (serialize/deserialize)
     */
    public SnapshotSerializationException(String message, String operation) {
        super(message);
        this.operation = operation;
        this.aggregateType = null;
    }
    
    /**
     * Construtor com causa.
     * 
     * @param message Mensagem de erro
     * @param cause Causa raiz
     * @param operation Operação que falhou
     */
    public SnapshotSerializationException(String message, Throwable cause, String operation) {
        super(message, cause);
        this.operation = operation;
        this.aggregateType = null;
    }
    
    /**
     * Construtor com contexto completo.
     * 
     * @param message Mensagem de erro
     * @param cause Causa raiz
     * @param operation Operação que falhou
     * @param aggregateId ID do aggregate
     * @param aggregateType Tipo do aggregate
     */
    public SnapshotSerializationException(String message, Throwable cause, String operation,
                                        String aggregateId, String aggregateType) {
        super(message, cause, aggregateId, null);
        this.operation = operation;
        this.aggregateType = aggregateType;
    }
    
    /**
     * Obtém a operação que falhou.
     * 
     * @return Nome da operação
     */
    public String getOperation() {
        return operation;
    }
    
    /**
     * Obtém o tipo do aggregate relacionado.
     * 
     * @return Tipo do aggregate ou null se não aplicável
     */
    public String getAggregateType() {
        return aggregateType;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        
        if (operation != null) {
            sb.append(" [operation=").append(operation);
            if (aggregateType != null) {
                sb.append(", aggregateType=").append(aggregateType);
            }
            sb.append("]");
        }
        
        return sb.toString();
    }
}