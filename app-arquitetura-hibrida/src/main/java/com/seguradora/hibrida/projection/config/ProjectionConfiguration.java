package com.seguradora.hibrida.projection.config;

import com.seguradora.hibrida.projection.ProjectionEventProcessor;
import com.seguradora.hibrida.projection.ProjectionHandler;
import com.seguradora.hibrida.projection.ProjectionRegistry;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuração do sistema de projeções.
 * 
 * <p>Configura:
 * <ul>
 *   <li>Registry de projection handlers</li>
 *   <li>Processador de eventos</li>
 *   <li>Pool de threads para processamento assíncrono</li>
 *   <li>Descoberta automática de handlers</li>
 * </ul>
 */
@Configuration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(ProjectionProperties.class)
public class ProjectionConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(ProjectionConfiguration.class);
    
    private final ProjectionProperties properties;
    
    public ProjectionConfiguration(ProjectionProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Registry para gerenciamento de projection handlers.
     */
    @Bean
    public ProjectionRegistry projectionRegistry() {
        return new ProjectionRegistry();
    }
    
    /**
     * Processador de eventos para projeções.
     */
    @Bean
    public ProjectionEventProcessor projectionEventProcessor(
            ProjectionRegistry projectionRegistry,
            ProjectionTrackerRepository trackerRepository) {
        
        ProjectionEventProcessor processor = new ProjectionEventProcessor(projectionRegistry);
        processor.setTrackerRepository(trackerRepository);
        return processor;
    }
    
    /**
     * Task executor para processamento assíncrono de projeções.
     */
    @Bean("projectionTaskExecutor")
    public TaskExecutor projectionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        ProjectionProperties.ThreadPool threadPool = properties.getThreadPool();
        executor.setCorePoolSize(threadPool.getCoreSize());
        executor.setMaxPoolSize(threadPool.getMaxSize());
        executor.setQueueCapacity(threadPool.getQueueCapacity());
        executor.setThreadNamePrefix(threadPool.getThreadNamePrefix());
        executor.setKeepAliveSeconds(threadPool.getKeepAliveSeconds());
        
        // Configurações adicionais
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        log.info("Configurado ProjectionTaskExecutor: core={}, max={}, queue={}", 
                threadPool.getCoreSize(), threadPool.getMaxSize(), threadPool.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * Registra automaticamente todos os projection handlers encontrados.
     */
    @Bean
    public ProjectionHandlerRegistrar projectionHandlerRegistrar(
            ProjectionRegistry registry,
            List<ProjectionHandler<? extends DomainEvent>> handlers) {
        
        return new ProjectionHandlerRegistrar(registry, handlers);
    }
    
    /**
     * Classe interna para registro automático de handlers.
     */
    public static class ProjectionHandlerRegistrar {
        
        private static final Logger log = LoggerFactory.getLogger(ProjectionHandlerRegistrar.class);
        
        public ProjectionHandlerRegistrar(ProjectionRegistry registry,
                                        List<ProjectionHandler<? extends DomainEvent>> handlers) {
            
            log.info("Iniciando registro automático de projection handlers...");
            
            for (ProjectionHandler<? extends DomainEvent> handler : handlers) {
                try {
                    registerHandlerSafely(registry, handler);
                } catch (Exception e) {
                    log.error("Erro ao registrar handler {}: {}", 
                             handler.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
            
            log.info("Registro de projection handlers concluído. Total: {} handlers", handlers.size());
            
            // Log de estatísticas
            logRegistryStatistics(registry);
        }
        
        @SuppressWarnings("unchecked")
        private void registerHandlerSafely(ProjectionRegistry registry,
                                         ProjectionHandler<? extends DomainEvent> handler) {
            
            Class<? extends DomainEvent> eventType = extractEventType(handler);
            
            if (eventType == null) {
                log.warn("Não foi possível determinar o tipo de evento para handler: {}", 
                        handler.getClass().getSimpleName());
                return;
            }
            
            log.debug("Registrando handler {} para evento {}", 
                     handler.getProjectionName(), eventType.getSimpleName());
            
            registry.registerHandler((ProjectionHandler<DomainEvent>) handler);
            
            log.info("Handler {} registrado com sucesso para evento {}", 
                    handler.getProjectionName(), eventType.getSimpleName());
        }
        
        private Class<? extends DomainEvent> extractEventType(ProjectionHandler<? extends DomainEvent> handler) {
            try {
                // Primeiro, tenta obter do método getEventType()
                return handler.getEventType();
            } catch (Exception e) {
                // Se falhar, tenta extrair via reflection
                return extractEventTypeFromGenericInterface(handler);
            }
        }
        
        @SuppressWarnings("unchecked")
        private Class<? extends DomainEvent> extractEventTypeFromGenericInterface(
                ProjectionHandler<? extends DomainEvent> handler) {
            
            Class<?> handlerClass = handler.getClass();
            
            // Busca na hierarquia de classes
            while (handlerClass != null && handlerClass != Object.class) {
                Type[] genericInterfaces = handlerClass.getGenericInterfaces();
                
                for (Type genericInterface : genericInterfaces) {
                    if (genericInterface instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                        
                        if (parameterizedType.getRawType().equals(ProjectionHandler.class)) {
                            Type[] typeArguments = parameterizedType.getActualTypeArguments();
                            if (typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                                return (Class<? extends DomainEvent>) typeArguments[0];
                            }
                        }
                    }
                }
                
                // Verifica superclasse
                Type genericSuperclass = handlerClass.getGenericSuperclass();
                if (genericSuperclass instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                        return (Class<? extends DomainEvent>) typeArguments[0];
                    }
                }
                
                handlerClass = handlerClass.getSuperclass();
            }
            
            return null;
        }
        
        private void logRegistryStatistics(ProjectionRegistry registry) {
            var statistics = registry.getStatistics();
            
            log.info("=== Estatísticas do Registry de Projeções ===");
            log.info("Total de tipos de eventos: {}", statistics.get("totalEventTypes"));
            log.info("Total de projeções: {}", statistics.get("totalProjections"));
            
            @SuppressWarnings("unchecked")
            var handlersByEventType = (java.util.Map<String, Integer>) statistics.get("handlersByEventType");
            
            if (handlersByEventType != null && !handlersByEventType.isEmpty()) {
                log.info("Handlers por tipo de evento:");
                handlersByEventType.forEach((eventType, count) -> 
                    log.info("  {} -> {} handler(s)", eventType, count));
            }
            
            log.info("=== Fim das Estatísticas ===");
        }
    }
}