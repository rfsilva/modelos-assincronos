package com.seguradora.hibrida.domain.sinistro.query.repository;

import com.seguradora.hibrida.domain.sinistro.query.model.SinistroAnalyticsView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SinistroAnalyticsRepository Tests")
class SinistroAnalyticsRepositoryTest {

    private final SinistroAnalyticsRepository repository = mock(SinistroAnalyticsRepository.class);

    @Test
    @DisplayName("findByPeriodoAndTipoPeriodoAndTipoSinistro deve retornar analytics quando encontrado")
    void findByPeriodoAndTipoShouldReturnWhenFound() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .tipoSinistro("COLISAO")
                .build();
        when(repository.findByPeriodoAndTipoPeriodoAndTipoSinistro("2024-01", "MES", "COLISAO"))
                .thenReturn(Optional.of(view));

        Optional<SinistroAnalyticsView> result =
                repository.findByPeriodoAndTipoPeriodoAndTipoSinistro("2024-01", "MES", "COLISAO");

        assertThat(result).isPresent();
        assertThat(result.get().getTipoSinistro()).isEqualTo("COLISAO");
        verify(repository).findByPeriodoAndTipoPeriodoAndTipoSinistro("2024-01", "MES", "COLISAO");
    }

    @Test
    @DisplayName("findAgregadoPorPeriodo deve retornar analytics agregado")
    void findAgregadoPorPeriodoShouldReturn() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .quantidade(100)
                .build();
        when(repository.findAgregadoPorPeriodo("2024-01", "MES")).thenReturn(Optional.of(view));

        Optional<SinistroAnalyticsView> result = repository.findAgregadoPorPeriodo("2024-01", "MES");

        assertThat(result).isPresent();
        assertThat(result.get().getQuantidade()).isEqualTo(100);
        verify(repository).findAgregadoPorPeriodo("2024-01", "MES");
    }

    @Test
    @DisplayName("findByPeriodoAndTipoPeriodoAndRegiao deve retornar analytics da região")
    void findByRegiaoShouldReturn() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .regiao("SP")
                .build();
        when(repository.findByPeriodoAndTipoPeriodoAndRegiao("2024-01", "MES", "SP"))
                .thenReturn(Optional.of(view));

        Optional<SinistroAnalyticsView> result =
                repository.findByPeriodoAndTipoPeriodoAndRegiao("2024-01", "MES", "SP");

        assertThat(result).isPresent();
        assertThat(result.get().getRegiao()).isEqualTo("SP");
        verify(repository).findByPeriodoAndTipoPeriodoAndRegiao("2024-01", "MES", "SP");
    }

    @Test
    @DisplayName("findByPeriodoAndTipoPeriodoAndAnalistaId deve retornar analytics do analista")
    void findByAnalistaShouldReturn() {
        SinistroAnalyticsView view = SinistroAnalyticsView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .analistaId("ANALISTA-01")
                .build();
        when(repository.findByPeriodoAndTipoPeriodoAndAnalistaId("2024-01", "MES", "ANALISTA-01"))
                .thenReturn(Optional.of(view));

        Optional<SinistroAnalyticsView> result =
                repository.findByPeriodoAndTipoPeriodoAndAnalistaId("2024-01", "MES", "ANALISTA-01");

        assertThat(result).isPresent();
        assertThat(result.get().getAnalistaId()).isEqualTo("ANALISTA-01");
        verify(repository).findByPeriodoAndTipoPeriodoAndAnalistaId("2024-01", "MES", "ANALISTA-01");
    }

    @Test
    @DisplayName("findSerieTemporalAgregada deve retornar lista ordenada")
    void findSerieTemporalAgregadaShouldReturnList() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim = LocalDate.of(2024, 3, 31);
        List<SinistroAnalyticsView> views = List.of(
                SinistroAnalyticsView.builder().periodo("2024-01").tipoPeriodo("MES").dataReferencia(LocalDate.of(2024, 1, 1)).build(),
                SinistroAnalyticsView.builder().periodo("2024-02").tipoPeriodo("MES").dataReferencia(LocalDate.of(2024, 2, 1)).build()
        );
        when(repository.findSerieTemporalAgregada(inicio, fim, "MES")).thenReturn(views);

        List<SinistroAnalyticsView> result = repository.findSerieTemporalAgregada(inicio, fim, "MES");

        assertThat(result).hasSize(2);
        verify(repository).findSerieTemporalAgregada(inicio, fim, "MES");
    }

    @Test
    @DisplayName("findTendencias deve retornar lista dos últimos N períodos")
    void findTendenciasShouldReturnList() {
        List<SinistroAnalyticsView> views = List.of(
                SinistroAnalyticsView.builder().periodo("2024-03").tipoPeriodo("MES").dataReferencia(LocalDate.of(2024, 3, 1)).build(),
                SinistroAnalyticsView.builder().periodo("2024-02").tipoPeriodo("MES").dataReferencia(LocalDate.of(2024, 2, 1)).build(),
                SinistroAnalyticsView.builder().periodo("2024-01").tipoPeriodo("MES").dataReferencia(LocalDate.of(2024, 1, 1)).build()
        );
        when(repository.findTendencias("MES", 3)).thenReturn(views);

        List<SinistroAnalyticsView> result = repository.findTendencias("MES", 3);

        assertThat(result).hasSize(3);
        verify(repository).findTendencias("MES", 3);
    }

    @Test
    @DisplayName("findByTendencia deve filtrar por tendência")
    void findByTendenciaShouldFilterByTendencia() {
        LocalDate limite = LocalDate.of(2024, 1, 1);
        List<SinistroAnalyticsView> views = List.of(
                SinistroAnalyticsView.builder()
                        .periodo("2024-02").tipoPeriodo("MES").dataReferencia(LocalDate.of(2024, 2, 1))
                        .tendencia("CRESCENTE").build()
        );
        when(repository.findByTendencia("CRESCENTE", limite)).thenReturn(views);

        List<SinistroAnalyticsView> result = repository.findByTendencia("CRESCENTE", limite);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTendencia()).isEqualTo("CRESCENTE");
        verify(repository).findByTendencia("CRESCENTE", limite);
    }

    @Test
    @DisplayName("findComAltaPerformance deve filtrar por score mínimo e data")
    void findComAltaPerformanceShouldFilterByScoreAndDate() {
        LocalDate dataLimite = LocalDate.of(2024, 1, 1);
        List<SinistroAnalyticsView> views = List.of(
                SinistroAnalyticsView.builder()
                        .periodo("2024-01").tipoPeriodo("MES").dataReferencia(LocalDate.of(2024, 1, 1))
                        .scoreQualidade(new BigDecimal("90.00")).build()
        );
        when(repository.findComAltaPerformance(new BigDecimal("85"), dataLimite)).thenReturn(views);

        List<SinistroAnalyticsView> result = repository.findComAltaPerformance(new BigDecimal("85"), dataLimite);

        assertThat(result).hasSize(1);
        verify(repository).findComAltaPerformance(new BigDecimal("85"), dataLimite);
    }
}
