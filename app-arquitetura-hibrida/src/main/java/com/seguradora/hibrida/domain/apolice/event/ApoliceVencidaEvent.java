package com.seguradora.hibrida.domain.apolice.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Evento disparado quando uma apólice vence.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ApoliceVencidaEvent extends DomainEvent {

    @JsonProperty("numeroApolice")
    private final String numeroApolice;

    @JsonProperty("seguradoId")
    private final String seguradoId;

    @JsonProperty("dataVencimento")
    private final String dataVencimento;

    @JsonProperty("valorSegurado")
    private final String valorSegurado;

    @JsonCreator
    public ApoliceVencidaEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("version") long version,
            @JsonProperty("numeroApolice") String numeroApolice,
            @JsonProperty("seguradoId") String seguradoId,
            @JsonProperty("dataVencimento") String dataVencimento,
            @JsonProperty("valorSegurado") String valorSegurado) {

        super(aggregateId, "ApoliceAggregate", version);
        this.numeroApolice = numeroApolice;
        this.seguradoId = seguradoId;
        this.dataVencimento = dataVencimento;
        this.valorSegurado = valorSegurado;
    }

    public static ApoliceVencidaEvent create(
            String apoliceId,
            long version,
            String numeroApolice,
            String seguradoId,
            LocalDate dataVencimento,
            String valorSegurado) {

        return new ApoliceVencidaEvent(
                apoliceId,
                version,
                numeroApolice,
                seguradoId,
                dataVencimento.toString(),
                valorSegurado
        );
    }

    public String getNumeroApolice() {
        return numeroApolice;
    }

    public String getSeguradoId() {
        return seguradoId;
    }

    public String getDataVencimento() {
        return dataVencimento;
    }

    public String getValorSegurado() {
        return valorSegurado;
    }

    @Override
    @JsonIgnore
    public String getEventType() {
        return "ApoliceVencidaEvent";
    }

    @Override
    public String toString() {
        return String.format("ApoliceVencidaEvent{aggregateId='%s', numeroApolice='%s', dataVencimento='%s'}",
                getAggregateId(), numeroApolice, dataVencimento);
    }
}
