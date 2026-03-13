package com.seguradora.hibrida.domain.segurado.model;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link Telefone}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("Telefone - Testes Unitários")
class TelefoneTest {

    @Test
    @DisplayName("Deve criar telefone fixo válido")
    void shouldCreateValidFixedPhone() {
        // Given
        String numero = "1134567890";

        // When
        Telefone telefone = Telefone.of(numero);

        // Then
        assertThat(telefone.getNumero()).isEqualTo("1134567890");
        assertThat(telefone.getDdd()).isEqualTo("11");
        assertThat(telefone.isCelular()).isFalse();
        assertThat(telefone.getFormatado()).isEqualTo("(11) 3456-7890");
    }

    @Test
    @DisplayName("Deve criar telefone celular válido")
    void shouldCreateValidCellPhone() {
        // Given
        String numero = "11987654321";

        // When
        Telefone telefone = Telefone.of(numero);

        // Then
        assertThat(telefone.getNumero()).isEqualTo("11987654321");
        assertThat(telefone.getDdd()).isEqualTo("11");
        assertThat(telefone.isCelular()).isTrue();
        assertThat(telefone.getFormatado()).isEqualTo("(11) 98765-4321");
    }

    @Test
    @DisplayName("Deve remover formatação do telefone")
    void shouldRemovePhoneFormatting() {
        // When
        Telefone telefone = Telefone.of("(11) 98765-4321");

        // Then
        assertThat(telefone.getNumero()).isEqualTo("11987654321");
    }

    @Test
    @DisplayName("Deve aceitar telefone com diversos formatos")
    void shouldAcceptVariousFormats() {
        assertThatNoException().isThrownBy(() -> Telefone.of("11987654321"));
        assertThatNoException().isThrownBy(() -> Telefone.of("(11) 98765-4321"));
        assertThatNoException().isThrownBy(() -> Telefone.of("11 98765-4321"));
        assertThatNoException().isThrownBy(() -> Telefone.of("(11)98765-4321"));
    }

    @Test
    @DisplayName("Deve falhar ao criar telefone nulo")
    void shouldFailToCreateNullPhone() {
        assertThatThrownBy(() -> Telefone.of(null))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("obrigatório");
    }

    @Test
    @DisplayName("Deve falhar ao criar telefone vazio")
    void shouldFailToCreateEmptyPhone() {
        assertThatThrownBy(() -> Telefone.of(""))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("obrigatório");
    }

