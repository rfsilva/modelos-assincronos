package com.seguradora.hibrida.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthController - Testes Unitários")
class HealthControllerTest {

    @InjectMocks
    private HealthController controller;

    @Test
    @DisplayName("Deve retornar status do sistema com sucesso")
    void shouldReturnSystemStatusSuccessfully() {
        // When
        ResponseEntity<Map<String, Object>> response = controller.status();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("aplicacao")).isEqualTo("Arquitetura Híbrida");
        assertThat(response.getBody().get("versao")).isEqualTo("1.0.0");
        assertThat(response.getBody().get("timestamp")).isInstanceOf(Instant.class);
        assertThat(response.getBody().get("arquitetura")).isEqualTo("hibrida");
    }

    @Test
    @DisplayName("Deve incluir funcionalidades no status")
    void shouldIncludeFunctionalitiesInStatus() {
        // When
        ResponseEntity<Map<String, Object>> response = controller.status();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Object funcionalidades = response.getBody().get("funcionalidades");
        assertThat(funcionalidades).isInstanceOf(String[].class);

        String[] funcArray = (String[]) funcionalidades;
        assertThat(funcArray).contains(
                "event-sourcing",
                "cqrs",
                "processamento-hibrido",
                "projections-otimizadas"
        );
    }

    @Test
    @DisplayName("Deve retornar health check com status UP")
    void shouldReturnHealthCheckWithStatusUp() {
        // When
        ResponseEntity<Health> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    @DisplayName("Deve incluir detalhes da aplicação no health check")
    void shouldIncludeApplicationDetailsInHealthCheck() {
        // When
        ResponseEntity<Health> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> details = response.getBody().getDetails();
        assertThat(details.get("aplicacao")).isEqualTo("Arquitetura Híbrida");
        assertThat(details.get("versao")).isEqualTo("1.0.0");
        assertThat(details.get("arquitetura")).isEqualTo("event-sourcing-cqrs");
    }

    @Test
    @DisplayName("Deve incluir informações de memória no health check")
    void shouldIncludeMemoryInformationInHealthCheck() {
        // When
        ResponseEntity<Health> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> details = response.getBody().getDetails();
        assertThat(details).containsKeys(
                "memoria_total",
                "memoria_usada",
                "memoria_livre",
                "processadores"
        );
    }

    @Test
    @DisplayName("Deve incluir status dos componentes no health check")
    void shouldIncludeComponentStatusInHealthCheck() {
        // When
        ResponseEntity<Health> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> details = response.getBody().getDetails();
        assertThat(details.get("eventStore")).isEqualTo("UP");
        assertThat(details.get("projections")).isEqualTo("UP");
        assertThat(details.get("commandSide")).isEqualTo("ATIVO");
        assertThat(details.get("querySide")).isEqualTo("ATIVO");
        assertThat(details.get("eventProcessing")).isEqualTo("ATIVO");
    }

    @Test
    @DisplayName("Deve incluir timestamp no health check")
    void shouldIncludeTimestampInHealthCheck() {
        // When
        ResponseEntity<Health> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> details = response.getBody().getDetails();
        assertThat(details.get("timestamp")).isInstanceOf(Instant.class);
    }

    @Test
    @DisplayName("Deve implementar HealthIndicator corretamente")
    void shouldImplementHealthIndicatorCorrectly() {
        // When
        Health health = controller.health();

        // Then
        assertThat(health).isNotNull();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve formatar bytes corretamente para KB")
    void shouldFormatBytesToKbCorrectly() {
        // When
        ResponseEntity<Health> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> details = response.getBody().getDetails();
        String memoriaTotal = (String) details.get("memoria_total");
        String memoriaUsada = (String) details.get("memoria_usada");
        String memoriaLivre = (String) details.get("memoria_livre");

        // Verifica se contém unidades (B, KB ou MB)
        assertThat(memoriaTotal).matches(".*[BKM].*");
        assertThat(memoriaUsada).matches(".*[BKM].*");
        assertThat(memoriaLivre).matches(".*[BKM].*");
    }

    @Test
    @DisplayName("Deve incluir número de processadores no health check")
    void shouldIncludeProcessorCountInHealthCheck() {
        // When
        ResponseEntity<Health> response = controller.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> details = response.getBody().getDetails();
        Integer processadores = (Integer) details.get("processadores");
        assertThat(processadores).isPositive();
    }

    @Test
    @DisplayName("Deve retornar status consistente entre endpoints")
    void shouldReturnConsistentStatusBetweenEndpoints() {
        // When
        ResponseEntity<Map<String, Object>> statusResponse = controller.status();
        ResponseEntity<Health> healthResponse = controller.healthCheck();

        // Then
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(statusResponse.getBody().get("status")).isEqualTo("UP");
        assertThat(healthResponse.getBody().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    @DisplayName("Deve retornar versão consistente entre endpoints")
    void shouldReturnConsistentVersionBetweenEndpoints() {
        // When
        ResponseEntity<Map<String, Object>> statusResponse = controller.status();
        ResponseEntity<Health> healthResponse = controller.healthCheck();

        // Then
        assertThat(statusResponse.getBody().get("versao")).isEqualTo("1.0.0");
        assertThat(healthResponse.getBody().getDetails().get("versao")).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Deve retornar nome da aplicação consistente entre endpoints")
    void shouldReturnConsistentApplicationNameBetweenEndpoints() {
        // When
        ResponseEntity<Map<String, Object>> statusResponse = controller.status();
        ResponseEntity<Health> healthResponse = controller.healthCheck();

        // Then
        assertThat(statusResponse.getBody().get("aplicacao")).isEqualTo("Arquitetura Híbrida");
        assertThat(healthResponse.getBody().getDetails().get("aplicacao")).isEqualTo("Arquitetura Híbrida");
    }
}
