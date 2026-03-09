package com.seguradora.hibrida.domain.segurado.model;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Value Object representando um Email válido.
 * 
 * <p>Este value object garante que apenas emails válidos sejam criados,
 * aplicando validações de formato robustas.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Getter
@EqualsAndHashCode
public class Email implements Serializable {
    
    // Regex robusta para validação de email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
    );
    
    private final String endereco;
    
    /**
     * Construtor privado para garantir validação.
     */
    private Email(String endereco) {
        this.endereco = endereco.toLowerCase().trim();
    }
    
    /**
     * Factory method para criar Email válido.
     * 
     * @param email String contendo o endereço de email
     * @return Email válido
     * @throws BusinessRuleViolationException se email for inválido
     */
    public static Email of(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessRuleViolationException(
                "Email é obrigatório", 
                List.of("Email não pode ser nulo ou vazio")
            );
        }
        
        String emailLimpo = email.trim().toLowerCase();
        
        if (emailLimpo.length() > 254) {
            throw new BusinessRuleViolationException(
                "Email muito longo", 
                List.of("Email não pode ter mais de 254 caracteres")
            );
        }
        
        if (!EMAIL_PATTERN.matcher(emailLimpo).matches()) {
            throw new BusinessRuleViolationException(
                "Email inválido", 
                List.of("Email deve estar em formato válido (exemplo@dominio.com)")
            );
        }
        
        // Validações adicionais
        String[] partes = emailLimpo.split("@");
        if (partes.length != 2) {
            throw new BusinessRuleViolationException(
                "Email inválido", 
                List.of("Email deve conter exatamente um símbolo @")
            );
        }
        
        String localPart = partes[0];
        String domainPart = partes[1];
        
        if (localPart.length() > 64) {
            throw new BusinessRuleViolationException(
                "Email inválido", 
                List.of("Parte local do email não pode ter mais de 64 caracteres")
            );
        }
        
        if (domainPart.length() > 253) {
            throw new BusinessRuleViolationException(
                "Email inválido", 
                List.of("Domínio do email não pode ter mais de 253 caracteres")
            );
        }
        
        return new Email(emailLimpo);
    }
    
    /**
     * Retorna o domínio do email.
     */
    public String getDominio() {
        return endereco.substring(endereco.indexOf('@') + 1);
    }
    
    /**
     * Retorna a parte local do email (antes do @).
     */
    public String getParteLocal() {
        return endereco.substring(0, endereco.indexOf('@'));
    }
    
    /**
     * Verifica se é um email corporativo (não é provedor público).
     */
    public boolean isCorporativo() {
        String dominio = getDominio().toLowerCase();
        return !dominio.equals("gmail.com") && 
               !dominio.equals("hotmail.com") && 
               !dominio.equals("yahoo.com") && 
               !dominio.equals("outlook.com") &&
               !dominio.equals("uol.com.br") &&
               !dominio.equals("terra.com.br") &&
               !dominio.equals("bol.com.br");
    }
    
    @Override
    public String toString() {
        return endereco;
    }
}