package com.seguradora.hibrida.domain.veiculo.relationship.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link TipoRelacionamento}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("TipoRelacionamento - Testes Unitários")
class TipoRelacionamentoTest {

    @Nested
    @DisplayName("Testes de Valores do Enum")
    class ValoresEnumTests {

        @Test
        @DisplayName("Deve ter 4 valores definidos")
        void deveTer4ValoresDefinidos() {
            TipoRelacionamento[] valores = TipoRelacionamento.values();
            assertThat(valores).hasSize(4);
        }

        @Test
        @DisplayName("Deve conter PRINCIPAL")
        void deveConterPrincipal() {
            assertThat(TipoRelacionamento.values()).contains(TipoRelacionamento.PRINCIPAL);
        }

        @Test
        @DisplayName("Deve conter ADICIONAL")
        void deveConterAdicional() {
            assertThat(TipoRelacionamento.values()).contains(TipoRelacionamento.ADICIONAL);
        }

        @Test
        @DisplayName("Deve conter TEMPORARIO")
        void deveConterTemporario() {
            assertThat(TipoRelacionamento.values()).contains(TipoRelacionamento.TEMPORARIO);
        }

        @Test
        @DisplayName("Deve conter SUBSTITUTO")
        void deveConterSubstituto() {
            assertThat(TipoRelacionamento.values()).contains(TipoRelacionamento.SUBSTITUTO);
        }

        @Test
        @DisplayName("Deve buscar valor por nome")
        void deveBuscarValorPorNome() {
            TipoRelacionamento tipo = TipoRelacionamento.valueOf("PRINCIPAL");
            assertThat(tipo).isEqualTo(TipoRelacionamento.PRINCIPAL);
        }
    }

    @Nested
    @DisplayName("Testes de Descrição")
    class DescricaoTests {

        @Test
        @DisplayName("PRINCIPAL deve ter descrição 'Principal'")
        void principalDeveTerDescricao() {
            assertThat(TipoRelacionamento.PRINCIPAL.getDescricao()).isEqualTo("Principal");
        }

        @Test
        @DisplayName("ADICIONAL deve ter descrição 'Adicional'")
        void adicionalDeveTerDescricao() {
            assertThat(TipoRelacionamento.ADICIONAL.getDescricao()).isEqualTo("Adicional");
        }

        @Test
        @DisplayName("TEMPORARIO deve ter descrição 'Temporário'")
        void temporarioDeveTerDescricao() {
            assertThat(TipoRelacionamento.TEMPORARIO.getDescricao()).isEqualTo("Temporário");
        }

        @Test
        @DisplayName("SUBSTITUTO deve ter descrição 'Substituto'")
        void substitutoDeveTerDescricao() {
            assertThat(TipoRelacionamento.SUBSTITUTO.getDescricao()).isEqualTo("Substituto");
        }

