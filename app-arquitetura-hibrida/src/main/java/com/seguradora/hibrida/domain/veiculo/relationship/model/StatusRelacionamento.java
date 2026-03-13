package com.seguradora.hibrida.domain.veiculo.relationship.model;

/**
 * Enum que representa o status de um relacionamento Veículo-Apólice.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum StatusRelacionamento {

    /**
     * Relacionamento ativo - veículo está coberto pela apólice.
     */
    ATIVO("Ativo", "Veículo com cobertura ativa"),

    /**
     * Relacionamento suspenso - cobertura temporariamente suspensa.
     */
    SUSPENSO("Suspenso", "Cobertura temporariamente suspensa"),

    /**
     * Relacionamento encerrado - cobertura finalizada.
     */
    ENCERRADO("Encerrado", "Cobertura finalizada"),

    /**
     * Relacionamento cancelado - cancelado antes do término natural.
     */
    CANCELADO("Cancelado", "Relacionamento cancelado");

    private final String descricao;
    private final String detalhamento;

    StatusRelacionamento(String descricao, String detalhamento) {
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
     * Verifica se o status permite cobertura.
     */
    public boolean permiteCobertura() {
        return this == ATIVO;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
