package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TipoSinistro Tests")
class TipoSinistroTest {

    @Test
    @DisplayName("Deve ter 6 valores")
    void shouldHaveSixValues() {
        assertThat(TipoSinistro.values()).hasSize(6);
    }

    @Test
    @DisplayName("Todos os valores devem ter descricao não-nula")
    void allValuesShouldHaveDescricao() {
        for (TipoSinistro t : TipoSinistro.values()) {
            assertThat(t.getDescricao()).isNotBlank();
            assertThat(t.getFranquiaPadrao()).isNotNull();
            assertThat(t.getPrazoProcessamentoDias()).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("ROUBO_FURTO e COLISAO devem requerer consulta Detran")
    void rouboFurtoAndColisaoShouldRequireDetran() {
        assertThat(TipoSinistro.ROUBO_FURTO.requerConsultaDetran()).isTrue();
        assertThat(TipoSinistro.COLISAO.requerConsultaDetran()).isTrue();
    }

    @Test
    @DisplayName("Outros tipos não devem requerer consulta Detran")
    void otherTypesShouldNotRequireDetran() {
        assertThat(TipoSinistro.INCENDIO.requerConsultaDetran()).isFalse();
        assertThat(TipoSinistro.ENCHENTE.requerConsultaDetran()).isFalse();
        assertThat(TipoSinistro.VANDALISMO.requerConsultaDetran()).isFalse();
        assertThat(TipoSinistro.TERCEIROS.requerConsultaDetran()).isFalse();
    }

    @Test
    @DisplayName("ROUBO_FURTO, VANDALISMO, INCENDIO devem requerer boletim de ocorrência")
    void someTypesShouldRequireBoletimOcorrencia() {
        assertThat(TipoSinistro.ROUBO_FURTO.requerBoletimOcorrencia()).isTrue();
        assertThat(TipoSinistro.VANDALISMO.requerBoletimOcorrencia()).isTrue();
        assertThat(TipoSinistro.INCENDIO.requerBoletimOcorrencia()).isTrue();
    }

    @Test
    @DisplayName("COLISAO, ENCHENTE, TERCEIROS não devem requerer boletim")
    void someTypesShouldNotRequireBoletim() {
        assertThat(TipoSinistro.COLISAO.requerBoletimOcorrencia()).isFalse();
        assertThat(TipoSinistro.ENCHENTE.requerBoletimOcorrencia()).isFalse();
        assertThat(TipoSinistro.TERCEIROS.requerBoletimOcorrencia()).isFalse();
    }

    @Test
    @DisplayName("possuiCarencia deve refletir carenciaDias > 0")
    void possuiCarenciaShouldReflectCarenciaDias() {
        assertThat(TipoSinistro.ROUBO_FURTO.possuiCarencia()).isTrue(); // 30 dias
        assertThat(TipoSinistro.INCENDIO.possuiCarencia()).isTrue();    // 15 dias
        assertThat(TipoSinistro.COLISAO.possuiCarencia()).isFalse();    // 0 dias
        assertThat(TipoSinistro.TERCEIROS.possuiCarencia()).isFalse();  // 0 dias
    }

    @Test
    @DisplayName("ROUBO_FURTO deve ter franquia zero")
    void rouboFurtoShouldHaveZeroFranquia() {
        assertThat(TipoSinistro.ROUBO_FURTO.getFranquiaPadrao()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("COLISAO deve ter franquia 1500")
    void colisaoShouldHaveFranquia1500() {
        assertThat(TipoSinistro.COLISAO.getFranquiaPadrao()).isEqualByComparingTo("1500.00");
    }
}
