package com.seguradora.hibrida.command.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommandExecutionException Tests")
class CommandExecutionExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem")
    void shouldCreateWithMessage() {
        CommandExecutionException exception = new CommandExecutionException("Execution failed");
        assertThat(exception.getMessage()).isEqualTo("Execution failed");
    }

    @Test
    @DisplayName("Deve criar com mensagem e causa")
    void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        CommandExecutionException exception = new CommandExecutionException("Execution failed", cause);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Deve ser uma CommandException")
    void shouldBeACommandException() {
        CommandExecutionException exception = new CommandExecutionException("Test");
        assertThat(exception).isInstanceOf(CommandException.class);
    }
}
