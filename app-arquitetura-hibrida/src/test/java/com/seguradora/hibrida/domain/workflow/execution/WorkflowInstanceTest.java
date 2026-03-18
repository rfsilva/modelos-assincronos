package com.seguradora.hibrida.domain.workflow.execution;

import com.seguradora.hibrida.domain.workflow.model.StatusEtapa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkflowInstance Tests")
class WorkflowInstanceTest {

    private WorkflowInstance instanceIniciada() {
        return WorkflowInstance.builder()
                .id("wi-001")
                .definicaoId("def-001")
                .sinistroId("SIN-001")
                .status(WorkflowInstance.StatusWorkflowInstance.INICIADO)
                .inicioEm(LocalDateTime.now())
                .build();
    }

    private WorkflowInstance instanceEmAndamento() {
        WorkflowInstance wi = instanceIniciada();
        wi.avancar("etapa-01");
        return wi;
    }

    private EtapaExecucao etapaConcluida(String nome) {
        EtapaExecucao e = EtapaExecucao.builder()
                .id("ex-" + nome)
                .etapaId("et-" + nome)
                .etapaNome(nome)
                .status(StatusEtapa.PENDENTE)
                .build();
        e.iniciar();
        e.concluir();
        return e;
    }

    private EtapaExecucao etapaFalhada(String nome) {
        EtapaExecucao e = EtapaExecucao.builder()
                .id("ex-" + nome)
                .etapaId("et-" + nome)
                .etapaNome(nome)
                .status(StatusEtapa.PENDENTE)
                .build();
        e.iniciar();
        e.falhar("Erro simulado");
        return e;
    }

    @Test
    @DisplayName("Builder deve criar instância com todos os campos")
    void builderShouldCreateInstanceWithAllFields() {
        WorkflowInstance wi = instanceIniciada();
        assertThat(wi.getId()).isEqualTo("wi-001");
        assertThat(wi.getDefinicaoId()).isEqualTo("def-001");
        assertThat(wi.getSinistroId()).isEqualTo("SIN-001");
        assertThat(wi.getStatus()).isEqualTo(WorkflowInstance.StatusWorkflowInstance.INICIADO);
    }

    // =========================================================================
    // StatusWorkflowInstance enum
    // =========================================================================

    @Test
    @DisplayName("StatusWorkflowInstance deve ter 6 valores")
    void statusWorkflowInstanceShouldHaveSixValues() {
        assertThat(WorkflowInstance.StatusWorkflowInstance.values()).hasSize(6);
    }

    // =========================================================================
    // avancar / retroceder
    // =========================================================================

    @Nested
    @DisplayName("avancar() / retroceder()")
    class AvancarRetroceder {

        @Test
        @DisplayName("avancar deve atualizar etapaAtualId e status")
        void avancarShouldUpdateCurrentStageAndStatus() {
            WorkflowInstance wi = instanceIniciada();
            wi.avancar("etapa-02");
            assertThat(wi.getEtapaAtualId()).isEqualTo("etapa-02");
            assertThat(wi.isEmAndamento()).isTrue();
        }

        @Test
        @DisplayName("avancar deve lançar exceção se workflow estiver COMPLETO")
        void avancarShouldThrowIfCompleted() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.completar();
            assertThatThrownBy(() -> wi.avancar("etapa-03"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("avancar deve lançar exceção se workflow estiver CANCELADO")
        void avancarShouldThrowIfCancelled() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.cancelar("Motivo");
            assertThatThrownBy(() -> wi.avancar("etapa-03"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("retroceder deve atualizar etapaAtualId")
        void retrocederShouldUpdateCurrentStage() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.retroceder("etapa-00");
            assertThat(wi.getEtapaAtualId()).isEqualTo("etapa-00");
            assertThat(wi.isEmAndamento()).isTrue();
        }
    }

    // =========================================================================
    // cancelar / pausar / retomar / completar / falhar
    // =========================================================================

    @Nested
    @DisplayName("cancelar() / pausar() / retomar() / completar() / falhar()")
    class TransicaoEstado {

        @Test
        @DisplayName("cancelar deve marcar workflow como CANCELADO")
        void cancelarShouldMarkAsCancelled() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.cancelar("Usuário cancelou");
            assertThat(wi.isCancelado()).isTrue();
            assertThat(wi.getMotivoCancelamento()).isEqualTo("Usuário cancelou");
            assertThat(wi.getCanceladoEm()).isNotNull();
            assertThat(wi.getFimEm()).isNotNull();
        }

        @Test
        @DisplayName("pausar deve marcar workflow como PAUSADO")
        void pausarShouldMarkAsPaused() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.pausar();
            assertThat(wi.isPausado()).isTrue();
            assertThat(wi.getPausadoEm()).isNotNull();
        }

        @Test
        @DisplayName("pausar não deve ter efeito se não estiver EM_ANDAMENTO")
        void pausarShouldHaveNoEffectIfNotInProgress() {
            WorkflowInstance wi = instanceIniciada();
            wi.pausar();
            assertThat(wi.getStatus()).isEqualTo(WorkflowInstance.StatusWorkflowInstance.INICIADO);
        }

        @Test
        @DisplayName("retomar deve restaurar status EM_ANDAMENTO de PAUSADO")
        void retomarShouldResumeFromPaused() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.pausar();
            wi.retomar();
            assertThat(wi.isEmAndamento()).isTrue();
            assertThat(wi.getPausadoEm()).isNull();
        }

