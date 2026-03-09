package com.seguradora.hibrida.eventstore.replay;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface principal para replay de eventos do Event Store.
 * 
 * <p>Permite reprocessar eventos históricos com diferentes filtros e configurações,
 * incluindo modo simulação para validação sem efeitos colaterais.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface EventReplayer {
    
    /**
     * Executa replay de eventos por período específico.
     * 
     * <p>Reprocessa todos os eventos entre as datas especificadas,
     * respeitando a configuração de velocidade e filtros.
     * 
     * @param configuration Configuração do replay
     * @return CompletableFuture com resultado do replay
     * @throws ReplayException em caso de erro na execução
     */
    CompletableFuture<ReplayResult> replayByPeriod(ReplayConfiguration configuration);
    
    /**
     * Executa replay de eventos por tipo específico.
     * 
     * <p>Reprocessa apenas eventos do tipo especificado no período,
     * útil para reprocessar projeções específicas.
     * 
     * @param eventType Tipo do evento a ser reprocessado
     * @param from Data inicial (inclusive)
     * @param to Data final (inclusive)
     * @param configuration Configuração adicional do replay
     * @return CompletableFuture com resultado do replay
     */
    CompletableFuture<ReplayResult> replayByEventType(String eventType, Instant from, Instant to, 
                                                     ReplayConfiguration configuration);
    
    /**
     * Executa replay de eventos por aggregate específico.
     * 
     * <p>Reprocessa todos os eventos de um aggregate específico,
     * útil para corrigir inconsistências em um aggregate.
     * 
     * @param aggregateId ID do aggregate
     * @param fromVersion Versão inicial (opcional)
     * @param configuration Configuração do replay
     * @return CompletableFuture com resultado do replay
     */
    CompletableFuture<ReplayResult> replayByAggregate(String aggregateId, Long fromVersion,
                                                     ReplayConfiguration configuration);
    
    /**
     * Executa replay com filtros avançados.
     * 
     * <p>Permite combinação de múltiplos filtros (AND/OR) para
     * reprocessamento seletivo de eventos.
     * 
     * @param filter Filtro avançado para seleção de eventos
     * @param configuration Configuração do replay
     * @return CompletableFuture com resultado do replay
     */
    CompletableFuture<ReplayResult> replayWithFilter(ReplayFilter filter, 
                                                    ReplayConfiguration configuration);
    
    /**
     * Executa replay em modo simulação.
     * 
     * <p>Processa eventos sem executar efeitos colaterais,
     * gerando relatório de impacto para validação.
     * 
     * @param configuration Configuração do replay (deve ter simulationMode = true)
     * @return CompletableFuture com resultado da simulação
     */
    CompletableFuture<ReplayResult> simulateReplay(ReplayConfiguration configuration);
    
    /**
     * Pausa replay em execução.
     * 
     * @param replayId ID do replay em execução
     * @return true se pausado com sucesso, false se não encontrado
     */
    boolean pauseReplay(UUID replayId);
    
    /**
     * Retoma replay pausado.
     * 
     * @param replayId ID do replay pausado
     * @return true se retomado com sucesso, false se não encontrado
     */
    boolean resumeReplay(UUID replayId);
    
    /**
     * Cancela replay em execução.
     * 
     * @param replayId ID do replay a ser cancelado
     * @return true se cancelado com sucesso, false se não encontrado
     */
    boolean cancelReplay(UUID replayId);
    
    /**
     * Obtém progresso de replay em execução.
     * 
     * @param replayId ID do replay
     * @return Progresso atual ou null se não encontrado
     */
    ReplayProgress getProgress(UUID replayId);
    
    /**
     * Lista todos os replays ativos.
     * 
     * @return Lista de progressos de replays ativos
     */
    List<ReplayProgress> getActiveReplays();
    
    /**
     * Obtém histórico de replays executados.
     * 
     * @param limit Número máximo de registros (padrão: 50)
     * @return Lista de resultados de replays anteriores
     */
    List<ReplayResult> getReplayHistory(int limit);
    
    /**
     * Verifica se o replayer está saudável e operacional.
     * 
     * @return true se saudável, false caso contrário
     */
    boolean isHealthy();
    
    /**
     * Obtém estatísticas de execução do replayer.
     * 
     * @return Estatísticas detalhadas
     */
    ReplayStatistics getStatistics();
}