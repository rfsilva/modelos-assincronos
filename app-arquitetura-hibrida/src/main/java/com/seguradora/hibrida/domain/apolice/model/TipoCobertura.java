package com.seguradora.hibrida.domain.apolice.model;

import java.math.BigDecimal;

/**
 * Enum que representa os tipos de cobertura disponíveis.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum TipoCobertura {
    
    /**
     * Cobertura total - cobre todos os tipos de sinistros.
     */
    TOTAL("Total", "Cobertura completa para todos os tipos de sinistros", new BigDecimal("1.0")),
    
    /**
     * Cobertura parcial - cobre apenas alguns tipos de sinistros.
     */
    PARCIAL("Parcial", "Cobertura limitada para tipos específicos de sinistros", new BigDecimal("0.7")),
    
    /**
     * Cobertura contra terceiros - cobre apenas danos a terceiros.
     */
    TERCEIROS("Terceiros", "Cobertura apenas para danos causados a terceiros", new BigDecimal("0.3")),
    
    /**
     * Cobertura contra roubo e furto.
     */
    ROUBO_FURTO("Roubo e Furto", "Cobertura específica para roubo e furto do veículo", new BigDecimal("0.5")),
    
    /**
     * Cobertura para colisão.
     */
    COLISAO("Colisão", "Cobertura para danos por colisão", new BigDecimal("0.6")),
    
    /**
     * Cobertura para incêndio.
     */
    INCENDIO("Incêndio", "Cobertura para danos por incêndio", new BigDecimal("0.4")),
    
    /**
     * Cobertura para fenômenos naturais.
     */
    FENOMENOS_NATURAIS("Fenômenos Naturais", "Cobertura para danos por fenômenos naturais", new BigDecimal("0.3"));
    
    private final String descricao;
    private final String detalhamento;
    private final BigDecimal fatorPremio;
    
    TipoCobertura(String descricao, String detalhamento, BigDecimal fatorPremio) {
        this.descricao = descricao;
        this.detalhamento = detalhamento;
        this.fatorPremio = fatorPremio;
    }
    
    /**
     * Retorna a descrição da cobertura.
     */
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Retorna o detalhamento da cobertura.
     */
    public String getDetalhamento() {
        return detalhamento;
    }
    
    /**
     * Retorna o fator de cálculo do prêmio para esta cobertura.
     */
    public BigDecimal getFatorPremio() {
        return fatorPremio;
    }
    
    /**
     * Verifica se esta cobertura inclui proteção contra roubo/furto.
     */
    public boolean cobreRouboFurto() {
        return this == TOTAL || this == ROUBO_FURTO;
    }
    
    /**
     * Verifica se esta cobertura inclui proteção contra colisão.
     */
    public boolean cobreColisao() {
        return this == TOTAL || this == PARCIAL || this == COLISAO;
    }
    
    /**
     * Verifica se esta cobertura inclui proteção contra terceiros.
     */
    public boolean cobreTerceiros() {
        return this == TOTAL || this == PARCIAL || this == TERCEIROS;
    }
    
    /**
     * Verifica se esta cobertura inclui proteção contra incêndio.
     */
    public boolean cobreIncendio() {
        return this == TOTAL || this == PARCIAL || this == INCENDIO;
    }
    
    /**
     * Verifica se esta cobertura inclui proteção contra fenômenos naturais.
     */
    public boolean cobreFenomenosNaturais() {
        return this == TOTAL || this == FENOMENOS_NATURAIS;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}