package com.seguradora.hibrida.domain.apolice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para a classe Premio.
 */
@DisplayName("Premio - Testes Unitários")
class PremioTest {

    @Test
    @DisplayName("Deve criar prêmio mensal válido")
    void deveCriarPremioMensalValido() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00);
        FormaPagamento forma = FormaPagamento.MENSAL;
        LocalDate dataInicio = LocalDate.now();

        // Act
        Premio premio = Premio.of(valorBase, forma, dataInicio);

        // Assert
        assertThat(premio).isNotNull();
        assertThat(premio.getFormaPagamento()).isEqualTo(forma);
        assertThat(premio.getValorTotal()).isEqualTo(valorBase.multiplicar(forma.getFatorAjuste()));
        assertThat(premio.getNumeroParcelas()).isEqualTo(12);
    }

    @Test
    @DisplayName("Deve criar prêmio anual à vista")
    void deveCriarPremioAnualAVista() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00);
        LocalDate dataVencimento = LocalDate.now().plusMonths(1);

        // Act
        Premio premio = Premio.anualAVista(valorBase, dataVencimento);

        // Assert
        assertThat(premio).isNotNull();
        assertThat(premio.getFormaPagamento()).isEqualTo(FormaPagamento.ANUAL);
        assertThat(premio.isAVista()).isTrue();
        assertThat(premio.isParcelado()).isFalse();
        assertThat(premio.getNumeroParcelas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve criar prêmio mensal com método conveniente")
    void deveCriarPremioMensalComMetodoConveniente() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00);
        LocalDate dataPrimeira = LocalDate.now();

        // Act
        Premio premio = Premio.mensal(valorBase, dataPrimeira);

        // Assert
        assertThat(premio.getFormaPagamento()).isEqualTo(FormaPagamento.MENSAL);
        assertThat(premio.getNumeroParcelas()).isEqualTo(12);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar prêmio com valor base nulo")
    void deveLancarExcecaoAoCriarComValorBaseNulo() {
        // Arrange
        LocalDate dataInicio = LocalDate.now();

        // Act & Assert
        assertThatThrownBy(() -> Premio.of(null, FormaPagamento.MENSAL, dataInicio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor base não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar prêmio com valor base não positivo")
    void deveLancarExcecaoAoCriarComValorBaseNaoPositivo() {
        // Arrange
        Valor valorZero = Valor.zero();
        LocalDate dataInicio = LocalDate.now();

        // Act & Assert
        assertThatThrownBy(() -> Premio.of(valorZero, FormaPagamento.MENSAL, dataInicio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor base deve ser positivo");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar prêmio com forma de pagamento nula")
    void deveLancarExcecaoAoCriarComFormaPagamentoNula() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00);
        LocalDate dataInicio = LocalDate.now();

        // Act & Assert
        assertThatThrownBy(() -> Premio.of(valorBase, null, dataInicio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Forma de pagamento não pode ser nula");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar prêmio com data início nula")
    void deveLancarExcecaoAoCriarComDataInicicioNula() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00);

        // Act & Assert
        assertThatThrownBy(() -> Premio.of(valorBase, FormaPagamento.MENSAL, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de início não pode ser nula");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar prêmio com data muito anterior")
    void deveLancarExcecaoAoCriarComDataMuitoAnterior() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00);
        LocalDate dataMuitoAnterior = LocalDate.now().minusDays(31);

        // Act & Assert
        assertThatThrownBy(() -> Premio.of(valorBase, FormaPagamento.MENSAL, dataMuitoAnterior))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de início não pode ser muito anterior à data atual");
    }

    @Test
    @DisplayName("Deve gerar parcelas mensais corretamente")
    void deveGerarParcelasMensaisCorretamente() {
        // Arrange
        Valor valorBase = Valor.reais(1200.00);
        LocalDate dataInicio = LocalDate.now();

        // Act
        Premio premio = Premio.of(valorBase, FormaPagamento.MENSAL, dataInicio);

        // Assert
        List<Premio.Parcela> parcelas = premio.getParcelas();
        assertThat(parcelas).hasSize(12);

        for (int i = 0; i < 12; i++) {
            Premio.Parcela parcela = parcelas.get(i);
            assertThat(parcela.getNumero()).isEqualTo(i + 1);
            assertThat(parcela.getDataVencimento()).isEqualTo(dataInicio.plusMonths(i));
        }
    }

    @Test
    @DisplayName("Deve gerar parcelas trimestrais corretamente")
    void deveGerarParcelasTrimestraisCorretamente() {
        // Arrange
        Valor valorBase = Valor.reais(1200.00);
        LocalDate dataInicio = LocalDate.now();

        // Act
        Premio premio = Premio.of(valorBase, FormaPagamento.TRIMESTRAL, dataInicio);

        // Assert
        List<Premio.Parcela> parcelas = premio.getParcelas();
        assertThat(parcelas).hasSize(4);
        assertThat(parcelas.get(0).getDataVencimento()).isEqualTo(dataInicio);
        assertThat(parcelas.get(1).getDataVencimento()).isEqualTo(dataInicio.plusMonths(3));
        assertThat(parcelas.get(2).getDataVencimento()).isEqualTo(dataInicio.plusMonths(6));
        assertThat(parcelas.get(3).getDataVencimento()).isEqualTo(dataInicio.plusMonths(9));
    }

    @Test
    @DisplayName("Deve gerar parcelas semestrais corretamente")
    void deveGerarParcelasSemestraisCorretamente() {
        // Arrange
        Valor valorBase = Valor.reais(1200.00);
        LocalDate dataInicio = LocalDate.now();

        // Act
        Premio premio = Premio.of(valorBase, FormaPagamento.SEMESTRAL, dataInicio);

        // Assert
        List<Premio.Parcela> parcelas = premio.getParcelas();
        assertThat(parcelas).hasSize(2);
        assertThat(parcelas.get(0).getDataVencimento()).isEqualTo(dataInicio);
        assertThat(parcelas.get(1).getDataVencimento()).isEqualTo(dataInicio.plusMonths(6));
    }

    @Test
    @DisplayName("Deve gerar apenas uma parcela para pagamento anual")
    void deveGerarUmaParcelaParaPagamentoAnual() {
        // Arrange
        Valor valorBase = Valor.reais(1200.00);
        LocalDate dataInicio = LocalDate.now();

        // Act
        Premio premio = Premio.of(valorBase, FormaPagamento.ANUAL, dataInicio);

        // Assert
        List<Premio.Parcela> parcelas = premio.getParcelas();
        assertThat(parcelas).hasSize(1);
        assertThat(parcelas.get(0).getDataVencimento()).isEqualTo(dataInicio);
    }

    @Test
    @DisplayName("Deve ajustar última parcela para compensar arredondamento")
    void deveAjustarUltimaParcelaParaCompensarArredondamento() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00); // 1050 total mensal
        LocalDate dataInicio = LocalDate.now();

        // Act
        Premio premio = Premio.of(valorBase, FormaPagamento.MENSAL, dataInicio);

        // Assert
        Valor somaParcelas = premio.getParcelas().stream()
                .map(Premio.Parcela::getValor)
                .reduce(Valor.zero(), Valor::somar);

        assertThat(somaParcelas).isEqualTo(premio.getValorTotal());
    }

    @Test
    @DisplayName("Deve retornar valor da primeira parcela")
    void deveRetornarValorPrimeiraParcela() {
        // Arrange
        Valor valorBase = Valor.reais(1200.00);
        LocalDate dataInicio = LocalDate.now();
        Premio premio = Premio.of(valorBase, FormaPagamento.MENSAL, dataInicio);

        // Act
        Valor valorPrimeira = premio.getValorPrimeiraParcela();

        // Assert
        assertThat(valorPrimeira).isNotNull();
        assertThat(valorPrimeira.isPositivo()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar data da primeira parcela")
    void deveRetornarDataPrimeiraParcela() {
        // Arrange
        LocalDate dataInicio = LocalDate.now();
        Premio premio = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, dataInicio);

        // Act
        LocalDate dataPrimeira = premio.getDataPrimeiraParcela();

        // Assert
        assertThat(dataPrimeira).isEqualTo(dataInicio);
    }

    @Test
    @DisplayName("Deve retornar data da última parcela")
    void deveRetornarDataUltimaParcela() {
        // Arrange
        LocalDate dataInicio = LocalDate.now();
        Premio premio = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, dataInicio);

        // Act
        LocalDate dataUltima = premio.getDataUltimaParcela();

        // Assert
        assertThat(dataUltima).isEqualTo(dataInicio.plusMonths(11));
    }

    @Test
    @DisplayName("Deve retornar parcelas vencidas até data específica")
    void deveRetornarParcelasVencidasAteData() {
        // Arrange
        LocalDate dataInicio = LocalDate.now().minusDays(20); // Dentro dos 30 dias permitidos
        Premio premio = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, dataInicio);
        LocalDate dataVerificacao = LocalDate.now().minusDays(5); // 5 dias atrás

        // Act
        List<Premio.Parcela> vencidas = premio.getParcelasVencidasAte(dataVerificacao);

        // Assert
        assertThat(vencidas).hasSize(1); // 1 mês já vencido
    }

    @Test
    @DisplayName("Deve retornar parcelas a vencer após data específica")
    void deveRetornarParcelasAVencerAposData() {
        // Arrange
        LocalDate dataInicio = LocalDate.now().minusDays(20); // Dentro dos 30 dias permitidos
        Premio premio = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, dataInicio);
        LocalDate dataVerificacao = LocalDate.now().minusDays(5); // 5 dias atrás

        // Act
        List<Premio.Parcela> aVencer = premio.getParcelasAVencerApos(dataVerificacao);

        // Assert
        assertThat(aVencer).hasSize(11); // 11 meses a vencer
    }

    @Test
    @DisplayName("Deve retornar lista vazia ao consultar parcelas vencidas com data nula")
    void deveRetornarListaVaziaAoConsultarVencidasComDataNula() {
        // Arrange
        Premio premio = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, LocalDate.now());

        // Act
        List<Premio.Parcela> vencidas = premio.getParcelasVencidasAte(null);

        // Assert
        assertThat(vencidas).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar todas parcelas ao consultar a vencer com data nula")
    void deveRetornarTodasParcelasAoConsultarAVencerComDataNula() {
        // Arrange
        Premio premio = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, LocalDate.now());

        // Act
        List<Premio.Parcela> aVencer = premio.getParcelasAVencerApos(null);

        // Assert
        assertThat(aVencer).hasSize(12);
    }

    @Test
    @DisplayName("Deve calcular valor vencido até data")
    void deveCalcularValorVencidoAteData() {
        // Arrange
        LocalDate dataInicio = LocalDate.now().minusDays(20); // Dentro dos 30 dias permitidos
        Premio premio = Premio.of(Valor.reais(1200), FormaPagamento.MENSAL, dataInicio);
        LocalDate dataVerificacao = LocalDate.now();

        // Act
        Valor valorVencido = premio.getValorVencidoAte(dataVerificacao);

        // Assert
        assertThat(valorVencido.isPositivo()).isTrue();
    }

    @Test
    @DisplayName("Deve calcular valor a vencer após data")
    void deveCalcularValorAVencerAposData() {
        // Arrange
        LocalDate dataInicio = LocalDate.now().minusDays(20); // Dentro dos 30 dias permitidos
        Premio premio = Premio.of(Valor.reais(1200), FormaPagamento.MENSAL, dataInicio);
        LocalDate dataVerificacao = LocalDate.now();

        // Act
        Valor valorAVencer = premio.getValorAVencer(dataVerificacao);

        // Assert
        assertThat(valorAVencer.isPositivo()).isTrue();
    }

    @Test
    @DisplayName("Deve verificar se tem parcelas vencidas")
    void deveVerificarSeTemParcelasVencidas() {
        // Arrange
        LocalDate dataPassada = LocalDate.now().minusDays(10);
        Premio premioComVencidas = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, dataPassada);

        LocalDate dataFutura = LocalDate.now().plusDays(10);
        Premio premioSemVencidas = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, dataFutura);

        // Act & Assert
        assertThat(premioComVencidas.temParcelasVencidas()).isTrue();
        assertThat(premioSemVencidas.temParcelasVencidas()).isFalse();
    }

    @Test
    @DisplayName("Deve gerar resumo para pagamento à vista")
    void deveGerarResumoParaPagamentoAVista() {
        // Arrange
        Valor valorBase = Valor.reais(1000.00);
        Premio premio = Premio.anualAVista(valorBase, LocalDate.now());

        // Act
        String resumo = premio.getResumo();

        // Assert
        assertThat(resumo).contains("À vista");
        assertThat(resumo).contains("950"); // Com desconto de 5%
    }

    @Test
    @DisplayName("Deve gerar resumo para pagamento parcelado")
    void deveGerarResumoParaPagamentoParcelado() {
        // Arrange
        Valor valorBase = Valor.reais(1200.00);
        Premio premio = Premio.mensal(valorBase, LocalDate.now());

        // Act
        String resumo = premio.getResumo();

        // Assert
        assertThat(resumo).contains("12x");
    }

    @Test
    @DisplayName("Deve implementar equals corretamente")
    void deveImplementarEqualsCorretamente() {
        // Arrange
        LocalDate data = LocalDate.now();
        Premio p1 = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, data);
        Premio p2 = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, data);
        Premio p3 = Premio.of(Valor.reais(2000), FormaPagamento.MENSAL, data);

        // Act & Assert
        assertThat(p1).isEqualTo(p2);
        assertThat(p1).isNotEqualTo(p3);
        assertThat(p1.equals(null)).isFalse();
    }

    @Test
    @DisplayName("Deve implementar hashCode corretamente")
    void deveImplementarHashCodeCorretamente() {
        // Arrange
        LocalDate data = LocalDate.now();
        Premio p1 = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, data);
        Premio p2 = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, data);

        // Act & Assert
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    @DisplayName("Deve implementar toString corretamente")
    void deveImplementarToStringCorretamente() {
        // Arrange
        Premio premio = Premio.of(Valor.reais(1000), FormaPagamento.MENSAL, LocalDate.now());

        // Act
        String toString = premio.toString();

        // Assert
        assertThat(toString).isEqualTo(premio.getResumo());
    }

    // Testes da classe interna Parcela

    @Test
    @DisplayName("Parcela - Deve criar parcela com todos os atributos")
    void parcelaDeveCriarComTodosAtributos() {
        // Arrange
        int numero = 1;
        Valor valor = Valor.reais(100);
        LocalDate dataVencimento = LocalDate.now().plusDays(30);

        // Act
        Premio.Parcela parcela = new Premio.Parcela(numero, valor, dataVencimento);

        // Assert
        assertThat(parcela.getNumero()).isEqualTo(numero);
        assertThat(parcela.getValor()).isEqualTo(valor);
        assertThat(parcela.getDataVencimento()).isEqualTo(dataVencimento);
    }

    @Test
    @DisplayName("Parcela - Deve verificar se está vencida")
    void parcelaDeveVerificarSeVencida() {
        // Arrange
        Premio.Parcela vencida = new Premio.Parcela(1, Valor.reais(100), LocalDate.now().minusDays(1));
        Premio.Parcela naovencida = new Premio.Parcela(1, Valor.reais(100), LocalDate.now().plusDays(1));

        // Act & Assert
        assertThat(vencida.isVencida()).isTrue();
        assertThat(naovencida.isVencida()).isFalse();
    }

    @Test
    @DisplayName("Parcela - Deve verificar se vence hoje")
    void parcelaDeveVerificarSeVenceHoje() {
        // Arrange
        Premio.Parcela venceHoje = new Premio.Parcela(1, Valor.reais(100), LocalDate.now());
        Premio.Parcela naoVenceHoje = new Premio.Parcela(1, Valor.reais(100), LocalDate.now().plusDays(1));

        // Act & Assert
        assertThat(venceHoje.venceHoje()).isTrue();
        assertThat(naoVenceHoje.venceHoje()).isFalse();
    }

    @Test
    @DisplayName("Parcela - Deve verificar se vence em N dias")
    void parcelaDeveVerificarSeVenceEmNDias() {
        // Arrange
        Premio.Parcela parcela = new Premio.Parcela(1, Valor.reais(100), LocalDate.now().plusDays(5));

        // Act & Assert
        assertThat(parcela.venceEm(7)).isTrue();
        assertThat(parcela.venceEm(3)).isFalse();
    }

    @Test
    @DisplayName("Parcela - Deve implementar equals corretamente")
    void parcelaDeveImplementarEqualsCorretamente() {
        // Arrange
        LocalDate data = LocalDate.now().plusDays(30);
        Premio.Parcela p1 = new Premio.Parcela(1, Valor.reais(100), data);
        Premio.Parcela p2 = new Premio.Parcela(1, Valor.reais(100), data);
        Premio.Parcela p3 = new Premio.Parcela(2, Valor.reais(100), data);

        // Act & Assert
        assertThat(p1).isEqualTo(p2);
        assertThat(p1).isNotEqualTo(p3);
    }

    @Test
    @DisplayName("Parcela - Deve implementar hashCode corretamente")
    void parcelaDeveImplementarHashCodeCorretamente() {
        // Arrange
        LocalDate data = LocalDate.now().plusDays(30);
        Premio.Parcela p1 = new Premio.Parcela(1, Valor.reais(100), data);
        Premio.Parcela p2 = new Premio.Parcela(1, Valor.reais(100), data);

        // Act & Assert
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    @DisplayName("Parcela - Deve implementar toString corretamente")
    void parcelaDeveImplementarToStringCorretamente() {
        // Arrange
        LocalDate data = LocalDate.now().plusDays(30);
        Premio.Parcela parcela = new Premio.Parcela(1, Valor.reais(100), data);

        // Act
        String toString = parcela.toString();

        // Assert
        assertThat(toString).contains("Parcela 1");
        assertThat(toString).contains("100");
    }
}
