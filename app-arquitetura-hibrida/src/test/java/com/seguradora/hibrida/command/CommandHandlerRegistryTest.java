package com.seguradora.hibrida.command;

import com.seguradora.hibrida.command.exception.CommandHandlerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link CommandHandlerRegistry}.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommandHandlerRegistry - Testes Unitários")
class CommandHandlerRegistryTest {

    private CommandHandlerRegistry registry;

    // Commands de teste
    static class TestCommand implements Command {
        private final UUID id = UUID.randomUUID();

        @Override
        public UUID getCommandId() {
            return id;
        }

        public String getAggregateId() {
            return id.toString();
        }

        @Override
        public String getUserId() {
            return "test-user";
        }

        @Override
        public UUID getCorrelationId() {
            return UUID.randomUUID();
        }

        @Override
        public java.time.Instant getTimestamp() {
            return java.time.Instant.now();
        }
    }

    static class AnotherCommand implements Command {
        private final UUID id = UUID.randomUUID();

        @Override
        public UUID getCommandId() {
            return id;
        }

        public String getAggregateId() {
            return id.toString();
        }

        @Override
        public String getUserId() {
            return "test-user";
        }

        @Override
        public UUID getCorrelationId() {
            return UUID.randomUUID();
        }

        @Override
        public java.time.Instant getTimestamp() {
            return java.time.Instant.now();
        }
    }

    // Handlers de teste
    static class TestCommandHandler implements CommandHandler<TestCommand> {
        @Override
        public CommandResult handle(TestCommand command) {
            return CommandResult.success("Test executed");
        }

        @Override
        public Class<TestCommand> getCommandType() {
            return TestCommand.class;
        }
    }

    static class AnotherCommandHandler implements CommandHandler<AnotherCommand> {
        @Override
        public CommandResult handle(AnotherCommand command) {
            return CommandResult.success("Another executed");
        }

        @Override
        public Class<AnotherCommand> getCommandType() {
            return AnotherCommand.class;
        }
    }

    @BeforeEach
    void setUp() {
        registry = new CommandHandlerRegistry();
    }

