package com.seguradora.hibrida.eventstore;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Interface principal do Event Store para persistência e recuperação de eventos de domínio.
 * 
 * Esta interface define o contrato para armazenamento de eventos seguindo os padrões
 * de Event Sourcing, garantindo persistência ACID e consultas otimizadas.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface EventStore {
    
    /**
     * Salva uma lista de eventos para um aggregate específico.
     * 
     * @param aggregateId ID único do aggregate
     * @param events Lista de eventos a serem persistidos
     * @param expectedVersion Versão esperada do aggregate para controle de concorrência
     * @throws ConcurrencyException se a versão esperada não coincidir
     * @throws EventStoreException em caso de erro na persistência
     */
    void saveEvents(String aggregateId, List<DomainEvent> events, long expectedVersion);
    
    /**
     * Carrega todos os eventos de um aggregate específico.
     * 
     * @param aggregateId ID único do aggregate
     * @return Lista ordenada de eventos por versão
     */
    List<DomainEvent> loadEvents(String aggregateId);
    
    /**
     * Carrega eventos de um aggregate a partir de uma versão específica.
     * 
     * @param aggregateId ID único do aggregate
     * @param fromVersion Versão inicial (inclusive)
     * @return Lista ordenada de eventos por versão
     */
    List<DomainEvent> loadEvents(String aggregateId, long fromVersion);
    
    /**
     * Carrega eventos por tipo em um período específico.
     * 
     * @param eventType Tipo do evento
     * @param from Data/hora inicial (inclusive)
     * @param to Data/hora final (inclusive)
     * @return Lista de eventos ordenada por timestamp
     */
    List<DomainEvent> loadEventsByType(String eventType, Instant from, Instant to);
    
    /**
     * Carrega eventos por correlation ID para rastreamento.
     * 
     * @param correlationId ID de correlação
     * @return Lista de eventos relacionados
     */
    List<DomainEvent> loadEventsByCorrelationId(UUID correlationId);
    
    /**
     * Obtém a versão atual de um aggregate.
     * 
     * @param aggregateId ID único do aggregate
     * @return Versão atual ou 0 se não existir
     */
    long getCurrentVersion(String aggregateId);
    
    /**
     * Verifica se um aggregate existe no event store.
     * 
     * @param aggregateId ID único do aggregate
     * @return true se existir, false caso contrário
     */
    boolean aggregateExists(String aggregateId);
}