package com.seguradora.hibrida.eventstore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração do Event Store.
 * 
 * Centraliza todas as configurações relacionadas ao Event Store
 * para facilitar manutenção e documentação.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "eventstore")
public class EventStoreProperties {
    
    /**
     * Configurações de serialização.
     */
    private Serialization serialization = new Serialization();
    
    /**
     * Configurações de snapshot.
     */
    private Snapshot snapshot = new Snapshot();
    
    /**
     * Configurações de performance.
     */
    private Performance performance = new Performance();
    
    /**
     * Configurações de monitoramento.
     */
    private Monitoring monitoring = new Monitoring();
    
    @Data
    public static class Serialization {
        /**
         * Formato de serialização (json, avro, protobuf).
         */
        private String format = "json";
        
        /**
         * Algoritmo de compressão (gzip, lz4, none).
         */
        private String compression = "gzip";
        
        /**
         * Tamanho mínimo para aplicar compressão (em bytes).
         */
        private int compressionThreshold = 1024;
        
        /**
         * Habilita versionamento automático de eventos.
         */
        private boolean versioningEnabled = true;
    }
    
    @Data
    public static class Snapshot {
        /**
         * Frequência de snapshot (número de eventos).
         */
        private int frequency = 50;
        
        /**
         * Processamento assíncrono de snapshots.
         */
        private boolean async = true;
        
        /**
         * Número máximo de snapshots a manter por aggregate.
         */
        private int maxRetention = 5;
        
        /**
         * Habilita limpeza automática de snapshots antigos.
         */
        private boolean autoCleanup = true;
    }
    
    @Data
    public static class Performance {
        /**
         * Tamanho do lote para operações em massa.
         */
        private int batchSize = 100;
        
        /**
         * Timeout para operações de escrita (em segundos).
         */
        private int writeTimeout = 30;
        
        /**
         * Timeout para operações de leitura (em segundos).
         */
        private int readTimeout = 15;
        
        /**
         * Habilita cache de consultas.
         */
        private boolean cacheEnabled = true;
        
        /**
         * TTL do cache em segundos.
         */
        private int cacheTtl = 300;
    }
    
    @Data
    public static class Monitoring {
        /**
         * Habilita métricas detalhadas.
         */
        private boolean metricsEnabled = true;
        
        /**
         * Habilita health checks.
         */
        private boolean healthCheckEnabled = true;
        
        /**
         * Intervalo de coleta de métricas (em segundos).
         */
        private int metricsInterval = 60;
        
        /**
         * Habilita logs de performance.
         */
        private boolean performanceLogsEnabled = false;
    }
}