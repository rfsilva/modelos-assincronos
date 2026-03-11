package com.seguradora.hibrida.domain.workflow.model;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Define os níveis hierárquicos de aprovação de sinistros com base no valor da indenização.
 * Cada nível possui um limite máximo de alçada e pode escalar para o nível superior.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Getter
public enum NivelAprovacao {

    /**
     * Nível 1 - Analista de Sinistros.
     * Alçada: até R$ 10.000,00
     * Aprovações de rotina e casos simples.
     */
    NIVEL_1_ANALISTA(1, new BigDecimal("10000.00"), "Analista de Sinistros"),

    /**
     * Nível 2 - Supervisor de Sinistros.
     * Alçada: até R$ 50.000,00
     * Casos de média complexidade e revisão de análises.
     */
    NIVEL_2_SUPERVISOR(2, new BigDecimal("50000.00"), "Supervisor de Sinistros"),

    /**
     * Nível 3 - Gerente de Sinistros.
     * Alçada: até R$ 200.000,00
     * Casos complexos e exceções operacionais.
     */
    NIVEL_3_GERENTE(3, new BigDecimal("200000.00"), "Gerente de Sinistros"),

    /**
     * Nível 4 - Diretor de Operações.
     * Alçada: sem limite (valores acima de R$ 200.000,00)
     * Casos excepcionais e alto valor.
     */
    NIVEL_4_DIRETOR(4, null, "Diretor de Operações");

    private final int nivel;
    private final BigDecimal limiteAlcada;
    private final String descricao;

    /**
     * Construtor do enum.
     *
     * @param nivel número do nível hierárquico
     * @param limiteAlcada valor máximo que pode aprovar (null = sem limite)
     * @param descricao descrição do cargo/função
     */
    NivelAprovacao(int nivel, BigDecimal limiteAlcada, String descricao) {
        this.nivel = nivel;
        this.limiteAlcada = limiteAlcada;
        this.descricao = descricao;
    }

    /**
     * Retorna o limite de alçada deste nível.
     *
     * @return valor máximo que pode aprovar, ou null se sem limite
     */
    public BigDecimal getLimite() {
        return limiteAlcada;
    }

    /**
     * Verifica se este nível pode aprovar um determinado valor.
     *
     * @param valor valor da indenização a ser aprovada
     * @return true se pode aprovar, false caso contrário
     */
    public boolean podeAprovar(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // Nível diretor não tem limite
        if (limiteAlcada == null) {
            return true;
        }

        return valor.compareTo(limiteAlcada) <= 0;
    }

    /**
     * Retorna o próximo nível hierárquico superior.
     * Útil para escalação de aprovações.
     *
     * @return próximo nível ou null se já é o nível máximo
     */
    public NivelAprovacao getProximo() {
        switch (this) {
            case NIVEL_1_ANALISTA:
                return NIVEL_2_SUPERVISOR;
            case NIVEL_2_SUPERVISOR:
                return NIVEL_3_GERENTE;
            case NIVEL_3_GERENTE:
                return NIVEL_4_DIRETOR;
            case NIVEL_4_DIRETOR:
                return null; // Já é o nível máximo
            default:
                return null;
        }
    }

    /**
     * Retorna o nível anterior (inferior) na hierarquia.
     *
     * @return nível anterior ou null se já é o nível mínimo
     */
    public NivelAprovacao getAnterior() {
        switch (this) {
            case NIVEL_2_SUPERVISOR:
                return NIVEL_1_ANALISTA;
            case NIVEL_3_GERENTE:
                return NIVEL_2_SUPERVISOR;
            case NIVEL_4_DIRETOR:
                return NIVEL_3_GERENTE;
            case NIVEL_1_ANALISTA:
                return null; // Já é o nível mínimo
            default:
                return null;
        }
    }

    /**
     * Determina o nível de aprovação necessário baseado no valor da indenização.
     *
     * @param valor valor da indenização
     * @return nível de aprovação adequado
     */
    public static NivelAprovacao determinarNivel(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }

        if (valor.compareTo(new BigDecimal("10000.00")) <= 0) {
            return NIVEL_1_ANALISTA;
        } else if (valor.compareTo(new BigDecimal("50000.00")) <= 0) {
            return NIVEL_2_SUPERVISOR;
        } else if (valor.compareTo(new BigDecimal("200000.00")) <= 0) {
            return NIVEL_3_GERENTE;
        } else {
            return NIVEL_4_DIRETOR;
        }
    }

    /**
     * Verifica se este é o nível máximo da hierarquia.
     *
     * @return true se é o nível máximo
     */
    public boolean isNivelMaximo() {
        return this == NIVEL_4_DIRETOR;
    }

    /**
     * Verifica se este é o nível mínimo da hierarquia.
     *
     * @return true se é o nível mínimo
     */
    public boolean isNivelMinimo() {
        return this == NIVEL_1_ANALISTA;
    }

    /**
     * Retorna o número do nível hierárquico.
     *
     * @return número do nível (1-4)
     */
    public int getNivel() {
        return nivel;
    }
}
