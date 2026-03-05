package com.seguradora.hibrida.projection;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;

/**
 * Classe base abstrata para implementação de Projection Handlers.
 * 
 * <p>Fornece funcionalidades comuns como:
 * <ul>
 *   <li>Logging estruturado</li>
 *   <li>Detecção automática do tipo de evento</li>
 *   <li>Controle de transação</li>
 *   <li>Métricas básicas</li>
 * </ul>
 * 
 * @param <T> Tipo do evento de domínio processado
 */
public abstract class AbstractProjectionHandler<T extends DomainEvent> implements ProjectionHandler<T> {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    private final Class<T> eventType;
    private final String projectionName;
    
    @SuppressWarnings("unchecked")
    protected AbstractProjectionHandler() {
        // Detecta automaticamente o tipo do evento através de reflection
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            this.eventType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
        } else {
            throw new IllegalStateException("Não foi possível determinar o tipo do evento para " + getClass().getName());
        }
        
        // Nome da projeção baseado no nome da classe
        this.projectionName = getClass().getSimpleName().replace("Handler", "");
    }
    
    protected AbstractProjectionHandler(String projectionName) {
        this();
        // Permite override do nome da projeção se necessário
    }
    
    @Override
    @Transactional("readTransactionManager")
    public final void handle(T event) {
        long startTime = System.currentTimeMillis();
        
        log.debug("Iniciando processamento de evento {} para projeção {}", 
                 event.getEventType(), getProjectionName());
        
        try {
            // Validação básica
            if (event == null) {
                throw new ProjectionException("Evento não pode ser null", getProjectionName());
            }
            
            if (!supports(event)) {
                log.debug("Evento {} não suportado por {}, ignorando", 
                         event.getEventType(), getProjectionName());
                return;
            }
            
            // Processamento do evento
            doHandle(event);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            log.debug("Evento {} processado com sucesso por {} em {}ms", 
                     event.getEventType(), getProjectionName(), processingTime);
            
            // Registrar métricas de sucesso
            recordSuccess(event, processingTime);
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            
            log.error("Erro ao processar evento {} na projeção {}: {}", 
                     event.getEventType(), getProjectionName(), e.getMessage(), e);
            
            // Registrar métricas de erro
            recordError(event, processingTime, e);
            
            // Re-lançar como ProjectionException
            throw new ProjectionException(
                "Falha ao processar evento " + event.getEventType() + " na projeção " + getProjectionName(), 
                getProjectionName(), e);
        }
    }
    
    /**
     * Implementação específica do processamento do evento.
     * 
     * <p>Este método deve ser implementado pelas classes filhas
     * com a lógica específica de atualização da projeção.
     * 
     * @param event Evento a ser processado
     * @throws Exception em caso de erro no processamento
     */
    protected abstract void doHandle(T event) throws Exception;
    
    @Override
    public Class<T> getEventType() {
        return eventType;
    }
    
    @Override
    public String getProjectionName() {
        return projectionName;
    }
    
    /**
     * Registra métricas de sucesso no processamento.
     * 
     * <p>Pode ser sobrescrito pelas classes filhas para
     * adicionar métricas específicas.
     * 
     * @param event Evento processado
     * @param processingTimeMs Tempo de processamento em milissegundos
     */
    protected void recordSuccess(T event, long processingTimeMs) {
        // Implementação padrão - pode ser sobrescrita
        log.trace("Registrando sucesso para evento {} - {}ms", 
                 event.getEventType(), processingTimeMs);
    }
    
    /**
     * Registra métricas de erro no processamento.
     * 
     * <p>Pode ser sobrescrito pelas classes filhas para
     * adicionar métricas específicas.
     * 
     * @param event Evento que falhou
     * @param processingTimeMs Tempo de processamento em milissegundos
     * @param error Erro ocorrido
     */
    protected void recordError(T event, long processingTimeMs, Exception error) {
        // Implementação padrão - pode ser sobrescrita
        log.trace("Registrando erro para evento {} - {}ms - {}", 
                 event.getEventType(), processingTimeMs, error.getMessage());
    }
    
    /**
     * Utilitário para logging estruturado de eventos.
     * 
     * @param event Evento a ser logado
     * @return String formatada para log
     */
    protected String formatEventForLog(T event) {
        return String.format("Event[type=%s, aggregateId=%s, timestamp=%s]",
                           event.getEventType(),
                           event.getMetadata() != null ? event.getMetadata().get("aggregateId") : "unknown",
                           event.getMetadata() != null ? event.getMetadata().get("timestamp") : Instant.now());
    }
    
    /**
     * Verifica se o evento é mais recente que um timestamp específico.
     * 
     * <p>Útil para implementar lógica de idempotência baseada em timestamp.
     * 
     * @param event Evento a ser verificado
     * @param lastProcessedTimestamp Último timestamp processado
     * @return true se o evento é mais recente
     */
    protected boolean isEventNewer(T event, Instant lastProcessedTimestamp) {
        if (lastProcessedTimestamp == null) {
            return true;
        }
        
        Object timestampObj = event.getMetadata() != null ? 
                             event.getMetadata().get("timestamp") : null;
        
        if (timestampObj instanceof Instant) {
            Instant eventTimestamp = (Instant) timestampObj;
            return eventTimestamp.isAfter(lastProcessedTimestamp);
        }
        
        // Se não conseguir determinar, assume que é mais recente
        return true;
    }
}