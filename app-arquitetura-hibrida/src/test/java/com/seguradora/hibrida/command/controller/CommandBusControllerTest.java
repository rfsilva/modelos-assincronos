package com.seguradora.hibrida.command.controller;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandBusStatistics;
import com.seguradora.hibrida.command.CommandHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CommandBusController - Testes Unitários")
class CommandBusControllerTest {

    @Mock
    private CommandBus commandBus;

    @Mock
    private CommandHandlerRegistry handlerRegistry;

    @InjectMocks
    private CommandBusController controller;

    private CommandBusStatistics mockStatistics;

    @BeforeEach
    void setUp() {
        mockStatistics = mock(CommandBusStatistics.class);
        when(mockStatistics.getTotalCommands()).thenReturn(1000L);
        when(mockStatistics.getTotalCommandsProcessed()).thenReturn(new AtomicLong(950L));
        when(mockStatistics.getTotalCommandsFailed()).thenReturn(new AtomicLong(30L));
        when(mockStatistics.getTotalCommandsTimedOut()).thenReturn(new AtomicLong(10L));
        when(mockStatistics.getTotalCommandsRejected()).thenReturn(new AtomicLong(10L));
        when(mockStatistics.getSuccessRate()).thenReturn(95.0);
        when(mockStatistics.getErrorRate()).thenReturn(5.0);
        when(mockStatistics.getAverageExecutionTimeMs()).thenReturn(120.5);
        when(mockStatistics.getMinExecutionTimeMs()).thenReturn(10L);
        when(mockStatistics.getMaxExecutionTimeMs()).thenReturn(5000L);
        when(mockStatistics.getThroughputPerSecond()).thenReturn(25.5);
        when(mockStatistics.getRegisteredHandlers()).thenReturn(15);
        when(mockStatistics.getLastUpdated()).thenReturn(Instant.now());
        when(mockStatistics.getStartedAt()).thenReturn(Instant.now().minusSeconds(3600));
    }

