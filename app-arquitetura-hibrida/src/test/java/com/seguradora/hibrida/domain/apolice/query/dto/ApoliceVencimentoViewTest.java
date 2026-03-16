package com.seguradora.hibrida.domain.apolice.query.dto;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para ApoliceVencimentoView.
 */
@DisplayName("ApoliceVencimentoView Tests")
class ApoliceVencimentoViewTest {

    private ApoliceVencimentoView view;

    @BeforeEach
    void setUp() {
        view = new ApoliceVencimentoView(
            "ap-001", "AP-2024-001", "Seguro Auto", StatusApolice.ATIVA,
            "João Silva", "12345678901", "11999999999", "joao@email.com",
            LocalDate.now().plusDays(30), 30, "MÉDIA", true, 85,
            "APROVADA", new BigDecimal("5000"), "Cartão", "Operador A", false
        );
    }

    @Nested
    @DisplayName("Cálculo de Prioridade")
    class CalculoPrioridade {

        @Test
        @DisplayName("Deve calcular VENCIDA para dias negativos")
        void deveCalcularVencida() {
            String prioridade = ApoliceVencimentoView.calcularPrioridade(-1);
            assertThat(prioridade).isEqualTo("VENCIDA");
        }

        @Test
        @DisplayName("Deve calcular VENCIDA para zero dias")
        void deveCalcularVencidaZero() {
            String prioridade = ApoliceVencimentoView.calcularPrioridade(0);
            assertThat(prioridade).isEqualTo("VENCIDA");
        }

        @Test
        @DisplayName("Deve calcular CRÍTICA para 7 dias ou menos")
        void deveCalcularCritica() {
            String prioridade = ApoliceVencimentoView.calcularPrioridade(7);
            assertThat(prioridade).isEqualTo("CRÍTICA");
        }

        @Test
        @DisplayName("Deve calcular ALTA para 15 dias ou menos")
        void deveCalcularAlta() {
            String prioridade = ApoliceVencimentoView.calcularPrioridade(15);
            assertThat(prioridade).isEqualTo("ALTA");
        }

        @Test
        @DisplayName("Deve calcular MÉDIA para 30 dias ou menos")
        void deveCalcularMedia() {
            String prioridade = ApoliceVencimentoView.calcularPrioridade(30);
            assertThat(prioridade).isEqualTo("MÉDIA");
        }

        @Test
        @DisplayName("Deve calcular BAIXA para mais de 30 dias")
        void deveCalcularBaixa() {
            String prioridade = ApoliceVencimentoView.calcularPrioridade(60);
            assertThat(prioridade).isEqualTo("BAIXA");
        }

        @Test
        @DisplayName("Deve retornar INDEFINIDA para null")
        void deveRetornarIndefinida() {
            String prioridade = ApoliceVencimentoView.calcularPrioridade(null);
            assertThat(prioridade).isEqualTo("INDEFINIDA");
        }
    }

    @Nested
    @DisplayName("Cálculo de Status de Renovação")
    class CalculoStatusRenovacao {

        @Test
        @DisplayName("Deve retornar MANUAL quando não é automática")
        void deveRetornarManual() {
            String status = ApoliceVencimentoView.calcularStatusRenovacao(false, 85);
            assertThat(status).isEqualTo("MANUAL");
        }

        @Test
        @DisplayName("Deve retornar PENDENTE_ANÁLISE quando score é nulo")
        void deveRetornarPendenteAnalise() {
            String status = ApoliceVencimentoView.calcularStatusRenovacao(true, null);
            assertThat(status).isEqualTo("PENDENTE_ANÁLISE");
        }

        @Test
        @DisplayName("Deve retornar APROVADA para score >= 80")
        void deveRetornarAprovada() {
            String status = ApoliceVencimentoView.calcularStatusRenovacao(true, 85);
            assertThat(status).isEqualTo("APROVADA");
        }

        @Test
        @DisplayName("Deve retornar PROVÁVEL para score >= 60")
        void deveRetornarProvavel() {
            String status = ApoliceVencimentoView.calcularStatusRenovacao(true, 70);
            assertThat(status).isEqualTo("PROVÁVEL");
        }

        @Test
        @DisplayName("Deve retornar DUVIDOSA para score >= 40")
        void deveRetornarDuvidosa() {
            String status = ApoliceVencimentoView.calcularStatusRenovacao(true, 50);
            assertThat(status).isEqualTo("DUVIDOSA");
        }

