package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Evento disparado quando uma consulta ao DETRAN falha.
 *
 * <p>Este evento representa uma falha na tentativa de consulta ao DETRAN, que pode
 * ocorrer por diversos motivos:
 * <ul>
 *   <li>Timeout ou indisponibilidade do serviço externo</li>
 *   <li>Erro de comunicação de rede</li>
 *   <li>Dados inválidos ou não encontrados</li>
 *   <li>Limite de requisições excedido</li>
 * </ul>
 *
 * <p>Este evento dispara:
 * <ul>
 *   <li>Estratégia de retry com backoff exponencial</li>
 *   <li>Registro de erro para diagnóstico</li>
 *   <li>Notificação ao analista caso esgote tentativas</li>
 *   <li>Circuit breaker para proteção do sistema</li>
 *   <li>Métricas de falha para monitoramento</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ConsultaDetranFalhadaEvent extends DomainEvent {

    @JsonProperty("sinistroId")
    private final String sinistroId;

    @JsonProperty("erro")
    private final String erro;

    @JsonProperty("tentativa")
    private final Integer tentativa;

    @JsonProperty("proximaTentativa")
    private final String proximaTentativa;

    /**
     * Construtor principal para criação do evento.
     *
     * @param aggregateId ID único do aggregate de sinistro
     * @param sinistroId ID do sinistro que solicitou a consulta
     * @param erro Mensagem de erro ou descrição da falha
     * @param tentativa Número da tentativa que falhou
     * @param proximaTentativa Timestamp ISO 8601 da próxima tentativa agendada (null se não haverá)
     */
    @JsonCreator
    public ConsultaDetranFalhadaEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("sinistroId") String sinistroId,
            @JsonProperty("erro") String erro,
            @JsonProperty("tentativa") Integer tentativa,
            @JsonProperty("proximaTentativa") String proximaTentativa) {

        super(aggregateId, "SinistroAggregate", 1);

        this.sinistroId = validarSinistroId(sinistroId);
        this.erro = validarErro(erro);
        this.tentativa = validarTentativa(tentativa);
        this.proximaTentativa = proximaTentativa; // Pode ser null
    }

    private String validarSinistroId(String sinistroId) {
        if (sinistroId == null || sinistroId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do sinistro não pode ser nulo ou vazio");
        }
        return sinistroId.trim();
    }

    private String validarErro(String erro) {
        if (erro == null || erro.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição do erro não pode ser nula ou vazia");
        }
        return erro.trim();
    }

    private Integer validarTentativa(Integer tentativa) {
        if (tentativa == null || tentativa < 1) {
            throw new IllegalArgumentException("Número da tentativa deve ser maior ou igual a 1");
        }
        return tentativa;
    }

    // Getters
    public String getSinistroId() { return sinistroId; }
    public String getErro() { return erro; }
    public Integer getTentativa() { return tentativa; }
    public String getProximaTentativa() { return proximaTentativa; }

    @Override
    public String getEventType() {
        return "ConsultaDetranFalhadaEvent";
    }

    @Override
    public String toString() {
        return String.format("ConsultaDetranFalhadaEvent{aggregateId='%s', sinistroId='%s', " +
                           "erro='%s', tentativa=%d, proximaTentativa='%s', correlationId=%s, timestamp=%s}",
                getAggregateId(), sinistroId, erro, tentativa, proximaTentativa,
                getCorrelationId(), getTimestamp());
    }
}
