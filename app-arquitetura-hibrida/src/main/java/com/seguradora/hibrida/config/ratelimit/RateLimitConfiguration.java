package com.seguradora.hibrida.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuração de Rate Limiting usando Bucket4j.
 *
 * <p>Implementa controle de taxa para proteger APIs de sobrecarga
 * e abuso. Usa algoritmo Token Bucket com limites configuráveis.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Configuration
@Slf4j
public class RateLimitConfiguration {

    /**
     * Cache de buckets por IP/usuário.
     */
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Cria bucket com limite padrão: 100 requisições por minuto.
     */
    @Bean
    public Bucket defaultBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Obtém bucket para um identificador (IP, usuário, etc).
     */
    public Bucket resolveBucket(String key) {
        return bucketCache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * Cria novo bucket com limites configurados.
     */
    private Bucket createNewBucket() {
        // Limite padrão: 100 requisições por minuto
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Cria bucket para endpoints de consulta (mais permissivo).
     * Limite: 300 requisições por minuto.
     */
    public Bucket createQueryBucket() {
        Bandwidth limit = Bandwidth.classic(300, Refill.intervally(300, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Cria bucket para endpoints de comando (mais restritivo).
     * Limite: 50 requisições por minuto.
     */
    public Bucket createCommandBucket() {
        Bandwidth limit = Bandwidth.classic(50, Refill.intervally(50, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
