package com.seguradora.hibrida.domain.workflow.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa uma transição condicional entre etapas de um workflow.
 * Define quando e como o workflow pode avançar de uma etapa para outra.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Entity
@Table(name = "workflow_transicoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransicaoWorkflow {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "etapa_origem_id", nullable = false, length = 36)
    private String etapaOrigemId;

    @Column(name = "etapa_destino_id", nullable = false, length = 36)
    private String etapaDestinoId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "workflow_transicao_condicoes", joinColumns = @JoinColumn(name = "transicao_id"))
    @MapKeyColumn(name = "chave")
    @Column(name = "valor", length = 500)
    @Builder.Default
    private Map<String, String> condicoes = new HashMap<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "prioridade")
    @Builder.Default
    private Integer prioridade = 0;

    @Column(length = 1000)
    private String descricao;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    /**
     * Verifica se pode transicionar baseado no contexto de execução.
     *
     * @param contexto contexto com dados do workflow e sinistro
     * @return true se pode transicionar
     */
    public boolean podeTransicionar(Map<String, Object> contexto) {
        if (!Boolean.TRUE.equals(ativo)) {
            return false;
        }

        if (condicoes == null || condicoes.isEmpty()) {
            return true; // Sem condições, sempre pode transicionar
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

        String valorStr = valorContexto.toString();

        // Suporta operadores de comparação
        if (valorEsperado.startsWith(">=")) {
            return compararNumerico(valorStr, valorEsperado.substring(2).trim(), ">=");
        } else if (valorEsperado.startsWith("<=")) {
            return compararNumerico(valorStr, valorEsperado.substring(2).trim(), "<=");
        } else if (valorEsperado.startsWith(">")) {
            return compararNumerico(valorStr, valorEsperado.substring(1).trim(), ">");
        } else if (valorEsperado.startsWith("<")) {
            return compararNumerico(valorStr, valorEsperado.substring(1).trim(), "<");
        } else if (valorEsperado.startsWith("!=")) {
            return !valorStr.equals(valorEsperado.substring(2).trim());
        } else if (valorEsperado.startsWith("==")) {
            return valorStr.equals(valorEsperado.substring(2).trim());
        } else if (valorEsperado.contains("*")) {
            // Suporta wildcard simples
            String pattern = valorEsperado.replace("*", ".*");
            return valorStr.matches(pattern);
        }

        // Comparação direta
        return valorStr.equals(valorEsperado);
    }

    /**
     * Compara valores numéricos com operador.
     *
     * @param valor1 primeiro valor
     * @param valor2 segundo valor
     * @param operador operador de comparação
     * @return resultado da comparação
     */
    private boolean compararNumerico(String valor1, String valor2, String operador) {
        try {
            double v1 = Double.parseDouble(valor1);
            double v2 = Double.parseDouble(valor2);

            switch (operador) {
                case ">":
                    return v1 > v2;
                case "<":
                    return v1 < v2;
                case ">=":
                    return v1 >= v2;
                case "<=":
                    return v1 <= v2;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Adiciona uma condição à transição.
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
     * Remove uma condição da transição.
     *
     * @param chave chave da condição
     */
    public void removerCondicao(String chave) {
        if (condicoes != null) {
            condicoes.remove(chave);
        }
    }

    /**
     * Verifica se a transição está ativa.
     *
     * @return true se está ativa
     */
    public boolean isAtivo() {
        return Boolean.TRUE.equals(ativo);
    }

    /**
     * Ativa a transição.
     */
    public void ativar() {
        this.ativo = true;
    }

    /**
     * Desativa a transição.
     */
    public void desativar() {
        this.ativo = false;
    }

    /**
     * Verifica se a transição tem condições definidas.
     *
     * @return true se tem condições
     */
    public boolean hasCondicoes() {
        return condicoes != null && !condicoes.isEmpty();
    }

    /**
     * Valida se a transição está configurada corretamente.
     *
     * @throws IllegalStateException se a validação falhar
     */
    public void validar() {
        if (etapaOrigemId == null || etapaOrigemId.trim().isEmpty()) {
            throw new IllegalStateException("Etapa de origem é obrigatória");
        }

        if (etapaDestinoId == null || etapaDestinoId.trim().isEmpty()) {
            throw new IllegalStateException("Etapa de destino é obrigatória");
        }

        if (etapaOrigemId.equals(etapaDestinoId)) {
            throw new IllegalStateException("Etapa de origem e destino não podem ser iguais");
        }
    }

    /**
     * Verifica se a transição é válida.
     *
     * @return true se é válida
     */
    public boolean isValida() {
        try {
            validar();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("TransicaoWorkflow[id=%s, origem=%s, destino=%s, ativo=%s, condicoes=%d]",
                id, etapaOrigemId, etapaDestinoId, ativo, condicoes != null ? condicoes.size() : 0);
    }
}
