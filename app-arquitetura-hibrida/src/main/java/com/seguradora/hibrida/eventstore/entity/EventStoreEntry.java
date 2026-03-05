package com.seguradora.hibrida.eventstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entidade JPA para persistência de eventos no Event Store.
 * 
 * Mapeia eventos de domínio para estrutura otimizada no PostgreSQL
 * com suporte a JSONB e índices compostos para performance.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Entity
@Table(name = "events", 
       indexes = {
           @Index(name = "idx_aggregate_version", columnList = "aggregateId, version", unique = true),
           @Index(name = "idx_aggregate_timestamp", columnList = "aggregateId, timestamp"),
           @Index(name = "idx_event_type_timestamp", columnList = "eventType, timestamp"),
           @Index(name = "idx_correlation_id", columnList = "correlationId"),
           @Index(name = "idx_timestamp", columnList = "timestamp")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStoreEntry {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "aggregateId", nullable = false, length = 255)
    private String aggregateId;
    
    @Column(name = "aggregateType", nullable = false, length = 100)
    private String aggregateType;
    
    @Column(name = "eventType", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "version", nullable = false)
    private Long version;
    
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    
    @Column(name = "correlationId", columnDefinition = "UUID")
    private UUID correlationId;
    
    @Column(name = "userId", length = 100)
    private String userId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "eventData", columnDefinition = "jsonb")
    private String eventData;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "compressed", nullable = false)
    @Builder.Default
    private Boolean compressed = false;
    
    @Column(name = "dataSize", nullable = false)
    @Builder.Default
    private Integer dataSize = 0;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (eventData != null) {
            dataSize = eventData.length();
        }
    }
}