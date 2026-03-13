package com.seguradora.hibrida.domain.segurado.model;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link Contato}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("Contato - Testes Unitários")
class ContatoTest {

    @Test
    @DisplayName("Deve criar contato de email válido")
    void shouldCreateValidEmailContact() {
        // When
        Contato contato = Contato.of(TipoContato.EMAIL, "joao@example.com", true);

        // Then
        assertThat(contato.getTipo()).isEqualTo(TipoContato.EMAIL);
        assertThat(contato.getValor()).isEqualTo("joao@example.com");
        assertThat(contato.isPrincipal()).isTrue();
        assertThat(contato.isAtivo()).isTrue();
        assertThat(contato.getDataCriacao()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar contato de celular válido")
    void shouldCreateValidCellPhoneContact() {
        // When
        Contato contato = Contato.of(TipoContato.CELULAR, "11987654321", false);

        // Then
        assertThat(contato.getTipo()).isEqualTo(TipoContato.CELULAR);
        assertThat(contato.getValor()).isEqualTo("11987654321");
        assertThat(contato.isPrincipal()).isFalse();
    }

    @Test
    @DisplayName("Deve criar contato de WhatsApp válido")
    void shouldCreateValidWhatsAppContact() {
        // When
        Contato contato = Contato.of(TipoContato.WHATSAPP, "11987654321", true);

        // Then
        assertThat(contato.getTipo()).isEqualTo(TipoContato.WHATSAPP);
        assertThat(contato.getValor()).isEqualTo("11987654321");
    }

    @Test
    @DisplayName("Deve criar contato de telefone fixo válido")
    void shouldCreateValidFixedPhoneContact() {
        // When
        Contato contato = Contato.of(TipoContato.TELEFONE, "1134567890", false);

        // Then
        assertThat(contato.getTipo()).isEqualTo(TipoContato.TELEFONE);
        assertThat(contato.getValor()).isEqualTo("1134567890");
    }

    @Test
    @DisplayName("Deve remover espaços do valor do contato")
    void shouldTrimContactValue() {
        // When
        Contato contato = Contato.of(TipoContato.EMAIL, "  joao@example.com  ", true);

        // Then
        assertThat(contato.getValor()).isEqualTo("joao@example.com");
    }

    @Test
    @DisplayName("Deve falhar ao criar contato sem tipo")
    void shouldFailToCreateContactWithoutType() {
        assertThatThrownBy(() -> Contato.of(null, "joao@example.com", true))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Tipo de contato é obrigatório");
    }

    @Test
    @DisplayName("Deve falhar ao criar contato sem valor")
    void shouldFailToCreateContactWithoutValue() {
        assertThatThrownBy(() -> Contato.of(TipoContato.EMAIL, null, true))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Valor do contato é obrigatório");

        assertThatThrownBy(() -> Contato.of(TipoContato.EMAIL, "", true))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Valor do contato é obrigatório");
    }

    @Test
    @DisplayName("Deve falhar ao criar contato de email com valor inválido")
    void shouldFailToCreateEmailContactWithInvalidValue() {
        assertThatThrownBy(() -> Contato.of(TipoContato.EMAIL, "email-invalido", true))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Email inválido");
    }

    @Test
    @DisplayName("Deve falhar ao criar contato de telefone com celular")
    void shouldFailToCreatePhoneContactWithCellNumber() {
        assertThatThrownBy(() -> Contato.of(TipoContato.TELEFONE, "11987654321", true))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("CELULAR");
    }

    @Test
    @DisplayName("Deve falhar ao criar contato de celular com telefone fixo")
    void shouldFailToCreateCellContactWithFixedNumber() {
        assertThatThrownBy(() -> Contato.of(TipoContato.CELULAR, "1134567890", true))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("TELEFONE");
    }

    @Test
    @DisplayName("Deve falhar ao criar contato de WhatsApp com telefone fixo")
    void shouldFailToCreateWhatsAppContactWithFixedNumber() {
        assertThatThrownBy(() -> Contato.of(TipoContato.WHATSAPP, "1134567890", true))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("TELEFONE");
    }

    @Test
    @DisplayName("Deve desativar contato")
    void shouldDeactivateContact() {
        // Given
        Contato contato = Contato.of(TipoContato.EMAIL, "joao@example.com", true);

        // When
        Contato contatoDesativado = contato.desativar();

        // Then
        assertThat(contatoDesativado.isAtivo()).isFalse();
        assertThat(contatoDesativado.getTipo()).isEqualTo(contato.getTipo());
        assertThat(contatoDesativado.getValor()).isEqualTo(contato.getValor());
    }

    @Test
    @DisplayName("Deve ativar contato")
    void shouldActivateContact() {
        // Given
        Contato contato = Contato.of(TipoContato.EMAIL, "joao@example.com", true);
        Contato contatoDesativado = contato.desativar();

        // When
        Contato contatoAtivado = contatoDesativado.ativar();

        // Then
        assertThat(contatoAtivado.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve marcar como principal")
    void shouldMarkAsPrincipal() {
        // Given
        Contato contato = Contato.of(TipoContato.EMAIL, "joao@example.com", false);

        // When
        Contato contatoPrincipal = contato.marcarComoPrincipal();

        // Then
        assertThat(contatoPrincipal.isPrincipal()).isTrue();
        assertThat(contatoPrincipal.getTipo()).isEqualTo(contato.getTipo());
    }

    @Test
    @DisplayName("Deve desmarcar como principal")
    void shouldUnmarkAsPrincipal() {
        // Given
        Contato contato = Contato.of(TipoContato.EMAIL, "joao@example.com", true);

        // When
        Contato contatoSecundario = contato.desmarcarComoPrincipal();

        // Then
        assertThat(contatoSecundario.isPrincipal()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar valor formatado para email")
    void shouldReturnFormattedValueForEmail() {
        // Given
        Contato contato = Contato.of(TipoContato.EMAIL, "JOAO@EXAMPLE.COM", true);

        // Then
        assertThat(contato.getValorFormatado()).isEqualTo("joao@example.com");
    }

    @Test
    @DisplayName("Deve retornar valor formatado para telefone")
    void shouldReturnFormattedValueForPhone() {
        // Given
        Contato contato = Contato.of(TipoContato.TELEFONE, "1134567890", true);

        // Then
        assertThat(contato.getValorFormatado()).isEqualTo("(11) 3456-7890");
    }

    @Test
    @DisplayName("Deve retornar valor formatado para celular")
    void shouldReturnFormattedValueForCell() {
        // Given
        Contato contato = Contato.of(TipoContato.CELULAR, "11987654321", true);

        // Then
        assertThat(contato.getValorFormatado()).isEqualTo("(11) 98765-4321");
    }

    @Test
    @DisplayName("Deve verificar se pode receber notificações")
    void shouldCheckIfCanReceiveNotifications() {
        // Given
        Contato email = Contato.of(TipoContato.EMAIL, "joao@example.com", true);
        Contato whatsapp = Contato.of(TipoContato.WHATSAPP, "11987654321", true);
        Contato telefone = Contato.of(TipoContato.TELEFONE, "1134567890", true);
        Contato emailInativo = email.desativar();

        // Then
        assertThat(email.podeReceberNotificacoes()).isTrue();
        assertThat(whatsapp.podeReceberNotificacoes()).isTrue();
        assertThat(telefone.podeReceberNotificacoes()).isFalse();
        assertThat(emailInativo.podeReceberNotificacoes()).isFalse();
    }

    @Test
    @DisplayName("Deve ter toString informativo")
    void shouldHaveInformativeToString() {
        // Given
        Contato email = Contato.of(TipoContato.EMAIL, "joao@example.com", true);
        Contato celular = Contato.of(TipoContato.CELULAR, "11987654321", false);
        Contato inativo = email.desativar();

        // Then
        assertThat(email.toString()).contains("Email", "joao@example.com", "Principal");
        assertThat(celular.toString()).contains("Celular", "(11) 98765-4321");
        assertThat(celular.toString()).doesNotContain("Principal");
        assertThat(inativo.toString()).contains("Inativo");
    }

    @Test
    @DisplayName("Deve ter equals e hashCode corretos")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        Contato contato1 = Contato.of(TipoContato.EMAIL, "joao@example.com", true);
        Contato contato2 = Contato.of(TipoContato.EMAIL, "joao@example.com", true);
        Contato contato3 = Contato.of(TipoContato.EMAIL, "maria@example.com", true);

        // Then
        assertThat(contato1).isEqualTo(contato2);
        assertThat(contato1).isNotEqualTo(contato3);
        assertThat(contato1.hashCode()).isEqualTo(contato2.hashCode());
    }
}
