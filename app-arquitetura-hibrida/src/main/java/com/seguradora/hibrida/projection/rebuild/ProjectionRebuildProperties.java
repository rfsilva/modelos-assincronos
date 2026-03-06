package com.seguradora.hibrida.projection.rebuild;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Propriedades de configuração para rebuild de projeções.
 */
@ConfigurationProperties(prefix = "cqrs.projection.rebuild")
@Validated
public class ProjectionRebuildProperties {
    
    /**
     * Se o rebuild automático está habilitado.
     */
    private boolean enabled = true;
    
    /**
     * Tamanho do batch para processamento durante rebuild.
     */
    @Min(value = 1, message = "Batch size deve ser pelo menos 1")
    private int batchSize = 100;
    
    /**
     * Threshold de lag em eventos para considerar rebuild necessário.
     */
    @Min(value = 1, message = "Lag threshold deve ser pelo menos 1")
    private long lagThresholdForRebuild = 10000L;
    
    /**
     * Threshold de lag para forçar rebuild completo ao invés de incremental.
     */
    @Min(value = 1, message = "Lag threshold para rebuild completo deve ser pelo menos 1")
    private long lagThresholdForFullRebuild = 50000L;
    
    /**
     * Threshold de taxa de erro para considerar rebuild necessário.
     */
    @Min(value = 0, message = "Error threshold não pode ser negativo")
    @Max(value = 1, message = "Error threshold não pode ser maior que 1")
    private double errorThresholdForRebuild = 0.1; // 10%
    
    /**
     * Threshold de taxa de erro para forçar rebuild completo.
     */
    @Min(value = 0, message = "Error rate threshold para rebuild completo não pode ser negativo")
    @Max(value = 1, message = "Error rate threshold para rebuild completo não pode ser maior que 1")
    private double errorRateThresholdForFullRebuild = 0.2; // 20%
    
    /**
     * Número máximo de erros antes de parar o rebuild.
     */
    @Min(value = 1, message = "Max errors deve ser pelo menos 1")
    private int maxErrorsBeforeStop = 1000;
    
    /**
     * Intervalo em segundos para verificação automática de projeções que precisam de rebuild.
     */
    @Min(value = 60, message = "Check interval deve ser pelo menos 60 segundos")
    private int autoCheckIntervalSeconds = 300; // 5 minutos
    
    /**
     * Timeout em segundos para operações de rebuild.
     */
    @Min(value = 60, message = "Timeout deve ser pelo menos 60 segundos")
    private int timeoutSeconds = 3600; // 1 hora
    
    /**
     * Se deve fazer log detalhado durante rebuild.
     */
    private boolean detailedLogging = false;
    
    /**
     * Número máximo de rebuilds simultâneos.
     */
    @Min(value = 1, message = "Max concurrent rebuilds deve ser pelo menos 1")
    private int maxConcurrentRebuilds = 3;
    
    /**
     * Se deve pausar rebuild automaticamente em caso de muitos erros.
     */
    private boolean autoPauseOnErrors = true;
    
    /**
     * Se deve tentar rebuild automático após falha.
     */
    private boolean autoRetryAfterFailure = true;
    
    /**
     * Delay em segundos antes de tentar rebuild novamente após falha.
     */
    @Min(value = 60, message = "Retry delay deve ser pelo menos 60 segundos")
    private int retryDelaySeconds = 1800; // 30 minutos
    
    // Getters e Setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getBatchSize() {
        return batchSize;
    }
    
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    public long getLagThresholdForRebuild() {
        return lagThresholdForRebuild;
    }
    
    public void setLagThresholdForRebuild(long lagThresholdForRebuild) {
        this.lagThresholdForRebuild = lagThresholdForRebuild;
    }
    
    public long getLagThresholdForFullRebuild() {
        return lagThresholdForFullRebuild;
    }
    
    public void setLagThresholdForFullRebuild(long lagThresholdForFullRebuild) {
        this.lagThresholdForFullRebuild = lagThresholdForFullRebuild;
    }
    
    public double getErrorThresholdForRebuild() {
        return errorThresholdForRebuild;
    }
    
    public void setErrorThresholdForRebuild(double errorThresholdForRebuild) {
        this.errorThresholdForRebuild = errorThresholdForRebuild;
    }
    
    public double getErrorRateThresholdForFullRebuild() {
        return errorRateThresholdForFullRebuild;
    }
    
    public void setErrorRateThresholdForFullRebuild(double errorRateThresholdForFullRebuild) {
        this.errorRateThresholdForFullRebuild = errorRateThresholdForFullRebuild;
    }
    
    public int getMaxErrorsBeforeStop() {
        return maxErrorsBeforeStop;
    }
    
    public void setMaxErrorsBeforeStop(int maxErrorsBeforeStop) {
        this.maxErrorsBeforeStop = maxErrorsBeforeStop;
    }
    
    public int getAutoCheckIntervalSeconds() {
        return autoCheckIntervalSeconds;
    }
    
    public void setAutoCheckIntervalSeconds(int autoCheckIntervalSeconds) {
        this.autoCheckIntervalSeconds = autoCheckIntervalSeconds;
    }
    
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
    
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
    
    public boolean isDetailedLogging() {
        return detailedLogging;
    }
    
    public void setDetailedLogging(boolean detailedLogging) {
        this.detailedLogging = detailedLogging;
    }
    
    public int getMaxConcurrentRebuilds() {
        return maxConcurrentRebuilds;
    }
    
    public void setMaxConcurrentRebuilds(int maxConcurrentRebuilds) {
        this.maxConcurrentRebuilds = maxConcurrentRebuilds;
    }
    
    public boolean isAutoPauseOnErrors() {
        return autoPauseOnErrors;
    }
    
    public void setAutoPauseOnErrors(boolean autoPauseOnErrors) {
        this.autoPauseOnErrors = autoPauseOnErrors;
    }
    
    public boolean isAutoRetryAfterFailure() {
        return autoRetryAfterFailure;
    }
    
    public void setAutoRetryAfterFailure(boolean autoRetryAfterFailure) {
        this.autoRetryAfterFailure = autoRetryAfterFailure;
    }
    
    public int getRetryDelaySeconds() {
        return retryDelaySeconds;
    }
    
    public void setRetryDelaySeconds(int retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }
}