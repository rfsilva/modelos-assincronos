package com.seguradora.hibrida.domain.veiculo.relationship.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link StatusRelacionamento}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("StatusRelacionamento - Testes Unitários")
class StatusRelacionamentoTest {

    @Nested
    @DisplayName("Testes de Valores do Enum")
    class ValoresEnumTests {

        @Test
        @DisplayName("Deve ter 4 valores definidos")
        void deveTer4ValoresDefinidos() {
            StatusRelacionamento[] valores = StatusRelacionamento.values();
            assertThat(valores).hasSize(4);
        }

        @Test
        @DisplayName("Deve conter ATIVO")
        void deveConterAtivo() {
            assertThat(StatusRelacionamento.values()).contains(StatusRelacionamento.ATIVO);
        }

        @Test
        @DisplayName("Deve conter SUSPENSO")
        void deveConterSuspenso() {
            assertThat(StatusRelacionamento.values()).contains(StatusRelacionamento.SUSPENSO);
        }

        @Test
        @DisplayName("Deve conter ENCERRADO")
        void deveConterEncerrado() {
            assertThat(StatusRelacionamento.values()).contains(StatusRelacionamento.ENCERRADO);
        }

        @Test
        @DisplayName("Deve conter CANCELADO")
        void deveConterCancelado() {
            assertThat(StatusRelacionamento.values()).contains(StatusRelacionamento.CANCELADO);
        }

        @Test
        @DisplayName("Deve buscar valor por nome")
        void deveBuscarValorPorNome() {
            StatusRelacionamento status = StatusRelacionamento.valueOf("ATIVO");
            assertThat(status).isEqualTo(StatusRelacionamento.ATIVO);
        }
    }

    @Nested
    @DisplayName("Testes de Descrição")
    class DescricaoTests {

        @Test
        @DisplayName("ATIVO deve ter descrição 'Ativo'")
        void ativoDeveTerDescricao() {
            assertThat(StatusRelacionamento.ATIVO.getDescricao()).isEqualTo("Ativo");
        }

        @Test
        @DisplayName("SUSPENSO deve ter descrição 'Suspenso'")
        void suspensoDeveTerDescricao() {
            assertThat(StatusRelacionamento.SUSPENSO.getDescricao()).isEqualTo("Suspenso");
        }

        @Test
        @DisplayName("ENCERRADO deve ter descrição 'Encerrado'")
        void encerradoDeveTerDescricao() {
            assertThat(StatusRelacionamento.ENCERRADO.getDescricao()).isEqualTo("Encerrado");
        }

        @Test
        @DisplayName("CANCELADO deve ter descrição 'Cancelado'")
        void canceladoDeveTerDescricao() {
            assertThat(StatusRelacionamento.CANCELADO.getDescricao()).isEqualTo("Cancelado");
        }

