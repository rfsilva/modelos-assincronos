package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Evento disparado quando um sinistro passa pela validação inicial.
 *
 * <p>Este evento representa a conclusão da etapa de validação, onde:
 * <ul>
 *   <li>Dados complementares são coletados e validados</li>
 *   <li>Documentos obrigatórios são anexados e verificados</li>
 *   <li>Informações básicas são conferidas quanto à consistência</li>
 *   <li>O sinistro está apto para iniciar análise técnica</li>
 * </ul>
 *
 * <p>Este evento dispara:
 * <ul>
 *   <li>Transição de status para EM_VALIDACAO → VALIDADO</li>
 *   <li>Liberação para análise técnica</li>
 *   <li>Atualização de histórico de validação</li>
 *   <li>Notificação ao segurado sobre progresso</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class SinistroValidadoEvent extends DomainEvent {

    @JsonProperty("sinistroId")
    private final String sinistroId;

    @JsonProperty("dadosComplementares")
    private final Map<String, Object> dadosComplementares;

    @JsonProperty("documentosAnexados")
    private final List<String> documentosAnexados;

    @JsonProperty("operadorId")
    private final String operadorId;

    /**
     * Construtor principal para criação do evento.
     *
     * @param aggregateId ID único do aggregate de sinistro
     * @param sinistroId ID do sinistro validado
     * @param dadosComplementares Mapa com dados adicionais coletados (local, testemunhas, etc)
     * @param documentosAnexados Lista de IDs dos documentos anexados e validados
     * @param operadorId ID do operador que realizou a validação
     */
    @JsonCreator
    public SinistroValidadoEvent(
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("sinistroId") String sinistroId,
            @JsonProperty("dadosComplementares") Map<String, Object> dadosComplementares,
            @JsonProperty("documentosAnexados") List<String> documentosAnexados,
            @JsonProperty("operadorId") String operadorId) {

        super(aggregateId, "SinistroAggregate", 1);

        this.sinistroId = validarSinistroId(sinistroId);
        this.dadosComplementares = validarDadosComplementares(dadosComplementares);
        this.documentosAnexados = validarDocumentosAnexados(documentosAnexados);
        this.operadorId = validarOperadorId(operadorId);
    }

    private String validarSinistroId(String sinistroId) {
        if (sinistroId == null || sinistroId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do sinistro não pode ser nulo ou vazio");
        }
        return sinistroId.trim();
    }

    private Map<String, Object> validarDadosComplementares(Map<String, Object> dadosComplementares) {
        if (dadosComplementares == null) {
            throw new IllegalArgumentException("Dados complementares não podem ser nulos");
        }
        return dadosComplementares;
    }

    private List<String> validarDocumentosAnexados(List<String> documentosAnexados) {
        if (documentosAnexados == null) {
            throw new IllegalArgumentException("Lista de documentos não pode ser nula");
        }
        return documentosAnexados;
    }

    private String validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        return operadorId.trim();
    }

    // Getters
    public String getSinistroId() { return sinistroId; }
    public Map<String, Object> getDadosComplementares() { return dadosComplementares; }
    public List<String> getDocumentosAnexados() { return documentosAnexados; }
    public String getOperadorId() { return operadorId; }

    @Override
    public String getEventType() {
        return "SinistroValidadoEvent";
    }

    @Override
    public String toString() {
        return String.format("SinistroValidadoEvent{aggregateId='%s', sinistroId='%s', " +
                           "documentosCount=%d, operadorId='%s', correlationId=%s, timestamp=%s}",
                getAggregateId(), sinistroId, documentosAnexados.size(), operadorId,
                getCorrelationId(), getTimestamp());
    }
}
