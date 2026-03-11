package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Evento disparado quando um sinistro é aprovado após análise técnica.
 *
 * <p>Este evento representa a decisão favorável ao segurado após análise completa,
 * incluindo:
 * <ul>
 *   <li>Valor de indenização calculado e aprovado</li>
 *   <li>Justificativa técnica da decisão</li>
 *   <li>Documentos comprobatórios que fundamentaram a aprovação</li>
 *   <li>Identificação do analista responsável pela decisão</li>
 * </ul>
 *
 * <p>Este evento dispara:
 * <ul>
 *   <li>Início do processo de pagamento de indenização</li>
 *   <li>Notificação ao segurado sobre aprovação</li>
 *   <li>Geração de documentos contratuais</li>
 *   <li>Atualização de estatísticas de sinistralidade</li>
 *   <li>Registro em histórico de apólice</li>
 *   <li>Atualização de índices de risco</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class SinistroAprovadoEvent extends DomainEvent {

    @JsonProperty("sinistroId")
    private final String sinistroId;

    @JsonProperty("valorIndenizacao")
    private final String valorIndenizacao;

    @JsonProperty("justificativa")
    private final String justificativa;

    @JsonProperty("analistaId")
    private final String analistaId;

    @JsonProperty("documentosComprobatorios")
    private final List<String> documentosComprobatorios;

    /**
     * Construtor principal para criação do evento.
     *
     * @param aggregateId ID único do aggregate de sinistro
     * @param sinistroId ID do sinistro aprovado
     * @param valorIndenizacao Valor da indenização aprovada (formato decimal como string)
     * @param justificativa Justificativa técnica da aprovação
     * @param analistaId ID do analista que aprovou o sinistro
     * @param documentosComprobatorios IDs dos documentos que fundamentaram a aprovação
     */
    @JsonCreator
    public SinistroAprovadoEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("sinistroId") String sinistroId,
            @JsonProperty("valorIndenizacao") String valorIndenizacao,
            @JsonProperty("justificativa") String justificativa,
            @JsonProperty("analistaId") String analistaId,
            @JsonProperty("documentosComprobatorios") List<String> documentosComprobatorios) {

        super(aggregateId, "SinistroAggregate", 1);

        this.sinistroId = validarSinistroId(sinistroId);
        this.valorIndenizacao = validarValorIndenizacao(valorIndenizacao);
        this.justificativa = validarJustificativa(justificativa);
        this.analistaId = validarAnalistaId(analistaId);
        this.documentosComprobatorios = validarDocumentosComprobatorios(documentosComprobatorios);
    }

    private String validarSinistroId(String sinistroId) {
        if (sinistroId == null || sinistroId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do sinistro não pode ser nulo ou vazio");
        }
        return sinistroId.trim();
    }

    private String validarValorIndenizacao(String valorIndenizacao) {
        if (valorIndenizacao == null || valorIndenizacao.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor da indenização não pode ser nulo ou vazio");
        }
        return valorIndenizacao.trim();
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

    private List<String> validarDocumentosComprobatorios(List<String> documentosComprobatorios) {
        if (documentosComprobatorios == null) {
            throw new IllegalArgumentException("Lista de documentos comprobatórios não pode ser nula");
        }
        return documentosComprobatorios;
    }

    // Getters
    public String getSinistroId() { return sinistroId; }
    public String getValorIndenizacao() { return valorIndenizacao; }
    public String getJustificativa() { return justificativa; }
    public String getAnalistaId() { return analistaId; }
    public List<String> getDocumentosComprobatorios() { return documentosComprobatorios; }

    @Override
    public String getEventType() {
        return "SinistroAprovadoEvent";
    }

    @Override
    public String toString() {
        return String.format("SinistroAprovadoEvent{aggregateId='%s', sinistroId='%s', " +
                           "valorIndenizacao='%s', analistaId='%s', documentosCount=%d, " +
                           "correlationId=%s, timestamp=%s}",
                getAggregateId(), sinistroId, valorIndenizacao, analistaId,
                documentosComprobatorios.size(), getCorrelationId(), getTimestamp());
    }
}
