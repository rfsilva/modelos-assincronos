package com.seguradora.hibrida.domain.veiculo.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Value Object representando uma placa de veículo brasileira.
 * Suporta tanto o formato antigo (ABC-1234) quanto o formato Mercosul (ABC1D23).
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Getter
@EqualsAndHashCode
@ToString
public class Placa implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Formato antigo: ABC-1234 ou ABC1234
    private static final Pattern FORMATO_ANTIGO = Pattern.compile("^[A-Z]{3}-?\\d{4}$");
    
    // Formato Mercosul: ABC1D23 ou ABC-1D23
    private static final Pattern FORMATO_MERCOSUL = Pattern.compile("^[A-Z]{3}\\d[A-Z]\\d{2}$");
    
    private final String valor;
    private final boolean mercosul;
    
    /**
     * Construtor privado para criar uma placa válida.
     * 
     * @param valor Valor da placa
     * @param mercosul Se é formato Mercosul
     */
    private Placa(String valor, boolean mercosul) {
        this.valor = valor;
        this.mercosul = mercosul;
    }
    
    /**
     * Factory method para criar uma placa a partir de uma string.
     * 
     * @param placa String representando a placa
     * @return Instância de Placa
     * @throws IllegalArgumentException se a placa for inválida
     */
    public static Placa of(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("Placa não pode ser vazia");
        }
        
        // Normalizar: remover espaços e converter para maiúsculas
        String placaNormalizada = placa.trim().toUpperCase().replace(" ", "");
        
        // Remover hífen para validação e armazenamento
        String placaSemHifen = placaNormalizada.replace("-", "");
        
        // Verificar formato Mercosul
        if (FORMATO_MERCOSUL.matcher(placaSemHifen).matches()) {
            return new Placa(placaSemHifen, true);
        }
        
        // Verificar formato antigo
        if (FORMATO_ANTIGO.matcher(placaNormalizada).matches()) {
            return new Placa(placaSemHifen, false);
        }
        
        throw new IllegalArgumentException(
                "Formato de placa inválido: " + placa + 
                ". Use ABC1234 (antigo) ou ABC1D23 (Mercosul)");
    }
    
    /**
     * Retorna a placa formatada com hífen.
     * 
     * @return Placa formatada (ABC-1234 ou ABC-1D23)
     */
    public String getFormatada() {
        if (mercosul) {
            // ABC1D23 -> ABC-1D23
            return String.format("%s-%s", 
                    valor.substring(0, 4), 
                    valor.substring(4));
        } else {
            // ABC1234 -> ABC-1234
            return String.format("%s-%s", 
                    valor.substring(0, 3), 
                    valor.substring(3));
        }
    }
    
    /**
     * Verifica se a placa é do formato Mercosul.
     * 
     * @return true se for Mercosul, false se for formato antigo
     */
    public boolean isMercosul() {
        return mercosul;
    }
}
