package com.seguradora.hibrida.domain.apolice.query.dto;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para ApoliceDetailView.
 */
@DisplayName("ApoliceDetailView Tests")
class ApoliceDetailViewTest {

    private ApoliceDetailView view;

    @BeforeEach
    void setUp() {
        view = new ApoliceDetailView(
            "ap-001", "AP-2024-001", "Seguro Auto", StatusApolice.ATIVA,
            "seg-001", "João Silva", "12345678901", "joao@email.com", "11999999999",
            "São Paulo", "SP", LocalDate.now(), LocalDate.now().plusDays(365),
            365, false, 12, new BigDecimal("100000"), new BigDecimal("5000"),
            new BigDecimal("5000"), new BigDecimal("10000"), "Cartão", 12,
            new BigDecimal("416.67"), List.of(TipoCobertura.TOTAL), "Total", true,
            Map.of(), "Operador A", "Online", "Observações importantes", true, 85,
            Instant.now(), Instant.now(), 1L, 1L, List.of(), List.of(),
            Map.of("scoreRenovacao", 85), List.of("Vencimento próximo"),
            List.of("Iniciar renovação")
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
            ApoliceDetailView cancelada = new ApoliceDetailView(
                "ap-002", "AP-2024-002", "Seguro Auto", StatusApolice.CANCELADA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now(), LocalDate.now().plusDays(365),
                365, false, 12, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, null, null,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of(), List.of()
            );
            assertThat(cancelada.isAtiva()).isFalse();
        }

        @Test
        @DisplayName("Deve identificar apólice vencida")
        void deveIdentificarApoliceVencida() {
            ApoliceDetailView vencida = new ApoliceDetailView(
                "ap-003", "AP-2024-003", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now().minusDays(100), LocalDate.now().minusDays(1),
                -1, false, 12, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, null, null,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of(), List.of()
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
            ApoliceDetailView semVigencia = new ApoliceDetailView(
                "ap-004", "AP-2024-004", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, null, null,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of(), List.of()
            );
            assertThat(semVigencia.isVencida()).isFalse();
        }
    }

    @Nested
    @DisplayName("Elegibilidade para Renovação")
    class ElegibilidadeRenovacao {

        @Test
        @DisplayName("Deve identificar apólice elegível para renovação")
        void deveIdentificarElegivelRenovacao() {
            ApoliceDetailView elegivel = new ApoliceDetailView(
                "ap-005", "AP-2024-005", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now(), LocalDate.now().plusDays(20),
                20, true, 12, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, true, 80,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of(), List.of()
            );
            assertThat(elegivel.isElegivelRenovacao()).isTrue();
        }

        @Test
        @DisplayName("Não deve ser elegível se não estiver ativa")
        void naoDeveSerElegivelSeNaoAtiva() {
            ApoliceDetailView cancelada = new ApoliceDetailView(
                "ap-006", "AP-2024-006", "Seguro Auto", StatusApolice.CANCELADA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now(), LocalDate.now().plusDays(20),
                20, true, 12, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, true, 80,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of(), List.of()
            );
            assertThat(cancelada.isElegivelRenovacao()).isFalse();
        }

        @Test
        @DisplayName("Não deve ser elegível se vencimento não for próximo")
        void naoDeveSerElegivelSeVencimentoNaoProximo() {
            ApoliceDetailView distante = new ApoliceDetailView(
                "ap-007", "AP-2024-007", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now(), LocalDate.now().plusDays(200),
                200, false, 12, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, true, 80,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of(), List.of()
            );
            assertThat(distante.isElegivelRenovacao()).isFalse();
        }

        @Test
        @DisplayName("Não deve ser elegível se score for baixo")
        void naoDeveSerElegivelSeScoreBaixo() {
            ApoliceDetailView scoreBaixo = new ApoliceDetailView(
                "ap-008", "AP-2024-008", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now(), LocalDate.now().plusDays(20),
                20, true, 12, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, true, 30,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of(), List.of()
            );
            assertThat(scoreBaixo.isElegivelRenovacao()).isFalse();
        }

        @Test
        @DisplayName("Deve ser elegível mesmo com score nulo")
        void deveSerElegivelComScoreNulo() {
            ApoliceDetailView scoreNulo = new ApoliceDetailView(
                "ap-009", "AP-2024-009", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now(), LocalDate.now().plusDays(20),
                20, true, 12, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, true, null,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of(), List.of()
            );
            assertThat(scoreNulo.isElegivelRenovacao()).isTrue();
        }
    }

    @Nested
    @DisplayName("Status Detalhado")
    class StatusDetalhado {

        @Test
        @DisplayName("Deve retornar status ativa simples")
        void deveRetornarStatusAtivaSimples() {
            assertThat(view.getStatusDetalhado()).isEqualTo("Ativa");
        }

        @Test
        @DisplayName("Deve retornar status ativa vencida")
        void deveRetornarStatusAtivaVencida() {
            ApoliceDetailView vencida = new ApoliceDetailView(
                "ap-010", "AP-2024-010", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now().minusDays(100), LocalDate.now().minusDays(1),
                -1, false, 12, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, true, null,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of(), List.of()
            );
            assertThat(vencida.getStatusDetalhado()).isEqualTo("Ativa (Vencida)");
        }

        @Test
        @DisplayName("Deve retornar status ativa com dias para vencimento")
        void deveRetornarStatusAtivaComDias() {
            ApoliceDetailView venceEm20 = new ApoliceDetailView(
                "ap-011", "AP-2024-011", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now(), LocalDate.now().plusDays(20),
                20, true, 12, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, true, null,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of(), List.of()
            );
            assertThat(venceEm20.getStatusDetalhado()).isEqualTo("Ativa (Vence em 20 dias)");
        }

        @Test
        @DisplayName("Deve retornar status cancelada")
        void deveRetornarStatusCancelada() {
            ApoliceDetailView cancelada = new ApoliceDetailView(
                "ap-012", "AP-2024-012", "Seguro Auto", StatusApolice.CANCELADA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, List.of(), null, null, Map.of(),
                null, null, null, null, null, null, null, null, null,
                List.of(), List.of(), Map.of(), List.of(), List.of()
            );
            assertThat(cancelada.getStatusDetalhado()).isEqualTo("Cancelada");
        }

        @Test
        @DisplayName("Deve retornar status suspensa")
        void deveRetornarStatusSuspensa() {
            ApoliceDetailView suspensa = new ApoliceDetailView(
                "ap-013", "AP-2024-013", "Seguro Auto", StatusApolice.SUSPENSA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, List.of(), null, null, Map.of(),
                null, null, null, null, null, null, null, null, null,
                List.of(), List.of(), Map.of(), List.of(), List.of()
            );
            assertThat(suspensa.getStatusDetalhado()).isEqualTo("Suspensa");
        }

        @Test
        @DisplayName("Deve retornar status vencida")
        void deveRetornarStatusVencida() {
            ApoliceDetailView vencida = new ApoliceDetailView(
                "ap-014", "AP-2024-014", "Seguro Auto", StatusApolice.VENCIDA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, List.of(), null, null, Map.of(),
                null, null, null, null, null, null, null, null, null,
                List.of(), List.of(), Map.of(), List.of(), List.of()
            );
            assertThat(vencida.getStatusDetalhado()).isEqualTo("Vencida");
        }

        @Test
        @DisplayName("Deve tratar status nulo")
        void deveTratarStatusNulo() {
            ApoliceDetailView statusNulo = new ApoliceDetailView(
                "ap-015", "AP-2024-015", "Seguro Auto", null,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, List.of(), null, null, Map.of(),
                null, null, null, null, null, null, null, null, null,
                List.of(), List.of(), Map.of(), List.of(), List.of()
            );
            assertThat(statusNulo.getStatusDetalhado()).isEqualTo("Status indefinido");
        }
    }

    @Nested
    @DisplayName("Período de Vigência")
    class PeriodoVigencia {

        @Test
        @DisplayName("Deve formatar período de vigência")
        void deveFormatarPeriodoVigencia() {
            LocalDate inicio = LocalDate.of(2024, 1, 1);
            LocalDate fim = LocalDate.of(2024, 12, 31);

            ApoliceDetailView comVigencia = new ApoliceDetailView(
                "ap-016", "AP-2024-016", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, inicio, fim, 365, false, 12, null, null, null,
                null, null, null, null, List.of(), null, null, Map.of(),
                null, null, null, null, null, null, null, null, null,
                List.of(), List.of(), Map.of(), List.of(), List.of()
            );

            assertThat(comVigencia.getPeriodoVigencia())
                .isEqualTo("2024-01-01 a 2024-12-31");
        }

        @Test
        @DisplayName("Deve tratar datas nulas")
        void deveTratarDatasNulas() {
            ApoliceDetailView semVigencia = new ApoliceDetailView(
                "ap-017", "AP-2024-017", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, List.of(), null, null, Map.of(),
                null, null, null, null, null, null, null, null, null,
                List.of(), List.of(), Map.of(), List.of(), List.of()
            );

            assertThat(semVigencia.getPeriodoVigencia())
                .isEqualTo("Período não definido");
        }
    }

    @Nested
    @DisplayName("Resumo Financeiro")
    class ResumoFinanceiro {

        @Test
        @DisplayName("Deve formatar resumo financeiro à vista")
        void deveFormatarResumoAVista() {
            ApoliceDetailView aVista = new ApoliceDetailView(
                "ap-018", "AP-2024-018", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now(), LocalDate.now().plusDays(365),
                365, false, 12, null, null, new BigDecimal("5000.00"),
                null, null, 1, null, List.of(), null, null, Map.of(),
                null, null, null, null, null, null, null, null, null,
                List.of(), List.of(), Map.of(), List.of(), List.of()
            );

            assertThat(aVista.getResumoFinanceiro())
                .isEqualTo("Total: R$ 5000,00");
        }

        @Test
        @DisplayName("Deve formatar resumo financeiro parcelado")
        void deveFormatarResumoParcelado() {
            assertThat(view.getResumoFinanceiro())
                .contains("Total: R$ 5000,00")
                .contains("12x R$ 416,67");
        }

        @Test
        @DisplayName("Deve tratar valor total nulo")
        void deveTratarValorTotalNulo() {
            ApoliceDetailView semValor = new ApoliceDetailView(
                "ap-019", "AP-2024-019", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, List.of(), null, null, Map.of(),
                null, null, null, null, null, null, null, null, null,
                List.of(), List.of(), Map.of(), List.of(), List.of()
            );

            assertThat(semValor.getResumoFinanceiro())
                .isEqualTo("Valores não definidos");
        }
    }

    @Nested
    @DisplayName("Alertas e Recomendações")
    class AlertasRecomendacoes {

        @Test
        @DisplayName("Deve obter alertas ativos")
        void deveObterAlertasAtivos() {
            ApoliceDetailView comAlertas = new ApoliceDetailView(
                "ap-020", "AP-2024-020", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now(), LocalDate.now().plusDays(20),
                20, true, 12, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, true, 30,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of("Alerta customizado"), List.of()
            );

            List<String> alertas = comAlertas.getAlertasAtivos();
            assertThat(alertas).contains("Vence em 20 dias");
            assertThat(alertas).contains("Score de renovação baixo");
            assertThat(alertas).contains("Alerta customizado");
        }

        @Test
        @DisplayName("Deve obter recomendações de ação")
        void deveObterRecomendacoesAcao() {
            ApoliceDetailView comRecomendacoes = new ApoliceDetailView(
                "ap-021", "AP-2024-021", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", "João Silva", "12345678901", null, null,
                null, null, LocalDate.now(), LocalDate.now().plusDays(20),
                20, true, 12, null, null, null, null, null, null, null,
                List.of(), null, null, Map.of(), null, null, null, true, 60,
                null, null, null, null, List.of(), List.of(),
                Map.of(), List.of(), List.of("Ação customizada")
            );

            List<String> recomendacoes = comRecomendacoes.getRecomendacoesAcao();
            assertThat(recomendacoes).contains("Iniciar processo de renovação");
            assertThat(recomendacoes).contains("Revisar condições para melhorar score");
            assertThat(recomendacoes).contains("Ação customizada");
        }
    }

    @Nested
    @DisplayName("Completude de Dados")
    class CompletudeDados {

        @Test
        @DisplayName("Deve identificar dados completos")
        void deveIdentificarDadosCompletos() {
            assertThat(view.temDadosCompletos()).isTrue();
        }

        @Test
        @DisplayName("Deve identificar dados incompletos")
        void deveIdentificarDadosIncompletos() {
            ApoliceDetailView incompleta = new ApoliceDetailView(
                "ap-022", "AP-2024-022", "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", null, "12345678901", null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, List.of(), null, null, Map.of(),
                null, null, null, null, null, null, null, null, null,
                List.of(), List.of(), Map.of(), List.of(), List.of()
            );

            assertThat(incompleta.temDadosCompletos()).isFalse();
        }

        @Test
        @DisplayName("Deve calcular nível de completude")
        void deveCalcularNivelCompletude() {
            assertThat(view.getNivelCompletude()).isGreaterThan(70);
        }

        @Test
        @DisplayName("Deve calcular nível de completude zero para dados vazios")
        void deveCalcularNivelZeroParaDadosVazios() {
            ApoliceDetailView vazia = new ApoliceDetailView(
                "ap-023", null, "Seguro Auto", StatusApolice.ATIVA,
                "seg-001", null, "12345678901", null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, List.of(), null, null, Map.of(),
                null, null, null, null, null, null, null, null, null,
                List.of(), List.of(), Map.of(), List.of(), List.of()
            );

            assertThat(vazia.getNivelCompletude()).isLessThan(50);
        }
    }
}