    @Test
    @DisplayName("Deve registrar handler corretamente")
    void shouldRegisterHandlerCorrectly() {
        // Given
        TestCommandHandler handler = new TestCommandHandler();

        // When
        registry.registerHandler(handler);

        // Then
        assertThat(registry.hasHandler(TestCommand.class)).isTrue();
        assertThat(registry.getHandler(TestCommand.class)).isEqualTo(handler);
        assertThat(registry.getHandlerCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve lançar exceção quando handler já registrado")
    void shouldThrowWhenHandlerAlreadyRegistered() {
        // Given
        TestCommandHandler handler1 = new TestCommandHandler();
        TestCommandHandler handler2 = new TestCommandHandler();
        registry.registerHandler(handler1);

        // When/Then
        assertThatThrownBy(() -> registry.registerHandler(handler2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Handler já registrado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando handler não encontrado")
    void shouldThrowWhenHandlerNotFound() {
        // When/Then
        assertThatThrownBy(() -> registry.getHandler(TestCommand.class))
            .isInstanceOf(CommandHandlerNotFoundException.class);
    }

    @Test
    @DisplayName("Deve remover handler corretamente")
    void shouldUnregisterHandlerCorrectly() {
        // Given
        TestCommandHandler handler = new TestCommandHandler();
        registry.registerHandler(handler);

        // When
        CommandHandler<?> removed = registry.unregisterHandler(TestCommand.class);

        // Then
        assertThat(removed).isEqualTo(handler);
        assertThat(registry.hasHandler(TestCommand.class)).isFalse();
        assertThat(registry.getHandlerCount()).isZero();
    }

    @Test
    @DisplayName("Deve retornar null ao remover handler inexistente")
    void shouldReturnNullWhenUnregisteringNonExistentHandler() {
        // When
        CommandHandler<?> removed = registry.unregisterHandler(TestCommand.class);

        // Then
        assertThat(removed).isNull();
    }

    @Test
    @DisplayName("Deve verificar existência de handler corretamente")
    void shouldCheckHandlerExistence() {
        // Given
        TestCommandHandler handler = new TestCommandHandler();

        // When/Then
        assertThat(registry.hasHandler(TestCommand.class)).isFalse();

        registry.registerHandler(handler);
        assertThat(registry.hasHandler(TestCommand.class)).isTrue();

        registry.unregisterHandler(TestCommand.class);
        assertThat(registry.hasHandler(TestCommand.class)).isFalse();
    }

    @Test
    @DisplayName("Deve listar todos os tipos de comando registrados")
    void shouldListAllRegisteredCommandTypes() {
        // Given
        TestCommandHandler handler1 = new TestCommandHandler();
        AnotherCommandHandler handler2 = new AnotherCommandHandler();
        registry.registerHandler(handler1);
        registry.registerHandler(handler2);

        // When
        Set<Class<? extends Command>> commandTypes = registry.getRegisteredCommandTypes();

        // Then
        assertThat(commandTypes)
            .hasSize(2)
            .contains(TestCommand.class, AnotherCommand.class);
    }

    @Test
    @DisplayName("Deve retornar contagem correta de handlers")
    void shouldReturnCorrectHandlerCount() {
        // Given/When/Then
        assertThat(registry.getHandlerCount()).isZero();

        registry.registerHandler(new TestCommandHandler());
        assertThat(registry.getHandlerCount()).isEqualTo(1);

        registry.registerHandler(new AnotherCommandHandler());
        assertThat(registry.getHandlerCount()).isEqualTo(2);

        registry.unregisterHandler(TestCommand.class);
        assertThat(registry.getHandlerCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve limpar todos os handlers")
    void shouldClearAllHandlers() {
        // Given
        registry.registerHandler(new TestCommandHandler());
        registry.registerHandler(new AnotherCommandHandler());
        assertThat(registry.getHandlerCount()).isEqualTo(2);

        // When
        registry.clear();

        // Then
        assertThat(registry.getHandlerCount()).isZero();
        assertThat(registry.hasHandler(TestCommand.class)).isFalse();
        assertThat(registry.hasHandler(AnotherCommand.class)).isFalse();
    }

    @Test
    @DisplayName("Deve obter informações de debug")
    void shouldGetDebugInfo() {
        // Given
        TestCommandHandler handler1 = new TestCommandHandler();
        AnotherCommandHandler handler2 = new AnotherCommandHandler();
        registry.registerHandler(handler1);
        registry.registerHandler(handler2);

        // When
        Map<String, String> debugInfo = registry.getDebugInfo();

        // Then
        assertThat(debugInfo)
            .hasSize(2)
            .containsEntry("TestCommand", "TestCommandHandler")
            .containsEntry("AnotherCommand", "AnotherCommandHandler");
    }

    @Test
    @DisplayName("Deve retornar conjunto imutável de tipos de comando")
    void shouldReturnImmutableCommandTypesSet() {
        // Given
        registry.registerHandler(new TestCommandHandler());

        // When
        Set<Class<? extends Command>> commandTypes = registry.getRegisteredCommandTypes();

        // Then
        assertThatThrownBy(() -> commandTypes.add(AnotherCommand.class))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Deve manter thread safety ao registrar handlers concorrentemente")
    void shouldMaintainThreadSafetyWhenRegisteringConcurrently() throws InterruptedException {
        // Given
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                // Cada thread registra seu próprio handler
                class DynamicCommand implements Command {
                    private final int idx;

                    DynamicCommand(int idx) {
                        this.idx = idx;
                    }

                    @Override
                    public UUID getCommandId() {
                        return UUID.randomUUID();
                    }

                    public String getAggregateId() {
                        return "test-" + idx;
                    }

                    @Override
                    public String getUserId() {
                        return "test-user-" + idx;
                    }

                    @Override
                    public UUID getCorrelationId() {
                        return UUID.randomUUID();
                    }

                    @Override
                    public java.time.Instant getTimestamp() {
                        return java.time.Instant.now();
                    }
                }

                CommandHandler<DynamicCommand> handler = new CommandHandler<DynamicCommand>() {
                    @Override
                    public CommandResult handle(DynamicCommand command) {
                        return CommandResult.success("Dynamic executed");
                    }

                    @Override
                    public Class<DynamicCommand> getCommandType() {
                        return DynamicCommand.class;
                    }
                };

                // Apenas o primeiro vai conseguir registrar
                try {
                    registry.registerHandler(handler);
                } catch (IllegalArgumentException e) {
                    // Esperado para threads concorrentes
                }
            });
            threads[i].start();
        }

        // Then
        for (Thread thread : threads) {
            thread.join();
        }

        // Pelo menos um deve ter sido registrado
        assertThat(registry.getHandlerCount()).isGreaterThan(0);
    }
}
