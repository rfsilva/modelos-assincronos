package com.seguradora.hibrida.command.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link CommandBusMetrics}.
 */
@DisplayName("CommandBusMetrics Tests")
class CommandBusMetricsTest {

    private SimpleMeterRegistry registry;
    private CommandBusMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new CommandBusMetrics(registry);
        metrics.initializeMetrics();
    }

    // =========================================================================
    // Registros no MeterRegistry
    // =========================================================================

    @Nested
    @DisplayName("Registros no MeterRegistry após inicialização")
    class RegistrosNoMeterRegistry {

        @Test
        @DisplayName("Deve registrar contador commandbus_commands_processed_total")
        void shouldRegisterCommandsProcessedCounter() {
            assertThat(registry.find("commandbus_commands_processed_total").counter()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar contador commandbus_commands_failed_total")
        void shouldRegisterCommandsFailedCounter() {
            assertThat(registry.find("commandbus_commands_failed_total").counter()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar contador commandbus_commands_timeout_total")
        void shouldRegisterCommandsTimeoutCounter() {
            assertThat(registry.find("commandbus_commands_timeout_total").counter()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar contador commandbus_commands_rejected_total")
        void shouldRegisterCommandsRejectedCounter() {
            assertThat(registry.find("commandbus_commands_rejected_total").counter()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar timer commandbus_execution_time")
        void shouldRegisterExecutionTimer() {
            assertThat(registry.find("commandbus_execution_time").timer()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar timer commandbus_validation_time")
        void shouldRegisterValidationTimer() {
            assertThat(registry.find("commandbus_validation_time").timer()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar gauge commandbus_handlers_registered")
        void shouldRegisterHandlersRegisteredGauge() {
            assertThat(registry.find("commandbus_handlers_registered").gauge()).isNotNull();
        }

        @Test
        @DisplayName("Deve registrar gauge commandbus_active_commands")
        void shouldRegisterActiveCommandsGauge() {
            assertThat(registry.find("commandbus_active_commands").gauge()).isNotNull();
        }
    }

    // =========================================================================
    // Incrementos de contadores
    // =========================================================================

    @Nested
    @DisplayName("Incrementos de contadores")
    class IncrementoDeContadores {

        @Test
        @DisplayName("incrementCommandsProcessed deve incrementar contador geral")
        void shouldIncrementGeneralProcessedCounter() {
            metrics.incrementCommandsProcessed();
            metrics.incrementCommandsProcessed();

            assertThat(registry.find("commandbus_commands_processed_total").counter().count())
                    .isEqualTo(2.0);
        }

        @Test
        @DisplayName("incrementCommandsProcessed(tipo) deve incrementar contador por tipo")
        void shouldIncrementProcessedCounterByType() {
            metrics.incrementCommandsProcessed("TestCommand");
            metrics.incrementCommandsProcessed("TestCommand");
            metrics.incrementCommandsProcessed("OutroCommand");

            assertThat(registry.find("commandbus_commands_processed_total")
                    .tag("command_type", "TestCommand").counter().count()).isEqualTo(2.0);
            assertThat(registry.find("commandbus_commands_processed_total")
                    .tag("command_type", "OutroCommand").counter().count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("incrementCommandsFailed deve incrementar contador geral")
        void shouldIncrementGeneralFailedCounter() {
            metrics.incrementCommandsFailed();

            assertThat(registry.find("commandbus_commands_failed_total").counter().count())
                    .isEqualTo(1.0);
        }

        @Test
        @DisplayName("incrementCommandsFailed(tipo, erro) deve incrementar por tipo e erro")
        void shouldIncrementFailedCounterByTypeAndError() {
            metrics.incrementCommandsFailed("TestCommand", "TIMEOUT");
            metrics.incrementCommandsFailed("TestCommand", "INVALID");

            assertThat(registry.find("commandbus_commands_failed_total")
                    .tag("command_type", "TestCommand").tag("error_type", "TIMEOUT")
                    .counter().count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("incrementCommandsTimeout deve incrementar contador geral")
        void shouldIncrementGeneralTimeoutCounter() {
            metrics.incrementCommandsTimeout();
            metrics.incrementCommandsTimeout();
            metrics.incrementCommandsTimeout();

            assertThat(registry.find("commandbus_commands_timeout_total").counter().count())
                    .isEqualTo(3.0);
        }

        @Test
        @DisplayName("incrementCommandsRejected deve incrementar contador geral")
        void shouldIncrementGeneralRejectedCounter() {
            metrics.incrementCommandsRejected();

            assertThat(registry.find("commandbus_commands_rejected_total").counter().count())
                    .isEqualTo(1.0);
        }
    }

    // =========================================================================
    // Timers
    // =========================================================================

    @Nested
    @DisplayName("Timers de execução e validação")
    class TimersTests {

        @Test
        @DisplayName("startExecutionTimer / stopExecutionTimer deve registrar duração")
        void shouldRecordExecutionDuration() {
            Timer.Sample sample = metrics.startExecutionTimer();
            sleep(30);
            metrics.stopExecutionTimer(sample);

            Timer timer = registry.find("commandbus_execution_time").timer();
            assertThat(timer.count()).isEqualTo(1);
            assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(0);
        }

        @Test
        @DisplayName("stopExecutionTimer(sample, tipo) deve registrar por tipo de comando")
        void shouldRecordExecutionDurationByCommandType() {
            Timer.Sample sample = metrics.startExecutionTimer();
            sleep(10);
            metrics.stopExecutionTimer(sample, "TestCommand");

            Timer timer = registry.find("commandbus_execution_time")
                    .tag("command_type", "TestCommand").timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("startValidationTimer / stopValidationTimer deve registrar duração")
        void shouldRecordValidationDuration() {
            Timer.Sample sample = metrics.startValidationTimer();
            sleep(10);
            metrics.stopValidationTimer(sample);

            Timer timer = registry.find("commandbus_validation_time").timer();
            assertThat(timer.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("recordExecutionTime deve registrar tempo customizado por tipo")
        void shouldRecordCustomExecutionTimeByType() {
            metrics.recordExecutionTime("TestCommand", 200L);

            Timer timer = registry.find("commandbus_execution_time")
                    .tag("command_type", "TestCommand").timer();
            assertThat(timer.count()).isEqualTo(1);
            assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(200.0);
        }
    }

    // =========================================================================
    // Gauges
    // =========================================================================

    @Nested
    @DisplayName("Gauges de handlers e comandos ativos")
    class GaugesTests {

        @Test
        @DisplayName("updateRegisteredHandlers deve atualizar o gauge")
        void shouldUpdateRegisteredHandlersGauge() {
            metrics.updateRegisteredHandlers(5);

            assertThat(metrics.getRegisteredHandlersCount()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Gauge de active commands deve incrementar no startExecutionTimer e decrementar no stop")
        void shouldIncrementActiveCommandsOnStartAndDecrementOnStop() {
            assertThat(metrics.getActiveCommandsCount()).isEqualTo(0.0);

            Timer.Sample sample = metrics.startExecutionTimer();
            assertThat(metrics.getActiveCommandsCount()).isEqualTo(1.0);

            metrics.stopExecutionTimer(sample);
            assertThat(metrics.getActiveCommandsCount()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Múltiplos starts devem acumular active commands")
        void multipleStartsShouldAccumulateActiveCommands() {
            Timer.Sample s1 = metrics.startExecutionTimer();
            Timer.Sample s2 = metrics.startExecutionTimer();
            assertThat(metrics.getActiveCommandsCount()).isEqualTo(2.0);

            metrics.stopExecutionTimer(s1);
            metrics.stopExecutionTimer(s2);
            assertThat(metrics.getActiveCommandsCount()).isEqualTo(0.0);
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
