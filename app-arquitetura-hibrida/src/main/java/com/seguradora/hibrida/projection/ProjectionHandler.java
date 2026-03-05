package com.seguradora.hibrida.projection;

import com.seguradora.hibrida.eventstore.model.DomainEvent;

/**
 * Interface base para handlers de projeção no CQRS.
 * 
 * <p>Projection Handlers são responsáveis por processar eventos de domínio
 * e atualizar os modelos de leitura (Query Models) correspondentes.
 * 
 * <p>Características principais:
 * <ul>
 *   <li>Processamento assíncrono de eventos</li>
 *   <li>Idempotência garantida</li>
 *   <li>Ordenação por aggregate</li>
 *   <li>Recovery automático</li>
 * </ul>
 * 
 * @param <T> Tipo do evento de domínio processado
 */
public interface ProjectionHandler<T extends DomainEvent> {
    
    /**
     * Processa um evento de domínio e atualiza a projeção correspondente.
     * 
     * <p>Este método deve ser idempotente, pois pode ser chamado
     * múltiplas vezes para o mesmo evento em caso de retry.
     * 
     * @param event Evento de domínio a ser processado
     * @throws ProjectionException em caso de erro no processamento
     */
    void handle(T event);
    
    /**
     * Retorna o tipo de evento que este handler processa.
     * 
     * <p>Usado pelo sistema de roteamento para direcionar
     * eventos para os handlers apropriados.
     * 
     * @return Classe do tipo de evento
     */
    Class<T> getEventType();
    
    /**
     * Nome único da projeção.
     * 
     * <p>Usado para tracking de posição e identificação
     * nos logs e métricas.
     * 
     * @return Nome da projeção
     */
    String getProjectionName();
    
    /**
     * Verifica se este handler suporta o processamento do evento.
     * 
     * <p>Permite validações adicionais além do tipo do evento.
     * Por exemplo, verificar versão do evento ou contexto específico.
     * 
     * @param event Evento a ser verificado
     * @return true se suporta, false caso contrário
     */
    default boolean supports(T event) {
        return true;
    }
    
    /**
     * Ordem de processamento quando múltiplos handlers
     * processam o mesmo tipo de evento.
     * 
     * <p>Handlers com ordem menor são executados primeiro.
     * Útil quando há dependências entre projeções.
     * 
     * @return Ordem de processamento (padrão: 100)
     */
    default int getOrder() {
        return 100;
    }
    
    /**
     * Indica se este handler deve ser processado de forma assíncrona.
     * 
     * <p>Handlers síncronos bloqueiam o processamento até completar.
     * Handlers assíncronos permitem maior throughput.
     * 
     * @return true se deve ser assíncrono, false caso contrário (padrão: true)
     */
    default boolean isAsync() {
        return true;
    }
    
    /**
     * Timeout em segundos para processamento deste handler.
     * 
     * <p>Se o handler não completar dentro do timeout,
     * será cancelado e tratado como falha.
     * 
     * @return Timeout em segundos (padrão: 30)
     */
    default int getTimeoutSeconds() {
        return 30;
    }
    
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
     * Número máximo de tentativas de retry para este handler.
     * 
     * <p>Usado apenas se isRetryable() retornar true.
     * 
     * @return Número máximo de tentativas (padrão: 3)
     */
    default int getMaxRetries() {
        return 3;
    }
}