package com.seguradora.hibrida.aggregate.config;

import com.seguradora.hibrida.aggregate.health.AggregateHealthIndicator;
import com.seguradora.hibrida.aggregate.metrics.AggregateMetrics;
import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link AggregateConfiguration}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AggregateConfiguration Tests")
class AggregateConfigurationTest {

    @Mock
    private EventStore eventStore;

    @Mock
    private SnapshotStore snapshotStore;

    private MeterRegistry meterRegistry;
    private AggregateProperties properties;
    private AggregateConfiguration configuration;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        properties = new AggregateProperties();
        configuration = new AggregateConfiguration();
    }

    // =========================================================================
    // Meta-anotações da classe de configuração
    // =========================================================================

    @Nested
    @DisplayName("Meta-anotações da classe")
    class MetaAnotacoes {

        @Test
        @DisplayName("Deve ter anotação @Configuration")
        void shouldHaveConfigurationAnnotation() {
            assertThat(AggregateConfiguration.class.isAnnotationPresent(Configuration.class))
                    .isTrue();
        }

        @Test
        @DisplayName("Deve ter anotação @EnableConfigurationProperties para AggregateProperties")
        void shouldHaveEnableConfigurationPropertiesAnnotation() {
            EnableConfigurationProperties annotation =
                    AggregateConfiguration.class.getAnnotation(EnableConfigurationProperties.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).contains(AggregateProperties.class);
        }
    }

    // =========================================================================
    // Bean: aggregateMetrics
    // =========================================================================

    @Nested
    @DisplayName("Bean: aggregateMetrics")
    class BeanAggregateMetrics {

        @Test
        @DisplayName("Deve criar bean AggregateMetrics")
        void shouldCreateAggregateMetricsBean() {
            // When
            AggregateMetrics metrics = configuration.aggregateMetrics(meterRegistry, properties);

            // Then
            assertThat(metrics).isNotNull();
        }

        @Test
        @DisplayName("Bean AggregateMetrics deve registrar contadores no registry")
        void aggregateMetricsBeanShouldRegisterCounters() {
            // When
            configuration.aggregateMetrics(meterRegistry, properties);

            // Then
            assertThat(meterRegistry.find("aggregate_saves_total").counter()).isNotNull();
            assertThat(meterRegistry.find("aggregate_loads_total").counter()).isNotNull();
        }

        @Test
        @DisplayName("Deve criar instâncias independentes a cada chamada")
        void shouldCreateIndependentInstancesOnEachCall() {
            // When
            AggregateMetrics metrics1 = configuration.aggregateMetrics(
                    new SimpleMeterRegistry(), properties);
            AggregateMetrics metrics2 = configuration.aggregateMetrics(
                    new SimpleMeterRegistry(), properties);

            // Then
            assertThat(metrics1).isNotSameAs(metrics2);
        }
    }

    // =========================================================================
    // Bean: aggregateHealthIndicator
    // =========================================================================

    @Nested
    @DisplayName("Bean: aggregateHealthIndicator")
    class BeanAggregateHealthIndicator {

        @BeforeEach
        void setupMocks() {
            when(eventStore.aggregateExists(anyString())).thenReturn(false);
            lenient().when(snapshotStore.hasSnapshots(anyString())).thenReturn(false);
            lenient().when(snapshotStore.getGlobalStatistics()).thenReturn(null);
        }

        @Test
        @DisplayName("Deve criar bean HealthIndicator")
        void shouldCreateHealthIndicatorBean() {
            // When
            HealthIndicator healthIndicator = configuration.aggregateHealthIndicator(
                    eventStore, snapshotStore, properties);

            // Then
            assertThat(healthIndicator).isNotNull();
            assertThat(healthIndicator).isInstanceOf(AggregateHealthIndicator.class);
        }

        @Test
        @DisplayName("HealthIndicator criado deve responder a health()")
        void healthIndicatorShouldRespondToHealthCall() {
            // When
            HealthIndicator healthIndicator = configuration.aggregateHealthIndicator(
                    eventStore, snapshotStore, properties);

            // Then
            assertThat(healthIndicator.health()).isNotNull();
        }
    }

    // =========================================================================
    // Bean: aggregateProperties
    // =========================================================================

    @Nested
    @DisplayName("Bean: aggregateProperties")
    class BeanAggregateProperties {

        @Test
        @DisplayName("Deve criar bean AggregateProperties com valores padrão")
        void shouldCreateAggregatePropertiesBeanWithDefaults() {
            // When
            AggregateProperties props = configuration.aggregateProperties();

            // Then
            assertThat(props).isNotNull();
            assertThat(props.getMetrics()).isNotNull();
            assertThat(props.getHealthCheck()).isNotNull();
            assertThat(props.getValidation()).isNotNull();
            assertThat(props.getPerformance()).isNotNull();
            assertThat(props.getSnapshot()).isNotNull();
        }

        @Test
        @DisplayName("Deve criar instâncias independentes de AggregateProperties")
        void shouldCreateIndependentAggregatePropertiesInstances() {
            // When
            AggregateProperties props1 = configuration.aggregateProperties();
            AggregateProperties props2 = configuration.aggregateProperties();

            // Then
            assertThat(props1).isNotSameAs(props2);
        }
    }
}
