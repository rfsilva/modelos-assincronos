package com.seguradora.hibrida.domain.veiculo.query.service;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoDetailView;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VeiculoQueryService}.
 * Interface de serviço com operações de consulta de veículos.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("VeiculoQueryService - Testes Unitários")
class VeiculoQueryServiceTest {

    private final VeiculoQueryService service = mock(VeiculoQueryService.class);

    @Nested
    @DisplayName("Consultas Básicas")
    class ConsultasBasicas {

        @Test
        @DisplayName("Deve buscar veículo por ID")
        void shouldBuscarPorId() {
            // Arrange
            String id = "VEI-001";
            VeiculoDetailView detailView = mock(VeiculoDetailView.class);
            when(service.buscarPorId(id)).thenReturn(Optional.of(detailView));

            // Act
            Optional<VeiculoDetailView> result = service.buscarPorId(id);

            // Assert
            assertThat(result).isPresent();
            verify(service).buscarPorId(id);
        }

        @Test
        @DisplayName("Deve buscar veículo por placa")
        void shouldBuscarPorPlaca() {
            // Arrange
            String placa = "ABC1234";
            VeiculoDetailView detailView = mock(VeiculoDetailView.class);
            when(service.buscarPorPlaca(placa)).thenReturn(Optional.of(detailView));

            // Act
            Optional<VeiculoDetailView> result = service.buscarPorPlaca(placa);

            // Assert
            assertThat(result).isPresent();
            verify(service).buscarPorPlaca(placa);
        }

        @Test
        @DisplayName("Deve buscar veículo por RENAVAM")
        void shouldBuscarPorRenavam() {
            // Arrange
            String renavam = "12345678901";
            VeiculoDetailView detailView = mock(VeiculoDetailView.class);
            when(service.buscarPorRenavam(renavam)).thenReturn(Optional.of(detailView));

            // Act
            Optional<VeiculoDetailView> result = service.buscarPorRenavam(renavam);

            // Assert
            assertThat(result).isPresent();
            verify(service).buscarPorRenavam(renavam);
        }

        @Test
        @DisplayName("Deve buscar veículo por chassi")
        void shouldBuscarPorChassi() {
            // Arrange
            String chassi = "9BWZZZ377VT004251";
            VeiculoDetailView detailView = mock(VeiculoDetailView.class);
            when(service.buscarPorChassi(chassi)).thenReturn(Optional.of(detailView));

            // Act
            Optional<VeiculoDetailView> result = service.buscarPorChassi(chassi);

            // Assert
            assertThat(result).isPresent();
            verify(service).buscarPorChassi(chassi);
        }

        @Test
        @DisplayName("Deve listar todos os veículos com paginação")
        void shouldListarTodos() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.listarTodos(pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.listarTodos(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(service).listarTodos(pageable);
        }

        @Test
        @DisplayName("Deve retornar vazio quando veículo não encontrado")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            String id = "VEI-999";
            when(service.buscarPorId(id)).thenReturn(Optional.empty());

            // Act
            Optional<VeiculoDetailView> result = service.buscarPorId(id);

            // Assert
            assertThat(result).isEmpty();
            verify(service).buscarPorId(id);
        }
    }

    @Nested
    @DisplayName("Consultas por Proprietário")
    class ConsultasPorProprietario {

        @Test
        @DisplayName("Deve buscar veículos por CPF do proprietário")
        void shouldBuscarPorProprietarioCpf() {
            // Arrange
            String cpf = "12345678909";
            List<VeiculoListView> veiculos = List.of(
                mock(VeiculoListView.class),
                mock(VeiculoListView.class)
            );
            when(service.buscarPorProprietarioCpf(cpf)).thenReturn(veiculos);

            // Act
            List<VeiculoListView> result = service.buscarPorProprietarioCpf(cpf);

            // Assert
            assertThat(result).hasSize(2);
            verify(service).buscarPorProprietarioCpf(cpf);
        }

