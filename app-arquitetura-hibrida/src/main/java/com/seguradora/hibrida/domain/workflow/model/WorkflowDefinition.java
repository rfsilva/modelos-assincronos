package com.seguradora.hibrida.domain.workflow.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Define a estrutura e configuração de um workflow de sinistro.
 * Representa o template/modelo que será usado para criar instâncias de execução.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Entity
@Table(name = "workflow_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDefinition {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false)
    private Integer versao;

    @Column(name = "tipo_sinistro", nullable = false, length = 50)
    private String tipoSinistro;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "workflow_definition_id")
    @OrderBy("ordem ASC")
    @Builder.Default
    private List<EtapaWorkflow> etapas = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(length = 1000)
    private String descricao;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (versao == null) {
            versao = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    /**
     * Valida se a definição do workflow está consistente e completa.
     *
     * @throws IllegalStateException se a validação falhar
     */
    public void validar() {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalStateException("Nome do workflow é obrigatório");
        }

        if (tipoSinistro == null || tipoSinistro.trim().isEmpty()) {
            throw new IllegalStateException("Tipo de sinistro é obrigatório");
        }

        if (etapas == null || etapas.isEmpty()) {
            throw new IllegalStateException("Workflow deve ter pelo menos uma etapa");
        }

        // Validar ordem sequencial das etapas
        List<Integer> ordens = etapas.stream()
                .map(EtapaWorkflow::getOrdem)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < ordens.size(); i++) {
            if (ordens.get(i) != i + 1) {
                throw new IllegalStateException("Ordem das etapas deve ser sequencial começando em 1");
            }
        }

        // Validar cada etapa
        etapas.forEach(EtapaWorkflow::validar);
    }

    /**
     * Retorna a próxima etapa após a etapa atual.
     *
     * @param etapaAtual etapa atual em execução
     * @return próxima etapa ou null se não houver
     */
    public EtapaWorkflow proximaEtapa(EtapaWorkflow etapaAtual) {
        if (etapaAtual == null) {
            return primeiraEtapa();
        }

        int ordemAtual = etapaAtual.getOrdem();
        return etapas.stream()
                .filter(e -> e.getOrdem() == ordemAtual + 1)
                .findFirst()
                .orElse(null);
    }

    /**
     * Retorna a primeira etapa do workflow.
     *
     * @return primeira etapa
     */
    public EtapaWorkflow primeiraEtapa() {
        return etapas.stream()
                .filter(e -> e.getOrdem() == 1)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Workflow não possui primeira etapa"));
    }

    /**
     * Retorna a última etapa do workflow.
     *
     * @return última etapa
     */
    public EtapaWorkflow ultimaEtapa() {
        return etapas.stream()
                .max((e1, e2) -> Integer.compare(e1.getOrdem(), e2.getOrdem()))
                .orElseThrow(() -> new IllegalStateException("Workflow não possui etapas"));
    }

    /**
     * Busca uma etapa pelo ID.
     *
     * @param etapaId ID da etapa
     * @return etapa encontrada
     */
    public EtapaWorkflow buscarEtapa(String etapaId) {
        return etapas.stream()
                .filter(e -> e.getId().equals(etapaId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Busca uma etapa pela ordem.
     *
     * @param ordem número da ordem
     * @return etapa encontrada
     */
    public EtapaWorkflow buscarEtapaPorOrdem(int ordem) {
        return etapas.stream()
                .filter(e -> e.getOrdem() == ordem)
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica se o workflow está completo (todas etapas foram definidas).
     *
     * @return true se está completo
     */
    public boolean isCompleto() {
        return etapas != null && !etapas.isEmpty() &&
               etapas.stream().allMatch(EtapaWorkflow::isValida);
    }

    /**
     * Retorna o número total de etapas.
     *
     * @return quantidade de etapas
     */
    public int totalEtapas() {
        return etapas != null ? etapas.size() : 0;
    }

    /**
     * Verifica se o workflow está ativo.
     *
     * @return true se está ativo
     */
    public boolean isAtivo() {
        return Boolean.TRUE.equals(ativo);
    }

    /**
     * Adiciona uma etapa ao workflow.
     *
     * @param etapa etapa a ser adicionada
     */
    public void adicionarEtapa(EtapaWorkflow etapa) {
        if (etapas == null) {
            etapas = new ArrayList<>();
        }
        etapas.add(etapa);
    }

    /**
     * Remove uma etapa do workflow.
     *
     * @param etapaId ID da etapa a ser removida
     */
    public void removerEtapa(String etapaId) {
        if (etapas != null) {
            etapas.removeIf(e -> e.getId().equals(etapaId));
        }
    }

    /**
     * Desativa o workflow.
     */
    public void desativar() {
        this.ativo = false;
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Ativa o workflow.
     */
    public void ativar() {
        validar(); // Valida antes de ativar
        this.ativo = true;
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Cria uma nova versão deste workflow.
     *
     * @return nova definição com versão incrementada
     */
    public WorkflowDefinition criarNovaVersao() {
        WorkflowDefinition novaVersao = WorkflowDefinition.builder()
                .id(UUID.randomUUID().toString())
                .nome(this.nome)
                .versao(this.versao + 1)
                .tipoSinistro(this.tipoSinistro)
                .descricao(this.descricao)
                .ativo(false) // Nova versão começa inativa
                .criadoEm(LocalDateTime.now())
                .etapas(new ArrayList<>())
                .build();

        // Copia as etapas
        this.etapas.forEach(etapa -> {
            EtapaWorkflow novaEtapa = etapa.clonar();
            novaVersao.adicionarEtapa(novaEtapa);
        });

        return novaVersao;
    }

    @Override
    public String toString() {
        return String.format("WorkflowDefinition[id=%s, nome=%s, versao=%d, tipoSinistro=%s, etapas=%d, ativo=%s]",
                id, nome, versao, tipoSinistro, totalEtapas(), ativo);
    }
}
