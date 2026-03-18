package com.seguradora.hibrida.domain.workflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("NivelAprovacao Tests")
class NivelAprovacaoTest {

    @Test
    @DisplayName("Deve ter 4 valores")
    void shouldHaveFourValues() {
        assertThat(NivelAprovacao.values()).hasSize(4);
    }

    @Test
    @DisplayName("Cada nível deve ter número sequencial correto")
    void eachLevelShouldHaveCorrectNumber() {
        assertThat(NivelAprovacao.NIVEL_1_ANALISTA.getNivel()).isEqualTo(1);
        assertThat(NivelAprovacao.NIVEL_2_SUPERVISOR.getNivel()).isEqualTo(2);
        assertThat(NivelAprovacao.NIVEL_3_GERENTE.getNivel()).isEqualTo(3);
        assertThat(NivelAprovacao.NIVEL_4_DIRETOR.getNivel()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cada nível deve ter descrição não nula")
    void eachLevelShouldHaveDescription() {
        for (NivelAprovacao nivel : NivelAprovacao.values()) {
            assertThat(nivel.getDescricao()).isNotNull().isNotBlank();
        }
    }

    @Test
    @DisplayName("NIVEL_4_DIRETOR deve ter limiteAlcada nulo")
    void nivel4ShouldHaveNullLimite() {
        assertThat(NivelAprovacao.NIVEL_4_DIRETOR.getLimiteAlcada()).isNull();
        assertThat(NivelAprovacao.NIVEL_4_DIRETOR.getLimite()).isNull();
    }

    @Test
    @DisplayName("Níveis 1-3 devem ter limites definidos")
    void levels1to3ShouldHaveLimits() {
        assertThat(NivelAprovacao.NIVEL_1_ANALISTA.getLimiteAlcada()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(NivelAprovacao.NIVEL_2_SUPERVISOR.getLimiteAlcada()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(NivelAprovacao.NIVEL_3_GERENTE.getLimiteAlcada()).isEqualByComparingTo(new BigDecimal("200000.00"));
    }

    // =========================================================================
    // isNivelMaximo / isNivelMinimo
    // =========================================================================

    @Nested
    @DisplayName("isNivelMaximo() / isNivelMinimo()")
    class NivelExtremos {

        @Test
        @DisplayName("NIVEL_4_DIRETOR deve ser nível máximo")
        void nivel4ShouldBeMaximo() {
            assertThat(NivelAprovacao.NIVEL_4_DIRETOR.isNivelMaximo()).isTrue();
            assertThat(NivelAprovacao.NIVEL_1_ANALISTA.isNivelMaximo()).isFalse();
            assertThat(NivelAprovacao.NIVEL_2_SUPERVISOR.isNivelMaximo()).isFalse();
            assertThat(NivelAprovacao.NIVEL_3_GERENTE.isNivelMaximo()).isFalse();
        }

        @Test
        @DisplayName("NIVEL_1_ANALISTA deve ser nível mínimo")
        void nivel1ShouldBeMinimo() {
            assertThat(NivelAprovacao.NIVEL_1_ANALISTA.isNivelMinimo()).isTrue();
            assertThat(NivelAprovacao.NIVEL_2_SUPERVISOR.isNivelMinimo()).isFalse();
            assertThat(NivelAprovacao.NIVEL_3_GERENTE.isNivelMinimo()).isFalse();
            assertThat(NivelAprovacao.NIVEL_4_DIRETOR.isNivelMinimo()).isFalse();
        }
    }

    // =========================================================================
    // getProximo / getAnterior
    // =========================================================================

    @Nested
    @DisplayName("getProximo()")
    class GetProximo {

        @Test
        @DisplayName("Deve retornar próximo nível na hierarquia")
        void shouldReturnNextLevel() {
            assertThat(NivelAprovacao.NIVEL_1_ANALISTA.getProximo()).isEqualTo(NivelAprovacao.NIVEL_2_SUPERVISOR);
            assertThat(NivelAprovacao.NIVEL_2_SUPERVISOR.getProximo()).isEqualTo(NivelAprovacao.NIVEL_3_GERENTE);
            assertThat(NivelAprovacao.NIVEL_3_GERENTE.getProximo()).isEqualTo(NivelAprovacao.NIVEL_4_DIRETOR);
        }

        @Test
        @DisplayName("NIVEL_4_DIRETOR.getProximo() deve retornar null")
        void nivel4ProximoShouldBeNull() {
            assertThat(NivelAprovacao.NIVEL_4_DIRETOR.getProximo()).isNull();
        }
    }

    @Nested
    @DisplayName("getAnterior()")
    class GetAnterior {

        @Test
        @DisplayName("Deve retornar nível anterior na hierarquia")
        void shouldReturnPreviousLevel() {
            assertThat(NivelAprovacao.NIVEL_2_SUPERVISOR.getAnterior()).isEqualTo(NivelAprovacao.NIVEL_1_ANALISTA);
            assertThat(NivelAprovacao.NIVEL_3_GERENTE.getAnterior()).isEqualTo(NivelAprovacao.NIVEL_2_SUPERVISOR);
            assertThat(NivelAprovacao.NIVEL_4_DIRETOR.getAnterior()).isEqualTo(NivelAprovacao.NIVEL_3_GERENTE);
        }

        @Test
        @DisplayName("NIVEL_1_ANALISTA.getAnterior() deve retornar null")
        void nivel1AnteriorShouldBeNull() {
            assertThat(NivelAprovacao.NIVEL_1_ANALISTA.getAnterior()).isNull();
        }
    }

    // =========================================================================
    // podeAprovar
    // =========================================================================

    @Nested
    @DisplayName("podeAprovar(BigDecimal)")
    class PodeAprovar {

        @Test
        @DisplayName("NIVEL_1 deve aprovar valores até 10000")
        void nivel1ShouldApproveUpTo10000() {
            assertThat(NivelAprovacao.NIVEL_1_ANALISTA.podeAprovar(new BigDecimal("1.00"))).isTrue();
            assertThat(NivelAprovacao.NIVEL_1_ANALISTA.podeAprovar(new BigDecimal("9999.99"))).isTrue();
            assertThat(NivelAprovacao.NIVEL_1_ANALISTA.podeAprovar(new BigDecimal("10000.00"))).isTrue();
            assertThat(NivelAprovacao.NIVEL_1_ANALISTA.podeAprovar(new BigDecimal("10000.01"))).isFalse();
            assertThat(NivelAprovacao.NIVEL_1_ANALISTA.podeAprovar(new BigDecimal("50000.00"))).isFalse();
        }

        @Test
        @DisplayName("NIVEL_2 deve aprovar valores até 50000")
        void nivel2ShouldApproveUpTo50000() {
            assertThat(NivelAprovacao.NIVEL_2_SUPERVISOR.podeAprovar(new BigDecimal("10001.00"))).isTrue();
            assertThat(NivelAprovacao.NIVEL_2_SUPERVISOR.podeAprovar(new BigDecimal("50000.00"))).isTrue();
            assertThat(NivelAprovacao.NIVEL_2_SUPERVISOR.podeAprovar(new BigDecimal("50000.01"))).isFalse();
        }

        @Test
        @DisplayName("NIVEL_3 deve aprovar valores até 200000")
        void nivel3ShouldApproveUpTo200000() {
            assertThat(NivelAprovacao.NIVEL_3_GERENTE.podeAprovar(new BigDecimal("50001.00"))).isTrue();
            assertThat(NivelAprovacao.NIVEL_3_GERENTE.podeAprovar(new BigDecimal("200000.00"))).isTrue();
            assertThat(NivelAprovacao.NIVEL_3_GERENTE.podeAprovar(new BigDecimal("200000.01"))).isFalse();
        }

        @Test
        @DisplayName("NIVEL_4 deve aprovar qualquer valor positivo")
        void nivel4ShouldApproveAnyPositiveValue() {
            assertThat(NivelAprovacao.NIVEL_4_DIRETOR.podeAprovar(new BigDecimal("1.00"))).isTrue();
            assertThat(NivelAprovacao.NIVEL_4_DIRETOR.podeAprovar(new BigDecimal("200001.00"))).isTrue();
            assertThat(NivelAprovacao.NIVEL_4_DIRETOR.podeAprovar(new BigDecimal("999999999.99"))).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false para valor nulo ou zero")
        void shouldReturnFalseForNullOrZero() {
            assertThat(NivelAprovacao.NIVEL_1_ANALISTA.podeAprovar(null)).isFalse();
            assertThat(NivelAprovacao.NIVEL_1_ANALISTA.podeAprovar(BigDecimal.ZERO)).isFalse();
            assertThat(NivelAprovacao.NIVEL_4_DIRETOR.podeAprovar(null)).isFalse();
            assertThat(NivelAprovacao.NIVEL_4_DIRETOR.podeAprovar(new BigDecimal("-1.00"))).isFalse();
        }
    }

    // =========================================================================
    // determinarNivel
    // =========================================================================

    @Nested
    @DisplayName("determinarNivel(BigDecimal)")
    class DeterminarNivel {

        @Test
        @DisplayName("Deve determinar NIVEL_1 para valores até 10000")
        void shouldDetermineNivel1() {
            assertThat(NivelAprovacao.determinarNivel(new BigDecimal("5000.00"))).isEqualTo(NivelAprovacao.NIVEL_1_ANALISTA);
            assertThat(NivelAprovacao.determinarNivel(new BigDecimal("10000.00"))).isEqualTo(NivelAprovacao.NIVEL_1_ANALISTA);
        }

        @Test
        @DisplayName("Deve determinar NIVEL_2 para valores entre 10001 e 50000")
        void shouldDetermineNivel2() {
            assertThat(NivelAprovacao.determinarNivel(new BigDecimal("10000.01"))).isEqualTo(NivelAprovacao.NIVEL_2_SUPERVISOR);
            assertThat(NivelAprovacao.determinarNivel(new BigDecimal("50000.00"))).isEqualTo(NivelAprovacao.NIVEL_2_SUPERVISOR);
        }

        @Test
        @DisplayName("Deve determinar NIVEL_3 para valores entre 50001 e 200000")
        void shouldDetermineNivel3() {
            assertThat(NivelAprovacao.determinarNivel(new BigDecimal("50000.01"))).isEqualTo(NivelAprovacao.NIVEL_3_GERENTE);
            assertThat(NivelAprovacao.determinarNivel(new BigDecimal("200000.00"))).isEqualTo(NivelAprovacao.NIVEL_3_GERENTE);
        }

        @Test
        @DisplayName("Deve determinar NIVEL_4 para valores acima de 200000")
        void shouldDetermineNivel4() {
            assertThat(NivelAprovacao.determinarNivel(new BigDecimal("200000.01"))).isEqualTo(NivelAprovacao.NIVEL_4_DIRETOR);
            assertThat(NivelAprovacao.determinarNivel(new BigDecimal("1000000.00"))).isEqualTo(NivelAprovacao.NIVEL_4_DIRETOR);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para valor nulo")
        void shouldThrowForNullValue() {
            assertThatThrownBy(() -> NivelAprovacao.determinarNivel(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para valor zero ou negativo")
        void shouldThrowForZeroOrNegative() {
            assertThatThrownBy(() -> NivelAprovacao.determinarNivel(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> NivelAprovacao.determinarNivel(new BigDecimal("-1")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
