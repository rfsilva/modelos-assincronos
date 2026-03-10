package com.seguradora.hibrida.domain.apolice.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value Object que representa o período de vigência de uma apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class Vigencia implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final LocalDate inicio;
    private final LocalDate fim;
    
    private Vigencia(LocalDate inicio, LocalDate fim) {
        this.inicio = inicio;
        this.fim = fim;
    }
    
    /**
     * Cria uma vigência com datas específicas.
     */
    public static Vigencia of(LocalDate inicio, LocalDate fim) {
        validarDatas(inicio, fim);
        return new Vigencia(inicio, fim);
    }
    
    /**
     * Cria uma vigência de 1 ano a partir da data de início.
     */
    public static Vigencia anual(LocalDate inicio) {
        if (inicio == null) {
            throw new IllegalArgumentException("Data de início não pode ser nula");
        }
        
        LocalDate fim = inicio.plusYears(1).minusDays(1);
        return new Vigencia(inicio, fim);
    }
    
    /**
     * Cria uma vigência de 1 ano a partir de hoje.
     */
    public static Vigencia anualAPartirDeHoje() {
        return anual(LocalDate.now());
    }
    
    /**
     * Cria uma vigência com duração específica em meses.
     */
    public static Vigencia comDuracaoMeses(LocalDate inicio, int meses) {
        if (inicio == null) {
            throw new IllegalArgumentException("Data de início não pode ser nula");
        }
        
        if (meses < 1 || meses > 60) {
            throw new IllegalArgumentException("Duração deve estar entre 1 e 60 meses");
        }
        
        LocalDate fim = inicio.plusMonths(meses).minusDays(1);
        return new Vigencia(inicio, fim);
    }
    
    private static void validarDatas(LocalDate inicio, LocalDate fim) {
        if (inicio == null) {
            throw new IllegalArgumentException("Data de início não pode ser nula");
        }
        
        if (fim == null) {
            throw new IllegalArgumentException("Data de fim não pode ser nula");
        }
        
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("Data de fim não pode ser anterior à data de início");
        }
        
        if (inicio.isBefore(LocalDate.now().minusDays(30))) {
            throw new IllegalArgumentException("Data de início não pode ser muito anterior à data atual");
        }
        
        long diasVigencia = ChronoUnit.DAYS.between(inicio, fim) + 1;
        if (diasVigencia < 30) {
            throw new IllegalArgumentException("Vigência mínima é de 30 dias");
        }
        
        if (diasVigencia > 1825) { // 5 anos
            throw new IllegalArgumentException("Vigência máxima é de 5 anos");
        }
    }
    
    /**
     * Retorna a data de início da vigência.
     */
    public LocalDate getInicio() {
        return inicio;
    }
    
    /**
     * Retorna a data de fim da vigência.
     */
    public LocalDate getFim() {
        return fim;
    }
    
    /**
     * Verifica se a vigência está ativa em uma data específica.
     */
    public boolean estaVigenteEm(LocalDate data) {
        if (data == null) {
            return false;
        }
        
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }
    
    /**
     * Verifica se a vigência está ativa hoje.
     */
    public boolean estaVigenteHoje() {
        return estaVigenteEm(LocalDate.now());
    }
    
    /**
     * Verifica se a vigência já expirou.
     */
    public boolean jaExpirou() {
        return LocalDate.now().isAfter(fim);
    }
    
    /**
     * Verifica se a vigência ainda não começou.
     */
    public boolean aindaNaoComecou() {
        return LocalDate.now().isBefore(inicio);
    }
    
    /**
     * Retorna o número de dias da vigência.
     */
    public long getDiasVigencia() {
        return ChronoUnit.DAYS.between(inicio, fim) + 1;
    }
    
    /**
     * Retorna o número de dias restantes da vigência.
     */
    public long getDiasRestantes() {
        LocalDate hoje = LocalDate.now();
        
        if (hoje.isAfter(fim)) {
            return 0;
        }
        
        if (hoje.isBefore(inicio)) {
            return ChronoUnit.DAYS.between(hoje, fim) + 1;
        }
        
        return ChronoUnit.DAYS.between(hoje, fim) + 1;
    }
    
    /**
     * Retorna o número de dias decorridos desde o início da vigência.
     */
    public long getDiasDecorridos() {
        LocalDate hoje = LocalDate.now();
        
        if (hoje.isBefore(inicio)) {
            return 0;
        }
        
        if (hoje.isAfter(fim)) {
            return ChronoUnit.DAYS.between(inicio, fim) + 1;
        }
        
        return ChronoUnit.DAYS.between(inicio, hoje) + 1;
    }
    
    /**
     * Retorna o período da vigência.
     */
    public Period getPeriodo() {
        return Period.between(inicio, fim);
    }
    
    /**
     * Verifica se a vigência vence nos próximos N dias.
     */
    public boolean venceEm(int dias) {
        LocalDate dataLimite = LocalDate.now().plusDays(dias);
        return fim.isBefore(dataLimite) || fim.isEqual(dataLimite);
    }
    
    /**
     * Cria uma nova vigência renovada a partir do fim desta.
     */
    public Vigencia renovar() {
        return anual(fim.plusDays(1));
    }
    
    /**
     * Cria uma nova vigência renovada com duração específica.
     */
    public Vigencia renovar(int meses) {
        return comDuracaoMeses(fim.plusDays(1), meses);
    }
    
    /**
     * Retorna uma representação formatada da vigência.
     */
    public String getFormatado() {
        return String.format("%s a %s", inicio.toString(), fim.toString());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vigencia vigencia = (Vigencia) obj;
        return Objects.equals(inicio, vigencia.inicio) && Objects.equals(fim, vigencia.fim);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(inicio, fim);
    }
    
    @Override
    public String toString() {
        return getFormatado();
    }
}