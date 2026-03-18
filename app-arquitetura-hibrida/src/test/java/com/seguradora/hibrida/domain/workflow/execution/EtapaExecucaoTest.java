package com.seguradora.hibrida.domain.workflow.execution;

import com.seguradora.hibrida.domain.workflow.model.StatusEtapa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EtapaExecucao Tests")
class EtapaExecucaoTest {

    private EtapaExecucao etapaPendente() {
        return EtapaExecucao.builder()
                .id("exec-001")
                .etapaId("etapa-01")
                .etapaNome("Validação Inicial")
                .status(StatusEtapa.PENDENTE)
                .build();
    }

    private EtapaExecucao etapaEmAndamento() {
        EtapaExecucao e = etapaPendente();
        e.iniciar();
        return e;
    }

    // =========================================================================
    // iniciar
    // =========================================================================

    @Nested
    @DisplayName("iniciar()")
    class Iniciar {

        @Test
        @DisplayName("Deve iniciar etapa pendente com responsável")
        void shouldStartPendingEtapaWithResponsavel() {
            EtapaExecucao etapa = etapaPendente();
            etapa.iniciar("USR-01", "João");
            assertThat(etapa.isEmAndamento()).isTrue();
            assertThat(etapa.getResponsavelId()).isEqualTo("USR-01");
            assertThat(etapa.getResponsavelNome()).isEqualTo("João");
            assertThat(etapa.getTentativas()).isEqualTo(1);
            assertThat(etapa.getInicioEm()).isNotNull();
        }

        @Test
        @DisplayName("iniciar() sem args deve usar SISTEMA como responsável")
        void iniciarWithoutArgsShouldUseSystem() {
            EtapaExecucao etapa = etapaPendente();
            etapa.iniciar();
            assertThat(etapa.isEmAndamento()).isTrue();
            assertThat(etapa.getResponsavelId()).isEqualTo("SISTEMA");
        }

        @Test
        @DisplayName("Deve lançar exceção se não estiver PENDENTE ou FALHADA")
        void shouldThrowIfNotPendingOrFailed() {
            EtapaExecucao etapa = etapaEmAndamento();
            assertThatThrownBy(etapa::iniciar)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Deve poder iniciar após FALHADA")
        void shouldStartAfterFailed() {
            EtapaExecucao etapa = etapaEmAndamento();
            etapa.falhar("Erro");
            etapa.iniciar();
            assertThat(etapa.isEmAndamento()).isTrue();
            assertThat(etapa.getTentativas()).isEqualTo(2);
        }
    }

    // =========================================================================
    // concluir
    // =========================================================================

    @Nested
    @DisplayName("concluir()")
    class Concluir {

        @Test
        @DisplayName("Deve concluir etapa em andamento")
        void shouldConcludeInProgressEtapa() {
            EtapaExecucao etapa = etapaEmAndamento();
            etapa.concluir("Resultado OK");
            assertThat(etapa.isConcluida()).isTrue();
            assertThat(etapa.getResultado()).isEqualTo("Resultado OK");
            assertThat(etapa.getFimEm()).isNotNull();
        }

        @Test
        @DisplayName("concluir() sem args deve usar mensagem padrão")
        void concluirWithoutArgsShouldUseDefaultMessage() {
            EtapaExecucao etapa = etapaEmAndamento();
            etapa.concluir();
            assertThat(etapa.isConcluida()).isTrue();
            assertThat(etapa.getResultado()).isNotBlank();
        }

