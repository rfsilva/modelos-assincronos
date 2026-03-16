package com.seguradora.hibrida.domain.apolice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para a classe Cobertura.
 */
@DisplayName("Cobertura - Testes Unitários")
class CoberturaTest {

    @Test
    @DisplayName("Deve criar cobertura válida com sucesso")
    void deveCriarCoberturaValida() {
        // Arrange
        TipoCobertura tipo = TipoCobertura.TOTAL;
        Valor valorCobertura = Valor.reais(50000.00);
        Valor franquia = Valor.reais(2000.00);
        int carenciaDias = 30;

        // Act
        Cobertura cobertura = Cobertura.of(tipo, valorCobertura, franquia, carenciaDias);

        // Assert
        assertThat(cobertura).isNotNull();
        assertThat(cobertura.getTipo()).isEqualTo(tipo);
        assertThat(cobertura.getValorCobertura()).isEqualTo(valorCobertura);
        assertThat(cobertura.getFranquia()).isEqualTo(franquia);
        assertThat(cobertura.getCarenciaDias()).isEqualTo(carenciaDias);
        assertThat(cobertura.isAtiva()).isTrue();
    }

    @Test
    @DisplayName("Deve criar cobertura sem carência")
    void deveCriarCoberturaSemCarencia() {
        // Arrange
        TipoCobertura tipo = TipoCobertura.COLISAO;
        Valor valorCobertura = Valor.reais(30000.00);
        Valor franquia = Valor.reais(1500.00);

        // Act
        Cobertura cobertura = Cobertura.semCarencia(tipo, valorCobertura, franquia);

        // Assert
        assertThat(cobertura).isNotNull();
        assertThat(cobertura.getCarenciaDias()).isZero();
        assertThat(cobertura.temCarencia()).isFalse();
    }

    @Test
    @DisplayName("Deve criar cobertura básica com valores padrão")
    void deveCriarCoberturaBasica() {
        // Arrange
        TipoCobertura tipo = TipoCobertura.ROUBO_FURTO;
        Valor valorCobertura = Valor.reais(40000.00);

        // Act
        Cobertura cobertura = Cobertura.basica(tipo, valorCobertura);

        // Assert
        assertThat(cobertura).isNotNull();
        assertThat(cobertura.getValorCobertura()).isEqualTo(valorCobertura);
        assertThat(cobertura.getFranquia()).isEqualTo(valorCobertura.porcentagem(5.0));
        assertThat(cobertura.getCarenciaDias()).isEqualTo(30); // Roubo/Furto tem 30 dias
    }

