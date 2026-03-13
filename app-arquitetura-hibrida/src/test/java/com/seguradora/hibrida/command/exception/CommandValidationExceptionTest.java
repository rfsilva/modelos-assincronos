package com.seguradora.hibrida.command.exception;

import com.seguradora.hibrida.command.Command;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommandValidationException Tests")
class CommandValidationExceptionTest {

    // Test Command
    private static class TestCommand implements Command {
        @Override
        public java.util.UUID getCommandId() {
            return java.util.UUID.randomUUID();
        }

        @Override
        public java.time.Instant getTimestamp() {
            return java.time.Instant.now();
        }

        @Override
        public java.util.UUID getCorrelationId() {
            return null;
        }

        @Override
        public String getUserId() {
            return "test-user";
        }
    }

    @Test
    @DisplayName("Deve criar com mensagem simples")
    void shouldCreateWithMessage() {
        CommandValidationException exception = new CommandValidationException("Validation failed");
        assertThat(exception.getMessage()).isEqualTo("Validation failed");
    }

    @Test
    @DisplayName("Deve criar com tipo de comando e mensagem")
    void shouldCreateWithCommandTypeAndMessage() {
        String message = "Campo obrigatório ausente";
        CommandValidationException exception = new CommandValidationException(TestCommand.class, message);

        assertThat(exception.getMessage()).contains("TestCommand");
        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getCommandType()).isEqualTo(TestCommand.class);
    }

    @Test
    @DisplayName("Deve criar com violações")
    void shouldCreateWithViolations() {
        Set<String> violations = Set.of("CPF inválido", "E-mail obrigatório");
        CommandValidationException exception = new CommandValidationException(TestCommand.class, violations);

        assertThat(exception.getMessage()).contains("TestCommand");
        assertThat(exception.hasViolations()).isTrue();
        assertThat(exception.getViolations()).containsExactlyInAnyOrder("CPF inválido", "E-mail obrigatório");
    }

    @Test
    @DisplayName("Deve criar com tipo de comando, mensagem e causa")
    void shouldCreateWithCommandTypeMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        CommandValidationException exception = new CommandValidationException(TestCommand.class, "Validation failed", cause);

        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCommandType()).isEqualTo(TestCommand.class);
    }

    @Test
    @DisplayName("Deve ser uma CommandException")
    void shouldBeACommandException() {
        CommandValidationException exception = new CommandValidationException("Test");
        assertThat(exception).isInstanceOf(CommandException.class);
    }
}
