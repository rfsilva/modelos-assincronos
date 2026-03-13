package com.seguradora.hibrida.config.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link RateLimitConfiguration}.
 *
 * <p>Valida a criação e configuração de buckets para rate limiting,
 * incluindo limites padrão, para queries e para commands.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitConfiguration - Testes Unitários")
class RateLimitConfigurationTest {

    private RateLimitConfiguration config;

    @BeforeEach
    void setUp() {
        config = new RateLimitConfiguration();
    }

    @Test
    @DisplayName("Deve criar bucket padrão corretamente")
    void shouldCreateDefaultBucketCorrectly() {
        // When
        Bucket bucket = config.defaultBucket();

        // Then
        assertThat(bucket).isNotNull();
        assertThat(bucket).isInstanceOf(Bucket.class);
    }

    @Test
    @DisplayName("Deve criar bucket padrão com limite de 100 tokens")
    void shouldCreateDefaultBucketWithLimit100() {
        // When
        Bucket bucket = config.defaultBucket();

        // Then
        assertThat(bucket).isNotNull();

        // Consumir 100 tokens (deve ser permitido)
        for (int i = 0; i < 100; i++) {
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            assertThat(probe.isConsumed()).isTrue();
        }

        // Tentar consumir mais um token (deve falhar)
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        assertThat(probe.isConsumed()).isFalse();
    }

    @Test
    @DisplayName("Deve criar bucket de query corretamente")
    void shouldCreateQueryBucketCorrectly() {
        // When
        Bucket bucket = config.createQueryBucket();

        // Then
        assertThat(bucket).isNotNull();
        assertThat(bucket).isInstanceOf(Bucket.class);
    }

    @Test
    @DisplayName("Deve criar bucket de query com limite de 300 tokens")
    void shouldCreateQueryBucketWithLimit300() {
        // When
        Bucket bucket = config.createQueryBucket();

        // Then
        assertThat(bucket).isNotNull();

        // Consumir 300 tokens (deve ser permitido)
        for (int i = 0; i < 300; i++) {
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            assertThat(probe.isConsumed()).isTrue();
        }

        // Tentar consumir mais um token (deve falhar)
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        assertThat(probe.isConsumed()).isFalse();
    }

    @Test
    @DisplayName("Deve criar bucket de command corretamente")
    void shouldCreateCommandBucketCorrectly() {
        // When
        Bucket bucket = config.createCommandBucket();

        // Then
        assertThat(bucket).isNotNull();
        assertThat(bucket).isInstanceOf(Bucket.class);
    }

    @Test
    @DisplayName("Deve criar bucket de command com limite de 50 tokens")
    void shouldCreateCommandBucketWithLimit50() {
        // When
        Bucket bucket = config.createCommandBucket();

        // Then
        assertThat(bucket).isNotNull();

        // Consumir 50 tokens (deve ser permitido)
        for (int i = 0; i < 50; i++) {
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            assertThat(probe.isConsumed()).isTrue();
        }

        // Tentar consumir mais um token (deve falhar)
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        assertThat(probe.isConsumed()).isFalse();
    }

    @Test
    @DisplayName("Deve resolver bucket para chave específica")
    void shouldResolveBucketForSpecificKey() {
        // Given
        String key = "user-123";

        // When
        Bucket bucket = config.resolveBucket(key);

        // Then
        assertThat(bucket).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar o mesmo bucket para a mesma chave")
    void shouldReturnSameBucketForSameKey() {
        // Given
        String key = "user-123";

        // When
        Bucket bucket1 = config.resolveBucket(key);
        Bucket bucket2 = config.resolveBucket(key);

        // Then
        assertThat(bucket1).isSameAs(bucket2);
    }

    @Test
    @DisplayName("Deve retornar buckets diferentes para chaves diferentes")
    void shouldReturnDifferentBucketsForDifferentKeys() {
        // Given
        String key1 = "user-123";
        String key2 = "user-456";

        // When
        Bucket bucket1 = config.resolveBucket(key1);
        Bucket bucket2 = config.resolveBucket(key2);

        // Then
        assertThat(bucket1).isNotSameAs(bucket2);
    }

    @Test
    @DisplayName("Deve criar bucket com cache correto para múltiplas chaves")
    void shouldCreateBucketWithCorrectCacheForMultipleKeys() {
        // Given
        String key1 = "user-123";
        String key2 = "user-456";
        String key3 = "user-789";

        // When
        Bucket bucket1 = config.resolveBucket(key1);
        Bucket bucket2 = config.resolveBucket(key2);
        Bucket bucket3 = config.resolveBucket(key3);

        // Then
        assertThat(bucket1).isNotNull();
        assertThat(bucket2).isNotNull();
        assertThat(bucket3).isNotNull();
        assertThat(bucket1).isNotSameAs(bucket2);
        assertThat(bucket1).isNotSameAs(bucket3);
        assertThat(bucket2).isNotSameAs(bucket3);
    }

    @Test
    @DisplayName("Deve validar hierarquia de limites - Command < Default < Query")
    void shouldValidateLimitHierarchyCommandLessThanDefaultLessThanQuery() {
        // When
        Bucket commandBucket = config.createCommandBucket();
        Bucket defaultBucket = config.defaultBucket();
        Bucket queryBucket = config.createQueryBucket();

        // Then
        // Command: 50 tokens
        int commandLimit = 0;
        while (commandBucket.tryConsumeAndReturnRemaining(1).isConsumed()) {
            commandLimit++;
        }

        // Default: 100 tokens
        int defaultLimit = 0;
        while (defaultBucket.tryConsumeAndReturnRemaining(1).isConsumed()) {
            defaultLimit++;
        }

        // Query: 300 tokens
        int queryLimit = 0;
        while (queryBucket.tryConsumeAndReturnRemaining(1).isConsumed()) {
            queryLimit++;
        }

        // Validar hierarquia
        assertThat(commandLimit).isEqualTo(50);
        assertThat(defaultLimit).isEqualTo(100);
        assertThat(queryLimit).isEqualTo(300);
        assertThat(commandLimit).isLessThan(defaultLimit);
        assertThat(defaultLimit).isLessThan(queryLimit);
    }
}
