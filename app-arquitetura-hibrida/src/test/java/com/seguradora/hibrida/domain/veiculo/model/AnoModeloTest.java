package com.seguradora.hibrida.domain.veiculo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link AnoModelo}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("AnoModelo - Testes Unitários")
class AnoModeloTest {

    @Nested
    @DisplayName("Testes de Criação e Validação")
    class CriacaoValidacaoTests {

        @Test
        @DisplayName("Deve criar ano modelo com anos iguais")
        void deveCriarAnoModeloComAnosIguais() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();

            // Act
            AnoModelo anoModelo = AnoModelo.of(anoAtual, anoAtual);

            // Assert
            assertThat(anoModelo).isNotNull();
            assertThat(anoModelo.getAnoFabricacao()).isEqualTo(anoAtual);
            assertThat(anoModelo.getAnoModelo()).isEqualTo(anoAtual);
        }

        @Test
        @DisplayName("Deve criar ano modelo com um único parâmetro")
        void deveCriarAnoModeloComUmParametro() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();

            // Act
            AnoModelo anoModelo = AnoModelo.of(anoAtual);

            // Assert
            assertThat(anoModelo.getAnoFabricacao()).isEqualTo(anoAtual);
            assertThat(anoModelo.getAnoModelo()).isEqualTo(anoAtual);
            assertThat(anoModelo.isAnoProximo()).isFalse();
        }

        @Test
        @DisplayName("Deve criar ano modelo com modelo 1 ano à frente")
        void deveCriarAnoModeloComModelo1AnoAFrente() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();

            // Act
            AnoModelo anoModelo = AnoModelo.of(anoAtual, anoAtual + 1);

            // Assert
            assertThat(anoModelo.getAnoFabricacao()).isEqualTo(anoAtual);
            assertThat(anoModelo.getAnoModelo()).isEqualTo(anoAtual + 1);
            assertThat(anoModelo.isAnoProximo()).isTrue();
        }

        @Test
        @DisplayName("Deve lançar exceção para ano fabricação anterior a 1900")
        void deveLancarExcecaoParaAnoFabricacaoAnterior1900() {
            assertThatThrownBy(() -> AnoModelo.of(1899, 1899))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser anterior a 1900");
        }

        @Test
        @DisplayName("Deve lançar exceção para ano fabricação muito futuro")
        void deveLancarExcecaoParaAnoFabricacaoMuitoFuturo() {
            // Arrange
            int anoMuitoFuturo = LocalDate.now().getYear() + 10;

            // Act & Assert
            assertThatThrownBy(() -> AnoModelo.of(anoMuitoFuturo, anoMuitoFuturo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser superior a");
        }

        @Test
        @DisplayName("Deve lançar exceção para ano modelo anterior a 1900")
        void deveLancarExcecaoParaAnoModeloAnterior1900() {
            assertThatThrownBy(() -> AnoModelo.of(1900, 1899))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser anterior a 1900");
        }

        @Test
        @DisplayName("Deve lançar exceção para ano modelo mais de 2 anos no futuro")
        void deveLancarExcecaoParaAnoModeloMuitoFuturo() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();

            // Act & Assert
            assertThatThrownBy(() -> AnoModelo.of(anoAtual, anoAtual + 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser superior a");
        }

        @Test
        @DisplayName("Deve lançar exceção para ano modelo anterior ao de fabricação")
        void deveLancarExcecaoParaAnoModeloAnteriorFabricacao() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();

            // Act & Assert
            assertThatThrownBy(() -> AnoModelo.of(anoAtual, anoAtual - 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser anterior ao ano de fabricação");
        }

        @Test
        @DisplayName("Deve lançar exceção para ano modelo mais de 1 ano posterior")
        void deveLancarExcecaoParaAnoModeloMaisDe1AnoPosterior() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();

            // Act & Assert
            assertThatThrownBy(() -> AnoModelo.of(anoAtual - 2, anoAtual))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser mais de 1 ano posterior");
        }
    }

    @Nested
    @DisplayName("Testes de Formatação")
    class FormatacaoTests {

        @Test
        @DisplayName("Deve formatar com anos iguais mostrando apenas um ano")
        void deveFormatarComAnosIguaisMostrandoApenasUmAno() {
            // Arrange
            int ano = 2020;
            AnoModelo anoModelo = AnoModelo.of(ano);

            // Act
            String formatado = anoModelo.getFormatado();

            // Assert
            assertThat(formatado).isEqualTo("2020");
        }

        @Test
        @DisplayName("Deve formatar com anos diferentes mostrando fabricação/modelo")
        void deveFormatarComAnosDiferentesMostrandoFabricacaoModelo() {
            // Arrange
            AnoModelo anoModelo = AnoModelo.of(2020, 2021);

            // Act
            String formatado = anoModelo.getFormatado();

            // Assert
            assertThat(formatado).isEqualTo("2020/2021");
        }

        @Test
        @DisplayName("ToString deve retornar valor formatado")
        void toStringDeveRetornarValorFormatado() {
            // Arrange
            AnoModelo anoModelo = AnoModelo.of(2020, 2021);

            // Act & Assert
            assertThat(anoModelo.toString()).isEqualTo("2020/2021");
        }
    }

    @Nested
    @DisplayName("Testes de Cálculo de Idade")
    class CalculoIdadeTests {

        @Test
        @DisplayName("Deve calcular idade corretamente para veículo atual")
        void deveCalcularIdadeCorretamenteParaVeiculoAtual() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo anoModelo = AnoModelo.of(anoAtual);

            // Act
            int idade = anoModelo.getIdade();

            // Assert
            assertThat(idade).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve calcular idade corretamente para veículo de 5 anos")
        void deveCalcularIdadeCorretamenteParaVeiculoDe5Anos() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo anoModelo = AnoModelo.of(anoAtual - 5);

            // Act
            int idade = anoModelo.getIdade();

            // Assert
            assertThat(idade).isEqualTo(5);
        }

        @Test
        @DisplayName("Deve calcular idade corretamente para veículo antigo")
        void deveCalcularIdadeCorretamenteParaVeiculoAntigo() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo anoModelo = AnoModelo.of(anoAtual - 25);

            // Act
            int idade = anoModelo.getIdade();

            // Assert
            assertThat(idade).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("Testes de Categoria de Veículo")
    class CategoriaVeiculoTests {

        @Test
        @DisplayName("Deve identificar veículo novo (0-1 ano)")
        void deveIdentificarVeiculoNovo() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo novo1 = AnoModelo.of(anoAtual);
            AnoModelo novo2 = AnoModelo.of(anoAtual - 1);

            // Act & Assert
            assertThat(novo1.isVeiculoNovo()).isTrue();
            assertThat(novo2.isVeiculoNovo()).isTrue();
            assertThat(novo1.getCategoriaIdade()).isEqualTo("Novo");
            assertThat(novo2.getCategoriaIdade()).isEqualTo("Novo");
        }

        @Test
        @DisplayName("Deve identificar veículo seminovo (2-5 anos)")
        void deveIdentificarVeiculoSeminovo() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo seminovo1 = AnoModelo.of(anoAtual - 2);
            AnoModelo seminovo2 = AnoModelo.of(anoAtual - 5);

            // Act & Assert
            assertThat(seminovo1.isVeiculoSeminovo()).isTrue();
            assertThat(seminovo2.isVeiculoSeminovo()).isTrue();
            assertThat(seminovo1.getCategoriaIdade()).isEqualTo("Seminovo");
            assertThat(seminovo2.getCategoriaIdade()).isEqualTo("Seminovo");
        }

        @Test
        @DisplayName("Deve identificar veículo usado (6-20 anos)")
        void deveIdentificarVeiculoUsado() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo usado1 = AnoModelo.of(anoAtual - 6);
            AnoModelo usado2 = AnoModelo.of(anoAtual - 15);

            // Act & Assert
            assertThat(usado1.isVeiculoUsado()).isTrue();
            assertThat(usado2.isVeiculoUsado()).isTrue();
            assertThat(usado1.getCategoriaIdade()).isEqualTo("Usado");
            assertThat(usado2.getCategoriaIdade()).isEqualTo("Usado");
        }

        @Test
        @DisplayName("Deve identificar veículo antigo (mais de 20 anos)")
        void deveIdentificarVeiculoAntigo() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo antigo = AnoModelo.of(anoAtual - 25);

            // Act & Assert
            assertThat(antigo.isVeiculoAntigo()).isTrue();
            assertThat(antigo.getCategoriaIdade()).isEqualTo("Antigo");
        }

        @Test
        @DisplayName("Categorias devem ser mutuamente exclusivas")
        void categoriasDevemSerMutuamenteExclusivas() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();

            // Act & Assert
            for (int idade = 0; idade <= 30; idade++) {
                AnoModelo anoModelo = AnoModelo.of(anoAtual - idade);

                int categorias = 0;
                if (anoModelo.isVeiculoNovo()) categorias++;
                if (anoModelo.isVeiculoSeminovo()) categorias++;
                if (anoModelo.isVeiculoUsado() && !anoModelo.isVeiculoAntigo()) categorias++;
                if (anoModelo.isVeiculoAntigo()) categorias++;

                assertThat(categorias)
                    .as("Veículo de idade %d deve ter exatamente uma categoria", idade)
                    .isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("Testes de Fator de Depreciação")
    class FatorDepreciacaoTests {

        @Test
        @DisplayName("Veículo novo não deve ter depreciação")
        void veiculoNovoNaoDeveTermDepreciacao() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo anoModelo = AnoModelo.of(anoAtual);

            // Act
            double fator = anoModelo.getFatorDepreciacao();

            // Assert
            assertThat(fator).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Veículo de 1 ano deve ter 15% de depreciação")
        void veiculoDe1AnoDeveTer15PorcentoDepreciacao() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo anoModelo = AnoModelo.of(anoAtual - 1);

            // Act
            double fator = anoModelo.getFatorDepreciacao();

            // Assert
            assertThat(fator).isEqualTo(0.85);
        }

        @ParameterizedTest
        @CsvSource({
            "2, 0.70",
            "3, 0.70",
            "4, 0.55",
            "5, 0.55",
            "10, 0.40"
        })
        @DisplayName("Deve calcular fator de depreciação corretamente por idade")
        void deveCalcularFatorDepreciacaoCorretamentePorIdade(int idade, double fatorEsperado) {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo anoModelo = AnoModelo.of(anoAtual - idade);

            // Act
            double fator = anoModelo.getFatorDepreciacao();

            // Assert
            assertThat(fator).isEqualTo(fatorEsperado);
        }

        @Test
        @DisplayName("Fator de depreciação deve ter mínimo de 15%")
        void fatorDepreciacaoDeveTerMinimo15Porcento() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo anoModelo = AnoModelo.of(1990); // Veículo muito antigo

            // Act
            double fator = anoModelo.getFatorDepreciacao();

            // Assert
            assertThat(fator).isGreaterThanOrEqualTo(0.15);
        }

        @Test
        @DisplayName("Fator de depreciação deve estar entre 0 e 1")
        void fatorDepreciacaoDeveEstarEntre0E1() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();

            // Act & Assert
            for (int idade = 0; idade <= 50; idade++) {
                AnoModelo anoModelo = AnoModelo.of(anoAtual - idade);
                double fator = anoModelo.getFatorDepreciacao();

                assertThat(fator)
                    .as("Fator de depreciação para idade %d", idade)
                    .isBetween(0.0, 1.0);
            }
        }
    }

    @Nested
    @DisplayName("Testes de Aceitação para Seguro")
    class AceitacaoSeguroTests {

        @Test
        @DisplayName("Deve aceitar veículo dentro da idade máxima")
        void deveAceitarVeiculoDentroIdadeMaxima() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo anoModelo = AnoModelo.of(anoAtual - 5);

            // Act
            boolean aceito = anoModelo.isAceitoParaSeguro(10);

            // Assert
            assertThat(aceito).isTrue();
        }

        @Test
        @DisplayName("Deve rejeitar veículo acima da idade máxima")
        void deveRejeitarVeiculoAcimaIdadeMaxima() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo anoModelo = AnoModelo.of(anoAtual - 15);

            // Act
            boolean aceito = anoModelo.isAceitoParaSeguro(10);

            // Assert
            assertThat(aceito).isFalse();
        }

        @Test
        @DisplayName("Deve aceitar veículo exatamente na idade máxima")
        void deveAceitarVeiculoExatamenteNaIdadeMaxima() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();
            AnoModelo anoModelo = AnoModelo.of(anoAtual - 10);

            // Act
            boolean aceito = anoModelo.isAceitoParaSeguro(10);

            // Assert
            assertThat(aceito).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Anos modelo com mesmos valores devem ser iguais")
        void anosModeloComMesmosValoresDevemSerIguais() {
            // Arrange
            AnoModelo ano1 = AnoModelo.of(2020, 2021);
            AnoModelo ano2 = AnoModelo.of(2020, 2021);

            // Act & Assert
            assertThat(ano1).isEqualTo(ano2);
            assertThat(ano1.hashCode()).isEqualTo(ano2.hashCode());
        }

        @Test
        @DisplayName("Anos modelo com valores diferentes não devem ser iguais")
        void anosModeloComValoresDiferentesNaoDevemSerIguais() {
            // Arrange
            AnoModelo ano1 = AnoModelo.of(2020, 2021);
            AnoModelo ano2 = AnoModelo.of(2020, 2020);

            // Act & Assert
            assertThat(ano1).isNotEqualTo(ano2);
        }

        @Test
        @DisplayName("Ano modelo deve ser igual a si mesmo")
        void anoModeloDeveSerIgualASiMesmo() {
            // Arrange
            AnoModelo ano = AnoModelo.of(2020, 2021);

            // Act & Assert
            assertThat(ano).isEqualTo(ano);
        }

        @Test
        @DisplayName("Ano modelo não deve ser igual a null")
        void anoModeloNaoDeveSerIgualANull() {
            // Arrange
            AnoModelo ano = AnoModelo.of(2020);

            // Act & Assert
            assertThat(ano).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Ano modelo não deve ser igual a objeto de outra classe")
        void anoModeloNaoDeveSerIgualAObjetoOutraClasse() {
            // Arrange
            AnoModelo ano = AnoModelo.of(2020);

            // Act & Assert
            assertThat(ano).isNotEqualTo("2020");
        }
    }

    @Nested
    @DisplayName("Testes de Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Deve criar exemplo válido")
        void deveCriarExemploValido() {
            // Act
            AnoModelo exemplo = AnoModelo.exemplo();

            // Assert
            assertThat(exemplo).isNotNull();
            assertThat(exemplo.getAnoFabricacao()).isPositive();
            assertThat(exemplo.getAnoModelo()).isPositive();
        }

        @Test
        @DisplayName("Deve validar combinação válida usando isValido")
        void deveValidarCombinacaoValidaUsandoIsValido() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();

            // Act
            boolean valido = AnoModelo.isValido(anoAtual, anoAtual);

            // Assert
            assertThat(valido).isTrue();
        }

        @Test
        @DisplayName("Deve invalidar combinação inválida usando isValido")
        void deveInvalidarCombinacaoInvalidaUsandoIsValido() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();

            // Act
            boolean valido = AnoModelo.isValido(anoAtual, anoAtual - 1);

            // Assert
            assertThat(valido).isFalse();
        }

        @Test
        @DisplayName("isValido deve retornar false para anos muito antigos")
        void isValidoDeveRetornarFalseParaAnosMuitoAntigos() {
            assertThat(AnoModelo.isValido(1800, 1800)).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve aceitar ano de fabricação 1900")
        void deveAceitarAnoFabricacao1900() {
            // Act & Assert
            assertThatCode(() -> AnoModelo.of(1900, 1900))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar ano fabricação ano atual + 1")
        void deveAceitarAnoFabricacaoAnoAtualMais1() {
            // Arrange
            int anoProximoAno = LocalDate.now().getYear() + 1;

            // Act & Assert
            assertThatCode(() -> AnoModelo.of(anoProximoAno, anoProximoAno))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar ano modelo até 2 anos no futuro")
        void deveAceitarAnoModeloAte2AnosNoFuturo() {
            // Arrange
            int anoAtual = LocalDate.now().getYear();

            // Act & Assert
            assertThatCode(() -> AnoModelo.of(anoAtual + 1, anoAtual + 2))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("isAnoProximo deve ser false quando anos são iguais")
        void isAnoProximoDeveSerFalseQuandoAnosSaoIguais() {
            // Arrange
            AnoModelo ano = AnoModelo.of(2020);

            // Act & Assert
            assertThat(ano.isAnoProximo()).isFalse();
        }

        @Test
        @DisplayName("isAnoProximo deve ser true quando modelo é posterior")
        void isAnoProximoDeveSerTrueQuandoModeloPosterior() {
            // Arrange
            AnoModelo ano = AnoModelo.of(2020, 2021);

            // Act & Assert
            assertThat(ano.isAnoProximo()).isTrue();
        }
    }
}
