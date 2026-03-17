package com.seguradora.hibrida.command.exception;

import com.seguradora.hibrida.command.example.TestCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link CommandTimeoutException}.
 */
@DisplayName("CommandTimeoutException Tests")
class CommandTimeoutExceptionTest {

    // =========================================================================
    // Construtor (commandType, timeoutSeconds)
    // =========================================================================

    @Nested
    @DisplayName("Construtor(commandType, timeoutSeconds)")
    class ConstrutorComTipoETimeout {

        @Test
        @DisplayName("Deve preservar commandType e timeoutSeconds")
        void shouldPreserveCommandTypeAndTimeoutSeconds() {
            CommandTimeoutException ex =
                    new CommandTimeoutException(TestCommand.class, 30);

            assertThat(ex.getCommandType()).isEqualTo(TestCommand.class);
            assertThat(ex.getTimeoutSeconds()).isEqualTo(30);
            assertThat(ex.getCommandId()).isNull();
            assertThat(ex.getActualExecutionTimeMs()).isZero();
        }

        @Test
        @DisplayName("Mensagem deve conter nome do tipo e timeout")
        void messageShouldContainTypeNameAndTimeout() {
            CommandTimeoutException ex =
                    new CommandTimeoutException(TestCommand.class, 10);

            assertThat(ex.getMessage())
                    .contains("TestCommand")
                    .contains("10");
        }
    }

    // =========================================================================
    // Construtor (commandType, commandId, timeoutSeconds)
    // =========================================================================

    @Nested
    @DisplayName("Construtor(commandType, commandId, timeoutSeconds)")
    class ConstrutorComId {

        @Test
        @DisplayName("Deve preservar commandType, commandId e timeoutSeconds")
        void shouldPreserveAllFields() {
            CommandTimeoutException ex =
                    new CommandTimeoutException(TestCommand.class, "cmd-001", 60);

            assertThat(ex.getCommandType()).isEqualTo(TestCommand.class);
            assertThat(ex.getCommandId()).isEqualTo("cmd-001");
            assertThat(ex.getTimeoutSeconds()).isEqualTo(60);
            assertThat(ex.getActualExecutionTimeMs()).isZero();
        }

        @Test
        @DisplayName("Mensagem deve conter commandId")
        void messageShouldContainCommandId() {
            CommandTimeoutException ex =
                    new CommandTimeoutException(TestCommand.class, "cmd-xyz", 30);

            assertThat(ex.getMessage()).contains("cmd-xyz");
        }
    }

    // =========================================================================
    // Construtor completo
    // =========================================================================

    @Nested
    @DisplayName("Construtor completo (commandType, commandId, timeoutSeconds, actualExecutionTimeMs)")
    class ConstrutorCompleto {

        @Test
        @DisplayName("Deve preservar todos os campos incluindo actualExecutionTimeMs")
        void shouldPreserveAllFieldsIncludingActualExecutionTime() {
            CommandTimeoutException ex =
                    new CommandTimeoutException(TestCommand.class, "cmd-full", 15, 18500L);

            assertThat(ex.getCommandType()).isEqualTo(TestCommand.class);
            assertThat(ex.getCommandId()).isEqualTo("cmd-full");
            assertThat(ex.getTimeoutSeconds()).isEqualTo(15);
            assertThat(ex.getActualExecutionTimeMs()).isEqualTo(18500L);
        }

        @Test
        @DisplayName("Mensagem deve mencionar tempo real de execução")
        void messageShouldMentionActualExecutionTime() {
            CommandTimeoutException ex =
                    new CommandTimeoutException(TestCommand.class, "id", 10, 12000L);

            assertThat(ex.getMessage()).contains("12000");
        }
    }

    // =========================================================================
    // Construtor com causa
    // =========================================================================

    @Nested
    @DisplayName("Construtor(commandType, message, cause)")
    class ConstrutorComCausa {

        @Test
        @DisplayName("Deve preservar commandType e causa")
        void shouldPreserveCommandTypeAndCause() {
            RuntimeException cause = new RuntimeException("timeout de rede");
            CommandTimeoutException ex =
                    new CommandTimeoutException(TestCommand.class, "Timeout customizado", cause);

            assertThat(ex.getCommandType()).isEqualTo(TestCommand.class);
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getMessage()).isEqualTo("Timeout customizado");
            assertThat(ex.getTimeoutSeconds()).isZero();
            assertThat(ex.getActualExecutionTimeMs()).isZero();
        }
    }

    // =========================================================================
    // Herança
    // =========================================================================

    @Test
    @DisplayName("Deve ser instância de CommandException e RuntimeException")
    void shouldBeInstanceOfCommandExceptionAndRuntimeException() {
        CommandTimeoutException ex = new CommandTimeoutException(TestCommand.class, 5);

        assertThat(ex).isInstanceOf(CommandException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
