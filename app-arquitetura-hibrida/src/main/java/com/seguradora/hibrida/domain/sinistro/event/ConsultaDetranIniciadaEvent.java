package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Evento disparado quando uma consulta ao DETRAN é iniciada.
 *
 * <p>Este evento representa o início de uma consulta assíncrona aos sistemas do DETRAN
 * para validação e obtenção de informações veiculares:
 * <ul>
 *   <li>Verificação de dados cadastrais do veículo</li>
 *   <li>Confirmação de propriedade e histórico</li>
 *   <li>Validação de licenciamento e situação</li>
 *   <li>Detecção de restrições ou pendências</li>
 * </ul>
 *
 * <p>Este evento dispara:
 * <ul>
 *   <li>Chamada assíncrona ao serviço externo do DETRAN</li>
 *   <li>Registro de tentativa para controle de retry</li>
 *   <li>Timeout monitoring para garantir resposta</li>
 *   <li>Atualização de status do sinistro</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ConsultaDetranIniciadaEvent extends DomainEvent {

    @JsonProperty("sinistroId")
    private final String sinistroId;

    @JsonProperty("placa")
    private final String placa;

    @JsonProperty("renavam")
    private final String renavam;

    @JsonProperty("tentativa")
    private final Integer tentativa;

    /**
     * Construtor principal para criação do evento.
     *
     * @param aggregateId ID único do aggregate de sinistro
     * @param sinistroId ID do sinistro que solicitou a consulta
     * @param placa Placa do veículo a ser consultado
     * @param renavam Número RENAVAM do veículo
     * @param tentativa Número da tentativa de consulta (para controle de retry)
     */
    @JsonCreator
    public ConsultaDetranIniciadaEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("sinistroId") String sinistroId,
            @JsonProperty("placa") String placa,
            @JsonProperty("renavam") String renavam,
            @JsonProperty("tentativa") Integer tentativa) {

        super(aggregateId, "SinistroAggregate", 1);

        this.sinistroId = validarSinistroId(sinistroId);
        this.placa = validarPlaca(placa);
        this.renavam = validarRenavam(renavam);
        this.tentativa = validarTentativa(tentativa);
    }

    private String validarSinistroId(String sinistroId) {
        if (sinistroId == null || sinistroId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do sinistro não pode ser nulo ou vazio");
        }
        return sinistroId.trim();
    }

    private String validarPlaca(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            throw new IllegalArgumentException("Placa não pode ser nula ou vazia");
        }
        return placa.trim().toUpperCase();
    }

    private String validarRenavam(String renavam) {
        if (renavam == null || renavam.trim().isEmpty()) {
            throw new IllegalArgumentException("RENAVAM não pode ser nulo ou vazio");
        }
        return renavam.trim();
    }

    private Integer validarTentativa(Integer tentativa) {
        if (tentativa == null || tentativa < 1) {
            throw new IllegalArgumentException("Número da tentativa deve ser maior ou igual a 1");
        }
        return tentativa;
    }

    // Getters
    public String getSinistroId() { return sinistroId; }
    public String getPlaca() { return placa; }
    public String getRenavam() { return renavam; }
    public Integer getTentativa() { return tentativa; }

    @Override
    public String getEventType() {
        return "ConsultaDetranIniciadaEvent";
    }

    @Override
    public String toString() {
        return String.format("ConsultaDetranIniciadaEvent{aggregateId='%s', sinistroId='%s', " +
                           "placa='%s', renavam='%s', tentativa=%d, correlationId=%s, timestamp=%s}",
                getAggregateId(), sinistroId, placa, renavam, tentativa,
                getCorrelationId(), getTimestamp());
    }
}
