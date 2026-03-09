package com.seguradora.hibrida.eventstore.replay.config;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventHandlerRegistry;
import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.replay.EventReplayer;
import com.seguradora.hibrida.eventstore.replay.impl.DefaultEventReplayer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração Spring para o sistema de replay de eventos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(ReplayProperties.class)
@ConditionalOnProperty(prefix = "eventstore.replay", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ReplayConfiguration {
    
    /**
     * Bean do Event Replayer.
     * 
     * @param eventStore Event Store para carregar eventos
     * @param eventBus Event Bus para publicar eventos
     * @param handlerRegistry Registry de handlers
     * @return Implementação do Event Replayer
     */
    @Bean
    public EventReplayer eventReplayer(EventStore eventStore, 
                                     EventBus eventBus, 
                                     EventHandlerRegistry handlerRegistry) {
        return new DefaultEventReplayer(eventStore, eventBus, handlerRegistry);
    }
    
    /**
     * Health indicator para o sistema de replay.
     * 
     * @param eventReplayer Event replayer
     * @return Health indicator
     */
    @Bean
    public ReplayHealthIndicator replayHealthIndicator(EventReplayer eventReplayer) {
        return new ReplayHealthIndicator(eventReplayer);
    }
    
    /**
     * Métricas para o sistema de replay.
     * 
     * @param eventReplayer Event replayer
     * @param meterRegistry Registry de métricas
     * @return Métricas do replay
     */
    @Bean
    public ReplayMetrics replayMetrics(EventReplayer eventReplayer, MeterRegistry meterRegistry) {
        return new ReplayMetrics(eventReplayer, meterRegistry);
    }
}