package com.seguradora.hibrida.command.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link CommandException}.
 *
 * <p>Usa uma subclasse concreta anônima para testar a classe abstrata base.
 */
@DisplayName("CommandException Tests")
class CommandExceptionTest {

    // Subclasse concreta para testes
    private static CommandException concrete(String message) {
        return new CommandException(message) {};
    }

    private static CommandException concrete(String message, Throwable cause) {
        return new CommandException(message, cause) {};
    }

    private static CommandException concrete(Throwable cause) {
        return new CommandException(cause) {};
    }

    // =========================================================================
    // Herança e tipo
    // =========================================================================

    @Nested
    @DisplayName("Hierarquia de tipos")
    class HierarquiaDeTipos {

        @Test
        @DisplayName("Deve estender RuntimeException")
        void shouldExtendRuntimeException() {
            assertThat(CommandException.class.getSuperclass()).isEqualTo(RuntimeException.class);
        }

        @Test
        @DisplayName("Deve ser abstrata")
        void shouldBeAbstract() {
            assertThat(java.lang.reflect.Modifier.isAbstract(
                    CommandException.class.getModifiers())).isTrue();
        }
    }

    // =========================================================================
    // Construtores
    // =========================================================================

    @Nested
    @DisplayName("Construtores")
    class Construtores {

        @Test
        @DisplayName("Construtor(mensagem) deve preservar a mensagem")
        void messageConstructorShouldPreserveMessage() {
            CommandException ex = concrete("Erro de comando");
            assertThat(ex.getMessage()).isEqualTo("Erro de comando");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("Construtor(mensagem, causa) deve preservar ambos")
        void messageCauseConstructorShouldPreserveBoth() {
            RuntimeException cause = new RuntimeException("causa original");
            CommandException ex = concrete("Erro de comando", cause);

            assertThat(ex.getMessage()).isEqualTo("Erro de comando");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("Construtor(causa) deve preservar a causa")
        void causeConstructorShouldPreserveCause() {
            RuntimeException cause = new RuntimeException("causa original");
            CommandException ex = concrete(cause);

            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("Deve ser capturável como RuntimeException")
        void shouldBeCatchableAsRuntimeException() {
            // Then
            assertThat(concrete("erro")).isInstanceOf(RuntimeException.class);
        }
    }

    // =========================================================================
    // Subclasses concretas são CommandException
    // =========================================================================

    @Nested
    @DisplayName("Subclasses concretas herdam CommandException")
    class SubclassesConcretas {

        @Test
        @DisplayName("CommandTimeoutException deve ser CommandException")
        void commandTimeoutExceptionShouldBeCommandException() {
            assertThat(CommandTimeoutException.class.getSuperclass())
                    .isEqualTo(CommandException.class);
        }

        @Test
        @DisplayName("CommandExecutionException deve ser CommandException")
        void commandExecutionExceptionShouldBeCommandException() {
            assertThat(CommandExecutionException.class.getSuperclass())
                    .isEqualTo(CommandException.class);
        }

        @Test
        @DisplayName("CommandHandlerNotFoundException deve ser CommandException")
        void commandHandlerNotFoundExceptionShouldBeCommandException() {
            assertThat(CommandHandlerNotFoundException.class.getSuperclass())
                    .isEqualTo(CommandException.class);
        }

        @Test
        @DisplayName("CommandValidationException deve ser CommandException")
        void commandValidationExceptionShouldBeCommandException() {
            assertThat(CommandValidationException.class.getSuperclass())
                    .isEqualTo(CommandException.class);
        }
    }
}
