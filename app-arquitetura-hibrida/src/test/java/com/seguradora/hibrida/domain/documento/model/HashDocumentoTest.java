package com.seguradora.hibrida.domain.documento.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HashDocumento - Testes Unitários")
class HashDocumentoTest {

    @Nested
    @DisplayName("Criação e Cálculo")
    class CriacaoECalculo {

        @Test
        @DisplayName("Deve calcular hash de byte array")
        void deveCalcularHashDeByteArray() {
            byte[] conteudo = "teste de conteúdo".getBytes(StandardCharsets.UTF_8);

            HashDocumento hash = HashDocumento.calcular(conteudo);

            assertThat(hash).isNotNull();
            assertThat(hash.getAlgoritmo()).isEqualTo("SHA-256");
            assertThat(hash.getValor()).hasSize(64); // SHA-256 produz 64 caracteres hex
            assertThat(hash.getValor()).matches("^[0-9a-f]{64}$");
        }

        @Test
        @DisplayName("Deve calcular hash de string")
        void deveCalcularHashDeString() {
            String conteudo = "teste de conteúdo";

            HashDocumento hash = HashDocumento.calcular(conteudo);

            assertThat(hash).isNotNull();
            assertThat(hash.getAlgoritmo()).isEqualTo("SHA-256");
            assertThat(hash.getValor()).hasSize(64);
        }

