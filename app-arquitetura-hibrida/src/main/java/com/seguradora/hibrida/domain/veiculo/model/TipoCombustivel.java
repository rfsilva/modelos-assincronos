package com.seguradora.hibrida.domain.veiculo.model;

/**
 * Enum representando os tipos de combustível suportados pelo sistema.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
public enum TipoCombustivel {
    
    /**
     * Veículo movido a gasolina.
     */
    GASOLINA,
    
    /**
     * Veículo movido a etanol.
     */
    ETANOL,
    
    /**
     * Veículo flex (gasolina ou etanol).
     */
    FLEX,
    
    /**
     * Veículo movido a diesel.
     */
    DIESEL,
    
    /**
     * Veículo movido a gás natural veicular.
     */
    GNV,
    
    /**
     * Veículo elétrico.
     */
    ELETRICO,
    
    /**
     * Veículo híbrido (combustível + elétrico).
     */
    HIBRIDO
}
