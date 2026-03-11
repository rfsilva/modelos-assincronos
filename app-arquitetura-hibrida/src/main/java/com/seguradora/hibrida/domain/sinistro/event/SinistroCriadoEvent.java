package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Evento disparado quando um novo sinistro é criado no sistema.
 *
 * <p>Este evento representa a abertura inicial de um sinistro e contém
 * todos os dados essenciais para registro e rastreamento:
 * <ul>
 *   <li>Protocolo único de identificação</li>
 *   <li>Referências ao segurado, veículo e apólice</li>
 *   <li>Tipo e descrição da ocorrência</li>
 *   <li>Identificação do operador responsável</li>
 * </ul>
 *
 * <p>Este evento dispara:
 * <ul>
 *   <li>Criação do registro de sinistro</li>
 *   <li>Notificação ao segurado</li>
 *   <li>Inicialização do fluxo de análise</li>
 *   <li>Auditoria e rastreabilidade</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class SinistroCriadoEvent extends DomainEvent {

    @JsonProperty("protocolo")
    private final String protocolo;

    @JsonProperty("seguradoId")
    private final String seguradoId;

    @JsonProperty("veiculoId")
    private final String veiculoId;

    @JsonProperty("apoliceId")
    private final String apoliceId;

    @JsonProperty("tipoSinistro")
    private final String tipoSinistro;

    @JsonProperty("ocorrencia")
    private final String ocorrencia;

    @JsonProperty("operadorId")
    private final String operadorId;

    /**
     * Construtor principal para criação do evento.
     *
     * @param aggregateId ID único do aggregate de sinistro
     * @param protocolo Número de protocolo do sinistro
     * @param seguradoId ID do segurado envolvido
     * @param veiculoId ID do veículo sinistrado
     * @param apoliceId ID da apólice relacionada
     * @param tipoSinistro Tipo de sinistro (COLISAO, ROUBO, etc)
     * @param ocorrencia Descrição detalhada da ocorrência
     * @param operadorId ID do operador que registrou o sinistro
     */
    @JsonCreator
    public SinistroCriadoEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("protocolo") String protocolo,
            @JsonProperty("seguradoId") String seguradoId,
            @JsonProperty("veiculoId") String veiculoId,
            @JsonProperty("apoliceId") String apoliceId,
            @JsonProperty("tipoSinistro") String tipoSinistro,
            @JsonProperty("ocorrencia") String ocorrencia,
            @JsonProperty("operadorId") String operadorId) {

        super(aggregateId, "SinistroAggregate", 1);

        this.protocolo = validarProtocolo(protocolo);
        this.seguradoId = validarSeguradoId(seguradoId);
        this.veiculoId = validarVeiculoId(veiculoId);
        this.apoliceId = validarApoliceId(apoliceId);
        this.tipoSinistro = validarTipoSinistro(tipoSinistro);
        this.ocorrencia = validarOcorrencia(ocorrencia);
        this.operadorId = validarOperadorId(operadorId);
    }

    private String validarProtocolo(String protocolo) {
        if (protocolo == null || protocolo.trim().isEmpty()) {
            throw new IllegalArgumentException("Protocolo não pode ser nulo ou vazio");
        }
        return protocolo.trim();
    }

    private String validarSeguradoId(String seguradoId) {
        if (seguradoId == null || seguradoId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do segurado não pode ser nulo ou vazio");
        }
        return seguradoId.trim();
    }

    private String validarVeiculoId(String veiculoId) {
        if (veiculoId == null || veiculoId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do veículo não pode ser nulo ou vazio");
        }
        return veiculoId.trim();
    }

    private String validarApoliceId(String apoliceId) {
        if (apoliceId == null || apoliceId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da apólice não pode ser nulo ou vazio");
        }
        return apoliceId.trim();
    }

    private String validarTipoSinistro(String tipoSinistro) {
        if (tipoSinistro == null || tipoSinistro.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de sinistro não pode ser nulo ou vazio");
        }
        return tipoSinistro.trim();
    }

    private String validarOcorrencia(String ocorrencia) {
        if (ocorrencia == null || ocorrencia.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição da ocorrência não pode ser nula ou vazia");
        }
        return ocorrencia.trim();
    }

    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }

    // Getters
    public String getProtocolo() { return protocolo; }
    public String getSeguradoId() { return seguradoId; }
    public String getVeiculoId() { return veiculoId; }
    public String getApoliceId() { return apoliceId; }
    public String getTipoSinistro() { return tipoSinistro; }
    public String getOcorrencia() { return ocorrencia; }
    public String getOperadorId() { return operadorId; }

    @Override
    public String getEventType() {
        return "SinistroCriadoEvent";
    }

    @Override
    public String toString() {
        return String.format("SinistroCriadoEvent{aggregateId='%s', protocolo='%s', seguradoId='%s', " +
                           "veiculoId='%s', apoliceId='%s', tipoSinistro='%s', operadorId='%s', " +
                           "correlationId=%s, timestamp=%s}",
                getAggregateId(), protocolo, seguradoId, veiculoId, apoliceId, tipoSinistro,
                operadorId, getCorrelationId(), getTimestamp());
    }
}
