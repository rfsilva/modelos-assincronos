package com.seguradora.hibrida.eventstore.replay.config;

import com.seguradora.hibrida.eventstore.replay.EventReplayer;
import com.seguradora.hibrida.eventstore.replay.ReplayProgress;
import com.seguradora.hibrida.eventstore.replay.ReplayStatistics;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Métricas para o sistema de replay de eventos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
public class ReplayMetrics {
    
    private final EventReplayer eventReplayer;
    private final MeterRegistry meterRegistry;
    
    // Contadores
    private final AtomicLong totalReplaysStarted = new AtomicLong(0);
    private final AtomicLong totalReplaysCompleted = new AtomicLong(0);
    private final AtomicLong totalReplaysFailed = new AtomicLong(0);
    private final AtomicLong totalReplaysCancelled = new AtomicLong(0);
    private final AtomicLong totalEventsReplayed = new AtomicLong(0);
    
    // Timers
    private Timer replayExecutionTimer;
    private Timer eventProcessingTimer;
    
    public ReplayMetrics(EventReplayer eventReplayer, MeterRegistry meterRegistry) {
        this.eventReplayer = eventReplayer;
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }
    
    private void initializeMetrics() {
        if (meterRegistry == null) {
            log.warn("MeterRegistry não disponível - métricas de replay desabilitadas");
            return;
        }
        
        // Gauges para estatísticas em tempo real usando a sintaxe correta
        Gauge.builder("replay.active.count", this, ReplayMetrics::getActiveReplaysCount)
            .description("Número de replays ativos")
            .register(meterRegistry);
            
        Gauge.builder("replay.success.rate", this, ReplayMetrics::getSuccessRate)
            .description("Taxa de sucesso dos replays (%)")
            .register(meterRegistry);
            
        Gauge.builder("replay.error.rate", this, ReplayMetrics::getErrorRate)
            .description("Taxa de erro dos replays (%)")
            .register(meterRegistry);
            
        Gauge.builder("replay.average.throughput", this, ReplayMetrics::getAverageThroughput)
            .description("Throughput médio (eventos/segundo)")
            .register(meterRegistry);
        
        // Contadores como gauges
        Gauge.builder("replay.total.started", totalReplaysStarted, AtomicLong::doubleValue)
            .description("Total de replays iniciados")
            .register(meterRegistry);
            
        Gauge.builder("replay.total.completed", totalReplaysCompleted, AtomicLong::doubleValue)
            .description("Total de replays concluídos")
            .register(meterRegistry);
            
        Gauge.builder("replay.total.failed", totalReplaysFailed, AtomicLong::doubleValue)
            .description("Total de replays falhados")
            .register(meterRegistry);
            
        Gauge.builder("replay.total.cancelled", totalReplaysCancelled, AtomicLong::doubleValue)
            .description("Total de replays cancelados")
            .register(meterRegistry);
            
        Gauge.builder("replay.events.total", totalEventsReplayed, AtomicLong::doubleValue)
            .description("Total de eventos reprocessados")
            .register(meterRegistry);
        
        // Timers
        replayExecutionTimer = Timer.builder("replay.execution.time")
            .description("Tempo de execução de replays")
            .register(meterRegistry);
            
        eventProcessingTimer = Timer.builder("replay.event.processing.time")
            .description("Tempo de processamento de eventos durante replay")
            .register(meterRegistry);
            
        log.info("Métricas de replay inicializadas com sucesso");
    }
    
    /**
     * Registra início de replay.
     */
    public void recordReplayStarted() {
        totalReplaysStarted.incrementAndGet();
        log.debug("Replay iniciado - Total: {}", totalReplaysStarted.get());
    }
    
    /**
     * Registra conclusão de replay com sucesso.
     */
    public void recordReplayCompleted() {
        totalReplaysCompleted.incrementAndGet();
        log.debug("Replay concluído - Total: {}", totalReplaysCompleted.get());
    }
    
    /**
     * Registra falha de replay.
     */
    public void recordReplayFailed() {
        totalReplaysFailed.incrementAndGet();
        log.debug("Replay falhado - Total: {}", totalReplaysFailed.get());
    }
    
