package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StatusSinistro Tests")
class StatusSinistroTest {

    @Test
    @DisplayName("Deve ter 9 valores")
    void shouldHaveNineValues() {
        assertThat(StatusSinistro.values()).hasSize(9);
    }

    @Test
    @DisplayName("Todos os valores devem ter descricao e detalhamento")
    void allValuesShouldHaveDescricaoAndDetalhamento() {
        for (StatusSinistro s : StatusSinistro.values()) {
            assertThat(s.getDescricao()).isNotBlank();
            assertThat(s.getDetalhamento()).isNotBlank();
        }
    }

    @Test
    @DisplayName("ARQUIVADO deve ser estado final")
    void arquivadoShouldBeFinal() {
        assertThat(StatusSinistro.ARQUIVADO.isFinal()).isTrue();
    }

    @Test
    @DisplayName("Outros estados não devem ser finais")
    void otherStatusShouldNotBeFinal() {
        for (StatusSinistro s : StatusSinistro.values()) {
            if (s != StatusSinistro.ARQUIVADO) {
                assertThat(s.isFinal()).isFalse();
            }
        }
    }

    @Test
    @DisplayName("isAberto deve retornar false para ARQUIVADO e PAGO")
    void isAbertoShouldReturnFalseForArquivadoAndPago() {
        assertThat(StatusSinistro.ARQUIVADO.isAberto()).isFalse();
        assertThat(StatusSinistro.PAGO.isAberto()).isFalse();
    }

    @Test
    @DisplayName("isAberto deve retornar true para estados intermediários")
    void isAbertoShouldReturnTrueForIntermediateStatus() {
        assertThat(StatusSinistro.NOVO.isAberto()).isTrue();
        assertThat(StatusSinistro.VALIDADO.isAberto()).isTrue();
        assertThat(StatusSinistro.EM_ANALISE.isAberto()).isTrue();
        assertThat(StatusSinistro.APROVADO.isAberto()).isTrue();
    }

    @Test
    @DisplayName("NOVO deve transicionar para VALIDADO e ARQUIVADO")
    void novoShouldTransitionToValidadoAndArquivado() {
        assertThat(StatusSinistro.NOVO.podeTransicionarPara(StatusSinistro.VALIDADO)).isTrue();
        assertThat(StatusSinistro.NOVO.podeTransicionarPara(StatusSinistro.ARQUIVADO)).isTrue();
        assertThat(StatusSinistro.NOVO.podeTransicionarPara(StatusSinistro.EM_ANALISE)).isFalse();
    }

    @Test
    @DisplayName("VALIDADO deve transicionar para EM_ANALISE e ARQUIVADO")
    void validadoShouldTransitionCorrectly() {
        assertThat(StatusSinistro.VALIDADO.podeTransicionarPara(StatusSinistro.EM_ANALISE)).isTrue();
        assertThat(StatusSinistro.VALIDADO.podeTransicionarPara(StatusSinistro.ARQUIVADO)).isTrue();
        assertThat(StatusSinistro.VALIDADO.podeTransicionarPara(StatusSinistro.NOVO)).isFalse();
    }

    @Test
    @DisplayName("APROVADO deve transicionar para PAGO e ARQUIVADO")
    void aprovadoShouldTransitionToPagoAndArquivado() {
        assertThat(StatusSinistro.APROVADO.podeTransicionarPara(StatusSinistro.PAGO)).isTrue();
        assertThat(StatusSinistro.APROVADO.podeTransicionarPara(StatusSinistro.ARQUIVADO)).isTrue();
        assertThat(StatusSinistro.APROVADO.podeTransicionarPara(StatusSinistro.NOVO)).isFalse();
    }

    @Test
    @DisplayName("ARQUIVADO não deve transicionar para nenhum estado")
    void arquivadoShouldNotTransitionToAnyStatus() {
        for (StatusSinistro s : StatusSinistro.values()) {
            assertThat(StatusSinistro.ARQUIVADO.podeTransicionarPara(s)).isFalse();
        }
    }

    @Test
    @DisplayName("REPROVADO deve transicionar apenas para ARQUIVADO")
    void reprovadoShouldOnlyTransitionToArquivado() {
        assertThat(StatusSinistro.REPROVADO.podeTransicionarPara(StatusSinistro.ARQUIVADO)).isTrue();
        assertThat(StatusSinistro.REPROVADO.podeTransicionarPara(StatusSinistro.APROVADO)).isFalse();
    }
}
