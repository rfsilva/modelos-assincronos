package com.seguradora.hibrida.domain.workflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EtapaWorkflow Tests")
class EtapaWorkflowTest {

    private EtapaWorkflow etapaAutomaticaValida() {
        return EtapaWorkflow.builder()
                .id("etapa-01")
                .nome("Validação Inicial")
                .tipo(TipoEtapa.AUTOMATICA)
                .ordem(1)
                .build();
    }

    private EtapaWorkflow etapaAprovacaoValida() {
        return EtapaWorkflow.builder()
                .id("etapa-02")
                .nome("Aprovação Gerente")
                .tipo(TipoEtapa.APROVACAO)
                .ordem(2)
                .nivelAprovacao(NivelAprovacao.NIVEL_3_GERENTE)
                .build();
    }

    // =========================================================================
    // validar
    // =========================================================================

    @Nested
    @DisplayName("validar()")
    class Validar {

        @Test
        @DisplayName("Etapa válida não deve lançar exceção")
        void validEtapaShouldNotThrow() {
            etapaAutomaticaValida().validar();
        }

        @Test
        @DisplayName("Nome nulo deve lançar exceção")
        void nullNameShouldThrow() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .tipo(TipoEtapa.AUTOMATICA).ordem(1).build();
            assertThatThrownBy(etapa::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Nome");
        }

        @Test
        @DisplayName("Nome vazio deve lançar exceção")
        void emptyNameShouldThrow() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .nome("  ").tipo(TipoEtapa.AUTOMATICA).ordem(1).build();
            assertThatThrownBy(etapa::validar)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Tipo nulo deve lançar exceção")
        void nullTypeShouldThrow() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .nome("Etapa").ordem(1).build();
            assertThatThrownBy(etapa::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tipo");
        }

        @Test
        @DisplayName("Ordem nula deve lançar exceção")
        void nullOrderShouldThrow() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .nome("Etapa").tipo(TipoEtapa.AUTOMATICA).build();
            assertThatThrownBy(etapa::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Ordem");
        }

        @Test
        @DisplayName("Ordem zero deve lançar exceção")
        void zeroOrderShouldThrow() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .nome("Etapa").tipo(TipoEtapa.AUTOMATICA).ordem(0).build();
            assertThatThrownBy(etapa::validar)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Etapa APROVACAO sem nivelAprovacao deve lançar exceção")
        void aprovacaoWithoutNivelShouldThrow() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .nome("Aprovação").tipo(TipoEtapa.APROVACAO).ordem(1).build();
            assertThatThrownBy(etapa::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("nível");
        }

        @Test
        @DisplayName("Timeout zero deve lançar exceção")
        void zeroTimeoutShouldThrow() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .nome("Etapa").tipo(TipoEtapa.AUTOMATICA).ordem(1).timeoutMinutos(0).build();
            assertThatThrownBy(etapa::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Timeout");
        }

        @Test
        @DisplayName("maxTentativas zero deve lançar exceção")
        void zeroMaxTentativasShouldThrow() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .nome("Etapa").tipo(TipoEtapa.AUTOMATICA).ordem(1).maxTentativas(0).build();
            assertThatThrownBy(etapa::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("tentativas");
        }
    }

    // =========================================================================
    // podeExecutar
    // =========================================================================

    @Nested
    @DisplayName("podeExecutar()")
    class PodeExecutar {

        @Test
        @DisplayName("Sem condições deve sempre poder executar")
        void withNoConditionsShouldAlwaysExecute() {
            EtapaWorkflow etapa = etapaAutomaticaValida();
            assertThat(etapa.podeExecutar(Map.of())).isTrue();
        }

        @Test
        @DisplayName("Deve executar quando condição de igualdade é satisfeita")
        void shouldExecuteWhenEqualityConditionMet() {
            EtapaWorkflow etapa = etapaAutomaticaValida();
            etapa.adicionarCondicao("status", "APROVADO");
            assertThat(etapa.podeExecutar(Map.of("status", "APROVADO"))).isTrue();
            assertThat(etapa.podeExecutar(Map.of("status", "PENDENTE"))).isFalse();
        }

        @Test
        @DisplayName("Deve suportar condições numéricas com >")
        void shouldSupportGreaterThanCondition() {
            EtapaWorkflow etapa = etapaAutomaticaValida();
            etapa.adicionarCondicao("valor", ">1000");
            assertThat(etapa.podeExecutar(Map.of("valor", "2000"))).isTrue();
            assertThat(etapa.podeExecutar(Map.of("valor", "500"))).isFalse();
        }

        @Test
        @DisplayName("Deve suportar condições numéricas com <")
        void shouldSupportLessThanCondition() {
            EtapaWorkflow etapa = etapaAutomaticaValida();
            etapa.adicionarCondicao("valor", "<1000");
            assertThat(etapa.podeExecutar(Map.of("valor", "500"))).isTrue();
            assertThat(etapa.podeExecutar(Map.of("valor", "2000"))).isFalse();
        }

        @Test
        @DisplayName("Deve suportar condições com !=")
        void shouldSupportNotEqualCondition() {
            EtapaWorkflow etapa = etapaAutomaticaValida();
            etapa.adicionarCondicao("status", "!=CANCELADO");
            assertThat(etapa.podeExecutar(Map.of("status", "APROVADO"))).isTrue();
            assertThat(etapa.podeExecutar(Map.of("status", "CANCELADO"))).isFalse();
        }

        @Test
        @DisplayName("Chave ausente deve retornar false quando valor não é null")
        void missingKeyShouldReturnFalseForNonNullValue() {
            EtapaWorkflow etapa = etapaAutomaticaValida();
            etapa.adicionarCondicao("status", "ATIVO");
            assertThat(etapa.podeExecutar(Map.of())).isFalse();
        }
    }

