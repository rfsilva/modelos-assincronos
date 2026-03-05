package com.seguradora.hibrida.projection.tracking;

/**
 * Status possíveis para uma projeção.
 */
public enum ProjectionStatus {
    
    /**
     * Projeção ativa e processando eventos normalmente.
     */
    ACTIVE,
    
    /**
     * Projeção pausada manualmente.
     */
    PAUSED,
    
    /**
     * Projeção com erro e não processando eventos.
     */
    ERROR,
    
    /**
     * Projeção sendo reconstruída (rebuild).
     */
    REBUILDING,
    
    /**
     * Projeção desabilitada.
     */
    DISABLED
}