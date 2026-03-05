package com.seguradora.hibrida.eventstore.controller;

import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.config.EventStoreHealthIndicator;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller REST para monitoramento e administração do Event Store.
 * 
 * Fornece endpoints para consulta de eventos, estatísticas e
 * monitoramento da saúde do Event Store.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/eventstore")
@RequiredArgsConstructor
@Tag(name = "Event Store", description = "APIs para monitoramento e administração do Event Store")
public class EventStoreController {
    
    private final EventStore eventStore;
    private final EventStoreRepository repository;
    private final EventStoreHealthIndicator healthIndicator;
    
    @GetMapping("/events/{aggregateId}")
    @Operation(summary = "Buscar eventos por aggregate ID", 
               description = "Retorna todos os eventos de um aggregate específico")
    public ResponseEntity<List<DomainEvent>> getEventsByAggregateId(
            @Parameter(description = "ID do aggregate") 
            @PathVariable String aggregateId,
            
            @Parameter(description = "Versão inicial (opcional)")
            @RequestParam(required = false) Long fromVersion) {
        
        log.debug("Buscando eventos para aggregate: {}, fromVersion: {}", aggregateId, fromVersion);
        
        List<DomainEvent> events = fromVersion != null 
            ? eventStore.loadEvents(aggregateId, fromVersion)
            : eventStore.loadEvents(aggregateId);
        
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/events/type/{eventType}")
    @Operation(summary = "Buscar eventos por tipo", 
               description = "Retorna eventos de um tipo específico em um período")
    public ResponseEntity<List<DomainEvent>> getEventsByType(
            @Parameter(description = "Tipo do evento (nome da classe)")
            @PathVariable String eventType,
            
            @Parameter(description = "Data/hora inicial (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime from,
            
            @Parameter(description = "Data/hora final (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime to) {
        
        log.debug("Buscando eventos do tipo: {} entre {} e {}", eventType, from, to);
        
        Instant fromInstant = from.toInstant(ZoneOffset.UTC);
        Instant toInstant = to.toInstant(ZoneOffset.UTC);
        
        List<DomainEvent> events = eventStore.loadEventsByType(eventType, fromInstant, toInstant);
        
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/events/correlation/{correlationId}")
    @Operation(summary = "Buscar eventos por correlation ID", 
               description = "Retorna todos os eventos relacionados por correlation ID")
    public ResponseEntity<List<DomainEvent>> getEventsByCorrelationId(
            @Parameter(description = "ID de correlação")
            @PathVariable UUID correlationId) {
        
        log.debug("Buscando eventos com correlation ID: {}", correlationId);
        
        List<DomainEvent> events = eventStore.loadEventsByCorrelationId(correlationId);
        
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/aggregates/{aggregateId}/version")
    @Operation(summary = "Obter versão atual do aggregate", 
               description = "Retorna a versão atual de um aggregate")
    public ResponseEntity<Map<String, Object>> getAggregateVersion(
            @Parameter(description = "ID do aggregate")
            @PathVariable String aggregateId) {
        
        long version = eventStore.getCurrentVersion(aggregateId);
        boolean exists = eventStore.aggregateExists(aggregateId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("aggregateId", aggregateId);
        response.put("version", version);
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Estatísticas do Event Store", 
               description = "Retorna estatísticas gerais do Event Store")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @Parameter(description = "Período em horas para estatísticas (padrão: 24)")
            @RequestParam(defaultValue = "24") int hours) {
        
        log.debug("Gerando estatísticas para as últimas {} horas", hours);
        
        Instant from = Instant.now().minusSeconds(hours * 3600L);
        Instant to = Instant.now();
        
        List<Object[]> stats = repository.getEventStatistics(from, to);
        
        Map<String, Object> response = new HashMap<>();
        response.put("period", hours + " horas");
        response.put("from", from);
        response.put("to", to);
        
        Map<String, Map<String, Object>> eventStats = new HashMap<>();
        long totalEvents = 0;
        long totalSize = 0;
        
        for (Object[] stat : stats) {
            String eventType = (String) stat[0];
            Long count = (Long) stat[1];
            Double avgSize = (Double) stat[2];
            
            Map<String, Object> eventStat = new HashMap<>();
            eventStat.put("count", count);
            eventStat.put("averageSize", avgSize != null ? avgSize.intValue() : 0);
            
            eventStats.put(eventType, eventStat);
            totalEvents += count;
            totalSize += avgSize != null ? (avgSize.intValue() * count) : 0;
        }
        
        response.put("totalEvents", totalEvents);
        response.put("totalSize", totalSize);
        response.put("averageEventSize", totalEvents > 0 ? totalSize / totalEvents : 0);
        response.put("eventsByType", eventStats);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/events/recent")
    @Operation(summary = "Eventos recentes", 
               description = "Retorna os eventos mais recentes com paginação")
    public ResponseEntity<Page<com.seguradora.hibrida.eventstore.entity.EventStoreEntry>> getRecentEvents(
            @Parameter(description = "Página (inicia em 0)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Horas atrás para buscar (padrão: 24)")
            @RequestParam(defaultValue = "24") int hours) {
        
        log.debug("Buscando eventos recentes: página {}, tamanho {}, últimas {} horas", 
                 page, size, hours);
        
        Instant from = Instant.now().minusSeconds(hours * 3600L);
        Instant to = Instant.now();
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<com.seguradora.hibrida.eventstore.entity.EventStoreEntry> events = 
            repository.findEventsByPeriod(from, to, pageRequest);
        
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check do Event Store", 
               description = "Verifica a saúde do Event Store")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = healthIndicator.checkHealth();
        
        if ("DOWN".equals(health.get("status"))) {
            return ResponseEntity.status(503).body(health);
        }
        
        return ResponseEntity.ok(health);
    }
}