        @Test
        @DisplayName("Deve lançar exceção para conteúdo nulo")
        void deveLancarExcecaoParaConteudoNulo() {
            assertThatThrownBy(() -> HashDocumento.calcular((byte[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Conteúdo não pode ser nulo");

            assertThatThrownBy(() -> HashDocumento.calcular((String) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Conteúdo não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção para conteúdo vazio")
        void deveLancarExcecaoParaConteudoVazio() {
            assertThatThrownBy(() -> HashDocumento.calcular(new byte[0]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Conteúdo não pode ser vazio");
        }

        @Test
        @DisplayName("Mesmo conteúdo deve gerar mesmo hash")
        void mesmoConteudoDeveGerarMesmoHash() {
            String conteudo = "conteúdo consistente";

            HashDocumento hash1 = HashDocumento.calcular(conteudo);
            HashDocumento hash2 = HashDocumento.calcular(conteudo);

            assertThat(hash1.getValor()).isEqualTo(hash2.getValor());
        }

        @Test
        @DisplayName("Conteúdos diferentes devem gerar hashes diferentes")
        void conteudosDiferentesDevemGerarHashesDiferentes() {
            HashDocumento hash1 = HashDocumento.calcular("conteúdo 1");
            HashDocumento hash2 = HashDocumento.calcular("conteúdo 2");

            assertThat(hash1.getValor()).isNotEqualTo(hash2.getValor());
        }
    }

    @Nested
    @DisplayName("Criação a partir de Hex")
    class CriacaoAPartirDeHex {

        @Test
        @DisplayName("Deve criar a partir de valor hexadecimal válido")
        void deveCriarDeHexValido() {
            String hex = "a".repeat(64);

            HashDocumento hash = HashDocumento.fromHex(hex);

            assertThat(hash).isNotNull();
            assertThat(hash.getValor()).isEqualTo(hex.toLowerCase());
            assertThat(hash.getAlgoritmo()).isEqualTo("SHA-256");
        }

        @Test
        @DisplayName("Deve converter para lowercase")
        void deveConverterParaLowercase() {
            String hexUpper = "ABCDEF0123456789".repeat(4); // 64 caracteres

            HashDocumento hash = HashDocumento.fromHex(hexUpper);

            assertThat(hash.getValor()).isEqualTo(hexUpper.toLowerCase());
        }

        @Test
        @DisplayName("Deve lançar exceção para valor nulo")
        void deveLancarExcecaoParaValorNulo() {
            assertThatThrownBy(() -> HashDocumento.fromHex(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Valor do hash não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção para valor com tamanho incorreto")
        void deveLancarExcecaoParaTamanhoIncorreto() {
            assertThatThrownBy(() -> HashDocumento.fromHex("abc123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Valor de hash inválido");

            assertThatThrownBy(() -> HashDocumento.fromHex("a".repeat(63)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Valor de hash inválido");
        }

        @Test
        @DisplayName("Deve lançar exceção para valor não hexadecimal")
        void deveLancarExcecaoParaValorNaoHex() {
            assertThatThrownBy(() -> HashDocumento.fromHex("g".repeat(64)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Valor de hash inválido");

            assertThatThrownBy(() -> HashDocumento.fromHex("xyz" + "a".repeat(61)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Valor de hash inválido");
        }
    }

    @Nested
    @DisplayName("Validação de Conteúdo")
    class ValidacaoConteudo {

        @Test
        @DisplayName("Deve validar conteúdo correto")
        void deveValidarConteudoCorreto() {
            String conteudo = "conteúdo original";
            byte[] bytes = conteudo.getBytes(StandardCharsets.UTF_8);

            HashDocumento hash = HashDocumento.calcular(bytes);

            assertThat(hash.validar(bytes)).isTrue();
        }

        @Test
        @DisplayName("Deve rejeitar conteúdo diferente")
        void deveRejeitarConteudoDiferente() {
            byte[] conteudoOriginal = "original".getBytes(StandardCharsets.UTF_8);
            byte[] conteudoModificado = "modificado".getBytes(StandardCharsets.UTF_8);

            HashDocumento hash = HashDocumento.calcular(conteudoOriginal);

            assertThat(hash.validar(conteudoModificado)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para conteúdo nulo")
        void deveRetornarFalseParaConteudoNulo() {
            HashDocumento hash = HashDocumento.calcular("teste");

            assertThat(hash.validar(null)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para conteúdo vazio")
        void deveRetornarFalseParaConteudoVazio() {
            HashDocumento hash = HashDocumento.calcular("teste");

            assertThat(hash.validar(new byte[0])).isFalse();
        }

        @Test
        @DisplayName("Método estático validar deve funcionar corretamente")
        void metodoEstaticoValidarDeveFuncionar() {
            byte[] conteudo = "teste".getBytes(StandardCharsets.UTF_8);
            HashDocumento hash = HashDocumento.calcular(conteudo);

            assertThat(HashDocumento.validar(conteudo, hash)).isTrue();
            assertThat(HashDocumento.validar("outro".getBytes(StandardCharsets.UTF_8), hash)).isFalse();
        }

        @Test
        @DisplayName("Método estático validar deve retornar false para hash nulo")
        void metodoEstaticoDeveRetornarFalseParaHashNulo() {
            byte[] conteudo = "teste".getBytes(StandardCharsets.UTF_8);

            assertThat(HashDocumento.validar(conteudo, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Verificação de Validade")
    class VerificacaoValidade {

        @Test
        @DisplayName("Hash calculado deve ser válido")
        void hashCalculadoDeveSerValido() {
            HashDocumento hash = HashDocumento.calcular("teste");

            assertThat(hash.isValido()).isTrue();
        }

        @Test
        @DisplayName("Hash criado de hex válido deve ser válido")
        void hashDeHexValidoDeveSerValido() {
            String hex = "a".repeat(64);
            HashDocumento hash = HashDocumento.fromHex(hex);

            assertThat(hash.isValido()).isTrue();
        }
    }

    @Nested
    @DisplayName("Formatação e Visualização")
    class FormatacaoVisualizacao {

        @Test
        @DisplayName("Deve retornar valor abreviado (8 caracteres)")
        void deveRetornarValorAbreviado() {
            String hex = "abcdef0123456789".repeat(4); // 64 caracteres
            HashDocumento hash = HashDocumento.fromHex(hex);

            String abreviado = hash.getValorAbreviado();

            assertThat(abreviado).isEqualTo("abcdef01...");
        }

        @Test
        @DisplayName("Deve retornar valor curto (16 caracteres)")
        void deveRetornarValorCurto() {
            String hex = "abcdef0123456789".repeat(4); // 64 caracteres
            HashDocumento hash = HashDocumento.fromHex(hex);

            String curto = hash.getValorCurto();

            assertThat(curto).isEqualTo("abcdef0123456789");
        }

        @Test
        @DisplayName("Deve retornar valor em uppercase")
        void deveRetornarValorEmUppercase() {
            String hex = "abcdef".repeat(10) + "abcd"; // 64 caracteres
            HashDocumento hash = HashDocumento.fromHex(hex);

            String upper = hash.getValorUpperCase();

            assertThat(upper).isEqualTo(hex.toUpperCase());
        }

        @Test
        @DisplayName("ToString deve retornar informação legível")
        void toStringDeveRetornarInformacaoLegivel() {
            HashDocumento hash = HashDocumento.calcular("teste");

            String toString = hash.toString();

            assertThat(toString).contains("SHA-256");
            assertThat(toString).contains(hash.getValor());
        }
    }

    @Nested
    @DisplayName("Conversão para Bytes")
    class ConversaoParaBytes {

        @Test
        @DisplayName("Deve converter hash para array de bytes")
        void deveConverterParaBytes() {
            String hex = "a".repeat(64);
            HashDocumento hash = HashDocumento.fromHex(hex);

            byte[] bytes = hash.toBytes();

            assertThat(bytes).hasSize(32); // SHA-256 = 32 bytes
        }

        @Test
        @DisplayName("Conversão deve ser reversível")
        void conversaoDeveSerReversivel() {
            HashDocumento hashOriginal = HashDocumento.calcular("teste");
            byte[] bytes = hashOriginal.toBytes();

            // Converter bytes de volta para hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : bytes) {
                hexString.append(String.format("%02x", b));
            }

            HashDocumento hashReconstruido = HashDocumento.fromHex(hexString.toString());

            assertThat(hashReconstruido).isEqualTo(hashOriginal);
        }
    }

    @Nested
    @DisplayName("Equals e HashCode")
    class EqualsEHashCode {

        @Test
        @DisplayName("Hashes com mesmo valor devem ser iguais")
        void hashesComMesmoValorDevemSerIguais() {
            String conteudo = "mesmo conteúdo";

            HashDocumento hash1 = HashDocumento.calcular(conteudo);
            HashDocumento hash2 = HashDocumento.calcular(conteudo);

            assertThat(hash1).isEqualTo(hash2);
            assertThat(hash1.hashCode()).isEqualTo(hash2.hashCode());
        }

        @Test
        @DisplayName("Hashes com valores diferentes não devem ser iguais")
        void hashesComValoresDiferentesNaoDevemSerIguais() {
            HashDocumento hash1 = HashDocumento.calcular("conteúdo 1");
            HashDocumento hash2 = HashDocumento.calcular("conteúdo 2");

            assertThat(hash1).isNotEqualTo(hash2);
            assertThat(hash1.hashCode()).isNotEqualTo(hash2.hashCode());
        }

        @Test
        @DisplayName("Hash criado de string e de hex devem ser iguais se valores iguais")
        void hashCriadoDiferentementeMasValoresIguais() {
            String conteudo = "teste";
            HashDocumento hash1 = HashDocumento.calcular(conteudo);

            // Pegar o valor do hash1 e criar hash2 a partir dele
            HashDocumento hash2 = HashDocumento.fromHex(hash1.getValor());

            assertThat(hash1).isEqualTo(hash2);
        }
    }

    @Nested
    @DisplayName("Comparação Segura")
    class ComparacaoSegura {

        @Test
        @DisplayName("Comparação segura deve retornar true para hashes iguais")
        void comparacaoSeguraDeveRetornarTrueParaIguais() {
            String conteudo = "teste seguro";
            HashDocumento hash1 = HashDocumento.calcular(conteudo);
            HashDocumento hash2 = HashDocumento.calcular(conteudo);

            assertThat(hash1.equalsSeguro(hash2)).isTrue();
        }

        @Test
        @DisplayName("Comparação segura deve retornar false para hashes diferentes")
        void comparacaoSeguraDeveRetornarFalseParaDiferentes() {
            HashDocumento hash1 = HashDocumento.calcular("conteúdo 1");
            HashDocumento hash2 = HashDocumento.calcular("conteúdo 2");

            assertThat(hash1.equalsSeguro(hash2)).isFalse();
        }

        @Test
        @DisplayName("Comparação segura deve retornar false para null")
        void comparacaoSeguraDeveRetornarFalseParaNull() {
            HashDocumento hash = HashDocumento.calcular("teste");

            assertThat(hash.equalsSeguro(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Constantes")
    class Constantes {

        @Test
        @DisplayName("Algoritmo padrão deve ser SHA-256")
        void algoritmoPadraoDeveSerSHA256() {
            assertThat(HashDocumento.ALGORITMO_PADRAO).isEqualTo("SHA-256");
        }
    }
}
