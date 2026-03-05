package com.seguradora.hibrida.eventbus;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Classe para coleta e exposição de estatísticas do Event Bus.
 * 
 * <p>Mantém métricas detalhadas sobre o funcionamento do Event Bus,
 * incluindo contadores, tempos de execução e taxas de erro.
 * 
 * <p>Todas as operações são thread-safe e otimizadas para alta concorrência.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class EventBusStatistics {
    
    // Contadores gerais
    private final AtomicLong eventsPublished = new AtomicLong(0);
    private final AtomicLong eventsProcessed = new AtomicLong(0);
    private final AtomicLong eventsFailed = new AtomicLong(0);
    private final AtomicLong eventsRetried = new AtomicLong(0);
    private final AtomicLong eventsDeadLettered = new AtomicLong(0);
    
    // Contadores por tipo de evento
    private final Map<String, AtomicLong> eventsByType = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> failuresByType = new ConcurrentHashMap<>();
    
    // Métricas de tempo
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicLong minProcessingTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxProcessingTime = new AtomicLong(0);
    
    // Métricas de throughput
    private volatile Instant startTime = Instant.now();
    private volatile Instant lastEventTime = Instant.now();
    
    // Handlers ativos
    private final AtomicLong activeHandlers = new AtomicLong(0);
    private final AtomicLong maxConcurrentHandlers = new AtomicLong(0);
    
    /**
     * Registra a publicação de um evento.
     * 
     * @param eventType Tipo do evento
     */
    public void recordEventPublished(String eventType) {
        eventsPublished.incrementAndGet();
        eventsByType.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        lastEventTime = Instant.now();
    }
    
    /**
     * Registra o processamento bem-sucedido de um evento.
     * 
     * @param eventType Tipo do evento
     * @param processingTimeMs Tempo de processamento em milissegundos
     */
    public void recordEventProcessed(String eventType, long processingTimeMs) {
        eventsProcessed.incrementAndGet();
        
        // Atualizar métricas de tempo
        totalProcessingTime.addAndGet(processingTimeMs);
        updateMinProcessingTime(processingTimeMs);
        updateMaxProcessingTime(processingTimeMs);
        
        lastEventTime = Instant.now();
    }
    
    /**
     * Registra a falha no processamento de um evento.
     * 
     * @param eventType Tipo do evento
     * @param isRetryable Se o evento pode ser reprocessado
     */
    public void recordEventFailed(String eventType, boolean isRetryable) {
        eventsFailed.incrementAndGet();
        failuresByType.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        
        if (!isRetryable) {
            eventsDeadLettered.incrementAndGet();
        }
        
        lastEventTime = Instant.now();
    }
    
    /**
     * Registra uma tentativa de retry de um evento.
     * 
     * @param eventType Tipo do evento
     */
    public void recordEventRetried(String eventType) {
        eventsRetried.incrementAndGet();
        lastEventTime = Instant.now();
    }
    
    /**
     * Registra o início do processamento de um handler.
     */
    public void recordHandlerStarted() {
        long current = activeHandlers.incrementAndGet();
        updateMaxConcurrentHandlers(current);
    }
    
    /**
     * Registra o fim do processamento de um handler.
     */
    public void recordHandlerFinished() {
        activeHandlers.decrementAndGet();
    }
    
    /**
     * Obtém o número total de eventos publicados.
     */
    public long getEventsPublished() {
        return eventsPublished.get();
    }
    
    /**
     * Obtém o número total de eventos processados com sucesso.
     */
    public long getEventsProcessed() {
        return eventsProcessed.get();
    }
    
    /**
     * Obtém o número total de eventos que falharam.
     */
    public long getEventsFailed() {
        return eventsFailed.get();
    }
    
    /**
     * Obtém o número total de eventos reprocessados.
     */
    public long getEventsRetried() {
        return eventsRetried.get();
    }
    
    /**
     * Obtém o número total de eventos enviados para dead letter queue.
     */
    public long getEventsDeadLettered() {
        return eventsDeadLettered.get();
    }
    
    /**
     * Obtém a taxa de sucesso (0.0 a 1.0).
     */
    public double getSuccessRate() {
        long total = eventsPublished.get();
        if (total == 0) {
            return 1.0;
        }
        return (double) eventsProcessed.get() / total;
    }
    
    /**
     * Obtém a taxa de erro (0.0 a 1.0).
     */
    public double getErrorRate() {
        long total = eventsPublished.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) eventsFailed.get() / total;
    }
    
    /**
     * Obtém o tempo médio de processamento em milissegundos.
     */
    public double getAverageProcessingTime() {
        long processed = eventsProcessed.get();
        if (processed == 0) {
            return 0.0;
        }
        return (double) totalProcessingTime.get() / processed;
    }
    
    /**
     * Obtém o tempo mínimo de processamento em milissegundos.
     */
    public long getMinProcessingTime() {
        long min = minProcessingTime.get();
        return min == Long.MAX_VALUE ? 0 : min;
    }
    
    /**
     * Obtém o tempo máximo de processamento em milissegundos.
     */
    public long getMaxProcessingTime() {
        return maxProcessingTime.get();
    }
    
    /**
     * Obtém o throughput em eventos por segundo.
     */
    public double getThroughput() {
        long durationSeconds = java.time.Duration.between(startTime, Instant.now()).getSeconds();
        if (durationSeconds == 0) {
            return 0.0;
        }
        return (double) eventsProcessed.get() / durationSeconds;
    }
    
    /**
     * Obtém o número de handlers atualmente ativos.
     */
    public long getActiveHandlers() {
        return activeHandlers.get();
    }
    
    /**
     * Obtém o número máximo de handlers concorrentes já registrado.
     */
    public long getMaxConcurrentHandlers() {
        return maxConcurrentHandlers.get();
    }
    
    /**
     * Obtém estatísticas por tipo de evento.
     */
    public Map<String, Long> getEventsByType() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        eventsByType.forEach((type, count) -> result.put(type, count.get()));
        return result;
    }
    
    /**
     * Obtém falhas por tipo de evento.
     */
    public Map<String, Long> getFailuresByType() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        failuresByType.forEach((type, count) -> result.put(type, count.get()));
        return result;
    }
    
    /**
     * Obtém o timestamp de início das estatísticas.
     */
    public Instant getStartTime() {
        return startTime;
    }
    
    /**
     * Obtém o timestamp do último evento processado.
     */
    public Instant getLastEventTime() {
        return lastEventTime;
    }
    
    /**
     * Reseta todas as estatísticas.
     */
    public void reset() {
        eventsPublished.set(0);
        eventsProcessed.set(0);
        eventsFailed.set(0);
        eventsRetried.set(0);
        eventsDeadLettered.set(0);
        
        eventsByType.clear();
        failuresByType.clear();
        
        totalProcessingTime.set(0);
        minProcessingTime.set(Long.MAX_VALUE);
        maxProcessingTime.set(0);
        
        activeHandlers.set(0);
        maxConcurrentHandlers.set(0);
        
        startTime = Instant.now();
        lastEventTime = Instant.now();
    }
    
    /**
     * Atualiza o tempo mínimo de processamento de forma thread-safe.
     */
    private void updateMinProcessingTime(long processingTime) {
        minProcessingTime.updateAndGet(current -> Math.min(current, processingTime));
    }
    
    /**
     * Atualiza o tempo máximo de processamento de forma thread-safe.
     */
    private void updateMaxProcessingTime(long processingTime) {
        maxProcessingTime.updateAndGet(current -> Math.max(current, processingTime));
    }
    
    /**
     * Atualiza o número máximo de handlers concorrentes de forma thread-safe.
     */
    private void updateMaxConcurrentHandlers(long current) {
        maxConcurrentHandlers.updateAndGet(max -> Math.max(max, current));
    }
    
    @Override
    public String toString() {
        return String.format(
            "EventBusStatistics{published=%d, processed=%d, failed=%d, retried=%d, " +
            "deadLettered=%d, successRate=%.2f%%, errorRate=%.2f%%, avgTime=%.2fms, " +
            "throughput=%.2f/s, activeHandlers=%d}",
            getEventsPublished(), getEventsProcessed(), getEventsFailed(), 
            getEventsRetried(), getEventsDeadLettered(),
            getSuccessRate() * 100, getErrorRate() * 100, getAverageProcessingTime(),
            getThroughput(), getActiveHandlers()
        );
    }
}