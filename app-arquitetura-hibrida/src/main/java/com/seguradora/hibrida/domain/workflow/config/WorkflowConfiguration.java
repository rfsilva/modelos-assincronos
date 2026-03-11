package com.seguradora.hibrida.domain.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuração do sistema de workflows.
 * Define executores assíncronos, políticas de retry e agendamentos.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Configuration
@EnableAsync
@EnableScheduling
@EnableRetry
public class WorkflowConfiguration {

    /**
     * Executor de tarefas para workflows.
     * Pool de threads dedicado para processamento assíncrono de workflows.
     *
     * @return executor configurado
     */
    @Bean(name = "workflowTaskExecutor")
    public Executor workflowTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Pool size
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);

        // Thread naming
        executor.setThreadNamePrefix("workflow-exec-");

        // Política de rejeição: CallerRunsPolicy (executa na thread que chamou)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // Timeout para threads ociosas
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);

        // Aguarda conclusão das tarefas no shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        return executor;
    }

    /**
     * Executor para processamento de aprovações.
     * Pool menor e dedicado para não competir com workflows.
     *
     * @return executor configurado
     */
    @Bean(name = "aprovacaoTaskExecutor")
    public Executor aprovacaoTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("aprovacao-exec-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        return executor;
    }

    /**
     * Executor para tarefas agendadas (scheduled).
     * Pool pequeno para monitoramento e limpezas periódicas.
     *
     * @return executor configurado
     */
    @Bean(name = "scheduledTaskExecutor")
    public Executor scheduledTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("scheduled-exec-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        return executor;
    }

    /**
     * Template de retry para operações de workflow.
     * Configurado com backoff exponencial.
     *
     * @return retry template configurado
     */
    @Bean
    public RetryTemplate workflowRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Política de retry: máximo 3 tentativas
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Política de backoff: exponencial (1s, 2s, 4s)
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1 segundo
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000); // Máximo 10 segundos
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    /**
     * Template de retry para integrações externas.
     * Mais tentativas e intervalos maiores.
     *
     * @return retry template configurado
     */
    @Bean
    public RetryTemplate integracaoRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Política de retry: máximo 5 tentativas
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Política de backoff: exponencial com intervalos maiores
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000); // 2 segundos
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(30000); // Máximo 30 segundos
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    /**
     * Configuração de timeout padrão para etapas automáticas (em minutos).
     *
     * @return timeout padrão
     */
    @Bean(name = "workflowDefaultTimeoutMinutes")
    public Integer workflowDefaultTimeoutMinutes() {
        return 30;
    }

    /**
     * Configuração de timeout padrão para etapas manuais (em minutos).
     *
     * @return timeout padrão
     */
    @Bean(name = "workflowManualTimeoutMinutes")
    public Integer workflowManualTimeoutMinutes() {
        return 480; // 8 horas
    }

    /**
     * Configuração de timeout padrão para aprovações (em minutos).
     *
     * @return timeout padrão
     */
    @Bean(name = "aprovacaoTimeoutMinutes")
    public Integer aprovacaoTimeoutMinutes() {
        return 4320; // 72 horas (3 dias)
    }

    /**
     * Tamanho máximo da fila de workflows pendentes.
     *
     * @return tamanho máximo
     */
    @Bean(name = "workflowMaxQueueSize")
    public Integer workflowMaxQueueSize() {
        return 1000;
    }

    /**
     * Intervalo de limpeza de workflows antigos (em dias).
     *
     * @return dias para limpeza
     */
    @Bean(name = "workflowCleanupDays")
    public Integer workflowCleanupDays() {
        return 90; // 90 dias
    }

    /**
     * Configuração de prioridade para tipos de sinistro.
     * Maior prioridade = processado primeiro.
     *
     * @return mapa de prioridades
     */
    @Bean(name = "workflowPriorities")
    public java.util.Map<String, Integer> workflowPriorities() {
        java.util.Map<String, Integer> priorities = new java.util.HashMap<>();
        priorities.put("ROUBO_FURTO", 1); // Prioridade máxima
        priorities.put("COMPLEXO", 2);
        priorities.put("TERCEIROS", 3);
        priorities.put("SIMPLES", 4);
        return priorities;
    }

    /**
     * Habilita cache de workflows em memória.
     *
     * @return true se habilitado
     */
    @Bean(name = "workflowCacheEnabled")
    public Boolean workflowCacheEnabled() {
        return true;
    }

    /**
     * Tamanho máximo do cache de workflows.
     *
     * @return tamanho máximo
     */
    @Bean(name = "workflowCacheMaxSize")
    public Integer workflowCacheMaxSize() {
        return 500;
    }

    /**
     * Tempo de expiração do cache em minutos.
     *
     * @return minutos de expiração
     */
    @Bean(name = "workflowCacheExpirationMinutes")
    public Integer workflowCacheExpirationMinutes() {
        return 60;
    }

    /**
     * Habilita métricas detalhadas de workflows.
     *
     * @return true se habilitado
     */
    @Bean(name = "workflowMetricsEnabled")
    public Boolean workflowMetricsEnabled() {
        return true;
    }

    /**
     * Habilita logging detalhado de workflows.
     *
     * @return true se habilitado
     */
    @Bean(name = "workflowDebugLoggingEnabled")
    public Boolean workflowDebugLoggingEnabled() {
        return false; // Desabilitado em produção
    }

    /**
     * Intervalo de monitoramento de SLA em minutos.
     *
     * @return intervalo em minutos
     */
    @Bean(name = "slaMonitoringIntervalMinutes")
    public Integer slaMonitoringIntervalMinutes() {
        return 60; // A cada hora
    }

    /**
     * Habilita notificações de SLA.
     *
     * @return true se habilitado
     */
    @Bean(name = "slaNotificationsEnabled")
    public Boolean slaNotificationsEnabled() {
        return true;
    }

    /**
     * Habilita escalação automática por SLA.
     *
     * @return true se habilitado
     */
    @Bean(name = "slaAutoEscalationEnabled")
    public Boolean slaAutoEscalationEnabled() {
        return true;
    }
}
