package com.seguradora.hibrida.domain.analytics.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link TipoMetrica}.
 */
@DisplayName("TipoMetrica Tests")
class TipoMetricaTest {

    @Test
    @DisplayName("Deve ter 16 valores")
    void shouldHaveFifteenValues() {
        assertThat(TipoMetrica.values()).hasSize(16);
    }

    @Test
    @DisplayName("getDisplayName deve retornar nome de exibição não nulo")
    void getDisplayNameShouldReturnNonNull() {
        for (TipoMetrica tipo : TipoMetrica.values()) {
            assertThat(tipo.getDisplayName()).isNotNull().isNotBlank();
        }
    }

    @Test
    @DisplayName("getDescricao deve retornar descrição não nula")
    void getDescricaoShouldReturnNonNull() {
        for (TipoMetrica tipo : TipoMetrica.values()) {
            assertThat(tipo.getDescricao()).isNotNull().isNotBlank();
        }
    }

    @Test
    @DisplayName("toString deve retornar displayName")
    void toStringShouldReturnDisplayName() {
        assertThat(TipoMetrica.GERAL.toString()).isEqualTo(TipoMetrica.GERAL.getDisplayName());
        assertThat(TipoMetrica.MENSAL.toString()).isEqualTo(TipoMetrica.MENSAL.getDisplayName());
    }

    // =========================================================================
    // isTemporal
    // =========================================================================

    @Nested
    @DisplayName("isTemporal()")
    class IsTemporal {

        @Test
        @DisplayName("Deve retornar true para DIARIA, SEMANAL, MENSAL, TRIMESTRAL, ANUAL")
        void shouldReturnTrueForTemporalTypes() {
            assertThat(TipoMetrica.DIARIA.isTemporal()).isTrue();
            assertThat(TipoMetrica.SEMANAL.isTemporal()).isTrue();
            assertThat(TipoMetrica.MENSAL.isTemporal()).isTrue();
            assertThat(TipoMetrica.TRIMESTRAL.isTemporal()).isTrue();
            assertThat(TipoMetrica.ANUAL.isTemporal()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false para tipos não temporais")
        void shouldReturnFalseForNonTemporalTypes() {
            assertThat(TipoMetrica.GERAL.isTemporal()).isFalse();
            assertThat(TipoMetrica.POR_REGIAO.isTemporal()).isFalse();
            assertThat(TipoMetrica.RENOVACAO.isTemporal()).isFalse();
            assertThat(TipoMetrica.FINANCEIRO.isTemporal()).isFalse();
        }
    }

    // =========================================================================
    // isDimensional
    // =========================================================================

    @Nested
    @DisplayName("isDimensional()")
    class IsDimensional {

        @Test
        @DisplayName("Deve retornar true para POR_REGIAO, POR_PRODUTO, POR_CANAL, POR_FAIXA_ETARIA, POR_OPERADOR")
        void shouldReturnTrueForDimensionalTypes() {
            assertThat(TipoMetrica.POR_REGIAO.isDimensional()).isTrue();
            assertThat(TipoMetrica.POR_PRODUTO.isDimensional()).isTrue();
            assertThat(TipoMetrica.POR_CANAL.isDimensional()).isTrue();
            assertThat(TipoMetrica.POR_FAIXA_ETARIA.isDimensional()).isTrue();
            assertThat(TipoMetrica.POR_OPERADOR.isDimensional()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false para tipos não dimensionais")
        void shouldReturnFalseForNonDimensionalTypes() {
            assertThat(TipoMetrica.GERAL.isDimensional()).isFalse();
            assertThat(TipoMetrica.MENSAL.isDimensional()).isFalse();
            assertThat(TipoMetrica.RENOVACAO.isDimensional()).isFalse();
        }
    }

    // =========================================================================
    // isEspecifica
    // =========================================================================

    @Nested
    @DisplayName("isEspecifica()")
    class IsEspecifica {

        @Test
        @DisplayName("Deve retornar true para RENOVACAO, CANCELAMENTO, VENCIMENTO, PERFORMANCE, FINANCEIRO")
        void shouldReturnTrueForEspecificTypes() {
            assertThat(TipoMetrica.RENOVACAO.isEspecifica()).isTrue();
            assertThat(TipoMetrica.CANCELAMENTO.isEspecifica()).isTrue();
            assertThat(TipoMetrica.VENCIMENTO.isEspecifica()).isTrue();
            assertThat(TipoMetrica.PERFORMANCE.isEspecifica()).isTrue();
            assertThat(TipoMetrica.FINANCEIRO.isEspecifica()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false para tipos não específicos")
        void shouldReturnFalseForNonEspecificTypes() {
            assertThat(TipoMetrica.GERAL.isEspecifica()).isFalse();
            assertThat(TipoMetrica.MENSAL.isEspecifica()).isFalse();
            assertThat(TipoMetrica.POR_REGIAO.isEspecifica()).isFalse();
        }
    }

    @Test
    @DisplayName("Cada tipo deve pertencer a exatamente uma categoria (temporal, dimensional, específica ou geral)")
    void eachTypeShouldBelongToOneCategory() {
        for (TipoMetrica tipo : TipoMetrica.values()) {
            long categorias = 0;
            if (tipo.isTemporal()) categorias++;
            if (tipo.isDimensional()) categorias++;
            if (tipo.isEspecifica()) categorias++;
            if (tipo == TipoMetrica.GERAL) categorias++;
            assertThat(categorias)
                    .as("TipoMetrica %s deve pertencer a exatamente uma categoria", tipo)
                    .isEqualTo(1L);
        }
    }
}
