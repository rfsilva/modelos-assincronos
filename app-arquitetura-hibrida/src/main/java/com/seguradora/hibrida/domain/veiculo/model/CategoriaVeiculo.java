package com.seguradora.hibrida.domain.veiculo.model;

/**
 * Enum que representa as categorias de veículos suportadas pelo sistema.
 * 
 * <p>Define as categorias principais de veículos para fins de seguro,
 * incluindo características específicas e regras de negócio para cada tipo.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum CategoriaVeiculo {
    
    /**
     * Veículo de passeio.
     * 
     * <p>Categoria para automóveis de uso pessoal.
     * Características:
     * - Até 9 lugares
     * - Uso particular ou comercial leve
     * - Maior volume de seguros
     */
    PASSEIO("Passeio", "Automóvel de passeio", 9, 1.0),
    
    /**
     * Veículo utilitário.
     * 
     * <p>Categoria para veículos de carga leve e SUVs.
     * Características:
     * - Pickups, vans, SUVs
     * - Uso misto (pessoal/comercial)
     * - Maior risco por uso comercial
     */
    UTILITARIO("Utilitário", "Veículo utilitário (pickup, van, SUV)", 15, 1.2),
    
    /**
     * Motocicleta.
     * 
     * <p>Categoria para motocicletas e similares.
     * Características:
     * - Duas rodas
     * - Alto risco de sinistros
     * - Regulamentação específica
     */
    MOTOCICLETA("Motocicleta", "Motocicleta, motoneta ou similar", 2, 2.5),
    
    /**
     * Caminhão.
     * 
     * <p>Categoria para veículos de carga pesada.
     * Características:
     * - Uso comercial exclusivo
     * - Alto valor de carga
     * - Regulamentação específica
     */
    CAMINHAO("Caminhão", "Veículo de carga pesada", 3, 1.8);
    
    private final String nome;
    private final String descricao;
    private final int capacidadeMaximaPessoas;
    private final double fatorRisco;
    
    /**
     * Construtor do enum.
     * 
     * @param nome Nome da categoria
     * @param descricao Descrição detalhada
     * @param capacidadeMaximaPessoas Capacidade máxima de pessoas
     * @param fatorRisco Fator de risco para cálculo de prêmio
     */
    CategoriaVeiculo(String nome, String descricao, int capacidadeMaximaPessoas, double fatorRisco) {
        this.nome = nome;
        this.descricao = descricao;
        this.capacidadeMaximaPessoas = capacidadeMaximaPessoas;
        this.fatorRisco = fatorRisco;
    }
    
    /**
     * Retorna o nome da categoria.
     * 
     * @return Nome da categoria
     */
    public String getNome() {
        return nome;
    }
    
    /**
     * Retorna a descrição detalhada.
     * 
     * @return Descrição da categoria
     */
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Retorna a capacidade máxima de pessoas.
     * 
     * @return Capacidade máxima
     */
    public int getCapacidadeMaximaPessoas() {
        return capacidadeMaximaPessoas;
    }
    
    /**
     * Retorna o fator de risco para cálculo de prêmio.
     * 
     * @return Fator de risco
     */
    public double getFatorRisco() {
        return fatorRisco;
    }
    
    /**
     * Verifica se é categoria de uso comercial.
     * 
     * @return true se é uso comercial
     */
    public boolean isUsoComercial() {
        return this == UTILITARIO || this == CAMINHAO;
    }
    
    /**
     * Verifica se é categoria de alto risco.
     * 
     * <p>Categorias com fator de risco > 2.0 são consideradas alto risco.
     * 
     * @return true se é alto risco
     */
    public boolean isAltoRisco() {
        return fatorRisco > 2.0;
    }
    
    /**
     * Verifica se requer documentação especial.
     * 
     * <p>Caminhões e utilitários comerciais requerem documentação adicional.
     * 
     * @return true se requer documentação especial
     */
    public boolean requerDocumentacaoEspecial() {
        return this == CAMINHAO || this == UTILITARIO;
    }
    
    /**
     * Verifica se permite determinado tipo de combustível.
     * 
     * @param combustivel Tipo de combustível
     * @return true se permite o combustível
     */
    public boolean permiteCombustivel(TipoCombustivel combustivel) {
        if (combustivel == null) {
            return false;
        }
        
        return combustivel.isCompativelCom(this);
    }
    
    /**
     * Retorna a cilindrada máxima recomendada para a categoria.
     * 
     * @return Cilindrada máxima em cc
     */
    public int getCilindradaMaximaRecomendada() {
        switch (this) {
            case PASSEIO:
                return 6000; // 6.0L
            case UTILITARIO:
                return 8000; // 8.0L
            case MOTOCICLETA:
                return 1500; // 1.5L
            case CAMINHAO:
                return 15000; // 15.0L
            default:
                return 2000;
        }
    }
    
    /**
     * Retorna a idade máxima recomendada para seguro.
     * 
     * @return Idade máxima em anos
     */
    public int getIdadeMaximaRecomendada() {
        switch (this) {
            case PASSEIO:
                return 20;
            case UTILITARIO:
                return 15;
            case MOTOCICLETA:
                return 10;
            case CAMINHAO:
                return 25;
            default:
                return 15;
        }
    }
    
    /**
     * Verifica se a categoria é compatível com determinada cilindrada.
     * 
     * @param cilindrada Cilindrada em cc
     * @return true se é compatível
     */
    public boolean isCompativelComCilindrada(Integer cilindrada) {
        if (cilindrada == null || cilindrada <= 0) {
            return false;
        }
        
        return cilindrada <= getCilindradaMaximaRecomendada();
    }
    
    /**
     * Retorna representação string da categoria.
     * 
     * @return Nome da categoria
     */
    @Override
    public String toString() {
        return nome;
    }
}