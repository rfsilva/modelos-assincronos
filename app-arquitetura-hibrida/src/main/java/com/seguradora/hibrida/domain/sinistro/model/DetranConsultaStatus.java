package com.seguradora.hibrida.domain.sinistro.model;

/**
 * Enum que representa o status de uma consulta ao Detran.
 *
 * <p>Estados possíveis da consulta assíncrona:
 * <ul>
 *   <li>PENDENTE → Consulta ainda não iniciada</li>
 *   <li>EM_ANDAMENTO → Consulta em execução</li>
 *   <li>CONCLUIDA → Consulta finalizada com sucesso</li>
 *   <li>FALHADA → Consulta falhou por erro</li>
 *   <li>TIMEOUT → Consulta exced

eu tempo limite</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum DetranConsultaStatus {

    /**
     * Consulta ainda não iniciada.
     */
    PENDENTE("Pendente", "Consulta aguardando processamento"),

    /**
     * Consulta em execução.
     */
    EM_ANDAMENTO("Em Andamento", "Consulta sendo processada"),

    /**
     * Consulta concluída com sucesso.
     */
    CONCLUIDA("Concluída", "Consulta finalizada com sucesso"),

    /**
     * Consulta falhou.
     */
    FALHADA("Falhada", "Erro ao processar consulta"),

    /**
     * Consulta excedeu tempo limite.
     */
    TIMEOUT("Timeout", "Consulta excedeu tempo limite de 30 segundos");

    private final String descricao;
    private final String detalhamento;

    DetranConsultaStatus(String descricao, String detalhamento) {
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
     * Verifica se a consulta está em estado final (sucesso ou erro).
     */
    public boolean isFinal() {
        return this == CONCLUIDA || this == FALHADA || this == TIMEOUT;
    }

    /**
     * Verifica se deve realizar retry automático.
     */
    public boolean deveRetry() {
        return this == FALHADA || this == TIMEOUT;
    }
}
