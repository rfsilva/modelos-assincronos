package com.seguradora.hibrida.aggregate.metrics;

import com.seguradora.hibrida.aggregate.config.AggregateProperties;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Métricas para o sistema de Aggregates.
 * 
 * <p>Coleta e expõe métricas detalhadas sobre:
 * <ul>
 *   <li>Operações de save/load de aggregates</li>
 *   <li>Performance de reconstrução de estado</li>
 *   <li>Utilização de snapshots</li>
 *   <li>Validação de regras de negócio</li>
 *   <li>Erros e exceções</li>
 * </ul>
 * 
 * <p><strong>Métricas expostas:</strong>
 * <ul>
 *   <li>aggregate_saves_total - Total de saves realizados</li>
 *   <li>aggregate_loads_total - Total de loads realizados</li>
 *   <li>aggregate_reconstruction_seconds - Tempo de reconstrução</li>
 *   <li>aggregate_validation_seconds - Tempo de validação</li>
 *   <li>aggregate_snapshots_used_total - Snapshots utilizados</li>
 *   <li>aggregate_errors_total - Total de erros</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
public class AggregateMetrics implements MeterBinder {
    
    private final AggregateProperties properties;
    private MeterRegistry registry;
    
    // Contadores
    private final AtomicLong totalSaves = new AtomicLong(0);
    private final AtomicLong totalLoads = new AtomicLong(0);
    private final AtomicLong totalSnapshots = new AtomicLong(0);
    private final AtomicLong totalValidations = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    
    // Timers
    private Timer saveTimer;
    private Timer loadTimer;
    private Timer reconstructionTimer;
    private Timer validationTimer;
    
    // Counters
    private Counter savesCounter;
    private Counter loadsCounter;
    private Counter snapshotsCounter;
    private Counter validationsCounter;
    private Counter errorsCounter;
    
    public AggregateMetrics(MeterRegistry meterRegistry, AggregateProperties properties) {
        this.properties = properties;
        bindTo(meterRegistry);
        log.info("Métricas de aggregates configuradas com prefixo: {}", properties.getMetrics().getPrefix());
    }
    
    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        String prefix = properties.getMetrics().getPrefix();
        
        // Contadores
        savesCounter = Counter.builder(prefix + "_saves_total")
                .description("Total de operações de save de aggregates")
                .register(registry);
        
        loadsCounter = Counter.builder(prefix + "_loads_total")
                .description("Total de operações de load de aggregates")
                .register(registry);
        
        snapshotsCounter = Counter.builder(prefix + "_snapshots_used_total")
                .description("Total de snapshots utilizados")
                .register(registry);
        
        validationsCounter = Counter.builder(prefix + "_validations_total")
                .description("Total de validações de regras de negócio")
                .register(registry);
        
        errorsCounter = Counter.builder(prefix + "_errors_total")
                .description("Total de erros em operações de aggregates")
                .register(registry);
        
        // Timers
        saveTimer = Timer.builder(prefix + "_save_seconds")
                .description("Tempo de execução de operações de save")
                .register(registry);
        
        loadTimer = Timer.builder(prefix + "_load_seconds")
                .description("Tempo de execução de operações de load")
                .register(registry);
        
        reconstructionTimer = Timer.builder(prefix + "_reconstruction_seconds")
                .description("Tempo de reconstrução de estado de aggregates")
                .register(registry);
        
        validationTimer = Timer.builder(prefix + "_validation_seconds")
                .description("Tempo de validação de regras de negócio")
                .register(registry);
        
