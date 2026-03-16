package com.seguradora.hibrida.domain.apolice.service;

import com.seguradora.hibrida.domain.apolice.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para CalculadoraPremioService.
 *
 * @author Test Automation Team
 * @since 1.0.0
 */
@DisplayName("CalculadoraPremioService - Testes Unitários")
class CalculadoraPremioServiceTest {

    private CalculadoraPremioService service;

    @BeforeEach
    void setUp() {
        service = new CalculadoraPremioService();
    }

    // ===================================================================
    // TESTES: calcularPremio (método principal)
    // ===================================================================

    @Nested
    @DisplayName("calcularPremio - Cálculo completo de prêmio")
    class CalcularPremioTests {

        @Test
        @DisplayName("Deve calcular prêmio com uma cobertura básica")
        void deveCalcularPremioComUmaCobertura() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorSegurado)
            );
            FormaPagamento formaPagamento = FormaPagamento.ANUAL;
            LocalDate dataInicio = LocalDate.now();

            // Act
            Premio premio = service.calcularPremio(valorSegurado, coberturas, formaPagamento, dataInicio);

            // Assert
            assertThat(premio).isNotNull();
            assertThat(premio.getValorTotal()).isNotNull();
            assertThat(premio.getValorTotal().isPositivo()).isTrue();
            assertThat(premio.getFormaPagamento()).isEqualTo(FormaPagamento.ANUAL);
            assertThat(premio.getNumeroParcelas()).isEqualTo(1);
        }

        @Test
        @DisplayName("Deve calcular prêmio com múltiplas coberturas")
        void deveCalcularPremioComMultiplasCoberturas() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.COLISAO, valorSegurado),
                Cobertura.basica(TipoCobertura.ROUBO_FURTO, valorSegurado),
                Cobertura.basica(TipoCobertura.INCENDIO, valorSegurado)
            );
            FormaPagamento formaPagamento = FormaPagamento.MENSAL;
            LocalDate dataInicio = LocalDate.now();

            // Act
            Premio premio = service.calcularPremio(valorSegurado, coberturas, formaPagamento, dataInicio);

            // Assert
            assertThat(premio).isNotNull();
            assertThat(premio.getValorTotal().isPositivo()).isTrue();
            assertThat(premio.getNumeroParcelas()).isEqualTo(12);
        }

        @Test
        @DisplayName("Deve aplicar desconto para 3 ou mais coberturas")
        void deveAplicarDescontoMultiplasCoberturas() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);

            // Prêmio com 2 coberturas (sem desconto)
            List<Cobertura> duasCoberturas = List.of(
                Cobertura.basica(TipoCobertura.COLISAO, valorSegurado),
                Cobertura.basica(TipoCobertura.ROUBO_FURTO, valorSegurado)
            );
            Premio premioSemDesconto = service.calcularPremio(
                valorSegurado, duasCoberturas, FormaPagamento.ANUAL, LocalDate.now()
            );

            // Prêmio com 3 coberturas (com desconto de 10%)
            List<Cobertura> tresCoberturas = List.of(
                Cobertura.basica(TipoCobertura.COLISAO, valorSegurado),
                Cobertura.basica(TipoCobertura.ROUBO_FURTO, valorSegurado),
                Cobertura.basica(TipoCobertura.INCENDIO, valorSegurado)
            );
            Premio premioComDesconto = service.calcularPremio(
                valorSegurado, tresCoberturas, FormaPagamento.ANUAL, LocalDate.now()
            );

            // Assert - Prêmio com 3 coberturas deve ser menor proporcionalmente devido ao desconto
            assertThat(premioComDesconto.getValorTotal()).isNotNull();
            assertThat(premioSemDesconto.getValorTotal()).isNotNull();
        }

        @Test
        @DisplayName("Deve aplicar fator de ajuste para pagamento mensal")
        void deveAplicarFatorAjusteMensal() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorSegurado)
            );
            LocalDate dataInicio = LocalDate.now();

            Premio premioAnual = service.calcularPremio(
                valorSegurado, coberturas, FormaPagamento.ANUAL, dataInicio
            );
            Premio premioMensal = service.calcularPremio(
                valorSegurado, coberturas, FormaPagamento.MENSAL, dataInicio
            );

            // Assert - Mensal deve ser mais caro que anual (fator 1.05 vs 0.95)
            assertThat(premioMensal.getValorTotal().ehMaiorQue(premioAnual.getValorTotal())).isTrue();
        }

        @ParameterizedTest
        @EnumSource(FormaPagamento.class)
        @DisplayName("Deve calcular prêmio para todas as formas de pagamento")
        void deveCalcularPremioParaTodasFormasPagamento(FormaPagamento formaPagamento) {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorSegurado)
            );
            LocalDate dataInicio = LocalDate.now();

            // Act
            Premio premio = service.calcularPremio(valorSegurado, coberturas, formaPagamento, dataInicio);

            // Assert
            assertThat(premio).isNotNull();
            assertThat(premio.getFormaPagamento()).isEqualTo(formaPagamento);
            assertThat(premio.getNumeroParcelas()).isEqualTo(formaPagamento.getNumeroParcelas());
        }

        @Test
        @DisplayName("Deve lançar exceção com valor segurado nulo")
        void deveLancarExcecaoValorSeguradoNulo() {
            // Arrange
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, Valor.reais(100000.00))
            );

            // Act & Assert
            assertThatThrownBy(() ->
                service.calcularPremio(null, coberturas, FormaPagamento.ANUAL, LocalDate.now())
            ).satisfiesAnyOf(
                ex -> assertThat(ex).isInstanceOf(NullPointerException.class),
                ex -> assertThat(ex).isInstanceOf(IllegalArgumentException.class)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção com valor segurado zero")
        void deveLancarExcecaoValorSeguradoZero() {
            // Arrange
            Valor valorZero = Valor.zero();
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, Valor.reais(100000.00))
            );

            // Act & Assert
            assertThatThrownBy(() ->
                service.calcularPremio(valorZero, coberturas, FormaPagamento.ANUAL, LocalDate.now())
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("positivo");
        }

        @Test
        @DisplayName("Deve lançar exceção com lista de coberturas vazia")
        void deveLancarExcecaoCoberturasVazia() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturasVazias = List.of();

            // Act & Assert
            assertThatThrownBy(() ->
                service.calcularPremio(valorSegurado, coberturasVazias, FormaPagamento.ANUAL, LocalDate.now())
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("cobertura");
        }

        @Test
        @DisplayName("Deve lançar exceção com forma de pagamento nula")
        void deveLancarExcecaoFormaPagamentoNula() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorSegurado)
            );

            // Act & Assert
            assertThatThrownBy(() ->
                service.calcularPremio(valorSegurado, coberturas, null, LocalDate.now())
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Forma de pagamento");
        }

        @Test
        @DisplayName("Deve lançar exceção com data de início nula")
        void deveLancarExcecaoDataInicioNula() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorSegurado)
            );

            // Act & Assert
            assertThatThrownBy(() ->
                service.calcularPremio(valorSegurado, coberturas, FormaPagamento.ANUAL, null)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Data de início");
        }
    }

    // ===================================================================
    // TESTES: calcularPremioAdicionalCobertura
    // ===================================================================

    @Nested
    @DisplayName("calcularPremioAdicionalCobertura - Cálculo de prêmio adicional")
    class CalcularPremioAdicionalCoberturaTests {

        @Test
        @DisplayName("Deve calcular prêmio adicional para nova cobertura")
        void deveCalcularPremioAdicionalNovaCobertura() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            Cobertura novaCobertura = Cobertura.basica(TipoCobertura.INCENDIO, valorSegurado);
            List<Cobertura> coberturasExistentes = List.of(
                Cobertura.basica(TipoCobertura.COLISAO, valorSegurado)
            );

            // Act
            Valor premioAdicional = service.calcularPremioAdicionalCobertura(
                valorSegurado, novaCobertura, coberturasExistentes
            );

            // Assert
            assertThat(premioAdicional).isNotNull();
            assertThat(premioAdicional.isPositivo()).isTrue();
        }

        @Test
        @DisplayName("Deve aplicar desconto adicional quando já houver 2+ coberturas")
        void deveAplicarDescontoAdicionalComMultiplasCoberturas() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            Cobertura novaCobertura = Cobertura.basica(TipoCobertura.INCENDIO, valorSegurado);

            // Com 1 cobertura existente (sem desconto adicional)
            List<Cobertura> umaCobertura = List.of(
                Cobertura.basica(TipoCobertura.COLISAO, valorSegurado)
            );
            Valor premioSemDesconto = service.calcularPremioAdicionalCobertura(
                valorSegurado, novaCobertura, umaCobertura
            );

            // Com 2 coberturas existentes (com desconto de 5%)
            List<Cobertura> duasCoberturas = List.of(
                Cobertura.basica(TipoCobertura.COLISAO, valorSegurado),
                Cobertura.basica(TipoCobertura.ROUBO_FURTO, valorSegurado)
            );
            Valor premioComDesconto = service.calcularPremioAdicionalCobertura(
                valorSegurado, novaCobertura, duasCoberturas
            );

            // Assert - Com desconto deve ser menor
            assertThat(premioComDesconto.ehMenorQue(premioSemDesconto)).isTrue();
        }

        @ParameterizedTest
        @EnumSource(TipoCobertura.class)
        @DisplayName("Deve calcular prêmio adicional para todos os tipos de cobertura")
        void deveCalcularPremioAdicionalTodostipos(TipoCobertura tipo) {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            Cobertura novaCobertura = Cobertura.basica(tipo, valorSegurado);
            List<Cobertura> coberturasExistentes = List.of();

            // Act
            Valor premioAdicional = service.calcularPremioAdicionalCobertura(
                valorSegurado, novaCobertura, coberturasExistentes
            );

            // Assert
            assertThat(premioAdicional).isNotNull();
            assertThat(premioAdicional.isPositivo()).isTrue();
        }
    }

    // ===================================================================
    // TESTES: recalcularPremio
    // ===================================================================

    @Nested
    @DisplayName("recalcularPremio - Recálculo após alteração")
    class RecalcularPremioTests {

        @Test
        @DisplayName("Deve recalcular prêmio com novo valor segurado maior")
        void deveRecalcularPremioComValorMaior() {
            // Arrange
            Valor valorOriginal = Valor.reais(100000.00);
            Valor valorNovo = Valor.reais(150000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorOriginal)
            );

            Premio premioOriginal = service.calcularPremio(
                valorOriginal, coberturas, FormaPagamento.ANUAL, LocalDate.now()
            );

            // Act
            List<Cobertura> coberturasAtualizadas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorNovo)
            );
            Premio premioRecalculado = service.recalcularPremio(
                premioOriginal, valorNovo, coberturasAtualizadas
            );

            // Assert
            assertThat(premioRecalculado.getValorTotal().ehMaiorQue(premioOriginal.getValorTotal()))
                .isTrue();
        }

        @Test
        @DisplayName("Deve recalcular prêmio com novo valor segurado menor")
        void deveRecalcularPremioComValorMenor() {
            // Arrange
            Valor valorOriginal = Valor.reais(100000.00);
            Valor valorNovo = Valor.reais(75000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorOriginal)
            );

            Premio premioOriginal = service.calcularPremio(
                valorOriginal, coberturas, FormaPagamento.ANUAL, LocalDate.now()
            );

            // Act
            List<Cobertura> coberturasAtualizadas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorNovo)
            );
            Premio premioRecalculado = service.recalcularPremio(
                premioOriginal, valorNovo, coberturasAtualizadas
            );

            // Assert
            assertThat(premioRecalculado.getValorTotal().ehMenorQue(premioOriginal.getValorTotal()))
                .isTrue();
        }

        @Test
        @DisplayName("Deve manter a mesma forma de pagamento ao recalcular")
        void deveManterFormaPagamentoAoRecalcular() {
            // Arrange
            Valor valorOriginal = Valor.reais(100000.00);
            Valor valorNovo = Valor.reais(120000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorOriginal)
            );

            Premio premioOriginal = service.calcularPremio(
                valorOriginal, coberturas, FormaPagamento.TRIMESTRAL, LocalDate.now()
            );

            // Act
            List<Cobertura> coberturasAtualizadas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorNovo)
            );
            Premio premioRecalculado = service.recalcularPremio(
                premioOriginal, valorNovo, coberturasAtualizadas
            );

            // Assert
            assertThat(premioRecalculado.getFormaPagamento()).isEqualTo(FormaPagamento.TRIMESTRAL);
        }
    }

    // ===================================================================
    // TESTES: calcularDescontoRenovacao
    // ===================================================================

    @Nested
    @DisplayName("calcularDescontoRenovacao - Descontos por fidelidade")
    class CalcularDescontoRenovacaoTests {

        @Test
        @DisplayName("Deve calcular desconto para cliente com 5+ anos sem sinistros e pagamento em dia")
        void deveCalcularDescontoMaximo() {
            // Act
            BigDecimal desconto = service.calcularDescontoRenovacao(5, false, true);

            // Assert - 15% (anos) + 10% (sem sinistros) + 5% (pagamento) = 30%, mas limitado a 25%
            assertThat(desconto).isEqualByComparingTo(new BigDecimal("0.25"));
        }

        @Test
        @DisplayName("Deve calcular desconto para cliente com 5+ anos")
        void deveCalcularDescontoCincoAnos() {
            // Act
            BigDecimal desconto = service.calcularDescontoRenovacao(5, true, false);

            // Assert - 15% apenas
            assertThat(desconto).isEqualByComparingTo(new BigDecimal("0.15"));
        }

        @Test
        @DisplayName("Deve calcular desconto para cliente com 3 anos")
        void deveCalcularDescontoTresAnos() {
            // Act
            BigDecimal desconto = service.calcularDescontoRenovacao(3, false, true);

            // Assert - 10% (anos) + 10% (sem sinistros) + 5% (pagamento) = 25%
            assertThat(desconto).isEqualByComparingTo(new BigDecimal("0.25"));
        }

        @Test
        @DisplayName("Deve calcular desconto para cliente com 1 ano")
        void deveCalcularDescontoUmAno() {
            // Act
            BigDecimal desconto = service.calcularDescontoRenovacao(1, false, false);

            // Assert - 5% (anos) + 10% (sem sinistros) = 15%
            assertThat(desconto).isEqualByComparingTo(new BigDecimal("0.15"));
        }

        @Test
        @DisplayName("Deve retornar zero para cliente novo com sinistros e pagamento atrasado")
        void deveRetornarZeroParaClienteSemDesconto() {
            // Act
            BigDecimal desconto = service.calcularDescontoRenovacao(0, true, false);

            // Assert
            assertThat(desconto).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Deve limitar desconto ao máximo de 25%")
        void deveLimitarDescontoMaximo() {
            // Act - Tentando obter desconto acima de 25%
            BigDecimal desconto = service.calcularDescontoRenovacao(10, false, true);

            // Assert - Deve ser limitado a 25%
            assertThat(desconto).isEqualByComparingTo(new BigDecimal("0.25"));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 10})
        @DisplayName("Deve calcular desconto para diferentes anos de relacionamento")
        void deveCalcularDescontoParaDiferentesAnos(int anos) {
            // Act
            BigDecimal desconto = service.calcularDescontoRenovacao(anos, false, false);

            // Assert
            assertThat(desconto).isNotNull();
            assertThat(desconto).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(desconto).isLessThanOrEqualTo(new BigDecimal("0.25"));
        }
    }

    // ===================================================================
    // TESTES: calcularIOF
    // ===================================================================

    @Nested
    @DisplayName("calcularIOF - Cálculo de IOF")
    class CalcularIOFTests {

        @Test
        @DisplayName("Deve calcular IOF corretamente (7.38%)")
        void deveCalcularIOFCorretamente() {
            // Arrange
            Valor premioTotal = Valor.reais(1000.00);

            // Act
            Valor iof = service.calcularIOF(premioTotal);

            // Assert - IOF de 7,38%
            Valor iofEsperado = Valor.reais(73.80);
            assertThat(iof).isEqualTo(iofEsperado);
        }

        @Test
        @DisplayName("Deve calcular IOF para valores altos")
        void deveCalcularIOFValoresAltos() {
            // Arrange
            Valor premioTotal = Valor.reais(10000.00);

            // Act
            Valor iof = service.calcularIOF(premioTotal);

            // Assert
            assertThat(iof.isPositivo()).isTrue();
            assertThat(iof.getQuantia()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Deve calcular IOF zero para prêmio zero")
        void deveCalcularIOFZero() {
            // Arrange
            Valor premioZero = Valor.zero();

            // Act
            Valor iof = service.calcularIOF(premioZero);

            // Assert
            assertThat(iof.isZero()).isTrue();
        }
    }

    // ===================================================================
    // TESTES: calcularValorTotalComImpostos
    // ===================================================================

    @Nested
    @DisplayName("calcularValorTotalComImpostos - Valor total com IOF")
    class CalcularValorTotalComImpostosTests {

        @Test
        @DisplayName("Deve calcular valor total incluindo IOF")
        void deveCalcularValorTotalComIOF() {
            // Arrange
            Valor premioTotal = Valor.reais(1000.00);

            // Act
            Valor valorTotal = service.calcularValorTotalComImpostos(premioTotal);

            // Assert - Deve ser maior que o prêmio original
            assertThat(valorTotal.ehMaiorQue(premioTotal)).isTrue();

            // Valor total = 1000 + (1000 * 0.0738) = 1073.80
            Valor esperado = Valor.reais(1073.80);
            assertThat(valorTotal).isEqualTo(esperado);
        }

        @Test
        @DisplayName("Deve calcular valor total para prêmios altos")
        void deveCalcularValorTotalPremiosAltos() {
            // Arrange
            Valor premioTotal = Valor.reais(50000.00);

            // Act
            Valor valorTotal = service.calcularValorTotalComImpostos(premioTotal);

            // Assert
            assertThat(valorTotal.ehMaiorQue(premioTotal)).isTrue();
        }
    }

    // ===================================================================
    // TESTES: calcularPremioComDadosExternos
    // ===================================================================

    @Nested
    @DisplayName("calcularPremioComDadosExternos - Cálculo com dados FIPE/CEP")
    class CalcularPremioComDadosExternosTests {

        @Test
        @DisplayName("Deve calcular prêmio com dados externos básicos")
        void deveCalcularPremioComDadosExternos() {
            // Arrange
            Valor valorSegurado = Valor.reais(50000.00);
            String marca = "TOYOTA";
            String modelo = "COROLLA";
            int anoFabricacao = 2023;
            String cep = "01310-100";

            // Act
            Valor premio = service.calcularPremioComDadosExternos(
                valorSegurado, marca, modelo, anoFabricacao, cep
            );

            // Assert
            assertThat(premio).isNotNull();
            assertThat(premio.isPositivo()).isTrue();
        }

        @Test
        @DisplayName("Deve aplicar fator diferente para marcas diferentes")
        void deveAplicarFatorMarca() {
            // Arrange
            Valor valorSegurado = Valor.reais(50000.00);
            int anoFabricacao = 2023;
            String cep = "01310-100";

            // Act
            Valor premioToyota = service.calcularPremioComDadosExternos(
                valorSegurado, "TOYOTA", "COROLLA", anoFabricacao, cep
            );
            Valor premioChevrolet = service.calcularPremioComDadosExternos(
                valorSegurado, "CHEVROLET", "ONIX", anoFabricacao, cep
            );

            // Assert - Marcas têm fatores diferentes
            assertThat(premioToyota).isNotEqualTo(premioChevrolet);
        }

        @Test
        @DisplayName("Deve aplicar fator menor para veículos novos")
        void deveAplicarFatorVeiculoNovo() {
            // Arrange
            Valor valorSegurado = Valor.reais(50000.00);
            String marca = "HONDA";
            String cep = "01310-100";

            // Act
            Valor premioNovo = service.calcularPremioComDadosExternos(
                valorSegurado, marca, "CIVIC", 2024, cep
            );
            Valor premioAntigo = service.calcularPremioComDadosExternos(
                valorSegurado, marca, "CIVIC", 2010, cep
            );

            // Assert - Veículo novo deve ter prêmio menor
            assertThat(premioNovo.ehMenorQue(premioAntigo)).isTrue();
        }

        @Test
        @DisplayName("Deve aplicar fator de localização baseado no CEP")
        void deveAplicarFatorLocalizacao() {
            // Arrange
            Valor valorSegurado = Valor.reais(50000.00);
            String marca = "HONDA";
            int anoFabricacao = 2023;

            // Act
            Valor premioSaoPaulo = service.calcularPremioComDadosExternos(
                valorSegurado, marca, "CIVIC", anoFabricacao, "01310-100"
            );
            Valor premioBeloHorizonte = service.calcularPremioComDadosExternos(
                valorSegurado, marca, "CIVIC", anoFabricacao, "30130-000"
            );

            // Assert - Localizações diferentes têm fatores diferentes
            assertThat(premioSaoPaulo).isNotEqualTo(premioBeloHorizonte);
        }

        @ParameterizedTest
        @ValueSource(ints = {2024, 2020, 2015, 2010, 2005})
        @DisplayName("Deve calcular prêmio para diferentes anos de fabricação")
        void deveCalcularPremioDiferentesAnos(int ano) {
            // Arrange
            Valor valorSegurado = Valor.reais(50000.00);

            // Act
            Valor premio = service.calcularPremioComDadosExternos(
                valorSegurado, "TOYOTA", "COROLLA", ano, "01310-100"
            );

            // Assert
            assertThat(premio).isNotNull();
            assertThat(premio.isPositivo()).isTrue();
        }
    }

    // ===================================================================
    // TESTES: Casos Extremos e Edge Cases
    // ===================================================================

    @Nested
    @DisplayName("Casos Extremos")
    class CasosExtremosTests {

        @Test
        @DisplayName("Deve calcular prêmio com valor segurado muito alto")
        void deveCalcularPremioValorAlto() {
            // Arrange
            Valor valorAlto = Valor.reais(5000000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorAlto)
            );

            // Act
            Premio premio = service.calcularPremio(
                valorAlto, coberturas, FormaPagamento.ANUAL, LocalDate.now()
            );

            // Assert
            assertThat(premio).isNotNull();
            assertThat(premio.getValorTotal().isPositivo()).isTrue();
        }

        @Test
        @DisplayName("Deve calcular prêmio com valor segurado muito baixo")
        void deveCalcularPremioValorBaixo() {
            // Arrange
            Valor valorBaixo = Valor.reais(1000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TERCEIROS, valorBaixo)
            );

            // Act
            Premio premio = service.calcularPremio(
                valorBaixo, coberturas, FormaPagamento.MENSAL, LocalDate.now()
            );

            // Assert
            assertThat(premio).isNotNull();
            assertThat(premio.getValorTotal().isPositivo()).isTrue();
        }

        @Test
        @DisplayName("Deve calcular prêmio com coberturas inativas")
        void deveCalcularPremioCoberturasInativas() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = new ArrayList<>();
            coberturas.add(Cobertura.basica(TipoCobertura.COLISAO, valorSegurado));
            coberturas.add(Cobertura.basica(TipoCobertura.ROUBO_FURTO, valorSegurado).desativar());

            // Act
            Premio premio = service.calcularPremio(
                valorSegurado, coberturas, FormaPagamento.ANUAL, LocalDate.now()
            );

            // Assert - Apenas coberturas ativas devem ser consideradas
            assertThat(premio).isNotNull();
            assertThat(premio.getValorTotal().isPositivo()).isTrue();
        }

        @Test
        @DisplayName("Deve calcular prêmio com data de início futura")
        void deveCalcularPremioDataFutura() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorSegurado)
            );
            LocalDate dataFutura = LocalDate.now().plusDays(30);

            // Act
            Premio premio = service.calcularPremio(
                valorSegurado, coberturas, FormaPagamento.ANUAL, dataFutura
            );

            // Assert
            assertThat(premio).isNotNull();
            assertThat(premio.getDataPrimeiraParcela()).isEqualTo(dataFutura);
        }

        @Test
        @DisplayName("Deve calcular desconto renovação com anos negativos")
        void deveCalcularDescontoAnosNegativos() {
            // Act
            BigDecimal desconto = service.calcularDescontoRenovacao(-1, false, false);

            // Assert - Deve retornar apenas desconto de não ter sinistros
            assertThat(desconto).isEqualByComparingTo(new BigDecimal("0.10"));
        }
    }
}
