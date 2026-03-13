package com.seguradora.hibrida.domain.veiculo.relationship.dto;

import com.seguradora.hibrida.domain.veiculo.relationship.model.StatusRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.TipoRelacionamento;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para histórico de relacionamentos de um veículo.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
public class HistoricoRelacionamentoDTO {

    private String relacionamentoId;
    private String apoliceId;
    private String apoliceNumero;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private StatusRelacionamento status;
    private TipoRelacionamento tipoRelacionamento;
    private String tipoCobertura;
    private String motivoDesassociacao;
    private long duracaoDias;

    /**
     * Verifica se é o relacionamento atual (ativo).
     */
    public boolean isAtual() {
        return StatusRelacionamento.ATIVO.equals(status);
    }

    /**
     * Formata a duração em texto legível.
     */
    public String getDuracaoFormatada() {
        if (duracaoDias < 30) {
            return duracaoDias + " dias";
        } else if (duracaoDias < 365) {
            long meses = duracaoDias / 30;
            return meses + " " + (meses == 1 ? "mês" : "meses");
        } else {
            long anos = duracaoDias / 365;
            long mesesRestantes = (duracaoDias % 365) / 30;
            return anos + " " + (anos == 1 ? "ano" : "anos") +
                   (mesesRestantes > 0 ? " e " + mesesRestantes + " " + (mesesRestantes == 1 ? "mês" : "meses") : "");
        }
    }
}
