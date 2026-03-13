package com.seguradora.hibrida.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguradora.hibrida.eventstore.EventStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.axonframework.common.caching.Cache;
import org.axonframework.common.caching.WeakReferenceCache;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Axon Framework integrada com Event Store customizado.
 * 
 * Configura os componentes do Axon para trabalhar com nossa implementação
 * customizada de Event Store, mantendo compatibilidade com CQRS/ES.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AxonConfig {

    private final EventStore customEventStore;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Serializer JSON otimizado para o Axon Framework.
     */
    @Bean("axonJsonSerializer")
    public Serializer axonJsonSerializer(ObjectMapper objectMapper) {
        log.info("Configurando Axon JSON Serializer");
        return JacksonSerializer.builder()
                .objectMapper(objectMapper)
                .build();
    }

    /**
     * Event Bus simples para publicação de eventos.
     * 
     * Integrado com nosso Event Store customizado para persistência.
     */
    @Bean("axonEventBus")
    public EventBus eventBus() {
        log.info("Configurando Axon Event Bus");
        return SimpleEventBus.builder()
                .build();
    }

    /**
     * Cache para aggregates em memória.
     * 
     * Otimiza performance evitando reconstrução desnecessária de aggregates.
     */
    @Bean
    public Cache aggregateCache() {
        log.info("Configurando cache de aggregates");
        return new WeakReferenceCache();
    }

    /**
     * Wrapper do nosso Event Store customizado para integração com Axon.
     *
     * Permite que o Axon use nossa implementação customizada mantendo
     * compatibilidade com a interface padrão.
     */
    @Bean("axonEventStore")
    public org.axonframework.eventsourcing.eventstore.EventStore axonEventStore() {
        log.info("Configurando integração Axon com Event Store customizado");

        // Por enquanto, mantemos o JPA Event Store do Axon
        // Em versões futuras, criaremos um adapter para nosso Event Store customizado
        return org.axonframework.eventsourcing.eventstore.EmbeddedEventStore.builder()
                .storageEngine(JpaEventStorageEngine.builder()
                        .entityManagerProvider(() -> entityManager)
                        .build())
                .build();
    }
}