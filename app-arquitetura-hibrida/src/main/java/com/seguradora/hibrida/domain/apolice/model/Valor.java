package com.seguradora.hibrida.domain.apolice.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * Value Object que representa um valor monetário com moeda e precisão.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class Valor implements Serializable, Comparable<Valor> {
    
    private static final long serialVersionUID = 1L;
    
    private static final Currency MOEDA_PADRAO = Currency.getInstance("BRL");
    private static final int ESCALA_PADRAO = 2;
    private static final RoundingMode ARREDONDAMENTO_PADRAO = RoundingMode.HALF_UP;
    
    private final BigDecimal quantia;
    private final Currency moeda;
    
    private Valor(BigDecimal quantia, Currency moeda) {
        this.quantia = quantia.setScale(ESCALA_PADRAO, ARREDONDAMENTO_PADRAO);
        this.moeda = moeda;
    }
    
    /**
     * Cria um valor em reais.
     */
    public static Valor reais(BigDecimal quantia) {
        validarQuantia(quantia);
        return new Valor(quantia, MOEDA_PADRAO);
    }
    
    /**
     * Cria um valor em reais a partir de double.
     */
    public static Valor reais(double quantia) {
        return reais(BigDecimal.valueOf(quantia));
    }
    
    /**
     * Cria um valor em reais a partir de string.
     */
    public static Valor reais(String quantia) {
        if (quantia == null || quantia.trim().isEmpty()) {
            throw new IllegalArgumentException("Quantia não pode ser nula ou vazia");
        }
        
        try {
            return reais(new BigDecimal(quantia.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Quantia inválida: " + quantia, e);
        }
    }
    
    /**
     * Cria um valor zero em reais.
     */
    public static Valor zero() {
        return reais(BigDecimal.ZERO);
    }
    
    /**
     * Cria um valor com moeda específica.
     */
    public static Valor of(BigDecimal quantia, Currency moeda) {
        validarQuantia(quantia);
        
        if (moeda == null) {
            throw new IllegalArgumentException("Moeda não pode ser nula");
        }
        
        return new Valor(quantia, moeda);
    }
    
    private static void validarQuantia(BigDecimal quantia) {
        if (quantia == null) {
            throw new IllegalArgumentException("Quantia não pode ser nula");
        }
        
        if (quantia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantia não pode ser negativa");
        }
        
        // Limite máximo para valores de apólice (R$ 10 milhões)
        BigDecimal limiteMaximo = new BigDecimal("10000000.00");
        if (quantia.compareTo(limiteMaximo) > 0) {
            throw new IllegalArgumentException("Quantia excede o limite máximo de R$ 10.000.000,00");
        }
    }
    
    /**
     * Retorna a quantia.
     */
    public BigDecimal getQuantia() {
        return quantia;
    }
    
    /**
     * Retorna a moeda.
     */
    public Currency getMoeda() {
        return moeda;
    }
    
    /**
     * Verifica se o valor é zero.
     */
    public boolean isZero() {
        return quantia.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Verifica se o valor é positivo.
     */
    public boolean isPositivo() {
        return quantia.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Soma este valor com outro.
     */
    public Valor somar(Valor outro) {
        validarMoedaCompativel(outro);
        return new Valor(quantia.add(outro.quantia), moeda);
    }
    
    /**
     * Subtrai outro valor deste.
     */
    public Valor subtrair(Valor outro) {
        validarMoedaCompativel(outro);
        BigDecimal resultado = quantia.subtract(outro.quantia);
        
        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resultado da subtração não pode ser negativo");
        }
        
        return new Valor(resultado, moeda);
    }
    
    /**
     * Multiplica este valor por um fator.
     */
    public Valor multiplicar(BigDecimal fator) {
        if (fator == null) {
            throw new IllegalArgumentException("Fator não pode ser nulo");
        }
        
        if (fator.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fator não pode ser negativo");
        }
        
        return new Valor(quantia.multiply(fator), moeda);
    }
    
    /**
     * Multiplica este valor por um fator double.
     */
    public Valor multiplicar(double fator) {
        return multiplicar(BigDecimal.valueOf(fator));
    }
    
    /**
     * Divide este valor por um divisor.
     */
    public Valor dividir(BigDecimal divisor) {
        if (divisor == null) {
            throw new IllegalArgumentException("Divisor não pode ser nulo");
        }
        
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Divisor não pode ser zero");
        }
        
        return new Valor(quantia.divide(divisor, ESCALA_PADRAO, ARREDONDAMENTO_PADRAO), moeda);
    }
    
    /**
     * Divide este valor por um divisor inteiro.
     */
    public Valor dividir(int divisor) {
        return dividir(BigDecimal.valueOf(divisor));
    }
    
    /**
     * Calcula uma porcentagem deste valor.
     */
    public Valor porcentagem(BigDecimal percentual) {
        if (percentual == null) {
            throw new IllegalArgumentException("Percentual não pode ser nulo");
        }
        
        BigDecimal fator = percentual.divide(new BigDecimal("100"), 4, ARREDONDAMENTO_PADRAO);
        return multiplicar(fator);
    }
    
    /**
     * Calcula uma porcentagem deste valor.
     */
    public Valor porcentagem(double percentual) {
        return porcentagem(BigDecimal.valueOf(percentual));
    }
    
    /**
     * Verifica se este valor é maior que outro.
     */
    public boolean ehMaiorQue(Valor outro) {
        validarMoedaCompativel(outro);
        return quantia.compareTo(outro.quantia) > 0;
    }
    
    /**
     * Verifica se este valor é menor que outro.
     */
    public boolean ehMenorQue(Valor outro) {
        validarMoedaCompativel(outro);
        return quantia.compareTo(outro.quantia) < 0;
    }
    
    /**
     * Verifica se este valor é igual a outro.
     */
    public boolean ehIgualA(Valor outro) {
        validarMoedaCompativel(outro);
        return quantia.compareTo(outro.quantia) == 0;
    }
    
    /**
     * Verifica se este valor é maior ou igual a outro.
     */
    public boolean ehMaiorOuIgualA(Valor outro) {
        return ehMaiorQue(outro) || ehIgualA(outro);
    }
    
    /**
     * Verifica se este valor é menor ou igual a outro.
     */
    public boolean ehMenorOuIgualA(Valor outro) {
        return ehMenorQue(outro) || ehIgualA(outro);
    }
    
    private void validarMoedaCompativel(Valor outro) {
        if (outro == null) {
            throw new IllegalArgumentException("Valor não pode ser nulo");
        }
        
        if (!moeda.equals(outro.moeda)) {
            throw new IllegalArgumentException(
                String.format("Moedas incompatíveis: %s e %s", moeda.getCurrencyCode(), outro.moeda.getCurrencyCode())
            );
        }
    }
    
    /**
     * Retorna o valor formatado para exibição.
     */
    public String getFormatado() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        formatter.setCurrency(moeda);
        return formatter.format(quantia);
    }
    
    /**
     * Retorna o valor formatado sem símbolo da moeda.
     */
    public String getFormatadoSemSimbolo() {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        formatter.setMinimumFractionDigits(ESCALA_PADRAO);
        formatter.setMaximumFractionDigits(ESCALA_PADRAO);
        return formatter.format(quantia);
    }
    
    @Override
    public int compareTo(Valor outro) {
        validarMoedaCompativel(outro);
        return quantia.compareTo(outro.quantia);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Valor valor = (Valor) obj;
        return Objects.equals(quantia, valor.quantia) && Objects.equals(moeda, valor.moeda);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(quantia, moeda);
    }
    
    @Override
    public String toString() {
        return getFormatado();
    }
}