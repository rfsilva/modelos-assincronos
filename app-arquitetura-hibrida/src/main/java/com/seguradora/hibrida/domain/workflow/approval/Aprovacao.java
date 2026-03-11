package com.seguradora.hibrida.domain.workflow.approval;

import com.seguradora.hibrida.domain.workflow.model.NivelAprovacao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa uma solicitação de aprovação de sinistro dentro de um workflow.
 * Gerencia o processo de aprovação com múltiplos níveis hierárquicos.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Entity
@Table(name = "workflow_aprovacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aprovacao {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "workflow_instance_id", nullable = false, length = 36)
    private String workflowInstanceId;

    @Column(name = "sinistro_id", nullable = false, length = 36)
    private String sinistroId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NivelAprovacao nivel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "workflow_aprovacao_aprovadores", joinColumns = @JoinColumn(name = "aprovacao_id"))
    @Column(name = "aprovador_id", length = 36)
    @Builder.Default
    private List<String> aprovadores = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusAprovacao status;

    @Column(name = "valor_sinistro", precision = 15, scale = 2)
    private BigDecimal valorSinistro;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_limite")
    private LocalDateTime dataLimite;

    @Column(name = "data_decisao")
    private LocalDateTime dataDecisao;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private DecisaoAprovacao decisao;

    @Column(name = "aprovador_decisao_id", length = 36)
    private String aprovadorDecisaoId;

    @Column(name = "aprovador_decisao_nome", length = 200)
    private String aprovadorDecisaoNome;

    @Column(name = "justificativa", length = 2000)
    private String justificativa;

    @Column(name = "observacoes", length = 2000)
    private String observacoes;

    @Column(name = "expirada")
    @Builder.Default
    private Boolean expirada = false;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (dataSolicitacao == null) {
            dataSolicitacao = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusAprovacao.PENDENTE;
        }
    }

    /**
     * Aprova a solicitação.
     *
     * @param aprovadorId ID do aprovador
     * @param aprovadorNome nome do aprovador
     * @param justificativa justificativa da aprovação
     */
    public void aprovar(String aprovadorId, String aprovadorNome, String justificativa) {
        validarPodeAprovar(aprovadorId);

        this.status = StatusAprovacao.APROVADA;
        this.decisao = DecisaoAprovacao.APROVADO;
        this.aprovadorDecisaoId = aprovadorId;
        this.aprovadorDecisaoNome = aprovadorNome;
        this.justificativa = justificativa;
        this.dataDecisao = LocalDateTime.now();
    }

    /**
     * Rejeita a solicitação.
     *
     * @param aprovadorId ID do aprovador
     * @param aprovadorNome nome do aprovador
     * @param motivo motivo da rejeição
     */
    public void rejeitar(String aprovadorId, String aprovadorNome, String motivo) {
        validarPodeAprovar(aprovadorId);

        this.status = StatusAprovacao.REJEITADA;
        this.decisao = DecisaoAprovacao.REJEITADO;
        this.aprovadorDecisaoId = aprovadorId;
        this.aprovadorDecisaoNome = aprovadorNome;
        this.justificativa = motivo;
        this.dataDecisao = LocalDateTime.now();
    }

    /**
     * Delega a aprovação para outros aprovadores.
     *
     * @param novosAprovadores lista de novos aprovadores
     */
    public void delegar(List<String> novosAprovadores) {
        if (status != StatusAprovacao.PENDENTE && status != StatusAprovacao.EM_ANALISE) {
            throw new IllegalStateException("Aprovação não pode ser delegada no status: " + status);
        }

        if (novosAprovadores == null || novosAprovadores.isEmpty()) {
            throw new IllegalArgumentException("Lista de aprovadores não pode ser vazia");
        }

        this.aprovadores = new ArrayList<>(novosAprovadores);
        this.status = StatusAprovacao.PENDENTE;
    }

    /**
     * Marca a aprovação como expirada por timeout.
     */
    public void expirar() {
        if (status == StatusAprovacao.PENDENTE || status == StatusAprovacao.EM_ANALISE) {
            this.status = StatusAprovacao.EXPIRADA;
            this.expirada = true;
            this.dataDecisao = LocalDateTime.now();
        }
    }

    /**
     * Marca a aprovação como em análise.
     *
     * @param aprovadorId ID do aprovador que iniciou análise
     */
    public void iniciarAnalise(String aprovadorId) {
        if (status != StatusAprovacao.PENDENTE) {
            throw new IllegalStateException("Aprovação deve estar pendente para iniciar análise");
        }

        if (!aprovadores.contains(aprovadorId)) {
            throw new IllegalArgumentException("Aprovador não autorizado: " + aprovadorId);
        }

        this.status = StatusAprovacao.EM_ANALISE;
    }

    /**
     * Escala a aprovação para o próximo nível.
     *
     * @param novosAprovadores aprovadores do próximo nível
     */
    public void escalar(List<String> novosAprovadores) {
        NivelAprovacao proximoNivel = nivel.getProximo();

        if (proximoNivel == null) {
            throw new IllegalStateException("Já está no nível máximo de aprovação");
        }

        this.nivel = proximoNivel;
        this.aprovadores = new ArrayList<>(novosAprovadores);
        this.status = StatusAprovacao.PENDENTE;
    }

    /**
     * Valida se o aprovador pode tomar decisão.
     */
    private void validarPodeAprovar(String aprovadorId) {
        if (status == StatusAprovacao.APROVADA || status == StatusAprovacao.REJEITADA) {
            throw new IllegalStateException("Aprovação já foi decidida");
        }

        if (status == StatusAprovacao.EXPIRADA) {
            throw new IllegalStateException("Aprovação expirada");
        }

        if (!aprovadores.contains(aprovadorId)) {
            throw new IllegalArgumentException("Aprovador não autorizado: " + aprovadorId);
        }
    }

    /**
     * Verifica se a aprovação está pendente.
     *
     * @return true se está pendente
     */
    public boolean isPendente() {
        return status == StatusAprovacao.PENDENTE;
    }

    /**
     * Verifica se a aprovação está em análise.
     *
     * @return true se está em análise
     */
    public boolean isEmAnalise() {
        return status == StatusAprovacao.EM_ANALISE;
    }

    /**
     * Verifica se a aprovação foi aprovada.
     *
     * @return true se foi aprovada
     */
    public boolean isAprovada() {
        return status == StatusAprovacao.APROVADA;
    }

    /**
     * Verifica se a aprovação foi rejeitada.
     *
     * @return true se foi rejeitada
     */
    public boolean isRejeitada() {
        return status == StatusAprovacao.REJEITADA;
    }

    /**
     * Verifica se a aprovação expirou.
     *
     * @return true se expirou
     */
    public boolean isExpirada() {
        return Boolean.TRUE.equals(expirada) || status == StatusAprovacao.EXPIRADA;
    }

    /**
     * Verifica se a data limite foi excedida.
     *
     * @return true se excedeu o prazo
     */
    public boolean excedeuPrazo() {
        return dataLimite != null && LocalDateTime.now().isAfter(dataLimite);
    }

    /**
     * Verifica se a aprovação está ativa (pode ser processada).
     *
     * @return true se está ativa
     */
    public boolean isAtiva() {
        return status == StatusAprovacao.PENDENTE || status == StatusAprovacao.EM_ANALISE;
    }

    /**
     * Adiciona uma observação.
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

    @Override
    public String toString() {
        return String.format("Aprovacao[id=%s, sinistro=%s, nivel=%s, status=%s, valor=%s]",
                id, sinistroId, nivel, status, valorSinistro);
    }

    /**
     * Enum para status de aprovação.
     */
    public enum StatusAprovacao {
        PENDENTE,
        EM_ANALISE,
        APROVADA,
        REJEITADA,
        EXPIRADA
    }

    /**
     * Enum para decisão de aprovação.
     */
    public enum DecisaoAprovacao {
        APROVADO,
        REJEITADO,
        ESCALADO
    }
}
