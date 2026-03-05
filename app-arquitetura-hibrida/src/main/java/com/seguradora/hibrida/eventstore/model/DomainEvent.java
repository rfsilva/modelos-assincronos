package com.seguradora.hibrida.eventstore.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Classe base para todos os eventos de domínio no sistema.
 * 
 * Implementa o padrão Domain Event com metadados essenciais para
 * Event Sourcing e rastreabilidade completa.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public abstract class DomainEvent {
    
    /**
     * ID único do evento.
     */
    private UUID eventId;
    
    /**
     * ID do aggregate que gerou o evento.
     */
    private String aggregateId;
    
    /**
     * Tipo do aggregate.
     */
    private String aggregateType;
    
    /**
     * Versão do aggregate quando o evento foi gerado.
     */
    private long version;
    
    /**
     * Timestamp de quando o evento foi gerado.
     */
    private Instant timestamp;
    
    /**
     * ID de correlação para rastreamento de operações.
     */
    private UUID correlationId;
    
    /**
     * ID da sessão/usuário que originou o evento.
     */
    private String userId;
    
    /**
     * Metadados adicionais do evento.
     */
    private EventMetadata metadata;
    
    /**
     * Construtor que inicializa campos obrigatórios.
     */
    protected DomainEvent(String aggregateId, String aggregateType, long version) {
        this.eventId = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = version;
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID();
        this.metadata = new EventMetadata();
    }
    
    /**
     * Retorna o nome simples da classe como tipo do evento.
     */
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Define o correlation ID para rastreamento.
     */
    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }
    
    /**
     * Define o usuário que originou o evento.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    /**
     * Adiciona metadado customizado.
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new EventMetadata();
        }
        this.metadata.put(key, value);
    }
}