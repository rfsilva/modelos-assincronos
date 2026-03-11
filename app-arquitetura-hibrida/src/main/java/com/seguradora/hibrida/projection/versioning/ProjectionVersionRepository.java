package com.seguradora.hibrida.projection.versioning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para acesso a versões de projeções.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Repository
public interface ProjectionVersionRepository extends JpaRepository<ProjectionVersion, ProjectionVersion.ProjectionVersionId> {

    /**
     * Busca a versão mais recente de uma projeção.
     */
    Optional<ProjectionVersion> findFirstByProjectionNameOrderByVersionDesc(String projectionName);

    /**
     * Busca todas as versões de uma projeção.
     */
    List<ProjectionVersion> findByProjectionNameOrderByVersionDesc(String projectionName);

    /**
     * Busca projeções que precisam de rebuild.
     */
    @Query("SELECT pv FROM ProjectionVersion pv WHERE pv.requiresRebuild = true AND pv.migrationStatus IN ('PENDING', 'FAILED')")
    List<ProjectionVersion> findProjectionsNeedingRebuild();

    /**
     * Busca projeções com migração em andamento.
     */
    List<ProjectionVersion> findByMigrationStatus(ProjectionVersion.MigrationStatus status);

    /**
     * Verifica se existe uma versão específica.
     */
    boolean existsByProjectionNameAndVersion(String projectionName, Integer version);

    /**
     * Conta quantas versões precisam de rebuild.
     */
    @Query("SELECT COUNT(pv) FROM ProjectionVersion pv WHERE pv.requiresRebuild = true")
    long countProjectionsNeedingRebuild();
}
