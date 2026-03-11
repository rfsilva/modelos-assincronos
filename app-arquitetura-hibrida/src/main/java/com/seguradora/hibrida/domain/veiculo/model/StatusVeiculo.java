package com.seguradora.hibrida.domain.veiculo.model;

/**
 * Enum que representa os possíveis status de um veículo no sistema.
 * 
 * <p>Define os estados que um veículo pode assumir durante seu ciclo de vida
 * no sistema de seguros, controlando as operações permitidas em cada estado.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum StatusVeiculo {
    
    /**
     * Veículo ativo e disponível para operações.
     * 
     * <p>Estado inicial padrão. Permite:
     * - Associação com apólices
     * - Alteração de especificações
     * - Transferência de propriedade
     */
    ATIVO("Ativo", "Veículo ativo e operacional"),
    
    /**
     * Veículo inativo temporariamente.
     * 
     * <p>Estado temporário que impede novas operações mas mantém histórico.
     * Permite apenas:
     * - Reativação
     * - Consulta de dados
     */
    INATIVO("Inativo", "Veículo temporariamente inativo"),
    
    /**
     * Veículo bloqueado por questões administrativas.
     * 
     * <p>Estado que impede operações até resolução de pendências.
     * Motivos comuns:
     * - Documentação irregular
     * - Restrições judiciais
     * - Problemas de propriedade
     */
    BLOQUEADO("Bloqueado", "Veículo bloqueado administrativamente"),
    
    /**
     * Veículo envolvido em sinistro com perda total.
     * 
     * <p>Estado final que indica perda total do veículo.
     * Características:
     * - Não permite novas apólices
     * - Mantém histórico para auditoria
     * - Pode ter processos de indenização
     */
    SINISTRADO("Sinistrado", "Veículo com perda total por sinistro");
    
    private final String nome;
    private final String descricao;
    
    /**
     * Construtor do enum.
     * 
     * @param nome Nome amigável do status
     * @param descricao Descrição detalhada do status
     */
    StatusVeiculo(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
    
    /**
     * Retorna o nome amigável do status.
     * 
     * @return Nome do status
     */
    public String getNome() {
        return nome;
    }
    
    /**
     * Retorna a descrição detalhada do status.
     * 
     * @return Descrição do status
     */
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Verifica se o veículo está ativo.
     * 
     * @return true se o status for ATIVO
     */
    public boolean isAtivo() {
        return this == ATIVO;
    }
    
    /**
     * Verifica se o veículo pode ser associado a apólices.
     * 
     * <p>Apenas veículos ativos podem ser associados a novas apólices.
     * 
     * @return true se pode ser associado a apólices
     */
    public boolean podeAssociarApolice() {
        return this == ATIVO;
    }
    
    /**
     * Verifica se o veículo permite alterações de especificação.
     * 
     * <p>Veículos ativos e inativos permitem alterações.
     * Veículos bloqueados e sinistrados não permitem.
     * 
     * @return true se permite alterações
     */
    public boolean permiteAlteracoes() {
        return this == ATIVO || this == INATIVO;
    }
    
    /**
     * Verifica se o veículo permite transferência de propriedade.
     * 
     * <p>Apenas veículos ativos permitem transferência.
     * 
     * @return true se permite transferência
     */
    public boolean permiteTransferencia() {
        return this == ATIVO;
    }
    
    /**
     * Verifica se é um status final (não permite mudanças).
     * 
     * <p>Status finais são aqueles que representam estados
     * definitivos do veículo.
     * 
     * @return true se é status final
     */
    public boolean isFinal() {
        return this == SINISTRADO;
    }
    
    /**
     * Retorna representação string do status.
     * 
     * @return Nome do status
     */
    @Override
    public String toString() {
        return nome;
    }
}