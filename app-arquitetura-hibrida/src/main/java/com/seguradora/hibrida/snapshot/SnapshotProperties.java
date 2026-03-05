package com.seguradora.hibrida.snapshot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuração do sistema de snapshots.
 * 
 * <p>Permite configuração flexível de:
 * <ul>
 *   <li>Threshold para criação automática de snapshots</li>
 *   <li>Configurações de compressão</li>
 *   <li>Políticas de limpeza</li>
 *   <li>Configurações de performance</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "snapshot")
@Getter
@Setter
public class SnapshotProperties {
    
    /**
     * Número de eventos necessários para criar um novo snapshot.
     * Padrão: 50 eventos.
     */
    private int snapshotThreshold = 50;
    
    /**
     * Número máximo de snapshots mantidos por aggregate.
     * Padrão: 5 snapshots.
     */
    private int maxSnapshotsPerAggregate = 5;
    
    /**
     * Tamanho mínimo (em bytes) para aplicar compressão.
     * Padrão: 1024 bytes (1KB).
     */
    private int compressionThreshold = 1024;
    
    /**
     * Algoritmo de compressão a ser usado.
     * Padrão: GZIP.
     */
    private String compressionAlgorithm = "GZIP";
    
    /**
     * Habilita compressão automática de snapshots.
     * Padrão: true.
     */
    private boolean compressionEnabled = true;
    
    /**
     * Habilita limpeza automática de snapshots antigos.
     * Padrão: true.
     */
    private boolean autoCleanupEnabled = true;
    
    /**
     * Intervalo em horas para limpeza automática global.
     * Padrão: 24 horas.
     */
    private int cleanupIntervalHours = 24;
    
    /**
     * Habilita criação assíncrona de snapshots.
     * Padrão: true.
     */
    private boolean asyncSnapshotCreation = true;
    
    /**
     * Timeout em segundos para operações de snapshot.
     * Padrão: 30 segundos.
     */
    private int operationTimeoutSeconds = 30;
    
    /**
     * Habilita validação de integridade com hash.
     * Padrão: true.
     */
    private boolean integrityValidationEnabled = true;
    
    /**
     * Habilita métricas detalhadas de snapshots.
     * Padrão: true.
     */
    private boolean metricsEnabled = true;
    
    /**
     * Número de dias para manter snapshots antes do arquivamento.
     * Padrão: 365 dias (1 ano).
     */
    private int retentionDays = 365;
    
    /**
     * Tamanho do pool de threads para operações assíncronas.
     * Padrão: 5 threads.
     */
    private int asyncThreadPoolSize = 5;
    
    /**
     * Tamanho da fila para operações assíncronas.
     * Padrão: 100 operações.
     */
    private int asyncQueueCapacity = 100;
    
    /**
     * Prefixo para nomes de threads assíncronas.
     * Padrão: "snapshot-".
     */
    private String asyncThreadNamePrefix = "snapshot-";
    
    /**
     * Habilita monitoramento de health checks.
     * Padrão: true.
     */
    private boolean healthCheckEnabled = true;
    
    /**
     * Intervalo em segundos para health checks.
     * Padrão: 60 segundos.
     */
    private int healthCheckIntervalSeconds = 60;
    
    /**
     * Número máximo de falhas consecutivas antes de marcar como unhealthy.
     * Padrão: 3 falhas.
     */
    private int maxConsecutiveFailures = 3;
    
    /**
     * Habilita cache de snapshots em memória.
     * Padrão: false (para evitar uso excessivo de memória).
     */
    private boolean cacheEnabled = false;
    
    /**
     * Tamanho máximo do cache de snapshots.
     * Padrão: 100 snapshots.
     */
    private int cacheMaxSize = 100;
    
    /**
     * TTL do cache em minutos.
     * Padrão: 30 minutos.
     */
    private int cacheTtlMinutes = 30;
    
    /**
     * Valida as configurações.
     * 
     * @throws IllegalArgumentException se alguma configuração for inválida
     */
    public void validate() {
        if (snapshotThreshold <= 0) {
            throw new IllegalArgumentException("snapshotThreshold must be positive");
        }
        
        if (maxSnapshotsPerAggregate <= 0) {
            throw new IllegalArgumentException("maxSnapshotsPerAggregate must be positive");
        }
        
        if (compressionThreshold < 0) {
            throw new IllegalArgumentException("compressionThreshold must be non-negative");
        }
        
        if (cleanupIntervalHours <= 0) {
            throw new IllegalArgumentException("cleanupIntervalHours must be positive");
        }
        
        if (operationTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("operationTimeoutSeconds must be positive");
        }
        
        if (retentionDays <= 0) {
            throw new IllegalArgumentException("retentionDays must be positive");
        }
        
        if (asyncThreadPoolSize <= 0) {
            throw new IllegalArgumentException("asyncThreadPoolSize must be positive");
        }
        
        if (asyncQueueCapacity <= 0) {
            throw new IllegalArgumentException("asyncQueueCapacity must be positive");
        }
        
        if (healthCheckIntervalSeconds <= 0) {
            throw new IllegalArgumentException("healthCheckIntervalSeconds must be positive");
        }
        
        if (maxConsecutiveFailures <= 0) {
            throw new IllegalArgumentException("maxConsecutiveFailures must be positive");
        }
        
        if (cacheMaxSize <= 0) {
            throw new IllegalArgumentException("cacheMaxSize must be positive");
        }
        
        if (cacheTtlMinutes <= 0) {
            throw new IllegalArgumentException("cacheTtlMinutes must be positive");
        }
    }
    
    /**
     * Verifica se a compressão deve ser aplicada para um tamanho específico.
     * 
     * @param dataSize Tamanho dos dados em bytes
     * @return true se deve comprimir, false caso contrário
     */
    public boolean shouldCompress(int dataSize) {
        return compressionEnabled && dataSize >= compressionThreshold;
    }
    
    /**
     * Verifica se a limpeza automática está habilitada.
     * 
     * @return true se habilitada, false caso contrário
     */
    public boolean isAutoCleanupEnabled() {
        return autoCleanupEnabled;
    }
    
    /**
     * Verifica se operações assíncronas estão habilitadas.
     * 
     * @return true se habilitadas, false caso contrário
     */
    public boolean isAsyncEnabled() {
        return asyncSnapshotCreation;
    }
}