package com.seguradora.hibrida.domain.workflow.repository;

import com.seguradora.hibrida.domain.workflow.model.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para definições de workflows.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, String> {

    /**
     * Busca definições ativas por tipo de sinistro.
     *
     * @param tipoSinistro tipo do sinistro
     * @return lista de definições ativas
     */
    List<WorkflowDefinition> findByTipoSinistroAndAtivoTrue(String tipoSinistro);

    /**
     * Busca a versão mais recente ativa de um tipo de sinistro.
     *
     * @param tipoSinistro tipo do sinistro
     * @return definição mais recente ou empty
     */
    @Query("SELECT w FROM WorkflowDefinition w WHERE w.tipoSinistro = :tipoSinistro " +
           "AND w.ativo = true ORDER BY w.versao DESC")
    Optional<WorkflowDefinition> findLatestByTipoSinistro(@Param("tipoSinistro") String tipoSinistro);

    /**
     * Busca todas as definições de um tipo de sinistro ordenadas por versão.
     *
     * @param tipoSinistro tipo do sinistro
     * @return lista de definições ordenadas
     */
    @Query("SELECT w FROM WorkflowDefinition w WHERE w.tipoSinistro = :tipoSinistro ORDER BY w.versao DESC")
    List<WorkflowDefinition> findAllByTipoSinistroOrderByVersaoDesc(@Param("tipoSinistro") String tipoSinistro);

    /**
     * Busca definições ativas.
     *
     * @return lista de definições ativas
     */
    List<WorkflowDefinition> findByAtivoTrue();

    /**
     * Busca definições por nome.
     *
     * @param nome nome do workflow
     * @return lista de definições com o nome
     */
    List<WorkflowDefinition> findByNomeContainingIgnoreCase(String nome);

    /**
     * Verifica se existe definição ativa para um tipo de sinistro.
     *
     * @param tipoSinistro tipo do sinistro
     * @return true se existe
     */
    boolean existsByTipoSinistroAndAtivoTrue(String tipoSinistro);

    /**
     * Conta o número de definições ativas.
     *
     * @return número de definições ativas
     */
    long countByAtivoTrue();
}
