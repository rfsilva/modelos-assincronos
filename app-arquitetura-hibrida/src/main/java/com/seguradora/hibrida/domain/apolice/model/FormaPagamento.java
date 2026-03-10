package com.seguradora.hibrida.domain.apolice.model;

import java.math.BigDecimal;

/**
 * Enum que representa as formas de pagamento disponíveis para apólices.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum FormaPagamento {
    
    /**
     * Pagamento mensal - 12 parcelas.
     */
    MENSAL("Mensal", 12, new BigDecimal("1.05")),
    
    /**
     * Pagamento trimestral - 4 parcelas.
     */
    TRIMESTRAL("Trimestral", 4, new BigDecimal("1.02")),
    
    /**
     * Pagamento semestral - 2 parcelas.
     */
    SEMESTRAL("Semestral", 2, new BigDecimal("1.01")),
    
    /**
     * Pagamento anual - à vista.
     */
    ANUAL("Anual", 1, new BigDecimal("0.95"));
    
    private final String descricao;
    private final int numeroParcelas;
    private final BigDecimal fatorAjuste;
    
    FormaPagamento(String descricao, int numeroParcelas, BigDecimal fatorAjuste) {
        this.descricao = descricao;
        this.numeroParcelas = numeroParcelas;
        this.fatorAjuste = fatorAjuste;
    }
    
    /**
     * Retorna a descrição da forma de pagamento.
     */
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Retorna o número de parcelas.
     */
    public int getNumeroParcelas() {
        return numeroParcelas;
    }
    
    /**
     * Retorna o fator de ajuste no valor (desconto para à vista, acréscimo para parcelado).
     */
    public BigDecimal getFatorAjuste() {
        return fatorAjuste;
    }
    
    /**
     * Verifica se é pagamento à vista.
     */
    public boolean isAVista() {
        return this == ANUAL;
    }
    
    /**
     * Verifica se é pagamento parcelado.
     */
    public boolean isParcelado() {
        return !isAVista();
    }
    
    /**
     * Calcula o valor da parcela baseado no valor total.
     */
    public Valor calcularValorParcela(Valor valorTotal) {
        if (valorTotal == null) {
            throw new IllegalArgumentException("Valor total não pode ser nulo");
        }
        
        return valorTotal.dividir(numeroParcelas);
    }
    
    /**
     * Calcula o valor total com ajustes baseado no valor base.
     */
    public Valor calcularValorTotalComAjuste(Valor valorBase) {
        if (valorBase == null) {
            throw new IllegalArgumentException("Valor base não pode ser nulo");
        }
        
        return valorBase.multiplicar(fatorAjuste);
    }
    
    @Override
    public String toString() {
        return descricao + " (" + numeroParcelas + "x)";
    }
}