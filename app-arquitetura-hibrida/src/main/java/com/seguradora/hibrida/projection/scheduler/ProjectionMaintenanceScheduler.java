package com.seguradora.hibrida.projection.scheduler;

import com.seguradora.hibrida.projection.consistency.ConsistencyReport;
import com.seguradora.hibrida.projection.consistency.ProjectionConsistencyChecker;
import com.seguradora.hibrida.projection.rebuild.ProjectionRebuilder;
import com.seguradora.hibrida.projection.rebuild.ProjectionRebuildProperties;
import com.seguradora.hibrida.projection.rebuild.RebuildResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Scheduler responsável pela manutenção automática de projeções.
 * 
 * <p>Funcionalidades:
 * <ul>
 *   <li>Verificação periódica de consistência</li>
 *   <li>Rebuild automático de projeções que precisam</li>
 *   <li>Limpeza de dados antigos</li>
 *   <li>Geração de relatórios de saúde</li>
 *   <li>Otimização de performance</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(prefix = "cqrs.projection.rebuild", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ProjectionMaintenanceScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(ProjectionMaintenanceScheduler.class);
    
    private final ProjectionRebuilder rebuilder;
    private final ProjectionConsistencyChecker consistencyChecker;
    private final ProjectionRebuildProperties properties;
    
    private Instant lastMaintenanceRun = Instant.now();
    private Instant lastConsistencyCheck = Instant.now();
    private ConsistencyReport lastConsistencyReport;
    
    public ProjectionMaintenanceScheduler(ProjectionRebuilder rebuilder,
                                        ProjectionConsistencyChecker consistencyChecker,
                                        ProjectionRebuildProperties properties) {
        this.rebuilder = rebuilder;
        this.consistencyChecker = consistencyChecker;
        this.properties = properties;
    }
    
    /**
     * Execução automática de rebuild de projeções que precisam.
     * Executa a cada intervalo configurado.
     */
    @Scheduled(fixedDelayString = "#{@projectionRebuildProperties.autoCheckIntervalSeconds * 1000}")
    public void autoRebuildProjections() {
        if (!properties.isEnabled()) {
            return;
        }
        
        log.debug("Executando verificação automática para rebuild de projeções");
        
        try {
            CompletableFuture<List<RebuildResult>> future = rebuilder.rebuildProjectionsNeedingRebuild();
            
            future.whenComplete((results, throwable) -> {
                if (throwable != null) {
                    log.error("Erro no rebuild automático de projeções: {}", throwable.getMessage(), throwable);
                } else if (results != null && !results.isEmpty()) {
                    log.info("Rebuild automático concluído: {} projeções processadas", results.size());
                    
                    long successCount = results.stream()
                        .filter(RebuildResult::isSuccess)
                        .count();
                    
                    long failureCount = results.size() - successCount;
                    
                    if (failureCount > 0) {
                        log.warn("Rebuild automático: {} sucessos, {} falhas", successCount, failureCount);
                    } else {
                        log.info("Rebuild automático: todas as {} projeções foram processadas com sucesso", successCount);
                    }
                }
            });
            
        } catch (Exception e) {
            log.error("Erro na execução do rebuild automático: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Verificação periódica de consistência.
     * Executa a cada 10 minutos.
     */
    @Scheduled(fixedDelay = 600000) // 10 minutos
    public void periodicConsistencyCheck() {
        log.debug("Executando verificação periódica de consistência");
        
        try {
            ConsistencyReport report = consistencyChecker.checkAllProjections();
            lastConsistencyReport = report;
            lastConsistencyCheck = Instant.now();
            
            if (report.hasCriticalIssues()) {
                log.warn("Verificação de consistência encontrou {} issues críticos", 
                        report.getCriticalIssuesCount());
            } else {
                log.debug("Verificação de consistência: {} projeções, {} issues, score: {:.1f}%", 
                         report.totalProjections(), report.getTotalIssues(), report.getHealthScore());
            }
            
        } catch (Exception e) {
            log.error("Erro na verificação periódica de consistência: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Manutenção geral do sistema de projeções.
     * Executa diariamente às 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void dailyMaintenance() {
        log.info("Iniciando manutenção diária do sistema de projeções");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Verificação completa de consistência
            log.info("Executando verificação completa de consistência");
            ConsistencyReport report = consistencyChecker.checkAllProjections();
            
            // 2. Limpeza de dados antigos (se implementado)
            log.info("Executando limpeza de dados antigos");
            cleanupOldData();
            
            // 3. Otimização de performance (se implementado)
            log.info("Executando otimização de performance");
            performanceOptimization();
            
            // 4. Geração de relatório de saúde
            log.info("Gerando relatório de saúde");
            generateHealthReport(report);
            
            long duration = System.currentTimeMillis() - startTime;
            lastMaintenanceRun = Instant.now();
            
            log.info("Manutenção diária concluída em {}ms. Score de saúde: {:.1f}%", 
                    duration, report.getHealthScore());
            
        } catch (Exception e) {
            log.error("Erro na manutenção diária: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Health check do sistema de projeções.
     * Executa a cada 5 minutos.
     */
    @Scheduled(fixedDelay = 300000) // 5 minutos
    public void healthCheck() {
        log.trace("Executando health check do sistema de projeções");
        
        try {
            // Verificar se há projeções críticas
            if (lastConsistencyReport != null && lastConsistencyReport.hasCriticalIssues()) {
                log.warn("Health check: {} issues críticos detectados", 
                        lastConsistencyReport.getCriticalIssuesCount());
            }
            
            // Verificar se a verificação de consistência está atualizada
            Instant staleThreshold = Instant.now().minus(15, ChronoUnit.MINUTES);
            if (lastConsistencyCheck.isBefore(staleThreshold)) {
                log.warn("Health check: verificação de consistência está desatualizada (última: {})", 
                        lastConsistencyCheck);
            }
            
            // Verificar se a manutenção está em dia
            Instant maintenanceThreshold = Instant.now().minus(25, ChronoUnit.HOURS);
            if (lastMaintenanceRun.isBefore(maintenanceThreshold)) {
                log.warn("Health check: manutenção diária está atrasada (última: {})", 
                        lastMaintenanceRun);
            }
            
        } catch (Exception e) {
            log.error("Erro no health check: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Limpeza de dados antigos.
     */
    private void cleanupOldData() {
        try {
            // Implementar limpeza de logs antigos, métricas antigas, etc.
            log.debug("Limpeza de dados antigos executada");
            
        } catch (Exception e) {
            log.error("Erro na limpeza de dados antigos: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Otimização de performance.
     */
    private void performanceOptimization() {
        try {
            // Implementar otimizações como:
            // - Análise de índices
            // - Compactação de dados
            // - Limpeza de cache
            log.debug("Otimização de performance executada");
            
        } catch (Exception e) {
            log.error("Erro na otimização de performance: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Geração de relatório de saúde.
     */
    private void generateHealthReport(ConsistencyReport report) {
        try {
            log.info("=== RELATÓRIO DE SAÚDE DAS PROJEÇÕES ===");
            log.info("Timestamp: {}", report.timestamp());
            log.info("Total de projeções: {}", report.totalProjections());
            log.info("Projeções saudáveis: {}", report.getHealthyProjectionsCount());
            log.info("Total de issues: {}", report.getTotalIssues());
            log.info("Issues críticos: {}", report.getCriticalIssuesCount());
            log.info("Issues de alta prioridade: {}", report.getHighPriorityIssuesCount());
            log.info("Score de saúde: {:.1f}%", report.getHealthScore());
            log.info("Duração da verificação: {}ms", report.durationMs());
            
            if (!report.getProjectionsWithIssues().isEmpty()) {
                log.info("Projeções com issues: {}", report.getProjectionsWithIssues());
            }
            
            log.info("=== FIM DO RELATÓRIO ===");
            
        } catch (Exception e) {
            log.error("Erro na geração do relatório de saúde: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Obtém o último relatório de consistência.
     */
    public ConsistencyReport getLastConsistencyReport() {
        return lastConsistencyReport;
    }
    
    /**
     * Obtém timestamp da última execução de manutenção.
     */
    public Instant getLastMaintenanceRun() {
        return lastMaintenanceRun;
    }
    
    /**
     * Obtém timestamp da última verificação de consistência.
     */
    public Instant getLastConsistencyCheck() {
        return lastConsistencyCheck;
    }
    
    /**
     * Verifica se o sistema está saudável.
     */
    public boolean isSystemHealthy() {
        if (lastConsistencyReport == null) {
            return false; // Ainda não executou verificação
        }
        
        return lastConsistencyReport.isHealthy() && 
               lastConsistencyCheck.isAfter(Instant.now().minus(15, ChronoUnit.MINUTES));
    }
    
    /**
     * Obtém score de saúde atual.
     */
    public double getCurrentHealthScore() {
        return lastConsistencyReport != null ? lastConsistencyReport.getHealthScore() : 0.0;
    }
}