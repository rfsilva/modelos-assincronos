package com.seguradora.hibrida.projection;

/**
 * Exceção específica para erros em processamento de projeções.
 * 
 * <p>Encapsula informações específicas sobre falhas em projection handlers,
 * incluindo o nome da projeção e contexto do erro.
 */
public class ProjectionException extends RuntimeException {
    
    private final String projectionName;
    private final String eventType;
    private final boolean retryable;
    
    public ProjectionException(String message, String projectionName) {
        super(message);
        this.projectionName = projectionName;
        this.eventType = null;
        this.retryable = true;
    }
    
    public ProjectionException(String message, String projectionName, Throwable cause) {
        super(message, cause);
        this.projectionName = projectionName;
        this.eventType = null;
        this.retryable = true;
    }
    
    public ProjectionException(String message, String projectionName, String eventType, boolean retryable) {
        super(message);
        this.projectionName = projectionName;
        this.eventType = eventType;
        this.retryable = retryable;
    }
    
    public ProjectionException(String message, String projectionName, String eventType, 
                             boolean retryable, Throwable cause) {
        super(message, cause);
        this.projectionName = projectionName;
        this.eventType = eventType;
        this.retryable = retryable;
    }
    
    public String getProjectionName() {
        return projectionName;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Projection: ").append(projectionName);
        
        if (eventType != null) {
            sb.append(", Event: ").append(eventType);
        }
        
        sb.append(" - ").append(super.getMessage());
        
        if (!retryable) {
            sb.append(" (NOT RETRYABLE)");
        }
        
        return sb.toString();
    }
}