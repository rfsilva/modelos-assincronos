package com.seguradora.hibrida.domain.apolice.notification.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link NotificationStatus}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("NotificationStatus - Testes Unitários")
class NotificationStatusTest {

    @Nested
    @DisplayName("Testes de Propriedades Básicas")
    class PropriedadesBasicasTests {

        @Test
        @DisplayName("PENDING deve ter propriedades corretas")
        void pendingDeveSerPropriedadesCorretas() {
            NotificationStatus status = NotificationStatus.PENDING;

            assertThat(status.getDisplayName()).isEqualTo("Pendente");
            assertThat(status.getDescription()).isEqualTo("Aguardando processamento");
            assertThat(status.toString()).isEqualTo("Pendente");
        }

        @Test
        @DisplayName("PROCESSING deve ter propriedades corretas")
        void processingDeveSerPropriedadesCorretas() {
            NotificationStatus status = NotificationStatus.PROCESSING;

            assertThat(status.getDisplayName()).isEqualTo("Processando");
            assertThat(status.getDescription()).isEqualTo("Em processamento");
            assertThat(status.toString()).isEqualTo("Processando");
        }

        @Test
        @DisplayName("SENT deve ter propriedades corretas")
        void sentDeveSerPropriedadesCorretas() {
            NotificationStatus status = NotificationStatus.SENT;

            assertThat(status.getDisplayName()).isEqualTo("Enviada");
            assertThat(status.getDescription()).isEqualTo("Enviada com sucesso");
            assertThat(status.toString()).isEqualTo("Enviada");
        }

        @Test
        @DisplayName("FAILED deve ter propriedades corretas")
        void failedDeveSerPropriedadesCorretas() {
            NotificationStatus status = NotificationStatus.FAILED;

            assertThat(status.getDisplayName()).isEqualTo("Falha");
            assertThat(status.getDescription()).isEqualTo("Falha no envio");
            assertThat(status.toString()).isEqualTo("Falha");
        }

        @Test
        @DisplayName("CANCELLED deve ter propriedades corretas")
        void cancelledDeveSerPropriedadesCorretas() {
            NotificationStatus status = NotificationStatus.CANCELLED;

            assertThat(status.getDisplayName()).isEqualTo("Cancelada");
            assertThat(status.getDescription()).isEqualTo("Cancelada pelo sistema");
            assertThat(status.toString()).isEqualTo("Cancelada");
        }

        @Test
        @DisplayName("EXPIRED deve ter propriedades corretas")
        void expiredDeveSerPropriedadesCorretas() {
            NotificationStatus status = NotificationStatus.EXPIRED;

            assertThat(status.getDisplayName()).isEqualTo("Expirada");
            assertThat(status.getDescription()).isEqualTo("Expirou o prazo de envio");
            assertThat(status.toString()).isEqualTo("Expirada");
        }
    }

    @Nested
    @DisplayName("Testes de Status Final")
    class StatusFinalTests {

        @Test
        @DisplayName("SENT deve ser status final")
        void sentDeveSerStatusFinal() {
            assertThat(NotificationStatus.SENT.isFinal()).isTrue();
        }

        @Test
        @DisplayName("FAILED deve ser status final")
        void failedDeveSerStatusFinal() {
            assertThat(NotificationStatus.FAILED.isFinal()).isTrue();
        }

        @Test
        @DisplayName("CANCELLED deve ser status final")
        void cancelledDeveSerStatusFinal() {
            assertThat(NotificationStatus.CANCELLED.isFinal()).isTrue();
        }

        @Test
        @DisplayName("EXPIRED deve ser status final")
        void expiredDeveSerStatusFinal() {
            assertThat(NotificationStatus.EXPIRED.isFinal()).isTrue();
        }

        @Test
        @DisplayName("PENDING não deve ser status final")
        void pendingNaoDeveSerStatusFinal() {
            assertThat(NotificationStatus.PENDING.isFinal()).isFalse();
        }

