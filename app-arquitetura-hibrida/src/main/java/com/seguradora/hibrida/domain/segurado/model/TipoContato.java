package com.seguradora.hibrida.domain.segurado.model;

/**
 * Enum representando os tipos de contato disponíveis para um Segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
public enum TipoContato {
    
    /**
     * Contato por email.
     */
    EMAIL("Email", "Contato por correio eletrônico"),
    
    /**
     * Telefone fixo.
     */
    TELEFONE("Telefone", "Telefone fixo residencial ou comercial"),
    
    /**
     * Telefone celular.
     */
    CELULAR("Celular", "Telefone celular/móvel"),
    
    /**
     * WhatsApp Business.
     */
    WHATSAPP("WhatsApp", "Contato via WhatsApp");
    
    private final String nome;
    private final String descricao;
    
    TipoContato(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Verifica se o tipo de contato é digital.
     */
    public boolean isDigital() {
        return this == EMAIL || this == WHATSAPP;
    }
    
    /**
     * Verifica se o tipo de contato é telefônico.
     */
    public boolean isTelefonico() {
        return this == TELEFONE || this == CELULAR || this == WHATSAPP;
    }
    
    /**
     * Verifica se suporta mensagens de texto.
     */
    public boolean suportaMensagemTexto() {
        return this == EMAIL || this == WHATSAPP;
    }
    
    @Override
    public String toString() {
        return nome;
    }
}