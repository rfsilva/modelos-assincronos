package com.seguradora.hibrida.domain.apolice.query.dto;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para ApoliceListView.
 */
@DisplayName("ApoliceListView Tests")
class ApoliceListViewTest {

    private ApoliceListView view;

    @BeforeEach
    void setUp() {
        view = new ApoliceListView(
            "ap-001", "AP-2024-001", "Seguro Auto", StatusApolice.ATIVA,
            "João Silva", "12345678901", "São Paulo", "SP",
            LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
            new BigDecimal("100000"), new BigDecimal("5000"), new BigDecimal("5000"),
            "Cartão", 12, List.of(TipoCobertura.TOTAL), "Total", true,
            "Operador A", "Online", true, 85
        );
    }

    @Nested
    @DisplayName("Verificações de Status")
    class VerificacoesStatus {

        @Test
        @DisplayName("Deve identificar apólice ativa")
        void deveIdentificarApoliceAtiva() {
            assertThat(view.isAtiva()).isTrue();
        }

        @Test
        @DisplayName("Deve identificar apólice não ativa")
        void deveIdentificarApoliceNaoAtiva() {
            ApoliceListView cancelada = new ApoliceListView(
                "ap-002", "AP-2024-002", "Seguro Auto", StatusApolice.CANCELADA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                new BigDecimal("100000"), new BigDecimal("5000"), new BigDecimal("5000"),
                "Cartão", 12, List.of(), null, false, null, null, false, null
            );
            assertThat(cancelada.isAtiva()).isFalse();
        }

        @Test
        @DisplayName("Deve identificar apólice vencida")
        void deveIdentificarApoliceVencida() {
            ApoliceListView vencida = new ApoliceListView(
                "ap-003", "AP-2024-003", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now().minusDays(100), LocalDate.now().minusDays(1), -1, false,
                new BigDecimal("100000"), new BigDecimal("5000"), new BigDecimal("5000"),
                "Cartão", 12, List.of(), null, false, null, null, false, null
            );
            assertThat(vencida.isVencida()).isTrue();
        }

        @Test
        @DisplayName("Deve identificar apólice não vencida")
        void deveIdentificarApoliceNaoVencida() {
            assertThat(view.isVencida()).isFalse();
        }

