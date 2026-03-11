package com.seguradora.hibrida.domain.workflow.engine;

import com.seguradora.hibrida.domain.workflow.execution.*;
import com.seguradora.hibrida.domain.workflow.model.*;
import com.seguradora.hibrida.domain.workflow.repository.WorkflowDefinitionRepository;
import com.seguradora.hibrida.domain.workflow.repository.WorkflowInstanceRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/**
 * Implementação completa do motor de execução de workflows.
 * Gerencia todo o ciclo de vida de workflows de sinistros com execução assíncrona,
 * retry automático, timeout handling e persistência de estado.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEngineImpl implements WorkflowEngine {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowExecutor executor;
    private final MeterRegistry meterRegistry;

    // Fila de tarefas prioritárias para processamento assíncrono
    private final PriorityBlockingQueue<WorkflowTask> filaTarefas = new PriorityBlockingQueue<>();

    // Cache de instâncias em execução
    private final Map<String, WorkflowInstance> cacheInstancias = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public WorkflowInstance iniciar(String sinistroId, String tipoSinistro) {
        log.info("Iniciando workflow para sinistro {} do tipo {}", sinistroId, tipoSinistro);

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // Busca a definição ativa para o tipo de sinistro
            WorkflowDefinition definicao = definitionRepository
                    .findByTipoSinistroAndAtivoTrue(tipoSinistro)
                    .stream()
                    .max(Comparator.comparing(WorkflowDefinition::getVersao))
                    .orElseThrow(() -> new IllegalStateException(
                            "Nenhuma definição de workflow ativa encontrada para tipo: " + tipoSinistro));

            // Valida a definição
            definicao.validar();

            // Cria a instância do workflow
            WorkflowInstance instance = WorkflowInstance.builder()
                    .id(UUID.randomUUID().toString())
                    .definicaoId(definicao.getId())
                    .sinistroId(sinistroId)
                    .status(WorkflowInstance.StatusWorkflowInstance.INICIADO)
                    .inicioEm(LocalDateTime.now())
                    .historicoEtapas(new ArrayList<>())
                    .contexto(new HashMap<>())
                    .progressoPercentual(0)
                    .build();

            // Define contexto inicial
            instance.setContexto("tipoSinistro", tipoSinistro);
            instance.setContexto("definicaoId", definicao.getId());
            instance.setContexto("versaoWorkflow", String.valueOf(definicao.getVersao()));

            // Persiste a instância
            instance = instanceRepository.save(instance);

            // Adiciona ao cache
            cacheInstancias.put(instance.getId(), instance);

            // Inicia a primeira etapa automaticamente
            EtapaWorkflow primeiraEtapa = definicao.primeiraEtapa();
            instance.avancar(primeiraEtapa.getId());
            instanceRepository.save(instance);

            // Se a primeira etapa é automática, executa de forma assíncrona
            if (primeiraEtapa.isAutomatica()) {
                avancarAsync(instance.getId());
            }

            sample.stop(Timer.builder("workflow.iniciar")
                    .tag("tipo", tipoSinistro)
                    .register(meterRegistry));

            log.info("Workflow {} iniciado com sucesso para sinistro {}", instance.getId(), sinistroId);

            return instance;

        } catch (Exception e) {
            log.error("Erro ao iniciar workflow para sinistro {}: {}", sinistroId, e.getMessage(), e);
            meterRegistry.counter("workflow.iniciar.erro",
                    "tipo", tipoSinistro,
                    "erro", e.getClass().getSimpleName()).increment();
            throw new RuntimeException("Erro ao iniciar workflow: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public WorkflowResult avancar(String instanceId) {
        log.info("Avançando workflow {}", instanceId);

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // Obtém a instância
            WorkflowInstance instance = obterInstancia(instanceId);

            if (!instance.isAtivo()) {
                return WorkflowResult.failure("Workflow não está ativo: " + instance.getStatus());
            }

            // Obtém a definição
            WorkflowDefinition definicao = obterDefinicao(instance.getDefinicaoId());

            // Obtém a etapa atual
            String etapaAtualId = instance.getEtapaAtualId();
            if (etapaAtualId == null) {
                return WorkflowResult.failure("Nenhuma etapa atual definida");
            }

            EtapaWorkflow etapaAtual = definicao.buscarEtapa(etapaAtualId);
            if (etapaAtual == null) {
                return WorkflowResult.failure("Etapa atual não encontrada: " + etapaAtualId);
            }

            // Cria o contexto de execução
            WorkflowContext contexto = criarContexto(instance);

            // Verifica se pode executar a etapa
            if (!etapaAtual.podeExecutar(contexto.toMap())) {
                return WorkflowResult.failure("Condições da etapa não satisfeitas");
            }

            // Cria a execução da etapa
            EtapaExecucao execucao = EtapaExecucao.builder()
                    .id(UUID.randomUUID().toString())
                    .etapaId(etapaAtual.getId())
                    .etapaNome(etapaAtual.getNome())
                    .status(StatusEtapa.PENDENTE)
                    .tentativas(0)
                    .build();

            // Inicia a execução
            execucao.iniciar();
            instance.adicionarEtapaHistorico(execucao);

            // Executa a etapa
            WorkflowResult resultado = executor.executeEtapa(etapaAtual, contexto);

            // Atualiza a execução baseado no resultado
            if (resultado.isSucesso()) {
                execucao.concluir(resultado.getMensagem());

                // Atualiza contexto com dados do resultado
                if (resultado.getDados() != null) {
                    resultado.getDados().forEach((k, v) ->
                            instance.setContexto(k, v != null ? v.toString() : null));
                }

                // Verifica se há próxima etapa
                EtapaWorkflow proximaEtapa = definicao.proximaEtapa(etapaAtual);

                if (proximaEtapa != null) {
                    instance.avancar(proximaEtapa.getId());

                    // Se a próxima etapa é automática, agenda execução
                    if (proximaEtapa.isAutomatica()) {
                        agendarExecucaoAsync(instanceId, 0);
                    }
                } else {
                    // Workflow completo
                    instance.completar();
                    log.info("Workflow {} completado com sucesso", instanceId);
                }

            } else {
                execucao.falhar(resultado.getMensagem());

                // Verifica se permite retry
                if (resultado.isPermiteRetry() && execucao.getTentativas() < etapaAtual.getMaxTentativas()) {
                    log.warn("Etapa {} falhou, agendando retry. Tentativa {}/{}",
                            etapaAtual.getNome(), execucao.getTentativas(), etapaAtual.getMaxTentativas());

                    execucao.retry();
                    agendarExecucaoAsync(instanceId, calcularDelayRetry(execucao.getTentativas()));
                } else {
                    instance.falhar("Etapa " + etapaAtual.getNome() + " falhou: " + resultado.getMensagem());
                }
            }

            // Persiste as mudanças
            instanceRepository.save(instance);

            sample.stop(Timer.builder("workflow.avancar")
                    .tag("sucesso", String.valueOf(resultado.isSucesso()))
                    .register(meterRegistry));

            return resultado;

        } catch (Exception e) {
            log.error("Erro ao avançar workflow {}: {}", instanceId, e.getMessage(), e);
            meterRegistry.counter("workflow.avancar.erro",
                    "erro", e.getClass().getSimpleName()).increment();
            return WorkflowResult.failure("Erro ao avançar workflow: " + e.getMessage());
        }
    }

    @Override
    @Async("workflowTaskExecutor")
    public void avancarAsync(String instanceId) {
        log.debug("Executando avanço assíncrono do workflow {}", instanceId);
        avancar(instanceId);
    }

    @Override
    @Transactional
    public void cancelar(String instanceId, String motivo) {
        log.info("Cancelando workflow {} - Motivo: {}", instanceId, motivo);

        WorkflowInstance instance = obterInstancia(instanceId);
        instance.cancelar(motivo);
        instanceRepository.save(instance);

        // Remove do cache
        cacheInstancias.remove(instanceId);

        meterRegistry.counter("workflow.cancelar").increment();
    }

    @Override
    @Transactional
    public void pausar(String instanceId) {
        log.info("Pausando workflow {}", instanceId);

        WorkflowInstance instance = obterInstancia(instanceId);
        instance.pausar();
        instanceRepository.save(instance);

        meterRegistry.counter("workflow.pausar").increment();
    }

    @Override
    @Transactional
    public void retomar(String instanceId) {
        log.info("Retomando workflow {}", instanceId);

        WorkflowInstance instance = obterInstancia(instanceId);
        instance.retomar();
        instanceRepository.save(instance);

        // Se a etapa atual é automática, agenda execução
        WorkflowDefinition definicao = obterDefinicao(instance.getDefinicaoId());
        EtapaWorkflow etapaAtual = definicao.buscarEtapa(instance.getEtapaAtualId());

        if (etapaAtual != null && etapaAtual.isAutomatica()) {
            avancarAsync(instanceId);
        }

        meterRegistry.counter("workflow.retomar").increment();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowInstance obterStatus(String instanceId) {
        return obterInstancia(instanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaExecucao> obterHistorico(String instanceId) {
        WorkflowInstance instance = obterInstancia(instanceId);
        return new ArrayList<>(instance.getHistoricoEtapas());
    }

    @Override
    @Transactional
    public void retroceder(String instanceId, String etapaId) {
        log.info("Retrocedendo workflow {} para etapa {}", instanceId, etapaId);

        WorkflowInstance instance = obterInstancia(instanceId);
        WorkflowDefinition definicao = obterDefinicao(instance.getDefinicaoId());

        EtapaWorkflow etapa = definicao.buscarEtapa(etapaId);
        if (etapa == null) {
            throw new IllegalArgumentException("Etapa não encontrada: " + etapaId);
        }

        instance.retroceder(etapaId);
        instanceRepository.save(instance);

        meterRegistry.counter("workflow.retroceder").increment();
    }

    @Override
    @Transactional
    public WorkflowResult retry(String instanceId, String etapaExecucaoId) {
        log.info("Reexecutando etapa {} do workflow {}", etapaExecucaoId, instanceId);

        WorkflowInstance instance = obterInstancia(instanceId);

        // Busca a execução da etapa no histórico
        EtapaExecucao execucao = instance.getHistoricoEtapas().stream()
                .filter(e -> e.getId().equals(etapaExecucaoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Execução de etapa não encontrada"));

        if (!execucao.podeRetry()) {
            return WorkflowResult.failure("Etapa não permite retry no status atual");
        }

        // Reseta o status para permitir reexecução
        execucao.retry();

        // Retrocede para esta etapa
        instance.retroceder(execucao.getEtapaId());

        instanceRepository.save(instance);

        // Avança novamente
        return avancar(instanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowInstance> listarPorSinistro(String sinistroId) {
        return instanceRepository.findBySinistroId(sinistroId);
    }

    @Override
    @Transactional
    public void processarTimeouts() {
        log.debug("Processando timeouts de workflows");

        List<WorkflowInstance> instanciasAtivas = instanceRepository
                .findByStatusIn(Arrays.asList(
                        WorkflowInstance.StatusWorkflowInstance.INICIADO,
                        WorkflowInstance.StatusWorkflowInstance.EM_ANDAMENTO
                ));

        int timeoutsProcessados = 0;

        for (WorkflowInstance instance : instanciasAtivas) {
            try {
                WorkflowDefinition definicao = obterDefinicao(instance.getDefinicaoId());
                EtapaWorkflow etapaAtual = definicao.buscarEtapa(instance.getEtapaAtualId());

                if (etapaAtual == null || !etapaAtual.hasTimeout()) {
                    continue;
                }

                EtapaExecucao execucaoAtual = instance.ultimaEtapaExecutada();

                if (execucaoAtual != null &&
                    execucaoAtual.isEmAndamento() &&
                    execucaoAtual.excedeuTimeout(etapaAtual.getTimeoutMinutos())) {

                    log.warn("Timeout detectado no workflow {} na etapa {}",
                            instance.getId(), etapaAtual.getNome());

                    execucaoAtual.timeout();

                    // Verifica se permite retry
                    if (etapaAtual.isPermiteRetry() &&
                        execucaoAtual.getTentativas() < etapaAtual.getMaxTentativas()) {
                        execucaoAtual.retry();
                        agendarExecucaoAsync(instance.getId(), 0);
                    } else {
                        instance.falhar("Timeout na etapa: " + etapaAtual.getNome());
                    }

                    instanceRepository.save(instance);
                    timeoutsProcessados++;
                }

            } catch (Exception e) {
                log.error("Erro ao processar timeout do workflow {}: {}",
                        instance.getId(), e.getMessage(), e);
            }
        }

        if (timeoutsProcessados > 0) {
            log.info("Processados {} timeouts de workflows", timeoutsProcessados);
            meterRegistry.counter("workflow.timeout.processados").increment(timeoutsProcessados);
        }
    }

    @Override
    @Async("workflowTaskExecutor")
    public void processarFilaPendente() {
        log.debug("Processando fila de tarefas pendentes. Tamanho: {}", filaTarefas.size());

        while (!filaTarefas.isEmpty()) {
            WorkflowTask task = filaTarefas.poll();
            if (task != null) {
                try {
                    avancar(task.getInstanceId());
                } catch (Exception e) {
                    log.error("Erro ao processar task do workflow {}: {}",
                            task.getInstanceId(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Obtém uma instância do workflow, primeiro do cache, depois do banco.
     */
    private WorkflowInstance obterInstancia(String instanceId) {
        WorkflowInstance instance = cacheInstancias.get(instanceId);

        if (instance == null) {
            instance = instanceRepository.findById(instanceId)
                    .orElseThrow(() -> new IllegalArgumentException("Workflow não encontrado: " + instanceId));
            cacheInstancias.put(instanceId, instance);
        }

        return instance;
    }

    /**
     * Obtém uma definição de workflow.
     */
    private WorkflowDefinition obterDefinicao(String definicaoId) {
        return definitionRepository.findById(definicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Definição de workflow não encontrada: " + definicaoId));
    }

    /**
     * Cria o contexto de execução a partir da instância.
     */
    private WorkflowContext criarContexto(WorkflowInstance instance) {
        Map<String, Object> contextoMap = instance.getContexto().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return WorkflowContext.fromMap(contextoMap);
    }

    /**
     * Agenda execução assíncrona com delay.
     */
    private void agendarExecucaoAsync(String instanceId, long delayMillis) {
        WorkflowTask task = new WorkflowTask(instanceId, System.currentTimeMillis() + delayMillis);
        filaTarefas.offer(task);
    }

    /**
     * Calcula o delay para retry exponencial.
     */
    private long calcularDelayRetry(int tentativa) {
        // Exponential backoff: 1s, 2s, 4s, 8s, ...
        return (long) Math.pow(2, tentativa - 1) * 1000;
    }

    /**
     * Classe interna para representar uma tarefa de workflow na fila.
     */
    @lombok.Value
    private static class WorkflowTask implements Comparable<WorkflowTask> {
        String instanceId;
        long scheduledTime;

        @Override
        public int compareTo(WorkflowTask other) {
            return Long.compare(this.scheduledTime, other.scheduledTime);
        }
    }
}
