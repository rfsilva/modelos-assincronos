package com.seguradora.hibrida.aggregate.validation;

import com.seguradora.hibrida.aggregate.AggregateRoot;

/**
 * Interface para definição de regras de negócio (invariantes) em Aggregates.
 * 
 * <p>Regras de negócio são validações que devem sempre ser verdadeiras
 * para manter a consistência e integridade do domínio. São executadas
 * automaticamente após a aplicação de eventos ao aggregate.
 * 
 * <p><strong>Características das regras de negócio:</strong>
 * <ul>
 *   <li>Devem ser idempotentes (mesmo resultado sempre)</li>
 *   <li>Não devem ter efeitos colaterais</li>
 *   <li>Devem ser rápidas de executar</li>
 *   <li>Devem ter mensagens de erro claras</li>
 * </ul>
 * 
 * <p><strong>Exemplo de implementação:</strong>
 * <pre>{@code
 * public class CpfValidoRule implements BusinessRule {
 *     
 *     @Override
 *     public boolean isValid(AggregateRoot aggregate) {
 *         if (!(aggregate instanceof SeguradoAggregate)) {
 *             return true; // Regra não se aplica
 *         }
 *         
 *         SeguradoAggregate segurado = (SeguradoAggregate) aggregate;
 *         return CpfValidator.isValid(segurado.getCpf());
 *     }
 *     
 *     @Override
 *     public String getErrorMessage() {
 *         return "CPF deve ser válido";
 *     }
 * }
 * }</pre>
 * 
 * <p><strong>Uso no aggregate:</strong>
 * <pre>{@code
 * public class SeguradoAggregate extends AggregateRoot {
 *     
 *     public SeguradoAggregate() {
 *         super();
 *         registerBusinessRule(new CpfValidoRule());
 *         registerBusinessRule(new IdadeMinimaRule(18));
 *     }
 * }
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface BusinessRule {
    
    /**
     * Valida se a regra de negócio é atendida pelo aggregate.
     * 
     * <p>Este método deve:
     * <ul>
     *   <li>Ser idempotente (mesmo resultado sempre)</li>
     *   <li>Não modificar o estado do aggregate</li>
     *   <li>Ser rápido de executar</li>
     *   <li>Não lançar exceções (retornar false em caso de erro)</li>
     * </ul>
     * 
     * @param aggregate Aggregate a ser validado
     * @return true se a regra é atendida, false caso contrário
     */
    boolean isValid(AggregateRoot aggregate);
    
    /**
     * Retorna mensagem de erro clara quando a regra é violada.
     * 
     * <p>A mensagem deve:
     * <ul>
     *   <li>Ser clara e específica</li>
     *   <li>Indicar o que está errado</li>
     *   <li>Sugerir como corrigir (quando possível)</li>
     *   <li>Ser adequada para exibição ao usuário</li>
     * </ul>
     * 
     * @return Mensagem de erro para violação da regra
     */
    String getErrorMessage();
    
    /**
     * Retorna nome identificador da regra para logs e debugging.
     * 
     * <p>Implementação padrão retorna o nome simples da classe.
     * Pode ser sobrescrita para nomes mais descritivos.
     * 
     * @return Nome da regra
     */
    default String getRuleName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Retorna prioridade da regra para ordenação de validação.
     * 
     * <p>Regras com prioridade maior são executadas primeiro.
     * Útil quando há dependências entre regras.
     * 
     * @return Prioridade da regra (padrão: 0)
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Indica se a regra deve ser executada durante replay de eventos.
     * 
     * <p>Algumas regras podem ser específicas para operações em tempo real
     * e não devem ser validadas durante reconstrução do histórico.
     * 
     * @return true se deve validar durante replay (padrão: true)
     */
    default boolean validateOnReplay() {
        return true;
    }
    
    /**
     * Indica se a regra se aplica ao tipo específico de aggregate.
     * 
     * <p>Permite otimização evitando validações desnecessárias.
     * Implementação padrão retorna true (aplica a todos).
     * 
     * @param aggregateType Tipo do aggregate
     * @return true se a regra se aplica ao tipo
     */
    default boolean appliesTo(Class<? extends AggregateRoot> aggregateType) {
        return true;
    }
}