        @Test
        @DisplayName("retomar não deve ter efeito se não estiver PAUSADO")
        void retomarShouldHaveNoEffectIfNotPaused() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.retomar();
            assertThat(wi.isEmAndamento()).isTrue();
        }

        @Test
        @DisplayName("completar deve marcar workflow como COMPLETO com 100% progresso")
        void completarShouldMarkAsCompleted() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.completar();
            assertThat(wi.isCompleta()).isTrue();
            assertThat(wi.getProgressoPercentual()).isEqualTo(100);
            assertThat(wi.getFimEm()).isNotNull();
        }

        @Test
        @DisplayName("falhar deve marcar workflow como FALHADO")
        void falharShouldMarkAsFailed() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.falhar("Erro crítico");
            assertThat(wi.isFalhado()).isTrue();
            assertThat(wi.getMotivoCancelamento()).isEqualTo("Erro crítico");
            assertThat(wi.getFimEm()).isNotNull();
        }
    }

    // =========================================================================
    // isAtivo
    // =========================================================================

    @Nested
    @DisplayName("isAtivo()")
    class IsAtivo {

        @Test
        @DisplayName("Deve retornar true para INICIADO e EM_ANDAMENTO")
        void shouldReturnTrueForInitiatedAndInProgress() {
            assertThat(instanceIniciada().isAtivo()).isTrue();
            assertThat(instanceEmAndamento().isAtivo()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false para estados finais")
        void shouldReturnFalseForFinalStates() {
            WorkflowInstance completo = instanceEmAndamento();
            completo.completar();
            assertThat(completo.isAtivo()).isFalse();

            WorkflowInstance cancelado = instanceEmAndamento();
            cancelado.cancelar("x");
            assertThat(cancelado.isAtivo()).isFalse();

            WorkflowInstance falhado = instanceEmAndamento();
            falhado.falhar("x");
            assertThat(falhado.isAtivo()).isFalse();
        }
    }

    // =========================================================================
    // Histórico / contexto
    // =========================================================================

    @Nested
    @DisplayName("Histórico de etapas")
    class HistoricoEtapas {

        @Test
        @DisplayName("adicionarEtapaHistorico deve adicionar ao histórico")
        void adicionarEtapaHistoricoShouldAddToHistory() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.adicionarEtapaHistorico(etapaConcluida("Validacao"));
            assertThat(wi.getHistoricoEtapas()).hasSize(1);
        }

        @Test
        @DisplayName("ultimaEtapaExecutada deve retornar primeira do histórico")
        void ultimaEtapaExecutadaShouldReturnFirstOfHistory() {
            WorkflowInstance wi = instanceEmAndamento();
            EtapaExecucao e = etapaConcluida("Validacao");
            wi.adicionarEtapaHistorico(e);
            assertThat(wi.ultimaEtapaExecutada()).isEqualTo(e);
        }

        @Test
        @DisplayName("ultimaEtapaExecutada deve retornar null se histórico vazio")
        void ultimaEtapaExecutadaShouldReturnNullIfEmpty() {
            WorkflowInstance wi = instanceEmAndamento();
            assertThat(wi.ultimaEtapaExecutada()).isNull();
        }

        @Test
        @DisplayName("etapasConcluidas deve filtrar apenas concluídas")
        void etapasConcluidasShouldFilterOnlyConcluded() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.adicionarEtapaHistorico(etapaConcluida("E1"));
            wi.adicionarEtapaHistorico(etapaFalhada("E2"));
            List<EtapaExecucao> concluidas = wi.etapasConcluidas();
            assertThat(concluidas).hasSize(1);
        }

        @Test
        @DisplayName("etapasFalhadas deve filtrar apenas falhadas")
        void etapasFailhadasShouldFilterOnlyFailed() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.adicionarEtapaHistorico(etapaConcluida("E1"));
            wi.adicionarEtapaHistorico(etapaFalhada("E2"));
            assertThat(wi.etapasFalhadas()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("setContexto() / getContexto()")
    class Contexto {

        @Test
        @DisplayName("Deve armazenar e recuperar valores de contexto")
        void shouldStoreAndRetrieveContextValues() {
            WorkflowInstance wi = instanceIniciada();
            wi.setContexto("chave", "valor");
            assertThat(wi.getContexto("chave")).isEqualTo("valor");
        }

        @Test
        @DisplayName("getContexto deve retornar null para chave inexistente")
        void getContextoShouldReturnNullForMissingKey() {
            WorkflowInstance wi = instanceIniciada();
            assertThat(wi.getContexto("inexistente")).isNull();
        }
    }

    // =========================================================================
    // tempoExecucao
    // =========================================================================

    @Test
    @DisplayName("tempoExecucao deve retornar duração não negativa")
    void tempoExecucaoShouldReturnNonNegativeDuration() {
        WorkflowInstance wi = instanceIniciada();
        assertThat(wi.tempoExecucao().isNegative()).isFalse();
        assertThat(wi.tempoExecucaoMinutos()).isGreaterThanOrEqualTo(0);
        assertThat(wi.tempoExecucaoHoras()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("toString deve retornar representação não nula")
    void toStringShouldReturnNonNull() {
        assertThat(instanceIniciada().toString()).isNotNull();
    }
}
