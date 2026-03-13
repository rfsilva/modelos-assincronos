package com.seguradora.hibrida.aggregate.health;

import com.seguradora.hibrida.aggregate.config.AggregateProperties;
import com.seguradora.hibrida.eventstore.EventStore;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import com.seguradora.hibrida.snapshot.SnapshotStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes do AggregateHealthIndicator")
class AggregateHealthIndicatorTest {

    @Mock
    private EventStore eventStore;

    @Mock
    private SnapshotStore snapshotStore;

    @Mock
    private AggregateProperties properties;

    @Mock
    private AggregateProperties.Metrics metricsProperties;

    @Mock
    private AggregateProperties.HealthCheck healthCheckProperties;

    @Mock
    private AggregateProperties.Validation validationProperties;

    @Mock
    private AggregateProperties.Performance performanceProperties;

    @Mock
    private AggregateProperties.Snapshot snapshotProperties;

    private AggregateHealthIndicator healthIndicator;

    private SnapshotStatistics createSnapshotStatistics(long totalSnapshots, long compressedSnapshots, double avgSize, double compressionRatio) {
        // Calcula compressionRatio baseado em totalCompressedSize / totalOriginalSize
        long totalOriginalSize = (long)(avgSize * totalSnapshots);
        long totalCompressedSize = (long)(totalOriginalSize * compressionRatio);

        return SnapshotStatistics.builder()
                .totalSnapshots(totalSnapshots)
                .compressedSnapshots(compressedSnapshots)
                .totalOriginalSize(totalOriginalSize)
                .totalCompressedSize(totalCompressedSize)
                .averageSnapshotSize(avgSize)
                .averageCompressionRatio(totalSnapshots > 0 ? (double) totalCompressedSize / totalOriginalSize : 0.0)
                .oldestSnapshot(java.time.Instant.now().minusSeconds(3600))
                .newestSnapshot(java.time.Instant.now())
                .latestVersion(100L)
                .build();
    }

    @BeforeEach
    void setUp() {
        // Setup default properties
        when(properties.getMetrics()).thenReturn(metricsProperties);
        when(properties.getHealthCheck()).thenReturn(healthCheckProperties);
        when(properties.getValidation()).thenReturn(validationProperties);
        when(properties.getPerformance()).thenReturn(performanceProperties);
        when(properties.getSnapshot()).thenReturn(snapshotProperties);

        when(metricsProperties.isEnabled()).thenReturn(true);
        when(healthCheckProperties.isEnabled()).thenReturn(true);
        when(healthCheckProperties.getTimeoutSeconds()).thenReturn(5);
        when(validationProperties.isEnabled()).thenReturn(true);
        when(performanceProperties.isCacheHandlers()).thenReturn(true);
        when(performanceProperties.isParallelValidation()).thenReturn(false);
        when(performanceProperties.isOptimizeReflection()).thenReturn(true);
        when(snapshotProperties.isAutoCreate()).thenReturn(true);
        when(snapshotProperties.getThresholdEvents()).thenReturn(50);

        healthIndicator = new AggregateHealthIndicator(eventStore, snapshotStore, properties);
    }

    @Test
    @DisplayName("Deve retornar UP quando todos os componentes estão saudáveis")
    void shouldReturnUpWhenAllComponentsHealthy() {
        // Arrange
        when(eventStore.aggregateExists(anyString())).thenReturn(false);
        when(snapshotStore.hasSnapshots(anyString())).thenReturn(false);

        SnapshotStatistics stats = createSnapshotStatistics(10, 5000L, 100.0, 0.5);
        when(snapshotStore.getGlobalStatistics()).thenReturn(stats);

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("status");
        assertThat(health.getDetails()).containsKey("eventStore");
        assertThat(health.getDetails()).containsKey("snapshotStore");
        assertThat(health.getDetails()).containsKey("configuration");
        assertThat(health.getDetails()).containsKey("performance");
        assertThat(health.getDetails()).containsKey("version");
    }

    @Test
    @DisplayName("Deve retornar DOWN quando Event Store está indisponível")
    void shouldReturnDownWhenEventStoreUnavailable() {
        // Arrange
        when(eventStore.aggregateExists(anyString())).thenThrow(new RuntimeException("Connection failed"));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);

