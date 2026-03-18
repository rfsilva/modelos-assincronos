package com.seguradora.hibrida.domain.sinistro.controller;

import com.seguradora.hibrida.domain.sinistro.query.dto.DashboardView;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroDetailView;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroFilter;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroListView;
import com.seguradora.hibrida.domain.sinistro.query.service.SinistroQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SinistroQueryController Tests")
class SinistroQueryControllerTest {

    @Mock
    private SinistroQueryService queryService;

    @Mock
    private SinistroDetailView detailViewMock;

    private SinistroQueryController controller;

    @BeforeEach
    void setUp() {
        controller = new SinistroQueryController(queryService);
    }

    // ==================== buscarPorId ====================

    @Test
    @DisplayName("buscarPorId deve retornar 200 quando sinistro encontrado")
    void buscarPorIdShouldReturn200WhenFound() {
        UUID id = UUID.randomUUID();
        when(queryService.buscarPorId(id)).thenReturn(Optional.of(detailViewMock));

        ResponseEntity<SinistroDetailView> response = controller.buscarPorId(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(detailViewMock);
    }

    @Test
    @DisplayName("buscarPorId deve retornar 404 quando não encontrado")
    void buscarPorIdShouldReturn404WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(queryService.buscarPorId(id)).thenReturn(Optional.empty());

        ResponseEntity<SinistroDetailView> response = controller.buscarPorId(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== buscarPorProtocolo ====================

    @Test
    @DisplayName("buscarPorProtocolo deve retornar 200 quando encontrado")
    void buscarPorProtocoloShouldReturn200WhenFound() {
        when(queryService.buscarPorProtocolo("SIN-2024-000001")).thenReturn(Optional.of(detailViewMock));

        ResponseEntity<SinistroDetailView> response = controller.buscarPorProtocolo("SIN-2024-000001");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(detailViewMock);
    }

    @Test
    @DisplayName("buscarPorProtocolo deve retornar 404 quando não encontrado")
    void buscarPorProtocoloShouldReturn404WhenNotFound() {
        when(queryService.buscarPorProtocolo("SIN-INEXISTENTE")).thenReturn(Optional.empty());

        ResponseEntity<SinistroDetailView> response = controller.buscarPorProtocolo("SIN-INEXISTENTE");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== listar ====================

    @Test
    @DisplayName("listar deve retornar 200 com página de resultados")
    void listarShouldReturn200WithPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SinistroListView> page = new PageImpl<>(List.of());
        when(queryService.listar(any(), any())).thenReturn(page);

        ResponseEntity<Page<SinistroListView>> response = controller.listar(new SinistroFilter(), pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    // ==================== buscarPorCpfSegurado ====================

    @Test
    @DisplayName("buscarPorCpfSegurado deve retornar 200 com resultados")
    void buscarPorCpfShouldReturn200() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SinistroListView> page = new PageImpl<>(List.of());
        when(queryService.buscarPorCpfSegurado(eq("12345678901"), any())).thenReturn(page);

        ResponseEntity<Page<SinistroListView>> response =
                controller.buscarPorCpfSegurado("12345678901", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ==================== buscarPorPlaca ====================

    @Test
    @DisplayName("buscarPorPlaca deve retornar 200 com resultados")
    void buscarPorPlacaShouldReturn200() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SinistroListView> page = new PageImpl<>(List.of());
        when(queryService.buscarPorPlaca(eq("ABC1234"), any())).thenReturn(page);

        ResponseEntity<Page<SinistroListView>> response =
                controller.buscarPorPlaca("ABC1234", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ==================== buscarPorTexto ====================

    @Test
    @DisplayName("buscarPorTexto deve retornar 200 para termo válido (>= 3 chars)")
    void buscarPorTextoShouldReturn200ForValidTerm() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SinistroListView> page = new PageImpl<>(List.of());
        when(queryService.buscarPorTexto(eq("acidente"), any())).thenReturn(page);

        ResponseEntity<Page<SinistroListView>> response =
                controller.buscarPorTexto("acidente", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("buscarPorTexto deve retornar 400 para termo muito curto (< 3 chars)")
    void buscarPorTextoShouldReturn400ForShortTerm() {
        Pageable pageable = PageRequest.of(0, 20);

        ResponseEntity<Page<SinistroListView>> response =
                controller.buscarPorTexto("ab", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("buscarPorTexto deve retornar 400 para termo nulo")
    void buscarPorTextoShouldReturn400ForNullTerm() {
        Pageable pageable = PageRequest.of(0, 20);

        ResponseEntity<Page<SinistroListView>> response =
                controller.buscarPorTexto(null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==================== buscarPorTag ====================

    @Test
    @DisplayName("buscarPorTag deve retornar 200 com resultados")
    void buscarPorTagShouldReturn200() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SinistroListView> page = new PageImpl<>(List.of());
        when(queryService.buscarPorTag(eq("URGENTE"), any())).thenReturn(page);

        ResponseEntity<Page<SinistroListView>> response =
                controller.buscarPorTag("URGENTE", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ==================== obterDashboard ====================

    @Test
    @DisplayName("obterDashboard deve retornar 200 com dados do dashboard")
    void obterDashboardShouldReturn200() {
        DashboardView dashboard = new DashboardView(100L, 20L, 5L, null, null, null, null, null, null, null, null);
        when(queryService.obterDashboard()).thenReturn(dashboard);

        ResponseEntity<DashboardView> response = controller.obterDashboard();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dashboard);
    }

    // ==================== healthCheck ====================

    @Test
    @DisplayName("healthCheck deve retornar 200 quando queryService funcionando")
    void healthCheckShouldReturn200WhenUp() {
        DashboardView dashboard = new DashboardView(50L, 10L, 2L, null, null, null, null, null, null, null, null);
        when(queryService.obterDashboard()).thenReturn(dashboard);

        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("status");
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    @DisplayName("healthCheck deve retornar 503 quando queryService lança exceção")
    void healthCheckShouldReturn503WhenDown() {
        when(queryService.obterDashboard()).thenThrow(new RuntimeException("DB down"));

        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().get("status")).isEqualTo("DOWN");
    }
}
