package com.seguradora.hibrida.snapshot.config;

import com.seguradora.hibrida.snapshot.SnapshotStatistics;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import io.micrometer.core.instrument.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Métricas customizadas para o sistema de snapshots.
 * 
 * <p>Coleta e expõe métricas importantes como:
 * <ul>
 *   <li>Número total de snapshots</li>
 *   <li>Taxa de compressão</li>
 *   <li>Tempos de operação</li>
 *   <li>Eficiência de armazenamento</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class SnapshotMetrics {
    
    private final MeterRegistry meterRegistry;
    private final SnapshotStore snapshotStore;
    
    // Contadores
    private Counter snapshotsCreated;
    private Counter snapshotsFailed;
    private Counter snapshotsLoaded;
    private Counter snapshotsDeleted;
    
    // Timers
    private Timer snapshotCreationTimer;
    private Timer snapshotLoadTimer;
    private Timer compressionTimer;
    
    // Gauges
    private final AtomicLong totalSnapshots = new AtomicLong(0);
    private final AtomicLong totalAggregates = new AtomicLong(0);
    private final AtomicLong totalStorageUsed = new AtomicLong(0);
    private final AtomicLong totalSpaceSaved = new AtomicLong(0);
    
    /**
     * Inicializa os gauges.
     */
    public void initializeGauges() {
        // Inicializar contadores
        snapshotsCreated = Counter.builder("snapshots.created.total")
                .description("Total number of snapshots created")
                .register(meterRegistry);
        
        snapshotsFailed = Counter.builder("snapshots.failed.total")
                .description("Total number of failed snapshot operations")
                .register(meterRegistry);
        
        snapshotsLoaded = Counter.builder("snapshots.loaded.total")
                .description("Total number of snapshots loaded")
                .register(meterRegistry);
        
        snapshotsDeleted = Counter.builder("snapshots.deleted.total")
                .description("Total number of snapshots deleted")
                .register(meterRegistry);
        
        // Inicializar timers
        snapshotCreationTimer = Timer.builder("snapshots.creation.time")
                .description("Time taken to create snapshots")
                .register(meterRegistry);
        
        snapshotLoadTimer = Timer.builder("snapshots.load.time")
                .description("Time taken to load snapshots")
                .register(meterRegistry);
        
        compressionTimer = Timer.builder("snapshots.compression.time")
                .description("Time taken to compress snapshots")
                .register(meterRegistry);
        
        // Registrar gauges usando supplier functions
        meterRegistry.gauge("snapshots.total", totalSnapshots, AtomicLong::doubleValue);
        meterRegistry.gauge("snapshots.aggregates.total", totalAggregates, AtomicLong::doubleValue);
        meterRegistry.gauge("snapshots.storage.used.bytes", totalStorageUsed, AtomicLong::doubleValue);
        meterRegistry.gauge("snapshots.storage.saved.bytes", totalSpaceSaved, AtomicLong::doubleValue);
        
        // Gauge para taxa de compressão
        meterRegistry.gauge("snapshots.compression.ratio", this, SnapshotMetrics::getCompressionRatio);
        
        // Gauge para eficiência de armazenamento
        meterRegistry.gauge("snapshots.storage.efficiency", this, SnapshotMetrics::getStorageEfficiency);
    }
    
    /**
     * Inicia timer para criação de snapshot.
     * 
     * @return Sample do timer
     */
    public Timer.Sample startCreationTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Para timer de criação e incrementa contador.
     * 
     * @param sample Sample do timer
     */
    public void stopCreationTimer(Timer.Sample sample) {
        if (snapshotCreationTimer != null) {
            sample.stop(snapshotCreationTimer);
        }
        if (snapshotsCreated != null) {
            snapshotsCreated.increment();
        }
    }
    
    /**
     * Inicia timer para carregamento de snapshot.
     * 
     * @return Sample do timer
     */
    public Timer.Sample startLoadTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Para timer de carregamento e incrementa contador.
     * 
     * @param sample Sample do timer
     */
    public void stopLoadTimer(Timer.Sample sample) {
        if (snapshotLoadTimer != null) {
            sample.stop(snapshotLoadTimer);
        }
        if (snapshotsLoaded != null) {
            snapshotsLoaded.increment();
        }
    }
    
    /**
     * Inicia timer para compressão.
     * 
     * @return Sample do timer
     */
    public Timer.Sample startCompressionTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Para timer de compressão.
     * 
     * @param sample Sample do timer
     */
    public void stopCompressionTimer(Timer.Sample sample) {
        if (compressionTimer != null) {
            sample.stop(compressionTimer);
        }
    }
    
    /**
     * Incrementa contador de falhas.
     */
    public void incrementFailures() {
        if (snapshotsFailed != null) {
            snapshotsFailed.increment();
        }
    }
    
    /**
     * Incrementa contador de snapshots deletados.
     * 
     * @param count Número de snapshots deletados
     */
    public void incrementDeleted(int count) {
        if (snapshotsDeleted != null) {
            snapshotsDeleted.increment(count);
        }
    }
    
    /**
     * Atualiza métricas de armazenamento.
     * 
     * @param storageUsed Armazenamento usado em bytes
     * @param spaceSaved Espaço economizado em bytes
     */
    public void updateStorageMetrics(long storageUsed, long spaceSaved) {
        totalStorageUsed.set(storageUsed);
        totalSpaceSaved.set(spaceSaved);
    }
    
    /**
     * Atualiza contadores totais.
     * 
     * @param snapshots Total de snapshots
     * @param aggregates Total de aggregates
     */
    public void updateTotals(long snapshots, long aggregates) {
        totalSnapshots.set(snapshots);
        totalAggregates.set(aggregates);
    }
    
    /**
     * Obtém taxa de compressão atual.
     * 
     * @return Taxa de compressão (0.0 a 1.0)
     */
    private double getCompressionRatio() {
        try {
            SnapshotStatistics stats = snapshotStore.getGlobalStatistics();
            return stats.getOverallCompressionRatio();
        } catch (Exception e) {
            log.warn("Failed to get compression ratio: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Obtém eficiência de armazenamento atual.
     * 
     * @return Eficiência (0.0 a 1.0)
     */
    private double getStorageEfficiency() {
        try {
            SnapshotStatistics stats = snapshotStore.getGlobalStatistics();
            return stats.getStorageEfficiency();
        } catch (Exception e) {
            log.warn("Failed to get storage efficiency: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Atualiza métricas periodicamente.
     */
    @Scheduled(fixedRate = 60000) // A cada minuto
    public void updateMetrics() {
        try {
            SnapshotStatistics stats = snapshotStore.getGlobalStatistics();
            
            updateTotals(stats.getTotalSnapshots(), 0); // Total de aggregates seria calculado separadamente
            updateStorageMetrics(stats.getTotalCompressedSize(), stats.getTotalSpaceSaved());
            
            log.debug("Updated snapshot metrics: {} snapshots, {} bytes used, {} bytes saved", 
                     stats.getTotalSnapshots(), stats.getTotalCompressedSize(), stats.getTotalSpaceSaved());
                     
        } catch (Exception e) {
            log.warn("Failed to update snapshot metrics: {}", e.getMessage());
        }
    }
    
    /**
     * Obtém estatísticas atuais das métricas.
     * 
     * @return Estatísticas das métricas
     */
    public MetricsStatistics getMetricsStatistics() {
        return MetricsStatistics.builder()
                .totalSnapshots(totalSnapshots.get())
                .totalAggregates(totalAggregates.get())
                .totalStorageUsed(totalStorageUsed.get())
                .totalSpaceSaved(totalSpaceSaved.get())
                .snapshotsCreated(snapshotsCreated != null ? snapshotsCreated.count() : 0.0)
                .snapshotsFailed(snapshotsFailed != null ? snapshotsFailed.count() : 0.0)
                .snapshotsLoaded(snapshotsLoaded != null ? snapshotsLoaded.count() : 0.0)
                .snapshotsDeleted(snapshotsDeleted != null ? snapshotsDeleted.count() : 0.0)
                .averageCreationTime(snapshotCreationTimer != null ? snapshotCreationTimer.mean(TimeUnit.MILLISECONDS) : 0.0)
                .averageLoadTime(snapshotLoadTimer != null ? snapshotLoadTimer.mean(TimeUnit.MILLISECONDS) : 0.0)
                .averageCompressionTime(compressionTimer != null ? compressionTimer.mean(TimeUnit.MILLISECONDS) : 0.0)
                .compressionRatio(getCompressionRatio())
                .storageEfficiency(getStorageEfficiency())
                .build();
    }
    
    /**
     * Estatísticas das métricas.
     */
    public static class MetricsStatistics {
        public final long totalSnapshots;
        public final long totalAggregates;
        public final long totalStorageUsed;
        public final long totalSpaceSaved;
        public final double snapshotsCreated;
        public final double snapshotsFailed;
        public final double snapshotsLoaded;
        public final double snapshotsDeleted;
        public final double averageCreationTime;
        public final double averageLoadTime;
        public final double averageCompressionTime;
        public final double compressionRatio;
        public final double storageEfficiency;
        
        private MetricsStatistics(Builder builder) {
            this.totalSnapshots = builder.totalSnapshots;
            this.totalAggregates = builder.totalAggregates;
            this.totalStorageUsed = builder.totalStorageUsed;
            this.totalSpaceSaved = builder.totalSpaceSaved;
            this.snapshotsCreated = builder.snapshotsCreated;
            this.snapshotsFailed = builder.snapshotsFailed;
            this.snapshotsLoaded = builder.snapshotsLoaded;
            this.snapshotsDeleted = builder.snapshotsDeleted;
            this.averageCreationTime = builder.averageCreationTime;
            this.averageLoadTime = builder.averageLoadTime;
            this.averageCompressionTime = builder.averageCompressionTime;
            this.compressionRatio = builder.compressionRatio;
            this.storageEfficiency = builder.storageEfficiency;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private long totalSnapshots;
            private long totalAggregates;
            private long totalStorageUsed;
            private long totalSpaceSaved;
            private double snapshotsCreated;
            private double snapshotsFailed;
            private double snapshotsLoaded;
            private double snapshotsDeleted;
            private double averageCreationTime;
            private double averageLoadTime;
            private double averageCompressionTime;
            private double compressionRatio;
            private double storageEfficiency;
            
            public Builder totalSnapshots(long totalSnapshots) {
                this.totalSnapshots = totalSnapshots;
                return this;
            }
            
            public Builder totalAggregates(long totalAggregates) {
                this.totalAggregates = totalAggregates;
                return this;
            }
            
            public Builder totalStorageUsed(long totalStorageUsed) {
                this.totalStorageUsed = totalStorageUsed;
                return this;
            }
            
            public Builder totalSpaceSaved(long totalSpaceSaved) {
                this.totalSpaceSaved = totalSpaceSaved;
                return this;
            }
            
            public Builder snapshotsCreated(double snapshotsCreated) {
                this.snapshotsCreated = snapshotsCreated;
                return this;
            }
            
            public Builder snapshotsFailed(double snapshotsFailed) {
                this.snapshotsFailed = snapshotsFailed;
                return this;
            }
            
            public Builder snapshotsLoaded(double snapshotsLoaded) {
                this.snapshotsLoaded = snapshotsLoaded;
                return this;
            }
            
            public Builder snapshotsDeleted(double snapshotsDeleted) {
                this.snapshotsDeleted = snapshotsDeleted;
                return this;
            }
            
            public Builder averageCreationTime(double averageCreationTime) {
                this.averageCreationTime = averageCreationTime;
                return this;
            }
            
            public Builder averageLoadTime(double averageLoadTime) {
                this.averageLoadTime = averageLoadTime;
                return this;
            }
            
            public Builder averageCompressionTime(double averageCompressionTime) {
                this.averageCompressionTime = averageCompressionTime;
                return this;
            }
            
            public Builder compressionRatio(double compressionRatio) {
                this.compressionRatio = compressionRatio;
                return this;
            }
            
            public Builder storageEfficiency(double storageEfficiency) {
                this.storageEfficiency = storageEfficiency;
                return this;
            }
            
            public MetricsStatistics build() {
                return new MetricsStatistics(this);
            }
        }
    }
}