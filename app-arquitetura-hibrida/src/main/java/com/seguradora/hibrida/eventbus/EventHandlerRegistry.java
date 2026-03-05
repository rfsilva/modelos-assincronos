package com.seguradora.hibrida.eventbus;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Registry responsável por gerenciar handlers de eventos de domínio.
 * 
 * <p>Esta classe mantém um mapeamento thread-safe entre tipos de eventos
 * e seus respectivos handlers, permitindo descoberta automática e
 * roteamento eficiente.
 * 
 * <p>Funcionalidades:
 * <ul>
 *   <li>Registro automático de handlers via Spring</li>
 *   <li>Mapeamento thread-safe tipo -> handlers</li>
 *   <li>Ordenação por prioridade</li>
 *   <li>Validação de handlers duplicados</li>
 *   <li>Estatísticas de registro</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class EventHandlerRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(EventHandlerRegistry.class);
    
    /**
     * Mapa thread-safe de tipo de evento para lista de handlers.
     * Usa ConcurrentHashMap para thread safety e CopyOnWriteArrayList
     * para permitir iteração segura durante modificações.
     */
    private final Map<Class<? extends DomainEvent>, List<EventHandler<? extends DomainEvent>>> handlers;
    
    /**
     * Cache de handlers ordenados por prioridade para otimizar consultas.
     */
    private final Map<Class<? extends DomainEvent>, List<EventHandler<? extends DomainEvent>>> sortedHandlersCache;
    
    /**
     * Estatísticas de registro.
     */
    private volatile int totalHandlers = 0;
    private volatile long lastRegistrationTime = 0;
    
    public EventHandlerRegistry() {
        this.handlers = new ConcurrentHashMap<>();
        this.sortedHandlersCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Registra um handler para um tipo específico de evento.
     * 
     * @param eventType Tipo do evento
     * @param handler Handler a ser registrado
     * @param <T> Tipo do evento
     * @throws IllegalArgumentException se eventType ou handler forem null
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void registerHandler(Class<T> eventType, EventHandler<T> handler) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null");
        }
        
        log.debug("Registering handler {} for event type {}", 
                 handler.getClass().getSimpleName(), eventType.getSimpleName());
        
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add((EventHandler<? extends DomainEvent>) handler);
        
        // Invalidar cache para forçar reordenação
        sortedHandlersCache.remove(eventType);
        
        totalHandlers++;
        lastRegistrationTime = System.currentTimeMillis();
        
        log.info("Handler {} registered for event type {}. Total handlers: {}", 
                handler.getClass().getSimpleName(), eventType.getSimpleName(), totalHandlers);
    }
    
    /**
     * Remove um handler registrado para um tipo de evento.
     * 
     * @param eventType Tipo do evento
     * @param handler Handler a ser removido
     * @param <T> Tipo do evento
     * @return true se o handler foi removido, false se não estava registrado
     */
    public <T extends DomainEvent> boolean unregisterHandler(Class<T> eventType, EventHandler<T> handler) {
        if (eventType == null || handler == null) {
            return false;
        }
        
        List<EventHandler<? extends DomainEvent>> eventHandlers = handlers.get(eventType);
        if (eventHandlers == null) {
            return false;
        }
        
        boolean removed = eventHandlers.remove(handler);
        if (removed) {
            // Remover lista vazia
            if (eventHandlers.isEmpty()) {
                handlers.remove(eventType);
            }
            
            // Invalidar cache
            sortedHandlersCache.remove(eventType);
            
            totalHandlers--;
            
            log.info("Handler {} unregistered for event type {}. Total handlers: {}", 
                    handler.getClass().getSimpleName(), eventType.getSimpleName(), totalHandlers);
        }
        
        return removed;
    }
    
    /**
     * Obtém todos os handlers registrados para um tipo de evento,
     * ordenados por prioridade (maior prioridade primeiro).
     * 
     * @param eventType Tipo do evento
     * @param <T> Tipo do evento
     * @return Lista de handlers ordenados por prioridade
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> List<EventHandler<T>> getHandlers(Class<T> eventType) {
        if (eventType == null) {
            return Collections.emptyList();
        }
        
        // Verificar cache primeiro
        List<EventHandler<? extends DomainEvent>> cached = sortedHandlersCache.get(eventType);
        if (cached != null) {
            return cached.stream()
                    .map(handler -> (EventHandler<T>) handler)
                    .collect(Collectors.toList());
        }
        
        // Obter handlers e ordenar por prioridade
        List<EventHandler<? extends DomainEvent>> eventHandlers = handlers.get(eventType);
        if (eventHandlers == null || eventHandlers.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<EventHandler<? extends DomainEvent>> sorted = eventHandlers.stream()
                .sorted((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()))
                .collect(Collectors.toList());
        
        // Cachear resultado
        sortedHandlersCache.put(eventType, sorted);
        
        return sorted.stream()
                .map(handler -> (EventHandler<T>) handler)
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica se existem handlers registrados para um tipo de evento.
     * 
     * @param eventType Tipo do evento
     * @return true se existirem handlers, false caso contrário
     */
    public boolean hasHandlers(Class<? extends DomainEvent> eventType) {
        if (eventType == null) {
            return false;
        }
        
        List<EventHandler<? extends DomainEvent>> eventHandlers = handlers.get(eventType);
        return eventHandlers != null && !eventHandlers.isEmpty();
    }
    
    /**
     * Obtém todos os tipos de eventos que possuem handlers registrados.
     * 
     * @return Set com todos os tipos de eventos
     */
    public Set<Class<? extends DomainEvent>> getRegisteredEventTypes() {
        return new HashSet<>(handlers.keySet());
    }
    
    /**
     * Obtém o número total de handlers registrados.
     * 
     * @return Número total de handlers
     */
    public int getTotalHandlers() {
        return totalHandlers;
    }
    
    /**
     * Obtém o número de tipos de eventos diferentes registrados.
     * 
     * @return Número de tipos de eventos
     */
    public int getEventTypesCount() {
        return handlers.size();
    }
    
    /**
     * Obtém o timestamp da última operação de registro.
     * 
     * @return Timestamp em milissegundos
     */
    public long getLastRegistrationTime() {
        return lastRegistrationTime;
    }
    
    /**
     * Limpa todos os handlers registrados.
     * 
     * <p>Usado principalmente para testes.
     */
    public void clear() {
        log.warn("Clearing all registered handlers");
        handlers.clear();
        sortedHandlersCache.clear();
        totalHandlers = 0;
        lastRegistrationTime = System.currentTimeMillis();
    }
    
    /**
     * Obtém estatísticas detalhadas do registry.
     * 
     * @return Mapa com estatísticas
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalHandlers", totalHandlers);
        stats.put("eventTypesCount", getEventTypesCount());
        stats.put("lastRegistrationTime", lastRegistrationTime);
        
        // Estatísticas por tipo de evento
        Map<String, Integer> handlersByType = new HashMap<>();
        handlers.forEach((eventType, handlerList) -> 
                handlersByType.put(eventType.getSimpleName(), handlerList.size()));
        stats.put("handlersByEventType", handlersByType);
        
        return stats;
    }
    
    /**
     * Valida a configuração atual do registry.
     * 
     * @return Lista de problemas encontrados (vazia se tudo OK)
     */
    public List<String> validateConfiguration() {
        List<String> issues = new ArrayList<>();
        
        if (totalHandlers == 0) {
            issues.add("No handlers registered");
        }
        
        // Verificar handlers duplicados por tipo
        handlers.forEach((eventType, handlerList) -> {
            Set<Class<?>> handlerTypes = new HashSet<>();
            for (EventHandler<? extends DomainEvent> handler : handlerList) {
                if (!handlerTypes.add(handler.getClass())) {
                    issues.add("Duplicate handler type for event " + eventType.getSimpleName() + 
                              ": " + handler.getClass().getSimpleName());
                }
            }
        });
        
        return issues;
    }
}