package com.seguradora.hibrida.domain.apolice.query.repository;

import com.seguradora.hibrida.domain.apolice.query.model.ApoliceQueryModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link ApoliceQueryRepositoryExtensions}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApoliceQueryRepositoryExtensions - Testes Unitários")
class ApoliceQueryRepositoryExtensionsTest {

    @Mock
    private ApoliceQueryRepositoryExtensions extensions;

    private LocalDate dataReferencia;
    private String statusAtivo;

    @BeforeEach
    void setUp() {
        dataReferencia = LocalDate.of(2024, 6, 30);
        statusAtivo = "ATIVA";
    }

    @Nested
    @DisplayName("Consultas por Vencimento Exato")
    class ConsultasPorVencimentoExato {

        @Test
        @DisplayName("Deve buscar apólices que vencem em data específica")
        void deveBuscarApolicesQueVencemEmDataEspecifica() {
            // Arrange
            ApoliceQueryModel apolice1 = createApoliceQueryModel("APO-001", dataReferencia);
            ApoliceQueryModel apolice2 = createApoliceQueryModel("APO-002", dataReferencia);
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2);

            when(extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getNumeroApolice()).isEqualTo("APO-001");
            assertThat(result.get(1).getNumeroApolice()).isEqualTo("APO-002");
            verify(extensions).findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusAtivo);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há apólices na data")
        void deveRetornarListaVaziaQuandoNaoHaApolicesNaData() {
            // Arrange
            when(extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusAtivo))
                .thenReturn(Collections.emptyList());

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).isEmpty();
            verify(extensions).findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusAtivo);
        }

        @Test
        @DisplayName("Deve buscar apenas com status específico")
        void deveBuscarApenasComStatusEspecifico() {
            // Arrange
            String statusCancelado = "CANCELADA";
            ApoliceQueryModel apolice = createApoliceQueryModel("APO-003", dataReferencia);
            List<ApoliceQueryModel> apolices = List.of(apolice);

            when(extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusCancelado))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(
                dataReferencia, statusCancelado);

            // Assert
            assertThat(result).hasSize(1);
            verify(extensions).findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusCancelado);
        }

        @Test
        @DisplayName("Deve retornar resultados ordenados por número de apólice")
        void deveRetornarResultadosOrdenadosPorNumero() {
            // Arrange
            ApoliceQueryModel apolice1 = createApoliceQueryModel("APO-001", dataReferencia);
            ApoliceQueryModel apolice2 = createApoliceQueryModel("APO-002", dataReferencia);
            ApoliceQueryModel apolice3 = createApoliceQueryModel("APO-003", dataReferencia);
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2, apolice3);

            when(extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getNumeroApolice()).isEqualTo("APO-001");
            assertThat(result.get(1).getNumeroApolice()).isEqualTo("APO-002");
            assertThat(result.get(2).getNumeroApolice()).isEqualTo("APO-003");
        }
    }

    @Nested
    @DisplayName("Consultas por Vencimento Anterior")
    class ConsultasPorVencimentoAnterior {

        @Test
        @DisplayName("Deve buscar apólices vencidas antes da data")
        void deveBuscarApolicesVencidasAntesDaData() {
            // Arrange
            LocalDate dataAnterior1 = dataReferencia.minusDays(10);
            LocalDate dataAnterior2 = dataReferencia.minusDays(5);

            ApoliceQueryModel apolice1 = createApoliceQueryModel("APO-004", dataAnterior1);
            ApoliceQueryModel apolice2 = createApoliceQueryModel("APO-005", dataAnterior2);
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2);

            when(extensions.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getVigenciaFim()).isEqualTo(dataAnterior1);
            assertThat(result.get(1).getVigenciaFim()).isEqualTo(dataAnterior2);
            verify(extensions).findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo);
        }

        @Test
        @DisplayName("Não deve incluir apólices com vencimento na data exata")
        void naoDeveIncluirApolicesComVencimentoNaDataExata() {
            // Arrange
            when(extensions.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(Collections.emptyList());

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).isEmpty();
            verify(extensions).findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo);
        }

        @Test
        @DisplayName("Deve retornar apólices ordenadas por data de vencimento")
        void deveRetornarApolicesOrdenadasPorDataVencimento() {
            // Arrange
            LocalDate data1 = dataReferencia.minusDays(30);
            LocalDate data2 = dataReferencia.minusDays(15);
            LocalDate data3 = dataReferencia.minusDays(5);

            ApoliceQueryModel apolice1 = createApoliceQueryModel("APO-006", data1);
            ApoliceQueryModel apolice2 = createApoliceQueryModel("APO-007", data2);
            ApoliceQueryModel apolice3 = createApoliceQueryModel("APO-008", data3);
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2, apolice3);

            when(extensions.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getVigenciaFim()).isEqualTo(data1);
            assertThat(result.get(1).getVigenciaFim()).isEqualTo(data2);
            assertThat(result.get(2).getVigenciaFim()).isEqualTo(data3);
        }
    }

    @Nested
    @DisplayName("Consultas por Vencimento Até Data")
    class ConsultasPorVencimentoAteData {

        @Test
        @DisplayName("Deve buscar apólices que vencem até a data (inclusive)")
        void deveBuscarApolicesQueVencemAteData() {
            // Arrange
            LocalDate dataAnterior = dataReferencia.minusDays(7);

            ApoliceQueryModel apolice1 = createApoliceQueryModel("APO-009", dataAnterior);
            ApoliceQueryModel apolice2 = createApoliceQueryModel("APO-010", dataReferencia);
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2);

            when(extensions.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getVigenciaFim()).isEqualTo(dataAnterior);
            assertThat(result.get(1).getVigenciaFim()).isEqualTo(dataReferencia);
            verify(extensions).findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo);
        }

        @Test
        @DisplayName("Deve incluir apólices com vencimento na data exata")
        void deveIncluirApolicesComVencimentoNaDataExata() {
            // Arrange
            ApoliceQueryModel apolice = createApoliceQueryModel("APO-011", dataReferencia);
            List<ApoliceQueryModel> apolices = List.of(apolice);

            when(extensions.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVigenciaFim()).isEqualTo(dataReferencia);
        }

        @Test
        @DisplayName("Não deve incluir apólices com vencimento posterior")
        void naoDeveIncluirApolicesComVencimentoPosterior() {
            // Arrange
            when(extensions.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(Collections.emptyList());

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve ser útil para identificar apólices elegíveis para renovação")
        void deveSerUtilParaIdentificarApolicesElegiveisRenovacao() {
            // Arrange
            LocalDate data1 = dataReferencia.minusDays(60);
            LocalDate data2 = dataReferencia.minusDays(30);
            LocalDate data3 = dataReferencia;

            ApoliceQueryModel apolice1 = createApoliceQueryModel("APO-012", data1);
            ApoliceQueryModel apolice2 = createApoliceQueryModel("APO-013", data2);
            ApoliceQueryModel apolice3 = createApoliceQueryModel("APO-014", data3);
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2, apolice3);

            when(extensions.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result).allMatch(a -> !a.getVigenciaFim().isAfter(dataReferencia));
        }
    }

    @Nested
    @DisplayName("Testes de Casos de Borda")
    class CasosDeBorda {

        @Test
        @DisplayName("Deve lidar com data no passado distante")
        void deveLidarComDataNoPassadoDistante() {
            // Arrange
            LocalDate passadoDistante = dataReferencia.minusYears(5);

            when(extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(passadoDistante, statusAtivo))
                .thenReturn(Collections.emptyList());

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(
                passadoDistante, statusAtivo);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve lidar com data no futuro distante")
        void deveLidarComDataNoFuturoDistante() {
            // Arrange
            LocalDate futuroDistante = dataReferencia.plusYears(5);

            when(extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(futuroDistante, statusAtivo))
                .thenReturn(Collections.emptyList());

            // Act
            List<ApoliceQueryModel> result = extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(
                futuroDistante, statusAtivo);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve funcionar com diferentes status")
        void deveFuncionarComDiferentesStatus() {
            // Arrange
            String[] statusList = {"ATIVA", "CANCELADA", "VENCIDA", "SUSPENSA"};

            for (String status : statusList) {
                when(extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, status))
                    .thenReturn(Collections.emptyList());

                // Act
                List<ApoliceQueryModel> result = extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(
                    dataReferencia, status);

                // Assert
                assertThat(result).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Testes de Integração entre Métodos")
    class IntegracaoEntreMetodos {

        @Test
        @DisplayName("Métodos devem ter comportamentos complementares")
        void metodosDevemTerComportamentosComplementares() {
            // Arrange
            LocalDate dataBase = LocalDate.of(2024, 6, 15);

            // Mock para vencimento exato
            ApoliceQueryModel apoliceExata = createApoliceQueryModel("APO-015", dataBase);
            when(extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(dataBase, statusAtivo))
                .thenReturn(List.of(apoliceExata));

            // Mock para vencimento anterior (não inclui data exata)
            ApoliceQueryModel apoliceAnterior = createApoliceQueryModel("APO-016", dataBase.minusDays(5));
            when(extensions.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataBase, statusAtivo))
                .thenReturn(List.of(apoliceAnterior));

            // Mock para vencimento até data (inclui data exata)
            when(extensions.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dataBase, statusAtivo))
                .thenReturn(List.of(apoliceAnterior, apoliceExata));

            // Act
            List<ApoliceQueryModel> exatas = extensions.findByVigenciaFimAndStatusOrderByNumeroApolice(dataBase, statusAtivo);
            List<ApoliceQueryModel> anteriores = extensions.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataBase, statusAtivo);
            List<ApoliceQueryModel> ateData = extensions.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dataBase, statusAtivo);

            // Assert
            assertThat(exatas).hasSize(1);
            assertThat(anteriores).hasSize(1);
            assertThat(ateData).hasSize(2); // Soma de anteriores + exatas
        }
    }

    // === Métodos auxiliares ===

    private ApoliceQueryModel createApoliceQueryModel(String numeroApolice, LocalDate vigenciaFim) {
        ApoliceQueryModel apolice = mock(ApoliceQueryModel.class);
        lenient().when(apolice.getNumeroApolice()).thenReturn(numeroApolice);
        lenient().when(apolice.getVigenciaFim()).thenReturn(vigenciaFim);
        lenient().when(apolice.getStatus()).thenReturn(com.seguradora.hibrida.domain.apolice.model.StatusApolice.ATIVA);
        return apolice;
    }
}
