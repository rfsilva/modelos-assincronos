package com.seguradora.hibrida.domain.workflow.execution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowResult Tests")
class WorkflowResultTest {

    // =========================================================================
    // Factories de sucesso
    // =========================================================================

    @Nested
    @DisplayName("success()")
    class Success {

        @Test
        @DisplayName("success(msg) deve criar resultado de sucesso")
        void successWithMessageShouldCreateSuccessResult() {
            WorkflowResult result = WorkflowResult.success("OK");
            assertThat(result.isSucesso()).isTrue();
            assertThat(result.isFalha()).isFalse();
            assertThat(result.getMensagem()).isEqualTo("OK");
        }

        @Test
        @DisplayName("success(msg, dados) deve criar resultado com dados")
        void successWithDataShouldCreateResultWithData() {
            Map<String, Object> dados = Map.of("chave", "valor");
            WorkflowResult result = WorkflowResult.success("OK", dados);
            assertThat(result.isSucesso()).isTrue();
            assertThat(result.getDado("chave")).isEqualTo("valor");
        }

        @Test
        @DisplayName("success(msg, null) deve criar resultado sem dados")
        void successWithNullDataShouldCreateResultWithEmptyData() {
            WorkflowResult result = WorkflowResult.success("OK", null);
            assertThat(result.isSucesso()).isTrue();
        }

        @Test
        @DisplayName("successComProximaEtapa deve definir etapaProximaId")
        void successComProximaEtapaShouldSetNextStage() {
            WorkflowResult result = WorkflowResult.successComProximaEtapa("OK", "etapa-02");
            assertThat(result.isSucesso()).isTrue();
            assertThat(result.hasProximaEtapa()).isTrue();
            assertThat(result.getEtapaProximaId()).isEqualTo("etapa-02");
        }
    }

    // =========================================================================
    // Factories de falha
    // =========================================================================

    @Nested
    @DisplayName("failure()")
    class Failure {

        @Test
        @DisplayName("failure(msg) deve criar resultado de falha com retry")
        void failureShouldCreateFailureWithRetry() {
            WorkflowResult result = WorkflowResult.failure("Erro");
            assertThat(result.isSucesso()).isFalse();
            assertThat(result.isFalha()).isTrue();
            assertThat(result.isPermiteRetry()).isTrue();
            assertThat(result.getMensagem()).isEqualTo("Erro");
        }

        @Test
        @DisplayName("failure(msg, codigoErro) deve criar resultado com código de erro")
        void failureWithCodeShouldSetErrorCode() {
            WorkflowResult result = WorkflowResult.failure("Erro", 500);
            assertThat(result.isFalha()).isTrue();
            assertThat(result.isPermiteRetry()).isTrue();
            assertThat(result.getCodigoErro()).isEqualTo(500);
        }

        @Test
        @DisplayName("failureSemRetry deve criar resultado sem retry")
        void failureSemRetryShouldCreateFailureWithoutRetry() {
            WorkflowResult result = WorkflowResult.failureSemRetry("Erro fatal");
            assertThat(result.isFalha()).isTrue();
            assertThat(result.isPermiteRetry()).isFalse();
        }
    }

    // =========================================================================
    // Factories de timeout
    // =========================================================================

    @Nested
    @DisplayName("timeout()")
    class Timeout {

        @Test
        @DisplayName("timeout(msg) deve criar resultado de timeout")
        void timeoutWithMessageShouldCreateTimeoutResult() {
            WorkflowResult result = WorkflowResult.timeout("Tempo esgotado");
            assertThat(result.isFalha()).isTrue();
            assertThat(result.isTimeout()).isTrue();
            assertThat(result.isPermiteRetry()).isTrue();
            assertThat(result.getMensagem()).isEqualTo("Tempo esgotado");
        }

        @Test
        @DisplayName("timeout() sem args deve criar resultado com mensagem padrão")
        void timeoutWithoutArgsShouldCreateDefaultTimeoutResult() {
            WorkflowResult result = WorkflowResult.timeout();
            assertThat(result.isTimeout()).isTrue();
            assertThat(result.getMensagem()).isNotBlank();
        }
    }

