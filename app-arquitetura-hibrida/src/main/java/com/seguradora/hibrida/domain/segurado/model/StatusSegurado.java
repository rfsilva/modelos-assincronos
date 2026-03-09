package com.seguradora.hibrida.domain.segurado.model;

/**
 * Enum representando os status possíveis de um Segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
public enum StatusSegurado {
    
    /**
     * Segurado ativo e com cadastro completo.
     */
    ATIVO("Ativo"),
    
    /**
     * Segurado suspenso temporariamente.
     */
    SUSPENSO("Suspenso"),
    
    /**
     * Segurado inativo/cancelado.
     */
    INATIVO("Inativo"),
    
    /**
     * Segurado bloqueado por irregularidades.
     */
    BLOQUEADO("Bloqueado");
    
    private final String descricao;
    
    StatusSegurado(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public boolean isAtivo() {
        return this == ATIVO;
    }
    
    public boolean podeOperarApolices() {
        return this == ATIVO || this == SUSPENSO;
    }
}
