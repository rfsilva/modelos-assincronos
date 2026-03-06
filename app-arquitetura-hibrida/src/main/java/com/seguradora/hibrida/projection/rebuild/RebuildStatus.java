package com.seguradora.hibrida.projection.rebuild;

/**
 * Status de uma operação de rebuild.
 */
public enum RebuildStatus {
    
    /**
     * Rebuild executado com sucesso.
     */
    SUCCESS("Sucesso"),
    
    /**
     * Rebuild falhou com erro.
     */
    FAILED("Falha"),
    
    /**
     * Rebuild foi pausado.
     */
    PAUSED("Pausado"),
    
    /**
     * Rebuild está em execução.
     */
    RUNNING("Executando");
    
    private final String displayName;
    
    RebuildStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}