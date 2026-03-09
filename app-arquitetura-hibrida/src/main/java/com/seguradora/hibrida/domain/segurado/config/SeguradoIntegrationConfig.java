package com.seguradora.hibrida.domain.segurado.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuração para integrações do domínio de Segurado.
 * 
 * <p>Configura clientes HTTP para integrações externas conforme US010:
 * <ul>
 *   <li>ViaCEP para validação de CEP</li>
 *   <li>Bureaus de crédito para validação de CPF</li>
 *   <li>Timeouts e retry configurados</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Configuration
public class SeguradoIntegrationConfig {
    
    /**
     * RestTemplate configurado para integrações do domínio Segurado.
     * 
     * <p>Configurações:
     * <ul>
     *   <li>Connect timeout: 5 segundos</li>
     *   <li>Read timeout: 10 segundos</li>
     *   <li>User-Agent customizado</li>
     * </ul>
     */
    @Bean("seguradoRestTemplate")
    public RestTemplate seguradoRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .defaultHeader("User-Agent", "Seguradora-Hibrida/2.0.0")
                .defaultHeader("Accept", "application/json")
                .build();
    }
    
    /**
     * RestTemplate específico para ViaCEP com configurações otimizadas.
     */
    @Bean("viaCepRestTemplate")
    public RestTemplate viaCepRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(8))
                .defaultHeader("User-Agent", "Seguradora-Hibrida-ViaCEP/2.0.0")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}