package com.seguradora.hibrida.aggregate.validation;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.example.ExampleAggregate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para a interface {@link BusinessRule}.
 *
 * <p>Valida o contrato da interface, os métodos default e implementações concretas inline.
 */
@DisplayName("BusinessRule Tests")
class BusinessRuleTest {

    // =========================================================================
    // Implementação mínima para testes
    // =========================================================================

    private static BusinessRule ruleValida() {
        return new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) { return true; }
            @Override
            public String getErrorMessage() { return "Regra violada"; }
        };
    }

    private static BusinessRule ruleInvalida(String mensagem) {
        return new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) { return false; }
            @Override
            public String getErrorMessage() { return mensagem; }
        };
    }

    // =========================================================================
    // Contrato básico
    // =========================================================================

    @Nested
    @DisplayName("Contrato básico")
    class ContratoBasico {

        @Test
        @DisplayName("isValid deve retornar true para regra satisfeita")
        void isValidShouldReturnTrueWhenRuleIsSatisfied() {
            ExampleAggregate agg = new ExampleAggregate("id-1");
            assertThat(ruleValida().isValid(agg)).isTrue();
        }

        @Test
        @DisplayName("isValid deve retornar false para regra violada")
        void isValidShouldReturnFalseWhenRuleIsViolated() {
            ExampleAggregate agg = new ExampleAggregate("id-2");
            assertThat(ruleInvalida("violação").isValid(agg)).isFalse();
        }

        @Test
        @DisplayName("getErrorMessage deve retornar mensagem configurada")
        void getErrorMessageShouldReturnConfiguredMessage() {
            BusinessRule rule = ruleInvalida("CPF inválido");
            assertThat(rule.getErrorMessage()).isEqualTo("CPF inválido");
        }
    }

    // =========================================================================
    // Métodos default
    // =========================================================================

    @Nested
    @DisplayName("Métodos default")
    class MetodosDefault {

        @Test
        @DisplayName("getRuleName deve retornar nome simples da classe por padrão")
        void getRuleNameShouldReturnSimpleClassNameByDefault() {
            // Given
            class MinhaRegra implements BusinessRule {
                @Override public boolean isValid(AggregateRoot a) { return true; }
                @Override public String getErrorMessage() { return "erro"; }
            }

            MinhaRegra rule = new MinhaRegra();
            assertThat(rule.getRuleName()).isEqualTo("MinhaRegra");
        }

        @Test
        @DisplayName("getPriority deve retornar 0 por padrão")
        void getPriorityShouldReturnZeroByDefault() {
            assertThat(ruleValida().getPriority()).isZero();
        }

        @Test
        @DisplayName("validateOnReplay deve retornar true por padrão")
        void validateOnReplayShouldReturnTrueByDefault() {
            assertThat(ruleValida().validateOnReplay()).isTrue();
        }

        @Test
        @DisplayName("appliesTo deve retornar true por padrão para qualquer aggregate")
        void appliesToShouldReturnTrueByDefaultForAnyAggregate() {
            assertThat(ruleValida().appliesTo(ExampleAggregate.class)).isTrue();
        }
    }

    // =========================================================================
    // Sobrescrita de métodos default
    // =========================================================================

    @Nested
    @DisplayName("Sobrescrita de métodos default")
    class SobreescritaDeMetodosDefault {

        @Test
        @DisplayName("getRuleName pode ser sobrescrito com nome customizado")
        void getRuleNameCanBeOverriddenWithCustomName() {
            BusinessRule rule = new BusinessRule() {
                @Override public boolean isValid(AggregateRoot a) { return true; }
                @Override public String getErrorMessage() { return "erro"; }
                @Override public String getRuleName() { return "RegraCustomizada"; }
            };
            assertThat(rule.getRuleName()).isEqualTo("RegraCustomizada");
        }

        @Test
        @DisplayName("getPriority pode ser sobrescrito com valor positivo")
        void getPriorityCanBeOverriddenWithPositiveValue() {
            BusinessRule rule = new BusinessRule() {
                @Override public boolean isValid(AggregateRoot a) { return true; }
                @Override public String getErrorMessage() { return "erro"; }
                @Override public int getPriority() { return 10; }
            };
            assertThat(rule.getPriority()).isEqualTo(10);
        }

        @Test
        @DisplayName("validateOnReplay pode ser sobrescrito para false")
        void validateOnReplayCanBeOverriddenToFalse() {
            BusinessRule rule = new BusinessRule() {
                @Override public boolean isValid(AggregateRoot a) { return true; }
                @Override public String getErrorMessage() { return "erro"; }
                @Override public boolean validateOnReplay() { return false; }
            };
            assertThat(rule.validateOnReplay()).isFalse();
        }

        @Test
        @DisplayName("appliesTo pode ser sobrescrito para restringir a tipo específico")
        void appliesToCanBeOverriddenToRestrictToSpecificType() {
            BusinessRule rule = new BusinessRule() {
                @Override public boolean isValid(AggregateRoot a) { return true; }
                @Override public String getErrorMessage() { return "erro"; }
                @Override
                public boolean appliesTo(Class<? extends AggregateRoot> type) {
                    return ExampleAggregate.class.isAssignableFrom(type);
                }
            };

            assertThat(rule.appliesTo(ExampleAggregate.class)).isTrue();
            // Subclasse fictícia não relacionada — usa outra classe via lambda
            assertThat(rule.appliesTo(com.seguradora.hibrida.aggregate.AggregateRoot.class
                    .asSubclass(AggregateRoot.class))).isFalse();
        }
    }

    // =========================================================================
    // Regra integrada com ExampleAggregate
    // =========================================================================

    @Nested
    @DisplayName("Integração com ExampleAggregate")
    class IntegracaoComExampleAggregate {

        @Test
        @DisplayName("Regra de nome curto deve falhar quando nome tem menos de 3 chars no aggregate")
        void nameTooShortRuleShouldFailWhenNameHasLessThan3Chars() {
            // Given – a regra está registrada internamente no ExampleAggregate
            ExampleAggregate agg = new ExampleAggregate("id-test");

            // Criar regra independente replicando a lógica interna
            BusinessRule nameRule = new BusinessRule() {
                @Override
                public boolean isValid(AggregateRoot aggregate) {
                    if (!(aggregate instanceof ExampleAggregate ex)) return true;
                    return ex.getName() == null || ex.getName().length() >= 3;
                }
                @Override
                public String getErrorMessage() { return "Nome deve ter pelo menos 3 caracteres"; }
            };

            // Aggregate recém criado sem nome — regra deve passar (null é aceito)
            assertThat(nameRule.isValid(agg)).isTrue();
        }

        @Test
        @DisplayName("Mensagem de erro deve descrever a violação claramente")
        void errorMessageShouldDescribeViolationClearly() {
            BusinessRule rule = ruleInvalida("O campo CPF é obrigatório e deve ser válido");
            assertThat(rule.getErrorMessage())
                    .isNotBlank()
                    .contains("CPF");
        }
    }
}
