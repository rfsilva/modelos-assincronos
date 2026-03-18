package com.seguradora.hibrida.domain.workflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StatusEtapa Tests")
class StatusEtapaTest {

    @Test
    @DisplayName("Deve ter 6 valores")
    void shouldHaveSixValues() {
        assertThat(StatusEtapa.values()).hasSize(6);
    }

    // =========================================================================
    // isFinal
    // =========================================================================

    @Nested
    @DisplayName("isFinal()")
    class IsFinal {

        @Test
        @DisplayName("CONCLUIDA e CANCELADA devem ser finais")
        void concludedAndCancelledShouldBeFinal() {
            assertThat(StatusEtapa.CONCLUIDA.isFinal()).isTrue();
            assertThat(StatusEtapa.CANCELADA.isFinal()).isTrue();
        }

        @Test
        @DisplayName("PENDENTE, EM_ANDAMENTO, FALHADA, TIMEOUT não devem ser finais")
        void activeStatusesShouldNotBeFinal() {
            assertThat(StatusEtapa.PENDENTE.isFinal()).isFalse();
            assertThat(StatusEtapa.EM_ANDAMENTO.isFinal()).isFalse();
            assertThat(StatusEtapa.FALHADA.isFinal()).isFalse();
            assertThat(StatusEtapa.TIMEOUT.isFinal()).isFalse();
        }
    }

    // =========================================================================
    // podeRetry
    // =========================================================================

    @Nested
    @DisplayName("podeRetry()")
    class PodeRetry {

        @Test
        @DisplayName("EM_ANDAMENTO, FALHADA, TIMEOUT devem permitir retry")
        void shouldAllowRetry() {
            assertThat(StatusEtapa.EM_ANDAMENTO.podeRetry()).isTrue();
            assertThat(StatusEtapa.FALHADA.podeRetry()).isTrue();
            assertThat(StatusEtapa.TIMEOUT.podeRetry()).isTrue();
        }

        @Test
        @DisplayName("PENDENTE, CONCLUIDA, CANCELADA não devem permitir retry")
        void shouldNotAllowRetry() {
            assertThat(StatusEtapa.PENDENTE.podeRetry()).isFalse();
            assertThat(StatusEtapa.CONCLUIDA.podeRetry()).isFalse();
            assertThat(StatusEtapa.CANCELADA.podeRetry()).isFalse();
        }
    }

    // =========================================================================
    // podeTransicionar
    // =========================================================================

    @Nested
    @DisplayName("podeTransicionar()")
    class PodeTransicionar {

        @Test
        @DisplayName("PENDENTE, EM_ANDAMENTO, FALHADA, TIMEOUT devem poder transicionar")
        void shouldAllowTransition() {
            assertThat(StatusEtapa.PENDENTE.podeTransicionar()).isTrue();
            assertThat(StatusEtapa.EM_ANDAMENTO.podeTransicionar()).isTrue();
            assertThat(StatusEtapa.FALHADA.podeTransicionar()).isTrue();
            assertThat(StatusEtapa.TIMEOUT.podeTransicionar()).isTrue();
        }

        @Test
        @DisplayName("CONCLUIDA e CANCELADA não devem poder transicionar")
        void finalStatusesShouldNotAllowTransition() {
            assertThat(StatusEtapa.CONCLUIDA.podeTransicionar()).isFalse();
            assertThat(StatusEtapa.CANCELADA.podeTransicionar()).isFalse();
        }
    }

    // =========================================================================
    // isSucesso
    // =========================================================================

    @Nested
    @DisplayName("isSucesso()")
    class IsSucesso {

        @Test
        @DisplayName("Apenas CONCLUIDA deve indicar sucesso")
        void onlyConcludedShouldBeSuccess() {
            assertThat(StatusEtapa.CONCLUIDA.isSucesso()).isTrue();
            assertThat(StatusEtapa.PENDENTE.isSucesso()).isFalse();
            assertThat(StatusEtapa.EM_ANDAMENTO.isSucesso()).isFalse();
            assertThat(StatusEtapa.FALHADA.isSucesso()).isFalse();
            assertThat(StatusEtapa.TIMEOUT.isSucesso()).isFalse();
            assertThat(StatusEtapa.CANCELADA.isSucesso()).isFalse();
        }
    }

    // =========================================================================
    // isErro
    // =========================================================================

    @Nested
    @DisplayName("isErro()")
    class IsErro {

        @Test
        @DisplayName("FALHADA e TIMEOUT devem indicar erro")
        void failedAndTimeoutShouldBeError() {
            assertThat(StatusEtapa.FALHADA.isErro()).isTrue();
            assertThat(StatusEtapa.TIMEOUT.isErro()).isTrue();
        }

        @Test
        @DisplayName("Demais statuses não devem indicar erro")
        void otherStatusesShouldNotBeError() {
            assertThat(StatusEtapa.PENDENTE.isErro()).isFalse();
            assertThat(StatusEtapa.EM_ANDAMENTO.isErro()).isFalse();
            assertThat(StatusEtapa.CONCLUIDA.isErro()).isFalse();
            assertThat(StatusEtapa.CANCELADA.isErro()).isFalse();
        }
    }

    // =========================================================================
    // isAtiva
    // =========================================================================

    @Nested
    @DisplayName("isAtiva()")
    class IsAtiva {

        @Test
        @DisplayName("EM_ANDAMENTO e PENDENTE devem ser ativos")
        void inProgressAndPendingShouldBeActive() {
            assertThat(StatusEtapa.EM_ANDAMENTO.isAtiva()).isTrue();
            assertThat(StatusEtapa.PENDENTE.isAtiva()).isTrue();
        }

        @Test
        @DisplayName("Demais statuses não devem ser ativos")
        void otherStatusesShouldNotBeActive() {
            assertThat(StatusEtapa.CONCLUIDA.isAtiva()).isFalse();
            assertThat(StatusEtapa.FALHADA.isAtiva()).isFalse();
            assertThat(StatusEtapa.TIMEOUT.isAtiva()).isFalse();
            assertThat(StatusEtapa.CANCELADA.isAtiva()).isFalse();
        }
    }

    // =========================================================================
    // podeAvancar
    // =========================================================================

    @Nested
    @DisplayName("podeAvancar()")
    class PodeAvancar {

        @Test
        @DisplayName("Apenas CONCLUIDA deve poder avançar")
        void onlyConcludedShouldAdvance() {
            assertThat(StatusEtapa.CONCLUIDA.podeAvancar()).isTrue();
            assertThat(StatusEtapa.PENDENTE.podeAvancar()).isFalse();
            assertThat(StatusEtapa.EM_ANDAMENTO.podeAvancar()).isFalse();
            assertThat(StatusEtapa.FALHADA.podeAvancar()).isFalse();
            assertThat(StatusEtapa.TIMEOUT.podeAvancar()).isFalse();
            assertThat(StatusEtapa.CANCELADA.podeAvancar()).isFalse();
        }
    }
}
