package com.seguradora.hibrida.domain.apolice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para a classe Valor.
 */
@DisplayName("Valor - Testes Unitários")
class ValorTest {

    @Test
    @DisplayName("Deve criar valor em reais com BigDecimal")
    void deveCriarValorEmReaisComBigDecimal() {
        // Arrange
        BigDecimal quantia = new BigDecimal("100.50");

        // Act
        Valor valor = Valor.reais(quantia);

        // Assert
        assertThat(valor).isNotNull();
        assertThat(valor.getQuantia()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(valor.getMoeda().getCurrencyCode()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("Deve criar valor em reais com double")
    void deveCriarValorEmReaisComDouble() {
        // Arrange
        double quantia = 100.50;

        // Act
        Valor valor = Valor.reais(quantia);

        // Assert
        assertThat(valor).isNotNull();
        assertThat(valor.getQuantia()).isEqualByComparingTo(new BigDecimal("100.50"));
    }

    @Test
    @DisplayName("Deve criar valor em reais com string")
    void deveCriarValorEmReaisComString() {
        // Arrange
        String quantia = "100.50";

        // Act
        Valor valor = Valor.reais(quantia);

        // Assert
        assertThat(valor).isNotNull();
        assertThat(valor.getQuantia()).isEqualByComparingTo(new BigDecimal("100.50"));
    }

    @Test
    @DisplayName("Deve criar valor zero")
    void deveCriarValorZero() {
        // Act
        Valor valor = Valor.zero();

        // Assert
        assertThat(valor).isNotNull();
        assertThat(valor.getQuantia()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(valor.isZero()).isTrue();
        assertThat(valor.isPositivo()).isFalse();
    }

    @Test
    @DisplayName("Deve criar valor com moeda específica")
    void deveCriarValorComMoedaEspecifica() {
        // Arrange
        BigDecimal quantia = new BigDecimal("100.00");
        Currency usd = Currency.getInstance("USD");

        // Act
        Valor valor = Valor.of(quantia, usd);

        // Assert
        assertThat(valor.getMoeda()).isEqualTo(usd);
    }

    @Test
    @DisplayName("Deve aplicar escala de 2 casas decimais")
    void deveAplicarEscalaDuasCasasDecimais() {
        // Arrange
        BigDecimal quantia = new BigDecimal("100.123456");

        // Act
        Valor valor = Valor.reais(quantia);

        // Assert
        assertThat(valor.getQuantia()).isEqualByComparingTo(new BigDecimal("100.12"));
    }

    @Test
    @DisplayName("Deve arredondar para cima quando terceiro decimal >= 5")
    void deveArredondarParaCimaQuandoTerceiroDecimalMaiorIgual5() {
        // Arrange
        BigDecimal quantia = new BigDecimal("100.125");

        // Act
        Valor valor = Valor.reais(quantia);

        // Assert
        assertThat(valor.getQuantia()).isEqualByComparingTo(new BigDecimal("100.13"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar valor com quantia nula")
    void deveLancarExcecaoAoCriarComQuantiaNula() {
        // Act & Assert
        assertThatThrownBy(() -> Valor.reais((BigDecimal) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantia não pode ser nula");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar valor com string nula")
    void deveLancarExcecaoAoCriarComStringNula() {
        // Act & Assert
        assertThatThrownBy(() -> Valor.reais((String) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantia não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar valor com string vazia")
    void deveLancarExcecaoAoCriarComStringVazia() {
        // Act & Assert
        assertThatThrownBy(() -> Valor.reais(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantia não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar valor com string inválida")
    void deveLancarExcecaoAoCriarComStringInvalida() {
        // Act & Assert
        assertThatThrownBy(() -> Valor.reais("abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantia inválida");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar valor negativo")
    void deveLancarExcecaoAoCriarValorNegativo() {
        // Act & Assert
        assertThatThrownBy(() -> Valor.reais(-100.00))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantia não pode ser negativa");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar valor acima do limite máximo")
    void deveLancarExcecaoAoCriarValorAcimaLimiteMaximo() {
        // Act & Assert
        assertThatThrownBy(() -> Valor.reais(10000001.00))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantia excede o limite máximo de R$ 10.000.000,00");
    }

    @Test
    @DisplayName("Deve aceitar valor no limite máximo")
    void deveAceitarValorNoLimiteMaximo() {
        // Act & Assert
        assertThatNoException().isThrownBy(() -> Valor.reais(10000000.00));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com moeda nula")
    void deveLancarExcecaoAoCriarComMoedaNula() {
        // Act & Assert
        assertThatThrownBy(() -> Valor.of(BigDecimal.TEN, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Moeda não pode ser nula");
    }

    @Test
    @DisplayName("Deve verificar se valor é zero")
    void deveVerificarSeValorEhZero() {
        // Arrange
        Valor zero = Valor.zero();
        Valor naoZero = Valor.reais(100.00);

        // Act & Assert
        assertThat(zero.isZero()).isTrue();
        assertThat(naoZero.isZero()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se valor é positivo")
    void deveVerificarSeValorEhPositivo() {
        // Arrange
        Valor zero = Valor.zero();
        Valor positivo = Valor.reais(100.00);

        // Act & Assert
        assertThat(zero.isPositivo()).isFalse();
        assertThat(positivo.isPositivo()).isTrue();
    }

    @Test
    @DisplayName("Deve somar valores corretamente")
    void deveSomarValoresCorretamente() {
        // Arrange
        Valor v1 = Valor.reais(100.00);
        Valor v2 = Valor.reais(50.00);

        // Act
        Valor resultado = v1.somar(v2);

        // Assert
        assertThat(resultado.getQuantia()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao somar valores de moedas diferentes")
    void deveLancarExcecaoAoSomarMoedasDiferentes() {
        // Arrange
        Valor brl = Valor.reais(100.00);
        Valor usd = Valor.of(new BigDecimal("100.00"), Currency.getInstance("USD"));

        // Act & Assert
        assertThatThrownBy(() -> brl.somar(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Moedas incompatíveis");
    }

    @Test
    @DisplayName("Deve subtrair valores corretamente")
    void deveSubtrairValoresCorretamente() {
        // Arrange
        Valor v1 = Valor.reais(100.00);
        Valor v2 = Valor.reais(30.00);

        // Act
        Valor resultado = v1.subtrair(v2);

        // Assert
        assertThat(resultado.getQuantia()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao subtrair resultando em valor negativo")
    void deveLancarExcecaoAoSubtrairResultandoNegativo() {
        // Arrange
        Valor v1 = Valor.reais(100.00);
        Valor v2 = Valor.reais(150.00);

        // Act & Assert
        assertThatThrownBy(() -> v1.subtrair(v2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Resultado da subtração não pode ser negativo");
    }

    @Test
    @DisplayName("Deve multiplicar valor por BigDecimal")
    void deveMultiplicarValorPorBigDecimal() {
        // Arrange
        Valor valor = Valor.reais(100.00);
        BigDecimal fator = new BigDecimal("1.5");

        // Act
        Valor resultado = valor.multiplicar(fator);

        // Assert
        assertThat(resultado.getQuantia()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Deve multiplicar valor por double")
    void deveMultiplicarValorPorDouble() {
        // Arrange
        Valor valor = Valor.reais(100.00);

        // Act
        Valor resultado = valor.multiplicar(2.0);

        // Assert
        assertThat(resultado.getQuantia()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao multiplicar por fator nulo")
    void deveLancarExcecaoAoMultiplicarPorFatorNulo() {
        // Arrange
        Valor valor = Valor.reais(100.00);

        // Act & Assert
        assertThatThrownBy(() -> valor.multiplicar((BigDecimal) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fator não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar exceção ao multiplicar por fator negativo")
    void deveLancarExcecaoAoMultiplicarPorFatorNegativo() {
        // Arrange
        Valor valor = Valor.reais(100.00);

        // Act & Assert
        assertThatThrownBy(() -> valor.multiplicar(-2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fator não pode ser negativo");
    }

    @Test
    @DisplayName("Deve dividir valor por BigDecimal")
    void deveDividirValorPorBigDecimal() {
        // Arrange
        Valor valor = Valor.reais(100.00);
        BigDecimal divisor = new BigDecimal("4");

        // Act
        Valor resultado = valor.dividir(divisor);

        // Assert
        assertThat(resultado.getQuantia()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("Deve dividir valor por int")
    void deveDividirValorPorInt() {
        // Arrange
        Valor valor = Valor.reais(100.00);

        // Act
        Valor resultado = valor.dividir(4);

        // Assert
        assertThat(resultado.getQuantia()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("Deve arredondar ao dividir")
    void deveArredondarAoDividir() {
        // Arrange
        Valor valor = Valor.reais(100.00);

        // Act
        Valor resultado = valor.dividir(3);

        // Assert
        assertThat(resultado.getQuantia()).isEqualByComparingTo(new BigDecimal("33.33"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao dividir por zero")
    void deveLancarExcecaoAoDividirPorZero() {
        // Arrange
        Valor valor = Valor.reais(100.00);

        // Act & Assert
        assertThatThrownBy(() -> valor.dividir(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Divisor não pode ser zero");
    }

    @Test
    @DisplayName("Deve lançar exceção ao dividir por nulo")
    void deveLancarExcecaoAoDividirPorNulo() {
        // Arrange
        Valor valor = Valor.reais(100.00);

        // Act & Assert
        assertThatThrownBy(() -> valor.dividir((BigDecimal) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Divisor não pode ser nulo");
    }

    @Test
    @DisplayName("Deve calcular porcentagem com BigDecimal")
    void deveCalcularPorcentagemComBigDecimal() {
        // Arrange
        Valor valor = Valor.reais(1000.00);
        BigDecimal percentual = new BigDecimal("10");

        // Act
        Valor resultado = valor.porcentagem(percentual);

        // Assert
        assertThat(resultado.getQuantia()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Deve calcular porcentagem com double")
    void deveCalcularPorcentagemComDouble() {
        // Arrange
        Valor valor = Valor.reais(1000.00);

        // Act
        Valor resultado = valor.porcentagem(15.5);

        // Assert
        assertThat(resultado.getQuantia()).isEqualByComparingTo(new BigDecimal("155.00"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao calcular porcentagem nula")
    void deveLancarExcecaoAoCalcularPorcentagemNula() {
        // Arrange
        Valor valor = Valor.reais(100.00);

        // Act & Assert
        assertThatThrownBy(() -> valor.porcentagem((BigDecimal) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Percentual não pode ser nulo");
    }

    @Test
    @DisplayName("Deve verificar se é maior que outro valor")
    void deveVerificarSeMaiorQue() {
        // Arrange
        Valor maior = Valor.reais(100.00);
        Valor menor = Valor.reais(50.00);

        // Act & Assert
        assertThat(maior.ehMaiorQue(menor)).isTrue();
        assertThat(menor.ehMaiorQue(maior)).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é menor que outro valor")
    void deveVerificarSeMenorQue() {
        // Arrange
        Valor maior = Valor.reais(100.00);
        Valor menor = Valor.reais(50.00);

        // Act & Assert
        assertThat(menor.ehMenorQue(maior)).isTrue();
        assertThat(maior.ehMenorQue(menor)).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é igual a outro valor")
    void deveVerificarSeIgualA() {
        // Arrange
        Valor v1 = Valor.reais(100.00);
        Valor v2 = Valor.reais(100.00);
        Valor v3 = Valor.reais(50.00);

        // Act & Assert
        assertThat(v1.ehIgualA(v2)).isTrue();
        assertThat(v1.ehIgualA(v3)).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é maior ou igual a outro valor")
    void deveVerificarSeMaiorOuIgualA() {
        // Arrange
        Valor v1 = Valor.reais(100.00);
        Valor v2 = Valor.reais(100.00);
        Valor v3 = Valor.reais(50.00);

        // Act & Assert
        assertThat(v1.ehMaiorOuIgualA(v2)).isTrue();
        assertThat(v1.ehMaiorOuIgualA(v3)).isTrue();
        assertThat(v3.ehMaiorOuIgualA(v1)).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é menor ou igual a outro valor")
    void deveVerificarSeMenorOuIgualA() {
        // Arrange
        Valor v1 = Valor.reais(100.00);
        Valor v2 = Valor.reais(100.00);
        Valor v3 = Valor.reais(150.00);

        // Act & Assert
        assertThat(v1.ehMenorOuIgualA(v2)).isTrue();
        assertThat(v1.ehMenorOuIgualA(v3)).isTrue();
        assertThat(v3.ehMenorOuIgualA(v1)).isFalse();
    }

    @Test
    @DisplayName("Deve lançar exceção ao comparar com valor nulo")
    void deveLancarExcecaoAoCompararComValorNulo() {
        // Arrange
        Valor valor = Valor.reais(100.00);

        // Act & Assert
        assertThatThrownBy(() -> valor.ehMaiorQue(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor não pode ser nulo");
    }

    @Test
    @DisplayName("Deve retornar valor formatado em português brasileiro")
    void deveRetornarValorFormatado() {
        // Arrange
        Valor valor = Valor.reais(1234.56);

        // Act
        String formatado = valor.getFormatado();

        // Assert
        assertThat(formatado).contains("1.234,56");
        assertThat(formatado).contains("R$");
    }

    @Test
    @DisplayName("Deve retornar valor formatado sem símbolo da moeda")
    void deveRetornarValorFormatadoSemSimbolo() {
        // Arrange
        Valor valor = Valor.reais(1234.56);

        // Act
        String formatado = valor.getFormatadoSemSimbolo();

        // Assert
        assertThat(formatado).contains("1.234,56");
        assertThat(formatado).doesNotContain("R$");
    }

    @Test
    @DisplayName("Deve implementar Comparable corretamente")
    void deveImplementarComparableCorretamente() {
        // Arrange
        Valor v1 = Valor.reais(50.00);
        Valor v2 = Valor.reais(100.00);
        Valor v3 = Valor.reais(100.00);

        // Act & Assert
        assertThat(v1.compareTo(v2)).isLessThan(0);
        assertThat(v2.compareTo(v1)).isGreaterThan(0);
        assertThat(v2.compareTo(v3)).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve implementar equals corretamente")
    void deveImplementarEqualsCorretamente() {
        // Arrange
        Valor v1 = Valor.reais(100.00);
        Valor v2 = Valor.reais(100.00);
        Valor v3 = Valor.reais(50.00);
        Valor usd = Valor.of(new BigDecimal("100.00"), Currency.getInstance("USD"));

        // Act & Assert
        assertThat(v1).isEqualTo(v2);
        assertThat(v1).isNotEqualTo(v3);
        assertThat(v1).isNotEqualTo(usd);
        assertThat(v1.equals(v1)).isTrue();
        assertThat(v1.equals(null)).isFalse();
        assertThat(v1.equals("string")).isFalse();
    }

    @Test
    @DisplayName("Deve implementar hashCode corretamente")
    void deveImplementarHashCodeCorretamente() {
        // Arrange
        Valor v1 = Valor.reais(100.00);
        Valor v2 = Valor.reais(100.00);

        // Act & Assert
        assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
    }

    @Test
    @DisplayName("Deve implementar toString corretamente")
    void deveImplementarToStringCorretamente() {
        // Arrange
        Valor valor = Valor.reais(100.00);

        // Act
        String toString = valor.toString();

        // Assert
        assertThat(toString).isEqualTo(valor.getFormatado());
    }

    @Test
    @DisplayName("Deve aceitar valor zero como válido")
    void deveAceitarValorZeroComoValido() {
        // Act & Assert
        assertThatNoException().isThrownBy(() -> Valor.reais(0.00));
    }

    @Test
    @DisplayName("Deve tratar operações com valor zero corretamente")
    void deveTratarOperacoesComValorZeroCorretamente() {
        // Arrange
        Valor zero = Valor.zero();
        Valor cem = Valor.reais(100.00);

        // Act & Assert
        assertThat(zero.somar(cem)).isEqualTo(cem);
        assertThat(cem.subtrair(cem)).isEqualTo(zero);
        assertThat(zero.multiplicar(10)).isEqualTo(zero);
    }

    @Test
    @DisplayName("Deve manter precisão em operações consecutivas")
    void deveManterPrecisaoEmOperacoesConsecutivas() {
        // Arrange
        Valor valor = Valor.reais(100.00);

        // Act
        Valor resultado = valor
                .multiplicar(1.1)
                .dividir(2)
                .somar(Valor.reais(10.00));

        // Assert
        assertThat(resultado.getQuantia().scale()).isEqualTo(2);
    }
}
