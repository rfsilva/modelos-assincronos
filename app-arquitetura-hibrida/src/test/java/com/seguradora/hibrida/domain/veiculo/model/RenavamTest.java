package com.seguradora.hibrida.domain.veiculo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link Renavam}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("Renavam - Testes Unitários")
class RenavamTest {

    @Nested
    @DisplayName("Testes de Criação e Validação")
    class CriacaoValidacaoTests {

        @Test
        @DisplayName("Deve criar RENAVAM válido")
        void deveCriarRenavamValido() {
            // Arrange - gerar um RENAVAM válido
            Renavam renavamGerado = Renavam.gerarComDigitoVerificador("1234567890");

            // Act
            Renavam renavam = Renavam.of(renavamGerado.getValor());

            // Assert
            assertThat(renavam).isNotNull();
            assertThat(renavam.getValor()).hasSize(11);
        }

        @Test
        @DisplayName("Deve criar RENAVAM removendo caracteres não numéricos")
        void deveCriarRenavamRemovendoCaracteresNaoNumericos() {
            // Arrange - gerar um RENAVAM válido e formatar
            Renavam renavamGerado = Renavam.gerarComDigitoVerificador("1234567890");
            String renavamFormatado = renavamGerado.getComMascara();

            // Act
            Renavam renavam = Renavam.of(renavamFormatado);

            // Assert
            assertThat(renavam.getValor()).isEqualTo(renavamGerado.getValor());
        }

        @Test
        @DisplayName("Deve criar RENAVAM removendo espaços")
        void deveCriarRenavamRemovendoEspacos() {
            // Arrange - gerar um RENAVAM válido
            Renavam renavamGerado = Renavam.gerarComDigitoVerificador("1234567890");
            String renavamComEspacos = "  " + renavamGerado.getValor() + "  ";

            // Act
            Renavam renavam = Renavam.of(renavamComEspacos);

            // Assert
            assertThat(renavam.getValor()).isEqualTo(renavamGerado.getValor());
        }

