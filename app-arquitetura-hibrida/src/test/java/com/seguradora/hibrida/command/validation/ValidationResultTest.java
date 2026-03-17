package com.seguradora.hibrida.command.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ValidationResult}.
 */
@DisplayName("ValidationResult Tests")
class ValidationResultTest {

    // =========================================================================
    // valid()
    // =========================================================================

    @Nested
    @DisplayName("valid() — resultado válido")
    class ValidTests {

        @Test
        @DisplayName("valid() deve criar resultado com valid=true e sem erros")
        void shouldCreateValidResultWithNoErrors() {
            ValidationResult result = ValidationResult.valid();

            assertThat(result.isValid()).isTrue();
            assertThat(result.isInvalid()).isFalse();
            assertThat(result.hasErrorMessages()).isFalse();
            assertThat(result.getFirstErrorMessage()).isNull();
            assertThat(result.getErrorCode()).isNull();
        }

        @Test
        @DisplayName("valid(metadata) deve preservar metadados")
        void shouldPreserveMetadataInValidResult() {
            Map<String, Object> meta = Map.of("campo", "valor");

            ValidationResult result = ValidationResult.valid(meta);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getMetadata()).containsEntry("campo", "valor");
        }
    }

    // =========================================================================
    // invalid()
    // =========================================================================

    @Nested
    @DisplayName("invalid() — resultado inválido")
    class InvalidTests {

        @Test
        @DisplayName("invalid(mensagem) deve criar resultado com valid=false e uma mensagem")
        void shouldCreateInvalidResultWithOneMessage() {
            ValidationResult result = ValidationResult.invalid("CPF inválido");

            assertThat(result.isValid()).isFalse();
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.hasErrorMessages()).isTrue();
            assertThat(result.getFirstErrorMessage()).isEqualTo("CPF inválido");
        }

        @Test
        @DisplayName("invalid(mensagem, código) deve preservar mensagem e errorCode")
        void shouldPreserveMessageAndErrorCode() {
            ValidationResult result = ValidationResult.invalid("Valor negativo", "NEGATIVE_VALUE");

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrorCode()).isEqualTo("NEGATIVE_VALUE");
            assertThat(result.getFirstErrorMessage()).isEqualTo("Valor negativo");
        }

        @Test
        @DisplayName("invalid(List<mensagens>) deve preservar todas as mensagens")
        void shouldPreserveAllMessagesFromList() {
            List<String> messages = List.of("Erro 1", "Erro 2", "Erro 3");

            ValidationResult result = ValidationResult.invalid(messages);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrorMessages()).containsExactly("Erro 1", "Erro 2", "Erro 3");
        }

        @Test
        @DisplayName("invalid(List, código, metadata) deve preservar todos os campos")
        void shouldPreserveAllFieldsInFullInvalidFactory() {
            List<String> messages = List.of("Campo obrigatório");
            Map<String, Object> meta = Map.of("campo", "nome");

            ValidationResult result = ValidationResult.invalid(messages, "REQUIRED", meta);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrorCode()).isEqualTo("REQUIRED");
            assertThat(result.getMetadata()).containsEntry("campo", "nome");
        }
    }

    // =========================================================================
    // addErrorMessage / addErrorMessages
    // =========================================================================

    @Nested
    @DisplayName("addErrorMessage / addErrorMessages — method chaining")
    class AddErrorMessageTests {

        @Test
        @DisplayName("addErrorMessage deve adicionar mensagem e tornar inválido")
        void shouldAddMessageAndMarkInvalid() {
            ValidationResult result = ValidationResult.valid();
            result.addErrorMessage("Erro adicionado");

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getFirstErrorMessage()).isEqualTo("Erro adicionado");
        }

        @Test
        @DisplayName("addErrorMessage deve retornar a mesma instância (method chaining)")
        void shouldReturnSameInstanceForChaining() {
            ValidationResult result = ValidationResult.valid();

            assertThat(result.addErrorMessage("x")).isSameAs(result);
        }

        @Test
        @DisplayName("addErrorMessages deve adicionar múltiplas mensagens")
        void shouldAddMultipleMessages() {
            ValidationResult result = ValidationResult.valid();
            result.addErrorMessages(List.of("Erro A", "Erro B"));

            assertThat(result.getErrorMessages()).containsExactly("Erro A", "Erro B");
        }
    }

    // =========================================================================
    // withValidatorName / withMetadata
    // =========================================================================

    @Nested
    @DisplayName("withValidatorName / withMetadata — method chaining")
    class WithMethodsTests {

        @Test
        @DisplayName("withValidatorName deve definir o nome do validador")
        void shouldSetValidatorName() {
            ValidationResult result = ValidationResult.valid()
                    .withValidatorName("CpfUnicoValidator");

            assertThat(result.getValidatorName()).isEqualTo("CpfUnicoValidator");
        }

        @Test
        @DisplayName("withMetadata deve adicionar chave/valor ao mapa")
        void shouldAddKeyValueToMetadata() {
            ValidationResult result = ValidationResult.valid()
                    .withMetadata("campo", "cpf");

            assertThat(result.getMetadata()).containsEntry("campo", "cpf");
        }

        @Test
        @DisplayName("withMetadata deve suportar múltiplas chamadas encadeadas")
        void shouldSupportMultipleChainedWithMetadata() {
            ValidationResult result = ValidationResult.valid()
                    .withMetadata("a", 1)
                    .withMetadata("b", 2);

            assertThat(result.getMetadata()).containsKeys("a", "b");
        }
    }

    // =========================================================================
    // combine()
    // =========================================================================

    @Nested
    @DisplayName("combine() — combinação de resultados")
    class CombineTests {

        @Test
        @DisplayName("Dois válidos combinados devem resultar em válido")
        void twoValidsCombinedShouldBeValid() {
            ValidationResult a = ValidationResult.valid();
            ValidationResult b = ValidationResult.valid();

            assertThat(a.combine(b).isValid()).isTrue();
        }

        @Test
        @DisplayName("Válido combinado com inválido deve resultar em inválido")
        void validCombinedWithInvalidShouldBeInvalid() {
            ValidationResult valid = ValidationResult.valid();
            ValidationResult invalid = ValidationResult.invalid("Erro no campo");

            ValidationResult combined = valid.combine(invalid);

            assertThat(combined.isInvalid()).isTrue();
            assertThat(combined.getErrorMessages()).contains("Erro no campo");
        }

        @Test
        @DisplayName("Dois inválidos combinados devem unir as mensagens")
        void twoInvalidsCombinedShouldUnifyMessages() {
            ValidationResult a = ValidationResult.invalid("Erro A");
            ValidationResult b = ValidationResult.invalid("Erro B");

            ValidationResult combined = a.combine(b);

            assertThat(combined.isInvalid()).isTrue();
            assertThat(combined.getErrorMessages()).containsExactly("Erro A", "Erro B");
        }

        @Test
        @DisplayName("combine(null) deve retornar a instância original")
        void combineWithNullShouldReturnSelf() {
            ValidationResult result = ValidationResult.valid();

            assertThat(result.combine(null)).isSameAs(result);
        }

        @Test
        @DisplayName("Combine deve unir metadados de ambos")
        void shouldUnifyMetadata() {
            ValidationResult a = ValidationResult.valid().withMetadata("x", 1);
            ValidationResult b = ValidationResult.valid().withMetadata("y", 2);

            ValidationResult combined = a.combine(b);

            assertThat(combined.getMetadata()).containsKeys("x", "y");
        }
    }
}
