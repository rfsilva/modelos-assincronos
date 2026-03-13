package com.seguradora.hibrida.eventbus.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link EventBusProperties}.
 */
@DisplayName("EventBusProperties Tests")
class EventBusPropertiesTest {

    private EventBusProperties properties;

    @BeforeEach
    void setUp() {
        properties = new EventBusProperties();
    }

    @Test
    @DisplayName("Deve criar com valores padrão")
    void shouldCreateWithDefaultValues() {
        // Then
        assertThat(properties).isNotNull();
        assertThat(properties.getThreadPool()).isNotNull();
        assertThat(properties.getRetry()).isNotNull();
        assertThat(properties.getTimeout()).isNotNull();
        assertThat(properties.getMonitoring()).isNotNull();
        assertThat(properties.getKafka()).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar thread pool")
    void shouldConfigureThreadPool() {
        // Given
        EventBusProperties.ThreadPool threadPool = new EventBusProperties.ThreadPool();
        threadPool.setCoreSize(8);
        threadPool.setMaxSize(16);
        threadPool.setKeepAliveSeconds(120);
        threadPool.setQueueCapacity(2000);
        threadPool.setThreadNamePrefix("Custom-Worker");

        // When
        properties.setThreadPool(threadPool);

        // Then
        assertThat(properties.getThreadPool().getCoreSize()).isEqualTo(8);
        assertThat(properties.getThreadPool().getMaxSize()).isEqualTo(16);
        assertThat(properties.getThreadPool().getKeepAliveSeconds()).isEqualTo(120);
        assertThat(properties.getThreadPool().getQueueCapacity()).isEqualTo(2000);
        assertThat(properties.getThreadPool().getThreadNamePrefix()).isEqualTo("Custom-Worker");
    }

    @Test
    @DisplayName("Deve ter valores padrão de thread pool corretos")
    void shouldHaveCorrectThreadPoolDefaults() {
        // Given
        EventBusProperties.ThreadPool threadPool = properties.getThreadPool();
        int processors = Runtime.getRuntime().availableProcessors();

        // Then
        assertThat(threadPool.getCoreSize()).isEqualTo(processors);
        assertThat(threadPool.getMaxSize()).isEqualTo(processors * 2);
        assertThat(threadPool.getKeepAliveSeconds()).isEqualTo(60);
        assertThat(threadPool.getQueueCapacity()).isEqualTo(1000);
        assertThat(threadPool.getThreadNamePrefix()).isEqualTo("EventBus-Worker");
    }

    @Test
    @DisplayName("Deve configurar retry")
    void shouldConfigureRetry() {
        // Given
        EventBusProperties.Retry retry = new EventBusProperties.Retry();
        retry.setEnabled(false);
        retry.setMaxAttempts(5);
        retry.setInitialDelayMs(2000);
        retry.setBackoffMultiplier(3.0);
        retry.setMaxDelayMs(60000);
        retry.setJitterPercent(0.2);

        // When
        properties.setRetry(retry);

        // Then
        assertThat(properties.getRetry().isEnabled()).isFalse();
        assertThat(properties.getRetry().getMaxAttempts()).isEqualTo(5);
        assertThat(properties.getRetry().getInitialDelayMs()).isEqualTo(2000);
        assertThat(properties.getRetry().getBackoffMultiplier()).isEqualTo(3.0);
        assertThat(properties.getRetry().getMaxDelayMs()).isEqualTo(60000);
        assertThat(properties.getRetry().getJitterPercent()).isEqualTo(0.2);
    }

    @Test
    @DisplayName("Deve ter valores padrão de retry corretos")
    void shouldHaveCorrectRetryDefaults() {
        // Given
        EventBusProperties.Retry retry = properties.getRetry();

        // Then
        assertThat(retry.isEnabled()).isTrue();
        assertThat(retry.getMaxAttempts()).isEqualTo(3);
        assertThat(retry.getInitialDelayMs()).isEqualTo(1000);
        assertThat(retry.getBackoffMultiplier()).isEqualTo(2.0);
        assertThat(retry.getMaxDelayMs()).isEqualTo(30000);
        assertThat(retry.getJitterPercent()).isEqualTo(0.1);
    }

    @Test
    @DisplayName("Deve configurar timeout")
    void shouldConfigureTimeout() {
        // Given
        EventBusProperties.Timeout timeout = new EventBusProperties.Timeout();
        timeout.setDefaultHandlerTimeoutSeconds(60);
        timeout.setShutdownTimeoutSeconds(120);

        // When
        properties.setTimeout(timeout);

        // Then
        assertThat(properties.getTimeout().getDefaultHandlerTimeoutSeconds()).isEqualTo(60);
        assertThat(properties.getTimeout().getShutdownTimeoutSeconds()).isEqualTo(120);
    }

    @Test
    @DisplayName("Deve ter valores padrão de timeout corretos")
    void shouldHaveCorrectTimeoutDefaults() {
        // Given
        EventBusProperties.Timeout timeout = properties.getTimeout();

        // Then
        assertThat(timeout.getDefaultHandlerTimeoutSeconds()).isEqualTo(30);
        assertThat(timeout.getShutdownTimeoutSeconds()).isEqualTo(60);
    }

    @Test
    @DisplayName("Deve configurar monitoring")
    void shouldConfigureMonitoring() {
        // Given
        EventBusProperties.Monitoring monitoring = new EventBusProperties.Monitoring();
        monitoring.setMetricsEnabled(false);
        monitoring.setHealthCheckEnabled(false);
        monitoring.setDetailedLogging(true);
        monitoring.setErrorRateThreshold(0.2);

        // When
        properties.setMonitoring(monitoring);

        // Then
        assertThat(properties.getMonitoring().isMetricsEnabled()).isFalse();
        assertThat(properties.getMonitoring().isHealthCheckEnabled()).isFalse();
        assertThat(properties.getMonitoring().isDetailedLogging()).isTrue();
        assertThat(properties.getMonitoring().getErrorRateThreshold()).isEqualTo(0.2);
    }

    @Test
    @DisplayName("Deve ter valores padrão de monitoring corretos")
    void shouldHaveCorrectMonitoringDefaults() {
        // Given
        EventBusProperties.Monitoring monitoring = properties.getMonitoring();

        // Then
        assertThat(monitoring.isMetricsEnabled()).isTrue();
        assertThat(monitoring.isHealthCheckEnabled()).isTrue();
        assertThat(monitoring.isDetailedLogging()).isFalse();
        assertThat(monitoring.getErrorRateThreshold()).isEqualTo(0.1);
    }

    @Test
    @DisplayName("Deve configurar Kafka")
    void shouldConfigureKafka() {
        // Given
        EventBusProperties.Kafka kafka = new EventBusProperties.Kafka();
        kafka.setEnabled(true);
        kafka.setBootstrapServers("kafka:9092");
        kafka.setDefaultTopic("custom-events");
        kafka.setPartitions(6);
        kafka.setReplicationFactor((short) 3);

        // When
        properties.setKafka(kafka);

        // Then
        assertThat(properties.getKafka().isEnabled()).isTrue();
        assertThat(properties.getKafka().getBootstrapServers()).isEqualTo("kafka:9092");
        assertThat(properties.getKafka().getDefaultTopic()).isEqualTo("custom-events");
        assertThat(properties.getKafka().getPartitions()).isEqualTo(6);
        assertThat(properties.getKafka().getReplicationFactor()).isEqualTo((short) 3);
    }

    @Test
    @DisplayName("Deve ter valores padrão de Kafka corretos")
    void shouldHaveCorrectKafkaDefaults() {
        // Given
        EventBusProperties.Kafka kafka = properties.getKafka();

        // Then
        assertThat(kafka.isEnabled()).isFalse();
        assertThat(kafka.getBootstrapServers()).isEqualTo("localhost:9092");
        assertThat(kafka.getDefaultTopic()).isEqualTo("domain-events");
        assertThat(kafka.getPartitions()).isEqualTo(3);
        assertThat(kafka.getReplicationFactor()).isEqualTo((short) 1);
        assertThat(kafka.getProducer()).isNotNull();
        assertThat(kafka.getConsumer()).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar Kafka Producer")
    void shouldConfigureKafkaProducer() {
        // Given
        EventBusProperties.Kafka.Producer producer = new EventBusProperties.Kafka.Producer();
        producer.setAcks("1");
        producer.setRetries(5);
        producer.setBatchSize(32768);
        producer.setLingerMs(10);
        producer.setBufferMemory(67108864);

        // When
        properties.getKafka().setProducer(producer);

        // Then
        EventBusProperties.Kafka.Producer actual = properties.getKafka().getProducer();
        assertThat(actual.getAcks()).isEqualTo("1");
        assertThat(actual.getRetries()).isEqualTo(5);
        assertThat(actual.getBatchSize()).isEqualTo(32768);
        assertThat(actual.getLingerMs()).isEqualTo(10);
        assertThat(actual.getBufferMemory()).isEqualTo(67108864);
    }

    @Test
    @DisplayName("Deve ter valores padrão de Kafka Producer corretos")
    void shouldHaveCorrectKafkaProducerDefaults() {
        // Given
        EventBusProperties.Kafka.Producer producer = properties.getKafka().getProducer();

        // Then
        assertThat(producer.getAcks()).isEqualTo("all");
        assertThat(producer.getRetries()).isEqualTo(3);
        assertThat(producer.getBatchSize()).isEqualTo(16384);
        assertThat(producer.getLingerMs()).isEqualTo(5);
        assertThat(producer.getBufferMemory()).isEqualTo(33554432);
    }

    @Test
    @DisplayName("Deve configurar Kafka Consumer")
    void shouldConfigureKafkaConsumer() {
        // Given
        EventBusProperties.Kafka.Consumer consumer = new EventBusProperties.Kafka.Consumer();
        consumer.setGroupId("custom-group");
        consumer.setAutoOffsetReset("latest");
        consumer.setEnableAutoCommit(true);
        consumer.setMaxPollRecords(1000);
        consumer.setSessionTimeoutMs(60000);

        // When
        properties.getKafka().setConsumer(consumer);

        // Then
        EventBusProperties.Kafka.Consumer actual = properties.getKafka().getConsumer();
        assertThat(actual.getGroupId()).isEqualTo("custom-group");
        assertThat(actual.getAutoOffsetReset()).isEqualTo("latest");
        assertThat(actual.isEnableAutoCommit()).isTrue();
        assertThat(actual.getMaxPollRecords()).isEqualTo(1000);
        assertThat(actual.getSessionTimeoutMs()).isEqualTo(60000);
    }

    @Test
    @DisplayName("Deve ter valores padrão de Kafka Consumer corretos")
    void shouldHaveCorrectKafkaConsumerDefaults() {
        // Given
        EventBusProperties.Kafka.Consumer consumer = properties.getKafka().getConsumer();

        // Then
        assertThat(consumer.getGroupId()).isEqualTo("event-bus-consumers");
        assertThat(consumer.getAutoOffsetReset()).isEqualTo("earliest");
        assertThat(consumer.isEnableAutoCommit()).isFalse();
        assertThat(consumer.getMaxPollRecords()).isEqualTo(500);
        assertThat(consumer.getSessionTimeoutMs()).isEqualTo(30000);
    }
}
