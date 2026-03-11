package com.seguradora.hibrida.domain.veiculo.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object que representa um RENAVAM (Registro Nacional de Veículos Automotores).
 * 
 * <p>O RENAVAM é um número único de identificação de veículos no Brasil,
 * composto por 11 dígitos com dígito verificador calculado por algoritmo específico.
 * 
 * <p>Este Value Object encapsula as regras de validação do RENAVAM,
 * incluindo o cálculo e verificação do dígito verificador.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public final class Renavam implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final int TAMANHO_RENAVAM = 11;
    private static final String SEQUENCIA_MULTIPLICADORES = "3298765432";
    
    private final String valor;
    
    /**
     * Construtor privado.
     * 
     * @param valor Valor do RENAVAM já validado
     */
    private Renavam(String valor) {
        this.valor = valor;
    }
    
    /**
     * Cria uma instância de Renavam a partir de uma string.
     * 
     * @param renavam String representando o RENAVAM
     * @return Instância de Renavam
     * @throws IllegalArgumentException se o RENAVAM for inválido
     */
    public static Renavam of(String renavam) {
        if (renavam == null || renavam.trim().isEmpty()) {
            throw new IllegalArgumentException("RENAVAM não pode ser nulo ou vazio");
        }
        
        // Remove caracteres não numéricos
        String apenasNumeros = renavam.replaceAll("\\D", "");
        
        // Valida formato básico
        if (apenasNumeros.length() != TAMANHO_RENAVAM) {
            throw new IllegalArgumentException(
                String.format("RENAVAM deve ter %d dígitos. Fornecido: %s", 
                    TAMANHO_RENAVAM, renavam));
        }
        
        // Valida se são todos números
        if (!apenasNumeros.matches("\\d{" + TAMANHO_RENAVAM + "}")) {
            throw new IllegalArgumentException("RENAVAM deve conter apenas números: " + renavam);
        }
        
        // Valida sequência (não pode ser todos iguais)
        if (apenasNumeros.matches("(\\d)\\1{" + (TAMANHO_RENAVAM - 1) + "}")) {
            throw new IllegalArgumentException("RENAVAM não pode ter todos os dígitos iguais: " + renavam);
        }
        
        // Valida dígito verificador
        if (!validarDigitoVerificador(apenasNumeros)) {
            throw new IllegalArgumentException("Dígito verificador do RENAVAM inválido: " + renavam);
        }
        
        return new Renavam(apenasNumeros);
    }
    
    /**
     * Retorna o valor do RENAVAM sem formatação.
     * 
     * @return Valor do RENAVAM (11 dígitos)
     */
    public String getValor() {
        return valor;
    }
    
    /**
     * Retorna o RENAVAM formatado.
     * 
     * <p>Formato: 00000000000 (sem formatação específica padrão)
     * 
     * @return RENAVAM formatado
     */
    public String getFormatado() {
        // RENAVAM não tem formatação padrão oficial, retorna sem formatação
        return valor;
    }
    
    /**
     * Retorna os 10 primeiros dígitos (sem o dígito verificador).
     * 
     * @return Primeiros 10 dígitos
     */
    public String getNumeroBase() {
        return valor.substring(0, 10);
    }
    
    /**
     * Retorna o dígito verificador.
     * 
     * @return Último dígito (verificador)
     */
    public char getDigitoVerificador() {
        return valor.charAt(10);
    }
    
    /**
     * Valida o dígito verificador do RENAVAM.
     * 
     * <p>Algoritmo oficial do RENAVAM:
     * 1. Multiplica cada dígito pela sequência 3,2,9,8,7,6,5,4,3,2
     * 2. Soma todos os produtos
     * 3. Divide por 11 e pega o resto
     * 4. Se resto < 2, DV = 0; senão DV = 11 - resto
     * 
     * @param renavam RENAVAM completo (11 dígitos)
     * @return true se o dígito verificador está correto
     */
    public static boolean validarDigitoVerificador(String renavam) {
        if (renavam == null || renavam.length() != TAMANHO_RENAVAM) {
            return false;
        }
        
        try {
            // Pega os 10 primeiros dígitos
            String numeroBase = renavam.substring(0, 10);
            int digitoInformado = Character.getNumericValue(renavam.charAt(10));
            
            // Calcula o dígito verificador
            int soma = 0;
            for (int i = 0; i < 10; i++) {
                int digito = Character.getNumericValue(numeroBase.charAt(i));
                int multiplicador = Character.getNumericValue(SEQUENCIA_MULTIPLICADORES.charAt(i));
                soma += digito * multiplicador;
            }
            
            int resto = soma % 11;
            int digitoCalculado = (resto < 2) ? 0 : (11 - resto);
            
            return digitoCalculado == digitoInformado;
            
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Calcula o dígito verificador para um número base de RENAVAM.
     * 
     * @param numeroBase Primeiros 10 dígitos do RENAVAM
     * @return Dígito verificador calculado
     * @throws IllegalArgumentException se o número base for inválido
     */
    public static int calcularDigitoVerificador(String numeroBase) {
        if (numeroBase == null || numeroBase.length() != 10) {
            throw new IllegalArgumentException("Número base deve ter 10 dígitos");
        }
        
        if (!numeroBase.matches("\\d{10}")) {
            throw new IllegalArgumentException("Número base deve conter apenas dígitos");
        }
        
        int soma = 0;
        for (int i = 0; i < 10; i++) {
            int digito = Character.getNumericValue(numeroBase.charAt(i));
            int multiplicador = Character.getNumericValue(SEQUENCIA_MULTIPLICADORES.charAt(i));
            soma += digito * multiplicador;
        }
        
        int resto = soma % 11;
        return (resto < 2) ? 0 : (11 - resto);
    }
    
    /**
     * Gera um RENAVAM completo a partir de um número base.
     * 
     * @param numeroBase Primeiros 10 dígitos
     * @return RENAVAM completo com dígito verificador
     */
    public static Renavam gerarComDigitoVerificador(String numeroBase) {
        int digitoVerificador = calcularDigitoVerificador(numeroBase);
        String renavamCompleto = numeroBase + digitoVerificador;
        return new Renavam(renavamCompleto);
    }
    
    /**
     * Gera um RENAVAM de exemplo para testes.
     * 
     * @return RENAVAM válido de exemplo
     */
    public static Renavam exemplo() {
        return gerarComDigitoVerificador("1234567890");
    }
    
    /**
     * Verifica se uma string pode ser um RENAVAM válido.
     * 
     * @param renavam String a ser verificada
     * @return true se pode ser um RENAVAM válido
     */
    public static boolean isValido(String renavam) {
        try {
            of(renavam);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Formata um RENAVAM para exibição com máscara.
     * 
     * <p>Como não há padrão oficial, usa formato: 00000.000000
     * 
     * @return RENAVAM com máscara
     */
    public String getComMascara() {
        return String.format("%s.%s", 
            valor.substring(0, 5), 
            valor.substring(5, 11));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Renavam renavam = (Renavam) obj;
        return Objects.equals(valor, renavam.valor);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
    
    @Override
    public String toString() {
        return valor;
    }
}