package com.seguradora.hibrida.projection.consistency;

/**
 * Tipos de issues de consistência em projeções.
 */
public enum IssueType {
    
    /**
     * Lag alto - projeção muito atrás dos eventos disponíveis.
     */
    HIGH_LAG("Lag Alto"),
    
    /**
     * Projeção travada - não processa eventos há muito tempo.
     */
    STALE_PROJECTION("Projeção Travada"),
    
    /**
     * Taxa de erro alta - muitas falhas no processamento.
     */
    HIGH_ERROR_RATE("Taxa de Erro Alta"),
    
    /**
     * Erro persistente - projeção em erro há muito tempo.
     */
    PERSISTENT_ERROR("Erro Persistente"),
    
    /**
     * Projeção pausada por muito tempo.
     */
    LONG_PAUSED("Pausada por Muito Tempo"),
    
    /**
     * Projeção órfã - nunca processou eventos.
     */
    ORPHANED_PROJECTION("Projeção Órfã"),
    
    /**
     * Projeção não encontrada no tracker.
     */
    PROJECTION_NOT_FOUND("Projeção Não Encontrada"),
    
    /**
     * Erro do sistema durante verificação.
     */
    SYSTEM_ERROR("Erro do Sistema"),
    
    /**
     * Inconsistência de dados entre projeção e eventos.
     */
    DATA_INCONSISTENCY("Inconsistência de Dados"),
    
    /**
     * Versão da projeção incompatível.
     */
    VERSION_MISMATCH("Incompatibilidade de Versão");
    
    private final String displayName;
    
    IssueType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Verifica se é um tipo de issue que requer ação imediata.
     */
    public boolean requiresImmediateAction() {
        return this == HIGH_LAG || 
               this == STALE_PROJECTION || 
               this == PERSISTENT_ERROR ||
               this == SYSTEM_ERROR;
    }
    
    /**
     * Verifica se é um tipo de issue que pode ser resolvido automaticamente.
     */
    public boolean canBeAutoResolved() {
        return this == HIGH_LAG || 
               this == STALE_PROJECTION || 
               this == HIGH_ERROR_RATE ||
               this == LONG_PAUSED;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}