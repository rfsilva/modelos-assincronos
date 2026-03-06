package com.seguradora.hibrida.aggregate.exception;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exceção lançada quando regras de negócio (invariantes) são violadas em um Aggregate.
 * 
 * <p>Esta exceção é lançada durante a validação automática de invariantes
 * após a aplicação de eventos ao aggregate. Contém informações detalhadas
 * sobre todas as violações encontradas.
 * 
 * <p><strong>Exemplo de uso:</strong>
 * <pre>{@code
 * // Em um aggregate
 * protected void validateBusinessRules() {
 *     List<String> violations = new ArrayList<>();
 *     
 *     if (cpf == null || !CpfValidator.isValid(cpf)) {
 *         violations.add("CPF deve ser válido");
 *     }
 *     
 *     if (idade < 18) {
 *         violations.add("Segurado deve ser maior de idade");
 *     }
 *     
 *     if (!violations.isEmpty()) {
 *         throw new BusinessRuleViolationException(
 *             "Violações encontradas no segurado", violations);
 *     }
 * }
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class BusinessRuleViolationException extends AggregateException {
    
    /**
     * Lista de violações de regras de negócio.
     */
    private final List<String> violations;
    
    /**
     * Construtor com mensagem e lista de violações.
     * 
     * @param message Mensagem principal do erro
     * @param violations Lista de violações específicas
     */
    public BusinessRuleViolationException(String message, List<String> violations) {
        super(buildMessage(message, violations));
        this.violations = violations != null ? List.copyOf(violations) : Collections.emptyList();
    }
    
    /**
     * Construtor com contexto do aggregate.
     * 
     * @param message Mensagem principal do erro
     * @param violations Lista de violações específicas
     * @param aggregateId ID do aggregate
     * @param aggregateType Tipo do aggregate
     * @param version Versão do aggregate
     */
    public BusinessRuleViolationException(String message, List<String> violations, 
                                        String aggregateId, String aggregateType, Long version) {
        super(buildMessage(message, violations), aggregateId, aggregateType, version);
        this.violations = violations != null ? List.copyOf(violations) : Collections.emptyList();
    }
    
    /**
     * Construtor com violação única.
     * 
     * @param message Mensagem principal do erro
     * @param violation Violação específica
     */
    public BusinessRuleViolationException(String message, String violation) {
        this(message, violation != null ? List.of(violation) : Collections.emptyList());
    }
    
    /**
     * Retorna lista imutável de violações.
     * 
     * @return Lista de violações de regras de negócio
     */
    public List<String> getViolations() {
        return violations;
    }
    
    /**
     * Verifica se existem violações.
     * 
     * @return true se existem violações
     */
    public boolean hasViolations() {
        return !violations.isEmpty();
    }
    
    /**
     * Retorna o número de violações.
     * 
     * @return Número de violações
     */
    public int getViolationCount() {
        return violations.size();
    }
    
    /**
     * Retorna as violações como uma string formatada.
     * 
     * @return String com todas as violações
     */
    public String getViolationsAsString() {
        return violations.stream()
                .collect(Collectors.joining("; "));
    }
    
    /**
     * Constrói a mensagem completa incluindo as violações.
     * 
     * @param message Mensagem principal
     * @param violations Lista de violações
     * @return Mensagem completa formatada
     */
    private static String buildMessage(String message, List<String> violations) {
        if (violations == null || violations.isEmpty()) {
            return message;
        }
        
        StringBuilder sb = new StringBuilder(message);
        sb.append(". Violações: ");
        sb.append(violations.stream().collect(Collectors.joining("; ")));
        
        return sb.toString();
    }
}