package com.seguradora.hibrida.domain.sinistro.model;

/**
 * Enum que representa os status possíveis de um sinistro.
 *
 * <p>Máquina de estados completa para controlar o ciclo de vida do sinistro:
 * <ul>
 *   <li>NOVO → Sinistro recém criado, aguardando validação inicial</li>
 *   <li>VALIDADO → Dados validados, aguardando análise</li>
 *   <li>EM_ANALISE → Em análise por um analista</li>
 *   <li>AGUARDANDO_DETRAN → Aguardando retorno da consulta ao Detran</li>
 *   <li>DADOS_COLETADOS → Todos os dados necessários foram coletados</li>
 *   <li>APROVADO → Sinistro aprovado, aguardando pagamento</li>
 *   <li>REPROVADO → Sinistro reprovado com justificativa</li>
 *   <li>PAGO → Indenização paga ao segurado</li>
 *   <li>ARQUIVADO → Sinistro finalizado e arquivado</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum StatusSinistro {

    /**
     * Sinistro recém criado, aguardando validação inicial.
     */
    NOVO("Novo", "Sinistro recém registrado no sistema"),

    /**
     * Dados validados, pronto para análise.
     */
    VALIDADO("Validado", "Dados do sinistro validados"),

    /**
     * Em análise por analista.
     */
    EM_ANALISE("Em Análise", "Sinistro em análise técnica"),

    /**
     * Aguardando resposta da consulta ao Detran.
     */
    AGUARDANDO_DETRAN("Aguardando Detran", "Aguardando dados do Detran"),

    /**
     * Todos os dados necessários foram coletados.
     */
    DADOS_COLETADOS("Dados Coletados", "Todos os dados necessários disponíveis"),

    /**
     * Sinistro aprovado, aguardando pagamento.
     */
    APROVADO("Aprovado", "Sinistro aprovado para pagamento"),

    /**
     * Sinistro reprovado.
     */
    REPROVADO("Reprovado", "Sinistro reprovado"),

    /**
     * Indenização paga.
     */
    PAGO("Pago", "Indenização paga ao segurado"),

    /**
     * Sinistro finalizado e arquivado.
     */
    ARQUIVADO("Arquivado", "Sinistro finalizado e arquivado");

    private final String descricao;
    private final String detalhamento;

    StatusSinistro(String descricao, String detalhamento) {
        this.descricao = descricao;
        this.detalhamento = detalhamento;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhamento() {
        return detalhamento;
    }

    /**
     * Verifica se a transição para o novo status é válida.
     *
     * @param novoStatus Status de destino
     * @return true se a transição é válida
     */
    public boolean podeTransicionarPara(StatusSinistro novoStatus) {
        return switch (this) {
            case NOVO -> novoStatus == VALIDADO || novoStatus == ARQUIVADO;
            case VALIDADO -> novoStatus == EM_ANALISE || novoStatus == ARQUIVADO;
            case EM_ANALISE -> novoStatus == AGUARDANDO_DETRAN || novoStatus == DADOS_COLETADOS ||
                             novoStatus == APROVADO || novoStatus == REPROVADO || novoStatus == ARQUIVADO;
            case AGUARDANDO_DETRAN -> novoStatus == DADOS_COLETADOS || novoStatus == EM_ANALISE ||
                                     novoStatus == ARQUIVADO;
            case DADOS_COLETADOS -> novoStatus == APROVADO || novoStatus == REPROVADO || novoStatus == ARQUIVADO;
            case APROVADO -> novoStatus == PAGO || novoStatus == ARQUIVADO;
            case REPROVADO -> novoStatus == ARQUIVADO;
            case PAGO -> novoStatus == ARQUIVADO;
            case ARQUIVADO -> false; // Estado final
        };
    }

    /**
     * Verifica se o status é final (não permite mais transições).
     */
    public boolean isFinal() {
        return this == ARQUIVADO;
    }

    /**
     * Verifica se o sinistro está aberto (em processamento).
     */
    public boolean isAberto() {
        return this != ARQUIVADO && this != PAGO;
    }
}
