package com.seguradora.hibrida.snapshot;

import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Interface principal para operações de snapshot de aggregates.
 * 
 * <p>O SnapshotStore é responsável por:
 * <ul>
 *   <li>Salvar snapshots de aggregates para otimizar reconstrução</li>
 *   <li>Recuperar snapshots mais recentes para reconstrução rápida</li>
 *   <li>Gerenciar limpeza automática de snapshots antigos</li>
 *   <li>Fornecer métricas de eficiência de snapshots</li>
 * </ul>
 * 
 * <p>Exemplo de uso:
 * <pre>{@code
 * // Salvar snapshot
 * AggregateSnapshot snapshot = new AggregateSnapshot(
 *     "aggregate-123", "SeguradoAggregate", 50, aggregateData
 * );
 * snapshotStore.saveSnapshot(snapshot);
 * 
 * // Recuperar snapshot mais recente
 * Optional<AggregateSnapshot> latest = snapshotStore.getLatestSnapshot("aggregate-123");
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface SnapshotStore {
    
    /**
     * Salva um snapshot de aggregate.
     * 
     * <p>O snapshot é salvo de forma assíncrona para não bloquear operações.
     * Automaticamente aplica compressão se o tamanho exceder o threshold configurado.
     * 
     * @param snapshot Snapshot a ser persistido
     * @throws SnapshotException em caso de erro na persistência
     */
    void saveSnapshot(AggregateSnapshot snapshot);
    
    /**
     * Recupera o snapshot mais recente de um aggregate.
     * 
     * @param aggregateId ID único do aggregate
     * @return Optional contendo o snapshot mais recente, ou empty se não existir
     */
    Optional<AggregateSnapshot> getLatestSnapshot(String aggregateId);
    
    /**
     * Recupera snapshot de um aggregate em uma versão específica ou anterior.
     * 
     * @param aggregateId ID único do aggregate
     * @param maxVersion Versão máxima do snapshot (inclusive)
     * @return Optional contendo o snapshot encontrado, ou empty se não existir
     */
    Optional<AggregateSnapshot> getSnapshotAtOrBeforeVersion(String aggregateId, long maxVersion);
    
    /**
     * Lista todos os snapshots de um aggregate ordenados por versão (mais recente primeiro).
     * 
     * @param aggregateId ID único do aggregate
     * @return Lista de snapshots ordenada por versão decrescente
     */
    List<AggregateSnapshot> getSnapshotHistory(String aggregateId);
    
    /**
     * Remove snapshots antigos mantendo apenas os N mais recentes.
     * 
     * <p>Por padrão, mantém os últimos 5 snapshots por aggregate.
     * Esta operação é executada automaticamente pelo scheduler.
     * 
     * @param aggregateId ID único do aggregate
     * @param keepCount Número de snapshots a manter (padrão: 5)
     * @return Número de snapshots removidos
     */
    int cleanupOldSnapshots(String aggregateId, int keepCount);
    
    /**
     * Remove todos os snapshots antigos do sistema.
     * 
     * <p>Executa limpeza em todos os aggregates mantendo apenas
     * os snapshots mais recentes conforme configuração.
     * 
     * @param keepCount Número de snapshots a manter por aggregate
     * @return Número total de snapshots removidos
     */
    int cleanupAllOldSnapshots(int keepCount);
    
    /**
     * Verifica se um aggregate possui snapshots.
     * 
     * @param aggregateId ID único do aggregate
     * @return true se existir pelo menos um snapshot, false caso contrário
     */
    boolean hasSnapshots(String aggregateId);
    
    /**
     * Obtém estatísticas de snapshots para um aggregate.
     * 
     * @param aggregateId ID único do aggregate
     * @return Estatísticas detalhadas do aggregate
     */
    SnapshotStatistics getSnapshotStatistics(String aggregateId);
    
    /**
     * Obtém estatísticas globais de snapshots.
     * 
     * @return Estatísticas globais do sistema de snapshots
     */
    SnapshotStatistics getGlobalStatistics();
    
    /**
     * Verifica se um snapshot deve ser criado para um aggregate.
     * 
     * <p>Baseado na configuração de threshold (padrão: 50 eventos)
     * e na existência de snapshots anteriores.
     * 
     * @param aggregateId ID único do aggregate
     * @param currentVersion Versão atual do aggregate
     * @return true se deve criar snapshot, false caso contrário
     */
    boolean shouldCreateSnapshot(String aggregateId, long currentVersion);
    
    /**
     * Remove todos os snapshots de um aggregate específico.
     * 
     * <p>Usado principalmente para testes ou limpeza completa.
     * 
     * @param aggregateId ID único do aggregate
     * @return Número de snapshots removidos
     */
    int deleteAllSnapshots(String aggregateId);
    
    /**
     * Obtém métricas de eficiência de snapshots.
     * 
     * @param aggregateId ID único do aggregate
     * @param period Período para análise (em dias)
     * @return Métricas de eficiência
     */
    SnapshotEfficiencyMetrics getEfficiencyMetrics(String aggregateId, int period);
}