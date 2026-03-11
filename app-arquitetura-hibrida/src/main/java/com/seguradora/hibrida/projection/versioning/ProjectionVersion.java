package com.seguradora.hibrida.projection.versioning;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * Entidade que representa uma versão de uma projeção.
 *
 * <p>Permite controle de evolução de schema das projeções, tracking
 * de migrações e rebuild automático quando necessário.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Entity
@Table(name = "projection_versions", schema = "eventstore")
@IdClass(ProjectionVersion.ProjectionVersionId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectionVersion {

    @Id
    @Column(name = "projection_name", length = 100, nullable = false)
    private String projectionName;

    @Id
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "schema_hash", length = 64, nullable = false)
    private String schemaHash;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 100, nullable = false)
    @Builder.Default
    private String createdBy = "SYSTEM";

    @Column(name = "migration_status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MigrationStatus migrationStatus = MigrationStatus.PENDING;

    @Column(name = "migration_started_at")
    private Instant migrationStartedAt;

    @Column(name = "migration_completed_at")
    private Instant migrationCompletedAt;

    @Column(name = "migration_error", columnDefinition = "TEXT")
    private String migrationError;

    @Column(name = "backward_compatible")
    @Builder.Default
    private Boolean backwardCompatible = false;

    @Column(name = "requires_rebuild", nullable = false)
    @Builder.Default
    private Boolean requiresRebuild = true;

    @Column(name = "estimated_rebuild_time_seconds")
    private Integer estimatedRebuildTimeSeconds;

    @Column(name = "events_to_process")
    @Builder.Default
    private Long eventsToProcess = 0L;

    @Column(name = "events_processed")
    @Builder.Default
    private Long eventsProcessed = 0L;

    // Métodos de negócio

    /**
     * Inicia uma migração.
     */
    public void startMigration() {
        this.migrationStatus = MigrationStatus.IN_PROGRESS;
        this.migrationStartedAt = Instant.now();
        this.migrationError = null;
    }

    /**
     * Completa uma migração com sucesso.
     */
    public void completeMigration() {
        this.migrationStatus = MigrationStatus.COMPLETED;
        this.migrationCompletedAt = Instant.now();
        this.requiresRebuild = false;
        this.migrationError = null;
    }

    /**
     * Marca migração como falha.
     */
    public void failMigration(String errorMessage) {
        this.migrationStatus = MigrationStatus.FAILED;
        this.migrationCompletedAt = Instant.now();
        this.migrationError = errorMessage;
    }

    /**
     * Verifica se a migração está completa.
     */
    public boolean isCompleted() {
        return migrationStatus == MigrationStatus.COMPLETED;
    }

    /**
     * Verifica se a migração está em andamento.
     */
    public boolean isInProgress() {
        return migrationStatus == MigrationStatus.IN_PROGRESS;
    }

    /**
     * Verifica se a migração falhou.
     */
    public boolean isFailed() {
        return migrationStatus == MigrationStatus.FAILED;
    }

    /**
     * Calcula o progresso da migração.
     */
    public double getProgress() {
        if (eventsToProcess == null || eventsToProcess == 0) {
            return 0.0;
        }
        return (double) (eventsProcessed != null ? eventsProcessed : 0) / eventsToProcess * 100.0;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @Override
    public String toString() {
        return "ProjectionVersion{" +
               "projectionName='" + projectionName + '\'' +
               ", version=" + version +
               ", status=" + migrationStatus +
               ", requiresRebuild=" + requiresRebuild +
               '}';
    }

    /**
     * Classe interna para chave composta.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectionVersionId implements Serializable {
        private String projectionName;
        private Integer version;
    }

    /**
     * Enum para status de migração.
     */
    public enum MigrationStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}
