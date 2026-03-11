package com.seguradora.hibrida.domain.workflow.repository;

import com.seguradora.hibrida.domain.workflow.approval.Aprovacao;
import com.seguradora.hibrida.domain.workflow.model.NivelAprovacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para aprovações de workflows.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Repository
public interface AprovacaoRepository extends JpaRepository<Aprovacao, String> {

    /**
     * Busca aprovações por sinistro.
     *
     * @param sinistroId ID do sinistro
     * @return lista de aprovações
     */
    List<Aprovacao> findBySinistroId(String sinistroId);

    /**
     * Busca aprovações por workflow instance.
     *
     * @param workflowInstanceId ID da instância do workflow
     * @return lista de aprovações
     */
    List<Aprovacao> findByWorkflowInstanceId(String workflowInstanceId);

    /**
     * Busca aprovações por status.
     *
     * @param status status da aprovação
     * @return lista de aprovações
     */
    List<Aprovacao> findByStatus(Aprovacao.StatusAprovacao status);

    /**
     * Busca aprovações por múltiplos status.
     *
     * @param status lista de status
     * @return lista de aprovações
     */
    List<Aprovacao> findByStatusIn(List<Aprovacao.StatusAprovacao> status);

    /**
     * Busca aprovações por sinistro e status.
     *
     * @param sinistroId ID do sinistro
     * @param status lista de status
     * @return lista de aprovações
     */
    List<Aprovacao> findBySinistroIdAndStatusIn(String sinistroId,
                                                List<Aprovacao.StatusAprovacao> status);

    /**
     * Busca aprovações de um aprovador por status.
     *
     * @param aprovadorId ID do aprovador
     * @param status lista de status
     * @return lista de aprovações pendentes do aprovador
     */
    List<Aprovacao> findByAprovadoresContainingAndStatusIn(String aprovadorId,
                                                           List<Aprovacao.StatusAprovacao> status);

    /**
     * Busca aprovações por nível.
     *
     * @param nivel nível de aprovação
     * @return lista de aprovações do nível
     */
    List<Aprovacao> findByNivel(NivelAprovacao nivel);

    /**
     * Busca aprovações expiradas ou próximas da data limite.
     *
     * @param dataLimite data limite de referência
     * @return lista de aprovações
     */
    @Query("SELECT a FROM Aprovacao a WHERE a.status IN ('PENDENTE', 'EM_ANALISE') " +
           "AND a.dataLimite <= :dataLimite")
    List<Aprovacao> findProximasDoTimeout(@Param("dataLimite") LocalDateTime dataLimite);

    /**
     * Busca aprovações pendentes de um aprovador.
     *
     * @param aprovadorId ID do aprovador
     * @return lista de aprovações pendentes
     */
    @Query("SELECT a FROM Aprovacao a WHERE :aprovadorId MEMBER OF a.aprovadores " +
           "AND a.status IN ('PENDENTE', 'EM_ANALISE')")
    List<Aprovacao> findPendentesPorAprovador(@Param("aprovadorId") String aprovadorId);

    /**
     * Conta aprovações pendentes de um aprovador.
     *
     * @param aprovadorId ID do aprovador
     * @return número de aprovações pendentes
     */
    @Query("SELECT COUNT(a) FROM Aprovacao a WHERE :aprovadorId MEMBER OF a.aprovadores " +
           "AND a.status IN ('PENDENTE', 'EM_ANALISE')")
    long countPendentesPorAprovador(@Param("aprovadorId") String aprovadorId);

    /**
     * Busca aprovações por período de solicitação.
     *
     * @param inicio data inicial
     * @param fim data final
     * @return lista de aprovações do período
     */
    List<Aprovacao> findByDataSolicitacaoBetween(LocalDateTime inicio, LocalDateTime fim);

    /**
     * Calcula tempo médio de aprovação por nível.
     *
     * @param nivel nível de aprovação
     * @return tempo médio em minutos
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, a.dataSolicitacao, a.dataDecisao)) FROM Aprovacao a " +
           "WHERE a.nivel = :nivel AND a.status = 'APROVADA'")
    Double calcularTempoMedioPorNivel(@Param("nivel") NivelAprovacao nivel);

    /**
     * Conta aprovações por status e nível.
     *
     * @param status status da aprovação
     * @param nivel nível de aprovação
     * @return número de aprovações
     */
    long countByStatusAndNivel(Aprovacao.StatusAprovacao status, NivelAprovacao nivel);
}
