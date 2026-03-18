package com.seguradora.hibrida.domain.sinistro.query.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroFilter Tests")
class SinistroFilterTest {

    @Test
    @DisplayName("empty deve retornar filtro sem nenhum campo preenchido")
    void emptyShouldReturnFilterWithNoFields() {
        SinistroFilter filter = SinistroFilter.empty();

        assertThat(filter.hasFilters()).isFalse();
        assertThat(filter.getStatus()).isNull();
        assertThat(filter.getTipoSinistro()).isNull();
        assertThat(filter.getCpfSegurado()).isNull();
        assertThat(filter.getPlaca()).isNull();
        assertThat(filter.getOperadorResponsavel()).isNull();
        assertThat(filter.getPrioridade()).isNull();
        assertThat(filter.getCanalAbertura()).isNull();
        assertThat(filter.getApoliceNumero()).isNull();
        assertThat(filter.getTag()).isNull();
        assertThat(filter.getDataAberturaInicio()).isNull();
        assertThat(filter.getDataAberturaFim()).isNull();
        assertThat(filter.getConsultaDetranPendente()).isNull();
    }

    @Test
    @DisplayName("porStatus deve preencher apenas o campo status")
    void porStatusShouldSetOnlyStatus() {
        SinistroFilter filter = SinistroFilter.porStatus("ABERTO");

        assertThat(filter.getStatus()).isEqualTo("ABERTO");
        assertThat(filter.hasFilters()).isTrue();
        assertThat(filter.getCpfSegurado()).isNull();
        assertThat(filter.getPlaca()).isNull();
    }

    @Test
    @DisplayName("porCpf deve preencher apenas o campo cpfSegurado")
    void porCpfShouldSetOnlyCpf() {
        SinistroFilter filter = SinistroFilter.porCpf("12345678901");

        assertThat(filter.getCpfSegurado()).isEqualTo("12345678901");
        assertThat(filter.hasFilters()).isTrue();
        assertThat(filter.getStatus()).isNull();
        assertThat(filter.getPlaca()).isNull();
    }

    @Test
    @DisplayName("porPlaca deve preencher apenas o campo placa")
    void porPlacaShouldSetOnlyPlaca() {
        SinistroFilter filter = SinistroFilter.porPlaca("ABC1234");

        assertThat(filter.getPlaca()).isEqualTo("ABC1234");
        assertThat(filter.hasFilters()).isTrue();
        assertThat(filter.getStatus()).isNull();
        assertThat(filter.getCpfSegurado()).isNull();
    }

    @Test
    @DisplayName("hasFilters deve retornar true quando tipoSinistro está preenchido")
    void hasFiltersShouldReturnTrueForTipoSinistro() {
        SinistroFilter filter = SinistroFilter.empty();
        filter.setTipoSinistro("COLISAO");

        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    @DisplayName("hasFilters deve retornar true quando operadorResponsavel está preenchido")
    void hasFiltersShouldReturnTrueForOperadorResponsavel() {
        SinistroFilter filter = SinistroFilter.empty();
        filter.setOperadorResponsavel("João");

        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    @DisplayName("hasFilters deve retornar true quando dataAberturaInicio está preenchido")
    void hasFiltersShouldReturnTrueForDataAberturaInicio() {
        SinistroFilter filter = SinistroFilter.empty();
        filter.setDataAberturaInicio(Instant.now());

        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    @DisplayName("hasFilters deve retornar true quando dataAberturaFim está preenchido")
    void hasFiltersShouldReturnTrueForDataAberturaFim() {
        SinistroFilter filter = SinistroFilter.empty();
        filter.setDataAberturaFim(Instant.now());

        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    @DisplayName("hasFilters deve retornar true quando apoliceNumero está preenchido")
    void hasFiltersShouldReturnTrueForApoliceNumero() {
        SinistroFilter filter = SinistroFilter.empty();
        filter.setApoliceNumero("AP-001");

        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    @DisplayName("hasFilters deve retornar true quando prioridade está preenchido")
    void hasFiltersShouldReturnTrueForPrioridade() {
        SinistroFilter filter = SinistroFilter.empty();
        filter.setPrioridade("ALTA");

        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    @DisplayName("hasFilters deve retornar true quando canalAbertura está preenchido")
    void hasFiltersShouldReturnTrueForCanalAbertura() {
        SinistroFilter filter = SinistroFilter.empty();
        filter.setCanalAbertura("WEB");

        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    @DisplayName("hasFilters deve retornar true quando consultaDetranPendente está preenchido")
    void hasFiltersShouldReturnTrueForConsultaDetranPendente() {
        SinistroFilter filter = SinistroFilter.empty();
        filter.setConsultaDetranPendente(true);

        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    @DisplayName("hasFilters deve retornar true quando tag está preenchido")
    void hasFiltersShouldReturnTrueForTag() {
        SinistroFilter filter = SinistroFilter.empty();
        filter.setTag("URGENTE");

        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    @DisplayName("construtor completo deve preencher todos os campos")
    void fullConstructorShouldSetAllFields() {
        Instant inicio = Instant.parse("2024-01-01T00:00:00Z");
        Instant fim = Instant.parse("2024-12-31T23:59:59Z");

        SinistroFilter filter = new SinistroFilter(
                "ABERTO", "COLISAO", "Analista1",
                inicio, fim,
                "12345678901", "ABC1234", "AP-001",
                "ALTA", "WEB", true, "URGENTE"
        );

        assertThat(filter.getStatus()).isEqualTo("ABERTO");
        assertThat(filter.getTipoSinistro()).isEqualTo("COLISAO");
        assertThat(filter.getOperadorResponsavel()).isEqualTo("Analista1");
        assertThat(filter.getDataAberturaInicio()).isEqualTo(inicio);
        assertThat(filter.getDataAberturaFim()).isEqualTo(fim);
        assertThat(filter.getCpfSegurado()).isEqualTo("12345678901");
        assertThat(filter.getPlaca()).isEqualTo("ABC1234");
        assertThat(filter.getApoliceNumero()).isEqualTo("AP-001");
        assertThat(filter.getPrioridade()).isEqualTo("ALTA");
        assertThat(filter.getCanalAbertura()).isEqualTo("WEB");
        assertThat(filter.getConsultaDetranPendente()).isTrue();
        assertThat(filter.getTag()).isEqualTo("URGENTE");
        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    @DisplayName("toString deve incluir campos principais")
    void toStringShouldIncludeMainFields() {
        SinistroFilter filter = SinistroFilter.porStatus("ABERTO");
        filter.setPlaca("ABC1234");

        String str = filter.toString();

        assertThat(str).contains("ABERTO");
        assertThat(str).contains("ABC1234");
    }

    @Test
    @DisplayName("setters devem atualizar os valores corretamente")
    void settersShouldUpdateValues() {
        SinistroFilter filter = SinistroFilter.empty();
        filter.setStatus("EM_ANALISE");
        filter.setTipoSinistro("ROUBO");
        filter.setPrioridade("URGENTE");

        assertThat(filter.getStatus()).isEqualTo("EM_ANALISE");
        assertThat(filter.getTipoSinistro()).isEqualTo("ROUBO");
        assertThat(filter.getPrioridade()).isEqualTo("URGENTE");
    }
}
