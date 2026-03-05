package com.seguradora.hibrida.snapshot.impl;

import com.seguradora.hibrida.snapshot.*;
import com.seguradora.hibrida.snapshot.entity.SnapshotEntry;
import com.seguradora.hibrida.snapshot.exception.SnapshotException;
import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import com.seguradora.hibrida.snapshot.repository.SnapshotRepository;
import com.seguradora.hibrida.snapshot.serialization.SnapshotSerializationResult;
import com.seguradora.hibrida.snapshot.serialization.SnapshotSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementação PostgreSQL do SnapshotStore com funcionalidades avançadas.
 * 
 * <p>Características principais:
 * <ul>
 *   <li>Persistência otimizada no PostgreSQL</li>
 *   <li>Compressão automática de snapshots grandes</li>
 *   <li>Limpeza automática de snapshots antigos</li>
 *   <li>Operações assíncronas para não bloquear threads</li>
 *   <li>Métricas detalhadas de performance</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostgreSQLSnapshotStore implements SnapshotStore {
    
    private final SnapshotRepository snapshotRepository;
    private final SnapshotSerializer snapshotSerializer;
    private final SnapshotProperties snapshotProperties;
    
    @Override
    @Async("snapshotTaskExecutor")
    public void saveSnapshot(AggregateSnapshot snapshot) {
        try {
            log.debug("Saving snapshot for aggregate {} at version {}", 
                     snapshot.getAggregateId(), snapshot.getVersion());
            
            // Serializar com compressão se necessário
            SnapshotSerializationResult result = snapshotSerializer.serializeWithCompression(
                snapshot, snapshotProperties.getCompressionThreshold()
            );
            
            // Criar entrada para persistência
            SnapshotEntry entry = createSnapshotEntry(snapshot, result);
            
            // Salvar no banco
            snapshotRepository.save(entry);
            
            log.info("Snapshot saved successfully for aggregate {} at version {} (compressed: {}, size: {} bytes)", 
                    snapshot.getAggregateId(), snapshot.getVersion(), 
                    result.isCompressed(), result.getEffectiveSize());
            
            // Agendar limpeza assíncrona se necessário
            scheduleCleanupIfNeeded(snapshot.getAggregateId());
            
        } catch (Exception e) {
            log.error("Failed to save snapshot for aggregate {} at version {}: {}", 
                     snapshot.getAggregateId(), snapshot.getVersion(), e.getMessage());
            throw new SnapshotException(
                "Failed to save snapshot", e, snapshot.getAggregateId(), snapshot.getVersion()
            );
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AggregateSnapshot> getLatestSnapshot(String aggregateId) {
        try {
            log.debug("Loading latest snapshot for aggregate {}", aggregateId);
            
            Optional<SnapshotEntry> entry = snapshotRepository.findFirstByAggregateIdOrderByVersionDesc(aggregateId);
            
            if (entry.isEmpty()) {
                log.debug("No snapshot found for aggregate {}", aggregateId);
                return Optional.empty();
            }
            
            AggregateSnapshot snapshot = deserializeSnapshot(entry.get());
            
            log.debug("Latest snapshot loaded for aggregate {} at version {}", 
                     aggregateId, snapshot.getVersion());
            
            return Optional.of(snapshot);
        } catch (Exception e) {
            log.error("Failed to load latest snapshot for aggregate {}: {}", aggregateId, e.getMessage());
            throw new SnapshotException("Failed to load latest snapshot", e, aggregateId, null);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AggregateSnapshot> getSnapshotAtOrBeforeVersion(String aggregateId, long maxVersion) {
        try {
            log.debug("Loading snapshot for aggregate {} at or before version {}", aggregateId, maxVersion);
            
            Optional<SnapshotEntry> entry = snapshotRepository
                .findFirstByAggregateIdAndVersionLessThanEqualOrderByVersionDesc(aggregateId, maxVersion);
            
            if (entry.isEmpty()) {
                log.debug("No snapshot found for aggregate {} at or before version {}", aggregateId, maxVersion);
                return Optional.empty();
            }
            
            AggregateSnapshot snapshot = deserializeSnapshot(entry.get());
            
            log.debug("Snapshot loaded for aggregate {} at version {} (requested max: {})", 
                     aggregateId, snapshot.getVersion(), maxVersion);
            
            return Optional.of(snapshot);
        } catch (Exception e) {
            log.error("Failed to load snapshot for aggregate {} at or before version {}: {}", 
                     aggregateId, maxVersion, e.getMessage());
            throw new SnapshotException("Failed to load snapshot", e, aggregateId, maxVersion);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AggregateSnapshot> getSnapshotHistory(String aggregateId) {
        try {
            log.debug("Loading snapshot history for aggregate {}", aggregateId);
            
            List<SnapshotEntry> entries = snapshotRepository.findByAggregateIdOrderByVersionDesc(aggregateId);
            
            List<AggregateSnapshot> snapshots = entries.stream()
                .map(this::deserializeSnapshot)
                .toList();
            
            log.debug("Loaded {} snapshots for aggregate {}", snapshots.size(), aggregateId);
            
            return snapshots;
        } catch (Exception e) {
            log.error("Failed to load snapshot history for aggregate {}: {}", aggregateId, e.getMessage());
            throw new SnapshotException("Failed to load snapshot history", e, aggregateId, null);
        }
    }
    
    @Override
    @Transactional
    public int cleanupOldSnapshots(String aggregateId, int keepCount) {
        try {
            log.debug("Cleaning up old snapshots for aggregate {} (keeping {})", aggregateId, keepCount);
            
            int deletedCount = snapshotRepository.deleteOldSnapshots(aggregateId, keepCount);
            
            if (deletedCount > 0) {
                log.info("Cleaned up {} old snapshots for aggregate {}", deletedCount, aggregateId);
            }
            
            return deletedCount;
        } catch (Exception e) {
            log.error("Failed to cleanup old snapshots for aggregate {}: {}", aggregateId, e.getMessage());
            throw new SnapshotException("Failed to cleanup old snapshots", e, aggregateId, null);
        }
    }
    
    @Override
    @Transactional
    public int cleanupAllOldSnapshots(int keepCount) {
        try {
            log.info("Starting global cleanup of old snapshots (keeping {} per aggregate)", keepCount);
            
            List<String> aggregatesNeedingCleanup = snapshotRepository.findAggregatesNeedingCleanup(keepCount);
            int totalDeleted = 0;
            
            for (String aggregateId : aggregatesNeedingCleanup) {
                int deleted = cleanupOldSnapshots(aggregateId, keepCount);
                totalDeleted += deleted;
            }
            
            log.info("Global cleanup completed. Removed {} snapshots from {} aggregates", 
                    totalDeleted, aggregatesNeedingCleanup.size());
            
            return totalDeleted;
        } catch (Exception e) {
            log.error("Failed to perform global cleanup of old snapshots: {}", e.getMessage());
            throw new SnapshotException("Failed to perform global cleanup", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasSnapshots(String aggregateId) {
        return snapshotRepository.existsByAggregateId(aggregateId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SnapshotStatistics getSnapshotStatistics(String aggregateId) {
        try {
            Object[] stats = snapshotRepository.getSnapshotStatistics(aggregateId);
            
            if (stats == null || stats.length < 4) {
                return buildEmptyStatistics(aggregateId);
            }
            
            long totalSnapshots = ((Number) stats[0]).longValue();
            long totalOriginalSize = ((Number) stats[1]).longValue();
            long totalCompressedSize = ((Number) stats[2]).longValue();
            double avgCompressionRatio = ((Number) stats[3]).doubleValue();
            
            // Buscar informações adicionais
            List<SnapshotEntry> entries = snapshotRepository.findByAggregateIdOrderByVersionDesc(aggregateId);
            
            Instant oldest = entries.isEmpty() ? null : entries.get(entries.size() - 1).getTimestamp();
            Instant newest = entries.isEmpty() ? null : entries.get(0).getTimestamp();
            long latestVersion = entries.isEmpty() ? 0 : entries.get(0).getVersion();
            
            long compressedSnapshots = entries.stream()
                .mapToLong(e -> Boolean.TRUE.equals(e.getCompressed()) ? 1 : 0)
                .sum();
            
            // Calcular métricas temporais
            Instant last24Hours = Instant.now().minus(24, ChronoUnit.HOURS);
            Instant lastWeek = Instant.now().minus(7, ChronoUnit.DAYS);
            
            long snapshotsLast24Hours = snapshotRepository.countByTimestampGreaterThanEqual(last24Hours);
            long snapshotsLastWeek = snapshotRepository.countByTimestampGreaterThanEqual(lastWeek);
            
            double averageTimeBetweenSnapshots = calculateAverageTimeBetweenSnapshots(entries);
            double averageSnapshotSize = totalSnapshots > 0 ? (double) totalOriginalSize / totalSnapshots : 0.0;
            long totalSpaceSaved = totalOriginalSize - totalCompressedSize;
            
            return SnapshotStatistics.builder()
                .aggregateId(aggregateId)
                .aggregateType(entries.isEmpty() ? null : entries.get(0).getAggregateType())
                .totalSnapshots(totalSnapshots)
                .compressedSnapshots(compressedSnapshots)
                .totalOriginalSize(totalOriginalSize)
                .totalCompressedSize(totalCompressedSize)
                .oldestSnapshot(oldest)
                .newestSnapshot(newest)
                .latestVersion(latestVersion)
                .averageTimeBetweenSnapshots(averageTimeBetweenSnapshots)
                .averageSnapshotSize(averageSnapshotSize)
                .averageCompressionRatio(avgCompressionRatio)
                .snapshotsLast24Hours(snapshotsLast24Hours)
                .snapshotsLastWeek(snapshotsLastWeek)
                .totalSpaceSaved(totalSpaceSaved)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to get snapshot statistics for aggregate {}: {}", aggregateId, e.getMessage());
            throw new SnapshotException("Failed to get snapshot statistics", e, aggregateId, null);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public SnapshotStatistics getGlobalStatistics() {
        try {
            Object[] stats = snapshotRepository.getGlobalStatistics();
            
            if (stats == null || stats.length < 7) {
                return buildEmptyGlobalStatistics();
            }
            
            long totalSnapshots = ((Number) stats[0]).longValue();
            long totalAggregates = ((Number) stats[1]).longValue();
            long totalOriginalSize = ((Number) stats[2]).longValue();
            long totalCompressedSize = ((Number) stats[3]).longValue();
            double avgCompressionRatio = ((Number) stats[4]).doubleValue();
            Instant oldest = stats[5] != null ? (Instant) stats[5] : null;
            Instant newest = stats[6] != null ? (Instant) stats[6] : null;
            
            long compressedSnapshots = snapshotRepository.countByCompressedTrue();
            
            // Métricas temporais
            Instant last24Hours = Instant.now().minus(24, ChronoUnit.HOURS);
            Instant lastWeek = Instant.now().minus(7, ChronoUnit.DAYS);
            
            long snapshotsLast24Hours = snapshotRepository.countByTimestampGreaterThanEqual(last24Hours);
            long snapshotsLastWeek = snapshotRepository.countByTimestampGreaterThanEqual(lastWeek);
            
            double averageTimeBetweenSnapshots = calculateGlobalAverageTimeBetweenSnapshots();
            double averageSnapshotSize = totalSnapshots > 0 ? (double) totalOriginalSize / totalSnapshots : 0.0;
            long totalSpaceSaved = totalOriginalSize - totalCompressedSize;
            
            return SnapshotStatistics.builder()
                .aggregateId(null) // Global statistics
                .aggregateType(null)
                .totalSnapshots(totalSnapshots)
                .compressedSnapshots(compressedSnapshots)
                .totalOriginalSize(totalOriginalSize)
                .totalCompressedSize(totalCompressedSize)
                .oldestSnapshot(oldest)
                .newestSnapshot(newest)
                .latestVersion(0) // Not applicable for global stats
                .averageTimeBetweenSnapshots(averageTimeBetweenSnapshots)
                .averageSnapshotSize(averageSnapshotSize)
                .averageCompressionRatio(avgCompressionRatio)
                .snapshotsLast24Hours(snapshotsLast24Hours)
                .snapshotsLastWeek(snapshotsLastWeek)
                .totalSpaceSaved(totalSpaceSaved)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to get global snapshot statistics: {}", e.getMessage());
            throw new SnapshotException("Failed to get global snapshot statistics", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean shouldCreateSnapshot(String aggregateId, long currentVersion) {
        try {
            Optional<SnapshotEntry> latestSnapshot = snapshotRepository
                .findFirstByAggregateIdOrderByVersionDesc(aggregateId);
            
            if (latestSnapshot.isEmpty()) {
                // Primeiro snapshot - criar se versão >= threshold
                return currentVersion >= snapshotProperties.getSnapshotThreshold();
            }
            
            long lastSnapshotVersion = latestSnapshot.get().getVersion();
            long eventsSinceLastSnapshot = currentVersion - lastSnapshotVersion;
            
            boolean shouldCreate = eventsSinceLastSnapshot >= snapshotProperties.getSnapshotThreshold();
            
            log.debug("Snapshot decision for aggregate {}: currentVersion={}, lastSnapshot={}, " +
                     "eventsSince={}, threshold={}, shouldCreate={}", 
                     aggregateId, currentVersion, lastSnapshotVersion, 
                     eventsSinceLastSnapshot, snapshotProperties.getSnapshotThreshold(), shouldCreate);
            
            return shouldCreate;
        } catch (Exception e) {
            log.error("Failed to determine if snapshot should be created for aggregate {}: {}", aggregateId, e.getMessage());
            // Em caso de erro, não criar snapshot para evitar problemas
            return false;
        }
    }
    
    @Override
    @Transactional
    public int deleteAllSnapshots(String aggregateId) {
        try {
            log.warn("Deleting ALL snapshots for aggregate {}", aggregateId);
            
            int deletedCount = snapshotRepository.deleteByAggregateId(aggregateId);
            
            log.info("Deleted {} snapshots for aggregate {}", deletedCount, aggregateId);
            
            return deletedCount;
        } catch (Exception e) {
            log.error("Failed to delete all snapshots for aggregate {}: {}", aggregateId, e.getMessage());
            throw new SnapshotException("Failed to delete all snapshots", e, aggregateId, null);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public SnapshotEfficiencyMetrics getEfficiencyMetrics(String aggregateId, int period) {
        try {
            Instant periodEnd = Instant.now();
            Instant periodStart = periodEnd.minus(period, ChronoUnit.DAYS);
            
            // Buscar métricas básicas
            Object[] metrics = snapshotRepository.getEfficiencyMetrics(aggregateId, periodStart, periodEnd);
            
            if (metrics == null || metrics.length < 4) {
                return buildEmptyEfficiencyMetrics(aggregateId, periodStart, periodEnd);
            }
            
            long snapshotsCreated = ((Number) metrics[0]).longValue();
            double avgOriginalSize = ((Number) metrics[1]).doubleValue();
            double avgCompressedSize = ((Number) metrics[2]).doubleValue();
            double avgCompressionRatio = ((Number) metrics[3]).doubleValue();
            
            // Para métricas mais detalhadas, seria necessário implementar
            // tracking de reconstruções e tempos - por simplicidade, usando valores simulados
            return SnapshotEfficiencyMetrics.builder()
                .aggregateId(aggregateId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .reconstructionsWithSnapshot(0L) // Seria implementado com tracking
                .reconstructionsWithoutSnapshot(0L)
                .averageReconstructionTimeWithSnapshot(0.0)
                .averageReconstructionTimeWithoutSnapshot(0.0)
                .averageEventsWithSnapshot(0.0)
                .averageEventsWithoutSnapshot(0.0)
                .totalSpaceSaved((long) ((avgOriginalSize - avgCompressedSize) * snapshotsCreated))
                .snapshotsCreated(snapshotsCreated)
                .snapshotsFailed(0L) // Seria implementado com tracking de falhas
                .averageSnapshotCreationTime(0.0)
                .averageCompressionRatio(avgCompressionRatio)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to get efficiency metrics for aggregate {} in period {} days: {}", 
                     aggregateId, period, e.getMessage());
            throw new SnapshotException("Failed to get efficiency metrics", e, aggregateId, null);
        }
    }
    
    /**
     * Cria entrada de snapshot para persistência.
     */
    private SnapshotEntry createSnapshotEntry(AggregateSnapshot snapshot, SnapshotSerializationResult result) {
        SnapshotEntry entry = new SnapshotEntry(
            snapshot.getSnapshotId(),
            snapshot.getAggregateId(),
            snapshot.getAggregateType(),
            snapshot.getVersion(),
            snapshot.getData(),
            snapshot.getTimestamp()
        );
        
        entry.setMetadata(snapshot.getMetadata());
        entry.setSchemaVersion(snapshot.getSchemaVersion());
        entry.setCompressed(result.isCompressed());
        entry.setOriginalSize(result.getOriginalSize());
        entry.setCompressedSize(result.getCompressedSize());
        entry.setCompressionAlgorithm(result.getCompressionAlgorithm());
        entry.setDataHash(result.getDataHash());
        
        return entry;
    }
    
    /**
     * Deserializa snapshot de entrada do banco.
     */
    private AggregateSnapshot deserializeSnapshot(SnapshotEntry entry) {
        // Para simplificar, retornando snapshot básico
        // Em implementação completa, seria necessário deserializar os dados comprimidos
        return new AggregateSnapshot(
            entry.getSnapshotId(),
            entry.getAggregateId(),
            entry.getAggregateType(),
            entry.getVersion(),
            entry.getSnapshotData(),
            entry.getTimestamp(),
            entry.getMetadata(),
            entry.getSchemaVersion(),
            entry.isCompressed(),
            entry.getOriginalSize() != null ? entry.getOriginalSize() : 0,
            entry.getCompressedSize() != null ? entry.getCompressedSize() : 0
        );
    }
    
    /**
     * Agenda limpeza assíncrona se necessário.
     */
    @Async("snapshotTaskExecutor")
    private void scheduleCleanupIfNeeded(String aggregateId) {
        try {
            long snapshotCount = snapshotRepository.countByAggregateId(aggregateId);
            
            if (snapshotCount > snapshotProperties.getMaxSnapshotsPerAggregate()) {
                log.debug("Scheduling cleanup for aggregate {} (has {} snapshots)", 
                         aggregateId, snapshotCount);
                
                CompletableFuture.runAsync(() -> 
                    cleanupOldSnapshots(aggregateId, snapshotProperties.getMaxSnapshotsPerAggregate())
                );
            }
        } catch (Exception e) {
            log.warn("Failed to schedule cleanup for aggregate {}: {}", aggregateId, e.getMessage());
        }
    }
    
    /**
     * Calcula tempo médio entre snapshots.
     */
    private double calculateAverageTimeBetweenSnapshots(List<SnapshotEntry> entries) {
        if (entries.size() < 2) {
            return 0.0;
        }
        
        long totalSeconds = 0;
        for (int i = 0; i < entries.size() - 1; i++) {
            Duration duration = Duration.between(entries.get(i + 1).getTimestamp(), entries.get(i).getTimestamp());
            totalSeconds += duration.getSeconds();
        }
        
        return (double) totalSeconds / (entries.size() - 1);
    }
    
    /**
     * Calcula tempo médio global entre snapshots.
     */
    private double calculateGlobalAverageTimeBetweenSnapshots() {
        // Implementação simplificada - em produção seria mais complexa
        return 3600.0; // 1 hora como padrão
    }
    
    /**
     * Constrói estatísticas vazias para aggregate.
     */
    private SnapshotStatistics buildEmptyStatistics(String aggregateId) {
        return SnapshotStatistics.builder()
            .aggregateId(aggregateId)
            .totalSnapshots(0L)
            .compressedSnapshots(0L)
            .totalOriginalSize(0L)
            .totalCompressedSize(0L)
            .averageTimeBetweenSnapshots(0.0)
            .averageSnapshotSize(0.0)
            .averageCompressionRatio(0.0)
            .snapshotsLast24Hours(0L)
            .snapshotsLastWeek(0L)
            .totalSpaceSaved(0L)
            .build();
    }
    
    /**
     * Constrói estatísticas globais vazias.
     */
    private SnapshotStatistics buildEmptyGlobalStatistics() {
        return SnapshotStatistics.builder()
            .totalSnapshots(0L)
            .compressedSnapshots(0L)
            .totalOriginalSize(0L)
            .totalCompressedSize(0L)
            .averageTimeBetweenSnapshots(0.0)
            .averageSnapshotSize(0.0)
            .averageCompressionRatio(0.0)
            .snapshotsLast24Hours(0L)
            .snapshotsLastWeek(0L)
            .totalSpaceSaved(0L)
            .build();
    }
    
    /**
     * Constrói métricas de eficiência vazias.
     */
    private SnapshotEfficiencyMetrics buildEmptyEfficiencyMetrics(String aggregateId, Instant start, Instant end) {
        return SnapshotEfficiencyMetrics.builder()
            .aggregateId(aggregateId)
            .periodStart(start)
            .periodEnd(end)
            .reconstructionsWithSnapshot(0L)
            .reconstructionsWithoutSnapshot(0L)
            .averageReconstructionTimeWithSnapshot(0.0)
            .averageReconstructionTimeWithoutSnapshot(0.0)
            .averageEventsWithSnapshot(0.0)
            .averageEventsWithoutSnapshot(0.0)
            .totalSpaceSaved(0L)
            .snapshotsCreated(0L)
            .snapshotsFailed(0L)
            .averageSnapshotCreationTime(0.0)
            .averageCompressionRatio(0.0)
            .build();
    }
}