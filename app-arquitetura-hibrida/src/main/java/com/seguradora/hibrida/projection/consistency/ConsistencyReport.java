package com.seguradora.hibrida.projection.consistency;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Relatório de verificação de consistência de projeções.
 * 
 * @param totalProjections Total de projeções verificadas
 * @param issues Lista de issues encontrados
 * @param maxEventId Último evento disponível no momento da verificação
 * @param durationMs Duração da verificação em milissegundos
 * @param timestamp Timestamp da verificação
 */
public record ConsistencyReport(
    int totalProjections,
    List<ConsistencyIssue> issues,
    Long maxEventId,
    long durationMs,
    Instant timestamp
) {
    
    /**
     * Obtém número total de issues.
     */
    public int getTotalIssues() {
        return issues.size();
    }
    
    /**
     * Obtém issues por severidade.
     */
    public Map<IssueSeverity, List<ConsistencyIssue>> getIssuesBySeverity() {
        return issues.stream()
            .collect(Collectors.groupingBy(ConsistencyIssue::severity));
    }
    
    /**
     * Obtém issues por tipo.
     */
    public Map<IssueType, List<ConsistencyIssue>> getIssuesByType() {
        return issues.stream()
            .collect(Collectors.groupingBy(ConsistencyIssue::type));
    }
    
    /**
     * Obtém número de issues críticos.
     */
    public long getCriticalIssuesCount() {
        return issues.stream()
            .filter(issue -> issue.severity() == IssueSeverity.CRITICAL)
            .count();
    }
    
    /**
     * Obtém número de issues de alta prioridade.
     */
    public long getHighPriorityIssuesCount() {
        return issues.stream()
            .filter(issue -> issue.severity() == IssueSeverity.HIGH)
            .count();
    }
    
    /**
     * Verifica se há issues críticos.
     */
    public boolean hasCriticalIssues() {
        return getCriticalIssuesCount() > 0;
    }
    
    /**
     * Verifica se o sistema está saudável (sem issues críticos ou de alta prioridade).
     */
    public boolean isHealthy() {
        return getCriticalIssuesCount() == 0 && getHighPriorityIssuesCount() == 0;
    }
    
    /**
     * Obtém score de saúde (0-100).
     */
    public double getHealthScore() {
        if (totalProjections == 0) {
            return 100.0;
        }
        
        long criticalIssues = getCriticalIssuesCount();
        long highIssues = getHighPriorityIssuesCount();
        long mediumIssues = issues.stream()
            .filter(issue -> issue.severity() == IssueSeverity.MEDIUM)
            .count();
        
        // Peso: crítico = 10, alto = 5, médio = 2, baixo = 1
        double weightedIssues = criticalIssues * 10 + highIssues * 5 + mediumIssues * 2 + 
                               (getTotalIssues() - criticalIssues - highIssues - mediumIssues);
        
        double maxPossibleScore = totalProjections * 10; // Todos críticos seria o pior caso
        
        return Math.max(0, 100.0 - (weightedIssues / maxPossibleScore * 100));
    }
    
    /**
     * Obtém resumo textual do relatório.
     */
    public String getSummary() {
        return String.format(
            "Consistência: %d projeções, %d issues (%d críticos, %d altos), score: %.1f%%, %dms",
            totalProjections, getTotalIssues(), getCriticalIssuesCount(), 
            getHighPriorityIssuesCount(), getHealthScore(), durationMs
        );
    }
    
    /**
     * Obtém projeções com issues.
     */
    public List<String> getProjectionsWithIssues() {
        return issues.stream()
            .map(ConsistencyIssue::projectionName)
            .distinct()
            .sorted()
            .toList();
    }
    
    /**
     * Obtém projeções saudáveis (sem issues).
     */
    public int getHealthyProjectionsCount() {
        return totalProjections - getProjectionsWithIssues().size();
    }
}