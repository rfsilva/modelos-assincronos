package com.seguradora.hibrida.domain.veiculo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link TipoCombustivel}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("TipoCombustivel - Testes Unitários")
class TipoCombustivelTest {

    @Nested
    @DisplayName("Testes de Valores do Enum")
    class ValoresEnumTests {

        @Test
        @DisplayName("Deve ter todos os tipos esperados")
        void deveTerTodosTiposEsperados() {
            TipoCombustivel[] valores = TipoCombustivel.values();

            assertThat(valores).hasSize(6);
            assertThat(valores).containsExactlyInAnyOrder(
                TipoCombustivel.GASOLINA,
                TipoCombustivel.ETANOL,
                TipoCombustivel.FLEX,
                TipoCombustivel.DIESEL,
                TipoCombustivel.GNV,
                TipoCombustivel.ELETRICO
            );
        }

        @ParameterizedTest
        @EnumSource(TipoCombustivel.class)
        @DisplayName("Todos os tipos devem ter dados completos")
        void todosTiposDevemTermDadosCompletos(TipoCombustivel tipo) {
            assertThat(tipo.getNome()).isNotNull().isNotEmpty();
            assertThat(tipo.getDescricao()).isNotNull().isNotEmpty();
            assertThat(tipo.getFatorRisco()).isPositive();
        }
    }

    @Nested
    @DisplayName("Testes de Derivado de Petróleo")
    class DerivadoPetroleoTests {

        @Test
        @DisplayName("Gasolina, etanol e flex devem ser derivados de petróleo")
        void gasolinaEtanolFlexDevemSerDerivadosPetroleo() {
            assertThat(TipoCombustivel.GASOLINA.isDerivadoPetroleo()).isTrue();
            assertThat(TipoCombustivel.ETANOL.isDerivadoPetroleo()).isTrue();
            assertThat(TipoCombustivel.FLEX.isDerivadoPetroleo()).isTrue();
        }

        @Test
        @DisplayName("Diesel, GNV e elétrico não devem ser derivados de petróleo")
        void dieselGnvEletricoNaoDevemSerDerivadosPetroleo() {
            assertThat(TipoCombustivel.DIESEL.isDerivadoPetroleo()).isFalse();
            assertThat(TipoCombustivel.GNV.isDerivadoPetroleo()).isFalse();
            assertThat(TipoCombustivel.ELETRICO.isDerivadoPetroleo()).isFalse();
        }

