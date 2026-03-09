package com.seguradora.hibrida.eventstore.replay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração para o sistema de replay.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "eventstore.replay")
public class ReplayProperties {
    
    /**
     * Habilita o sistema de replay.
     */
    private boolean enabled = true;
    
    /**
     * Configurações padrão para replay.
     */
    private DefaultSettings defaults = new DefaultSettings();
    
    /**
     * Configurações de performance.
     */
    private Performance performance = new Performance();
    
    /**
     * Configurações de monitoramento.
     */
    private Monitoring monitoring = new Monitoring();
    
    /**
     * Configurações padrão.
     */
    @Data
    public static class DefaultSettings {
        
        /**
         * Tamanho padrão do lote.
         */
        private int batchSize = 100;
        
        /**
         * Timeout padrão por lote (segundos).
         */
        private int batchTimeoutSeconds = 30;
        
        /**
         * Número máximo de tentativas.
         */
        private int maxRetries = 3;
        
        /**
         * Delay entre tentativas (milissegundos).
         */
        private long retryDelayMs = 1000;
        
        /**
         * Parar no primeiro erro.
         */
        private boolean stopOnError = false;
        
        /**
         * Gerar relatório detalhado por padrão.
         */
        private boolean generateDetailedReport = false;
        
        /**
         * Intervalo de notificação de progresso.
         */
        private int progressNotificationInterval = 1000;
    }
    
    /**
     * Configurações de performance.
     */
    @Data
    public static class Performance {
        
        /**
         * Número máximo de replays simultâneos.
         */
        private int maxConcurrentReplays = 5;
        
        /**
         * Tamanho do pool de threads.
         */
        private int threadPoolSize = 10;
        
        /**
         * Timeout para operações de replay (segundos).
         */
        private int operationTimeoutSeconds = 3600; // 1 hora
        
        /**
         * Tamanho máximo da fila de replays.
         */
        private int maxQueueSize = 100;
        
        /**
         * Habilita cache de eventos para replay.
         */
        private boolean enableEventCache = true;
        
        /**
         * TTL do cache de eventos (segundos).
         */
        private int eventCacheTtlSeconds = 300; // 5 minutos
    }
    
    /**
     * Configurações de monitoramento.
     */
    @Data
    public static class Monitoring {
        
        /**
         * Habilita métricas detalhadas.
         */
        private boolean enableDetailedMetrics = true;
        
        /**
         * Habilita health checks.
         */
        private boolean enableHealthChecks = true;
        
        /**
         * Intervalo de coleta de métricas (segundos).
         */
        private int metricsCollectionIntervalSeconds = 60;
        
        /**
         * Número máximo de resultados no histórico.
         */
        private int maxHistorySize = 1000;
        
        /**
         * Habilita logs detalhados.
         */
        private boolean enableDetailedLogging = false;
        
        /**
         * Nível de log para replay.
         */
        private String logLevel = "INFO";
    }
}