package com.seguradora.hibrida.domain.segurado.model;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link Email}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("Email - Testes Unitários")
class EmailTest {

    @Test
    @DisplayName("Deve criar email válido")
    void shouldCreateValidEmail() {
        // Given
        String endereco = "joao@example.com";

        // When
        Email email = Email.of(endereco);

        // Then
        assertThat(email.getEndereco()).isEqualTo("joao@example.com");
        assertThat(email.toString()).isEqualTo("joao@example.com");
    }

    @Test
    @DisplayName("Deve normalizar email para minúsculas")
    void shouldNormalizeEmailToLowercase() {
        // When
        Email email = Email.of("JOAO@EXAMPLE.COM");

        // Then
        assertThat(email.getEndereco()).isEqualTo("joao@example.com");
    }

    @Test
    @DisplayName("Deve remover espaços do email")
    void shouldTrimEmail() {
        // When
        Email email = Email.of("  joao@example.com  ");

        // Then
        assertThat(email.getEndereco()).isEqualTo("joao@example.com");
    }

    @Test
    @DisplayName("Deve aceitar email com caracteres especiais válidos")
    void shouldAcceptEmailWithValidSpecialChars() {
        // When/Then
        assertThatNoException().isThrownBy(() -> Email.of("joao.silva+tag@example.com"));
        assertThatNoException().isThrownBy(() -> Email.of("joao_silva@example.com"));
        assertThatNoException().isThrownBy(() -> Email.of("joao-silva@example.com"));
    }

    @Test
    @DisplayName("Deve falhar ao criar email nulo")
    void shouldFailToCreateNullEmail() {
        assertThatThrownBy(() -> Email.of(null))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("obrigatório");
    }

    @Test
    @DisplayName("Deve falhar ao criar email vazio")
    void shouldFailToCreateEmptyEmail() {
        assertThatThrownBy(() -> Email.of(""))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("obrigatório");

        assertThatThrownBy(() -> Email.of("   "))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("obrigatório");
    }

    @Test
    @DisplayName("Deve falhar ao criar email sem @")
    void shouldFailToCreateEmailWithoutAt() {
        assertThatThrownBy(() -> Email.of("joaoexample.com"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("inválido");
    }

    @Test
    @DisplayName("Deve falhar ao criar email com múltiplos @")
    void shouldFailToCreateEmailWithMultipleAt() {
        assertThatThrownBy(() -> Email.of("joao@@example.com"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("inválido");
    }

    @Test
    @DisplayName("Deve falhar ao criar email sem domínio")
    void shouldFailToCreateEmailWithoutDomain() {
        assertThatThrownBy(() -> Email.of("joao@"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("inválido");
    }

    @Test
    @DisplayName("Deve falhar ao criar email sem parte local")
    void shouldFailToCreateEmailWithoutLocalPart() {
        assertThatThrownBy(() -> Email.of("@example.com"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("inválido");
    }

    @Test
    @DisplayName("Deve falhar ao criar email muito longo")
    void shouldFailToCreateTooLongEmail() {
        String longEmail = "a".repeat(250) + "@example.com";

        assertThatThrownBy(() -> Email.of(longEmail))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("muito longo");
    }

    @Test
    @DisplayName("Deve falhar ao criar email com parte local muito longa")
    void shouldFailToCreateEmailWithTooLongLocalPart() {
        String longLocalPart = "a".repeat(65) + "@example.com";

        assertThatThrownBy(() -> Email.of(longLocalPart))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Parte local");
    }

    @Test
    @DisplayName("Deve falhar ao criar email com domínio muito longo")
    void shouldFailToCreateEmailWithTooLongDomain() {
        // Criar um domínio com mais de 253 caracteres
        String longDomain = "joao@" + "a".repeat(250) + ".com";

        assertThatThrownBy(() -> Email.of(longDomain))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("muito longo");
    }

    @Test
    @DisplayName("Deve extrair domínio corretamente")
    void shouldExtractDomainCorrectly() {
        // Given
        Email email = Email.of("joao@example.com");

        // When/Then
        assertThat(email.getDominio()).isEqualTo("example.com");
    }

    @Test
    @DisplayName("Deve extrair parte local corretamente")
    void shouldExtractLocalPartCorrectly() {
        // Given
        Email email = Email.of("joao.silva@example.com");

        // When/Then
        assertThat(email.getParteLocal()).isEqualTo("joao.silva");
    }

    @Test
    @DisplayName("Deve identificar email corporativo")
    void shouldIdentifyCorporateEmail() {
        // Given
        Email email = Email.of("joao@empresa.com.br");

        // When/Then
        assertThat(email.isCorporativo()).isTrue();
    }

    @Test
    @DisplayName("Deve identificar email não corporativo - Gmail")
    void shouldIdentifyNonCorporateEmailGmail() {
        assertThat(Email.of("joao@gmail.com").isCorporativo()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar email não corporativo - Hotmail")
    void shouldIdentifyNonCorporateEmailHotmail() {
        assertThat(Email.of("joao@hotmail.com").isCorporativo()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar email não corporativo - Yahoo")
    void shouldIdentifyNonCorporateEmailYahoo() {
        assertThat(Email.of("joao@yahoo.com").isCorporativo()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar email não corporativo - Outlook")
    void shouldIdentifyNonCorporateEmailOutlook() {
        assertThat(Email.of("joao@outlook.com").isCorporativo()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar email não corporativo - UOL")
    void shouldIdentifyNonCorporateEmailUol() {
        assertThat(Email.of("joao@uol.com.br").isCorporativo()).isFalse();
    }

    @Test
    @DisplayName("Deve ter equals e hashCode corretos")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        Email email1 = Email.of("joao@example.com");
        Email email2 = Email.of("JOAO@EXAMPLE.COM");
        Email email3 = Email.of("maria@example.com");

        // Then
        assertThat(email1).isEqualTo(email2);
        assertThat(email1).isNotEqualTo(email3);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }
}