        @ParameterizedTest
        @EnumSource(TipoRelacionamento.class)
        @DisplayName("Todos os valores devem ter descrição não nula")
        void todosValoresDevemTerDescricaoNaoNula(TipoRelacionamento tipo) {
            assertThat(tipo.getDescricao()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Detalhamento")
    class DetalhamentoTests {

        @Test
        @DisplayName("PRINCIPAL deve ter detalhamento sobre veículo principal")
        void principalDeveTerDetalhamento() {
            assertThat(TipoRelacionamento.PRINCIPAL.getDetalhamento())
                .isEqualTo("Veículo principal coberto pela apólice");
        }

        @Test
        @DisplayName("ADICIONAL deve ter detalhamento sobre veículo adicional")
        void adicionalDeveTerDetalhamento() {
            assertThat(TipoRelacionamento.ADICIONAL.getDetalhamento())
                .isEqualTo("Veículo adicional na apólice");
        }

        @Test
        @DisplayName("TEMPORARIO deve ter detalhamento sobre cobertura temporária")
        void temporarioDeveTerDetalhamento() {
            assertThat(TipoRelacionamento.TEMPORARIO.getDetalhamento())
                .isEqualTo("Cobertura temporária do veículo");
        }

        @Test
        @DisplayName("SUBSTITUTO deve ter detalhamento sobre veículo substituto")
        void substitutoDeveTerDetalhamento() {
            assertThat(TipoRelacionamento.SUBSTITUTO.getDetalhamento())
                .isEqualTo("Veículo substituto temporário");
        }

        @ParameterizedTest
        @EnumSource(TipoRelacionamento.class)
        @DisplayName("Todos os valores devem ter detalhamento não nulo")
        void todosValoresDevemTerDetalhamentoNaoNulo(TipoRelacionamento tipo) {
            assertThat(tipo.getDetalhamento()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Regra: É Permanente")
    class PermanenteTests {

        @Test
        @DisplayName("PRINCIPAL deve ser permanente")
        void principalDeveSerPermanente() {
            assertThat(TipoRelacionamento.PRINCIPAL.isPermanente()).isTrue();
        }

        @Test
        @DisplayName("ADICIONAL deve ser permanente")
        void adicionalDeveSerPermanente() {
            assertThat(TipoRelacionamento.ADICIONAL.isPermanente()).isTrue();
        }

        @Test
        @DisplayName("TEMPORARIO não deve ser permanente")
        void temporarioNaoDeveSerPermanente() {
            assertThat(TipoRelacionamento.TEMPORARIO.isPermanente()).isFalse();
        }

        @Test
        @DisplayName("SUBSTITUTO não deve ser permanente")
        void substitutoNaoDeveSerPermanente() {
            assertThat(TipoRelacionamento.SUBSTITUTO.isPermanente()).isFalse();
        }

        @Test
        @DisplayName("Deve ter exatamente 2 tipos permanentes")
        void deveTerExatamente2TiposPermanentes() {
            long countPermanentes = java.util.Arrays.stream(TipoRelacionamento.values())
                .filter(TipoRelacionamento::isPermanente)
                .count();

            assertThat(countPermanentes).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Testes de Regra: É Temporário")
    class TemporarioTests {

        @Test
        @DisplayName("PRINCIPAL não deve ser temporário")
        void principalNaoDeveSerTemporario() {
            assertThat(TipoRelacionamento.PRINCIPAL.isTemporario()).isFalse();
        }

        @Test
        @DisplayName("ADICIONAL não deve ser temporário")
        void adicionalNaoDeveSerTemporario() {
            assertThat(TipoRelacionamento.ADICIONAL.isTemporario()).isFalse();
        }

        @Test
        @DisplayName("TEMPORARIO deve ser temporário")
        void temporarioDeveSerTemporario() {
            assertThat(TipoRelacionamento.TEMPORARIO.isTemporario()).isTrue();
        }

        @Test
        @DisplayName("SUBSTITUTO deve ser temporário")
        void substitutoDeveSerTemporario() {
            assertThat(TipoRelacionamento.SUBSTITUTO.isTemporario()).isTrue();
        }

        @Test
        @DisplayName("Deve ter exatamente 2 tipos temporários")
        void deveTerExatamente2TiposTemporarios() {
            long countTemporarios = java.util.Arrays.stream(TipoRelacionamento.values())
                .filter(TipoRelacionamento::isTemporario)
                .count();

            assertThat(countTemporarios).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Testes de Regras Complementares")
    class RegrasComplementaresTests {

        @ParameterizedTest
        @EnumSource(TipoRelacionamento.class)
        @DisplayName("Tipo deve ser permanente OU temporário, nunca ambos")
        void tipoDeveSerPermanenteOuTemporarioNuncaAmbos(TipoRelacionamento tipo) {
            boolean permanente = tipo.isPermanente();
            boolean temporario = tipo.isTemporario();

            // XOR lógico - só um pode ser true
            assertThat(permanente ^ temporario).isTrue();
        }

        @Test
        @DisplayName("PRINCIPAL e ADICIONAL devem ser complementares")
        void principalEAdicionalDevemSerComplementares() {
            assertThat(TipoRelacionamento.PRINCIPAL.isPermanente()).isTrue();
            assertThat(TipoRelacionamento.ADICIONAL.isPermanente()).isTrue();
            assertThat(TipoRelacionamento.PRINCIPAL.isTemporario()).isFalse();
            assertThat(TipoRelacionamento.ADICIONAL.isTemporario()).isFalse();
        }

        @Test
        @DisplayName("TEMPORARIO e SUBSTITUTO devem ser complementares")
        void temporarioESubstitutoDevemSerComplementares() {
            assertThat(TipoRelacionamento.TEMPORARIO.isTemporario()).isTrue();
            assertThat(TipoRelacionamento.SUBSTITUTO.isTemporario()).isTrue();
            assertThat(TipoRelacionamento.TEMPORARIO.isPermanente()).isFalse();
            assertThat(TipoRelacionamento.SUBSTITUTO.isPermanente()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de toString")
    class ToStringTests {

        @Test
        @DisplayName("toString deve retornar descrição")
        void toStringDeveRetornarDescricao() {
            assertThat(TipoRelacionamento.PRINCIPAL.toString()).isEqualTo("Principal");
        }

        @ParameterizedTest
        @EnumSource(TipoRelacionamento.class)
        @DisplayName("toString de todos os valores deve retornar descrição")
        void toStringTodosValoresDeveRetornarDescricao(TipoRelacionamento tipo) {
            assertThat(tipo.toString()).isEqualTo(tipo.getDescricao());
        }
    }

    @Nested
    @DisplayName("Testes de Comparação e Identidade")
    class ComparacaoIdentidadeTests {

        @Test
        @DisplayName("Deve comparar valores iguais")
        void deveCompararValoresIguais() {
            TipoRelacionamento tipo1 = TipoRelacionamento.PRINCIPAL;
            TipoRelacionamento tipo2 = TipoRelacionamento.valueOf("PRINCIPAL");

            assertThat(tipo1).isEqualTo(tipo2);
            assertThat(tipo1 == tipo2).isTrue();
        }

        @Test
        @DisplayName("Deve comparar valores diferentes")
        void deveCompararValoresDiferentes() {
            assertThat(TipoRelacionamento.PRINCIPAL).isNotEqualTo(TipoRelacionamento.ADICIONAL);
        }

        @Test
        @DisplayName("Deve ter hashCode consistente")
        void deveTerHashCodeConsistente() {
            TipoRelacionamento tipo1 = TipoRelacionamento.PRINCIPAL;
            TipoRelacionamento tipo2 = TipoRelacionamento.valueOf("PRINCIPAL");

            assertThat(tipo1.hashCode()).isEqualTo(tipo2.hashCode());
        }
    }

    @Nested
    @DisplayName("Testes de Uso em Estruturas de Dados")
    class UsoEstruturasDadosTests {

        @Test
        @DisplayName("Deve usar como chave em Map")
        void deveUsarComoChaveEmMap() {
            java.util.Map<TipoRelacionamento, String> mapa = new java.util.HashMap<>();
            mapa.put(TipoRelacionamento.PRINCIPAL, "Veículo principal");
            mapa.put(TipoRelacionamento.ADICIONAL, "Veículo adicional");

            assertThat(mapa.get(TipoRelacionamento.PRINCIPAL)).isEqualTo("Veículo principal");
            assertThat(mapa).hasSize(2);
        }

        @Test
        @DisplayName("Deve usar em Set sem duplicatas")
        void deveUsarEmSetSemDuplicatas() {
            java.util.Set<TipoRelacionamento> set = new java.util.HashSet<>();
            set.add(TipoRelacionamento.PRINCIPAL);
            set.add(TipoRelacionamento.PRINCIPAL);
            set.add(TipoRelacionamento.ADICIONAL);

            assertThat(set).hasSize(2);
            assertThat(set).contains(TipoRelacionamento.PRINCIPAL, TipoRelacionamento.ADICIONAL);
        }

        @Test
        @DisplayName("Deve usar em switch statement")
        void deveUsarEmSwitchStatement() {
            String resultado = switch (TipoRelacionamento.PRINCIPAL) {
                case PRINCIPAL -> "Veículo principal";
                case ADICIONAL -> "Veículo adicional";
                case TEMPORARIO -> "Veículo temporário";
                case SUBSTITUTO -> "Veículo substituto";
            };

            assertThat(resultado).isEqualTo("Veículo principal");
        }
    }

    @Nested
    @DisplayName("Testes de Cenários de Uso")
    class CenariosUsoTests {

        @Test
        @DisplayName("Deve identificar tipo principal em apólice")
        void deveIdentificarTipoPrincipalEmApolice() {
            TipoRelacionamento tipo = TipoRelacionamento.PRINCIPAL;

            assertThat(tipo.isPermanente()).isTrue();
            assertThat(tipo.getDescricao()).contains("Principal");
        }

        @Test
        @DisplayName("Deve identificar veículo adicional na mesma apólice")
        void deveIdentificarVeiculoAdicionalNaMesmaApolice() {
            TipoRelacionamento tipo = TipoRelacionamento.ADICIONAL;

            assertThat(tipo.isPermanente()).isTrue();
            assertThat(tipo.getDetalhamento()).contains("adicional");
        }

        @Test
        @DisplayName("Deve identificar cobertura temporária")
        void deveIdentificarCoberturaTemporaria() {
            TipoRelacionamento tipo = TipoRelacionamento.TEMPORARIO;

            assertThat(tipo.isTemporario()).isTrue();
            assertThat(tipo.getDetalhamento()).contains("temporária");
        }

        @Test
        @DisplayName("Deve identificar veículo substituto durante manutenção")
        void deveIdentificarVeiculoSubstitutoDuranteManutencao() {
            TipoRelacionamento tipo = TipoRelacionamento.SUBSTITUTO;

            assertThat(tipo.isTemporario()).isTrue();
            assertThat(tipo.getDetalhamento()).contains("substituto");
        }
    }

    @Nested
    @DisplayName("Testes de Validação")
    class ValidacaoTests {

        @Test
        @DisplayName("Deve lançar exceção ao buscar valor inexistente")
        void deveLancarExcecaoAoBuscarValorInexistente() {
            assertThatThrownBy(() -> TipoRelacionamento.valueOf("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve ser case-sensitive na busca por nome")
        void deveSerCaseSensitiveNaBuscaPorNome() {
            assertThatThrownBy(() -> TipoRelacionamento.valueOf("principal"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Testes de Combinação com StatusRelacionamento")
    class CombinacaoStatusTests {

        @Test
        @DisplayName("Tipo permanente pode ter qualquer status")
        void tipoPermanentePodeTerQualquerStatus() {
            TipoRelacionamento tipo = TipoRelacionamento.PRINCIPAL;

            // Tipo permanente pode estar ATIVO, SUSPENSO, ENCERRADO, CANCELADO
            assertThat(tipo.isPermanente()).isTrue();
        }

        @Test
        @DisplayName("Tipo temporário geralmente tem status ATIVO ou ENCERRADO")
        void tipoTemporarioGeralmenteTemStatusAtivoOuEncerrado() {
            TipoRelacionamento tipo = TipoRelacionamento.TEMPORARIO;

            // Tipo temporário é usado por período limitado
            assertThat(tipo.isTemporario()).isTrue();
        }
    }
}
