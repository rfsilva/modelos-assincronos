package com.seguradora.hibrida.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguradora.hibrida.eventstore.EventStore;
import org.axonframework.common.caching.Cache;
import org.axonframework.common.caching.WeakReferenceCache;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link AxonConfig}.
 *
 * <p>Valida a criação e configuração dos beans do Axon Framework,
 * incluindo serializer, event bus, cache e event store.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AxonConfig - Testes Unitários")
class AxonConfigTest {

    @Mock
    private EventStore customEventStore;

    @Mock
    private ObjectMapper objectMapper;

    private AxonConfig config;

    @BeforeEach
    void setUp() {
        config = new AxonConfig(customEventStore);
    }

    @Test
    @DisplayName("Deve criar Axon JSON Serializer corretamente")
    void shouldCreateAxonJsonSerializerCorrectly() {
        // When
        Serializer serializer = config.axonJsonSerializer(objectMapper);

        // Then
        assertThat(serializer).isNotNull();
        assertThat(serializer).isInstanceOf(JacksonSerializer.class);
    }

    @Test
    @DisplayName("Deve configurar Serializer com ObjectMapper fornecido")
    void shouldConfigureSerializerWithProvidedObjectMapper() {
        // Given
        when(objectMapper.getTypeFactory()).thenReturn(new ObjectMapper().getTypeFactory());

        // When
        Serializer serializer = config.axonJsonSerializer(objectMapper);

        // Then
        assertThat(serializer).isNotNull();
        assertThat(serializer).isInstanceOf(JacksonSerializer.class);
    }

    @Test
    @DisplayName("Deve criar Event Bus corretamente")
    void shouldCreateEventBusCorrectly() {
        // When
        EventBus eventBus = config.eventBus();

        // Then
        assertThat(eventBus).isNotNull();
        assertThat(eventBus).isInstanceOf(SimpleEventBus.class);
    }

    @Test
    @DisplayName("Deve criar Event Bus do tipo SimpleEventBus")
    void shouldCreateSimpleEventBus() {
        // When
        EventBus eventBus = config.eventBus();

        // Then
        assertThat(eventBus).isInstanceOf(SimpleEventBus.class);
    }

    @Test
    @DisplayName("Deve criar cache de aggregates corretamente")
    void shouldCreateAggregateCacheCorrectly() {
        // When
        Cache cache = config.aggregateCache();

        // Then
        assertThat(cache).isNotNull();
        assertThat(cache).isInstanceOf(WeakReferenceCache.class);
    }

    @Test
    @DisplayName("Deve criar cache do tipo WeakReferenceCache")
    void shouldCreateWeakReferenceCache() {
        // When
        Cache cache = config.aggregateCache();

        // Then
        assertThat(cache).isInstanceOf(WeakReferenceCache.class);
    }

    @Test
    @DisplayName("Deve criar todos os beans básicos sem exceções")
    void shouldCreateAllBasicBeansWithoutExceptions() {
        // When & Then - não deve lançar exceção
        // Nota: axonEventStore() requer EntityManagerProvider injetado pelo Spring, não testado aqui
        assertThat(config.axonJsonSerializer(objectMapper)).isNotNull();
        assertThat(config.eventBus()).isNotNull();
        assertThat(config.aggregateCache()).isNotNull();
    }

    @Test
    @DisplayName("Deve injetar EventStore customizado no construtor")
    void shouldInjectCustomEventStoreInConstructor() {
        // Given
        AxonConfig newConfig = new AxonConfig(customEventStore);

        // When & Then - deve criar config sem exceção
        assertThat(newConfig).isNotNull();
    }
}
