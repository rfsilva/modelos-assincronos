package com.seguradora.hibrida.domain.workflow.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Representa uma etapa individual dentro de um workflow de sinistro.
 * Define o comportamento, condições e ações de cada passo do processo.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Entity
@Table(name = "workflow_etapas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaWorkflow {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoEtapa tipo;

    @Column(nullable = false)
    private Integer ordem;

    @Column(name = "timeout_minutos")
    private Integer timeoutMinutos;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "workflow_etapa_condicoes", joinColumns = @JoinColumn(name = "etapa_id"))
    @MapKeyColumn(name = "chave")
    @Column(name = "valor", length = 500)
    @Builder.Default
    private Map<String, String> condicoes = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "workflow_etapa_acoes", joinColumns = @JoinColumn(name = "etapa_id"))
    @Column(name = "acao", length = 500)
    @Builder.Default
    private List<String> acoes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_aprovacao", length = 50)
    private NivelAprovacao nivelAprovacao;

    @Column(length = 1000)
    private String descricao;

    @Column(name = "permite_retry")
    @Builder.Default
    private Boolean permiteRetry = true;

    @Column(name = "max_tentativas")
    @Builder.Default
    private Integer maxTentativas = 3;

    @Column(name = "obrigatoria")
    @Builder.Default
    private Boolean obrigatoria = true;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    /**
     * Valida se a etapa está configurada corretamente.
     *
     * @throws IllegalStateException se a validação falhar
     */
    public void validar() {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalStateException("Nome da etapa é obrigatório");
        }

        if (tipo == null) {
            throw new IllegalStateException("Tipo da etapa é obrigatório");
        }

        if (ordem == null || ordem <= 0) {
            throw new IllegalStateException("Ordem deve ser maior que zero");
        }

        if (tipo == TipoEtapa.APROVACAO && nivelAprovacao == null) {
            throw new IllegalStateException("Etapa de aprovação deve ter nível definido");
        }

        if (timeoutMinutos != null && timeoutMinutos <= 0) {
            throw new IllegalStateException("Timeout deve ser maior que zero");
        }

        if (maxTentativas != null && maxTentativas <= 0) {
            throw new IllegalStateException("Número máximo de tentativas deve ser maior que zero");
        }
    }

    /**
     * Verifica se a etapa pode ser executada baseado nas condições e contexto.
     *
     * @param contexto contexto de execução com dados do sinistro
     * @return true se pode executar
     */
    public boolean podeExecutar(Map<String, Object> contexto) {
        if (condicoes == null || condicoes.isEmpty()) {
            return true; // Sem condições, sempre pode executar
        }

        // Avalia todas as condições
        for (Map.Entry<String, String> condicao : condicoes.entrySet()) {
            if (!avaliarCondicao(condicao.getKey(), condicao.getValue(), contexto)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Avalia uma condição específica contra o contexto.
     *
     * @param chave chave da condição
     * @param valorEsperado valor esperado
     * @param contexto contexto de execução
     * @return true se a condição é satisfeita
     */
    private boolean avaliarCondicao(String chave, String valorEsperado, Map<String, Object> contexto) {
        Object valorContexto = contexto.get(chave);

        if (valorContexto == null) {
            return "null".equals(valorEsperado) || valorEsperado == null;
        }

        // Suporta comparações básicas
        String valorStr = valorContexto.toString();

        if (valorEsperado.startsWith(">")) {
            try {
                double valor = Double.parseDouble(valorStr);
                double limite = Double.parseDouble(valorEsperado.substring(1).trim());
                return valor > limite;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (valorEsperado.startsWith("<")) {
            try {
                double valor = Double.parseDouble(valorStr);
                double limite = Double.parseDouble(valorEsperado.substring(1).trim());
                return valor < limite;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (valorEsperado.startsWith(">=")) {
            try {
                double valor = Double.parseDouble(valorStr);
                double limite = Double.parseDouble(valorEsperado.substring(2).trim());
                return valor >= limite;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (valorEsperado.startsWith("<=")) {
            try {
                double valor = Double.parseDouble(valorStr);
                double limite = Double.parseDouble(valorEsperado.substring(2).trim());
                return valor <= limite;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (valorEsperado.startsWith("!=")) {
            return !valorStr.equals(valorEsperado.substring(2).trim());
        }

        return valorStr.equals(valorEsperado);
    }

    /**
     * Retorna as ações automáticas que devem ser executadas nesta etapa.
     *
     * @return lista de ações automáticas
     */
    public List<String> getAcoesAutomaticas() {
        if (acoes == null) {
            return Collections.emptyList();
        }

        return acoes.stream()
                .filter(acao -> !acao.startsWith("MANUAL:"))
                .collect(Collectors.toList());
    }

    /**
     * Retorna as ações manuais que devem ser executadas nesta etapa.
     *
     * @return lista de ações manuais
     */
    public List<String> getAcoesManuais() {
        if (acoes == null) {
            return Collections.emptyList();
        }

        return acoes.stream()
                .filter(acao -> acao.startsWith("MANUAL:"))
                .map(acao -> acao.substring(7)) // Remove prefixo "MANUAL:"
                .collect(Collectors.toList());
    }

    /**
     * Verifica se a etapa está válida.
     *
     * @return true se está válida
     */
    public boolean isValida() {
        try {
            validar();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Verifica se a etapa requer aprovação.
     *
     * @return true se requer aprovação
     */
    public boolean requerAprovacao() {
        return tipo == TipoEtapa.APROVACAO;
    }

    /**
     * Verifica se a etapa é automática.
     *
     * @return true se é automática
     */
    public boolean isAutomatica() {
        return tipo == TipoEtapa.AUTOMATICA;
    }

    /**
     * Verifica se a etapa é manual.
     *
     * @return true se é manual
     */
    public boolean isManual() {
        return tipo == TipoEtapa.MANUAL;
    }

    /**
     * Verifica se a etapa é de integração.
     *
     * @return true se é de integração
     */
    public boolean isIntegracao() {
        return tipo == TipoEtapa.INTEGRACAO;
    }

    /**
     * Verifica se a etapa tem timeout configurado.
     *
     * @return true se tem timeout
     */
    public boolean hasTimeout() {
        return timeoutMinutos != null && timeoutMinutos > 0;
    }

    /**
     * Verifica se a etapa permite retry em caso de falha.
     *
     * @return true se permite retry
     */
    public boolean isPermiteRetry() {
        return Boolean.TRUE.equals(permiteRetry);
    }

    /**
     * Verifica se a etapa é obrigatória.
     *
     * @return true se é obrigatória
     */
    public boolean isObrigatoria() {
        return Boolean.TRUE.equals(obrigatoria);
    }

    /**
     * Adiciona uma condição à etapa.
     *
     * @param chave chave da condição
     * @param valor valor esperado
     */
    public void adicionarCondicao(String chave, String valor) {
        if (condicoes == null) {
            condicoes = new HashMap<>();
        }
        condicoes.put(chave, valor);
    }

    /**
     * Adiciona uma ação à etapa.
     *
     * @param acao descrição da ação
     */
    public void adicionarAcao(String acao) {
        if (acoes == null) {
            acoes = new ArrayList<>();
        }
        acoes.add(acao);
    }

    /**
     * Clona esta etapa criando uma nova instância.
     *
     * @return nova etapa clonada
     */
    public EtapaWorkflow clonar() {
        return EtapaWorkflow.builder()
                .id(UUID.randomUUID().toString())
                .nome(this.nome)
                .tipo(this.tipo)
                .ordem(this.ordem)
                .timeoutMinutos(this.timeoutMinutos)
                .condicoes(new HashMap<>(this.condicoes))
                .acoes(new ArrayList<>(this.acoes))
                .nivelAprovacao(this.nivelAprovacao)
                .descricao(this.descricao)
                .permiteRetry(this.permiteRetry)
                .maxTentativas(this.maxTentativas)
                .obrigatoria(this.obrigatoria)
                .build();
    }

    @Override
    public String toString() {
        return String.format("EtapaWorkflow[id=%s, nome=%s, tipo=%s, ordem=%d]",
                id, nome, tipo, ordem);
    }
}
