package com.seguradora.hibrida.command.example;

import com.seguradora.hibrida.command.Command;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link TestCommand}.
 */
@DisplayName("TestCommand Tests")
class TestCommandTest {

    // =========================================================================
    // Construção via builder
    // =========================================================================

    @Nested
    @DisplayName("Construção via builder")
    class ConstrucaoViaBuilder {

        @Test
        @DisplayName("builder deve gerar commandId único automaticamente")
        void builderShouldGenerateUniqueCommandId() {
            TestCommand c1 = TestCommand.builder().build();
            TestCommand c2 = TestCommand.builder().build();

            assertThat(c1.getCommandId()).isNotNull();
            assertThat(c2.getCommandId()).isNotNull();
            assertThat(c1.getCommandId()).isNotEqualTo(c2.getCommandId());
        }

        @Test
        @DisplayName("builder deve gerar timestamp próximo ao instante atual")
        void builderShouldGenerateTimestampNearNow() {
            Instant before = Instant.now();
            TestCommand command = TestCommand.builder().build();
            Instant after = Instant.now();

            assertThat(command.getTimestamp()).isBetween(before, after);
        }

        @Test
        @DisplayName("builder deve definir active=true por padrão")
        void builderShouldDefaultActiveTrueTrue() {
            TestCommand command = TestCommand.builder().build();
            assertThat(command.getActive()).isTrue();
        }

        @Test
        @DisplayName("builder deve aceitar data e value customizados")
        void builderShouldAcceptCustomDataAndValue() {
            TestCommand command = TestCommand.builder()
                    .data("payload")
                    .value(42)
                    .build();

            assertThat(command.getData()).isEqualTo("payload");
            assertThat(command.getValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("builder deve aceitar correlationId e userId")
        void builderShouldAcceptCorrelationIdAndUserId() {
            UUID correlationId = UUID.randomUUID();

            TestCommand command = TestCommand.builder()
                    .correlationId(correlationId)
                    .userId("user-42")
                    .build();

            assertThat(command.getCorrelationId()).isEqualTo(correlationId);
            assertThat(command.getUserId()).isEqualTo("user-42");
        }

        @Test
        @DisplayName("builder deve permitir commandId customizado")
        void builderShouldAllowCustomCommandId() {
            UUID customId = UUID.randomUUID();

            TestCommand command = TestCommand.builder()
                    .commandId(customId)
                    .build();

            assertThat(command.getCommandId()).isEqualTo(customId);
        }
    }

    // =========================================================================
    // Implementação da interface Command
    // =========================================================================

    @Nested
    @DisplayName("Implementação de Command")
    class ImplementacaoCommand {

        @Test
        @DisplayName("TestCommand deve implementar a interface Command")
        void shouldImplementCommandInterface() {
            TestCommand command = TestCommand.builder().build();
            assertThat(command).isInstanceOf(Command.class);
        }

        @Test
        @DisplayName("getCommandType padrão deve retornar 'TestCommand'")
        void defaultGetCommandTypeShouldReturnSimpleName() {
            TestCommand command = TestCommand.builder().build();
            assertThat(command.getCommandType()).isEqualTo("TestCommand");
        }
    }

    // =========================================================================
    // Setters (Lombok @Data)
    // =========================================================================

    @Nested
    @DisplayName("Setters via @Data")
    class SettersTests {

        @Test
        @DisplayName("setCommandId deve atualizar o campo")
        void setCommandIdShouldUpdateField() {
            TestCommand command = TestCommand.builder().build();
            UUID newId = UUID.randomUUID();
            command.setCommandId(newId);

            assertThat(command.getCommandId()).isEqualTo(newId);
        }

        @Test
        @DisplayName("setCommandId(null) deve aceitar null")
        void setCommandIdNullShouldBeAccepted() {
            TestCommand command = TestCommand.builder().build();
            command.setCommandId(null);

            assertThat(command.getCommandId()).isNull();
        }

        @Test
        @DisplayName("setTimestamp deve atualizar o campo")
        void setTimestampShouldUpdateField() {
            TestCommand command = TestCommand.builder().build();
            Instant newTs = Instant.EPOCH;
            command.setTimestamp(newTs);

            assertThat(command.getTimestamp()).isEqualTo(Instant.EPOCH);
        }

        @Test
        @DisplayName("setActive(false) deve atualizar o campo")
        void setActiveFalseShouldUpdateField() {
            TestCommand command = TestCommand.builder().build();
            command.setActive(false);

            assertThat(command.getActive()).isFalse();
        }
    }

    // =========================================================================
    // Construtor no-args
    // =========================================================================

    @Test
    @DisplayName("Construtor no-args deve criar instância com commandId gerado e campos de dados nulos")
    void noArgsConstructorShouldCreateInstanceWithNullFields() {
        TestCommand command = new TestCommand();

        // commandId é @Builder.Default → sempre gerado (nunca null)
        assertThat(command.getCommandId()).isNotNull();
        assertThat(command.getData()).isNull();
        assertThat(command.getValue()).isNull();
    }
}
