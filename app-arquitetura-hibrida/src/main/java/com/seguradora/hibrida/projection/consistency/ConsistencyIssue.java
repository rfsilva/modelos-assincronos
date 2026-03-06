package com.seguradora.hibrida.projection.consistency;

import java.time.Instant;

/**
 * Representa um issue de consistência encontrado em uma projeção.
 * 
 * @param projectionName Nome da projeção afetada
 * @param type Tipo do issue
 * @param severity Severidade do issue
 * @param description Descrição detalhada do issue
 * @param value Valor associado ao issue (lag, taxa de erro, etc.)
 */
public record ConsistencyIssue(
    String projectionName,
    IssueType type,
    IssueSeverity severity,
    String description,
    Object value
) {
    
    /**
     * Timestamp de criação do issue.
     */
    public Instant getTimestamp() {
        return Instant.now();
    }
    
    /**
     * Verifica se é um issue crítico.
     */
    public boolean isCritical() {
        return severity == IssueSeverity.CRITICAL;
    }
    
    /**
     * Verifica se é um issue de alta prioridade.
     */
    public boolean isHighPriority() {
        return severity == IssueSeverity.HIGH;
    }
    
    /**
     * Obtém valor como Long (para lag, tempo, etc.).
     */
    public Long getValueAsLong() {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
    
    /**
     * Obtém valor como Double (para taxas, percentuais, etc.).
     */
    public Double getValueAsDouble() {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
    
    /**
     * Obtém representação textual do valor.
     */
    public String getValueAsString() {
        if (value == null) {
            return "N/A";
        }
        
        if (value instanceof Double) {
            return String.format("%.2f", (Double) value);
        }
        
        return value.toString();
    }
    
    /**
     * Cria issue de lag alto.
     */
    public static ConsistencyIssue highLag(String projectionName, long lag, long threshold) {
        IssueSeverity severity = lag > threshold * 5 ? IssueSeverity.CRITICAL : IssueSeverity.HIGH;
        return new ConsistencyIssue(
            projectionName,
            IssueType.HIGH_LAG,
            severity,
            String.format("Lag de %d eventos (threshold: %d)", lag, threshold),
            lag
        );
    }
    
    /**
     * Cria issue de taxa de erro alta.
     */
    public static ConsistencyIssue highErrorRate(String projectionName, double errorRate, double threshold) {
        IssueSeverity severity = errorRate > threshold * 2 ? IssueSeverity.CRITICAL : IssueSeverity.HIGH;
        return new ConsistencyIssue(
            projectionName,
            IssueType.HIGH_ERROR_RATE,
            severity,
            String.format("Taxa de erro de %.2f%% (threshold: %.2f%%)", errorRate * 100, threshold * 100),
            errorRate
        );
    }
    
    /**
     * Cria issue de projeção travada.
     */
    public static ConsistencyIssue staleProjection(String projectionName, long minutesStale) {
        IssueSeverity severity = minutesStale > 60 ? IssueSeverity.CRITICAL : IssueSeverity.HIGH;
        return new ConsistencyIssue(
            projectionName,
            IssueType.STALE_PROJECTION,
            severity,
            String.format("Projeção travada há %d minutos", minutesStale),
            minutesStale
        );
    }
    
    /**
     * Cria issue de erro persistente.
     */
    public static ConsistencyIssue persistentError(String projectionName, String errorMessage, long minutesInError) {
        return new ConsistencyIssue(
            projectionName,
            IssueType.PERSISTENT_ERROR,
            IssueSeverity.HIGH,
            String.format("Erro persistente há %d minutos: %s", minutesInError, errorMessage),
            minutesInError
        );
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s (valor: %s)",
                           severity, projectionName, type, description, getValueAsString());
    }
}