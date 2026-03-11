package com.seguradora.hibrida.domain.workflow.execution;

import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Value Object que representa o contexto de execução de um workflow.
 * Contém todos os dados necessários para avaliação de condições e execução de ações.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowContext {

    private String sinistroId;
    private BigDecimal valorIndenizacao;
    private String tipoSinistro;

    @Builder.Default
    private Map<String, Object> dadosSinistro = new HashMap<>();

    @Builder.Default
    private Map<String, Object> variaveisWorkflow = new HashMap<>();

    /**
     * Obtém um valor do contexto por chave.
     * Busca primeiro em variaveisWorkflow, depois em dadosSinistro.
     *
     * @param chave chave do valor
     * @return valor encontrado ou null
     */
    public Object get(String chave) {
        if (variaveisWorkflow.containsKey(chave)) {
            return variaveisWorkflow.get(chave);
        }
        return dadosSinistro.get(chave);
    }

    /**
     * Define um valor no contexto.
     *
     * @param chave chave do valor
     * @param valor valor a armazenar
     */
    public void set(String chave, Object valor) {
        variaveisWorkflow.put(chave, valor);
    }

    /**
     * Define um dado do sinistro no contexto.
     *
     * @param chave chave do dado
     * @param valor valor a armazenar
     */
    public void setDadoSinistro(String chave, Object valor) {
        dadosSinistro.put(chave, valor);
    }

    /**
     * Avalia uma expressão condicional contra o contexto.
     *
     * @param expressao expressão a avaliar (formato: "chave operador valor")
     * @return true se a condição é satisfeita
     */
    public boolean avaliarCondicao(String expressao) {
        if (expressao == null || expressao.trim().isEmpty()) {
            return true;
        }

        // Suporta expressões simples: chave > valor, chave == valor, etc.
        String[] partes = expressao.trim().split("\\s+");

        if (partes.length < 3) {
            // Formato simples: apenas chave (verifica se existe e é true)
            Object valor = get(partes[0]);
            return valor != null && (Boolean.TRUE.equals(valor) || "true".equalsIgnoreCase(valor.toString()));
        }

        String chave = partes[0];
        String operador = partes[1];
        String valorEsperado = String.join(" ", java.util.Arrays.copyOfRange(partes, 2, partes.length));

        Object valorContexto = get(chave);

        if (valorContexto == null) {
            return "null".equals(valorEsperado);
        }

        return avaliarComOperador(valorContexto, operador, valorEsperado);
    }

    /**
     * Avalia uma condição usando operador específico.
     *
     * @param valorContexto valor do contexto
     * @param operador operador de comparação
     * @param valorEsperado valor esperado
     * @return resultado da avaliação
     */
    private boolean avaliarComOperador(Object valorContexto, String operador, String valorEsperado) {
        String valorStr = valorContexto.toString();

        switch (operador) {
            case "==":
            case "=":
                return valorStr.equals(valorEsperado);

            case "!=":
                return !valorStr.equals(valorEsperado);

            case ">":
                return compararNumerico(valorStr, valorEsperado) > 0;

            case "<":
                return compararNumerico(valorStr, valorEsperado) < 0;

            case ">=":
                return compararNumerico(valorStr, valorEsperado) >= 0;

            case "<=":
                return compararNumerico(valorStr, valorEsperado) <= 0;

            case "contains":
                return valorStr.toLowerCase().contains(valorEsperado.toLowerCase());

            case "startsWith":
                return valorStr.startsWith(valorEsperado);

            case "endsWith":
                return valorStr.endsWith(valorEsperado);

            default:
                return false;
        }
    }

    /**
     * Compara dois valores numéricos.
     *
     * @param valor1 primeiro valor
     * @param valor2 segundo valor
     * @return resultado da comparação (-1, 0, 1)
     */
    private int compararNumerico(String valor1, String valor2) {
        try {
            BigDecimal v1 = new BigDecimal(valor1);
            BigDecimal v2 = new BigDecimal(valor2);
            return v1.compareTo(v2);
        } catch (NumberFormatException e) {
            // Fallback para comparação de strings
            return valor1.compareTo(valor2);
        }
    }

    /**
     * Converte o contexto para Map para uso em templates.
     *
     * @return mapa com todos os dados do contexto
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sinistroId", sinistroId);
        map.put("valorIndenizacao", valorIndenizacao);
        map.put("tipoSinistro", tipoSinistro);
        map.putAll(dadosSinistro);
        map.putAll(variaveisWorkflow);
        return map;
    }

    /**
     * Cria um contexto a partir de um Map.
     *
     * @param dados mapa com dados do contexto
     * @return novo contexto criado
     */
    public static WorkflowContext fromMap(Map<String, Object> dados) {
        WorkflowContext context = new WorkflowContext();

        if (dados.containsKey("sinistroId")) {
            context.sinistroId = dados.get("sinistroId").toString();
        }

        if (dados.containsKey("valorIndenizacao")) {
            Object valor = dados.get("valorIndenizacao");
            if (valor instanceof BigDecimal) {
                context.valorIndenizacao = (BigDecimal) valor;
            } else if (valor instanceof Number) {
                context.valorIndenizacao = BigDecimal.valueOf(((Number) valor).doubleValue());
            } else {
                context.valorIndenizacao = new BigDecimal(valor.toString());
            }
        }

        if (dados.containsKey("tipoSinistro")) {
            context.tipoSinistro = dados.get("tipoSinistro").toString();
        }

        // Adiciona todos os outros dados
        dados.forEach((chave, valor) -> {
            if (!chave.equals("sinistroId") && !chave.equals("valorIndenizacao") && !chave.equals("tipoSinistro")) {
                context.dadosSinistro.put(chave, valor);
            }
        });

        return context;
    }

    /**
     * Verifica se uma chave existe no contexto.
     *
     * @param chave chave a verificar
     * @return true se existe
     */
    public boolean containsKey(String chave) {
        return variaveisWorkflow.containsKey(chave) || dadosSinistro.containsKey(chave);
    }

    /**
     * Remove uma variável do workflow.
     *
     * @param chave chave da variável
     */
    public void removeVariavel(String chave) {
        variaveisWorkflow.remove(chave);
    }

    /**
     * Limpa todas as variáveis do workflow.
     */
    public void limparVariaveis() {
        variaveisWorkflow.clear();
    }

    /**
     * Copia este contexto.
     *
     * @return cópia do contexto
     */
    public WorkflowContext copy() {
        return WorkflowContext.builder()
                .sinistroId(this.sinistroId)
                .valorIndenizacao(this.valorIndenizacao)
                .tipoSinistro(this.tipoSinistro)
                .dadosSinistro(new HashMap<>(this.dadosSinistro))
                .variaveisWorkflow(new HashMap<>(this.variaveisWorkflow))
                .build();
    }

    @Override
    public String toString() {
        return String.format("WorkflowContext[sinistro=%s, tipo=%s, valor=%s, vars=%d]",
                sinistroId, tipoSinistro, valorIndenizacao, variaveisWorkflow.size());
    }
}
