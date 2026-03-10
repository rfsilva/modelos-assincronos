package com.seguradora.hibrida.domain.sinistro.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuração de cache para o domínio de sinistros.
 * 
 * <p>Configura cache Redis com diferentes TTLs para diferentes
 * tipos de consultas de sinistros, otimizando a performance do Query Side.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class SinistroCacheConfiguration {
    
    /**
     * Configura o CacheManager para consultas de sinistros.
     */
    @Bean("sinistroCacheManager")
    public CacheManager sinistroCacheManager(RedisConnectionFactory connectionFactory) {
        
        // Configuração padrão do cache
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // TTL padrão: 5 minutos
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // Configurações específicas por cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Cache para detalhes de sinistro (TTL: 10 minutos)
        cacheConfigurations.put("sinistro-detail", defaultConfig
                .entryTtl(Duration.ofMinutes(10)));
        
        // Cache para listas por CPF (TTL: 5 minutos)
        cacheConfigurations.put("sinistros-por-cpf", defaultConfig
                .entryTtl(Duration.ofMinutes(5)));
        
        // Cache para listas por placa (TTL: 5 minutos)
        cacheConfigurations.put("sinistros-por-placa", defaultConfig
                .entryTtl(Duration.ofMinutes(5)));
        
        // Cache para dashboard (TTL: 2 minutos)
        cacheConfigurations.put("dashboard", defaultConfig
                .entryTtl(Duration.ofMinutes(2)));
        
        // Cache para consultas frequentes (TTL: 1 minuto)
        cacheConfigurations.put("consultas-frequentes", defaultConfig
                .entryTtl(Duration.ofMinutes(1)));
        
        // Cache para estatísticas (TTL: 15 minutos)
        cacheConfigurations.put("estatisticas", defaultConfig
                .entryTtl(Duration.ofMinutes(15)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}