package com.seguradora.hibrida.command;

import com.seguradora.hibrida.command.example.TestCommand;
import com.seguradora.hibrida.command.example.TestCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para a interface {@link CommandHandler}.
 *
 * <p>Usa {@link TestCommandHandler} como implementação concreta para exercitar o contrato.
 */
@DisplayName("CommandHandler Tests")
class CommandHandlerTest {

    private TestCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TestCommandHandler();
    }

    // =========================================================================
    // Contrato da interface
    // =========================================================================

    @Nested
    @DisplayName("Contrato da interface")
    class ContratoInterface {

        @Test
        @DisplayName("Interface deve declarar handle(T)")
        void shouldDeclareHandleMethod() throws NoSuchMethodException {
            assertThat(CommandHandler.class.getMethod("handle", Command.class)).isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar getCommandType()")
        void shouldDeclareGetCommandTypeMethod() throws NoSuchMethodException {
            assertThat(CommandHandler.class.getMethod("getCommandType")).isNotNull();
        }

        @Test
        @DisplayName("supports deve ser método default")
        void supportsShouldBeDefaultMethod() throws NoSuchMethodException {
            assertThat(CommandHandler.class.getMethod("supports", Class.class).isDefault()).isTrue();
        }

        @Test
        @DisplayName("getTimeoutSeconds deve ser método default")
        void getTimeoutSecondsShouldBeDefaultMethod() throws NoSuchMethodException {
            assertThat(CommandHandler.class.getMethod("getTimeoutSeconds").isDefault()).isTrue();
        }
    }

    // =========================================================================
    // Métodos default
    // =========================================================================

    @Nested
    @DisplayName("Métodos default")
    class MetodosDefault {

        @Test
        @DisplayName("getTimeoutSeconds padrão deve retornar 30")
        void defaultTimeoutSecondsShouldBeThirty() {
            // Implementação anônima usando o default
            CommandHandler<TestCommand> minimal = new CommandHandler<>() {
                @Override public CommandResult handle(TestCommand c) { return CommandResult.success("ok"); }
                @Override public Class<TestCommand> getCommandType()  { return TestCommand.class; }
            };

            assertThat(minimal.getTimeoutSeconds()).isEqualTo(30);
        }

        @Test
        @DisplayName("supports deve retornar true para o tipo correto")
        void supportsShouldReturnTrueForCorrectType() {
            assertThat(handler.supports(TestCommand.class)).isTrue();
        }

        @Test
        @DisplayName("supports deve retornar false para tipo diferente")
        void supportsShouldReturnFalseForDifferentType() {
            assertThat(handler.supports(Command.class)).isFalse();
        }
    }

    // =========================================================================
    // TestCommandHandler
    // =========================================================================

    @Nested
    @DisplayName("TestCommandHandler: implementação concreta")
    class TestCommandHandlerTests {

        @Test
        @DisplayName("getCommandType deve retornar TestCommand.class")
        void getCommandTypeShouldReturnTestCommandClass() {
            assertThat(handler.getCommandType()).isEqualTo(TestCommand.class);
        }

        @Test
        @DisplayName("getTimeoutSeconds deve retornar 10 (customizado)")
        void getTimeoutSecondsShouldReturnTen() {
            assertThat(handler.getTimeoutSeconds()).isEqualTo(10);
        }

        @Test
        @DisplayName("handle deve retornar sucesso para comando válido")
        void handleShouldReturnSuccessForValidCommand() {
            // Given
            TestCommand command = TestCommand.builder()
                    .data("dados-teste")
                    .value(5)
                    .build();

            // When
            CommandResult result = handler.handle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("handle deve retornar falha para valor negativo")
        void handleShouldReturnFailureForNegativeValue() {
            // Given
            TestCommand command = TestCommand.builder()
                    .data("dados-teste")
                    .value(-1)
                    .build();

            // When
            CommandResult result = handler.handle(command);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("INVALID_VALUE");
        }

        @Test
        @DisplayName("handle deve retornar sucesso para valor null")
        void handleShouldReturnSuccessForNullValue() {
            // Given
            TestCommand command = TestCommand.builder()
                    .data("dados-teste")
                    .value(null)
                    .build();

            // When
            CommandResult result = handler.handle(command);

            // Then
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("handle deve incluir metadados no resultado de sucesso")
        void handleShouldIncludeMetadataInSuccessResult() {
            // Given
            TestCommand command = TestCommand.builder()
                    .data("payload")
                    .value(10)
                    .build();

            // When
            CommandResult result = handler.handle(command);

            // Then
            assertThat(result.getMetadata()).isNotNull();
            assertThat(result.getMetadata()).containsKey("originalData");
            assertThat(result.getMetadata()).containsKey("processedValue");
        }

        @Test
        @DisplayName("resultId deve iniciar com 'processed-'")
        void resultIdShouldStartWithProcessed() {
            // Given
            TestCommand command = TestCommand.builder().data("x").value(1).build();

            // When
            CommandResult result = handler.handle(command);

            // Then
            assertThat(result.getData()).asString().startsWith("processed-");
        }
    }
}
