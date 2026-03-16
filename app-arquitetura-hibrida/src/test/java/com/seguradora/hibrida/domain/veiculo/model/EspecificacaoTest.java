package com.seguradora.hibrida.domain.veiculo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link Especificacao}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("Especificacao - Testes Unitários")
class EspecificacaoTest {

    @Nested
    @DisplayName("Testes de Criação e Validação")
    class CriacaoValidacaoTests {

        @Test
        @DisplayName("Deve criar especificação completa")
        void deveCriarEspecificacaoCompleta() {
            // Act
            Especificacao spec = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600);

            // Assert
            assertThat(spec).isNotNull();
            assertThat(spec.getCor()).isEqualTo("Branco");
            assertThat(spec.getTipoCombustivel()).isEqualTo(TipoCombustivel.FLEX);
            assertThat(spec.getCategoria()).isEqualTo(CategoriaVeiculo.PASSEIO);
            assertThat(spec.getCilindrada()).isEqualTo(1600);
        }

        @Test
        @DisplayName("Deve criar especificação sem cilindrada")
        void deveCriarEspecificacaoSemCilindrada() {
            // Act
            Especificacao spec = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                                                  CategoriaVeiculo.PASSEIO);

            // Assert
            assertThat(spec.getCilindrada()).isNull();
            assertThat(spec.getCilindradaFormatada()).isEqualTo("N/I");
        }

        @Test
        @DisplayName("Deve lançar exceção para cor nula")
        void deveLancarExcecaoParaCorNula() {
            assertThatThrownBy(() -> Especificacao.of(null, TipoCombustivel.FLEX,
                                                       CategoriaVeiculo.PASSEIO, 1600))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cor não pode ser nula");
        }

        @Test
        @DisplayName("Deve lançar exceção para cor vazia")
        void deveLancarExcecaoParaCorVazia() {
            assertThatThrownBy(() -> Especificacao.of("", TipoCombustivel.FLEX,
                                                       CategoriaVeiculo.PASSEIO, 1600))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cor não pode ser nula ou vazia");
        }

        @Test
        @DisplayName("Deve lançar exceção para cor muito longa")
        void deveLancarExcecaoParaCorMuitoLonga() {
            String corLonga = "A".repeat(51);

            assertThatThrownBy(() -> Especificacao.of(corLonga, TipoCombustivel.FLEX,
                                                       CategoriaVeiculo.PASSEIO, 1600))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ter mais de 50 caracteres");
        }

        @Test
        @DisplayName("Deve lançar exceção para combustível nulo")
        void deveLancarExcecaoParaCombustivelNulo() {
            assertThatThrownBy(() -> Especificacao.of("Branco", null,
                                                       CategoriaVeiculo.PASSEIO, 1600))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("combustível não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção para categoria nula")
        void deveLancarExcecaoParaCategoriaNula() {
            assertThatThrownBy(() -> Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                       null, 1600))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Categoria não pode ser nula");
        }

        @Test
        @DisplayName("Deve lançar exceção para cilindrada zero ou negativa")
        void deveLancarExcecaoParaCilindradaZeroOuNegativa() {
            assertThatThrownBy(() -> Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                       CategoriaVeiculo.PASSEIO, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve ser maior que zero");

            assertThatThrownBy(() -> Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                       CategoriaVeiculo.PASSEIO, -1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve ser maior que zero");
        }

        @Test
        @DisplayName("Deve lançar exceção para cilindrada muito alta")
        void deveLancarExcecaoParaCilindradaMuitoAlta() {
            assertThatThrownBy(() -> Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                       CategoriaVeiculo.PASSEIO, 25000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser superior a 20000cc");
        }

        @Test
        @DisplayName("Deve remover espaços da cor")
        void deveRemoverEspacosDaCor() {
            Especificacao spec = Especificacao.of("  Branco  ", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600);

            assertThat(spec.getCor()).isEqualTo("Branco");
        }
    }

    @Nested
    @DisplayName("Testes de Compatibilidade")
    class CompatibilidadeTests {

        @Test
        @DisplayName("Deve validar compatibilidade entre combustível e categoria")
        void deveValidarCompatibilidadeEntreCombustivelECategoria() {
            // Gasolina é compatível com passeio
            assertThatCode(() -> Especificacao.of("Branco", TipoCombustivel.GASOLINA,
                                                  CategoriaVeiculo.PASSEIO, 1600))
                .doesNotThrowAnyException();

            // Diesel não é compatível com passeio
            assertThatThrownBy(() -> Especificacao.of("Branco", TipoCombustivel.DIESEL,
                                                       CategoriaVeiculo.PASSEIO, 1600))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não é compatível");
        }

        @Test
        @DisplayName("Deve validar compatibilidade de cilindrada com categoria")
        void deveValidarCompatibilidadeCilindradaComCategoria() {
            // Cilindrada alta demais para motocicleta
            assertThatThrownBy(() -> Especificacao.of("Preta", TipoCombustivel.GASOLINA,
                                                       CategoriaVeiculo.MOTOCICLETA, 3000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não é compatível");
        }

        @Test
        @DisplayName("Deve verificar se especificação é compatível")
        void deveVerificarSeEspecificacaoECompativel() {
            Especificacao spec = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600);

            assertThat(spec.isCompativel()).isTrue();
            assertThat(spec.isCombustivelCompativel()).isTrue();
        }

        @Test
        @DisplayName("Deve verificar compatibilidade com categoria específica")
        void deveVerificarCompatibilidadeComCategoriaEspecifica() {
            Especificacao spec = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600);

            assertThat(spec.isCompativel(CategoriaVeiculo.PASSEIO)).isTrue();
            assertThat(spec.isCompativel(CategoriaVeiculo.CAMINHAO)).isFalse();
            assertThat(spec.isCompativel(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Formatação de Cilindrada")
    class FormatacaoCilindradaTests {

        @ParameterizedTest
        @CsvSource(delimiter = '|', value = {
            "500|500cc",
            "999|999cc",
            "1000|1,0L",
            "1600|1,6L",
            "2000|2,0L",
            "3000|3,0L"
        })
        @DisplayName("Deve formatar cilindrada corretamente")
        void deveFormatarCilindradaCorretamente(int cilindrada, String esperado) {
            Especificacao spec = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, cilindrada);

            assertThat(spec.getCilindradaFormatada()).isEqualTo(esperado);
        }

        @Test
        @DisplayName("Deve retornar N/I para cilindrada null")
        void deveRetornarNIParaCilindradaNull() {
            Especificacao spec = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO);

            assertThat(spec.getCilindradaFormatada()).isEqualTo("N/I");
        }
    }

    @Nested
    @DisplayName("Testes de Fator de Risco")
    class FatorRiscoTests {

        @Test
        @DisplayName("Deve calcular fator de risco combinado")
        void deveCalcularFatorRiscoCombinado() {
            Especificacao spec = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600);

            double fatorRisco = spec.getFatorRisco();

            assertThat(fatorRisco).isPositive();
        }

        @Test
        @DisplayName("Cilindrada alta deve aumentar fator de risco")
        void cilindradaAltaDeveAumentarFatorRisco() {
            Especificacao spec1 = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                   CategoriaVeiculo.PASSEIO, 1000);
            Especificacao spec2 = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                   CategoriaVeiculo.PASSEIO, 3000);

            assertThat(spec2.getFatorRisco()).isGreaterThan(spec1.getFatorRisco());
        }

        @Test
        @DisplayName("Motocicleta deve ter fator de risco maior")
        void motocicletaDeveTerFatorRiscoMaior() {
            Especificacao passeio = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                     CategoriaVeiculo.PASSEIO, 1000);
            Especificacao moto = Especificacao.of("Preta", TipoCombustivel.GASOLINA,
                                                  CategoriaVeiculo.MOTOCICLETA, 800);

            assertThat(moto.getFatorRisco()).isGreaterThan(passeio.getFatorRisco());
        }
    }

    @Nested
    @DisplayName("Testes de Classificação de Veículo")
    class ClassificacaoVeiculoTests {

        @Test
        @DisplayName("Deve identificar veículo esportivo")
        void deveIdentificarVeiculoEsportivo() {
            Especificacao esportivo = Especificacao.of("Vermelho", TipoCombustivel.GASOLINA,
                                                       CategoriaVeiculo.PASSEIO, 3000);
            Especificacao normal = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                    CategoriaVeiculo.PASSEIO, 1600);

            assertThat(esportivo.isVeiculoEsportivo()).isTrue();
            assertThat(normal.isVeiculoEsportivo()).isFalse();
        }

        @Test
        @DisplayName("Deve identificar veículo econômico")
        void deveIdentificarVeiculoEconomico() {
            Especificacao economico1 = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                        CategoriaVeiculo.PASSEIO, 1000);
            Especificacao economico2 = Especificacao.of("Branco", TipoCombustivel.ELETRICO,
                                                        CategoriaVeiculo.PASSEIO);
            Especificacao naoEconomico = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                                                          CategoriaVeiculo.PASSEIO, 2000);

            assertThat(economico1.isVeiculoEconomico()).isTrue();
            assertThat(economico2.isVeiculoEconomico()).isTrue();
            assertThat(naoEconomico.isVeiculoEconomico()).isFalse();
        }

        @Test
        @DisplayName("Veículo não pode ser esportivo e econômico simultaneamente")
        void veiculoNaoPodeSerEsportivoEEconomicoSimultaneamente() {
            // Testar várias combinações
            for (CategoriaVeiculo cat : CategoriaVeiculo.values()) {
                for (TipoCombustivel comb : TipoCombustivel.values()) {
                    if (!comb.isCompativelCom(cat)) continue;

                    try {
                        Especificacao spec = Especificacao.of("Teste", comb, cat, 1500);
                        boolean ambos = spec.isVeiculoEsportivo() && spec.isVeiculoEconomico();
                        assertThat(ambos).isFalse();
                    } catch (IllegalArgumentException e) {
                        // Ignorar combinações inválidas
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Testes de Descrição")
    class DescricaoTests {

        @Test
        @DisplayName("Deve gerar descrição resumo completa")
        void deveGerarDescricaoResumoCompleta() {
            Especificacao spec = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600);

            String descricao = spec.getDescricaoResumo();

            assertThat(descricao)
                .contains("Passeio")
                .contains("Branco")
                .contains("Flex")
                .contains("1,6L");
        }

        @Test
        @DisplayName("Deve gerar descrição resumo sem cilindrada")
        void deveGerarDescricaoResumoSemCilindrada() {
            Especificacao spec = Especificacao.of("Preto", TipoCombustivel.ELETRICO,
                                                  CategoriaVeiculo.PASSEIO);

            String descricao = spec.getDescricaoResumo();

            assertThat(descricao)
                .contains("Passeio")
                .contains("Preto")
                .contains("Elétrico")
                .doesNotContain("N/I");
        }

        @Test
        @DisplayName("ToString deve retornar descrição resumo")
        void toStringDeveRetornarDescricaoResumo() {
            Especificacao spec = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600);

            assertThat(spec.toString()).isEqualTo(spec.getDescricaoResumo());
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Especificações com mesmos valores devem ser iguais")
        void especificacoesComMesmosValoresDevemSerIguais() {
            Especificacao spec1 = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                   CategoriaVeiculo.PASSEIO, 1600);
            Especificacao spec2 = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                   CategoriaVeiculo.PASSEIO, 1600);

            assertThat(spec1).isEqualTo(spec2);
            assertThat(spec1.hashCode()).isEqualTo(spec2.hashCode());
        }

        @Test
        @DisplayName("Especificações com valores diferentes não devem ser iguais")
        void especificacoesComValoresDiferentesNaoDevemSerIguais() {
            Especificacao spec1 = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                   CategoriaVeiculo.PASSEIO, 1600);
            Especificacao spec2 = Especificacao.of("Preto", TipoCombustivel.FLEX,
                                                   CategoriaVeiculo.PASSEIO, 1600);

            assertThat(spec1).isNotEqualTo(spec2);
        }

        @Test
        @DisplayName("Especificação deve ser igual a si mesma")
        void especificacaoDeveSerIgualASiMesma() {
            Especificacao spec = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600);

            assertThat(spec).isEqualTo(spec);
        }

        @Test
        @DisplayName("Especificação não deve ser igual a null")
        void especificacaoNaoDeveSerIgualANull() {
            Especificacao spec = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600);

            assertThat(spec).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Especificação não deve ser igual a objeto de outra classe")
        void especificacaoNaoDeveSerIgualAObjetoOutraClasse() {
            Especificacao spec = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600);

            assertThat(spec).isNotEqualTo("Branco Flex");
        }
    }

    @Nested
    @DisplayName("Testes de Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Deve criar exemplo válido")
        void deveCriarExemploValido() {
            Especificacao exemplo = Especificacao.exemplo();

            assertThat(exemplo).isNotNull();
            assertThat(exemplo.getCor()).isNotNull();
            assertThat(exemplo.getTipoCombustivel()).isNotNull();
            assertThat(exemplo.getCategoria()).isNotNull();
            assertThat(exemplo.isCompativel()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @ParameterizedTest
        @ValueSource(strings = {"Branco", "Preto", "Prata", "Vermelho", "Azul", "Verde"})
        @DisplayName("Deve aceitar cores comuns")
        void deveAceitarCoresComuns(String cor) {
            assertThatCode(() -> Especificacao.of(cor, TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar cor com acentos e caracteres especiais")
        void deveAceitarCorComAcentosECaracteresEspeciais() {
            assertThatCode(() -> Especificacao.of("Azul Metálico", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Cilindrada null deve ter fator de risco padrão")
        void cilindradaNullDeveTerFatorRiscoPadrao() {
            Especificacao comCilindrada = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                           CategoriaVeiculo.PASSEIO, 1600);
            Especificacao semCilindrada = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                           CategoriaVeiculo.PASSEIO);

            // Sem cilindrada deve ter fator menor (multiplicado por 1.0)
            assertThat(semCilindrada.getFatorRisco()).isLessThanOrEqualTo(comCilindrada.getFatorRisco());
        }
    }
}
