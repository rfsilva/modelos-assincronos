package com.seguradora.hibrida.eventbus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuração do Event Bus.
 * 
 * <p>Permite configuração externa via application.yml das principais
 * configurações do Event Bus, incluindo pool de threads, retry,
 * timeouts e monitoramento.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "event-bus")
public class EventBusProperties {
    
    /**
     * Configurações do pool de threads.
     */
    private ThreadPool threadPool = new ThreadPool();
    
    /**
     * Configurações de retry.
     */
    private Retry retry = new Retry();
    
    /**
     * Configurações de timeout.
     */
    private Timeout timeout = new Timeout();
    
    /**
     * Configurações de monitoramento.
     */
    private Monitoring monitoring = new Monitoring();
    
    /**
     * Configurações de Kafka (quando habilitado).
     */
    private Kafka kafka = new Kafka();
    
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
    
    public Timeout getTimeout() {
        return timeout;
    }
    
    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }
    
    public Monitoring getMonitoring() {
        return monitoring;
    }
    
    public void setMonitoring(Monitoring monitoring) {
        this.monitoring = monitoring;
    }
    
    public Kafka getKafka() {
        return kafka;
    }
    
    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }
    
    /**
     * Configurações do pool de threads.
     */
    public static class ThreadPool {
        
        /**
         * Número de threads core do pool.
         * Padrão: número de processadores disponíveis.
         */
        private int coreSize = Runtime.getRuntime().availableProcessors();
        
        /**
         * Número máximo de threads do pool.
         * Padrão: 2x o número de processadores.
         */
        private int maxSize = Runtime.getRuntime().availableProcessors() * 2;
        
        /**
         * Tempo de vida de threads idle em segundos.
         * Padrão: 60 segundos.
         */
        private int keepAliveSeconds = 60;
        
        /**
         * Tamanho da fila de trabalho.
         * Padrão: 1000.
         */
        private int queueCapacity = 1000;
        
        /**
         * Prefixo do nome das threads.
         * Padrão: "EventBus-Worker".
         */
        private String threadNamePrefix = "EventBus-Worker";
        
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
        
        public int getKeepAliveSeconds() {
            return keepAliveSeconds;
        }
        
        public void setKeepAliveSeconds(int keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
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
    }
    
    /**
     * Configurações de retry.
     */
    public static class Retry {
        
        /**
         * Se retry está habilitado.
         * Padrão: true.
         */
        private boolean enabled = true;
        
        /**
         * Número máximo de tentativas.
         * Padrão: 3.
         */
        private int maxAttempts = 3;
        
        /**
         * Delay inicial em milissegundos.
         * Padrão: 1000ms (1 segundo).
         */
        private long initialDelayMs = 1000;
        
        /**
         * Multiplicador para backoff exponencial.
         * Padrão: 2.0.
         */
        private double backoffMultiplier = 2.0;
        
        /**
         * Delay máximo em milissegundos.
         * Padrão: 30000ms (30 segundos).
         */
        private long maxDelayMs = 30000;
        
        /**
         * Percentual de jitter para evitar thundering herd.
         * Padrão: 0.1 (10%).
         */
        private double jitterPercent = 0.1;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getMaxAttempts() {
            return maxAttempts;
        }
        
        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
        
        public long getInitialDelayMs() {
            return initialDelayMs;
        }
        
        public void setInitialDelayMs(long initialDelayMs) {
            this.initialDelayMs = initialDelayMs;
        }
        
        public double getBackoffMultiplier() {
            return backoffMultiplier;
        }
        
        public void setBackoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
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
     * Configurações de timeout.
     */
    public static class Timeout {
        
        /**
         * Timeout padrão para handlers em segundos.
         * Padrão: 30 segundos.
         */
        private int defaultHandlerTimeoutSeconds = 30;
        
        /**
         * Timeout para shutdown em segundos.
         * Padrão: 60 segundos.
         */
        private int shutdownTimeoutSeconds = 60;
        
        public int getDefaultHandlerTimeoutSeconds() {
            return defaultHandlerTimeoutSeconds;
        }
        
        public void setDefaultHandlerTimeoutSeconds(int defaultHandlerTimeoutSeconds) {
            this.defaultHandlerTimeoutSeconds = defaultHandlerTimeoutSeconds;
        }
        
        public int getShutdownTimeoutSeconds() {
            return shutdownTimeoutSeconds;
        }
        
        public void setShutdownTimeoutSeconds(int shutdownTimeoutSeconds) {
            this.shutdownTimeoutSeconds = shutdownTimeoutSeconds;
        }
    }
    
    /**
     * Configurações de monitoramento.
     */
    public static class Monitoring {
        
        /**
         * Se métricas estão habilitadas.
         * Padrão: true.
         */
        private boolean metricsEnabled = true;
        
        /**
         * Se health checks estão habilitados.
         * Padrão: true.
         */
        private boolean healthCheckEnabled = true;
        
        /**
         * Se logs detalhados estão habilitados.
         * Padrão: false.
         */
        private boolean detailedLogging = false;
        
        /**
         * Threshold de taxa de erro para considerar unhealthy.
         * Padrão: 0.1 (10%).
         */
        private double errorRateThreshold = 0.1;
        
        public boolean isMetricsEnabled() {
            return metricsEnabled;
        }
        
        public void setMetricsEnabled(boolean metricsEnabled) {
            this.metricsEnabled = metricsEnabled;
        }
        
        public boolean isHealthCheckEnabled() {
            return healthCheckEnabled;
        }
        
        public void setHealthCheckEnabled(boolean healthCheckEnabled) {
            this.healthCheckEnabled = healthCheckEnabled;
        }
        
        public boolean isDetailedLogging() {
            return detailedLogging;
        }
        
        public void setDetailedLogging(boolean detailedLogging) {
            this.detailedLogging = detailedLogging;
        }
        
        public double getErrorRateThreshold() {
            return errorRateThreshold;
        }
        
        public void setErrorRateThreshold(double errorRateThreshold) {
            this.errorRateThreshold = errorRateThreshold;
        }
    }
    
    /**
     * Configurações de Kafka.
     */
    public static class Kafka {
        
        /**
         * Se integração com Kafka está habilitada.
         * Padrão: false.
         */
        private boolean enabled = false;
        
        /**
         * Servidores bootstrap do Kafka.
         */
        private String bootstrapServers = "localhost:9092";
        
        /**
         * Tópico padrão para eventos.
         */
        private String defaultTopic = "domain-events";
        
        /**
         * Número de partições por tópico.
         * Padrão: 3.
         */
        private int partitions = 3;
        
        /**
         * Fator de replicação.
         * Padrão: 1.
         */
        private short replicationFactor = 1;
        
        /**
         * Configurações do producer.
         */
        private Producer producer = new Producer();
        
        /**
         * Configurações do consumer.
         */
        private Consumer consumer = new Consumer();
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getBootstrapServers() {
            return bootstrapServers;
        }
        
        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }
        
        public String getDefaultTopic() {
            return defaultTopic;
        }
        
        public void setDefaultTopic(String defaultTopic) {
            this.defaultTopic = defaultTopic;
        }
        
        public int getPartitions() {
            return partitions;
        }
        
        public void setPartitions(int partitions) {
            this.partitions = partitions;
        }
        
        public short getReplicationFactor() {
            return replicationFactor;
        }
        
        public void setReplicationFactor(short replicationFactor) {
            this.replicationFactor = replicationFactor;
        }
        
        public Producer getProducer() {
            return producer;
        }
        
        public void setProducer(Producer producer) {
            this.producer = producer;
        }
        
        public Consumer getConsumer() {
            return consumer;
        }
        
        public void setConsumer(Consumer consumer) {
            this.consumer = consumer;
        }
        
        /**
         * Configurações do producer Kafka.
         */
        public static class Producer {
            
            private String acks = "all";
            private int retries = 3;
            private int batchSize = 16384;
            private int lingerMs = 5;
            private long bufferMemory = 33554432;
            
            public String getAcks() {
                return acks;
            }
            
            public void setAcks(String acks) {
                this.acks = acks;
            }
            
            public int getRetries() {
                return retries;
            }
            
            public void setRetries(int retries) {
                this.retries = retries;
            }
            
            public int getBatchSize() {
                return batchSize;
            }
            
            public void setBatchSize(int batchSize) {
                this.batchSize = batchSize;
            }
            
            public int getLingerMs() {
                return lingerMs;
            }
            
            public void setLingerMs(int lingerMs) {
                this.lingerMs = lingerMs;
            }
            
            public long getBufferMemory() {
                return bufferMemory;
            }
            
            public void setBufferMemory(long bufferMemory) {
                this.bufferMemory = bufferMemory;
            }
        }
        
        /**
         * Configurações do consumer Kafka.
         */
        public static class Consumer {
            
            private String groupId = "event-bus-consumers";
            private String autoOffsetReset = "earliest";
            private boolean enableAutoCommit = false;
            private int maxPollRecords = 500;
            private int sessionTimeoutMs = 30000;
            
            public String getGroupId() {
                return groupId;
            }
            
            public void setGroupId(String groupId) {
                this.groupId = groupId;
            }
            
            public String getAutoOffsetReset() {
                return autoOffsetReset;
            }
            
            public void setAutoOffsetReset(String autoOffsetReset) {
                this.autoOffsetReset = autoOffsetReset;
            }
            
            public boolean isEnableAutoCommit() {
                return enableAutoCommit;
            }
            
            public void setEnableAutoCommit(boolean enableAutoCommit) {
                this.enableAutoCommit = enableAutoCommit;
            }
            
            public int getMaxPollRecords() {
                return maxPollRecords;
            }
            
            public void setMaxPollRecords(int maxPollRecords) {
                this.maxPollRecords = maxPollRecords;
            }
            
            public int getSessionTimeoutMs() {
                return sessionTimeoutMs;
            }
            
            public void setSessionTimeoutMs(int sessionTimeoutMs) {
                this.sessionTimeoutMs = sessionTimeoutMs;
            }
        }
    }
}