package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Evento disparado quando um documento anexado ao sinistro é rejeitado.
 *
 * <p>Este evento representa a reprovação de um documento após verificação técnica,
 * que pode ocorrer por diversos motivos:
 * <ul>
 *   <li>Documento ilegível ou de baixa qualidade</li>
 *   <li>Informações incompletas ou inconsistentes</li>
 *   <li>Documento vencido ou fora do prazo de validade</li>
 *   <li>Suspeita de adulteração ou falsificação</li>
 *   <li>Tipo de documento não adequado ao solicitado</li>
 * </ul>
 *
 * <p>Este evento dispara:
 * <ul>
 *   <li>Notificação ao segurado sobre necessidade de reenvio</li>
 *   <li>Marcação do documento como rejeitado no checklist</li>
 *   <li>Solicitação de novo documento ou correção</li>
 *   <li>Bloqueio temporário do avanço do sinistro</li>
 *   <li>Registro de auditoria da rejeição</li>
 *   <li>Atualização de métricas de qualidade documental</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class DocumentoRejeitadoEvent extends DomainEvent {

    @JsonProperty("sinistroId")
    private final String sinistroId;

    @JsonProperty("documentoId")
    private final String documentoId;

    @JsonProperty("motivo")
    private final String motivo;

    @JsonProperty("validadorId")
    private final String validadorId;

    /**
     * Construtor principal para criação do evento.
     *
     * @param aggregateId ID único do aggregate de sinistro
     * @param sinistroId ID do sinistro ao qual o documento pertence
     * @param documentoId ID único do documento rejeitado
     * @param motivo Motivo da rejeição do documento
     * @param validadorId ID do operador/analista que rejeitou o documento
     */
    @JsonCreator
    public DocumentoRejeitadoEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("sinistroId") String sinistroId,
            @JsonProperty("documentoId") String documentoId,
            @JsonProperty("motivo") String motivo,
            @JsonProperty("validadorId") String validadorId) {

        super(aggregateId, "SinistroAggregate", 1);

        this.sinistroId = validarSinistroId(sinistroId);
        this.documentoId = validarDocumentoId(documentoId);
        this.motivo = validarMotivo(motivo);
        this.validadorId = validarValidadorId(validadorId);
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

    private String validarMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da rejeição não pode ser nulo ou vazio");
        }
        return motivo.trim();
    }

    private String validarValidadorId(String validadorId) {
        if (validadorId == null || validadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do validador não pode ser nulo ou vazio");
        }
        return validadorId.trim();
    }

    // Getters
    public String getSinistroId() { return sinistroId; }
    public String getDocumentoId() { return documentoId; }
    public String getMotivo() { return motivo; }
    public String getValidadorId() { return validadorId; }

    @Override
    public String getEventType() {
        return "DocumentoRejeitadoEvent";
    }

    @Override
    public String toString() {
        return String.format("DocumentoRejeitadoEvent{aggregateId='%s', sinistroId='%s', " +
                           "documentoId='%s', motivo='%s', validadorId='%s', " +
                           "correlationId=%s, timestamp=%s}",
                getAggregateId(), sinistroId, documentoId, motivo, validadorId,
                getCorrelationId(), getTimestamp());
    }
}