        @Test
        @DisplayName("Deve retornar REJEITADA para score < 40")
        void deveRetornarRejeitada() {
            String status = ApoliceVencimentoView.calcularStatusRenovacao(true, 30);
            assertThat(status).isEqualTo("REJEITADA");
        }

        @Test
        @DisplayName("Deve tratar renovação automática nula como false")
        void deveTratarRenovacaoNula() {
            String status = ApoliceVencimentoView.calcularStatusRenovacao(null, 85);
            assertThat(status).isEqualTo("MANUAL");
        }
    }

    @Nested
    @DisplayName("Precisa Ação Imediata")
    class PrecisaAcaoImediata {

        @Test
        @DisplayName("Deve precisar de ação quando vencida")
        void devePrecisarAcaoQuandoVencida() {
            boolean precisa = ApoliceVencimentoView.precisaAcaoImediata(-1, true, 85);
            assertThat(precisa).isTrue();
        }

        @Test
        @DisplayName("Deve precisar de ação quando vence hoje")
        void devePrecisarAcaoQuandoVenceHoje() {
            boolean precisa = ApoliceVencimentoView.precisaAcaoImediata(0, true, 85);
            assertThat(precisa).isTrue();
        }

        @Test
        @DisplayName("Deve precisar de ação quando vence em 7 dias sem renovação automática")
        void devePrecisarAcaoQuandoVence7DiasSemRenovacao() {
            boolean precisa = ApoliceVencimentoView.precisaAcaoImediata(7, false, 85);
            assertThat(precisa).isTrue();
        }

        @Test
        @DisplayName("Deve precisar de ação quando renovação automática com score baixo")
        void devePrecisarAcaoQuandoScoreBaixo() {
            boolean precisa = ApoliceVencimentoView.precisaAcaoImediata(30, true, 40);
            assertThat(precisa).isTrue();
        }

        @Test
        @DisplayName("Não deve precisar de ação quando tudo ok")
        void naoDevePrecisarAcaoQuandoOk() {
            boolean precisa = ApoliceVencimentoView.precisaAcaoImediata(30, true, 85);
            assertThat(precisa).isFalse();
        }

        @Test
        @DisplayName("Deve tratar dias nulo")
        void deveTratarDiasNulo() {
            boolean precisa = ApoliceVencimentoView.precisaAcaoImediata(null, true, 85);
            assertThat(precisa).isFalse();
        }
    }

    @Nested
    @DisplayName("Cor de Prioridade")
    class CorPrioridade {

        @Test
        @DisplayName("Deve retornar vermelho para VENCIDA")
        void deveRetornarVermelhoVencida() {
            ApoliceVencimentoView vencida = new ApoliceVencimentoView(
                "ap-002", "AP-2024-002", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().minusDays(1), -1, "VENCIDA", false, null,
                "MANUAL", null, null, null, false
            );
            assertThat(vencida.getCorPrioridade()).isEqualTo("#dc3545");
        }

        @Test
        @DisplayName("Deve retornar laranja escuro para CRÍTICA")
        void deveRetornarLaranjaCritica() {
            ApoliceVencimentoView critica = new ApoliceVencimentoView(
                "ap-003", "AP-2024-003", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(5), 5, "CRÍTICA", false, null,
                "MANUAL", null, null, null, true
            );
            assertThat(critica.getCorPrioridade()).isEqualTo("#fd7e14");
        }

        @Test
        @DisplayName("Deve retornar amarelo para ALTA")
        void deveRetornarAmareloAlta() {
            ApoliceVencimentoView alta = new ApoliceVencimentoView(
                "ap-004", "AP-2024-004", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(10), 10, "ALTA", false, null,
                "MANUAL", null, null, null, true
            );
            assertThat(alta.getCorPrioridade()).isEqualTo("#ffc107");
        }

        @Test
        @DisplayName("Deve retornar azul claro para MÉDIA")
        void deveRetornarAzulMedia() {
            assertThat(view.getCorPrioridade()).isEqualTo("#17a2b8");
        }

        @Test
        @DisplayName("Deve retornar verde para BAIXA")
        void deveRetornarVerdeBaixa() {
            ApoliceVencimentoView baixa = new ApoliceVencimentoView(
                "ap-005", "AP-2024-005", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(60), 60, "BAIXA", false, null,
                "MANUAL", null, null, null, false
            );
            assertThat(baixa.getCorPrioridade()).isEqualTo("#28a745");
        }

