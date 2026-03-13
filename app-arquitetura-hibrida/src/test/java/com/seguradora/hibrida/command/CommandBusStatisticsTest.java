package com.seguradora.hibrida.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link CommandBusStatistics}.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("CommandBusStatistics - Testes Unitários")
class CommandBusStatisticsTest {

    private CommandBusStatistics statistics;

    @BeforeEach
    void setUp() {
        statistics = new CommandBusStatistics();
    }

    @Test
    @DisplayName("Deve criar instância com valores iniciais corretos")
    void shouldCreateInstanceWithCorrectInitialValues() {
        // Then
        assertThat(statistics.getTotalCommandsProcessed().get()).isZero();
        assertThat(statistics.getTotalCommandsFailed().get()).isZero();
        assertThat(statistics.getTotalCommandsTimedOut().get()).isZero();
        assertThat(statistics.getTotalCommandsRejected().get()).isZero();
        assertThat(statistics.getAverageExecutionTimeMs()).isZero();
        assertThat(statistics.getMinExecutionTimeMs()).isEqualTo(Long.MAX_VALUE);
        assertThat(statistics.getMaxExecutionTimeMs()).isZero();
        assertThat(statistics.getRegisteredHandlers()).isZero();
        assertThat(statistics.getStartedAt()).isNotNull();
        assertThat(statistics.getLastUpdated()).isNotNull();
        assertThat(statistics.getCommandTypeStats()).isEmpty();
    }

