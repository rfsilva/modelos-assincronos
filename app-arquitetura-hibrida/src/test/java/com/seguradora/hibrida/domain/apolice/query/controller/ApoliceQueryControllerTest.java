package com.seguradora.hibrida.domain.apolice.query.controller;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceDetailView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceListView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceVencimentoView;
import com.seguradora.hibrida.domain.apolice.query.service.ApoliceQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ApoliceQueryController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApoliceQueryController Tests")
class ApoliceQueryControllerTest {

    @Mock
    private ApoliceQueryService queryService;

    @InjectMocks
    private ApoliceQueryController controller;

    private ApoliceDetailView detailView;
    private ApoliceListView listView;
    private ApoliceVencimentoView vencimentoView;
    private Page<ApoliceListView> page;

    @BeforeEach
    void setUp() {
        // Setup detail view
        detailView = new ApoliceDetailView(
            "ap-001", "AP-2024-001", "Seguro Auto", StatusApolice.ATIVA,
            "seg-001", "João Silva", "12345678901", "joao@email.com", "11999999999",
            "São Paulo", "SP", LocalDate.now(), LocalDate.now().plusDays(365),
            365, false, 12, new BigDecimal("100000"), new BigDecimal("5000"),
            new BigDecimal("5000"), new BigDecimal("10000"), "Cartão", 12,
            new BigDecimal("416.67"), List.of(TipoCobertura.TOTAL), "Total", true,
            Map.of(), "Operador A", "Online", "Obs", true, 85,
            Instant.now(), Instant.now(), 1L, 1L, List.of(), List.of(),
            Map.of(), List.of(), List.of()
        );

        // Setup list view
        listView = new ApoliceListView(
            "ap-001", "AP-2024-001", "Seguro Auto", StatusApolice.ATIVA,
            "João Silva", "12345678901", "São Paulo", "SP",
            LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
            new BigDecimal("100000"), new BigDecimal("5000"), new BigDecimal("5000"),
            "Cartão", 12, List.of(TipoCobertura.TOTAL), "Total", true,
            "Operador A", "Online", true, 85
        );

        // Setup vencimento view
        vencimentoView = new ApoliceVencimentoView(
            "ap-001", "AP-2024-001", "Seguro Auto", StatusApolice.ATIVA,
            "João Silva", "12345678901", "11999999999", "joao@email.com",
            LocalDate.now().plusDays(30), 30, "MÉDIA", true, 85,
            "APROVADA", new BigDecimal("5000"), "Cartão", "Operador A", false
        );

        // Setup page
        page = new PageImpl<>(List.of(listView));
    }

    @Nested
    @DisplayName("Consultas Básicas")
    class ConsultasBasicas {

        @Test
        @DisplayName("Deve buscar apólice por ID com sucesso")
        void deveBuscarPorId() {
            // Arrange
            when(queryService.buscarPorId("ap-001")).thenReturn(Optional.of(detailView));

            // Act
            ResponseEntity<ApoliceDetailView> response = controller.buscarPorId("ap-001");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(detailView);
            verify(queryService).buscarPorId("ap-001");
        }

        @Test
        @DisplayName("Deve retornar 404 quando apólice não encontrada por ID")
        void deveRetornar404QuandoNaoEncontradaPorId() {
            // Arrange
            when(queryService.buscarPorId("inexistente")).thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApoliceDetailView> response = controller.buscarPorId("inexistente");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("Deve buscar apólice por número com sucesso")
        void deveBuscarPorNumero() {
            // Arrange
            when(queryService.buscarPorNumero("AP-2024-001")).thenReturn(Optional.of(detailView));

            // Act
            ResponseEntity<ApoliceDetailView> response = controller.buscarPorNumero("AP-2024-001");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(detailView);
        }

        @Test
        @DisplayName("Deve retornar 404 quando apólice não encontrada por número")
        void deveRetornar404QuandoNaoEncontradaPorNumero() {
            // Arrange
            when(queryService.buscarPorNumero("inexistente")).thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApoliceDetailView> response = controller.buscarPorNumero("inexistente");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Deve listar todas as apólices com paginação")
        void deveListarTodas() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(queryService.listarTodas(pageable)).thenReturn(page);

            // Act
            ResponseEntity<Page<ApoliceListView>> response = controller.listarTodas(pageable);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(page);
            assertThat(response.getBody().getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Consultas por Segurado")
    class ConsultasPorSegurado {

        @Test
        @DisplayName("Deve buscar apólices por CPF do segurado")
        void deveBuscarPorCpfSegurado() {
            // Arrange
            when(queryService.buscarPorCpfSegurado("12345678901"))
                .thenReturn(List.of(listView));

            // Act
            ResponseEntity<List<ApoliceListView>> response =
                controller.buscarPorCpfSegurado("12345678901");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0)).isEqualTo(listView);
        }

        @Test
        @DisplayName("Deve buscar apólices ativas por CPF")
        void deveBuscarAtivasPorCpf() {
            // Arrange
            when(queryService.buscarAtivasPorCpfSegurado("12345678901"))
                .thenReturn(List.of(listView));

            // Act
            ResponseEntity<List<ApoliceListView>> response =
                controller.buscarAtivasPorCpf("12345678901");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices por nome do segurado")
        void deveBuscarPorNomeSegurado() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(queryService.buscarPorNomeSegurado("João", pageable)).thenReturn(page);

            // Act
            ResponseEntity<Page<ApoliceListView>> response =
                controller.buscarPorNomeSegurado("João", pageable);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(page);
        }
    }

    @Nested
    @DisplayName("Consultas por Status")
    class ConsultasPorStatus {

        @Test
        @DisplayName("Deve buscar apólices por status")
        void deveBuscarPorStatus() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(queryService.buscarPorStatus(StatusApolice.ATIVA, pageable)).thenReturn(page);

            // Act
            ResponseEntity<Page<ApoliceListView>> response =
                controller.buscarPorStatus(StatusApolice.ATIVA, pageable);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(page);
        }

        @Test
        @DisplayName("Deve buscar apólices ativas")
        void deveBuscarAtivas() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(queryService.buscarAtivas(pageable)).thenReturn(page);

            // Act
            ResponseEntity<Page<ApoliceListView>> response = controller.buscarAtivas(pageable);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(page);
        }