        @Test
        @DisplayName("Deve retornar cinza para prioridade desconhecida")
        void deveRetornarCinzaDesconhecida() {
            ApoliceVencimentoView desconhecida = new ApoliceVencimentoView(
                "ap-006", "AP-2024-006", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(30), 30, "OUTRA", false, null,
                "MANUAL", null, null, null, false
            );
            assertThat(desconhecida.getCorPrioridade()).isEqualTo("#6c757d");
        }
    }

    @Nested
    @DisplayName("Ícone de Status de Renovação")
    class IconeStatusRenovacao {

        @Test
        @DisplayName("Deve retornar check para APROVADA")
        void deveRetornarCheckAprovada() {
            assertThat(view.getIconeStatusRenovacao()).isEqualTo("✅");
        }

        @Test
        @DisplayName("Deve retornar círculo amarelo para PROVÁVEL")
        void deveRetornarCirculoProvavel() {
            ApoliceVencimentoView provavel = new ApoliceVencimentoView(
                "ap-007", "AP-2024-007", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(30), 30, "MÉDIA", true, 70,
                "PROVÁVEL", null, null, null, false
            );
            assertThat(provavel.getIconeStatusRenovacao()).isEqualTo("🟡");
        }

        @Test
        @DisplayName("Deve retornar aviso para DUVIDOSA")
        void deveRetornarAvisoDuvidosa() {
            ApoliceVencimentoView duvidosa = new ApoliceVencimentoView(
                "ap-008", "AP-2024-008", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(30), 30, "MÉDIA", true, 50,
                "DUVIDOSA", null, null, null, false
            );
            assertThat(duvidosa.getIconeStatusRenovacao()).isEqualTo("⚠️");
        }

        @Test
        @DisplayName("Deve retornar X para REJEITADA")
        void deveRetornarXRejeitada() {
            ApoliceVencimentoView rejeitada = new ApoliceVencimentoView(
                "ap-009", "AP-2024-009", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(30), 30, "MÉDIA", true, 30,
                "REJEITADA", null, null, null, true
            );
            assertThat(rejeitada.getIconeStatusRenovacao()).isEqualTo("❌");
        }

        @Test
        @DisplayName("Deve retornar pessoa para MANUAL")
        void deveRetornarPessoaManual() {
            ApoliceVencimentoView manual = new ApoliceVencimentoView(
                "ap-010", "AP-2024-010", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(30), 30, "MÉDIA", false, null,
                "MANUAL", null, null, null, false
            );
            assertThat(manual.getIconeStatusRenovacao()).isEqualTo("👤");
        }

        @Test
        @DisplayName("Deve retornar ampulheta para status desconhecido")
        void deveRetornarAmpulhetaDesconhecido() {
            ApoliceVencimentoView desconhecido = new ApoliceVencimentoView(
                "ap-011", "AP-2024-011", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(30), 30, "MÉDIA", true, null,
                "PENDENTE", null, null, null, false
            );
            assertThat(desconhecido.getIconeStatusRenovacao()).isEqualTo("⏳");
        }
    }

    @Nested
    @DisplayName("Ação Recomendada")
    class AcaoRecomendada {

        @Test
        @DisplayName("Deve recomendar verificação quando dias é nulo")
        void deveRecomendarVerificacao() {
            ApoliceVencimentoView semDias = new ApoliceVencimentoView(
                "ap-012", "AP-2024-012", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(30), null, "MÉDIA", false, null,
                "MANUAL", null, null, null, false
            );
            assertThat(semDias.getAcaoRecomendada())
                .isEqualTo("Verificar data de vencimento");
        }

        @Test
        @DisplayName("Deve recomendar reativação quando vencida")
        void deveRecomendarReativacao() {
            ApoliceVencimentoView vencida = new ApoliceVencimentoView(
                "ap-013", "AP-2024-013", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().minusDays(1), -1, "VENCIDA", false, null,
                "MANUAL", null, null, null, false
            );
            assertThat(vencida.getAcaoRecomendada())
                .isEqualTo("Apólice vencida - Verificar reativação");
        }

        @Test
        @DisplayName("Deve informar renovação automática em andamento")
        void deveInformarRenovacaoEmAndamento() {
            ApoliceVencimentoView renovacaoOk = new ApoliceVencimentoView(
                "ap-014", "AP-2024-014", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(5), 5, "CRÍTICA", true, 85,
                "APROVADA", null, null, null, false
            );
            assertThat(renovacaoOk.getAcaoRecomendada())
                .isEqualTo("Renovação automática em andamento");
        }

