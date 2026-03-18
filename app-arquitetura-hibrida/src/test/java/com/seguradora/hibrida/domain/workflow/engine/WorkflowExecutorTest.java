package com.seguradora.hibrida.domain.workflow.engine;

import com.seguradora.hibrida.domain.workflow.execution.WorkflowContext;
import com.seguradora.hibrida.domain.workflow.execution.WorkflowResult;
import com.seguradora.hibrida.domain.workflow.model.EtapaWorkflow;
import com.seguradora.hibrida.domain.workflow.model.NivelAprovacao;
import com.seguradora.hibrida.domain.workflow.model.TipoEtapa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowExecutor Tests")
class WorkflowExecutorTest {

    private WorkflowExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new WorkflowExecutor();
    }

    private WorkflowContext contextoBasico() {
        return WorkflowContext.builder()
                .sinistroId("SIN-001")
                .tipoSinistro("SIMPLES")
                .valorIndenizacao(new BigDecimal("5000.00"))
                .build();
    }

    private EtapaWorkflow etapaAutomatica() {
        return EtapaWorkflow.builder()
                .id("et-01")
                .nome("Auto Etapa")
                .tipo(TipoEtapa.AUTOMATICA)
                .ordem(1)
                .build();
    }

    // =========================================================================
    // executeEtapa - AUTOMATICA
    // =========================================================================

    @Nested
    @DisplayName("executeEtapa() - AUTOMATICA")
    class ExecuteEtapaAutomatica {

        @Test
        @DisplayName("Deve executar etapa automática sem ações")
        void shouldExecuteAutoEtapaWithNoActions() {
            EtapaWorkflow etapa = etapaAutomatica();
            WorkflowResult result = executor.executeEtapa(etapa, contextoBasico());
            assertThat(result.isSucesso()).isTrue();
        }

        @Test
        @DisplayName("Deve executar etapa automática com ações CALCULAR")
        void shouldExecuteAutoEtapaWithCalcActions() {
            EtapaWorkflow etapa = etapaAutomatica();
            etapa.adicionarAcao("CALCULAR:valor_indenizacao");
            WorkflowResult result = executor.executeEtapa(etapa, contextoBasico());
            assertThat(result.isSucesso()).isTrue();
        }

        @Test
        @DisplayName("Deve executar etapa automática com ações VALIDAR")
        void shouldExecuteAutoEtapaWithValidarActions() {
            EtapaWorkflow etapa = etapaAutomatica();
            etapa.adicionarAcao("VALIDAR:sinistroId");
            WorkflowContext ctx = contextoBasico();
            ctx.set("sinistroId", "SIN-001");
            WorkflowResult result = executor.executeEtapa(etapa, ctx);
            assertThat(result.isSucesso()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar falha quando campo obrigatório ausente")
        void shouldFailWhenRequiredFieldMissing() {
            EtapaWorkflow etapa = etapaAutomatica();
            etapa.adicionarAcao("VALIDAR:campoObrigatorio");
            WorkflowResult result = executor.executeEtapa(etapa, contextoBasico());
            assertThat(result.isFalha()).isTrue();
        }

        @Test
        @DisplayName("Deve executar ação NOTIFICAR")
        void shouldExecuteNotifyAction() {
            EtapaWorkflow etapa = etapaAutomatica();
            etapa.adicionarAcao("NOTIFICAR:segurado");
            WorkflowResult result = executor.executeEtapa(etapa, contextoBasico());
            assertThat(result.isSucesso()).isTrue();
        }

        @Test
        @DisplayName("Deve executar ação REGISTRAR")
        void shouldExecuteRegisterAction() {
            EtapaWorkflow etapa = etapaAutomatica();
            etapa.adicionarAcao("REGISTRAR:evento_teste");
            WorkflowResult result = executor.executeEtapa(etapa, contextoBasico());
            assertThat(result.isSucesso()).isTrue();
        }

        @Test
        @DisplayName("Deve executar ação SET")
        void shouldExecuteSetAction() {
            EtapaWorkflow etapa = etapaAutomatica();
            etapa.adicionarAcao("SET:status=PROCESSADO");
            WorkflowContext ctx = contextoBasico();
            WorkflowResult result = executor.executeEtapa(etapa, ctx);
            assertThat(result.isSucesso()).isTrue();
        }
    }

    // =========================================================================
    // executeEtapa - MANUAL
    // =========================================================================

    @Nested
    @DisplayName("executeEtapa() - MANUAL")
    class ExecuteEtapaManual {

        @Test
        @DisplayName("Deve retornar sucesso indicando espera por ação manual")
        void shouldReturnSuccessWaitingForManualAction() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .id("et-02").nome("Manual").tipo(TipoEtapa.MANUAL).ordem(1).build();
            etapa.adicionarAcao("MANUAL:Revisar documentos");
            WorkflowResult result = executor.executeEtapa(etapa, contextoBasico());
            assertThat(result.isSucesso()).isTrue();
            assertThat(result.getDado("aguardandoAcaoManual")).isEqualTo(true);
        }
    }

    // =========================================================================
    // executeEtapa - APROVACAO
    // =========================================================================

    @Nested
    @DisplayName("executeEtapa() - APROVACAO")
    class ExecuteEtapaAprovacao {

        @Test
        @DisplayName("Deve aguardar aprovação quando status não definido")
        void shouldWaitForAprovacaoWhenStatusNotDefined() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .id("et-03").nome("Aprovação").tipo(TipoEtapa.APROVACAO).ordem(1)
                    .nivelAprovacao(NivelAprovacao.NIVEL_1_ANALISTA).build();
            WorkflowResult result = executor.executeEtapa(etapa, contextoBasico());
            assertThat(result.isSucesso()).isTrue();
            assertThat(result.getDado("aguardandoAprovacao")).isEqualTo(true);
        }

        @Test
        @DisplayName("Deve retornar sucesso quando aprovação já concedida")
        void shouldReturnSuccessWhenAlreadyApproved() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .id("et-03").nome("Aprovação").tipo(TipoEtapa.APROVACAO).ordem(1)
                    .nivelAprovacao(NivelAprovacao.NIVEL_1_ANALISTA).build();
            WorkflowContext ctx = contextoBasico();
            ctx.set("aprovacao_status", "APROVADO");
            WorkflowResult result = executor.executeEtapa(etapa, ctx);
            assertThat(result.isSucesso()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar falha quando aprovação rejeitada")
        void shouldReturnFailureWhenRejected() {
            EtapaWorkflow etapa = EtapaWorkflow.builder()
                    .id("et-03").nome("Aprovação").tipo(TipoEtapa.APROVACAO).ordem(1)
                    .nivelAprovacao(NivelAprovacao.NIVEL_1_ANALISTA).build();
            WorkflowContext ctx = contextoBasico();
            ctx.set("aprovacao_status", "REJEITADO");
            WorkflowResult result = executor.executeEtapa(etapa, ctx);
            assertThat(result.isFalha()).isTrue();
        }
    }

    // =========================================================================
    // executeEtapa - condições
    // =========================================================================

    @Test
    @DisplayName("Deve falhar quando condições não satisfeitas")
    void shouldFailWhenConditionsNotMet() {
        EtapaWorkflow etapa = etapaAutomatica();
        etapa.adicionarCondicao("status", "APROVADO");
        WorkflowContext ctx = contextoBasico();
        ctx.set("status", "PENDENTE");
        WorkflowResult result = executor.executeEtapa(etapa, ctx);
        assertThat(result.isFalha()).isTrue();
    }

    // =========================================================================
    // executeAcaoAutomatica (public)
    // =========================================================================

    @Nested
    @DisplayName("executeAcaoAutomatica()")
    class ExecuteAcaoAutomatica {

        @Test
        @DisplayName("Deve falhar para formato de ação inválido")
        void shouldFailForInvalidActionFormat() {
            WorkflowResult result = executor.executeAcaoAutomatica("FORMATO_INVALIDO", contextoBasico());
            assertThat(result.isFalha()).isTrue();
        }

        @Test
        @DisplayName("Tipo desconhecido deve retornar sucesso (ação ignorada)")
        void unknownTypeShouldReturnSuccess() {
            WorkflowResult result = executor.executeAcaoAutomatica("DESCONHECIDO:parametro", contextoBasico());
            assertThat(result.isSucesso()).isTrue();
        }
    }

    // =========================================================================
    // validateCondicoes (public)
    // =========================================================================

    @Nested
    @DisplayName("validateCondicoes()")
    class ValidateCondicoes {

        @Test
        @DisplayName("Mapa vazio deve retornar true")
        void emptyMapShouldReturnTrue() {
            assertThat(executor.validateCondicoes(Map.of(), contextoBasico())).isTrue();
        }

        @Test
        @DisplayName("Mapa null deve retornar true")
        void nullMapShouldReturnTrue() {
            assertThat(executor.validateCondicoes(null, contextoBasico())).isTrue();
        }

        @Test
        @DisplayName("Condição satisfeita deve retornar true")
        void satisfiedConditionShouldReturnTrue() {
            WorkflowContext ctx = contextoBasico();
            ctx.set("status", "ATIVO");
            assertThat(executor.validateCondicoes(Map.of("status", "== ATIVO"), ctx)).isTrue();
        }
    }
}
