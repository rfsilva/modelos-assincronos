package com.seguradora.hibrida.domain.workflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TransicaoWorkflow Tests")
class TransicaoWorkflowTest {

    private TransicaoWorkflow transicaoValida() {
        return TransicaoWorkflow.builder()
                .id("trans-01")
                .etapaOrigemId("etapa-01")
                .etapaDestinoId("etapa-02")
                .build();
    }

    // =========================================================================
    // validar
    // =========================================================================

    @Nested
    @DisplayName("validar()")
    class Validar {

        @Test
        @DisplayName("Transição válida não deve lançar exceção")
        void validTransitionShouldNotThrow() {
            transicaoValida().validar();
        }

        @Test
        @DisplayName("Origem nula deve lançar exceção")
        void nullOriginShouldThrow() {
            TransicaoWorkflow t = TransicaoWorkflow.builder()
                    .etapaDestinoId("etapa-02").build();
            assertThatThrownBy(t::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("origem");
        }

        @Test
        @DisplayName("Destino nulo deve lançar exceção")
        void nullDestinationShouldThrow() {
            TransicaoWorkflow t = TransicaoWorkflow.builder()
                    .etapaOrigemId("etapa-01").build();
            assertThatThrownBy(t::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("destino");
        }

        @Test
        @DisplayName("Origem igual ao destino deve lançar exceção")
        void sameOriginAndDestinationShouldThrow() {
            TransicaoWorkflow t = TransicaoWorkflow.builder()
                    .etapaOrigemId("etapa-01")
                    .etapaDestinoId("etapa-01")
                    .build();
            assertThatThrownBy(t::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("iguais");
        }

        @Test
        @DisplayName("isValida deve retornar true para transição válida")
        void isValidaShouldReturnTrueForValidTransition() {
            assertThat(transicaoValida().isValida()).isTrue();
        }

        @Test
        @DisplayName("isValida deve retornar false para transição inválida")
        void isValidaShouldReturnFalseForInvalidTransition() {
            TransicaoWorkflow t = TransicaoWorkflow.builder()
                    .etapaOrigemId("etapa-01")
                    .etapaDestinoId("etapa-01")
                    .build();
            assertThat(t.isValida()).isFalse();
        }
    }

    // =========================================================================
    // podeTransicionar
    // =========================================================================

    @Nested
    @DisplayName("podeTransicionar()")
    class PodeTransicionar {

        @Test
        @DisplayName("Sem condições deve sempre poder transicionar")
        void withNoConditionsShouldAlwaysTransition() {
            TransicaoWorkflow t = transicaoValida();
            assertThat(t.podeTransicionar(Map.of())).isTrue();
        }

        @Test
        @DisplayName("Transição inativa não deve poder transicionar")
        void inactiveTransitionShouldNotTransition() {
            TransicaoWorkflow t = transicaoValida();
            t.desativar();
            assertThat(t.podeTransicionar(Map.of())).isFalse();
        }

        @Test
        @DisplayName("Condição de igualdade deve ser avaliada")
        void equalityConditionShouldBeEvaluated() {
            TransicaoWorkflow t = transicaoValida();
            t.adicionarCondicao("status", "APROVADO");
            assertThat(t.podeTransicionar(Map.of("status", "APROVADO"))).isTrue();
            assertThat(t.podeTransicionar(Map.of("status", "PENDENTE"))).isFalse();
        }

        @Test
        @DisplayName("Condição == deve funcionar")
        void doubleEqualConditionShouldWork() {
            TransicaoWorkflow t = transicaoValida();
            t.adicionarCondicao("tipo", "==SIMPLES");
            assertThat(t.podeTransicionar(Map.of("tipo", "SIMPLES"))).isTrue();
        }

        @Test
        @DisplayName("Condição != deve funcionar")
        void notEqualConditionShouldWork() {
            TransicaoWorkflow t = transicaoValida();
            t.adicionarCondicao("status", "!=CANCELADO");
            assertThat(t.podeTransicionar(Map.of("status", "APROVADO"))).isTrue();
            assertThat(t.podeTransicionar(Map.of("status", "CANCELADO"))).isFalse();
        }

        @Test
        @DisplayName("Condições numéricas devem funcionar")
        void numericConditionsShouldWork() {
            TransicaoWorkflow t = transicaoValida();
            t.adicionarCondicao("valor", ">5000");
            assertThat(t.podeTransicionar(Map.of("valor", "10000"))).isTrue();
            assertThat(t.podeTransicionar(Map.of("valor", "1000"))).isFalse();
        }

        @Test
        @DisplayName("Condição com wildcard deve funcionar")
        void wildcardConditionShouldWork() {
            TransicaoWorkflow t = transicaoValida();
            t.adicionarCondicao("codigo", "SIN*");
            assertThat(t.podeTransicionar(Map.of("codigo", "SIN-001"))).isTrue();
            assertThat(t.podeTransicionar(Map.of("codigo", "DOC-001"))).isFalse();
        }

        @Test
        @DisplayName("Chave ausente com valor não-null deve retornar false")
        void missingKeyWithNonNullValueShouldReturnFalse() {
            TransicaoWorkflow t = transicaoValida();
            t.adicionarCondicao("status", "APROVADO");
            assertThat(t.podeTransicionar(Map.of())).isFalse();
        }

        @Test
        @DisplayName("Chave ausente com valor 'null' deve retornar true")
        void missingKeyWithNullValueShouldReturnTrue() {
            TransicaoWorkflow t = transicaoValida();
            t.adicionarCondicao("status", "null");
            assertThat(t.podeTransicionar(Map.of())).isTrue();
        }
    }

    // =========================================================================
    // ativar / desativar / isAtivo
    // =========================================================================

    @Nested
    @DisplayName("ativar() / desativar() / isAtivo()")
    class AtivarDesativar {

        @Test
        @DisplayName("Transição deve ser ativa por padrão")
        void transitionShouldBeActiveByDefault() {
            assertThat(transicaoValida().isAtivo()).isTrue();
        }

        @Test
        @DisplayName("desativar deve marcar como inativo")
        void desativarShouldMarkAsInactive() {
            TransicaoWorkflow t = transicaoValida();
            t.desativar();
            assertThat(t.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("ativar deve restaurar ativo")
        void ativarShouldRestoreActive() {
            TransicaoWorkflow t = transicaoValida();
            t.desativar();
            t.ativar();
            assertThat(t.isAtivo()).isTrue();
        }
    }

    // =========================================================================
    // adicionarCondicao / removerCondicao / hasCondicoes
    // =========================================================================

    @Nested
    @DisplayName("Gerenciamento de condições")
    class GerenciarCondicoes {

        @Test
        @DisplayName("adicionarCondicao deve adicionar ao mapa")
        void adicionarCondicaoShouldAddToMap() {
            TransicaoWorkflow t = transicaoValida();
            t.adicionarCondicao("chave", "valor");
            assertThat(t.hasCondicoes()).isTrue();
            assertThat(t.getCondicoes()).containsEntry("chave", "valor");
        }

        @Test
        @DisplayName("removerCondicao deve remover do mapa")
        void removerCondicaoShouldRemoveFromMap() {
            TransicaoWorkflow t = transicaoValida();
            t.adicionarCondicao("chave", "valor");
            t.removerCondicao("chave");
            assertThat(t.hasCondicoes()).isFalse();
        }

        @Test
        @DisplayName("hasCondicoes deve retornar false quando mapa vazio")
        void hasCondicoesShouldReturnFalseWhenEmpty() {
            assertThat(transicaoValida().hasCondicoes()).isFalse();
        }
    }

    @Test
    @DisplayName("toString deve retornar representação não nula")
    void toStringShouldReturnNonNull() {
        assertThat(transicaoValida().toString()).isNotNull();
    }
}
