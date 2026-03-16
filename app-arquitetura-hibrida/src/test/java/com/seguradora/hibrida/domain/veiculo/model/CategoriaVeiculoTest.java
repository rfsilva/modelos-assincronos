package com.seguradora.hibrida.domain.veiculo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link CategoriaVeiculo}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("CategoriaVeiculo - Testes Unitários")
class CategoriaVeiculoTest {

    @Nested
    @DisplayName("Testes de Valores do Enum")
    class ValoresEnumTests {

        @Test
        @DisplayName("Deve ter todas as categorias esperadas")
        void deveTerTodasCategoriasEsperadas() {
            CategoriaVeiculo[] valores = CategoriaVeiculo.values();

            assertThat(valores).hasSize(4);
            assertThat(valores).containsExactlyInAnyOrder(
                CategoriaVeiculo.PASSEIO,
                CategoriaVeiculo.UTILITARIO,
                CategoriaVeiculo.MOTOCICLETA,
                CategoriaVeiculo.CAMINHAO
            );
        }

        @ParameterizedTest
        @EnumSource(CategoriaVeiculo.class)
        @DisplayName("Todas as categorias devem ter dados completos")
        void todasCategoriasDevemTermDadosCompletos(CategoriaVeiculo categoria) {
            assertThat(categoria.getNome()).isNotNull().isNotEmpty();
            assertThat(categoria.getDescricao()).isNotNull().isNotEmpty();
            assertThat(categoria.getCapacidadeMaximaPessoas()).isPositive();
            assertThat(categoria.getFatorRisco()).isPositive();
        }
    }

    @Nested
    @DisplayName("Testes de Capacidade e Fator de Risco")
    class CapacidadeRiscoTests {

        @Test
        @DisplayName("Categorias devem ter capacidade máxima correta")
        void categoriasDevemTermCapacidadeMaximaCorreta() {
            assertThat(CategoriaVeiculo.PASSEIO.getCapacidadeMaximaPessoas()).isEqualTo(9);
            assertThat(CategoriaVeiculo.UTILITARIO.getCapacidadeMaximaPessoas()).isEqualTo(15);
            assertThat(CategoriaVeiculo.MOTOCICLETA.getCapacidadeMaximaPessoas()).isEqualTo(2);
            assertThat(CategoriaVeiculo.CAMINHAO.getCapacidadeMaximaPessoas()).isEqualTo(3);
        }

        @Test
        @DisplayName("Categorias devem ter fator de risco correto")
        void categoriasDevemTermFatorRiscoCorreto() {
            assertThat(CategoriaVeiculo.PASSEIO.getFatorRisco()).isEqualTo(1.0);
            assertThat(CategoriaVeiculo.UTILITARIO.getFatorRisco()).isEqualTo(1.2);
            assertThat(CategoriaVeiculo.MOTOCICLETA.getFatorRisco()).isEqualTo(2.5);
            assertThat(CategoriaVeiculo.CAMINHAO.getFatorRisco()).isEqualTo(1.8);
        }

        @Test
        @DisplayName("Motocicleta deve ter maior fator de risco")
        void motocicletaDeveTerMaiorFatorRisco() {
            double maiorFator = 0;
            for (CategoriaVeiculo cat : CategoriaVeiculo.values()) {
                if (cat.getFatorRisco() > maiorFator) {
                    maiorFator = cat.getFatorRisco();
                }
            }

            assertThat(CategoriaVeiculo.MOTOCICLETA.getFatorRisco()).isEqualTo(maiorFator);
        }
    }

    @Nested
    @DisplayName("Testes de Uso Comercial")
    class UsoComercialTests {

        @Test
        @DisplayName("Utilitário e caminhão devem ser uso comercial")
        void utilitarioECaminhaoDevemSerUsoComercial() {
            assertThat(CategoriaVeiculo.UTILITARIO.isUsoComercial()).isTrue();
            assertThat(CategoriaVeiculo.CAMINHAO.isUsoComercial()).isTrue();
        }

