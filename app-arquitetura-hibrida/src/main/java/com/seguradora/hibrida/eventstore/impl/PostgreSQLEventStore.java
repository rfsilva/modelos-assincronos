package com.seguradora.hibrida.eventstore.impl;

import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.entity.EventStoreEntry;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;
import com.seguradora.hibrida.eventstore.exception.EventStoreException;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.eventstore.serialization.EventSerializer;
import com.seguradora.hibrida.eventstore.serialization.SerializationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementação do Event Store usando PostgreSQL.
 * 
 * Fornece persistência ACID de eventos com controle de concorrência
 * otimista, compressão automática e consultas otimizadas.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostgreSQLEventStore implements EventStore {
    
    private final EventStoreRepository repository;
    private final EventSerializer eventSerializer;
    
    @Value("${eventstore.serialization.compression-threshold:1024}")
    private int compressionThreshold;
    
    @Override
    @Transactional
    public void saveEvents(String aggregateId, List<DomainEvent> events, long expectedVersion) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        log.debug("Salvando {} eventos para aggregate {}, versão esperada: {}", 
                 events.size(), aggregateId, expectedVersion);
        
        // Verifica controle de concorrência
        long currentVersion = getCurrentVersion(aggregateId);
        if (currentVersion != expectedVersion) {
            throw new ConcurrencyException(aggregateId, expectedVersion, currentVersion);
        }
        
        try {
            List<EventStoreEntry> entries = events.stream()
                    .map(event -> convertToEntry(event))
                    .collect(Collectors.toList());
            
            repository.saveAll(entries);
            
            log.info("Salvos {} eventos para aggregate {} (versões {} a {})", 
                    events.size(), aggregateId, 
                    expectedVersion + 1, expectedVersion + events.size());
            
        } catch (DataIntegrityViolationException e) {
            // Pode ser violação de constraint de versão única
            long actualVersion = getCurrentVersion(aggregateId);
            throw new ConcurrencyException(aggregateId, expectedVersion, actualVersion);
        } catch (Exception e) {
            log.error("Erro ao salvar eventos para aggregate {}", aggregateId, e);
            throw new EventStoreException("Falha ao salvar eventos", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> loadEvents(String aggregateId) {
        log.debug("Carregando todos os eventos para aggregate {}", aggregateId);
        
        List<EventStoreEntry> entries = repository.findByAggregateIdOrderByVersionAsc(aggregateId);
        
        List<DomainEvent> events = entries.stream()
                .map(this::convertFromEntry)
                .collect(Collectors.toList());
        
        log.debug("Carregados {} eventos para aggregate {}", events.size(), aggregateId);
        return events;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> loadEvents(String aggregateId, long fromVersion) {
        log.debug("Carregando eventos para aggregate {} a partir da versão {}", aggregateId, fromVersion);
        
        List<EventStoreEntry> entries = repository
                .findByAggregateIdAndVersionGreaterThanEqualOrderByVersionAsc(aggregateId, fromVersion);
        
        List<DomainEvent> events = entries.stream()
                .map(this::convertFromEntry)
                .collect(Collectors.toList());
        
        log.debug("Carregados {} eventos para aggregate {} (versão >= {})", 
                 events.size(), aggregateId, fromVersion);
        return events;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> loadEventsByType(String eventType, Instant from, Instant to) {
        log.debug("Carregando eventos do tipo {} entre {} e {}", eventType, from, to);
        
        List<EventStoreEntry> entries = repository
                .findByEventTypeAndTimestampBetweenOrderByTimestampAsc(eventType, from, to);
        
        List<DomainEvent> events = entries.stream()
                .map(this::convertFromEntry)
                .collect(Collectors.toList());
        
        log.debug("Carregados {} eventos do tipo {} no período especificado", events.size(), eventType);
        return events;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> loadEventsByCorrelationId(UUID correlationId) {
        log.debug("Carregando eventos com correlation ID {}", correlationId);
        
        List<EventStoreEntry> entries = repository.findByCorrelationIdOrderByTimestampAsc(correlationId);
        
        List<DomainEvent> events = entries.stream()
                .map(this::convertFromEntry)
                .collect(Collectors.toList());
        
        log.debug("Carregados {} eventos com correlation ID {}", events.size(), correlationId);
        return events;
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getCurrentVersion(String aggregateId) {
        return repository.findMaxVersionByAggregateId(aggregateId).orElse(0L);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean aggregateExists(String aggregateId) {
        return repository.existsByAggregateId(aggregateId);
    }
    
    /**
     * Converte DomainEvent para EventStoreEntry.
     */
    private EventStoreEntry convertToEntry(DomainEvent event) {
        try {
            SerializationResult result = eventSerializer.serializeWithCompression(event, compressionThreshold);
            
            return EventStoreEntry.builder()
                    .id(event.getEventId())
                    .aggregateId(event.getAggregateId())
                    .aggregateType(event.getAggregateType())
                    .eventType(event.getClass().getName())
                    .version(event.getVersion())
                    .timestamp(event.getTimestamp())
                    .correlationId(event.getCorrelationId())
                    .userId(event.getUserId())
                    .eventData(result.getData())
                    .metadata(event.getMetadata())
                    .compressed(result.isCompressed())
                    .dataSize(result.getFinalSize())
                    .build();
                    
        } catch (Exception e) {
            log.error("Erro ao converter evento para entry: {}", event.getEventType(), e);
            throw new EventStoreException("Falha na conversão do evento", e);
        }
    }
    
    /**
     * Converte EventStoreEntry para DomainEvent.
     */
    private DomainEvent convertFromEntry(EventStoreEntry entry) {
        try {
            DomainEvent event = eventSerializer.deserializeCompressed(
                    entry.getEventData(), 
                    entry.getEventType(), 
                    entry.getCompressed()
            );
            
            // Garante que os metadados do entry sejam preservados
            if (entry.getMetadata() != null && !entry.getMetadata().isEmpty()) {
                if (event.getMetadata() == null) {
                    event.setMetadata(new com.seguradora.hibrida.eventstore.model.EventMetadata());
                }
                event.getMetadata().putAll(entry.getMetadata());
            }
            
            return event;
            
        } catch (Exception e) {
            log.error("Erro ao converter entry para evento: {} (ID: {})", 
                     entry.getEventType(), entry.getId(), e);
            throw new EventStoreException("Falha na conversão do entry", e);
        }
    }
}