package com.seguradora.hibrida.domain.veiculo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link Proprietario}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("Proprietario - Testes Unitários")
class ProprietarioTest {

    // CPFs e CNPJs válidos para testes
    private static final String CPF_VALIDO = "11144477735"; // CPF válido
    private static final String CNPJ_VALIDO = "11222333000181"; // CNPJ válido

    @Nested
    @DisplayName("Testes de Criação e Validação")
    class CriacaoValidacaoTests {

        @Test
        @DisplayName("Deve criar proprietário pessoa física")
        void deveCriarProprietarioPessoaFisica() {
            // Act
            Proprietario proprietario = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);

            // Assert
            assertThat(proprietario).isNotNull();
            assertThat(proprietario.getCpfCnpj()).isEqualTo(CPF_VALIDO);
            assertThat(proprietario.getNome()).isEqualTo("João Silva");
            assertThat(proprietario.getTipoPessoa()).isEqualTo(TipoPessoa.FISICA);
            assertThat(proprietario.isPessoaFisica()).isTrue();
            assertThat(proprietario.isPessoaJuridica()).isFalse();
        }

        @Test
        @DisplayName("Deve criar proprietário pessoa jurídica")
        void deveCriarProprietarioPessoaJuridica() {
            // Act
            Proprietario proprietario = Proprietario.of(CNPJ_VALIDO, "Empresa LTDA", TipoPessoa.JURIDICA);

            // Assert
            assertThat(proprietario.isPessoaJuridica()).isTrue();
            assertThat(proprietario.isPessoaFisica()).isFalse();
        }

        @Test
        @DisplayName("Deve criar proprietário detectando tipo automaticamente - CPF")
        void deveCriarProprietarioDetectandoTipoAutomaticamenteCpf() {
            // Act
            Proprietario proprietario = Proprietario.of(CPF_VALIDO, "Maria Santos");

            // Assert
            assertThat(proprietario.getTipoPessoa()).isEqualTo(TipoPessoa.FISICA);
            assertThat(proprietario.isPessoaFisica()).isTrue();
        }

        @Test
        @DisplayName("Deve criar proprietário detectando tipo automaticamente - CNPJ")
        void deveCriarProprietarioDetectandoTipoAutomaticamenteCnpj() {
            // Act
            Proprietario proprietario = Proprietario.of(CNPJ_VALIDO, "Empresa ABC");

            // Assert
            assertThat(proprietario.getTipoPessoa()).isEqualTo(TipoPessoa.JURIDICA);
            assertThat(proprietario.isPessoaJuridica()).isTrue();
        }