        @Test
        @DisplayName("Deve lançar exceção para RENAVAM nulo")
        void deveLancarExcecaoParaRenavamNulo() {
            assertThatThrownBy(() -> Renavam.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para RENAVAM vazio")
        void deveLancarExcecaoParaRenavamVazio() {
            assertThatThrownBy(() -> Renavam.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para RENAVAM em branco")
        void deveLancarExcecaoParaRenavamEmBranco() {
            assertThatThrownBy(() -> Renavam.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser nulo ou vazio");
        }

        @ParameterizedTest
        @ValueSource(strings = {"1", "123", "12345", "1234567", "123456789"})
        @DisplayName("Deve lançar exceção para RENAVAM com menos de 11 dígitos")
        void deveLancarExcecaoParaRenavamCurto(String renavamInvalido) {
            assertThatThrownBy(() -> Renavam.of(renavamInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve ter 11 dígitos");
        }

        @ParameterizedTest
        @ValueSource(strings = {"123456789012", "12345678901234"})
        @DisplayName("Deve lançar exceção para RENAVAM com mais de 11 dígitos")
        void deveLancarExcecaoParaRenavamLongo(String renavamInvalido) {
            assertThatThrownBy(() -> Renavam.of(renavamInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve ter 11 dígitos");
        }

        @ParameterizedTest
        @ValueSource(strings = {"00000000000", "11111111111", "22222222222", "99999999999"})
        @DisplayName("Deve lançar exceção para RENAVAM com todos dígitos iguais")
        void deveLancarExcecaoParaRenavamComTodosDigitosIguais(String renavamInvalido) {
            assertThatThrownBy(() -> Renavam.of(renavamInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ter todos os dígitos iguais");
        }

        @Test
        @DisplayName("Deve lançar exceção para dígito verificador inválido")
        void deveLancarExcecaoParaDigitoVerificadorInvalido() {
            // Arrange - gerar RENAVAM correto e alterar o DV
            Renavam renavamCorreto = Renavam.gerarComDigitoVerificador("1234567890");
            char dvCorreto = renavamCorreto.getDigitoVerificador();
            char dvIncorreto = (dvCorreto == '0') ? '9' : '0';
            String renavamComDVErrado = renavamCorreto.getNumeroBase() + dvIncorreto;

            // Act & Assert
            assertThatThrownBy(() -> Renavam.of(renavamComDVErrado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dígito verificador do RENAVAM inválido");
        }
    }

    @Nested
    @DisplayName("Testes de Dígito Verificador")
    class DigitoVerificadorTests {

        @Test
        @DisplayName("Deve calcular dígito verificador corretamente")
        void deveCalcularDigitoVerificadorCorretamente() {
            // Arrange
            String numeroBase = "1234567890";

            // Act
            int dv = Renavam.calcularDigitoVerificador(numeroBase);

            // Assert
            assertThat(dv).isBetween(0, 9);
        }

        @Test
        @DisplayName("Deve validar dígito verificador correto")
        void deveValidarDigitoVerificadorCorreto() {
            // Arrange
            String numeroBase = "1234567890";
            int dv = Renavam.calcularDigitoVerificador(numeroBase);
            String renavamCompleto = numeroBase + dv;

            // Act
            boolean valido = Renavam.validarDigitoVerificador(renavamCompleto);

            // Assert
            assertThat(valido).isTrue();
        }

        @Test
        @DisplayName("Deve rejeitar dígito verificador incorreto")
        void deveRejeitarDigitoVerificadorIncorreto() {
            // Arrange
            String numeroBase = "1234567890";
            int dv = Renavam.calcularDigitoVerificador(numeroBase);
            int dvIncorreto = (dv + 1) % 10; // DV diferente
            String renavamCompleto = numeroBase + dvIncorreto;

            // Act
            boolean valido = Renavam.validarDigitoVerificador(renavamCompleto);

            // Assert
            assertThat(valido).isFalse();
        }

        @Test
        @DisplayName("Deve gerar RENAVAM com dígito verificador")
        void deveGerarRenavamComDigitoVerificador() {
            // Arrange
            String numeroBase = "9876543210";

            // Act
            Renavam renavam = Renavam.gerarComDigitoVerificador(numeroBase);

            // Assert
            assertThat(renavam).isNotNull();
            assertThat(renavam.getNumeroBase()).isEqualTo(numeroBase);
            assertThat(renavam.getValor()).hasSize(11);
        }

        @Test
        @DisplayName("Deve lançar exceção ao calcular DV com número base inválido")
        void deveLancarExcecaoAoCalcularDVComNumeroBaseInvalido() {
            assertThatThrownBy(() -> Renavam.calcularDigitoVerificador("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve ter 10 dígitos");
        }

        @Test
        @DisplayName("Deve lançar exceção ao calcular DV com número base nulo")
        void deveLancarExcecaoAoCalcularDVComNumeroBaseNulo() {
            assertThatThrownBy(() -> Renavam.calcularDigitoVerificador(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve ter 10 dígitos");
        }

        @Test
        @DisplayName("Deve lançar exceção ao calcular DV com letras")
        void deveLancarExcecaoAoCalcularDVComLetras() {
            assertThatThrownBy(() -> Renavam.calcularDigitoVerificador("123456789A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve conter apenas dígitos");
        }

        @Test
        @DisplayName("DV deve ser 0 quando resto < 2")
        void dvDeveSerZeroQuandoRestoMenorQueDois() {
            // Para 1234567890, o cálculo resulta em resto 0 (< 2), então DV = 0
            String numeroBase = "1234567890";

            int dv = Renavam.calcularDigitoVerificador(numeroBase);

            // Se resto < 2, DV deve ser 0
            assertThat(dv).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Testes de Formatação")
    class FormatacaoTests {

        @Test
        @DisplayName("Deve retornar RENAVAM sem formatação em getFormatado")
        void deveRetornarRenavamSemFormatacao() {
            // Arrange
            Renavam renavam = Renavam.gerarComDigitoVerificador("1234567890");

            // Act
            String formatado = renavam.getFormatado();

            // Assert
            assertThat(formatado).isEqualTo(renavam.getValor());
        }

        @Test
        @DisplayName("Deve retornar RENAVAM com máscara")
        void deveRetornarRenavamComMascara() {
            // Arrange
            Renavam renavam = Renavam.gerarComDigitoVerificador("1234567890");

            // Act
            String comMascara = renavam.getComMascara();

            // Assert
            assertThat(comMascara).matches("\\d{5}\\.\\d{6}");
            assertThat(comMascara).hasSize(12); // 11 dígitos + 1 ponto
        }

        @Test
        @DisplayName("ToString deve retornar valor sem formatação")
        void toStringDeveRetornarValorSemFormatacao() {
            // Arrange
            Renavam renavam = Renavam.gerarComDigitoVerificador("1234567890");

            // Act & Assert
            assertThat(renavam.toString()).isEqualTo(renavam.getValor());
        }
    }

    @Nested
    @DisplayName("Testes de Extração de Partes")
    class ExtracaoPartesTests {

        @Test
        @DisplayName("Deve extrair número base")
        void deveExtrairNumeroBase() {
            // Arrange
            String numeroBase = "9876543210";
            Renavam renavam = Renavam.gerarComDigitoVerificador(numeroBase);

            // Act & Assert
            assertThat(renavam.getNumeroBase()).isEqualTo(numeroBase);
            assertThat(renavam.getNumeroBase()).hasSize(10);
        }

        @Test
        @DisplayName("Deve extrair dígito verificador")
        void deveExtrairDigitoVerificador() {
            // Arrange
            String numeroBase = "9876543210";
            Renavam renavam = Renavam.gerarComDigitoVerificador(numeroBase);
            int dvEsperado = Renavam.calcularDigitoVerificador(numeroBase);

            // Act
            char dv = renavam.getDigitoVerificador();

            // Assert
            assertThat(Character.getNumericValue(dv)).isEqualTo(dvEsperado);
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("RENAVAMs com mesmo valor devem ser iguais")
        void renavamsComMesmoValorDevemSerIguais() {
            // Arrange
            String numeroBase = "1234567890";
            Renavam renavam1 = Renavam.gerarComDigitoVerificador(numeroBase);
            Renavam renavam2 = Renavam.of(renavam1.getValor());

            // Act & Assert
            assertThat(renavam1).isEqualTo(renavam2);
            assertThat(renavam1.hashCode()).isEqualTo(renavam2.hashCode());
        }

        @Test
        @DisplayName("RENAVAMs com valores diferentes não devem ser iguais")
        void renavamsComValoresDiferentesNaoDevemSerIguais() {
            // Arrange
            Renavam renavam1 = Renavam.gerarComDigitoVerificador("1234567890");
            Renavam renavam2 = Renavam.gerarComDigitoVerificador("9876543210");

            // Act & Assert
            assertThat(renavam1).isNotEqualTo(renavam2);
        }

        @Test
        @DisplayName("RENAVAM deve ser igual a si mesmo")
        void renavamDeveSerIgualASiMesmo() {
            // Arrange
            Renavam renavam = Renavam.gerarComDigitoVerificador("1234567890");

            // Act & Assert
            assertThat(renavam).isEqualTo(renavam);
        }

        @Test
        @DisplayName("RENAVAM não deve ser igual a null")
        void renavamNaoDeveSerIgualANull() {
            // Arrange
            Renavam renavam = Renavam.gerarComDigitoVerificador("1234567890");

            // Act & Assert
            assertThat(renavam).isNotEqualTo(null);
        }

        @Test
        @DisplayName("RENAVAM não deve ser igual a objeto de outra classe")
        void renavamNaoDeveSerIgualAObjetoOutraClasse() {
            // Arrange
            Renavam renavam = Renavam.gerarComDigitoVerificador("1234567890");

            // Act & Assert
            assertThat(renavam).isNotEqualTo("12345678900");
        }
    }

    @Nested
    @DisplayName("Testes de Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Deve criar RENAVAM de exemplo válido")
        void deveCriarRenavamExemploValido() {
            // Act
            Renavam exemplo = Renavam.exemplo();

            // Assert
            assertThat(exemplo).isNotNull();
            assertThat(exemplo.getValor()).hasSize(11);
            assertThat(exemplo.getNumeroBase()).isEqualTo("1234567890");
        }

        @Test
        @DisplayName("Deve validar RENAVAM usando isValido - válido")
        void deveValidarRenavamUsandoIsValidoValido() {
            // Arrange
            Renavam renavam = Renavam.gerarComDigitoVerificador("5555555555");

            // Act
            boolean valido = Renavam.isValido(renavam.getValor());

            // Assert
            assertThat(valido).isTrue();
        }

        @Test
        @DisplayName("Deve validar RENAVAM usando isValido - inválido")
        void deveValidarRenavamUsandoIsValidoInvalido() {
            // Arrange - usar RENAVAM com DV errado
            Renavam renavamCorreto = Renavam.gerarComDigitoVerificador("1234567890");
            char dvCorreto = renavamCorreto.getDigitoVerificador();
            char dvIncorreto = (dvCorreto == '0') ? '9' : '0';
            String renavamInvalido = renavamCorreto.getNumeroBase() + dvIncorreto;

            // Act
            boolean valido = Renavam.isValido(renavamInvalido);

            // Assert
            assertThat(valido).isFalse();
        }

        @Test
        @DisplayName("isValido deve retornar false para RENAVAM nulo")
        void isValidoDeveRetornarFalseParaRenavamNulo() {
            assertThat(Renavam.isValido(null)).isFalse();
        }

        @Test
        @DisplayName("isValido deve retornar false para RENAVAM vazio")
        void isValidoDeveRetornarFalseParaRenavamVazio() {
            assertThat(Renavam.isValido("")).isFalse();
        }

        @Test
        @DisplayName("isValido deve retornar false para todos dígitos iguais")
        void isValidoDeveRetornarFalseParaTodosDigitosIguais() {
            assertThat(Renavam.isValido("11111111111")).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve aceitar RENAVAM com zeros")
        void deveAceitarRenavamComZeros() {
            // Arrange
            String numeroBase = "0000000001";

            // Act
            Renavam renavam = Renavam.gerarComDigitoVerificador(numeroBase);

            // Assert
            assertThat(renavam.getValor()).startsWith("0000000001");
        }

        @Test
        @DisplayName("Deve aceitar RENAVAM com noves")
        void deveAceitarRenavamComNoves() {
            // Arrange
            String numeroBase = "9999999998";

            // Act
            Renavam renavam = Renavam.gerarComDigitoVerificador(numeroBase);

            // Assert
            assertThat(renavam.getNumeroBase()).isEqualTo(numeroBase);
        }

        @Test
        @DisplayName("Deve aceitar RENAVAM com formatação variada")
        void deveAceitarRenavamComFormatacaoVariada() {
            // Arrange
            String numeroBase = "1234567890";
            Renavam renavam = Renavam.gerarComDigitoVerificador(numeroBase);
            String renavamComFormatacao = renavam.getComMascara();

            // Act
            Renavam renavamRecriado = Renavam.of(renavamComFormatacao);

            // Assert
            assertThat(renavamRecriado).isEqualTo(renavam);
        }

        @Test
        @DisplayName("Deve aceitar RENAVAM com espaços e pontos")
        void deveAceitarRenavamComEspacosEPontos() {
            // Arrange
            Renavam renavam = Renavam.gerarComDigitoVerificador("1234567890");
            String renavamFormatado = String.format("  %s.%s  ",
                renavam.getValor().substring(0, 5),
                renavam.getValor().substring(5));

            // Act
            Renavam renavamRecriado = Renavam.of(renavamFormatado);

            // Assert
            assertThat(renavamRecriado).isEqualTo(renavam);
        }

        @Test
        @DisplayName("Deve calcular DV corretamente para sequências específicas")
        void deveCalcularDVCorretamenteParaSequenciasEspecificas() {
            // Arrange - números base conhecidos com DV conhecido
            String[] numerosBase = {
                "0000000001",
                "1111111112",
                "9999999998"
            };

            for (String numeroBase : numerosBase) {
                // Act
                int dv = Renavam.calcularDigitoVerificador(numeroBase);
                String renavamCompleto = numeroBase + dv;

                // Assert
                assertThat(Renavam.validarDigitoVerificador(renavamCompleto))
                    .as("RENAVAM %s deve ter DV válido", renavamCompleto)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("Deve manter consistência entre geração e validação")
        void deveManterConsistenciaEntreGeracaoEValidacao() {
            // Arrange
            for (int i = 0; i < 100; i++) {
                String numeroBase = String.format("%010d", i * 12345678);

                // Act
                Renavam renavam = Renavam.gerarComDigitoVerificador(numeroBase);

                // Assert
                assertThat(Renavam.validarDigitoVerificador(renavam.getValor()))
                    .as("RENAVAM gerado %s deve ser válido", renavam.getValor())
                    .isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Testes de Algoritmo do Dígito Verificador")
    class AlgoritmoDigitoVerificadorTests {

        @Test
        @DisplayName("Deve usar sequência de multiplicadores correta (3298765432)")
        void deveUsarSequenciaMultiplicadoresCorreta() {
            // Arrange - exemplo conhecido
            // Para 1234567890, o cálculo deve ser:
            // 1*3 + 2*2 + 3*9 + 4*8 + 5*7 + 6*6 + 7*5 + 8*4 + 9*3 + 0*2
            // = 3 + 4 + 27 + 32 + 35 + 36 + 35 + 32 + 27 + 0 = 231
            // 231 % 11 = 0
            // DV = 0 (pois resto < 2)

            String numeroBase = "1234567890";

            // Act
            int dv = Renavam.calcularDigitoVerificador(numeroBase);

            // Assert
            assertThat(dv).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve retornar DV=0 quando resto < 2")
        void deveRetornarDVZeroQuandoRestoMenorQueDois() {
            // Arrange - exemplo que resulta em resto 1
            // 0000000002: 0*3+0*2+0*9+0*8+0*7+0*6+0*5+0*4+0*3+2*2 = 4
            // 4 % 11 = 4 (não < 2, então não serve)
            // Vamos usar um exemplo validado
            String numeroBase = "1234567890";

            // Act
            int dv = Renavam.calcularDigitoVerificador(numeroBase);

            // Assert - sabemos que este caso resulta em resto 0 (< 2), então DV = 0
            assertThat(dv).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve calcular DV=11-resto quando resto >= 2")
        void deveCalcularDVOnzeMenosRestoQuandoRestoMaiorIgualDois() {
            // Arrange - exemplo que resulta em resto >= 2
            String numeroBase = "9876543210";

            // Cálculo: 9*3+8*2+7*9+6*8+5*7+4*6+3*5+2*4+1*3+0*2
            // = 27+16+63+48+35+24+15+8+3+0 = 239
            // 239 % 11 = 8
            // DV = 11 - 8 = 3

            // Act
            int dv = Renavam.calcularDigitoVerificador(numeroBase);

            // Assert
            assertThat(dv).isEqualTo(3);
        }

        @Test
        @DisplayName("DV deve estar sempre entre 0 e 9")
        void dvDeveEstarSempreEntreZeroENove() {
            // Arrange & Act
            for (int i = 0; i < 1000; i++) {
                String numeroBase = String.format("%010d", i);
                int dv = Renavam.calcularDigitoVerificador(numeroBase);

                // Assert
                assertThat(dv)
                    .as("DV para base %s deve estar entre 0 e 9", numeroBase)
                    .isBetween(0, 9);
            }
        }
    }
}
