package com.seguradora.hibrida.domain.workflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TipoEtapa Tests")
class TipoEtapaTest {

    @Test
    @DisplayName("Deve ter 4 valores")
    void shouldHaveFourValues() {
        assertThat(TipoEtapa.values()).hasSize(4);
    }

    // =========================================================================
    // requerIntervencaoHumana
    // =========================================================================

    @Nested
    @DisplayName("requerIntervencaoHumana()")
    class RequerIntervencaoHumana {

        @Test
        @DisplayName("MANUAL e APROVACAO devem requerer intervenção humana")
        void manualAndAprovacaoShouldRequireHumanIntervention() {
            assertThat(TipoEtapa.MANUAL.requerIntervencaoHumana()).isTrue();
            assertThat(TipoEtapa.APROVACAO.requerIntervencaoHumana()).isTrue();
        }

        @Test
        @DisplayName("AUTOMATICA e INTEGRACAO não devem requerer intervenção humana")
        void automaticaAndIntegracaoShouldNotRequireHumanIntervention() {
            assertThat(TipoEtapa.AUTOMATICA.requerIntervencaoHumana()).isFalse();
            assertThat(TipoEtapa.INTEGRACAO.requerIntervencaoHumana()).isFalse();
        }
    }

    // =========================================================================
    // isAssincrona
    // =========================================================================

    @Nested
    @DisplayName("isAssincrona()")
    class IsAssincrona {

        @Test
        @DisplayName("AUTOMATICA e INTEGRACAO devem ser assíncronas")
        void automaticaAndIntegracaoShouldBeAssincrona() {
            assertThat(TipoEtapa.AUTOMATICA.isAssincrona()).isTrue();
            assertThat(TipoEtapa.INTEGRACAO.isAssincrona()).isTrue();
        }

        @Test
        @DisplayName("MANUAL e APROVACAO não devem ser assíncronas")
        void manualAndAprovacaoShouldNotBeAssincrona() {
            assertThat(TipoEtapa.MANUAL.isAssincrona()).isFalse();
            assertThat(TipoEtapa.APROVACAO.isAssincrona()).isFalse();
        }
    }

    // =========================================================================
    // isPodeExecutarAutomaticamente
    // =========================================================================

    @Nested
    @DisplayName("isPodeExecutarAutomaticamente()")
    class IsPodeExecutarAutomaticamente {

        @Test
        @DisplayName("AUTOMATICA e INTEGRACAO devem poder executar automaticamente")
        void automaticaAndIntegracaoCanExecuteAutomatically() {
            assertThat(TipoEtapa.AUTOMATICA.isPodeExecutarAutomaticamente()).isTrue();
            assertThat(TipoEtapa.INTEGRACAO.isPodeExecutarAutomaticamente()).isTrue();
        }

        @Test
        @DisplayName("MANUAL e APROVACAO não devem poder executar automaticamente")
        void manualAndAprovacaoCannotExecuteAutomatically() {
            assertThat(TipoEtapa.MANUAL.isPodeExecutarAutomaticamente()).isFalse();
            assertThat(TipoEtapa.APROVACAO.isPodeExecutarAutomaticamente()).isFalse();
        }
    }

    // =========================================================================
    // isAprovacao
    // =========================================================================

    @Nested
    @DisplayName("isAprovacao()")
    class IsAprovacao {

        @Test
        @DisplayName("Apenas APROVACAO deve retornar true")
        void onlyAprovacaoShouldReturnTrue() {
            assertThat(TipoEtapa.APROVACAO.isAprovacao()).isTrue();
            assertThat(TipoEtapa.AUTOMATICA.isAprovacao()).isFalse();
            assertThat(TipoEtapa.MANUAL.isAprovacao()).isFalse();
            assertThat(TipoEtapa.INTEGRACAO.isAprovacao()).isFalse();
        }
    }
}
