package com.seguradora.hibrida.command.validation;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.command.example.TestCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link CommandValidator}.
 */
@DisplayName("CommandValidator Tests")
class CommandValidatorTest {

    // Implementação concreta de teste
    private static class TestCommandValidator implements CommandValidator<TestCommand> {

        @Override
        public ValidationResult validate(TestCommand command) {
            if (command.getData() == null || command.getData().isBlank()) {
                return ValidationResult.invalid("data é obrigatório", "REQUIRED_DATA");
            }
            if (command.getValue() != null && command.getValue() < 0) {
                return ValidationResult.invalid("value não pode ser negativo", "NEGATIVE_VALUE");
            }
            return ValidationResult.valid();
        }

        @Override
        public Class<TestCommand> getCommandType() {
            return TestCommand.class;
        }
    }

    // Implementação que sobrescreve getPriority
    private static class PriorityValidator implements CommandValidator<TestCommand> {
        @Override public ValidationResult validate(TestCommand cmd) { return ValidationResult.valid(); }
        @Override public Class<TestCommand> getCommandType() { return TestCommand.class; }
        @Override public int getPriority() { return 10; }
    }

    // =========================================================================
    // Contrato da interface
    // =========================================================================

    @Nested
    @DisplayName("Contrato da interface")
    class ContratoInterface {

        @Test
        @DisplayName("Deve declarar os métodos abstratos validate e getCommandType")
        void shouldDeclareAbstractMethods() {
            Set<String> methodNames = Arrays.stream(CommandValidator.class.getDeclaredMethods())
                    .map(Method::getName)
                    .collect(Collectors.toSet());

            assertThat(methodNames).contains("validate", "getCommandType");
        }

        @Test
        @DisplayName("supports deve ser método default")
        void supportsShouldBeDefaultMethod() throws NoSuchMethodException {
            Method method = CommandValidator.class.getMethod("supports", Class.class);
            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("getPriority deve ser método default")
        void getPriorityShouldBeDefaultMethod() throws NoSuchMethodException {
            Method method = CommandValidator.class.getMethod("getPriority");
            assertThat(method.isDefault()).isTrue();
        }
    }

    // =========================================================================
    // validate()
    // =========================================================================

    @Nested
    @DisplayName("validate() — lógica do validador concreto")
    class ValidateTests {

        private final TestCommandValidator validator = new TestCommandValidator();

        @Test
        @DisplayName("Deve retornar válido para comando com data e value positivo")
        void shouldReturnValidForCorrectCommand() {
            TestCommand command = TestCommand.builder().data("ok").value(10).build();

            ValidationResult result = validator.validate(command);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar inválido quando data é nulo")
        void shouldReturnInvalidWhenDataIsNull() {
            TestCommand command = TestCommand.builder().data(null).build();

            ValidationResult result = validator.validate(command);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrorCode()).isEqualTo("REQUIRED_DATA");
        }

        @Test
        @DisplayName("Deve retornar inválido quando data é em branco")
        void shouldReturnInvalidWhenDataIsBlank() {
            TestCommand command = TestCommand.builder().data("   ").build();

            ValidationResult result = validator.validate(command);

            assertThat(result.isInvalid()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar inválido quando value é negativo")
        void shouldReturnInvalidWhenValueIsNegative() {
            TestCommand command = TestCommand.builder().data("ok").value(-5).build();

            ValidationResult result = validator.validate(command);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrorCode()).isEqualTo("NEGATIVE_VALUE");
        }
    }

    // =========================================================================
    // supports() — método default
    // =========================================================================

    @Nested
    @DisplayName("supports() — método default")
    class SupportsTests {

        private final TestCommandValidator validator = new TestCommandValidator();

        @Test
        @DisplayName("supports deve retornar true para o tipo correto")
        void shouldReturnTrueForMatchingType() {
            assertThat(validator.supports(TestCommand.class)).isTrue();
        }

        @Test
        @DisplayName("supports deve retornar false para tipo diferente")
        void shouldReturnFalseForDifferentType() {
            assertThat(validator.supports(Command.class)).isFalse();
        }
    }

    // =========================================================================
    // getPriority() — método default
    // =========================================================================

    @Nested
    @DisplayName("getPriority() — método default")
    class GetPriorityTests {

        @Test
        @DisplayName("getPriority padrão deve ser 100")
        void defaultPriorityShouldBe100() {
            TestCommandValidator validator = new TestCommandValidator();
            assertThat(validator.getPriority()).isEqualTo(100);
        }

        @Test
        @DisplayName("getPriority deve poder ser sobrescrito")
        void priorityShouldBeOverridable() {
            PriorityValidator validator = new PriorityValidator();
            assertThat(validator.getPriority()).isEqualTo(10);
        }
    }

    // =========================================================================
    // getCommandType()
    // =========================================================================

    @Test
    @DisplayName("getCommandType deve retornar o tipo do comando configurado")
    void getCommandTypeShouldReturnConfiguredType() {
        TestCommandValidator validator = new TestCommandValidator();
        assertThat(validator.getCommandType()).isEqualTo(TestCommand.class);
    }
}