        @Test
        @DisplayName("Deve buscar apólices vencidas")
        void deveBuscarVencidas() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(queryService.buscarVencidas(pageable)).thenReturn(page);

            // Act
            ResponseEntity<Page<ApoliceListView>> response = controller.buscarVencidas(pageable);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("Consultas por Vencimento")
    class ConsultasPorVencimento {

        @Test
        @DisplayName("Deve buscar apólices vencendo entre datas")
        void deveBuscarVencendoEntre() {
            // Arrange
            LocalDate inicio = LocalDate.now();
            LocalDate fim = LocalDate.now().plusDays(30);
            when(queryService.buscarVencendoEntre(inicio, fim))
                .thenReturn(List.of(vencimentoView));

            // Act
            ResponseEntity<List<ApoliceVencimentoView>> response =
                controller.buscarVencendoEntre(inicio, fim);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices vencendo em X dias")
        void deveBuscarVencendoEm() {
            // Arrange
            when(queryService.buscarVencendoEm(30)).thenReturn(List.of(vencimentoView));

            // Act
            ResponseEntity<List<ApoliceVencimentoView>> response =
                controller.buscarVencendoEm(30);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices com vencimento próximo")
        void deveBuscarComVencimentoProximo() {
            // Arrange
            when(queryService.buscarComVencimentoProximo())
                .thenReturn(List.of(vencimentoView));

            // Act
            ResponseEntity<List<ApoliceVencimentoView>> response =
                controller.buscarComVencimentoProximo();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Consultas por Produto e Cobertura")
    class ConsultasPorProdutoCobertura {

        @Test
        @DisplayName("Deve buscar apólices por produto")
        void deveBuscarPorProduto() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(queryService.buscarPorProduto("Seguro Auto", pageable)).thenReturn(page);

            // Act
            ResponseEntity<Page<ApoliceListView>> response =
                controller.buscarPorProduto("Seguro Auto", pageable);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Deve buscar apólices por cobertura")
        void deveBuscarPorCobertura() {
            // Arrange
            when(queryService.buscarPorCobertura(TipoCobertura.TOTAL))
                .thenReturn(List.of(listView));

            // Act
            ResponseEntity<List<ApoliceListView>> response =
                controller.buscarPorCobertura(TipoCobertura.TOTAL);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices com cobertura total")
        void deveBuscarComCoberturaTotal() {
            // Arrange
            when(queryService.buscarComCoberturaTotal()).thenReturn(List.of(listView));

            // Act
            ResponseEntity<List<ApoliceListView>> response =
                controller.buscarComCoberturaTotal();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("Consultas por Valor")
    class ConsultasPorValor {

        @Test
        @DisplayName("Deve buscar apólices por faixa de valor")
        void deveBuscarPorFaixaValor() {
            // Arrange
            BigDecimal valorMin = new BigDecimal("1000");
            BigDecimal valorMax = new BigDecimal("10000");
            Pageable pageable = PageRequest.of(0, 20);
            when(queryService.buscarPorFaixaValor(valorMin, valorMax, pageable))
                .thenReturn(page);

            // Act
            ResponseEntity<Page<ApoliceListView>> response =
                controller.buscarPorFaixaValor(valorMin, valorMax, pageable);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Deve buscar apólices de alto valor")
        void deveBuscarAltoValor() {
            // Arrange
            BigDecimal valorMinimo = new BigDecimal("50000");
            when(queryService.buscarAltoValor(valorMinimo)).thenReturn(List.of(listView));

            // Act
            ResponseEntity<List<ApoliceListView>> response =
                controller.buscarAltoValor(valorMinimo);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("Consultas de Renovação")
    class ConsultasRenovacao {

        @Test
        @DisplayName("Deve buscar apólices elegíveis para renovação")
        void deveBuscarElegiveisRenovacao() {
            // Arrange
            when(queryService.buscarElegiveisRenovacaoAutomatica())
                .thenReturn(List.of(vencimentoView));

            // Act
            ResponseEntity<List<ApoliceVencimentoView>> response =
                controller.buscarElegiveisRenovacao();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Deve buscar apólices precisando de atenção")
        void deveBuscarPrecisandoAtencao() {
            // Arrange
            when(queryService.buscarPrecisandoAtencaoRenovacao())
                .thenReturn(List.of(vencimentoView));

            // Act
            ResponseEntity<List<ApoliceVencimentoView>> response =
                controller.buscarPrecisandoAtencao();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Deve buscar apólices por score de renovação")
        void deveBuscarPorScoreRenovacao() {
            // Arrange
            when(queryService.buscarPorScoreRenovacao(70, 100))
                .thenReturn(List.of(listView));

            // Act
            ResponseEntity<List<ApoliceListView>> response =
                controller.buscarPorScoreRenovacao(70, 100);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("Consultas por Localização")
    class ConsultasPorLocalizacao {

        @Test
        @DisplayName("Deve buscar apólices por cidade")
        void deveBuscarPorCidade() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(queryService.buscarPorCidade("São Paulo", pageable)).thenReturn(page);

            // Act
            ResponseEntity<Page<ApoliceListView>> response =
                controller.buscarPorCidade("São Paulo", pageable);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Deve buscar apólices por estado")
        void deveBuscarPorEstado() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(queryService.buscarPorEstado("SP", pageable)).thenReturn(page);

            // Act
            ResponseEntity<Page<ApoliceListView>> response =
                controller.buscarPorEstado("SP", pageable);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("Consultas Customizadas")
    class ConsultasCustomizadas {

        @Test
        @DisplayName("Deve buscar com múltiplos filtros")
        void deveBuscarComFiltros() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(queryService.buscarComFiltros(
                any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(page);

            // Act
            ResponseEntity<Page<ApoliceListView>> response = controller.buscarComFiltros(
                StatusApolice.ATIVA, "Seguro Auto", "12345678901",
                LocalDate.now(), LocalDate.now().plusDays(365),
                new BigDecimal("1000"), new BigDecimal("10000"), pageable
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Deve buscar com filtros nulos")
        void deveBuscarComFiltrosNulos() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(queryService.buscarComFiltros(
                null, null, null, null, null, null, null, pageable
            )).thenReturn(page);

            // Act
            ResponseEntity<Page<ApoliceListView>> response = controller.buscarComFiltros(
                null, null, null, null, null, null, null, pageable
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("Verificações e Utilitários")
    class Verificacoes {

        @Test
        @DisplayName("Deve verificar existência por número")
        void deveVerificarExistenciaPorNumero() {
            // Arrange
            when(queryService.existeComNumero("AP-2024-001")).thenReturn(true);

            // Act
            ResponseEntity<Map<String, Boolean>> response =
                controller.verificarExistenciaPorNumero("AP-2024-001");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("existe", true);
        }

        @Test
        @DisplayName("Deve contar apólices ativas por CPF")
        void deveContarAtivasPorCpf() {
            // Arrange
            when(queryService.contarAtivasPorCpf("12345678901")).thenReturn(5L);

            // Act
            ResponseEntity<Map<String, Long>> response =
                controller.contarAtivasPorCpf("12345678901");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("count", 5L);
        }

        @Test
        @DisplayName("Deve verificar se segurado possui apólices ativas")
        void deveVerificarSeguradoPossuiAtivas() {
            // Arrange
            when(queryService.seguradoPossuiApolicesAtivas("12345678901")).thenReturn(true);

            // Act
            ResponseEntity<Map<String, Boolean>> response =
                controller.verificarSeguradoPossuiAtivas("12345678901");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("possuiAtivas", true);
        }

        @Test
        @DisplayName("Deve retornar health check")
        void deveRetornarHealthCheck() {
            // Act
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsKey("status");
            assertThat(response.getBody().get("status")).isEqualTo("UP");
            assertThat(response.getBody()).containsKey("service");
            assertThat(response.getBody()).containsKey("timestamp");
            assertThat(response.getBody()).containsKey("version");
        }
    }

    @Nested
    @DisplayName("Testes de Integração de Fluxo")
    class TestesFluxo {

        @Test
        @DisplayName("Deve executar fluxo completo de consulta de apólice")
        void deveExecutarFluxoCompleto() {
            // Arrange
            when(queryService.buscarPorNumero("AP-2024-001"))
                .thenReturn(Optional.of(detailView));
            when(queryService.existeComNumero("AP-2024-001")).thenReturn(true);

            // Act
            ResponseEntity<Map<String, Boolean>> existeResponse =
                controller.verificarExistenciaPorNumero("AP-2024-001");
            ResponseEntity<ApoliceDetailView> detailResponse =
                controller.buscarPorNumero("AP-2024-001");

            // Assert
            assertThat(existeResponse.getBody()).containsEntry("existe", true);
            assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(detailResponse.getBody()).isNotNull();
        }
    }
}
