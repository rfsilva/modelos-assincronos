package com.seguradora.hibrida.domain.workflow.repository;

import com.seguradora.hibrida.domain.workflow.execution.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para instâncias de workflows.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, String> {

    /**
     * Busca workflows por sinistro.
     *
     * @param sinistroId ID do sinistro
     * @return lista de workflows do sinistro
     */
    List<WorkflowInstance> findBySinistroId(String sinistroId);

    /**
     * Busca workflows por status.
     *
     * @param status status do workflow
     * @return lista de workflows com o status
     */
    List<WorkflowInstance> findByStatus(WorkflowInstance.StatusWorkflowInstance status);

    /**
     * Busca workflows por múltiplos status.
     *
     * @param status lista de status
     * @return lista de workflows
     */
    List<WorkflowInstance> findByStatusIn(List<WorkflowInstance.StatusWorkflowInstance> status);

    /**
     * Busca workflows por definição.
     *
     * @param definicaoId ID da definição
     * @return lista de workflows da definição
     */
    List<WorkflowInstance> findByDefinicaoId(String definicaoId);

    /**
     * Busca workflows iniciados em um período.
     *
     * @param inicio data inicial
     * @param fim data final
     * @return lista de workflows do período
     */
    List<WorkflowInstance> findByInicioEmBetween(LocalDateTime inicio, LocalDateTime fim);

    /**
     * Busca workflows completos em um período.
     *
     * @param inicio data inicial
     * @param fim data final
     * @return lista de workflows completados
     */
    @Query("SELECT w FROM WorkflowInstance w WHERE w.status = 'COMPLETO' " +
           "AND w.fimEm BETWEEN :inicio AND :fim")
    List<WorkflowInstance> findCompletosNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                                   @Param("fim") LocalDateTime fim);

    /**
     * Busca workflows ativos há mais tempo que o especificado.
     *
     * @param tempo tempo limite
     * @return lista de workflows antigos
     */
    @Query("SELECT w FROM WorkflowInstance w WHERE w.status IN ('INICIADO', 'EM_ANDAMENTO') " +
           "AND w.inicioEm < :tempo")
    List<WorkflowInstance> findAtivosAntigos(@Param("tempo") LocalDateTime tempo);

    /**
     * Conta workflows por status.
     *
     * @param status status do workflow
     * @return número de workflows
     */
    long countByStatus(WorkflowInstance.StatusWorkflowInstance status);

    /**
     * Busca workflows pendentes (sem etapa atual).
     *
     * @return lista de workflows pendentes
     */
    @Query("SELECT w FROM WorkflowInstance w WHERE w.status IN ('INICIADO', 'EM_ANDAMENTO') " +
           "AND w.etapaAtualId IS NULL")
    List<WorkflowInstance> findPendentes();

    /**
     * Calcula tempo médio de execução por tipo de sinistro.
     *
     * @param tipoSinistro tipo do sinistro
     * @return tempo médio em minutos
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, w.inicioEm, w.fimEm)) FROM WorkflowInstance w " +
           "WHERE w.status = 'COMPLETO' " +
           "AND w.contexto['tipoSinistro'] = :tipoSinistro")
    Double calcularTempoMedioPorTipo(@Param("tipoSinistro") String tipoSinistro);

    /**
     * Busca workflows por sinistro e status.
     *
     * @param sinistroId ID do sinistro
     * @param status status do workflow
     * @return lista de workflows
     */
    List<WorkflowInstance> findBySinistroIdAndStatus(String sinistroId,
                                                     WorkflowInstance.StatusWorkflowInstance status);
}
