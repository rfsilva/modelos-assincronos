package com.seguradora.hibrida.aggregate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para marcar métodos que processam eventos de Event Sourcing.
 * 
 * <p>Métodos anotados com @EventSourcingHandler são automaticamente
 * invocados quando eventos correspondentes são aplicados ao aggregate.
 * 
 * <p><strong>Regras de Uso:</strong>
 * <ul>
 *   <li>Método deve ter exatamente um parâmetro do tipo DomainEvent</li>
 *   <li>Método deve ser protected ou package-private</li>
 *   <li>Método não deve retornar valor (void)</li>
 *   <li>Método deve ser idempotente</li>
 *   <li>Método não deve lançar exceções</li>
 * </ul>
 * 
 * <p><strong>Exemplo:</strong>
 * <pre>{@code
 * @EventSourcingHandler
 * protected void on(SeguradoCriadoEvent event) {
 *     this.nome = event.getNome();
 *     this.cpf = event.getCpf();
 *     this.status = StatusSegurado.ATIVO;
 * }
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventSourcingHandler {
    
    /**
     * Nome opcional do handler para debugging.
     * 
     * @return Nome do handler
     */
    String value() default "";
    
    /**
     * Indica se o handler deve ser executado durante replay.
     * 
     * <p>Alguns handlers podem ser específicos para operações em tempo real
     * e não devem ser executados durante reconstrução do histórico.
     * 
     * @return true se deve executar durante replay (padrão: true)
     */
    boolean replayable() default true;
    
    /**
     * Ordem de execução quando múltiplos handlers processam o mesmo evento.
     * 
     * <p>Handlers com ordem menor são executados primeiro.
     * 
     * @return Ordem de execução (padrão: 0)
     */
    int order() default 0;
}