package com.seguradora.hibrida.domain.segurado.model;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Value Object representando um CPF válido.
 * 
 * <p>Este value object garante que apenas CPFs válidos sejam criados,
 * aplicando todas as validações necessárias incluindo dígitos verificadores.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Getter
@EqualsAndHashCode
public class CPF implements Serializable {
    
    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{11}");
    
    private final String numero;
    
    /**
     * Construtor privado para garantir validação.
     */
    private CPF(String numero) {
        this.numero = numero;
    }
    
    /**
     * Factory method para criar CPF válido.
     * 
     * @param cpf String contendo o CPF (apenas números)
     * @return CPF válido
     * @throws BusinessRuleViolationException se CPF for inválido
     */
    public static CPF of(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new BusinessRuleViolationException(
                "CPF é obrigatório", 
                List.of("CPF não pode ser nulo ou vazio")
            );
        }
        
        // Remove formatação se houver
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        
        if (!CPF_PATTERN.matcher(cpfLimpo).matches()) {
            throw new BusinessRuleViolationException(
                "CPF inválido", 
                List.of("CPF deve conter exatamente 11 dígitos numéricos")
            );
        }
        
        // Rejeitar CPFs com todos os dígitos iguais
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            throw new BusinessRuleViolationException(
                "CPF inválido", 
                List.of("CPF não pode ter todos os dígitos iguais")
            );
        }
        
        // Validar dígitos verificadores
        if (!validarDigitosVerificadores(cpfLimpo)) {
            throw new BusinessRuleViolationException(
                "CPF inválido", 
                List.of("CPF não passou na validação de dígitos verificadores")
            );
        }
        
        return new CPF(cpfLimpo);
    }
    
    /**
     * Retorna o CPF formatado (XXX.XXX.XXX-XX).
     */
    public String getFormatado() {
        return String.format("%s.%s.%s-%s",
            numero.substring(0, 3),
            numero.substring(3, 6),
            numero.substring(6, 9),
            numero.substring(9, 11)
        );
    }
    
    /**
     * Validação de CPF com dígitos verificadores usando algoritmo oficial.
     */
    private static boolean validarDigitosVerificadores(String cpf) {
        try {
            // Calcular primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int primeiroDigito = 11 - (soma % 11);
            if (primeiroDigito >= 10) primeiroDigito = 0;
            
            // Calcular segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int segundoDigito = 11 - (soma % 11);
            if (segundoDigito >= 10) segundoDigito = 0;
            
            // Validar dígitos
            return Character.getNumericValue(cpf.charAt(9)) == primeiroDigito &&
                   Character.getNumericValue(cpf.charAt(10)) == segundoDigito;
                   
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return getFormatado();
    }
}