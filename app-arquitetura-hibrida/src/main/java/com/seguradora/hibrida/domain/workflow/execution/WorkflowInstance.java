package com.seguradora.hibrida.domain.workflow.execution;

import com.seguradora.hibrida.domain.workflow.model.StatusEtapa;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Representa uma instância de execução de um workflow para um sinistro específico.
 * Mantém o estado atual, histórico e contexto de execução.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Entity
@Table(name = "workflow_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowInstance {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "definicao_id", nullable = false, length = 36)
    private String definicaoId;

    @Column(name = "sinistro_id", nullable = false, length = 36)
    private String sinistroId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusWorkflowInstance status;

    @Column(name = "etapa_atual_id", length = 36)
    private String etapaAtualId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "workflow_instance_id")
    @OrderBy("inicioEm DESC")
    @Builder.Default
    private List<EtapaExecucao> historicoEtapas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "workflow_instance_contexto", joinColumns = @JoinColumn(name = "instance_id"))
    @MapKeyColumn(name = "chave")
    @Column(name = "valor", length = 2000)
    @Builder.Default
    private Map<String, String> contexto = new HashMap<>();

    @Column(name = "inicio_em", nullable = false)
    private LocalDateTime inicioEm;

    @Column(name = "fim_em")
    private LocalDateTime fimEm;

    @Column(name = "pausado_em")
    private LocalDateTime pausadoEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @Column(name = "motivo_cancelamento", length = 1000)
    private String motivoCancelamento;

    @Column(name = "progresso_percentual")
    @Builder.Default
    private Integer progressoPercentual = 0;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (inicioEm == null) {
            inicioEm = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusWorkflowInstance.INICIADO;
        }
    }

    /**
     * Avança o workflow para a próxima etapa.
     *
     * @param proximaEtapaId ID da próxima etapa
     */
    public void avancar(String proximaEtapaId) {
        if (status == StatusWorkflowInstance.COMPLETO ||
            status == StatusWorkflowInstance.CANCELADO) {
            throw new IllegalStateException("Workflow já está finalizado");
        }

        this.etapaAtualId = proximaEtapaId;
        this.status = StatusWorkflowInstance.EM_ANDAMENTO;
        atualizarProgresso();
    }

    /**
     * Retrocede o workflow para uma etapa anterior.
     *
     * @param etapaAnteriorId ID da etapa anterior
     */
    public void retroceder(String etapaAnteriorId) {
        if (status == StatusWorkflowInstance.COMPLETO ||
            status == StatusWorkflowInstance.CANCELADO) {
            throw new IllegalStateException("Workflow já está finalizado");
        }

        this.etapaAtualId = etapaAnteriorId;
        this.status = StatusWorkflowInstance.EM_ANDAMENTO;
        atualizarProgresso();
    }

    /**
     * Cancela a execução do workflow.
     *
     * @param motivo motivo do cancelamento
     */
    public void cancelar(String motivo) {
        this.status = StatusWorkflowInstance.CANCELADO;
        this.canceladoEm = LocalDateTime.now();
        this.motivoCancelamento = motivo;
        this.fimEm = LocalDateTime.now();
    }

    /**
     * Pausa a execução do workflow.
     */
    public void pausar() {
        if (status == StatusWorkflowInstance.EM_ANDAMENTO) {
            this.status = StatusWorkflowInstance.PAUSADO;
            this.pausadoEm = LocalDateTime.now();
        }
    }

    /**
     * Retoma a execução do workflow pausado.
     */
    public void retomar() {
        if (status == StatusWorkflowInstance.PAUSADO) {
            this.status = StatusWorkflowInstance.EM_ANDAMENTO;
            this.pausadoEm = null;
        }
    }

    /**
     * Marca o workflow como completo.
     */
    public void completar() {
        this.status = StatusWorkflowInstance.COMPLETO;
        this.fimEm = LocalDateTime.now();
        this.progressoPercentual = 100;
    }

    /**
     * Marca o workflow como falhado.
     *
     * @param motivo motivo da falha
     */
    public void falhar(String motivo) {
        this.status = StatusWorkflowInstance.FALHADO;
        this.fimEm = LocalDateTime.now();
        this.motivoCancelamento = motivo;
    }

    /**
     * Verifica se o workflow está completo.
     *
     * @return true se está completo
     */
    public boolean isCompleta() {
        return status == StatusWorkflowInstance.COMPLETO;
    }

    /**
     * Verifica se o workflow está em andamento.
     *
     * @return true se está em andamento
     */
    public boolean isEmAndamento() {
        return status == StatusWorkflowInstance.EM_ANDAMENTO;
    }

    /**
     * Verifica se o workflow está pausado.
     *
     * @return true se está pausado
     */
    public boolean isPausado() {
        return status == StatusWorkflowInstance.PAUSADO;
    }

    /**
     * Verifica se o workflow foi cancelado.
     *
     * @return true se foi cancelado
     */
    public boolean isCancelado() {
        return status == StatusWorkflowInstance.CANCELADO;
    }

    /**
     * Verifica se o workflow falhou.
     *
     * @return true se falhou
     */
    public boolean isFalhado() {
        return status == StatusWorkflowInstance.FALHADO;
    }

    /**
     * Calcula o tempo total de execução do workflow.
     *
     * @return duração da execução
     */
    public Duration tempoExecucao() {
        LocalDateTime fim = fimEm != null ? fimEm : LocalDateTime.now();
        return Duration.between(inicioEm, fim);
    }

    /**
     * Retorna o tempo de execução em minutos.
     *
     * @return minutos de execução
     */
    public long tempoExecucaoMinutos() {
        return tempoExecucao().toMinutes();
    }

    /**
     * Retorna o tempo de execução em horas.
     *
     * @return horas de execução
     */
    public long tempoExecucaoHoras() {
        return tempoExecucao().toHours();
    }

    /**
     * Adiciona uma etapa ao histórico de execução.
     *
     * @param etapa etapa executada
     */
    public void adicionarEtapaHistorico(EtapaExecucao etapa) {
        if (historicoEtapas == null) {
            historicoEtapas = new ArrayList<>();
        }
        historicoEtapas.add(etapa);
    }

    /**
     * Retorna a última etapa executada.
     *
     * @return última etapa ou null se não houver
     */
    public EtapaExecucao ultimaEtapaExecutada() {
        if (historicoEtapas == null || historicoEtapas.isEmpty()) {
            return null;
        }
        return historicoEtapas.get(0); // Já ordenado por DESC
    }

    /**
     * Retorna as etapas concluídas.
     *
     * @return lista de etapas concluídas
     */
    public List<EtapaExecucao> etapasConcluidas() {
        if (historicoEtapas == null) {
            return Collections.emptyList();
        }
        return historicoEtapas.stream()
                .filter(e -> e.getStatus() == StatusEtapa.CONCLUIDA)
                .toList();
    }

    /**
     * Retorna as etapas falhadas.
     *
     * @return lista de etapas falhadas
     */
    public List<EtapaExecucao> etapasFalhadas() {
        if (historicoEtapas == null) {
            return Collections.emptyList();
        }
        return historicoEtapas.stream()
                .filter(e -> e.getStatus() == StatusEtapa.FALHADA)
                .toList();
    }

    /**
     * Atualiza o progresso percentual do workflow.
     */
    private void atualizarProgresso() {
        // Progresso baseado no histórico de etapas concluídas
        // Nota: Precisa do total de etapas da definição para cálculo preciso
        if (historicoEtapas != null && !historicoEtapas.isEmpty()) {
            long concluidas = etapasConcluidas().size();
            // Estimativa simplificada - idealmente deveria usar total de etapas da definição
            this.progressoPercentual = Math.min(95, (int) (concluidas * 15));
        }
    }

    /**
     * Define um valor no contexto de execução.
     *
     * @param chave chave do valor
     * @param valor valor a armazenar
     */
    public void setContexto(String chave, String valor) {
        if (contexto == null) {
            contexto = new HashMap<>();
        }
        contexto.put(chave, valor);
    }

    /**
     * Obtém um valor do contexto de execução.
     *
     * @param chave chave do valor
     * @return valor ou null se não existir
     */
    public String getContexto(String chave) {
        return contexto != null ? contexto.get(chave) : null;
    }

    /**
     * Verifica se o workflow está ativo (pode ser executado).
     *
     * @return true se está ativo
     */
    public boolean isAtivo() {
        return status == StatusWorkflowInstance.INICIADO ||
               status == StatusWorkflowInstance.EM_ANDAMENTO;
    }

    @Override
    public String toString() {
        return String.format("WorkflowInstance[id=%s, sinistro=%s, status=%s, etapaAtual=%s, progresso=%d%%]",
                id, sinistroId, status, etapaAtualId, progressoPercentual);
    }

    /**
     * Enum para status da instância de workflow.
     */
    public enum StatusWorkflowInstance {
        INICIADO,
        EM_ANDAMENTO,
        PAUSADO,
        COMPLETO,
        FALHADO,
        CANCELADO
    }
}
