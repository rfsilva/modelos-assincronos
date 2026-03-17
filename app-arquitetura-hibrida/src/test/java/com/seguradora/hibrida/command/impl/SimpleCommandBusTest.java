package com.seguradora.hibrida.command.impl;

import com.seguradora.hibrida.command.*;
import com.seguradora.hibrida.command.example.TestCommand;
import com.seguradora.hibrida.command.example.TestCommandHandler;
import com.seguradora.hibrida.command.exception.CommandTimeoutException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link SimpleCommandBus}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SimpleCommandBus Tests")
class SimpleCommandBusTest {

    private CommandHandlerRegistry registry;
    private SimpleCommandBus bus;

    @BeforeEach
    void setUp() {
        registry = new CommandHandlerRegistry();
        bus = new SimpleCommandBus(registry);
        bus.registerHandler(new TestCommandHandler());
    }

    @AfterEach
    void tearDown() {
        bus.shutdown();
    }

    // =========================================================================
    // send() — caminho feliz
    // =========================================================================

    @Nested
    @DisplayName("send() — execução síncrona")
    class SendTests {

        @Test
        @DisplayName("Deve processar comando válido com sucesso")
        void shouldProcessValidCommandSuccessfully() {
            // Given
            TestCommand command = TestCommand.builder()
                    .data("payload-ok").value(5).build();

            // When
            CommandResult result = bus.send(command);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).asString().startsWith("processed-");
        }

        @Test
        @DisplayName("Deve retornar falha para comando com valor negativo")
        void shouldReturnFailureForNegativeValue() {
            // Given
            TestCommand command = TestCommand.builder()
                    .data("bad").value(-10).build();

            // When
            CommandResult result = bus.send(command);

            // Then
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Resultado deve carregar correlationId do comando")
        void resultShouldCarryCommandCorrelationId() {
            UUID correlationId = UUID.randomUUID();
            TestCommand command = TestCommand.builder()
                    .correlationId(correlationId).data("x").build();

            CommandResult result = bus.send(command);

            assertThat(result.getCorrelationId()).isEqualTo(correlationId);
        }

        @Test
        @DisplayName("Resultado deve informar tempo de execução")
        void resultShouldReportExecutionTime() {
            TestCommand command = TestCommand.builder().data("timing").build();

            CommandResult result = bus.send(command);

            assertThat(result.getExecutionTimeMs()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Deve retornar falha VALIDATION_ERROR para commandId null")
        void shouldReturnValidationErrorForNullCommandId() {
            // Given – criar comando com commandId null via setter
            TestCommand command = TestCommand.builder().build();
            command.setCommandId(null);

            // When
            CommandResult result = bus.send(command);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        }

        @Test
        @DisplayName("Deve retornar falha VALIDATION_ERROR para timestamp null")
        void shouldReturnValidationErrorForNullTimestamp() {
            TestCommand command = TestCommand.builder().build();
            command.setTimestamp(null);

            CommandResult result = bus.send(command);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        }
    }

    // =========================================================================
    // send() — sem handler registrado
    // =========================================================================

    @Nested
    @DisplayName("send() — sem handler registrado")
    class SendSemHandler {

        @Test
        @DisplayName("Deve retornar falha EXECUTION_ERROR para comando sem handler")
        void shouldReturnExecutionErrorForCommandWithoutHandler() {
            // Given – comando sem handler
            Command orphan = new Command() {
                final UUID id = UUID.randomUUID();
                @Override public UUID getCommandId()    { return id; }
                @Override public java.time.Instant getTimestamp() { return java.time.Instant.now(); }
                @Override public UUID getCorrelationId() { return null; }
                @Override public String getUserId()     { return null; }
            };

            // When
            CommandResult result = bus.send(orphan);

            // Then
            assertThat(result.isSuccess()).isFalse();
        }
    }

    // =========================================================================
    // sendAsync()
    // =========================================================================

    @Nested
    @DisplayName("sendAsync() — execução assíncrona")
    class SendAsyncTests {

        @Test
        @DisplayName("Deve retornar CompletableFuture com resultado de sucesso")
        void shouldReturnCompletableFutureWithSuccessResult() throws Exception {
            // Given
            TestCommand command = TestCommand.builder().data("async-ok").value(1).build();

            // When
            CompletableFuture<CommandResult> future = bus.sendAsync(command);
            CommandResult result = future.get(30, TimeUnit.SECONDS);

            // Then
            assertThat(result.isSuccess()).isTrue();
        }
    }

    // =========================================================================
    // registerHandler / unregisterHandler / hasHandler
    // =========================================================================

    @Nested
    @DisplayName("Gerenciamento de handlers")
    class GerenciamentoDeHandlers {

        @Test
        @DisplayName("hasHandler deve retornar true após registerHandler")
        void shouldReturnTrueAfterRegisterHandler() {
            assertThat(bus.hasHandler(TestCommand.class)).isTrue();
        }

        @Test
        @DisplayName("hasHandler deve retornar false após unregisterHandler")
        void shouldReturnFalseAfterUnregisterHandler() {
            bus.unregisterHandler(TestCommand.class);
            assertThat(bus.hasHandler(TestCommand.class)).isFalse();
        }

        @Test
        @DisplayName("hasHandler deve retornar false para tipo nunca registrado")
        void shouldReturnFalseForNeverRegisteredType() {
            assertThat(bus.hasHandler(Command.class)).isFalse();
        }
    }

    // =========================================================================
    // getStatistics
    // =========================================================================

    @Nested
    @DisplayName("getStatistics()")
    class GetStatisticsTests {

        @Test
        @DisplayName("Deve retornar estatísticas não nulas")
        void shouldReturnNonNullStatistics() {
            assertThat(bus.getStatistics()).isNotNull();
        }

        @Test
        @DisplayName("Deve reportar 1 handler registrado após setup")
        void shouldReportOneRegisteredHandler() {
            assertThat(bus.getStatistics().getRegisteredHandlers()).isEqualTo(1);
        }

        @Test
        @DisplayName("Deve incrementar totalCommandsProcessed após send bem-sucedido")
        void shouldIncrementProcessedAfterSuccessfulSend() {
            TestCommand command = TestCommand.builder().data("stat").value(1).build();
            bus.send(command);

            assertThat(bus.getStatistics().getTotalCommandsProcessed().get())
                    .isGreaterThanOrEqualTo(1L);
        }
    }
}
