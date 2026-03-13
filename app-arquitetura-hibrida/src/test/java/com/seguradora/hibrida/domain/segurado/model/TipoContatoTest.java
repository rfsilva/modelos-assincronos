package com.seguradora.hibrida.domain.segurado.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link TipoContato}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("TipoContato - Testes Unitários")
class TipoContatoTest {

    @Test
    @DisplayName("Deve ter todos os tipos esperados")
    void shouldHaveAllExpectedTypes() {
        assertThat(TipoContato.values()).hasSize(4);
        assertThat(TipoContato.values()).containsExactlyInAnyOrder(
            TipoContato.EMAIL,
            TipoContato.TELEFONE,
            TipoContato.CELULAR,
            TipoContato.WHATSAPP
        );
    }

    @Test
    @DisplayName("Deve retornar nome correto para cada tipo")
    void shouldReturnCorrectNameForEachType() {
        assertThat(TipoContato.EMAIL.getNome()).isEqualTo("Email");
        assertThat(TipoContato.TELEFONE.getNome()).isEqualTo("Telefone");
        assertThat(TipoContato.CELULAR.getNome()).isEqualTo("Celular");
        assertThat(TipoContato.WHATSAPP.getNome()).isEqualTo("WhatsApp");
    }

    @Test
    @DisplayName("Deve retornar descrição correta para cada tipo")
    void shouldReturnCorrectDescriptionForEachType() {
        assertThat(TipoContato.EMAIL.getDescricao()).isNotEmpty();
        assertThat(TipoContato.TELEFONE.getDescricao()).isNotEmpty();
        assertThat(TipoContato.CELULAR.getDescricao()).isNotEmpty();
        assertThat(TipoContato.WHATSAPP.getDescricao()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve identificar tipos digitais corretamente")
    void shouldIdentifyDigitalTypesCorrectly() {
        assertThat(TipoContato.EMAIL.isDigital()).isTrue();
        assertThat(TipoContato.WHATSAPP.isDigital()).isTrue();
        assertThat(TipoContato.TELEFONE.isDigital()).isFalse();
        assertThat(TipoContato.CELULAR.isDigital()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar tipos telefônicos corretamente")
    void shouldIdentifyPhoneTypesCorrectly() {
        assertThat(TipoContato.TELEFONE.isTelefonico()).isTrue();
        assertThat(TipoContato.CELULAR.isTelefonico()).isTrue();
        assertThat(TipoContato.WHATSAPP.isTelefonico()).isTrue();
        assertThat(TipoContato.EMAIL.isTelefonico()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar tipos que suportam mensagem de texto")
    void shouldIdentifyTextMessageSupportCorrectly() {
        assertThat(TipoContato.EMAIL.suportaMensagemTexto()).isTrue();
        assertThat(TipoContato.WHATSAPP.suportaMensagemTexto()).isTrue();
        assertThat(TipoContato.TELEFONE.suportaMensagemTexto()).isFalse();
        assertThat(TipoContato.CELULAR.suportaMensagemTexto()).isFalse();
    }

    @Test
    @DisplayName("Deve ter toString igual ao nome")
    void shouldHaveToStringEqualToName() {
        assertThat(TipoContato.EMAIL.toString()).isEqualTo("Email");
        assertThat(TipoContato.TELEFONE.toString()).isEqualTo("Telefone");
        assertThat(TipoContato.CELULAR.toString()).isEqualTo("Celular");
        assertThat(TipoContato.WHATSAPP.toString()).isEqualTo("WhatsApp");
    }

    @Test
    @DisplayName("Deve converter string para enum corretamente")
    void shouldConvertStringToEnumCorrectly() {
        assertThat(TipoContato.valueOf("EMAIL")).isEqualTo(TipoContato.EMAIL);
        assertThat(TipoContato.valueOf("TELEFONE")).isEqualTo(TipoContato.TELEFONE);
        assertThat(TipoContato.valueOf("CELULAR")).isEqualTo(TipoContato.CELULAR);
        assertThat(TipoContato.valueOf("WHATSAPP")).isEqualTo(TipoContato.WHATSAPP);
    }

    @Test
    @DisplayName("Deve lançar exceção para valor inválido")
    void shouldThrowExceptionForInvalidValue() {
        assertThatThrownBy(() -> TipoContato.valueOf("INVALIDO"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
