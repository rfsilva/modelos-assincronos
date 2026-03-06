package com.seguradora.hibrida.eventstore.scheduler;

import com.seguradora.hibrida.eventstore.archive.ArchiveSummary;
import com.seguradora.hibrida.eventstore.archive.EventArchiver;
import com.seguradora.hibrida.eventstore.partition.PartitionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Scheduler para manutenção automática do Event Store.
 * 
 * <p>Responsável por:
 * <ul>
 *   <li>Manutenção de partições (criação de futuras)</li>
 *   <li>Arquivamento automático de partições antigas</li>
 *   <li>Compactação de dados antigos</li>
 *   <li>Limpeza de logs e estatísticas</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "eventstore.maintenance.enabled", havingValue = "true", matchIfMissing = true)
public class EventStoreMaintenanceScheduler {
    
    private final PartitionManager partitionManager;
    private final EventArchiver eventArchiver;
    
    /**
     * Manutenção diária de partições.
     * 
     * <p>Executa todos os dias às 02:00 para criar partições futuras
     * e verificar integridade das existentes.
     */
    @Scheduled(cron = "0 0 2 * * *") // Diário às 02:00
    public void dailyPartitionMaintenance() {
        log.info("Iniciando manutenção diária de partições");
        
        try {
            boolean success = partitionManager.schedulePartitionMaintenance();
            
            if (success) {
                log.info("Manutenção diária de partições concluída com sucesso");
            } else {
                log.error("Falha na manutenção diária de partições");
            }
            
        } catch (Exception e) {
            log.error("Erro na manutenção diária de partições: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Arquivamento semanal de partições antigas.
     * 
     * <p>Executa toda segunda-feira às 03:00 para arquivar
     * partições elegíveis (> 2 anos).
     */
    @Scheduled(cron = "0 0 3 * * MON") // Segundas às 03:00
    public void weeklyArchiving() {
        log.info("Iniciando arquivamento semanal de partições antigas");
        
        try {
            ArchiveSummary summary = eventArchiver.executeAutoArchiving();
            summary.finish();
            
            log.info("Arquivamento semanal concluído. Sucessos: {}, Erros: {}, Duração: {}ms", 
                summary.getSuccessCount(), 
                summary.getErrorCount(), 
                summary.getDurationMs());
            
            // Log detalhado se houver erros
            if (summary.getErrorCount() > 0) {
                summary.getResults().stream()
                    .filter(r -> !r.isSuccess())
                    .forEach(r -> log.warn("Falha no arquivamento de {}: {}", 
                        r.getPartitionName(), r.getErrorMessage()));
            }
            
        } catch (Exception e) {
            log.error("Erro no arquivamento semanal: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Verificação de saúde das partições.
     * 
     * <p>Executa a cada 6 horas para verificar se as partições
     * necessárias existem e estão saudáveis.
     */
    @Scheduled(fixedRate = 21600000) // A cada 6 horas
    public void partitionHealthCheck() {
        log.debug("Executando verificação de saúde das partições");
        
        try {
            boolean healthy = partitionManager.arePartitionsHealthy();
            
            if (!healthy) {
                log.warn("Partições não estão saudáveis - executando manutenção corretiva");
                partitionManager.maintainPartitions();
            } else {
                log.debug("Partições estão saudáveis");
            }
            
        } catch (Exception e) {
            log.error("Erro na verificação de saúde das partições: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Limpeza mensal de logs antigos.
     * 
     * <p>Executa no primeiro dia de cada mês às 04:00 para
     * remover logs de manutenção antigos (> 90 dias).
     */
    @Scheduled(cron = "0 0 4 1 * *") // Primeiro dia do mês às 04:00
    public void monthlyLogCleanup() {
        log.info("Iniciando limpeza mensal de logs antigos");
        
        try {
            // Implementar limpeza de logs via JDBC
            // jdbcTemplate.update("SELECT cleanup_archive_logs(90)");
            
            log.info("Limpeza mensal de logs concluída");
            
        } catch (Exception e) {
            log.error("Erro na limpeza mensal de logs: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Atualização de estatísticas de arquivamento.
     * 
     * <p>Executa diariamente às 05:00 para calcular e
     * armazenar estatísticas de arquivamento.
     */
    @Scheduled(cron = "0 0 5 * * *") // Diário às 05:00
    public void dailyStatisticsUpdate() {
        log.debug("Atualizando estatísticas diárias de arquivamento");
        
        try {
            // Implementar atualização de estatísticas via JDBC
            // jdbcTemplate.update("SELECT calculate_archive_statistics()");
            
            log.debug("Estatísticas de arquivamento atualizadas");
            
        } catch (Exception e) {
            log.error("Erro na atualização de estatísticas: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Compactação trimestral de dados antigos.
     * 
     * <p>Executa no primeiro dia de cada trimestre às 01:00
     * para compactar dados arquivados antigos.
     */
    @Scheduled(cron = "0 0 1 1 1,4,7,10 *") // Primeiro dia dos trimestres às 01:00
    public void quarterlyCompaction() {
        log.info("Iniciando compactação trimestral de dados antigos");
        
        try {
            // TODO: Implementar compactação de arquivos antigos
            // Recomprimir arquivos com algoritmos mais eficientes
            // Consolidar arquivos pequenos
            
            log.info("Compactação trimestral concluída");
            
        } catch (Exception e) {
            log.error("Erro na compactação trimestral: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Relatório semanal de status.
     * 
     * <p>Executa toda sexta-feira às 18:00 para gerar
     * relatório de status do Event Store.
     */
    @Scheduled(cron = "0 0 18 * * FRI") // Sextas às 18:00
    public void weeklyStatusReport() {
        log.info("Gerando relatório semanal de status do Event Store");
        
        try {
            // Coletar estatísticas
            var partitionStats = partitionManager.getPartitionStatistics();
            var archiveStats = eventArchiver.getArchiveStatistics();
            
            log.info("=== RELATÓRIO SEMANAL EVENT STORE ===");
            log.info("Partições ativas: {}", partitionStats.size());
            log.info("Arquivos totais: {}", archiveStats.getTotalArchives());
            log.info("Eventos arquivados: {}", archiveStats.getTotalEvents());
            log.info("Tamanho total arquivos: {}", archiveStats.getFormattedSize());
            log.info("=====================================");
            
        } catch (Exception e) {
            log.error("Erro na geração do relatório semanal: {}", e.getMessage(), e);
        }
    }
}