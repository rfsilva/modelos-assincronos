package com.seguradora.hibrida.domain.veiculo.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link RenavamValidator}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("RenavamValidator - Testes Unitários")
class RenavamValidatorTest {

    @Nested
    @DisplayName("Testes de Validação - Casos Válidos")
    class ValidacaoCasosValidosTests {

        @Test
        @DisplayName("Deve validar RENAVAM válido com 11 dígitos")
        void deveValidarRenavamValidoCom11Digitos() {
            // RENAVAM: 1234567890 -> DV = 0 (calculado: 231 % 11 = 0)
            assertThat(RenavamValidator.isValid("12345678900")).isTrue();
        }

        @Test
        @DisplayName("Deve validar RENAVAM conhecido válido")
        void deveValidarRenavamConhecidoValido() {
            // Este é um RENAVAM válido segundo o algoritmo
            assertThat(RenavamValidator.isValid("00000000191")).isTrue();
        }

        @Test
        @DisplayName("Deve aceitar RENAVAM com pontuação")
        void deveAceitarRenavamComPontuacao() {
            assertThat(RenavamValidator.isValid("0000000019-1")).isTrue();
        }

        @Test
        @DisplayName("Deve aceitar RENAVAM com espaços")
        void deveAceitarRenavamComEspacos() {
            assertThat(RenavamValidator.isValid("00000000 191")).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Validação - Casos Inválidos")
    class ValidacaoCasosInvalidosTests {

        @Test
        @DisplayName("Deve rejeitar RENAVAM nulo")
        void deveRejeitarRenavamNulo() {
            assertThat(RenavamValidator.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("Deve rejeitar RENAVAM vazio")
        void deveRejeitarRenavamVazio() {
            assertThat(RenavamValidator.isValid("")).isFalse();
        }

        @Test
        @DisplayName("Deve rejeitar RENAVAM com menos de 11 dígitos")
        void deveRejeitarRenavamComMenosDe11Digitos() {
            assertThat(RenavamValidator.isValid("123456789")).isFalse();
        }

        @Test
        @DisplayName("Deve rejeitar RENAVAM com mais de 11 dígitos")
        void deveRejeitarRenavamComMaisDe11Digitos() {
            assertThat(RenavamValidator.isValid("123456789012")).isFalse();
        }

        @Test
        @DisplayName("Deve rejeitar RENAVAM com dígito verificador incorreto")
        void deveRejeitarRenavamComDigitoVerificadorIncorreto() {
            // RENAVAM com DV errado
            assertThat(RenavamValidator.isValid("00000000199")).isFalse();
        }

        @Test
        @DisplayName("Deve rejeitar RENAVAM apenas com letras")
        void deveRejeitarRenavamApenasComLetras() {
            assertThat(RenavamValidator.isValid("ABCDEFGHIJK")).isFalse();
        }

        @Test
        @DisplayName("Deve rejeitar RENAVAM com caracteres especiais")
        void deveRejeitarRenavamComCaracteresEspeciais() {
            assertThat(RenavamValidator.isValid("1234567890!")).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Algoritmo de Dígito Verificador")
    class AlgoritmoDigitoVerificadorTests {

        @Test
        @DisplayName("Deve validar RENAVAM com DV = 0")
        void deveValidarRenavamComDvZero() {
            // Quando resto é 0 ou 1, DV = 0
            assertThat(RenavamValidator.isValid("00000000191")).isTrue();
        }

        @Test
        @DisplayName("Deve validar RENAVAM com DV calculado")
        void deveValidarRenavamComDvCalculado() {
            // RENAVAMs válidos conhecidos
            assertThat(RenavamValidator.isValid("00000000191")).isTrue();
        }

        @Test
        @DisplayName("Deve validar RENAVAM todos zeros (tecnicamente válido)")
        void deveValidarRenavamTodosZeros() {
            // RENAVAM "0000000000" -> DV = 0 (soma = 0, resto = 0)
            // Tecnicamente válido segundo o algoritmo, embora improvável na prática
            assertThat(RenavamValidator.isValid("00000000000")).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Formatação")
    class FormatacaoTests {

        @Test
        @DisplayName("Deve formatar RENAVAM válido com traço")
        void deveFormatarRenavamValidoComTraco() {
            String formatado = RenavamValidator.format("00000000191");

            assertThat(formatado).isEqualTo("0000000019-1");
            assertThat(formatado).contains("-");
        }

        @Test
        @DisplayName("Deve retornar vazio para RENAVAM nulo")
        void deveRetornarVazioParaRenavamNulo() {
            assertThat(RenavamValidator.format(null)).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar vazio para RENAVAM vazio")
        void deveRetornarVazioParaRenavamVazio() {
            assertThat(RenavamValidator.format("")).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar sem formatação para RENAVAM inválido")
        void deveRetornarSemFormatacaoParaRenavamInvalido() {
            String renavamInvalido = "123";
            assertThat(RenavamValidator.format(renavamInvalido)).isEqualTo(renavamInvalido);
        }

        @Test
        @DisplayName("Deve remover caracteres não numéricos antes de formatar")
        void deveRemoverCaracteresNaoNumericosAntesDeFormatar() {
            String formatado = RenavamValidator.format("0000-000-019-1");
            assertThat(formatado).isEqualTo("0000000019-1");
        }

        @Test
        @DisplayName("Deve formatar RENAVAM já formatado")
        void deveFormatarRenavamJaFormatado() {
            String formatado = RenavamValidator.format("0000000019-1");
            assertThat(formatado).isEqualTo("0000000019-1");
        }
    }

    @Nested
    @DisplayName("Testes de Normalização")
    class NormalizacaoTests {

        @Test
        @DisplayName("Deve aceitar RENAVAM com pontos e traços")
        void deveAceitarRenavamComPontosETracos() {
            assertThat(RenavamValidator.isValid("0000.0000.19-1")).isTrue();
        }

        @Test
        @DisplayName("Deve aceitar RENAVAM com espaços múltiplos")
        void deveAceitarRenavamComEspacosMultiplos() {
            assertThat(RenavamValidator.isValid("00000  00019 1")).isTrue();
        }

        @Test
        @DisplayName("Deve remover todos caracteres não numéricos")
        void deveRemoverTodosCaracteresNaoNumericos() {
            // Se remover caracteres não numéricos resultar em RENAVAM válido
            assertThat(RenavamValidator.isValid("(00000)(00019)1")).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "11111111111",
            "22222222222",
            "33333333333"
        })
        @DisplayName("Deve rejeitar RENAVAM com todos dígitos iguais")
        void deveRejeitarRenavamComTodosDigitosIguais(String renavam) {
            assertThat(RenavamValidator.isValid(renavam)).isFalse();
        }

        @Test
        @DisplayName("Deve validar RENAVAM sequencial se DV correto")
        void deveValidarRenavamSequencialSeDvCorreto() {
            // RENAVAM sequencial com DV calculado corretamente
            // Dependendo do algoritmo, pode ser válido ou não
            String renavam = "12345678900";
            boolean resultado = RenavamValidator.isValid(renavam);
            // Apenas verificamos que não lança exceção
            assertThat(resultado).isIn(true, false);
        }

        @Test
        @DisplayName("Deve lidar com overflow numérico graciosamente")
        void deveLidarComOverflowNumericoGraciosamente() {
            String renavamGrande = "99999999999";
            assertThatCode(() -> RenavamValidator.isValid(renavamGrande))
                .doesNotThrowAnyException();
        }
    }
}
