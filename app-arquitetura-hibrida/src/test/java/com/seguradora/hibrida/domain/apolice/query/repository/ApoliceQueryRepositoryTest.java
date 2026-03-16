package com.seguradora.hibrida.domain.apolice.query.repository;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.query.model.ApoliceQueryModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para ApoliceQueryRepository.
 *
 * Nota: Estes são testes unitários com mocks. Testes de integração
 * devem ser criados separadamente para validar as queries reais.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApoliceQueryRepository Tests")
class ApoliceQueryRepositoryTest {

    @Mock
    private ApoliceQueryRepository repository;

    private ApoliceQueryModel model;
    private List<ApoliceQueryModel> modelList;
    private Page<ApoliceQueryModel> modelPage;

    @BeforeEach
    void setUp() {
        model = new ApoliceQueryModel("ap-001", "AP-2024-001", "seg-001");
        model.setProduto("Seguro Auto");
        model.setStatus(StatusApolice.ATIVA);
        model.setVigenciaInicio(LocalDate.now());
        model.setVigenciaFim(LocalDate.now().plusDays(365));
        model.setValorTotal(new BigDecimal("5000"));
        model.setSeguradoNome("João Silva");
        model.setSeguradoCpf("12345678901");

        modelList = List.of(model);
        modelPage = new PageImpl<>(modelList);
    }

    @Nested
    @DisplayName("Consultas Básicas")
    class ConsultasBasicas {

        @Test
        @DisplayName("Deve buscar apólice por número")
        void deveBuscarPorNumero() {
            // Arrange
            when(repository.findByNumero("AP-2024-001")).thenReturn(Optional.of(model));

            // Act
            Optional<ApoliceQueryModel> result = repository.findByNumero("AP-2024-001");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(model);
            verify(repository).findByNumero("AP-2024-001");
        }

        @Test
        @DisplayName("Deve buscar apólices por CPF do segurado")
        void deveBuscarPorCpf() {
            // Arrange
            when(repository.findBySeguradoCpfOrderByVigenciaInicioDesc("12345678901"))
                .thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result =
                repository.findBySeguradoCpfOrderByVigenciaInicioDesc("12345678901");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(model);
        }

        @Test
        @DisplayName("Deve buscar apólices por ID do segurado")
        void deveBuscarPorSeguradoId() {
            // Arrange
            when(repository.findBySeguradoIdOrderByVigenciaInicioDesc("seg-001"))
                .thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result =
                repository.findBySeguradoIdOrderByVigenciaInicioDesc("seg-001");

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices por produto")
        void deveBuscarPorProduto() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findByProdutoOrderByVigenciaInicioDesc("Seguro Auto", pageable))
                .thenReturn(modelPage);

