package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Evento de domínio para sinistros.
 * 
 * <p>Este evento representa mudanças no domínio de sinistros
 * e é usado para comunicação entre bounded contexts:
 * <ul>
 *   <li>Serialização/deserialização JSON</li>
 *   <li>Roteamento automático para handlers</li>
 *   <li>Processamento assíncrono</li>
 *   <li>Retry em caso de falha</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@SuperBuilder
public class SinistroEvent extends DomainEvent {
    
    private final String numeroSinistro;
    private final String status;
    private final String descricao;
    private final Double valorEstimado;
    
    /**
     * Construtor protegido para uso interno.
     */
    protected SinistroEvent(String aggregateId, String numeroSinistro, 
                           String status, String descricao, Double valorEstimado) {
        super(aggregateId, "Sinistro", 1L);
        this.numeroSinistro = numeroSinistro;
        this.status = status;
        this.descricao = descricao;
        this.valorEstimado = valorEstimado;
    }
    
    /**
     * Construtor sem argumentos para deserialização.
     */
    protected SinistroEvent() {
        super();
        this.numeroSinistro = null;
        this.status = null;
        this.descricao = null;
        this.valorEstimado = null;
    }
    
    /**
     * Factory method para evento de criação de sinistro.
     */
    public static SinistroEvent sinistroCriado(String aggregateId, String numeroSinistro, 
                                              String descricao, Double valorEstimado) {
        SinistroEvent event = new SinistroEvent(aggregateId, numeroSinistro, 
                                              "ABERTO", descricao, valorEstimado);
        event.addMetadata("eventType", "SinistroCriado");
        return event;
    }
    
    /**
     * Factory method para evento de atualização de sinistro.
     */
    public static SinistroEvent sinistroAtualizado(String aggregateId, String numeroSinistro, 
                                                  String status, String descricao, Double valorEstimado) {
        SinistroEvent event = new SinistroEvent(aggregateId, numeroSinistro, 
                                              status, descricao, valorEstimado);
        event.addMetadata("eventType", "SinistroAtualizado");
        return event;
    }
    
    /**
     * Factory method para evento de finalização de sinistro.
     */
    public static SinistroEvent sinistroFinalizado(String aggregateId, String numeroSinistro, 
                                                  Double valorFinal) {
        SinistroEvent event = new SinistroEvent(aggregateId, numeroSinistro, 
                                              "FINALIZADO", "Sinistro finalizado", valorFinal);
        event.addMetadata("eventType", "SinistroFinalizado");
        return event;
    }
    
    /**
     * Factory method para evento de cancelamento de sinistro.
     */
    public static SinistroCancelado sinistroCancelado(String aggregateId, String numeroSinistro, 
                                                     String motivo) {
        return new SinistroCancelado(aggregateId, numeroSinistro, motivo);
    }
    
    @Override
    public String toString() {
        return String.format("SinistroEvent{eventType='%s', aggregateId='%s', numeroSinistro='%s', " +
                           "status='%s', valorEstimado=%s, correlationId=%s}", 
                           getEventType(), getAggregateId(), numeroSinistro, status, 
                           valorEstimado, getCorrelationId());
    }
    
    /**
     * Evento específico para cancelamento com informações adicionais.
     */
    @Getter
    public static class SinistroCancelado extends SinistroEvent {
        
        private final String motivoCancelamento;
        
        protected SinistroCancelado(String aggregateId, String numeroSinistro, String motivoCancelamento) {
            super(aggregateId, numeroSinistro, "CANCELADO", motivoCancelamento, 0.0);
            this.motivoCancelamento = motivoCancelamento;
            this.addMetadata("eventType", "SinistroCancelado");
        }
        
        /**
         * Construtor sem argumentos para deserialização.
         */
        protected SinistroCancelado() {
            super();
            this.motivoCancelamento = null;
        }
        
        @Override
        public String toString() {
            return String.format("SinistroCancelado{aggregateId='%s', numeroSinistro='%s', " +
                               "motivo='%s', correlationId=%s}", 
                               getAggregateId(), getNumeroSinistro(), motivoCancelamento, getCorrelationId());
        }
    }
}