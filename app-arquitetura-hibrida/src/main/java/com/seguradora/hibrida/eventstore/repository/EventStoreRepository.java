package com.seguradora.hibrida.eventstore.repository;

import com.seguradora.hibrida.eventstore.entity.EventStoreEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de persistência do Event Store.
 * 
 * Implementa consultas otimizadas para Event Sourcing com suporte
 * a paginação e filtros avançados.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Repository
public interface EventStoreRepository extends JpaRepository<EventStoreEntry, UUID> {
    
    /**
     * Busca todos os eventos de um aggregate ordenados por versão.
     */
    List<EventStoreEntry> findByAggregateIdOrderByVersionAsc(String aggregateId);
    
    /**
     * Busca eventos de um aggregate a partir de uma versão específica.
     */
    List<EventStoreEntry> findByAggregateIdAndVersionGreaterThanEqualOrderByVersionAsc(
            String aggregateId, Long fromVersion);
    
    /**
     * Busca eventos por tipo em um período específico.
     */
    List<EventStoreEntry> findByEventTypeAndTimestampBetweenOrderByTimestampAsc(
            String eventType, Instant from, Instant to);
    
    /**
     * Busca eventos por correlation ID.
     */
    List<EventStoreEntry> findByCorrelationIdOrderByTimestampAsc(UUID correlationId);
    
    /**
     * Obtém a versão máxima de um aggregate.
     */
    @Query("SELECT MAX(e.version) FROM EventStoreEntry e WHERE e.aggregateId = :aggregateId")
    Optional<Long> findMaxVersionByAggregateId(@Param("aggregateId") String aggregateId);
    
    /**
     * Verifica se existe aggregate com ID específico.
     */
    boolean existsByAggregateId(String aggregateId);
    
    /**
     * Conta eventos por aggregate.
     */
    long countByAggregateId(String aggregateId);
    
    /**
     * Busca eventos paginados por período.
     */
    @Query("SELECT e FROM EventStoreEntry e WHERE e.timestamp BETWEEN :from AND :to ORDER BY e.timestamp ASC")
    Page<EventStoreEntry> findEventsByPeriod(
            @Param("from") Instant from, 
            @Param("to") Instant to, 
            Pageable pageable);
    
    /**
     * Busca eventos por múltiplos aggregates.
     */
    @Query("SELECT e FROM EventStoreEntry e WHERE e.aggregateId IN :aggregateIds ORDER BY e.aggregateId, e.version ASC")
    List<EventStoreEntry> findByAggregateIds(@Param("aggregateIds") List<String> aggregateIds);
    
    /**
     * Busca eventos por tipo de aggregate.
     */
    List<EventStoreEntry> findByAggregateTypeOrderByTimestampAsc(String aggregateType);
    
    /**
     * Busca últimos eventos por aggregate (para snapshot).
     */
    @Query(value = """
        SELECT e.* FROM events e 
        WHERE e.aggregate_id = :aggregateId 
        AND e.version > :fromVersion 
        ORDER BY e.version ASC 
        LIMIT :limit
        """, nativeQuery = true)
    List<EventStoreEntry> findEventsForSnapshot(
            @Param("aggregateId") String aggregateId,
            @Param("fromVersion") Long fromVersion,
            @Param("limit") int limit);
    
    /**
     * Estatísticas de eventos por período.
     */
    @Query("""
        SELECT e.eventType, COUNT(e), AVG(e.dataSize) 
        FROM EventStoreEntry e 
        WHERE e.timestamp BETWEEN :from AND :to 
        GROUP BY e.eventType
        """)
    List<Object[]> getEventStatistics(@Param("from") Instant from, @Param("to") Instant to);
}