        // Gauges
        Gauge.builder(prefix + "_active_count", this, AggregateMetrics::getActiveAggregatesCount)
                .description("Número de aggregates ativos em memória")
                .register(registry);
    }
    
    /**
     * Inicia timer para operação de save.
     */
    public Timer.Sample startSaveTimer() {
        return Timer.start(registry);
    }
    
    /**
     * Para timer de save e registra métrica.
     */
    public void stopSaveTimer(Timer.Sample sample) {
        sample.stop(saveTimer);
        savesCounter.increment();
        totalSaves.incrementAndGet();
    }
    
    /**
     * Inicia timer para operação de load.
     */
    public Timer.Sample startLoadTimer() {
        return Timer.start(registry);
    }
    
    /**
     * Para timer de load e registra métrica.
     */
    public void stopLoadTimer(Timer.Sample sample) {
        sample.stop(loadTimer);
        loadsCounter.increment();
        totalLoads.incrementAndGet();
    }
    
    /**
     * Inicia timer para reconstrução de estado.
     */
    public Timer.Sample startReconstructionTimer() {
        return Timer.start(registry);
    }
    
    /**
     * Para timer de reconstrução e registra métrica.
     */
    public void stopReconstructionTimer(Timer.Sample sample) {
        sample.stop(reconstructionTimer);
    }
    
    /**
     * Inicia timer para validação.
     */
    public Timer.Sample startValidationTimer() {
        return Timer.start(registry);
    }
    
    /**
     * Para timer de validação e registra métrica.
     */
    public void stopValidationTimer(Timer.Sample sample) {
        sample.stop(validationTimer);
        validationsCounter.increment();
        totalValidations.incrementAndGet();
    }
    
    /**
     * Incrementa contador de snapshots utilizados.
     */
    public void incrementSnapshotsUsed() {
        snapshotsCounter.increment();
        totalSnapshots.incrementAndGet();
    }
    
    /**
     * Incrementa contador de erros por tipo.
     */
    public void incrementErrors(String errorType) {
        Counter.builder(properties.getMetrics().getPrefix() + "_errors_total")
                .tag("type", errorType)
                .register(registry)
                .increment();
        totalErrors.incrementAndGet();
        
        if (properties.getMetrics().isDetailedLogging()) {
            log.debug("Erro registrado nas métricas: {}", errorType);
        }
    }
    
    /**
     * Registra tempo de operação customizada.
     */
    public void recordCustomTimer(String operation, long timeMs) {
        Timer.builder(properties.getMetrics().getPrefix() + "_" + operation + "_seconds")
                .description("Tempo de execução de " + operation)
                .register(registry)
                .record(timeMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Obtém estatísticas atuais.
     */
    public MetricsStatistics getStatistics() {
        return MetricsStatistics.builder()
                .totalSaves(totalSaves.get())
                .totalLoads(totalLoads.get())
                .totalSnapshots(totalSnapshots.get())
                .totalValidations(totalValidations.get())
                .totalErrors(totalErrors.get())
                .averageSaveTime(saveTimer.mean(TimeUnit.MILLISECONDS))
                .averageLoadTime(loadTimer.mean(TimeUnit.MILLISECONDS))
                .averageReconstructionTime(reconstructionTimer.mean(TimeUnit.MILLISECONDS))
                .averageValidationTime(validationTimer.mean(TimeUnit.MILLISECONDS))
                .build();
    }
    
    /**
     * Obtém número de aggregates ativos (placeholder - implementação específica).
     */
    private double getActiveAggregatesCount() {
        // Esta implementação seria específica para cada caso
        // Por enquanto retorna 0
        return 0.0;
    }
    
    /**
     * Classe para estatísticas de métricas.
     */
    public static class MetricsStatistics {
        private final long totalSaves;
        private final long totalLoads;
        private final long totalSnapshots;
        private final long totalValidations;
        private final long totalErrors;
        private final double averageSaveTime;
        private final double averageLoadTime;
        private final double averageReconstructionTime;
        private final double averageValidationTime;
        
        private MetricsStatistics(Builder builder) {
            this.totalSaves = builder.totalSaves;
            this.totalLoads = builder.totalLoads;
            this.totalSnapshots = builder.totalSnapshots;
            this.totalValidations = builder.totalValidations;
            this.totalErrors = builder.totalErrors;
            this.averageSaveTime = builder.averageSaveTime;
            this.averageLoadTime = builder.averageLoadTime;
            this.averageReconstructionTime = builder.averageReconstructionTime;
            this.averageValidationTime = builder.averageValidationTime;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters
        public long getTotalSaves() { return totalSaves; }
        public long getTotalLoads() { return totalLoads; }
        public long getTotalSnapshots() { return totalSnapshots; }
        public long getTotalValidations() { return totalValidations; }
        public long getTotalErrors() { return totalErrors; }
        public double getAverageSaveTime() { return averageSaveTime; }
        public double getAverageLoadTime() { return averageLoadTime; }
        public double getAverageReconstructionTime() { return averageReconstructionTime; }
        public double getAverageValidationTime() { return averageValidationTime; }
        
        public static class Builder {
            private long totalSaves;
            private long totalLoads;
            private long totalSnapshots;
            private long totalValidations;
            private long totalErrors;
            private double averageSaveTime;
            private double averageLoadTime;
            private double averageReconstructionTime;
            private double averageValidationTime;
            
            public Builder totalSaves(long totalSaves) {
                this.totalSaves = totalSaves;
                return this;
            }
            
            public Builder totalLoads(long totalLoads) {
                this.totalLoads = totalLoads;
                return this;
            }
            
            public Builder totalSnapshots(long totalSnapshots) {
                this.totalSnapshots = totalSnapshots;
                return this;
            }
            
            public Builder totalValidations(long totalValidations) {
                this.totalValidations = totalValidations;
                return this;
            }
            
            public Builder totalErrors(long totalErrors) {
                this.totalErrors = totalErrors;
                return this;
            }
            
            public Builder averageSaveTime(double averageSaveTime) {
                this.averageSaveTime = averageSaveTime;
                return this;
            }
            
            public Builder averageLoadTime(double averageLoadTime) {
                this.averageLoadTime = averageLoadTime;
                return this;
            }
            
            public Builder averageReconstructionTime(double averageReconstructionTime) {
                this.averageReconstructionTime = averageReconstructionTime;
                return this;
            }
            
            public Builder averageValidationTime(double averageValidationTime) {
                this.averageValidationTime = averageValidationTime;
                return this;
            }
            
            public MetricsStatistics build() {
                return new MetricsStatistics(this);
            }
        }
    }
}