package com.seguradora.hibrida.domain.segurado.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link SeguradoCacheConfig}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoCacheConfig - Testes Unitários")
class SeguradoCacheConfigTest {

    private final SeguradoCacheConfig config = new SeguradoCacheConfig();

    @Test
    @DisplayName("Deve criar cache manager com todos os caches configurados")
    void shouldCreateCacheManagerWithAllCaches() {
        // When
        CacheManager cacheManager = config.seguradoCacheManager();

        // Then
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).isNotEmpty();

        // Verificar caches principais
        assertThat(cacheManager.getCacheNames()).contains(
            "cpf-validation",
            "email-validation",
            "telefone-validation",
            "bureau-validation",
            "viacep-cache",
            "cep-validation-enhanced",
            "cep-exists",
            "score-cache"
        );
    }

    @Test
    @DisplayName("Deve criar cache específico quando solicitado")
    void shouldCreateSpecificCacheWhenRequested() {
        // Given
        CacheManager cacheManager = config.seguradoCacheManager();

        // When
        var cpfCache = cacheManager.getCache("cpf-validation");

        // Then
        assertThat(cpfCache).isNotNull();
        assertThat(cpfCache.getName()).isEqualTo("cpf-validation");
    }

    @Test
    @DisplayName("Deve permitir armazenar e recuperar valores do cache")
    void shouldAllowStoringAndRetrievingValuesFromCache() {
        // Given
        CacheManager cacheManager = config.seguradoCacheManager();
        var cache = cacheManager.getCache("cpf-validation");

        // When
        cache.put("12345678909", true);
        var valor = cache.get("12345678909", Boolean.class);

        // Then
        assertThat(valor).isTrue();
    }

    @Test
    @DisplayName("Deve retornar null para chave não existente")
    void shouldReturnNullForNonExistentKey() {
        // Given
        CacheManager cacheManager = config.seguradoCacheManager();
        var cache = cacheManager.getCache("cpf-validation");

        // When
        var valor = cache.get("nao-existe");

        // Then
        assertThat(valor).isNull();
    }

    @Test
    @DisplayName("Deve permitir evict de valores do cache")
    void shouldAllowEvictingValuesFromCache() {
        // Given
        CacheManager cacheManager = config.seguradoCacheManager();
        var cache = cacheManager.getCache("email-validation");
        cache.put("test@example.com", true);

        // When
        cache.evict("test@example.com");
        var valor = cache.get("test@example.com");

        // Then
        assertThat(valor).isNull();
    }

    @Test
    @DisplayName("Deve permitir limpar cache completamente")
    void shouldAllowClearingCache() {
        // Given
        CacheManager cacheManager = config.seguradoCacheManager();
        var cache = cacheManager.getCache("bureau-validation");
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // When
        cache.clear();
        var valor1 = cache.get("key1");
        var valor2 = cache.get("key2");

        // Then
        assertThat(valor1).isNull();
        assertThat(valor2).isNull();
    }

    @Test
    @DisplayName("Deve ter exatamente 8 caches configurados")
    void shouldHaveExactly8CachesConfigured() {
        // When
        CacheManager cacheManager = config.seguradoCacheManager();

        // Then
        assertThat(cacheManager.getCacheNames()).hasSize(8);
    }
}