    /**
     * Registra cancelamento de replay.
     */
    public void recordReplayCancelled() {
        totalReplaysCancelled.incrementAndGet();
        log.debug("Replay cancelado - Total: {}", totalReplaysCancelled.get());
    }
    
    /**
     * Registra eventos reprocessados.
     * 
     * @param count Número de eventos
     */
    public void recordEventsReplayed(long count) {
        totalEventsReplayed.addAndGet(count);
    }
    
    /**
     * Inicia timer de execução de replay.
     * 
     * @return Sample do timer
     */
    public Timer.Sample startReplayTimer() {
        return replayExecutionTimer != null ? Timer.start(meterRegistry) : null;
    }
    
    /**
     * Para timer de execução de replay.
     * 
     * @param sample Sample do timer
     */
    public void stopReplayTimer(Timer.Sample sample) {
        if (sample != null && replayExecutionTimer != null) {
            sample.stop(replayExecutionTimer);
        }
    }
    
    /**
     * Inicia timer de processamento de evento.
     * 
     * @return Sample do timer
     */
    public Timer.Sample startEventProcessingTimer() {
        return eventProcessingTimer != null ? Timer.start(meterRegistry) : null;
    }
    
    /**
     * Para timer de processamento de evento.
     * 
     * @param sample Sample do timer
     */
    public void stopEventProcessingTimer(Timer.Sample sample) {
        if (sample != null && eventProcessingTimer != null) {
            sample.stop(eventProcessingTimer);
        }
    }
    
    /**
     * Obtém número de replays ativos.
     * 
     * @return Número de replays ativos
     */
    public double getActiveReplaysCount() {
        try {
            List<ReplayProgress> activeReplays = eventReplayer.getActiveReplays();
            return activeReplays != null ? activeReplays.size() : 0;
        } catch (Exception e) {
            log.warn("Erro ao obter contagem de replays ativos", e);
            return 0;
        }
    }
    
    /**
     * Obtém taxa de sucesso.
     * 
     * @return Taxa de sucesso (0-100)
     */
    public double getSuccessRate() {
        try {
            ReplayStatistics stats = eventReplayer.getStatistics();
            return stats != null ? stats.getOverallSuccessRate() : 0.0;
        } catch (Exception e) {
            log.warn("Erro ao obter taxa de sucesso", e);
            return 0.0;
        }
    }
    
    /**
     * Obtém taxa de erro.
     * 
     * @return Taxa de erro (0-100)
     */
    public double getErrorRate() {
        try {
            ReplayStatistics stats = eventReplayer.getStatistics();
            return stats != null ? stats.getOverallErrorRate() : 0.0;
        } catch (Exception e) {
            log.warn("Erro ao obter taxa de erro", e);
            return 0.0;
        }
    }
    
    /**
     * Obtém throughput médio.
     * 
     * @return Throughput médio (eventos/segundo)
     */
    public double getAverageThroughput() {
        try {
            ReplayStatistics stats = eventReplayer.getStatistics();
            return stats != null ? stats.getAverageThroughput() : 0.0;
        } catch (Exception e) {
            log.warn("Erro ao obter throughput médio", e);
            return 0.0;
        }
    }
    
    /**
     * Força atualização das métricas.
     */
    public void updateMetrics() {
        try {
            ReplayStatistics stats = eventReplayer.getStatistics();
            if (stats != null) {
                // Atualiza contadores baseados nas estatísticas
                totalReplaysStarted.set(stats.getTotalReplaysExecuted());
                totalReplaysCompleted.set(stats.getSuccessfulReplays());
                totalReplaysFailed.set(stats.getFailedReplays());
                totalReplaysCancelled.set(stats.getCancelledReplays());
                totalEventsReplayed.set(stats.getTotalEventsReprocessed());
            }
        } catch (Exception e) {
            log.warn("Erro ao atualizar métricas", e);
        }
    }
    
    /**
     * Reseta todas as métricas.
     */
    public void reset() {
        totalReplaysStarted.set(0);
        totalReplaysCompleted.set(0);
        totalReplaysFailed.set(0);
        totalReplaysCancelled.set(0);
        totalEventsReplayed.set(0);
        
        log.info("Métricas de replay resetadas");
    }
}