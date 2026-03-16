package com.seguradora.hibrida.domain.apolice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para a classe NumeroApolice.
 */
@DisplayName("NumeroApolice - Testes Unitários")
class NumeroApoliceTest {

    @Test
    @DisplayName("Deve criar número de apólice válido")
    void deveCriarNumeroApoliceValido() {
        // Arrange
        String numero = "AP-2024-000001";

        // Act
        NumeroApolice numeroApolice = NumeroApolice.of(numero);

        // Assert
        assertThat(numeroApolice).isNotNull();
        assertThat(numeroApolice.getNumero()).isEqualTo(numero);
    }

    @Test
    @DisplayName("Deve criar número de apólice em lowercase e converter para uppercase")
    void deveCriarNumeroComCaseInsensitive() {
        // Arrange
        String numero = "ap-2024-000001";

        // Act
        NumeroApolice numeroApolice = NumeroApolice.of(numero);

        // Assert
        assertThat(numeroApolice.getNumero()).isEqualTo("AP-2024-000001");
    }

    @Test
    @DisplayName("Deve criar número de apólice removendo espaços extras")
    void deveCriarNumeroRemovendoEspacos() {
        // Arrange
        String numero = "  AP-2024-000001  ";

        // Act
        NumeroApolice numeroApolice = NumeroApolice.of(numero);

        // Assert
        assertThat(numeroApolice.getNumero()).isEqualTo("AP-2024-000001");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com número nulo")
    void deveLancarExcecaoAoCriarComNumeroNulo() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número da apólice não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com número vazio")
    void deveLancarExcecaoAoCriarComNumeroVazio() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número da apólice não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com número em branco")
    void deveLancarExcecaoAoCriarComNumeroEmBranco() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número da apólice não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com padrão inválido - sem prefixo")
    void deveLancarExcecaoComPadraoInvalidoSemPrefixo() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.of("2024-000001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve seguir o padrão AP-YYYY-NNNNNN");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com padrão inválido - ano com 2 dígitos")
    void deveLancarExcecaoComPadraoInvalidoAno2Digitos() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.of("AP-24-000001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve seguir o padrão AP-YYYY-NNNNNN");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com padrão inválido - sequencial com 5 dígitos")
    void deveLancarExcecaoComPadraoInvalidoSequencial5Digitos() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.of("AP-2024-00001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve seguir o padrão AP-YYYY-NNNNNN");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com padrão inválido - separador errado")
    void deveLancarExcecaoComPadraoInvalidoSeparador() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.of("AP_2024_000001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve seguir o padrão AP-YYYY-NNNNNN");
    }

    @Test
    @DisplayName("Deve gerar número de apólice com ano e sequencial")
    void deveGerarNumeroComAnoESequencial() {
        // Arrange
        int ano = 2024;
        long sequencial = 1;

        // Act
        NumeroApolice numeroApolice = NumeroApolice.gerar(ano, sequencial);

        // Assert
        assertThat(numeroApolice.getNumero()).isEqualTo("AP-2024-000001");
        assertThat(numeroApolice.getAno()).isEqualTo(ano);
        assertThat(numeroApolice.getSequencial()).isEqualTo(sequencial);
    }

    @Test
    @DisplayName("Deve gerar número de apólice com sequencial máximo")
    void deveGerarNumeroComSequencialMaximo() {
        // Arrange
        int ano = 2024;
        long sequencial = 999999;

        // Act
        NumeroApolice numeroApolice = NumeroApolice.gerar(ano, sequencial);

        // Assert
        assertThat(numeroApolice.getNumero()).isEqualTo("AP-2024-999999");
    }

