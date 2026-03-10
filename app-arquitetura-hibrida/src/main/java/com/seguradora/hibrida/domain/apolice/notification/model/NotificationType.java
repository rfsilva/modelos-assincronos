package com.seguradora.hibrida.domain.apolice.notification.model;

/**
 * Tipos de notificação para apólices.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum NotificationType {
    
    APOLICE_CRIADA("Apólice Criada", "Sua apólice foi criada com sucesso", true),
    APOLICE_ATUALIZADA("Apólice Atualizada", "Sua apólice foi atualizada", true),
    APOLICE_CANCELADA("Apólice Cancelada", "Sua apólice foi cancelada", true),
    APOLICE_RENOVADA("Apólice Renovada", "Sua apólice foi renovada", true),
    
    VENCIMENTO_30_DIAS("Vencimento em 30 dias", "Sua apólice vence em 30 dias", true),
    VENCIMENTO_15_DIAS("Vencimento em 15 dias", "Sua apólice vence em 15 dias", true),
    VENCIMENTO_7_DIAS("Vencimento em 7 dias", "Sua apólice vence em 7 dias", true),
    VENCIMENTO_1_DIA("Vencimento amanhã", "Sua apólice vence amanhã", true),
    APOLICE_VENCIDA("Apólice Vencida", "Sua apólice está vencida", true),
    
    COBERTURA_ADICIONADA("Cobertura Adicionada", "Nova cobertura foi adicionada à sua apólice", false),
    RENOVACAO_AUTOMATICA("Renovação Automática", "Sua apólice será renovada automaticamente", false),
    SCORE_BAIXO("Score de Renovação Baixo", "Sua apólice tem score baixo para renovação", false);
    
    private final String titulo;
    private final String descricao;
    private final boolean obrigatoria;
    
    NotificationType(String titulo, String descricao, boolean obrigatoria) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.obrigatoria = obrigatoria;
    }
    
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public boolean isObrigatoria() { return obrigatoria; }
    
    /**
     * Verifica se é notificação de vencimento.
     */
    public boolean isVencimento() {
        return name().startsWith("VENCIMENTO_") || this == APOLICE_VENCIDA;
    }
    
    /**
     * Verifica se é notificação crítica.
     */
    public boolean isCritica() {
        return this == VENCIMENTO_1_DIA || this == APOLICE_VENCIDA || this == APOLICE_CANCELADA;
    }
    
    /**
     * Obtém prioridade da notificação (1 = mais alta).
     */
    public int getPrioridade() {
        return switch (this) {
            case APOLICE_VENCIDA, VENCIMENTO_1_DIA -> 1;
            case VENCIMENTO_7_DIAS, APOLICE_CANCELADA -> 2;
            case VENCIMENTO_15_DIAS, APOLICE_CRIADA -> 3;
            case VENCIMENTO_30_DIAS, APOLICE_RENOVADA -> 4;
            default -> 5;
        };
    }
}