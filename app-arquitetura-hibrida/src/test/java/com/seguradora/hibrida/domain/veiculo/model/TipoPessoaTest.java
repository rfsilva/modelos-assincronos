package com.seguradora.hibrida.domain.veiculo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link TipoPessoa}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("TipoPessoa - Testes Unitários")
class TipoPessoaTest {

    @Nested
    @DisplayName("Testes de Valores do Enum")
    class ValoresEnumTests {

        @Test
        @DisplayName("Deve ter todos os tipos esperados")
        void deveTerTodosTiposEsperados() {
            TipoPessoa[] valores = TipoPessoa.values();

            assertThat(valores).hasSize(2);
            assertThat(valores).containsExactlyInAnyOrder(
                TipoPessoa.FISICA,
                TipoPessoa.JURIDICA
            );
        }

        @ParameterizedTest
        @EnumSource(TipoPessoa.class)
        @DisplayName("Todos os tipos devem ter dados completos")
        void todosTiposDevemTermDadosCompletos(TipoPessoa tipo) {
            assertThat(tipo.getNome()).isNotNull().isNotEmpty();
            assertThat(tipo.getSigla()).isNotNull().isNotEmpty();
            assertThat(tipo.getTamanhoDocumento()).isPositive();
            assertThat(tipo.getTipoDocumento()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Dados Básicos")
    class DadosBasicosTests {

        @Test
        @DisplayName("Pessoa física deve ter dados corretos")
        void pessoaFisicaDeveTerDadosCorretos() {
            assertThat(TipoPessoa.FISICA.getNome()).isEqualTo("Pessoa Física");
            assertThat(TipoPessoa.FISICA.getSigla()).isEqualTo("PF");
            assertThat(TipoPessoa.FISICA.getTamanhoDocumento()).isEqualTo(11);
            assertThat(TipoPessoa.FISICA.getTipoDocumento()).isEqualTo("CPF");
        }

        @Test
        @DisplayName("Pessoa jurídica deve ter dados corretos")
        void pessoaJuridicaDeveTerDadosCorretos() {
            assertThat(TipoPessoa.JURIDICA.getNome()).isEqualTo("Pessoa Jurídica");
            assertThat(TipoPessoa.JURIDICA.getSigla()).isEqualTo("PJ");
            assertThat(TipoPessoa.JURIDICA.getTamanhoDocumento()).isEqualTo(14);
            assertThat(TipoPessoa.JURIDICA.getTipoDocumento()).isEqualTo("CNPJ");
        }
    }

    @Nested
    @DisplayName("Testes de Identificação de Tipo")
    class IdentificacaoTipoTests {

        @Test
        @DisplayName("Deve identificar pessoa física")
        void deveIdentificarPessoaFisica() {
            assertThat(TipoPessoa.FISICA.isPessoaFisica()).isTrue();
            assertThat(TipoPessoa.FISICA.isPessoaJuridica()).isFalse();
        }

        @Test
        @DisplayName("Deve identificar pessoa jurídica")
        void deveIdentificarPessoaJuridica() {
            assertThat(TipoPessoa.JURIDICA.isPessoaJuridica()).isTrue();
            assertThat(TipoPessoa.JURIDICA.isPessoaFisica()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Documento")
    class ValidacaoDocumentoTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "12345678900",
            "111.222.333-44",
            "   12345678901   ",
            "123.456.789-01"
        })
        @DisplayName("Pessoa física deve validar CPF com 11 dígitos")
        void pessoaFisicaDeveValidarCpfCom11Digitos(String cpf) {
            assertThat(TipoPessoa.FISICA.validarFormatoDocumento(cpf)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "12345678000190",
            "12.345.678/0001-90",
            "   12345678000190   "
        })
        @DisplayName("Pessoa jurídica deve validar CNPJ com 14 dígitos")
        void pessoaJuridicaDeveValidarCnpjCom14Digitos(String cnpj) {
            assertThat(TipoPessoa.JURIDICA.validarFormatoDocumento(cnpj)).isTrue();
        }

        @Test
        @DisplayName("Deve rejeitar documento com tamanho incorreto")
        void deveRejeitarDocumentoComTamanhoIncorreto() {
            assertThat(TipoPessoa.FISICA.validarFormatoDocumento("123456")).isFalse();
            assertThat(TipoPessoa.JURIDICA.validarFormatoDocumento("12345")).isFalse();
        }

        @Test
        @DisplayName("Deve rejeitar documento null ou vazio")
        void deveRejeitarDocumentoNullOuVazio() {
            assertThat(TipoPessoa.FISICA.validarFormatoDocumento(null)).isFalse();
            assertThat(TipoPessoa.FISICA.validarFormatoDocumento("")).isFalse();
            assertThat(TipoPessoa.FISICA.validarFormatoDocumento("   ")).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Formatação de Documento")
    class FormatacaoDocumentoTests {

        @Test
        @DisplayName("Deve formatar CPF corretamente")
        void deveFormatarCpfCorretamente() {
            String cpfFormatado = TipoPessoa.FISICA.formatarDocumento("12345678900");

            assertThat(cpfFormatado).isEqualTo("123.456.789-00");
        }

        @Test
        @DisplayName("Deve formatar CNPJ corretamente")
        void deveFormatarCnpjCorretamente() {
            String cnpjFormatado = TipoPessoa.JURIDICA.formatarDocumento("12345678000190");

            assertThat(cnpjFormatado).isEqualTo("12.345.678/0001-90");
        }

        @Test
        @DisplayName("Deve formatar documento já com pontuação")
        void deveFormatarDocumentoJaComPontuacao() {
            String cpf = TipoPessoa.FISICA.formatarDocumento("123.456.789-00");
            String cnpj = TipoPessoa.JURIDICA.formatarDocumento("12.345.678/0001-90");

            assertThat(cpf).isEqualTo("123.456.789-00");
            assertThat(cnpj).isEqualTo("12.345.678/0001-90");
        }

        @Test
        @DisplayName("Deve retornar null para documento inválido")
        void deveRetornarNullParaDocumentoInvalido() {
            assertThat(TipoPessoa.FISICA.formatarDocumento("123")).isNull();
            assertThat(TipoPessoa.JURIDICA.formatarDocumento("456")).isNull();
            assertThat(TipoPessoa.FISICA.formatarDocumento(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Testes de Identificação por Documento")
    class IdentificacaoPorDocumentoTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "12345678900",
            "111.222.333-44",
            "   12345678901   "
        })
        @DisplayName("Deve identificar pessoa física por documento")
        void deveIdentificarPessoaFisicaPorDocumento(String cpf) {
            TipoPessoa tipo = TipoPessoa.identificarPorDocumento(cpf);

            assertThat(tipo).isEqualTo(TipoPessoa.FISICA);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "12345678000190",
            "12.345.678/0001-90",
            "   12345678000190   "
        })
        @DisplayName("Deve identificar pessoa jurídica por documento")
        void deveIdentificarPessoaJuridicaPorDocumento(String cnpj) {
            TipoPessoa tipo = TipoPessoa.identificarPorDocumento(cnpj);

            assertThat(tipo).isEqualTo(TipoPessoa.JURIDICA);
        }

        @Test
        @DisplayName("Deve retornar null para documento com tamanho inválido")
        void deveRetornarNullParaDocumentoTamanhoInvalido() {
            assertThat(TipoPessoa.identificarPorDocumento("123")).isNull();
            assertThat(TipoPessoa.identificarPorDocumento("12345")).isNull();
        }

        @Test
        @DisplayName("Deve retornar null para documento null")
        void deveRetornarNullParaDocumentoNull() {
            assertThat(TipoPessoa.identificarPorDocumento(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Testes de Permissão de Categoria")
    class PermissaoCategoriaTests {

        @Test
        @DisplayName("Pessoa física deve permitir todas as categorias")
        void pessoaFisicaDevePermitirTodasCategorias() {
            for (CategoriaVeiculo categoria : CategoriaVeiculo.values()) {
                assertThat(TipoPessoa.FISICA.permiteCategoria(categoria))
                    .as("PF deve permitir %s", categoria)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("Pessoa jurídica não deve permitir motocicleta")
        void pessoaJuridicaNaoDevePermitirMotocicleta() {
            assertThat(TipoPessoa.JURIDICA.permiteCategoria(CategoriaVeiculo.MOTOCICLETA))
                .isFalse();
        }

        @Test
        @DisplayName("Pessoa jurídica deve permitir outras categorias")
        void pessoaJuridicaDevePermitirOutrasCategorias() {
            assertThat(TipoPessoa.JURIDICA.permiteCategoria(CategoriaVeiculo.PASSEIO)).isTrue();
            assertThat(TipoPessoa.JURIDICA.permiteCategoria(CategoriaVeiculo.UTILITARIO)).isTrue();
            assertThat(TipoPessoa.JURIDICA.permiteCategoria(CategoriaVeiculo.CAMINHAO)).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false para categoria null")
        void deveRetornarFalseParaCategoriaNullo() {
            assertThat(TipoPessoa.FISICA.permiteCategoria(null)).isFalse();
            assertThat(TipoPessoa.JURIDICA.permiteCategoria(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Limite de Veículos")
    class LimiteVeiculosTests {

        @Test
        @DisplayName("Pessoa física deve ter limite de 5 veículos")
        void pessoaFisicaDeveTerLimite5Veiculos() {
            assertThat(TipoPessoa.FISICA.getLimiteVeiculosRecomendado()).isEqualTo(5);
        }

        @Test
        @DisplayName("Pessoa jurídica deve ter limite de 100 veículos")
        void pessoaJuridicaDeveTerLimite100Veiculos() {
            assertThat(TipoPessoa.JURIDICA.getLimiteVeiculosRecomendado()).isEqualTo(100);
        }

        @Test
        @DisplayName("Pessoa jurídica deve ter limite maior que pessoa física")
        void pessoaJuridicaDeveTerLimiteMaiorQuePessoaFisica() {
            assertThat(TipoPessoa.JURIDICA.getLimiteVeiculosRecomendado())
                .isGreaterThan(TipoPessoa.FISICA.getLimiteVeiculosRecomendado());
        }
    }

    @Nested
    @DisplayName("Testes de Formatação")
    class FormatacaoTests {

        @Test
        @DisplayName("ToString deve retornar a sigla")
        void toStringDeveRetornarSigla() {
            assertThat(TipoPessoa.FISICA.toString()).isEqualTo("PF");
            assertThat(TipoPessoa.JURIDICA.toString()).isEqualTo("PJ");
        }

        @ParameterizedTest
        @EnumSource(TipoPessoa.class)
        @DisplayName("ToString não deve retornar null ou vazio")
        void toStringNaoDeveRetornarNullOuVazio(TipoPessoa tipo) {
            assertThat(tipo.toString()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve remover caracteres especiais ao validar")
        void deveRemoverCaracteresEspeciaisAoValidar() {
            String cpfComCaracteres = "123@456#789$01";
            assertThat(TipoPessoa.FISICA.validarFormatoDocumento(cpfComCaracteres)).isTrue();
        }

        @Test
        @DisplayName("Deve lidar com espaços extras")
        void deveLidarComEspacosExtras() {
            String cpfComEspacos = "   123   456   789   01   ";
            assertThat(TipoPessoa.FISICA.validarFormatoDocumento(cpfComEspacos)).isTrue();
        }

        @Test
        @DisplayName("Formatação deve preservar apenas números")
        void formatacaoDevePreservarApenasNumeros() {
            String cnpjComLetras = "12ABC345DEF678GHI0001JKL90";
            String formatado = TipoPessoa.JURIDICA.formatarDocumento(cnpjComLetras);

            assertThat(formatado).isEqualTo("12.345.678/0001-90");
        }
    }
}
