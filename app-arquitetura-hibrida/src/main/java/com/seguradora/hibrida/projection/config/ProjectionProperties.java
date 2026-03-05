package com.seguradora.hibrida.projection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;

/**
 * Propriedades de configuração para o sistema de projeções.
 * 
 * <p>Permite configurar:
 * <ul>
 *   <li>Pool de threads para processamento assíncrono</li>
 *   <li>Configurações de timeout e retry</li>
 *   <li>Configurações de batch processing</li>
 *   <li>Configurações de monitoramento</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "cqrs.projection")
@Validated
public class ProjectionProperties {
    
    /**
     * Tamanho do batch para processamento de eventos.
     */
    @Min(value = 1, message = "Batch size deve ser pelo menos 1")
    private int batchSize = 50;
    
    /**
     * Se deve processar eventos em paralelo.
     */
    private boolean parallel = true;
    
    /**
     * Timeout padrão em segundos para processamento de handlers.
     */
    @Min(value = 1, message = "Timeout deve ser pelo menos 1 segundo")
    private int timeoutSeconds = 30;
    
    /**
     * Configurações do pool de threads.
     */
    @Valid
    private ThreadPool threadPool = new ThreadPool();
    
    /**
     * Configurações de retry.
     */
    @Valid
    private Retry retry = new Retry();
    
    /**
     * Configurações de monitoramento.
     */
    @Valid
    private Monitoring monitoring = new Monitoring();
    
    // Getters e Setters
    public int getBatchSize() {
        return batchSize;
    }
    
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    public boolean isParallel() {
        return parallel;
    }
    
    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }
    
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
    
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
    
    public ThreadPool getThreadPool() {
        return threadPool;
    }
    
    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }
    
    public Retry getRetry() {
        return retry;
    }
    
    public void setRetry(Retry retry) {
        this.retry = retry;
    }
    
    public Monitoring getMonitoring() {
        return monitoring;
    }
    
    public void setMonitoring(Monitoring monitoring) {
        this.monitoring = monitoring;
    }
    
    /**
     * Configurações do pool de threads para processamento assíncrono.
     */
    public static class ThreadPool {
        
        /**
         * Número de threads core do pool.
         */
        @Min(value = 1, message = "Core size deve ser pelo menos 1")
        private int coreSize = 5;
        
        /**
         * Número máximo de threads do pool.
         */
        @Min(value = 1, message = "Max size deve ser pelo menos 1")
        private int maxSize = 20;
        
        /**
         * Capacidade da fila de tarefas.
         */
        @Min(value = 1, message = "Queue capacity deve ser pelo menos 1")
        private int queueCapacity = 1000;
        
        /**
         * Prefixo do nome das threads.
         */
        @NotBlank(message = "Thread name prefix é obrigatório")
        private String threadNamePrefix = "projection-";
        
        /**
         * Tempo em segundos para manter threads idle vivas.
         */
        @Min(value = 1, message = "Keep alive deve ser pelo menos 1 segundo")
        private int keepAliveSeconds = 60;
        
        // Getters e Setters
        public int getCoreSize() {
            return coreSize;
        }
        
        public void setCoreSize(int coreSize) {
            this.coreSize = coreSize;
        }
        
        public int getMaxSize() {
            return maxSize;
        }
        
        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }
        
        public int getQueueCapacity() {
            return queueCapacity;
        }
        
        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
        
        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }
        
        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
        
        public int getKeepAliveSeconds() {
            return keepAliveSeconds;
        }
        
        public void setKeepAliveSeconds(int keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
        }
    }
    
    /**
     * Configurações de retry para falhas de processamento.
     */
    public static class Retry {
        
        /**
         * Número máximo de tentativas.
         */
        @Min(value = 1, message = "Max attempts deve ser pelo menos 1")
        private int maxAttempts = 3;
        
        /**
         * Multiplicador para backoff exponencial.
         */
        @Min(value = 1, message = "Backoff multiplier deve ser pelo menos 1")
        private double backoffMultiplier = 2.0;
        
        /**
         * Delay inicial em milissegundos.
         */
        @Min(value = 100, message = "Initial delay deve ser pelo menos 100ms")
        private long initialDelayMs = 1000;
        
        /**
         * Delay máximo em milissegundos.
         */
        @Min(value = 1000, message = "Max delay deve ser pelo menos 1000ms")
        private long maxDelayMs = 30000;
        
        /**
         * Percentual de jitter para randomizar delays.
         */
        @Min(value = 0, message = "Jitter não pode ser negativo")
        private double jitterPercent = 0.1;
        
        // Getters e Setters
        public int getMaxAttempts() {
            return maxAttempts;
        }
        
        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
        
        public double getBackoffMultiplier() {
            return backoffMultiplier;
        }
        
        public void setBackoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
        }
        
        public long getInitialDelayMs() {
            return initialDelayMs;
        }
        
        public void setInitialDelayMs(long initialDelayMs) {
            this.initialDelayMs = initialDelayMs;
        }
        
        public long getMaxDelayMs() {
            return maxDelayMs;
        }
        
        public void setMaxDelayMs(long maxDelayMs) {
            this.maxDelayMs = maxDelayMs;
        }
        
        public double getJitterPercent() {
            return jitterPercent;
        }
        
        public void setJitterPercent(double jitterPercent) {
            this.jitterPercent = jitterPercent;
        }
    }
    
    /**
     * Configurações de monitoramento e métricas.
     */
    public static class Monitoring {
        
        /**
         * Se o monitoramento está habilitado.
         */
        private boolean enabled = true;
        
        /**
         * Intervalo em segundos para coleta de métricas.
         */
        @Min(value = 1, message = "Metrics interval deve ser pelo menos 1 segundo")
        private int metricsIntervalSeconds = 60;
        
        /**
         * Threshold de lag em eventos para alertas.
         */
        @Min(value = 1, message = "Lag threshold deve ser pelo menos 1")
        private long lagThreshold = 1000;
        
        /**
         * Threshold de taxa de erro para alertas.
         */
        @Min(value = 0, message = "Error rate threshold não pode ser negativo")
        private double errorRateThreshold = 0.05; // 5%
        
        /**
         * Se deve fazer log detalhado de processamento.
         */
        private boolean detailedLogging = false;
        
        // Getters e Setters
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getMetricsIntervalSeconds() {
            return metricsIntervalSeconds;
        }
        
        public void setMetricsIntervalSeconds(int metricsIntervalSeconds) {
            this.metricsIntervalSeconds = metricsIntervalSeconds;
        }
        
        public long getLagThreshold() {
            return lagThreshold;
        }
        
        public void setLagThreshold(long lagThreshold) {
            this.lagThreshold = lagThreshold;
        }
        
        public double getErrorRateThreshold() {
            return errorRateThreshold;
        }
        
        public void setErrorRateThreshold(double errorRateThreshold) {
            this.errorRateThreshold = errorRateThreshold;
        }
        
        public boolean isDetailedLogging() {
            return detailedLogging;
        }
        
        public void setDetailedLogging(boolean detailedLogging) {
            this.detailedLogging = detailedLogging;
        }
    }
}