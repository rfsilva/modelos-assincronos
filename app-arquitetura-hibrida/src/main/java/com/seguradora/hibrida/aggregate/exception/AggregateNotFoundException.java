package com.seguradora.hibrida.aggregate.exception;

/**
 * Exceção lançada quando um aggregate não é encontrado no repositório.
 * 
 * <p>Esta exceção é tipicamente lançada quando:
 * <ul>
 *   <li>Busca-se um aggregate por ID que não existe</li>
 *   <li>Tenta-se carregar um aggregate em uma versão inexistente</li>
 *   <li>Referencia-se um aggregate que foi removido</li>
 * </ul>
 * 
 * <p><strong>Exemplo de uso:</strong>
 * <pre>{@code
 * public SeguradoAggregate buscarSegurado(String id) {
 *     return seguradoRepository.findById(id)
 *         .orElseThrow(() -> new AggregateNotFoundException(id, "SeguradoAggregate"));
 * }
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class AggregateNotFoundException extends AggregateException {
    
    /**
     * Construtor com ID e tipo do aggregate.
     * 
     * @param aggregateId ID do aggregate não encontrado
     * @param aggregateType Tipo do aggregate
     */
    public AggregateNotFoundException(String aggregateId, String aggregateType) {
        super(String.format("Aggregate %s com ID '%s' não foi encontrado", 
                aggregateType, aggregateId), 
                aggregateId, aggregateType, null);
    }
    
    /**
     * Construtor com ID e classe do aggregate.
     * 
     * @param aggregateId ID do aggregate não encontrado
     * @param aggregateClass Classe do aggregate
     */
    public AggregateNotFoundException(String aggregateId, Class<?> aggregateClass) {
        this(aggregateId, aggregateClass.getSimpleName());
    }
    
    /**
     * Construtor com ID, tipo e versão específica.
     * 
     * @param aggregateId ID do aggregate não encontrado
     * @param aggregateType Tipo do aggregate
     * @param version Versão específica não encontrada
     */
    public AggregateNotFoundException(String aggregateId, String aggregateType, Long version) {
        super(String.format("Aggregate %s com ID '%s' na versão %d não foi encontrado", 
                aggregateType, aggregateId, version), 
                aggregateId, aggregateType, version);
    }
    
    /**
     * Construtor com mensagem customizada.
     * 
     * @param message Mensagem de erro customizada
     * @param aggregateId ID do aggregate não encontrado
     * @param aggregateType Tipo do aggregate
     */
    public AggregateNotFoundException(String message, String aggregateId, String aggregateType) {
        super(message, aggregateId, aggregateType, null);
    }
}