package com.seguradora.hibrida.domain.veiculo.query.service;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoDetailView;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoListView;
import com.seguradora.hibrida.domain.veiculo.query.model.VeiculoQueryModel;
import com.seguradora.hibrida.domain.veiculo.query.repository.VeiculoQueryRepository;
import com.seguradora.hibrida.domain.veiculo.query.repository.VeiculoQueryRepositoryImpl;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VeiculoQueryServiceImpl}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoQueryServiceImpl - Testes Unitários")
class VeiculoQueryServiceImplTest {

    @Mock
    private VeiculoQueryRepository veiculoRepository;

    @Mock
    private VeiculoQueryRepositoryImpl veiculoRepositoryImpl;

    @InjectMocks
    private VeiculoQueryServiceImpl service;

    private VeiculoQueryModel veiculoMock;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        veiculoMock = new VeiculoQueryModel();
        veiculoMock.setId("VEI-001");
        veiculoMock.setPlaca("ABC1234");
        veiculoMock.setRenavam("12345678900");
        veiculoMock.setChassi("1HGBH41J6MN109186");
        veiculoMock.setMarca("Honda");
        veiculoMock.setModelo("Civic");
        veiculoMock.setAnoFabricacao(2020);
        veiculoMock.setAnoModelo(2021);
        veiculoMock.setCor("Branco");
        veiculoMock.setTipoCombustivel("FLEX");
        veiculoMock.setCategoria("PASSEIO");
        veiculoMock.setCilindrada(1600);
        veiculoMock.setProprietarioCpf("12345678909");
        veiculoMock.setProprietarioNome("João Silva");
        veiculoMock.setProprietarioTipo("FISICA");
        veiculoMock.setStatus(StatusVeiculo.ATIVO);
        veiculoMock.setApoliceAtiva(true);
        veiculoMock.setVersion(1L);
        veiculoMock.setCreatedAt(Instant.now());
        veiculoMock.setUpdatedAt(Instant.now());

        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Testes de Consultas Básicas")
    class ConsultasBasicasTests {

        @Test
        @DisplayName("Deve buscar veículo por ID")
        void deveBuscarVeiculoPorId() {
            when(veiculoRepository.findById("VEI-001")).thenReturn(Optional.of(veiculoMock));

            Optional<VeiculoDetailView> result = service.buscarPorId("VEI-001");

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo("VEI-001");
            verify(veiculoRepository).findById("VEI-001");
        }

        @Test
        @DisplayName("Deve buscar veículo por placa")
        void deveBuscarVeiculoPorPlaca() {
            when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.of(veiculoMock));

            Optional<VeiculoDetailView> result = service.buscarPorPlaca("ABC1234");

            assertThat(result).isPresent();
            assertThat(result.get().placa()).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("Deve buscar veículo por RENAVAM")
        void deveBuscarVeiculoPorRenavam() {
            when(veiculoRepository.findByRenavam("12345678900")).thenReturn(Optional.of(veiculoMock));

            Optional<VeiculoDetailView> result = service.buscarPorRenavam("12345678900");

            assertThat(result).isPresent();
            assertThat(result.get().renavam()).isEqualTo("12345678900");
        }

        @Test
        @DisplayName("Deve buscar veículo por chassi")
        void deveBuscarVeiculoPorChassi() {
            when(veiculoRepository.findByChassi("1HGBH41J6MN109186")).thenReturn(Optional.of(veiculoMock));

            Optional<VeiculoDetailView> result = service.buscarPorChassi("1HGBH41J6MN109186");

            assertThat(result).isPresent();
            assertThat(result.get().chassi()).isEqualTo("1HGBH41J6MN109186");
        }

        @Test
        @DisplayName("Deve listar todos os veículos com paginação")
        void deveListarTodosVeiculosComPaginacao() {
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(veiculoMock));
            when(veiculoRepository.findAll(any(Pageable.class))).thenReturn(page);

            Page<VeiculoListView> result = service.listarTodos(pageable);

            assertThat(result).isNotEmpty();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar empty quando não encontrar")
        void deveRetornarEmptyQuandoNaoEncontrar() {
            when(veiculoRepository.findById("VEI-999")).thenReturn(Optional.empty());

            Optional<VeiculoDetailView> result = service.buscarPorId("VEI-999");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Consultas por Proprietário")
    class ConsultasProprietarioTests {

        @Test
        @DisplayName("Deve buscar veículos por CPF do proprietário")
        void deveBuscarVeiculosPorCpfProprietario() {
            when(veiculoRepository.findByProprietarioCpf("12345678909"))
                .thenReturn(List.of(veiculoMock));

            List<VeiculoListView> result = service.buscarPorProprietarioCpf("12345678909");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).proprietarioCpf()).isEqualTo("12345678909");
        }

        @Test
        @DisplayName("Deve buscar veículos por nome do proprietário")
        void deveBuscarVeiculosPorNomeProprietario() {
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(veiculoMock));
            when(veiculoRepositoryImpl.findByProprietarioNomeContainingIgnoreCase(anyString(), any()))
                .thenReturn(page);

