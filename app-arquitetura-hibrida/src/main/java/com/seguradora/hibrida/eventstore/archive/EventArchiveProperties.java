package com.seguradora.hibrida.eventstore.archive;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração do sistema de arquivamento.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "eventstore.archive")
public class EventArchiveProperties {
    
    /**
     * Habilita o sistema de arquivamento.
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
    
    /**
     * Configurações de monitoramento.
     */
    private Monitoring monitoring = new Monitoring();
    
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
    
    @Data
    public static class Monitoring {
        /**
         * Habilita métricas de arquivamento.
         */
        private boolean metricsEnabled = true;
        
        /**
         * Habilita alertas de falhas.
         */
        private boolean alertsEnabled = true;
        
        /**
         * Threshold de falhas para alerta.
         */
        private double failureThreshold = 0.1; // 10%
    }
}