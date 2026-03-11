package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Evento disparado quando um documento anexado ao sinistro é validado.
 *
 * <p>Este evento representa a aprovação formal de um documento após verificação:
 * <ul>
 *   <li>Autenticidade e validade do documento</li>
 *   <li>Conformidade com requisitos regulatórios</li>
 *   <li>Completude das informações</li>
 *   <li>Legibilidade e qualidade técnica</li>
 * </ul>
 *
 * <p>Este evento dispara:
 * <ul>
 *   <li>Marcação do documento como válido no checklist</li>
 *   <li>Progressão do fluxo de análise do sinistro</li>
 *   <li>Atualização de status de documentação</li>
 *   <li>Registro de auditoria da validação</li>
 *   <li>Notificação de avanço no processo (opcional)</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class DocumentoValidadoEvent extends DomainEvent {

    @JsonProperty("sinistroId")
    private final String sinistroId;

    @JsonProperty("documentoId")
    private final String documentoId;

    @JsonProperty("validadorId")
    private final String validadorId;

    @JsonProperty("timestampValidacao")
    private final String timestampValidacao;

    /**
     * Construtor principal para criação do evento.
     *
     * @param aggregateId ID único do aggregate de sinistro
     * @param sinistroId ID do sinistro ao qual o documento pertence
     * @param documentoId ID único do documento validado
     * @param validadorId ID do operador/analista que validou o documento
     * @param timestampValidacao Timestamp ISO 8601 da validação
     */
    @JsonCreator
    public DocumentoValidadoEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("sinistroId") String sinistroId,
            @JsonProperty("documentoId") String documentoId,
            @JsonProperty("validadorId") String validadorId,
            @JsonProperty("timestampValidacao") String timestampValidacao) {

        super(aggregateId, "SinistroAggregate", 1);

        this.sinistroId = validarSinistroId(sinistroId);
        this.documentoId = validarDocumentoId(documentoId);
        this.validadorId = validarValidadorId(validadorId);
        this.timestampValidacao = validarTimestampValidacao(timestampValidacao);
    }

    private String validarSinistroId(String sinistroId) {
        if (sinistroId == null || sinistroId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do sinistro não pode ser nulo ou vazio");
        }
        return sinistroId.trim();
    }

    private String validarDocumentoId(String documentoId) {
        if (documentoId == null || documentoId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do documento não pode ser nulo ou vazio");
        }
        return documentoId.trim();
    }

    private String validarValidadorId(String validadorId) {
        if (validadorId == null || validadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do validador não pode ser nulo ou vazio");
        }
        return validadorId.trim();
    }

    private String validarTimestampValidacao(String timestampValidacao) {
        if (timestampValidacao == null || timestampValidacao.trim().isEmpty()) {
            throw new IllegalArgumentException("Timestamp de validação não pode ser nulo ou vazio");
        }
        return timestampValidacao.trim();
    }

    // Getters
    public String getSinistroId() { return sinistroId; }
    public String getDocumentoId() { return documentoId; }
    public String getValidadorId() { return validadorId; }
    public String getTimestampValidacao() { return timestampValidacao; }

    @Override
    public String getEventType() {
        return "DocumentoValidadoEvent";
    }

    @Override
    public String toString() {
        return String.format("DocumentoValidadoEvent{aggregateId='%s', sinistroId='%s', " +
                           "documentoId='%s', validadorId='%s', timestampValidacao='%s', " +
                           "correlationId=%s, timestamp=%s}",
                getAggregateId(), sinistroId, documentoId, validadorId, timestampValidacao,
                getCorrelationId(), getTimestamp());
    }
}
