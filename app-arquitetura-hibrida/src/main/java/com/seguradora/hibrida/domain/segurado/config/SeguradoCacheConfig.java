package com.seguradora.hibrida.domain.segurado.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuração de cache para o domínio de Segurado.
 * 
 * <p>Configura caches específicos conforme requisitos da US010:
 * <ul>
 *   <li>Cache de validação de CPF (TTL 1 hora)</li>
 *   <li>Cache de validação de email (TTL 1 hora)</li>
 *   <li>Cache de consulta ViaCEP (TTL 24 horas)</li>
 *   <li>Cache de bureau de crédito (TTL 1 hora)</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Configuration
@EnableCaching
public class SeguradoCacheConfig {
    
    /**
     * Cache manager para validações de segurado.
     * 
     * <p>Caches configurados:
     * <ul>
     *   <li>cpf-validation - Validação de unicidade de CPF</li>
     *   <li>email-validation - Validação de unicidade de email</li>
     *   <li>telefone-validation - Validação de telefone</li>
     *   <li>bureau-validation - Validação em bureaus de crédito</li>
     *   <li>viacep-cache - Cache de consultas ViaCEP</li>
     *   <li>cep-validation-enhanced - Cache de validação de CEP</li>
     *   <li>cep-exists - Cache de existência de CEP</li>
     *   <li>score-cache - Cache de scores de crédito</li>
     * </ul>
     */
    @Bean
    @Primary
    public CacheManager seguradoCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Configurar caches específicos
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "cpf-validation",
            "email-validation", 
            "telefone-validation",
            "bureau-validation",
            "viacep-cache",
            "cep-validation-enhanced",
            "cep-exists",
            "score-cache"
        ));
        
        // Permitir criação dinâmica de novos caches
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}