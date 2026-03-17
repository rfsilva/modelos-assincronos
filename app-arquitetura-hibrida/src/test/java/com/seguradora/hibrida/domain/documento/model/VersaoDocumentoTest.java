package com.seguradora.hibrida.domain.documento.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VersaoDocumento - Testes Unitários")
class VersaoDocumentoTest {

    @Nested
    @DisplayName("Criação de Versão Inicial")
    class CriacaoVersaoInicial {

        @Test
        @DisplayName("Deve criar versão inicial com número 1")
        void deveCriarVersaoInicialComNumero1() {
            VersaoDocumento versao = VersaoDocumento.versaoInicial("hash123", "operador1");

            assertThat(versao.getNumero()).isEqualTo(1);
            assertThat(versao.getHashAtual()).isEqualTo("hash123");
            assertThat(versao.getOperadorId()).isEqualTo("operador1");
            assertThat(versao.getHashAnterior()).isNull();
            assertThat(versao.getDescricaoAlteracao()).isEqualTo("Versão inicial");
        }

        @Test
        @DisplayName("Deve definir timestamp ao criar versão inicial")
        void deveDefinirTimestamp() {
            Instant antes = Instant.now();
            VersaoDocumento versao = VersaoDocumento.versaoInicial("hash123", "operador1");
            Instant depois = Instant.now();

            assertThat(versao.getTimestamp()).isBetween(antes, depois);
        }

