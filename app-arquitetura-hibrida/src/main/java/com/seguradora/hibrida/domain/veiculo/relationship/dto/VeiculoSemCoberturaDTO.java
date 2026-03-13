package com.seguradora.hibrida.domain.veiculo.relationship.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para representar veículos sem cobertura ativa.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
public class VeiculoSemCoberturaDTO {

    private String veiculoId;
    private String placa;
    private String seguradoCpf;
    private String seguradoNome;
    private String ultimaApolice;
    private LocalDate dataFimUltimaCobertura;
    private int diasSemCobertura;

    /**
     * Verifica se a situação é crítica (mais de 30 dias sem cobertura).
     */
    public boolean isSituacaoCritica() {
        return diasSemCobertura > 30;
    }

    /**
     * Verifica se requer ação urgente (mais de 7 dias sem cobertura).
     */
    public boolean requerAcaoUrgente() {
        return diasSemCobertura > 7;
    }
}
