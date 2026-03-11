package com.seguradora.hibrida.domain.veiculo.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object que representa as especificações técnicas de um veículo.
 * 
 * <p>Encapsula informações como cor, combustível, categoria e cilindrada,
 * incluindo validações de compatibilidade entre os diferentes atributos.
 * 
 * <p>Este Value Object garante que as especificações sejam consistentes
 * e compatíveis entre si, seguindo as regras da indústria automotiva.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public final class Especificacao implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String cor;
    private final TipoCombustivel tipoCombustivel;
    private final CategoriaVeiculo categoria;
    private final Integer cilindrada; // em cc
    
    /**
     * Construtor privado.
     * 
     * @param cor Cor do veículo
     * @param tipoCombustivel Tipo de combustível
     * @param categoria Categoria do veículo
     * @param cilindrada Cilindrada em cc
     */
    private Especificacao(String cor, TipoCombustivel tipoCombustivel, 
                         CategoriaVeiculo categoria, Integer cilindrada) {
        this.cor = cor;
        this.tipoCombustivel = tipoCombustivel;
        this.categoria = categoria;
        this.cilindrada = cilindrada;
    }
    
    /**
     * Cria uma instância de Especificacao.
     * 
     * @param cor Cor do veículo
     * @param tipoCombustivel Tipo de combustível
     * @param categoria Categoria do veículo
     * @param cilindrada Cilindrada em cc (pode ser null)
     * @return Instância de Especificacao
     * @throws IllegalArgumentException se alguma especificação for inválida
     */
    public static Especificacao of(String cor, TipoCombustivel tipoCombustivel, 
                                  CategoriaVeiculo categoria, Integer cilindrada) {
        
        String corValidada = validarCor(cor);
        TipoCombustivel combustivelValidado = validarTipoCombustivel(tipoCombustivel);
        CategoriaVeiculo categoriaValidada = validarCategoria(categoria);
        Integer cilindradaValidada = validarCilindrada(cilindrada, categoria);
        
        // Validar compatibilidade entre especificações
        validarCompatibilidade(combustivelValidado, categoriaValidada, cilindradaValidada);
        
        return new Especificacao(corValidada, combustivelValidado, 
                                categoriaValidada, cilindradaValidada);
    }
    
    /**
     * Cria uma especificação básica sem cilindrada.
     * 
     * @param cor Cor do veículo
     * @param tipoCombustivel Tipo de combustível
     * @param categoria Categoria do veículo
     * @return Instância de Especificacao
     */
    public static Especificacao of(String cor, TipoCombustivel tipoCombustivel, 
                                  CategoriaVeiculo categoria) {
        return of(cor, tipoCombustivel, categoria, null);
    }
    
    /**
     * Retorna a cor do veículo.
     * 
     * @return Cor do veículo
     */
    public String getCor() {
        return cor;
    }
    
    /**
     * Retorna o tipo de combustível.
     * 
     * @return Tipo de combustível
     */
    public TipoCombustivel getTipoCombustivel() {
        return tipoCombustivel;
    }
    
    /**
     * Retorna a categoria do veículo.
     * 
     * @return Categoria do veículo
     */
    public CategoriaVeiculo getCategoria() {
        return categoria;
    }
    
    /**
     * Retorna a cilindrada em cc.
     * 
     * @return Cilindrada ou null se não informada
     */
    public Integer getCilindrada() {
        return cilindrada;
    }
    
    /**
     * Retorna a cilindrada formatada.
     * 
     * @return Cilindrada formatada (ex: "1.6L") ou "N/I" se não informada
     */
    public String getCilindradaFormatada() {
        if (cilindrada == null) {
            return "N/I";
        }
        
        if (cilindrada < 1000) {
            return cilindrada + "cc";
        } else {
            double litros = cilindrada / 1000.0;
            return String.format("%.1fL", litros);
        }
    }
    
    /**
     * Verifica se as especificações são compatíveis entre si.
     * 
     * @return true se são compatíveis
     */
    public boolean isCompativel() {
        return isCompativel(categoria) && isCombustivelCompativel();
    }
    
    /**
     * Verifica se é compatível com uma categoria específica.
     * 
     * @param categoria Categoria a verificar
     * @return true se é compatível
     */
    public boolean isCompativel(CategoriaVeiculo categoria) {
        if (categoria == null) {
            return false;
        }
        
        // Verificar compatibilidade de combustível com categoria
        if (!tipoCombustivel.isCompativelCom(categoria)) {
            return false;
        }
        
        // Verificar compatibilidade de cilindrada com categoria
        if (cilindrada != null && !categoria.isCompativelComCilindrada(cilindrada)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Verifica se o combustível é compatível com a categoria.
     * 
     * @return true se é compatível
     */
    public boolean isCombustivelCompativel() {
        return tipoCombustivel.isCompativelCom(categoria);
    }
    
    /**
     * Calcula o fator de risco baseado nas especificações.
     * 
     * @return Fator de risco combinado
     */
    public double getFatorRisco() {
        double fatorCategoria = categoria.getFatorRisco();
        double fatorCombustivel = tipoCombustivel.getFatorRisco();
        double fatorCilindrada = calcularFatorRiscoCilindrada();
        
        return fatorCategoria * fatorCombustivel * fatorCilindrada;
    }
    
    /**
     * Calcula o fator de risco baseado na cilindrada.
     * 
     * @return Fator de risco da cilindrada
     */
    private double calcularFatorRiscoCilindrada() {
        if (cilindrada == null) {
            return 1.0;
        }
        
        if (cilindrada <= 1000) {
            return 0.9; // Menor risco
        } else if (cilindrada <= 1600) {
            return 1.0; // Risco padrão
        } else if (cilindrada <= 2000) {
            return 1.1;
        } else if (cilindrada <= 3000) {
            return 1.3;
        } else {
            return 1.5; // Alto risco
        }
    }
    
    /**
     * Verifica se é um veículo esportivo baseado nas especificações.
     * 
     * @return true se tem características esportivas
     */
    public boolean isVeiculoEsportivo() {
        return cilindrada != null && cilindrada > 2500 && 
               categoria == CategoriaVeiculo.PASSEIO;
    }
    
    /**
     * Verifica se é um veículo econômico.
     * 
     * @return true se tem características econômicas
     */
    public boolean isVeiculoEconomico() {
        return (cilindrada == null || cilindrada <= 1000) &&
               (tipoCombustivel == TipoCombustivel.FLEX || 
                tipoCombustivel == TipoCombustivel.ETANOL ||
                tipoCombustivel == TipoCombustivel.ELETRICO);
    }
    
    /**
     * Retorna uma descrição resumida das especificações.
     * 
     * @return Descrição das especificações
     */
    public String getDescricaoResumo() {
        StringBuilder sb = new StringBuilder();
        sb.append(categoria.getNome());
        sb.append(" ").append(cor);
        sb.append(" ").append(tipoCombustivel.getNome());
        
        if (cilindrada != null) {
            sb.append(" ").append(getCilindradaFormatada());
        }
        
        return sb.toString();
    }
    
    /**
     * Valida a cor do veículo.
     * 
     * @param cor Cor a ser validada
     * @return Cor validada
     * @throws IllegalArgumentException se a cor for inválida
     */
    private static String validarCor(String cor) {
        if (cor == null || cor.trim().isEmpty()) {
            throw new IllegalArgumentException("Cor não pode ser nula ou vazia");
        }
        
        String corLimpa = cor.trim();
        if (corLimpa.length() > 50) {
            throw new IllegalArgumentException("Cor não pode ter mais de 50 caracteres");
        }
        
        return corLimpa;
    }
    
    /**
     * Valida o tipo de combustível.
     * 
     * @param tipoCombustivel Tipo a ser validado
     * @return Tipo validado
     * @throws IllegalArgumentException se o tipo for inválido
     */
    private static TipoCombustivel validarTipoCombustivel(TipoCombustivel tipoCombustivel) {
        if (tipoCombustivel == null) {
            throw new IllegalArgumentException("Tipo de combustível não pode ser nulo");
        }
        return tipoCombustivel;
    }
    
    /**
     * Valida a categoria do veículo.
     * 
     * @param categoria Categoria a ser validada
     * @return Categoria validada
     * @throws IllegalArgumentException se a categoria for inválida
     */
    private static CategoriaVeiculo validarCategoria(CategoriaVeiculo categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria não pode ser nula");
        }
        return categoria;
    }
    
    /**
     * Valida a cilindrada.
     * 
     * @param cilindrada Cilindrada a ser validada
     * @param categoria Categoria para validação de compatibilidade
     * @return Cilindrada validada
     * @throws IllegalArgumentException se a cilindrada for inválida
     */
    private static Integer validarCilindrada(Integer cilindrada, CategoriaVeiculo categoria) {
        if (cilindrada == null) {
            return null; // Cilindrada é opcional
        }
        
        if (cilindrada <= 0) {
            throw new IllegalArgumentException("Cilindrada deve ser maior que zero");
        }
        
        if (cilindrada > 20000) { // 20L máximo
            throw new IllegalArgumentException("Cilindrada não pode ser superior a 20000cc");
        }
        
        return cilindrada;
    }
    
    /**
     * Valida a compatibilidade entre as especificações.
     * 
     * @param combustivel Tipo de combustível
     * @param categoria Categoria do veículo
     * @param cilindrada Cilindrada
     * @throws IllegalArgumentException se houver incompatibilidade
     */
    private static void validarCompatibilidade(TipoCombustivel combustivel, 
                                             CategoriaVeiculo categoria, 
                                             Integer cilindrada) {
        
        if (!combustivel.isCompativelCom(categoria)) {
            throw new IllegalArgumentException(
                String.format("Combustível %s não é compatível com categoria %s", 
                    combustivel.getNome(), categoria.getNome()));
        }
        
        if (cilindrada != null && !categoria.isCompativelComCilindrada(cilindrada)) {
            throw new IllegalArgumentException(
                String.format("Cilindrada %dcc não é compatível com categoria %s", 
                    cilindrada, categoria.getNome()));
        }
    }
    
    /**
     * Gera uma especificação de exemplo para testes.
     * 
     * @return Especificação de exemplo
     */
    public static Especificacao exemplo() {
        return of("Branco", TipoCombustivel.FLEX, CategoriaVeiculo.PASSEIO, 1600);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Especificacao that = (Especificacao) obj;
        return Objects.equals(cor, that.cor) &&
               tipoCombustivel == that.tipoCombustivel &&
               categoria == that.categoria &&
               Objects.equals(cilindrada, that.cilindrada);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cor, tipoCombustivel, categoria, cilindrada);
    }
    
    @Override
    public String toString() {
        return getDescricaoResumo();
    }
}