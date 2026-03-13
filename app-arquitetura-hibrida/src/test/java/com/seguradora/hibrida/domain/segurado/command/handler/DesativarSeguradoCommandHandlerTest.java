package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.command.DesativarSeguradoCommand;
import com.seguradora.hibrida.domain.segurado.event.SeguradoCriadoEvent;
import com.seguradora.hibrida.eventstore.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link DesativarSeguradoCommandHandler}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DesativarSeguradoCommandHandler - Testes Unitários")
class DesativarSeguradoCommandHandlerTest {

    @Mock
    private EventStore eventStore;

    @InjectMocks
    private DesativarSeguradoCommandHandler handler;

    @BeforeEach
    void setUp() {
        // Setup comum para testes
    }

    @Test
    @DisplayName("Deve desativar segurado com sucesso")
    void shouldDeactivateSeguradoSuccessfully() {
        // Given
        String seguradoId = "SEG-001";
        DesativarSeguradoCommand command = DesativarSeguradoCommand.builder()
            .seguradoId(seguradoId)
            .motivo("Solicitação do cliente")
            .build();

        SeguradoCriadoEvent eventoCriacao = new SeguradoCriadoEvent(
            seguradoId, "12345678909", "João Silva", "joao@example.com",
            "11987654321", LocalDate.of(1990, 1, 15), null
        );

        when(eventStore.loadEvents(seguradoId))
            .thenReturn(List.of(eventoCriacao));

        // When
        CommandResult result = handler.handle(command);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(seguradoId);

        verify(eventStore).loadEvents(seguradoId);
        verify(eventStore).saveEvents(eq(seguradoId), anyList(), anyLong());
    }

    @Test
    @DisplayName("Deve retornar falha quando ocorre exceção")
    void shouldReturnFailureWhenExceptionOccurs() {
        // Given
        String seguradoId = "SEG-001";
        DesativarSeguradoCommand command = DesativarSeguradoCommand.builder()
            .seguradoId(seguradoId)
            .motivo("Motivo teste")
            .build();

        when(eventStore.loadEvents(seguradoId))
            .thenThrow(new RuntimeException("Erro ao carregar eventos"));

        // When
        CommandResult result = handler.handle(command);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isNotBlank();

        verify(eventStore, never()).saveEvents(anyString(), anyList(), anyLong());
    }

    @Test
    @DisplayName("Deve retornar tipo de comando correto")
    void shouldReturnCorrectCommandType() {
        // When
        Class<DesativarSeguradoCommand> commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(DesativarSeguradoCommand.class);
    }
}
