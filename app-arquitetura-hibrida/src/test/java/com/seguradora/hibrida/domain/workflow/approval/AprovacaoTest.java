package com.seguradora.hibrida.domain.workflow.approval;

import com.seguradora.hibrida.domain.workflow.model.NivelAprovacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Aprovacao Tests")
class AprovacaoTest {

    private Aprovacao aprovacaoPendente() {
        return Aprovacao.builder()
                .id("aprov-001")
                .workflowInstanceId("wi-001")
                .sinistroId("SIN-001")
                .nivel(NivelAprovacao.NIVEL_1_ANALISTA)
                .status(Aprovacao.StatusAprovacao.PENDENTE)
                .aprovadores(List.of("ANALISTA_001", "ANALISTA_002"))
                .valorSinistro(new BigDecimal("5000.00"))
                .dataSolicitacao(LocalDateTime.now())
                .build();
    }

    // =========================================================================
    // StatusAprovacao / DecisaoAprovacao enums
    // =========================================================================

    @Test
    @DisplayName("StatusAprovacao deve ter 5 valores")
    void statusAprovacaoShouldHaveFiveValues() {
        assertThat(Aprovacao.StatusAprovacao.values()).hasSize(5);
    }

    @Test
    @DisplayName("DecisaoAprovacao deve ter 3 valores")
    void decisaoAprovacaoShouldHaveThreeValues() {
        assertThat(Aprovacao.DecisaoAprovacao.values()).hasSize(3);
    }

    // =========================================================================
    // aprovar
    // =========================================================================

    @Nested
    @DisplayName("aprovar()")
    class Aprovar {

