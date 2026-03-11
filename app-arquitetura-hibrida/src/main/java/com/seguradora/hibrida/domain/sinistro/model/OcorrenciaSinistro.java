package com.seguradora.hibrida.domain.sinistro.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Entidade que representa a ocorrência de um sinistro.
 *
 * <p>Contém informações sobre como e onde o sinistro ocorreu:
 * <ul>
 *   <li>Data e hora da ocorrência</li>
 *   <li>Local detalhado</li>
 *   <li>Descrição do acontecido</li>
 *   <li>Número do boletim de ocorrência (quando aplicável)</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
public class OcorrenciaSinistro {

    private final Instant dataOcorrencia;
    private final LocalOcorrencia localOcorrencia;
    private final String descricao;
    private final String boletimOcorrencia;
    private final String circunstancias;

    /**
     * Valida se a ocorrência possui informações mínimas necessárias.
     */
    public boolean isValida() {
        return dataOcorrencia != null &&
               localOcorrencia != null &&
               localOcorrencia.isCompleto() &&
               descricao != null &&
               !descricao.isBlank() &&
               descricao.length() >= 20; // Mínimo 20 caracteres
    }

    /**
     * Verifica se possui boletim de ocorrência.
     */
    public boolean possuiBoletimOcorrencia() {
        return boletimOcorrencia != null && !boletimOcorrencia.isBlank();
    }

    /**
     * Verifica se a ocorrência é recente (últimas 72 horas).
     */
    public boolean isRecente() {
        if (dataOcorrencia == null) {
            return false;
        }

        Instant limite = Instant.now().minusSeconds(72 * 3600); // 72 horas
        return dataOcorrencia.isAfter(limite);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OcorrenciaSinistro that = (OcorrenciaSinistro) o;
        return Objects.equals(dataOcorrencia, that.dataOcorrencia) &&
               Objects.equals(localOcorrencia, that.localOcorrencia) &&
               Objects.equals(boletimOcorrencia, that.boletimOcorrencia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataOcorrencia, localOcorrencia, boletimOcorrencia);
    }

    @Override
    public String toString() {
        return String.format("Ocorrência em %s - %s",
            localOcorrencia != null ? localOcorrencia.getCidade() : "Local não especificado",
            dataOcorrencia
        );
    }
}
