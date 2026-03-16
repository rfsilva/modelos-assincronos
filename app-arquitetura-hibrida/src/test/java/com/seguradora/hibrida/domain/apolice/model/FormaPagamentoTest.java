package com.seguradora.hibrida.domain.apolice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para o enum FormaPagamento.
 */
@DisplayName("FormaPagamento - Testes Unitários")
class FormaPagamentoTest {

    @Test
    @DisplayName("Deve ter todas as formas de pagamento disponíveis")
    void deveTerTodasFormasPagamentoDisponiveis() {
        // Act & Assert
        assertThat(FormaPagamento.values()).containsExactly(
                FormaPagamento.MENSAL,
                FormaPagamento.TRIMESTRAL,
                FormaPagamento.SEMESTRAL,
                FormaPagamento.ANUAL
        );
    }

    @Test
    @DisplayName("Deve retornar descrição correta para cada forma de pagamento")
    void deveRetornarDescricaoCorreta() {
        // Act & Assert
        assertThat(FormaPagamento.MENSAL.getDescricao()).isEqualTo("Mensal");
        assertThat(FormaPagamento.TRIMESTRAL.getDescricao()).isEqualTo("Trimestral");
        assertThat(FormaPagamento.SEMESTRAL.getDescricao()).isEqualTo("Semestral");
        assertThat(FormaPagamento.ANUAL.getDescricao()).isEqualTo("Anual");
    }

    @Test
    @DisplayName("Deve retornar número de parcelas correto para cada forma")
    void deveRetornarNumeroParcelasCorreto() {
        // Act & Assert
        assertThat(FormaPagamento.MENSAL.getNumeroParcelas()).isEqualTo(12);
        assertThat(FormaPagamento.TRIMESTRAL.getNumeroParcelas()).isEqualTo(4);
        assertThat(FormaPagamento.SEMESTRAL.getNumeroParcelas()).isEqualTo(2);
        assertThat(FormaPagamento.ANUAL.getNumeroParcelas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve retornar fator de ajuste correto para cada forma")
    void deveRetornarFatorAjusteCorreto() {
        // Act & Assert
        assertThat(FormaPagamento.MENSAL.getFatorAjuste()).isEqualByComparingTo(new BigDecimal("1.05"));
        assertThat(FormaPagamento.TRIMESTRAL.getFatorAjuste()).isEqualByComparingTo(new BigDecimal("1.02"));
        assertThat(FormaPagamento.SEMESTRAL.getFatorAjuste()).isEqualByComparingTo(new BigDecimal("1.01"));
        assertThat(FormaPagamento.ANUAL.getFatorAjuste()).isEqualByComparingTo(new BigDecimal("0.95"));
    }

    @Test
    @DisplayName("Deve identificar pagamento à vista corretamente")
    void deveIdentificarPagamentoAVista() {
        // Act & Assert
        assertThat(FormaPagamento.MENSAL.isAVista()).isFalse();
        assertThat(FormaPagamento.TRIMESTRAL.isAVista()).isFalse();
        assertThat(FormaPagamento.SEMESTRAL.isAVista()).isFalse();
        assertThat(FormaPagamento.ANUAL.isAVista()).isTrue();
    }

    @Test
    @DisplayName("Deve identificar pagamento parcelado corretamente")
    void deveIdentificarPagamentoParcelado() {
        // Act & Assert
        assertThat(FormaPagamento.MENSAL.isParcelado()).isTrue();
        assertThat(FormaPagamento.TRIMESTRAL.isParcelado()).isTrue();
        assertThat(FormaPagamento.SEMESTRAL.isParcelado()).isTrue();
        assertThat(FormaPagamento.ANUAL.isParcelado()).isFalse();
    }

    @Test
    @DisplayName("Deve calcular valor da parcela mensal corretamente")
    void deveCalcularValorParcelaMensal() {
        // Arrange
        Valor valorTotal = Valor.reais(1200.00);

        // Act
        Valor valorParcela = FormaPagamento.MENSAL.calcularValorParcela(valorTotal);

        // Assert
        assertThat(valorParcela).isEqualTo(Valor.reais(100.00));
    }

    @Test
    @DisplayName("Deve calcular valor da parcela trimestral corretamente")
    void deveCalcularValorParcelaTrimestral() {
        // Arrange
        Valor valorTotal = Valor.reais(1200.00);

        // Act
        Valor valorParcela = FormaPagamento.TRIMESTRAL.calcularValorParcela(valorTotal);

        // Assert
        assertThat(valorParcela).isEqualTo(Valor.reais(300.00));
    }

    @Test
    @DisplayName("Deve calcular valor da parcela semestral corretamente")
    void deveCalcularValorParcelaSemestral() {
        // Arrange
        Valor valorTotal = Valor.reais(1200.00);

        // Act
        Valor valorParcela = FormaPagamento.SEMESTRAL.calcularValorParcela(valorTotal);

        // Assert
        assertThat(valorParcela).isEqualTo(Valor.reais(600.00));
    }

    @Test
    @DisplayName("Deve calcular valor da parcela anual corretamente")
    void deveCalcularValorParcelaAnual() {
        // Arrange
        Valor valorTotal = Valor.reais(1200.00);

        // Act
        Valor valorParcela = FormaPagamento.ANUAL.calcularValorParcela(valorTotal);

        // Assert
        assertThat(valorParcela).isEqualTo(valorTotal);
    }

    @Test
    @DisplayName("Deve lançar exceção ao calcular parcela com valor nulo")
    void deveLancarExcecaoAoCalcularParcelaComValorNulo() {
        // Act & Assert
        assertThatThrownBy(() -> FormaPagamento.MENSAL.calcularValorParcela(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor total não pode ser nulo");
    }

    @Test
    @DisplayName("Deve calcular valor total com ajuste mensal corretamente")
    void deveCalcularValorTotalComAjusteMensal() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00);

        // Act
        Valor valorComAjuste = FormaPagamento.MENSAL.calcularValorTotalComAjuste(valorBase);

        // Assert
        assertThat(valorComAjuste).isEqualTo(Valor.reais(1050.00)); // 1000 * 1.05
    }

    @Test
    @DisplayName("Deve calcular valor total com ajuste trimestral corretamente")
    void deveCalcularValorTotalComAjusteTrimestral() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00);

        // Act
        Valor valorComAjuste = FormaPagamento.TRIMESTRAL.calcularValorTotalComAjuste(valorBase);

        // Assert
        assertThat(valorComAjuste).isEqualTo(Valor.reais(1020.00)); // 1000 * 1.02
    }

