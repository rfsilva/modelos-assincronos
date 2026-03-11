package com.seguradora.hibrida.domain.workflow.engine;

import com.seguradora.hibrida.domain.workflow.execution.EtapaExecucao;
import com.seguradora.hibrida.domain.workflow.execution.WorkflowInstance;
import com.seguradora.hibrida.domain.workflow.execution.WorkflowResult;

import java.util.List;

/**
 * Interface principal do motor de execução de workflows.
 * Define as operações de gerenciamento e controle de workflows de sinistros.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
public interface WorkflowEngine {

    /**
     * Inicia um novo workflow para um sinistro.
     *
     * @param sinistroId ID do sinistro
     * @param tipoSinistro tipo do sinistro
     * @return instância de workflow criada
     */
    WorkflowInstance iniciar(String sinistroId, String tipoSinistro);

    /**
     * Avança o workflow para a próxima etapa.
     *
     * @param instanceId ID da instância do workflow
     * @return resultado da execução
     */
    WorkflowResult avancar(String instanceId);

    /**
     * Avança o workflow de forma assíncrona.
     *
     * @param instanceId ID da instância do workflow
     */
    void avancarAsync(String instanceId);

    /**
     * Cancela a execução de um workflow.
     *
     * @param instanceId ID da instância do workflow
     * @param motivo motivo do cancelamento
     */
    void cancelar(String instanceId, String motivo);

    /**
     * Pausa a execução de um workflow.
     *
     * @param instanceId ID da instância do workflow
     */
    void pausar(String instanceId);

    /**
     * Retoma a execução de um workflow pausado.
     *
     * @param instanceId ID da instância do workflow
     */
    void retomar(String instanceId);

    /**
     * Obtém o status atual de um workflow.
     *
     * @param instanceId ID da instância do workflow
     * @return instância do workflow
     */
    WorkflowInstance obterStatus(String instanceId);

    /**
     * Obtém o histórico de execução de um workflow.
     *
     * @param instanceId ID da instância do workflow
     * @return lista de etapas executadas
     */
    List<EtapaExecucao> obterHistorico(String instanceId);

    /**
     * Retorna um workflow para uma etapa anterior.
     *
     * @param instanceId ID da instância do workflow
     * @param etapaId ID da etapa para retornar
     */
    void retroceder(String instanceId, String etapaId);

    /**
     * Força a reexecução de uma etapa falhada.
     *
     * @param instanceId ID da instância do workflow
     * @param etapaExecucaoId ID da execução da etapa
     * @return resultado da reexecução
     */
    WorkflowResult retry(String instanceId, String etapaExecucaoId);

    /**
     * Lista todos os workflows ativos de um sinistro.
     *
     * @param sinistroId ID do sinistro
     * @return lista de instâncias de workflow
     */
    List<WorkflowInstance> listarPorSinistro(String sinistroId);

    /**
     * Verifica e processa workflows com timeout.
     */
    void processarTimeouts();

    /**
     * Executa o processamento de workflows pendentes em background.
     */
    void processarFilaPendente();
}
