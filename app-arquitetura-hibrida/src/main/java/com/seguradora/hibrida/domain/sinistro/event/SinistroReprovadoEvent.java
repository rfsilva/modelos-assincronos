package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Evento disparado quando um sinistro é reprovado após análise técnica.
 *
 * <p>Este evento representa a decisão desfavorável ao segurado após análise completa,
 * incluindo:
 * <ul>
 *   <li>Motivo principal da reprovação (categoria)</li>
 *   <li>Justificativa técnica detalhada da decisão</li>
 *   <li>Fundamento legal ou contratual aplicável</li>
 *   <li>Identificação do analista responsável pela decisão</li>
 * </ul>
 *
 * <p>Este evento dispara:
 * <ul>
 *   <li>Notificação formal ao segurado sobre reprovação</li>
 *   <li>Geração de carta de negativa com fundamentação</li>
 *   <li>Abertura de prazo para recurso administrativo</li>
 *   <li>Registro em histórico de apólice</li>
 *   <li>Atualização de estatísticas de negativas</li>
 *   <li>Encerramento do processo de sinistro</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class SinistroReprovadoEvent extends DomainEvent {

    @JsonProperty("sinistroId")
    private final String sinistroId;

    @JsonProperty("motivo")
    private final String motivo;

    @JsonProperty("justificativa")
    private final String justificativa;

    @JsonProperty("analistaId")
    private final String analistaId;

    @JsonProperty("fundamentoLegal")
    private final String fundamentoLegal;

    /**
     * Construtor principal para criação do evento.
     *
     * @param aggregateId ID único do aggregate de sinistro
     * @param sinistroId ID do sinistro reprovado
     * @param motivo Motivo principal da reprovação (COBERTURA_NAO_APLICAVEL, FRAUDE_DETECTADA, etc)
     * @param justificativa Justificativa técnica detalhada da reprovação
     * @param analistaId ID do analista que reprovou o sinistro
     * @param fundamentoLegal Artigo/cláusula contratual ou base legal da reprovação
     */
    @JsonCreator
    public SinistroReprovadoEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("sinistroId") String sinistroId,
            @JsonProperty("motivo") String motivo,
            @JsonProperty("justificativa") String justificativa,
            @JsonProperty("analistaId") String analistaId,
            @JsonProperty("fundamentoLegal") String fundamentoLegal) {

        super(aggregateId, "SinistroAggregate", 1);

        this.sinistroId = validarSinistroId(sinistroId);
        this.motivo = validarMotivo(motivo);
        this.justificativa = validarJustificativa(justificativa);
        this.analistaId = validarAnalistaId(analistaId);
        this.fundamentoLegal = validarFundamentoLegal(fundamentoLegal);
    }

    private String validarSinistroId(String sinistroId) {
        if (sinistroId == null || sinistroId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do sinistro não pode ser nulo ou vazio");
        }
        return sinistroId.trim();
    }

    private String validarMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da reprovação não pode ser nulo ou vazio");
        }
        return motivo.trim();
    }

    private String validarJustificativa(String justificativa) {
        if (justificativa == null || justificativa.trim().isEmpty()) {
            throw new IllegalArgumentException("Justificativa não pode ser nula ou vazia");
        }
        return justificativa.trim();
    }

    private String validarAnalistaId(String analistaId) {
        if (analistaId == null || analistaId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do analista não pode ser nulo ou vazio");
        }
        return analistaId.trim();
    }

    private String validarFundamentoLegal(String fundamentoLegal) {
        if (fundamentoLegal == null || fundamentoLegal.trim().isEmpty()) {
            throw new IllegalArgumentException("Fundamento legal não pode ser nulo ou vazio");
        }
        return fundamentoLegal.trim();
    }

    // Getters
    public String getSinistroId() { return sinistroId; }
    public String getMotivo() { return motivo; }
    public String getJustificativa() { return justificativa; }
    public String getAnalistaId() { return analistaId; }
    public String getFundamentoLegal() { return fundamentoLegal; }

    @Override
    public String getEventType() {
        return "SinistroReprovadoEvent";
    }

    @Override
    public String toString() {
        return String.format("SinistroReprovadoEvent{aggregateId='%s', sinistroId='%s', " +
                           "motivo='%s', analistaId='%s', fundamentoLegal='%s', " +
                           "correlationId=%s, timestamp=%s}",
                getAggregateId(), sinistroId, motivo, analistaId, fundamentoLegal,
                getCorrelationId(), getTimestamp());
    }
}
