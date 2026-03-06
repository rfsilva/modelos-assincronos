package com.seguradora.hibrida.projection.consistency;

/**
 * Níveis de severidade para issues de consistência.
 */
public enum IssueSeverity {
    
    /**
     * Crítico - requer ação imediata, pode afetar operações.
     */
    CRITICAL("Crítico", 4),
    
    /**
     * Alto - requer atenção em breve, pode causar problemas.
     */
    HIGH("Alto", 3),
    
    /**
     * Médio - deve ser investigado, mas não é urgente.
     */
    MEDIUM("Médio", 2),
    
    /**
     * Baixo - informativo, pode ser tratado quando conveniente.
     */
    LOW("Baixo", 1);
    
    private final String displayName;
    private final int priority;
    
    IssueSeverity(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getPriority() {
        return priority;
    }
    
    /**
     * Verifica se é severidade crítica ou alta.
     */
    public boolean isHighPriority() {
        return this == CRITICAL || this == HIGH;
    }
    
    /**
     * Verifica se requer ação imediata.
     */
    public boolean requiresImmediateAction() {
        return this == CRITICAL;
    }
    
    /**
     * Obtém cor para exibição (para dashboards).
     */
    public String getColor() {
        return switch (this) {
            case CRITICAL -> "#FF0000"; // Vermelho
            case HIGH -> "#FF8C00";     // Laranja
            case MEDIUM -> "#FFD700";   // Amarelo
            case LOW -> "#90EE90";      // Verde claro
        };
    }
    
    /**
     * Obtém ícone para exibição.
     */
    public String getIcon() {
        return switch (this) {
            case CRITICAL -> "🔴";
            case HIGH -> "🟠";
            case MEDIUM -> "🟡";
            case LOW -> "🟢";
        };
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}