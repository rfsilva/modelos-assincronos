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
    
    /**
     * Configurações de particionamento.
     */
    private Partitioning partitioning = new Partitioning();
    
    /**
     * Configurações de arquivamento.
     */
    private Archive archive = new Archive();
    
    /**
     * Configurações de manutenção.
     */
    private Maintenance maintenance = new Maintenance();
    
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
    
    @Data
    public static class Partitioning {
        /**
         * Habilita particionamento automático.
         */
        private boolean enabled = true;
        
        /**
         * Estratégia de particionamento (monthly, weekly, daily).
         */
        private String strategy = "monthly";
        
        /**
         * Número de partições futuras a manter.
         */
        private int futurePartitions = 3;
        
        /**
         * Habilita criação automática de índices.
         */
        private boolean autoIndexes = true;
        
        /**
         * Habilita otimizações de performance.
         */
        private boolean performanceOptimizations = true;
    }
    
    @Data
    public static class Archive {
        /**
         * Habilita arquivamento automático.
         */
        private boolean enabled = true;
        
        /**
         * Idade mínima para arquivamento (em anos).
         */
        private int archiveAfterYears = 2;
        
        /**
         * Remove partição original após arquivamento.
         */
        private boolean deleteAfterArchive = false;
        
        /**
         * Pausa entre arquivamentos (em ms).
         */
        private long archivePauseMs = 1000;
        
        /**
         * Configurações de storage.
         */
        private Storage storage = new Storage();
        
        /**
         * Configurações de compactação.
         */
        private Compaction compaction = new Compaction();
        
        @Data
        public static class Storage {
            /**
             * Tipo de storage (filesystem, s3, minio).
             */
            private String type = "filesystem";
            
            /**
             * Caminho base para storage filesystem.
             */
            private String basePath = "./data/archives";
            
            /**
             * Configurações S3/MinIO.
             */
            private String endpoint;
            private String accessKey;
            private String secretKey;
            private String bucket = "eventstore-archives";
            private String region = "us-east-1";
        }
        
        @Data
        public static class Compaction {
            /**
             * Habilita compactação automática.
             */
            private boolean enabled = true;
            
            /**
             * Idade mínima para compactação (em meses).
             */
            private int compactAfterMonths = 6;
            
            /**
             * Algoritmo de compactação (gzip, lz4).
             */
            private String algorithm = "gzip";
        }
    }
    
    @Data
    public static class Maintenance {
        /**
         * Habilita manutenção automática.
         */
        private boolean enabled = true;
        
        /**
         * Habilita verificação de saúde das partições.
         */
        private boolean healthCheckEnabled = true;
        
        /**
         * Intervalo de verificação de saúde (em horas).
         */
        private int healthCheckIntervalHours = 6;
        
        /**
         * Habilita limpeza automática de logs.
         */
        private boolean logCleanupEnabled = true;
        
        /**
         * Retenção de logs de manutenção (em dias).
         */
        private int logRetentionDays = 90;
        
        /**
         * Habilita relatórios automáticos.
         */
        private boolean reportsEnabled = true;
    }
}