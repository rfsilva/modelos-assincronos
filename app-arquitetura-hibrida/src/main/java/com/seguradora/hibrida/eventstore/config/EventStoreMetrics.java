package com.seguradora.hibrida.eventstore.config;

import com.seguradora.hibrida.eventstore.EventStore;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Métricas customizadas para monitoramento do Event Store.
 * 
 * Coleta métricas detalhadas sobre performance, throughput e
 * saúde do Event Store para observabilidade.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Component
public class EventStoreMetrics {
    
    private final MeterRegistry meterRegistry;
    private final EventStore eventStore;
    
    // Contadores
    private final Counter eventsWritten;
    private final Counter eventsRead;
    private final Counter concurrencyErrors;
    private final Counter serializationErrors;
    
    // Timers
    private final Timer writeTimer;
    private final Timer readTimer;
    private final Timer serializationTimer;
    
    // Gauges
    private final AtomicLong totalAggregates = new AtomicLong(0);
    private final AtomicLong totalEvents = new AtomicLong(0);
    
    public EventStoreMetrics(MeterRegistry meterRegistry, EventStore eventStore) {
        this.meterRegistry = meterRegistry;
        this.eventStore = eventStore;
        
        // Inicializa contadores
        this.eventsWritten = Counter.builder("eventstore.events.written")
                .description("Total de eventos escritos no Event Store")
                .register(meterRegistry);
                
        this.eventsRead = Counter.builder("eventstore.events.read")
                .description("Total de eventos lidos do Event Store")
                .register(meterRegistry);
                
        this.concurrencyErrors = Counter.builder("eventstore.errors.concurrency")
                .description("Total de erros de concorrência")
                .register(meterRegistry);
                
        this.serializationErrors = Counter.builder("eventstore.errors.serialization")
                .description("Total de erros de serialização")
                .register(meterRegistry);
        
        // Inicializa timers
        this.writeTimer = Timer.builder("eventstore.operations.write")
                .description("Tempo de operações de escrita")
                .register(meterRegistry);
                
        this.readTimer = Timer.builder("eventstore.operations.read")
                .description("Tempo de operações de leitura")
                .register(meterRegistry);
                
        this.serializationTimer = Timer.builder("eventstore.serialization.time")
                .description("Tempo de serialização/deserialização")
                .register(meterRegistry);
        
        // Inicializa gauges com sintaxe correta
        Gauge.builder("eventstore.aggregates.total", totalAggregates, AtomicLong::doubleValue)
                .description("Total de aggregates no Event Store")
                .register(meterRegistry);
                
        Gauge.builder("eventstore.events.total", totalEvents, AtomicLong::doubleValue)
                .description("Total de eventos no Event Store")
                .register(meterRegistry);
        
        log.info("Métricas do Event Store inicializadas");
    }
    
    /**
     * Incrementa contador de eventos escritos.
     */
    public void incrementEventsWritten(int count) {
        eventsWritten.increment(count);
        totalEvents.addAndGet(count);
    }
    
    /**
     * Incrementa contador de eventos lidos.
     */
    public void incrementEventsRead(int count) {
        eventsRead.increment(count);
    }
    
    /**
     * Incrementa contador de erros de concorrência.
     */
    public void incrementConcurrencyErrors() {
        concurrencyErrors.increment();
    }
    
    /**
     * Incrementa contador de erros de serialização.
     */
    public void incrementSerializationErrors() {
        serializationErrors.increment();
    }
    
    /**
     * Registra tempo de operação de escrita.
     */
    public Timer.Sample startWriteTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Para timer de escrita.
     */
    public void stopWriteTimer(Timer.Sample sample) {
        sample.stop(writeTimer);
    }
    
    /**
     * Registra tempo de operação de leitura.
     */
    public Timer.Sample startReadTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Para timer de leitura.
     */
    public void stopReadTimer(Timer.Sample sample) {
        sample.stop(readTimer);
    }
    
    /**
     * Registra tempo de serialização.
     */
    public Timer.Sample startSerializationTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Para timer de serialização.
     */
    public void stopSerializationTimer(Timer.Sample sample) {
        sample.stop(serializationTimer);
    }
    
    /**
     * Atualiza contador de aggregates.
     */
    public void setTotalAggregates(long count) {
        totalAggregates.set(count);
    }
    
    /**
     * Obtém total de aggregates.
     */
    public double getTotalAggregates() {
        return totalAggregates.get();
    }
    
    /**
     * Obtém total de eventos.
     */
    public double getTotalEvents() {
        return totalEvents.get();
    }
}