        @Test
        @DisplayName("Deve aprovar quando aprovador autorizado")
        void shouldApproveWithAuthorizedAprovador() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.aprovar("ANALISTA_001", "João", "Aprovado sem ressalvas");
            assertThat(aprov.isAprovada()).isTrue();
            assertThat(aprov.getDecisao()).isEqualTo(Aprovacao.DecisaoAprovacao.APROVADO);
            assertThat(aprov.getAprovadorDecisaoId()).isEqualTo("ANALISTA_001");
            assertThat(aprov.getJustificativa()).isEqualTo("Aprovado sem ressalvas");
            assertThat(aprov.getDataDecisao()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção quando aprovador não autorizado")
        void shouldThrowWhenAprovadorNotAuthorized() {
            Aprovacao aprov = aprovacaoPendente();
            assertThatThrownBy(() -> aprov.aprovar("NAO_AUTORIZADO", "Nome", "Just"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não autorizado");
        }

        @Test
        @DisplayName("Deve lançar exceção quando aprovação já decidida")
        void shouldThrowWhenAlreadyDecided() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.aprovar("ANALISTA_001", "João", "OK");
            assertThatThrownBy(() -> aprov.aprovar("ANALISTA_002", "Maria", "OK"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("decidida");
        }

        @Test
        @DisplayName("Deve lançar exceção quando aprovação expirada")
        void shouldThrowWhenExpired() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.expirar();
            assertThatThrownBy(() -> aprov.aprovar("ANALISTA_001", "João", "OK"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("expirada");
        }
    }

    // =========================================================================
    // rejeitar
    // =========================================================================

    @Nested
    @DisplayName("rejeitar()")
    class Rejeitar {

        @Test
        @DisplayName("Deve rejeitar quando aprovador autorizado")
        void shouldRejectWithAuthorizedAprovador() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.rejeitar("ANALISTA_001", "João", "Documentação incompleta");
            assertThat(aprov.isRejeitada()).isTrue();
            assertThat(aprov.getDecisao()).isEqualTo(Aprovacao.DecisaoAprovacao.REJEITADO);
            assertThat(aprov.getJustificativa()).isEqualTo("Documentação incompleta");
        }

        @Test
        @DisplayName("Deve lançar exceção quando aprovador não autorizado")
        void shouldThrowWhenNotAuthorized() {
            Aprovacao aprov = aprovacaoPendente();
            assertThatThrownBy(() -> aprov.rejeitar("DESCONHECIDO", "X", "Motivo"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // =========================================================================
    // delegar
    // =========================================================================

    @Nested
    @DisplayName("delegar()")
    class Delegar {

        @Test
        @DisplayName("Deve delegar para novos aprovadores quando PENDENTE")
        void shouldDelegateFromPending() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.delegar(List.of("SUPERVISOR_001", "SUPERVISOR_002"));
            assertThat(aprov.getAprovadores()).containsExactlyInAnyOrder("SUPERVISOR_001", "SUPERVISOR_002");
            assertThat(aprov.isPendente()).isTrue();
        }

        @Test
        @DisplayName("Deve delegar quando EM_ANALISE")
        void shouldDelegateFromInAnalysis() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.iniciarAnalise("ANALISTA_001");
            aprov.delegar(List.of("SUPERVISOR_001"));
            assertThat(aprov.getAprovadores()).containsExactly("SUPERVISOR_001");
        }

        @Test
        @DisplayName("Deve lançar exceção quando aprovação já decidida")
        void shouldThrowWhenDecided() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.aprovar("ANALISTA_001", "João", "OK");
            assertThatThrownBy(() -> aprov.delegar(List.of("OUTRO")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para lista vazia")
        void shouldThrowForEmptyList() {
            Aprovacao aprov = aprovacaoPendente();
            assertThatThrownBy(() -> aprov.delegar(List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // =========================================================================
    // expirar
    // =========================================================================

    @Nested
    @DisplayName("expirar()")
    class Expirar {

        @Test
        @DisplayName("Deve expirar aprovação PENDENTE")
        void shouldExpirePendingAprovacao() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.expirar();
            assertThat(aprov.isExpirada()).isTrue();
            assertThat(aprov.getStatus()).isEqualTo(Aprovacao.StatusAprovacao.EXPIRADA);
        }

        @Test
        @DisplayName("Deve expirar aprovação EM_ANALISE")
        void shouldExpireInAnalysisAprovacao() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.iniciarAnalise("ANALISTA_001");
            aprov.expirar();
            assertThat(aprov.isExpirada()).isTrue();
        }

        @Test
        @DisplayName("Não deve ter efeito se já decidida")
        void shouldHaveNoEffectIfAlreadyDecided() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.aprovar("ANALISTA_001", "João", "OK");
            aprov.expirar(); // Sem exceção
            assertThat(aprov.isAprovada()).isTrue();
        }
    }

    // =========================================================================
    // iniciarAnalise
    // =========================================================================

    @Nested
    @DisplayName("iniciarAnalise()")
    class IniciarAnalise {

        @Test
        @DisplayName("Deve iniciar análise quando PENDENTE e aprovador autorizado")
        void shouldStartAnalysis() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.iniciarAnalise("ANALISTA_001");
            assertThat(aprov.isEmAnalise()).isTrue();
        }

        @Test
        @DisplayName("Deve lançar exceção se não PENDENTE")
        void shouldThrowIfNotPending() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.iniciarAnalise("ANALISTA_001");
            assertThatThrownBy(() -> aprov.iniciarAnalise("ANALISTA_002"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção se aprovador não está na lista")
        void shouldThrowIfAprovadorNotInList() {
            Aprovacao aprov = aprovacaoPendente();
            assertThatThrownBy(() -> aprov.iniciarAnalise("GERENTE_001"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // =========================================================================
    // escalar
    // =========================================================================

    @Nested
    @DisplayName("escalar()")
    class Escalar {

        @Test
        @DisplayName("Deve escalar para próximo nível")
        void shouldEscalateToNextLevel() {
            Aprovacao aprov = aprovacaoPendente();
            aprov.escalar(List.of("SUPERVISOR_001"));
            assertThat(aprov.getNivel()).isEqualTo(NivelAprovacao.NIVEL_2_SUPERVISOR);
            assertThat(aprov.getAprovadores()).containsExactly("SUPERVISOR_001");
            assertThat(aprov.isPendente()).isTrue();
        }

        @Test
        @DisplayName("Deve lançar exceção se no nível máximo")
        void shouldThrowIfAtMaxLevel() {
            Aprovacao aprov = Aprovacao.builder()
                    .id("aprov-max")
                    .workflowInstanceId("wi-001")
                    .sinistroId("SIN-001")
                    .nivel(NivelAprovacao.NIVEL_4_DIRETOR)
                    .status(Aprovacao.StatusAprovacao.PENDENTE)
                    .aprovadores(List.of("DIRETOR_001"))
                    .dataSolicitacao(LocalDateTime.now())
                    .build();
            assertThatThrownBy(() -> aprov.escalar(List.of("OUTRO")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("máximo");
        }
    }

    // =========================================================================
    // excedeuPrazo / isAtiva
    // =========================================================================

    @Test
    @DisplayName("excedeuPrazo deve retornar false quando dataLimite é null")
    void excedeuPrazoShouldReturnFalseWhenNoLimit() {
        assertThat(aprovacaoPendente().excedeuPrazo()).isFalse();
    }

    @Test
    @DisplayName("excedeuPrazo deve retornar true quando dataLimite passou")
    void excedeuPrazoShouldReturnTrueWhenPassed() {
        Aprovacao aprov = aprovacaoPendente();
        aprov.setDataLimite(LocalDateTime.now().minusHours(1));
        assertThat(aprov.excedeuPrazo()).isTrue();
    }

    @Test
    @DisplayName("isAtiva deve retornar true para PENDENTE e EM_ANALISE")
    void isAtivaShouldReturnTrueForPendingAndInAnalysis() {
        assertThat(aprovacaoPendente().isAtiva()).isTrue();
        Aprovacao emAnalise = aprovacaoPendente();
        emAnalise.iniciarAnalise("ANALISTA_001");
        assertThat(emAnalise.isAtiva()).isTrue();
    }

    @Test
    @DisplayName("isAtiva deve retornar false para estados finais")
    void isAtivaShouldReturnFalseForFinalStates() {
        Aprovacao aprovada = aprovacaoPendente();
        aprovada.aprovar("ANALISTA_001", "João", "OK");
        assertThat(aprovada.isAtiva()).isFalse();
    }

    // =========================================================================
    // adicionarObservacao
    // =========================================================================

    @Test
    @DisplayName("adicionarObservacao deve acumular observações")
    void adicionarObservacaoShouldAccumulateObservations() {
        Aprovacao aprov = aprovacaoPendente();
        aprov.adicionarObservacao("Obs 1");
        assertThat(aprov.getObservacoes()).isEqualTo("Obs 1");
        aprov.adicionarObservacao("Obs 2");
        assertThat(aprov.getObservacoes()).contains("Obs 1").contains("Obs 2");
    }

    @Test
    @DisplayName("toString deve retornar representação não nula")
    void toStringShouldReturnNonNull() {
        assertThat(aprovacaoPendente().toString()).isNotNull();
    }
}
