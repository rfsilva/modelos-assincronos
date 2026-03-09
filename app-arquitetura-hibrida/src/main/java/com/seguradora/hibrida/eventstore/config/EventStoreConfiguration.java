package com.seguradora.hibrida.eventstore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.impl.PostgreSQLEventStore;
import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.eventstore.serialization.EventSerializer;
import com.seguradora.hibrida.eventstore.serialization.JsonEventSerializer;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuração do Event Store e componentes relacionados.
 * 
 * Configura beans necessários para funcionamento do Event Store,
 * incluindo serialização, métricas e otimizações.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(EventStoreProperties.class)
public class EventStoreConfiguration {
    
    /**
     * ObjectMapper otimizado para serialização de eventos.
     */
    @Bean("eventStoreObjectMapper")
    public ObjectMapper eventStoreObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configurações para Java Time
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Configurações para enums
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        
        // Configurações para propriedades desconhecidas
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Configurações para valores nulos
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        
        log.info("ObjectMapper do Event Store configurado com suporte a Java Time e enums");
        return mapper;
    }
    
    /**
     * Serializer de eventos usando Jackson JSON.
     */
    @Bean
    public EventSerializer eventSerializer(ObjectMapper eventStoreObjectMapper) {
        log.info("Configurando JsonEventSerializer");
        return new JsonEventSerializer(eventStoreObjectMapper);
    }
    
    /**
     * Implementação principal do Event Store.
     */
    @Bean
    @Primary
    public EventStore eventStore(EventStoreRepository repository, EventSerializer eventSerializer) {
        log.info("Configurando PostgreSQLEventStore");
        return new PostgreSQLEventStore(repository, eventSerializer);
    }
    
    /**
     * Métricas customizadas do Event Store.
     */
    @Bean
    public EventStoreMetrics eventStoreMetrics(MeterRegistry meterRegistry, EventStore eventStore) {
        log.info("Configurando métricas do Event Store");
        return new EventStoreMetrics(meterRegistry, eventStore);
    }
    
    /**
     * Health indicator para o Event Store.
     */
    @Bean
    public EventStoreHealthIndicator eventStoreHealthIndicator(EventStore eventStore) {
        log.info("Configurando health indicator do Event Store");
        return new EventStoreHealthIndicator(eventStore);
    }
}