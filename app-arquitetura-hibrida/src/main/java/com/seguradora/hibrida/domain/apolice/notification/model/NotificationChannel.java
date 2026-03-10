package com.seguradora.hibrida.domain.apolice.notification.model;

/**
 * Canais de notificação disponíveis.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum NotificationChannel {
    
    EMAIL("Email", "email", true, 1000),
    SMS("SMS", "sms", true, 500),
    WHATSAPP("WhatsApp", "whatsapp", false, 200),
    PUSH("Push Notification", "push", false, 100),
    IN_APP("Notificação In-App", "in_app", true, 50);
    
    private final String displayName;
    private final String code;
    private final boolean enabled;
    private final int dailyLimit;
    
    NotificationChannel(String displayName, String code, boolean enabled, int dailyLimit) {
        this.displayName = displayName;
        this.code = code;
        this.enabled = enabled;
        this.dailyLimit = dailyLimit;
    }
    
    public String getDisplayName() { return displayName; }
    public String getCode() { return code; }
    public boolean isEnabled() { return enabled; }
    public int getDailyLimit() { return dailyLimit; }
    
    /**
     * Verifica se é canal instantâneo.
     */
    public boolean isInstantaneo() {
        return this == SMS || this == WHATSAPP || this == PUSH;
    }
    
    /**
     * Verifica se é canal confiável.
     */
    public boolean isConfiavel() {
        return this == EMAIL || this == SMS;
    }
    
    /**
     * Obtém custo relativo do canal (1-5).
     */
    public int getCustoRelativo() {
        return switch (this) {
            case IN_APP -> 1;
            case PUSH -> 2;
            case EMAIL -> 3;
            case WHATSAPP -> 4;
            case SMS -> 5;
        };
    }
    
    /**
     * Obtém ordem de preferência para fallback.
     */
    public int getOrdemFallback() {
        return switch (this) {
            case EMAIL -> 1;
            case SMS -> 2;
            case WHATSAPP -> 3;
            case IN_APP -> 4;
            case PUSH -> 5;
        };
    }
}