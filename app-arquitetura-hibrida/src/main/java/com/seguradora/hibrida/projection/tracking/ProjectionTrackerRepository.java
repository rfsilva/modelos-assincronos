package com.seguradora.hibrida.projection.tracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de tracking de projeções.
 * 
 * <p>Fornece operações para:
 * <ul>
 *   <li>Controle de posição das projeções</li>
 *   <li>Monitoramento de saúde</li>
 *   <li>Estatísticas de processamento</li>
 *   <li>Identificação de projeções com lag</li>
 * </ul>
 */
@Repository
public interface ProjectionTrackerRepository extends JpaRepository<ProjectionTracker, String> {
    
    /**
     * Busca projeção por nome.
     * 
     * @param projectionName Nome da projeção
     * @return Optional com a projeção encontrada
     */
    Optional<ProjectionTracker> findByProjectionName(String projectionName);
    
    /**
     * Busca projeções por status.
     * 
     * @param status Status da projeção
     * @return Lista de projeções com o status especificado
     */
    List<ProjectionTracker> findByStatus(ProjectionStatus status);
    
    /**
     * Busca projeções ativas (não pausadas ou desabilitadas).
     * 
     * @return Lista de projeções ativas
     */
    @Query("SELECT pt FROM ProjectionTracker pt WHERE pt.status IN ('ACTIVE', 'ERROR')")
    List<ProjectionTracker> findActiveProjections();
    
    /**
     * Busca projeções com erro.
     * 
     * @return Lista de projeções com erro
     */
    List<ProjectionTracker> findByStatusAndLastErrorAtIsNotNull(ProjectionStatus status);
    
    /**
     * Busca projeções que não foram atualizadas há muito tempo.
     * 
     * @param threshold Timestamp limite
     * @return Lista de projeções possivelmente travadas
     */
    @Query("SELECT pt FROM ProjectionTracker pt WHERE pt.lastProcessedAt < :threshold AND pt.status = 'ACTIVE'")
    List<ProjectionTracker> findStaleProjections(@Param("threshold") Instant threshold);
    
    /**
     * Busca projeções com lag alto (diferença entre último evento processado e máximo disponível).
     * 
     * @param maxEventId ID do último evento disponível no event store
     * @param lagThreshold Threshold de lag considerado alto
     * @return Lista de projeções com lag alto
     */
    @Query("SELECT pt FROM ProjectionTracker pt WHERE ((:maxEventId - pt.lastProcessedEventId) > :lagThreshold) AND pt.status = 'ACTIVE'")
    List<ProjectionTracker> findProjectionsWithHighLag(@Param("maxEventId") Long maxEventId, 
                                                       @Param("lagThreshold") Long lagThreshold);
    
    /**
     * Obtém estatísticas gerais de todas as projeções.
     * 
     * @return Array com [total, ativas, com erro, pausadas, desabilitadas]
     */
    @Query("""
        SELECT COUNT(*), 
               SUM(CASE WHEN pt.status = 'ACTIVE' THEN 1 ELSE 0 END),
               SUM(CASE WHEN pt.status = 'ERROR' THEN 1 ELSE 0 END),
               SUM(CASE WHEN pt.status = 'PAUSED' THEN 1 ELSE 0 END),
               SUM(CASE WHEN pt.status = 'DISABLED' THEN 1 ELSE 0 END)
        FROM ProjectionTracker pt
        """)
    Object[] getProjectionStatistics();
    
    /**
     * Obtém projeções ordenadas por lag (maior lag primeiro).
     * 
     * @param maxEventId ID do último evento disponível
     * @return Lista ordenada por lag decrescente
     */
    @Query("""
        SELECT pt FROM ProjectionTracker pt 
        WHERE pt.status = 'ACTIVE'
        ORDER BY (:maxEventId - pt.lastProcessedEventId) DESC
        """)
    List<ProjectionTracker> findProjectionsOrderedByLag(@Param("maxEventId") Long maxEventId);
    
    /**
     * Busca projeções que falharam recentemente.
     * 
     * @param since Timestamp a partir do qual considerar falhas
     * @return Lista de projeções com falhas recentes
     */
    @Query("SELECT pt FROM ProjectionTracker pt WHERE pt.lastErrorAt >= :since")
    List<ProjectionTracker> findProjectionsWithRecentErrors(@Param("since") Instant since);
    
