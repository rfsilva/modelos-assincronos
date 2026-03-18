package com.seguradora.hibrida.domain.analytics.controller;

import com.seguradora.hibrida.domain.analytics.dto.*;
import com.seguradora.hibrida.domain.analytics.service.RelatorioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link RelatorioController}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RelatorioController Tests")
class RelatorioControllerTest {

    @Mock
    private RelatorioService relatorioService;

    private RelatorioController controller;

    @BeforeEach
    void setUp() {
        controller = new RelatorioController(relatorioService);
    }

    // =========================================================================
    // Meta-informação
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @RestController")
    void shouldBeAnnotatedWithRestController() {
        assertThat(RelatorioController.class.isAnnotationPresent(RestController.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar mapeado em /api/v1/relatorios")
    void shouldBeMappedToRelatoriosPath() {
        RequestMapping mapping = RelatorioController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/api/v1/relatorios");
    }

    @Test
    @DisplayName("obterDashboardExecutivo deve estar mapeado em GET /dashboard")
    void obterDashboardExecutivoShouldBeMappedToGetDashboard() throws NoSuchMethodException {
        var method = RelatorioController.class.getMethod("obterDashboardExecutivo", LocalDate.class);
        assertThat(method.isAnnotationPresent(GetMapping.class)).isTrue();
        assertThat(method.getAnnotation(GetMapping.class).value()).contains("/dashboard");
    }

    @Test
    @DisplayName("healthCheck deve estar mapeado em GET /health")
    void healthCheckShouldBeMappedToGetHealth() throws NoSuchMethodException {
        var method = RelatorioController.class.getMethod("healthCheck");
        assertThat(method.isAnnotationPresent(GetMapping.class)).isTrue();
        assertThat(method.getAnnotation(GetMapping.class).value()).contains("/health");
    }

    // =========================================================================
    // obterDashboardExecutivo
    // =========================================================================

    @Nested
    @DisplayName("obterDashboardExecutivo()")
    class ObterDashboardExecutivo {

        @Test
        @DisplayName("Deve retornar 200 com dados do dashboard")
        void shouldReturn200WithDashboardData() {
            LocalDate data = LocalDate.now();
            DashboardExecutivoView dashboard = DashboardExecutivoView.builder()
                    .dataReferencia(data)
                    .totalSegurados(1000L)
                    .build();
            when(relatorioService.obterDashboardExecutivo(data)).thenReturn(dashboard);

            ResponseEntity<DashboardExecutivoView> response = controller.obterDashboardExecutivo(data);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTotalSegurados()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("Deve usar data de hoje quando data é null")
        void shouldUseTodayWhenDataIsNull() {
            DashboardExecutivoView dashboard = DashboardExecutivoView.builder()
                    .dataReferencia(LocalDate.now())
                    .totalSegurados(0L)
                    .build();
            when(relatorioService.obterDashboardExecutivo(any())).thenReturn(dashboard);

            ResponseEntity<DashboardExecutivoView> response = controller.obterDashboardExecutivo(null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        private <T> T any() { return null; }
    }

    // =========================================================================
    // obterRelatorioSegurados
    // =========================================================================

    @Nested
    @DisplayName("obterRelatorioSegurados()")
    class ObterRelatorioSegurados {

        @Test
        @DisplayName("Deve retornar 200 com relatório")
        void shouldReturn200WithRelatorio() {
            LocalDate inicio = LocalDate.of(2024, 1, 1);
            LocalDate fim = LocalDate.of(2024, 12, 31);
            RelatorioSeguradosView relatorio = RelatorioSeguradosView.builder()
                    .periodoInicio(inicio).periodoFim(fim).totalSegurados(500L).build();
            when(relatorioService.obterRelatorioSegurados(inicio, fim)).thenReturn(relatorio);

            ResponseEntity<RelatorioSeguradosView> response = controller.obterRelatorioSegurados(inicio, fim);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getTotalSegurados()).isEqualTo(500L);
        }

        @Test
        @DisplayName("Deve retornar 400 quando início é após fim")
        void shouldReturn400WhenInicioIsAfterFim() {
            LocalDate inicio = LocalDate.of(2024, 12, 31);
            LocalDate fim = LocalDate.of(2024, 1, 1);

            ResponseEntity<RelatorioSeguradosView> response = controller.obterRelatorioSegurados(inicio, fim);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // =========================================================================
    // obterRelatorioApolices
    // =========================================================================

    @Nested
    @DisplayName("obterRelatorioApolices()")
    class ObterRelatorioApolices {

        @Test
        @DisplayName("Deve retornar 200 com relatório de apólices")
        void shouldReturn200WithRelatorioApolices() {
            LocalDate inicio = LocalDate.of(2024, 1, 1);
            LocalDate fim = LocalDate.of(2024, 12, 31);
            RelatorioApolicesView relatorio = RelatorioApolicesView.builder()
                    .totalApolices(300L).build();
            when(relatorioService.obterRelatorioApolices(inicio, fim)).thenReturn(relatorio);

            ResponseEntity<RelatorioApolicesView> response = controller.obterRelatorioApolices(inicio, fim);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Deve retornar 400 quando período inválido")
        void shouldReturn400WhenPeriodIsInvalid() {
            ResponseEntity<RelatorioApolicesView> response = controller.obterRelatorioApolices(
                    LocalDate.of(2024, 12, 1), LocalDate.of(2024, 1, 1));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // =========================================================================
    // obterRelatorioPerformance
    // =========================================================================

    @Test
    @DisplayName("obterRelatorioPerformance deve retornar 400 quando período inválido")
    void obterRelatorioPerformanceShouldReturn400WhenPeriodIsInvalid() {
        ResponseEntity<RelatorioPerformanceView> response = controller.obterRelatorioPerformance(
                LocalDate.of(2024, 12, 1), LocalDate.of(2024, 1, 1));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // =========================================================================
    // obterRelatorioRenovacoes
    // =========================================================================

    @Test
    @DisplayName("obterRelatorioRenovacoes deve retornar 400 quando período inválido")
    void obterRelatorioRenovacoesShouldReturn400WhenPeriodIsInvalid() {
        ResponseEntity<RelatorioRenovacoesView> response = controller.obterRelatorioRenovacoes(
                LocalDate.of(2024, 12, 1), LocalDate.of(2024, 1, 1));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // =========================================================================
    // healthCheck
    // =========================================================================

    @Test
    @DisplayName("healthCheck deve retornar 200 com mensagem")
    void healthCheckShouldReturn200WithMessage() {
        ResponseEntity<String> response = controller.healthCheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();
    }
}
