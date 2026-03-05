package com.seguradora.hibrida.eventbus.config;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventBusStatistics;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Métricas customizadas para o Event Bus usando Micrometer.
 * 
 * <p>Esta classe expõe métricas detalhadas sobre o funcionamento do Event Bus
 * para sistemas de monitoramento como Prometheus, Grafana, etc.
 * 
 * <p>Métricas expostas:
 * <ul>
 *   <li>Contadores de eventos publicados, processados, falhados</li>
 *   <li>Timers de tempo de processamento</li>
 *   <li>Gauges de handlers ativos e registrados</li>
 *   <li>Métricas de throughput e taxa de erro</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class EventBusMetrics implements MeterBinder {
    
    private static final Logger log = LoggerFactory.getLogger(EventBusMetrics.class);
    
    private final EventBus eventBus;
    private final AtomicReference<EventBusStatistics> statisticsRef;
    
    // Métricas
    private Counter eventsPublishedCounter;
    private Counter eventsProcessedCounter;
    private Counter eventsFailedCounter;
    private Counter eventsRetriedCounter;
    private Counter eventsDeadLetteredCounter;
    
    private Timer processingTimer;
    
    public EventBusMetrics(EventBus eventBus) {
        this.eventBus = eventBus;
        this.statisticsRef = new AtomicReference<>();
        updateStatistics();
        
        log.info("EventBus metrics initialized");
    }
    
    @Override
    public void bindTo(MeterRegistry registry) {
        log.info("Binding EventBus metrics to registry: {}", registry.getClass().getSimpleName());
        
        // Contadores
        eventsPublishedCounter = Counter.builder("eventbus.events.published")
                .description("Total number of events published")
                .register(registry);
        
        eventsProcessedCounter = Counter.builder("eventbus.events.processed")
                .description("Total number of events processed successfully")
                .register(registry);
        
        eventsFailedCounter = Counter.builder("eventbus.events.failed")
                .description("Total number of events that failed processing")
                .register(registry);
        
        eventsRetriedCounter = Counter.builder("eventbus.events.retried")
                .description("Total number of event retry attempts")
                .register(registry);
        
        eventsDeadLetteredCounter = Counter.builder("eventbus.events.deadlettered")
                .description("Total number of events sent to dead letter queue")
                .register(registry);
        
        // Timer para tempo de processamento
        processingTimer = Timer.builder("eventbus.processing.time")
                .description("Event processing time")
                .register(registry);
        
        // Gauges usando a sintaxe correta do Micrometer
        registry.gauge("eventbus.handlers.active", this, EventBusMetrics::getActiveHandlers);
        registry.gauge("eventbus.handlers.registered", this, EventBusMetrics::getRegisteredHandlers);
        registry.gauge("eventbus.success.rate", this, EventBusMetrics::getSuccessRate);
        registry.gauge("eventbus.error.rate", this, EventBusMetrics::getErrorRate);
        registry.gauge("eventbus.throughput", this, EventBusMetrics::getThroughput);
        registry.gauge("eventbus.health", this, metrics -> eventBus.isHealthy() ? 1.0 : 0.0);
        
        log.info("EventBus metrics bound successfully");
    }
    
    /**
     * Atualiza as estatísticas do Event Bus.
     * 
     * <p>Este método deve ser chamado periodicamente para manter
     * as métricas atualizadas.
     */
    public void updateStatistics() {
        try {
            EventBusStatistics stats = eventBus.getStatistics();
            EventBusStatistics previous = statisticsRef.getAndSet(stats);
            
            if (previous != null && eventsPublishedCounter != null) {
                // Incrementar contadores com a diferença
                long publishedDiff = stats.getEventsPublished() - previous.getEventsPublished();
                long processedDiff = stats.getEventsProcessed() - previous.getEventsProcessed();
                long failedDiff = stats.getEventsFailed() - previous.getEventsFailed();
                long retriedDiff = stats.getEventsRetried() - previous.getEventsRetried();
                long deadLetteredDiff = stats.getEventsDeadLettered() - previous.getEventsDeadLettered();
                
                if (publishedDiff > 0) eventsPublishedCounter.increment(publishedDiff);
                if (processedDiff > 0) eventsProcessedCounter.increment(processedDiff);
                if (failedDiff > 0) eventsFailedCounter.increment(failedDiff);
                if (retriedDiff > 0) eventsRetriedCounter.increment(retriedDiff);
                if (deadLetteredDiff > 0) eventsDeadLetteredCounter.increment(deadLetteredDiff);
            }
            
        } catch (Exception e) {
            log.error("Error updating EventBus statistics", e);
        }
    }
    
    /**
     * Registra o tempo de processamento de um evento.
     * 
     * @param processingTimeMs Tempo de processamento em milissegundos
     */
    public void recordProcessingTime(long processingTimeMs) {
        if (processingTimer != null) {
            processingTimer.record(processingTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Inicia um timer para medir tempo de processamento.
     * 
     * @return Sample do timer
     */
    public Timer.Sample startProcessingTimer() {
        return processingTimer != null ? Timer.start() : null;
    }
    
    /**
     * Para um timer de processamento.
     * 
     * @param sample Sample do timer
     */
    public void stopProcessingTimer(Timer.Sample sample) {
        if (sample != null && processingTimer != null) {
            sample.stop(processingTimer);
        }
    }
    
    /**
     * Incrementa contador de eventos publicados.
     */
    public void incrementEventsPublished() {
        if (eventsPublishedCounter != null) {
            eventsPublishedCounter.increment();
        }
    }
    
    /**
     * Incrementa contador de eventos processados.
     */
    public void incrementEventsProcessed() {
        if (eventsProcessedCounter != null) {
            eventsProcessedCounter.increment();
        }
    }
    
    /**
     * Incrementa contador de eventos falhados.
     */
    public void incrementEventsFailed() {
        if (eventsFailedCounter != null) {
            eventsFailedCounter.increment();
        }
    }
    
    /**
     * Incrementa contador de eventos reprocessados.
     */
    public void incrementEventsRetried() {
        if (eventsRetriedCounter != null) {
            eventsRetriedCounter.increment();
        }
    }
    
    /**
     * Incrementa contador de eventos enviados para dead letter queue.
     */
    public void incrementEventsDeadLettered() {
        if (eventsDeadLetteredCounter != null) {
            eventsDeadLetteredCounter.increment();
        }
    }
    
    // Métodos para Gauges
    
    private double getActiveHandlers() {
        EventBusStatistics stats = statisticsRef.get();
        return stats != null ? stats.getActiveHandlers() : 0.0;
    }
    
    private double getRegisteredHandlers() {
        // TODO: Implementar contagem de handlers registrados
        return 0.0;
    }
    
    private double getSuccessRate() {
        EventBusStatistics stats = statisticsRef.get();
        return stats != null ? stats.getSuccessRate() : 1.0;
    }
    
    private double getErrorRate() {
        EventBusStatistics stats = statisticsRef.get();
        return stats != null ? stats.getErrorRate() : 0.0;
    }
    
    private double getThroughput() {
        EventBusStatistics stats = statisticsRef.get();
        return stats != null ? stats.getThroughput() : 0.0;
    }
    
    /**
     * Obtém estatísticas atuais do Event Bus.
     * 
     * @return Estatísticas atuais
     */
    public EventBusStatistics getCurrentStatistics() {
        return statisticsRef.get();
    }
    
    /**
     * Reseta todas as métricas.
     */
    public void reset() {
        log.info("Resetting EventBus metrics");
        
        if (eventsPublishedCounter != null) {
            // Note: Micrometer counters cannot be reset, they are cumulative
            log.warn("Micrometer counters cannot be reset - they are cumulative by design");
        }
        
        // Reset statistics
        EventBusStatistics stats = statisticsRef.get();
        if (stats != null) {
            stats.reset();
        }
    }
}