    @Test
    @DisplayName("Deve falhar ao criar telefone com menos de 10 dígitos")
    void shouldFailToCreatePhoneWithLessThan10Digits() {
        assertThatThrownBy(() -> Telefone.of("119876543"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("10 ou 11 dígitos");
    }

    @Test
    @DisplayName("Deve falhar ao criar telefone com mais de 11 dígitos")
    void shouldFailToCreatePhoneWithMoreThan11Digits() {
        assertThatThrownBy(() -> Telefone.of("119876543210"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("10 ou 11 dígitos");
    }

    @Test
    @DisplayName("Deve falhar ao criar telefone fixo começando com 9")
    void shouldFailToCreateFixedPhoneStartingWith9() {
        assertThatThrownBy(() -> Telefone.of("1194567890"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("fixo não pode começar com 9");
    }

    @Test
    @DisplayName("Deve falhar ao criar celular não começando com 9")
    void shouldFailToCreateCellPhoneNotStartingWith9() {
        assertThatThrownBy(() -> Telefone.of("11887654321"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Celular deve começar com 9");
    }

    @Test
    @DisplayName("Deve falhar ao criar telefone com DDD inválido")
    void shouldFailToCreatePhoneWithInvalidDDD() {
        // DDD 00 não existe
        assertThatThrownBy(() -> Telefone.of("0034567890"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("DDD");

        // DDD 20 não existe (RJ tem 21, 22, 24)
        assertThatThrownBy(() -> Telefone.of("2034567890"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("DDD");

        // DDD 90 não existe (PA tem 91, 93, 94)
        assertThatThrownBy(() -> Telefone.of("9034567890"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("DDD");
    }

    @Test
    @DisplayName("Deve aceitar todos os DDDs válidos de São Paulo")
    void shouldAcceptAllValidDDDsFromSP() {
        assertThatNoException().isThrownBy(() -> Telefone.of("1134567890"));
        assertThatNoException().isThrownBy(() -> Telefone.of("1234567890"));
        assertThatNoException().isThrownBy(() -> Telefone.of("1334567890"));
        assertThatNoException().isThrownBy(() -> Telefone.of("1434567890"));
        assertThatNoException().isThrownBy(() -> Telefone.of("1534567890"));
        assertThatNoException().isThrownBy(() -> Telefone.of("1634567890"));
        assertThatNoException().isThrownBy(() -> Telefone.of("1734567890"));
        assertThatNoException().isThrownBy(() -> Telefone.of("1834567890"));
        assertThatNoException().isThrownBy(() -> Telefone.of("1934567890"));
    }

    @Test
    @DisplayName("Deve aceitar DDDs válidos de outras regiões")
    void shouldAcceptValidDDDsFromOtherRegions() {
        assertThatNoException().isThrownBy(() -> Telefone.of("2134567890")); // RJ
        assertThatNoException().isThrownBy(() -> Telefone.of("3134567890")); // MG
        assertThatNoException().isThrownBy(() -> Telefone.of("4134567890")); // PR
        assertThatNoException().isThrownBy(() -> Telefone.of("5134567890")); // RS
        assertThatNoException().isThrownBy(() -> Telefone.of("6134567890")); // DF
        assertThatNoException().isThrownBy(() -> Telefone.of("7134567890")); // BA
        assertThatNoException().isThrownBy(() -> Telefone.of("8134567890")); // PE
        assertThatNoException().isThrownBy(() -> Telefone.of("9134567890")); // PA
    }

    @Test
    @DisplayName("Deve extrair número sem DDD corretamente")
    void shouldExtractNumberWithoutDDD() {
        // Given
        Telefone telefone = Telefone.of("11987654321");

        // When/Then
        assertThat(telefone.getNumeroSemDdd()).isEqualTo("987654321");
    }

    @Test
    @DisplayName("Deve identificar WhatsApp corretamente")
    void shouldIdentifyWhatsAppCorrectly() {
        // Given
        Telefone celular = Telefone.of("11987654321");
        Telefone fixo = Telefone.of("1134567890");

        // Then
        assertThat(celular.isWhatsApp()).isTrue();
        assertThat(fixo.isWhatsApp()).isFalse();
    }

    @Test
    @DisplayName("Deve formatar telefone fixo corretamente")
    void shouldFormatFixedPhoneCorrectly() {
        // Given
        Telefone telefone = Telefone.of("1134567890");

        // Then
        assertThat(telefone.getFormatado()).isEqualTo("(11) 3456-7890");
        assertThat(telefone.toString()).isEqualTo("(11) 3456-7890");
    }

    @Test
    @DisplayName("Deve formatar telefone celular corretamente")
    void shouldFormatCellPhoneCorrectly() {
        // Given
        Telefone telefone = Telefone.of("11987654321");

        // Then
        assertThat(telefone.getFormatado()).isEqualTo("(11) 98765-4321");
        assertThat(telefone.toString()).isEqualTo("(11) 98765-4321");
    }

    @Test
    @DisplayName("Deve ter equals e hashCode corretos")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        Telefone tel1 = Telefone.of("11987654321");
        Telefone tel2 = Telefone.of("(11) 98765-4321");
        Telefone tel3 = Telefone.of("11987654322");

        // Then
        assertThat(tel1).isEqualTo(tel2);
        assertThat(tel1).isNotEqualTo(tel3);
        assertThat(tel1.hashCode()).isEqualTo(tel2.hashCode());
    }
}
