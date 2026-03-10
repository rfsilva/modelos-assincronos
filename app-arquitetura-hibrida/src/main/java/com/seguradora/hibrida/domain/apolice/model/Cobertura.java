package com.seguradora.hibrida.domain.apolice.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object que representa uma cobertura de apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class Cobertura implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final TipoCobertura tipo;
    private final Valor valorCobertura;
    private final Valor franquia;
    private final int carenciaDias;
    private final boolean ativa;
    
    private Cobertura(TipoCobertura tipo, Valor valorCobertura, Valor franquia, int carenciaDias, boolean ativa) {
        this.tipo = tipo;
        this.valorCobertura = valorCobertura;
        this.franquia = franquia;
        this.carenciaDias = carenciaDias;
        this.ativa = ativa;
    }
    
    /**
     * Cria uma nova cobertura.
     */
    public static Cobertura of(TipoCobertura tipo, Valor valorCobertura, Valor franquia, int carenciaDias) {
        validarParametros(tipo, valorCobertura, franquia, carenciaDias);
        return new Cobertura(tipo, valorCobertura, franquia, carenciaDias, true);
    }
    
    /**
     * Cria uma cobertura sem carência.
     */
    public static Cobertura semCarencia(TipoCobertura tipo, Valor valorCobertura, Valor franquia) {
        return of(tipo, valorCobertura, franquia, 0);
    }
    
    /**
     * Cria uma cobertura básica com valores padrão.
     */
    public static Cobertura basica(TipoCobertura tipo, Valor valorCobertura) {
        Valor franquiaPadrao = valorCobertura.porcentagem(5.0); // 5% do valor como franquia
        int carenciaPadrao = tipo == TipoCobertura.ROUBO_FURTO ? 30 : 0; // 30 dias para roubo/furto
        
        return of(tipo, valorCobertura, franquiaPadrao, carenciaPadrao);
    }
    
    private static void validarParametros(TipoCobertura tipo, Valor valorCobertura, Valor franquia, int carenciaDias) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de cobertura não pode ser nulo");
        }
        
        if (valorCobertura == null) {
            throw new IllegalArgumentException("Valor da cobertura não pode ser nulo");
        }
        
        if (!valorCobertura.isPositivo()) {
            throw new IllegalArgumentException("Valor da cobertura deve ser positivo");
        }
        
        if (franquia == null) {
            throw new IllegalArgumentException("Franquia não pode ser nula");
        }
        
        if (franquia.ehMaiorQue(valorCobertura)) {
            throw new IllegalArgumentException("Franquia não pode ser maior que o valor da cobertura");
        }
        
        if (carenciaDias < 0 || carenciaDias > 365) {
            throw new IllegalArgumentException("Carência deve estar entre 0 e 365 dias");
        }
    }
    
    /**
     * Retorna o tipo da cobertura.
     */
    public TipoCobertura getTipo() {
        return tipo;
    }
    
    /**
     * Retorna o valor da cobertura.
     */
    public Valor getValorCobertura() {
        return valorCobertura;
    }
    
    /**
     * Retorna a franquia.
     */
    public Valor getFranquia() {
        return franquia;
    }
    
    /**
     * Retorna a carência em dias.
     */
    public int getCarenciaDias() {
        return carenciaDias;
    }
    
    /**
     * Verifica se a cobertura está ativa.
     */
    public boolean isAtiva() {
        return ativa;
    }
    
    /**
     * Verifica se tem carência.
     */
    public boolean temCarencia() {
        return carenciaDias > 0;
    }
    
    /**
     * Verifica se a carência já foi cumprida para uma data específica.
     */
    public boolean carenciaCumpridaEm(LocalDate dataInicioVigencia, LocalDate dataVerificacao) {
        if (!temCarencia()) {
            return true;
        }
        
        if (dataInicioVigencia == null || dataVerificacao == null) {
            return false;
        }
        
        LocalDate fimCarencia = dataInicioVigencia.plusDays(carenciaDias);
        return !dataVerificacao.isBefore(fimCarencia);
    }
    
    /**
     * Calcula o valor líquido da indenização (valor - franquia).
     */
    public Valor calcularIndenizacaoLiquida(Valor valorSinistro) {
        if (valorSinistro == null) {
            throw new IllegalArgumentException("Valor do sinistro não pode ser nulo");
        }
        
        if (!ativa) {
            return Valor.zero();
        }
        
        // Limitar ao valor da cobertura
        Valor valorLimitado = valorSinistro.ehMaiorQue(valorCobertura) ? valorCobertura : valorSinistro;
        
        // Subtrair franquia
        if (franquia.ehMaiorOuIgualA(valorLimitado)) {
            return Valor.zero();
        }
        
        return valorLimitado.subtrair(franquia);
    }
    
    /**
     * Calcula o prêmio desta cobertura baseado no valor segurado.
     */
    public Valor calcularPremio(Valor valorSegurado) {
        if (valorSegurado == null) {
            throw new IllegalArgumentException("Valor segurado não pode ser nulo");
        }
        
        if (!ativa) {
            return Valor.zero();
        }
        
        return valorSegurado.multiplicar(tipo.getFatorPremio());
    }
    
    /**
     * Desativa esta cobertura.
     */
    public Cobertura desativar() {
        return new Cobertura(tipo, valorCobertura, franquia, carenciaDias, false);
    }
    
    /**
     * Ativa esta cobertura.
     */
    public Cobertura ativar() {
        return new Cobertura(tipo, valorCobertura, franquia, carenciaDias, true);
    }
    
    /**
     * Atualiza o valor da cobertura.
     */
    public Cobertura atualizarValor(Valor novoValor) {
        if (novoValor == null) {
            throw new IllegalArgumentException("Novo valor não pode ser nulo");
        }
        
        // Ajustar franquia proporcionalmente se necessário
        Valor novaFranquia = franquia;
        if (franquia.ehMaiorQue(novoValor)) {
            novaFranquia = novoValor.porcentagem(5.0); // 5% do novo valor
        }
        
        return new Cobertura(tipo, novoValor, novaFranquia, carenciaDias, ativa);
    }
    
    /**
     * Atualiza a franquia.
     */
    public Cobertura atualizarFranquia(Valor novaFranquia) {
        validarParametros(tipo, valorCobertura, novaFranquia, carenciaDias);
        return new Cobertura(tipo, valorCobertura, novaFranquia, carenciaDias, ativa);
    }
    
    /**
     * Retorna uma descrição resumida da cobertura.
     */
    public String getResumo() {
        return String.format("%s - %s (Franquia: %s)", 
            tipo.getDescricao(), 
            valorCobertura.getFormatado(), 
            franquia.getFormatado()
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cobertura cobertura = (Cobertura) obj;
        return carenciaDias == cobertura.carenciaDias &&
               ativa == cobertura.ativa &&
               tipo == cobertura.tipo &&
               Objects.equals(valorCobertura, cobertura.valorCobertura) &&
               Objects.equals(franquia, cobertura.franquia);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tipo, valorCobertura, franquia, carenciaDias, ativa);
    }
    
    @Override
    public String toString() {
        return getResumo();
    }
}