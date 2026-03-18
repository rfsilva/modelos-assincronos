package com.seguradora.hibrida.domain.workflow.defaults;

import com.seguradora.hibrida.domain.workflow.model.WorkflowDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowTemplates Tests")
class WorkflowTemplatesTest {

    @Test
    @DisplayName("criarWorkflowSinistroSimples deve criar workflow válido com 4 etapas")
    void criarWorkflowSinistroSimplesShouldCreateValidWorkflowWith4Etapas() {
        WorkflowDefinition workflow = WorkflowTemplates.criarWorkflowSinistroSimples();
        assertThat(workflow).isNotNull();
        assertThat(workflow.getNome()).isNotBlank();
        assertThat(workflow.getTipoSinistro()).isEqualTo("SIMPLES");
        assertThat(workflow.totalEtapas()).isEqualTo(4);
        assertThat(workflow.isAtivo()).isTrue();
        assertThat(workflow.getVersao()).isEqualTo(1);
    }

    @Test
    @DisplayName("criarWorkflowSinistroComplexo deve criar workflow com 6 etapas")
    void criarWorkflowSinistroComplexoShouldCreateWorkflowWith6Etapas() {
        WorkflowDefinition workflow = WorkflowTemplates.criarWorkflowSinistroComplexo();
        assertThat(workflow).isNotNull();
        assertThat(workflow.getTipoSinistro()).isEqualTo("COMPLEXO");
        assertThat(workflow.totalEtapas()).isEqualTo(6);
        assertThat(workflow.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("criarWorkflowRouboFurto deve criar workflow para ROUBO_FURTO")
    void criarWorkflowRouboFurtoShouldCreateWorkflowForRouboFurto() {
        WorkflowDefinition workflow = WorkflowTemplates.criarWorkflowRouboFurto();
        assertThat(workflow).isNotNull();
        assertThat(workflow.getTipoSinistro()).isEqualTo("ROUBO_FURTO");
        assertThat(workflow.totalEtapas()).isGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("criarWorkflowTerceiros deve criar workflow para TERCEIROS")
    void criarWorkflowTerceirosShouldCreateWorkflowForTerceiros() {
        WorkflowDefinition workflow = WorkflowTemplates.criarWorkflowTerceiros();
        assertThat(workflow).isNotNull();
        assertThat(workflow.getTipoSinistro()).isEqualTo("TERCEIROS");
        assertThat(workflow.totalEtapas()).isGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("listarTodos deve retornar todos os templates")
    void listarTodosShouldReturnAllTemplates() {
        List<WorkflowDefinition> todos = WorkflowTemplates.listarTodos();
        assertThat(todos).isNotEmpty();
        assertThat(todos).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("Cada template deve ter etapas com ordens sequenciais")
    void eachTemplateShouldHaveSequentialOrders() {
        List<WorkflowDefinition> todos = WorkflowTemplates.listarTodos();
        for (WorkflowDefinition wf : todos) {
            for (int i = 0; i < wf.totalEtapas(); i++) {
                assertThat(wf.buscarEtapaPorOrdem(i + 1))
                        .as("Workflow %s deve ter etapa na ordem %d", wf.getNome(), i + 1)
                        .isNotNull();
            }
        }
    }

    @Test
    @DisplayName("WorkflowSimples primeira etapa deve ser AUTOMATICA")
    void simplesFirstEtapaShouldBeAutomatic() {
        WorkflowDefinition workflow = WorkflowTemplates.criarWorkflowSinistroSimples();
        assertThat(workflow.primeiraEtapa().isAutomatica()).isTrue();
    }

    @Test
    @DisplayName("WorkflowSimples deve ter etapa de aprovação de nível 1")
    void simplesWorkflowShouldHaveLevel1AprovacaoEtapa() {
        WorkflowDefinition workflow = WorkflowTemplates.criarWorkflowSinistroSimples();
        boolean hasAprovacao = workflow.getEtapas().stream()
                .anyMatch(e -> e.requerAprovacao());
        assertThat(hasAprovacao).isTrue();
    }
}
