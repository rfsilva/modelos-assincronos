package com.seguradora.hibrida.domain.segurado.model;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link CPF}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("CPF - Testes Unitários")
class CPFTest {

    @Test
    @DisplayName("Deve criar CPF válido sem formatação")
    void shouldCreateValidCPFWithoutFormatting() {
        // Given
        String cpfValido = "12345678909";

        // When
        CPF cpf = CPF.of(cpfValido);

        // Then
        assertThat(cpf.getNumero()).isEqualTo("12345678909");
        assertThat(cpf.getFormatado()).isEqualTo("123.456.789-09");
    }

    @Test
    @DisplayName("Deve criar CPF válido com formatação")
    void shouldCreateValidCPFWithFormatting() {
        // Given
        String cpfFormatado = "123.456.789-09";

        // When
        CPF cpf = CPF.of(cpfFormatado);

        // Then
        assertThat(cpf.getNumero()).isEqualTo("12345678909");
        assertThat(cpf.getFormatado()).isEqualTo("123.456.789-09");
    }

    @Test
    @DisplayName("Deve remover caracteres não numéricos")
    void shouldRemoveNonNumericCharacters() {
        // When
        CPF cpf = CPF.of("123.456.789-09");

        // Then
        assertThat(cpf.getNumero()).isEqualTo("12345678909");
    }

    @Test
    @DisplayName("Deve aceitar CPFs válidos conhecidos")
    void shouldAcceptKnownValidCPFs() {
        assertThatNoException().isThrownBy(() -> CPF.of("12345678909"));
        assertThatNoException().isThrownBy(() -> CPF.of("11144477735"));
    }

    @Test
    @DisplayName("Deve falhar ao criar CPF nulo")
    void shouldFailToCreateNullCPF() {
        assertThatThrownBy(() -> CPF.of(null))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("obrigatório");
    }

    @Test
    @DisplayName("Deve falhar ao criar CPF vazio")
    void shouldFailToCreateEmptyCPF() {
        assertThatThrownBy(() -> CPF.of(""))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("obrigatório");

        assertThatThrownBy(() -> CPF.of("   "))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("obrigatório");
    }

    @Test
    @DisplayName("Deve falhar ao criar CPF com menos de 11 dígitos")
    void shouldFailToCreateCPFWithLessThan11Digits() {
        assertThatThrownBy(() -> CPF.of("1234567890"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("11 dígitos");
    }

    @Test
    @DisplayName("Deve falhar ao criar CPF com mais de 11 dígitos")
    void shouldFailToCreateCPFWithMoreThan11Digits() {
        assertThatThrownBy(() -> CPF.of("123456789012"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("11 dígitos");
    }

    @Test
    @DisplayName("Deve falhar ao criar CPF com todos os dígitos iguais")
    void shouldFailToCreateCPFWithAllSameDigits() {
        assertThatThrownBy(() -> CPF.of("00000000000"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("dígitos iguais");

        assertThatThrownBy(() -> CPF.of("11111111111"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("dígitos iguais");

        assertThatThrownBy(() -> CPF.of("99999999999"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("dígitos iguais");
    }

    @Test
    @DisplayName("Deve falhar ao criar CPF com dígitos verificadores inválidos")
    void shouldFailToCreateCPFWithInvalidCheckDigits() {
        // CPF com dígitos verificadores errados
        assertThatThrownBy(() -> CPF.of("12345678900"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("verificadores");

        assertThatThrownBy(() -> CPF.of("11144477734"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("verificadores");
    }

    @Test
    @DisplayName("Deve formatar CPF corretamente")
    void shouldFormatCPFCorrectly() {
        // Given
        CPF cpf = CPF.of("12345678909");

        // Then
        assertThat(cpf.getFormatado()).isEqualTo("123.456.789-09");
        assertThat(cpf.toString()).isEqualTo("123.456.789-09");
    }

    @Test
    @DisplayName("Deve ter equals e hashCode corretos")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        CPF cpf1 = CPF.of("12345678909");
        CPF cpf2 = CPF.of("123.456.789-09");
        CPF cpf3 = CPF.of("11144477735");

        // Then
        assertThat(cpf1).isEqualTo(cpf2);
        assertThat(cpf1).isNotEqualTo(cpf3);
        assertThat(cpf1.hashCode()).isEqualTo(cpf2.hashCode());
    }

    @Test
    @DisplayName("Deve validar CPF real corretamente")
    void shouldValidateRealCPFCorrectly() {
        // CPFs válidos conhecidos (gerados com algoritmo correto)
        assertThatNoException().isThrownBy(() -> CPF.of("12345678909"));
        assertThatNoException().isThrownBy(() -> CPF.of("11144477735"));
    }

    @Test
    @DisplayName("Deve retornar apenas números no getNumero")
    void shouldReturnOnlyNumbersInGetNumero() {
        // Given
        CPF cpf = CPF.of("123.456.789-09");

        // Then
        assertThat(cpf.getNumero()).matches("\\d{11}");
        assertThat(cpf.getNumero()).doesNotContain(".", "-");
    }
}