        @Test
        @DisplayName("Deve tratar vigenciaFim nulo")
        void deveTratarVigenciaFimNulo() {
            ApoliceListView semVigencia = new ApoliceListView(
                "ap-004", "AP-2024-004", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), null, null, false,
                new BigDecimal("100000"), new BigDecimal("5000"), new BigDecimal("5000"),
                "Cartão", 12, List.of(), null, false, null, null, false, null
            );
            assertThat(semVigencia.isVencida()).isFalse();
        }
    }

    @Nested
    @DisplayName("Descrição de Status")
    class DescricaoStatus {

        @Test
        @DisplayName("Deve retornar descrição ativa")
        void deveRetornarDescricaoAtiva() {
            assertThat(view.getStatusDescricao()).isEqualTo("Ativa");
        }

        @Test
        @DisplayName("Deve retornar descrição vencida para apólice ativa vencida")
        void deveRetornarDescricaoVencida() {
            ApoliceListView vencida = new ApoliceListView(
                "ap-005", "AP-2024-005", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now().minusDays(100), LocalDate.now().minusDays(1), -1, false,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(vencida.getStatusDescricao()).isEqualTo("Vencida");
        }

        @Test
        @DisplayName("Deve retornar descrição cancelada")
        void deveRetornarDescricaoCancelada() {
            ApoliceListView cancelada = new ApoliceListView(
                "ap-006", "AP-2024-006", "Seguro Auto", StatusApolice.CANCELADA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(cancelada.getStatusDescricao()).isEqualTo("Cancelada");
        }

        @Test
        @DisplayName("Deve retornar descrição suspensa")
        void deveRetornarDescricaoSuspensa() {
            ApoliceListView suspensa = new ApoliceListView(
                "ap-007", "AP-2024-007", "Seguro Auto", StatusApolice.SUSPENSA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(suspensa.getStatusDescricao()).isEqualTo("Suspensa");
        }

        @Test
        @DisplayName("Deve retornar N/A para status nulo")
        void deveRetornarNAParaStatusNulo() {
            ApoliceListView statusNulo = new ApoliceListView(
                "ap-008", "AP-2024-008", "Seguro Auto", null,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(statusNulo.getStatusDescricao()).isEqualTo("N/A");
        }
    }

    @Nested
    @DisplayName("CSS Class de Status")
    class CssClassStatus {

        @Test
        @DisplayName("Deve retornar classe CSS para ativa")
        void deveRetornarClasseAtiva() {
            assertThat(view.getStatusCssClass()).isEqualTo("status-active");
        }

        @Test
        @DisplayName("Deve retornar classe CSS para vencida")
        void deveRetornarClasseVencida() {
            ApoliceListView vencida = new ApoliceListView(
                "ap-009", "AP-2024-009", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now().minusDays(100), LocalDate.now().minusDays(1), -1, false,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(vencida.getStatusCssClass()).isEqualTo("status-expired");
        }

        @Test
        @DisplayName("Deve retornar classe CSS para cancelada")
        void deveRetornarClasseCancelada() {
            ApoliceListView cancelada = new ApoliceListView(
                "ap-010", "AP-2024-010", "Seguro Auto", StatusApolice.CANCELADA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(cancelada.getStatusCssClass()).isEqualTo("status-cancelled");
        }

        @Test
        @DisplayName("Deve retornar classe CSS para suspensa")
        void deveRetornarClasseSuspensa() {
            ApoliceListView suspensa = new ApoliceListView(
                "ap-011", "AP-2024-011", "Seguro Auto", StatusApolice.SUSPENSA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(suspensa.getStatusCssClass()).isEqualTo("status-suspended");
        }

        @Test
        @DisplayName("Deve retornar classe CSS para status nulo")
        void deveRetornarClasseStatusNulo() {
            ApoliceListView statusNulo = new ApoliceListView(
                "ap-012", "AP-2024-012", "Seguro Auto", null,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(statusNulo.getStatusCssClass()).isEqualTo("status-unknown");
        }
    }

    @Nested
    @DisplayName("Valor da Parcela")
    class ValorParcela {

        @Test
        @DisplayName("Deve calcular valor da parcela")
        void deveCalcularValorParcela() {
            BigDecimal valorParcela = view.getValorParcela();
            assertThat(valorParcela).isEqualByComparingTo(new BigDecimal("416.67"));
        }

        @Test
        @DisplayName("Deve retornar valor total quando não há parcelas")
        void deveRetornarValorTotalSemParcelas() {
            ApoliceListView aVista = new ApoliceListView(
                "ap-013", "AP-2024-013", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                new BigDecimal("100000"), new BigDecimal("5000"), new BigDecimal("5000"),
                "Cartão", null, List.of(), null, false, null, null, false, null
            );
            assertThat(aVista.getValorParcela()).isEqualByComparingTo(new BigDecimal("5000"));
        }

        @Test
        @DisplayName("Deve retornar valor total quando parcelas é zero")
        void deveRetornarValorTotalQuandoParcelasZero() {
            ApoliceListView parcelaZero = new ApoliceListView(
                "ap-014", "AP-2024-014", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                new BigDecimal("100000"), new BigDecimal("5000"), new BigDecimal("5000"),
                "Cartão", 0, List.of(), null, false, null, null, false, null
            );
            assertThat(parcelaZero.getValorParcela()).isEqualByComparingTo(new BigDecimal("5000"));
        }

        @Test
        @DisplayName("Deve retornar null quando valor total é nulo")
        void deveRetornarNullQuandoValorNulo() {
            ApoliceListView semValor = new ApoliceListView(
                "ap-015", "AP-2024-015", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                null, null, null, "Cartão", 12, List.of(), null, false,
                null, null, false, null
            );
            assertThat(semValor.getValorParcela()).isNull();
        }
    }

    @Nested
    @DisplayName("Coberturas Formatadas")
    class CoberturasFormatadas {

        @Test
        @DisplayName("Deve retornar resumo quando disponível")
        void deveRetornarResumo() {
            assertThat(view.getCoberturasFormatadas()).isEqualTo("Total");
        }

        @Test
        @DisplayName("Deve retornar 'Cobertura Total' quando temCoberturaTotal é true")
        void deveRetornarCoberturaTotal() {
            ApoliceListView comCoberturaTotal = new ApoliceListView(
                "ap-016", "AP-2024-016", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                null, null, null, null, null,
                List.of(TipoCobertura.TOTAL), null, true,
                null, null, false, null
            );
            assertThat(comCoberturaTotal.getCoberturasFormatadas()).isEqualTo("Cobertura Total");
        }

        @Test
        @DisplayName("Deve retornar lista de coberturas quando não há resumo")
        void deveRetornarListaCoberturas() {
            ApoliceListView multiplaCoberturas = new ApoliceListView(
                "ap-017", "AP-2024-017", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                null, null, null, null, null,
                List.of(TipoCobertura.COLISAO, TipoCobertura.INCENDIO), null, false,
                null, null, false, null
            );
            String coberturas = multiplaCoberturas.getCoberturasFormatadas();
            assertThat(coberturas).contains("Colisão");
            assertThat(coberturas).contains("Incêndio");
        }

        @Test
        @DisplayName("Deve retornar 'Nenhuma cobertura' quando lista está vazia")
        void deveRetornarNenhumaCobertura() {
            ApoliceListView semCoberturas = new ApoliceListView(
                "ap-018", "AP-2024-018", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(semCoberturas.getCoberturasFormatadas()).isEqualTo("Nenhuma cobertura");
        }
    }

    @Nested
    @DisplayName("Prioridade de Renovação")
    class PrioridadeRenovacao {

        @Test
        @DisplayName("Deve retornar ALTA para 7 dias ou menos")
        void deveRetornarAltaPara7Dias() {
            ApoliceListView alta = new ApoliceListView(
                "ap-019", "AP-2024-019", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(7), 7, true,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(alta.getPrioridadeRenovacao()).isEqualTo("ALTA");
        }

        @Test
        @DisplayName("Deve retornar MÉDIA para 15 dias ou menos")
        void deveRetornarMediaPara15Dias() {
            ApoliceListView media = new ApoliceListView(
                "ap-020", "AP-2024-020", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(15), 15, true,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(media.getPrioridadeRenovacao()).isEqualTo("MÉDIA");
        }

        @Test
        @DisplayName("Deve retornar BAIXA para 30 dias ou menos")
        void deveRetornarBaixaPara30Dias() {
            ApoliceListView baixa = new ApoliceListView(
                "ap-021", "AP-2024-021", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(30), 30, true,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(baixa.getPrioridadeRenovacao()).isEqualTo("BAIXA");
        }

        @Test
        @DisplayName("Deve retornar BAIXA quando vencimento não é próximo")
        void deveRetornarBaixaQuandoVencimentoNaoProximo() {
            assertThat(view.getPrioridadeRenovacao()).isEqualTo("BAIXA");
        }
    }

    @Nested
    @DisplayName("Precisa Atenção")
    class PrecisaAtencao {

        @Test
        @DisplayName("Deve precisar de atenção quando vencimento próximo")
        void devePrecisarAtencaoQuandoVencimentoProximo() {
            ApoliceListView vencimentoProximo = new ApoliceListView(
                "ap-022", "AP-2024-022", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(20), 20, true,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(vencimentoProximo.precisaAtencao()).isTrue();
        }

        @Test
        @DisplayName("Deve precisar de atenção quando score baixo")
        void devePrecisarAtencaoQuandoScoreBaixo() {
            ApoliceListView scoreBaixo = new ApoliceListView(
                "ap-023", "AP-2024-023", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now(), LocalDate.now().plusDays(365), 365, false,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, 30
            );
            assertThat(scoreBaixo.precisaAtencao()).isTrue();
        }

        @Test
        @DisplayName("Deve precisar de atenção quando vencida")
        void devePrecisarAtencaoQuandoVencida() {
            ApoliceListView vencida = new ApoliceListView(
                "ap-024", "AP-2024-024", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", "São Paulo", "SP",
                LocalDate.now().minusDays(100), LocalDate.now().minusDays(1), -1, false,
                null, null, null, null, null, List.of(), null, false,
                null, null, false, null
            );
            assertThat(vencida.precisaAtencao()).isTrue();
        }

        @Test
        @DisplayName("Não deve precisar de atenção quando tudo ok")
        void naoDevePrecisarAtencaoQuandoOk() {
            assertThat(view.precisaAtencao()).isFalse();
        }
    }
}
