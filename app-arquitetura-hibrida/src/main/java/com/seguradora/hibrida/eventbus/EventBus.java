package com.seguradora.hibrida.eventbus;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface principal do Event Bus para publicação de eventos de domínio.
 * 
 * <p>O Event Bus é responsável por:
 * <ul>
 *   <li>Publicar eventos de domínio de forma assíncrona</li>
 *   <li>Rotear eventos para handlers apropriados</li>
 *   <li>Garantir processamento ordenado por aggregate</li>
 *   <li>Fornecer mecanismos de retry e dead letter queue</li>
 * </ul>
 * 
 * <p>Exemplo de uso:
 * <pre>{@code
 * @Autowired
 * private EventBus eventBus;
 * 
 * // Publicação síncrona
 * eventBus.publish(new ContratoCriadoEvent(contratoId, dados));
 * 
 * // Publicação assíncrona
 * CompletableFuture<Void> future = eventBus.publishAsync(evento);
 * 
 * // Publicação em lote
 * eventBus.publishBatch(eventos);
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface EventBus {
    
    /**
     * Publica um evento de domínio de forma síncrona.
     * 
     * <p>O evento será processado imediatamente pelos handlers registrados.
     * Este método bloqueia até que todos os handlers tenham processado o evento.
     * 
     * @param event Evento de domínio a ser publicado
     * @throws EventBusException em caso de erro na publicação
     * @throws IllegalArgumentException se o evento for null
     */
    void publish(DomainEvent event);
    
    /**
     * Publica um evento de domínio de forma assíncrona.
     * 
     * <p>O evento será enfileirado para processamento assíncrono.
     * Este método retorna imediatamente um CompletableFuture.
     * 
     * @param event Evento de domínio a ser publicado
     * @return CompletableFuture que será completado quando o evento for processado
     * @throws EventBusException em caso de erro na publicação
     * @throws IllegalArgumentException se o evento for null
     */
    CompletableFuture<Void> publishAsync(DomainEvent event);
    
    /**
     * Publica uma lista de eventos em lote.
     * 
     * <p>Os eventos serão processados em ordem, mantendo a consistência
     * por aggregate ID. Eventos do mesmo aggregate são processados sequencialmente.
     * 
     * @param events Lista de eventos a serem publicados
     * @throws EventBusException em caso de erro na publicação
     * @throws IllegalArgumentException se a lista for null ou vazia
     */
    void publishBatch(List<DomainEvent> events);
    
    /**
     * Publica uma lista de eventos em lote de forma assíncrona.
     * 
     * @param events Lista de eventos a serem publicados
     * @return CompletableFuture que será completado quando todos os eventos forem processados
     * @throws EventBusException em caso de erro na publicação
     * @throws IllegalArgumentException se a lista for null ou vazia
     */
    CompletableFuture<Void> publishBatchAsync(List<DomainEvent> events);
    
    /**
     * Registra um handler para um tipo específico de evento.
     * 
     * <p>Este método é usado internamente pelo sistema de descoberta automática.
     * Normalmente não deve ser chamado diretamente.
     * 
     * @param eventType Tipo do evento (classe)
     * @param handler Handler a ser registrado
     * @param <T> Tipo do evento
     */
    <T extends DomainEvent> void registerHandler(Class<T> eventType, EventHandler<T> handler);
    
    /**
     * Remove um handler registrado para um tipo de evento.
     * 
     * @param eventType Tipo do evento
     * @param handler Handler a ser removido
     * @param <T> Tipo do evento
     */
    <T extends DomainEvent> void unregisterHandler(Class<T> eventType, EventHandler<T> handler);
    
    /**
     * Verifica se existem handlers registrados para um tipo de evento.
     * 
     * @param eventType Tipo do evento
     * @return true se existirem handlers, false caso contrário
     */
    boolean hasHandlers(Class<? extends DomainEvent> eventType);
    
    /**
     * Obtém estatísticas de execução do Event Bus.
     * 
     * @return Estatísticas detalhadas
     */
    EventBusStatistics getStatistics();
    
    /**
     * Verifica se o Event Bus está saudável e operacional.
     * 
     * @return true se estiver saudável, false caso contrário
     */
    boolean isHealthy();
    
    /**
     * Para o Event Bus graciosamente, aguardando o processamento
     * de eventos pendentes.
     * 
     * @param timeoutSeconds Timeout em segundos para aguardar
     * @return true se parou graciosamente, false se houve timeout
     */
    boolean shutdown(int timeoutSeconds);
}