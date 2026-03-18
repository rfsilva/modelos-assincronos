package com.seguradora.hibrida.domain.workflow.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowConfiguration Tests")
class WorkflowConfigurationTest {

    private WorkflowConfiguration config;

    @BeforeEach
    void setUp() {
        config = new WorkflowConfiguration();
    }

    @Test
    @DisplayName("workflowTaskExecutor deve criar executor com configurações corretas")
    void workflowTaskExecutorShouldCreateWithCorrectConfig() {
        Executor executor = config.workflowTaskExecutor();
        assertThat(executor).isNotNull();
        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertThat(taskExecutor.getCorePoolSize()).isEqualTo(5);
        assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(10);
        assertThat(taskExecutor.getQueueCapacity()).isEqualTo(100);
    }

    @Test
    @DisplayName("aprovacaoTaskExecutor deve criar executor para aprovações")
    void aprovacaoTaskExecutorShouldCreateExecutor() {
        Executor executor = config.aprovacaoTaskExecutor();
        assertThat(executor).isNotNull();
        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertThat(taskExecutor.getCorePoolSize()).isEqualTo(2);
        assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("scheduledTaskExecutor deve criar executor para tarefas agendadas")
    void scheduledTaskExecutorShouldCreateExecutor() {
        Executor executor = config.scheduledTaskExecutor();
        assertThat(executor).isNotNull();
        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
    }

    @Test
    @DisplayName("workflowRetryTemplate deve criar RetryTemplate com 3 tentativas")
    void workflowRetryTemplateShouldCreateWithThreeAttempts() {
        RetryTemplate template = config.workflowRetryTemplate();
        assertThat(template).isNotNull();
    }

    @Test
    @DisplayName("integracaoRetryTemplate deve criar RetryTemplate com 5 tentativas")
    void integracaoRetryTemplateShouldCreateWithFiveAttempts() {
        RetryTemplate template = config.integracaoRetryTemplate();
        assertThat(template).isNotNull();
    }

    @Test
    @DisplayName("WorkflowConfiguration deve ter anotações corretas")
    void workflowConfigurationShouldHaveCorrectAnnotations() {
        assertThat(WorkflowConfiguration.class)
                .hasAnnotation(org.springframework.context.annotation.Configuration.class)
                .hasAnnotation(org.springframework.scheduling.annotation.EnableAsync.class)
                .hasAnnotation(org.springframework.scheduling.annotation.EnableScheduling.class)
                .hasAnnotation(org.springframework.retry.annotation.EnableRetry.class);
    }
}
