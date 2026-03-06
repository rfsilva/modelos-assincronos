package com.seguradora.hibrida.aggregate.config;

import com.seguradora.hibrida.aggregate.metrics.AggregateMetrics;
import com.seguradora.hibrida.aggregate.health.AggregateHealthIndicator;
import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração Spring para o sistema de Aggregates.
 * 
 * <p>Esta configuração:
 * <ul>
 *   <li>Configura beans necessários para o sistema de aggregates</li>
 *   <li>Habilita métricas e monitoramento</li>
 *   <li>Configura health checks</li>
 *   <li>Define propriedades customizáveis</li>
 * </ul>
 * 
 * <p><strong>Propriedades configuráveis:</strong>
 * <pre>
 * aggregate:
 *   metrics:
 *     enabled: true
 *   health-check:
 *     enabled: true
 *   validation:
 *     enabled: true
 *     fail-fast: false
 *   performance:
 *     cache-handlers: true
 *     parallel-validation: false
 * </pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AggregateProperties.class)
public class AggregateConfiguration {
    
    /**
     * Configura métricas para o sistema de aggregates.
     * 
     * @param meterRegistry Registry de métricas do Micrometer
     * @param properties Propriedades de configuração
     * @return Bean de métricas de aggregates
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "aggregate.metrics", 
            name = "enabled", 
            havingValue = "true", 
            matchIfMissing = true
    )
    public AggregateMetrics aggregateMetrics(MeterRegistry meterRegistry, 
                                           AggregateProperties properties) {
        log.info("Configurando métricas de aggregates");
        return new AggregateMetrics(meterRegistry, properties);
    }
    
    /**
     * Configura health indicator para o sistema de aggregates.
     * 
     * @param eventStore Event Store para verificação de saúde
     * @param snapshotStore Snapshot Store para verificação de saúde
     * @param properties Propriedades de configuração
     * @return Bean de health indicator
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "aggregate.health-check", 
            name = "enabled", 
            havingValue = "true", 
            matchIfMissing = true
    )
    public HealthIndicator aggregateHealthIndicator(EventStore eventStore,
                                                  SnapshotStore snapshotStore,
                                                  AggregateProperties properties) {
        log.info("Configurando health indicator de aggregates");
        return new AggregateHealthIndicator(eventStore, snapshotStore, properties);
    }
    
    /**
     * Configura propriedades padrão se não especificadas.
     */
    @Bean
    public AggregateProperties aggregateProperties() {
        return new AggregateProperties();
    }
}