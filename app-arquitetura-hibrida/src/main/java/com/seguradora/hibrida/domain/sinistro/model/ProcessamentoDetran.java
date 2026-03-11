package com.seguradora.hibrida.domain.sinistro.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Entidade que representa o processamento de consulta ao Detran.
 *
 * <p>Gerencia o ciclo de vida da consulta assíncrona:
 * <ul>
 *   <li>Status da consulta</li>
 *   <li>Dados retornados pelo Detran</li>
 *   <li>Controle de tentativas e retry</li>
 *   <li>Timestamps de execução</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
public class ProcessamentoDetran {

    private final DetranConsultaStatus status;
    @Builder.Default
    private final Map<String, Object> dadosRetornados = new HashMap<>();
    private final int tentativas;
    private final Instant dataInicio;
    private final Instant dataFim;
    private final String mensagemErro;
    private final String placa;
    private final String renavam;

    /**
     * Verifica se a consulta foi bem-sucedida.
     */
    public boolean isSucesso() {
        return status == DetranConsultaStatus.CONCLUIDA &&
               dadosRetornados != null &&
               !dadosRetornados.isEmpty();
    }

    /**
     * Verifica se deve realizar retry automático.
     */
    public boolean deveRetry() {
        if (status == null) {
            return false;
        }

        return status.deveRetry() && tentativas < 3; // Máximo 3 tentativas
    }

    /**
     * Obtém o tempo de processamento em milissegundos.
     */
    public long getTempoProcessamentoMs() {
        if (dataInicio == null || dataFim == null) {
            return 0;
        }

        return dataFim.toEpochMilli() - dataInicio.toEpochMilli();
    }

    /**
     * Verifica se excedeu o timeout (30 segundos).
     */
    public boolean excedeuTimeout() {
        return status == DetranConsultaStatus.TIMEOUT ||
               getTempoProcessamentoMs() > 30000;
    }

    /**
     * Obtém um dado específico retornado pelo Detran.
     */
    @SuppressWarnings("unchecked")
    public <T> T getDado(String chave, Class<T> tipo) {
        if (dadosRetornados == null || !dadosRetornados.containsKey(chave)) {
            return null;
        }

        Object valor = dadosRetornados.get(chave);
        if (tipo.isInstance(valor)) {
            return (T) valor;
        }

        return null;
    }

    /**
     * Verifica se possui informações de restrição.
     */
    public boolean possuiRestricao() {
        Boolean restricao = getDado("possui_restricao", Boolean.class);
        return Boolean.TRUE.equals(restricao);
    }

    /**
     * Verifica se o veículo está regular.
     */
    public boolean isVeiculoRegular() {
        if (!isSucesso()) {
            return false;
        }

        Boolean restricao = getDado("possui_restricao", Boolean.class);
        Boolean debito = getDado("possui_debito", Boolean.class);

        return Boolean.FALSE.equals(restricao) && Boolean.FALSE.equals(debito);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessamentoDetran that = (ProcessamentoDetran) o;
        return tentativas == that.tentativas &&
               status == that.status &&
               Objects.equals(placa, that.placa) &&
               Objects.equals(renavam, that.renavam) &&
               Objects.equals(dataInicio, that.dataInicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, tentativas, placa, renavam, dataInicio);
    }

    @Override
    public String toString() {
        return String.format("Consulta Detran [%s] - Status: %s, Tentativas: %d",
            placa,
            status != null ? status.getDescricao() : "Desconhecido",
            tentativas
        );
    }
}