            Page<VeiculoListView> result = service.buscarPorProprietarioNome("João", pageable);

            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Consultas por Status")
    class ConsultasStatusTests {

        @Test
        @DisplayName("Deve buscar veículos por status")
        void deveBuscarVeiculosPorStatus() {
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(veiculoMock));
            when(veiculoRepository.findByStatus(StatusVeiculo.ATIVO, pageable)).thenReturn(page);

            Page<VeiculoListView> result = service.buscarPorStatus(StatusVeiculo.ATIVO, pageable);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Deve buscar veículos ativos")
        void deveBuscarVeiculosAtivos() {
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(veiculoMock));
            when(veiculoRepository.findByStatus(StatusVeiculo.ATIVO, pageable)).thenReturn(page);

            Page<VeiculoListView> result = service.buscarAtivos(pageable);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Deve buscar veículos com apólice ativa")
        void deveBuscarVeiculosComApoliceAtiva() {
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(veiculoMock));
            when(veiculoRepository.findByApoliceAtivaTrue(pageable)).thenReturn(page);

            Page<VeiculoListView> result = service.buscarComApoliceAtiva(pageable);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Deve buscar veículos sem apólice ativa")
        void deveBuscarVeiculosSemApoliceAtiva() {
            veiculoMock.setApoliceAtiva(false);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(veiculoMock));
            when(veiculoRepository.findByApoliceAtivaFalse(pageable)).thenReturn(page);

            Page<VeiculoListView> result = service.buscarSemApoliceAtiva(pageable);

            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Verificações")
    class VerificacoesTests {

        @Test
        @DisplayName("Deve verificar se existe veículo com placa")
        void deveVerificarSeExisteVeiculoComPlaca() {
            when(veiculoRepository.existsByPlaca("ABC1234")).thenReturn(true);

            boolean result = service.existeComPlaca("ABC1234");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Deve verificar se existe veículo com RENAVAM")
        void deveVerificarSeExisteVeiculoComRenavam() {
            when(veiculoRepository.existsByRenavam("12345678900")).thenReturn(true);

            boolean result = service.existeComRenavam("12345678900");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Deve verificar se existe veículo com chassi")
        void deveVerificarSeExisteVeiculoComChassi() {
            when(veiculoRepository.existsByChassi("1HGBH41J6MN109186")).thenReturn(true);

            boolean result = service.existeComChassi("1HGBH41J6MN109186");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Deve contar veículos por proprietário e status")
        void deveContarVeiculosPorProprietarioEStatus() {
            when(veiculoRepository.countByProprietarioCpfAndStatus("12345678909", StatusVeiculo.ATIVO))
                .thenReturn(3L);

            long result = service.contarPorProprietarioEStatus("12345678909", StatusVeiculo.ATIVO);

            assertThat(result).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("Testes de Estatísticas")
    class EstatisticasTests {

        @Test
        @DisplayName("Deve obter estatísticas gerais")
        void deveObterEstatisticasGerais() {
            when(veiculoRepository.count()).thenReturn(100L);
            when(veiculoRepositoryImpl.countByStatus(StatusVeiculo.ATIVO)).thenReturn(80L);
            when(veiculoRepositoryImpl.countByApoliceAtivaTrue()).thenReturn(75L);

            List<Object[]> listaMarcas = new java.util.ArrayList<>();
            listaMarcas.add(new Object[]{"Honda", 50L});
            when(veiculoRepositoryImpl.countByMarca()).thenReturn(listaMarcas);

            List<Object[]> listaEstados = new java.util.ArrayList<>();
            listaEstados.add(new Object[]{"SP", 60L});
            when(veiculoRepositoryImpl.countByEstado()).thenReturn(listaEstados);

            VeiculoQueryService.VeiculoStatistics stats = service.obterEstatisticas();

            assertThat(stats.totalVeiculos()).isEqualTo(100L);
            assertThat(stats.veiculosAtivos()).isEqualTo(80L);
            assertThat(stats.veiculosComApolice()).isEqualTo(75L);
            assertThat(stats.veiculosSemApolice()).isEqualTo(25L);
            assertThat(stats.marcaMaisComum()).isEqualTo("Honda");
            assertThat(stats.estadoMaisComum()).isEqualTo("SP");
        }

        @Test
        @DisplayName("Deve obter estatísticas por estado")
        void deveObterEstatisticasPorEstado() {
            List<Object[]> listaEstados = new java.util.ArrayList<>();
            listaEstados.add(new Object[]{"SP", 60L});
            listaEstados.add(new Object[]{"RJ", 30L});
            when(veiculoRepositoryImpl.countByEstado()).thenReturn(listaEstados);

            List<Object[]> result = service.obterEstatisticasPorEstado();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Deve obter estatísticas por marca")
        void deveObterEstatisticasPorMarca() {
            List<Object[]> listaMarcas = new java.util.ArrayList<>();
            listaMarcas.add(new Object[]{"Honda", 50L});
            listaMarcas.add(new Object[]{"Toyota", 30L});
            when(veiculoRepositoryImpl.countByMarca()).thenReturn(listaMarcas);

            List<Object[]> result = service.obterEstatisticasPorMarca();

            assertThat(result).hasSize(2);
        }
    }
}
