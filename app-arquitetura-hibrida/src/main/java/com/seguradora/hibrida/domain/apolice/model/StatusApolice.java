package com.seguradora.hibrida.domain.apolice.model;

/**
 * Enum que representa os possíveis status de uma apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum StatusApolice {
    
    /**
     * Apólice ativa e vigente.
     */
    ATIVA("Ativa", "Apólice ativa e vigente"),
    
    /**
     * Apólice cancelada pelo segurado ou seguradora.
     */
    CANCELADA("Cancelada", "Apólice cancelada"),
    
    /**
     * Apólice vencida (fim da vigência).
     */
    VENCIDA("Vencida", "Apólice vencida"),
    
    /**
     * Apólice suspensa temporariamente.
     */
    SUSPENSA("Suspensa", "Apólice suspensa temporariamente");
    
    private final String descricao;
    private final String detalhamento;
    
    StatusApolice(String descricao, String detalhamento) {
        this.descricao = descricao;
        this.detalhamento = detalhamento;
    }
    
    /**
     * Retorna a descrição do status.
     */
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Retorna o detalhamento do status.
     */
    public String getDetalhamento() {
        return detalhamento;
    }
    
    /**
     * Verifica se a apólice está ativa.
     */
    public boolean isAtiva() {
        return this == ATIVA;
    }
    
    /**
     * Verifica se a apólice pode ser renovada.
     */
    public boolean podeSerRenovada() {
        return this == ATIVA || this == VENCIDA;
    }
    
    /**
     * Verifica se a apólice pode ser cancelada.
     */
    public boolean podeSerCancelada() {
        return this == ATIVA || this == SUSPENSA;
    }
    
    /**
     * Verifica se a apólice pode gerar sinistros.
     */
    public boolean podeGerarSinistros() {
        return this == ATIVA;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}