package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.command.ReativarSeguradoCommand;
import com.seguradora.hibrida.domain.segurado.event.SeguradoCriadoEvent;
import com.seguradora.hibrida.domain.segurado.event.SeguradoDesativadoEvent;
import com.seguradora.hibrida.eventstore.EventStore;
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
 * Testes unitários para {@link ReativarSeguradoCommandHandler}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReativarSeguradoCommandHandler - Testes Unitários")
class ReativarSeguradoCommandHandlerTest {

    @Mock
    private EventStore eventStore;

    @InjectMocks
    private ReativarSeguradoCommandHandler handler;

    @Test
    @DisplayName("Deve reativar segurado com sucesso")
    void shouldReactivateSeguradoSuccessfully() {
        // Given
        String seguradoId = "SEG-001";
        ReativarSeguradoCommand command = ReativarSeguradoCommand.builder()
            .seguradoId(seguradoId)
            .motivo("Regularização de pendências")
            .build();

        SeguradoCriadoEvent eventoCriacao = new SeguradoCriadoEvent(
            seguradoId, "12345678909", "João Silva", "joao@example.com",
            "11987654321", LocalDate.of(1990, 1, 15), null
        );

        // Para reativar, o segurado precisa estar inativo
        SeguradoDesativadoEvent eventoDesativacao = new SeguradoDesativadoEvent(
            seguradoId, "Teste de desativação"
        );

        when(eventStore.loadEvents(seguradoId))
            .thenReturn(List.of(eventoCriacao, eventoDesativacao));

        doNothing().when(eventStore).saveEvents(anyString(), anyList(), anyLong());

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
        ReativarSeguradoCommand command = ReativarSeguradoCommand.builder()
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
        Class<ReativarSeguradoCommand> commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(ReativarSeguradoCommand.class);
    }
}
