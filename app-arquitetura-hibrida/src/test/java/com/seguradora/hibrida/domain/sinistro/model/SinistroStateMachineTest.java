package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroStateMachine Tests")
class SinistroStateMachineTest {

    @Test
    @DisplayName("podeTransicionar deve retornar false para status nulo")
    void podeTransicionarShouldReturnFalseForNullStatus() {
        assertThat(SinistroStateMachine.podeTransicionar(null, StatusSinistro.VALIDADO)).isFalse();
        assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.NOVO, null)).isFalse();
    }

    @Test
    @DisplayName("NOVO pode transicionar para VALIDADO e ARQUIVADO")
    void novoCanTransitionToValidadoAndArquivado() {
        assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.NOVO, StatusSinistro.VALIDADO)).isTrue();
        assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.NOVO, StatusSinistro.ARQUIVADO)).isTrue();
    }

    @Test
    @DisplayName("NOVO não pode transicionar para EM_ANALISE diretamente")
    void novoCannotTransitionToEmAnalise() {
        assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.NOVO, StatusSinistro.EM_ANALISE)).isFalse();
    }

    @Test
    @DisplayName("VALIDADO pode transicionar para EM_ANALISE e ARQUIVADO")
    void validadoCanTransitionToEmAnalise() {
        assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.VALIDADO, StatusSinistro.EM_ANALISE)).isTrue();
        assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.VALIDADO, StatusSinistro.ARQUIVADO)).isTrue();
    }

    @Test
    @DisplayName("EM_ANALISE pode transicionar para múltiplos estados")
    void emAnaliseCanTransitionToMultipleStates() {
        assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.EM_ANALISE, StatusSinistro.AGUARDANDO_DETRAN)).isTrue();
        assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.EM_ANALISE, StatusSinistro.DADOS_COLETADOS)).isTrue();
        assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.EM_ANALISE, StatusSinistro.APROVADO)).isTrue();
        assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.EM_ANALISE, StatusSinistro.REPROVADO)).isTrue();
        assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.EM_ANALISE, StatusSinistro.ARQUIVADO)).isTrue();
    }

    @Test
    @DisplayName("ARQUIVADO não pode transicionar para nenhum estado")
    void arquivadoCannotTransitionToAnything() {
        for (StatusSinistro s : StatusSinistro.values()) {
            assertThat(SinistroStateMachine.podeTransicionar(StatusSinistro.ARQUIVADO, s)).isFalse();
        }
    }

    @Test
    @DisplayName("getTransicoesPossiveis deve retornar set vazio para null")
    void getTransicoesPassiveisShouldReturnEmptyForNull() {
        assertThat(SinistroStateMachine.getTransicoesPossiveis(null)).isEmpty();
    }

    @Test
    @DisplayName("getTransicoesPossiveis para APROVADO deve conter PAGO e ARQUIVADO")
    void getTransicoesPassiveisShouldContainPagoForAprovado() {
        Set<StatusSinistro> transicoes = SinistroStateMachine.getTransicoesPossiveis(StatusSinistro.APROVADO);
        assertThat(transicoes).contains(StatusSinistro.PAGO, StatusSinistro.ARQUIVADO);
    }

    @Test
    @DisplayName("isEstadoFinal deve retornar true apenas para ARQUIVADO")
    void isEstadoFinalShouldReturnTrueOnlyForArquivado() {
        assertThat(SinistroStateMachine.isEstadoFinal(StatusSinistro.ARQUIVADO)).isTrue();
        assertThat(SinistroStateMachine.isEstadoFinal(StatusSinistro.NOVO)).isFalse();
        assertThat(SinistroStateMachine.isEstadoFinal(StatusSinistro.PAGO)).isFalse();
    }

    @Test
    @DisplayName("isEstadoFinal deve retornar false para null")
    void isEstadoFinalShouldReturnFalseForNull() {
        assertThat(SinistroStateMachine.isEstadoFinal(null)).isFalse();
    }

    @Test
    @DisplayName("getAcoesAutomaticas EM_ANALISE -> AGUARDANDO_DETRAN deve conter consulta Detran")
    void getAcoesAutomaticasShouldContainDetranForEmAnaliseToAguardando() {
        Set<String> acoes = SinistroStateMachine.getAcoesAutomaticas(
                StatusSinistro.EM_ANALISE, StatusSinistro.AGUARDANDO_DETRAN);
        assertThat(acoes).contains("DISPARAR_CONSULTA_DETRAN");
    }

    @Test
    @DisplayName("getAcoesAutomaticas DADOS_COLETADOS -> APROVADO deve conter calcular indenização")
    void getAcoesAutomaticasShouldContainCalcularForDadosColetadosToAprovado() {
        Set<String> acoes = SinistroStateMachine.getAcoesAutomaticas(
                StatusSinistro.DADOS_COLETADOS, StatusSinistro.APROVADO);
        assertThat(acoes).contains("CALCULAR_INDENIZACAO", "VALIDAR_ALADA");
    }

    @Test
    @DisplayName("getAcoesAutomaticas APROVADO -> PAGO deve conter processar pagamento")
    void getAcoesAutomaticasShouldContainProcessarPagamentoForAprovadoToPago() {
        Set<String> acoes = SinistroStateMachine.getAcoesAutomaticas(
                StatusSinistro.APROVADO, StatusSinistro.PAGO);
        assertThat(acoes).contains("PROCESSAR_PAGAMENTO", "ENVIAR_NOTIFICACAO");
    }

    @Test
    @DisplayName("getAcoesAutomaticas para qualquer -> ARQUIVADO deve conter finalizar")
    void getAcoesAutomaticasShouldContainFinalizarForAnyToArquivado() {
        Set<String> acoes = SinistroStateMachine.getAcoesAutomaticas(
                StatusSinistro.REPROVADO, StatusSinistro.ARQUIVADO);
        assertThat(acoes).contains("FINALIZAR_PROCESSAMENTO", "ARQUIVAR_DOCUMENTOS");
    }

    @Test
    @DisplayName("getAcoesAutomaticas deve retornar set vazio para transição sem ação")
    void getAcoesAutomaticasShouldReturnEmptyForNoAction() {
        Set<String> acoes = SinistroStateMachine.getAcoesAutomaticas(
                StatusSinistro.NOVO, StatusSinistro.VALIDADO);
        assertThat(acoes).isEmpty();
    }

    @Test
    @DisplayName("requerDadosAdicionais deve retornar true para APROVADO e REPROVADO")
    void requerDadosAdicionaisShouldReturnTrueForAprovadoAndReprovado() {
        assertThat(SinistroStateMachine.requerDadosAdicionais(StatusSinistro.APROVADO)).isTrue();
        assertThat(SinistroStateMachine.requerDadosAdicionais(StatusSinistro.REPROVADO)).isTrue();
    }

    @Test
    @DisplayName("requerDadosAdicionais deve retornar false para outros estados")
    void requerDadosAdicionaisShouldReturnFalseForOtherStates() {
        assertThat(SinistroStateMachine.requerDadosAdicionais(StatusSinistro.NOVO)).isFalse();
        assertThat(SinistroStateMachine.requerDadosAdicionais(StatusSinistro.VALIDADO)).isFalse();
        assertThat(SinistroStateMachine.requerDadosAdicionais(StatusSinistro.ARQUIVADO)).isFalse();
    }
}