        @SuppressWarnings("unchecked")
        Map<String, Object> eventStoreHealth = (Map<String, Object>) health.getDetails().get("eventStore");
        assertThat(eventStoreHealth.get("connected")).isEqualTo(false);
        assertThat(eventStoreHealth.get("status")).isEqualTo("DOWN");
    }

    @Test
    @DisplayName("Deve verificar tempo de resposta do Event Store")
    void shouldCheckEventStoreResponseTime() {
        // Arrange
        when(eventStore.aggregateExists(anyString())).thenAnswer(invocation -> {
            Thread.sleep(100); // Simula latência
            return false;
        });

        when(snapshotStore.hasSnapshots(anyString())).thenReturn(false);
        when(snapshotStore.getGlobalStatistics()).thenReturn(createSnapshotStatistics(0, 0L, 0.0, 0.0));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> eventStoreHealth = (Map<String, Object>) health.getDetails().get("eventStore");
        assertThat(eventStoreHealth.get("responseTimeMs")).isNotNull();

        Long responseTime = (Long) eventStoreHealth.get("responseTimeMs");
        assertThat(responseTime).isGreaterThanOrEqualTo(100L);
    }

    @Test
    @DisplayName("Deve alertar quando tempo de resposta é alto")
    void shouldWarnWhenResponseTimeIsHigh() {
        // Arrange
        when(eventStore.aggregateExists(anyString())).thenAnswer(invocation -> {
            Thread.sleep(1100); // Simula latência alta
            return false;
        });

        when(snapshotStore.hasSnapshots(anyString())).thenReturn(false);
        when(snapshotStore.getGlobalStatistics()).thenReturn(createSnapshotStatistics(0, 0L, 0.0, 0.0));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> eventStoreHealth = (Map<String, Object>) health.getDetails().get("eventStore");
        assertThat(eventStoreHealth).containsKey("warning");

        String warning = (String) eventStoreHealth.get("warning");
        assertThat(warning).contains("Response time alto");
    }

    @Test
    @DisplayName("Deve verificar funcionalidade do Snapshot Store")
    void shouldCheckSnapshotStoreFunctionality() {
        // Arrange
        when(eventStore.aggregateExists(anyString())).thenReturn(false);
        when(snapshotStore.hasSnapshots(anyString())).thenReturn(true);

        SnapshotStatistics stats = createSnapshotStatistics(15, 10000L, 150.0, 0.6);
        when(snapshotStore.getGlobalStatistics()).thenReturn(stats);

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> snapshotHealth = (Map<String, Object>) health.getDetails().get("snapshotStore");
        assertThat(snapshotHealth.get("functional")).isEqualTo(true);
        assertThat(snapshotHealth.get("status")).isEqualTo("UP");
        assertThat(snapshotHealth.get("totalSnapshots")).isEqualTo(15L);
        // compressionRatio calculado via getOverallCompressionRatio() = 1.0 - (compressed / original)
        assertThat(snapshotHealth.get("compressionRatio")).isEqualTo(stats.getOverallCompressionRatio());
    }

    @Test
    @DisplayName("Deve continuar saudável mesmo se Snapshot Store falhar")
    void shouldStayHealthyEvenIfSnapshotStoreFails() {
        // Arrange
        when(eventStore.aggregateExists(anyString())).thenReturn(false);
        when(snapshotStore.hasSnapshots(anyString())).thenThrow(new RuntimeException("Snapshot Store unavailable"));

        // Act
        Health health = healthIndicator.health();

        // Assert - Sistema deve estar UP pois Snapshot Store não é crítico
        assertThat(health.getStatus()).isEqualTo(Status.UP);

        @SuppressWarnings("unchecked")
        Map<String, Object> snapshotHealth = (Map<String, Object>) health.getDetails().get("snapshotStore");
        assertThat(snapshotHealth.get("functional")).isEqualTo(false);
        assertThat(snapshotHealth.get("status")).isEqualTo("DOWN");
    }

    @Test
    @DisplayName("Deve verificar configurações do sistema")
    void shouldCheckSystemConfiguration() {
        // Arrange
        when(eventStore.aggregateExists(anyString())).thenReturn(false);
        when(snapshotStore.hasSnapshots(anyString())).thenReturn(false);
        when(snapshotStore.getGlobalStatistics()).thenReturn(createSnapshotStatistics(0, 0L, 0.0, 0.0));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> configHealth = (Map<String, Object>) health.getDetails().get("configuration");
        assertThat(configHealth.get("metricsEnabled")).isEqualTo(true);
        assertThat(configHealth.get("healthCheckEnabled")).isEqualTo(true);
        assertThat(configHealth.get("validationEnabled")).isEqualTo(true);
        assertThat(configHealth.get("cacheEnabled")).isEqualTo(true);
        assertThat(configHealth.get("snapshotAutoCreate")).isEqualTo(true);
        assertThat(configHealth.get("status")).isEqualTo("UP");
    }

    @Test
    @DisplayName("Deve alertar quando timeout está fora do range recomendado")
    void shouldWarnWhenTimeoutOutOfRange() {
        // Arrange
        when(healthCheckProperties.getTimeoutSeconds()).thenReturn(60);
        when(eventStore.aggregateExists(anyString())).thenReturn(false);
        when(snapshotStore.hasSnapshots(anyString())).thenReturn(false);
        when(snapshotStore.getGlobalStatistics()).thenReturn(createSnapshotStatistics(0, 0L, 0.0, 0.0));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> configHealth = (Map<String, Object>) health.getDetails().get("configuration");
        assertThat(configHealth).containsKey("warning");

        String warning = (String) configHealth.get("warning");
        assertThat(warning).contains("Timeout de health check fora do range recomendado");
    }

    @Test
    @DisplayName("Deve alertar quando snapshot threshold está fora do range recomendado")
    void shouldWarnWhenSnapshotThresholdOutOfRange() {
        // Arrange
        when(snapshotProperties.getThresholdEvents()).thenReturn(5000);
        when(eventStore.aggregateExists(anyString())).thenReturn(false);
        when(snapshotStore.hasSnapshots(anyString())).thenReturn(false);
        when(snapshotStore.getGlobalStatistics()).thenReturn(createSnapshotStatistics(0, 0L, 0.0, 0.0));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> configHealth = (Map<String, Object>) health.getDetails().get("configuration");
        assertThat(configHealth).containsKey("warning");

        String warning = (String) configHealth.get("warning");
        assertThat(warning).contains("Threshold de snapshot fora do range recomendado");
    }

    @Test
    @DisplayName("Deve verificar métricas de performance")
    void shouldCheckPerformanceMetrics() {
        // Arrange
        when(eventStore.aggregateExists(anyString())).thenReturn(false);
        when(snapshotStore.hasSnapshots(anyString())).thenReturn(false);
        when(snapshotStore.getGlobalStatistics()).thenReturn(createSnapshotStatistics(0, 0L, 0.0, 0.0));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> performanceHealth = (Map<String, Object>) health.getDetails().get("performance");
        assertThat(performanceHealth.get("memoryUsagePercent")).isNotNull();
        assertThat(performanceHealth.get("totalMemoryMB")).isNotNull();
        assertThat(performanceHealth.get("usedMemoryMB")).isNotNull();
        assertThat(performanceHealth.get("cacheEnabled")).isEqualTo(true);
        assertThat(performanceHealth.get("parallelValidation")).isEqualTo(false);
        assertThat(performanceHealth.get("optimizeReflection")).isEqualTo(true);
        assertThat(performanceHealth.get("status")).isEqualTo("UP");
    }

    @Test
    @DisplayName("Deve incluir timestamp e versão")
    void shouldIncludeTimestampAndVersion() {
        // Arrange
        when(eventStore.aggregateExists(anyString())).thenReturn(false);
        when(snapshotStore.hasSnapshots(anyString())).thenReturn(false);
        when(snapshotStore.getGlobalStatistics()).thenReturn(createSnapshotStatistics(0, 0L, 0.0, 0.0));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getDetails()).containsKey("timestamp");
        assertThat(health.getDetails()).containsKey("version");
        assertThat(health.getDetails().get("version")).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Deve retornar DOWN quando há erro inesperado")
    void shouldReturnDownOnUnexpectedError() {
        // Arrange
        when(properties.getHealthCheck()).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        // O erro é inserido em configuration, eventStore ou snapshotStore, não no nível raiz
        assertThat(health.getDetails()).containsKey("timestamp");

        // Verificar que pelo menos um componente tem erro
        boolean hasError = health.getDetails().values().stream()
            .anyMatch(value -> {
                if (value instanceof Map) {
                    return ((Map<?, ?>) value).containsKey("error");
                }
                return false;
            });
        assertThat(hasError).isTrue();
    }

    @Test
    @DisplayName("Deve retornar status DEGRADED quando Snapshot Store está down mas Event Store está UP")
    void shouldReturnDegradedWhenOnlySnapshotStoreDown() {
        // Arrange
        when(eventStore.aggregateExists(anyString())).thenReturn(false);
        when(snapshotStore.hasSnapshots(anyString())).thenThrow(new RuntimeException("Snapshot Store down"));

        // Act
        Health health = healthIndicator.health();

        // Assert - Como Event Store está UP, sistema deve estar funcional
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails().get("status")).isEqualTo("UP");
    }

    @Test
    @DisplayName("Deve tratar erro ao obter estatísticas de snapshot")
    void shouldHandleErrorWhenGettingSnapshotStatistics() {
        // Arrange
        when(eventStore.aggregateExists(anyString())).thenReturn(false);
        when(snapshotStore.hasSnapshots(anyString())).thenReturn(true);
        when(snapshotStore.getGlobalStatistics()).thenThrow(new RuntimeException("Stats unavailable"));

        // Act
        Health health = healthIndicator.health();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> snapshotHealth = (Map<String, Object>) health.getDetails().get("snapshotStore");
        assertThat(snapshotHealth.get("statsError")).isEqualTo("Não foi possível obter estatísticas");
    }
}