        @Test
        @DisplayName("Deve lançar exceção se hash for nulo")
        void deveLancarExcecaoSeHashNulo() {
            assertThatThrownBy(() -> VersaoDocumento.versaoInicial(null, "operador1"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Hash inicial não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção se operador ID for nulo")
        void deveLancarExcecaoSeOperadorIdNulo() {
            assertThatThrownBy(() -> VersaoDocumento.versaoInicial("hash123", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Operador ID não pode ser nulo");
        }

        @Test
        @DisplayName("Versão inicial deve ser identificada corretamente")
        void versaoInicialDeveSerIdentificada() {
            VersaoDocumento versao = VersaoDocumento.versaoInicial("hash123", "operador1");

            assertThat(versao.isVersaoInicial()).isTrue();
        }
    }

    @Nested
    @DisplayName("Criação de Próxima Versão")
    class CriacaoProximaVersao {

        @Test
        @DisplayName("Deve criar próxima versão incrementando número")
        void deveCriarProximaVersaoIncrementandoNumero() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");
            VersaoDocumento v2 = v1.proximaVersao("hash2", "Atualização de conteúdo", "operador2");

            assertThat(v2.getNumero()).isEqualTo(2);
            assertThat(v2.getHashAtual()).isEqualTo("hash2");
            assertThat(v2.getHashAnterior()).isEqualTo("hash1");
            assertThat(v2.getDescricaoAlteracao()).isEqualTo("Atualização de conteúdo");
            assertThat(v2.getOperadorId()).isEqualTo("operador2");
            assertThat(v2.isVersaoInicial()).isFalse();
        }

        @Test
        @DisplayName("Deve definir timestamp ao criar próxima versão")
        void deveDefinirTimestampNaProximaVersao() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");

            Instant antes = Instant.now();
            VersaoDocumento v2 = v1.proximaVersao("hash2", "Atualização", "operador2");
            Instant depois = Instant.now();

            assertThat(v2.getTimestamp()).isBetween(antes, depois);
        }

        @Test
        @DisplayName("Deve lançar exceção se novo hash for nulo")
        void deveLancarExcecaoSeNovoHashNulo() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");

            assertThatThrownBy(() -> v1.proximaVersao(null, "Descrição", "operador2"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Hash não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção se descrição for nula")
        void deveLancarExcecaoSeDescricaoNula() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");

            assertThatThrownBy(() -> v1.proximaVersao("hash2", null, "operador2"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Descrição não pode ser nula");
        }

        @Test
        @DisplayName("Deve lançar exceção se descrição for vazia")
        void deveLancarExcecaoSeDescricaoVazia() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");

            assertThatThrownBy(() -> v1.proximaVersao("hash2", "   ", "operador2"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Descrição das alterações é obrigatória");
        }

        @Test
        @DisplayName("Deve lançar exceção se operador ID for nulo")
        void deveLancarExcecaoSeOperadorIdNuloNaProximaVersao() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");

            assertThatThrownBy(() -> v1.proximaVersao("hash2", "Descrição", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Operador ID não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção se hash não foi modificado")
        void deveLancarExcecaoSeHashNaoModificado() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");

            assertThatThrownBy(() -> v1.proximaVersao("hash1", "Descrição", "operador2"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Conteúdo não foi modificado (hash idêntico)");
        }

        @Test
        @DisplayName("Deve permitir criar múltiplas versões em sequência")
        void devePermitirCriarMultiplasVersoes() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");
            VersaoDocumento v2 = v1.proximaVersao("hash2", "Versão 2", "operador2");
            VersaoDocumento v3 = v2.proximaVersao("hash3", "Versão 3", "operador3");
            VersaoDocumento v4 = v3.proximaVersao("hash4", "Versão 4", "operador4");

            assertThat(v1.getNumero()).isEqualTo(1);
            assertThat(v2.getNumero()).isEqualTo(2);
            assertThat(v3.getNumero()).isEqualTo(3);
            assertThat(v4.getNumero()).isEqualTo(4);

            assertThat(v4.getHashAnterior()).isEqualTo(v3.getHashAtual());
            assertThat(v3.getHashAnterior()).isEqualTo(v2.getHashAtual());
            assertThat(v2.getHashAnterior()).isEqualTo(v1.getHashAtual());
        }
    }

    @Nested
    @DisplayName("Descrição e Formatação")
    class DescricaoFormatacao {

        @Test
        @DisplayName("Deve retornar descrição completa da versão inicial")
        void deveRetornarDescricaoCompletaVersaoInicial() {
            VersaoDocumento versao = VersaoDocumento.versaoInicial("hash123", "operador1");

            String descricao = versao.getDescricaoCompleta();

            assertThat(descricao)
                    .contains("v1")
                    .contains("Versão inicial")
                    .contains("operador1")
                    .doesNotContain("hash anterior");
        }

        @Test
        @DisplayName("Deve retornar descrição completa de versão posterior")
        void deveRetornarDescricaoCompletaVersaoPosterior() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash123456789", "operador1");
            VersaoDocumento v2 = v1.proximaVersao("hash987654321", "Segunda versão", "operador2");

            String descricao = v2.getDescricaoCompleta();

            assertThat(descricao)
                    .contains("v2")
                    .contains("Segunda versão")
                    .contains("operador2")
                    .contains("hash anterior")
                    .contains("hash1234"); // primeiros 8 caracteres do hash anterior
        }

        @Test
        @DisplayName("Deve retornar número formatado")
        void deveRetornarNumeroFormatado() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");
            VersaoDocumento v2 = v1.proximaVersao("hash2", "Versão 2", "operador2");

            assertThat(v1.getNumeroFormatado()).isEqualTo("v1.0");
            assertThat(v2.getNumeroFormatado()).isEqualTo("v2.0");
        }
    }

    @Nested
    @DisplayName("Validação de Hash Anterior")
    class ValidacaoHashAnterior {

        @Test
        @DisplayName("Versão inicial deve sempre retornar true para hash anterior")
        void versaoInicialDeveRetornarTrueParaHashAnterior() {
            VersaoDocumento versao = VersaoDocumento.versaoInicial("hash1", "operador1");

            assertThat(versao.hashAnteriorCorresponde("qualquer_hash")).isTrue();
            assertThat(versao.hashAnteriorCorresponde(null)).isTrue();
        }

        @Test
        @DisplayName("Deve validar hash anterior correspondente")
        void deveValidarHashAnteriorCorrespondente() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");
            VersaoDocumento v2 = v1.proximaVersao("hash2", "Versão 2", "operador2");

            assertThat(v2.hashAnteriorCorresponde("hash1")).isTrue();
        }

        @Test
        @DisplayName("Deve rejeitar hash anterior diferente")
        void deveRejeitarHashAnteriorDiferente() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");
            VersaoDocumento v2 = v1.proximaVersao("hash2", "Versão 2", "operador2");

            assertThat(v2.hashAnteriorCorresponde("hash_diferente")).isFalse();
        }
    }

    @Nested
    @DisplayName("Verificação de Autoria")
    class VerificacaoAutoria {

        @Test
        @DisplayName("Deve identificar se foi criada por operador específico")
        void deveIdentificarSeFoiCriadaPorOperador() {
            VersaoDocumento versao = VersaoDocumento.versaoInicial("hash1", "operador123");

            assertThat(versao.foiCriadaPor("operador123")).isTrue();
            assertThat(versao.foiCriadaPor("operador456")).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false se operador for nulo")
        void deveRetornarFalseSeOperadorNulo() {
            VersaoDocumento versao = VersaoDocumento.versaoInicial("hash1", "operador123");

            assertThat(versao.foiCriadaPor(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Comparação de Versões")
    class ComparacaoVersoes {

        @Test
        @DisplayName("Deve identificar versão mais recente")
        void deveIdentificarVersaoMaisRecente() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");
            VersaoDocumento v2 = v1.proximaVersao("hash2", "Versão 2", "operador2");
            VersaoDocumento v3 = v2.proximaVersao("hash3", "Versão 3", "operador3");

            assertThat(v3.isMaisRecenteQue(v2)).isTrue();
            assertThat(v3.isMaisRecenteQue(v1)).isTrue();
            assertThat(v2.isMaisRecenteQue(v1)).isTrue();

            assertThat(v1.isMaisRecenteQue(v2)).isFalse();
            assertThat(v1.isMaisRecenteQue(v3)).isFalse();
            assertThat(v2.isMaisRecenteQue(v3)).isFalse();
        }

        @Test
        @DisplayName("Comparação com null deve retornar true")
        void comparacaoComNullDeveRetornarTrue() {
            VersaoDocumento versao = VersaoDocumento.versaoInicial("hash1", "operador1");

            assertThat(versao.isMaisRecenteQue(null)).isTrue();
        }

        @Test
        @DisplayName("Versões iguais não são mais recentes")
        void versoesIguaisNaoSaoMaisRecentes() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");

            assertThat(v1.isMaisRecenteQue(v1)).isFalse();
        }

        @Test
        @DisplayName("Deve calcular diferença de versões")
        void deveCalcularDiferencaVersoes() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");
            VersaoDocumento v2 = v1.proximaVersao("hash2", "Versão 2", "operador2");
            VersaoDocumento v3 = v2.proximaVersao("hash3", "Versão 3", "operador3");
            VersaoDocumento v5 = v3.proximaVersao("hash4", "Versão 4", "operador4")
                    .proximaVersao("hash5", "Versão 5", "operador5");

            assertThat(v5.diferencaVersoes(v1)).isEqualTo(4); // v5 - v1 = 5 - 1 = 4
            assertThat(v5.diferencaVersoes(v3)).isEqualTo(2); // v5 - v3 = 5 - 3 = 2
            assertThat(v1.diferencaVersoes(v5)).isEqualTo(4); // |v1 - v5| = |1 - 5| = 4
            assertThat(v2.diferencaVersoes(v2)).isEqualTo(0); // mesma versão
        }

        @Test
        @DisplayName("Diferença com null deve retornar número da versão")
        void diferencaComNullDeveRetornarNumeroVersao() {
            VersaoDocumento v3 = VersaoDocumento.versaoInicial("hash1", "operador1")
                    .proximaVersao("hash2", "v2", "op2")
                    .proximaVersao("hash3", "v3", "op3");

            assertThat(v3.diferencaVersoes(null)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Equals e HashCode")
    class EqualsEHashCode {

        @Test
        @DisplayName("Versões com timestamps diferentes não devem ser iguais")
        void versoesComTimestampsDiferentesNaoDevemSerIguais() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");

            // Aguardar um pouco para garantir timestamp diferente
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // Ignorar
            }

            // Criar segunda versão com mesmo conteúdo (mas timestamp diferente)
            VersaoDocumento v2 = VersaoDocumento.versaoInicial("hash1", "operador1");

            // Como timestamp é diferente, não serão iguais
            assertThat(v1).isNotEqualTo(v2);
        }

        @Test
        @DisplayName("Mesma instância deve ser igual a si mesma")
        void mesmaInstanciaDeveSerIgual() {
            VersaoDocumento versao = VersaoDocumento.versaoInicial("hash1", "operador1");

            assertThat(versao).isEqualTo(versao);
            assertThat(versao.hashCode()).isEqualTo(versao.hashCode());
        }

        @Test
        @DisplayName("Versões diferentes não devem ser iguais")
        void versoesDiferentesNaoDevemSerIguais() {
            VersaoDocumento v1 = VersaoDocumento.versaoInicial("hash1", "operador1");
            VersaoDocumento v2 = v1.proximaVersao("hash2", "Versão 2", "operador2");

            assertThat(v1).isNotEqualTo(v2);
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToString {

        @Test
        @DisplayName("ToString deve conter informações principais")
        void toStringDeveConterInformacoesPrincipais() {
            VersaoDocumento versao = VersaoDocumento.versaoInicial("hash123", "operador1");

            String toString = versao.toString();

            assertThat(toString)
                    .contains("1") // número
                    .contains("Versão inicial") // descrição
                    .contains("operador1"); // operador
        }
    }
}
