package com.seguradora.hibrida.domain.apolice.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value Object que representa o prêmio de uma apólice com suas parcelas e vencimentos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class Premio implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final Valor valorTotal;
    private final FormaPagamento formaPagamento;
    private final List<Parcela> parcelas;
    
    private Premio(Valor valorTotal, FormaPagamento formaPagamento, List<Parcela> parcelas) {
        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.parcelas = Collections.unmodifiableList(new ArrayList<>(parcelas));
    }
    
    /**
     * Cria um prêmio com forma de pagamento específica.
     */
    public static Premio of(Valor valorBase, FormaPagamento formaPagamento, LocalDate dataInicio) {
        validarParametros(valorBase, formaPagamento, dataInicio);
        
        Valor valorTotal = formaPagamento.calcularValorTotalComAjuste(valorBase);
        List<Parcela> parcelas = gerarParcelas(valorTotal, formaPagamento, dataInicio);
        
        return new Premio(valorTotal, formaPagamento, parcelas);
    }
    
    /**
     * Cria um prêmio anual à vista.
     */
    public static Premio anualAVista(Valor valorBase, LocalDate dataVencimento) {
        return of(valorBase, FormaPagamento.ANUAL, dataVencimento);
    }
    
    /**
     * Cria um prêmio mensal.
     */
    public static Premio mensal(Valor valorBase, LocalDate dataPrimeiraParcela) {
        return of(valorBase, FormaPagamento.MENSAL, dataPrimeiraParcela);
    }
    
    private static void validarParametros(Valor valorBase, FormaPagamento formaPagamento, LocalDate dataInicio) {
        if (valorBase == null) {
            throw new IllegalArgumentException("Valor base não pode ser nulo");
        }
        
        if (!valorBase.isPositivo()) {
            throw new IllegalArgumentException("Valor base deve ser positivo");
        }
        
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento não pode ser nula");
        }
        
        if (dataInicio == null) {
            throw new IllegalArgumentException("Data de início não pode ser nula");
        }
        
        if (dataInicio.isBefore(LocalDate.now().minusDays(30))) {
            throw new IllegalArgumentException("Data de início não pode ser muito anterior à data atual");
        }
    }
    
    private static List<Parcela> gerarParcelas(Valor valorTotal, FormaPagamento formaPagamento, LocalDate dataInicio) {
        List<Parcela> parcelas = new ArrayList<>();
        
        Valor valorParcela = formaPagamento.calcularValorParcela(valorTotal);
        int numeroParcelas = formaPagamento.getNumeroParcelas();
        
        for (int i = 0; i < numeroParcelas; i++) {
            LocalDate dataVencimento = calcularDataVencimento(dataInicio, formaPagamento, i);
            
            // Ajustar última parcela para eventuais diferenças de arredondamento
            Valor valorFinal = valorParcela;
            if (i == numeroParcelas - 1) {
                Valor totalParcelas = valorParcela.multiplicar(numeroParcelas - 1);
                valorFinal = valorTotal.subtrair(totalParcelas);
            }
            
            parcelas.add(new Parcela(i + 1, valorFinal, dataVencimento));
        }
        
        return parcelas;
    }
    
    private static LocalDate calcularDataVencimento(LocalDate dataInicio, FormaPagamento formaPagamento, int indiceParcela) {
        return switch (formaPagamento) {
            case ANUAL -> dataInicio;
            case SEMESTRAL -> dataInicio.plusMonths(indiceParcela * 6L);
            case TRIMESTRAL -> dataInicio.plusMonths(indiceParcela * 3L);
            case MENSAL -> dataInicio.plusMonths(indiceParcela);
        };
    }
    
    /**
     * Retorna o valor total do prêmio.
     */
    public Valor getValorTotal() {
        return valorTotal;
    }
    
    /**
     * Retorna a forma de pagamento.
     */
    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }
    
    /**
     * Retorna a lista de parcelas.
     */
    public List<Parcela> getParcelas() {
        return parcelas;
    }
    
    /**
     * Retorna o número de parcelas.
     */
    public int getNumeroParcelas() {
        return parcelas.size();
    }
    
    /**
     * Retorna o valor da primeira parcela.
     */
    public Valor getValorPrimeiraParcela() {
        return parcelas.isEmpty() ? Valor.zero() : parcelas.get(0).getValor();
    }
    
    /**
     * Retorna a data de vencimento da primeira parcela.
     */
    public LocalDate getDataPrimeiraParcela() {
        return parcelas.isEmpty() ? null : parcelas.get(0).getDataVencimento();
    }
    
    /**
     * Retorna a data de vencimento da última parcela.
     */
    public LocalDate getDataUltimaParcela() {
        return parcelas.isEmpty() ? null : parcelas.get(parcelas.size() - 1).getDataVencimento();
    }
    
    /**
     * Verifica se é pagamento à vista.
     */
    public boolean isAVista() {
        return formaPagamento.isAVista();
    }
    
    /**
     * Verifica se é pagamento parcelado.
     */
    public boolean isParcelado() {
        return formaPagamento.isParcelado();
    }
    
    /**
     * Retorna as parcelas vencidas até uma data específica.
     */
    public List<Parcela> getParcelasVencidasAte(LocalDate data) {
        if (data == null) {
            return Collections.emptyList();
        }
        
        return parcelas.stream()
                .filter(parcela -> !parcela.getDataVencimento().isAfter(data))
                .toList();
    }
    
    /**
     * Retorna as parcelas a vencer após uma data específica.
     */
    public List<Parcela> getParcelasAVencerApos(LocalDate data) {
        if (data == null) {
            return new ArrayList<>(parcelas);
        }
        
        return parcelas.stream()
                .filter(parcela -> parcela.getDataVencimento().isAfter(data))
                .toList();
    }
    
    /**
     * Calcula o valor total das parcelas vencidas até uma data.
     */
    public Valor getValorVencidoAte(LocalDate data) {
        return getParcelasVencidasAte(data).stream()
                .map(Parcela::getValor)
                .reduce(Valor.zero(), Valor::somar);
    }
    
    /**
     * Calcula o valor total das parcelas a vencer.
     */
    public Valor getValorAVencer(LocalDate data) {
        return getParcelasAVencerApos(data).stream()
                .map(Parcela::getValor)
                .reduce(Valor.zero(), Valor::somar);
    }
    
    /**
     * Verifica se há parcelas vencidas.
     */
    public boolean temParcelasVencidas() {
        return !getParcelasVencidasAte(LocalDate.now()).isEmpty();
    }
    
    /**
     * Retorna uma representação resumida do prêmio.
     */
    public String getResumo() {
        if (isAVista()) {
            return String.format("À vista: %s", valorTotal.getFormatado());
        } else {
            return String.format("%dx %s = %s", 
                getNumeroParcelas(), 
                getValorPrimeiraParcela().getFormatado(), 
                valorTotal.getFormatado()
            );
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Premio premio = (Premio) obj;
        return Objects.equals(valorTotal, premio.valorTotal) &&
               formaPagamento == premio.formaPagamento &&
               Objects.equals(parcelas, premio.parcelas);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(valorTotal, formaPagamento, parcelas);
    }
    
    @Override
    public String toString() {
        return getResumo();
    }
    
    /**
     * Classe interna que representa uma parcela do prêmio.
     */
    public static class Parcela implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private final int numero;
        private final Valor valor;
        private final LocalDate dataVencimento;
        
        public Parcela(int numero, Valor valor, LocalDate dataVencimento) {
            this.numero = numero;
            this.valor = valor;
            this.dataVencimento = dataVencimento;
        }
        
        public int getNumero() {
            return numero;
        }
        
        public Valor getValor() {
            return valor;
        }
        
        public LocalDate getDataVencimento() {
            return dataVencimento;
        }
        
        public boolean isVencida() {
            return LocalDate.now().isAfter(dataVencimento);
        }
        
        public boolean venceHoje() {
            return LocalDate.now().isEqual(dataVencimento);
        }
        
        public boolean venceEm(int dias) {
            LocalDate dataLimite = LocalDate.now().plusDays(dias);
            return dataVencimento.isBefore(dataLimite) || dataVencimento.isEqual(dataLimite);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Parcela parcela = (Parcela) obj;
            return numero == parcela.numero &&
                   Objects.equals(valor, parcela.valor) &&
                   Objects.equals(dataVencimento, parcela.dataVencimento);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(numero, valor, dataVencimento);
        }
        
        @Override
        public String toString() {
            return String.format("Parcela %d: %s (Venc: %s)", numero, valor.getFormatado(), dataVencimento);
        }
    }
}