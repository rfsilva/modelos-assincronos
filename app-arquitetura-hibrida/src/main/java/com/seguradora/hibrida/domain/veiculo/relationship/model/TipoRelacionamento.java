package com.seguradora.hibrida.domain.veiculo.relationship.model;

/**
 * Enum que representa o tipo de relacionamento Veículo-Apólice.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum TipoRelacionamento {

    /**
     * Veículo principal da apólice.
     */
    PRINCIPAL("Principal", "Veículo principal coberto pela apólice"),

    /**
     * Veículo adicional incluído na apólice.
     */
    ADICIONAL("Adicional", "Veículo adicional na apólice"),

    /**
     * Veículo com cobertura temporária.
     */
    TEMPORARIO("Temporário", "Cobertura temporária do veículo"),

    /**
     * Veículo substituto enquanto o principal está em manutenção.
     */
    SUBSTITUTO("Substituto", "Veículo substituto temporário");

    private final String descricao;
    private final String detalhamento;

    TipoRelacionamento(String descricao, String detalhamento) {
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
     * Verifica se é um relacionamento permanente.
     */
    public boolean isPermanente() {
        return this == PRINCIPAL || this == ADICIONAL;
    }

    /**
     * Verifica se é um relacionamento temporário.
     */
    public boolean isTemporario() {
        return this == TEMPORARIO || this == SUBSTITUTO;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
