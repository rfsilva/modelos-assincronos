package com.seguradora.hibrida.projection.consistency;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Propriedades de configuração para verificação de consistência de projeções.
 */
@ConfigurationProperties(prefix = "cqrs.projection.consistency")
@Validated
public class ProjectionConsistencyProperties {
    
    /**
     * Se a verificação de consistência está habilitada.
     */
    private boolean enabled = true;
    
    /**
     * Intervalo em segundos para verificação automática.
     */
    @Min(value = 60, message = "Check interval deve ser pelo menos 60 segundos")
    private int checkIntervalSeconds = 300; // 5 minutos
    
    /**
     * Lag máximo permitido em número de eventos.
     */
    @Min(value = 1, message = "Max allowed lag deve ser pelo menos 1")
    private long maxAllowedLag = 1000L;
    
    /**
     * Threshold de lag para considerar crítico.
     */
    @Min(value = 1, message = "Critical lag threshold deve ser pelo menos 1")
    private long criticalLagThreshold = 10000L;
    
    /**
     * Taxa de erro máxima permitida (0.0 a 1.0).
     */
    @Min(value = 0, message = "Max allowed error rate não pode ser negativo")
    @Max(value = 1, message = "Max allowed error rate não pode ser maior que 1")
    private double maxAllowedErrorRate = 0.05; // 5%
    
    /**
     * Taxa de erro para considerar crítica.
     */
    @Min(value = 0, message = "Critical error rate não pode ser negativo")
    @Max(value = 1, message = "Critical error rate não pode ser maior que 1")
    private double criticalErrorRate = 0.2; // 20%
    
    /**
     * Threshold em minutos para considerar projeção travada.
     */
    @Min(value = 1, message = "Stale threshold deve ser pelo menos 1 minuto")
    private int staleThresholdMinutes = 30;
    
    /**
     * Duração máxima em minutos que uma projeção pode ficar em erro.
     */
    @Min(value = 1, message = "Max error duration deve ser pelo menos 1 minuto")
    private int maxErrorDurationMinutes = 60;
    
    /**
     * Duração máxima em minutos que uma projeção pode ficar pausada.
     */
    @Min(value = 1, message = "Max pause duration deve ser pelo menos 1 minuto")
    private int maxPauseDurationMinutes = 120;
    
    /**
     * Threshold em horas para considerar projeção órfã.
     */
    @Min(value = 1, message = "Orphan threshold deve ser pelo menos 1 hora")
    private int orphanThresholdHours = 24;
    
    /**
     * Se deve reiniciar automaticamente projeções com lag alto.
     */
    private boolean autoRestartOnHighLag = true;
    
    /**
     * Se deve reiniciar automaticamente projeções travadas.
     */
    private boolean autoRestartOnStale = true;
    
    /**
     * Se deve pausar automaticamente projeções com taxa de erro alta.
     */
    private boolean autoPauseOnHighErrorRate = true;
    
    /**
     * Se deve fazer rebuild automático em caso de erro persistente.
     */
    private boolean autoRebuildOnPersistentError = true;
    
    /**
     * Se deve fazer log detalhado durante verificações.
     */
    private boolean detailedLogging = false;
    
    /**
     * Se deve enviar alertas para issues críticos.
     */
    private boolean alertsEnabled = true;
    
    /**
     * Threshold de score de saúde para enviar alertas.
     */
    @Min(value = 0, message = "Health score threshold não pode ser negativo")
    @Max(value = 100, message = "Health score threshold não pode ser maior que 100")
    private double healthScoreAlertThreshold = 80.0;
    
    // Getters e Setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getCheckIntervalSeconds() {
        return checkIntervalSeconds;
    }
    
    public void setCheckIntervalSeconds(int checkIntervalSeconds) {
        this.checkIntervalSeconds = checkIntervalSeconds;
    }
    
    public long getMaxAllowedLag() {
        return maxAllowedLag;
    }
    
    public void setMaxAllowedLag(long maxAllowedLag) {
        this.maxAllowedLag = maxAllowedLag;
    }
    
    public long getCriticalLagThreshold() {
        return criticalLagThreshold;
    }
    
    public void setCriticalLagThreshold(long criticalLagThreshold) {
        this.criticalLagThreshold = criticalLagThreshold;
    }
    
    public double getMaxAllowedErrorRate() {
        return maxAllowedErrorRate;
    }
    
    public void setMaxAllowedErrorRate(double maxAllowedErrorRate) {
        this.maxAllowedErrorRate = maxAllowedErrorRate;
    }
    
    public double getCriticalErrorRate() {
        return criticalErrorRate;
    }
    
    public void setCriticalErrorRate(double criticalErrorRate) {
        this.criticalErrorRate = criticalErrorRate;
    }
    
    public int getStaleThresholdMinutes() {
        return staleThresholdMinutes;
    }
    
    public void setStaleThresholdMinutes(int staleThresholdMinutes) {
        this.staleThresholdMinutes = staleThresholdMinutes;
    }
    
    public int getMaxErrorDurationMinutes() {
        return maxErrorDurationMinutes;
    }
    
    public void setMaxErrorDurationMinutes(int maxErrorDurationMinutes) {
        this.maxErrorDurationMinutes = maxErrorDurationMinutes;
    }
    
    public int getMaxPauseDurationMinutes() {
        return maxPauseDurationMinutes;
    }
    
    public void setMaxPauseDurationMinutes(int maxPauseDurationMinutes) {
        this.maxPauseDurationMinutes = maxPauseDurationMinutes;
    }
    
    public int getOrphanThresholdHours() {
        return orphanThresholdHours;
    }
    
    public void setOrphanThresholdHours(int orphanThresholdHours) {
        this.orphanThresholdHours = orphanThresholdHours;
    }
    
    public boolean isAutoRestartOnHighLag() {
        return autoRestartOnHighLag;
    }
    
    public void setAutoRestartOnHighLag(boolean autoRestartOnHighLag) {
        this.autoRestartOnHighLag = autoRestartOnHighLag;
    }
    
    public boolean isAutoRestartOnStale() {
        return autoRestartOnStale;
    }
    
    public void setAutoRestartOnStale(boolean autoRestartOnStale) {
        this.autoRestartOnStale = autoRestartOnStale;
    }
    
    public boolean isAutoPauseOnHighErrorRate() {
        return autoPauseOnHighErrorRate;
    }
    
    public void setAutoPauseOnHighErrorRate(boolean autoPauseOnHighErrorRate) {
        this.autoPauseOnHighErrorRate = autoPauseOnHighErrorRate;
    }
    
    public boolean isAutoRebuildOnPersistentError() {
        return autoRebuildOnPersistentError;
    }
    
    public void setAutoRebuildOnPersistentError(boolean autoRebuildOnPersistentError) {
        this.autoRebuildOnPersistentError = autoRebuildOnPersistentError;
    }
    
    public boolean isDetailedLogging() {
        return detailedLogging;
    }
    
    public void setDetailedLogging(boolean detailedLogging) {
        this.detailedLogging = detailedLogging;
    }
    
    public boolean isAlertsEnabled() {
        return alertsEnabled;
    }
    
    public void setAlertsEnabled(boolean alertsEnabled) {
        this.alertsEnabled = alertsEnabled;
    }
    
    public double getHealthScoreAlertThreshold() {
        return healthScoreAlertThreshold;
    }
    
    public void setHealthScoreAlertThreshold(double healthScoreAlertThreshold) {
        this.healthScoreAlertThreshold = healthScoreAlertThreshold;
    }
}