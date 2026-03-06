package com.seguradora.hibrida.projection.rebuild;

/**
 * Tipos de rebuild de projeção.
 */
public enum RebuildType {
    
    /**
     * Rebuild completo - reprocessa todos os eventos desde o início.
     */
    FULL("Completo"),
    
    /**
     * Rebuild incremental - reprocessa apenas eventos desde a última posição.
     */
    INCREMENTAL("Incremental");
    
    private final String displayName;
    
    RebuildType(String displayName) {
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