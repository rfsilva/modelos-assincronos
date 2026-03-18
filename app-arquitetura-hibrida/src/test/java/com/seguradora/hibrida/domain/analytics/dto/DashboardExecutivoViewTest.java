package com.seguradora.hibrida.domain.analytics.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link DashboardExecutivoView}.
 */
@DisplayName("DashboardExecutivoView Tests")
class DashboardExecutivoViewTest {

    // =========================================================================
    // getTicketMedioPorSegurado
    // =========================================================================

    @Nested
    @DisplayName("getTicketMedioPorSegurado()")
    class GetTicketMedioPorSegurado {

        @Test
        @DisplayName("Deve retornar ZERO quando totalSegurados é null")
        void shouldReturnZeroWhenTotalSeguradosIsNull() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .totalSegurados(null)
                    .receitaTotal(new BigDecimal("10000"))
                    .build();
            assertThat(view.getTicketMedioPorSegurado()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Deve retornar ZERO quando totalSegurados é 0")
        void shouldReturnZeroWhenTotalSeguradosIsZero() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .totalSegurados(0L)
                    .receitaTotal(new BigDecimal("10000"))
                    .build();
            assertThat(view.getTicketMedioPorSegurado()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Deve retornar ZERO quando receitaTotal é null")
        void shouldReturnZeroWhenReceitaTotalIsNull() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .totalSegurados(100L)
                    .receitaTotal(null)
                    .build();
            assertThat(view.getTicketMedioPorSegurado()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Deve calcular ticket médio corretamente")
        void shouldCalculateTicketMedioCorrectly() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .totalSegurados(100L)
                    .receitaTotal(new BigDecimal("50000"))
                    .build();
            assertThat(view.getTicketMedioPorSegurado()).isEqualByComparingTo(new BigDecimal("500.00"));
        }
    }

    // =========================================================================
    // getTaxaConversao
    // =========================================================================

    @Nested
    @DisplayName("getTaxaConversao()")
    class GetTaxaConversao {

        @Test
        @DisplayName("Deve retornar ZERO quando totalSegurados é 0")
        void shouldReturnZeroWhenTotalSeguradosIsZero() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .totalSegurados(0L)
                    .totalApolices(100L)
                    .build();
            assertThat(view.getTaxaConversao()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Deve calcular taxa de conversão corretamente")
        void shouldCalculateTaxaConversaoCorrectly() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .totalSegurados(200L)
                    .totalApolices(150L)
                    .build();
            // 150 / 200 * 100 = 75%
            assertThat(view.getTaxaConversao()).isEqualByComparingTo(new BigDecimal("75.0000"));
        }
    }

    // =========================================================================
    // isCrescimentoSaudavel
    // =========================================================================

    @Nested
    @DisplayName("isCrescimentoSaudavel()")
    class IsCrescimentoSaudavel {

        @Test
        @DisplayName("Deve retornar true quando todos os indicadores estão saudáveis")
        void shouldReturnTrueWhenAllIndicatorsAreHealthy() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .crescimentoSegurados(new BigDecimal("5.0"))
                    .crescimentoApolices(new BigDecimal("3.0"))
                    .taxaRenovacaoMedia(80.0)
                    .taxaCancelamentoMedia(10.0)
                    .build();
            assertThat(view.isCrescimentoSaudavel()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando crescimentoSegurados é negativo")
        void shouldReturnFalseWhenCrescimentoSeguradosIsNegative() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .crescimentoSegurados(new BigDecimal("-1.0"))
                    .crescimentoApolices(new BigDecimal("3.0"))
                    .taxaRenovacaoMedia(80.0)
                    .taxaCancelamentoMedia(10.0)
                    .build();
            assertThat(view.isCrescimentoSaudavel()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando taxaRenovacao é menor que 70%")
        void shouldReturnFalseWhenTaxaRenovacaoIsBelowThreshold() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .crescimentoSegurados(new BigDecimal("5.0"))
                    .crescimentoApolices(new BigDecimal("3.0"))
                    .taxaRenovacaoMedia(65.0)
                    .taxaCancelamentoMedia(10.0)
                    .build();
            assertThat(view.isCrescimentoSaudavel()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando taxaCancelamento é maior que 20%")
        void shouldReturnFalseWhenTaxaCancelamentoIsAboveThreshold() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .crescimentoSegurados(new BigDecimal("5.0"))
                    .crescimentoApolices(new BigDecimal("3.0"))
                    .taxaRenovacaoMedia(80.0)
                    .taxaCancelamentoMedia(25.0)
                    .build();
            assertThat(view.isCrescimentoSaudavel()).isFalse();
        }
    }

    // =========================================================================
    // getStatusNegocio
    // =========================================================================

    @Nested
    @DisplayName("getStatusNegocio()")
    class GetStatusNegocio {

        @Test
        @DisplayName("Deve retornar EXCELENTE quando crescimento é saudável")
        void shouldReturnExcelenteWhenCrescimentoIsHealthy() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .crescimentoSegurados(new BigDecimal("5.0"))
                    .crescimentoApolices(new BigDecimal("3.0"))
                    .taxaRenovacaoMedia(80.0)
                    .taxaCancelamentoMedia(10.0)
                    .build();
            assertThat(view.getStatusNegocio()).isEqualTo("EXCELENTE");
        }

        @Test
        @DisplayName("Deve retornar BOM quando taxa renovação está entre 60 e 70")
        void shouldReturnBomWhenTaxaRenovacaoIsBetween60And70() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .taxaRenovacaoMedia(65.0)
                    .taxaCancelamentoMedia(25.0)
                    .crescimentoSegurados(BigDecimal.ZERO)
                    .build();
            assertThat(view.getStatusNegocio()).isEqualTo("BOM");
        }

        @Test
        @DisplayName("Deve retornar REGULAR quando taxa renovação está entre 40 e 60")
        void shouldReturnRegularWhenTaxaRenovacaoIsBetween40And60() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .taxaRenovacaoMedia(50.0)
                    .crescimentoSegurados(BigDecimal.ZERO)
                    .build();
            assertThat(view.getStatusNegocio()).isEqualTo("REGULAR");
        }

        @Test
        @DisplayName("Deve retornar ATENCAO quando taxa renovação é abaixo de 40%")
        void shouldReturnAtencaoWhenTaxaRenovacaoIsBelow40() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .taxaRenovacaoMedia(30.0)
                    .build();
            assertThat(view.getStatusNegocio()).isEqualTo("ATENÇÃO");
        }

        @Test
        @DisplayName("Deve retornar ATENCAO quando taxaRenovacao é null")
        void shouldReturnAtencaoWhenTaxaRenovacaoIsNull() {
            DashboardExecutivoView view = DashboardExecutivoView.builder()
                    .taxaRenovacaoMedia(null)
                    .build();
            assertThat(view.getStatusNegocio()).isEqualTo("ATENÇÃO");
        }
    }

    // =========================================================================
    // Builder básico
    // =========================================================================

    @Test
    @DisplayName("Builder deve criar instância com todos os campos")
    void builderShouldCreateInstanceWithAllFields() {
        LocalDate data = LocalDate.now();
        DashboardExecutivoView view = DashboardExecutivoView.builder()
                .dataReferencia(data)
                .totalSegurados(100L)
                .totalApolices(80L)
                .receitaTotal(new BigDecimal("120000"))
                .build();

        assertThat(view.getDataReferencia()).isEqualTo(data);
        assertThat(view.getTotalSegurados()).isEqualTo(100L);
        assertThat(view.getTotalApolices()).isEqualTo(80L);
    }
}
