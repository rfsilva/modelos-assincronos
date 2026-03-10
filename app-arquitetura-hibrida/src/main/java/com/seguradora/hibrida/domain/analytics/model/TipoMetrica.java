package com.seguradora.hibrida.domain.analytics.model;

/**
 * Tipos de métricas analíticas disponíveis.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum TipoMetrica {
    
    // === MÉTRICAS GERAIS ===
    GERAL("Métricas Gerais", "Visão geral do negócio"),
    
    // === MÉTRICAS POR DIMENSÃO ===
    POR_REGIAO("Por Região", "Métricas segmentadas por região geográfica"),
    POR_PRODUTO("Por Produto", "Métricas segmentadas por tipo de produto"),
    POR_CANAL("Por Canal", "Métricas segmentadas por canal de venda"),
    POR_FAIXA_ETARIA("Por Faixa Etária", "Métricas segmentadas por idade"),
    POR_OPERADOR("Por Operador", "Métricas segmentadas por operador"),
    
    // === MÉTRICAS TEMPORAIS ===
    DIARIA("Diária", "Métricas consolidadas por dia"),
    SEMANAL("Semanal", "Métricas consolidadas por semana"),
    MENSAL("Mensal", "Métricas consolidadas por mês"),
    TRIMESTRAL("Trimestral", "Métricas consolidadas por trimestre"),
    ANUAL("Anual", "Métricas consolidadas por ano"),
    
    // === MÉTRICAS ESPECÍFICAS ===
    RENOVACAO("Renovação", "Métricas específicas de renovação"),
    CANCELAMENTO("Cancelamento", "Métricas específicas de cancelamento"),
    VENCIMENTO("Vencimento", "Métricas de vencimentos próximos"),
    PERFORMANCE("Performance", "Métricas de performance operacional"),
    FINANCEIRO("Financeiro", "Métricas financeiras e de receita");
    
    private final String displayName;
    private final String descricao;
    
    TipoMetrica(String displayName, String descricao) {
        this.displayName = displayName;
        this.descricao = descricao;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    /**
     * Verifica se é uma métrica temporal.
     */
    public boolean isTemporal() {
        return this == DIARIA || this == SEMANAL || this == MENSAL || 
               this == TRIMESTRAL || this == ANUAL;
    }
    
    /**
     * Verifica se é uma métrica dimensional.
     */
    public boolean isDimensional() {
        return this == POR_REGIAO || this == POR_PRODUTO || this == POR_CANAL || 
               this == POR_FAIXA_ETARIA || this == POR_OPERADOR;
    }
    
    /**
     * Verifica se é uma métrica específica.
     */
    public boolean isEspecifica() {
        return this == RENOVACAO || this == CANCELAMENTO || this == VENCIMENTO || 
               this == PERFORMANCE || this == FINANCEIRO;
    }
}