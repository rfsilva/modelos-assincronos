package com.seguradora.hibrida.eventstore.serialization;

import com.seguradora.hibrida.eventstore.model.DomainEvent;

/**
 * Interface para serialização e deserialização de eventos.
 * 
 * Define o contrato para conversão de eventos de domínio em formato
 * persistível, com suporte a versionamento e compressão.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface EventSerializer {
    
    /**
     * Serializa um evento de domínio para string JSON.
     * 
     * @param event Evento a ser serializado
     * @return String JSON representando o evento
     * @throws SerializationException em caso de erro na serialização
     */
    String serialize(DomainEvent event);
    
    /**
     * Deserializa uma string JSON para evento de domínio.
     * 
     * @param eventData String JSON do evento
     * @param eventType Tipo do evento para deserialização
     * @return Evento de domínio deserializado
     * @throws SerializationException em caso de erro na deserialização
     */
    DomainEvent deserialize(String eventData, String eventType);
    
    /**
     * Serializa com compressão se necessário.
     * 
     * @param event Evento a ser serializado
     * @param compressionThreshold Tamanho mínimo para compressão (em bytes)
     * @return Resultado da serialização com informação de compressão
     */
    SerializationResult serializeWithCompression(DomainEvent event, int compressionThreshold);
    
    /**
     * Deserializa dados possivelmente comprimidos.
     * 
     * @param eventData Dados do evento
     * @param eventType Tipo do evento
     * @param compressed Indica se os dados estão comprimidos
     * @return Evento deserializado
     */
    DomainEvent deserializeCompressed(String eventData, String eventType, boolean compressed);
    
    /**
     * Verifica se o serializer suporta um tipo de evento.
     * 
     * @param eventType Tipo do evento
     * @return true se suportado, false caso contrário
     */
    boolean supports(String eventType);
}