    @Test
    @DisplayName("Deve retornar estatísticas com sucesso")
    void shouldReturnStatisticsSuccessfully() {
        // Given
        when(commandBus.getStatistics()).thenReturn(mockStatistics);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalCommands")).isEqualTo(1000L);
        assertThat(response.getBody().get("commandsProcessed")).isEqualTo(950L);
        assertThat(response.getBody().get("commandsFailed")).isEqualTo(30L);
        assertThat(response.getBody().get("successRate")).asString().matches("95[.,]00%");
        assertThat(response.getBody().get("errorRate")).asString().matches("5[.,]00%");

        verify(commandBus).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorre exceção ao obter estatísticas")
    void shouldReturn500WhenExceptionOccursGettingStatistics() {
        // Given
        when(commandBus.getStatistics()).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Database error");

        verify(commandBus).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar estatísticas por tipo com sucesso")
    void shouldReturnStatisticsByTypeSuccessfully() {
        // Given
        CommandBusStatistics.CommandTypeStatistics typeStats = mock(CommandBusStatistics.CommandTypeStatistics.class);
        when(typeStats.getTotal()).thenReturn(100L);
        when(typeStats.getProcessed()).thenReturn(new AtomicLong(95L));
        when(typeStats.getFailed()).thenReturn(new AtomicLong(3L));
        when(typeStats.getTimedOut()).thenReturn(new AtomicLong(1L));
        when(typeStats.getRejected()).thenReturn(new AtomicLong(1L));
        when(typeStats.getSuccessRate()).thenReturn(95.0);
        when(typeStats.getAverageExecutionTimeMs()).thenReturn(100.0);
        when(typeStats.getMinExecutionTimeMs()).thenReturn(50L);
        when(typeStats.getMaxExecutionTimeMs()).thenReturn(500L);
        when(typeStats.getLastExecuted()).thenReturn(Instant.now());

        Map<String, CommandBusStatistics.CommandTypeStatistics> typeStatsMap = new HashMap<>();
        typeStatsMap.put("CriarSeguradoCommand", typeStats);

        when(commandBus.getStatistics()).thenReturn(mockStatistics);
        when(mockStatistics.getCommandTypeStats()).thenReturn(typeStatsMap);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatisticsByType();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("CriarSeguradoCommand");

        @SuppressWarnings("unchecked")
        Map<String, Object> commandStats = (Map<String, Object>) response.getBody().get("CriarSeguradoCommand");
        assertThat(commandStats.get("total")).isEqualTo(100L);
        assertThat(commandStats.get("processed")).isEqualTo(95L);
        assertThat(commandStats.get("successRate")).asString().matches("95[.,]00%");

        verify(commandBus).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar handlers registrados com sucesso")
    void shouldReturnRegisteredHandlersSuccessfully() {
        // Given
        when(handlerRegistry.getHandlerCount()).thenReturn(15);
        when(handlerRegistry.getRegisteredCommandTypes()).thenReturn(Collections.emptySet());
        when(handlerRegistry.getDebugInfo()).thenReturn(Map.of(
                "CriarSeguradoCommand", "CriarSeguradoCommandHandler",
                "AtualizarSeguradoCommand", "AtualizarSeguradoCommandHandler"
        ));

        // When
        ResponseEntity<Map<String, Object>> response = controller.getRegisteredHandlers();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalHandlers")).isEqualTo(15);
        assertThat(response.getBody().get("registeredTypes")).isNotNull();

        verify(handlerRegistry).getHandlerCount();
        verify(handlerRegistry).getRegisteredCommandTypes();
        verify(handlerRegistry).getDebugInfo();
    }

    @Test
    @DisplayName("Deve verificar handler específico com sucesso")
    void shouldCheckHandlerSuccessfully() {
        // Given
        String commandType = "Command"; // Classe base para teste
        when(commandBus.hasHandler(any())).thenReturn(true);
        when(handlerRegistry.getDebugInfo()).thenReturn(Map.of("Command", "CommandHandler"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.checkHandler(commandType);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("commandType")).isEqualTo(commandType);
        assertThat(response.getBody().get("hasHandler")).isEqualTo(true);
    }

    @Test
    @DisplayName("Deve retornar 400 quando tipo de comando não é encontrado")
    void shouldReturn400WhenCommandTypeNotFound() {
        // Given
        String commandType = "NonExistentCommand";

        // When
        ResponseEntity<Map<String, Object>> response = controller.checkHandler(commandType);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).asString().contains("não encontrado");
    }

    @Test
    @DisplayName("Deve retornar health check com status UP")
    void shouldReturnHealthCheckWithStatusUp() {
        // Given
        when(commandBus.getStatistics()).thenReturn(mockStatistics);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getHealth();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("registeredHandlers")).isEqualTo(15);
        assertThat(response.getBody().get("totalCommands")).isEqualTo(1000L);

        verify(commandBus).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar health check com status DEGRADED quando taxa de erro é alta")
    void shouldReturnHealthCheckWithDegradedStatusWhenHighErrorRate() {
        // Given
        when(commandBus.getStatistics()).thenReturn(mockStatistics);
        when(mockStatistics.getErrorRate()).thenReturn(15.0); // > 10%

        // When
        ResponseEntity<Map<String, Object>> response = controller.getHealth();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("DEGRADED");
        assertThat(response.getBody().get("warning")).isEqualTo("Taxa de erro alta");

        verify(commandBus).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar health check com warning quando não há handlers")
    void shouldReturnHealthCheckWithWarningWhenNoHandlers() {
        // Given
        when(commandBus.getStatistics()).thenReturn(mockStatistics);
        when(mockStatistics.getRegisteredHandlers()).thenReturn(0);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getHealth();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("DEGRADED");
        assertThat(response.getBody().get("warning")).isEqualTo("Nenhum handler registrado");

        verify(commandBus).getStatistics();
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorre erro no health check")
    void shouldReturn500WhenHealthCheckFails() {
        // Given
        when(commandBus.getStatistics()).thenThrow(new RuntimeException("Health check error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.getHealth();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("DOWN");
        assertThat(response.getBody().get("error")).isEqualTo("Health check error");

        verify(commandBus).getStatistics();
    }

    @Test
    @DisplayName("Deve resetar estatísticas com sucesso")
    void shouldResetStatisticsSuccessfully() {
        // Given
        when(commandBus.getStatistics()).thenReturn(mockStatistics);
        doNothing().when(mockStatistics).reset();

        // When
        ResponseEntity<Map<String, Object>> response = controller.resetStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Estatísticas resetadas com sucesso");

        verify(commandBus).getStatistics();
        verify(mockStatistics).reset();
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorre erro ao resetar estatísticas")
    void shouldReturn500WhenErrorResettingStatistics() {
        // Given
        when(commandBus.getStatistics()).thenThrow(new RuntimeException("Reset error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.resetStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Reset error");

        verify(commandBus).getStatistics();
    }

    @Test
    @DisplayName("Deve formatar métricas de tempo corretamente")
    void shouldFormatTimeMetricsCorrectly() {
        // Given
        when(commandBus.getStatistics()).thenReturn(mockStatistics);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("averageExecutionTimeMs")).asString().matches("120[.,]50");
        assertThat(response.getBody().get("minExecutionTimeMs")).isEqualTo(10L);
        assertThat(response.getBody().get("maxExecutionTimeMs")).isEqualTo(5000L);
    }

    @Test
    @DisplayName("Deve incluir timestamp nas respostas")
    void shouldIncludeTimestampInResponses() {
        // Given
        when(commandBus.getStatistics()).thenReturn(mockStatistics);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("timestamp")).isInstanceOf(Instant.class);
    }
}
