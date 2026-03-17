package com.seguradora.hibrida.eventstore.config;

import com.seguradora.hibrida.eventstore.archive.EventArchiveProperties;
import com.seguradora.hibrida.eventstore.archive.EventArchiver;
import com.seguradora.hibrida.eventstore.archive.ArchiveStorageService;
import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.eventstore.partition.PartitionManager;
import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.eventstore.serialization.EventSerializer;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link EventStoreConfiguration}.
 */
@DisplayName("EventStoreConfiguration Tests")
class EventStoreConfigurationTest {

    @Test
    @DisplayName("Deve estar anotado com @Configuration")
    void shouldBeAnnotatedWithConfiguration() {
        assertThat(EventStoreConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @EnableScheduling")
    void shouldBeAnnotatedWithEnableScheduling() {
        assertThat(EventStoreConfiguration.class.isAnnotationPresent(EnableScheduling.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @EnableConfigurationProperties")
    void shouldBeAnnotatedWithEnableConfigurationProperties() {
        assertThat(EventStoreConfiguration.class.isAnnotationPresent(EnableConfigurationProperties.class)).isTrue();
    }

    @Test
    @DisplayName("Deve declarar bean eventStoreObjectMapper")
    void shouldDeclareEventStoreObjectMapperBean() {
        boolean found = Arrays.stream(EventStoreConfiguration.class.getMethods())
                .anyMatch(m -> m.isAnnotationPresent(Bean.class)
                        && (m.getName().equals("eventStoreObjectMapper")));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Deve declarar bean eventSerializer")
    void shouldDeclareEventSerializerBean() throws NoSuchMethodException {
        // eventSerializer(ObjectMapper) → bean
        Method m = EventStoreConfiguration.class.getMethod("eventSerializer",
                com.fasterxml.jackson.databind.ObjectMapper.class);
        assertThat(m.isAnnotationPresent(Bean.class)).isTrue();
        assertThat(m.getReturnType()).isEqualTo(EventSerializer.class);
    }

    @Test
    @DisplayName("Deve declarar bean eventStore")
    void shouldDeclareEventStoreBean() throws NoSuchMethodException {
        Method m = EventStoreConfiguration.class.getMethod("eventStore",
                EventStoreRepository.class, EventSerializer.class);
        assertThat(m.isAnnotationPresent(Bean.class)).isTrue();
        assertThat(m.getReturnType()).isEqualTo(EventStore.class);
    }

    @Test
    @DisplayName("Deve declarar bean partitionManager")
    void shouldDeclarePartitionManagerBean() throws NoSuchMethodException {
        Method m = EventStoreConfiguration.class.getMethod("partitionManager", JdbcTemplate.class);
        assertThat(m.isAnnotationPresent(Bean.class)).isTrue();
        assertThat(m.getReturnType()).isEqualTo(PartitionManager.class);
    }

    @Test
    @DisplayName("Deve declarar bean eventStoreMetrics")
    void shouldDeclareEventStoreMetricsBean() throws NoSuchMethodException {
        Method m = EventStoreConfiguration.class.getMethod("eventStoreMetrics",
                MeterRegistry.class, EventStore.class);
        assertThat(m.isAnnotationPresent(Bean.class)).isTrue();
        assertThat(m.getReturnType()).isEqualTo(EventStoreMetrics.class);
    }

    @Test
    @DisplayName("Deve declarar bean eventStoreHealthIndicator")
    void shouldDeclareEventStoreHealthIndicatorBean() throws NoSuchMethodException {
        Method m = EventStoreConfiguration.class.getMethod("eventStoreHealthIndicator",
                EventStore.class, PartitionManager.class, EventArchiver.class);
        assertThat(m.isAnnotationPresent(Bean.class)).isTrue();
        assertThat(m.getReturnType()).isEqualTo(EventStoreHealthIndicator.class);
    }
}
