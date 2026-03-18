package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PrazoProcessamento Tests")
class PrazoProcessamentoTest {

    @Test
    @DisplayName("criar deve definir diasUteis conforme tipo de sinistro")
    void criarShouldSetDiasUteisFromTipoSinistro() {
        PrazoProcessamento prazo = PrazoProcessamento.criar(TipoSinistro.COLISAO);
        assertThat(prazo.getDiasUteis()).isEqualTo(TipoSinistro.COLISAO.getPrazoProcessamentoDias());
        assertThat(prazo.getTipoSinistro()).isEqualTo(TipoSinistro.COLISAO);
        assertThat(prazo.getDataInicio()).isNotNull();
        assertThat(prazo.getDataLimite()).isAfter(prazo.getDataInicio());
    }

    @Test
    @DisplayName("criar com dataInicio deve usar data fornecida")
    void criarWithDataInicioShouldUseProvidedDate() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(10);
        PrazoProcessamento prazo = PrazoProcessamento.criar(TipoSinistro.ROUBO_FURTO, inicio);
        assertThat(prazo.getDataInicio()).isEqualTo(inicio);
    }

    @Test
    @DisplayName("isVencido deve retornar true quando data limite é passada")
    void isVencidoShouldReturnTrueWhenPastDue() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        PrazoProcessamento prazo = PrazoProcessamento.builder()
                .dataInicio(inicio)
                .dataLimite(LocalDateTime.now().minusDays(1))
                .diasUteis(5)
                .tipoSinistro(TipoSinistro.COLISAO)
                .build();
        assertThat(prazo.isVencido()).isTrue();
    }

    @Test
    @DisplayName("isVencido deve retornar false quando prazo ainda válido")
    void isVencidoShouldReturnFalseWhenStillValid() {
        PrazoProcessamento prazo = PrazoProcessamento.criar(TipoSinistro.COLISAO);
        assertThat(prazo.isVencido()).isFalse();
    }

    @Test
    @DisplayName("getHorasRestantes deve retornar zero quando vencido")
    void getHorasRestantesShouldReturnZeroWhenExpired() {
        PrazoProcessamento prazo = PrazoProcessamento.builder()
                .dataInicio(LocalDateTime.now().minusDays(10))
                .dataLimite(LocalDateTime.now().minusDays(1))
                .diasUteis(5)
                .tipoSinistro(TipoSinistro.COLISAO)
                .build();
        assertThat(prazo.getHorasRestantes()).isEqualTo(0L);
    }

    @Test
    @DisplayName("getHorasRestantes deve retornar valor positivo quando dentro do prazo")
    void getHorasRestantesShouldReturnPositiveWhenInTime() {
        PrazoProcessamento prazo = PrazoProcessamento.criar(TipoSinistro.COLISAO);
        assertThat(prazo.getHorasRestantes()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("getPercentualDecorrido deve retornar 0 antes do início")
    void getPercentualDecorridoShouldReturn0BeforeStart() {
        PrazoProcessamento prazo = PrazoProcessamento.builder()
                .dataInicio(LocalDateTime.now().plusDays(1))
                .dataLimite(LocalDateTime.now().plusDays(10))
                .diasUteis(5)
                .tipoSinistro(TipoSinistro.COLISAO)
                .build();
        assertThat(prazo.getPercentualDecorrido()).isEqualTo(0);
    }

    @Test
    @DisplayName("getPercentualDecorrido deve retornar 100 após vencimento")
    void getPercentualDecorridoShouldReturn100AfterExpiry() {
        PrazoProcessamento prazo = PrazoProcessamento.builder()
                .dataInicio(LocalDateTime.now().minusDays(10))
                .dataLimite(LocalDateTime.now().minusDays(1))
                .diasUteis(5)
                .tipoSinistro(TipoSinistro.COLISAO)
                .build();
        assertThat(prazo.getPercentualDecorrido()).isEqualTo(100);
    }

    @Test
    @DisplayName("isProximoVencimento deve retornar false quando não iniciado")
    void isProximoVencimentoShouldReturnFalseWhenNotStarted() {
        PrazoProcessamento prazo = PrazoProcessamento.builder()
                .dataInicio(LocalDateTime.now().plusDays(1))
                .dataLimite(LocalDateTime.now().plusDays(10))
                .diasUteis(5)
                .tipoSinistro(TipoSinistro.COLISAO)
                .build();
        assertThat(prazo.isProximoVencimento()).isFalse();
    }

    @Test
    @DisplayName("equals deve comparar campos relevantes")
    void equalsShouldCompareRelevantFields() {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime limite = inicio.plusDays(7);
        PrazoProcessamento p1 = PrazoProcessamento.builder()
                .dataInicio(inicio).dataLimite(limite)
                .diasUteis(5).tipoSinistro(TipoSinistro.COLISAO).build();
        PrazoProcessamento p2 = PrazoProcessamento.builder()
                .dataInicio(inicio).dataLimite(limite)
                .diasUteis(5).tipoSinistro(TipoSinistro.COLISAO).build();
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    @DisplayName("toString deve conter diasUteis e percentual")
    void toStringShouldContainDiasUteisAndPercentual() {
        PrazoProcessamento prazo = PrazoProcessamento.criar(TipoSinistro.COLISAO);
        assertThat(prazo.toString()).contains("5");
    }
}
