package com.seguradora.hibrida.projection;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry para descoberta e gerenciamento de Projection Handlers.
 * 
 * <p>Responsável por:
 * <ul>
 *   <li>Descoberta automática de handlers</li>
 *   <li>Roteamento de eventos para handlers</li>
 *   <li>Ordenação de handlers por prioridade</li>
 *   <li>Validação de configuração</li>
 * </ul>
 */
@Component
public class ProjectionRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(ProjectionRegistry.class);
    
    private final Map<Class<? extends DomainEvent>, List<ProjectionHandler<? extends DomainEvent>>> handlers;
    private final Map<String, ProjectionHandler<? extends DomainEvent>> handlersByName;
    private final Object lock = new Object();
    
    public ProjectionRegistry() {
        this.handlers = new ConcurrentHashMap<>();
        this.handlersByName = new ConcurrentHashMap<>();
    }
    
    /**
     * Registra um projection handler.
     * 
     * @param handler Handler a ser registrado
     * @param <T> Tipo do evento processado pelo handler
     */
    public <T extends DomainEvent> void registerHandler(ProjectionHandler<T> handler) {
        synchronized (lock) {
            Class<T> eventType = handler.getEventType();
            String projectionName = handler.getProjectionName();
            
            log.info("Registrando projection handler: {} para evento: {}", 
                    projectionName, eventType.getSimpleName());
            
            // Validar se já existe handler com mesmo nome
            if (handlersByName.containsKey(projectionName)) {
                throw new IllegalArgumentException(
                    "Já existe um handler registrado com o nome: " + projectionName);
            }
            
            // Adicionar à lista de handlers por tipo de evento
            handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
            
            // Ordenar handlers por prioridade (ordem crescente)
            handlers.get(eventType).sort(Comparator.comparingInt(ProjectionHandler::getOrder));
            
            // Adicionar ao mapa por nome
            handlersByName.put(projectionName, handler);
            
            log.debug("Handler {} registrado com sucesso. Total de handlers para {}: {}", 
                     projectionName, eventType.getSimpleName(), handlers.get(eventType).size());
        }
    }
    
    /**
     * Remove um projection handler.
     * 
     * @param projectionName Nome da projeção
     * @return true se foi removido, false se não existia
     */
    public boolean unregisterHandler(String projectionName) {
        synchronized (lock) {
            ProjectionHandler<? extends DomainEvent> handler = handlersByName.remove(projectionName);
            
            if (handler != null) {
                Class<? extends DomainEvent> eventType = handler.getEventType();
                List<ProjectionHandler<? extends DomainEvent>> eventHandlers = handlers.get(eventType);
                
                if (eventHandlers != null) {
                    eventHandlers.remove(handler);
                    
                    if (eventHandlers.isEmpty()) {
                        handlers.remove(eventType);
                    }
                }
                
                log.info("Handler {} removido com sucesso", projectionName);
                return true;
            }
            
            log.warn("Tentativa de remover handler inexistente: {}", projectionName);
            return false;
        }
    }
    
    /**
     * Obtém todos os handlers para um tipo de evento.
     * 
     * @param eventType Tipo do evento
     * @return Lista de handlers ordenada por prioridade
     */
    public List<ProjectionHandler<? extends DomainEvent>> getHandlers(Class<? extends DomainEvent> eventType) {
        List<ProjectionHandler<? extends DomainEvent>> eventHandlers = handlers.get(eventType);
        
        if (eventHandlers == null || eventHandlers.isEmpty()) {
            return Collections.emptyList();
        }
        
        return new ArrayList<>(eventHandlers);
    }
    
    /**
     * Obtém um handler específico pelo nome.
     * 
     * @param projectionName Nome da projeção
     * @return Handler ou null se não encontrado
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> ProjectionHandler<T> getHandler(String projectionName) {
        return (ProjectionHandler<T>) handlersByName.get(projectionName);
    }
    
    /**
     * Verifica se existem handlers para um tipo de evento.
     * 
     * @param eventType Tipo do evento
     * @return true se existem handlers
     */
    public boolean hasHandlers(Class<? extends DomainEvent> eventType) {
        List<ProjectionHandler<? extends DomainEvent>> eventHandlers = handlers.get(eventType);
        return eventHandlers != null && !eventHandlers.isEmpty();
    }
    
    /**
     * Obtém todos os tipos de eventos registrados.
     * 
     * @return Set com todos os tipos de eventos
     */
    public Set<Class<? extends DomainEvent>> getRegisteredEventTypes() {
        return new HashSet<>(handlers.keySet());
    }
    
    /**
     * Obtém todos os nomes de projeções registradas.
     * 
     * @return Set com todos os nomes de projeções
     */
    public Set<String> getRegisteredProjectionNames() {
        return new HashSet<>(handlersByName.keySet());
    }
    
    /**
     * Obtém estatísticas do registry.
     * 
     * @return Mapa com estatísticas
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalEventTypes", handlers.size());
        stats.put("totalProjections", handlersByName.size());
        
        Map<String, Integer> handlersByEventType = new HashMap<>();
        handlers.forEach((eventType, handlerList) -> 
            handlersByEventType.put(eventType.getSimpleName(), handlerList.size()));
        
        stats.put("handlersByEventType", handlersByEventType);
        
        return stats;
    }
    
    /**
     * Valida a configuração do registry.
     * 
     * @return Lista de problemas encontrados (vazia se tudo OK)
     */
    public List<String> validateConfiguration() {
        List<String> issues = new ArrayList<>();
        
        // Verificar se há handlers duplicados
        Map<String, Integer> projectionCounts = new HashMap<>();
        handlersByName.forEach((name, handler) -> 
            projectionCounts.merge(name, 1, Integer::sum));
        
        projectionCounts.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .forEach(entry -> issues.add("Projeção duplicada: " + entry.getKey()));
        
        // Verificar se há handlers órfãos
        handlersByName.values().forEach(handler -> {
            if (!handlers.containsKey(handler.getEventType())) {
                issues.add("Handler órfão encontrado: " + handler.getProjectionName());
            }
        });
        
        return issues;
    }
    
    /**
     * Limpa todos os handlers registrados.
     * 
     * <p>Usado principalmente para testes.
     */
    public void clear() {
        synchronized (lock) {
            handlers.clear();
            handlersByName.clear();
            log.info("Registry limpo - todos os handlers removidos");
        }
    }
}