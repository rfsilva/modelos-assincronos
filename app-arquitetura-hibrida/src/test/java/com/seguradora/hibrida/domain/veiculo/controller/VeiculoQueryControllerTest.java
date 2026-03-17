package com.seguradora.hibrida.domain.veiculo.controller;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoDetailView;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoListView;
import com.seguradora.hibrida.domain.veiculo.query.service.VeiculoQueryService;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VeiculoQueryController}.
 *
 * <p>Testa todos os endpoints REST de consulta de veículos, incluindo:
 * - Consultas básicas por identificadores únicos
 * - Listagens paginadas por diversos critérios
 * - Consultas por proprietário
 * - Consultas por marca/modelo
 * - Consultas geográficas
 * - Consultas de apólice
 * - Filtros avançados
 * - Verificações de existência
 * - Estatísticas
 * - Health check
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoQueryController - Testes Unitários")
class VeiculoQueryControllerTest {

    @Mock
    private VeiculoQueryService veiculoQueryService;

    @InjectMocks
    private VeiculoQueryController controller;

    private VeiculoDetailView detailView;
    private VeiculoListView listView;
    private Page<VeiculoListView> page;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup common test data
        detailView = new VeiculoDetailView(
            "vei-001",
            "ABC1234",
            "12345678901",
            "1HGBH41JXMN109186",
            "Honda",
            "Civic",
            2020,
            2021,
            "Branco",
            "FLEX",
            "PASSEIO",
            1600,
            "João Silva",
            "12345678901",
            "FISICA",
            StatusVeiculo.ATIVO,
            true,
            "São Paulo",
            "SP",
            "SUDESTE",
            1L,
            100L,
            Instant.now(),
            Instant.now(),
            Collections.emptyList(),
            Collections.emptyList()
        );

        listView = new VeiculoListView(
            "vei-001",
            "ABC1234",
            "Honda",
            "Civic",
            2020,
            2021,
            "Branco",
            "João Silva",
            "12345678901",
            StatusVeiculo.ATIVO,
            true,
            "São Paulo",
            "SP",
            Instant.now(),
            Instant.now()
        );