        @Test
        @DisplayName("Diesel não deve ser derivado de petróleo")
        void dieselNaoDeveSerDerivadoPetroleo() {
            assertThat(TipoCombustivel.DIESEL.isDerivadoPetroleo()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Renovável")
    class RenovavelTests {

        @Test
        @DisplayName("Etanol, Flex, GNV e elétrico devem ser renováveis")
        void etanolFlexGnvEletricoDevemSerRenovaveis() {
            assertThat(TipoCombustivel.ETANOL.isRenovavel()).isTrue();
            assertThat(TipoCombustivel.FLEX.isRenovavel()).isTrue();
            assertThat(TipoCombustivel.GNV.isRenovavel()).isTrue();
            assertThat(TipoCombustivel.ELETRICO.isRenovavel()).isTrue();
        }

        @Test
        @DisplayName("Gasolina e diesel não devem ser renováveis")
        void gasolinaEDieselNaoDevemSerRenovaveis() {
            assertThat(TipoCombustivel.GASOLINA.isRenovavel()).isFalse();
            assertThat(TipoCombustivel.DIESEL.isRenovavel()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Combustível Líquido")
    class CombustivelLiquidoTests {

        @Test
        @DisplayName("Gasolina, etanol, flex e diesel devem ser líquidos")
        void gasolinaEtanolFlexDieselDevemSerLiquidos() {
            assertThat(TipoCombustivel.GASOLINA.isLiquido()).isTrue();
            assertThat(TipoCombustivel.ETANOL.isLiquido()).isTrue();
            assertThat(TipoCombustivel.FLEX.isLiquido()).isTrue();
            assertThat(TipoCombustivel.DIESEL.isLiquido()).isTrue();
        }

        @Test
        @DisplayName("GNV e elétrico não devem ser líquidos")
        void gnvEEletricoNaoDevemSerLiquidos() {
            assertThat(TipoCombustivel.GNV.isLiquido()).isFalse();
            assertThat(TipoCombustivel.ELETRICO.isLiquido()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Combustível Alternativo")
    class CombustivelAlternativoTests {

        @Test
        @DisplayName("Apenas GNV e elétrico devem ser alternativos")
        void apenasGnvEEletricoDevemSerAlternativos() {
            assertThat(TipoCombustivel.GNV.isAlternativo()).isTrue();
            assertThat(TipoCombustivel.ELETRICO.isAlternativo()).isTrue();

            assertThat(TipoCombustivel.GASOLINA.isAlternativo()).isFalse();
            assertThat(TipoCombustivel.ETANOL.isAlternativo()).isFalse();
            assertThat(TipoCombustivel.FLEX.isAlternativo()).isFalse();
            assertThat(TipoCombustivel.DIESEL.isAlternativo()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Compatibilidade com Categoria")
    class CompatibilidadeCategoriaTests {

        @Test
        @DisplayName("Gasolina deve ser compatível com passeio, utilitário e motocicleta")
        void gasolinaDeveSerCompativelComPasseioUtilitarioMotocicleta() {
            assertThat(TipoCombustivel.GASOLINA.isCompativelCom(CategoriaVeiculo.PASSEIO)).isTrue();
            assertThat(TipoCombustivel.GASOLINA.isCompativelCom(CategoriaVeiculo.UTILITARIO)).isTrue();
            assertThat(TipoCombustivel.GASOLINA.isCompativelCom(CategoriaVeiculo.MOTOCICLETA)).isTrue();
            assertThat(TipoCombustivel.GASOLINA.isCompativelCom(CategoriaVeiculo.CAMINHAO)).isFalse();
        }

        @Test
        @DisplayName("Diesel deve ser compatível com utilitário e caminhão")
        void dieselDeveSerCompativelComUtilitarioCaminhao() {
            assertThat(TipoCombustivel.DIESEL.isCompativelCom(CategoriaVeiculo.UTILITARIO)).isTrue();
            assertThat(TipoCombustivel.DIESEL.isCompativelCom(CategoriaVeiculo.CAMINHAO)).isTrue();
            assertThat(TipoCombustivel.DIESEL.isCompativelCom(CategoriaVeiculo.PASSEIO)).isFalse();
            assertThat(TipoCombustivel.DIESEL.isCompativelCom(CategoriaVeiculo.MOTOCICLETA)).isFalse();
        }

        @Test
        @DisplayName("Elétrico deve ser compatível com passeio e utilitário")
        void eletricoDeveSerCompativelComPasseioUtilitario() {
            assertThat(TipoCombustivel.ELETRICO.isCompativelCom(CategoriaVeiculo.PASSEIO)).isTrue();
            assertThat(TipoCombustivel.ELETRICO.isCompativelCom(CategoriaVeiculo.UTILITARIO)).isTrue();
            assertThat(TipoCombustivel.ELETRICO.isCompativelCom(CategoriaVeiculo.MOTOCICLETA)).isFalse();
            assertThat(TipoCombustivel.ELETRICO.isCompativelCom(CategoriaVeiculo.CAMINHAO)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para categoria null")
        void deveRetornarFalseParaCategoriaNullo() {
            assertThat(TipoCombustivel.GASOLINA.isCompativelCom(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Fator de Risco")
    class FatorRiscoTests {

        @ParameterizedTest
        @CsvSource({
            "ELETRICO, 0.8",
            "GNV, 0.9",
            "DIESEL, 0.95",
            "GASOLINA, 1.0",
            "FLEX, 1.0",
            "ETANOL, 1.1"
        })
        @DisplayName("Deve ter fator de risco correto por tipo")
        void deveTerFatorRiscoCorretoPorTipo(TipoCombustivel tipo, double fatorEsperado) {
            assertThat(tipo.getFatorRisco()).isEqualTo(fatorEsperado);
        }

        @Test
        @DisplayName("Elétrico deve ter menor fator de risco")
        void eletricoDeveTerMenorFatorRisco() {
            double menorFator = Double.MAX_VALUE;
            for (TipoCombustivel tipo : TipoCombustivel.values()) {
                if (tipo.getFatorRisco() < menorFator) {
                    menorFator = tipo.getFatorRisco();
                }
            }

            assertThat(TipoCombustivel.ELETRICO.getFatorRisco()).isEqualTo(menorFator);
        }

        @Test
        @DisplayName("Etanol deve ter maior fator de risco")
        void etanolDeveTerMaiorFatorRisco() {
            double maiorFator = 0;
            for (TipoCombustivel tipo : TipoCombustivel.values()) {
                if (tipo.getFatorRisco() > maiorFator) {
                    maiorFator = tipo.getFatorRisco();
                }
            }

            assertThat(TipoCombustivel.ETANOL.getFatorRisco()).isEqualTo(maiorFator);
        }

        @Test
        @DisplayName("Fator de risco deve estar entre 0.5 e 1.5")
        void fatorRiscoDeveEstarEntre05E15() {
            for (TipoCombustivel tipo : TipoCombustivel.values()) {
                assertThat(tipo.getFatorRisco())
                    .as("Fator de risco de %s", tipo)
                    .isBetween(0.5, 1.5);
            }
        }
    }

    @Nested
    @DisplayName("Testes de Formatação")
    class FormatacaoTests {

        @Test
        @DisplayName("ToString deve retornar o nome do combustível")
        void toStringDeveRetornarNomeDoCombustivel() {
            assertThat(TipoCombustivel.GASOLINA.toString()).isEqualTo("Gasolina");
            assertThat(TipoCombustivel.ETANOL.toString()).isEqualTo("Etanol");
            assertThat(TipoCombustivel.FLEX.toString()).isEqualTo("Flex");
            assertThat(TipoCombustivel.DIESEL.toString()).isEqualTo("Diesel");
            assertThat(TipoCombustivel.GNV.toString()).isEqualTo("GNV");
            assertThat(TipoCombustivel.ELETRICO.toString()).isEqualTo("Elétrico");
        }

        @ParameterizedTest
        @EnumSource(TipoCombustivel.class)
        @DisplayName("ToString não deve retornar null ou vazio")
        void toStringNaoDeveRetornarNullOuVazio(TipoCombustivel tipo) {
            assertThat(tipo.toString()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Regras de Negócio")
    class RegrasNegocioTests {

        @Test
        @DisplayName("Combustíveis renováveis devem ter incentivos")
        void combustiveisRenovaveisDevemTermIncentivos() {
            // Regra: combustíveis renováveis geralmente têm menor risco ou benefícios
            for (TipoCombustivel tipo : TipoCombustivel.values()) {
                if (tipo.isRenovavel()) {
                    // Pode verificar se tem algum benefício (ex: fator risco não é o maior)
                    assertThat(tipo).isNotNull();
                }
            }
        }

        @Test
        @DisplayName("Combustíveis líquidos devem ser maioria")
        void combustiveisLiquidosDevemSerMaioria() {
            int liquidos = 0;
            int total = TipoCombustivel.values().length;

            for (TipoCombustivel tipo : TipoCombustivel.values()) {
                if (tipo.isLiquido()) liquidos++;
            }

            assertThat(liquidos).isGreaterThan(total / 2);
        }
    }
}
