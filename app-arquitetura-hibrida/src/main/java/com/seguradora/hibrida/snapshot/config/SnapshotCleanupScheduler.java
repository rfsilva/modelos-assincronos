package com.seguradora.hibrida.snapshot.config;

import com.seguradora.hibrida.snapshot.SnapshotProperties;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Scheduler para limpeza automática de snapshots antigos.
 * 
 * <p>Executa tarefas de manutenção como:
 * <ul>
 *   <li>Limpeza de snapshots antigos</li>
 *   <li>Otimização de armazenamento</li>
 *   <li>Relatórios de uso</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class SnapshotCleanupScheduler {
    
    private final SnapshotStore snapshotStore;
    private final SnapshotProperties snapshotProperties;
    
    /**
     * Executa limpeza automática de snapshots antigos.
     * 
     * Executado a cada hora configurada (padrão: 24 horas).
     */
    @Scheduled(fixedRateString = "#{${snapshot.cleanup-interval-hours:24} * 60 * 60 * 1000}")
    public void cleanupOldSnapshots() {
        if (!snapshotProperties.isAutoCleanupEnabled()) {
            log.debug("Auto cleanup is disabled, skipping cleanup");
            return;
        }
        
        try {
            log.info("Starting scheduled cleanup of old snapshots");
            
            long startTime = System.currentTimeMillis();
            int deletedCount = snapshotStore.cleanupAllOldSnapshots(
                snapshotProperties.getMaxSnapshotsPerAggregate()
            );
            long duration = System.currentTimeMillis() - startTime;
            
            if (deletedCount > 0) {
                log.info("Cleanup completed: removed {} old snapshots in {}ms", deletedCount, duration);
            } else {
                log.debug("Cleanup completed: no old snapshots to remove ({}ms)", duration);
            }
            
        } catch (Exception e) {
            log.error("Failed to execute scheduled cleanup of old snapshots", e);
        }
    }
    
    /**
     * Gera relatório de uso de snapshots.
     * 
     * Executado diariamente às 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateUsageReport() {
        if (!snapshotProperties.isMetricsEnabled()) {
            return;
        }
        
        try {
            log.info("Generating daily snapshot usage report");
            
            var globalStats = snapshotStore.getGlobalStatistics();
            
            log.info("=== SNAPSHOT USAGE REPORT ===");
            log.info("Total snapshots: {}", globalStats.getTotalSnapshots());
            log.info("Compressed snapshots: {} ({}%)", 
                    globalStats.getCompressedSnapshots(),
                    Math.round(globalStats.getCompressionPercentage()));
            log.info("Total storage used: {} MB", 
                    globalStats.getTotalCompressedSize() / (1024 * 1024));
            log.info("Space saved by compression: {} MB", 
                    globalStats.getTotalSpaceSaved() / (1024 * 1024));
            log.info("Overall compression ratio: {}%", 
                    Math.round(globalStats.getOverallCompressionRatio() * 100));
            log.info("Snapshots created last 24h: {}", globalStats.getSnapshotsLast24Hours());
            log.info("Snapshots created last week: {}", globalStats.getSnapshotsLastWeek());
            log.info("Storage efficiency: {}%", 
                    Math.round(globalStats.getStorageEfficiency() * 100));
            log.info("=== END REPORT ===");
            
        } catch (Exception e) {
            log.error("Failed to generate usage report", e);
        }
    }
    
    /**
     * Verifica saúde do sistema de snapshots.
     * 
     * Executado a cada 30 minutos.
     */
    @Scheduled(fixedRate = 1800000) // 30 minutos
    public void healthCheck() {
        if (!snapshotProperties.isHealthCheckEnabled()) {
            return;
        }
        
        try {
            var globalStats = snapshotStore.getGlobalStatistics();
            
            // Verificar se há atividade recente
            long recentSnapshots = globalStats.getSnapshotsLast24Hours();
            if (recentSnapshots == 0) {
                log.warn("No snapshots created in the last 24 hours - system may be inactive");
            }
            
            // Verificar eficiência de compressão
            double compressionRatio = globalStats.getOverallCompressionRatio();
            if (compressionRatio < 0.1 && globalStats.getCompressedSnapshots() > 0) {
                log.warn("Low compression efficiency: {}% - consider reviewing compression settings", 
                        Math.round(compressionRatio * 100));
            }
            
            // Verificar crescimento de dados
            double growthRate = globalStats.getSnapshotGrowthRate();
            if (growthRate > 1000) { // Mais de 1000 snapshots por dia
                log.warn("High snapshot growth rate: {} snapshots/day - monitor storage capacity", 
                        Math.round(growthRate));
            }
            
            log.debug("Snapshot system health check completed successfully");
            
        } catch (Exception e) {
            log.error("Snapshot system health check failed", e);
        }
    }
    
    /**
     * Otimização de performance.
     * 
     * Executado semanalmente aos domingos às 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void performanceOptimization() {
        try {
            log.info("Starting weekly performance optimization");
            
            // Aqui poderiam ser executadas tarefas como:
            // - Reindexação de tabelas
            // - Análise de estatísticas do banco
            // - Compactação de dados
            // - Otimização de consultas
            
            log.info("Performance optimization completed");
            
        } catch (Exception e) {
            log.error("Failed to execute performance optimization", e);
        }
    }
}