        page = new PageImpl<>(List.of(listView));
        pageable = PageRequest.of(0, 20);
    }

    @Nested
    @DisplayName("Consultas Básicas")
    class ConsultasBasicas {

        @Test
        @DisplayName("Deve buscar veículo por ID com sucesso")
        void deveBuscarPorIdComSucesso() {
            // Given
            String id = "vei-001";
            when(veiculoQueryService.buscarPorId(id)).thenReturn(Optional.of(detailView));

            // When
            ResponseEntity<VeiculoDetailView> response = controller.buscarPorId(id);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEqualTo(detailView);
            assertThat(response.getBody().id()).isEqualTo(id);
            verify(veiculoQueryService).buscarPorId(id);
        }

        @Test
        @DisplayName("Deve retornar 404 quando veículo não encontrado por ID")
        void deveRetornar404QuandoVeiculoNaoEncontradoPorId() {
            // Given
            String id = "vei-inexistente";
            when(veiculoQueryService.buscarPorId(id)).thenReturn(Optional.empty());

            // When
            ResponseEntity<VeiculoDetailView> response = controller.buscarPorId(id);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
            verify(veiculoQueryService).buscarPorId(id);
        }

        @Test
        @DisplayName("Deve buscar veículo por placa com sucesso")
        void deveBuscarPorPlacaComSucesso() {
            // Given
            String placa = "ABC1234";
            when(veiculoQueryService.buscarPorPlaca(placa)).thenReturn(Optional.of(detailView));

            // When
            ResponseEntity<VeiculoDetailView> response = controller.buscarPorPlaca(placa);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().placa()).isEqualTo(placa);
            verify(veiculoQueryService).buscarPorPlaca(placa);
        }

        @Test
        @DisplayName("Deve retornar 404 quando veículo não encontrado por placa")
        void deveRetornar404QuandoVeiculoNaoEncontradoPorPlaca() {
            // Given
            String placa = "XYZ9999";
            when(veiculoQueryService.buscarPorPlaca(placa)).thenReturn(Optional.empty());

            // When
            ResponseEntity<VeiculoDetailView> response = controller.buscarPorPlaca(placa);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
            verify(veiculoQueryService).buscarPorPlaca(placa);
        }

        @Test
        @DisplayName("Deve buscar veículo por RENAVAM com sucesso")
        void deveBuscarPorRenavamComSucesso() {
            // Given
            String renavam = "12345678901";
            when(veiculoQueryService.buscarPorRenavam(renavam)).thenReturn(Optional.of(detailView));

            // When
            ResponseEntity<VeiculoDetailView> response = controller.buscarPorRenavam(renavam);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().renavam()).isEqualTo(renavam);
            verify(veiculoQueryService).buscarPorRenavam(renavam);
        }

        @Test
        @DisplayName("Deve retornar 404 quando veículo não encontrado por RENAVAM")
        void deveRetornar404QuandoVeiculoNaoEncontradoPorRenavam() {
            // Given
            String renavam = "99999999999";
            when(veiculoQueryService.buscarPorRenavam(renavam)).thenReturn(Optional.empty());

            // When
            ResponseEntity<VeiculoDetailView> response = controller.buscarPorRenavam(renavam);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
            verify(veiculoQueryService).buscarPorRenavam(renavam);
        }

        @Test
        @DisplayName("Deve buscar veículo por chassi com sucesso")
        void deveBuscarPorChassiComSucesso() {
            // Given
            String chassi = "1HGBH41JXMN109186";
            when(veiculoQueryService.buscarPorChassi(chassi)).thenReturn(Optional.of(detailView));

            // When
            ResponseEntity<VeiculoDetailView> response = controller.buscarPorChassi(chassi);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().chassi()).isEqualTo(chassi);
            verify(veiculoQueryService).buscarPorChassi(chassi);
        }

        @Test
        @DisplayName("Deve retornar 404 quando veículo não encontrado por chassi")
        void deveRetornar404QuandoVeiculoNaoEncontradoPorChassi() {
            // Given
            String chassi = "XXXXXXXXXXX999999";
            when(veiculoQueryService.buscarPorChassi(chassi)).thenReturn(Optional.empty());

            // When
            ResponseEntity<VeiculoDetailView> response = controller.buscarPorChassi(chassi);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
            verify(veiculoQueryService).buscarPorChassi(chassi);
        }
    }

    @Nested
    @DisplayName("Listagens")
    class Listagens {

        @Test
        @DisplayName("Deve listar todos os veículos com paginação")
        void deveListarTodosComPaginacao() {
            // Given
            when(veiculoQueryService.listarTodos(pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.listarTodos(pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0)).isEqualTo(listView);
            verify(veiculoQueryService).listarTodos(pageable);
        }

        @Test
        @DisplayName("Deve listar veículos por status")
        void deveListarPorStatus() {
            // Given
            StatusVeiculo status = StatusVeiculo.ATIVO;
            when(veiculoQueryService.buscarPorStatus(status, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.listarPorStatus(status, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            verify(veiculoQueryService).buscarPorStatus(status, pageable);
        }

        @Test
        @DisplayName("Deve listar veículos ativos")
        void deveListarVeiculosAtivos() {
            // Given
            when(veiculoQueryService.buscarAtivos(pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.listarAtivos(pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            verify(veiculoQueryService).buscarAtivos(pageable);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há veículos")
        void deveRetornarListaVaziaQuandoNaoHaVeiculos() {
            // Given
            Page<VeiculoListView> emptyPage = new PageImpl<>(Collections.emptyList());
            when(veiculoQueryService.listarTodos(pageable)).thenReturn(emptyPage);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.listarTodos(pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isEmpty();
            verify(veiculoQueryService).listarTodos(pageable);
        }
    }

    @Nested
    @DisplayName("Consultas Por Proprietário")
    class ConsultasPorProprietario {

        @Test
        @DisplayName("Deve buscar veículos por CPF do proprietário")
        void deveBuscarPorProprietarioCpf() {
            // Given
            String cpf = "12345678901";
            List<VeiculoListView> veiculos = List.of(listView);
            when(veiculoQueryService.buscarPorProprietarioCpf(cpf)).thenReturn(veiculos);

            // When
            ResponseEntity<List<VeiculoListView>> response = controller.buscarPorProprietarioCpf(cpf);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).proprietarioCpf()).isEqualTo(cpf);
            verify(veiculoQueryService).buscarPorProprietarioCpf(cpf);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando CPF não possui veículos")
        void deveRetornarListaVaziaQuandoCpfNaoPossuiVeiculos() {
            // Given
            String cpf = "99999999999";
            when(veiculoQueryService.buscarPorProprietarioCpf(cpf)).thenReturn(Collections.emptyList());

            // When
            ResponseEntity<List<VeiculoListView>> response = controller.buscarPorProprietarioCpf(cpf);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
            verify(veiculoQueryService).buscarPorProprietarioCpf(cpf);
        }

        @Test
        @DisplayName("Deve buscar veículos por nome do proprietário")
        void deveBuscarPorProprietarioNome() {
            // Given
            String nome = "João Silva";
            when(veiculoQueryService.buscarPorProprietarioNome(nome, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorProprietarioNome(nome, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            assertThat(response.getBody().getContent().get(0).proprietarioNome()).contains("João");
            verify(veiculoQueryService).buscarPorProprietarioNome(nome, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por nome parcial do proprietário")
        void deveBuscarPorNomeParcialDoProprietario() {
            // Given
            String nomeParcial = "João";
            when(veiculoQueryService.buscarPorProprietarioNome(nomeParcial, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorProprietarioNome(nomeParcial, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            verify(veiculoQueryService).buscarPorProprietarioNome(nomeParcial, pageable);
        }
    }

    @Nested
    @DisplayName("Consultas Por Marca e Modelo")
    class ConsultasPorMarcaEModelo {

        @Test
        @DisplayName("Deve buscar veículos por marca")
        void deveBuscarPorMarca() {
            // Given
            String marca = "Honda";
            when(veiculoQueryService.buscarPorMarca(marca, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorMarca(marca, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            assertThat(response.getBody().getContent().get(0).marca()).isEqualTo(marca);
            verify(veiculoQueryService).buscarPorMarca(marca, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por marca e modelo")
        void deveBuscarPorMarcaEModelo() {
            // Given
            String marca = "Honda";
            String modelo = "Civic";
            when(veiculoQueryService.buscarPorMarcaEModelo(marca, modelo, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorMarcaEModelo(marca, modelo, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            assertThat(response.getBody().getContent().get(0).marca()).isEqualTo(marca);
            assertThat(response.getBody().getContent().get(0).modelo()).isEqualTo(modelo);
            verify(veiculoQueryService).buscarPorMarcaEModelo(marca, modelo, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por termo fuzzy em marca ou modelo")
        void deveBuscarPorMarcaOuModelo() {
            // Given
            String termo = "civic";
            when(veiculoQueryService.buscarPorMarcaOuModelo(termo, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorMarcaOuModelo(termo, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            verify(veiculoQueryService).buscarPorMarcaOuModelo(termo, pageable);
        }
    }

    @Nested
    @DisplayName("Consultas Por Ano")
    class ConsultasPorAno {

        @Test
        @DisplayName("Deve buscar veículos por ano de fabricação")
        void deveBuscarPorAno() {
            // Given
            Integer ano = 2020;
            when(veiculoQueryService.buscarPorAno(ano, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorAno(ano, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            assertThat(response.getBody().getContent().get(0).anoFabricacao()).isEqualTo(ano);
            verify(veiculoQueryService).buscarPorAno(ano, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por faixa de ano")
        void deveBuscarPorFaixaAno() {
            // Given
            Integer anoInicio = 2018;
            Integer anoFim = 2022;
            when(veiculoQueryService.buscarPorFaixaAno(anoInicio, anoFim, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorFaixaAno(anoInicio, anoFim, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            assertThat(response.getBody().getContent().get(0).anoFabricacao())
                .isGreaterThanOrEqualTo(anoInicio)
                .isLessThanOrEqualTo(anoFim);
            verify(veiculoQueryService).buscarPorFaixaAno(anoInicio, anoFim, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos com ano futuro")
        void deveBuscarVeiculosComAnoFuturo() {
            // Given
            Integer anoFuturo = 2025;
            Page<VeiculoListView> emptyPage = new PageImpl<>(Collections.emptyList());
            when(veiculoQueryService.buscarPorAno(anoFuturo, pageable)).thenReturn(emptyPage);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorAno(anoFuturo, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isEmpty();
            verify(veiculoQueryService).buscarPorAno(anoFuturo, pageable);
        }
    }

    @Nested
    @DisplayName("Consultas Geográficas")
    class ConsultasGeograficas {

        @Test
        @DisplayName("Deve buscar veículos por cidade")
        void deveBuscarPorCidade() {
            // Given
            String cidade = "São Paulo";
            when(veiculoQueryService.buscarPorCidade(cidade, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorCidade(cidade, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            assertThat(response.getBody().getContent().get(0).cidade()).isEqualTo(cidade);
            verify(veiculoQueryService).buscarPorCidade(cidade, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por estado")
        void deveBuscarPorEstado() {
            // Given
            String estado = "SP";
            when(veiculoQueryService.buscarPorEstado(estado, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorEstado(estado, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            assertThat(response.getBody().getContent().get(0).estado()).isEqualTo(estado);
            verify(veiculoQueryService).buscarPorEstado(estado, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por região")
        void deveBuscarPorRegiao() {
            // Given
            String regiao = "SUDESTE";
            when(veiculoQueryService.buscarPorRegiao(regiao, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorRegiao(regiao, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            verify(veiculoQueryService).buscarPorRegiao(regiao, pageable);
        }

        @Test
        @DisplayName("Deve retornar lista vazia para região inexistente")
        void deveRetornarListaVaziaParaRegiaoInexistente() {
            // Given
            String regiao = "INEXISTENTE";
            Page<VeiculoListView> emptyPage = new PageImpl<>(Collections.emptyList());
            when(veiculoQueryService.buscarPorRegiao(regiao, pageable)).thenReturn(emptyPage);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorRegiao(regiao, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isEmpty();
            verify(veiculoQueryService).buscarPorRegiao(regiao, pageable);
        }
    }

    @Nested
    @DisplayName("Consultas De Apólice")
    class ConsultasDeApolice {

        @Test
        @DisplayName("Deve listar veículos com apólice ativa")
        void deveListarVeiculosComApoliceAtiva() {
            // Given
            when(veiculoQueryService.buscarComApoliceAtiva(pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.listarComApoliceAtiva(pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            assertThat(response.getBody().getContent().get(0).apoliceAtiva()).isTrue();
            verify(veiculoQueryService).buscarComApoliceAtiva(pageable);
        }

        @Test
        @DisplayName("Deve listar veículos sem apólice ativa")
        void deveListarVeiculosSemApoliceAtiva() {
            // Given
            VeiculoListView veiculoSemApolice = new VeiculoListView(
                "vei-002", "XYZ5678", "Toyota", "Corolla", 2019, 2020, "Preto",
                "Maria Santos", "98765432100", StatusVeiculo.ATIVO, false,
                "Rio de Janeiro", "RJ", Instant.now(), Instant.now()
            );
            Page<VeiculoListView> pageSemApolice = new PageImpl<>(List.of(veiculoSemApolice));
            when(veiculoQueryService.buscarSemApoliceAtiva(pageable)).thenReturn(pageSemApolice);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.listarSemApoliceAtiva(pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            assertThat(response.getBody().getContent().get(0).apoliceAtiva()).isFalse();
            verify(veiculoQueryService).buscarSemApoliceAtiva(pageable);
        }
    }

    @Nested
    @DisplayName("Consultas Avançadas Com Filtros")
    class ConsultasAvancadasComFiltros {

        @Test
        @DisplayName("Deve buscar com todos os filtros preenchidos")
        void deveBuscarComTodosFiltrosPreenchidos() {
            // Given
            String marca = "Honda";
            String modelo = "Civic";
            StatusVeiculo status = StatusVeiculo.ATIVO;
            Integer anoInicio = 2018;
            Integer anoFim = 2022;
            String estado = "SP";

            when(veiculoQueryService.buscarComFiltros(marca, modelo, status, anoInicio, anoFim, estado, pageable))
                .thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarComFiltros(
                marca, modelo, status, anoInicio, anoFim, estado, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            verify(veiculoQueryService).buscarComFiltros(marca, modelo, status, anoInicio, anoFim, estado, pageable);
        }

        @Test
        @DisplayName("Deve buscar com filtros parciais")
        void deveBuscarComFiltrosParciais() {
            // Given
            String marca = "Honda";
            when(veiculoQueryService.buscarComFiltros(marca, null, null, null, null, null, pageable))
                .thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarComFiltros(
                marca, null, null, null, null, null, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(veiculoQueryService).buscarComFiltros(marca, null, null, null, null, null, pageable);
        }

        @Test
        @DisplayName("Deve buscar sem nenhum filtro")
        void deveBuscarSemNenhumFiltro() {
            // Given
            when(veiculoQueryService.buscarComFiltros(null, null, null, null, null, null, pageable))
                .thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarComFiltros(
                null, null, null, null, null, null, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(veiculoQueryService).buscarComFiltros(null, null, null, null, null, null, pageable);
        }

        @Test
        @DisplayName("Deve buscar com filtro de status específico")
        void deveBuscarComFiltroDeStatusEspecifico() {
            // Given
            StatusVeiculo status = StatusVeiculo.INATIVO;
            when(veiculoQueryService.buscarComFiltros(null, null, status, null, null, null, pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarComFiltros(
                null, null, status, null, null, null, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(veiculoQueryService).buscarComFiltros(null, null, status, null, null, null, pageable);
        }
    }

    @Nested
    @DisplayName("Verificações De Existência")
    class VerificacoesDeExistencia {

        @Test
        @DisplayName("Deve verificar que placa existe")
        void deveVerificarQuePlacaExiste() {
            // Given
            String placa = "ABC1234";
            when(veiculoQueryService.existeComPlaca(placa)).thenReturn(true);

            // When
            ResponseEntity<Boolean> response = controller.existeComPlaca(placa);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
            verify(veiculoQueryService).existeComPlaca(placa);
        }

        @Test
        @DisplayName("Deve verificar que placa não existe")
        void deveVerificarQuePlacaNaoExiste() {
            // Given
            String placa = "ZZZ9999";
            when(veiculoQueryService.existeComPlaca(placa)).thenReturn(false);

            // When
            ResponseEntity<Boolean> response = controller.existeComPlaca(placa);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();
            verify(veiculoQueryService).existeComPlaca(placa);
        }

        @Test
        @DisplayName("Deve verificar que RENAVAM existe")
        void deveVerificarQueRenavamExiste() {
            // Given
            String renavam = "12345678901";
            when(veiculoQueryService.existeComRenavam(renavam)).thenReturn(true);

            // When
            ResponseEntity<Boolean> response = controller.existeComRenavam(renavam);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
            verify(veiculoQueryService).existeComRenavam(renavam);
        }

        @Test
        @DisplayName("Deve verificar que RENAVAM não existe")
        void deveVerificarQueRenavamNaoExiste() {
            // Given
            String renavam = "99999999999";
            when(veiculoQueryService.existeComRenavam(renavam)).thenReturn(false);

            // When
            ResponseEntity<Boolean> response = controller.existeComRenavam(renavam);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();
            verify(veiculoQueryService).existeComRenavam(renavam);
        }

        @Test
        @DisplayName("Deve verificar que chassi existe")
        void deveVerificarQueChassiExiste() {
            // Given
            String chassi = "1HGBH41JXMN109186";
            when(veiculoQueryService.existeComChassi(chassi)).thenReturn(true);

            // When
            ResponseEntity<Boolean> response = controller.existeComChassi(chassi);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
            verify(veiculoQueryService).existeComChassi(chassi);
        }

        @Test
        @DisplayName("Deve verificar que chassi não existe")
        void deveVerificarQueChassiNaoExiste() {
            // Given
            String chassi = "XXXXXXXXXXX999999";
            when(veiculoQueryService.existeComChassi(chassi)).thenReturn(false);

            // When
            ResponseEntity<Boolean> response = controller.existeComChassi(chassi);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();
            verify(veiculoQueryService).existeComChassi(chassi);
        }
    }

    @Nested
    @DisplayName("Estatísticas")
    class Estatisticas {

        @Test
        @DisplayName("Deve obter estatísticas gerais")
        void deveObterEstatisticasGerais() {
            // Given
            VeiculoQueryService.VeiculoStatistics stats = new VeiculoQueryService.VeiculoStatistics(
                1000L, 850L, 600L, 400L, 60.0, "Honda", "SP"
            );
            when(veiculoQueryService.obterEstatisticas()).thenReturn(stats);

            // When
            ResponseEntity<VeiculoQueryService.VeiculoStatistics> response = controller.obterEstatisticas();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().totalVeiculos()).isEqualTo(1000L);
            assertThat(response.getBody().veiculosAtivos()).isEqualTo(850L);
            assertThat(response.getBody().veiculosComApolice()).isEqualTo(600L);
            assertThat(response.getBody().veiculosSemApolice()).isEqualTo(400L);
            assertThat(response.getBody().percentualComApolice()).isEqualTo(60.0);
            assertThat(response.getBody().marcaMaisComum()).isEqualTo("Honda");
            assertThat(response.getBody().estadoMaisComum()).isEqualTo("SP");
            verify(veiculoQueryService).obterEstatisticas();
        }

        @Test
        @DisplayName("Deve obter estatísticas por estado")
        void deveObterEstatisticasPorEstado() {
            // Given
            List<Object[]> stats = List.of(
                new Object[]{"SP", 500L},
                new Object[]{"RJ", 300L},
                new Object[]{"MG", 200L}
            );
            when(veiculoQueryService.obterEstatisticasPorEstado()).thenReturn(stats);

            // When
            ResponseEntity<List<Object[]>> response = controller.obterEstatisticasPorEstado();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(3);
            assertThat(response.getBody().get(0)[0]).isEqualTo("SP");
            assertThat(response.getBody().get(0)[1]).isEqualTo(500L);
            verify(veiculoQueryService).obterEstatisticasPorEstado();
        }

        @Test
        @DisplayName("Deve obter estatísticas por marca")
        void deveObterEstatisticasPorMarca() {
            // Given
            List<Object[]> stats = List.of(
                new Object[]{"Honda", 350L},
                new Object[]{"Toyota", 280L},
                new Object[]{"Volkswagen", 250L}
            );
            when(veiculoQueryService.obterEstatisticasPorMarca()).thenReturn(stats);

            // When
            ResponseEntity<List<Object[]>> response = controller.obterEstatisticasPorMarca();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(3);
            assertThat(response.getBody().get(0)[0]).isEqualTo("Honda");
            assertThat(response.getBody().get(0)[1]).isEqualTo(350L);
            verify(veiculoQueryService).obterEstatisticasPorMarca();
        }

        @Test
        @DisplayName("Deve retornar estatísticas vazias quando não há dados")
        void deveRetornarEstatisticasVaziasQuandoNaoHaDados() {
            // Given
            VeiculoQueryService.VeiculoStatistics stats = new VeiculoQueryService.VeiculoStatistics(
                0L, 0L, 0L, 0L, 0.0, null, null
            );
            when(veiculoQueryService.obterEstatisticas()).thenReturn(stats);

            // When
            ResponseEntity<VeiculoQueryService.VeiculoStatistics> response = controller.obterEstatisticas();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().totalVeiculos()).isZero();
            verify(veiculoQueryService).obterEstatisticas();
        }
    }

    @Nested
    @DisplayName("Health Check")
    class HealthCheck {

        @Test
        @DisplayName("Deve retornar status UP quando serviço está saudável")
        void deveRetornarStatusUpQuandoServicoEstaSaudavel() {
            // Given
            VeiculoQueryService.VeiculoStatistics stats = new VeiculoQueryService.VeiculoStatistics(
                1000L, 850L, 600L, 400L, 60.0, "Honda", "SP"
            );
            when(veiculoQueryService.obterEstatisticas()).thenReturn(stats);

            // When
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("status")).isEqualTo("UP");
            assertThat(response.getBody().get("service")).isEqualTo("VeiculoQueryService");
            assertThat(response.getBody().get("totalVeiculos")).isEqualTo(1000L);
            assertThat(response.getBody().get("veiculosAtivos")).isEqualTo(850L);
            assertThat(response.getBody()).containsKey("timestamp");
            assertThat(response.getBody()).containsKey("percentualComApolice");
            verify(veiculoQueryService).obterEstatisticas();
        }

        @Test
        @DisplayName("Deve retornar status DOWN quando serviço está indisponível")
        void deveRetornarStatusDownQuandoServicoEstaIndisponivel() {
            // Given
            when(veiculoQueryService.obterEstatisticas())
                .thenThrow(new RuntimeException("Database connection failed"));

            // When
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("status")).isEqualTo("DOWN");
            assertThat(response.getBody().get("service")).isEqualTo("VeiculoQueryService");
            assertThat(response.getBody().get("error")).isEqualTo("Database connection failed");
            assertThat(response.getBody()).containsKey("timestamp");
            verify(veiculoQueryService).obterEstatisticas();
        }

        @Test
        @DisplayName("Deve retornar status DOWN quando ocorre erro genérico")
        void deveRetornarStatusDownQuandoOcorreErroGenerico() {
            // Given
            when(veiculoQueryService.obterEstatisticas())
                .thenThrow(new RuntimeException("Unexpected error"));

            // When
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("status")).isEqualTo("DOWN");
            assertThat(response.getBody()).containsKey("error");
            verify(veiculoQueryService).obterEstatisticas();
        }
    }

    @Nested
    @DisplayName("Casos De Borda E Validações")
    class CasosDeBordaEValidacoes {

        @Test
        @DisplayName("Deve buscar com paginação customizada")
        void deveBuscarComPaginacaoCustomizada() {
            // Given
            Pageable customPageable = PageRequest.of(2, 50);
            when(veiculoQueryService.listarTodos(customPageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.listarTodos(customPageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(veiculoQueryService).listarTodos(customPageable);
        }

        @Test
        @DisplayName("Deve buscar com múltiplos veículos no resultado")
        void deveBuscarComMultiplosVeiculosNoResultado() {
            // Given
            VeiculoListView veiculo2 = new VeiculoListView(
                "vei-002", "DEF5678", "Toyota", "Corolla", 2019, 2020, "Preto",
                "Maria Santos", "98765432100", StatusVeiculo.ATIVO, false,
                "Rio de Janeiro", "RJ", Instant.now(), Instant.now()
            );
            Page<VeiculoListView> multiPage = new PageImpl<>(List.of(listView, veiculo2));
            when(veiculoQueryService.listarTodos(pageable)).thenReturn(multiPage);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.listarTodos(pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(2);
            verify(veiculoQueryService).listarTodos(pageable);
        }

        @Test
        @DisplayName("Deve buscar com caracteres especiais no termo de busca")
        void deveBuscarComCaracteresEspeciaisNoTermoDeBusca() {
            // Given
            String termoComEspeciais = "hõndá çívîc";
            when(veiculoQueryService.buscarPorMarcaOuModelo(termoComEspeciais, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<VeiculoListView>> response = controller.buscarPorMarcaOuModelo(termoComEspeciais, pageable);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(veiculoQueryService).buscarPorMarcaOuModelo(termoComEspeciais, pageable);
        }

        @Test
        @DisplayName("Deve listar todos os status de veículos")
        void deveListarTodosOsStatusDeVeiculos() {
            // Given - testa todos os status possíveis
            for (StatusVeiculo status : StatusVeiculo.values()) {
                when(veiculoQueryService.buscarPorStatus(status, pageable)).thenReturn(page);

                // When
                ResponseEntity<Page<VeiculoListView>> response = controller.listarPorStatus(status, pageable);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                verify(veiculoQueryService).buscarPorStatus(status, pageable);
            }
        }
    }
}