        @Test
        @DisplayName("Deve lançar exceção se não estiver EM_ANDAMENTO")
        void shouldThrowIfNotInProgress() {
            EtapaExecucao etapa = etapaPendente();
            assertThatThrownBy(() -> etapa.concluir("Resultado"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // falhar
    // =========================================================================

    @Nested
    @DisplayName("falhar()")
    class Falhar {

        @Test
        @DisplayName("Deve marcar etapa como falhada")
        void shouldMarkEtapaAsFailed() {
            EtapaExecucao etapa = etapaEmAndamento();
            etapa.falhar("Erro de integração");
            assertThat(etapa.isFalhada()).isTrue();
            assertThat(etapa.getErroMensagem()).isEqualTo("Erro de integração");
            assertThat(etapa.getFimEm()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção se não estiver EM_ANDAMENTO")
        void shouldThrowIfNotInProgress() {
            EtapaExecucao etapa = etapaPendente();
            assertThatThrownBy(() -> etapa.falhar("Erro"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // timeout / cancelar
    // =========================================================================

    @Nested
    @DisplayName("timeout() / cancelar()")
    class TimeoutCancelar {

        @Test
        @DisplayName("timeout() deve marcar etapa com TIMEOUT")
        void timeoutShouldMarkEtapaAsTimeout() {
            EtapaExecucao etapa = etapaEmAndamento();
            etapa.timeout();
            assertThat(etapa.isTimeout()).isTrue();
            assertThat(etapa.getTimeoutEm()).isNotNull();
            assertThat(etapa.getErroMensagem()).isNotBlank();
        }

        @Test
        @DisplayName("cancelar() deve marcar etapa como cancelada")
        void cancelarShouldMarkEtapaAsCancelled() {
            EtapaExecucao etapa = etapaEmAndamento();
            etapa.cancelar("Cancelado pelo usuário");
            assertThat(etapa.isCancelada()).isTrue();
            assertThat(etapa.getObservacoes()).isEqualTo("Cancelado pelo usuário");
            assertThat(etapa.getFimEm()).isNotNull();
        }
    }

    // =========================================================================
    // retry
    // =========================================================================

    @Nested
    @DisplayName("retry()")
    class Retry {

        @Test
        @DisplayName("Deve fazer retry de etapa falhada")
        void shouldRetryFailedEtapa() {
            EtapaExecucao etapa = etapaEmAndamento();
            etapa.falhar("Erro");
            etapa.retry();
            assertThat(etapa.isPendente()).isTrue();
            assertThat(etapa.getErroMensagem()).isNull();
            assertThat(etapa.getFimEm()).isNull();
        }

        @Test
        @DisplayName("Deve fazer retry de etapa em andamento")
        void shouldRetryInProgressEtapa() {
            EtapaExecucao etapa = etapaEmAndamento();
            etapa.retry();
            assertThat(etapa.isPendente()).isTrue();
        }

        @Test
        @DisplayName("Deve lançar exceção para status que não permite retry (CONCLUIDA)")
        void shouldThrowForStatusThatDoesNotAllowRetry() {
            EtapaExecucao etapa = etapaEmAndamento();
            etapa.concluir();
            assertThatThrownBy(etapa::retry)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para CANCELADA")
        void shouldThrowForCancelled() {
            EtapaExecucao etapa = etapaEmAndamento();
            etapa.cancelar("Motivo");
            assertThatThrownBy(etapa::retry)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // Verificações de estado
    // =========================================================================

    @Nested
    @DisplayName("Verificações de estado")
    class StateChecks {

        @Test
        @DisplayName("isAtiva deve retornar true para PENDENTE e EM_ANDAMENTO")
        void isAtivaShouldReturnTrueForActiveStatuses() {
            EtapaExecucao pendente = etapaPendente();
            assertThat(pendente.isAtiva()).isTrue();

            EtapaExecucao emAndamento = etapaEmAndamento();
            assertThat(emAndamento.isAtiva()).isTrue();
        }

        @Test
        @DisplayName("isFinalizada deve retornar true para CONCLUIDA e CANCELADA")
        void isFinalizadaShouldReturnTrueForFinalStatuses() {
            EtapaExecucao concluida = etapaEmAndamento();
            concluida.concluir();
            assertThat(concluida.isFinalizada()).isTrue();

            EtapaExecucao cancelada = etapaEmAndamento();
            cancelada.cancelar("Motivo");
            assertThat(cancelada.isFinalizada()).isTrue();
        }

        @Test
        @DisplayName("hasResponsavel deve retornar true quando responsavel definido")
        void hasResponsavelShouldReturnTrueWhenDefined() {
            EtapaExecucao etapa = etapaPendente();
            etapa.iniciar("USR-01", "Maria");
            assertThat(etapa.hasResponsavel()).isTrue();
        }

        @Test
        @DisplayName("hasResponsavel deve retornar false quando não definido")
        void hasResponsavelShouldReturnFalseWhenNotDefined() {
            EtapaExecucao etapa = etapaPendente();
            assertThat(etapa.hasResponsavel()).isFalse();
        }

        @Test
        @DisplayName("podeRetry deve delegar para StatusEtapa")
        void podeRetryShouldDelegateToStatusEtapa() {
            EtapaExecucao falhada = etapaEmAndamento();
            falhada.falhar("Erro");
            assertThat(falhada.podeRetry()).isTrue();
        }
    }

    // =========================================================================
    // tempoExecucao
    // =========================================================================

    @Nested
    @DisplayName("tempoExecucao()")
    class TempoExecucao {

        @Test
        @DisplayName("tempoExecucao deve retornar ZERO quando sem início")
        void tempoExecucaoShouldReturnZeroWithNoStart() {
            EtapaExecucao etapa = etapaPendente();
            assertThat(etapa.tempoExecucao().isZero()).isTrue();
            assertThat(etapa.tempoExecucaoMinutos()).isEqualTo(0);
            assertThat(etapa.tempoExecucaoSegundos()).isEqualTo(0);
        }

        @Test
        @DisplayName("tempoExecucao deve retornar duração após início")
        void tempoExecucaoShouldReturnDurationAfterStart() {
            EtapaExecucao etapa = etapaEmAndamento();
            assertThat(etapa.tempoExecucao()).isNotNull();
        }

        @Test
        @DisplayName("excedeuTimeout deve retornar false sem início")
        void excedeuTimeoutShouldReturnFalseWithoutStart() {
            EtapaExecucao etapa = etapaPendente();
            assertThat(etapa.excedeuTimeout(10)).isFalse();
        }

        @Test
        @DisplayName("excedeuTimeout deve retornar false quando concluída")
        void excedeuTimeoutShouldReturnFalseWhenCompleted() {
            EtapaExecucao etapa = etapaEmAndamento();
            etapa.concluir();
            assertThat(etapa.excedeuTimeout(0)).isFalse();
        }
    }

    // =========================================================================
    // adicionarObservacao
    // =========================================================================

    @Test
    @DisplayName("adicionarObservacao deve acumular observações")
    void adicionarObservacaoShouldAccumulateObservations() {
        EtapaExecucao etapa = etapaPendente();
        etapa.adicionarObservacao("Obs 1");
        assertThat(etapa.getObservacoes()).isEqualTo("Obs 1");

        etapa.adicionarObservacao("Obs 2");
        assertThat(etapa.getObservacoes()).contains("Obs 1").contains("Obs 2");
    }

    @Test
    @DisplayName("toString deve retornar representação não nula")
    void toStringShouldReturnNonNull() {
        assertThat(etapaPendente().toString()).isNotNull();
    }
}
