package com.seguradora.hibrida.aggregate.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BusinessRuleViolationException Tests")
class BusinessRuleViolationExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem e lista de violações")
    void shouldCreateWithMessageAndViolations() {
        String message = "Rule violated";
        List<String> violations = Arrays.asList("CPF inválido", "Idade menor que 18");

        BusinessRuleViolationException exception = new BusinessRuleViolationException(message, violations);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getViolations()).containsExactlyElementsOf(violations);
        assertThat(exception.hasViolations()).isTrue();
        assertThat(exception.getViolationCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve criar com mensagem e violação única")
    void shouldCreateWithMessageAndSingleViolation() {
        String message = "Rule violated";
        String violation = "CPF inválido";

        BusinessRuleViolationException exception = new BusinessRuleViolationException(message, violation);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getViolations()).containsExactly(violation);
        assertThat(exception.hasViolations()).isTrue();
        assertThat(exception.getViolationCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve ser uma AggregateException")
    void shouldBeAnAggregateException() {
        BusinessRuleViolationException exception = new BusinessRuleViolationException("Test", "violation");
        assertThat(exception).isInstanceOf(AggregateException.class);
    }

    @Test
    @DisplayName("Deve retornar violações como string")
    void shouldReturnViolationsAsString() {
        List<String> violations = Arrays.asList("Erro 1", "Erro 2");
        BusinessRuleViolationException exception = new BusinessRuleViolationException("Test", violations);

        String violationsString = exception.getViolationsAsString();
        assertThat(violationsString).contains("Erro 1");
        assertThat(violationsString).contains("Erro 2");
    }
}
