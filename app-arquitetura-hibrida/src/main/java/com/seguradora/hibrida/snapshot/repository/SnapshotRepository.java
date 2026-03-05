package com.seguradora.hibrida.snapshot.repository;

import com.seguradora.hibrida.snapshot.entity.SnapshotEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de persistência de snapshots.
 * 
 * <p>Fornece consultas otimizadas para:
 * <ul>
 *   <li>Recuperação rápida de snapshots por aggregate</li>
 *   <li>Limpeza automática de snapshots antigos</li>
 *   <li>Consultas estatísticas para monitoramento</li>
 *   <li>Operações de manutenção em lote</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Repository
public interface SnapshotRepository extends JpaRepository<SnapshotEntry, String> {
    
    /**
     * Busca todos os snapshots de um aggregate ordenados por versão (mais recente primeiro).
     * 
     * @param aggregateId ID único do aggregate
     * @return Lista de snapshots ordenada por versão decrescente
     */
    List<SnapshotEntry> findByAggregateIdOrderByVersionDesc(String aggregateId);
    
    /**
     * Busca o snapshot mais recente de um aggregate.
     * 
     * @param aggregateId ID único do aggregate
     * @return Optional contendo o snapshot mais recente, ou empty se não existir
     */
    Optional<SnapshotEntry> findFirstByAggregateIdOrderByVersionDesc(String aggregateId);
    
    /**
     * Busca snapshot de um aggregate em uma versão específica ou anterior.
     * 
     * @param aggregateId ID único do aggregate
     * @param maxVersion Versão máxima (inclusive)
     * @return Optional contendo o snapshot encontrado, ou empty se não existir
     */
    Optional<SnapshotEntry> findFirstByAggregateIdAndVersionLessThanEqualOrderByVersionDesc(
            String aggregateId, Long maxVersion);
    
    /**
     * Verifica se existe snapshot para um aggregate.
     * 
     * @param aggregateId ID único do aggregate
     * @return true se existe pelo menos um snapshot, false caso contrário
     */
    boolean existsByAggregateId(String aggregateId);
    
    /**
     * Conta o número de snapshots de um aggregate.
     * 
     * @param aggregateId ID único do aggregate
     * @return Número de snapshots
     */
    long countByAggregateId(String aggregateId);
    
    /**
     * Busca snapshots antigos de um aggregate (mantém apenas os N mais recentes).
     * 
     * @param aggregateId ID único do aggregate
     * @param keepCount Número de snapshots a manter
     * @return Lista de snapshots a serem removidos
     */
    @Query("""
        SELECT s FROM SnapshotEntry s 
        WHERE s.aggregateId = :aggregateId 
        AND s.version NOT IN (
            SELECT s2.version FROM SnapshotEntry s2 
            WHERE s2.aggregateId = :aggregateId 
            ORDER BY s2.version DESC 
            LIMIT :keepCount
        )
        ORDER BY s.version ASC
        """)
    List<SnapshotEntry> findOldSnapshots(@Param("aggregateId") String aggregateId, 
                                        @Param("keepCount") int keepCount);
    
    /**
     * Remove snapshots antigos de um aggregate mantendo apenas os N mais recentes.
     * 
     * @param aggregateId ID único do aggregate
     * @param keepCount Número de snapshots a manter
     * @return Número de snapshots removidos
     */
    @Modifying
    @Query("""
        DELETE FROM SnapshotEntry s 
        WHERE s.aggregateId = :aggregateId 
        AND s.version NOT IN (
            SELECT s2.version FROM SnapshotEntry s2 
            WHERE s2.aggregateId = :aggregateId 
            ORDER BY s2.version DESC 
            LIMIT :keepCount
        )
        """)
    int deleteOldSnapshots(@Param("aggregateId") String aggregateId, 
                          @Param("keepCount") int keepCount);
    
    /**
     * Remove todos os snapshots de um aggregate.
     * 
     * @param aggregateId ID único do aggregate
     * @return Número de snapshots removidos
     */
    int deleteByAggregateId(String aggregateId);
    
    /**
     * Busca snapshots por tipo de aggregate.
     * 
     * @param aggregateType Tipo do aggregate
     * @return Lista de snapshots do tipo especificado
     */
    List<SnapshotEntry> findByAggregateTypeOrderByTimestampDesc(String aggregateType);
    
    /**
     * Busca snapshots criados em um período específico.
     * 
     * @param from Data/hora inicial (inclusive)
     * @param to Data/hora final (inclusive)
     * @return Lista de snapshots no período
     */
    List<SnapshotEntry> findByTimestampBetweenOrderByTimestampDesc(Instant from, Instant to);
    
