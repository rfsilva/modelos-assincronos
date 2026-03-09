package com.seguradora.hibrida.projection.config;

import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.projection.ProjectionEventProcessor;
import com.seguradora.hibrida.projection.ProjectionRegistry;
import com.seguradora.hibrida.projection.consistency.ProjectionConsistencyChecker;
import com.seguradora.hibrida.projection.consistency.ProjectionConsistencyProperties;
import com.seguradora.hibrida.projection.rebuild.ProjectionRebuilder;
import com.seguradora.hibrida.projection.rebuild.ProjectionRebuildProperties;
import com.seguradora.hibrida.projection.scheduler.ProjectionMaintenanceScheduler;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração para funcionalidades de rebuild e consistência de projeções.
 */
@Configuration
@EnableConfigurationProperties({
    ProjectionRebuildProperties.class,
    ProjectionConsistencyProperties.class
})
public class ProjectionRebuildConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(ProjectionRebuildConfiguration.class);
    
    /**
     * Serviço de rebuild de projeções.
     */
    @Bean
    @ConditionalOnProperty(prefix = "cqrs.projection.rebuild", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ProjectionRebuilder projectionRebuilder(EventStore eventStore,
                                                 ProjectionRegistry projectionRegistry,
                                                 ProjectionTrackerRepository trackerRepository,
                                                 ProjectionEventProcessor eventProcessor,
                                                 ProjectionRebuildProperties properties) {
        
        log.info("Configurando ProjectionRebuilder com batch size: {}", properties.getBatchSize());
        
        return new ProjectionRebuilder(
            eventStore,
            projectionRegistry,
            trackerRepository,
            eventProcessor,
            properties
        );
    }
    
    /**
     * Verificador de consistência de projeções.
     */
    @Bean
    @ConditionalOnProperty(prefix = "cqrs.projection.consistency", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ProjectionConsistencyChecker projectionConsistencyChecker(
            ProjectionTrackerRepository trackerRepository,
            EventStoreRepository eventStoreRepository,
            ProjectionConsistencyProperties properties) {
        
        log.info("Configurando ProjectionConsistencyChecker com intervalo: {}s", 
                properties.getCheckIntervalSeconds());
        
        return new ProjectionConsistencyChecker(
            trackerRepository,
            eventStoreRepository,
            properties
        );
    }
    
    /**
     * Scheduler de manutenção de projeções.
     */
    @Bean
    @ConditionalOnProperty(prefix = "cqrs.projection.rebuild", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ProjectionMaintenanceScheduler projectionMaintenanceScheduler(
            ProjectionRebuilder rebuilder,
            ProjectionConsistencyChecker consistencyChecker,
            ProjectionRebuildProperties properties) {
        
        log.info("Configurando ProjectionMaintenanceScheduler");
        
        return new ProjectionMaintenanceScheduler(
            rebuilder,
            consistencyChecker,
            properties
        );
    }
}