    @Test
    @DisplayName("Deve criar cobertura básica de colisão sem carência")
    void deveCriarCoberturaBasicaColisaoSemCarencia() {
        // Arrange
        TipoCobertura tipo = TipoCobertura.COLISAO;
        Valor valorCobertura = Valor.reais(50000.00);

        // Act
        Cobertura cobertura = Cobertura.basica(tipo, valorCobertura);

        // Assert
        assertThat(cobertura.getCarenciaDias()).isZero();
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cobertura com tipo nulo")
    void deveLancarExcecaoAoCriarComTipoNulo() {
        // Arrange
        Valor valorCobertura = Valor.reais(50000.00);
        Valor franquia = Valor.reais(2000.00);

        // Act & Assert
        assertThatThrownBy(() -> Cobertura.of(null, valorCobertura, franquia, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de cobertura não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cobertura com valor nulo")
    void deveLancarExcecaoAoCriarComValorNulo() {
        // Arrange
        TipoCobertura tipo = TipoCobertura.TOTAL;
        Valor franquia = Valor.reais(2000.00);

        // Act & Assert
        assertThatThrownBy(() -> Cobertura.of(tipo, null, franquia, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor da cobertura não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cobertura com valor não positivo")
    void deveLancarExcecaoAoCriarComValorNaoPositivo() {
        // Arrange
        TipoCobertura tipo = TipoCobertura.TOTAL;
        Valor valorCobertura = Valor.zero();
        Valor franquia = Valor.zero();

        // Act & Assert
        assertThatThrownBy(() -> Cobertura.of(tipo, valorCobertura, franquia, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor da cobertura deve ser positivo");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cobertura com franquia nula")
    void deveLancarExcecaoAoCriarComFranquiaNula() {
        // Arrange
        TipoCobertura tipo = TipoCobertura.TOTAL;
        Valor valorCobertura = Valor.reais(50000.00);

        // Act & Assert
        assertThatThrownBy(() -> Cobertura.of(tipo, valorCobertura, null, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Franquia não pode ser nula");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cobertura com franquia maior que valor")
    void deveLancarExcecaoAoCriarComFranquiaMaiorQueValor() {
        // Arrange
        TipoCobertura tipo = TipoCobertura.TOTAL;
        Valor valorCobertura = Valor.reais(50000.00);
        Valor franquia = Valor.reais(60000.00);

        // Act & Assert
        assertThatThrownBy(() -> Cobertura.of(tipo, valorCobertura, franquia, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Franquia não pode ser maior que o valor da cobertura");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cobertura com carência negativa")
    void deveLancarExcecaoAoCriarComCarenciaNegativa() {
        // Arrange
        TipoCobertura tipo = TipoCobertura.TOTAL;
        Valor valorCobertura = Valor.reais(50000.00);
        Valor franquia = Valor.reais(2000.00);

        // Act & Assert
        assertThatThrownBy(() -> Cobertura.of(tipo, valorCobertura, franquia, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Carência deve estar entre 0 e 365 dias");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cobertura com carência maior que 365 dias")
    void deveLancarExcecaoAoCriarComCarenciaMaiorQue365() {
        // Arrange
        TipoCobertura tipo = TipoCobertura.TOTAL;
        Valor valorCobertura = Valor.reais(50000.00);
        Valor franquia = Valor.reais(2000.00);

        // Act & Assert
        assertThatThrownBy(() -> Cobertura.of(tipo, valorCobertura, franquia, 366))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Carência deve estar entre 0 e 365 dias");
    }

    @Test
    @DisplayName("Deve verificar se tem carência")
    void deveVerificarSeTemCarencia() {
        // Arrange
        Cobertura comCarencia = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);
        Cobertura semCarencia = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 0);

        // Act & Assert
        assertThat(comCarencia.temCarencia()).isTrue();
        assertThat(semCarencia.temCarencia()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar carência cumprida corretamente")
    void deveVerificarCarenciaCumpridaCorretamente() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);
        LocalDate dataInicio = LocalDate.of(2024, 1, 1);
        LocalDate dataAntes = LocalDate.of(2024, 1, 15);
        LocalDate dataDepois = LocalDate.of(2024, 2, 1);

        // Act & Assert
        assertThat(cobertura.carenciaCumpridaEm(dataInicio, dataAntes)).isFalse();
        assertThat(cobertura.carenciaCumpridaEm(dataInicio, dataDepois)).isTrue();
    }

    @Test
    @DisplayName("Deve considerar carência cumprida quando não tem carência")
    void deveConsiderarCarenciaCumpridaQuandoNaoTemCarencia() {
        // Arrange
        Cobertura cobertura = Cobertura.semCarencia(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000));
        LocalDate dataInicio = LocalDate.of(2024, 1, 1);
        LocalDate qualquerData = LocalDate.of(2024, 1, 2);

        // Act & Assert
        assertThat(cobertura.carenciaCumpridaEm(dataInicio, qualquerData)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando datas de verificação de carência são nulas")
    void deveRetornarFalsoQuandoDatasSaoNulas() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);
        LocalDate dataInicio = LocalDate.of(2024, 1, 1);

        // Act & Assert
        assertThat(cobertura.carenciaCumpridaEm(null, dataInicio)).isFalse();
        assertThat(cobertura.carenciaCumpridaEm(dataInicio, null)).isFalse();
    }

    @Test
    @DisplayName("Deve calcular indenização líquida corretamente")
    void deveCalcularIndenizacaoLiquidaCorretamente() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 0);
        Valor valorSinistro = Valor.reais(10000);

        // Act
        Valor indenizacao = cobertura.calcularIndenizacaoLiquida(valorSinistro);

        // Assert
        assertThat(indenizacao).isEqualTo(Valor.reais(8000)); // 10000 - 2000
    }

    @Test
    @DisplayName("Deve limitar indenização ao valor da cobertura")
    void deveLimitarIndenizacaoAoValorDaCobertura() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 0);
        Valor valorSinistro = Valor.reais(100000);

        // Act
        Valor indenizacao = cobertura.calcularIndenizacaoLiquida(valorSinistro);

        // Assert
        assertThat(indenizacao).isEqualTo(Valor.reais(48000)); // 50000 - 2000
    }

    @Test
    @DisplayName("Deve retornar zero quando franquia é maior ou igual ao sinistro")
    void deveRetornarZeroQuandoFranquiaMaiorOuIgualSinistro() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(5000), 0);
        Valor valorSinistro = Valor.reais(4000);

        // Act
        Valor indenizacao = cobertura.calcularIndenizacaoLiquida(valorSinistro);

        // Assert
        assertThat(indenizacao).isEqualTo(Valor.zero());
    }

    @Test
    @DisplayName("Deve retornar zero quando cobertura está inativa")
    void deveRetornarZeroQuandoCoberturaInativa() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 0)
                .desativar();
        Valor valorSinistro = Valor.reais(10000);

        // Act
        Valor indenizacao = cobertura.calcularIndenizacaoLiquida(valorSinistro);

        // Assert
        assertThat(indenizacao).isEqualTo(Valor.zero());
    }

    @Test
    @DisplayName("Deve lançar exceção ao calcular indenização com sinistro nulo")
    void deveLancarExcecaoAoCalcularIndenizacaoComSinistroNulo() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 0);

        // Act & Assert
        assertThatThrownBy(() -> cobertura.calcularIndenizacaoLiquida(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor do sinistro não pode ser nulo");
    }

    @Test
    @DisplayName("Deve calcular prêmio corretamente")
    void deveCalcularPremioCorretamente() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 0);
        Valor valorSegurado = Valor.reais(100000);

        // Act
        Valor premio = cobertura.calcularPremio(valorSegurado);

        // Assert
        assertThat(premio).isEqualTo(valorSegurado.multiplicar(TipoCobertura.TOTAL.getFatorPremio()));
    }

    @Test
    @DisplayName("Deve retornar zero no prêmio quando cobertura está inativa")
    void deveRetornarZeroPremioQuandoCoberturaInativa() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 0)
                .desativar();
        Valor valorSegurado = Valor.reais(100000);

        // Act
        Valor premio = cobertura.calcularPremio(valorSegurado);

        // Assert
        assertThat(premio).isEqualTo(Valor.zero());
    }

