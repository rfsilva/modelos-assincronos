package com.seguradora.hibrida.aggregate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para marcar métodos que processam eventos de domínio em Aggregates.
 * 
 * <p>Métodos anotados com @EventSourcingHandler são automaticamente
 * descobertos e invocados quando eventos correspondentes são aplicados
 * ao aggregate durante reconstrução do estado ou aplicação de novos eventos.
 * 
 * <p><strong>Regras para métodos handler:</strong>
 * <ul>
 *   <li>Deve ter exatamente um parâmetro do tipo DomainEvent</li>
 *   <li>Deve ser protected ou private (não public)</li>
 *   <li>Não deve retornar valor (void)</li>
 *   <li>Deve ser idempotente (pode ser chamado múltiplas vezes)</li>
 *   <li>Não deve lançar exceções de negócio</li>
 * </ul>
 * 
 * <p><strong>Exemplo de uso:</strong>
 * <pre>{@code
 * public class SeguradoAggregate extends AggregateRoot {
 *     
 *     private String nome;
 *     private String cpf;
 *     private StatusSegurado status;
 *     
 *     @EventSourcingHandler
 *     protected void on(SeguradoCriadoEvent event) {
 *         this.nome = event.getNome();
 *         this.cpf = event.getCpf();
 *         this.status = StatusSegurado.ATIVO;
 *     }
 *     
 *     @EventSourcingHandler
 *     protected void on(SeguradoAtualizadoEvent event) {
 *         this.nome = event.getNovoNome();
 *         // Não atualizar CPF - imutável
 *     }
 *     
 *     @EventSourcingHandler
 *     protected void on(SeguradoDesativadoEvent event) {
 *         this.status = StatusSegurado.INATIVO;
 *     }
 * }
 * }</pre>
 * 
 * <p><strong>Performance:</strong>
 * Os métodos handler são descobertos via reflection na primeira utilização
 * e cached para otimizar performance em aplicações subsequentes.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventSourcingHandler {
    
    /**
     * Nome opcional do handler para debugging e logs.
     * 
     * <p>Se não especificado, será usado o nome do método.
     * 
     * @return Nome do handler
     */
    String value() default "";
    
    /**
     * Prioridade do handler quando múltiplos handlers existem para o mesmo evento.
     * 
     * <p>Handlers com prioridade maior são executados primeiro.
     * Valor padrão é 0.
     * 
     * @return Prioridade do handler
     */
    int priority() default 0;
    
    /**
     * Indica se o handler deve ser executado durante replay de eventos.
     * 
     * <p>Alguns handlers podem ser específicos para processamento em tempo real
     * e não devem ser executados durante replay histórico.
     * 
     * @return true se deve executar durante replay (padrão: true)
     */
    boolean executeOnReplay() default true;
}