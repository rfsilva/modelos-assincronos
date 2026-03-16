package com.seguradora.hibrida.domain.veiculo.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que representa uma placa de veículo brasileira.
 * 
 * <p>Suporta tanto o formato antigo (ABC-1234) quanto o novo formato
 * Mercosul (ABC1D23), com validações específicas para cada padrão.
 * 
 * <p>Este é um Value Object imutável que encapsula as regras de negócio
 * e validações relacionadas a placas de veículos no Brasil.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public final class Placa implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Padrões regex para validação
    private static final Pattern FORMATO_ANTIGO = Pattern.compile("^[A-Z]{3}-?[0-9]{4}$");
    private static final Pattern FORMATO_MERCOSUL = Pattern.compile("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");
    private static final Pattern FORMATO_MERCOSUL_COM_HIFEN = Pattern.compile("^[A-Z]{3}-?[0-9][A-Z][0-9]{2}$");
    
    private final String valor;
    private final boolean mercosul;
    
    /**
     * Construtor privado.
     * 
     * @param valor Valor da placa já validado
     * @param mercosul Se é formato Mercosul
     */
    private Placa(String valor, boolean mercosul) {
        this.valor = valor;
        this.mercosul = mercosul;
    }
    
    /**
     * Cria uma instância de Placa a partir de uma string.
     *
     * @param placa String representando a placa
     * @return Instância de Placa
     * @throws IllegalArgumentException se a placa for inválida
     */
    public static Placa of(String placa) {
        if (placa == null) {
            throw new IllegalArgumentException("Placa não pode ser nula ou vazia");
        }

        String placaLimpa = placa.trim().toUpperCase().replace("-", "");

        // Validar tamanho (incluindo string vazia após trim)
        if (placaLimpa.length() != 7) {
            throw new IllegalArgumentException("Placa deve ter 7 caracteres");
        }

        // Validar três primeiras posições devem ser letras
        if (!placaLimpa.substring(0, 3).matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("As três primeiras posições devem ser letras");
        }

        // Validar caracteres proibidos
        if (!validarCaracteresPermitidos(placaLimpa)) {
            throw new IllegalArgumentException("Placa não pode conter as letras I, O ou Q");
        }

        // Validar formato
        if (!isFormatoValido(placaLimpa)) {
            throw new IllegalArgumentException("Formato de placa inválido: " + placa);
        }

        // Determinar se é Mercosul
        boolean isMercosul = isMercosul(placaLimpa);

        return new Placa(placaLimpa, isMercosul);
    }
    
    /**
     * Retorna o valor da placa sem formatação.
     * 
     * @return Valor da placa (ex: "ABC1234")
     */
    public String getValor() {
        return valor;
    }
    
    /**
     * Retorna a placa formatada com hífen.
     * 
     * @return Placa formatada (ex: "ABC-1234" ou "ABC-1D23")
     */
    public String getFormatada() {
        if (mercosul) {
            // Formato Mercosul: ABC-1D23
            return String.format("%s-%s%s%s",
                valor.substring(0, 3),
                valor.substring(3, 4),
                valor.substring(4, 5),
                valor.substring(5, 7));
        } else {
            // Formato antigo: ABC-1234
            return String.format("%s-%s",
                valor.substring(0, 3),
                valor.substring(3, 7));
        }
    }
    
    /**
     * Verifica se é placa no formato Mercosul.
     * 
     * @return true se é formato Mercosul
     */
    public boolean isMercosul() {
        return mercosul;
    }
    
    /**
     * Verifica se é placa no formato antigo.
     * 
     * @return true se é formato antigo
     */
    public boolean isFormatoAntigo() {
        return !mercosul;
    }
    
    /**
     * Retorna as letras da placa.
     * 
     * @return Três primeiras letras da placa
     */
    public String getLetras() {
        return valor.substring(0, 3);
    }
    
    /**
     * Retorna os números da placa.
     * 
     * @return Parte numérica da placa
     */
    public String getNumeros() {
        if (mercosul) {
            // Mercosul: 1D23 -> 123
            return valor.substring(3, 4) + valor.substring(5, 7);
        } else {
            // Antigo: 1234
            return valor.substring(3, 7);
        }
    }
    
    /**
     * Retorna a letra do meio (apenas para Mercosul).
     * 
     * @return Letra do meio ou null se não for Mercosul
     */
    public String getLetraMeio() {
        return mercosul ? valor.substring(4, 5) : null;
    }
    
    /**
     * Converte placa antiga para formato Mercosul equivalente.
     * 
     * <p>Esta conversão é apenas ilustrativa, pois na prática
     * a conversão real depende de regras específicas do Detran.
     * 
     * @return Placa no formato Mercosul ou esta mesma se já for Mercosul
     */
    public Placa converterParaMercosul() {
        if (mercosul) {
            return this;
        }
        
        // Conversão ilustrativa: ABC1234 -> ABC1A23
        // Na prática, seria necessário consultar tabela de conversão oficial
        String letras = getLetras();
        String numeros = getNumeros();
        
        // Usar primeira letra disponível baseada no último dígito
        char letraMeio = (char) ('A' + (Character.getNumericValue(numeros.charAt(3)) % 10));
        
        String novaPlaca = letras + numeros.substring(0, 1) + letraMeio + numeros.substring(1, 3);
        
        return new Placa(novaPlaca, true);
    }
    
    /**
     * Valida se uma string representa um formato de placa válido.
     * 
     * @param placa String a ser validada
     * @return true se é formato válido
     */
    private static boolean isFormatoValido(String placa) {
        if (placa == null || placa.length() < 7 || placa.length() > 8) {
            return false;
        }
        
        String placaSemHifen = placa.replace("-", "");
        
        return FORMATO_ANTIGO.matcher(placaSemHifen).matches() ||
               FORMATO_MERCOSUL.matcher(placaSemHifen).matches();
    }
    
    /**
     * Verifica se uma placa está no formato Mercosul.
     * 
     * @param placa Placa a ser verificada (sem hífen)
     * @return true se é formato Mercosul
     */
    private static boolean isMercosul(String placa) {
        return FORMATO_MERCOSUL.matcher(placa).matches();
    }
    
    /**
     * Valida caracteres permitidos em placas.
     * 
     * <p>Letras I, O e Q não são permitidas em placas brasileiras
     * para evitar confusão com números.
     * 
     * @param placa Placa a ser validada
     * @return true se todos os caracteres são permitidos
     */
    public static boolean validarCaracteresPermitidos(String placa) {
        if (placa == null) {
            return false;
        }
        
        String placaLimpa = placa.toUpperCase().replace("-", "");
        
        // Verificar se contém letras proibidas
        return !placaLimpa.contains("I") && 
               !placaLimpa.contains("O") && 
               !placaLimpa.contains("Q");
    }
    
    /**
     * Gera uma placa de exemplo para testes.
     * 
     * @param mercosul Se deve ser formato Mercosul
     * @return Placa de exemplo
     */
    public static Placa exemplo(boolean mercosul) {
        if (mercosul) {
            return new Placa("ABC1D23", true);
        } else {
            return new Placa("ABC1234", false);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Placa placa = (Placa) obj;
        return Objects.equals(valor, placa.valor);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
    
    @Override
    public String toString() {
        return getFormatada();
    }
}