    // =========================================================================
    // getAcoesAutomaticas / getAcoesManuais
    // =========================================================================

    @Nested
    @DisplayName("getAcoesAutomaticas() / getAcoesManuais()")
    class Acoes {

        @Test
        @DisplayName("getAcoesAutomaticas deve filtrar ações sem prefixo MANUAL:")
        void getAcoesAutomaticasShouldFilterNonManualActions() {
            EtapaWorkflow etapa = etapaAutomaticaValida();
            etapa.adicionarAcao("CALCULAR_VALOR");
            etapa.adicionarAcao("MANUAL:REVISAR_DOCUMENTOS");
            List<String> automaticas = etapa.getAcoesAutomaticas();
            assertThat(automaticas).containsExactly("CALCULAR_VALOR");
        }

        @Test
        @DisplayName("getAcoesManuais deve filtrar ações com prefixo MANUAL: e removê-lo")
        void getAcoesManuaisShouldFilterAndRemoveManualPrefix() {
            EtapaWorkflow etapa = etapaAutomaticaValida();
            etapa.adicionarAcao("CALCULAR_VALOR");
            etapa.adicionarAcao("MANUAL:REVISAR_DOCUMENTOS");
            List<String> manuais = etapa.getAcoesManuais();
            assertThat(manuais).containsExactly("REVISAR_DOCUMENTOS");
        }
    }

    // =========================================================================
    // Tipo checks
    // =========================================================================

    @Nested
    @DisplayName("Tipo checks")
    class TipoChecks {

        @Test
        @DisplayName("isAutomatica deve retornar true apenas para AUTOMATICA")
        void isAutomaticaShouldReturnTrueOnlyForAutomatic() {
            assertThat(etapaAutomaticaValida().isAutomatica()).isTrue();
            assertThat(etapaAprovacaoValida().isAutomatica()).isFalse();
        }

        @Test
        @DisplayName("isManual deve retornar true para MANUAL")
        void isManualShouldReturnTrueForManual() {
            EtapaWorkflow manual = EtapaWorkflow.builder()
                    .nome("Manual").tipo(TipoEtapa.MANUAL).ordem(1).build();
            assertThat(manual.isManual()).isTrue();
            assertThat(etapaAutomaticaValida().isManual()).isFalse();
        }

        @Test
        @DisplayName("isIntegracao deve retornar true para INTEGRACAO")
        void isIntegracaoShouldReturnTrueForIntegracao() {
            EtapaWorkflow integ = EtapaWorkflow.builder()
                    .nome("Integração").tipo(TipoEtapa.INTEGRACAO).ordem(1).build();
            assertThat(integ.isIntegracao()).isTrue();
        }

        @Test
        @DisplayName("requerAprovacao deve retornar true para APROVACAO")
        void requerAprovacaoShouldReturnTrueForAprovacao() {
            assertThat(etapaAprovacaoValida().requerAprovacao()).isTrue();
            assertThat(etapaAutomaticaValida().requerAprovacao()).isFalse();
        }

        @Test
        @DisplayName("hasTimeout deve retornar true quando timeoutMinutos definido")
        void hasTimeoutShouldReturnTrueWhenTimeoutDefined() {
            EtapaWorkflow comTimeout = EtapaWorkflow.builder()
                    .nome("Etapa").tipo(TipoEtapa.AUTOMATICA).ordem(1).timeoutMinutos(30).build();
            assertThat(comTimeout.hasTimeout()).isTrue();
            assertThat(etapaAutomaticaValida().hasTimeout()).isFalse();
        }

        @Test
        @DisplayName("isPermiteRetry deve retornar true por padrão")
        void isPermiteRetryShouldReturnTrueByDefault() {
            assertThat(etapaAutomaticaValida().isPermiteRetry()).isTrue();
        }

        @Test
        @DisplayName("isObrigatoria deve retornar true por padrão")
        void isObrigatoriaShouldReturnTrueByDefault() {
            assertThat(etapaAutomaticaValida().isObrigatoria()).isTrue();
        }

        @Test
        @DisplayName("isValida deve retornar true para etapa válida")
        void isValidaShouldReturnTrueForValidEtapa() {
            assertThat(etapaAutomaticaValida().isValida()).isTrue();
        }

        @Test
        @DisplayName("isValida deve retornar false para etapa inválida")
        void isValidaShouldReturnFalseForInvalidEtapa() {
            EtapaWorkflow invalida = EtapaWorkflow.builder().build();
            assertThat(invalida.isValida()).isFalse();
        }
    }

    // =========================================================================
    // clonar
    // =========================================================================

    @Test
    @DisplayName("clonar deve criar nova instância independente com novo ID")
    void clonarShouldCreateIndependentInstanceWithNewId() {
        EtapaWorkflow original = etapaAutomaticaValida();
        original.adicionarAcao("ACAO_1");
        original.adicionarCondicao("chave", "valor");

        EtapaWorkflow clone = original.clonar();
        assertThat(clone.getId()).isNotEqualTo(original.getId());
        assertThat(clone.getNome()).isEqualTo(original.getNome());
        assertThat(clone.getTipo()).isEqualTo(original.getTipo());
        assertThat(clone.getOrdem()).isEqualTo(original.getOrdem());
        assertThat(clone.getAcoes()).contains("ACAO_1");
        assertThat(clone.getCondicoes()).containsKey("chave");
    }

    @Test
    @DisplayName("toString deve retornar representação não nula")
    void toStringShouldReturnNonNull() {
        assertThat(etapaAutomaticaValida().toString()).isNotNull();
    }
}