        @Test
        @DisplayName("Deve recomendar verificação de aprovação")
        void deveRecomendarVerificacaoAprovacao() {
            ApoliceVencimentoView aguardando = new ApoliceVencimentoView(
                "ap-015", "AP-2024-015", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(5), 5, "CRÍTICA", true, 60,
                "PROVÁVEL", null, null, null, false
            );
            assertThat(aguardando.getAcaoRecomendada())
                .isEqualTo("Verificar aprovação da renovação");
        }

        @Test
        @DisplayName("Deve recomendar contato com segurado")
        void deveRecomendarContatoSegurado() {
            ApoliceVencimentoView manual = new ApoliceVencimentoView(
                "ap-016", "AP-2024-016", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(5), 5, "CRÍTICA", false, null,
                "MANUAL", null, null, null, false
            );
            assertThat(manual.getAcaoRecomendada())
                .isEqualTo("Contatar segurado para renovação");
        }

        @Test
        @DisplayName("Deve recomendar preparo para renovação")
        void deveRecomendarPreparo() {
            ApoliceVencimentoView preparo = new ApoliceVencimentoView(
                "ap-017", "AP-2024-017", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(15), 15, "ALTA", false, null,
                "MANUAL", null, null, null, false
            );
            assertThat(preparo.getAcaoRecomendada())
                .isEqualTo("Preparar processo de renovação");
        }

        @Test
        @DisplayName("Deve recomendar agendamento de contato")
        void deveRecomendarAgendamento() {
            assertThat(view.getAcaoRecomendada())
                .isEqualTo("Agendar contato para renovação");
        }

        @Test
        @DisplayName("Deve recomendar monitoramento")
        void deveRecomendarMonitoramento() {
            ApoliceVencimentoView monitor = new ApoliceVencimentoView(
                "ap-018", "AP-2024-018", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(60), 60, "BAIXA", false, null,
                "MANUAL", null, null, null, false
            );
            assertThat(monitor.getAcaoRecomendada())
                .isEqualTo("Monitorar vencimento");
        }
    }

    @Nested
    @DisplayName("Elegibilidade para Renovação Automática")
    class ElegibilidadeRenovacao {

        @Test
        @DisplayName("Deve ser elegível quando renovação automática ativa com score alto")
        void deveSerElegivel() {
            assertThat(view.isElegivelRenovacaoAutomatica()).isTrue();
        }

        @Test
        @DisplayName("Não deve ser elegível sem renovação automática")
        void naoDeveSerElegivelSemRenovacao() {
            ApoliceVencimentoView semRenovacao = new ApoliceVencimentoView(
                "ap-019", "AP-2024-019", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(30), 30, "MÉDIA", false, 85,
                "MANUAL", null, null, null, false
            );
            assertThat(semRenovacao.isElegivelRenovacaoAutomatica()).isFalse();
        }

        @Test
        @DisplayName("Não deve ser elegível com score nulo")
        void naoDeveSerElegivelComScoreNulo() {
            ApoliceVencimentoView scoreNulo = new ApoliceVencimentoView(
                "ap-020", "AP-2024-020", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(30), 30, "MÉDIA", true, null,
                "PENDENTE_ANÁLISE", null, null, null, false
            );
            assertThat(scoreNulo.isElegivelRenovacaoAutomatica()).isFalse();
        }

        @Test
        @DisplayName("Não deve ser elegível com score baixo")
        void naoDeveSerElegivelComScoreBaixo() {
            ApoliceVencimentoView scoreBaixo = new ApoliceVencimentoView(
                "ap-021", "AP-2024-021", "Seguro Auto", StatusApolice.ATIVA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(30), 30, "MÉDIA", true, 50,
                "DUVIDOSA", null, null, null, false
            );
            assertThat(scoreBaixo.isElegivelRenovacaoAutomatica()).isFalse();
        }

        @Test
        @DisplayName("Não deve ser elegível quando não está ativa")
        void naoDeveSerElegivelQuandoNaoAtiva() {
            ApoliceVencimentoView cancelada = new ApoliceVencimentoView(
                "ap-022", "AP-2024-022", "Seguro Auto", StatusApolice.CANCELADA,
                "João Silva", "12345678901", null, null,
                LocalDate.now().plusDays(30), 30, "MÉDIA", true, 85,
                "MANUAL", null, null, null, false
            );
            assertThat(cancelada.isElegivelRenovacaoAutomatica()).isFalse();
        }
    }
}
