package com.seguradora.hibrida.domain.veiculo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link Placa}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("Placa - Testes Unitários")
class PlacaTest {

    @Nested
    @DisplayName("Testes de Criação e Validação")
    class CriacaoValidacaoTests {

        @Test
        @DisplayName("Deve criar placa com formato antigo válido")
        void deveCriarPlacaFormatoAntigoValida() {
            // Arrange & Act
            Placa placa = Placa.of("ABC1234");

            // Assert
            assertThat(placa).isNotNull();
            assertThat(placa.getValor()).isEqualTo("ABC1234");
            assertThat(placa.isFormatoAntigo()).isTrue();
            assertThat(placa.isMercosul()).isFalse();
        }

        @Test
        @DisplayName("Deve criar placa com formato Mercosul válido")
        void deveCriarPlacaFormatoMercosulValida() {
            // Arrange & Act
            Placa placa = Placa.of("ABC1D23");

            // Assert
            assertThat(placa).isNotNull();
            assertThat(placa.getValor()).isEqualTo("ABC1D23");
            assertThat(placa.isMercosul()).isTrue();
            assertThat(placa.isFormatoAntigo()).isFalse();
        }

        @Test
        @DisplayName("Deve criar placa ignorando case")
        void deveCriarPlacaIgnorandoCase() {
            // Arrange & Act
            Placa placa = Placa.of("abc1234");

            // Assert
            assertThat(placa.getValor()).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("Deve criar placa removendo espaços")
        void deveCriarPlacaRemovendoEspacos() {
            // Arrange & Act
            Placa placa = Placa.of("  ABC1234  ");

            // Assert
            assertThat(placa.getValor()).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("Deve criar placa removendo hífen")
        void deveCriarPlacaRemovendoHifen() {
            // Arrange & Act
            Placa placa = Placa.of("ABC-1234");

            // Assert
            assertThat(placa.getValor()).isEqualTo("ABC1234");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "A", "AB", "ABC", "ABC123", "ABC12345", "ABCD1234"})
        @DisplayName("Deve lançar exceção para placas com tamanho inválido")
        void deveLancarExcecaoParaTamanhoInvalido(String placaInvalida) {
            assertThatThrownBy(() -> Placa.of(placaInvalida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Placa deve ter 7 caracteres");
        }

        @Test
        @DisplayName("Deve lançar exceção para placa nula")
        void deveLancarExcecaoParaPlacaNula() {
            assertThatThrownBy(() -> Placa.of(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"1BC1234", "A2C1234", "AB31234"})
        @DisplayName("Deve lançar exceção quando três primeiros caracteres não são letras")
        void deveLancarExcecaoParaPrimeirosCaracteresNaoLetras(String placaInvalida) {
            assertThatThrownBy(() -> Placa.of(placaInvalida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("três primeiras posições devem ser letras");
        }

        @ParameterizedTest
        @ValueSource(strings = {"ABCA234", "ABCB234", "ABCC234"})
        @DisplayName("Deve lançar exceção para formato antigo com quarta posição sendo letra")
        void deveLancarExcecaoFormatoAntigoComLetraQuartaPosicao(String placaInvalida) {
            assertThatThrownBy(() -> Placa.of(placaInvalida))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"ABC12D3", "ABC1D2E"})
        @DisplayName("Deve lançar exceção para Mercosul com formato inválido")
        void deveLancarExcecaoMercosulFormatoInvalido(String placaInvalida) {
            assertThatThrownBy(() -> Placa.of(placaInvalida))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"AIC1234", "ABO1234", "ABQ1234", "ABC1I34", "ABC1O34", "ABC1Q34"})
        @DisplayName("Deve lançar exceção para placas com caracteres proibidos (I, O, Q)")
        void deveLancarExcecaoParaCaracteresProibidos(String placaInvalida) {
            assertThatThrownBy(() -> Placa.of(placaInvalida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode conter as letras I, O ou Q");
        }

        @ParameterizedTest
        @ValueSource(strings = {"ABC1A24", "ABC1B24", "ABC1Z24"})
        @DisplayName("Deve aceitar Mercosul com qualquer letra na quinta posição")
        void deveAceitarMercosulComQualquerLetraQuintaPosicao(String placaValida) {
            // Arrange & Act
            Placa placa = Placa.of(placaValida);

            // Assert
            assertThat(placa.isMercosul()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Formatação")
    class FormatacaoTests {

        @Test
        @DisplayName("Deve formatar placa antiga com hífen")
        void deveFormatarPlacaAntigaComHifen() {
            // Arrange
            Placa placa = Placa.of("ABC1234");

            // Act
            String formatada = placa.getFormatada();

            // Assert
            assertThat(formatada).isEqualTo("ABC-1234");
        }

        @Test
        @DisplayName("Deve formatar placa Mercosul com hífen")
        void deveFormatarPlacaMercosulComHifen() {
            // Arrange
            Placa placa = Placa.of("ABC1D23");

            // Act
            String formatada = placa.getFormatada();

            // Assert
            assertThat(formatada).isEqualTo("ABC-1D23");
        }

        @Test
        @DisplayName("ToString deve retornar valor formatado")
        void toStringDeveRetornarValorFormatado() {
            // Arrange
            Placa placa = Placa.of("ABC1234");

            // Act & Assert
            assertThat(placa.toString()).isEqualTo("ABC-1234");
        }
    }

    @Nested
    @DisplayName("Testes de Extração de Partes")
    class ExtracaoPartesTests {

        @Test
        @DisplayName("Deve extrair letras da placa antiga")
        void deveExtrairLetrasPlacaAntiga() {
            // Arrange
            Placa placa = Placa.of("ABC1234");

            // Act & Assert
            assertThat(placa.getLetras()).isEqualTo("ABC");
        }

        @Test
        @DisplayName("Deve extrair números da placa antiga")
        void deveExtrairNumerosPlacaAntiga() {
            // Arrange
            Placa placa = Placa.of("ABC1234");

            // Act & Assert
            assertThat(placa.getNumeros()).isEqualTo("1234");
        }

        @Test
        @DisplayName("Deve retornar null para letra do meio em placa antiga")
        void deveRetornarNullParaLetraMeioPlacaAntiga() {
            // Arrange
            Placa placa = Placa.of("ABC1234");

            // Act & Assert
            assertThat(placa.getLetraMeio()).isNull();
        }

        @Test
        @DisplayName("Deve extrair letra do meio da placa Mercosul")
        void deveExtrairLetraMeioPlacaMercosul() {
            // Arrange
            Placa placa = Placa.of("ABC1D23");

            // Act & Assert
            assertThat(placa.getLetraMeio()).isEqualTo("D");
        }

        @Test
        @DisplayName("Deve extrair números da placa Mercosul")
        void deveExtrairNumerosPlacaMercosul() {
            // Arrange
            Placa placa = Placa.of("ABC1D23");

            // Act & Assert
            assertThat(placa.getNumeros()).isEqualTo("123");
        }
    }

    @Nested
    @DisplayName("Testes de Conversão Mercosul")
    class ConversaoMercosulTests {

        @Test
        @DisplayName("Deve converter placa antiga para Mercosul")
        void deveConverterPlacaAntigaParaMercosul() {
            // Arrange
            Placa placa = Placa.of("ABC1234");

            // Act
            Placa mercosul = placa.converterParaMercosul();

            // Assert
            assertThat(mercosul.isMercosul()).isTrue();
            assertThat(mercosul.getLetras()).isEqualTo("ABC");
            assertThat(mercosul.getLetraMeio()).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar a mesma placa ao converter Mercosul já convertida")
        void deveRetornarMesmaPlacaAoConverterMercosulJaConvertida() {
            // Arrange
            Placa placaMercosul = Placa.of("ABC1D23");

            // Act
            Placa resultado = placaMercosul.converterParaMercosul();

            // Assert
            assertThat(resultado).isEqualTo(placaMercosul);
            assertThat(resultado.isMercosul()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Placas com mesmo valor devem ser iguais")
        void placasComMesmoValorDevemSerIguais() {
            // Arrange
            Placa placa1 = Placa.of("ABC1234");
            Placa placa2 = Placa.of("abc-1234"); // Case diferente, formatação diferente

            // Act & Assert
            assertThat(placa1).isEqualTo(placa2);
            assertThat(placa1.hashCode()).isEqualTo(placa2.hashCode());
        }

        @Test
        @DisplayName("Placas com valores diferentes não devem ser iguais")
        void placasComValoresDiferentesNaoDevemSerIguais() {
            // Arrange
            Placa placa1 = Placa.of("ABC1234");
            Placa placa2 = Placa.of("XYZ9999");

            // Act & Assert
            assertThat(placa1).isNotEqualTo(placa2);
        }

        @Test
        @DisplayName("Placa deve ser igual a si mesma")
        void placaDeveSerIgualASiMesma() {
            // Arrange
            Placa placa = Placa.of("ABC1234");

            // Act & Assert
            assertThat(placa).isEqualTo(placa);
        }

        @Test
        @DisplayName("Placa não deve ser igual a null")
        void placaNaoDeveSerIgualANull() {
            // Arrange
            Placa placa = Placa.of("ABC1234");

            // Act & Assert
            assertThat(placa).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Placa não deve ser igual a objeto de outra classe")
        void placaNaoDeveSerIgualAObjetoOutraClasse() {
            // Arrange
            Placa placa = Placa.of("ABC1234");

            // Act & Assert
            assertThat(placa).isNotEqualTo("ABC1234");
        }
    }

    @Nested
    @DisplayName("Testes de Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Deve criar placa de exemplo válida - formato antigo")
        void deveCriarPlacaExemploValidaFormatoAntigo() {
            // Act
            Placa exemplo = Placa.exemplo(false);

            // Assert
            assertThat(exemplo).isNotNull();
            assertThat(exemplo.getValor()).matches("[A-Z]{3}[0-9]{4}");
            assertThat(exemplo.isFormatoAntigo()).isTrue();
        }

        @Test
        @DisplayName("Deve criar placa de exemplo válida - formato Mercosul")
        void deveCriarPlacaExemploValidaFormatoMercosul() {
            // Act
            Placa exemplo = Placa.exemplo(true);

            // Assert
            assertThat(exemplo).isNotNull();
            assertThat(exemplo.isMercosul()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve aceitar placa com todos números sendo zero exceto último")
        void deveAceitarPlacaComZeros() {
            // Arrange & Act
            Placa placa = Placa.of("ABC0001");

            // Assert
            assertThat(placa.getValor()).isEqualTo("ABC0001");
        }

        @Test
        @DisplayName("Deve aceitar placa com todas letras AAA")
        void deveAceitarPlacaComAAA() {
            // Arrange & Act
            Placa placa = Placa.of("AAA1234");

            // Assert
            assertThat(placa.getValor()).isEqualTo("AAA1234");
        }

        @Test
        @DisplayName("Deve aceitar placa com todas letras ZZZ")
        void deveAceitarPlacaComZZZ() {
            // Arrange & Act
            Placa placa = Placa.of("ZZZ9999");

            // Assert
            assertThat(placa.getValor()).isEqualTo("ZZZ9999");
        }

        @Test
        @DisplayName("Deve aceitar placa Mercosul com letra A no meio")
        void deveAceitarPlacaMercosulComLetraANoMeio() {
            // Arrange & Act
            Placa placa = Placa.of("ABC1A23");

            // Assert
            assertThat(placa.isMercosul()).isTrue();
            assertThat(placa.getLetraMeio()).isEqualTo("A");
        }

        @Test
        @DisplayName("Deve aceitar placa Mercosul com letra Z no meio")
        void deveAceitarPlacaMercosulComLetraZNoMeio() {
            // Arrange & Act
            Placa placa = Placa.of("ABC1Z23");

            // Assert
            assertThat(placa.isMercosul()).isTrue();
            assertThat(placa.getLetraMeio()).isEqualTo("Z");
        }
    }
}
