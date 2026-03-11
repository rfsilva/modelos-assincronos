package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Evento disparado quando um documento é anexado a um sinistro.
 *
 * <p>Este evento representa a inclusão de documentação comprobatória no processo
 * de sinistro, podendo ser:
 * <ul>
 *   <li>Boletim de ocorrência policial</li>
 *   <li>Fotos do veículo e local do sinistro</li>
 *   <li>Laudos técnicos e perícias</li>
 *   <li>Orçamentos de reparo</li>
 *   <li>Documentos pessoais e do veículo</li>
 * </ul>
 *
 * <p>Este evento dispara:
 * <ul>
 *   <li>Armazenamento seguro do documento</li>
 *   <li>Geração de thumbnail ou preview quando aplicável</li>
 *   <li>Validação automática de tipo e tamanho</li>
 *   <li>Atualização de checklist documental</li>
 *   <li>Notificação ao analista sobre novo documento</li>
 *   <li>Registro de auditoria</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class DocumentoAnexadoEvent extends DomainEvent {

    @JsonProperty("sinistroId")
    private final String sinistroId;

    @JsonProperty("documentoId")
    private final String documentoId;

    @JsonProperty("tipoDocumento")
    private final String tipoDocumento;

    @JsonProperty("operadorId")
    private final String operadorId;

    @JsonProperty("observacoes")
    private final String observacoes;

    /**
     * Construtor principal para criação do evento.
     *
     * @param aggregateId ID único do aggregate de sinistro
     * @param sinistroId ID do sinistro ao qual o documento foi anexado
     * @param documentoId ID único do documento anexado
     * @param tipoDocumento Tipo/categoria do documento (BO, FOTO, LAUDO, etc)
     * @param operadorId ID do operador que anexou o documento
     * @param observacoes Observações ou comentários sobre o documento (opcional)
     */
    @JsonCreator
    public DocumentoAnexadoEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("sinistroId") String sinistroId,
            @JsonProperty("documentoId") String documentoId,
            @JsonProperty("tipoDocumento") String tipoDocumento,
            @JsonProperty("operadorId") String operadorId,
            @JsonProperty("observacoes") String observacoes) {

        super(aggregateId, "SinistroAggregate", 1);

        this.sinistroId = validarSinistroId(sinistroId);
        this.documentoId = validarDocumentoId(documentoId);
        this.tipoDocumento = validarTipoDocumento(tipoDocumento);
        this.operadorId = validarOperadorId(operadorId);
        this.observacoes = observacoes; // Pode ser null ou vazio
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

    private String validarTipoDocumento(String tipoDocumento) {
        if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo do documento não pode ser nulo ou vazio");
        }
        return tipoDocumento.trim();
    }

    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }

    // Getters
    public String getSinistroId() { return sinistroId; }
    public String getDocumentoId() { return documentoId; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getOperadorId() { return operadorId; }
    public String getObservacoes() { return observacoes; }

    @Override
    public String getEventType() {
        return "DocumentoAnexadoEvent";
    }

    @Override
    public String toString() {
        return String.format("DocumentoAnexadoEvent{aggregateId='%s', sinistroId='%s', " +
                           "documentoId='%s', tipoDocumento='%s', operadorId='%s', " +
                           "correlationId=%s, timestamp=%s}",
                getAggregateId(), sinistroId, documentoId, tipoDocumento, operadorId,
                getCorrelationId(), getTimestamp());
    }
}
