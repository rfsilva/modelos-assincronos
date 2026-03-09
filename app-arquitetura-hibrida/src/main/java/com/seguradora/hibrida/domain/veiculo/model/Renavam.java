package com.seguradora.hibrida.domain.veiculo.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Value Object representando um RENAVAM (Registro Nacional de Veículos Automotores).
 * Implementa validação de dígito verificador conforme algoritmo oficial.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Getter
@EqualsAndHashCode
@ToString
public class Renavam implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final int TAMANHO_RENAVAM = 11;
    private static final String SEQUENCIA_MULTIPLICADORA = "3298765432";
    
    private final String valor;
    
    /**
     * Construtor privado para criar um RENAVAM válido.
     * 
     * @param valor Valor do RENAVAM (11 dígitos)
     */
    private Renavam(String valor) {
        this.valor = valor;
    }
    
    /**
     * Factory method para criar um RENAVAM a partir de uma string.
     * 
     * @param renavam String representando o RENAVAM
     * @return Instância de Renavam
     * @throws IllegalArgumentException se o RENAVAM for inválido
     */
    public static Renavam of(String renavam) {
        if (renavam == null || renavam.isBlank()) {
            throw new IllegalArgumentException("RENAVAM não pode ser vazio");
        }
        
        // Remover espaços e caracteres não numéricos
        String renavamLimpo = renavam.replaceAll("\\D", "");
        
        // Validar tamanho
        if (renavamLimpo.length() != TAMANHO_RENAVAM) {
            throw new IllegalArgumentException(
                    "RENAVAM deve conter exatamente 11 dígitos. Recebido: " + renavamLimpo.length());
        }
        
        // Validar dígito verificador
        if (!validarDigitoVerificador(renavamLimpo)) {
            throw new IllegalArgumentException("RENAVAM com dígito verificador inválido");
        }
        
        return new Renavam(renavamLimpo);
    }
    
    /**
     * Valida o dígito verificador do RENAVAM usando o algoritmo oficial.
     * 
     * @param renavam RENAVAM completo com 11 dígitos
     * @return true se o dígito verificador for válido
     */
    private static boolean validarDigitoVerificador(String renavam) {
        try {
            // Extrair os primeiros 10 dígitos e o dígito verificador
            String base = renavam.substring(0, 10);
            int digitoInformado = Character.getNumericValue(renavam.charAt(10));
            
            // Calcular o dígito verificador
            int soma = 0;
            for (int i = 0; i < 10; i++) {
                int digito = Character.getNumericValue(base.charAt(i));
                int multiplicador = Character.getNumericValue(SEQUENCIA_MULTIPLICADORA.charAt(i));
                soma += digito * multiplicador;
            }
            
            // O dígito verificador é o resto da divisão por 11
            // Se o resto for 0 ou 1, o dígito é 0
            int digitoCalculado = soma % 11;
            if (digitoCalculado == 0 || digitoCalculado == 1) {
                digitoCalculado = 0;
            } else {
                digitoCalculado = 11 - digitoCalculado;
            }
            
            return digitoCalculado == digitoInformado;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Retorna o RENAVAM formatado (com espaços para melhor legibilidade).
     * 
     * @return RENAVAM formatado (ex: 12345678901)
     */
    public String getFormatado() {
        return valor;
    }
}
