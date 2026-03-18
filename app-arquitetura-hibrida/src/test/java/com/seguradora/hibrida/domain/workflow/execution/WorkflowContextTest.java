package com.seguradora.hibrida.domain.workflow.execution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowContext Tests")
class WorkflowContextTest {

    private WorkflowContext contextoPadrao() {
        return WorkflowContext.builder()
                .sinistroId("SIN-001")
                .valorIndenizacao(new BigDecimal("5000.00"))
                .tipoSinistro("SIMPLES")
                .build();
    }

    @Test
    @DisplayName("Builder deve criar instância com todos os campos")
    void builderShouldCreateInstanceWithAllFields() {
        WorkflowContext ctx = contextoPadrao();
        assertThat(ctx.getSinistroId()).isEqualTo("SIN-001");
        assertThat(ctx.getValorIndenizacao()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(ctx.getTipoSinistro()).isEqualTo("SIMPLES");
    }

    @Test
    @DisplayName("Construtor sem args deve criar contexto com mapas vazios")
    void noArgConstructorShouldCreateContextWithEmptyMaps() {
        WorkflowContext ctx = new WorkflowContext();
        assertThat(ctx.getSinistroId()).isNull();
        assertThat(ctx.getDadosSinistro()).isNotNull();
        assertThat(ctx.getVariaveisWorkflow()).isNotNull();
    }

    // =========================================================================
    // get / set / setDadoSinistro
    // =========================================================================

    @Nested
    @DisplayName("get() / set() / setDadoSinistro()")
    class GetSet {

        @Test
        @DisplayName("set() deve adicionar variável ao workflow")
        void setShouldAddWorkflowVariable() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("status", "APROVADO");
            assertThat(ctx.get("status")).isEqualTo("APROVADO");
        }

        @Test
        @DisplayName("setDadoSinistro() deve adicionar dado ao sinistro")
        void setDadoSinistroShouldAddToSinistroData() {
            WorkflowContext ctx = contextoPadrao();
            ctx.setDadoSinistro("placa", "ABC-1234");
            assertThat(ctx.get("placa")).isEqualTo("ABC-1234");
        }

        @Test
        @DisplayName("variaveisWorkflow deve ter prioridade sobre dadosSinistro")
        void workflowVariablesShouldTakePriorityOverSinistroData() {
            WorkflowContext ctx = contextoPadrao();
            ctx.setDadoSinistro("chave", "valor-sinistro");
            ctx.set("chave", "valor-workflow");
            assertThat(ctx.get("chave")).isEqualTo("valor-workflow");
        }

        @Test
        @DisplayName("get() deve retornar null para chave inexistente")
        void getShouldReturnNullForMissingKey() {
            WorkflowContext ctx = contextoPadrao();
            assertThat(ctx.get("inexistente")).isNull();
        }
    }

    // =========================================================================
    // containsKey / removeVariavel / limparVariaveis
    // =========================================================================

    @Nested
    @DisplayName("containsKey() / removeVariavel() / limparVariaveis()")
    class KeyManagement {

        @Test
        @DisplayName("containsKey deve retornar true quando chave existe")
        void containsKeyShouldReturnTrueWhenKeyExists() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("var1", "x");
            assertThat(ctx.containsKey("var1")).isTrue();
        }

        @Test
        @DisplayName("containsKey deve retornar false quando chave não existe")
        void containsKeyShouldReturnFalseWhenKeyMissing() {
            WorkflowContext ctx = contextoPadrao();
            assertThat(ctx.containsKey("inexistente")).isFalse();
        }

