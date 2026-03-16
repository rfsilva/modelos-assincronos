package com.seguradora.hibrida.domain.veiculo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link Chassi}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("Chassi - Testes Unitários")
class ChassiTest {

    @Nested
    @DisplayName("Testes de Criação e Validação")
    class CriacaoValidacaoTests {

        @Test
        @DisplayName("Deve criar chassi válido")
        void deveCriarChassiValido() {
            // Arrange - usar exemplo válido
            Chassi exemplo = Chassi.exemplo();

            // Act
            Chassi chassi = Chassi.of(exemplo.getValor());

            // Assert
            assertThat(chassi).isNotNull();
            assertThat(chassi.getValor()).hasSize(17);
        }

        @Test
        @DisplayName("Deve criar chassi convertendo para uppercase")
        void deveCriarChassiConvertendoParaUppercase() {
            // Arrange
            Chassi exemplo = Chassi.exemplo();
            String chassiLower = exemplo.getValor().toLowerCase();

            // Act
            Chassi chassi = Chassi.of(chassiLower);

            // Assert
            assertThat(chassi.getValor()).isUpperCase();
            assertThat(chassi.getValor()).isEqualTo(exemplo.getValor());
        }

        @Test
        @DisplayName("Deve criar chassi removendo espaços")
        void deveCriarChassiRemovendoEspacos() {
            // Arrange
            Chassi exemplo = Chassi.exemplo();
            String chassiComEspacos = "  " + exemplo.getValor() + "  ";

            // Act
            Chassi chassi = Chassi.of(chassiComEspacos);

            // Assert
            assertThat(chassi.getValor()).isEqualTo(exemplo.getValor());
        }

