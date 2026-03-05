package com.seguradora.hibrida.projection.tracking;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidade para rastreamento de posição das projeções.
 * 
 * <p>Mantém o controle de qual foi o último evento processado
 * por cada projeção, permitindo recovery e monitoramento de lag.
 */
@Entity
@Table(name = "projection_tracking", schema = "eventstore")
public class ProjectionTracker {
    
    @Id
    @Column(name = "projection_name", length = 100)
    private String projectionName;
    
    @Column(name = "last_processed_event_id", nullable = false)
    private Long lastProcessedEventId = 0L;
    
    @Column(name = "last_processed_at", nullable = false)
    private Instant lastProcessedAt = Instant.now();
    
    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectionStatus status = ProjectionStatus.ACTIVE;
    
    @Column(name = "events_processed", nullable = false)
    private Long eventsProcessed = 0L;
    
    @Column(name = "events_failed", nullable = false)
    private Long eventsFailed = 0L;
    
    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;
    
    @Column(name = "last_error_at")
    private Instant lastErrorAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Version
    private Long version;
    
    // Construtores
    public ProjectionTracker() {}
    
    public ProjectionTracker(String projectionName) {
        this.projectionName = projectionName;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    // Métodos de negócio
    
    /**
     * Atualiza a posição da projeção após processar um evento com sucesso.
     */
    public void updatePosition(Long eventId) {
        this.lastProcessedEventId = eventId;
        this.lastProcessedAt = Instant.now();
        this.eventsProcessed++;
        this.updatedAt = Instant.now();
        
        // Limpar erro anterior se houver
        if (this.status == ProjectionStatus.ERROR) {
            this.status = ProjectionStatus.ACTIVE;
            this.lastErrorMessage = null;
            this.lastErrorAt = null;
        }
    }
    
    /**
     * Registra uma falha no processamento.
     */
    public void recordFailure(String errorMessage) {
        this.eventsFailed++;
        this.lastErrorMessage = errorMessage;
        this.lastErrorAt = Instant.now();
        this.status = ProjectionStatus.ERROR;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Pausa a projeção.
     */
    public void pause() {
        this.status = ProjectionStatus.PAUSED;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Retoma a projeção.
     */
    public void resume() {
        this.status = ProjectionStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Calcula o lag em relação ao último evento disponível.
     */
    public long calculateLag(Long maxAvailableEventId) {
        if (maxAvailableEventId == null) {
            return 0L;
        }
        return Math.max(0L, maxAvailableEventId - this.lastProcessedEventId);
    }
    
    /**
     * Verifica se a projeção está saudável.
     */
    public boolean isHealthy() {
        return status == ProjectionStatus.ACTIVE && 
               (lastErrorAt == null || lastErrorAt.isBefore(lastProcessedAt));
    }
    
    /**
     * Obtém taxa de erro (falhas / total processado).
     */
    public double getErrorRate() {
        long total = eventsProcessed + eventsFailed;
        return total > 0 ? (double) eventsFailed / total : 0.0;
    }
    
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Getters e Setters
    public String getProjectionName() {
        return projectionName;
    }
    
    public void setProjectionName(String projectionName) {
        this.projectionName = projectionName;
    }
    
    public Long getLastProcessedEventId() {
        return lastProcessedEventId;
    }
    
    public void setLastProcessedEventId(Long lastProcessedEventId) {
        this.lastProcessedEventId = lastProcessedEventId;
    }
    
    public Instant getLastProcessedAt() {
        return lastProcessedAt;
    }
    
    public void setLastProcessedAt(Instant lastProcessedAt) {
        this.lastProcessedAt = lastProcessedAt;
    }
    
    public ProjectionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProjectionStatus status) {
        this.status = status;
    }
    
    public Long getEventsProcessed() {
        return eventsProcessed;
    }
    
    public void setEventsProcessed(Long eventsProcessed) {
        this.eventsProcessed = eventsProcessed;
    }
    
    public Long getEventsFailed() {
        return eventsFailed;
    }
    
    public void setEventsFailed(Long eventsFailed) {
        this.eventsFailed = eventsFailed;
    }
    
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
    
    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }
    
    public Instant getLastErrorAt() {
        return lastErrorAt;
    }
    
    public void setLastErrorAt(Instant lastErrorAt) {
        this.lastErrorAt = lastErrorAt;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "ProjectionTracker{" +
               "projectionName='" + projectionName + '\'' +
               ", lastProcessedEventId=" + lastProcessedEventId +
               ", status=" + status +
               ", eventsProcessed=" + eventsProcessed +
               ", eventsFailed=" + eventsFailed +
               '}';
    }
}