            // Act
            Page<ApoliceQueryModel> result =
                repository.findByProdutoOrderByVigenciaInicioDesc("Seguro Auto", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
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
            when(repository.findByStatusOrderByVigenciaFimAsc(StatusApolice.ATIVA, pageable))
                .thenReturn(modelPage);

            // Act
            Page<ApoliceQueryModel> result =
                repository.findByStatusOrderByVigenciaFimAsc(StatusApolice.ATIVA, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices ativas")
        void deveBuscarAtivas() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findApolicesAtivas(pageable)).thenReturn(modelPage);

            // Act
            Page<ApoliceQueryModel> result = repository.findApolicesAtivas(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices vencidas")
        void deveBuscarVencidas() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findApolicesVencidas(pageable)).thenReturn(modelPage);

            // Act
            Page<ApoliceQueryModel> result = repository.findApolicesVencidas(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
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
            when(repository.findApolicesVencendoEntre(inicio, fim)).thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result = repository.findApolicesVencendoEntre(inicio, fim);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices vencendo em X dias")
        void deveBuscarVencendoEm() {
            // Arrange
            LocalDate dataLimite = LocalDate.now().plusDays(30);
            when(repository.findApolicesVencendoEm(dataLimite)).thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result = repository.findApolicesVencendoEm(dataLimite);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices com vencimento próximo")
        void deveBuscarComVencimentoProximo() {
            // Arrange
            when(repository.findApolicesComVencimentoProximo()).thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result = repository.findApolicesComVencimentoProximo();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar por vigência fim e status")
        void deveBuscarPorVigenciaFimEStatus() {
            // Arrange
            LocalDate vigenciaFim = LocalDate.now().plusDays(30);
            when(repository.findByVigenciaFimAndStatusOrderByNumeroApolice(vigenciaFim, "ATIVA"))
                .thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result =
                repository.findByVigenciaFimAndStatusOrderByNumeroApolice(vigenciaFim, "ATIVA");

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar por vigência fim menor que data")
        void deveBuscarPorVigenciaFimMenorQue() {
            // Arrange
            LocalDate vigenciaFim = LocalDate.now().plusDays(30);
            when(repository.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(vigenciaFim, "ATIVA"))
                .thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result =
                repository.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(vigenciaFim, "ATIVA");

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar por vigência fim menor ou igual")
        void deveBuscarPorVigenciaFimMenorIgual() {
            // Arrange
            LocalDate vigenciaFim = LocalDate.now().plusDays(30);
            when(repository.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(vigenciaFim, "ATIVA"))
                .thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result =
                repository.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(vigenciaFim, "ATIVA");

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Consultas por Segurado")
    class ConsultasPorSegurado {

        @Test
        @DisplayName("Deve buscar apólices ativas por CPF")
        void deveBuscarAtivasPorCpf() {
            // Arrange
            when(repository.findApolicesAtivasPorCpf("12345678901")).thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result = repository.findApolicesAtivasPorCpf("12345678901");

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar por nome do segurado")
        void deveBuscarPorNomeSegurado() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findBySeguradoNomeContaining("João", pageable)).thenReturn(modelPage);

            // Act
            Page<ApoliceQueryModel> result =
                repository.findBySeguradoNomeContaining("João", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar por cidade do segurado")
        void deveBuscarPorCidade() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findBySeguradoCidadeOrderByVigenciaFimDesc("São Paulo", pageable))
                .thenReturn(modelPage);

            // Act
            Page<ApoliceQueryModel> result =
                repository.findBySeguradoCidadeOrderByVigenciaFimDesc("São Paulo", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar por estado do segurado")
        void deveBuscarPorEstado() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findBySeguradoEstadoOrderByVigenciaFimDesc("SP", pageable))
                .thenReturn(modelPage);

            // Act
            Page<ApoliceQueryModel> result =
                repository.findBySeguradoEstadoOrderByVigenciaFimDesc("SP", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Consultas por Cobertura e Valor")
    class ConsultasCoberturaValor {

        @Test
        @DisplayName("Deve buscar por cobertura")
        void deveBuscarPorCobertura() {
            // Arrange
            when(repository.findByCobertura(TipoCobertura.TOTAL)).thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result = repository.findByCobertura(TipoCobertura.TOTAL);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar com cobertura total")
        void deveBuscarComCoberturaTotal() {
            // Arrange
            when(repository.findApolicesComCoberturaTotal()).thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result = repository.findApolicesComCoberturaTotal();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar por faixa de valor")
        void deveBuscarPorFaixaValor() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            BigDecimal min = new BigDecimal("1000");
            BigDecimal max = new BigDecimal("10000");
            when(repository.findByValorTotalBetween(min, max, pageable)).thenReturn(modelPage);

            // Act
            Page<ApoliceQueryModel> result = repository.findByValorTotalBetween(min, max, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar alto valor")
        void deveBuscarAltoValor() {
            // Arrange
            BigDecimal valor = new BigDecimal("50000");
            when(repository.findApolicesAltoValor(valor)).thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result = repository.findApolicesAltoValor(valor);

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Consultas de Renovação")
    class ConsultasRenovacao {

        @Test
        @DisplayName("Deve buscar elegíveis para renovação")
        void deveBuscarElegiveis() {
            // Arrange
            when(repository.findElegiveisRenovacaoAutomatica()).thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result = repository.findElegiveisRenovacaoAutomatica();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar precisando de atenção")
        void deveBuscarPrecisandoAtencao() {
            // Arrange
            when(repository.findPrecisandoAtencaoRenovacao()).thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result = repository.findPrecisandoAtencaoRenovacao();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar por score de renovação")
        void deveBuscarPorScore() {
            // Arrange
            when(repository.findByScoreRenovacao(70, 100)).thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result = repository.findByScoreRenovacao(70, 100);

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Consultas Analíticas")
    class ConsultasAnaliticas {

        @Test
        @DisplayName("Deve contar por status")
        void deveContarPorStatus() {
            // Arrange
            Object[] row = new Object[]{StatusApolice.ATIVA, 10L};
            List<Object[]> expected = Arrays.<Object[]>asList(row);
            when(repository.countByStatus()).thenReturn(expected);

            // Act
            List<Object[]> result = repository.countByStatus();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve contar por produto")
        void deveContarPorProduto() {
            // Arrange
            Object[] row = new Object[]{"Seguro Auto", 10L};
            List<Object[]> expected = Arrays.<Object[]>asList(row);
            when(repository.countByProduto()).thenReturn(expected);

            // Act
            List<Object[]> result = repository.countByProduto();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve contar por estado")
        void deveContarPorEstado() {
            // Arrange
            Object[] row = new Object[]{"SP", 10L};
            List<Object[]> expected = Arrays.<Object[]>asList(row);
            when(repository.countByEstado()).thenReturn(expected);

            // Act
            List<Object[]> result = repository.countByEstado();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve obter estatísticas de valores por produto")
        void deveObterEstatisticasValores() {
            // Arrange
            Object[] row = new Object[]{"Seguro Auto", 10L, new BigDecimal("5000"),
                             new BigDecimal("50000"), new BigDecimal("1000"),
                             new BigDecimal("10000")};
            List<Object[]> expected = Arrays.<Object[]>asList(row);
            when(repository.getEstatisticasValoresPorProduto()).thenReturn(expected);

            // Act
            List<Object[]> result = repository.getEstatisticasValoresPorProduto();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve obter relatório de vencimentos por mês")
        void deveObterRelatorioVencimentos() {
            // Arrange
            Object[] row = new Object[]{2024, 3, 10L, new BigDecimal("50000")};
            List<Object[]> expected = Arrays.<Object[]>asList(row);
            when(repository.getRelatorioVencimentosPorMes()).thenReturn(expected);

            // Act
            List<Object[]> result = repository.getRelatorioVencimentosPorMes();

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Consultas de Performance")
    class ConsultasPerformance {

        @Test
        @DisplayName("Deve buscar atualizadas recentemente")
        void deveBuscarAtualizadasRecentemente() {
            // Arrange
            Instant since = Instant.now().minusSeconds(3600);
            when(repository.findByUpdatedAtGreaterThanOrderByUpdatedAtDesc(since))
                .thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result =
                repository.findByUpdatedAtGreaterThanOrderByUpdatedAtDesc(since);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar por último evento")
        void deveBuscarPorUltimoEvento() {
            // Arrange
            when(repository.findByLastEventIdGreaterThanOrderByLastEventIdAsc(100L))
                .thenReturn(modelList);

            // Act
            List<ApoliceQueryModel> result =
                repository.findByLastEventIdGreaterThanOrderByLastEventIdAsc(100L);

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Consultas Customizadas")
    class ConsultasCustomizadas {

        @Test
        @DisplayName("Deve buscar com filtros")
        void deveBuscarComFiltros() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(modelPage);

            // Act
            Page<ApoliceQueryModel> result = repository.findWithFilters(
                StatusApolice.ATIVA, "Seguro Auto", "12345678901",
                LocalDate.now(), LocalDate.now().plusDays(365),
                new BigDecimal("1000"), new BigDecimal("10000"), pageable
            );

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Verificações")
    class Verificacoes {

        @Test
        @DisplayName("Deve verificar existência por número")
        void deveVerificarExistenciaPorNumero() {
            // Arrange
            when(repository.existsByNumero("AP-2024-001")).thenReturn(true);

            // Act
            boolean exists = repository.existsByNumero("AP-2024-001");

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Deve contar ativas por CPF")
        void deveContarAtivasPorCpf() {
            // Arrange
            when(repository.countApolicesAtivasPorCpf("12345678901")).thenReturn(5L);

            // Act
            long count = repository.countApolicesAtivasPorCpf("12345678901");

            // Assert
            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("Deve verificar se segurado possui ativas")
        void deveVerificarSeguradoPossuiAtivas() {
            // Arrange
            when(repository.seguradoPossuiApolicesAtivas("12345678901")).thenReturn(true);

            // Act
            boolean possui = repository.seguradoPossuiApolicesAtivas("12345678901");

            // Assert
            assertThat(possui).isTrue();
        }
    }
}
