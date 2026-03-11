package com.seguradora.hibrida.domain.workflow.model;

import lombok.Getter;

/**
 * Define os status possíveis de uma etapa de workflow durante sua execução.
 * Controla o ciclo de vida e possibilidades de transição de estado.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Getter
public enum StatusEtapa {

    /**
     * Etapa aguardando início de execução.
     * Estado inicial após criação ou quando dependências ainda não foram satisfeitas.
     */
    PENDENTE(false, false, true),

    /**
     * Etapa em processo de execução.
     * Pode ser processamento automático ou aguardando ação humana.
     */
    EM_ANDAMENTO(false, true, true),

    /**
     * Etapa concluída com sucesso.
     * Estado final positivo, permite avançar para próxima etapa.
     */
    CONCLUIDA(true, false, false),

    /**
     * Etapa falhou durante execução.
     * Pode permitir retry dependendo da configuração e tipo de falha.
     */
    FALHADA(false, true, true),

    /**
     * Etapa excedeu o tempo limite configurado.
     * Pode permitir retry ou requer intervenção manual.
     */
    TIMEOUT(false, true, true),

    /**
     * Etapa cancelada por decisão do sistema ou usuário.
     * Estado final negativo, não permite continuação.
     */
    CANCELADA(true, false, false);

    private final boolean statusFinal;
    private final boolean permiteRetry;
    private final boolean podeTransicionar;

    /**
     * Construtor do enum.
     *
     * @param statusFinal indica se é um estado final (não transiciona mais)
     * @param permiteRetry indica se permite nova tentativa de execução
     * @param podeTransicionar indica se pode transicionar para outro status
     */
    StatusEtapa(boolean statusFinal, boolean permiteRetry, boolean podeTransicionar) {
        this.statusFinal = statusFinal;
        this.permiteRetry = permiteRetry;
        this.podeTransicionar = podeTransicionar;
    }

    /**
     * Verifica se este é um status final (não permite mais transições).
     *
     * @return true se é status final
     */
    public boolean isFinal() {
        return statusFinal;
    }

    /**
     * Verifica se permite retry (nova tentativa de execução).
     *
     * @return true se permite retry
     */
    public boolean podeRetry() {
        return permiteRetry;
    }

    /**
     * Verifica se pode transicionar para outro status.
     *
     * @return true se pode transicionar
     */
    public boolean podeTransicionar() {
        return podeTransicionar;
    }

    /**
     * Verifica se o status indica sucesso na execução.
     *
     * @return true se é status de sucesso
     */
    public boolean isSucesso() {
        return this == CONCLUIDA;
    }

    /**
     * Verifica se o status indica erro ou falha.
     *
     * @return true se é status de erro
     */
    public boolean isErro() {
        return this == FALHADA || this == TIMEOUT;
    }

    /**
     * Verifica se o status indica que a etapa está ativa.
     *
     * @return true se está em execução
     */
    public boolean isAtiva() {
        return this == EM_ANDAMENTO || this == PENDENTE;
    }

    /**
     * Verifica se pode avançar para próxima etapa.
     *
     * @return true se pode avançar
     */
    public boolean podeAvancar() {
        return this == CONCLUIDA;
    }
}
