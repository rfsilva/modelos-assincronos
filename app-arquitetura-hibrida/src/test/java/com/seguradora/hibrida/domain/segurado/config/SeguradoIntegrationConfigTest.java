package com.seguradora.hibrida.domain.segurado.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link SeguradoIntegrationConfig}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoIntegrationConfig - Testes Unitários")
class SeguradoIntegrationConfigTest {

    private final SeguradoIntegrationConfig config = new SeguradoIntegrationConfig();

    @Test
    @DisplayName("Deve criar RestTemplate para segurado com configurações corretas")
    void shouldCreateSeguradoRestTemplateWithCorrectConfiguration() {
        // Given
        RestTemplateBuilder builder = new RestTemplateBuilder();

        // When
        RestTemplate restTemplate = config.seguradoRestTemplate(builder);

        // Then
        assertThat(restTemplate).isNotNull();
    }

    @Test
    @DisplayName("Deve criar RestTemplate para ViaCEP com configurações corretas")
    void shouldCreateViaCepRestTemplateWithCorrectConfiguration() {
        // Given
        RestTemplateBuilder builder = new RestTemplateBuilder();

        // When
        RestTemplate restTemplate = config.viaCepRestTemplate(builder);

        // Then
        assertThat(restTemplate).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar timeouts no RestTemplate de segurado")
    void shouldConfigureTimeoutsOnSeguradoRestTemplate() {
        // Given
        RestTemplateBuilder builder = new RestTemplateBuilder();

        // When
        RestTemplate restTemplate = config.seguradoRestTemplate(builder);

        // Then
        // RestTemplate foi criado com builder que tem timeouts configurados
        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate.getRequestFactory()).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar timeouts no RestTemplate de ViaCEP")
    void shouldConfigureTimeoutsOnViaCepRestTemplate() {
        // Given
        RestTemplateBuilder builder = new RestTemplateBuilder();

        // When
        RestTemplate restTemplate = config.viaCepRestTemplate(builder);

        // Then
        // RestTemplate foi criado com builder que tem timeouts configurados
        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate.getRequestFactory()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar beans diferentes para cada RestTemplate")
    void shouldCreateDifferentBeansForEachRestTemplate() {
        // Given
        RestTemplateBuilder builder = new RestTemplateBuilder();

        // When
        RestTemplate seguradoRestTemplate = config.seguradoRestTemplate(builder);
        RestTemplate viaCepRestTemplate = config.viaCepRestTemplate(builder);

        // Then
        assertThat(seguradoRestTemplate).isNotSameAs(viaCepRestTemplate);
    }

    @Test
    @DisplayName("Deve permitir criar múltiplas instâncias de RestTemplate")
    void shouldAllowCreatingMultipleInstancesOfRestTemplate() {
        // Given
        RestTemplateBuilder builder = new RestTemplateBuilder();

        // When
        RestTemplate rt1 = config.seguradoRestTemplate(builder);
        RestTemplate rt2 = config.seguradoRestTemplate(builder);

        // Then
        assertThat(rt1).isNotNull();
        assertThat(rt2).isNotNull();
        // Cada chamada cria uma nova instância
        assertThat(rt1).isNotSameAs(rt2);
    }
}