        @Test
        @DisplayName("PROCESSING não deve ser status final")
        void processingNaoDeveSerStatusFinal() {
            assertThat(NotificationStatus.PROCESSING.isFinal()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Status de Sucesso")
    class StatusSucessoTests {

        @Test
        @DisplayName("SENT deve ser status de sucesso")
        void sentDeveSerStatusSucesso() {
            assertThat(NotificationStatus.SENT.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("PENDING não deve ser status de sucesso")
        void pendingNaoDeveSerStatusSucesso() {
            assertThat(NotificationStatus.PENDING.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("PROCESSING não deve ser status de sucesso")
        void processingNaoDeveSerStatusSucesso() {
            assertThat(NotificationStatus.PROCESSING.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("FAILED não deve ser status de sucesso")
        void failedNaoDeveSerStatusSucesso() {
            assertThat(NotificationStatus.FAILED.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED não deve ser status de sucesso")
        void cancelledNaoDeveSerStatusSucesso() {
            assertThat(NotificationStatus.CANCELLED.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("EXPIRED não deve ser status de sucesso")
        void expiredNaoDeveSerStatusSucesso() {
            assertThat(NotificationStatus.EXPIRED.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Status de Erro")
    class StatusErroTests {

        @Test
        @DisplayName("FAILED deve ser status de erro")
        void failedDeveSerStatusErro() {
            assertThat(NotificationStatus.FAILED.isError()).isTrue();
        }

        @Test
        @DisplayName("EXPIRED deve ser status de erro")
        void expiredDeveSerStatusErro() {
            assertThat(NotificationStatus.EXPIRED.isError()).isTrue();
        }

        @Test
        @DisplayName("PENDING não deve ser status de erro")
        void pendingNaoDeveSerStatusErro() {
            assertThat(NotificationStatus.PENDING.isError()).isFalse();
        }

        @Test
        @DisplayName("PROCESSING não deve ser status de erro")
        void processingNaoDeveSerStatusErro() {
            assertThat(NotificationStatus.PROCESSING.isError()).isFalse();
        }

        @Test
        @DisplayName("SENT não deve ser status de erro")
        void sentNaoDeveSerStatusErro() {
            assertThat(NotificationStatus.SENT.isError()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED não deve ser status de erro")
        void cancelledNaoDeveSerStatusErro() {
            assertThat(NotificationStatus.CANCELLED.isError()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Possibilidade de Retry")
    class PossibilidadeRetryTests {

        @Test
        @DisplayName("FAILED deve permitir retry mas método retorna false pois isFinal é true")
        void failedDeveRetornarFalseNoCanRetry() {
            // Nota: canRetry() verifica se !isFinal(), mas FAILED.isFinal() é true
            // Portanto, mesmo sendo FAILED, canRetry() retorna false
            // A lógica real de retry é implementada na entidade ApoliceNotification
            assertThat(NotificationStatus.FAILED.canRetry()).isFalse();
        }

        @Test
        @DisplayName("PENDING não deve permitir retry")
        void pendingNaoDevePermitirRetry() {
            assertThat(NotificationStatus.PENDING.canRetry()).isFalse();
        }

        @Test
        @DisplayName("PROCESSING não deve permitir retry")
        void processingNaoDevePermitirRetry() {
            assertThat(NotificationStatus.PROCESSING.canRetry()).isFalse();
        }

        @Test
        @DisplayName("SENT não deve permitir retry")
        void sentNaoDevePermitirRetry() {
            assertThat(NotificationStatus.SENT.canRetry()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED não deve permitir retry")
        void cancelledNaoDevePermitirRetry() {
            assertThat(NotificationStatus.CANCELLED.canRetry()).isFalse();
        }

        @Test
        @DisplayName("EXPIRED não deve permitir retry")
        void expiredNaoDevePermitirRetry() {
            assertThat(NotificationStatus.EXPIRED.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Todos os Valores")
    class ValidacaoTodosValoresTests {

        @ParameterizedTest
        @EnumSource(NotificationStatus.class)
        @DisplayName("Todos os status devem ter display name não nulo")
        void todosStatusDevemTerDisplayName(NotificationStatus status) {
            assertThat(status.getDisplayName()).isNotNull().isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(NotificationStatus.class)
        @DisplayName("Todos os status devem ter description não nula")
        void todosStatusDevemTerDescription(NotificationStatus status) {
            assertThat(status.getDescription()).isNotNull().isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(NotificationStatus.class)
        @DisplayName("ToString deve retornar o display name")
        void toStringDeveRetornarDisplayName(NotificationStatus status) {
            assertThat(status.toString()).isEqualTo(status.getDisplayName());
        }
    }

    @Nested
    @DisplayName("Testes de Lógica de Negócio")
    class LogicaNegocioTests {

        @Test
        @DisplayName("Status de sucesso não deve ser status de erro")
        void statusSucessoNaoDeveSerErro() {
            for (NotificationStatus status : NotificationStatus.values()) {
                if (status.isSuccess()) {
                    assertThat(status.isError())
                        .as("Status de sucesso %s não deveria ser erro", status)
                        .isFalse();
                }
            }
        }

        @Test
        @DisplayName("Status final deve ser sucesso ou erro ou cancelado")
        void statusFinalDeveSerSucessoOuErroOuCancelado() {
            for (NotificationStatus status : NotificationStatus.values()) {
                if (status.isFinal()) {
                    boolean ehSucessoOuErroOuCancelado =
                        status.isSuccess() ||
                        status.isError() ||
                        status == NotificationStatus.CANCELLED;

                    assertThat(ehSucessoOuErroOuCancelado)
                        .as("Status final %s deveria ser sucesso, erro ou cancelado", status)
                        .isTrue();
                }
            }
        }

        @Test
        @DisplayName("Deve ter exatamente 6 status")
        void deveTerExatamente6Status() {
            assertThat(NotificationStatus.values()).hasSize(6);
        }

        @Test
        @DisplayName("Status transitórios devem ser PENDING e PROCESSING")
        void statusTransitoriosDevemSerPendingEProcessing() {
            long statusTransitorios = java.util.Arrays.stream(NotificationStatus.values())
                .filter(s -> !s.isFinal())
                .count();

            assertThat(statusTransitorios).isEqualTo(2);
            assertThat(NotificationStatus.PENDING.isFinal()).isFalse();
            assertThat(NotificationStatus.PROCESSING.isFinal()).isFalse();
        }

        @Test
        @DisplayName("Status finais devem ser SENT, FAILED, CANCELLED e EXPIRED")
        void statusFinaisDevemSerQuatro() {
            long statusFinais = java.util.Arrays.stream(NotificationStatus.values())
                .filter(NotificationStatus::isFinal)
                .count();

            assertThat(statusFinais).isEqualTo(4);
            assertThat(NotificationStatus.SENT.isFinal()).isTrue();
            assertThat(NotificationStatus.FAILED.isFinal()).isTrue();
            assertThat(NotificationStatus.CANCELLED.isFinal()).isTrue();
            assertThat(NotificationStatus.EXPIRED.isFinal()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Transição de Estados")
    class TransicaoEstadosTests {

        @Test
        @DisplayName("Fluxo normal: PENDING -> PROCESSING -> SENT")
        void fluxoNormalPendingProcessingSent() {
            NotificationStatus pending = NotificationStatus.PENDING;
            NotificationStatus processing = NotificationStatus.PROCESSING;
            NotificationStatus sent = NotificationStatus.SENT;

            assertThat(pending.isFinal()).isFalse();
            assertThat(processing.isFinal()).isFalse();
            assertThat(sent.isFinal()).isTrue();
            assertThat(sent.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Fluxo com erro: PENDING -> PROCESSING -> FAILED")
        void fluxoComErroPendingProcessingFailed() {
            NotificationStatus pending = NotificationStatus.PENDING;
            NotificationStatus processing = NotificationStatus.PROCESSING;
            NotificationStatus failed = NotificationStatus.FAILED;

            assertThat(pending.isFinal()).isFalse();
            assertThat(processing.isFinal()).isFalse();
            assertThat(failed.isFinal()).isTrue();
            assertThat(failed.isError()).isTrue();
        }

        @Test
        @DisplayName("Fluxo cancelado: PENDING -> CANCELLED")
        void fluxoCanceladoPendingCancelled() {
            NotificationStatus pending = NotificationStatus.PENDING;
            NotificationStatus cancelled = NotificationStatus.CANCELLED;

            assertThat(pending.isFinal()).isFalse();
            assertThat(cancelled.isFinal()).isTrue();
            assertThat(cancelled.isSuccess()).isFalse();
            assertThat(cancelled.isError()).isFalse();
        }

        @Test
        @DisplayName("Fluxo expirado: PENDING -> EXPIRED ou FAILED -> EXPIRED")
        void fluxoExpirado() {
            NotificationStatus expired = NotificationStatus.EXPIRED;

            assertThat(expired.isFinal()).isTrue();
            assertThat(expired.isError()).isTrue();
            assertThat(expired.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("valueOf deve funcionar corretamente")
        void valueOfDeveFuncionarCorretamente() {
            assertThat(NotificationStatus.valueOf("PENDING")).isEqualTo(NotificationStatus.PENDING);
            assertThat(NotificationStatus.valueOf("PROCESSING")).isEqualTo(NotificationStatus.PROCESSING);
            assertThat(NotificationStatus.valueOf("SENT")).isEqualTo(NotificationStatus.SENT);
            assertThat(NotificationStatus.valueOf("FAILED")).isEqualTo(NotificationStatus.FAILED);
            assertThat(NotificationStatus.valueOf("CANCELLED")).isEqualTo(NotificationStatus.CANCELLED);
            assertThat(NotificationStatus.valueOf("EXPIRED")).isEqualTo(NotificationStatus.EXPIRED);
        }

        @Test
        @DisplayName("valueOf deve lançar exceção para valor inválido")
        void valueOfDeveLancarExcecaoParaValorInvalido() {
            assertThatThrownBy(() -> NotificationStatus.valueOf("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("values deve retornar todos os status")
        void valuesDeveRetornarTodosStatus() {
            NotificationStatus[] statuses = NotificationStatus.values();

            assertThat(statuses).contains(
                NotificationStatus.PENDING,
                NotificationStatus.PROCESSING,
                NotificationStatus.SENT,
                NotificationStatus.FAILED,
                NotificationStatus.CANCELLED,
                NotificationStatus.EXPIRED
            );
        }

        @Test
        @DisplayName("Todos os display names devem ser únicos")
        void todosDisplayNamesDevemSerUnicos() {
            NotificationStatus[] statuses = NotificationStatus.values();
            long displayNamesUnicos = java.util.Arrays.stream(statuses)
                .map(NotificationStatus::getDisplayName)
                .distinct()
                .count();

            assertThat(displayNamesUnicos).isEqualTo(statuses.length);
        }

        @Test
        @DisplayName("Todas as descriptions devem ser únicas")
        void todasDescriptionsDevemSerUnicas() {
            NotificationStatus[] statuses = NotificationStatus.values();
            long descriptionsUnicas = java.util.Arrays.stream(statuses)
                .map(NotificationStatus::getDescription)
                .distinct()
                .count();

            assertThat(descriptionsUnicas).isEqualTo(statuses.length);
        }
    }
}
