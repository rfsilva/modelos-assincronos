package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TipoDano Tests")
class TipoDanoTest {

    @Test
    @DisplayName("Deve ter 5 valores")
    void shouldHaveFiveValues() {
        assertThat(TipoDano.values()).hasSize(5);
    }

    @Test
    @DisplayName("Todos os valores devem ter descricao e detalhamento")
    void allValuesShouldHaveDescricaoAndDetalhamento() {
        for (TipoDano t : TipoDano.values()) {
            assertThat(t.getDescricao()).isNotBlank();
            assertThat(t.getDetalhamento()).isNotBlank();
            assertThat(t.getPercentualMaximo()).isNotNull();
        }
    }

    @Test
    @DisplayName("TOTAL deve ser perda total")
    void totalShouldBePerdaTotal() {
        assertThat(TipoDano.TOTAL.isPerdaTotal()).isTrue();
    }

    @Test
    @DisplayName("Outros tipos não devem ser perda total")
    void otherTypesShouldNotBePerdaTotal() {
        assertThat(TipoDano.PARCIAL.isPerdaTotal()).isFalse();
        assertThat(TipoDano.TERCEIROS.isPerdaTotal()).isFalse();
        assertThat(TipoDano.VIDROS.isPerdaTotal()).isFalse();
        assertThat(TipoDano.ACESSORIOS.isPerdaTotal()).isFalse();
    }

    @Test
    @DisplayName("TOTAL e PARCIAL devem requerer laudo pericial")
    void totalAndParcialShouldRequireLaudo() {
        assertThat(TipoDano.TOTAL.requerLaudoPericial()).isTrue();
        assertThat(TipoDano.PARCIAL.requerLaudoPericial()).isTrue();
    }

    @Test
    @DisplayName("Outros tipos não devem requerer laudo pericial")
    void otherTypesShouldNotRequireLaudo() {
        assertThat(TipoDano.TERCEIROS.requerLaudoPericial()).isFalse();
        assertThat(TipoDano.VIDROS.requerLaudoPericial()).isFalse();
        assertThat(TipoDano.ACESSORIOS.requerLaudoPericial()).isFalse();
    }

    @Test
    @DisplayName("TOTAL deve ter percentual 0.75 e PARCIAL 0.74")
    void percentuaisDevemEstarCorretos() {
        assertThat(TipoDano.TOTAL.getPercentualMaximo()).isEqualByComparingTo("0.75");
        assertThat(TipoDano.PARCIAL.getPercentualMaximo()).isEqualByComparingTo("0.74");
    }

    @Test
    @DisplayName("TERCEIROS, VIDROS, ACESSORIOS devem ter percentual zero")
    void outrosTiposDevemTerPercentualZero() {
        assertThat(TipoDano.TERCEIROS.getPercentualMaximo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(TipoDano.VIDROS.getPercentualMaximo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(TipoDano.ACESSORIOS.getPercentualMaximo()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
