package com.seguradora.hibrida.domain.veiculo.model;

/**
 * Enum representando os possíveis status de um veículo no sistema.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
public enum StatusVeiculo {
    
    /**
     * Veículo ativo e disponível para operações.
     */
    ATIVO,
    
    /**
     * Veículo inativo, não pode ser associado a novas apólices.
     */
    INATIVO,
    
    /**
     * Veículo bloqueado por questões administrativas ou judiciais.
     */
    BLOQUEADO,
    
    /**
     * Veículo com sinistro registrado, pode ter restrições.
     */
    SINISTRADO
}