    @Test
    @DisplayName("Deve calcular valor total com ajuste semestral corretamente")
    void deveCalcularValorTotalComAjusteSemestral() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00);

        // Act
        Valor valorComAjuste = FormaPagamento.SEMESTRAL.calcularValorTotalComAjuste(valorBase);

        // Assert
        assertThat(valorComAjuste).isEqualTo(Valor.reais(1010.00)); // 1000 * 1.01
    }

    @Test
    @DisplayName("Deve calcular valor total com desconto anual corretamente")
    void deveCalcularValorTotalComDescontoAnual() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00);

        // Act
        Valor valorComDesconto = FormaPagamento.ANUAL.calcularValorTotalComAjuste(valorBase);

        // Assert
        assertThat(valorComDesconto).isEqualTo(Valor.reais(950.00)); // 1000 * 0.95
    }

    @Test
    @DisplayName("Deve lançar exceção ao calcular ajuste com valor nulo")
    void deveLancarExcecaoAoCalcularAjusteComValorNulo() {
        // Act & Assert
        assertThatThrownBy(() -> FormaPagamento.MENSAL.calcularValorTotalComAjuste(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor base não pode ser nulo");
    }

    @Test
    @DisplayName("Deve retornar toString com descrição e número de parcelas")
    void deveRetornarToStringComDescricaoEParcelas() {
        // Act & Assert
        assertThat(FormaPagamento.MENSAL.toString()).isEqualTo("Mensal (12x)");
        assertThat(FormaPagamento.TRIMESTRAL.toString()).isEqualTo("Trimestral (4x)");
        assertThat(FormaPagamento.SEMESTRAL.toString()).isEqualTo("Semestral (2x)");
        assertThat(FormaPagamento.ANUAL.toString()).isEqualTo("Anual (1x)");
    }

    @Test
    @DisplayName("Deve permitir conversão de string para enum")
    void devePermitirConversaoDeStringParaEnum() {
        // Act & Assert
        assertThat(FormaPagamento.valueOf("MENSAL")).isEqualTo(FormaPagamento.MENSAL);
        assertThat(FormaPagamento.valueOf("TRIMESTRAL")).isEqualTo(FormaPagamento.TRIMESTRAL);
        assertThat(FormaPagamento.valueOf("SEMESTRAL")).isEqualTo(FormaPagamento.SEMESTRAL);
        assertThat(FormaPagamento.valueOf("ANUAL")).isEqualTo(FormaPagamento.ANUAL);
    }

    @Test
    @DisplayName("Deve lançar exceção ao converter string inválida")
    void deveLancarExcecaoAoConverterStringInvalida() {
        // Act & Assert
        assertThatThrownBy(() -> FormaPagamento.valueOf("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve verificar comparação entre formas de pagamento")
    void deveVerificarComparacaoEntreFormas() {
        // Act & Assert
        assertThat(FormaPagamento.MENSAL).isEqualTo(FormaPagamento.MENSAL);
        assertThat(FormaPagamento.MENSAL).isNotEqualTo(FormaPagamento.ANUAL);
    }

    @Test
    @DisplayName("Deve ter hashCode consistente")
    void deveTerHashCodeConsistente() {
        // Act & Assert
        assertThat(FormaPagamento.MENSAL.hashCode()).isEqualTo(FormaPagamento.MENSAL.hashCode());
    }
}
