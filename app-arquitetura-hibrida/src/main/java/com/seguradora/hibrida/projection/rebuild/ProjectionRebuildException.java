package com.seguradora.hibrida.projection.rebuild;

/**
 * Exceção específica para erros de rebuild de projeções.
 */
public class ProjectionRebuildException extends RuntimeException {
    
    private final String projectionName;
    private final RebuildType rebuildType;
    
    public ProjectionRebuildException(String message) {
        super(message);
        this.projectionName = null;
        this.rebuildType = null;
    }
    
    public ProjectionRebuildException(String message, Throwable cause) {
        super(message, cause);
        this.projectionName = null;
        this.rebuildType = null;
    }
    
    public ProjectionRebuildException(String message, String projectionName, RebuildType rebuildType) {
        super(message);
        this.projectionName = projectionName;
        this.rebuildType = rebuildType;
    }
    
    public ProjectionRebuildException(String message, String projectionName, RebuildType rebuildType, Throwable cause) {
        super(message, cause);
        this.projectionName = projectionName;
        this.rebuildType = rebuildType;
    }
    
    public String getProjectionName() {
        return projectionName;
    }
    
    public RebuildType getRebuildType() {
        return rebuildType;
    }
    
    public boolean hasProjectionContext() {
        return projectionName != null;
    }
    
    @Override
    public String getMessage() {
        if (hasProjectionContext()) {
            return String.format("Erro no rebuild %s da projeção '%s': %s", 
                               rebuildType != null ? rebuildType.getDisplayName().toLowerCase() : "desconhecido",
                               projectionName, 
                               super.getMessage());
        }
        return super.getMessage();
    }
}