    @Test
    @DisplayName("Deve lançar exceção ao calcular prêmio com valor nulo")
    void deveLancarExcecaoAoCalcularPremioComValorNulo() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 0);

        // Act & Assert
        assertThatThrownBy(() -> cobertura.calcularPremio(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor segurado não pode ser nulo");
    }

    @Test
    @DisplayName("Deve desativar cobertura")
    void deveDesativarCobertura() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);

        // Act
        Cobertura desativada = cobertura.desativar();

        // Assert
        assertThat(cobertura.isAtiva()).isTrue();
        assertThat(desativada.isAtiva()).isFalse();
        assertThat(desativada.getTipo()).isEqualTo(cobertura.getTipo());
    }

    @Test
    @DisplayName("Deve ativar cobertura")
    void deveAtivarCobertura() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30)
                .desativar();

        // Act
        Cobertura ativada = cobertura.ativar();

        // Assert
        assertThat(cobertura.isAtiva()).isFalse();
        assertThat(ativada.isAtiva()).isTrue();
    }

    @Test
    @DisplayName("Deve atualizar valor da cobertura")
    void deveAtualizarValorDaCobertura() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);
        Valor novoValor = Valor.reais(60000);

        // Act
        Cobertura atualizada = cobertura.atualizarValor(novoValor);

        // Assert
        assertThat(atualizada.getValorCobertura()).isEqualTo(novoValor);
        assertThat(atualizada.getFranquia()).isEqualTo(cobertura.getFranquia());
    }

    @Test
    @DisplayName("Deve ajustar franquia proporcionalmente ao atualizar valor para menor")
    void deveAjustarFranquiaAoAtualizarValorMenor() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(5000), 30);
        Valor novoValor = Valor.reais(4000); // Menor que a franquia

        // Act
        Cobertura atualizada = cobertura.atualizarValor(novoValor);

        // Assert
        assertThat(atualizada.getValorCobertura()).isEqualTo(novoValor);
        assertThat(atualizada.getFranquia()).isEqualTo(novoValor.porcentagem(5.0));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar valor com nulo")
    void deveLancarExcecaoAoAtualizarValorNulo() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);

        // Act & Assert
        assertThatThrownBy(() -> cobertura.atualizarValor(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Novo valor não pode ser nulo");
    }

    @Test
    @DisplayName("Deve atualizar franquia")
    void deveAtualizarFranquia() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);
        Valor novaFranquia = Valor.reais(3000);

        // Act
        Cobertura atualizada = cobertura.atualizarFranquia(novaFranquia);

        // Assert
        assertThat(atualizada.getFranquia()).isEqualTo(novaFranquia);
        assertThat(atualizada.getValorCobertura()).isEqualTo(cobertura.getValorCobertura());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar franquia inválida")
    void deveLancarExcecaoAoAtualizarFranquiaInvalida() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);
        Valor franquiaInvalida = Valor.reais(60000); // Maior que valor da cobertura

        // Act & Assert
        assertThatThrownBy(() -> cobertura.atualizarFranquia(franquiaInvalida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Franquia não pode ser maior que o valor da cobertura");
    }

    @Test
    @DisplayName("Deve gerar resumo correto")
    void deveGerarResumoCorreto() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);

        // Act
        String resumo = cobertura.getResumo();

        // Assert
        assertThat(resumo).contains("Total");
        assertThat(resumo).contains("50.000");
        assertThat(resumo).contains("2.000");
    }

    @Test
    @DisplayName("Deve implementar equals corretamente")
    void deveImplementarEqualsCorretamente() {
        // Arrange
        Cobertura c1 = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);
        Cobertura c2 = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);
        Cobertura c3 = Cobertura.of(TipoCobertura.PARCIAL, Valor.reais(50000), Valor.reais(2000), 30);

        // Act & Assert
        assertThat(c1).isEqualTo(c2);
        assertThat(c1).isNotEqualTo(c3);
        assertThat(c1.equals(c1)).isTrue();
        assertThat(c1.equals(null)).isFalse();
        assertThat(c1.equals("string")).isFalse();
    }

    @Test
    @DisplayName("Deve implementar hashCode corretamente")
    void deveImplementarHashCodeCorretamente() {
        // Arrange
        Cobertura c1 = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);
        Cobertura c2 = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);

        // Act & Assert
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    @DisplayName("Deve implementar toString corretamente")
    void deveImplementarToStringCorretamente() {
        // Arrange
        Cobertura cobertura = Cobertura.of(TipoCobertura.TOTAL, Valor.reais(50000), Valor.reais(2000), 30);

        // Act
        String toString = cobertura.toString();

        // Assert
        assertThat(toString).isEqualTo(cobertura.getResumo());
    }
}