    /**
     * Busca snapshots paginados por período.
     * 
     * @param from Data/hora inicial (inclusive)
     * @param to Data/hora final (inclusive)
     * @param pageable Configuração de paginação
     * @return Página de snapshots
     */
    Page<SnapshotEntry> findByTimestampBetween(Instant from, Instant to, Pageable pageable);
    
    /**
     * Obtém estatísticas básicas de snapshots por aggregate.
     * 
     * @param aggregateId ID único do aggregate
     * @return Array com [count, totalOriginalSize, totalCompressedSize, avgCompressionRatio]
     */
    @Query("""
        SELECT COUNT(s), 
               COALESCE(SUM(s.originalSize), 0), 
               COALESCE(SUM(s.compressedSize), 0),
               COALESCE(AVG(CASE WHEN s.compressed = true AND s.originalSize > 0 
                               THEN 1.0 - (CAST(s.compressedSize AS double) / s.originalSize) 
                               ELSE 0.0 END), 0.0)
        FROM SnapshotEntry s 
        WHERE s.aggregateId = :aggregateId
        """)
    Object[] getSnapshotStatistics(@Param("aggregateId") String aggregateId);
    
    /**
     * Obtém estatísticas globais de snapshots.
     * 
     * @return Array com estatísticas globais
     */
    @Query("""
        SELECT COUNT(s), 
               COUNT(DISTINCT s.aggregateId),
               COALESCE(SUM(s.originalSize), 0), 
               COALESCE(SUM(s.compressedSize), 0),
               COALESCE(AVG(CASE WHEN s.compressed = true AND s.originalSize > 0 
                               THEN 1.0 - (CAST(s.compressedSize AS double) / s.originalSize) 
                               ELSE 0.0 END), 0.0),
               MIN(s.timestamp),
               MAX(s.timestamp)
        FROM SnapshotEntry s
        """)
    Object[] getGlobalStatistics();
    
    /**
     * Conta snapshots comprimidos.
     * 
     * @return Número de snapshots comprimidos
     */
    long countByCompressedTrue();
    
    /**
     * Busca snapshots criados nas últimas N horas.
     * 
     * @param hours Número de horas atrás
     * @return Lista de snapshots recentes
     */
    @Query("SELECT s FROM SnapshotEntry s WHERE s.timestamp >= :since ORDER BY s.timestamp DESC")
    List<SnapshotEntry> findRecentSnapshots(@Param("since") Instant since);
    
    /**
     * Conta snapshots criados nas últimas N horas.
     * 
     * @param since Timestamp de referência
     * @return Número de snapshots criados desde o timestamp
     */
    long countByTimestampGreaterThanEqual(Instant since);
    
    /**
     * Busca aggregates que precisam de limpeza de snapshots.
     * 
     * @param maxSnapshots Número máximo de snapshots por aggregate
     * @return Lista de aggregate IDs que excedem o limite
     */
    @Query("""
        SELECT s.aggregateId 
        FROM SnapshotEntry s 
        GROUP BY s.aggregateId 
        HAVING COUNT(s) > :maxSnapshots
        """)
    List<String> findAggregatesNeedingCleanup(@Param("maxSnapshots") long maxSnapshots);
    
    /**
     * Obtém métricas de eficiência por período.
     * 
     * @param aggregateId ID do aggregate (opcional)
     * @param from Data inicial
     * @param to Data final
     * @return Array com métricas de eficiência
     */
    @Query("""
        SELECT COUNT(s),
               COALESCE(AVG(s.originalSize), 0),
               COALESCE(AVG(s.compressedSize), 0),
               COALESCE(AVG(CASE WHEN s.compressed = true AND s.originalSize > 0 
                               THEN 1.0 - (CAST(s.compressedSize AS double) / s.originalSize) 
                               ELSE 0.0 END), 0.0)
        FROM SnapshotEntry s 
        WHERE (:aggregateId IS NULL OR s.aggregateId = :aggregateId)
        AND s.timestamp BETWEEN :from AND :to
        """)
    Object[] getEfficiencyMetrics(@Param("aggregateId") String aggregateId,
                                 @Param("from") Instant from,
                                 @Param("to") Instant to);
    
    /**
     * Remove snapshots mais antigos que uma data específica.
     * 
     * @param before Data limite (exclusive)
     * @return Número de snapshots removidos
     */
    @Modifying
    @Query("DELETE FROM SnapshotEntry s WHERE s.timestamp < :before")
    int deleteSnapshotsOlderThan(@Param("before") Instant before);
}