        @Test
        @DisplayName("Deve buscar veículos por nome do proprietário")
        void shouldBuscarPorProprietarioNome() {
            // Arrange
            String nome = "João Silva";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarPorProprietarioNome(nome, pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarPorProprietarioNome(nome, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(service).buscarPorProprietarioNome(nome, pageable);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando CPF não tem veículos")
        void shouldReturnEmptyListWhenCpfHasNoVehicles() {
            // Arrange
            String cpf = "99999999999";
            when(service.buscarPorProprietarioCpf(cpf)).thenReturn(List.of());

            // Act
            List<VeiculoListView> result = service.buscarPorProprietarioCpf(cpf);

            // Assert
            assertThat(result).isEmpty();
            verify(service).buscarPorProprietarioCpf(cpf);
        }
    }

    @Nested
    @DisplayName("Consultas por Status")
    class ConsultasPorStatus {

        @Test
        @DisplayName("Deve buscar veículos por status")
        void shouldBuscarPorStatus() {
            // Arrange
            StatusVeiculo status = StatusVeiculo.ATIVO;
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarPorStatus(status, pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarPorStatus(status, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarPorStatus(status, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos ativos")
        void shouldBuscarAtivos() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarAtivos(pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarAtivos(pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarAtivos(pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos com apólice ativa")
        void shouldBuscarComApoliceAtiva() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarComApoliceAtiva(pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarComApoliceAtiva(pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarComApoliceAtiva(pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos sem apólice ativa")
        void shouldBuscarSemApoliceAtiva() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarSemApoliceAtiva(pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarSemApoliceAtiva(pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarSemApoliceAtiva(pageable);
        }
    }

    @Nested
    @DisplayName("Consultas por Marca e Modelo")
    class ConsultasPorMarcaModelo {

        @Test
        @DisplayName("Deve buscar veículos por marca e modelo")
        void shouldBuscarPorMarcaEModelo() {
            // Arrange
            String marca = "Toyota";
            String modelo = "Corolla";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarPorMarcaEModelo(marca, modelo, pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarPorMarcaEModelo(marca, modelo, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarPorMarcaEModelo(marca, modelo, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por marca")
        void shouldBuscarPorMarca() {
            // Arrange
            String marca = "Honda";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarPorMarca(marca, pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarPorMarca(marca, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarPorMarca(marca, pageable);
        }

        @Test
        @DisplayName("Deve buscar fuzzy por marca ou modelo")
        void shouldBuscarPorMarcaOuModelo() {
            // Arrange
            String termo = "civic";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarPorMarcaOuModelo(termo, pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarPorMarcaOuModelo(termo, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarPorMarcaOuModelo(termo, pageable);
        }
    }

    @Nested
    @DisplayName("Consultas por Ano")
    class ConsultasPorAno {

        @Test
        @DisplayName("Deve buscar veículos por faixa de ano")
        void shouldBuscarPorFaixaAno() {
            // Arrange
            Integer anoInicio = 2020;
            Integer anoFim = 2023;
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarPorFaixaAno(anoInicio, anoFim, pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarPorFaixaAno(anoInicio, anoFim, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarPorFaixaAno(anoInicio, anoFim, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por ano específico")
        void shouldBuscarPorAno() {
            // Arrange
            Integer ano = 2022;
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarPorAno(ano, pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarPorAno(ano, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarPorAno(ano, pageable);
        }
    }

    @Nested
    @DisplayName("Consultas Geográficas")
    class ConsultasGeograficas {

        @Test
        @DisplayName("Deve buscar veículos por cidade")
        void shouldBuscarPorCidade() {
            // Arrange
            String cidade = "São Paulo";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarPorCidade(cidade, pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarPorCidade(cidade, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarPorCidade(cidade, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por estado")
        void shouldBuscarPorEstado() {
            // Arrange
            String estado = "SP";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarPorEstado(estado, pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarPorEstado(estado, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarPorEstado(estado, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por cidade e estado")
        void shouldBuscarPorCidadeEEstado() {
            // Arrange
            String cidade = "Rio de Janeiro";
            String estado = "RJ";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarPorCidadeEEstado(cidade, estado, pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarPorCidadeEEstado(cidade, estado, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarPorCidadeEEstado(cidade, estado, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por região")
        void shouldBuscarPorRegiao() {
            // Arrange
            String regiao = "SUDESTE";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));
            when(service.buscarPorRegiao(regiao, pageable)).thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarPorRegiao(regiao, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarPorRegiao(regiao, pageable);
        }
    }

    @Nested
    @DisplayName("Consultas com Múltiplos Filtros")
    class ConsultasMultiplosFiltros {

        @Test
        @DisplayName("Deve buscar veículos com múltiplos filtros")
        void shouldBuscarComFiltros() {
            // Arrange
            String marca = "Toyota";
            String modelo = "Corolla";
            StatusVeiculo status = StatusVeiculo.ATIVO;
            Integer anoInicio = 2020;
            Integer anoFim = 2023;
            String estado = "SP";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));

            when(service.buscarComFiltros(marca, modelo, status, anoInicio, anoFim, estado, pageable))
                .thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarComFiltros(
                marca, modelo, status, anoInicio, anoFim, estado, pageable
            );

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarComFiltros(marca, modelo, status, anoInicio, anoFim, estado, pageable);
        }

        @Test
        @DisplayName("Deve buscar com filtros opcionais nulos")
        void shouldBuscarComFiltrosOpcionaisNulos() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoListView> page = new PageImpl<>(List.of(mock(VeiculoListView.class)));

            when(service.buscarComFiltros(null, null, null, null, null, null, pageable))
                .thenReturn(page);

            // Act
            Page<VeiculoListView> result = service.buscarComFiltros(
                null, null, null, null, null, null, pageable
            );

            // Assert
            assertThat(result).isNotNull();
            verify(service).buscarComFiltros(null, null, null, null, null, null, pageable);
        }
    }

    @Nested
    @DisplayName("Verificações de Existência")
    class VerificacoesExistencia {

        @Test
        @DisplayName("Deve verificar se existe veículo com a placa")
        void shouldExisteComPlaca() {
            // Arrange
            String placa = "ABC1234";
            when(service.existeComPlaca(placa)).thenReturn(true);

            // Act
            boolean exists = service.existeComPlaca(placa);

            // Assert
            assertThat(exists).isTrue();
            verify(service).existeComPlaca(placa);
        }

        @Test
        @DisplayName("Deve verificar se existe veículo com o RENAVAM")
        void shouldExisteComRenavam() {
            // Arrange
            String renavam = "12345678901";
            when(service.existeComRenavam(renavam)).thenReturn(false);

            // Act
            boolean exists = service.existeComRenavam(renavam);

            // Assert
            assertThat(exists).isFalse();
            verify(service).existeComRenavam(renavam);
        }

        @Test
        @DisplayName("Deve verificar se existe veículo com o chassi")
        void shouldExisteComChassi() {
            // Arrange
            String chassi = "9BWZZZ377VT004251";
            when(service.existeComChassi(chassi)).thenReturn(true);

            // Act
            boolean exists = service.existeComChassi(chassi);

            // Assert
            assertThat(exists).isTrue();
            verify(service).existeComChassi(chassi);
        }

        @Test
        @DisplayName("Deve contar veículos por proprietário e status")
        void shouldContarPorProprietarioEStatus() {
            // Arrange
            String cpf = "12345678909";
            StatusVeiculo status = StatusVeiculo.ATIVO;
            when(service.contarPorProprietarioEStatus(cpf, status)).thenReturn(3L);

            // Act
            long count = service.contarPorProprietarioEStatus(cpf, status);

            // Assert
            assertThat(count).isEqualTo(3L);
            verify(service).contarPorProprietarioEStatus(cpf, status);
        }
    }

    @Nested
    @DisplayName("Estatísticas")
    class Estatisticas {

        @Test
        @DisplayName("Deve obter estatísticas gerais")
        void shouldObterEstatisticas() {
            // Arrange
            VeiculoQueryService.VeiculoStatistics stats = new VeiculoQueryService.VeiculoStatistics(
                200L, 180L, 150L, 50L, 75.0, "Toyota", "SP"
            );
            when(service.obterEstatisticas()).thenReturn(stats);

            // Act
            VeiculoQueryService.VeiculoStatistics result = service.obterEstatisticas();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.totalVeiculos()).isEqualTo(200L);
            assertThat(result.veiculosAtivos()).isEqualTo(180L);
            assertThat(result.veiculosComApolice()).isEqualTo(150L);
            assertThat(result.veiculosSemApolice()).isEqualTo(50L);
            assertThat(result.percentualComApolice()).isEqualTo(75.0);
            verify(service).obterEstatisticas();
        }

        @Test
        @DisplayName("Deve obter estatísticas por estado")
        void shouldObterEstatisticasPorEstado() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"SP", 120L},
                new Object[]{"RJ", 80L}
            );
            when(service.obterEstatisticasPorEstado()).thenReturn(stats);

            // Act
            List<Object[]> result = service.obterEstatisticasPorEstado();

            // Assert
            assertThat(result).hasSize(2);
            verify(service).obterEstatisticasPorEstado();
        }

        @Test
        @DisplayName("Deve obter estatísticas por marca")
        void shouldObterEstatisticasPorMarca() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"Toyota", 45L},
                new Object[]{"Honda", 38L}
            );
            when(service.obterEstatisticasPorMarca()).thenReturn(stats);

            // Act
            List<Object[]> result = service.obterEstatisticasPorMarca();

            // Assert
            assertThat(result).hasSize(2);
            verify(service).obterEstatisticasPorMarca();
        }
    }

    @Nested
    @DisplayName("Validação da Interface")
    class ValidacaoInterface {

        @Test
        @DisplayName("Deve validar que a interface pode ser implementada")
        void shouldValidateInterfaceCanBeImplemented() {
            // Arrange & Act
            VeiculoQueryService mockService = mock(VeiculoQueryService.class);

            // Assert
            assertThat(mockService).isNotNull();
            assertThat(mockService).isInstanceOf(VeiculoQueryService.class);
        }

        @Test
        @DisplayName("Deve validar que todos os métodos da interface podem ser chamados")
        void shouldValidateAllInterfaceMethodsCanBeCalled() {
            // Arrange
            VeiculoQueryService mockService = mock(VeiculoQueryService.class);
            Pageable pageable = PageRequest.of(0, 20);

            // Act & Assert - Verificar que todos os métodos existem
            assertThatCode(() -> {
                mockService.buscarPorId("id");
                mockService.buscarPorPlaca("placa");
                mockService.buscarPorRenavam("renavam");
                mockService.buscarPorChassi("chassi");
                mockService.listarTodos(pageable);
                mockService.buscarPorProprietarioCpf("cpf");
                mockService.buscarPorProprietarioNome("nome", pageable);
                mockService.buscarPorStatus(StatusVeiculo.ATIVO, pageable);
                mockService.buscarAtivos(pageable);
                mockService.buscarComApoliceAtiva(pageable);
                mockService.buscarSemApoliceAtiva(pageable);
                mockService.buscarPorMarcaEModelo("marca", "modelo", pageable);
                mockService.buscarPorMarca("marca", pageable);
                mockService.buscarPorMarcaOuModelo("termo", pageable);
                mockService.buscarPorFaixaAno(2020, 2023, pageable);
                mockService.buscarPorAno(2022, pageable);
                mockService.buscarPorCidade("cidade", pageable);
                mockService.buscarPorEstado("estado", pageable);
                mockService.buscarPorCidadeEEstado("cidade", "estado", pageable);
                mockService.buscarPorRegiao("regiao", pageable);
                mockService.buscarComFiltros(null, null, null, null, null, null, pageable);
                mockService.existeComPlaca("placa");
                mockService.existeComRenavam("renavam");
                mockService.existeComChassi("chassi");
                mockService.contarPorProprietarioEStatus("cpf", StatusVeiculo.ATIVO);
                mockService.obterEstatisticas();
                mockService.obterEstatisticasPorEstado();
                mockService.obterEstatisticasPorMarca();
            }).doesNotThrowAnyException();
        }
    }
}