        @ParameterizedTest
        @EnumSource(StatusRelacionamento.class)
        @DisplayName("Todos os valores devem ter descrição não nula")
        void todosValoresDevemTerDescricaoNaoNula(StatusRelacionamento status) {
            assertThat(status.getDescricao()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Detalhamento")
    class DetalhamentoTests {

        @Test
        @DisplayName("ATIVO deve ter detalhamento sobre cobertura ativa")
        void ativoDeveTerDetalhamento() {
            assertThat(StatusRelacionamento.ATIVO.getDetalhamento())
                .isEqualTo("Veículo com cobertura ativa");
        }

        @Test
        @DisplayName("SUSPENSO deve ter detalhamento sobre suspensão temporária")
        void suspensoDeveTerDetalhamento() {
            assertThat(StatusRelacionamento.SUSPENSO.getDetalhamento())
                .isEqualTo("Cobertura temporariamente suspensa");
        }

        @Test
        @DisplayName("ENCERRADO deve ter detalhamento sobre finalização")
        void encerradoDeveTerDetalhamento() {
            assertThat(StatusRelacionamento.ENCERRADO.getDetalhamento())
                .isEqualTo("Cobertura finalizada");
        }

        @Test
        @DisplayName("CANCELADO deve ter detalhamento sobre cancelamento")
        void canceladoDeveTerDetalhamento() {
            assertThat(StatusRelacionamento.CANCELADO.getDetalhamento())
                .isEqualTo("Relacionamento cancelado");
        }

        @ParameterizedTest
        @EnumSource(StatusRelacionamento.class)
        @DisplayName("Todos os valores devem ter detalhamento não nulo")
        void todosValoresDevemTerDetalhamentoNaoNulo(StatusRelacionamento status) {
            assertThat(status.getDetalhamento()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Regra: Permite Cobertura")
    class PermiteCoberturaTests {

        @Test
        @DisplayName("ATIVO deve permitir cobertura")
        void ativoDevePermitirCobertura() {
            assertThat(StatusRelacionamento.ATIVO.permiteCobertura()).isTrue();
        }

        @Test
        @DisplayName("SUSPENSO não deve permitir cobertura")
        void suspensoNaoDevePermitirCobertura() {
            assertThat(StatusRelacionamento.SUSPENSO.permiteCobertura()).isFalse();
        }

        @Test
        @DisplayName("ENCERRADO não deve permitir cobertura")
        void encerradoNaoDevePermitirCobertura() {
            assertThat(StatusRelacionamento.ENCERRADO.permiteCobertura()).isFalse();
        }

        @Test
        @DisplayName("CANCELADO não deve permitir cobertura")
        void canceladoNaoDevePermitirCobertura() {
            assertThat(StatusRelacionamento.CANCELADO.permiteCobertura()).isFalse();
        }

        @Test
        @DisplayName("Apenas ATIVO deve permitir cobertura")
        void apenasAtivoDevePermitirCobertura() {
            long countPermiteCobertura = java.util.Arrays.stream(StatusRelacionamento.values())
                .filter(StatusRelacionamento::permiteCobertura)
                .count();

            assertThat(countPermiteCobertura).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Testes de toString")
    class ToStringTests {

        @Test
        @DisplayName("toString deve retornar descrição")
        void toStringDeveRetornarDescricao() {
            assertThat(StatusRelacionamento.ATIVO.toString()).isEqualTo("Ativo");
        }

        @ParameterizedTest
        @EnumSource(StatusRelacionamento.class)
        @DisplayName("toString de todos os valores deve retornar descrição")
        void toStringTodosValoresDeveRetornarDescricao(StatusRelacionamento status) {
            assertThat(status.toString()).isEqualTo(status.getDescricao());
        }
    }

    @Nested
    @DisplayName("Testes de Comparação e Identidade")
    class ComparacaoIdentidadeTests {

        @Test
        @DisplayName("Deve comparar valores iguais")
        void deveCompararValoresIguais() {
            StatusRelacionamento status1 = StatusRelacionamento.ATIVO;
            StatusRelacionamento status2 = StatusRelacionamento.valueOf("ATIVO");

            assertThat(status1).isEqualTo(status2);
            assertThat(status1 == status2).isTrue();
        }

        @Test
        @DisplayName("Deve comparar valores diferentes")
        void deveCompararValoresDiferentes() {
            assertThat(StatusRelacionamento.ATIVO).isNotEqualTo(StatusRelacionamento.SUSPENSO);
        }

        @Test
        @DisplayName("Deve ter hashCode consistente")
        void deveTerHashCodeConsistente() {
            StatusRelacionamento status1 = StatusRelacionamento.ATIVO;
            StatusRelacionamento status2 = StatusRelacionamento.valueOf("ATIVO");

            assertThat(status1.hashCode()).isEqualTo(status2.hashCode());
        }
    }

    @Nested
    @DisplayName("Testes de Uso em Estruturas de Dados")
    class UsoEstruturasDadosTests {

        @Test
        @DisplayName("Deve usar como chave em Map")
        void deveUsarComoChaveEmMap() {
            java.util.Map<StatusRelacionamento, String> mapa = new java.util.HashMap<>();
            mapa.put(StatusRelacionamento.ATIVO, "Cobertura ativa");
            mapa.put(StatusRelacionamento.SUSPENSO, "Cobertura suspensa");

            assertThat(mapa.get(StatusRelacionamento.ATIVO)).isEqualTo("Cobertura ativa");
            assertThat(mapa).hasSize(2);
        }

        @Test
        @DisplayName("Deve usar em Set sem duplicatas")
        void deveUsarEmSetSemDuplicatas() {
            java.util.Set<StatusRelacionamento> set = new java.util.HashSet<>();
            set.add(StatusRelacionamento.ATIVO);
            set.add(StatusRelacionamento.ATIVO);
            set.add(StatusRelacionamento.SUSPENSO);

            assertThat(set).hasSize(2);
            assertThat(set).contains(StatusRelacionamento.ATIVO, StatusRelacionamento.SUSPENSO);
        }

        @Test
        @DisplayName("Deve usar em switch statement")
        void deveUsarEmSwitchStatement() {
            String resultado = switch (StatusRelacionamento.ATIVO) {
                case ATIVO -> "Permite cobertura";
                case SUSPENSO -> "Suspenso temporariamente";
                case ENCERRADO -> "Finalizado";
                case CANCELADO -> "Cancelado";
            };

            assertThat(resultado).isEqualTo("Permite cobertura");
        }
    }

    @Nested
    @DisplayName("Testes de Ciclo de Vida")
    class CicloVidaTests {

        @Test
        @DisplayName("Deve representar transição de ATIVO para SUSPENSO")
        void deveRepresentarTransicaoAtivoParaSuspenso() {
            StatusRelacionamento inicial = StatusRelacionamento.ATIVO;
            StatusRelacionamento novo = StatusRelacionamento.SUSPENSO;

            assertThat(inicial.permiteCobertura()).isTrue();
            assertThat(novo.permiteCobertura()).isFalse();
        }

        @Test
        @DisplayName("Deve representar transição de SUSPENSO para ATIVO")
        void deveRepresentarTransicaoSuspensoParaAtivo() {
            StatusRelacionamento inicial = StatusRelacionamento.SUSPENSO;
            StatusRelacionamento novo = StatusRelacionamento.ATIVO;

            assertThat(inicial.permiteCobertura()).isFalse();
            assertThat(novo.permiteCobertura()).isTrue();
        }

        @Test
        @DisplayName("Deve representar transição de ATIVO para ENCERRADO")
        void deveRepresentarTransicaoAtivoParaEncerrado() {
            StatusRelacionamento inicial = StatusRelacionamento.ATIVO;
            StatusRelacionamento novo = StatusRelacionamento.ENCERRADO;

            assertThat(inicial.permiteCobertura()).isTrue();
            assertThat(novo.permiteCobertura()).isFalse();
        }

        @Test
        @DisplayName("Deve representar transição de ATIVO para CANCELADO")
        void deveRepresentarTransicaoAtivoParaCancelado() {
            StatusRelacionamento inicial = StatusRelacionamento.ATIVO;
            StatusRelacionamento novo = StatusRelacionamento.CANCELADO;

            assertThat(inicial.permiteCobertura()).isTrue();
            assertThat(novo.permiteCobertura()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Validação")
    class ValidacaoTests {

        @Test
        @DisplayName("Deve lançar exceção ao buscar valor inexistente")
        void deveLancarExcecaoAoBuscarValorInexistente() {
            assertThatThrownBy(() -> StatusRelacionamento.valueOf("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve ser case-sensitive na busca por nome")
        void deveSerCaseSensitiveNaBuscaPorNome() {
            assertThatThrownBy(() -> StatusRelacionamento.valueOf("ativo"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