    @Test
    @DisplayName("Deve gerar número de apólice para ano atual")
    void deveGerarNumeroParaAnoAtual() {
        // Arrange
        long sequencial = 123;
        int anoAtual = LocalDate.now().getYear();

        // Act
        NumeroApolice numeroApolice = NumeroApolice.gerar(sequencial);

        // Assert
        assertThat(numeroApolice.getAno()).isEqualTo(anoAtual);
        assertThat(numeroApolice.getSequencial()).isEqualTo(sequencial);
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar com ano menor que 2000")
    void deveLancarExcecaoAoGerarComAnoMenorQue2000() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.gerar(1999, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ano deve estar entre 2000 e 2100");
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar com ano maior que 2100")
    void deveLancarExcecaoAoGerarComAnoMaiorQue2100() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.gerar(2101, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ano deve estar entre 2000 e 2100");
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar com sequencial zero")
    void deveLancarExcecaoAoGerarComSequencialZero() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.gerar(2024, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sequencial deve estar entre 1 e 999999");
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar com sequencial negativo")
    void deveLancarExcecaoAoGerarComSequencialNegativo() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.gerar(2024, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sequencial deve estar entre 1 e 999999");
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar com sequencial maior que 999999")
    void deveLancarExcecaoAoGerarComSequencialMaiorQue999999() {
        // Act & Assert
        assertThatThrownBy(() -> NumeroApolice.gerar(2024, 1000000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sequencial deve estar entre 1 e 999999");
    }

    @Test
    @DisplayName("Deve retornar ano extraído do número")
    void deveRetornarAnoExtraido() {
        // Arrange
        NumeroApolice numeroApolice = NumeroApolice.of("AP-2024-000123");

        // Act
        int ano = numeroApolice.getAno();

        // Assert
        assertThat(ano).isEqualTo(2024);
    }

    @Test
    @DisplayName("Deve retornar sequencial extraído do número")
    void deveRetornarSequencialExtraido() {
        // Arrange
        NumeroApolice numeroApolice = NumeroApolice.of("AP-2024-000123");

        // Act
        long sequencial = numeroApolice.getSequencial();

        // Assert
        assertThat(sequencial).isEqualTo(123);
    }

    @Test
    @DisplayName("Deve retornar número formatado")
    void deveRetornarNumeroFormatado() {
        // Arrange
        NumeroApolice numeroApolice = NumeroApolice.of("AP-2024-000123");

        // Act
        String formatado = numeroApolice.getFormatado();

        // Assert
        assertThat(formatado).isEqualTo("AP-2024-000123");
    }

    @Test
    @DisplayName("Deve verificar se é do ano atual")
    void deveVerificarSeDoAnoAtual() {
        // Arrange
        int anoAtual = LocalDate.now().getYear();
        NumeroApolice numeroAtual = NumeroApolice.gerar(anoAtual, 1);
        NumeroApolice numeroAntigo = NumeroApolice.gerar(anoAtual - 1, 1);

        // Act & Assert
        assertThat(numeroAtual.isAnoAtual()).isTrue();
        assertThat(numeroAntigo.isAnoAtual()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é de ano específico")
    void deveVerificarSeDeAnoEspecifico() {
        // Arrange
        NumeroApolice numero2024 = NumeroApolice.of("AP-2024-000001");
        NumeroApolice numero2023 = NumeroApolice.of("AP-2023-000001");

        // Act & Assert
        assertThat(numero2024.isDoAno(2024)).isTrue();
        assertThat(numero2024.isDoAno(2023)).isFalse();
        assertThat(numero2023.isDoAno(2023)).isTrue();
    }

    @Test
    @DisplayName("Deve implementar equals corretamente")
    void deveImplementarEqualsCorretamente() {
        // Arrange
        NumeroApolice n1 = NumeroApolice.of("AP-2024-000001");
        NumeroApolice n2 = NumeroApolice.of("AP-2024-000001");
        NumeroApolice n3 = NumeroApolice.of("AP-2024-000002");

        // Act & Assert
        assertThat(n1).isEqualTo(n2);
        assertThat(n1).isNotEqualTo(n3);
        assertThat(n1.equals(n1)).isTrue();
        assertThat(n1.equals(null)).isFalse();
        assertThat(n1.equals("string")).isFalse();
    }

    @Test
    @DisplayName("Deve implementar hashCode corretamente")
    void deveImplementarHashCodeCorretamente() {
        // Arrange
        NumeroApolice n1 = NumeroApolice.of("AP-2024-000001");
        NumeroApolice n2 = NumeroApolice.of("AP-2024-000001");

        // Act & Assert
        assertThat(n1.hashCode()).isEqualTo(n2.hashCode());
    }

    @Test
    @DisplayName("Deve implementar toString corretamente")
    void deveImplementarToStringCorretamente() {
        // Arrange
        NumeroApolice numero = NumeroApolice.of("AP-2024-000001");

        // Act
        String toString = numero.toString();

        // Assert
        assertThat(toString).isEqualTo("AP-2024-000001");
    }

    @Test
    @DisplayName("Deve aceitar diferentes anos válidos")
    void deveAceitarDiferentesAnosValidos() {
        // Act & Assert
        assertThatNoException().isThrownBy(() -> NumeroApolice.gerar(2000, 1));
        assertThatNoException().isThrownBy(() -> NumeroApolice.gerar(2050, 1));
        assertThatNoException().isThrownBy(() -> NumeroApolice.gerar(2100, 1));
    }

    @Test
    @DisplayName("Deve aceitar diferentes sequenciais válidos")
    void deveAceitarDiferentesSequenciaisValidos() {
        // Act & Assert
        assertThatNoException().isThrownBy(() -> NumeroApolice.gerar(2024, 1));
        assertThatNoException().isThrownBy(() -> NumeroApolice.gerar(2024, 500000));
        assertThatNoException().isThrownBy(() -> NumeroApolice.gerar(2024, 999999));
    }
}
