package com.seguradora.hibrida.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para a interface {@link CommandBus}.
 *
 * <p>Valida o contrato da interface usando mock/stub inline.
 */
@DisplayName("CommandBus Tests")
class CommandBusTest {

    // =========================================================================
    // Contrato da interface
    // =========================================================================

    @Nested
    @DisplayName("Contrato da interface")
    class ContratoInterface {

        @Test
        @DisplayName("Interface deve declarar send(Command)")
        void shouldDeclareSendMethod() throws NoSuchMethodException {
            assertThat(CommandBus.class.getMethod("send", Command.class)).isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar sendAsync(Command)")
        void shouldDeclareSendAsyncMethod() throws NoSuchMethodException {
            assertThat(CommandBus.class.getMethod("sendAsync", Command.class)).isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar registerHandler(CommandHandler)")
        void shouldDeclareRegisterHandlerMethod() throws NoSuchMethodException {
            assertThat(CommandBus.class.getMethod("registerHandler", CommandHandler.class))
                    .isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar unregisterHandler(Class)")
        void shouldDeclareUnregisterHandlerMethod() throws NoSuchMethodException {
            assertThat(CommandBus.class.getMethod("unregisterHandler", Class.class))
                    .isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar hasHandler(Class)")
        void shouldDeclareHasHandlerMethod() throws NoSuchMethodException {
            assertThat(CommandBus.class.getMethod("hasHandler", Class.class)).isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar getStatistics()")
        void shouldDeclareGetStatisticsMethod() throws NoSuchMethodException {
            assertThat(CommandBus.class.getMethod("getStatistics")).isNotNull();
        }

        @Test
        @DisplayName("sendAsync deve retornar CompletableFuture<CommandResult>")
        void sendAsyncShouldReturnCompletableFuture() throws NoSuchMethodException {
            var returnType = CommandBus.class.getMethod("sendAsync", Command.class).getReturnType();
            assertThat(returnType).isEqualTo(CompletableFuture.class);
        }
    }

    // =========================================================================
    // Implementação stub para teste de contrato
    // =========================================================================

    @Nested
    @DisplayName("Comportamento esperado de implementações")
    class ComportamentoEsperado {

        @Test
        @DisplayName("send deve retornar CommandResult não nulo")
        void sendShouldReturnNonNullCommandResult() {
            // Given
            CommandBus bus = buildStubBus();
            Command cmd = buildStubCommand();

            // When
            CommandResult result = bus.send(cmd);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("hasHandler deve retornar false para tipo sem handler")
        void hasHandlerShouldReturnFalseForUnregisteredType() {
            // Given
            CommandBus bus = buildStubBus();

            // Then
            assertThat(bus.hasHandler(Command.class)).isFalse();
        }

        @Test
        @DisplayName("registerHandler deve tornar hasHandler true")
        void registerHandlerShouldMakeHasHandlerTrue() {
            // Given
            com.seguradora.hibrida.command.CommandHandlerRegistry registry =
                    new com.seguradora.hibrida.command.CommandHandlerRegistry();
            CommandBus bus = new com.seguradora.hibrida.command.impl.SimpleCommandBus(registry);

            CommandHandler<com.seguradora.hibrida.command.example.TestCommand> handler =
                    new com.seguradora.hibrida.command.example.TestCommandHandler();

            // When
            bus.registerHandler(handler);

            // Then
            assertThat(bus.hasHandler(com.seguradora.hibrida.command.example.TestCommand.class))
                    .isTrue();
        }

        @Test
        @DisplayName("unregisterHandler deve tornar hasHandler false")
        void unregisterHandlerShouldMakeHasHandlerFalse() {
            // Given
            com.seguradora.hibrida.command.CommandHandlerRegistry registry =
                    new com.seguradora.hibrida.command.CommandHandlerRegistry();
            CommandBus bus = new com.seguradora.hibrida.command.impl.SimpleCommandBus(registry);
            CommandHandler<com.seguradora.hibrida.command.example.TestCommand> handler =
                    new com.seguradora.hibrida.command.example.TestCommandHandler();
            bus.registerHandler(handler);

            // When
            bus.unregisterHandler(com.seguradora.hibrida.command.example.TestCommand.class);

            // Then
            assertThat(bus.hasHandler(com.seguradora.hibrida.command.example.TestCommand.class))
                    .isFalse();
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CommandBus buildStubBus() {
        com.seguradora.hibrida.command.CommandHandlerRegistry registry =
                new com.seguradora.hibrida.command.CommandHandlerRegistry();
        return new com.seguradora.hibrida.command.impl.SimpleCommandBus(registry);
    }

    private Command buildStubCommand() {
        return com.seguradora.hibrida.command.example.TestCommand.builder()
                .data("payload")
                .build();
    }
}
