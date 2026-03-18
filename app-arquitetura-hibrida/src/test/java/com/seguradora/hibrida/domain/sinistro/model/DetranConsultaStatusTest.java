package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DetranConsultaStatus Tests")
class DetranConsultaStatusTest {

    @Test
    @DisplayName("Deve ter 5 valores")
    void shouldHaveFiveValues() {
        assertThat(DetranConsultaStatus.values()).hasSize(5);
    }

    @Test
    @DisplayName("Todos os valores devem ter descricao e detalhamento")
    void allValuesShouldHaveDescricaoAndDetalhamento() {
        for (DetranConsultaStatus s : DetranConsultaStatus.values()) {
            assertThat(s.getDescricao()).isNotBlank();
            assertThat(s.getDetalhamento()).isNotBlank();
        }
    }

    @Test
    @DisplayName("CONCLUIDA, FALHADA, TIMEOUT devem ser finais")
    void finalStatusShouldBeFinal() {
        assertThat(DetranConsultaStatus.CONCLUIDA.isFinal()).isTrue();
        assertThat(DetranConsultaStatus.FALHADA.isFinal()).isTrue();
        assertThat(DetranConsultaStatus.TIMEOUT.isFinal()).isTrue();
    }

    @Test
    @DisplayName("PENDENTE e EM_ANDAMENTO não devem ser finais")
    void nonFinalStatusShouldNotBeFinal() {
        assertThat(DetranConsultaStatus.PENDENTE.isFinal()).isFalse();
        assertThat(DetranConsultaStatus.EM_ANDAMENTO.isFinal()).isFalse();
    }

    @Test
    @DisplayName("FALHADA e TIMEOUT devem fazer retry")
    void falhadaAndTimeoutShouldRetry() {
        assertThat(DetranConsultaStatus.FALHADA.deveRetry()).isTrue();
        assertThat(DetranConsultaStatus.TIMEOUT.deveRetry()).isTrue();
    }

    @Test
    @DisplayName("PENDENTE, EM_ANDAMENTO, CONCLUIDA não devem fazer retry")
    void otherStatusShouldNotRetry() {
        assertThat(DetranConsultaStatus.PENDENTE.deveRetry()).isFalse();
        assertThat(DetranConsultaStatus.EM_ANDAMENTO.deveRetry()).isFalse();
        assertThat(DetranConsultaStatus.CONCLUIDA.deveRetry()).isFalse();
    }
}