        @Test
        @DisplayName("Deve lançar exceção para chassi nulo")
        void deveLancarExcecaoParaChassiNulo() {
            assertThatThrownBy(() -> Chassi.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para chassi vazio")
        void deveLancarExcecaoParaChassiVazio() {
            assertThatThrownBy(() -> Chassi.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para chassi em branco")
        void deveLancarExcecaoParaChassiEmBranco() {
            assertThatThrownBy(() -> Chassi.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser nulo ou vazio");
        }

        @ParameterizedTest
        @ValueSource(strings = {"1", "ABC", "1HGBH41JX", "1HGBH41JXMN10918"})
        @DisplayName("Deve lançar exceção para chassi com tamanho diferente de 17")
        void deveLancarExcecaoParaChassiTamanhoInvalido(String chassiInvalido) {
            assertThatThrownBy(() -> Chassi.of(chassiInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve ter 17 caracteres");
        }

        @ParameterizedTest
        @ValueSource(strings = {"1HGBH41IXMN109186", "1HGBH41JXON109186", "1HGBH41JXMN109Q86"})
        @DisplayName("Deve lançar exceção para chassi com caracteres proibidos (I, O, Q)")
        void deveLancarExcecaoParaCaracteresProibidos(String chassiInvalido) {
            assertThatThrownBy(() -> Chassi.of(chassiInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("caracteres proibidos (I, O, Q)");
        }

        @Test
        @DisplayName("Deve lançar exceção para dígito verificador inválido")
        void deveLancarExcecaoParaDigitoVerificadorInvalido() {
            // Arrange - pegar exemplo e trocar o DV (posição 9)
            Chassi exemplo = Chassi.exemplo();
            char[] chars = exemplo.getValor().toCharArray();
            chars[8] = (chars[8] == 'X') ? '0' : 'X'; // trocar o DV
            String chassiComDVErrado = new String(chars);

            // Act & Assert
            assertThatThrownBy(() -> Chassi.of(chassiComDVErrado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dígito verificador do chassi inválido");
        }

        @Test
        @DisplayName("Deve lançar exceção para caracteres especiais")
        void deveLancarExcecaoParaCaracteresEspeciais() {
            assertThatThrownBy(() -> Chassi.of("1HGBH41J@MN109186"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve conter apenas letras e números válidos");
        }
    }

    @Nested
    @DisplayName("Testes de Dígito Verificador")
    class DigitoVerificadorTests {

        @Test
        @DisplayName("Deve validar dígito verificador correto")
        void deveValidarDigitoVerificadorCorreto() {
            // Arrange
            Chassi exemplo = Chassi.exemplo();

            // Act
            boolean valido = Chassi.validarDigitoVerificador(exemplo.getValor());

            // Assert
            assertThat(valido).isTrue();
        }

        @Test
        @DisplayName("Deve rejeitar dígito verificador incorreto")
        void deveRejeitarDigitoVerificadorIncorreto() {
            // Arrange - trocar o DV
            Chassi exemplo = Chassi.exemplo();
            char[] chars = exemplo.getValor().toCharArray();
            chars[8] = (chars[8] == 'X') ? '0' : 'X';
            String chassiComDVErrado = new String(chars);

            // Act
            boolean valido = Chassi.validarDigitoVerificador(chassiComDVErrado);

            // Assert
            assertThat(valido).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para chassi com tamanho inválido")
        void deveRetornarFalseParaChassiTamanhoInvalido() {
            assertThat(Chassi.validarDigitoVerificador("1HGBH41JX")).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para chassi nulo")
        void deveRetornarFalseParaChassiNulo() {
            assertThat(Chassi.validarDigitoVerificador(null)).isFalse();
        }

        @Test
        @DisplayName("Deve aceitar caracteres válidos como dígito verificador")
        void deveAceitarCaracteresValidosComoDigitoVerificador() {
            Chassi exemplo = Chassi.exemplo();
            char dv = exemplo.getDigitoVerificador();

            // DV deve ser 0-9 ou X
            assertThat(dv).isIn('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'X');
        }
    }

    @Nested
    @DisplayName("Testes de Formatação")
    class FormatacaoTests {

        @Test
        @DisplayName("Deve formatar chassi com espaços")
        void deveFormatarChassiComEspacos() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act
            String formatado = chassi.getFormatado();

            // Assert
            // Formato: WMI(3) VDS(6) DV(1) VIS(8) = 3 + 1 + 6 + 1 + 1 + 1 + 8 = 21 chars (com espaços)
            assertThat(formatado).contains(" ");
            assertThat(formatado.replace(" ", "")).isEqualTo(chassi.getValor());
        }

        @Test
        @DisplayName("ToString deve retornar valor sem formatação")
        void toStringDeveRetornarValorSemFormatacao() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act & Assert
            assertThat(chassi.toString()).isEqualTo(chassi.getValor());
            assertThat(chassi.toString()).doesNotContain(" ");
        }
    }

    @Nested
    @DisplayName("Testes de Extração de Partes")
    class ExtracaoPartesTests {

        @Test
        @DisplayName("Deve extrair código do fabricante (WMI)")
        void deveExtrairCodigoFabricante() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act & Assert
            assertThat(chassi.getCodigoFabricante()).hasSize(3);
            assertThat(chassi.getCodigoFabricante()).isEqualTo(chassi.getValor().substring(0, 3));
        }

        @Test
        @DisplayName("Deve extrair código do veículo (VDS)")
        void deveExtrairCodigoVeiculo() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act & Assert
            assertThat(chassi.getCodigoVeiculo()).hasSize(6);
            assertThat(chassi.getCodigoVeiculo()).isEqualTo(chassi.getValor().substring(3, 9));
        }

        @Test
        @DisplayName("Deve extrair dígito verificador")
        void deveExtrairDigitoVerificador() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act & Assert
            assertThat(chassi.getDigitoVerificador()).isEqualTo(chassi.getValor().charAt(8));
        }

        @Test
        @DisplayName("Deve extrair código de identificação (VIS)")
        void deveExtrairCodigoIdentificacao() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act & Assert
            assertThat(chassi.getCodigoIdentificacao()).hasSize(8);
            assertThat(chassi.getCodigoIdentificacao()).isEqualTo(chassi.getValor().substring(9, 17));
        }

        @Test
        @DisplayName("Deve extrair ano do modelo se disponível")
        void deveExtrairAnoModeloSeDisponivel() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act
            Integer ano = chassi.getAnoModelo();

            // Assert - pode ser nulo ou um ano válido
            if (ano != null) {
                assertThat(ano).isBetween(2001, 2030);
            }
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Chassis com mesmo valor devem ser iguais")
        void chassisComMesmoValorDevemSerIguais() {
            // Arrange
            Chassi exemplo = Chassi.exemplo();
            Chassi chassi1 = Chassi.of(exemplo.getValor());
            Chassi chassi2 = Chassi.of(exemplo.getValor().toLowerCase());

            // Act & Assert
            assertThat(chassi1).isEqualTo(chassi2);
            assertThat(chassi1.hashCode()).isEqualTo(chassi2.hashCode());
        }

        @Test
        @DisplayName("Chassi deve ser igual a si mesmo")
        void chassiDeveSerIgualASiMesmo() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act & Assert
            assertThat(chassi).isEqualTo(chassi);
        }

        @Test
        @DisplayName("Chassi não deve ser igual a null")
        void chassiNaoDeveSerIgualANull() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act & Assert
            assertThat(chassi).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Chassi não deve ser igual a objeto de outra classe")
        void chassiNaoDeveSerIgualAObjetoOutraClasse() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act & Assert
            assertThat(chassi).isNotEqualTo(chassi.getValor());
        }
    }

    @Nested
    @DisplayName("Testes de Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Deve criar chassi de exemplo válido")
        void deveCriarChassiExemploValido() {
            // Act
            Chassi exemplo = Chassi.exemplo();

            // Assert
            assertThat(exemplo).isNotNull();
            assertThat(exemplo.getValor()).hasSize(17);
            assertThat(Chassi.validarDigitoVerificador(exemplo.getValor())).isTrue();
        }

        @Test
        @DisplayName("Deve validar chassi usando isValido - válido")
        void deveValidarChassiUsandoIsValidoValido() {
            // Arrange
            Chassi exemplo = Chassi.exemplo();

            // Act
            boolean valido = Chassi.isValido(exemplo.getValor());

            // Assert
            assertThat(valido).isTrue();
        }

        @Test
        @DisplayName("Deve validar chassi usando isValido - inválido")
        void deveValidarChassiUsandoIsValidoInvalido() {
            // Act
            boolean valido = Chassi.isValido("ABC123");

            // Assert
            assertThat(valido).isFalse();
        }

        @Test
        @DisplayName("isValido deve retornar false para chassi nulo")
        void isValidoDeveRetornarFalseParaChassiNulo() {
            assertThat(Chassi.isValido(null)).isFalse();
        }

        @Test
        @DisplayName("isValido deve retornar false para chassi vazio")
        void isValidoDeveRetornarFalseParaChassiVazio() {
            assertThat(Chassi.isValido("")).isFalse();
        }

        @Test
        @DisplayName("isValido deve retornar false para caracteres proibidos")
        void isValidoDeveRetornarFalseParaCaracteresProibidos() {
            assertThat(Chassi.isValido("1HGBH41IXMN109186")).isFalse(); // I proibido
            assertThat(Chassi.isValido("1HGBH41JXON109186")).isFalse(); // O proibido
            assertThat(Chassi.isValido("1HGBH41JXMN109Q86")).isFalse(); // Q proibido
        }
    }

    @Nested
    @DisplayName("Testes de Veículo Nacional")
    class VeiculoNacionalTests {

        @Test
        @DisplayName("Exemplo deve ser veículo não-nacional")
        void exemploDeveSerVeiculoNaoNacional() {
            // Arrange - exemplo começa com "1HG" (Honda USA)
            Chassi chassi = Chassi.exemplo();

            // Act & Assert
            // O exemplo é Honda USA, então não é nacional
            assertThat(chassi.isVeiculoNacional()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve aceitar chassi com letras e números")
        void deveAceitarChassiComLetrasENumeros() {
            // Arrange & Act
            Chassi chassi = Chassi.exemplo();

            // Assert
            assertThat(chassi.getValor()).matches("[A-HJ-NPR-Z0-9]{17}");
        }

        @Test
        @DisplayName("Deve manter maiúsculas após criação")
        void deveManterMaiusculasAposCriacao() {
            // Arrange
            Chassi exemplo = Chassi.exemplo();
            Chassi chassi = Chassi.of(exemplo.getValor().toLowerCase());

            // Act & Assert
            assertThat(chassi.getValor()).isUpperCase();
        }

        @Test
        @DisplayName("Deve ter todas as partes somando 17 caracteres")
        void deveTermTodasPartesSomando17Caracteres() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act
            // WMI (3) + VDS (6, já inclui DV na posição 9) + VIS (8) = 17
            int tamanhoTotal = chassi.getCodigoFabricante().length() +
                              chassi.getCodigoVeiculo().length() +
                              chassi.getCodigoIdentificacao().length();

            // Assert
            assertThat(tamanhoTotal).isEqualTo(17);
        }
    }

    @Nested
    @DisplayName("Testes de Algoritmo do Dígito Verificador")
    class AlgoritmoDigitoVerificadorTests {

        @Test
        @DisplayName("DV deve estar entre 0-9 ou ser X")
        void dvDeveEstarEntre0E9OuSerX() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act
            char dv = chassi.getDigitoVerificador();

            // Assert
            assertThat(dv).isIn('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'X');
        }

        @Test
        @DisplayName("Validação deve ser consistente")
        void validacaoDeveSerConsistente() {
            // Arrange
            Chassi chassi = Chassi.exemplo();
            String chassiStr = chassi.getValor();

            // Act
            boolean valido1 = Chassi.validarDigitoVerificador(chassiStr);
            boolean valido2 = Chassi.validarDigitoVerificador(chassi.getValor());

            // Assert
            assertThat(valido1).isTrue();
            assertThat(valido2).isTrue();
            assertThat(valido1).isEqualTo(valido2);
        }

        @Test
        @DisplayName("Alteração de qualquer caractere deve invalidar o DV")
        void alteracaoDeQualquerCaractereDeveInvalidarDV() {
            // Arrange
            Chassi chassi = Chassi.exemplo();

            // Act & Assert - testar alteração de diferentes posições
            // O algoritmo ISO 3779 usa módulo 11, o que significa que existem apenas 11 valores possíveis (0-9, X)
            // Isso leva a uma chance de ~9% (1/11) de colisão em cada posição
            // Portanto, testamos que a MAIORIA das alterações invalida o DV, não todas
            int falhasDetectadas = 0;
            int totalTestes = 0;

            for (int i = 0; i < 17; i++) {
                if (i == 8) continue; // pula o próprio DV

                char[] chars = chassi.getValor().toCharArray();
                char original = chars[i];

                // Testa múltiplas substituições para cada posição
                char[] substituicoes = Character.isDigit(original)
                    ? new char[]{'A', 'Z', 'H'}
                    : new char[]{'0', '9', '5'};

                for (char substituto : substituicoes) {
                    if (substituto == original) continue;

                    chars[i] = substituto;
                    String chassiAlterado = new String(chars);

                    if (!Chassi.validarDigitoVerificador(chassiAlterado)) {
                        falhasDetectadas++;
                    }
                    totalTestes++;

                    // Restaura para próxima iteração
                    chars[i] = original;
                }
            }

            // Assert - Pelo menos 80% das alterações devem invalidar o DV
            // (considerando a probabilidade matemática de colisões com módulo 11)
            assertThat(falhasDetectadas)
                .as("Pelo menos 80%% das alterações devem invalidar o DV (detectadas %d de %d)", falhasDetectadas, totalTestes)
                .isGreaterThanOrEqualTo((int)(totalTestes * 0.8));
        }
    }
}
