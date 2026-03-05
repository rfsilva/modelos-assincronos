package com.seguradora.hibrida.eventbus;

import com.seguradora.hibrida.eventstore.model.DomainEvent;

/**
 * Interface base para handlers de eventos de domínio.
 * 
 * <p>Implementações desta interface são responsáveis por processar
 * eventos específicos de domínio. O Event Bus automaticamente
 * descobre e registra handlers anotados com @Component.
 * 
 * <p>Exemplo de implementação:
 * <pre>{@code
 * @Component
 * public class ContratoEventHandler implements EventHandler<ContratoCriadoEvent> {
 *     
 *     @Override
 *     public void handle(ContratoCriadoEvent event) {
 *         // Processar evento
 *         log.info("Contrato criado: {}", event.getContratoId());
 *         // Atualizar projeções, enviar notificações, etc.
 *     }
 *     
 *     @Override
 *     public Class<ContratoCriadoEvent> getEventType() {
 *         return ContratoCriadoEvent.class;
 *     }
 *     
 *     @Override
 *     public boolean isRetryable() {
 *         return true; // Permite retry em caso de falha
 *     }
 * }
 * }</pre>
 * 
 * @param <T> Tipo do evento de domínio que este handler processa
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface EventHandler<T extends DomainEvent> {
    
    /**
     * Processa um evento de domínio.
     * 
     * <p>Este método deve ser idempotente, pois pode ser chamado
     * múltiplas vezes em caso de retry.
     * 
     * @param event Evento a ser processado
     * @throws EventHandlingException em caso de erro no processamento
     */
    void handle(T event);
    
    /**
     * Retorna o tipo de evento que este handler processa.
     * 
     * <p>Usado pelo Event Bus para roteamento automático.
     * 
     * @return Classe do tipo de evento
     */
    Class<T> getEventType();
    
    /**
     * Indica se este handler suporta retry em caso de falha.
     * 
     * <p>Handlers que retornam true terão suas falhas reprocessadas
     * de acordo com a política de retry configurada.
     * 
     * @return true se suporta retry, false caso contrário (padrão: true)
     */
    default boolean isRetryable() {
        return true;
    }
    
    /**
     * Retorna a prioridade deste handler.
     * 
     * <p>Handlers com prioridade maior são executados primeiro.
     * Útil quando há dependências entre handlers.
     * 
     * @return Prioridade (padrão: 0)
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Indica se este handler deve ser executado de forma assíncrona.
     * 
     * <p>Handlers assíncronos são executados em thread separada,
     * permitindo maior throughput mas perdendo garantias de ordem.
     * 
     * @return true se deve ser assíncrono, false caso contrário (padrão: true)
     */
    default boolean isAsync() {
        return true;
    }
    
    /**
     * Retorna o timeout em segundos para execução deste handler.
     * 
     * <p>Se o handler não completar dentro do timeout, será cancelado
     * e tratado como falha.
     * 
     * @return Timeout em segundos (padrão: 30)
     */
    default int getTimeoutSeconds() {
        return 30;
    }
    
    /**
     * Verifica se este handler suporta o processamento do evento.
     * 
     * <p>Permite validações adicionais além do tipo do evento.
     * 
     * @param event Evento a ser verificado
     * @return true se suporta, false caso contrário (padrão: true)
     */
    default boolean supports(T event) {
        return true;
    }
}