package com.seguradora.hibrida.eventbus.config;

import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventHandler;
import com.seguradora.hibrida.eventbus.EventHandlerRegistry;
import com.seguradora.hibrida.eventbus.impl.SimpleEventBus;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Configuração Spring para o Event Bus.
 * 
 * <p>Esta configuração:
 * <ul>
 *   <li>Registra o Event Bus como bean Spring</li>
 *   <li>Descobre automaticamente handlers anotados com @Component</li>
 *   <li>Registra handlers no registry automaticamente</li>
 *   <li>Configura métricas e monitoramento</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Configuration
public class EventBusConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(EventBusConfiguration.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private EventBusProperties properties;
    
    /**
     * Configura o Event Bus principal.
     */
    @Bean
    public EventBus eventBus(EventHandlerRegistry handlerRegistry) {
        log.info("Configuring Event Bus with properties: {}", properties);
        return new SimpleEventBus(handlerRegistry);
    }
    
    /**
     * Configura o registry de handlers.
     */
    @Bean
    public EventHandlerRegistry eventHandlerRegistry() {
        return new EventHandlerRegistry();
    }
    
    /**
     * Configura métricas do Event Bus.
     */
    @Bean
    public EventBusMetrics eventBusMetrics(EventBus eventBus) {
        if (properties.getMonitoring().isMetricsEnabled()) {
            return new EventBusMetrics(eventBus);
        }
        return null;
    }
    
    /**
     * Configura health indicator do Event Bus.
     */
    @Bean
    public EventBusHealthIndicator eventBusHealthIndicator(EventBus eventBus) {
        if (properties.getMonitoring().isHealthCheckEnabled()) {
            return new EventBusHealthIndicator(eventBus, properties);
        }
        return null;
    }
    
    /**
     * Descobre e registra automaticamente todos os handlers de eventos.
     * 
     * <p>Este método é executado após a inicialização do contexto Spring
     * e busca por todos os beans que implementam EventHandler, registrando-os
     * automaticamente no Event Bus.
     */
    @PostConstruct
    @SuppressWarnings("unchecked")
    public void registerEventHandlers() {
        log.info("Starting automatic discovery of event handlers");
        
        EventHandlerRegistry registry = applicationContext.getBean(EventHandlerRegistry.class);
        Map<String, EventHandler> handlers = applicationContext.getBeansOfType(EventHandler.class);
        
        int registeredCount = 0;
        
        for (Map.Entry<String, EventHandler> entry : handlers.entrySet()) {
            String beanName = entry.getKey();
            EventHandler<? extends DomainEvent> handler = entry.getValue();
            
            try {
                Class<? extends DomainEvent> eventType = extractEventType(handler);
                if (eventType != null) {
                    // Cast seguro usando wildcards
                    registerHandlerSafely(registry, eventType, handler);
                    registeredCount++;
                    
                    log.debug("Registered handler {} for event type {} (bean: {})", 
                             handler.getClass().getSimpleName(), eventType.getSimpleName(), beanName);
                } else {
                    log.warn("Could not determine event type for handler {} (bean: {})", 
                            handler.getClass().getSimpleName(), beanName);
                }
                
            } catch (Exception e) {
                log.error("Failed to register handler {} (bean: {}): {}", 
                         handler.getClass().getSimpleName(), beanName, e.getMessage(), e);
            }
        }
        
        log.info("Automatic discovery completed. Registered {} event handlers from {} beans", 
                registeredCount, handlers.size());
        
        // Validar configuração
        var issues = registry.validateConfiguration();
        if (!issues.isEmpty()) {
            log.warn("Event handler configuration issues found:");
            issues.forEach(issue -> log.warn("  - {}", issue));
        }
    }
    
    /**
     * Registra um handler de forma type-safe usando wildcards.
     */
    @SuppressWarnings("unchecked")
    private <T extends DomainEvent> void registerHandlerSafely(
            EventHandlerRegistry registry, 
            Class<T> eventType, 
            EventHandler<? extends DomainEvent> handler) {
        
        // Cast seguro - sabemos que o handler processa eventos do tipo T
        EventHandler<T> typedHandler = (EventHandler<T>) handler;
        registry.registerHandler(eventType, typedHandler);
    }
    
    /**
     * Extrai o tipo de evento de um handler usando reflection.
     * 
     * <p>Este método analisa a hierarquia de tipos do handler para
     * determinar qual tipo de DomainEvent ele processa.
     * 
     * @param handler Handler a ser analisado
     * @return Tipo do evento ou null se não puder ser determinado
     */
    @SuppressWarnings("unchecked")
    private Class<? extends DomainEvent> extractEventType(EventHandler<? extends DomainEvent> handler) {
        try {
            // Primeiro, tentar usar o método getEventType() se disponível
            Class<? extends DomainEvent> eventType = handler.getEventType();
            if (eventType != null) {
                return eventType;
            }
        } catch (Exception e) {
            log.debug("Could not get event type from handler method: {}", e.getMessage());
        }
        
        // Fallback: usar reflection para analisar tipos genéricos
        return extractEventTypeFromGenericInterface(handler);
    }
    
    /**
     * Extrai o tipo de evento analisando interfaces genéricas.
     */
    @SuppressWarnings("unchecked")
    private Class<? extends DomainEvent> extractEventTypeFromGenericInterface(EventHandler<? extends DomainEvent> handler) {
        Class<?> handlerClass = handler.getClass();
        
        // Analisar interfaces implementadas
        Type[] genericInterfaces = handlerClass.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                Type rawType = parameterizedType.getRawType();
                
                if (rawType.equals(EventHandler.class)) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                        Class<?> eventType = (Class<?>) typeArguments[0];
                        if (DomainEvent.class.isAssignableFrom(eventType)) {
                            return (Class<? extends DomainEvent>) eventType;
                        }
                    }
                }
            }
        }
        
        // Analisar superclasses
        Class<?> superclass = handlerClass.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            Type genericSuperclass = handlerClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                    Class<?> eventType = (Class<?>) typeArguments[0];
                    if (DomainEvent.class.isAssignableFrom(eventType)) {
                        return (Class<? extends DomainEvent>) eventType;
                    }
                }
            }
        }
        
        // Usar ResolvableType como último recurso
        try {
            ResolvableType resolvableType = ResolvableType.forClass(handlerClass).as(EventHandler.class);
            ResolvableType eventType = resolvableType.getGeneric(0);
            if (eventType.getRawClass() != null && DomainEvent.class.isAssignableFrom(eventType.getRawClass())) {
                return (Class<? extends DomainEvent>) eventType.getRawClass();
            }
        } catch (Exception e) {
            log.debug("Could not resolve event type using ResolvableType: {}", e.getMessage());
        }
        
        return null;
    }
}