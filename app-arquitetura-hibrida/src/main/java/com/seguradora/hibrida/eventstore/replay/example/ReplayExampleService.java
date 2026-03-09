package com.seguradora.hibrida.eventstore.replay.example;

import com.seguradora.hibrida.eventstore.replay.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço de exemplo para demonstrar o uso do sistema de replay.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReplayExampleService {
    
    private final EventReplayer eventReplayer;
    
    /**
     * Exemplo de replay por período - últimas 24 horas.
     */
    public CompletableFuture<ReplayResult> replayLast24Hours() {
        log.info("Iniciando replay das últimas 24 horas");
        
        Instant now = Instant.now();
        Instant yesterday = now.minus(24, ChronoUnit.HOURS);
        
        ReplayConfiguration config = ReplayConfiguration.builder()
            .name("Replay Últimas 24h")
            .description("Reprocessamento de eventos das últimas 24 horas")
            .fromTimestamp(yesterday)
            .toTimestamp(now)
            .eventsPerSecond(500) // Controle de velocidade
            .batchSize(50)
            .generateDetailedReport(true)
            .initiatedBy("ReplayExampleService")
            .build();
        
        return eventReplayer.replayByPeriod(config)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Erro no replay das últimas 24h", throwable);
                } else {
                    log.info("Replay das últimas 24h concluído: {}", result.getSummary());
                }
            });
    }
    
    /**
     * Exemplo de replay por tipo de evento específico.
     */
    public CompletableFuture<ReplayResult> replaySinistroEvents() {
        log.info("Iniciando replay de eventos de sinistro");
        
        Instant now = Instant.now();
        Instant lastWeek = now.minus(7, ChronoUnit.DAYS);
        
        ReplayConfiguration config = ReplayConfiguration.builder()
            .name("Replay Eventos Sinistro")
            .description("Reprocessamento de eventos de sinistro da última semana")
            .eventTypes(List.of("SinistroCriado", "SinistroAtualizado", "SinistroFinalizado"))
            .eventsPerSecond(100)
            .maxRetries(5)
            .stopOnError(false)
            .initiatedBy("ReplayExampleService")
            .build();
        
        return eventReplayer.replayByEventType("SinistroCriado", lastWeek, now, config)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Erro no replay de eventos de sinistro", throwable);
                } else {
                    log.info("Replay de eventos de sinistro concluído: {}", result.getSummary());
                }
            });
    }
    
    /**
     * Exemplo de simulação de replay para validação.
     */
    public CompletableFuture<ReplayResult> simulateReplayValidation() {
        log.info("Iniciando simulação de replay para validação");
        
        Instant now = Instant.now();
        Instant lastHour = now.minus(1, ChronoUnit.HOURS);
        
        ReplayConfiguration config = ReplayConfiguration.builder()
            .name("Simulação de Validação")
            .description("Simulação para validar impacto do replay")
            .fromTimestamp(lastHour)
            .toTimestamp(now)
            .simulationMode(true) // Modo simulação
            .generateDetailedReport(true)
            .batchSize(20)
            .initiatedBy("ReplayExampleService")
            .build();
        
        return eventReplayer.simulateReplay(config)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Erro na simulação de replay", throwable);
                } else {
                    log.info("Simulação de replay concluída: {}", result.getSummary());
                    if (result.getDetailedReport() != null) {
                        log.info("Relatório detalhado disponível com {} recomendações", 
                            result.getDetailedReport().getRecommendations().size());
                    }
                }
            });
    }
    
    /**
     * Exemplo de replay com filtros avançados.
     */
    public CompletableFuture<ReplayResult> replayWithAdvancedFilters() {
        log.info("Iniciando replay com filtros avançados");
        
        Instant now = Instant.now();
        Instant lastWeek = now.minus(7, ChronoUnit.DAYS);
        
        // Filtro avançado: apenas eventos de sinistro com valor alto
        ReplayFilter filter = ReplayFilter.builder()
            .fromTimestamp(lastWeek)
            .toTimestamp(now)
            .eventTypes(List.of("SinistroCriado", "SinistroAtualizado"))
            .metadataFilters(Map.of("valorEstimado", ">10000"))
            .operator(ReplayFilter.LogicalOperator.AND)
            .build();
        
        ReplayConfiguration config = ReplayConfiguration.builder()
            .name("Replay Filtros Avançados")
            .description("Replay de sinistros de alto valor com filtros avançados")
            .eventsPerSecond(200)
            .generateDetailedReport(true)
            .initiatedBy("ReplayExampleService")
            .build();
        
        return eventReplayer.replayWithFilter(filter, config)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Erro no replay com filtros avançados", throwable);
                } else {
                    log.info("Replay com filtros avançados concluído: {}", result.getSummary());
                }
            });
    }
    
    /**
     * Exemplo de monitoramento de replay em execução.
     */
    public void monitorActiveReplays() {
        List<ReplayProgress> activeReplays = eventReplayer.getActiveReplays();
        
        log.info("Replays ativos: {}", activeReplays.size());
        
        for (ReplayProgress progress : activeReplays) {
            log.info("Replay {}: {} - {:.2f}% concluído, {:.2f} eventos/s", 
                progress.getReplayId(),
                progress.getName(),
                progress.getProgressPercentage(),
                progress.getCurrentThroughput());
            
            // Exemplo de controle baseado em progresso
            if (progress.getErrorRate() > 10.0) {
                log.warn("Replay {} com alta taxa de erro: {:.2f}%", 
                    progress.getReplayId(), progress.getErrorRate());
                
                // Poderia pausar o replay se necessário
                // eventReplayer.pauseReplay(progress.getReplayId());
            }
        }
    }
    
    /**
     * Exemplo de análise de estatísticas de replay.
     */
    public void analyzeReplayStatistics() {
        ReplayStatistics stats = eventReplayer.getStatistics();
        
        log.info("Estatísticas de Replay:");
        log.info("- Total executados: {}", stats.getTotalReplaysExecuted());
        log.info("- Taxa de sucesso: {:.2f}%", stats.getOverallSuccessRate());
        log.info("- Taxa de erro: {:.2f}%", stats.getOverallErrorRate());
        log.info("- Replays ativos: {}", stats.getActiveReplays());
        log.info("- Throughput médio: {:.2f} eventos/s", stats.getAverageThroughput());
        
        // Análise de tendências
        if (stats.getOverallErrorRate() > 5.0) {
            log.warn("Taxa de erro alta detectada: {:.2f}%", stats.getOverallErrorRate());
        }
        
        if (stats.getAverageThroughput() < 100.0) {
            log.warn("Throughput baixo detectado: {:.2f} eventos/s", stats.getAverageThroughput());
        }
    }
    
    /**
     * Exemplo de replay de aggregate específico.
     */
    public CompletableFuture<ReplayResult> replaySpecificAggregate(String aggregateId) {
        log.info("Iniciando replay do aggregate: {}", aggregateId);
        
        ReplayConfiguration config = ReplayConfiguration.builder()
            .name("Replay Aggregate Específico")
            .description(String.format("Replay completo do aggregate %s", aggregateId))
            .eventsPerSecond(0) // Velocidade máxima
            .generateDetailedReport(true)
            .initiatedBy("ReplayExampleService")
            .build();
        
        return eventReplayer.replayByAggregate(aggregateId, null, config)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Erro no replay do aggregate {}", aggregateId, throwable);
                } else {
                    log.info("Replay do aggregate {} concluído: {}", aggregateId, result.getSummary());
                }
            });
    }
}