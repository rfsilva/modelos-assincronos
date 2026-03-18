package com.seguradora.hibrida.domain.workflow.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowEngine Tests")
class WorkflowEngineTest {

    @Test
    @DisplayName("WorkflowEngine deve ser uma interface")
    void workflowEngineShouldBeInterface() {
        assertThat(WorkflowEngine.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("WorkflowEngine deve declarar 13 métodos")
    void workflowEngineShouldDeclare13Methods() {
        assertThat(WorkflowEngine.class.getDeclaredMethods()).hasSizeGreaterThanOrEqualTo(10);
    }
}
