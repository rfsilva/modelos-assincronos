package com.seguradora.hibrida.domain.segurado.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link StatusSegurado}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("StatusSegurado - Testes Unitários")
class StatusSeguradoTest {

    @Test
    @DisplayName("Deve ter todos os status esperados")
    void shouldHaveAllExpectedStatus() {
        assertThat(StatusSegurado.values()).hasSize(4);
        assertThat(StatusSegurado.values()).containsExactlyInAnyOrder(
            StatusSegurado.ATIVO,
            StatusSegurado.SUSPENSO,
            StatusSegurado.INATIVO,
            StatusSegurado.BLOQUEADO
        );
    }

    @Test
    @DisplayName("Deve converter string para enum corretamente")
    void shouldConvertStringToEnumCorrectly() {
        assertThat(StatusSegurado.valueOf("ATIVO")).isEqualTo(StatusSegurado.ATIVO);
        assertThat(StatusSegurado.valueOf("SUSPENSO")).isEqualTo(StatusSegurado.SUSPENSO);
        assertThat(StatusSegurado.valueOf("INATIVO")).isEqualTo(StatusSegurado.INATIVO);
        assertThat(StatusSegurado.valueOf("BLOQUEADO")).isEqualTo(StatusSegurado.BLOQUEADO);
    }

    @Test
    @DisplayName("Deve lançar exceção para valor inválido")
    void shouldThrowExceptionForInvalidValue() {
        assertThatThrownBy(() -> StatusSegurado.valueOf("INVALIDO"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve ter toString com nome do enum")
    void shouldHaveToStringWithEnumName() {
        assertThat(StatusSegurado.ATIVO.toString()).isEqualTo("ATIVO");
        assertThat(StatusSegurado.SUSPENSO.toString()).isEqualTo("SUSPENSO");
        assertThat(StatusSegurado.INATIVO.toString()).isEqualTo("INATIVO");
        assertThat(StatusSegurado.BLOQUEADO.toString()).isEqualTo("BLOQUEADO");
    }

    @Test
    @DisplayName("Deve retornar descrição correta")
    void shouldReturnCorrectDescription() {
        assertThat(StatusSegurado.ATIVO.getDescricao()).isEqualTo("Ativo");
        assertThat(StatusSegurado.SUSPENSO.getDescricao()).isEqualTo("Suspenso");
        assertThat(StatusSegurado.INATIVO.getDescricao()).isEqualTo("Inativo");
        assertThat(StatusSegurado.BLOQUEADO.getDescricao()).isEqualTo("Bloqueado");
    }

    @Test
    @DisplayName("Deve identificar status ativo corretamente")
    void shouldIdentifyActiveStatusCorrectly() {
        assertThat(StatusSegurado.ATIVO.isAtivo()).isTrue();
        assertThat(StatusSegurado.SUSPENSO.isAtivo()).isFalse();
        assertThat(StatusSegurado.INATIVO.isAtivo()).isFalse();
        assertThat(StatusSegurado.BLOQUEADO.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar se pode operar apólices")
    void shouldIdentifyIfCanOperatePolicies() {
        assertThat(StatusSegurado.ATIVO.podeOperarApolices()).isTrue();
        assertThat(StatusSegurado.SUSPENSO.podeOperarApolices()).isTrue();
        assertThat(StatusSegurado.INATIVO.podeOperarApolices()).isFalse();
        assertThat(StatusSegurado.BLOQUEADO.podeOperarApolices()).isFalse();
    }
}
