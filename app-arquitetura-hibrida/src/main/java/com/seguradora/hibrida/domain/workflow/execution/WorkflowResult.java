package com.seguradora.hibrida.domain.workflow.execution;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Value Object que representa o resultado da execução de uma etapa ou workflow.
 * Encapsula o sucesso/falha e dados adicionais resultantes da execução.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowResult {

    private boolean sucesso;
    private String mensagem;
    private String proximoStatus;

    @Builder.Default
    private Map<String, Object> dados = new HashMap<>();

    private String etapaProximaId;
    private Integer codigoErro;
    private boolean permiteRetry;

    /**
     * Cria um resultado de sucesso.
     *
     * @param mensagem mensagem de sucesso
     * @return resultado de sucesso
     */
    public static WorkflowResult success(String mensagem) {
        return WorkflowResult.builder()
                .sucesso(true)
                .mensagem(mensagem)
                .build();
    }

    /**
     * Cria um resultado de sucesso com dados adicionais.
     *
     * @param mensagem mensagem de sucesso
     * @param dados dados resultantes
     * @return resultado de sucesso
     */
    public static WorkflowResult success(String mensagem, Map<String, Object> dados) {
        return WorkflowResult.builder()
                .sucesso(true)
                .mensagem(mensagem)
                .dados(dados != null ? dados : new HashMap<>())
                .build();
    }

    /**
     * Cria um resultado de sucesso indicando próxima etapa.
     *
     * @param mensagem mensagem de sucesso
     * @param proximaEtapaId ID da próxima etapa
     * @return resultado de sucesso
     */
    public static WorkflowResult successComProximaEtapa(String mensagem, String proximaEtapaId) {
        return WorkflowResult.builder()
                .sucesso(true)
                .mensagem(mensagem)
                .etapaProximaId(proximaEtapaId)
                .build();
    }

    /**
     * Cria um resultado de falha.
     *
     * @param mensagem mensagem de erro
     * @return resultado de falha
     */
    public static WorkflowResult failure(String mensagem) {
        return WorkflowResult.builder()
                .sucesso(false)
                .mensagem(mensagem)
                .permiteRetry(true)
                .build();
    }

    /**
     * Cria um resultado de falha com código de erro.
     *
     * @param mensagem mensagem de erro
     * @param codigoErro código do erro
     * @return resultado de falha
     */
    public static WorkflowResult failure(String mensagem, Integer codigoErro) {
        return WorkflowResult.builder()
                .sucesso(false)
                .mensagem(mensagem)
                .codigoErro(codigoErro)
                .permiteRetry(true)
                .build();
    }

    /**
     * Cria um resultado de falha sem permitir retry.
     *
     * @param mensagem mensagem de erro
     * @return resultado de falha
     */
    public static WorkflowResult failureSemRetry(String mensagem) {
        return WorkflowResult.builder()
                .sucesso(false)
                .mensagem(mensagem)
                .permiteRetry(false)
                .build();
    }

    /**
     * Cria um resultado de timeout.
     *
     * @param mensagem mensagem de timeout
     * @return resultado de timeout
     */
    public static WorkflowResult timeout(String mensagem) {
        return WorkflowResult.builder()
                .sucesso(false)
                .mensagem(mensagem)
                .proximoStatus("TIMEOUT")
                .permiteRetry(true)
                .build();
    }

    /**
     * Cria um resultado de timeout sem retry.
     *
     * @return resultado de timeout
     */
    public static WorkflowResult timeout() {
        return timeout("Tempo limite de execução excedido");
    }

    /**
     * Adiciona um dado ao resultado.
     *
     * @param chave chave do dado
     * @param valor valor do dado
     * @return este resultado para fluent interface
     */
    public WorkflowResult comDado(String chave, Object valor) {
        if (dados == null) {
            dados = new HashMap<>();
        }
        dados.put(chave, valor);
        return this;
    }

    /**
     * Define múltiplos dados ao resultado.
     *
     * @param dadosAdicionais dados a adicionar
     * @return este resultado para fluent interface
     */
    public WorkflowResult comDados(Map<String, Object> dadosAdicionais) {
        if (dados == null) {
            dados = new HashMap<>();
        }
        if (dadosAdicionais != null) {
            dados.putAll(dadosAdicionais);
        }
        return this;
    }

    /**
     * Define a próxima etapa no resultado.
     *
     * @param etapaId ID da próxima etapa
     * @return este resultado para fluent interface
     */
    public WorkflowResult comProximaEtapa(String etapaId) {
        this.etapaProximaId = etapaId;
        return this;
    }

    /**
     * Define o próximo status no resultado.
     *
     * @param status próximo status
     * @return este resultado para fluent interface
     */
    public WorkflowResult comProximoStatus(String status) {
        this.proximoStatus = status;
        return this;
    }

    /**
     * Verifica se o resultado indica sucesso.
     *
     * @return true se foi sucesso
     */
    public boolean isSucesso() {
        return sucesso;
    }

    /**
     * Verifica se o resultado indica falha.
     *
     * @return true se foi falha
     */
    public boolean isFalha() {
        return !sucesso;
    }

    /**
     * Verifica se o resultado indica timeout.
     *
     * @return true se foi timeout
     */
    public boolean isTimeout() {
        return "TIMEOUT".equals(proximoStatus);
    }

    /**
     * Verifica se há próxima etapa definida.
     *
     * @return true se há próxima etapa
     */
    public boolean hasProximaEtapa() {
        return etapaProximaId != null && !etapaProximaId.isEmpty();
    }

    /**
     * Verifica se permite retry.
     *
     * @return true se permite retry
     */
    public boolean isPermiteRetry() {
        return permiteRetry;
    }

    /**
     * Obtém um dado do resultado.
     *
     * @param chave chave do dado
     * @return valor do dado ou null
     */
    public Object getDado(String chave) {
        return dados != null ? dados.get(chave) : null;
    }

    /**
     * Obtém um dado do resultado com tipo específico.
     *
     * @param chave chave do dado
     * @param tipo classe do tipo esperado
     * @param <T> tipo do dado
     * @return valor do dado ou null
     */
    @SuppressWarnings("unchecked")
    public <T> T getDado(String chave, Class<T> tipo) {
        Object valor = getDado(chave);
        if (valor != null && tipo.isInstance(valor)) {
            return (T) valor;
        }
        return null;
    }

    /**
     * Verifica se contém um dado específico.
     *
     * @param chave chave do dado
     * @return true se contém o dado
     */
    public boolean hasDado(String chave) {
        return dados != null && dados.containsKey(chave);
    }

    /**
     * Mescla dados de outro resultado neste.
     *
     * @param outroResultado outro resultado
     * @return este resultado para fluent interface
     */
    public WorkflowResult merge(WorkflowResult outroResultado) {
        if (outroResultado != null && outroResultado.dados != null) {
            comDados(outroResultado.dados);
        }
        return this;
    }

    @Override
    public String toString() {
        return String.format("WorkflowResult[sucesso=%s, mensagem=%s, dados=%d, proximaEtapa=%s]",
                sucesso, mensagem, dados != null ? dados.size() : 0, etapaProximaId);
    }
}
