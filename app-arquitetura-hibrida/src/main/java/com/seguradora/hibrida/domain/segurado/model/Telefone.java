package com.seguradora.hibrida.domain.segurado.model;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Value Object representando um Telefone brasileiro válido.
 * 
 * <p>Este value object garante que apenas telefones válidos sejam criados,
 * aplicando validações específicas para o formato brasileiro.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Getter
@EqualsAndHashCode
public class Telefone implements Serializable {
    
    private static final Pattern TELEFONE_PATTERN = Pattern.compile("\\d{10,11}");
    
    // DDDs válidos no Brasil
    private static final Set<String> DDDS_VALIDOS = Set.of(
        "11", "12", "13", "14", "15", "16", "17", "18", "19", // SP
        "21", "22", "24", // RJ
        "27", "28", // ES
        "31", "32", "33", "34", "35", "37", "38", // MG
        "41", "42", "43", "44", "45", "46", // PR
        "47", "48", "49", // SC
        "51", "53", "54", "55", // RS
        "61", // DF
        "62", "64", // GO
        "63", // TO
        "65", "66", // MT
        "67", // MS
        "68", // AC
        "69", // RO
        "71", "73", "74", "75", "77", // BA
        "79", // SE
        "81", "87", // PE
        "82", // AL
        "83", // PB
        "84", // RN
        "85", "88", // CE
        "86", "89", // PI
        "91", "93", "94", // PA
        "92", "97", // AM
        "95", // RR
        "96", // AP
        "98", "99" // MA
    );
    
    private final String numero;
    private final String ddd;
    private final boolean isCelular;
    
    /**
     * Construtor privado para garantir validação.
     */
    private Telefone(String numero, String ddd, boolean isCelular) {
        this.numero = numero;
        this.ddd = ddd;
        this.isCelular = isCelular;
    }
    
    /**
     * Factory method para criar Telefone válido.
     * 
     * @param telefone String contendo o telefone (apenas números)
     * @return Telefone válido
     * @throws BusinessRuleViolationException se telefone for inválido
     */
    public static Telefone of(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            throw new BusinessRuleViolationException(
                "Telefone é obrigatório", 
                List.of("Telefone não pode ser nulo ou vazio")
            );
        }
        
        // Remove formatação se houver
        String telefoneLimpo = telefone.replaceAll("[^0-9]", "");
        
        if (!TELEFONE_PATTERN.matcher(telefoneLimpo).matches()) {
            throw new BusinessRuleViolationException(
                "Telefone inválido", 
                List.of("Telefone deve conter 10 ou 11 dígitos numéricos")
            );
        }
        
        String ddd;
        String numeroSemDdd;
        boolean isCelular;
        
        if (telefoneLimpo.length() == 10) {
            // Telefone fixo: (XX) XXXX-XXXX
            ddd = telefoneLimpo.substring(0, 2);
            numeroSemDdd = telefoneLimpo.substring(2);
            isCelular = false;
            
            // Validar se primeiro dígito não é 9 (celular)
            if (numeroSemDdd.charAt(0) == '9') {
                throw new BusinessRuleViolationException(
                    "Telefone inválido", 
                    List.of("Telefone fixo não pode começar com 9")
                );
            }
        } else {
            // Celular: (XX) 9XXXX-XXXX
            ddd = telefoneLimpo.substring(0, 2);
            numeroSemDdd = telefoneLimpo.substring(2);
            isCelular = true;
            
            // Validar se primeiro dígito é 9 (celular)
            if (numeroSemDdd.charAt(0) != '9') {
                throw new BusinessRuleViolationException(
                    "Telefone inválido", 
                    List.of("Celular deve começar com 9")
                );
            }
        }
        
        // Validar DDD
        if (!DDDS_VALIDOS.contains(ddd)) {
            throw new BusinessRuleViolationException(
                "DDD inválido", 
                List.of("DDD " + ddd + " não é válido no Brasil")
            );
        }
        
        return new Telefone(telefoneLimpo, ddd, isCelular);
    }
    
    /**
     * Retorna o telefone formatado.
     * Fixo: (XX) XXXX-XXXX
     * Celular: (XX) 9XXXX-XXXX
     */
    public String getFormatado() {
        if (isCelular) {
            return String.format("(%s) %s-%s",
                ddd,
                numero.substring(2, 7),
                numero.substring(7)
            );
        } else {
            return String.format("(%s) %s-%s",
                ddd,
                numero.substring(2, 6),
                numero.substring(6)
            );
        }
    }
    
    /**
     * Retorna apenas o número sem DDD.
     */
    public String getNumeroSemDdd() {
        return numero.substring(2);
    }
    
    /**
     * Verifica se é um número WhatsApp (celular).
     */
    public boolean isWhatsApp() {
        return isCelular;
    }
    
    @Override
    public String toString() {
        return getFormatado();
    }
}