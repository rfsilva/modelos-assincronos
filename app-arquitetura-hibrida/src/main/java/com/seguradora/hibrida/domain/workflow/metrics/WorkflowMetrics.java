package com.seguradora.hibrida.domain.workflow.metrics;

import com.seguradora.hibrida.domain.workflow.execution.WorkflowInstance;
import com.seguradora.hibrida.domain.workflow.repository.WorkflowInstanceRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Componente para cálculo e registro de métricas de workflows.
 * Fornece estatísticas sobre desempenho, taxa de sucesso e gargalos.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowMetrics {

    private final WorkflowInstanceRepository instanceRepository;
    private final MeterRegistry meterRegistry;

    /**
     * Calcula o tempo médio de execução para um tipo de workflow.
     *
     * @param tipoWorkflow tipo do workflow
     * @return tempo médio em minutos
     */
    public double calcularTempoMedio(String tipoWorkflow) {
        log.debug("Calculando tempo médio para workflow tipo: {}", tipoWorkflow);

        Double tempoMedio = instanceRepository.calcularTempoMedioPorTipo(tipoWorkflow);

        if (tempoMedio != null) {
            registrarMetrica("workflow.tempo.medio", tempoMedio, "tipo", tipoWorkflow);
            return tempoMedio;
        }

        return 0.0;
    }

    /**
     * Calcula a taxa de sucesso de um tipo de workflow.
     *
     * @param tipoWorkflow tipo do workflow
     * @return taxa de sucesso (0-100)
     */
    public double calcularTaxaSucesso(String tipoWorkflow) {
        log.debug("Calculando taxa de sucesso para workflow tipo: {}", tipoWorkflow);

        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        List<WorkflowInstance> instances = instanceRepository.findByInicioEmBetween(inicio, LocalDateTime.now());

        long total = instances.stream()
                .filter(w -> tipoWorkflow.equals(w.getContexto("tipoSinistro")))
                .count();

        long sucesso = instances.stream()
                .filter(w -> tipoWorkflow.equals(w.getContexto("tipoSinistro")))
                .filter(WorkflowInstance::isCompleta)
                .count();

        double taxaSucesso = total > 0 ? (sucesso * 100.0 / total) : 0.0;

        registrarMetrica("workflow.taxa.sucesso", taxaSucesso, "tipo", tipoWorkflow);

        log.info("Taxa de sucesso para {}: {:.2f}% ({}/{})", tipoWorkflow, taxaSucesso, sucesso, total);

        return taxaSucesso;
    }

    /**
     * Identifica gargalos em um tipo de workflow.
     *
     * @param tipoWorkflow tipo do workflow
     * @return mapa com etapas e tempo médio de execução
     */
    public Map<String, Double> identificarGargalos(String tipoWorkflow) {
        log.info("Identificando gargalos para workflow tipo: {}", tipoWorkflow);

        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        List<WorkflowInstance> instances = instanceRepository.findByInicioEmBetween(inicio, LocalDateTime.now());

        Map<String, List<Long>> temposPorEtapa = new HashMap<>();

        instances.stream()
                .filter(w -> tipoWorkflow.equals(w.getContexto("tipoSinistro")))
                .forEach(instance -> {
                    instance.getHistoricoEtapas().forEach(etapa -> {
                        if (etapa.isConcluida()) {
                            temposPorEtapa
                                    .computeIfAbsent(etapa.getEtapaNome(), k -> new ArrayList<>())
                                    .add(etapa.tempoExecucaoMinutos());
                        }
                    });
                });

        Map<String, Double> mediasPorEtapa = new HashMap<>();

        temposPorEtapa.forEach((etapa, tempos) -> {
            double media = tempos.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            mediasPorEtapa.put(etapa, media);

            registrarMetrica("workflow.etapa.tempo.medio", media,
                    "tipo", tipoWorkflow,
                    "etapa", etapa);
        });

        // Ordena por tempo decrescente para identificar gargalos
        Map<String, Double> gargalos = mediasPorEtapa.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        log.info("Top 5 gargalos para {}: {}", tipoWorkflow, gargalos);

        return gargalos;
    }

    /**
     * Obtém tendências de execução ao longo do tempo.
     *
     * @param periodo período em dias
     * @return mapa com data e número de workflows completos
     */
    public Map<LocalDateTime, Long> getTendencias(int periodo) {
        log.debug("Obtendo tendências dos últimos {} dias", periodo);

        LocalDateTime inicio = LocalDateTime.now().minusDays(periodo);
        List<WorkflowInstance> instances = instanceRepository.findByInicioEmBetween(inicio, LocalDateTime.now());

        Map<LocalDateTime, Long> tendencias = instances.stream()
                .filter(WorkflowInstance::isCompleta)
                .collect(Collectors.groupingBy(
                        w -> w.getFimEm().toLocalDate().atStartOfDay(),
                        Collectors.counting()
                ));

        return new TreeMap<>(tendencias);
    }

    /**
     * Calcula estatísticas gerais de workflows.
     *
     * @return mapa com estatísticas
     */
    public Map<String, Object> calcularEstatisticasGerais() {
        log.info("Calculando estatísticas gerais de workflows");

        Map<String, Object> estatisticas = new HashMap<>();

        // Total de workflows
        long total = instanceRepository.count();
        estatisticas.put("total", total);

        // Por status
        for (WorkflowInstance.StatusWorkflowInstance status : WorkflowInstance.StatusWorkflowInstance.values()) {
            long count = instanceRepository.countByStatus(status);
            estatisticas.put("status_" + status.name(), count);
            registrarMetrica("workflow.count", count, "status", status.name());
        }

        // Workflows ativos
        long ativos = instanceRepository.countByStatus(WorkflowInstance.StatusWorkflowInstance.EM_ANDAMENTO);
        estatisticas.put("ativos", ativos);

        // Workflows completados últimos 30 dias
        LocalDateTime trintaDiasAtras = LocalDateTime.now().minusDays(30);
        List<WorkflowInstance> completosRecentes = instanceRepository
                .findCompletosNoPeriodo(trintaDiasAtras, LocalDateTime.now());
        estatisticas.put("completos_30d", completosRecentes.size());

        // Tempo médio de execução
        if (!completosRecentes.isEmpty()) {
            double tempoMedio = completosRecentes.stream()
                    .mapToLong(WorkflowInstance::tempoExecucaoMinutos)
                    .average()
                    .orElse(0.0);
            estatisticas.put("tempo_medio_minutos", tempoMedio);
            registrarMetrica("workflow.tempo.medio.geral", tempoMedio);
        }

        // Taxa de sucesso geral
        long sucesso = completosRecentes.size();
        long falhas = instanceRepository.countByStatus(WorkflowInstance.StatusWorkflowInstance.FALHADO);
        double taxaSucesso = (sucesso + falhas) > 0 ? (sucesso * 100.0 / (sucesso + falhas)) : 0.0;
        estatisticas.put("taxa_sucesso", taxaSucesso);

        log.info("Estatísticas gerais calculadas: {}", estatisticas);

        return estatisticas;
    }

    /**
     * Registra workflows em risco de SLA.
     *
     * @param limiteHoras limite em horas
     * @return lista de workflows em risco
     */
    public List<WorkflowInstance> identificarEmRiscoSLA(int limiteHoras) {
        LocalDateTime limite = LocalDateTime.now().minusHours(limiteHoras);

        List<WorkflowInstance> emRisco = instanceRepository.findAtivosAntigos(limite);

        registrarMetrica("workflow.sla.em_risco", emRisco.size());

        log.warn("Identificados {} workflows em risco de SLA (mais de {}h ativos)",
                emRisco.size(), limiteHoras);

        return emRisco;
    }

    /**
     * Calcula distribuição de workflows por tipo.
     *
     * @return mapa com tipo e quantidade
     */
    public Map<String, Long> calcularDistribuicaoPorTipo() {
        List<WorkflowInstance> todas = instanceRepository.findAll();

        return todas.stream()
                .collect(Collectors.groupingBy(
                        w -> w.getContexto("tipoSinistro") != null ? w.getContexto("tipoSinistro") : "INDEFINIDO",
                        Collectors.counting()
                ));
    }

    /**
     * Registra uma métrica no registry.
     */
    private void registrarMetrica(String nome, double valor, String... tags) {
        List<Tag> tagList = new ArrayList<>();
        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                tagList.add(Tag.of(tags[i], tags[i + 1]));
            }
        }

        meterRegistry.gauge(nome, tagList, valor);
    }

    /**
     * Exporta métricas consolidadas.
     *
     * @return mapa com todas as métricas
     */
    public Map<String, Object> exportarMetricas() {
        Map<String, Object> metricas = new HashMap<>();

        metricas.put("estatisticas_gerais", calcularEstatisticasGerais());
        metricas.put("distribuicao_tipo", calcularDistribuicaoPorTipo());
        metricas.put("tendencias_7d", getTendencias(7));

        return metricas;
    }
}
