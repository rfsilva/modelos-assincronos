package com.seguradora.hibrida.domain.workflow.execution;

import com.seguradora.hibrida.domain.workflow.model.StatusEtapa;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa a execução de uma etapa específica dentro de uma instância de workflow.
 * Mantém o estado, resultado e histórico de tentativas da etapa.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Entity
@Table(name = "workflow_etapa_execucao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaExecucao {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "etapa_id", nullable = false, length = 36)
    private String etapaId;

    @Column(name = "etapa_nome", nullable = false, length = 200)
    private String etapaNome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusEtapa status;

    @Column(name = "responsavel_id", length = 36)
    private String responsavelId;

    @Column(name = "responsavel_nome", length = 200)
    private String responsavelNome;

    @Column(name = "inicio_em")
    private LocalDateTime inicioEm;

    @Column(name = "fim_em")
    private LocalDateTime fimEm;

    @Column(name = "resultado", length = 2000)
    private String resultado;

    @Column(name = "observacoes", length = 2000)
    private String observacoes;

    @Column(name = "tentativas")
    @Builder.Default
    private Integer tentativas = 0;

    @Column(name = "erro_mensagem", length = 2000)
    private String erroMensagem;

    @Column(name = "timeout_em")
    private LocalDateTime timeoutEm;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (status == null) {
            status = StatusEtapa.PENDENTE;
        }
    }

    /**
     * Inicia a execução da etapa.
     *
     * @param responsavelId ID do responsável pela execução
     * @param responsavelNome nome do responsável
     */
    public void iniciar(String responsavelId, String responsavelNome) {
        if (status != StatusEtapa.PENDENTE && status != StatusEtapa.FALHADA) {
            throw new IllegalStateException("Etapa só pode ser iniciada se estiver pendente ou após falha");
        }

        this.status = StatusEtapa.EM_ANDAMENTO;
        this.inicioEm = LocalDateTime.now();
        this.responsavelId = responsavelId;
        this.responsavelNome = responsavelNome;
        this.tentativas++;
    }

    /**
     * Inicia a execução da etapa sem responsável (automática).
     */
    public void iniciar() {
        iniciar("SISTEMA", "Execução Automática");
    }

    /**
     * Conclui a execução da etapa com sucesso.
     *
     * @param resultado resultado da execução
     */
    public void concluir(String resultado) {
        if (status != StatusEtapa.EM_ANDAMENTO) {
            throw new IllegalStateException("Etapa deve estar em andamento para ser concluída");
        }

        this.status = StatusEtapa.CONCLUIDA;
        this.fimEm = LocalDateTime.now();
        this.resultado = resultado;
    }

    /**
     * Conclui a execução da etapa com sucesso sem resultado.
     */
    public void concluir() {
        concluir("Execução concluída com sucesso");
    }

    /**
     * Marca a etapa como falhada.
     *
     * @param erroMensagem mensagem de erro
     */
    public void falhar(String erroMensagem) {
        if (status != StatusEtapa.EM_ANDAMENTO) {
            throw new IllegalStateException("Etapa deve estar em andamento para falhar");
        }

        this.status = StatusEtapa.FALHADA;
        this.fimEm = LocalDateTime.now();
        this.erroMensagem = erroMensagem;
    }

    /**
     * Marca a etapa como timeout.
     */
    public void timeout() {
        this.status = StatusEtapa.TIMEOUT;
        this.fimEm = LocalDateTime.now();
        this.timeoutEm = LocalDateTime.now();
        this.erroMensagem = "Tempo limite de execução excedido";
    }

    /**
     * Cancela a execução da etapa.
     *
     * @param motivo motivo do cancelamento
     */
    public void cancelar(String motivo) {
        this.status = StatusEtapa.CANCELADA;
        this.fimEm = LocalDateTime.now();
        this.observacoes = motivo;
    }

    /**
     * Realiza uma nova tentativa de execução (retry).
     */
    public void retry() {
        if (!status.podeRetry()) {
            throw new IllegalStateException("Etapa não permite retry no status atual: " + status);
        }

        this.status = StatusEtapa.PENDENTE;
        this.erroMensagem = null;
        this.fimEm = null;
    }

    /**
     * Verifica se a etapa está concluída.
     *
     * @return true se está concluída
     */
    public boolean isConcluida() {
        return status == StatusEtapa.CONCLUIDA;
    }

    /**
     * Verifica se a etapa está em andamento.
     *
     * @return true se está em andamento
     */
    public boolean isEmAndamento() {
        return status == StatusEtapa.EM_ANDAMENTO;
    }

    /**
     * Verifica se a etapa falhou.
     *
     * @return true se falhou
     */
    public boolean isFalhada() {
        return status == StatusEtapa.FALHADA;
    }

    /**
     * Verifica se a etapa teve timeout.
     *
     * @return true se teve timeout
     */
    public boolean isTimeout() {
        return status == StatusEtapa.TIMEOUT;
    }

    /**
     * Verifica se a etapa foi cancelada.
     *
     * @return true se foi cancelada
     */
    public boolean isCancelada() {
        return status == StatusEtapa.CANCELADA;
    }

    /**
     * Verifica se a etapa está pendente.
     *
     * @return true se está pendente
     */
    public boolean isPendente() {
        return status == StatusEtapa.PENDENTE;
    }

    /**
     * Verifica se a etapa pode ser retentada.
     *
     * @return true se pode retry
     */
    public boolean podeRetry() {
        return status.podeRetry();
    }

    /**
     * Calcula o tempo de execução da etapa.
     *
     * @return duração da execução
     */
    public Duration tempoExecucao() {
        if (inicioEm == null) {
            return Duration.ZERO;
        }

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
     * Retorna o tempo de execução em segundos.
     *
     * @return segundos de execução
     */
    public long tempoExecucaoSegundos() {
        return tempoExecucao().getSeconds();
    }

    /**
     * Verifica se a etapa excedeu o timeout configurado.
     *
     * @param timeoutMinutos timeout em minutos
     * @return true se excedeu o timeout
     */
    public boolean excedeuTimeout(int timeoutMinutos) {
        if (inicioEm == null || fimEm != null) {
            return false;
        }

        long minutosDecorridos = Duration.between(inicioEm, LocalDateTime.now()).toMinutes();
        return minutosDecorridos > timeoutMinutos;
    }

    /**
     * Adiciona uma observação à etapa.
     *
     * @param obs observação a adicionar
     */
    public void adicionarObservacao(String obs) {
        if (this.observacoes == null || this.observacoes.isEmpty()) {
            this.observacoes = obs;
        } else {
            this.observacoes += "\n" + obs;
        }
    }

    /**
     * Verifica se a etapa tem responsável definido.
     *
     * @return true se tem responsável
     */
    public boolean hasResponsavel() {
        return responsavelId != null && !responsavelId.isEmpty();
    }

    /**
     * Verifica se a etapa está ativa (em execução ou pendente).
     *
     * @return true se está ativa
     */
    public boolean isAtiva() {
        return status == StatusEtapa.EM_ANDAMENTO || status == StatusEtapa.PENDENTE;
    }

    /**
     * Verifica se a etapa está finalizada.
     *
     * @return true se está finalizada
     */
    public boolean isFinalizada() {
        return status.isFinal();
    }

    @Override
    public String toString() {
        return String.format("EtapaExecucao[id=%s, nome=%s, status=%s, tentativas=%d, tempo=%dmin]",
                id, etapaNome, status, tentativas, tempoExecucaoMinutos());
    }
}