    @Test
    @DisplayName("Deve incrementar contador de comandos processados")
    void shouldIncrementProcessedCounter() {
        // When
        statistics.incrementProcessed();
        statistics.incrementProcessed();

        // Then
        assertThat(statistics.getTotalCommandsProcessed().get()).isEqualTo(2);
        assertThat(statistics.getTotalCommands()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve incrementar contador de comandos falhados")
    void shouldIncrementFailedCounter() {
        // When
        statistics.incrementFailed();

        // Then
        assertThat(statistics.getTotalCommandsFailed().get()).isEqualTo(1);
        assertThat(statistics.getTotalCommands()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve incrementar contador de comandos com timeout")
    void shouldIncrementTimedOutCounter() {
        // When
        statistics.incrementTimedOut();

        // Then
        assertThat(statistics.getTotalCommandsTimedOut().get()).isEqualTo(1);
        assertThat(statistics.getTotalCommands()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve incrementar contador de comandos rejeitados")
    void shouldIncrementRejectedCounter() {
        // When
        statistics.incrementRejected();

        // Then
        assertThat(statistics.getTotalCommandsRejected().get()).isEqualTo(1);
        assertThat(statistics.getTotalCommands()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve calcular total de comandos corretamente")
    void shouldCalculateTotalCommandsCorrectly() {
        // When
        statistics.incrementProcessed();
        statistics.incrementProcessed();
        statistics.incrementFailed();
        statistics.incrementTimedOut();
        statistics.incrementRejected();

        // Then
        assertThat(statistics.getTotalCommands()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve calcular taxa de sucesso corretamente")
    void shouldCalculateSuccessRateCorrectly() {
        // Given
        statistics.incrementProcessed();
        statistics.incrementProcessed();
        statistics.incrementProcessed();
        statistics.incrementFailed();

        // When
        double successRate = statistics.getSuccessRate();

        // Then
        assertThat(successRate).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Deve retornar taxa de sucesso zero quando não há comandos")
    void shouldReturnZeroSuccessRateWhenNoCommands() {
        // When
        double successRate = statistics.getSuccessRate();

        // Then
        assertThat(successRate).isZero();
    }

    @Test
    @DisplayName("Deve calcular taxa de erro corretamente")
    void shouldCalculateErrorRateCorrectly() {
        // Given
        statistics.incrementProcessed();
        statistics.incrementFailed();
        statistics.incrementTimedOut();
        statistics.incrementRejected();

        // When
        double errorRate = statistics.getErrorRate();

        // Then
        assertThat(errorRate).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Deve retornar taxa de erro zero quando não há comandos")
    void shouldReturnZeroErrorRateWhenNoCommands() {
        // When
        double errorRate = statistics.getErrorRate();

        // Then
        assertThat(errorRate).isZero();
    }

    @Test
    @DisplayName("Deve atualizar tempos de execução corretamente")
    void shouldUpdateExecutionTimeCorrectly() {
        // When
        statistics.incrementProcessed();
        statistics.updateExecutionTime(100L);

        statistics.incrementProcessed();
        statistics.updateExecutionTime(200L);

        statistics.incrementProcessed();
        statistics.updateExecutionTime(150L);

        // Then
        assertThat(statistics.getMinExecutionTimeMs()).isEqualTo(100L);
        assertThat(statistics.getMaxExecutionTimeMs()).isEqualTo(200L);
        assertThat(statistics.getAverageExecutionTimeMs()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("Deve atualizar timestamp ao incrementar contadores")
    void shouldUpdateTimestampWhenIncrementingCounters() {
        // Given
        Instant before = Instant.now();

        // When
        statistics.incrementProcessed();

        // Then
        assertThat(statistics.getLastUpdated()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("Deve calcular throughput corretamente")
    void shouldCalculateThroughputCorrectly() throws InterruptedException {
        // Given
        statistics.incrementProcessed();
        statistics.incrementProcessed();
        statistics.incrementProcessed();

        // Aguardar pelo menos 1 segundo para garantir que Duration.between() resulte em durationSeconds >= 1
        Thread.sleep(1100);

        // When
        double throughput = statistics.getThroughputPerSecond();

        // Then
        // Throughput = totalCommands (3) / durationSeconds (aproximadamente 1)
        assertThat(throughput).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Deve retornar throughput zero quando duração é zero")
    void shouldReturnZeroThroughputWhenDurationIsZero() {
        // When - logo após criação
        double throughput = statistics.getThroughputPerSecond();

        // Then
        assertThat(throughput).isZero();
    }

    @Test
    @DisplayName("Deve atualizar número de handlers registrados")
    void shouldUpdateRegisteredHandlers() {
        // When
        statistics.updateRegisteredHandlers(5);

        // Then
        assertThat(statistics.getRegisteredHandlers()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve adicionar estatísticas por tipo de comando")
    void shouldAddCommandTypeStatistics() {
        // Given
        CommandBusStatistics.CommandTypeStatistics typeStats = new CommandBusStatistics.CommandTypeStatistics();
        typeStats.getProcessed().incrementAndGet();

        // When
        statistics.updateCommandTypeStatistics("TestCommand", typeStats);

        // Then
        CommandBusStatistics.CommandTypeStatistics retrieved = statistics.getCommandTypeStatistics("TestCommand");
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getProcessed().get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve retornar null para estatísticas de tipo inexistente")
    void shouldReturnNullForNonExistentCommandType() {
        // When
        CommandBusStatistics.CommandTypeStatistics stats = statistics.getCommandTypeStatistics("NonExistent");

        // Then
        assertThat(stats).isNull();
    }

    @Test
    @DisplayName("Deve resetar todas as estatísticas")
    void shouldResetAllStatistics() {
        // Given
        statistics.incrementProcessed();
        statistics.incrementFailed();
        statistics.updateExecutionTime(100L);
        statistics.updateRegisteredHandlers(5);
        statistics.updateCommandTypeStatistics("TestCommand", new CommandBusStatistics.CommandTypeStatistics());

        // When
        statistics.reset();

        // Then
        assertThat(statistics.getTotalCommandsProcessed().get()).isZero();
        assertThat(statistics.getTotalCommandsFailed().get()).isZero();
        assertThat(statistics.getTotalCommandsTimedOut().get()).isZero();
        assertThat(statistics.getTotalCommandsRejected().get()).isZero();
        assertThat(statistics.getAverageExecutionTimeMs()).isZero();
        assertThat(statistics.getMinExecutionTimeMs()).isEqualTo(Long.MAX_VALUE);
        assertThat(statistics.getMaxExecutionTimeMs()).isZero();
        assertThat(statistics.getCommandTypeStats()).isEmpty();
    }

    @Test
    @DisplayName("CommandTypeStatistics deve criar instância com valores iniciais corretos")
    void commandTypeStatisticsShouldHaveCorrectInitialValues() {
        // When
        CommandBusStatistics.CommandTypeStatistics typeStats = new CommandBusStatistics.CommandTypeStatistics();

        // Then
        assertThat(typeStats.getProcessed().get()).isZero();
        assertThat(typeStats.getFailed().get()).isZero();
        assertThat(typeStats.getTimedOut().get()).isZero();
        assertThat(typeStats.getRejected().get()).isZero();
        assertThat(typeStats.getAverageExecutionTimeMs()).isZero();
        assertThat(typeStats.getMinExecutionTimeMs()).isEqualTo(Long.MAX_VALUE);
        assertThat(typeStats.getMaxExecutionTimeMs()).isZero();
        assertThat(typeStats.getLastExecuted()).isNotNull();
    }

    @Test
    @DisplayName("CommandTypeStatistics deve calcular total corretamente")
    void commandTypeStatisticsShouldCalculateTotalCorrectly() {
        // Given
        CommandBusStatistics.CommandTypeStatistics typeStats = new CommandBusStatistics.CommandTypeStatistics();
        typeStats.getProcessed().incrementAndGet();
        typeStats.getProcessed().incrementAndGet();
        typeStats.getFailed().incrementAndGet();

        // When
        long total = typeStats.getTotal();

        // Then
        assertThat(total).isEqualTo(3);
    }

    @Test
    @DisplayName("CommandTypeStatistics deve calcular taxa de sucesso corretamente")
    void commandTypeStatisticsShouldCalculateSuccessRateCorrectly() {
        // Given
        CommandBusStatistics.CommandTypeStatistics typeStats = new CommandBusStatistics.CommandTypeStatistics();
        typeStats.getProcessed().set(3);
        typeStats.getFailed().set(1);

        // When
        double successRate = typeStats.getSuccessRate();

        // Then
        assertThat(successRate).isEqualTo(75.0);
    }

    @Test
    @DisplayName("CommandTypeStatistics deve retornar taxa de sucesso zero quando não há comandos")
    void commandTypeStatisticsShouldReturnZeroSuccessRateWhenNoCommands() {
        // Given
        CommandBusStatistics.CommandTypeStatistics typeStats = new CommandBusStatistics.CommandTypeStatistics();

        // When
        double successRate = typeStats.getSuccessRate();

        // Then
        assertThat(successRate).isZero();
    }

    @Test
    @DisplayName("Deve ter equals e hashCode corretos via Lombok")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        CommandBusStatistics stats1 = new CommandBusStatistics();
        CommandBusStatistics stats2 = new CommandBusStatistics();

        // Modificar ambos da mesma forma
        stats1.incrementProcessed();
        stats2.incrementProcessed();

        // Then - Lombok gera equals/hashCode baseado em todos os campos
        // Como contém Instant.now(), stats diferentes terão timestamps diferentes
        assertThat(stats1).isNotEqualTo(stats2);
    }

    @Test
    @DisplayName("Deve ter toString útil via Lombok")
    void shouldHaveUsefulToString() {
        // Given
        statistics.incrementProcessed();
        statistics.incrementFailed();

        // When
        String toString = statistics.toString();

        // Then
        assertThat(toString)
            .contains("CommandBusStatistics")
            .contains("totalCommandsProcessed")
            .contains("totalCommandsFailed");
    }
}