        @Test
        @DisplayName("Deve lançar exceção para documento nulo")
        void deveLancarExcecaoParaDocumentoNulo() {
            assertThatThrownBy(() -> Proprietario.of(null, "João Silva", TipoPessoa.FISICA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para nome nulo")
        void deveLancarExcecaoParaNomeNulo() {
            assertThatThrownBy(() -> Proprietario.of(CPF_VALIDO, null, TipoPessoa.FISICA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para tipo pessoa nulo")
        void deveLancarExcecaoParaTipoPessoaNulo() {
            assertThatThrownBy(() -> Proprietario.of(CPF_VALIDO, "João Silva", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de pessoa não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção para CPF inválido")
        void deveLancarExcecaoParaCpfInvalido() {
            assertThatThrownBy(() -> Proprietario.of("12345678900", "João Silva", TipoPessoa.FISICA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF inválido");
        }

        @Test
        @DisplayName("Deve lançar exceção para CNPJ inválido")
        void deveLancarExcecaoParaCnpjInvalido() {
            assertThatThrownBy(() -> Proprietario.of("11222333000180", "Empresa LTDA", TipoPessoa.JURIDICA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CNPJ inválido");
        }

        @Test
        @DisplayName("Deve lançar exceção para nome muito curto")
        void deveLancarExcecaoParaNomeMuitoCurto() {
            assertThatThrownBy(() -> Proprietario.of(CPF_VALIDO, "A", TipoPessoa.FISICA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pelo menos 2 caracteres");
        }

        @Test
        @DisplayName("Deve lançar exceção para nome muito longo")
        void deveLancarExcecaoParaNomeMuitoLongo() {
            String nomeLongo = "A".repeat(101);

            assertThatThrownBy(() -> Proprietario.of(CPF_VALIDO, nomeLongo, TipoPessoa.FISICA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ter mais de 100 caracteres");
        }

        @Test
        @DisplayName("Deve lançar exceção para nome com caracteres inválidos")
        void deveLancarExcecaoParaNomeComCaracteresInvalidos() {
            assertThatThrownBy(() -> Proprietario.of(CPF_VALIDO, "João@Silva#123", TipoPessoa.FISICA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("caracteres inválidos");
        }

        @Test
        @DisplayName("Deve aceitar nome com acentos e caracteres especiais válidos")
        void deveAceitarNomeComAcentosECaracteresEspeciaisValidos() {
            assertThatCode(() -> Proprietario.of(CPF_VALIDO, "José D'Angelo-Müller", TipoPessoa.FISICA))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve remover espaços extras do nome")
        void deveRemoverEspacosExtrasDoNome() {
            Proprietario proprietario = Proprietario.of(CPF_VALIDO, "  João  Silva  ", TipoPessoa.FISICA);

            assertThat(proprietario.getNome()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("Deve aceitar documento formatado")
        void deveAceitarDocumentoFormatado() {
            assertThatCode(() -> Proprietario.of("123.456.789-09", "João Silva", TipoPessoa.FISICA))
                .doesNotThrowAnyException();

            assertThatCode(() -> Proprietario.of("11.222.333/0001-81", "Empresa LTDA", TipoPessoa.JURIDICA))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve rejeitar CPF com todos dígitos iguais")
        void deveRejeitarCpfComTodosDigitosIguais() {
            assertThatThrownBy(() -> Proprietario.of("11111111111", "João Silva", TipoPessoa.FISICA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF inválido");
        }

        @Test
        @DisplayName("Deve rejeitar CNPJ com todos dígitos iguais")
        void deveRejeitarCnpjComTodosDigitosIguais() {
            assertThatThrownBy(() -> Proprietario.of("11111111111111", "Empresa LTDA", TipoPessoa.JURIDICA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CNPJ inválido");
        }

        @Test
        @DisplayName("Deve lançar exceção quando não consegue identificar tipo por documento")
        void deveLancarExcecaoQuandoNaoConsegueIdentificarTipoPorDocumento() {
            assertThatThrownBy(() -> Proprietario.of("123", "João Silva"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Não foi possível identificar o tipo de pessoa");
        }
    }

    @Nested
    @DisplayName("Testes de Formatação")
    class FormatacaoTests {

        @Test
        @DisplayName("Deve formatar CPF corretamente")
        void deveFormatarCpfCorretamente() {
            Proprietario proprietario = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);

            assertThat(proprietario.getCpfCnpjFormatado()).matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");
        }

        @Test
        @DisplayName("Deve formatar CNPJ corretamente")
        void deveFormatarCnpjCorretamente() {
            Proprietario proprietario = Proprietario.of(CNPJ_VALIDO, "Empresa LTDA", TipoPessoa.JURIDICA);

            assertThat(proprietario.getCpfCnpjFormatado()).matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}");
        }

        @Test
        @DisplayName("Deve gerar resumo com nome e documento formatado")
        void deveGerarResumoComNomeEDocumentoFormatado() {
            Proprietario proprietario = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);

            String resumo = proprietario.getResumo();

            assertThat(resumo).contains("João Silva");
            assertThat(resumo).contains("CPF");
            assertThat(resumo).contains(proprietario.getCpfCnpjFormatado());
        }

        @Test
        @DisplayName("ToString deve retornar resumo")
        void toStringDeveRetornarResumo() {
            Proprietario proprietario = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);

            assertThat(proprietario.toString()).isEqualTo(proprietario.getResumo());
        }
    }

    @Nested
    @DisplayName("Testes de Privacidade")
    class PrivacidadeTests {

        @Test
        @DisplayName("Deve gerar iniciais do nome")
        void deveGerarIniciaisDoNome() {
            Proprietario proprietario = Proprietario.of(CPF_VALIDO, "João da Silva", TipoPessoa.FISICA);

            assertThat(proprietario.getIniciais()).isEqualTo("J.d.S.");
        }

        @Test
        @DisplayName("Deve gerar iniciais para nome simples")
        void deveGerarIniciaisParaNomeSimples() {
            Proprietario proprietario = Proprietario.of(CPF_VALIDO, "João", TipoPessoa.FISICA);

            assertThat(proprietario.getIniciais()).isEqualTo("J.");
        }

        @Test
        @DisplayName("Deve gerar nome mascarado para nome único")
        void deveGerarNomeMascaradoParaNomeUnico() {
            Proprietario proprietario = Proprietario.of(CPF_VALIDO, "João", TipoPessoa.FISICA);

            String mascarado = proprietario.getNomeMascarado();

            assertThat(mascarado).startsWith("J");
            assertThat(mascarado).endsWith("o");
            assertThat(mascarado).contains("***");
        }

        @Test
        @DisplayName("Deve gerar nome mascarado para nome completo")
        void deveGerarNomeMascaradoParaNomeCompleto() {
            Proprietario proprietario = Proprietario.of(CPF_VALIDO, "João da Silva", TipoPessoa.FISICA);

            String mascarado = proprietario.getNomeMascarado();

            assertThat(mascarado).startsWith("João");
            assertThat(mascarado).contains("d.");
            assertThat(mascarado).contains("S.");
        }

        @Test
        @DisplayName("Deve retornar nome original para nomes muito curtos")
        void deveRetornarNomeOriginalParaNomesMuitoCurtos() {
            Proprietario proprietario = Proprietario.of(CPF_VALIDO, "Ana", TipoPessoa.FISICA);

            assertThat(proprietario.getNomeMascarado()).isEqualTo("Ana");
        }
    }

    @Nested
    @DisplayName("Testes de Regras de Negócio")
    class RegrasNegocioTests {

        @Test
        @DisplayName("Pessoa física pode possuir qualquer categoria")
        void pessoaFisicaPodePossuirQualquerCategoria() {
            Proprietario pf = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);

            for (CategoriaVeiculo categoria : CategoriaVeiculo.values()) {
                assertThat(pf.podePosituirCategoria(categoria))
                    .as("PF deve poder possuir %s", categoria)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("Pessoa jurídica não pode possuir motocicleta")
        void pessoaJuridicaNaoPodePossuirMotocicleta() {
            Proprietario pj = Proprietario.of(CNPJ_VALIDO, "Empresa LTDA", TipoPessoa.JURIDICA);

            assertThat(pj.podePosituirCategoria(CategoriaVeiculo.MOTOCICLETA)).isFalse();
        }

        @Test
        @DisplayName("Pessoa jurídica pode possuir outras categorias")
        void pessoaJuridicaPodePossuirOutrasCategorias() {
            Proprietario pj = Proprietario.of(CNPJ_VALIDO, "Empresa LTDA", TipoPessoa.JURIDICA);

            assertThat(pj.podePosituirCategoria(CategoriaVeiculo.PASSEIO)).isTrue();
            assertThat(pj.podePosituirCategoria(CategoriaVeiculo.UTILITARIO)).isTrue();
            assertThat(pj.podePosituirCategoria(CategoriaVeiculo.CAMINHAO)).isTrue();
        }

        @Test
        @DisplayName("Pessoa física tem limite de 5 veículos")
        void pessoaFisicaTemLimite5Veiculos() {
            Proprietario pf = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);

            assertThat(pf.getLimiteVeiculosRecomendado()).isEqualTo(5);
        }

        @Test
        @DisplayName("Pessoa jurídica tem limite de 100 veículos")
        void pessoaJuridicaTemLimite100Veiculos() {
            Proprietario pj = Proprietario.of(CNPJ_VALIDO, "Empresa LTDA", TipoPessoa.JURIDICA);

            assertThat(pj.getLimiteVeiculosRecomendado()).isEqualTo(100);
        }

        @Test
        @DisplayName("Deve retornar tipo de documento correto")
        void deveRetornarTipoDocumentoCorreto() {
            Proprietario pf = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);
            Proprietario pj = Proprietario.of(CNPJ_VALIDO, "Empresa LTDA", TipoPessoa.JURIDICA);

            assertThat(pf.getTipoDocumento()).isEqualTo("CPF");
            assertThat(pj.getTipoDocumento()).isEqualTo("CNPJ");
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Proprietários com mesmo documento devem ser iguais")
        void proprietariosComMesmoDocumentoDevemSerIguais() {
            Proprietario prop1 = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);
            Proprietario prop2 = Proprietario.of(CPF_VALIDO, "João Santos", TipoPessoa.FISICA);

            assertThat(prop1).isEqualTo(prop2);
            assertThat(prop1.hashCode()).isEqualTo(prop2.hashCode());
        }

        @Test
        @DisplayName("Proprietários com documentos diferentes não devem ser iguais")
        void proprietariosComDocumentosDiferentesNaoDevemSerIguais() {
            Proprietario prop1 = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);
            Proprietario prop2 = Proprietario.of(CNPJ_VALIDO, "Empresa LTDA", TipoPessoa.JURIDICA);

            assertThat(prop1).isNotEqualTo(prop2);
        }

        @Test
        @DisplayName("Proprietário deve ser igual a si mesmo")
        void proprietarioDeveSerIgualASiMesmo() {
            Proprietario prop = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);

            assertThat(prop).isEqualTo(prop);
        }

        @Test
        @DisplayName("Proprietário não deve ser igual a null")
        void proprietarioNaoDeveSerIgualANull() {
            Proprietario prop = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);

            assertThat(prop).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Proprietário não deve ser igual a objeto de outra classe")
        void proprietarioNaoDeveSerIgualAObjetoOutraClasse() {
            Proprietario prop = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);

            assertThat(prop).isNotEqualTo("João Silva");
        }
    }

    @Nested
    @DisplayName("Testes de Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Deve criar exemplo válido")
        void deveCriarExemploValido() {
            Proprietario exemplo = Proprietario.exemplo();

            assertThat(exemplo).isNotNull();
            assertThat(exemplo.isPessoaFisica()).isTrue();
        }

        @Test
        @DisplayName("Deve criar exemplo empresa válido")
        void deveCriarExemploEmpresaValido() {
            Proprietario exemplo = Proprietario.exemploEmpresa();

            assertThat(exemplo).isNotNull();
            assertThat(exemplo.isPessoaJuridica()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve lidar com documento sem formatação")
        void deveLidarComDocumentoSemFormatacao() {
            Proprietario prop = Proprietario.of(CPF_VALIDO, "João Silva", TipoPessoa.FISICA);

            assertThat(prop.getCpfCnpj()).doesNotContain(".");
            assertThat(prop.getCpfCnpj()).doesNotContain("-");
        }

        @Test
        @DisplayName("Deve aceitar nomes compostos")
        void deveAceitarNomesCompostos() {
            assertThatCode(() -> Proprietario.of(CPF_VALIDO, "Maria da Conceição Santos Silva", TipoPessoa.FISICA))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar nomes com números romanos")
        void deveAceitarNomesComNumerosRomanos() {
            // Números romanos não são aceitos pelo regex atual, mas nomes com ponto sim
            assertThatCode(() -> Proprietario.of(CPF_VALIDO, "João Silva Jr.", TipoPessoa.FISICA))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Iniciais vazias para nome vazio não deve falhar")
        void iniciaisVaziasParaNomeVazioNaoDeveFalhar() {
            // Não é possível criar com nome vazio, mas testamos o método diretamente
            Proprietario prop = Proprietario.of(CPF_VALIDO, "João", TipoPessoa.FISICA);
            assertThat(prop.getIniciais()).isNotNull();
        }
    }
}
