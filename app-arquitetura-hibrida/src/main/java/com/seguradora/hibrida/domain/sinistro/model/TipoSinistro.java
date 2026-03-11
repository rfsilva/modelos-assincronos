package com.seguradora.hibrida.domain.sinistro.model;

import java.math.BigDecimal;

/**
 * Enum que representa os tipos de sinistro possíveis.
 *
 * <p>Cada tipo possui características específicas como:
 * <ul>
 *   <li>Franquia padrão</li>
 *   <li>Prazo de processamento em dias úteis</li>
 *   <li>Documentação obrigatória</li>
 *   <li>Carência aplicável</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum TipoSinistro {

    /**
     * Colisão com outro veículo ou objeto.
     */
    COLISAO("Colisão", new BigDecimal("1500.00"), 5, 0),

    /**
     * Roubo ou furto do veículo.
     */
    ROUBO_FURTO("Roubo/Furto", new BigDecimal("0.00"), 10, 30),

    /**
     * Incêndio total ou parcial.
     */
    INCENDIO("Incêndio", new BigDecimal("2000.00"), 7, 15),

    /**
     * Danos causados por enchente/alagamento.
     */
    ENCHENTE("Enchente", new BigDecimal("2500.00"), 7, 15),

    /**
     * Vandalismo ou danos maliciosos.
     */
    VANDALISMO("Vandalismo", new BigDecimal("1000.00"), 5, 0),

    /**
     * Danos causados a terceiros.
     */
    TERCEIROS("Terceiros", new BigDecimal("0.00"), 15, 0);

    private final String descricao;
    private final BigDecimal franquiaPadrao;
    private final int prazoProcessamentoDias;
    private final int carenciaDias;

    TipoSinistro(String descricao, BigDecimal franquiaPadrao, int prazoProcessamentoDias, int carenciaDias) {
        this.descricao = descricao;
        this.franquiaPadrao = franquiaPadrao;
        this.prazoProcessamentoDias = prazoProcessamentoDias;
        this.carenciaDias = carenciaDias;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getFranquiaPadrao() {
        return franquiaPadrao;
    }

    public int getPrazoProcessamentoDias() {
        return prazoProcessamentoDias;
    }

    public int getCarenciaDias() {
        return carenciaDias;
    }

    /**
     * Verifica se o tipo requer consulta ao Detran.
     */
    public boolean requerConsultaDetran() {
        return this == ROUBO_FURTO || this == COLISAO;
    }

    /**
     * Verifica se o tipo requer boletim de ocorrência.
     */
    public boolean requerBoletimOcorrencia() {
        return this == ROUBO_FURTO || this == VANDALISMO || this == INCENDIO;
    }

    /**
     * Verifica se o tipo possui carência.
     */
    public boolean possuiCarencia() {
        return carenciaDias > 0;
    }
}
