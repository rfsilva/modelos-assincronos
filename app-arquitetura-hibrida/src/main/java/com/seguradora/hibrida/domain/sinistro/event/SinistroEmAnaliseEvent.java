package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Evento disparado quando um sinistro inicia o processo de análise técnica.
 *
 * <p>Este evento representa a transição do sinistro para análise especializada, onde:
 * <ul>
 *   <li>Um analista técnico é designado para o caso</li>
 *   <li>Um prazo de análise é estabelecido (SLA)</li>
 *   <li>A prioridade de análise é definida</li>
 *   <li>O fluxo de investigação detalhada é iniciado</li>
 * </ul>
 *
 * <p>Este evento dispara:
 * <ul>
 *   <li>Atribuição do sinistro ao analista</li>
 *   <li>Criação de agenda e prazo de SLA</li>
 *   <li>Ativação de workflows de análise</li>
 *   <li>Notificação ao analista designado</li>
 *   <li>Atualização de métricas de distribuição</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class SinistroEmAnaliseEvent extends DomainEvent {

    @JsonProperty("sinistroId")
    private final String sinistroId;

    @JsonProperty("analistaId")
    private final String analistaId;

    @JsonProperty("prazoAnalise")
    private final String prazoAnalise;

    @JsonProperty("prioridadeAnalise")
    private final String prioridadeAnalise;

    /**
     * Construtor principal para criação do evento.
     *
     * @param aggregateId ID único do aggregate de sinistro
     * @param sinistroId ID do sinistro em análise
     * @param analistaId ID do analista técnico designado
     * @param prazoAnalise Data limite para conclusão da análise (formato ISO 8601)
     * @param prioridadeAnalise Nível de prioridade (BAIXA, MEDIA, ALTA, URGENTE)
     */
    @JsonCreator
    public SinistroEmAnaliseEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("sinistroId") String sinistroId,
            @JsonProperty("analistaId") String analistaId,
            @JsonProperty("prazoAnalise") String prazoAnalise,
            @JsonProperty("prioridadeAnalise") String prioridadeAnalise) {

        super(aggregateId, "SinistroAggregate", 1);

        this.sinistroId = validarSinistroId(sinistroId);
        this.analistaId = validarAnalistaId(analistaId);
        this.prazoAnalise = validarPrazoAnalise(prazoAnalise);
        this.prioridadeAnalise = validarPrioridadeAnalise(prioridadeAnalise);
    }

    private String validarSinistroId(String sinistroId) {
        if (sinistroId == null || sinistroId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do sinistro não pode ser nulo ou vazio");
        }
        return sinistroId.trim();
    }

    private String validarAnalistaId(String analistaId) {
        if (analistaId == null || analistaId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do analista não pode ser nulo ou vazio");
        }
        return analistaId.trim();
    }

    private String validarPrazoAnalise(String prazoAnalise) {
        if (prazoAnalise == null || prazoAnalise.trim().isEmpty()) {
            throw new IllegalArgumentException("Prazo de análise não pode ser nulo ou vazio");
        }
        return prazoAnalise.trim();
    }

    private String validarPrioridadeAnalise(String prioridadeAnalise) {
        if (prioridadeAnalise == null || prioridadeAnalise.trim().isEmpty()) {
            throw new IllegalArgumentException("Prioridade de análise não pode ser nula ou vazia");
        }
        return prioridadeAnalise.trim();
    }

    // Getters
    public String getSinistroId() { return sinistroId; }
    public String getAnalistaId() { return analistaId; }
    public String getPrazoAnalise() { return prazoAnalise; }
    public String getPrioridadeAnalise() { return prioridadeAnalise; }

    @Override
    public String getEventType() {
        return "SinistroEmAnaliseEvent";
    }

    @Override
    public String toString() {
        return String.format("SinistroEmAnaliseEvent{aggregateId='%s', sinistroId='%s', " +
                           "analistaId='%s', prazoAnalise='%s', prioridade='%s', " +
                           "correlationId=%s, timestamp=%s}",
                getAggregateId(), sinistroId, analistaId, prazoAnalise, prioridadeAnalise,
                getCorrelationId(), getTimestamp());
    }
}
