package com.seguradora.hibrida.domain.sinistro.query.repository;

import com.seguradora.hibrida.domain.sinistro.query.model.SinistroDashboardView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SinistroDashboardRepository Tests")
class SinistroDashboardRepositoryTest {

    private final SinistroDashboardRepository repository = mock(SinistroDashboardRepository.class);

    @Test
    @DisplayName("findByPeriodoAndTipoPeriodo deve retornar dashboard quando encontrado")
    void findByPeriodoAndTipoPeriodoShouldReturn() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-01")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 1, 1))
                .totalSinistros(100)
                .build();
        when(repository.findByPeriodoAndTipoPeriodo("2024-01", "MES")).thenReturn(Optional.of(view));

        Optional<SinistroDashboardView> result = repository.findByPeriodoAndTipoPeriodo("2024-01", "MES");

        assertThat(result).isPresent();
        assertThat(result.get().getPeriodo()).isEqualTo("2024-01");
        assertThat(result.get().getTotalSinistros()).isEqualTo(100);
        verify(repository).findByPeriodoAndTipoPeriodo("2024-01", "MES");
    }

    @Test
    @DisplayName("findByPeriodoAndTipoPeriodo deve retornar empty quando não encontrado")
    void findByPeriodoAndTipoPeriodoShouldReturnEmpty() {
        when(repository.findByPeriodoAndTipoPeriodo("2099-01", "MES")).thenReturn(Optional.empty());

        Optional<SinistroDashboardView> result = repository.findByPeriodoAndTipoPeriodo("2099-01", "MES");

        assertThat(result).isEmpty();
        verify(repository).findByPeriodoAndTipoPeriodo("2099-01", "MES");
    }

    @Test
    @DisplayName("findUltimosDias deve retornar dashboards a partir da data limite")
    void findUltimosDiasShouldReturnList() {
        LocalDate dataLimite = LocalDate.of(2024, 1, 1);
        List<SinistroDashboardView> views = List.of(
                SinistroDashboardView.builder().periodo("2024-01-03").tipoPeriodo("DIA").dataReferencia(LocalDate.of(2024, 1, 3)).build(),
                SinistroDashboardView.builder().periodo("2024-01-02").tipoPeriodo("DIA").dataReferencia(LocalDate.of(2024, 1, 2)).build()
        );
        when(repository.findUltimosDias(dataLimite, "DIA")).thenReturn(views);

        List<SinistroDashboardView> result = repository.findUltimosDias(dataLimite, "DIA");

        assertThat(result).hasSize(2);
        verify(repository).findUltimosDias(dataLimite, "DIA");
    }

    @Test
    @DisplayName("findUltimosPeriodos deve retornar os N períodos mais recentes")
    void findUltimosPeriodosShouldReturnN() {
        List<SinistroDashboardView> views = List.of(
                SinistroDashboardView.builder().periodo("2024-03").tipoPeriodo("MES").dataReferencia(LocalDate.of(2024, 3, 1)).build(),
                SinistroDashboardView.builder().periodo("2024-02").tipoPeriodo("MES").dataReferencia(LocalDate.of(2024, 2, 1)).build()
        );
        when(repository.findUltimosPeriodos("MES", 2)).thenReturn(views);

        List<SinistroDashboardView> result = repository.findUltimosPeriodos("MES", 2);

        assertThat(result).hasSize(2);
        verify(repository).findUltimosPeriodos("MES", 2);
    }

    @Test
    @DisplayName("findByIntervalo deve retornar dashboards no intervalo de datas")
    void findByIntervaloShouldReturnList() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim = LocalDate.of(2024, 3, 31);
        List<SinistroDashboardView> views = List.of(
                SinistroDashboardView.builder().periodo("2024-01").tipoPeriodo("MES").dataReferencia(LocalDate.of(2024, 1, 1)).build(),
                SinistroDashboardView.builder().periodo("2024-02").tipoPeriodo("MES").dataReferencia(LocalDate.of(2024, 2, 1)).build()
        );
        when(repository.findByIntervalo(inicio, fim, "MES")).thenReturn(views);

        List<SinistroDashboardView> result = repository.findByIntervalo(inicio, fim, "MES");

        assertThat(result).hasSize(2);
        verify(repository).findByIntervalo(inicio, fim, "MES");
    }

    @Test
    @DisplayName("existsByPeriodoAndTipoPeriodo deve retornar true quando existe")
    void existsByPeriodoAndTipoPeriodoShouldReturnTrue() {
        when(repository.existsByPeriodoAndTipoPeriodo("2024-01", "MES")).thenReturn(true);

        boolean result = repository.existsByPeriodoAndTipoPeriodo("2024-01", "MES");

        assertThat(result).isTrue();
        verify(repository).existsByPeriodoAndTipoPeriodo("2024-01", "MES");
    }

    @Test
    @DisplayName("findLastUpdated deve retornar o dashboard mais recentemente atualizado")
    void findLastUpdatedShouldReturnLatest() {
        SinistroDashboardView view = SinistroDashboardView.builder()
                .periodo("2024-03")
                .tipoPeriodo("MES")
                .dataReferencia(LocalDate.of(2024, 3, 1))
                .build();
        when(repository.findLastUpdated()).thenReturn(Optional.of(view));

        Optional<SinistroDashboardView> result = repository.findLastUpdated();

        assertThat(result).isPresent();
        verify(repository).findLastUpdated();
    }
}
