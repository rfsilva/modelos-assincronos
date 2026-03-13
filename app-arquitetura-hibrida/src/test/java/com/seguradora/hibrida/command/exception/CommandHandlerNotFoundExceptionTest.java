package com.seguradora.hibrida.command.exception;

import com.seguradora.hibrida.command.Command;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommandHandlerNotFoundException Tests")
class CommandHandlerNotFoundExceptionTest {

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
    @DisplayName("Deve criar com tipo de comando")
    void shouldCreateWithCommandType() {
        CommandHandlerNotFoundException exception = new CommandHandlerNotFoundException(TestCommand.class);
        assertThat(exception.getMessage()).contains("TestCommand");
        assertThat(exception.getCommandType()).isEqualTo(TestCommand.class);
    }

    @Test
    @DisplayName("Deve criar com tipo de comando e mensagem")
    void shouldCreateWithCommandTypeAndMessage() {
        String message = "Handler not found for command";
        CommandHandlerNotFoundException exception = new CommandHandlerNotFoundException(TestCommand.class, message);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCommandType()).isEqualTo(TestCommand.class);
    }

    @Test
    @DisplayName("Deve criar com tipo de comando, mensagem e causa")
    void shouldCreateWithCommandTypeMessageAndCause() {
        String message = "Handler not found";
        Throwable cause = new RuntimeException("Cause");
        CommandHandlerNotFoundException exception = new CommandHandlerNotFoundException(TestCommand.class, message, cause);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCommandType()).isEqualTo(TestCommand.class);
    }

    @Test
    @DisplayName("Deve ser uma CommandException")
    void shouldBeACommandException() {
        CommandHandlerNotFoundException exception = new CommandHandlerNotFoundException(TestCommand.class);
        assertThat(exception).isInstanceOf(CommandException.class);
    }
}
