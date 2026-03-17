package com.seguradora.hibrida.command.config;

import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandBusStatistics;
import com.seguradora.hibrida.command.CommandHandlerRegistry;
import com.seguradora.hibrida.command.example.TestCommand;
import com.seguradora.hibrida.command.example.TestCommandHandler;
import com.seguradora.hibrida.command.impl.SimpleCommandBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link CommandBusHealthIndicator}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommandBusHealthIndicator Tests")
class CommandBusHealthIndicatorTest {

    @Mock
    private CommandBus commandBus;

    private CommandBusHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new CommandBusHealthIndicator(commandBus);
    }

    // =========================================================================
    // checkHealth — cenários saudáveis
    // =========================================================================

    @Nested
    @DisplayName("checkHealth() — estado saudável")
    class CheckHealthSaudavel {

        @Test
        @DisplayName("Deve retornar status UP com handlers suficientes e baixa taxa de erro")
        void shouldReturnUpWithSufficientHandlersAndLowErrorRate() {
            // Given
            when(commandBus.getStatistics()).thenReturn(buildHealthyStats(2, 100, 5, 0, 0, 10.0));

            // When
            Map<String, Object> health = healthIndicator.checkHealth();

            // Then
            assertThat(health.get("status")).isEqualTo("UP");
            assertThat(health.get("healthy")).isEqualTo(true);
        }

        @Test
        @DisplayName("Deve incluir todas as métricas esperadas no resultado")
        void shouldIncludeAllExpectedMetricsInResult() {
            // Given
            when(commandBus.getStatistics()).thenReturn(buildHealthyStats(1, 50, 2, 0, 0, 50.0));

            // When
            Map<String, Object> health = healthIndicator.checkHealth();

            // Then
            assertThat(health).containsKeys(
                    "registeredHandlers", "totalCommands", "commandsProcessed",
                    "commandsFailed", "commandsTimedOut", "commandsRejected",
                    "successRate", "errorRate", "averageExecutionTimeMs",
                    "minExecutionTimeMs", "maxExecutionTimeMs", "throughputPerSecond",
                    "lastUpdated", "startedAt", "uptime", "status", "healthy");
        }
    }

    // =========================================================================
    // checkHealth — estado degradado
    // =========================================================================

    @Nested
    @DisplayName("checkHealth() — estado degradado")
    class CheckHealthDegradado {

        @Test
        @DisplayName("Deve retornar DOWN quando não há handlers registrados")
        void shouldReturnDownWhenNoHandlersRegistered() {
            // Given
            when(commandBus.getStatistics()).thenReturn(buildHealthyStats(0, 0, 0, 0, 0, 0.0));

            // When
            Map<String, Object> health = healthIndicator.checkHealth();

            // Then
            assertThat(health.get("status")).isEqualTo("DOWN");
            assertThat(health.get("healthy")).isEqualTo(false);
            assertThat(health.get("issues").toString()).contains("Poucos handlers");
        }

        @Test
        @DisplayName("Deve retornar DOWN quando taxa de erro ultrapassa 10%")
        void shouldReturnDownWhenErrorRateExceedsTenPercent() {
            // Given – 20 processados, 3 falharam → ~13% de erro
            when(commandBus.getStatistics()).thenReturn(buildHealthyStats(1, 23, 20, 3, 0, 50.0));

            // When
            Map<String, Object> health = healthIndicator.checkHealth();

            // Then
            assertThat(health.get("status")).isEqualTo("DOWN");
            assertThat(health.get("issues").toString()).contains("Taxa de erro alta");
        }

        @Test
        @DisplayName("Deve retornar DOWN para tempo médio de execução acima de 5s com comandos processados")
        void shouldReturnDownWhenAverageExecutionTimeAboveFiveSeconds() {
            // Given
            CommandBusStatistics stats = buildHealthyStats(1, 10, 10, 0, 0, 6000.0);
            when(commandBus.getStatistics()).thenReturn(stats);

            // When
            Map<String, Object> health = healthIndicator.checkHealth();

            // Then
            assertThat(health.get("status")).isEqualTo("DOWN");
            assertThat(health.get("issues").toString()).contains("Tempo médio de execução alto");
        }

        @Test
        @DisplayName("Deve retornar DOWN e incluir campo error quando getStatistics lança exceção")
        void shouldReturnDownAndIncludeErrorFieldWhenGetStatisticsThrows() {
            // Given
            when(commandBus.getStatistics()).thenThrow(new RuntimeException("Falha interna"));

            // When
            Map<String, Object> health = healthIndicator.checkHealth();

            // Then
            assertThat(health.get("status")).isEqualTo("DOWN");
            assertThat(health.get("healthy")).isEqualTo(false);
            assertThat(health.get("error")).isEqualTo("Falha interna");
        }
    }

    // =========================================================================
    // isOperational
    // =========================================================================

    @Nested
    @DisplayName("isOperational()")
    class IsOperationalTests {

        @Test
        @DisplayName("Deve retornar true quando checkHealth indica UP")
        void shouldReturnTrueWhenCheckHealthIndicatesUp() {
            // Given
            when(commandBus.getStatistics()).thenReturn(buildHealthyStats(1, 10, 10, 0, 0, 50.0));

            // Then
            assertThat(healthIndicator.isOperational()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando checkHealth indica DOWN")
        void shouldReturnFalseWhenCheckHealthIndicatesDown() {
            // Given
            when(commandBus.getStatistics()).thenReturn(buildHealthyStats(0, 0, 0, 0, 0, 0.0));

            // Then
            assertThat(healthIndicator.isOperational()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando getStatistics lança exceção")
        void shouldReturnFalseWhenGetStatisticsThrows() {
            // Given
            when(commandBus.getStatistics()).thenThrow(new RuntimeException("Erro"));

            // Then
            assertThat(healthIndicator.isOperational()).isFalse();
        }
    }

    // =========================================================================
    // Integração com SimpleCommandBus real
    // =========================================================================

    @Nested
    @DisplayName("Integração com SimpleCommandBus")
    class IntegracaoComSimpleCommandBus {

        @Test
        @DisplayName("Deve reportar UP com handler registrado via SimpleCommandBus")
        void shouldReportUpWithHandlerRegisteredViaSimpleCommandBus() {
            // Given
            CommandHandlerRegistry registry = new CommandHandlerRegistry();
            SimpleCommandBus realBus = new SimpleCommandBus(registry);
            realBus.registerHandler(new TestCommandHandler());

            CommandBusHealthIndicator indicator = new CommandBusHealthIndicator(realBus);

            // When
            Map<String, Object> health = indicator.checkHealth();

            // Then
            assertThat(health.get("status")).isEqualTo("UP");
            assertThat(health.get("registeredHandlers")).isEqualTo(1);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Builds a CommandBusStatistics where:
     *   totalCommands = processed + failed + timedOut  (rejected = 0)
     * The {@code total} parameter is ignored – it was only used by the old mock helper.
     */
    private CommandBusStatistics buildHealthyStats(int handlers, int total,
            int processed, int failed, int timedOut, double avgExecTime) {
        CommandBusStatistics stats = new CommandBusStatistics();
        stats.updateRegisteredHandlers(handlers);
        stats.getTotalCommandsProcessed().set(processed);
        stats.getTotalCommandsFailed().set(failed);
        stats.getTotalCommandsTimedOut().set(timedOut);
        // rejected stays 0 so errorRate = (failed + timedOut) / (processed + failed + timedOut)
        stats.setAverageExecutionTimeMs(avgExecTime);
        stats.setMinExecutionTimeMs(10L);
        stats.setMaxExecutionTimeMs(100L);
        return stats;
    }
}
