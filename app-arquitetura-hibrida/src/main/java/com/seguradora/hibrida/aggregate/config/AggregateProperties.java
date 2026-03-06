package com.seguradora.hibrida.aggregate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração para o sistema de Aggregates.
 * 
 * <p>Permite customização de comportamentos do sistema de aggregates
 * através do arquivo application.yml ou variáveis de ambiente.
 * 
 * <p><strong>Exemplo de configuração:</strong>
 * <pre>
 * aggregate:
 *   metrics:
 *     enabled: true
 *     detailed-logging: false
 *   health-check:
 *     enabled: true
 *     timeout-seconds: 5
 *   validation:
 *     enabled: true
 *     fail-fast: false
 *     max-violations: 10
 *   performance:
 *     cache-handlers: true
 *     cache-size: 1000
 *     parallel-validation: false
 *     max-validation-threads: 4
 *   snapshot:
 *     auto-create: true
 *     threshold-events: 50
 * </pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "aggregate")
public class AggregateProperties {
    
    /**
     * Configurações de métricas.
     */
    private Metrics metrics = new Metrics();
    
    /**
     * Configurações de health check.
     */
    private HealthCheck healthCheck = new HealthCheck();
    
    /**
     * Configurações de validação.
     */
    private Validation validation = new Validation();
    
    /**
     * Configurações de performance.
     */
    private Performance performance = new Performance();
    
    /**
     * Configurações de snapshot.
     */
    private Snapshot snapshot = new Snapshot();
    
    @Data
    public static class Metrics {
        /**
         * Habilita coleta de métricas.
         */
        private boolean enabled = true;
        
        /**
         * Habilita logging detalhado de métricas.
         */
        private boolean detailedLogging = false;
        
        /**
         * Prefixo para nomes das métricas.
         */
        private String prefix = "aggregate";
    }
    
    @Data
    public static class HealthCheck {
        /**
         * Habilita health checks.
         */
        private boolean enabled = true;
        
        /**
         * Timeout para verificações de saúde em segundos.
         */
        private int timeoutSeconds = 5;
        
        /**
         * Intervalo entre verificações em segundos.
         */
        private int intervalSeconds = 30;
    }
    
    @Data
    public static class Validation {
        /**
         * Habilita validação automática de regras de negócio.
         */
        private boolean enabled = true;
        
        /**
         * Para na primeira violação encontrada.
         */
        private boolean failFast = false;
        
        /**
         * Número máximo de violações a coletar.
         */
        private int maxViolations = 10;
        
        /**
         * Timeout para validação em milissegundos.
         */
        private long timeoutMs = 1000;
    }
    
    @Data
    public static class Performance {
        /**
         * Habilita cache de handlers de eventos.
         */
        private boolean cacheHandlers = true;
        
        /**
         * Tamanho máximo do cache de handlers.
         */
        private int cacheSize = 1000;
        
        /**
         * Habilita validação paralela de regras de negócio.
         */
        private boolean parallelValidation = false;
        
        /**
         * Número máximo de threads para validação paralela.
         */
        private int maxValidationThreads = 4;
        
        /**
         * Habilita otimizações de reflection.
         */
        private boolean optimizeReflection = true;
    }
    
    @Data
    public static class Snapshot {
        /**
         * Habilita criação automática de snapshots.
         */
        private boolean autoCreate = true;
        
        /**
         * Número de eventos para disparar criação de snapshot.
         */
        private int thresholdEvents = 50;
        
        /**
         * Habilita compressão de snapshots.
         */
        private boolean compression = true;
        
        /**
         * Algoritmo de compressão (gzip, lz4).
         */
        private String compressionAlgorithm = "gzip";
    }
}