        @Test
        @DisplayName("removeVariavel deve remover variável do workflow")
        void removeVariavelShouldRemoveVariable() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("var1", "x");
            ctx.removeVariavel("var1");
            assertThat(ctx.containsKey("var1")).isFalse();
        }

        @Test
        @DisplayName("limparVariaveis deve limpar todas variáveis do workflow")
        void limparVariaveisShouldClearAllVariables() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("a", 1);
            ctx.set("b", 2);
            ctx.limparVariaveis();
            assertThat(ctx.getVariaveisWorkflow()).isEmpty();
        }
    }

    // =========================================================================
    // avaliarCondicao
    // =========================================================================

    @Nested
    @DisplayName("avaliarCondicao()")
    class AvaliarCondicao {

        @Test
        @DisplayName("Expressão vazia ou null deve retornar true")
        void emptyOrNullExpressionShouldReturnTrue() {
            WorkflowContext ctx = contextoPadrao();
            assertThat(ctx.avaliarCondicao(null)).isTrue();
            assertThat(ctx.avaliarCondicao("")).isTrue();
            assertThat(ctx.avaliarCondicao("   ")).isTrue();
        }

        @Test
        @DisplayName("Operador == deve comparar igualdade")
        void equalOperatorShouldCompareEquality() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("status", "APROVADO");
            assertThat(ctx.avaliarCondicao("status == APROVADO")).isTrue();
            assertThat(ctx.avaliarCondicao("status == REPROVADO")).isFalse();
        }

        @Test
        @DisplayName("Operador = deve comparar igualdade")
        void singleEqualOperatorShouldCompareEquality() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("tipo", "SIMPLES");
            assertThat(ctx.avaliarCondicao("tipo = SIMPLES")).isTrue();
        }

        @Test
        @DisplayName("Operador != deve comparar desigualdade")
        void notEqualOperatorShouldCompareInequality() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("status", "PENDENTE");
            assertThat(ctx.avaliarCondicao("status != APROVADO")).isTrue();
            assertThat(ctx.avaliarCondicao("status != PENDENTE")).isFalse();
        }

        @Test
        @DisplayName("Operadores numéricos devem funcionar")
        void numericOperatorsShouldWork() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("valor", "5000");
            assertThat(ctx.avaliarCondicao("valor > 4000")).isTrue();
            assertThat(ctx.avaliarCondicao("valor < 6000")).isTrue();
            assertThat(ctx.avaliarCondicao("valor >= 5000")).isTrue();
            assertThat(ctx.avaliarCondicao("valor <= 5000")).isTrue();
            assertThat(ctx.avaliarCondicao("valor > 5001")).isFalse();
        }

        @Test
        @DisplayName("Operador contains deve verificar substring")
        void containsOperatorShouldCheckSubstring() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("descricao", "Sinistro de veículo");
            assertThat(ctx.avaliarCondicao("descricao contains veículo")).isTrue();
            assertThat(ctx.avaliarCondicao("descricao contains casa")).isFalse();
        }

        @Test
        @DisplayName("Operador startsWith deve verificar prefixo")
        void startsWithOperatorShouldCheckPrefix() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("codigo", "SIN-001");
            assertThat(ctx.avaliarCondicao("codigo startsWith SIN")).isTrue();
            assertThat(ctx.avaliarCondicao("codigo startsWith DOC")).isFalse();
        }

        @Test
        @DisplayName("Operador endsWith deve verificar sufixo")
        void endsWithOperatorShouldCheckSuffix() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("codigo", "SIN-001");
            assertThat(ctx.avaliarCondicao("codigo endsWith 001")).isTrue();
            assertThat(ctx.avaliarCondicao("codigo endsWith 999")).isFalse();
        }

        @Test
        @DisplayName("Formato simples de chave deve verificar valor booleano")
        void simpleKeyFormatShouldCheckBooleanValue() {
            WorkflowContext ctx = contextoPadrao();
            ctx.set("ativo", true);
            assertThat(ctx.avaliarCondicao("ativo")).isTrue();
            ctx.set("inativo", false);
            assertThat(ctx.avaliarCondicao("inativo")).isFalse();
        }

        @Test
        @DisplayName("Chave inexistente com == null deve retornar true")
        void missingKeyEqualsNullShouldReturnTrue() {
            WorkflowContext ctx = contextoPadrao();
            assertThat(ctx.avaliarCondicao("inexistente == null")).isTrue();
        }
    }

    // =========================================================================
    // toMap / fromMap
    // =========================================================================

    @Nested
    @DisplayName("toMap() / fromMap()")
    class ToMapFromMap {

        @Test
        @DisplayName("toMap deve incluir campos principais")
        void toMapShouldIncludeMainFields() {
            WorkflowContext ctx = contextoPadrao();
            Map<String, Object> map = ctx.toMap();
            assertThat(map).containsKey("sinistroId");
            assertThat(map).containsKey("valorIndenizacao");
            assertThat(map).containsKey("tipoSinistro");
        }

        @Test
        @DisplayName("fromMap deve criar contexto a partir de mapa")
        void fromMapShouldCreateContextFromMap() {
            Map<String, Object> dados = Map.of(
                    "sinistroId", "SIN-002",
                    "tipoSinistro", "COMPLEXO",
                    "valorIndenizacao", new BigDecimal("15000.00"),
                    "campo", "extra"
            );
            WorkflowContext ctx = WorkflowContext.fromMap(dados);
            assertThat(ctx.getSinistroId()).isEqualTo("SIN-002");
            assertThat(ctx.getTipoSinistro()).isEqualTo("COMPLEXO");
            assertThat(ctx.get("campo")).isEqualTo("extra");
        }

        @Test
        @DisplayName("fromMap deve converter Number para BigDecimal")
        void fromMapShouldConvertNumberToBigDecimal() {
            Map<String, Object> dados = Map.of(
                    "sinistroId", "SIN-003",
                    "valorIndenizacao", 1000.0
            );
            WorkflowContext ctx = WorkflowContext.fromMap(dados);
            assertThat(ctx.getValorIndenizacao()).isNotNull();
        }
    }

    // =========================================================================
    // copy
    // =========================================================================

    @Test
    @DisplayName("copy deve criar cópia independente")
    void copyShouldCreateIndependentCopy() {
        WorkflowContext original = contextoPadrao();
        original.set("var", "original");

        WorkflowContext copia = original.copy();
        assertThat(copia.getSinistroId()).isEqualTo(original.getSinistroId());
        assertThat(copia.get("var")).isEqualTo("original");

        copia.set("var", "modificado");
        assertThat(original.get("var")).isEqualTo("original");
    }

    @Test
    @DisplayName("toString deve retornar representação não nula")
    void toStringShouldReturnNonNull() {
        assertThat(contextoPadrao().toString()).isNotNull();
    }
}
