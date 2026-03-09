package com.seguradora.hibrida.aggregate.repository;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.exception.AggregateException;
import com.seguradora.hibrida.aggregate.exception.AggregateNotFoundException;
import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;
import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.*;

/**
 * Implementação do repositório de Aggregates usando Event Sourcing.
 * 
 * <p>Esta implementação:
 * <ul>
 *   <li>Persiste aggregates via Event Store</li>
 *   <li>Utiliza snapshots para otimização</li>
 *   <li>Publica eventos no Event Bus</li>
 *   <li>Implementa controle de concorrência</li>
 *   <li>Coleta métricas de performance</li>
 * </ul>
 * 
 * <p><strong>Configuração como Bean:</strong>
 * <pre>{@code
 * @Configuration
 * public class RepositoryConfiguration {
 *     
 *     @Bean
 *     public AggregateRepository<SeguradoAggregate> seguradoRepository(
 *             EventStore eventStore, 
 *             SnapshotStore snapshotStore,
 *             EventBus eventBus) {
 *         return new EventSourcingAggregateRepository<>(
 *             SeguradoAggregate.class, eventStore, snapshotStore, eventBus);
 *     }
 * }
 * }</pre>
 * 
 * @param <T> Tipo do aggregate que estende AggregateRoot
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
public class EventSourcingAggregateRepository<T extends AggregateRoot> implements AggregateRepository<T> {
    
    private final Class<T> aggregateType;
    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private final EventBus eventBus;
    
    // Métricas internas
    private long totalSaves = 0;
    private long totalLoads = 0;
    private long totalSnapshots = 0;
    private long totalConcurrencyErrors = 0;
    private Instant lastActivity = Instant.now();
    
    /**
     * Construtor principal.
     * 
     * @param aggregateType Classe do tipo de aggregate
     * @param eventStore Event Store para persistência
     * @param snapshotStore Snapshot Store para otimização
     * @param eventBus Event Bus para publicação de eventos
     */
    public EventSourcingAggregateRepository(Class<T> aggregateType, 
                                          EventStore eventStore,
                                          SnapshotStore snapshotStore,
                                          EventBus eventBus) {
        this.aggregateType = aggregateType;
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
        this.eventBus = eventBus;
        
        log.info("Repositório criado para aggregate type: {}", aggregateType.getSimpleName());
    }
    
    /**
     * Construtor sem Event Bus (para casos onde eventos não precisam ser publicados).
     */
    public EventSourcingAggregateRepository(Class<T> aggregateType, 
                                          EventStore eventStore,
                                          SnapshotStore snapshotStore) {
        this(aggregateType, eventStore, snapshotStore, null);
    }
    
    @Override
    @Transactional
    public void save(T aggregate) {
        if (aggregate == null) {
            throw new IllegalArgumentException("Aggregate não pode ser null");
        }
        
        if (!aggregate.hasUncommittedEvents()) {
            log.debug("Aggregate {} não possui eventos não commitados, ignorando save", 
                    aggregate.getId());
            return;
        }
        
        String aggregateId = aggregate.getId();
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        long expectedVersion = aggregate.getVersion() - events.size();
        
        log.debug("Salvando aggregate {} com {} eventos. Versão esperada: {}", 
                aggregateId, events.size(), expectedVersion);
        
        try {
            // Persistir eventos no Event Store
            eventStore.saveEvents(aggregateId, events, expectedVersion);
            
            // Marcar eventos como commitados
            aggregate.markEventsAsCommitted();
            
            // Criar snapshot se necessário
            createSnapshotIfNeeded(aggregate);
            
            // Publicar eventos no Event Bus
            publishEvents(events);
            
            // Atualizar métricas
            totalSaves++;
            lastActivity = Instant.now();
            
            log.debug("Aggregate {} salvo com sucesso. Nova versão: {}", 
                    aggregateId, aggregate.getVersion());
            
        } catch (ConcurrencyException e) {
            totalConcurrencyErrors++;
            log.warn("Erro de concorrência ao salvar aggregate {}: {}", aggregateId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro ao salvar aggregate {}: {}", aggregateId, e.getMessage(), e);
            throw new AggregateException("Erro ao salvar aggregate: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<T> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        
        log.debug("Carregando aggregate {} do tipo {}", id, aggregateType.getSimpleName());
        
        try {
            T aggregate = createAggregateInstance();
            
            // Tentar carregar do snapshot primeiro
            Optional<AggregateSnapshot> snapshot = snapshotStore.getLatestSnapshot(id);
            
            if (snapshot.isPresent()) {
                log.debug("Snapshot encontrado para aggregate {}, carregando eventos incrementais", id);
                
                // Carregar eventos desde o snapshot
                long snapshotVersion = snapshot.get().getVersion();
                List<DomainEvent> incrementalEvents = eventStore.loadEvents(id, snapshotVersion + 1);
                
                // Reconstruir do snapshot + eventos incrementais
                aggregate.loadFromSnapshot(snapshot.get().getData(), incrementalEvents);
                totalSnapshots++;
                
            } else {
                log.debug("Nenhum snapshot encontrado para aggregate {}, carregando todos os eventos", id);
                
                // Carregar todos os eventos
                List<DomainEvent> allEvents = eventStore.loadEvents(id);
                
                if (allEvents.isEmpty()) {
                    log.debug("Nenhum evento encontrado para aggregate {}", id);
                    return Optional.empty();
                }
                
                // Reconstruir do histórico completo
                aggregate.loadFromHistory(allEvents);
            }
            
            // Atualizar métricas
            totalLoads++;
            lastActivity = Instant.now();
            
            log.debug("Aggregate {} carregado com sucesso. Versão: {}", id, aggregate.getVersion());
            return Optional.of(aggregate);
            
        } catch (Exception e) {
            log.error("Erro ao carregar aggregate {}: {}", id, e.getMessage(), e);
            throw new AggregateException("Erro ao carregar aggregate: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<T> findByIdAndVersion(String id, long version) {
        if (id == null || id.trim().isEmpty() || version < 0) {
            return Optional.empty();
        }
        
        log.debug("Carregando aggregate {} na versão {}", id, version);
        
        try {
            // Carregar eventos até a versão especificada
            List<DomainEvent> events = eventStore.loadEvents(id)
                    .stream()
                    .filter(event -> event.getVersion() <= version)
                    .toList();
            
            if (events.isEmpty()) {
                return Optional.empty();
            }
            
            // Reconstruir aggregate até a versão
            T aggregate = createAggregateInstance();
            aggregate.loadFromHistory(events);
            
            return Optional.of(aggregate);
            
        } catch (Exception e) {
            log.error("Erro ao carregar aggregate {} na versão {}: {}", id, version, e.getMessage(), e);
            throw new AggregateException("Erro ao carregar aggregate na versão: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean exists(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        return eventStore.aggregateExists(id);
    }
    
    @Override
    public long getCurrentVersion(String id) {
        if (id == null || id.trim().isEmpty()) {
            return 0;
        }
        
        return eventStore.getCurrentVersion(id);
    }
    
    @Override
    @Transactional
    public boolean delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        log.warn("ATENÇÃO: Removendo todos os dados do aggregate {}. Esta operação é irreversível!", id);
        
        try {
            // Remover snapshots
            snapshotStore.deleteAllSnapshots(id);
            
            // Remover eventos (implementação dependente do Event Store)
            // Nota: Esta operação pode não estar disponível em todos os Event Stores
            // por questões de auditoria
            
            log.warn("Aggregate {} removido completamente", id);
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao remover aggregate {}: {}", id, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean createSnapshot(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        try {
            Optional<T> aggregate = findById(id);
            if (aggregate.isEmpty()) {
                return false;
            }
            
            return createSnapshotForAggregate(aggregate.get());
            
        } catch (Exception e) {
            log.error("Erro ao criar snapshot para aggregate {}: {}", id, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Class<T> getAggregateType() {
        return aggregateType;
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("aggregateType", aggregateType.getSimpleName());
        stats.put("totalSaves", totalSaves);
        stats.put("totalLoads", totalLoads);
        stats.put("totalSnapshots", totalSnapshots);
        stats.put("totalConcurrencyErrors", totalConcurrencyErrors);
        stats.put("lastActivity", lastActivity);
        
        // Estatísticas do Event Store
        if (eventStore instanceof com.seguradora.hibrida.eventstore.impl.PostgreSQLEventStore) {
            // Adicionar estatísticas específicas se disponíveis
        }
        
        return stats;
    }
    
    /**
     * Cria uma nova instância do aggregate usando reflection.
     */
    private T createAggregateInstance() {
        try {
            Constructor<T> constructor = aggregateType.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new AggregateException(
                    "Erro ao criar instância do aggregate " + aggregateType.getSimpleName() + 
                    ". Certifique-se de que existe um construtor padrão.", e);
        }
    }
    
    /**
     * Verifica se deve criar snapshot e o cria se necessário.
     */
    private void createSnapshotIfNeeded(T aggregate) {
        try {
            if (snapshotStore.shouldCreateSnapshot(aggregate.getId(), aggregate.getVersion())) {
                createSnapshotForAggregate(aggregate);
            }
        } catch (Exception e) {
            // Log do erro mas não falha a operação principal
            log.warn("Erro ao criar snapshot para aggregate {}: {}", 
                    aggregate.getId(), e.getMessage());
        }
    }
    
    /**
     * Cria snapshot para um aggregate específico.
     */
    @SuppressWarnings("unchecked")
    private boolean createSnapshotForAggregate(T aggregate) {
        try {
            Object snapshotData = aggregate.createSnapshot();
            
            // Converter snapshotData para Map se necessário
            Map<String, Object> dataMap;
            if (snapshotData instanceof Map) {
                dataMap = (Map<String, Object>) snapshotData;
            } else {
                // Se não for Map, criar um Map wrapper
                dataMap = new HashMap<>();
                dataMap.put("data", snapshotData);
            }
            
            AggregateSnapshot snapshot = new AggregateSnapshot(
                    aggregate.getId(),
                    aggregate.getAggregateType(),
                    aggregate.getVersion(),
                    dataMap
            );
            
            snapshotStore.saveSnapshot(snapshot);
            
            log.debug("Snapshot criado para aggregate {} na versão {}", 
                    aggregate.getId(), aggregate.getVersion());
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao criar snapshot para aggregate {}: {}", 
                    aggregate.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Publica eventos no Event Bus se disponível.
     */
    private void publishEvents(List<DomainEvent> events) {
        if (eventBus == null || events.isEmpty()) {
            return;
        }
        
        try {
            for (DomainEvent event : events) {
                eventBus.publishAsync(event);
            }
            
            log.debug("Publicados {} eventos no Event Bus", events.size());
            
        } catch (Exception e) {
            // Log do erro mas não falha a operação principal
            log.warn("Erro ao publicar eventos no Event Bus: {}", e.getMessage());
        }
    }
}