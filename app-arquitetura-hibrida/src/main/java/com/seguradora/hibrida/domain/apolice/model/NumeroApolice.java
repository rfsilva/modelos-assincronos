package com.seguradora.hibrida.domain.apolice.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que representa o número de uma apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class NumeroApolice implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Padrão: AP-YYYY-NNNNNN (ex: AP-2024-000001)
    private static final Pattern PATTERN = Pattern.compile("^AP-\\d{4}-\\d{6}$");
    private static final String PREFIX = "AP";
    
    private final String numero;
    
    private NumeroApolice(String numero) {
        this.numero = numero;
    }
    
    /**
     * Cria um NumeroApolice a partir de uma string.
     */
    public static NumeroApolice of(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("Número da apólice não pode ser nulo ou vazio");
        }
        
        String numeroLimpo = numero.trim().toUpperCase();
        
        if (!PATTERN.matcher(numeroLimpo).matches()) {
            throw new IllegalArgumentException(
                "Número da apólice deve seguir o padrão AP-YYYY-NNNNNN. Recebido: " + numero
            );
        }
        
        return new NumeroApolice(numeroLimpo);
    }
    
    /**
     * Gera um novo número de apólice baseado no ano e sequencial.
     */
    public static NumeroApolice gerar(int ano, long sequencial) {
        if (ano < 2000 || ano > 2100) {
            throw new IllegalArgumentException("Ano deve estar entre 2000 e 2100");
        }
        
        if (sequencial < 1 || sequencial > 999999) {
            throw new IllegalArgumentException("Sequencial deve estar entre 1 e 999999");
        }
        
        String numero = String.format("%s-%04d-%06d", PREFIX, ano, sequencial);
        return new NumeroApolice(numero);
    }
    
    /**
     * Gera um novo número de apólice para o ano atual.
     */
    public static NumeroApolice gerar(long sequencial) {
        return gerar(LocalDate.now().getYear(), sequencial);
    }
    
    /**
     * Retorna o número da apólice.
     */
    public String getNumero() {
        return numero;
    }
    
    /**
     * Retorna o ano da apólice.
     */
    public int getAno() {
        String[] partes = numero.split("-");
        return Integer.parseInt(partes[1]);
    }
    
    /**
     * Retorna o sequencial da apólice.
     */
    public long getSequencial() {
        String[] partes = numero.split("-");
        return Long.parseLong(partes[2]);
    }
    
    /**
     * Retorna o número formatado para exibição.
     */
    public String getFormatado() {
        return numero;
    }
    
    /**
     * Verifica se é uma apólice do ano atual.
     */
    public boolean isAnoAtual() {
        return getAno() == LocalDate.now().getYear();
    }
    
    /**
     * Verifica se é uma apólice de um ano específico.
     */
    public boolean isDoAno(int ano) {
        return getAno() == ano;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NumeroApolice that = (NumeroApolice) obj;
        return Objects.equals(numero, that.numero);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }
    
    @Override
    public String toString() {
        return numero;
    }
}