    /**
     * Obtém a posição mínima entre todas as projeções ativas.
     * 
     * @return Menor ID de evento processado entre todas as projeções ativas
     */
    @Query("SELECT MIN(pt.lastProcessedEventId) FROM ProjectionTracker pt WHERE pt.status = 'ACTIVE'")
    Optional<Long> findMinProcessedEventId();
    
    /**
     * Obtém a posição máxima entre todas as projeções ativas.
     * 
     * @return Maior ID de evento processado entre todas as projeções ativas
     */
    @Query("SELECT MAX(pt.lastProcessedEventId) FROM ProjectionTracker pt WHERE pt.status = 'ACTIVE'")
    Optional<Long> findMaxProcessedEventId();
    
    /**
     * Conta projeções por status.
     * 
     * @param status Status a contar
     * @return Número de projeções com o status especificado
     */
    long countByStatus(ProjectionStatus status);
    
    /**
     * Busca projeções que precisam de rebuild (muito atrás ou com muitos erros).
     * 
     * @param maxEventId ID do último evento disponível
     * @param lagThreshold Threshold de lag para considerar rebuild
     * @param errorThreshold Threshold de erros para considerar rebuild
     * @return Lista de projeções que precisam de rebuild
     */
    @Query("""
        SELECT pt FROM ProjectionTracker pt 
        WHERE pt.status = 'ACTIVE' 
        AND (
            (:maxEventId - pt.lastProcessedEventId) > :lagThreshold 
            OR pt.eventsFailed > :errorThreshold
        )
        """)
    List<ProjectionTracker> findProjectionsNeedingRebuild(@Param("maxEventId") Long maxEventId,
                                                          @Param("lagThreshold") Long lagThreshold,
                                                          @Param("errorThreshold") Long errorThreshold);
    
    /**
     * Atualiza status de múltiplas projeções.
     * 
     * @param projectionNames Lista de nomes das projeções
     * @param newStatus Novo status
     * @return Número de projeções atualizadas
     */
    @Query("UPDATE ProjectionTracker pt SET pt.status = :newStatus, pt.updatedAt = CURRENT_TIMESTAMP WHERE pt.projectionName IN :projectionNames")
    int updateStatusForProjections(@Param("projectionNames") List<String> projectionNames,
                                  @Param("newStatus") ProjectionStatus newStatus);
    
    /**
     * Remove trackers de projeções que não existem mais.
     * 
     * @param activeProjectionNames Lista de nomes de projeções ativas
     * @return Número de trackers removidos
     */
    @Query("DELETE FROM ProjectionTracker pt WHERE pt.projectionName NOT IN :activeProjectionNames")
    int deleteOrphanedTrackers(@Param("activeProjectionNames") List<String> activeProjectionNames);
    
    /**
     * Obtém métricas de performance por projeção.
     * 
     * @param projectionName Nome da projeção
     * @return Array com [eventos processados, eventos falhados, taxa de erro, tempo médio]
     */
    @Query("""
        SELECT pt.eventsProcessed, pt.eventsFailed,
               CASE WHEN (pt.eventsProcessed + pt.eventsFailed) > 0 
                    THEN CAST(pt.eventsFailed AS DOUBLE) / (pt.eventsProcessed + pt.eventsFailed)
                    ELSE 0.0 END,
               EXTRACT(EPOCH FROM (pt.updatedAt - pt.createdAt)) / NULLIF(pt.eventsProcessed, 0)
        FROM ProjectionTracker pt 
        WHERE pt.projectionName = :projectionName
        """)
    Object[] getProjectionMetrics(@Param("projectionName") String projectionName);
    
    /**
     * Busca projeções criadas recentemente.
     * 
     * @param since Timestamp a partir do qual considerar criação
     * @return Lista de projeções criadas recentemente
     */
    List<ProjectionTracker> findByCreatedAtGreaterThanEqual(Instant since);
    
    /**
     * Verifica se existe alguma projeção ativa.
     * 
     * @return true se existe pelo menos uma projeção ativa
     */
    boolean existsByStatus(ProjectionStatus status);
}