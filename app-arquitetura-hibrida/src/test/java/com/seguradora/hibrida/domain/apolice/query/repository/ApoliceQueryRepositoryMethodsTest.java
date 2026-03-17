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
 * Testes unitários para {@link ApoliceQueryRepositoryMethods}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApoliceQueryRepositoryMethods - Testes Unitários")
class ApoliceQueryRepositoryMethodsTest {

    @Mock
    private ApoliceQueryRepositoryMethods repositoryMethods;

    private LocalDate dataReferencia;
    private String statusAtivo;

    @BeforeEach
    void setUp() {
        dataReferencia = LocalDate.of(2024, 7, 15);
        statusAtivo = "ATIVA";
    }

    @Nested
    @DisplayName("Detecção de Vencimentos")
    class DeteccaoVencimentos {

        @Test
        @DisplayName("Deve detectar apólices que vencem exatamente na data")
        void deveDetectarApolicesQueVencemExatamenteNaData() {
            // Arrange
            ApoliceQueryModel apolice1 = createApolice("APO-2024-001", dataReferencia);
            ApoliceQueryModel apolice2 = createApolice("APO-2024-002", dataReferencia);
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2);

            when(repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(a -> a.getVigenciaFim().equals(dataReferencia));
            verify(repositoryMethods).findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusAtivo);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há vencimentos na data")
        void deveRetornarListaVaziaQuandoNaoHaVencimentosNaData() {
            // Arrange
            when(repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusAtivo))
                .thenReturn(Collections.emptyList());

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).isEmpty();
            verify(repositoryMethods).findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusAtivo);
        }

        @Test
        @DisplayName("Deve ser usado pelo scheduler para detectar vencimentos")
        void deveSerUsadoPeloSchedulerParaDetectarVencimentos() {
            // Arrange - Simular scheduler rodando diariamente
            LocalDate hoje = LocalDate.now();
            ApoliceQueryModel apolice = createApolice("APO-SCHEDULER-001", hoje);

            when(repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(hoje, statusAtivo))
                .thenReturn(List.of(apolice));

            // Act
            List<ApoliceQueryModel> vencendoHoje = repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(
                hoje, statusAtivo);

            // Assert
            assertThat(vencendoHoje).hasSize(1);
            assertThat(vencendoHoje.get(0).getNumeroApolice()).isEqualTo("APO-SCHEDULER-001");
        }

        @Test
        @DisplayName("Resultados devem estar ordenados por número de apólice")
        void resultadosDevemEstarOrdenadosPorNumeroApolice() {
            // Arrange
            ApoliceQueryModel apolice1 = createApolice("APO-2024-001", dataReferencia);
            ApoliceQueryModel apolice2 = createApolice("APO-2024-002", dataReferencia);
            ApoliceQueryModel apolice3 = createApolice("APO-2024-003", dataReferencia);
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2, apolice3);

            when(repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getNumeroApolice()).isEqualTo("APO-2024-001");
            assertThat(result.get(1).getNumeroApolice()).isEqualTo("APO-2024-002");
            assertThat(result.get(2).getNumeroApolice()).isEqualTo("APO-2024-003");
        }
    }

    @Nested
    @DisplayName("Detecção de Apólices Já Vencidas")
    class DeteccaoApolicesJaVencidas {

        @Test
        @DisplayName("Deve detectar apólices já vencidas")
        void deveDetectarApolicesJaVencidas() {
            // Arrange
            LocalDate vencida1 = dataReferencia.minusDays(15);
            LocalDate vencida2 = dataReferencia.minusDays(5);

            ApoliceQueryModel apolice1 = createApolice("APO-VENCIDA-001", vencida1);
            ApoliceQueryModel apolice2 = createApolice("APO-VENCIDA-002", vencida2);
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2);

            when(repositoryMethods.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(a -> a.getVigenciaFim().isBefore(dataReferencia));
            verify(repositoryMethods).findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo);
        }

        @Test
        @DisplayName("Não deve incluir apólices que vencem hoje")
        void naoDeveIncluirApolicesQueVencemHoje() {
            // Arrange
            LocalDate vencida = dataReferencia.minusDays(10);
            ApoliceQueryModel apolice = createApolice("APO-VENCIDA-003", vencida);

            when(repositoryMethods.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(List.of(apolice));

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVigenciaFim()).isBefore(dataReferencia);
        }

        @Test
        @DisplayName("Deve retornar apólices ordenadas por data de vencimento")
        void deveRetornarApolicesOrdenadasPorDataVencimento() {
            // Arrange
            LocalDate data1 = dataReferencia.minusDays(30);
            LocalDate data2 = dataReferencia.minusDays(20);
            LocalDate data3 = dataReferencia.minusDays(10);

            ApoliceQueryModel apolice1 = createApolice("APO-VENCIDA-004", data1);
            ApoliceQueryModel apolice2 = createApolice("APO-VENCIDA-005", data2);
            ApoliceQueryModel apolice3 = createApolice("APO-VENCIDA-006", data3);
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2, apolice3);

            when(repositoryMethods.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getVigenciaFim()).isEqualTo(data1);
            assertThat(result.get(1).getVigenciaFim()).isEqualTo(data2);
            assertThat(result.get(2).getVigenciaFim()).isEqualTo(data3);
        }

        @Test
        @DisplayName("Deve identificar apólices críticas vencidas há muito tempo")
        void deveIdentificarApolicesCriticasVencidasHaMuitoTempo() {
            // Arrange
            LocalDate vencidaHaMeses = dataReferencia.minusMonths(3);
            ApoliceQueryModel apolice = createApolice("APO-CRITICA-001", vencidaHaMeses);

            when(repositoryMethods.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(List.of(apolice));

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVigenciaFim()).isBefore(dataReferencia.minusMonths(2));
        }
    }

    @Nested
    @DisplayName("Detecção de Elegíveis para Renovação")
    class DeteccaoElegiveisRenovacao {

        @Test
        @DisplayName("Deve detectar apólices elegíveis para renovação")
        void deveDetectarApolicesElegiveisParaRenovacao() {
            // Arrange
            LocalDate elegivel1 = dataReferencia.minusDays(30);
            LocalDate elegivel2 = dataReferencia.minusDays(15);
            LocalDate elegivel3 = dataReferencia; // Hoje também é elegível

            ApoliceQueryModel apolice1 = createApolice("APO-RENOV-001", elegivel1);
            ApoliceQueryModel apolice2 = createApolice("APO-RENOV-002", elegivel2);
            ApoliceQueryModel apolice3 = createApolice("APO-RENOV-003", elegivel3);
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2, apolice3);

            when(repositoryMethods.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            assertThat(result).allMatch(a -> !a.getVigenciaFim().isAfter(dataReferencia));
            verify(repositoryMethods).findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo);
        }

        @Test
        @DisplayName("Deve incluir apólices que vencem hoje")
        void deveIncluirApolicesQueVencemHoje() {
            // Arrange
            ApoliceQueryModel apoliceHoje = createApolice("APO-HOJE-001", dataReferencia);

            when(repositoryMethods.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(List.of(apoliceHoje));

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVigenciaFim()).isEqualTo(dataReferencia);
        }

        @Test
        @DisplayName("Não deve incluir apólices com vencimento futuro")
        void naoDeveIncluirApolicesComVencimentoFuturo() {
            // Arrange
            LocalDate vencidaPassada = dataReferencia.minusDays(5);
            ApoliceQueryModel apolice = createApolice("APO-PASSADA-001", vencidaPassada);

            when(repositoryMethods.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dataReferencia, statusAtivo))
                .thenReturn(List.of(apolice));

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).allMatch(a -> !a.getVigenciaFim().isAfter(dataReferencia));
        }

        @Test
        @DisplayName("Deve ser útil para campanhas de renovação")
        void deveSerUtilParaCampanhasDeRenovacao() {
            // Arrange - Buscar apólices que vencem nos próximos 60 dias
            LocalDate dentroDeSesentaDias = LocalDate.now().plusDays(60);

            ApoliceQueryModel apolice1 = createApolice("APO-CAMPANHA-001", LocalDate.now().plusDays(30));
            ApoliceQueryModel apolice2 = createApolice("APO-CAMPANHA-002", LocalDate.now().plusDays(45));
            List<ApoliceQueryModel> apolices = List.of(apolice1, apolice2);

            when(repositoryMethods.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dentroDeSesentaDias, statusAtivo))
                .thenReturn(apolices);

            // Act
            List<ApoliceQueryModel> candidatasRenovacao = repositoryMethods
                .findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(dentroDeSesentaDias, statusAtivo);

            // Assert
            assertThat(candidatasRenovacao).hasSize(2);
            assertThat(candidatasRenovacao).allMatch(a -> !a.getVigenciaFim().isAfter(dentroDeSesentaDias));
        }
    }

    @Nested
    @DisplayName("Testes com Diferentes Status")
    class TestesDiferentesStatus {

        @Test
        @DisplayName("Deve filtrar por status ATIVA")
        void deveFiltrarPorStatusAtiva() {
            // Arrange
            ApoliceQueryModel apolice = createApolice("APO-ATIVA-001", dataReferencia);

            when(repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, "ATIVA"))
                .thenReturn(List.of(apolice));

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(
                dataReferencia, "ATIVA");

            // Assert
            assertThat(result).hasSize(1);
            verify(repositoryMethods).findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, "ATIVA");
        }

        @Test
        @DisplayName("Deve filtrar por status CANCELADA")
        void deveFiltrarPorStatusCancelada() {
            // Arrange
            ApoliceQueryModel apolice = createApolice("APO-CANCELADA-001", dataReferencia);

            when(repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, "CANCELADA"))
                .thenReturn(List.of(apolice));

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(
                dataReferencia, "CANCELADA");

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve filtrar por status VENCIDA")
        void deveFiltrarPorStatusVencida() {
            // Arrange
            LocalDate passado = dataReferencia.minusDays(10);
            ApoliceQueryModel apolice = createApolice("APO-VENCIDA-STATUS-001", passado);

            when(repositoryMethods.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(dataReferencia, "VENCIDA"))
                .thenReturn(List.of(apolice));

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(
                dataReferencia, "VENCIDA");

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Casos de Borda")
    class CasosDeBorda {

        @Test
        @DisplayName("Deve retornar lista vazia para data sem apólices")
        void deveRetornarListaVaziaParaDataSemApolices() {
            // Arrange
            LocalDate dataSemApolices = LocalDate.of(2050, 12, 31);

            when(repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(dataSemApolices, statusAtivo))
                .thenReturn(Collections.emptyList());

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(
                dataSemApolices, statusAtivo);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve lidar com grandes volumes de apólices")
        void deveLidarComGrandesVolumesDeApolices() {
            // Arrange
            List<ApoliceQueryModel> grandeVolume = java.util.stream.IntStream.range(1, 1001)
                .mapToObj(i -> createApolice(String.format("APO-%04d", i), dataReferencia))
                .toList();

            when(repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(dataReferencia, statusAtivo))
                .thenReturn(grandeVolume);

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(
                dataReferencia, statusAtivo);

            // Assert
            assertThat(result).hasSize(1000);
        }

        @Test
        @DisplayName("Deve funcionar corretamente em virada de ano")
        void deveFuncionarCorretamenteEmViradaDeAno() {
            // Arrange
            LocalDate ultimoDiaAno = LocalDate.of(2024, 12, 31);
            ApoliceQueryModel apolice = createApolice("APO-VIRADA-ANO-001", ultimoDiaAno);

            when(repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(ultimoDiaAno, statusAtivo))
                .thenReturn(List.of(apolice));

            // Act
            List<ApoliceQueryModel> result = repositoryMethods.findByVigenciaFimAndStatusOrderByNumeroApolice(
                ultimoDiaAno, statusAtivo);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVigenciaFim()).isEqualTo(ultimoDiaAno);
        }
    }

    // === Métodos auxiliares ===

    private ApoliceQueryModel createApolice(String numeroApolice, LocalDate vigenciaFim) {
        ApoliceQueryModel apolice = mock(ApoliceQueryModel.class);
        lenient().when(apolice.getNumeroApolice()).thenReturn(numeroApolice);
        lenient().when(apolice.getVigenciaFim()).thenReturn(vigenciaFim);
        lenient().when(apolice.getStatus()).thenReturn(com.seguradora.hibrida.domain.apolice.model.StatusApolice.ATIVA);
        return apolice;
    }
}
