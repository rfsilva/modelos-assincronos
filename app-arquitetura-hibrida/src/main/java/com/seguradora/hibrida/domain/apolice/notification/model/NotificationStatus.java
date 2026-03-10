package com.seguradora.hibrida.domain.apolice.notification.model;

/**
 * Status de uma notificação de apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum NotificationStatus {
    
    /**
     * Notificação criada e aguardando processamento.
     */
    PENDING("Pendente", "Aguardando processamento"),
    
    /**
     * Notificação sendo processada.
     */
    PROCESSING("Processando", "Em processamento"),
    
    /**
     * Notificação enviada com sucesso.
     */
    SENT("Enviada", "Enviada com sucesso"),
    
    /**
     * Falha no envio da notificação.
     */
    FAILED("Falha", "Falha no envio"),
    
    /**
     * Notificação cancelada.
     */
    CANCELLED("Cancelada", "Cancelada pelo sistema"),
    
    /**
     * Notificação expirada.
     */
    EXPIRED("Expirada", "Expirou o prazo de envio");
    
    private final String displayName;
    private final String description;
    
    NotificationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Nome para exibição.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Descrição do status.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica se é um status final (não pode mais ser alterado).
     */
    public boolean isFinal() {
        return this == SENT || this == FAILED || this == CANCELLED || this == EXPIRED;
    }
    
    /**
     * Verifica se é um status de sucesso.
     */
    public boolean isSuccess() {
        return this == SENT;
    }
    
    /**
     * Verifica se é um status de erro.
     */
    public boolean isError() {
        return this == FAILED || this == EXPIRED;
    }
    
    /**
     * Verifica se pode ser reprocessado.
     */
    public boolean canRetry() {
        return this == FAILED && !isFinal();
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}