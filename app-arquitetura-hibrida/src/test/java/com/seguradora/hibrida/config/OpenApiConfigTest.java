package com.seguradora.hibrida.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link OpenApiConfig}.
 *
 * <p>Valida a criação e configuração do OpenAPI bean,
 * garantindo que todas as informações, servidores e tags estejam corretos.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OpenApiConfig - Testes Unitários")
class OpenApiConfigTest {

    private OpenApiConfig config;

    @BeforeEach
    void setUp() {
        config = new OpenApiConfig();
        ReflectionTestUtils.setField(config, "contextPath", "/api");
    }

    @Test
    @DisplayName("Deve criar OpenAPI bean corretamente")
    void shouldCreateOpenApiBeanCorrectly() {
        // When
        OpenAPI openAPI = config.arquiteturaHibridaOpenAPI();

        // Then
        assertThat(openAPI).isNotNull();
        assertThat(openAPI).isInstanceOf(OpenAPI.class);
    }

    @Test
    @DisplayName("Deve configurar informações da API corretamente")
    void shouldConfigureApiInfoCorrectly() {
        // When
        OpenAPI openAPI = config.arquiteturaHibridaOpenAPI();

        // Then
        assertThat(openAPI.getInfo()).isNotNull();
        Info info = openAPI.getInfo();
        assertThat(info.getTitle()).contains("Arquitetura Híbrida");
        assertThat(info.getDescription()).contains("Event Sourcing");
        assertThat(info.getDescription()).contains("CQRS");
        assertThat(info.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Deve configurar contato corretamente")
    void shouldConfigureContactCorrectly() {
        // When
        OpenAPI openAPI = config.arquiteturaHibridaOpenAPI();

        // Then
        Contact contact = openAPI.getInfo().getContact();
        assertThat(contact).isNotNull();
        assertThat(contact.getName()).isEqualTo("Equipe de Arquitetura Híbrida");
        assertThat(contact.getEmail()).isEqualTo("arquitetura-hibrida@seguradora.com");
        assertThat(contact.getUrl()).contains("github.com");
    }

    @Test
    @DisplayName("Deve configurar licença corretamente")
    void shouldConfigureLicenseCorrectly() {
        // When
        OpenAPI openAPI = config.arquiteturaHibridaOpenAPI();

        // Then
        License license = openAPI.getInfo().getLicense();
        assertThat(license).isNotNull();
        assertThat(license.getName()).isEqualTo("MIT License");
        assertThat(license.getUrl()).contains("opensource.org");
    }

    @Test
    @DisplayName("Deve configurar servidores corretamente")
    void shouldConfigureServersCorrectly() {
        // When
        OpenAPI openAPI = config.arquiteturaHibridaOpenAPI();

        // Then
        List<Server> servers = openAPI.getServers();
        assertThat(servers).isNotNull();
        assertThat(servers).hasSize(3);

        // Servidor local
        assertThat(servers.get(0).getUrl()).contains("localhost:8083");
        assertThat(servers.get(0).getDescription()).contains("Local");

        // Servidor de desenvolvimento
        assertThat(servers.get(1).getUrl()).contains("hibrida-dev.seguradora.com");
        assertThat(servers.get(1).getDescription()).contains("Desenvolvimento");

        // Servidor de produção
        assertThat(servers.get(2).getUrl()).contains("hibrida.seguradora.com");
        assertThat(servers.get(2).getDescription()).contains("Produção");
    }

    @Test
    @DisplayName("Deve incluir contextPath nas URLs dos servidores")
    void shouldIncludeContextPathInServerUrls() {
        // When
        OpenAPI openAPI = config.arquiteturaHibridaOpenAPI();

        // Then
        openAPI.getServers().forEach(server ->
            assertThat(server.getUrl()).endsWith("/api")
        );
    }

    @Test
    @DisplayName("Deve configurar tags corretamente")
    void shouldConfigureTagsCorrectly() {
        // When
        OpenAPI openAPI = config.arquiteturaHibridaOpenAPI();

        // Then
        List<Tag> tags = openAPI.getTags();
        assertThat(tags).isNotNull();
        assertThat(tags).hasSize(5);

        // Validar tags principais
        assertThat(tags).extracting(Tag::getName)
            .containsExactly(
                "📝 Commands",
                "🔍 Queries",
                "📚 Events",
                "📊 Projections",
                "🔧 Sistema"
            );
    }

    @Test
    @DisplayName("Deve configurar descrição de tags corretamente")
    void shouldConfigureTagDescriptionsCorrectly() {
        // When
        OpenAPI openAPI = config.arquiteturaHibridaOpenAPI();

        // Then
        List<Tag> tags = openAPI.getTags();

        // Validar descrições
        assertThat(tags.get(0).getDescription()).contains("Command Side");
        assertThat(tags.get(1).getDescription()).contains("Query Side");
        assertThat(tags.get(2).getDescription()).contains("Timeline");
        assertThat(tags.get(3).getDescription()).contains("projections");
        assertThat(tags.get(4).getDescription()).contains("sistema");
    }

    @Test
    @DisplayName("Deve usar contextPath padrão quando não especificado")
    void shouldUseDefaultContextPathWhenNotSpecified() {
        // Given
        ReflectionTestUtils.setField(config, "contextPath", "/");

        // When
        OpenAPI openAPI = config.arquiteturaHibridaOpenAPI();

        // Then
        openAPI.getServers().forEach(server ->
            assertThat(server.getUrl()).endsWith("/")
        );
    }
}
