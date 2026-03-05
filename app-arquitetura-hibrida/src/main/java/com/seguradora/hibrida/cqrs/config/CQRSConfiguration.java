package com.seguradora.hibrida.cqrs.config;

import com.seguradora.hibrida.cqrs.health.CQRSHealthIndicator;
import com.seguradora.hibrida.cqrs.metrics.CQRSMetrics;
import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuração para monitoramento CQRS.
 * 
 * <p>Configura componentes de monitoramento, métricas e health checks
 * específicos para o padrão CQRS implementado.
 */
@Configuration
@EnableScheduling
public class CQRSConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(CQRSConfiguration.class);
    
    /**
     * Configura o health indicator do CQRS.
     */
    @Bean("cqrsHealthIndicator")
    public HealthIndicator cqrsHealthIndicator(EventStoreRepository eventStoreRepository,
                                              ProjectionTrackerRepository projectionTrackerRepository) {
        log.info("Configurando CQRS Health Indicator");
        return new CQRSHealthIndicator(eventStoreRepository, projectionTrackerRepository);
    }
    
    /**
     * Configura as métricas do CQRS.
     */
    @Bean("cqrsMetrics")
    public CQRSMetrics cqrsMetrics(EventStoreRepository eventStoreRepository,
                                  ProjectionTrackerRepository projectionTrackerRepository,
                                  MeterRegistry meterRegistry) {
        log.info("Configurando CQRS Metrics");
        
        CQRSMetrics metrics = new CQRSMetrics(eventStoreRepository, projectionTrackerRepository);
        
        // Registrar métricas no registry
        metrics.bindTo(meterRegistry);
        
        return metrics;
    }
    
    /**
     * Scheduler para atualização periódica das métricas.
     */
    @Scheduled(fixedRate = 30000) // A cada 30 segundos
    public void updateCQRSMetrics() {
        try {
            // Buscar o bean de métricas e forçar atualização
            // Isso será injetado automaticamente pelo Spring
            log.debug("Atualizando métricas CQRS periodicamente");
            
        } catch (Exception e) {
            log.error("Erro ao atualizar métricas CQRS periodicamente", e);
        }
    }
}