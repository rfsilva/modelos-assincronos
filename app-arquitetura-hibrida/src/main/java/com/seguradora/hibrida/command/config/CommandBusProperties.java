package com.seguradora.hibrida.command.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuração do Command Bus.
 * 
 * <p>Permite configurar comportamentos do Command Bus via application.yml:</p>
 * 
 * <pre>{@code
 * command-bus:
 *   default-timeout: 30
 *   async-pool-size: 10
 *   metrics-enabled: true
 *   validation-enabled: true
 *   retry:
 *     enabled: false
 *     max-attempts: 3
 *     delay-ms: 1000
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "command-bus")
public class CommandBusProperties {
    
    /**
     * Timeout padrão para execução de comandos em segundos.
     */
    private int defaultTimeout = 30;
    
    /**
     * Tamanho do pool de threads para execução assíncrona.
     */
    private int asyncPoolSize = 10;
    
    /**
     * Indica se métricas estão habilitadas.
     */
    private boolean metricsEnabled = true;
    
    /**
     * Indica se validação automática está habilitada.
     */
    private boolean validationEnabled = true;
    
    /**
     * Indica se logs detalhados estão habilitados.
     */
    private boolean detailedLogging = false;
    
    /**
     * Configurações de retry.
     */
    private Retry retry = new Retry();
    
    /**
     * Configurações de circuit breaker.
     */
    private CircuitBreaker circuitBreaker = new CircuitBreaker();
    
    /**
     * Configurações específicas de retry.
     */
    @Data
    public static class Retry {
        
        /**
         * Indica se retry está habilitado.
         */
        private boolean enabled = false;
        
        /**
         * Número máximo de tentativas.
         */
        private int maxAttempts = 3;
        
        /**
         * Delay inicial entre tentativas em milissegundos.
         */
        private long delayMs = 1000;
        
        /**
         * Multiplicador para backoff exponencial.
         */
        private double backoffMultiplier = 2.0;
        
        /**
         * Delay máximo entre tentativas em milissegundos.
         */
        private long maxDelayMs = 30000;
    }
    
    /**
     * Configurações de circuit breaker.
     */
    @Data
    public static class CircuitBreaker {
        
        /**
         * Indica se circuit breaker está habilitado.
         */
        private boolean enabled = false;
        
        /**
         * Número de falhas consecutivas para abrir o circuito.
         */
        private int failureThreshold = 5;
        
        /**
         * Tempo em milissegundos para tentar fechar o circuito.
         */
        private long recoveryTimeoutMs = 60000;
        
        /**
         * Percentual de sucesso necessário para fechar o circuito.
         */
        private double successThreshold = 0.8;
    }
}