package com.seguradora.hibrida.domain.veiculo.model;

/**
 * Enum que representa os tipos de combustível suportados pelo sistema.
 * 
 * <p>Define os combustíveis aceitos para veículos, incluindo informações
 * sobre compatibilidade e características específicas de cada tipo.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum TipoCombustivel {
    
    /**
     * Gasolina comum.
     * 
     * <p>Combustível tradicional para veículos de passeio.
     * Características:
     * - Amplamente disponível
     * - Compatível com motores convencionais
     * - Octanagem padrão
     */
    GASOLINA("Gasolina", "Gasolina comum", true, false),
    
    /**
     * Etanol (álcool).
     * 
     * <p>Combustível renovável derivado de cana-de-açúcar.
     * Características:
     * - Combustível renovável
     * - Menor poder calorífico
     * - Requer motor específico ou flex
     */
    ETANOL("Etanol", "Álcool etílico hidratado", true, true),
    
    /**
     * Flex (gasolina e etanol).
     * 
     * <p>Sistema que permite uso de gasolina e/ou etanol.
     * Características:
     * - Flexibilidade de combustível
     * - Tecnologia bicombustível
     * - Mais comum em veículos nacionais
     */
    FLEX("Flex", "Bicombustível (gasolina e etanol)", true, true),
    
    /**
     * Diesel comum.
     * 
     * <p>Combustível para veículos pesados e alguns de passeio.
     * Características:
     * - Maior eficiência energética
     * - Usado em veículos comerciais
     * - Motor de ciclo diesel
     */
    DIESEL("Diesel", "Óleo diesel", false, false),
    
    /**
     * Gás Natural Veicular (GNV).
     * 
     * <p>Combustível gasoso como alternativa econômica.
     * Características:
     * - Combustível mais limpo
     * - Requer kit de conversão
     * - Menor custo operacional
     */
    GNV("GNV", "Gás Natural Veicular", false, true),
    
    /**
     * Elétrico (bateria).
     * 
     * <p>Veículo movido exclusivamente por energia elétrica.
     * Características:
     * - Zero emissões locais
     * - Tecnologia emergente
     * - Requer infraestrutura de recarga
     */
    ELETRICO("Elétrico", "Energia elétrica (bateria)", false, true);
    
    private final String nome;
    private final String descricao;
    private final boolean derivadoPetroleo;
    private final boolean renovavel;
    
    /**
     * Construtor do enum.
     * 
     * @param nome Nome do combustível
     * @param descricao Descrição detalhada
     * @param derivadoPetroleo Se é derivado de petróleo
     * @param renovavel Se é fonte renovável
     */
    TipoCombustivel(String nome, String descricao, boolean derivadoPetroleo, boolean renovavel) {
        this.nome = nome;
        this.descricao = descricao;
        this.derivadoPetroleo = derivadoPetroleo;
        this.renovavel = renovavel;
    }
    
    /**
     * Retorna o nome do combustível.
     * 
     * @return Nome do combustível
     */
    public String getNome() {
        return nome;
    }
    
    /**
     * Retorna a descrição detalhada.
     * 
     * @return Descrição do combustível
     */
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Verifica se é derivado de petróleo.
     * 
     * @return true se é derivado de petróleo
     */
    public boolean isDerivadoPetroleo() {
        return derivadoPetroleo;
    }
    
    /**
     * Verifica se é fonte renovável.
     * 
     * @return true se é renovável
     */
    public boolean isRenovavel() {
        return renovavel;
    }
    
    /**
     * Verifica se é combustível líquido.
     * 
     * <p>Combustíveis líquidos são gasolina, etanol, flex e diesel.
     * 
     * @return true se é combustível líquido
     */
    public boolean isLiquido() {
        return this != GNV && this != ELETRICO;
    }
    
    /**
     * Verifica se é combustível alternativo.
     * 
     * <p>Combustíveis alternativos são GNV e elétrico.
     * 
     * @return true se é alternativo
     */
    public boolean isAlternativo() {
        return this == GNV || this == ELETRICO;
    }
    
    /**
     * Verifica se é compatível com determinada categoria de veículo.
     * 
     * @param categoria Categoria do veículo
     * @return true se é compatível
     */
    public boolean isCompativelCom(CategoriaVeiculo categoria) {
        if (categoria == null) {
            return false;
        }
        
        switch (this) {
            case GASOLINA:
            case ETANOL:
            case FLEX:
                return categoria == CategoriaVeiculo.PASSEIO || 
                       categoria == CategoriaVeiculo.UTILITARIO ||
                       categoria == CategoriaVeiculo.MOTOCICLETA;
                       
            case DIESEL:
                return categoria == CategoriaVeiculo.UTILITARIO || 
                       categoria == CategoriaVeiculo.CAMINHAO;
                       
            case GNV:
                return categoria == CategoriaVeiculo.PASSEIO || 
                       categoria == CategoriaVeiculo.UTILITARIO;
                       
            case ELETRICO:
                return categoria == CategoriaVeiculo.PASSEIO ||
                       categoria == CategoriaVeiculo.UTILITARIO;
                       
            default:
                return false;
        }
    }
    
    /**
     * Retorna o fator de risco para cálculo de seguro.
     * 
     * <p>Combustíveis mais seguros têm fator menor.
     * 
     * @return Fator de risco (0.8 a 1.2)
     */
    public double getFatorRisco() {
        switch (this) {
            case ELETRICO:
                return 0.8; // Menor risco
            case GNV:
                return 0.9;
            case DIESEL:
                return 0.95;
            case GASOLINA:
            case FLEX:
                return 1.0; // Risco padrão
            case ETANOL:
                return 1.1; // Maior risco (corrosão)
            default:
                return 1.0;
        }
    }
    
    /**
     * Retorna representação string do combustível.
     * 
     * @return Nome do combustível
     */
    @Override
    public String toString() {
        return nome;
    }
}