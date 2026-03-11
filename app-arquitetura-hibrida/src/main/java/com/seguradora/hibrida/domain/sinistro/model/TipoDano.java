package com.seguradora.hibrida.domain.sinistro.model;

import java.math.BigDecimal;

/**
 * Enum que representa os tipos de dano possíveis em um sinistro.
 *
 * <p>Classificação dos danos para cálculo de indenização:
 * <ul>
 *   <li>TOTAL → Perda total do veículo (>75% do valor)</li>
 *   <li>PARCIAL → Dano parcial reparável (<75% do valor)</li>
 *   <li>TERCEIROS → Danos causados a terceiros</li>
 *   <li>VIDROS → Danos específicos em vidros</li>
 *   <li>ACESSORIOS → Danos em acessórios opcionais</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum TipoDano {

    /**
     * Perda total do veículo.
     */
    TOTAL("Total", "Perda total do veículo", new BigDecimal("0.75")),

    /**
     * Dano parcial reparável.
     */
    PARCIAL("Parcial", "Dano parcial reparável", new BigDecimal("0.74")),

    /**
     * Danos a terceiros.
     */
    TERCEIROS("Terceiros", "Danos causados a terceiros", BigDecimal.ZERO),

    /**
     * Danos em vidros.
     */
    VIDROS("Vidros", "Danos específicos em vidros", BigDecimal.ZERO),

    /**
     * Danos em acessórios.
     */
    ACESSORIOS("Acessórios", "Danos em acessórios opcionais", BigDecimal.ZERO);

    private final String descricao;
    private final String detalhamento;
    private final BigDecimal percentualMaximo;

    TipoDano(String descricao, String detalhamento, BigDecimal percentualMaximo) {
        this.descricao = descricao;
        this.detalhamento = detalhamento;
        this.percentualMaximo = percentualMaximo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhamento() {
        return detalhamento;
    }

    public BigDecimal getPercentualMaximo() {
        return percentualMaximo;
    }

    /**
     * Verifica se é perda total.
     */
    public boolean isPerdaTotal() {
        return this == TOTAL;
    }

    /**
     * Verifica se requer laudo pericial.
     */
    public boolean requerLaudoPericial() {
        return this == TOTAL || this == PARCIAL;
    }
}
