package com.seguradora.hibrida.domain.workflow.metrics;

import com.seguradora.hibrida.domain.workflow.execution.WorkflowInstance;
import com.seguradora.hibrida.domain.workflow.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Monitor de SLAs de workflows com verificação automática e escalação.
 * Executa periodicamente verificações de cumprimento de SLA e emite alertas.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlaMonitor {

    private final WorkflowInstanceRepository instanceRepository;
    private final SlaConfiguration slaConfiguration;
    private final WorkflowMetrics workflowMetrics;

    // Cache de últimos alertas para evitar spam
    private final Map<String, LocalDateTime> ultimosAlertas = new HashMap<>();

    /**
     * Monitora SLAs a cada hora.
     * Identifica workflows em risco e aciona alertas/escalações.
     */
    @Scheduled(fixedRate = 3600000) // A cada hora
    public void monitorarSlas() {
        log.info("Iniciando monitoramento de SLAs");

        List<WorkflowInstance> ativos = instanceRepository.findByStatusIn(
                List.of(
                        WorkflowInstance.StatusWorkflowInstance.INICIADO,
                        WorkflowInstance.StatusWorkflowInstance.EM_ANDAMENTO
                )
        );

        int totalAtivos = ativos.size();
        int emRisco = 0;
        int excedidos = 0;

        for (WorkflowInstance instance : ativos) {
            try {
                String tipoSinistro = instance.getContexto("tipoSinistro");
                if (tipoSinistro == null) {
                    tipoSinistro = "COMPLEXO"; // Default
                }

                long horasDecorridas = Duration.between(instance.getInicioEm(), LocalDateTime.now()).toHours();
                double percentualSla = slaConfiguration.calcularPercentualSla(tipoSinistro, horasDecorridas);

                SlaConfiguration.NivelAlerta nivelAlerta = slaConfiguration.getNivelAlerta(percentualSla);

                if (nivelAlerta != null) {
                    if (percentualSla >= 100) {
                        excedidos++;
                        log.warn("SLA EXCEDIDO: Workflow {} - Tipo: {} - {}% do SLA",
                                instance.getId(), tipoSinistro, String.format("%.1f", percentualSla));
                    } else {
                        emRisco++;
                        log.warn("SLA EM RISCO: Workflow {} - Tipo: {} - {}% do SLA - Nível: {}",
                                instance.getId(), tipoSinistro, String.format("%.1f", percentualSla), nivelAlerta);
                    }

                    gerarAlerta(instance, nivelAlerta, percentualSla, tipoSinistro);

                    // Verifica se deve escalar automaticamente
                    if (slaConfiguration.getEscalacao().isAutomatica() &&
                        percentualSla >= slaConfiguration.getEscalacao().getPercentualParaEscalar()) {
                        escalarAutomaticamente(instance, tipoSinistro, percentualSla);
                    }
                }

            } catch (Exception e) {
                log.error("Erro ao monitorar SLA do workflow {}: {}", instance.getId(), e.getMessage(), e);
            }
        }

        log.info("Monitoramento de SLA concluído - Total: {} | Em risco: {} | Excedidos: {}",
                totalAtivos, emRisco, excedidos);

        // Registra métricas
        workflowMetrics.calcularEstatisticasGerais();
    }

    /**
     * Identifica workflows em risco de SLA.
     *
     * @return lista de workflows em risco
     */
    public List<Map<String, Object>> identificarEmRisco() {
        log.debug("Identificando workflows em risco de SLA");

        List<WorkflowInstance> ativos = instanceRepository.findByStatusIn(
                List.of(
                        WorkflowInstance.StatusWorkflowInstance.INICIADO,
                        WorkflowInstance.StatusWorkflowInstance.EM_ANDAMENTO
                )
        );

        List<Map<String, Object>> emRisco = new ArrayList<>();

        for (WorkflowInstance instance : ativos) {
            String tipoSinistro = instance.getContexto("tipoSinistro");
            if (tipoSinistro == null) {
                tipoSinistro = "COMPLEXO";
            }

            long horasDecorridas = Duration.between(instance.getInicioEm(), LocalDateTime.now()).toHours();
            double percentualSla = slaConfiguration.calcularPercentualSla(tipoSinistro, horasDecorridas);

            if (percentualSla >= slaConfiguration.getAlertas().getPercentual50()) {
                Map<String, Object> info = new HashMap<>();
                info.put("workflowId", instance.getId());
                info.put("sinistroId", instance.getSinistroId());
                info.put("tipoSinistro", tipoSinistro);
                info.put("percentualSla", percentualSla);
                info.put("horasDecorridas", horasDecorridas);
                info.put("slaHoras", slaConfiguration.getSlaHoras(tipoSinistro));
                info.put("nivelAlerta", slaConfiguration.getNivelAlerta(percentualSla));
                info.put("excedido", percentualSla >= 100);

                emRisco.add(info);
            }
        }

        log.info("Identificados {} workflows em risco de SLA", emRisco.size());

        return emRisco;
    }

    /**
     * Gera alertas para workflows próximos ou acima do SLA.
     *
     * @param instance instância do workflow
     * @param nivelAlerta nível do alerta
     * @param percentualSla percentual do SLA consumido
     * @param tipoSinistro tipo do sinistro
     */
    public void gerarAlertas(WorkflowInstance instance, SlaConfiguration.NivelAlerta nivelAlerta,
                            double percentualSla, String tipoSinistro) {
        gerarAlerta(instance, nivelAlerta, percentualSla, tipoSinistro);
    }

    /**
     * Gera um alerta para um workflow.
     */
    private void gerarAlerta(WorkflowInstance instance, SlaConfiguration.NivelAlerta nivelAlerta,
                            double percentualSla, String tipoSinistro) {

        // Verifica se já foi enviado alerta recentemente
        String chaveAlerta = instance.getId() + "_" + nivelAlerta;
        LocalDateTime ultimoAlerta = ultimosAlertas.get(chaveAlerta);

        if (ultimoAlerta != null) {
            long minutosDesdeUltimoAlerta = Duration.between(ultimoAlerta, LocalDateTime.now()).toMinutes();
            if (minutosDesdeUltimoAlerta < slaConfiguration.getAlertas().getIntervaloMinutosEntreAlertas()) {
                log.debug("Alerta já enviado recentemente para workflow {}", instance.getId());
                return;
            }
        }

        // Registra o alerta
        ultimosAlertas.put(chaveAlerta, LocalDateTime.now());

        String mensagem = String.format(
                "ALERTA SLA [%s]: Workflow %s (Sinistro: %s) - Tipo: %s - %.1f%% do SLA consumido",
                nivelAlerta.getDescricao(),
                instance.getId(),
                instance.getSinistroId(),
                tipoSinistro,
                percentualSla
        );

        log.warn(mensagem);

        // Aqui seria integrado com sistema de notificações real
        // - Email para gestores
        // - Notificação push
        // - Integração com ferramentas de monitoramento (PagerDuty, etc)

        if (slaConfiguration.getAlertas().isEmailHabilitado()) {
            enviarAlertaEmail(instance, nivelAlerta, percentualSla, tipoSinistro);
        }

        if (slaConfiguration.getAlertas().isPushHabilitado()) {
            enviarAlertaPush(instance, nivelAlerta, percentualSla);
        }
    }

    /**
     * Escala automaticamente um workflow para nível superior.
     *
     * @param instance instância do workflow
     * @param tipoSinistro tipo do sinistro
     * @param percentualSla percentual do SLA
     */
    public void escalarAutomaticamente(WorkflowInstance instance, String tipoSinistro, double percentualSla) {
        log.warn("ESCALANDO AUTOMATICAMENTE: Workflow {} - {}% do SLA",
                instance.getId(), String.format("%.1f", percentualSla));

        // Aqui seria implementada a lógica de escalação real:
        // - Notificar níveis superiores de gestão
        // - Atribuir prioridade alta ao workflow
        // - Alocar recursos adicionais
        // - Registrar a escalação no histórico

        String mensagem = String.format(
                "Workflow escalado automaticamente devido a consumo de %.1f%% do SLA para tipo %s",
                percentualSla, tipoSinistro
        );

        log.info("Escalação automática registrada para workflow {}: {}", instance.getId(), mensagem);

        // Mock de notificação de escalação
        notificarEscalacao(instance, percentualSla);
    }

    /**
     * Verifica workflows sem movimentação há muito tempo.
     */
    @Scheduled(cron = "0 0 8,14,20 * * *") // 3x ao dia: 8h, 14h, 20h
    public void verificarWorkflowsParados() {
        log.info("Verificando workflows sem movimentação");

        int horasSemMovimentacao = slaConfiguration.getEscalacao().getHorasSemMovimentacao();
        LocalDateTime limite = LocalDateTime.now().minusHours(horasSemMovimentacao);

        List<WorkflowInstance> ativos = instanceRepository.findByStatusIn(
                List.of(WorkflowInstance.StatusWorkflowInstance.EM_ANDAMENTO)
        );

        int parados = 0;

        for (WorkflowInstance instance : ativos) {
            if (instance.ultimaEtapaExecutada() != null) {
                LocalDateTime ultimaMovimentacao = instance.ultimaEtapaExecutada().getFimEm();
                if (ultimaMovimentacao != null && ultimaMovimentacao.isBefore(limite)) {
                    log.warn("Workflow {} sem movimentação há mais de {}h",
                            instance.getId(), horasSemMovimentacao);

                    String tipoSinistro = instance.getContexto("tipoSinistro");
                    escalarAutomaticamente(instance, tipoSinistro != null ? tipoSinistro : "INDEFINIDO", 0);

                    parados++;
                }
            }
        }

        if (parados > 0) {
            log.warn("Identificados {} workflows sem movimentação há mais de {}h",
                    parados, horasSemMovimentacao);
        }
    }

    // Métodos auxiliares de notificação (mock)

    private void enviarAlertaEmail(WorkflowInstance instance, SlaConfiguration.NivelAlerta nivel,
                                   double percentualSla, String tipoSinistro) {
        log.debug("Mock: Enviando email de alerta nível {} para workflow {}", nivel, instance.getId());
        // Em produção, integraria com serviço de email
    }

    private void enviarAlertaPush(WorkflowInstance instance, SlaConfiguration.NivelAlerta nivel,
                                  double percentualSla) {
        log.debug("Mock: Enviando notificação push nível {} para workflow {}", nivel, instance.getId());
        // Em produção, integraria com serviço de notificações push
    }

    private void notificarEscalacao(WorkflowInstance instance, double percentualSla) {
        log.debug("Mock: Notificando escalação do workflow {}", instance.getId());
        // Em produção, notificaria gestores e sistemas relacionados
    }

    /**
     * Limpa cache de alertas antigos periodicamente.
     */
    @Scheduled(cron = "0 0 0 * * *") // Diariamente à meia-noite
    public void limparCacheAlertas() {
        int tamanhoAntes = ultimosAlertas.size();

        LocalDateTime limite = LocalDateTime.now().minusHours(24);
        ultimosAlertas.entrySet().removeIf(entry -> entry.getValue().isBefore(limite));

        int removidos = tamanhoAntes - ultimosAlertas.size();
        if (removidos > 0) {
            log.info("Cache de alertas limpo: {} entradas removidas", removidos);
        }
    }
}
