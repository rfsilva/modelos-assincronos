package com.seguradora.hibrida.command;

import com.seguradora.hibrida.command.example.TestCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para a interface {@link Command}.
 *
 * <p>Usa {@link TestCommand} como implementação concreta para exercitar o contrato.
 */
@DisplayName("Command Tests")
class CommandTest {

    // =========================================================================
    // Contrato da interface
    // =========================================================================

    @Nested
    @DisplayName("Contrato da interface")
    class ContratoInterface {

        @Test
        @DisplayName("Interface deve declarar getCommandId")
        void shouldDeclareGetCommandId() throws NoSuchMethodException {
            assertThat(Command.class.getMethod("getCommandId")).isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar getTimestamp")
        void shouldDeclareGetTimestamp() throws NoSuchMethodException {
            assertThat(Command.class.getMethod("getTimestamp")).isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar getCorrelationId")
        void shouldDeclareGetCorrelationId() throws NoSuchMethodException {
            assertThat(Command.class.getMethod("getCorrelationId")).isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar getUserId")
        void shouldDeclareGetUserId() throws NoSuchMethodException {
            assertThat(Command.class.getMethod("getUserId")).isNotNull();
        }

        @Test
        @DisplayName("getCommandType deve ser método default")
        void getCommandTypeShouldBeDefaultMethod() throws NoSuchMethodException {
            assertThat(Command.class.getMethod("getCommandType").isDefault()).isTrue();
        }
    }

    // =========================================================================
    // Método default getCommandType
    // =========================================================================

    @Nested
    @DisplayName("getCommandType (método default)")
    class GetCommandType {

        @Test
        @DisplayName("Deve retornar nome simples da classe por padrão")
        void shouldReturnSimpleClassNameByDefault() {
            // Given
            TestCommand command = TestCommand.builder().build();

            // Then
            assertThat(command.getCommandType()).isEqualTo("TestCommand");
        }

        @Test
        @DisplayName("Pode ser sobrescrito para retornar nome customizado")
        void canBeOverriddenToReturnCustomName() {
            // Given
            Command custom = new Command() {
                @Override public UUID getCommandId()    { return UUID.randomUUID(); }
                @Override public Instant getTimestamp() { return Instant.now(); }
                @Override public UUID getCorrelationId(){ return null; }
                @Override public String getUserId()     { return null; }
                @Override public String getCommandType(){ return "MeuComandoCustom"; }
            };

            // Then
            assertThat(custom.getCommandType()).isEqualTo("MeuComandoCustom");
        }
    }

    // =========================================================================
    // TestCommand como implementação concreta
    // =========================================================================

    @Nested
    @DisplayName("TestCommand como implementação concreta de Command")
    class TestCommandImplementsCommand {

        @Test
        @DisplayName("TestCommand deve implementar Command")
        void testCommandShouldImplementCommand() {
            assertThat(TestCommand.class).isAssignableTo(Command.class);
        }

        @Test
        @DisplayName("Deve gerar commandId único por padrão")
        void shouldGenerateUniqueCommandId() {
            TestCommand c1 = TestCommand.builder().build();
            TestCommand c2 = TestCommand.builder().build();

            assertThat(c1.getCommandId()).isNotEqualTo(c2.getCommandId());
        }

        @Test
        @DisplayName("Deve ter timestamp preenchido por padrão")
        void shouldHaveTimestampFilledByDefault() {
            TestCommand command = TestCommand.builder().build();

            assertThat(command.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Deve aceitar correlationId e userId via builder")
        void shouldAcceptCorrelationIdAndUserIdViaBuilder() {
            UUID correlationId = UUID.randomUUID();
            TestCommand command = TestCommand.builder()
                    .correlationId(correlationId)
                    .userId("user-42")
                    .build();

            assertThat(command.getCorrelationId()).isEqualTo(correlationId);
            assertThat(command.getUserId()).isEqualTo("user-42");
        }
    }
}
