package com.seguradora.hibrida.domain.sinistro.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que representa a avaliação de danos de um sinistro.
 *
 * <p>Contém informações sobre:
 * <ul>
 *   <li>Tipo de dano identificado</li>
 *   <li>Valor estimado do reparo/indenização</li>
 *   <li>Laudo pericial (quando necessário)</li>
 *   <li>Fotos dos danos</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
public class AvaliacaoDanos {

    private final TipoDano tipoDano;
    private final BigDecimal valorEstimado;
    private final String laudoPericial;
    @Builder.Default
    private final List<String> fotos = new ArrayList<>();
    private final String observacoes;
    private final String periciadorId;

    /**
     * Valida se a avaliação está completa.
     */
    public boolean isCompleta() {
        return tipoDano != null &&
               valorEstimado != null &&
               valorEstimado.compareTo(BigDecimal.ZERO) > 0 &&
               (!tipoDano.requerLaudoPericial() || (laudoPericial != null && !laudoPericial.isBlank()));
    }

    /**
     * Verifica se possui laudo pericial.
     */
    public boolean possuiLaudoPericial() {
        return laudoPericial != null && !laudoPericial.isBlank();
    }

    /**
     * Verifica se possui fotos anexadas.
     */
    public boolean possuiFotos() {
        return fotos != null && !fotos.isEmpty();
    }

    /**
     * Obtém lista imutável de fotos.
     */
    public List<String> getFotos() {
        return fotos != null ? Collections.unmodifiableList(fotos) : Collections.emptyList();
    }

    /**
     * Verifica se o valor está dentro do percentual máximo para o tipo de dano.
     *
     * @param valorSegurado Valor segurado do veículo
     * @return true se está dentro do limite
     */
    public boolean isValorDentroLimite(BigDecimal valorSegurado) {
        if (valorEstimado == null || valorSegurado == null || tipoDano == null) {
            return false;
        }

        BigDecimal percentualMaximo = tipoDano.getPercentualMaximo();

        // Para tipos sem limite definido (percentual zero), não há restrição
        if (percentualMaximo.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }

        BigDecimal limiteMaximo = valorSegurado.multiply(percentualMaximo);
        return valorEstimado.compareTo(limiteMaximo) <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvaliacaoDanos that = (AvaliacaoDanos) o;
        return tipoDano == that.tipoDano &&
               Objects.equals(valorEstimado, that.valorEstimado) &&
               Objects.equals(laudoPericial, that.laudoPericial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoDano, valorEstimado, laudoPericial);
    }

    @Override
    public String toString() {
        return String.format("%s - Valor Estimado: R$ %.2f",
            tipoDano != null ? tipoDano.getDescricao() : "Tipo não definido",
            valorEstimado != null ? valorEstimado : BigDecimal.ZERO
        );
    }
}