    // =========================================================================
    // Métodos fluentes
    // =========================================================================

    @Nested
    @DisplayName("Métodos fluentes")
    class FluentMethods {

        @Test
        @DisplayName("comDado deve adicionar dado ao resultado")
        void comDadoShouldAddData() {
            WorkflowResult result = WorkflowResult.success("OK").comDado("key", "value");
            assertThat(result.getDado("key")).isEqualTo("value");
            assertThat(result.hasDado("key")).isTrue();
        }

        @Test
        @DisplayName("comDados deve adicionar múltiplos dados")
        void comDadosShouldAddMultipleData() {
            WorkflowResult result = WorkflowResult.success("OK").comDados(Map.of("a", 1, "b", 2));
            assertThat(result.getDado("a")).isEqualTo(1);
            assertThat(result.getDado("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("comProximaEtapa deve definir etapa")
        void comProximaEtapaShouldSetEtapa() {
            WorkflowResult result = WorkflowResult.success("OK").comProximaEtapa("etapa-03");
            assertThat(result.hasProximaEtapa()).isTrue();
            assertThat(result.getEtapaProximaId()).isEqualTo("etapa-03");
        }

        @Test
        @DisplayName("comProximoStatus deve definir status")
        void comProximoStatusShouldSetStatus() {
            WorkflowResult result = WorkflowResult.success("OK").comProximoStatus("AGUARDANDO");
            assertThat(result.getProximoStatus()).isEqualTo("AGUARDANDO");
        }
    }

    // =========================================================================
    // getDado com tipo
    // =========================================================================

    @Nested
    @DisplayName("getDado(chave, Class)")
    class GetDadoComTipo {

        @Test
        @DisplayName("Deve retornar dado com tipo correto")
        void shouldReturnTypedData() {
            WorkflowResult result = WorkflowResult.success("OK").comDado("numero", 42);
            assertThat(result.getDado("numero", Integer.class)).isEqualTo(42);
        }

        @Test
        @DisplayName("Deve retornar null quando tipo incompatível")
        void shouldReturnNullForWrongType() {
            WorkflowResult result = WorkflowResult.success("OK").comDado("texto", "abc");
            assertThat(result.getDado("texto", Integer.class)).isNull();
        }

        @Test
        @DisplayName("Deve retornar null para chave inexistente")
        void shouldReturnNullForMissingKey() {
            WorkflowResult result = WorkflowResult.success("OK");
            assertThat(result.getDado("inexistente")).isNull();
            assertThat(result.hasDado("inexistente")).isFalse();
        }
    }

    // =========================================================================
    // merge
    // =========================================================================

    @Nested
    @DisplayName("merge()")
    class Merge {

        @Test
        @DisplayName("Deve mesclar dados de outro resultado")
        void shouldMergeData() {
            WorkflowResult r1 = WorkflowResult.success("R1").comDado("a", 1);
            WorkflowResult r2 = WorkflowResult.success("R2").comDado("b", 2);
            r1.merge(r2);
            assertThat(r1.getDado("a")).isEqualTo(1);
            assertThat(r1.getDado("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("Merge com null não deve lançar exceção")
        void mergeWithNullShouldNotThrow() {
            WorkflowResult result = WorkflowResult.success("OK");
            result.merge(null);
        }
    }

    // =========================================================================
    // hasProximaEtapa
    // =========================================================================

    @Test
    @DisplayName("hasProximaEtapa deve retornar false quando etapaProximaId é null ou vazio")
    void hasProximaEtapaShouldReturnFalseWhenNullOrEmpty() {
        WorkflowResult r1 = WorkflowResult.success("OK");
        assertThat(r1.hasProximaEtapa()).isFalse();

        WorkflowResult r2 = WorkflowResult.success("OK").comProximaEtapa("");
        assertThat(r2.hasProximaEtapa()).isFalse();
    }

    @Test
    @DisplayName("toString deve retornar representação não nula")
    void toStringShouldReturnNonNull() {
        assertThat(WorkflowResult.success("OK").toString()).isNotNull();
    }
}
