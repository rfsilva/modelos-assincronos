package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Evento disparado quando uma consulta ao DETRAN é concluída com sucesso.
 *
 * <p>Este evento representa a conclusão bem-sucedida de uma consulta assíncrona
 * ao DETRAN, contendo:
 * <ul>
 *   <li>Dados cadastrais completos do veículo</li>
 *   <li>Histórico de propriedade e transferências</li>
 *   <li>Situação de licenciamento e débitos</li>
 *   <li>Restrições administrativas ou judiciais</li>
 * </ul>
 *
 * <p>Este evento dispara:
 * <ul>
 *   <li>Armazenamento dos dados do DETRAN no sinistro</li>
 *   <li>Validação automática de consistência</li>
 *   <li>Continuação do fluxo de análise</li>
 *   <li>Atualização de cache de consultas</li>
 *   <li>Registro de sucesso para métricas</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ConsultaDetranConcluidaEvent extends DomainEvent {

    @JsonProperty("sinistroId")
    private final String sinistroId;

    @JsonProperty("dadosDetran")
    private final Map<String, Object> dadosDetran;

    @JsonProperty("timestampConsulta")
    private final String timestampConsulta;

    /**
     * Construtor principal para criação do evento.
     *
     * @param aggregateId ID único do aggregate de sinistro
     * @param sinistroId ID do sinistro que solicitou a consulta
     * @param dadosDetran Mapa com todos os dados retornados pelo DETRAN
     * @param timestampConsulta Timestamp ISO 8601 da conclusão da consulta
     */
    @JsonCreator
    public ConsultaDetranConcluidaEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("sinistroId") String sinistroId,
            @JsonProperty("dadosDetran") Map<String, Object> dadosDetran,
            @JsonProperty("timestampConsulta") String timestampConsulta) {

        super(aggregateId, "SinistroAggregate", 1);

        this.sinistroId = validarSinistroId(sinistroId);
        this.dadosDetran = validarDadosDetran(dadosDetran);
        this.timestampConsulta = validarTimestampConsulta(timestampConsulta);
    }

    private String validarSinistroId(String sinistroId) {
        if (sinistroId == null || sinistroId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do sinistro não pode ser nulo ou vazio");
        }
        return sinistroId.trim();
    }

    private Map<String, Object> validarDadosDetran(Map<String, Object> dadosDetran) {
        if (dadosDetran == null || dadosDetran.isEmpty()) {
            throw new IllegalArgumentException("Dados do DETRAN não podem ser nulos ou vazios");
        }
        return dadosDetran;
    }

    private String validarTimestampConsulta(String timestampConsulta) {
        if (timestampConsulta == null || timestampConsulta.trim().isEmpty()) {
            throw new IllegalArgumentException("Timestamp da consulta não pode ser nulo ou vazio");
        }
        return timestampConsulta.trim();
    }

    // Getters
    public String getSinistroId() { return sinistroId; }
    public Map<String, Object> getDadosDetran() { return dadosDetran; }
    public String getTimestampConsulta() { return timestampConsulta; }

    @Override
    public String getEventType() {
        return "ConsultaDetranConcluidaEvent";
    }

    @Override
    public String toString() {
        return String.format("ConsultaDetranConcluidaEvent{aggregateId='%s', sinistroId='%s', " +
                           "dadosCount=%d, timestampConsulta='%s', correlationId=%s, timestamp=%s}",
                getAggregateId(), sinistroId, dadosDetran.size(), timestampConsulta,
                getCorrelationId(), getTimestamp());
    }
}
