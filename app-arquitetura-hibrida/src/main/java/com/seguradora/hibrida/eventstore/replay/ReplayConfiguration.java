package com.seguradora.hibrida.eventstore.replay;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Configuração para execução de replay de eventos.
 * 
 * <p>Define todos os parâmetros necessários para controlar
 * como o replay será executado, incluindo filtros, velocidade
 * e modo de operação.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
public class ReplayConfiguration {
    
    /**
     * ID único do replay para rastreamento.
     */
    @Builder.Default
    private UUID replayId = UUID.randomUUID();
    
    /**
     * Nome descritivo do replay.
     */
    private String name;
    
    /**
     * Descrição detalhada do objetivo do replay.
     */
    private String description;
    
    /**
     * Data/hora inicial para replay (inclusive).
     */
    private Instant fromTimestamp;
    
    /**
     * Data/hora final para replay (inclusive).
     */
    private Instant toTimestamp;
    
    /**
     * Tipos de eventos específicos para replay.
     * Se vazio, todos os tipos serão processados.
     */
    @Builder.Default
    private List<String> eventTypes = List.of();
    
    /**
     * IDs de aggregates específicos para replay.
     * Se vazio, todos os aggregates serão processados.
     */
    @Builder.Default
    private List<String> aggregateIds = List.of();
    
    /**
     * Tipos de aggregates específicos para replay.
     * Se vazio, todos os tipos serão processados.
     */
    @Builder.Default
    private List<String> aggregateTypes = List.of();
    
    /**
     * Modo simulação - não executa efeitos colaterais.
     */
    @Builder.Default
    private boolean simulationMode = false;
    
    /**
     * Velocidade do replay (eventos por segundo).
     * 0 = velocidade máxima, > 0 = throttling.
     */
    @Builder.Default
    private int eventsPerSecond = 0;
    
    /**
     * Tamanho do lote para processamento.
     */
    @Builder.Default
    private int batchSize = 100;
    
    /**
     * Timeout em segundos para processamento de cada lote.
     */
    @Builder.Default
    private int batchTimeoutSeconds = 30;
    
    /**
     * Número máximo de tentativas em caso de erro.
     */
    @Builder.Default
    private int maxRetries = 3;
    
    /**
     * Delay entre tentativas em caso de erro (milissegundos).
     */
    @Builder.Default
    private long retryDelayMs = 1000;
    
    /**
     * Parar replay no primeiro erro.
     */
    @Builder.Default
    private boolean stopOnError = false;
    
    /**
     * Incluir eventos arquivados no replay.
     */
    @Builder.Default
    private boolean includeArchivedEvents = false;
    
    /**
     * Filtros customizados por metadados.
     */
    @Builder.Default
    private Map<String, Object> metadataFilters = Map.of();
    
    /**
     * Handlers específicos para executar durante o replay.
     * Se vazio, todos os handlers registrados serão executados.
     */
    @Builder.Default
    private List<String> targetHandlers = List.of();
    
    /**
     * Handlers a serem excluídos do replay.
     */
    @Builder.Default
    private List<String> excludedHandlers = List.of();
    
    /**
     * Gerar relatório detalhado do replay.
     */
    @Builder.Default
    private boolean generateDetailedReport = false;
    
    /**
     * Notificar progresso a cada N eventos processados.
     */
    @Builder.Default
    private int progressNotificationInterval = 1000;
    
    /**
     * Usuário que iniciou o replay.
     */
    private String initiatedBy;
    
    /**
     * Timestamp de criação da configuração.
     */
    @Builder.Default
    private Instant createdAt = Instant.now();
    
    /**
     * Metadados adicionais para o replay.
     */
    @Builder.Default
    private Map<String, Object> additionalMetadata = Map.of();
    
    /**
     * Valida se a configuração é válida.
     * 
     * @return true se válida, false caso contrário
     */
    public boolean isValid() {
        if (fromTimestamp != null && toTimestamp != null && fromTimestamp.isAfter(toTimestamp)) {
            return false;
        }
        
        if (eventsPerSecond < 0 || batchSize <= 0 || batchTimeoutSeconds <= 0) {
            return false;
        }
        
        if (maxRetries < 0 || retryDelayMs < 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Cria configuração para replay por período.
     * 
     * @param from Data inicial
     * @param to Data final
     * @return Configuração configurada
     */
    public static ReplayConfiguration forPeriod(Instant from, Instant to) {
        return ReplayConfiguration.builder()
                .fromTimestamp(from)
                .toTimestamp(to)
                .name("Replay por período")
                .description(String.format("Replay de eventos entre %s e %s", from, to))
                .build();
    }
    
    /**
     * Cria configuração para replay por tipo de evento.
     * 
     * @param eventType Tipo do evento
     * @param from Data inicial
     * @param to Data final
     * @return Configuração configurada
     */
    public static ReplayConfiguration forEventType(String eventType, Instant from, Instant to) {
        return ReplayConfiguration.builder()
                .eventTypes(List.of(eventType))
                .fromTimestamp(from)
                .toTimestamp(to)
                .name("Replay por tipo de evento")
                .description(String.format("Replay de eventos do tipo %s entre %s e %s", eventType, from, to))
                .build();
    }
    
    /**
     * Cria configuração para replay por aggregate.
     * 
     * @param aggregateId ID do aggregate
     * @return Configuração configurada
     */
    public static ReplayConfiguration forAggregate(String aggregateId) {
        return ReplayConfiguration.builder()
                .aggregateIds(List.of(aggregateId))
                .name("Replay por aggregate")
                .description(String.format("Replay de eventos do aggregate %s", aggregateId))
                .build();
    }
    
    /**
     * Cria configuração para simulação.
     * 
     * @param baseConfig Configuração base
     * @return Configuração para simulação
     */
    public static ReplayConfiguration forSimulation(ReplayConfiguration baseConfig) {
        return baseConfig.toBuilder()
                .simulationMode(true)
                .name("Simulação - " + baseConfig.getName())
                .description("Simulação: " + baseConfig.getDescription())
                .generateDetailedReport(true)
                .build();
    }
}