        @Test
        @DisplayName("Passeio e motocicleta não devem ser uso comercial")
        void passeioEMotocicletaNaoDevemSerUsoComercial() {
            assertThat(CategoriaVeiculo.PASSEIO.isUsoComercial()).isFalse();
            assertThat(CategoriaVeiculo.MOTOCICLETA.isUsoComercial()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Alto Risco")
    class AltoRiscoTests {

        @Test
        @DisplayName("Apenas motocicleta deve ser alto risco")
        void apenasMotocicletaDeveSerAltoRisco() {
            assertThat(CategoriaVeiculo.MOTOCICLETA.isAltoRisco()).isTrue();
            assertThat(CategoriaVeiculo.PASSEIO.isAltoRisco()).isFalse();
            assertThat(CategoriaVeiculo.UTILITARIO.isAltoRisco()).isFalse();
            assertThat(CategoriaVeiculo.CAMINHAO.isAltoRisco()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Documentação Especial")
    class DocumentacaoEspecialTests {

        @Test
        @DisplayName("Caminhão e utilitário devem requerer documentação especial")
        void caminhaoEUtilitarioDevemRequererDocumentacaoEspecial() {
            assertThat(CategoriaVeiculo.CAMINHAO.requerDocumentacaoEspecial()).isTrue();
            assertThat(CategoriaVeiculo.UTILITARIO.requerDocumentacaoEspecial()).isTrue();
        }

        @Test
        @DisplayName("Passeio e motocicleta não devem requerer documentação especial")
        void passeioEMotocicletaNaoDevemRequererDocumentacaoEspecial() {
            assertThat(CategoriaVeiculo.PASSEIO.requerDocumentacaoEspecial()).isFalse();
            assertThat(CategoriaVeiculo.MOTOCICLETA.requerDocumentacaoEspecial()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Compatibilidade com Combustível")
    class CompatibilidadeCombustivelTests {

        @Test
        @DisplayName("Passeio deve permitir gasolina, etanol e flex")
        void passeioDevePermitirGasolinaEtanolFlex() {
            assertThat(CategoriaVeiculo.PASSEIO.permiteCombustivel(TipoCombustivel.GASOLINA)).isTrue();
            assertThat(CategoriaVeiculo.PASSEIO.permiteCombustivel(TipoCombustivel.ETANOL)).isTrue();
            assertThat(CategoriaVeiculo.PASSEIO.permiteCombustivel(TipoCombustivel.FLEX)).isTrue();
        }

        @Test
        @DisplayName("Caminhão deve permitir diesel")
        void caminhaoDevePermitirDiesel() {
            assertThat(CategoriaVeiculo.CAMINHAO.permiteCombustivel(TipoCombustivel.DIESEL)).isTrue();
        }

        @Test
        @DisplayName("Motocicleta não deve permitir diesel")
        void motocicletaNaoDevePermitirDiesel() {
            assertThat(CategoriaVeiculo.MOTOCICLETA.permiteCombustivel(TipoCombustivel.DIESEL)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para combustível null")
        void deveRetornarFalseParaCombustivelNull() {
            assertThat(CategoriaVeiculo.PASSEIO.permiteCombustivel(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Cilindrada")
    class CilindradaTests {

        @ParameterizedTest
        @CsvSource({
            "PASSEIO, 6000",
            "UTILITARIO, 8000",
            "MOTOCICLETA, 1500",
            "CAMINHAO, 15000"
        })
        @DisplayName("Deve ter cilindrada máxima correta por categoria")
        void deveTerCilindradaMaximaCorretaPorCategoria(CategoriaVeiculo categoria, int cilindradaEsperada) {
            assertThat(categoria.getCilindradaMaximaRecomendada()).isEqualTo(cilindradaEsperada);
        }

        @Test
        @DisplayName("Deve ser compatível com cilindrada dentro do limite")
        void deveSerCompativelComCilindradaDentroLimite() {
            assertThat(CategoriaVeiculo.PASSEIO.isCompativelComCilindrada(2000)).isTrue();
            assertThat(CategoriaVeiculo.MOTOCICLETA.isCompativelComCilindrada(1000)).isTrue();
        }

        @Test
        @DisplayName("Não deve ser compatível com cilindrada acima do limite")
        void naoDeveSerCompativelComCilindradaAcimaLimite() {
            assertThat(CategoriaVeiculo.PASSEIO.isCompativelComCilindrada(10000)).isFalse();
            assertThat(CategoriaVeiculo.MOTOCICLETA.isCompativelComCilindrada(3000)).isFalse();
        }

        @Test
        @DisplayName("Não deve ser compatível com cilindrada null ou negativa")
        void naoDeveSerCompativelComCilindradaNullOuNegativa() {
            assertThat(CategoriaVeiculo.PASSEIO.isCompativelComCilindrada(null)).isFalse();
            assertThat(CategoriaVeiculo.PASSEIO.isCompativelComCilindrada(0)).isFalse();
            assertThat(CategoriaVeiculo.PASSEIO.isCompativelComCilindrada(-1000)).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Idade Máxima")
    class IdadeMaximaTests {

        @ParameterizedTest
        @CsvSource({
            "PASSEIO, 20",
            "UTILITARIO, 15",
            "MOTOCICLETA, 10",
            "CAMINHAO, 25"
        })
        @DisplayName("Deve ter idade máxima correta por categoria")
        void deveTerIdadeMaximaCorretaPorCategoria(CategoriaVeiculo categoria, int idadeEsperada) {
            assertThat(categoria.getIdadeMaximaRecomendada()).isEqualTo(idadeEsperada);
        }

        @Test
        @DisplayName("Caminhão deve ter maior idade máxima")
        void caminhaoDeveTerMaiorIdadeMaxima() {
            int maiorIdade = 0;
            for (CategoriaVeiculo cat : CategoriaVeiculo.values()) {
                if (cat.getIdadeMaximaRecomendada() > maiorIdade) {
                    maiorIdade = cat.getIdadeMaximaRecomendada();
                }
            }

            assertThat(CategoriaVeiculo.CAMINHAO.getIdadeMaximaRecomendada()).isEqualTo(maiorIdade);
        }

        @Test
        @DisplayName("Motocicleta deve ter menor idade máxima")
        void motocicletaDeveTerMenorIdadeMaxima() {
            int menorIdade = Integer.MAX_VALUE;
            for (CategoriaVeiculo cat : CategoriaVeiculo.values()) {
                if (cat.getIdadeMaximaRecomendada() < menorIdade) {
                    menorIdade = cat.getIdadeMaximaRecomendada();
                }
            }

            assertThat(CategoriaVeiculo.MOTOCICLETA.getIdadeMaximaRecomendada()).isEqualTo(menorIdade);
        }
    }

    @Nested
    @DisplayName("Testes de Formatação")
    class FormatacaoTests {

        @Test
        @DisplayName("ToString deve retornar o nome da categoria")
        void toStringDeveRetornarNomeDaCategoria() {
            assertThat(CategoriaVeiculo.PASSEIO.toString()).isEqualTo("Passeio");
            assertThat(CategoriaVeiculo.UTILITARIO.toString()).isEqualTo("Utilitário");
            assertThat(CategoriaVeiculo.MOTOCICLETA.toString()).isEqualTo("Motocicleta");
            assertThat(CategoriaVeiculo.CAMINHAO.toString()).isEqualTo("Caminhão");
        }

        @ParameterizedTest
        @EnumSource(CategoriaVeiculo.class)
        @DisplayName("ToString não deve retornar null ou vazio")
        void toStringNaoDeveRetornarNullOuVazio(CategoriaVeiculo categoria) {
            assertThat(categoria.toString()).isNotNull().isNotEmpty();
        }
    }
}
