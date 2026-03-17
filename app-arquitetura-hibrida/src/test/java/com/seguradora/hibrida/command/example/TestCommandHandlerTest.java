package com.seguradora.hibrida.command.example;

import com.seguradora.hibrida.command.CommandResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link TestCommandHandler}.
 */
@DisplayName("TestCommandHandler Tests")
class TestCommandHandlerTest {

    private TestCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TestCommandHandler();
    }

    // =========================================================================
    // Anotações e metadados
    // =========================================================================

    @Nested
    @DisplayName("Anotações e metadados")
    class AnotacoesEMetadados {

        @Test
        @DisplayName("Deve estar anotado com @Component")
        void shouldBeAnnotatedWithComponent() {
            assertThat(TestCommandHandler.class.isAnnotationPresent(Component.class)).isTrue();
        }

        @Test
        @DisplayName("getCommandType deve retornar TestCommand.class")
        void getCommandTypeShouldReturnTestCommandClass() {
            assertThat(handler.getCommandType()).isEqualTo(TestCommand.class);
        }

        @Test
        @DisplayName("getTimeoutSeconds deve retornar 10 (valor customizado)")
        void getTimeoutSecondsShouldReturnTen() {
            assertThat(handler.getTimeoutSeconds()).isEqualTo(10);
        }

        @Test
        @DisplayName("supports deve retornar true para TestCommand")
        void supportsShouldReturnTrueForTestCommand() {
            assertThat(handler.supports(TestCommand.class)).isTrue();
        }
    }

    // =========================================================================
    // handle() — caminho feliz
    // =========================================================================

    @Nested
    @DisplayName("handle() — sucesso")
    class HandleSucessoTests {

        @Test
        @DisplayName("Deve retornar sucesso para comando válido")
        void shouldReturnSuccessForValidCommand() {
            TestCommand command = TestCommand.builder().data("payload").value(5).build();

            CommandResult result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("getData deve iniciar com 'processed-'")
        void getDataShouldStartWithProcessed() {
            TestCommand command = TestCommand.builder().data("x").value(1).build();

            CommandResult result = handler.handle(command);

            assertThat(result.getData()).asString().startsWith("processed-");
        }

        @Test
        @DisplayName("metadata deve conter chaves 'processedAt', 'originalData' e 'processedValue'")
        void metadataShouldContainExpectedKeys() {
            TestCommand command = TestCommand.builder().data("info").value(3).build();

            CommandResult result = handler.handle(command);

            assertThat(result.getMetadata())
                    .containsKeys("processedAt", "originalData", "processedValue");
        }

        @Test
        @DisplayName("processedValue deve ser o dobro do value original")
        void processedValueShouldBeDoubleOfOriginal() {
            TestCommand command = TestCommand.builder().data("calc").value(7).build();

            CommandResult result = handler.handle(command);

            assertThat(result.getMetadata().get("processedValue")).isEqualTo(14);
        }

        @Test
        @DisplayName("originalData deve refletir o data do comando")
        void originalDataShouldReflectCommandData() {
            TestCommand command = TestCommand.builder().data("meu-dado").value(1).build();

            CommandResult result = handler.handle(command);

            assertThat(result.getMetadata().get("originalData")).isEqualTo("meu-dado");
        }

        @Test
        @DisplayName("processedValue deve ser 0 quando value é null")
        void processedValueShouldBeZeroWhenValueIsNull() {
            TestCommand command = TestCommand.builder().data("sem-valor").build();

            CommandResult result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMetadata().get("processedValue")).isEqualTo(0);
        }
    }

    // =========================================================================
    // handle() — cenários de falha
    // =========================================================================

    @Nested
    @DisplayName("handle() — falha")
    class HandleFalhaTests {

        @Test
        @DisplayName("Deve retornar falha para value negativo")
        void shouldReturnFailureForNegativeValue() {
            TestCommand command = TestCommand.builder().data("neg").value(-1).build();

            CommandResult result = handler.handle(command);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("INVALID_VALUE");
        }

        @Test
        @DisplayName("Mensagem de erro deve indicar valor negativo")
        void errorMessageShouldIndicateNegativeValue() {
            TestCommand command = TestCommand.builder().data("neg").value(-100).build();

            CommandResult result = handler.handle(command);

            assertThat(result.getErrorMessage()).contains("negativo");
        }
    }
}
