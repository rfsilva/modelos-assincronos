package com.seguradora.hibrida.domain.analytics.service;

import com.seguradora.hibrida.domain.analytics.dto.*;
import com.seguradora.hibrida.domain.analytics.model.AnalyticsProjection;
import com.seguradora.hibrida.domain.analytics.model.TipoMetrica;
import com.seguradora.hibrida.domain.analytics.repository.AnalyticsProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link RelatorioService}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RelatorioService Tests")
class RelatorioServiceTest {

    @Mock
    private AnalyticsProjectionRepository analyticsRepository;

    private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        relatorioService = new RelatorioService(analyticsRepository);
    }

    // =========================================================================
    // Meta-informação
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Service")
    void shouldBeAnnotatedWithService() {
        assertThat(RelatorioService.class.isAnnotationPresent(Service.class)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar AnalyticsProjectionRepository no construtor")
    void shouldAcceptRepositoryInConstructor() throws NoSuchMethodException {
        assertThat(RelatorioService.class.getConstructor(AnalyticsProjectionRepository.class))
                .isNotNull();
    }

    // =========================================================================
    // obterDashboardExecutivo
    // =========================================================================

    @Nested
    @DisplayName("obterDashboardExecutivo()")
    class ObterDashboardExecutivo {

        @Test
        @DisplayName("Deve retornar dashboard vazio quando resumo é nulo")
        void shouldReturnEmptyDashboardWhenResumoIsNull() {
            LocalDate data = LocalDate.now();
            when(analyticsRepository.getResumoExecutivo(data)).thenReturn(null);

            DashboardExecutivoView result = relatorioService.obterDashboardExecutivo(data);

            assertThat(result).isNotNull();
            assertThat(result.getDataReferencia()).isEqualTo(data);
            assertThat(result.getTotalSegurados()).isZero();
            assertThat(result.getTotalApolices()).isZero();
        }

        @Test
        @DisplayName("Deve retornar dashboard vazio quando primeiro elemento é nulo")
        void shouldReturnEmptyDashboardWhenFirstElementIsNull() {
            LocalDate data = LocalDate.now();
            when(analyticsRepository.getResumoExecutivo(data))
                    .thenReturn(new Object[]{null, null, null, null, null});

            DashboardExecutivoView result = relatorioService.obterDashboardExecutivo(data);

            assertThat(result.getTotalSegurados()).isZero();
        }

        @Test
        @DisplayName("Deve retornar dashboard vazio em caso de exceção")
        void shouldReturnEmptyDashboardOnException() {
            LocalDate data = LocalDate.now();
            when(analyticsRepository.getResumoExecutivo(data))
                    .thenThrow(new RuntimeException("DB error"));

            DashboardExecutivoView result = relatorioService.obterDashboardExecutivo(data);

            assertThat(result).isNotNull();
            assertThat(result.getTotalSegurados()).isZero();
        }
    }

    // =========================================================================
    // obterRelatorioSegurados
    // =========================================================================

    @Nested
    @DisplayName("obterRelatorioSegurados()")
    class ObterRelatorioSegurados {

        private final LocalDate inicio = LocalDate.of(2024, 1, 1);
        private final LocalDate fim = LocalDate.of(2024, 12, 31);

        @Test
        @DisplayName("Deve retornar relatório com total de segurados")
        void shouldReturnRelatorioWithTotalSegurados() {
            when(analyticsRepository.sumTotalSeguradosPorPeriodo(inicio, fim, TipoMetrica.DIARIA))
                    .thenReturn(500L);
            when(analyticsRepository.findMetricasPorRegiaoNaData(fim)).thenReturn(List.of());
            when(analyticsRepository.findMetricasPorFaixaEtariaNaData(fim)).thenReturn(List.of());
            when(analyticsRepository.findCrescimentoSegurados(inicio)).thenReturn(List.of());

            RelatorioSeguradosView result = relatorioService.obterRelatorioSegurados(inicio, fim);

            assertThat(result.getTotalSegurados()).isEqualTo(500L);
            assertThat(result.getPeriodoInicio()).isEqualTo(inicio);
            assertThat(result.getPeriodoFim()).isEqualTo(fim);
        }

        @Test
        @DisplayName("Deve usar 0 quando sumTotalSegurados retorna null")
        void shouldUseZeroWhenSumReturnsNull() {
            when(analyticsRepository.sumTotalSeguradosPorPeriodo(inicio, fim, TipoMetrica.DIARIA))
                    .thenReturn(null);
            when(analyticsRepository.findMetricasPorRegiaoNaData(fim)).thenReturn(List.of());
            when(analyticsRepository.findMetricasPorFaixaEtariaNaData(fim)).thenReturn(List.of());
            when(analyticsRepository.findCrescimentoSegurados(inicio)).thenReturn(List.of());

            RelatorioSeguradosView result = relatorioService.obterRelatorioSegurados(inicio, fim);

            assertThat(result.getTotalSegurados()).isZero();
        }

        @Test
        @DisplayName("Deve propagar exceção do repositório")
        void shouldPropagateRepositoryException() {
            when(analyticsRepository.sumTotalSeguradosPorPeriodo(inicio, fim, TipoMetrica.DIARIA))
                    .thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> relatorioService.obterRelatorioSegurados(inicio, fim))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao gerar relatório de segurados");
        }
    }

    // =========================================================================
    // obterRelatorioApolices
    // =========================================================================

    @Nested
    @DisplayName("obterRelatorioApolices()")
    class ObterRelatorioApolices {

        private final LocalDate inicio = LocalDate.of(2024, 1, 1);
        private final LocalDate fim = LocalDate.of(2024, 12, 31);

        @Test
        @DisplayName("Deve retornar relatório com totais corretos")
        void shouldReturnRelatorioWithCorrectTotals() {
            when(analyticsRepository.sumTotalApolicesPorPeriodo(inicio, fim, TipoMetrica.DIARIA))
                    .thenReturn(300L);
            when(analyticsRepository.avgPremioPorPeriodo(inicio, fim, TipoMetrica.DIARIA))
                    .thenReturn(1500.0);
            when(analyticsRepository.avgTaxaRenovacaoPorPeriodo(inicio, fim, TipoMetrica.DIARIA))
                    .thenReturn(85.0);
            when(analyticsRepository.findMetricasPorProdutoNaData(fim)).thenReturn(List.of());
            when(analyticsRepository.findMetricasPorCanalNaData(fim)).thenReturn(List.of());
            when(analyticsRepository.findEvolucaoReceita(inicio)).thenReturn(List.of());

            RelatorioApolicesView result = relatorioService.obterRelatorioApolices(inicio, fim);

            assertThat(result.getTotalApolices()).isEqualTo(300L);
            assertThat(result.getPremioMedio()).isEqualByComparingTo(new BigDecimal("1500.0"));
            assertThat(result.getTaxaRenovacao()).isEqualByComparingTo(new BigDecimal("85.0"));
        }

        @Test
        @DisplayName("Deve usar BigDecimal.ZERO quando premioMedio é null")
        void shouldUseZeroWhenPremioMedioIsNull() {
            when(analyticsRepository.sumTotalApolicesPorPeriodo(inicio, fim, TipoMetrica.DIARIA)).thenReturn(0L);
            when(analyticsRepository.avgPremioPorPeriodo(inicio, fim, TipoMetrica.DIARIA)).thenReturn(null);
            when(analyticsRepository.avgTaxaRenovacaoPorPeriodo(inicio, fim, TipoMetrica.DIARIA)).thenReturn(null);
            when(analyticsRepository.findMetricasPorProdutoNaData(fim)).thenReturn(List.of());
            when(analyticsRepository.findMetricasPorCanalNaData(fim)).thenReturn(List.of());
            when(analyticsRepository.findEvolucaoReceita(inicio)).thenReturn(List.of());

            RelatorioApolicesView result = relatorioService.obterRelatorioApolices(inicio, fim);

            assertThat(result.getPremioMedio()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getTaxaRenovacao()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // =========================================================================
    // obterRelatorioRenovacoes
    // =========================================================================

    @Nested
    @DisplayName("obterRelatorioRenovacoes()")
    class ObterRelatorioRenovacoes {

        private final LocalDate inicio = LocalDate.of(2024, 1, 1);
        private final LocalDate fim = LocalDate.of(2024, 12, 31);

        @Test
        @DisplayName("Deve retornar relatório vazio quando não há renovações")
        void shouldReturnEmptyRelatorioWhenNoRenovacoes() {
            when(analyticsRepository.findByDataReferenciaBetweenAndTipoMetricaOrderByDataReferencia(
                    inicio, fim, TipoMetrica.RENOVACAO))
                    .thenReturn(List.of());

            RelatorioRenovacoesView result = relatorioService.obterRelatorioRenovacoes(inicio, fim);

            assertThat(result.getTotalRenovacoes()).isZero();
            assertThat(result.getTaxaRenovacaoMedia()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getReceitaRenovacoes()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Deve somar renovações das projeções")
        void shouldSumRenovacoesFromProjections() {
            AnalyticsProjection p1 = AnalyticsProjection.builder()
                    .renovacoes(10L).premioTotal(new BigDecimal("5000"))
                    .taxaRenovacao(new BigDecimal("80.00")).build();
            AnalyticsProjection p2 = AnalyticsProjection.builder()
                    .renovacoes(20L).premioTotal(new BigDecimal("10000"))
                    .taxaRenovacao(new BigDecimal("90.00")).build();

            when(analyticsRepository.findByDataReferenciaBetweenAndTipoMetricaOrderByDataReferencia(
                    inicio, fim, TipoMetrica.RENOVACAO))
                    .thenReturn(List.of(p1, p2));

            RelatorioRenovacoesView result = relatorioService.obterRelatorioRenovacoes(inicio, fim);

            assertThat(result.getTotalRenovacoes()).isEqualTo(30L);
            assertThat(result.getReceitaRenovacoes()).isEqualByComparingTo(new BigDecimal("15000"));
        }
    }

    // =========================================================================
    // obterRelatorioPerformance
    // =========================================================================

    @Test
    @DisplayName("obterRelatorioPerformance deve retornar relatório não nulo")
    void obterRelatorioPerformanceShouldReturnNonNull() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim = LocalDate.of(2024, 12, 31);

        when(analyticsRepository.findTendenciaTaxaRenovacao(inicio)).thenReturn(List.of());
        when(analyticsRepository.findTopRegioesPorApolices(eq(fim), any())).thenReturn(List.of());
        when(analyticsRepository.findTopProdutosPorReceita(eq(fim), any())).thenReturn(List.of());
        when(analyticsRepository.findTopCanaisPorVolume(eq(fim), any())).thenReturn(List.of());

        RelatorioPerformanceView result = relatorioService.obterRelatorioPerformance(inicio, fim);

        assertThat(result).isNotNull();
        assertThat(result.getPeriodoInicio()).isEqualTo(inicio);
        assertThat(result.getPeriodoFim()).isEqualTo(fim);
    }
}
